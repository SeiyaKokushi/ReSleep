package com.kakudalab.kokushiseiya.resleep;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.SimpleExpandableListAdapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by kokushiseiya on 15/12/30.
 */
public class SettingActivity extends AppCompatActivity {
    private final String KEY1 = "TITLE";
    private final String KEY2 = "SUMMARY";
    long activityStart, activityEnd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        Toolbar toolbar = (Toolbar) findViewById(R.id.setting_toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("ReSleep");
        toolbar.setSubtitle("Setting");

        toolbar.setNavigationIcon(R.drawable.back_icon);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }
        });

        setExpandableListView();
    }

    private void setExpandableListView(){
        int PARENT_DATA = 3;
        List<Map<String, String>> parentList = new ArrayList<Map<String, String>>();

        for (int i = 0; i < PARENT_DATA; i++){
            Map<String, String> parentData = new HashMap<String, String>();
            if (i == 0) {
                parentData.put(KEY1, "睡眠時間");
                parentList.add(parentData);
            } else if (i == 1) {
                parentData.put(KEY1, "起きる時間");
                parentList.add(parentData);
            } else if (i == 2) {
                parentData.put(KEY1, "ユーザ情報");
                parentList.add(parentData);
            }
        }

        int CHILD_DATA = 3;
        // 子要素全体用のリスト
        List<List<Map<String, String>>> allChildList = new ArrayList<List<Map<String, String>>>();

        // 子要素として表示する文字を生成
        for (int i = 0; i < PARENT_DATA; i++) {
            // 各グループ別のリスト項目用のリスト
            List<Map<String, String>> childList = new ArrayList<Map<String, String>>();

            // リスト項目用データ格納
            for (int j = 0; j < CHILD_DATA; j++) {
                if (i == 0 && j == 0) {
                    Map<String, String> childData = new HashMap<String, String>();
                    childData.put(KEY1, "睡眠時間設定");
                    childData.put(KEY2, "推奨値に設定されている睡眠時間をカスタマイズします");
                    // リストに文字を格納
                    childList.add(childData);
                } else if (i == 1 && j == 0) {
                    Map<String, String> childData = new HashMap<String, String>();
                    childData.put(KEY1, "起床時間設定");
                    childData.put(KEY2, "最初に設定した起床時間をカスタマイズします");
                    // リストに文字を格納
                    childList.add(childData);
                } else if (i == 2 && j == 0) {
                    Map<String, String> childData = new HashMap<String, String>();
                    childData.put(KEY1, "名前変更");
                    childData.put(KEY2, "最初に設定した名前を変更します");
                    // リストに文字を格納
                    childList.add(childData);
                } else if (i == 2 && j == 1) {
                    Map<String, String> childData = new HashMap<String, String>();
                    childData.put(KEY1, "性別変更");
                    childData.put(KEY2, "最初に設定した性別を変更します");
                    // リストに文字を格納
                    childList.add(childData);
                } else if (i == 2 && j == 2) {
                    Map<String, String> childData = new HashMap<String, String>();
                    childData.put(KEY1, "年齢変更");
                    childData.put(KEY2, "最初に設定した年齢を変更します");
                    // リストに文字を格納
                    childList.add(childData);
                }

            }
            // 子要素全体用のリストに各グループごとデータを格納
            allChildList.add(childList);
        }

        // アダプタを作る
        SimpleExpandableListAdapter adapter = new SimpleExpandableListAdapter(
                        this,
                        parentList,
                        android.R.layout.simple_expandable_list_item_1,
                        new String[] { KEY1 },
                        new int[] { android.R.id.text1, android.R.id.text2 },
                        allChildList,
                        android.R.layout.simple_expandable_list_item_2,
                        new String[] { KEY1, KEY2 },
                        new int[] { android.R.id.text1, android.R.id.text2 }
                );
        //生成した情報をセット
        ExpandableListView lv = (ExpandableListView)findViewById(R.id.settingExpandableListView);
        lv.setAdapter(adapter);


        // リスト項目がクリックされた時の処理
        lv.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View view,
                                        int groupPosition, int childPosition, long id) {
                ExpandableListAdapter adapter = parent.getExpandableListAdapter();
                // クリックされた場所の内容情報を取得
                @SuppressWarnings("unchecked")
                Map<String, String> item = (Map<String, String>) adapter.getChild(groupPosition, childPosition);
                // アラート表示
                if (item.get(KEY1).equals("睡眠時間設定")) {
                    Intent intent = new Intent(getApplicationContext(), SleepTimeSetActivity.class);
                    startActivity(intent);
                } else if (item.get(KEY1).equals("起床時間設定")) {
                    Intent intent = new Intent(getApplicationContext(), WakeTimeSetActivity.class);
                    startActivity(intent);
                } else if (item.get(KEY1).equals("名前変更")) {
                    Intent intent = new Intent(getApplicationContext(), NameSetActivity.class);
                    startActivity(intent);
                } else if (item.get(KEY1).equals("性別変更")) {
                    Intent intent = new Intent(getApplicationContext(), SexSetActivity.class);
                    startActivity(intent);
                } else if (item.get(KEY1).equals("年齢変更")) {
                    Intent intent = new Intent(getApplicationContext(), AgeSetActivity.class);
                    startActivity(intent);
                }

                    return false;
            }
        });

        lv.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View view,
                                        int groupPosition, long id) {
                ExpandableListAdapter adapter = parent.getExpandableListAdapter();

                return false;
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
