package com.kakudalab.kokushiseiya.resleep;

import com.microsoft.band.BandClient;

import java.util.Calendar;

/**
 * ユーザー情報に関するクラス
 */
public class UserInfo {
    private String userName; //名前
    private boolean userSex; //性別 true: male, false: female
    private int userHeight; //身長(cm)
    private int userWeight; //体重(kg)
    private int userAge; //年齢

    private Calendar birthDate = new Calendar() {
        @Override
        public void add(int field, int value) {

        }

        @Override
        protected void computeFields() {

        }

        @Override
        protected void computeTime() {

        }

        @Override
        public int getGreatestMinimum(int field) {
            return 0;
        }

        @Override
        public int getLeastMaximum(int field) {
            return 0;
        }

        @Override
        public int getMaximum(int field) {
            return 0;
        }

        @Override
        public int getMinimum(int field) {
            return 0;
        }

        @Override
        public void roll(int field, boolean increment) {

        }
    }; //誕生日

    public void setUserInfo(String userName, boolean userSex, int userHeight, int userWeight, int userAge){
        this.userName = userName;
        this.userSex = userSex;
        this.userHeight = userHeight;
        this.userWeight = userWeight;
        this.userAge = userAge;
    }

    public String getUserName(){
        return userName;
    }

    public String getUserSex(){
        if (userSex){
            return "male";
        }else{
            return "female";
        }
    }

    public int getUserHeight(){
        return userHeight;
    }

    public int getUserWeight(){
        return userWeight;
    }

    public int getUserAge(){
        return userAge;
    }

    public int getAge(Calendar today, int birthYear, int birthMonth, int birthDay) {
        birthDate.set(birthYear, birthMonth, birthDay);

        // 計算日の年と誕生日の年の差を算出
        int yearDiff = today.get(Calendar.YEAR) - birthDate.get(Calendar.YEAR);
        // ただし誕生月・日より年齢計算月日が前であれば年齢は1歳少ない
        if (today.get(Calendar.MONTH) < birthDate.get(Calendar.MONTH)) {
            yearDiff--;
        } else if (today.get(Calendar.MONTH) == birthDate.get(Calendar.MONTH) &&
                today.get(Calendar.DAY_OF_MONTH) < birthDate.get(Calendar.DAY_OF_MONTH)) {
            yearDiff--;
        }
        return yearDiff;
    }
}
