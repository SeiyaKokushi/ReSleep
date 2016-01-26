package com.kakudalab.kokushiseiya.resleep;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import greendao.DecideAction;
import greendao.DecideActionDao;
import greendao.Graph;
import greendao.GraphDao;
import greendao.Recommend;
import greendao.RecommendDao;
import greendao.User;
import greendao.UserDao;

/**
 * ダイアログを表示する表示するフラグメント
 */
public class AlertDialogFragment extends DialogFragment{
    private String message = "";
    private int id = 4;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("ReSleep");
        builder.setMessage(message);
        builder.setPositiveButton("◯", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (id >= 5 && id <= 10) {
                    //許容時間のロード
                    double max = loadGraphInfo(id + 1);
                    double min = loadGraphInfo(id - 5);

                    //現在時刻をdoubleで表現
                    Calendar currentCalendar = Calendar.getInstance();
                    double currentHour = (double)currentCalendar.get(Calendar.HOUR_OF_DAY);
                    double currentMin = (double)currentCalendar.get(Calendar.MINUTE) / 60.0;
                    double currentTime = currentHour + currentMin;

                    //新しいmaxとmin
                    double newMax = loadGraphInfo(id + 1);
                    double newMin = loadGraphInfo(id - 5);

                    if (currentTime <= max && currentTime >= min) {
                        //現在時刻が許容時間内の時
                        switch (id){
                            case 5: //exercise
                                //何もしない
                                break;

                            case 6: //eat
                                //現在時刻までminを縮小する
                                newMin = currentTime;
                                break;

                            case 7: //cafe
                                //maxを1時間縮める
                                newMax -= 1.0;
                                break;

                            case 8: //alcohol
                                //maxを0.5時間縮める
                                newMax -= 0.5;
                                break;

                            case 9: //tabacco
                                //maxを1.0時間縮める
                                newMax -= 1.0;
                                break;

                            case 10: //nap
                                //許容時間を0にする
                                newMax = min;
                                break;
                        }

                        if (newMax < newMin) {
                            //最大が最小より小さくなった時
                            if (id >= 7) {
                                newMax = min;
                                newMin = min;
                            } else if (id == 6) {
                                newMax = max;
                                newMin = max;
                            }
                        }

                        //新しい許容時間の設定;
                        insertGraphInfo(newMax, id + 1);
                        insertGraphInfo(newMin, id - 5);
                    } else {
                        //現在時刻が許容時間をオーバーした時
                        //それぞれのrecommendを見せる
                        switch (id){
                            case 5: //exercise
                                Intent exerciseIntent = new Intent(getContext(), ExerciseActivity.class);
                                startActivity(exerciseIntent);
                                break;

                            case 6: //eat
                                Intent eatIntent = new Intent(getContext(), EatActivity.class);
                                startActivity(eatIntent);
                                break;

                            case 7: //cafe
                                Intent cafeIntent = new Intent(getContext(), CafeActivity.class);
                                startActivity(cafeIntent);
                                break;

                            case 8: //alcohol
                                Intent alcoholIntent = new Intent(getContext(), AlcoholActivity.class);
                                startActivity(alcoholIntent);
                                break;

                            case 9: //tabacco
                                Intent tabaccoIntent = new Intent(getContext(), TabaccoActivity.class);
                                startActivity(tabaccoIntent);
                                break;

                            case 10: //nap
                                Intent napIntent = new Intent(getContext(), NapActivity.class);
                                startActivity(napIntent);
                                break;
                        }

                    }

                    String action = "" ;

                    switch (id){
                        case 5: //exercise
                            action = "exercise";
                            break;

                        case 6: //eat
                            action = "eat";
                            break;

                        case 7: //cafe
                            action = "cafe";
                            break;

                        case 8: //alcohol
                            action = "alcohol";
                            break;

                        case 9: //tabacco
                            action = "tabacco";
                            break;

                        case 10: //nap
                            action = "nap";
                            break;
                    }

                    outputDetectedAction(action + ", " + "true");
                } else {
                    outputRecommendEvaluation(message + ", " + "true");
                }
            }
        });

        builder.setNegativeButton("×", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (id >= 5 && id <= 10) {
                    String action = "";

                    switch (id){
                        case 5: //exercise
                            action = "exercise";
                            break;

                        case 6: //eat
                            action = "eat";
                            break;

                        case 7: //cafe
                            action = "cafe";
                            break;

                        case 8: //alcohol
                            action = "alcohol";
                            break;

                        case 9: //tabacco
                            action = "tabacco";
                            break;

                        case 10: //nap
                            action = "nap";
                            break;
                    }

                    outputDetectedAction(action + ", " + "false");

                    Intent intent = new Intent(getContext(), ChooseActionActivity.class);
                    startActivity(intent);
                } else {
                    outputRecommendEvaluation(message + ", " + "false");
                }
            }
        });

        Dialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(true);

        return dialog;
    }

    @Override
    public void onStop(){
        NotificationManager notificationManager = (NotificationManager)getActivity().getSystemService(Service.NOTIFICATION_SERVICE);
        notificationManager.cancel(id + 1);
        super.onStop();
        getActivity().finish();
    }

    public void setMessage(String m){
        message = m;
    }

    public void setId(int i) {
        id = i;
    }

    /**
     * レコメンドの評価をテキストファイルへの書き込みを行うメソッド
     * @param string 書き込む文字列
     */
    public void outputRecommendEvaluation(String string){
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd/HH:mm:ss");
        String nowDate = dateFormat.format(calendar.getTime());
        FileOutput fileOutput = new FileOutput();
        fileOutput.writeFile(getContext(), nowDate + ", " + string, "recommend_evaluation.txt");
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
        fileOutput.writeFile(getContext(), nowDate + ", " + string, "detected_action.txt");
    }

    private double loadGraphInfo(int id) {
        Graph graphMax = getGraphDao(getActivity()).loadByRowId((long)id);
        double max = graphMax.getValue();

        return max;
    }

    private void insertGraphInfo(double value, int id){
        Graph graph = new Graph();
        graph.setValue(value);
        graph.setId((long) id);
        getGraphDao(getActivity()).insertOrReplace(graph);
    }

    private static GraphDao getGraphDao(Context context){
        return ((ReSleepApplication)context.getApplicationContext()).getDaoSession().getGraphDao();
    }

    private void insertDecideActionInfo(boolean evaluation, String action, long date){
        DecideAction decideAction = new DecideAction();
        decideAction.setDate(date);
        decideAction.setAction(action);
        decideAction.setEvaluation(evaluation);
        getDecideActionDao(getActivity()).insertOrReplace(decideAction);
    }

    private static DecideActionDao getDecideActionDao(Context context){
        return ((ReSleepApplication)context.getApplicationContext()).getDaoSession().getDecideActionDao();
    }
}
