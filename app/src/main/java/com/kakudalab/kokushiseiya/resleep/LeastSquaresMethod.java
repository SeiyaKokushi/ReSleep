package com.kakudalab.kokushiseiya.resleep;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.ArrayList;

import greendao.Graph;
import greendao.GraphDao;
import greendao.User;
import greendao.UserDao;

/**
 * Created by kokushiseiya on 16/01/08.
 */
public class LeastSquaresMethod implements Runnable {
    // 定数定義
    static int N;  // データ数
    static ArrayList<Double> X;  // 測定データ x
    static ArrayList<Double> Y;  // 測定データ y

    Context context;

    // コンストラクタ
    LeastSquaresMethod(ArrayList<Double> x, ArrayList<Double> y, int size, Context context) {
        X = new ArrayList<>();
        Y = new ArrayList<>();
        X = x;
        Y = y;
        N = size;
        this.context = context;
    }

    // 最小二乗法
    @Override
    public void run() {
        try {
            sendBroadCast("STOP", 3);
            double achieveTime = calc();
            compare(achieveTime);
            sendBroadCast("OK", 3);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void compare(double achieveTime) {
        double min = loadGraphInfo(0L);
        double max = loadGraphInfo(6L);

        if (achieveTime == 0.0) {
            //何もしない
        } else if (achieveTime >= max) {
            //何もしない
        } else if (achieveTime < max && achieveTime >= min) {
            double newMax = achieveTime;
            insertGraphInfo(achieveTime, 6L);
        }
    }

    private double calc() {
        int i;
        double a0, a1;
        double A00, A01, A02, A11, A12;

        A00 = A01 = A02 = A11 = A12 = 0.0;


        for (i = 0; i < N; i++) {
            A00 += 1.0;
            A01 += X.get(i);
            A02 += Y.get(i);
            A11 += X.get(i) * X.get(i);
            A12 += X.get(i) * Y.get(i);
        }

        /*１次式の係数の計算*/
        a0 = (A02 * A11 - A01 * A12) / (A00 * A11 - A01 * A01);
        a1 = (A00 * A12 - A01 * A02) / (A00 * A11 - A01 * A01);

        Log.d("ReSleep", "a0 = " + a0 + ", " + "a1 = " + a1);

        double averageCal = 2102;

        double achieveTime;
        if (a1 != 0) {
            achieveTime = (averageCal - a0) / a1;
        } else {
            achieveTime = 0.0;
        }

        return achieveTime;
    }

    protected void sendBroadCast(String message, int id) {

        Intent broadcastIntent = new Intent();
        broadcastIntent.putExtra("MESSAGE", message);
        broadcastIntent.putExtra("ID", id);
        broadcastIntent.setAction("UPDATE_ACTION");
        context.sendBroadcast(broadcastIntent);

    }

    private int loadUserInfo(long id) {
        User user = getUserDao(context).loadByRowId(id);
        return Integer.valueOf(user.getText());
    }

    private void insertUserInfo(String txt, long date, String category, long id){
        User user = new User();
        user.setCategory(category);
        user.setDate(date);
        user.setText(txt);
        user.setId(id);
        getUserDao(context).insertOrReplace(user);
    }

    private static UserDao getUserDao(Context context){
        return ((ReSleepApplication)context.getApplicationContext()).getDaoSession().getUserDao();
    }

    private double loadGraphInfo(long id) {
        Graph graph = getGraphDao(context).loadByRowId(id);
        return graph.getValue();
    }

    private void insertGraphInfo(double value, long id){
        Graph graph = new Graph();
        graph.setValue(value);
        graph.setId(id);
        getGraphDao(context).insertOrReplace(graph);
    }

    private static GraphDao getGraphDao(Context context){
        return ((ReSleepApplication)context.getApplicationContext()).getDaoSession().getGraphDao();
    }
}
