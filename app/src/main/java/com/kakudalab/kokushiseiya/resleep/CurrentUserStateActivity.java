package com.kakudalab.kokushiseiya.resleep;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.microsoft.band.BandClient;
import com.microsoft.band.BandClientManager;
import com.microsoft.band.BandException;
import com.microsoft.band.BandInfo;
import com.microsoft.band.ConnectionState;
import com.microsoft.band.UserConsent;
import com.microsoft.band.sensors.BandCaloriesEvent;
import com.microsoft.band.sensors.BandCaloriesEventListener;
import com.microsoft.band.sensors.HeartRateConsentListener;

import java.text.DecimalFormat;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import greendao.DataFlag;
import greendao.DataFlagDao;
import greendao.Sensor;
import greendao.SensorDao;

/**
 * Created by kokushiseiya on 15/12/30.
 */
public class CurrentUserStateActivity extends AppCompatActivity {
    private TextView heart, temp, cal;
    private UpdateTextReceiver updateTextReceiver;
    private IntentFilter intentFilter;
    long activityStart, activityEnd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_user_state);

        Toolbar toolbar = (Toolbar) findViewById(R.id.userState_toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("ReSleep");
        toolbar.setSubtitle("ユーザーの状態");

        toolbar.setNavigationIcon(R.drawable.back_icon);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }
        });

        updateTextReceiver = new UpdateTextReceiver();
        intentFilter = new IntentFilter();
        intentFilter.addAction("UPDATE_ACTION");
        registerReceiver(updateTextReceiver, intentFilter);
        updateTextReceiver.registerHandler(updateTextHandler);

        heart = (TextView) findViewById(R.id.heart_text_view);
        temp = (TextView) findViewById(R.id.temp_text_view);
        cal = (TextView) findViewById(R.id.cal_text_view);

    }

    private Handler updateTextHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Bundle bundle = msg.getData();
            String message = bundle.getString("MESSAGE");
            int id = bundle.getInt("ID");

            if (id == 0) {
                heart.setText(message);
            } else if (id == 1) {
                temp.setText(message);
            } else if (id == 2) {
                cal.setText(message);
            }

        }
    };

    @Override
    public void onDestroy(){
        super.onDestroy();

        unregisterReceiver(updateTextReceiver);
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
}
