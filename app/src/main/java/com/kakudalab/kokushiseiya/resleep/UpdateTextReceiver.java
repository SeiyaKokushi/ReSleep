package com.kakudalab.kokushiseiya.resleep;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

/**
 * Created by kokushiseiya on 16/01/08.
 */
public class UpdateTextReceiver extends BroadcastReceiver {

    public static Handler handler;

    @Override
    public void onReceive(Context context, Intent intent) {

        Bundle bundle = intent.getExtras();
        String message = bundle.getString("MESSAGE");
        int id = bundle.getInt("ID");

        if(handler != null){
            Message msg = new Message();

            Bundle data = new Bundle();
            data.putString("MESSAGE", message);
            data.putInt("ID", id);
            msg.setData(data);
            handler.sendMessage(msg);
        }
    }

    /**
     * メイン画面の表示を更新
     */
    public void registerHandler(Handler locationUpdateHandler) {
        handler = locationUpdateHandler;
    }
}
