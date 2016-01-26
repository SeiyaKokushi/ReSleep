package com.kakudalab.kokushiseiya.resleep;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageButton;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import greendao.DecideAction;
import greendao.DecideActionDao;

public class ChooseActionActivity extends AppCompatActivity implements View.OnClickListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_action);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("ReSleep");
        toolbar.setSubtitle("どの活動を行っていましたか?");
        setSupportActionBar(toolbar);

        ImageButton exercise = (ImageButton) findViewById(R.id.exerciseButton);
        ImageButton eat = (ImageButton) findViewById(R.id.eatButton);
        ImageButton cafe = (ImageButton) findViewById(R.id.cafeButton);
        ImageButton alcohol = (ImageButton) findViewById(R.id.alcoholButton);
        ImageButton tabacco = (ImageButton) findViewById(R.id.tabaccoButton);
        ImageButton nap = (ImageButton) findViewById(R.id.napButton);
        ImageButton bath = (ImageButton) findViewById(R.id.bathButton);
        ImageButton nothing = (ImageButton) findViewById(R.id.nothingButton);

        exercise.setOnClickListener(this);
        eat.setOnClickListener(this);
        cafe.setOnClickListener(this);
        alcohol.setOnClickListener(this);
        tabacco.setOnClickListener(this);
        nap.setOnClickListener(this);
        bath.setOnClickListener(this);
        nothing.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        String action = "";

        if (view.getId() == R.id.exerciseButton) {
            action = "exercise";
        } else if (view.getId() == R.id.eatButton) {
            action = "eat";
        } else if (view.getId() == R.id.cafeButton) {
            action = "cafe";
        } else if (view.getId() == R.id.alcoholButton) {
            action = "alcohol";
        } else if (view.getId() == R.id.tabaccoButton) {
            action = "tabacco";
        } else if (view.getId() == R.id.napButton) {
            action = "nap";
        } else if (view.getId() == R.id.bathButton) {
            action = "nap";
        } else if (view.getId() == R.id.nothingButton) {
            action = "nothing";
        }

        outputDetectedAction(action + ", " + "true");

        this.finish();
    }

    /**
     * 検知した行動をテキストファイルへの書き込みを行うメソッド
     * @param string 書き込む文字列
     */
    public void outputDetectedAction(String string){
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd/HH:mm:ss");
        String nowDate = dateFormat.format(calendar.getTime());
        FileOutput fileOutput = new FileOutput();
        fileOutput.writeFile(this, nowDate + ", " + string, "detected_action.txt");
    }
}
