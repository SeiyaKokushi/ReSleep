package com.kakudalab.kokushiseiya.resleep;

import android.content.Context;
import android.text.format.DateFormat;
import android.util.Log;

import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import greendao.Graph;
import greendao.GraphDao;
import greendao.Heart;
import greendao.HeartDao;
import greendao.Memo;
import greendao.MemoDao;

/**
 * 分析に関するクラス
 */
public class Analyze implements Runnable{
    static final String TAG = "ReSleep";
    static final int DATASIZE_H = 512;
    static final int DATASIZE_T = 16;
    private double[] heartRateRRData = new double[DATASIZE_H];
    private double[] tempData = new double[DATASIZE_T];
    private int heartRateDataSize;
    private int tempDataSize;
    private double average_h, average_t;
    private double[] rowData_h = new double[DATASIZE_H];
    private double[] rowData_t = new double[DATASIZE_T];
    private Fft fft = new Fft();
    private Complex[] rowComplexData_h = new Complex[DATASIZE_H];
    private Complex[] rowComplexData_t = new Complex[DATASIZE_T];
    private double[] power_h = new double[DATASIZE_H];
    private double[] power_t = new double[DATASIZE_T];
    private double powerAverage_h;
    private double powerAverage_t;
    private double[] powerDeviation_h = new double[DATASIZE_H / 2 - 1];
    private double[] powerDeviation_t = new double[DATASIZE_T / 2 - 1];
    private double peakPow_h, peakPow_t;
    private double peakHz_h, peakHz_t;
    private HeartModelData exerciseModel_h, eatModel_h, napModel_h, alcoholModel_h, cafeModel_h, tabaccoModel_h;
    private TempModelData exerciseModel_t, eatModel_t, napModel_t, alcoholModel_t, cafeModel_t, tabaccoModel_t;
    private Context context;
    private ArrayList<Integer> heartResult = new ArrayList<>();
    private ArrayList<Integer> tempResult = new ArrayList<>();

    Analyze(double[] data_h, int size_h, double[] data_t, int size_t, Context context){
        this.context = context;

        exerciseModel_h = new HeartModelData("exercise");
        eatModel_h = new HeartModelData("eat");
        napModel_h = new HeartModelData("nap");
        alcoholModel_h = new HeartModelData("alcohol");
        cafeModel_h = new HeartModelData("cafe");
        tabaccoModel_h = new HeartModelData("tabacco");

        //心拍数から心拍間隔時間へ
        for (int i = 0; i < size_h; i++) {
            if (data_h[i] == 0.0) {
                heartRateRRData[i] = 0.0;
            } else {
                heartRateRRData[i] = 60.0 / data_h[i];
            }
        }
        heartRateDataSize = size_h;

        exerciseModel_t = new TempModelData("exercise");
        eatModel_t = new TempModelData("eat");
        napModel_t = new TempModelData("nap");
        alcoholModel_t = new TempModelData("alcohol");
        cafeModel_t = new TempModelData("cafe");
        tabaccoModel_t = new TempModelData("tabacco");

        for (int i = 0; i < size_t; i++) {
            if (data_t[i] == 0.0) {
                tempData[i] = 0.0;
            } else {
                tempData[i] = data_t[i];
            }
        }

        tempDataSize = size_t;
    }

    @Override
    public void run(){
        //0:exercise 1:eat 2:cafe 3:alcohol 4:tabacco 5:sleep 6:bath
        //平均値計算
        averageCalc();
        //データから平均値を引く
        rrMinusAverage();
        //FFTとパワースペクトル密度計算
        fftCalc();
        //printPower(id);
        calcPower();
        checkPeak();
        int act = compareToModel(0);
        actProcessor(act, 0);
    }

    public void output(int id) {
        //0:exercise 1:eat 2:cafe 3:alcohol 4:tabacco 5:sleep 6:bath

        String act = "";
        switch (id) {
            case 0:
                act = "exercise";
                break;

            case 1:
                act = "eat";
                break;

            case 2:
                act = "cafe";
                break;

            case 3:
                act = "alcohol";
                break;

            case 4:
                act = "tabacco";
                break;

            case 5:
                act = "nap";
                break;
        }

        outputHeartData(act);
        outputSkinData(act);
    }

