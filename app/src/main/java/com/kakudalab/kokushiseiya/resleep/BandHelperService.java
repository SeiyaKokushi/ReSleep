package com.kakudalab.kokushiseiya.resleep;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.microsoft.band.BandClient;
import com.microsoft.band.BandClientManager;
import com.microsoft.band.BandInfo;

/**
 * BandServiceと相互にbindし合うService
 */
public class BandHelperService extends Service{
    private static String TAG = "ReSleep";
    private final IBinder binder = new BandHelperBinder();
    private BandClient bandClient;


    /**
     * このサービス自身を返すクラス
     */
    public class BandHelperBinder extends Binder {
        public BandHelperService getService(){
            return BandHelperService.this;
        }
    }

    @Override
    public void onCreate(){
        if(bandClient == null){
            BandInfo[] devices = BandClientManager.getInstance().getPairedBands();
            if (devices.length == 0) {
                Log.d(TAG, "Band isn't paired with your phone.\n");
            }
            bandClient = BandClientManager.getInstance().create(getBaseContext(), devices[0]);
        }
    }

    @Override
    public IBinder onBind(Intent intent){
        Log.d(TAG, "onBind(BandHelperService)");

        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent){
        Log.d(TAG, "onUnBind(BandHelperService)");

        return true;
    }

    @Override
    public void onRebind(Intent intent){
        Log.d(TAG, "onRebind(BandHelperService)");
    }

    @Override
    public void onTaskRemoved(Intent rootIntent){
        Log.d(TAG, "onTaskRemoved(BandHelperService)");
    }

    @Override
    public  void onLowMemory(){
        Log.d(TAG, "onLowMemory(BandHelperService)");
    }

    @Override
    public void onTrimMemory(int level){
        Log.d(TAG, "onTrimMemory(BandHelperService)");
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig){
        Log.d(TAG, "onConfigurationChanged(BandHelperService)");
    }

    //メモリの使用が多くてOSがServiceを終了させた時に再起動させる
    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        //初回起動時のみの処理
        if(intent.getBooleanExtra("helperPreference", false)){
            Log.d(TAG, "onStart(BandHelperService)");
            //BandServiceの作成と起動、バインド
            startBandService(false, true);
        }

        return Service.START_NOT_STICKY;
    }


    //このServiceとBandServiceをバインド
    private ServiceConnection bandConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            //BandServiceが死んだら、BandServiceの作成と起動、バインド
            startBandService(false, true);
            Log.d(TAG, "onServiceDisconnected(BandHelperService)");
        }
    };

    /**
     * バンドの情報をセットするメソッド
     * @param client バンドの情報
     */
    public void setBandClient(BandClient client){
        if (client != null) {
            Log.d(TAG, "setBandClient(BandHelperService)");
            this.bandClient = client;
        }else{
            Log.d(TAG, "BandClient is null(BandHelperService)");
        }
    }

    /**
     * BandHelperServiceを起動するメソッド
     * @param isFirst 初回起動か
     * @param needStart startServiceが必要か
     */
    public void startBandService(boolean isFirst, boolean needStart){
        //BandHelperServiceの作成と起動、バインド
        Intent bandIntent = new Intent(this, BandService.class);
        bandIntent.setAction("bandStreaming");
        if (isFirst) {
            bandIntent.putExtra("preference", true);
        }else{
            bandIntent.putExtra("preference", false);
        }
        if (needStart) {
            startService(bandIntent);
        }
        bindService(bandIntent, bandConnection, BIND_AUTO_CREATE);
    }
}
