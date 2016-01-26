package com.kakudalab.kokushiseiya.resleep;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

import java.util.List;

/**
 * スマホの起動を検知してサービスを起動させるクラス
 */
public class ProcessNotExistReceiver extends BroadcastReceiver{
    @Override
    public void onReceive(Context context, Intent intent){
        boolean isExist = false;

        /*
        if(intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)){
            //サービスの起動
            context.startService(new Intent(context, BandService.class));
        }
        */

        Log.d("AlarmReceiver", "Alarm Received! : " + intent.getIntExtra(Intent.EXTRA_ALARM_COUNT, 0));

        ActivityManager activityManager = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
        final List<ActivityManager.RunningAppProcessInfo> apps = activityManager.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo curr : apps){
            if (curr.processName.equals("com.kakudalab.kokushiseiya.resleep:bandServiceProcess")){
                isExist = true;
                break;
            }
        }
        if (!isExist){
            //BandServiceの作成と起動
            Intent bandIntent = new Intent(context, BandService.class);
            bandIntent.setAction("bandStreaming");
            bandIntent.putExtra("preference", true);
            context.startService(bandIntent);
        }

    }
}