    /**
     * 検知した行動の後処理
     * @param act
     */
    public void actProcessor(int act, int id) {
        if (id == 0) {
            if (act == -1) {
                //何も検知してない時
                //何もしない
            } else if (act == -2) {
                //検知したものが2つ以上の時
                //皮膚温度の検定
                int action = compareToModel(1);
                actProcessor(act, 1);
            } else if (act >= 0) {
                //検知したものが1つの時
                //0:exercise 1:eat 2:cafe 3:alcohol 4:tabacco 5:sleep 6:bath
                //行動の確認とグラフの変化と新しいモデルの作成
                LocalNotification localNotification = new LocalNotification();
                Calendar calendar = Calendar.getInstance();
                localNotification.setLocalNotification(context, 0, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), act + 5);
                output(act);
            }
        } else {
            //皮膚温度の検定による候補が2つ以上の時
            LocalNotification localNotification = new LocalNotification();
            Calendar calendar = Calendar.getInstance();
            if (act == -1) {
                //何も検知してない時
                localNotification.setLocalNotification(context, 0, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), heartResult.get(heartResult.size()-1) + 5);
                output(heartResult.get(heartResult.size()-1));
            } else if (act == -2 || act >= 0) {
                //検知したものが2つ以上の時
                boolean isDetect = false;

                for (int i = 0; i < heartResult.size(); i++) {
                    if (heartResult.get(i) == act) {
                        localNotification.setLocalNotification(context, 0, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), act + 5);
                        isDetect = true;
                        output(act);
                    }
                }

