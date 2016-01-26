package com.kakudalab.kokushiseiya.resleep;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by kokushiseiya on 15/12/30.
 */
public class GoToSleepActivity extends AppCompatActivity {
    long activityStart, activityEnd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_go_to_sleep);

        Toolbar toolbar = (Toolbar) findViewById(R.id.goToSleep_toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("ReSleep");
        toolbar.setSubtitle("Go to Bed ?");

        toolbar.setNavigationIcon(R.drawable.back_icon);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }
        });

        Button button = (Button) findViewById(R.id.sleep_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), StanfordActivity.class);
                startActivity(intent);
            }
        });
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
