package com.kakudalab.kokushiseiya.resleep;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import greendao.DataFlag;
import greendao.DataFlagDao;
import greendao.Sensor;
import greendao.SensorDao;
import greendao.Stanford;
import greendao.StanfordDao;

/**
 * Created by kokushiseiya on 15/12/30.
 */
public class StanfordActivity extends Activity {
    int evaluation = 0;
    long activityStart, activityEnd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stanford);

        RadioGroup radioGroup = (RadioGroup) findViewById(R.id.stanfordRadiogroup);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.radioButton) {
                    evaluation = 1;
                } else if (checkedId == R.id.radioButton2) {
                    evaluation = 2;
                } else if (checkedId == R.id.radioButton3) {
                    evaluation = 3;
                } else if (checkedId == R.id.radioButton4) {
                    evaluation = 4;
                } else if (checkedId == R.id.radioButton5) {
                    evaluation = 5;
                } else if (checkedId == R.id.radioButton6) {
                    evaluation = 6;
                } else if (checkedId == R.id.radioButton7) {
                    evaluation = 7;
                }
            }
        });

        Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (evaluation > 0) {
                    outputStanford(evaluation + "");
                    Intent intent = new Intent(getApplicationContext(), WakeupActivity.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(getApplicationContext(), "必ず評価を行ってください！", Toast.LENGTH_LONG).show();
                }
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

    /**
     * stanford評価をテキストファイルへの書き込みを行うメソッド
     * @param string 書き込む文字列
     */
    public void outputStanford(String string){
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd/HH:mm:ss");
        String nowDate = dateFormat.format(calendar.getTime());
        FileOutput fileOutput = new FileOutput();
        fileOutput.writeFile(this, nowDate + ", " + string, "stanford.txt");
    }
}
