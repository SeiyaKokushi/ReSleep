package com.kakudalab.kokushiseiya.resleep;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TimePicker;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import greendao.Graph;
import greendao.GraphDao;
import greendao.User;
import greendao.UserDao;

/**
 * Created by kokushiseiya on 15/12/30.
 */
public class SleepTimeSetActivity extends AppCompatActivity{
    long activityStart, activityEnd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sleep_time_set);

        Toolbar toolbar = (Toolbar) findViewById(R.id.set_toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("ReSleep");
        toolbar.setSubtitle("睡眠時間設定");

        toolbar.setNavigationIcon(R.drawable.back_icon);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SettingActivity.class);
                startActivity(intent);
            }
        });

        final TimePicker timePicker = (TimePicker) findViewById(R.id.sleepTimePicker);
        timePicker.setIs24HourView(true);
        timePicker.setCurrentHour(loadUserInfo(7L));
        timePicker.setCurrentMinute(loadUserInfo(8L));


        Button button = (Button) findViewById(R.id.changeButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int hour = timePicker.getCurrentHour();
                int min = timePicker.getCurrentMinute();

                User userWakeHour = getUserDao(getApplicationContext()).loadByRowId(3L);
                int wakeHour = Integer.valueOf(userWakeHour.getText());

                User userWakeMin = getUserDao(getApplicationContext()).loadByRowId(4L);
                int wakeMin = Integer.valueOf(userWakeMin.getText());

                int sleepHour, sleepMin;

                if (min == 0) {
                    sleepMin = wakeMin;
                    sleepHour = wakeHour - hour;
                } else if (min > wakeMin) {
                    sleepMin = 60 - (min - wakeMin);
                    sleepHour = wakeHour - hour - 1;
                } else {
                    sleepMin = wakeMin - min;
                    sleepHour = wakeHour - hour;
                }

                if (sleepHour < wakeHour) {
                    sleepHour += 24;
                }


                insertUserInfo(String.valueOf(sleepHour), System.currentTimeMillis(), "SLEEP_HOUR", 5L);
                outputUserInfo(String.valueOf(sleepHour) + ", sleep_hour");
                insertUserInfo(String.valueOf(sleepMin), System.currentTimeMillis(), "SLEEP_MIN", 6L);
                outputUserInfo(String.valueOf(sleepMin) + ", sleep_min");
                insertUserInfo(String.valueOf(hour), System.currentTimeMillis(), "SLEEPTIME_HOUR", 7L);
                outputUserInfo(String.valueOf(hour) + ", sleeptime_hour");
                insertUserInfo(String.valueOf(min), System.currentTimeMillis(), "SLEEPTIME_MIN", 8L);
                outputUserInfo(String.valueOf(min) + ", sleeptime_min");

                setRange();

                Toast.makeText(getApplicationContext(), "睡眠時間を" + hour + "時間" + min + "分に設定しました", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setRange(){
        double[] minValues = new double[7];
        double[] maxValues = new double[7];
        //bath sleep-6~sleep-0.5
        //nap 12~15
        //tabacco wake~sleep-1
        //alcohol sleep-6~sleep-4
        //cafe wake~sleep-6
        //eat wake~sleep-3
        //exercise wake~sleep-4
        int wakeHour = loadUserInfo(3L);
        int wakeMin = loadUserInfo(4L);
        int sleepHour = loadUserInfo(5L);
        int sleepMin = loadUserInfo(6L);

        minValues[0] = (double)sleepHour + ((double)sleepMin / 60.0) - 6.0;
        minValues[1] = 12.0;
        minValues[2] = (double)wakeHour + ((double)wakeMin / 60.0);
        minValues[3] = (double)sleepHour + ((double)sleepMin / 60.0) - 6.0;
        minValues[4] = (double)wakeHour + ((double)wakeMin / 60.0);
        minValues[5] = (double)wakeHour + ((double)wakeMin / 60.0);
        minValues[6] = (double)wakeHour + ((double)wakeMin / 60.0);

        maxValues[0] = (double)sleepHour + ((double)sleepMin / 60.0) - 0.5;
        maxValues[1] = 15.0;
        maxValues[2] = (double)sleepHour + ((double)sleepMin / 60.0) - 1.0;
        maxValues[3] = (double)sleepHour + ((double)sleepMin / 60.0) - 4.0;
        maxValues[4] = (double)sleepHour + ((double)sleepMin / 60.0) - 6.0;
        maxValues[5] = (double)sleepHour + ((double)sleepMin / 60.0) - 3.0;
        maxValues[6] = (double)sleepHour + ((double)sleepMin / 60.0) - 4.0;


        //0:exercise 1:eat 2:cafe 3:alcohol 4:tabacco 5:sleep 6:bath

        insertGraphInfo(minValues[6], 0L);
        insertGraphInfo(maxValues[6], 6L);
        insertGraphInfo(minValues[5], 1L);
        insertGraphInfo(maxValues[5], 7L);
        insertGraphInfo(minValues[4], 2L);
        insertGraphInfo(maxValues[4], 8L);
        insertGraphInfo(minValues[3], 3L);
        insertGraphInfo(maxValues[3], 9L);
        insertGraphInfo(minValues[2], 4L);
        insertGraphInfo(maxValues[2], 10L);
        insertGraphInfo(minValues[1], 5L);
        insertGraphInfo(maxValues[1], 11L);
    }

    private void insertGraphInfo(double value, long id){
        Graph graph = new Graph();
        graph.setValue(value);
        graph.setId(id);
        getGraphDao(this).insertOrReplace(graph);
    }

    private static GraphDao getGraphDao(Context context){
        return ((ReSleepApplication)context.getApplicationContext()).getDaoSession().getGraphDao();
    }

    private int loadUserInfo(long id) {
        User user = getUserDao(this).loadByRowId(id);
        return Integer.valueOf(user.getText());
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
        return ((ReSleepApplication)context.getApplicationContext()).getDaoSession().getUserDao();
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
