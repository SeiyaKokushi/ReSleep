package com.kakudalab.kokushiseiya.resleep;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import greendao.DataFlag;
import greendao.DataFlagDao;
import greendao.Graph;
import greendao.GraphDao;
import greendao.Heart;
import greendao.HeartDao;
import greendao.Memo;
import greendao.MemoDao;
import greendao.Sensor;
import greendao.SensorDao;
import greendao.User;
import greendao.UserDao;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    HorizontalScrollView scrollView;
    long activityStart, activityEnd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //プリファレンスの準備
        SharedPreferences preference = getSharedPreferences("Preference Name", MODE_PRIVATE);
        SharedPreferences.Editor editor = preference.edit();

        if (preference.getBoolean("Launched", false) && preference.getBoolean("Wake", true) && preference.getBoolean("CantSleep", true)){
            //初回起動以外の処理

            editor.putLong("SleepTime", 0);
            editor.commit();

            boolean isExist = false;

            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            setTitle("ReSleep");
            setSupportActionBar(toolbar);


            FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getApplicationContext(), CurrentUserStateActivity.class);
                    startActivity(intent);
                }
            });

            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawer.setDrawerListener(toggle);
            toggle.syncState();

            NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
            navigationView.setNavigationItemSelectedListener(this);



            Intent bandIntent = new Intent(this, BandService.class);
            bandIntent.setAction("bandStreaming");
            bandIntent.putExtra("preference", true);
            this.startService(bandIntent);


            makeGraph();

            scroll();

            output();

        } else if (!preference.getBoolean("Launched", false)) {
            //初期起動時の処理
            //プリファレンスの書き変え
            editor.putBoolean("Launched", true);
            editor.commit();

            Intent intent = new Intent(this, UserInfoActivity.class);
            startActivity(intent);
            this.finish();
        } else if (!preference.getBoolean("CantSleep", true)) {
            Intent intent = new Intent(this, CantSleepActivity.class);
            startActivity(intent);
            this.finish();
        } else if (!preference.getBoolean("Wake", true)) {
            Intent intent = new Intent(this, WakeupActivity.class);
            startActivity(intent);
            this.finish();
        }
    }

    private void output(){
        String[] category = {"名前", "性別", "年齢", "wake_hour", "wake_min", "sleep_hour", "sleep_min", "sleeptime_hour", "sleeptime_min"};

        for (int i= 0; i < 9; i++) {
            String data = loadUserData((long)i);
            outputUserInfo(data + ", " + category[i]);
        }
    }

    private void insertSensorInfo(double data, long id){
        Sensor sensor = new Sensor();
        sensor.setId(id);
        sensor.setValue(data);
        getSensorDao(getApplicationContext()).insertOrReplace(sensor);
    }

    private static SensorDao getSensorDao(Context context){
        return ((ReSleepApplication)context.getApplicationContext()).getDaoSession().getSensorDao();
    }

    private void loadHeart(){
        FileOutput fileOutput = new FileOutput();
        Calendar calendar = Calendar.getInstance();
        List<Heart> list = getHeartDao(this).loadAll();
        for (Heart heart : list){
            double hz = heart.getHz();
            double pow = heart.getPow();
            calendar.setTimeInMillis(heart.getDate());
            CharSequence date = DateFormat.format("yyyy/MM/dd, E, kk:mm", calendar);
            double hr = heart.getHeatRate();
            fileOutput.writeFile(this, date + ", " + hz + ", " + pow + ", " + hr + "\n", "heart.txt");
        }

    }

    private static HeartDao getHeartDao(Context context){
        return ((ReSleepApplication)context.getApplicationContext()).getDaoSession().getHeartDao();
    }

    private void loadMemo(){
        FileOutput fileOutput = new FileOutput();

        Calendar calendar = Calendar.getInstance();
        List<Memo> list = getDao(this).loadAll();
        for (Memo memo : list){
            calendar.setTimeInMillis(memo.getDate());
            CharSequence date = DateFormat.format("yyyy/MM/dd, E, kk:mm", calendar);
            String text = memo.getText();

            fileOutput.writeFile(this, date + ", " + text + "\n", "data.txt");
        }

    }

    private static MemoDao getDao(Context context){
        return ((ReSleepApplication)context.getApplicationContext()).getDaoSession().getMemoDao();
    }


    private double loadGraphInfo(long id) {
        Graph graph = getGraphDao(this).loadByRowId(id);

        return graph.getValue();
    }

    private static GraphDao getGraphDao(Context context){
        return ((ReSleepApplication)context.getApplicationContext()).getDaoSession().getGraphDao();
    }


    private boolean loadFlagInfo(long id) {
        DataFlag flag = getFlagDao(this).loadByRowId(id);
        return flag.getIs();
    }

    private void insertFlagInfo(boolean is, long id){
        DataFlag flag = new DataFlag();
        flag.setId(id);
        flag.setIs(is);
        getFlagDao(getApplicationContext()).insertOrReplace(flag);
    }

    private static DataFlagDao getFlagDao(Context context) {
        return ((ReSleepApplication)context.getApplicationContext()).getDaoSession().getDataFlagDao();
    }

    private void makeGraph() {
        //グラフビューを作成
        GraphLayout graph = new GraphLayout(this, 0);

        //グラフの値
        List<Double> minValues = new ArrayList<>();
        List<Double> maxValues = new ArrayList<>();

        //グラフのxラベル
        List<String> yLabels = new ArrayList<>();

        minValues.add((double)loadUserInfo(5L) + ((double)loadUserInfo(6L) / 60.0) - 6.0);
        maxValues.add((double)loadUserInfo(5L) + ((double)loadUserInfo(6L) / 60.0) - 0.5);
        yLabels.add("");

        //データの用意します
        for(int i = 5; i >= 0 ; i--){
            minValues.add(loadGraphInfo((long)i));
            maxValues.add(loadGraphInfo((long)(i + 6)));
            yLabels.add("");
        }

        //値をグラフに渡す
        graph.setMinValues(minValues);
        graph.setMaxValues(maxValues);

        //xラベルをグラフに渡す
        graph.setYLabels(yLabels);

        //線の数をグラフに渡す
        graph.setXLineCount(checkSleepHour() - checkWakeHour());

        //始まりの時間をグラフに渡す
        graph.setWakeHour(checkWakeHour());

        //終わりの時間をグラフに渡す
        graph.setSleepHour(checkSleepHour());

        //睡眠時刻をグラフに渡す
        graph.setSleepTime(checkSleepHour() - 1, loadUserInfo(6L));

        //グラフを挿入
        //追加できるのはLinearLayoutのみ！
        ((LinearLayout)findViewById(R.id.graph)).addView(graph);
    }

    private void scroll(){
        scrollView = (HorizontalScrollView)findViewById(R.id.mainScrollView);

        scrollView.post(new Runnable() {
            public void run() {
                Calendar calendar = Calendar.getInstance();
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                if (hour <= checkWakeHour()){
                    hour += 24;
                }
                scrollView.scrollTo((hour - checkWakeHour()) * (1150 / (checkSleepHour() - checkWakeHour())), 0);
            }
        });
    }

    private void insertUserInfo(String txt, long date, String category, long id){
        User user = new User();
        user.setCategory(category);
        user.setDate(date);
        user.setText(txt);
        user.setId(id);
        getUserDao(this).insertOrReplace(user);
    }

    private int loadUserInfo(long id) {
        User user = getUserDao(this).loadByRowId(id);
        return Integer.valueOf(user.getText());
    }

    private String loadUserData(long id) {
        User user = getUserDao(this).loadByRowId(id);
        return user.getText();
    }

    private static UserDao getUserDao(Context context){
        return ((ReSleepApplication)context.getApplicationContext()).getDaoSession().getUserDao();
    }

    private int checkWakeHour(){
        int wakeHour;

        if (loadUserInfo(4L) == 0){
            wakeHour = loadUserInfo(3L) - 1;
        }else{
            wakeHour = loadUserInfo(3L);
        }

        return wakeHour;
    }

    private int checkSleepHour(){
        int sleepHour;

        if (loadUserInfo(6L) == 0){
            sleepHour = loadUserInfo(5L) + 1;
        }else{
            sleepHour = loadUserInfo(5L) + 1;
        }

        return sleepHour;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_exercise) {
            // Handle the camera action
            Intent intent = new Intent(this, ExerciseActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_eat) {
            Intent intent = new Intent(this, EatActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_bath) {
            Intent intent = new Intent(this, BathActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_cafe) {
            Intent intent = new Intent(this, CafeActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_alcohol) {
            Intent intent = new Intent(this, AlcoholActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_tabacco) {
            Intent intent = new Intent(this, TabaccoActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_nap) {
            Intent intent = new Intent(this, NapActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_sleep) {
            Intent intent = new Intent(this, GoToSleepActivity.class);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onResume() {
        Calendar calendar = Calendar.getInstance();
        activityStart = calendar.getTimeInMillis();

        super.onResume();
    }

    @Override
    public void onPause() {
        Calendar calendar = Calendar.getInstance();
        activityEnd = calendar.getTimeInMillis();

        long interactTime = activityEnd - activityStart;

        outputDetectedAction(interactTime + ", " + getClass().getSimpleName());

        super.onPause();
    }

    /**
     * interact時間をテキストファイルへの書き込みを行うメソッド
     * @param string 書き込む文字列
     */
    public void outputDetectedAction(String string){
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd/HH:mm:ss");
        String nowDate = dateFormat.format(calendar.getTime());
        FileOutput fileOutput = new FileOutput();
        fileOutput.writeFile(this, nowDate + ", " + string, "interact_time.txt");
    }

    /**
     * 検知した行動をテキストファイルへの書き込みを行うメソッド
     * @param string 書き込む文字列
     */
    public void outputUserInfo(String string){
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd/HH:mm:ss");
        String nowDate = dateFormat.format(calendar.getTime());
        FileOutput fileOutput = new FileOutput();
        fileOutput.writeFile(this, nowDate + ", " + string, "user_info.txt");
    }
}