                if (!isDetect) {
                    localNotification.setLocalNotification(context, 0, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), heartResult.get(heartResult.size()-1) + 5);
                    output(heartResult.get(heartResult.size()-1));
                }
            }
        }
    }

    /**
     * 平均値計算
     */
    public void averageCalc(){
        double sum = 0.0;
        for (int i = 0; i < heartRateDataSize; i++) {
            sum += heartRateRRData[i];
        }

        average_h = sum / (double) heartRateDataSize;

        sum = 0.0;
        for (int i = 0; i < tempDataSize; i++){
            sum += tempData[i];
        }

        average_t = sum / (double) tempDataSize;

    }

    /**
     * データから直線的成分を引く
     */
    public void rrMinusAverage(){
        for (int i = 0; i < heartRateDataSize; i++) {
            rowData_h[i] = heartRateRRData[i] - average_h;
        }

        if (heartRateDataSize < DATASIZE_H) {
            for (int i = heartRateDataSize; i < DATASIZE_H; i++) {
                rowData_h[i] = 0.0;
            }
        }


        for (int i = 0; i < tempDataSize; i++) {
            rowData_t[i] = tempData[i] - average_t;
        }

        if (tempDataSize < DATASIZE_T) {
            for (int i = tempDataSize; i < DATASIZE_T; i++) {
                rowData_t[i] = 0.0;
            }
        }
    }

    /**
     * fftしてパワースペクトル密度計算
     */
    public void fftCalc(){
        Complex[] spectrum_h = new Complex[DATASIZE_H];
        for (int i = 0; i < DATASIZE_H; i++) {
            rowComplexData_h[i] = new Complex(rowData_h[i], 0.0);
        }
        spectrum_h = fft.fft(rowComplexData_h);
        power_h = fft.makepower(spectrum_h);

        for (int i = 0; i < DATASIZE_H; i++) {
            outputHeartData(String.format("power, %.3f", power_h[i]));
        }

        Complex[] spectrum_t = new Complex[DATASIZE_T];
        for (int i = 0; i < DATASIZE_T; i++) {
            rowComplexData_t[i] = new Complex(rowData_t[i], 0.0);
        }
        spectrum_t = fft.fft(rowComplexData_t);
        power_t = fft.makepower(spectrum_t);

        for (int i = 0; i < DATASIZE_T; i++) {
            outputSkinData(String.format("power, %.3f", power_t[i]));
        }
    }

    public void printPower(){
        for (int i = 0; i < DATASIZE_H; i++) {
            System.out.println((double) i / (double) DATASIZE_H + ", " + power_h[i]);
        }
        Log.d(TAG, "finish");


        for (int i = 0; i < DATASIZE_T; i++) {
            System.out.println((double) i / (double) DATASIZE_T + ", " + power_t[i]);
        }
        Log.d(TAG, "finish");
    }

    public void calcPower(){
        powerAverage_h = 0.0;
        for (int i = 0; i < DATASIZE_H / 2 - 1; i++) {
            powerAverage_h += power_h[i];
        }
        powerAverage_h = powerAverage_h / (double)(DATASIZE_H / 2 - 1);

        for (int i = 0; i < DATASIZE_H / 2 - 1; i++) {
            powerDeviation_h[i] = power_h[i] - powerAverage_h;
        }


        powerAverage_t = 0.0;
        for (int i = 0; i < DATASIZE_T / 2 - 1; i++) {
            powerAverage_t += power_t[i];
        }
        powerAverage_t = powerAverage_t / (double)(DATASIZE_T / 2 - 1);

        for (int i = 0; i < DATASIZE_T / 2 - 1; i++) {
            powerDeviation_t[i] = power_t[i] - powerAverage_t;
        }
    }

    public void checkPeak() {

        double maxPeak_h = power_h[0];
        double maxHz_h = 0.0;
        for (int i = 0; i < DATASIZE_H / 2 - 1; i++) {
            if (power_h[i] > maxPeak_h) {
                maxPeak_h = power_h[i];
                maxHz_h = (double)i / (double)DATASIZE_H;
            }
        }
        peakPow_h = maxPeak_h;
        peakHz_h = maxHz_h;


        double maxPeak_t = power_t[0];
        double maxHz_t = 0.0;
        for (int i = 0; i < DATASIZE_T / 2 - 1; i++) {
            if (power_t[i] > maxPeak_t) {
                maxPeak_t = power_t[i];
                maxHz_t = (double)i * (1.0 / 30.0) / (double)DATASIZE_T;
            }
        }
        peakPow_t = maxPeak_t;
        peakHz_t = maxHz_t;

    }

    public int compareToModel(int id){
        if (id == 0) {
            double relation = 0.0;
            double[] result_relation = new double[6]; //0:exercise 1:eat 2:cafe 3:alcohol 4:tabacco 5:sleep 6:bath
            boolean[] result_peak = new boolean[6];
            ArrayList<Integer> result = new ArrayList<>();

            if (powerAverage_h <= eatModel_h.getPowerRangeMax_h() && powerAverage_h >= eatModel_h.getPowerRangeMin_h()) {
                //eatとの比較
                relation = calcRelation(eatModel_h.getDeviation_h(), eatModel_h.getStandardDeviation_h(), 0);
                result_relation[1] = relation;

                if (peakPow_h >= eatModel_h.getPeakPowerMin_h() && peakPow_h <= eatModel_h.getPeakPowerMax_h()) {
                    result_peak[1] = true;
                }

            }


            if (powerAverage_h <= cafeModel_h.getPowerRangeMax_h() && powerAverage_h >= cafeModel_h.getPowerRangeMin_h()) {
                //cafeとの比較
                relation = calcRelation(cafeModel_h.getDeviation_h(), cafeModel_h.getStandardDeviation_h(), 0);
                result_relation[2] = relation;

                if (peakPow_h >= cafeModel_h.getPeakPowerMin_h() && peakPow_h <= cafeModel_h.getPeakPowerMax_h()) {
                    result_peak[2] = true;
                }


            }

            if (powerAverage_h <= tabaccoModel_h.getPowerRangeMax_h() && powerAverage_h >= tabaccoModel_h.getPowerRangeMin_h()) {
                //tabaccoとの比較
                relation = calcRelation(tabaccoModel_h.getDeviation_h(), tabaccoModel_h.getStandardDeviation_h(), 0);
                result_relation[4] = relation;

                if (peakPow_h >= tabaccoModel_h.getPeakPowerMin_h() && peakPow_h <= tabaccoModel_h.getPeakPowerMax_h()) {
                    result_peak[4] = true;
                }


            }

            if (powerAverage_h <= napModel_h.getPowerRangeMax_h() && powerAverage_h >= napModel_h.getPowerRangeMin_h()) {
                //napとの比較
                relation = calcRelation(napModel_h.getDeviation_h(), napModel_h.getStandardDeviation_h(), 0);
                result_relation[5] = relation;

                if (peakPow_h >= napModel_h.getPeakPowerMin_h() && peakPow_h <= napModel_h.getPeakPowerMax_h()) {
                    result_peak[5] = true;
                }


            }

            if (powerAverage_h <= alcoholModel_h.getPowerRangeMax_h() && powerAverage_h >= alcoholModel_h.getPowerRangeMin_h()) {
                //alcoholとの比較
                relation = calcRelation(alcoholModel_h.getDeviation_h(), alcoholModel_h.getStandardDeviation_h(), 0);
                result_relation[3] = relation;

                if (peakPow_h >= alcoholModel_h.getPeakPowerMin_h() && peakPow_h <= alcoholModel_h.getPeakPowerMax_h()) {
                    result_peak[3] = true;
                }

            }

            if (powerAverage_h <= exerciseModel_h.getPowerRangeMax_h() && powerAverage_h >= exerciseModel_h.getPowerRangeMin_h()) {
                //exerciseとの比較
                relation = calcRelation(exerciseModel_h.getDeviation_h(), exerciseModel_h.getStandardDeviation_h(), 0);
                result_relation[0] = relation;

                if (peakPow_h >= exerciseModel_h.getPeakPowerMin_h() && peakPow_h <= exerciseModel_h.getPeakPowerMax_h()) {
                    result_peak[0] = true;
                }
            }

            //0:exercise 1:eat 2:cafe 3:alcohol 4:tabacco 5:sleep 6:bath

            result = decideAct(result_relation, result_peak);

            if (result.size() == 1) {
                if (result.get(0) == -1) {
                    return -1;
                } else {
                    return result.get(0);
                }
            } else {
                heartResult = result;
                return -2;
            }
        } else {

            double relation = 0.0;
            double[] result_relation = new double[6]; //0:exercise 1:eat 2:cafe 3:alcohol 4:tabacco 5:sleep 6:bath
            boolean[] result_peak = new boolean[6];
            ArrayList<Integer> result = new ArrayList<>();

            if (powerAverage_t <= eatModel_t.getPowerRangeMax_t() && powerAverage_t >= eatModel_t.getPowerRangeMin_t()) {
                //eatとの比較

                relation = calcRelation(eatModel_t.getDeviation_t(), eatModel_t.getStandardDeviation_t(), 1);
                result_relation[1] = relation;

                if (peakPow_t >= eatModel_t.getPeakPowerMin_t() && peakPow_t <= eatModel_t.getPeakPowerMax_t()) {
                    result_peak[1] = true;
                }


            }

            if (powerAverage_t <= tabaccoModel_t.getPowerRangeMax_t() && powerAverage_t >= tabaccoModel_t.getPowerRangeMin_t()) {
                //tabaccoとの比較

                relation = calcRelation(tabaccoModel_t.getDeviation_t(), tabaccoModel_t.getStandardDeviation_t(), 1);
                result_relation[4] = relation;

                if (peakPow_t >= tabaccoModel_t.getPeakPowerMin_t() && peakPow_t <= tabaccoModel_t.getPeakPowerMax_t()) {
                    result_peak[4] = true;
                }


            }

            if (powerAverage_t <= alcoholModel_t.getPowerRangeMax_t() && powerAverage_t >= alcoholModel_t.getPowerRangeMin_t()) {
                //alcoholとの比較

                relation = calcRelation(alcoholModel_t.getDeviation_t(), alcoholModel_t.getStandardDeviation_t(), 1);
                result_relation[3] = relation;

                if (peakPow_t >= alcoholModel_t.getPeakPowerMin_t() && peakPow_t <= alcoholModel_t.getPeakPowerMax_t()) {
                    result_peak[3] = true;
                }


            }

            if (powerAverage_t <= napModel_t.getPowerRangeMax_t() && powerAverage_t >= napModel_t.getPowerRangeMin_t()) {
                //napとの比較

                relation = calcRelation(napModel_t.getDeviation_t(), napModel_t.getStandardDeviation_t(), 1);
                result_relation[5] = relation;

                if (peakPow_t >= napModel_t.getPeakPowerMin_t() && peakPow_t <= napModel_t.getPeakPowerMax_t()) {
                    result_peak[5] = true;
                }

            }

            if (powerAverage_t <= cafeModel_t.getPowerRangeMax_t() && powerAverage_t >= cafeModel_t.getPowerRangeMin_t()) {
                //cafeとの比較

                relation = calcRelation(cafeModel_t.getDeviation_t(), cafeModel_t.getStandardDeviation_t(), 1);
                result_relation[2] = relation;

                if (peakPow_t >= cafeModel_t.getPeakPowerMin_t() && peakPow_t <= cafeModel_t.getPeakPowerMax_t()) {
                    result_peak[2] = true;
                }

            }

            if (powerAverage_t <= exerciseModel_t.getPowerRangeMax_t() && powerAverage_t >= exerciseModel_t.getPowerRangeMin_t()) {
                //exerciseとの比較

                relation = calcRelation(exerciseModel_t.getDeviation_t(), exerciseModel_t.getStandardDeviation_t(), 1);
                result_relation[0] = relation;

                if (peakPow_t >= exerciseModel_t.getPeakPowerMin_t() && peakPow_t <= exerciseModel_t.getPeakPowerMax_t()) {
                    result_peak[0] = true;
                }

            }


            result = decideAct(result_relation, result_peak);
            if (result.size() == 1) {
                if (result.get(0) == -1) {
                    return -1;
                } else {
                    return result.get(0);
                }
            } else {
                tempResult = result;
                return -2;
            }
        }
    }

    public ArrayList<Integer> decideAct(double[] relation, boolean[] peak) {
    //0:exercise 1:eat 2:cafe 3:alcohol 4:tabacco 5:sleep 6:bath

        double relationMax = 0.0;
        ArrayList<Integer> result = new ArrayList<>();
        int num = -1;

        for (int i = 0; i < 6; i++) {
            if (relation[i] > 0.7 && peak[i]) {
                if (relation[i] > relationMax) {
                    relationMax = relation[i];
                    num = i;
                }
            }
        }

        result.add(num);

        if (num != -1) {
            for (int i = 0; i < 6; i++) {
                if (relation[i] > 0.7 && peak[i]) {
                    if (relation[i] == relationMax) {
                        if (i != num) {
                            result.add(i);
                        }
                    }
                }
            }
        }

        return result;
    }

    public double calcRelation(double[] modelDeviation, double modelStandardDeviation, int id){
        if (id == 0) {
            double dataVariance = 0.0;
            for (int i = 0; i < DATASIZE_H / 2 - 1; i++) {
                dataVariance += powerDeviation_h[i] * powerDeviation_h[i];
            }
            dataVariance = dataVariance / (double) (DATASIZE_H / 2 - 1);

            double dataStandardDeviation = Math.sqrt(dataVariance);

            double bothVariance = 0.0;
            for (int i = 0; i < DATASIZE_H / 2 - 1; i++) {
                bothVariance += modelDeviation[i] * powerDeviation_h[i];
            }
            bothVariance = bothVariance / (double)(DATASIZE_H / 2 - 1);

            return bothVariance / (modelStandardDeviation * dataStandardDeviation);
        }else{
            double dataVariance = 0.0;
            for (int i = 0; i < DATASIZE_T / 2 - 1; i++) {
                dataVariance += powerDeviation_t[i] * powerDeviation_t[i];
            }
            dataVariance = dataVariance / (double) (DATASIZE_T / 2 - 1);

            double dataStandardDeviation = Math.sqrt(dataVariance);

            double bothVariance = 0.0;
            for (int i = 0; i < DATASIZE_T / 2 - 1; i++) {
                bothVariance += modelDeviation[i] * powerDeviation_t[i];
            }
            bothVariance = bothVariance / (double)(DATASIZE_T / 2 - 1);

            return bothVariance / (modelStandardDeviation * dataStandardDeviation);
        }
    }

    /**
     * 心拍数情報をテキストファイルへの書き込みを行うメソッド
     * @param string 書き込む文字列
     */
    public void outputHeartData(String string){
        Calendar calendar = Calendar.getInstance();
        calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd/HH:mm:ss");
        FileOutput fileOutput = new FileOutput();
        String nowDate = dateFormat.format(calendar.getTime());
        fileOutput.writeFile(context, nowDate + " " + string, "heart.txt");
    }

    /**
     * 皮膚温度情報をテキストファイルへの書き込みを行うメソッド
     * @param string 書き込む文字列
     */
    public void outputSkinData(String string){
        Calendar calendar = Calendar.getInstance();
        calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd/HH:mm:ss");
        FileOutput fileOutput = new FileOutput();
        String nowDate = dateFormat.format(calendar.getTime());
        fileOutput.writeFile(context, nowDate + " " + string, "skin.txt");
    }
}
