package com.kakudalab.kokushiseiya.resleep;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.microsoft.band.BandClient;
import com.microsoft.band.BandClientManager;
import com.microsoft.band.BandException;
import com.microsoft.band.BandIOException;
import com.microsoft.band.BandInfo;
import com.microsoft.band.ConnectionState;
import com.microsoft.band.UserConsent;
import com.microsoft.band.sensors.BandCaloriesEvent;
import com.microsoft.band.sensors.BandCaloriesEventListener;
import com.microsoft.band.sensors.BandContactEvent;
import com.microsoft.band.sensors.BandContactEventListener;
import com.microsoft.band.sensors.BandHeartRateEvent;
import com.microsoft.band.sensors.BandHeartRateEventListener;
import com.microsoft.band.sensors.BandSkinTemperatureEvent;
import com.microsoft.band.sensors.BandSkinTemperatureEventListener;
import com.microsoft.band.sensors.HeartRateConsentListener;


import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

import greendao.DataFlag;
import greendao.DataFlagDao;
import greendao.Sensor;
import greendao.SensorDao;
import greendao.User;
import greendao.UserDao;

/**
 * 心拍数と皮膚温度を常に測定するためのサービス
 */
public class BandService extends Service {
    private static String TAG = "ReSleep";
    private BandClient bandClient;
    private final IBinder binder = new BandBinder();
    private NotificationManager notificationManager;
    private Calendar calendar;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd/HH:mm:ss");
    private DialogActivity dialogActivity;
    private android.os.Handler analyzeHandler;
    private Handler leastSquaresMethodHandler;
    private HandlerThread analyzeHandlerThread;
    private HandlerThread leastSquaresMethodHandlerThread;
    private boolean bandContact = false;
    private double[] dataBox_h;
    private final int DATASIZE_H = 512;
    private int datasize_h = 0;
    private long preMillis_h = 0;
    private double[] dataBox_t;
    private final int DATASIZE_T = 16;
    private int datasize_t = 0;
    private ArrayList<Double> dataBox_c;
    private ArrayList<Double> timeBox_c;
    private int datasize_c = 0;
    private Handler handler;
    private long preCal;
    private UpdateTextReceiver updateTextReceiver;
    private IntentFilter intentFilter;
    private boolean isStop, isAnalyze;
    private int day = 0;
    private int calCounter = 0;

    /**
     * このサービス自身を返すクラス
     */
    public class BandBinder extends Binder{
        public BandService getService(){
            return BandService.this;
        }
    }

    @Override
    public void onCreate(){
        Log.d(TAG, "onCreate(BandService)");

        Intent alarmIntent = new Intent(this, ProcessNotExistReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(this, 0, alarmIntent, 0);

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        long firstTime = SystemClock.elapsedRealtime();
        firstTime += 15*1000;
        alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, firstTime, 15 * 1000, sender);

        //解析用Handlerを生成
        analyzeHandlerThread = new HandlerThread("analyzeLooper");
        analyzeHandlerThread.start();
        analyzeHandler = new android.os.Handler(analyzeHandlerThread.getLooper());
        leastSquaresMethodHandlerThread = new HandlerThread("leastSquaresMethodLooper");
        leastSquaresMethodHandlerThread.start();
        leastSquaresMethodHandler = new Handler(leastSquaresMethodHandlerThread.getLooper());

        updateTextReceiver = new UpdateTextReceiver();
        intentFilter = new IntentFilter();
        intentFilter.addAction("UPDATE_ACTION");
        registerReceiver(updateTextReceiver, intentFilter);
        updateTextReceiver.registerHandler(updateTextHandler);

        day = (int)loadUserInfo(10L);
        preCal = loadUserInfo(9L);

    }



