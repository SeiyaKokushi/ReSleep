package com.kakudalab.kokushiseiya.resleep;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import greendao.Graph;
import greendao.GraphDao;
import greendao.User;
import greendao.UserDao;

public class AlcoholActivity extends AppCompatActivity {
    double minValues;
    double maxValues;
    HorizontalScrollView scrollView;
    long activityStart, activityEnd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_alcohol);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("ReSleep");
        toolbar.setSubtitle("Alcohol");
        setSupportActionBar(toolbar);


        toolbar.setNavigationIcon(R.drawable.back_icon);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }
        });

        setRange();
        makeGraph();
        scroll();
    }

    private void scroll(){
        scrollView = (HorizontalScrollView)findViewById(R.id.horizontalScrollView2);

        scrollView.post(new Runnable() {
            public void run() {
                Calendar calendar = Calendar.getInstance();
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                if (hour <= checkWakeHour()) {
                    hour += 24;
                }
                scrollView.scrollTo((hour - checkWakeHour()) * (1500 / (checkSleepHour() - checkWakeHour())), 0);
            }
        });
    }

    private void setRange(){
    //bath sleep-6~sleep-0.5
        //nap 12~15
        //tabacco wake~sleep-1
        //alcohol sleep-6~sleep-4
        //cafe wake~sleep-6
        //eat wake~sleep-3
        //exercise wake~sleep-4

        minValues = loadGraphInfo(3L);

        maxValues = loadGraphInfo(9L);
    }

    private void makeGraph() {
        //グラフビューを作成
        GraphLayout graph = new GraphLayout(this, 4);

        //グラフの値
        List<Double> minValues = new ArrayList<>();
        List<Double> maxValues = new ArrayList<>();

        //グラフのxラベル
        List<String> yLabels = new ArrayList<>();

        //データの用意します
        minValues.add(this.minValues);
        maxValues.add(this.maxValues);
        yLabels.add("");

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

    private double loadGraphInfo(long id){
        Graph graph = getGraphDao(this).loadByRowId(id);

        return graph.getValue();
    }

    private static GraphDao getGraphDao(Context context){
        return ((ReSleepApplication)context.getApplicationContext()).getDaoSession().getGraphDao();
    }

    private int loadUserInfo(long id){
        User user = getUserDao(this).loadByRowId(id);

        return Integer.valueOf(user.getText());
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
}
