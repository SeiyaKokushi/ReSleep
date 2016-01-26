package com.kakudalab.kokushiseiya.resleep;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import greendao.User;
import greendao.UserDao;

/**
 * Created by kokushiseiya on 15/12/30.
 */
public class NameSetActivity extends AppCompatActivity {
    long activityStart, activityEnd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_name_set);

        Toolbar toolbar = (Toolbar) findViewById(R.id.set_toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("ReSleep");
        toolbar.setSubtitle("名前変更");

        toolbar.setNavigationIcon(R.drawable.back_icon);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SettingActivity.class);
                startActivity(intent);
            }
        });

        final EditText nameText = (EditText) findViewById(R.id.nameText);
        nameText.setHint(loadUserInfo(0L));

        Button button = (Button) findViewById(R.id.changeButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                insertUserInfo(nameText.getText().toString(), System.currentTimeMillis(), "NAME", 0L);
                outputUserInfo(nameText.getText().toString() + ", 名前");

                Toast.makeText(getApplicationContext(), "名前を" + nameText.getText().toString() + "に変更しました", Toast.LENGTH_LONG).show();
            }
        });
    }

    private String loadUserInfo(long id) {
        User user = getUserDao(this).loadByRowId(id);
        return user.getText();
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