    //このServiceとBandHelperServiceをバインド
    private ServiceConnection bandHelperConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            //BandHelperServiceが死んだら、BandHelperServiceの作成と起動、バインド
            startBandHelperService(false, true);
            Log.d(TAG, "onServiceDisconnected(BandService)");
        }
    };

    @Override
    public IBinder onBind(Intent intent){
        Log.d(TAG, "onBind(BandService)");

        dialogActivity = new DialogActivity();
        /*
        //DialogActivityに遷移
        Intent dialogIntent = new Intent(this, DialogActivity.class);
        startActivity(dialogIntent);
        */

        //バンドとコネクトする
        appTask task = new appTask();
        task.execute();

        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent){
        Log.d(TAG, "onUnbind(BandService)");

        //センサーのリスナーを解除
        //unsetSensorListener();

        //通知の非表示
        if(notificationManager != null) {
            notificationManager.cancel(0);
        }

        return true;
    }

    @Override
    public void onRebind(Intent intent){
        Log.d(TAG, "onRebind(BandService)");
    }

    @Override
    public void onTaskRemoved(Intent rootIntent){
        Log.d(TAG, "onTaskRemoved(BandService)");
    }

    @Override
    public  void onLowMemory(){
        Log.d(TAG, "onLowMemory(BandService)");
    }

    @Override
    public void onTrimMemory(int level){
        Log.d(TAG, "onTrimMemory(BandService)");
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig){
        Log.d(TAG, "onConfigurationChanged(BandService)");
    }

    //メモリの使用が多くてOSがServiceを終了させた時に再起動させる
    @Override
    public int onStartCommand(Intent intent, int flags, int startId){


        //初回起動時のみの処理
        if(intent.getBooleanExtra("preference", false)){
            Log.d(TAG, "onStart(BandService初回)");
            //BandHelperServiceの作成と起動、バインド
            startBandHelperService(true, true);
        } else {
            if (intent.getBooleanExtra("FROM", false)) {
                preCal = intent.getLongExtra("CALORY", 0);
                insertUserInfo(String.valueOf(preCal), System.currentTimeMillis(), "CALORY", 9L);
                day = intent.getIntExtra("DAY", 0);
                insertUserInfo(String.valueOf(day), System.currentTimeMillis(), "DAY", 10L);
                calCounter = 0;
                datasize_c = 0;
                insertUserInfo(String.valueOf(intent.getIntExtra("SLEEP_HOUR", 0)), System.currentTimeMillis(), "SLEEP_HOUR", 5L);
                insertUserInfo(String.valueOf(intent.getIntExtra("SLEEP_MIN", 0)), System.currentTimeMillis(), "SLEEP_MIN", 6L);
            }
            Log.d(TAG, "onStart(BandService)");
            setLocalNotification();
        }



        isAnalyze = true;
        isStop = false;

        return Service.START_NOT_STICKY;
    }

    //ユーザ操作によってServiceが終了させられた時に再起動させる
    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy(BandService)");

        //通知の非表示
        if(notificationManager != null) {
            notificationManager.cancel(0);
        }
        //unsetSensorListener();
        if (bandClient != null) {
            try {
                bandClient.disconnect().await();
            } catch (InterruptedException e) {
                // Do nothing as this is happening during destroy
            } catch (BandException e) {
                // Do nothing as this is happening during destroy
            }
        }

        unregisterReceiver(updateTextReceiver);

        super.onDestroy();
        startService(new Intent(this, BandService.class));
    }

    /**
     * 通知を設定して表示するメソッド
     */
    public void setNotify(){
        //通知の作成、設定
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext());
        builder.setContentTitle("Resleep");
        builder.setContentText("測定中");
        builder.setOngoing(true);
        builder.setSmallIcon(android.R.drawable.ic_dialog_info);
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        //通知の表示
        notificationManager.notify(0, builder.build());
        startForeground(0, builder.build());
    }

    /**
     * バンドの情報をセットするメソッド
     * @param client バンドの情報
     */
    public void setBandClient(BandClient client){
        if (client != null) {
            Log.d(TAG, "setBandClient(BandService)");
            bandClient = client;
        }else{
            Log.d(TAG, "BandClient is null(BandService)");
        }
    }

    public void setContactListener(){
        if (bandClient != null) {
            try {
                bandClient.getSensorManager().registerContactEventListener(bandContactEventListener);
            } catch (BandException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * センサーのリスナーをセットするメソッド
     */
    public void setSensorListener(){

        if (bandClient != null) {
            try {
                bandClient.getSensorManager().registerHeartRateEventListener(heartRateListener);
                bandClient.getSensorManager().registerSkinTemperatureEventListener(tempListener);
                bandClient.getSensorManager().registerCaloriesEventListener(bandCaloriesEventListener);
                Log.d(TAG, "setSensor(BandService)");


            } catch (BandException e) {
                e.printStackTrace();
            }
        }else {
            Log.d(TAG, "bandClient is null");
        }
    }

    /**
     * センサーのリスナーを解除するメソッド
     */
    public void unsetSensorListener(){
        if (bandClient != null) {
            try{
                bandClient.getSensorManager().unregisterHeartRateEventListeners();
                bandClient.getSensorManager().unregisterSkinTemperatureEventListeners();
                bandClient.getSensorManager().unregisterCaloriesEventListeners();
                if (notificationManager != null) {
                    notificationManager.cancel(0);
                }
                Log.d(TAG, "unsetSensor(BandService)");
            } catch (BandIOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 心拍数センサーのリスナー
     */
    private BandHeartRateEventListener heartRateListener = new BandHeartRateEventListener() {
        @Override
        public void onBandHeartRateChanged(final BandHeartRateEvent bandHeartRateEvent) {
            if (bandHeartRateEvent != null) {
                if (bandContact && isAnalyze) {
                    outputHeartData(String.format("HeartRate, %d", bandHeartRateEvent.getHeartRate()));
                    //insertMemo(Integer.toString(bandHeartRateEvent.getHeartRate()), System.currentTimeMillis());
                    Log.i(TAG, " HeartRate = " + bandHeartRateEvent.getHeartRate() + ", " + System.currentTimeMillis());
                    prepareAnalyze((double) bandHeartRateEvent.getHeartRate(), System.currentTimeMillis(), 0);
                    sendBroadCast(String.valueOf(bandHeartRateEvent.getHeartRate()), 0);
                }
            }
        }
    };

    /**
     * 皮膚温度センサーのリスナー
     */
    private BandSkinTemperatureEventListener tempListener = new BandSkinTemperatureEventListener() {
        @Override
        public void onBandSkinTemperatureChanged(final BandSkinTemperatureEvent bandSkinTemperatureEvent) {
            if (bandSkinTemperatureEvent != null){
                if (bandContact && isAnalyze) {
                    outputSkinData(String.format("SkinTemperature, %.3f", bandSkinTemperatureEvent.getTemperature()));
                    Log.i(TAG, " SkinTemperature = " + bandSkinTemperatureEvent.getTemperature() + ", " + System.currentTimeMillis());
                    prepareAnalyze(bandSkinTemperatureEvent.getTemperature(), System.currentTimeMillis(), 1);
                    DecimalFormat df1 = new DecimalFormat("0.0");
                    sendBroadCast(df1.format(bandSkinTemperatureEvent.getTemperature()), 1);
                }
            }
        }
    };

    /**
     * カロリーセンサーのリスナー
     */
    private BandCaloriesEventListener bandCaloriesEventListener = new BandCaloriesEventListener() {
        @Override
        public void onBandCaloriesChanged(BandCaloriesEvent bandCaloriesEvent) {
            if (bandCaloriesEvent != null) {
                if (bandContact) {
                    if (!isStop) {
                        outputCalorieData(String.format("Calorie, %d", bandCaloriesEvent.getCalories() - preCal));
                        sendBroadCast(String.valueOf(bandCaloriesEvent.getCalories() - preCal), 2);
                        Log.d(TAG, String.valueOf(bandCaloriesEvent.getCalories() - preCal));
                        if (datasize_c == 0) {
                            dataBox_c = new ArrayList<>();
                            timeBox_c = new ArrayList<>();
                        }
                        if (calCounter == 0) {
                            calCounter++;
                        }
                        if (calCounter % 60 == 0) {
                            prepareLeastSquaresMethod((double) bandCaloriesEvent.getCalories());
                        }
                    }
                }
            }
        }
    };

    public void registerHandler(Handler UpdateHandler) {
        handler = UpdateHandler;
    }

    private Handler updateTextHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Bundle bundle = msg.getData();
            String message = bundle.getString("MESSAGE");
            int id = bundle.getInt("ID");

            if (id == 3) {
                if (message != null) {
                    if (message.equals("STOP")) {
                        isStop = true;
                    } else {
                        isStop = false;
                    }
                }
            }

            if (id == 4) {
                if (message != null) {
                    if (message.equals("STOP")) {
                        isAnalyze = false;
                        datasize_h = 0;
                        dataBox_h = new double[DATASIZE_H];
                        long currentMillis = System.currentTimeMillis();
                        preMillis_h = currentMillis;
                        datasize_t = 0;
                        dataBox_t = new double[DATASIZE_T];
                    } else {
                        isAnalyze = true;
                    }
                }
            }

        }
    };

    protected void sendBroadCast(String message, int id) {

        Intent broadcastIntent = new Intent();
        broadcastIntent.putExtra("MESSAGE", message);
        broadcastIntent.putExtra("ID", id);
        broadcastIntent.setAction("UPDATE_ACTION");
        getBaseContext().sendBroadcast(broadcastIntent);

    }

    /**
     * バンド装着センサーのリスナー
     */
    private BandContactEventListener bandContactEventListener = new BandContactEventListener() {
        @Override
        public void onBandContactChanged(BandContactEvent bandContactEvent) {
            if (bandContactEvent != null){
                Log.i(TAG, " BandContact = " + bandContactEvent.getContactState().toString());
                if(bandContactEvent.getContactState().toString().equals("WORN")){
                    if (!bandContact) {
                        setSensorListener();
                        setNotify();
                    }
                    bandContact = true;
                }else{
                    if (bandContact){
                        if (dataBox_h != null && datasize_h > 1) {
                            startAnalyze(dataBox_h, datasize_h, dataBox_t, datasize_t);
                            Log.d(TAG, "startAnalyze(listener)");
                            outputHeartData("startAnalyze");
                            outputSkinData("startAnalyze");
                            dataBox_h = null;
                            datasize_h = 0;
                            dataBox_t = null;
                            datasize_t = 0;
                        }
                        unsetSensorListener();
                    }
                    bandContact = false;

                }
            }
        }
    };

    /**
     * MicrosoftBandが接続されているか
     * @return 接続されていたらtrue
     * @throws InterruptedException アプリケーションの例外処理
     * @throws BandException MicrosoftBandの例外処理
     */
    private boolean getConnectedBandClient() throws InterruptedException, BandException {
        if (bandClient == null) {
            BandInfo[] devices = BandClientManager.getInstance().getPairedBands();
            if (devices.length == 0) {
                appendToUI("Band isn't paired with your phone.\n");
                return false;
            }
            bandClient = BandClientManager.getInstance().create(getBaseContext(), devices[0]);
        } else if (ConnectionState.CONNECTED == bandClient.getConnectionState()) {
            return true;
        }

        appendToUI("Band is connecting...\n");
        return ConnectionState.CONNECTED == bandClient.connect().await();
    }

    public class DialogActivity extends Activity {

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
        }
    }

    /**
     * バンドとスマホをつなげるためのクラス
     */
    private class appTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            try {
                if (getConnectedBandClient()) {

                    appendToUI("Band is connected.\n");
                    if(bandClient.getSensorManager().getCurrentHeartRateConsent() != UserConsent.GRANTED) {

                        bandClient.getSensorManager().requestHeartRateConsent(dialogActivity, new HeartRateConsentListener() {
                            @Override
                            public void userAccepted(boolean consentGiven) {
                            }
                        });
                    }
                    setContactListener();

                } else {
                    appendToUI("Band isn't connected. Please make sure bluetooth is on and the band is in range.\n");
                }
            } catch (BandException e) {
                String exceptionMessage="";
                switch (e.getErrorType()) {
                    case UNSUPPORTED_SDK_VERSION_ERROR:
                        exceptionMessage = "Microsoft Health BandService doesn't support your SDK Version. Please update to latest SDK.\n";
                        break;
                    case SERVICE_ERROR:
                        exceptionMessage = "Microsoft Health BandService is not available. Please make sure Microsoft Health is installed and that you have the correct permissions.\n";
                        break;
                    default:
                        exceptionMessage = "Unknown error occured: " + e.getMessage() + "\n";
                        break;
                }
                appendToUI(exceptionMessage);

            } catch (Exception e) {
                appendToUI(e.getMessage());
            }
            return null;
        }
    }

    /**
     * 分析を開始するメソッド
     */
    public void startAnalyze(double[] data_h, int size_h, double[] data_t, int size_t){
        analyzeHandler.post(new Analyze(data_h, size_h, data_t, size_t, this));
    }

    /**
     * メッセージをログに追加するメソッド
     * @param message メッセージ
     */
    public void appendToUI(final String message) {
        Log.d(TAG, message);
    }

    /**
     * 心拍数情報をテキストファイルへの書き込みを行うメソッド
     * @param string 書き込む文字列
     */
    public void outputHeartData(String string){
        calendar = Calendar.getInstance();
        String nowDate = dateFormat.format(calendar.getTime());
        FileOutput fileOutput = new FileOutput();
        fileOutput.writeFile(this, nowDate + " " + string, "heart.txt");
    }

    /**
     * 皮膚温度情報をテキストファイルへの書き込みを行うメソッド
     * @param string 書き込む文字列
     */
    public void outputSkinData(String string){
        calendar = Calendar.getInstance();
        String nowDate = dateFormat.format(calendar.getTime());
        FileOutput fileOutput = new FileOutput();
        fileOutput.writeFile(this, nowDate + " " + string, "skin.txt");
    }

    /**
     * カロリー情報をテキストファイルへの書き込みを行うメソッド
     * @param string 書き込む文字列
     */
    public void outputCalorieData(String string){
        calendar = Calendar.getInstance();
        String nowDate = dateFormat.format(calendar.getTime());
        FileOutput fileOutput = new FileOutput();
        fileOutput.writeFile(this, nowDate + " " + string, "cal.txt");
    }

    /**
     * BandHelperServiceを起動するメソッド
     * @param isFirst 初回起動か
     * @param needStart startServiceが必要か
     */
    public void startBandHelperService(boolean isFirst, boolean needStart){
        //BandHelperServiceの作成と起動、バインド

        Intent bandHelperIntent = new Intent(this, BandHelperService.class);
        bandHelperIntent.setAction("bandHelp");
        if (isFirst) {
            bandHelperIntent.putExtra("helperPreference", true);
        }else{
            bandHelperIntent.putExtra("helperPreference", false);
        }
        if (needStart) {
            startService(bandHelperIntent);
        }
        bindService(bandHelperIntent, bandHelperConnection, BIND_AUTO_CREATE);
    }

    private long loadUserInfo(long id){
        User user = getUserDao(this).loadByRowId(id);
        return Long.valueOf(user.getText());
    }

    private void insertUserInfo(String txt, long date, String category, long id){
        User user = new User();
        user.setCategory(category);
        user.setDate(date);
        user.setText(txt);
        user.setId(id);
        getUserDao(this).insertOrReplace(user);
    }

    private static UserDao getUserDao(Context context){
        return ((ReSleepApplication) context.getApplicationContext()).getDaoSession().getUserDao();
    }

    /**
     * 解析を始めるまでのデータ整理を行うメソッド
     * @param data
     * @param currentMillis
     * @param id 0:HeartRate 1:Temperature
     */
    public void prepareAnalyze(double data, long currentMillis, int id){
        if (id == 0) {
            datasize_h++;
            if (currentMillis - preMillis_h > 2000 || datasize_h > DATASIZE_H) {
                if (dataBox_h != null && datasize_h > 1) {
                    startAnalyze(dataBox_h, datasize_h - 1, dataBox_t, datasize_t);
                    Log.d(TAG, "startAnalyze(prepareAnalyze)");
                    outputSkinData("startAnalyze");
                    outputHeartData("startAnalyze");
                }
                datasize_h = 1;
                dataBox_h = new double[DATASIZE_H];
                dataBox_h[datasize_h - 1] = data;
                preMillis_h = currentMillis;
                datasize_t = 0;
                if (dataBox_t == null) {
                    dataBox_t = new double[DATASIZE_T];
                }
            } else {
                dataBox_h[datasize_h - 1] = data;
                preMillis_h = currentMillis;
            }
        }else{
            datasize_t++;
            if (datasize_t > DATASIZE_T) {
                datasize_t = DATASIZE_T;
            } else {
                if (dataBox_t == null) {
                    dataBox_t = new double[DATASIZE_T];
                }
                dataBox_t[datasize_t - 1] = data;
            }
        }
    }

    public void prepareLeastSquaresMethod(double data) {
        dataBox_c.add(data);

        Calendar calendar = Calendar.getInstance();
        double hour = (double)calendar.get(Calendar.HOUR_OF_DAY);
        double min = ((double)calendar.get(Calendar.MINUTE)) / 60.0;
        double time = hour + min;
        timeBox_c.add(time);

        datasize_c++;

        if (datasize_c != 0 && datasize_c % 1800 == 0) {
            leastSquaresMethodHandler.post(new LeastSquaresMethod(dataBox_c, timeBox_c, datasize_c, getApplicationContext()));
            outputCalorieData("startAnalyze");
        }
    }

    public void setLocalNotification(){
        LocalNotification localNotification = new LocalNotification();
        Random rnd = new Random();
        Calendar calendar = Calendar.getInstance();
        int currentHour = calendar.get(Calendar.HOUR_OF_DAY);

        User userWakeHour = getUserDao(this).loadByRowId(3L);
        int wakeHour = Integer.valueOf(userWakeHour.getText());

        User userWakeMin = getUserDao(this).loadByRowId(4L);
        int wakeMin = Integer.valueOf(userWakeMin.getText());

        User userSleepHour = getUserDao(this).loadByRowId(5L);
        int sleepHour = Integer.valueOf(userSleepHour.getText());

        User userSleepMin = getUserDao(this).loadByRowId(6L);
        int sleepMin = Integer.valueOf(userSleepMin.getText());

        //朝の通知をセット
        //起床1時間後に通知
        if (currentHour <= wakeHour + 1) {
            localNotification.setLocalNotification(this, 0, wakeHour + 1, wakeMin, 0);
        }

        //日中の通知をセット
        //12時に通知
        if (currentHour <= 12) {
            localNotification.setLocalNotification(this, 0, 12, 0, 1);
        }

        //夜の通知をセット
        //就寝1時間前と就寝3時間前と就寝6時間前に通知
        if (currentHour < sleepHour - 6) {
            localNotification.setLocalNotification(this, 0, sleepHour - 6, sleepMin, 2);
        }
        if (currentHour < sleepHour - 3) {
            localNotification.setLocalNotification(this, 0, sleepHour - 3, sleepMin, 2);
        }
        if (currentHour < sleepHour - 1) {
            localNotification.setLocalNotification(this, 0, sleepHour - 1, sleepMin, 13);
        }

        //夜中の通知をセット
        //ユーザの目標としている就寝時間以降に起きていたら通知
        localNotification.setLocalNotification(this, 0, sleepHour, sleepMin, 3);

        //時間指定なしの通知をセット
        //朝と日中の通知の間と日中と夜の通知の間の2回通知
        if (12 - (wakeHour + 1) > 1){
            int dayTime = wakeHour + 2 + rnd.nextInt(12 - (wakeHour + 1) - 1);
            if (currentHour < dayTime) {
                localNotification.setLocalNotification(this, 0, dayTime, wakeMin, 4);
            }
        }

        if ((sleepHour - 6) - 12 > 1) {
            int dayTime2 = 13 + rnd.nextInt((sleepHour - 6) - 12 - 1);
            if (currentHour < dayTime2) {
                localNotification.setLocalNotification(this, 0, dayTime2, sleepMin, 4);
            }
        }

    }

}

