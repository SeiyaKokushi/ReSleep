package com.kakudalab.kokushiseiya.resleep;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

/**
 * ダイアログを表示させるアクティビティ
 */
public class AlertDialogActivity extends FragmentActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        AlertDialogFragment fragment = new AlertDialogFragment();
        if(intent != null && intent.hasExtra("MESSAGE")){
            fragment.setMessage(intent.getStringExtra("MESSAGE"));
            fragment.setId(intent.getIntExtra("ID", 4));
        }
        fragment.show(getSupportFragmentManager(), "alertDialog");
    }

    @Override
    public void onDestroy() {
        NotificationManager notificationManager = (NotificationManager)this.getSystemService(Service.NOTIFICATION_SERVICE);
        Intent intent = getIntent();
        notificationManager.cancel(intent.getIntExtra("ID", 4) + 1);
        super.onDestroy();
    }
}
