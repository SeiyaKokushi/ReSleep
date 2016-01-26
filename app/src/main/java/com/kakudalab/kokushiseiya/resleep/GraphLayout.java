package com.kakudalab.kokushiseiya.resleep;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.content.Context;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ComposeShader;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.Shader;
import android.view.ViewGroup;
import android.widget.LinearLayout;

public class GraphLayout extends LinearLayout{

    //グラフの値
    private List<Double> minValues = new ArrayList<>();
    private List<Double> maxValues = new ArrayList<>();

    //グラフのyラベル
    private List<String> yLabels = new ArrayList<>();

    //グラフのxラベル
    private List<String> xLabels = new ArrayList<>();

    //maxValuesの中の最大値
    private Double maxValue = 0.0;

    //グラフのみの幅
    private Integer plotWidth;

    //グラフのみの高さ
    private Integer plotHeight;

    //起きる時間
    private Integer wakehour;

    //縦の線の数を指定
    Integer xLineCount;

    //呼び出し元のid
    int id = 0;

    //寝る時刻
    private Integer sleepHour = 0;
    private Integer sleepMin = 0;

    /**
     * コンストラクター
     * Viewのクラス内では、getContext()でcontextの取得ができる
     *
     * @param context
     */
    public GraphLayout(Context context, int id) {
        super(context);

        setWillNotDraw(false);
        this.id = id;
    }

    /**
     * AddViewなんかで実際にviewが追加されたときに呼び出される
     *
     */
    @Override
    protected void onAttachedToWindow(){
        super.onAttachedToWindow();

    }

    /**
     * タテヨコの大きさを決める時に呼び出される
     * Viewの大きさを指定したい場合は、ここで指定
     *
     */
    @Override
    protected void onMeasure(int width, int height) {
        super.onMeasure(width, height);

        setLayoutParams(new LinearLayout.LayoutParams(2000, ViewGroup.LayoutParams.MATCH_PARENT));
    }


    /**
     * レイアウトを決める時に呼び出される
     *
     */
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
    }

    /**
     * 描画する！
     *
     */
    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //ラベルのペイントを取る
        Paint txtPaint = getLabelPaint();

        //高さを出したいので、仮に一番最初のラベルの文字列を取得します
        String yLabel = yLabels.get(0);

        //四角形
        Rect bounds = new Rect();

        //指定した文字列を入れる四角形を作成
        txtPaint.getTextBounds(yLabel, 0, yLabel.length(), bounds);

        //四角形の幅を取得 = 文字列の幅
        int yWidth = bounds.width();

        //グラフだけの幅を計算
        plotWidth = getMeasuredWidth() - yWidth;


        //### X軸ラベルを作成 ###

        //それぞれのXの値を計算。誤差を少なくする為に一旦floatに変えてます
        Float eachX = maxValue.floatValue()/xLineCount.floatValue();
        Float tmpX = (float)wakehour;

        //先にフィールドとして宣言したxLabelsを初期化
        xLabels = new ArrayList<>();

        for(int i=0; i<xLineCount + 1; i++){
            xLabels.add(String.valueOf(tmpX.intValue()));
            tmpX = tmpX + eachX;
        }

        //x軸の幅を取得します。変数はyのを流用。
        //一番最後の値がでかそうなので一番最後の文字列を取得
        yLabel = xLabels.get(xLabels.size()-1);
        txtPaint.getTextBounds(yLabel, 0, yLabel.length(),bounds);
        int xHeight = bounds.height();

        //グラフだけの高さを計算
        plotHeight = getMeasuredHeight() - xHeight;

        //描画する！
        drawLabels(canvas);
        drawBackground(canvas);
        drawBars(canvas);
        Float x = drawCurrentBar(canvas);
        drawShade(canvas, x);
        drawSleepLine(canvas);
    }

    /**
     * ラベルの色・サイズはY軸、X軸ともに共通のを使用するので一つにまとめます
     * Paintは、色やサイズなんかを保持するオブジェクトです。
     *
     *
     */
    private Paint getLabelPaint(){

        Paint paint = new Paint();
        //アンチエイリアス
        paint.setAntiAlias(true);
        //文字の色
        paint.setColor(Color.parseColor("#777777"));
        //文字のサイズ
        paint.setTextSize(40f);

        return paint;
    }

    /**
     * ラベルを書く
     *
     * @param canvas
     */
    private void drawLabels(Canvas canvas){
        Paint paint = getLabelPaint();
        //右揃えにする
        paint.setTextAlign(Align.RIGHT);


        //## Yのラベルを描く！
        //それぞれの高さを計算
        Float yEach = plotHeight.floatValue()/((Integer)yLabels.size()).floatValue();

        //さらにそれぞれの真ん中の位置を計算
        Float center = yEach/2;

        //yLabelsは、0から入ってるのでグラフの下の部分から描画します。
        Float yCurrent = plotHeight.floatValue() - center * 0.8f;

        //前に計算した文字列の幅をもっかい計算するという愚行
        Float xPlotStart = getMeasuredWidth() - plotWidth.floatValue();

        for(int i=0; i< yLabels.size();i++){
            //テキストを描画します。
            //xの位置は、右側を指定
            canvas.drawText(yLabels.get(i), xPlotStart, yCurrent, paint);

            //次の位置を計算
            yCurrent = yCurrent - yEach;
        }


        //## Xのラベルを描く！

        //中央ぞろえ
        paint.setTextAlign(Align.CENTER);

        //それぞれの幅を計算
        Float xEach = plotWidth.floatValue()/xLineCount.floatValue();

        //yで計算したxの位置を流用。
        Float tmpX = xPlotStart;

        for(int i=0;i<xLabels.size();i++){
            canvas.drawText(xLabels.get(i), tmpX, getMeasuredHeight(), paint);
            tmpX = tmpX + xEach;
        }
    }


    /**
     * 背景を書く
     *
     * @param canvas
     */
    private void drawBackground(Canvas canvas){
        Paint paint = new Paint();
        paint.setAntiAlias(true);

        int plotStartX = getMeasuredWidth() - plotWidth;
        int plotStartY = 0;


        //background
        paint.setColor(Color.parseColor("#EFEFEF"));
        Rect rect = new Rect(plotStartX, 0, getMeasuredWidth(), plotHeight);
        canvas.drawRect(rect, paint);

        //## xの線を描く！
        Float xEach = plotWidth.floatValue()/xLineCount.floatValue();

        //前に計算した文字列の幅をもっかい計算するという愚行
        Float tmpX = getMeasuredWidth() - plotWidth.floatValue();

        for(int i=0; i< xLabels.size();i++){
            paint.setStrokeWidth(1);
            paint.setColor(Color.parseColor("#AAAAAA"));

            //線を描きます
            canvas.drawLine(tmpX, plotStartY, tmpX, plotHeight.floatValue(), paint);

            //次の位置を計算
            tmpX += xEach;
        }
    }


    /**
     * バーを書く
     *
     * @param canvas
     */
    private void drawBars(Canvas canvas){
        Paint paint = new Paint();
        paint.setAntiAlias(true);

        //それぞれの高さを計算
        Float yEach = plotHeight.floatValue()/((Integer)yLabels.size()).floatValue();

        //棒グラフに適度な余白を追加します。
        Float barSpace;
        if (id == 0) {
            barSpace = 40F;
        } else {
            barSpace = 80F;
        }

        //最初のyの位置を計算
        //yの開始位置
        Float tmpYBottom = plotHeight.floatValue() - barSpace ;
        //xの終わり
        Float tmpYTop = plotHeight - yEach + barSpace ;

        //前に計算した文字列の幅をもっかい計算するという愚行
        Float xPlotStart = getMeasuredWidth() - plotWidth.floatValue();

        //valuesの値1に対する高さを計算
        Float eachWidth = plotWidth.floatValue()/maxValue.floatValue();

        Shader shader = new Shader();

        if (id == 0) {
            for (int i = 0; i < maxValues.size(); i++) {
                switch (i) {
                    case 0:
                        shader = new LinearGradient(xPlotStart + (float) (eachWidth * (minValues.get(i) - wakehour)), (tmpYTop - tmpYBottom) / 2, xPlotStart + (float) (eachWidth * (maxValues.get(i) - wakehour)), (tmpYTop - tmpYBottom) / 2, Color.parseColor("#528ed1"), Color.parseColor("#98dcff"), Shader.TileMode.MIRROR);
                        break;

                    case 1:
                        shader = new LinearGradient(xPlotStart + (float) (eachWidth * (minValues.get(i) - wakehour)), (tmpYTop - tmpYBottom) / 2, xPlotStart + (float) (eachWidth * (maxValues.get(i) - wakehour)), (tmpYTop - tmpYBottom) / 2, Color.parseColor("#a7a3bc"), Color.parseColor("#e2deff"), Shader.TileMode.MIRROR);
                        break;

                    case 2:
                        shader = new LinearGradient(xPlotStart + (float) (eachWidth * (minValues.get(i) - wakehour)), (tmpYTop - tmpYBottom) / 2, xPlotStart + (float) (eachWidth * (maxValues.get(i) - wakehour)), (tmpYTop - tmpYBottom) / 2, Color.parseColor("#5edc5a"), Color.parseColor("#acdcb5"), Shader.TileMode.MIRROR);
                        break;

                    case 3:
                        shader = new LinearGradient(xPlotStart + (float) (eachWidth * (minValues.get(i) - wakehour)), (tmpYTop - tmpYBottom) / 2, xPlotStart + (float) (eachWidth * (maxValues.get(i) - wakehour)), (tmpYTop - tmpYBottom) / 2, Color.parseColor("#7676BC"), Color.parseColor("#b8b6ff"), Shader.TileMode.MIRROR);
                        break;

                    case 4:
                        shader = new LinearGradient(xPlotStart + (float) (eachWidth * (minValues.get(i) - wakehour)), (tmpYTop - tmpYBottom) / 2, xPlotStart + (float) (eachWidth * (maxValues.get(i) - wakehour)), (tmpYTop - tmpYBottom) / 2, Color.parseColor("#FBAD6B"), Color.parseColor("#fbe8d9"), Shader.TileMode.MIRROR);
                        break;

                    case 5:
                        shader = new LinearGradient(xPlotStart + (float) (eachWidth * (minValues.get(i) - wakehour)), (tmpYTop - tmpYBottom) / 2, xPlotStart + (float) (eachWidth * (maxValues.get(i) - wakehour)), (tmpYTop - tmpYBottom) / 2, Color.parseColor("#eee43b"), Color.parseColor("#ffffbf"), Shader.TileMode.MIRROR);
                        break;

                    case 6:
                        shader = new LinearGradient(xPlotStart + (float) (eachWidth * (minValues.get(i) - wakehour)), (tmpYTop - tmpYBottom) / 2, xPlotStart + (float) (eachWidth * (maxValues.get(i) - wakehour)), (tmpYTop - tmpYBottom) / 2, Color.parseColor("#E65749"), Color.parseColor("#ffd5e3"), Shader.TileMode.MIRROR);
                        break;
                }
                paint.setShader(shader);
                canvas.drawRect(xPlotStart + (float) (eachWidth * (minValues.get(i) - wakehour)), tmpYTop, xPlotStart + (float) (eachWidth * (maxValues.get(i) - wakehour)), tmpYBottom, paint);
                tmpYBottom -= yEach;
                tmpYTop -= yEach;
            }
        } else if (id == 1) {
            shader = new LinearGradient(xPlotStart + (float) (eachWidth * (minValues.get(0) - wakehour)), (tmpYTop - tmpYBottom) / 2, xPlotStart + (float) (eachWidth * (maxValues.get(0) - wakehour)), (tmpYTop - tmpYBottom) / 2, Color.parseColor("#E65749"), Color.parseColor("#ffd5e3"), Shader.TileMode.MIRROR);
            paint.setShader(shader);
            canvas.drawRect(xPlotStart + (float) (eachWidth * (minValues.get(0) - wakehour)), tmpYTop, xPlotStart + (float) (eachWidth * (maxValues.get(0) - wakehour)), tmpYBottom, paint);
        } else if (id == 2) {
            shader = new LinearGradient(xPlotStart + (float) (eachWidth * (minValues.get(0) - wakehour)), (tmpYTop - tmpYBottom) / 2, xPlotStart + (float) (eachWidth * (maxValues.get(0) - wakehour)), (tmpYTop - tmpYBottom) / 2, Color.parseColor("#eee43b"), Color.parseColor("#ffffbf"), Shader.TileMode.MIRROR);
            paint.setShader(shader);
            canvas.drawRect(xPlotStart + (float) (eachWidth * (minValues.get(0) - wakehour)), tmpYTop, xPlotStart + (float) (eachWidth * (maxValues.get(0) - wakehour)), tmpYBottom, paint);
        } else if (id == 3) {
            shader = new LinearGradient(xPlotStart + (float) (eachWidth * (minValues.get(0) - wakehour)), (tmpYTop - tmpYBottom) / 2, xPlotStart + (float) (eachWidth * (maxValues.get(0) - wakehour)), (tmpYTop - tmpYBottom) / 2, Color.parseColor("#FBAD6B"), Color.parseColor("#fbe8d9"), Shader.TileMode.MIRROR);
            paint.setShader(shader);
            canvas.drawRect(xPlotStart + (float) (eachWidth * (minValues.get(0) - wakehour)), tmpYTop, xPlotStart + (float) (eachWidth * (maxValues.get(0) - wakehour)), tmpYBottom, paint);
        } else if (id == 4) {
            shader = new LinearGradient(xPlotStart + (float) (eachWidth * (minValues.get(0) - wakehour)), (tmpYTop - tmpYBottom) / 2, xPlotStart + (float) (eachWidth * (maxValues.get(0) - wakehour)), (tmpYTop - tmpYBottom) / 2, Color.parseColor("#7676BC"), Color.parseColor("#b8b6ff"), Shader.TileMode.MIRROR);
            paint.setShader(shader);
            canvas.drawRect(xPlotStart + (float) (eachWidth * (minValues.get(0) - wakehour)), tmpYTop, xPlotStart + (float) (eachWidth * (maxValues.get(0) - wakehour)), tmpYBottom, paint);
        } else if (id == 5) {
            shader = new LinearGradient(xPlotStart + (float) (eachWidth * (minValues.get(0) - wakehour)), (tmpYTop - tmpYBottom) / 2, xPlotStart + (float) (eachWidth * (maxValues.get(0) - wakehour)), (tmpYTop - tmpYBottom) / 2, Color.parseColor("#5edc5a"), Color.parseColor("#acdcb5"), Shader.TileMode.MIRROR);
            paint.setShader(shader);
            canvas.drawRect(xPlotStart + (float) (eachWidth * (minValues.get(0) - wakehour)), tmpYTop, xPlotStart + (float) (eachWidth * (maxValues.get(0) - wakehour)), tmpYBottom, paint);
        } else if (id == 6) {
            shader = new LinearGradient(xPlotStart + (float) (eachWidth * (minValues.get(0) - wakehour)), (tmpYTop - tmpYBottom) / 2, xPlotStart + (float) (eachWidth * (maxValues.get(0) - wakehour)), (tmpYTop - tmpYBottom) / 2, Color.parseColor("#a7a3bc"), Color.parseColor("#e2deff"), Shader.TileMode.MIRROR);
            paint.setShader(shader);
            canvas.drawRect(xPlotStart + (float) (eachWidth * (minValues.get(0) - wakehour)), tmpYTop, xPlotStart + (float) (eachWidth * (maxValues.get(0) - wakehour)), tmpYBottom, paint);
        } else if (id == 7) {
            shader = new LinearGradient(xPlotStart + (float) (eachWidth * (minValues.get(0) - wakehour)), (tmpYTop - tmpYBottom) / 2, xPlotStart + (float) (eachWidth * (maxValues.get(0) - wakehour)), (tmpYTop - tmpYBottom) / 2, Color.parseColor("#528ed1"), Color.parseColor("#98dcff"), Shader.TileMode.MIRROR);
            paint.setShader(shader);
            canvas.drawRect(xPlotStart + (float) (eachWidth * (minValues.get(0) - wakehour)), tmpYTop, xPlotStart + (float) (eachWidth * (maxValues.get(0) - wakehour)), tmpYBottom, paint);
        }
    }

    /**
     * 現在時刻に線を引く
     * @param canvas
     */
    private Float drawCurrentBar(Canvas canvas) {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int min = calendar.get(Calendar.MINUTE);

        Paint paint = new Paint();
        paint.setAntiAlias(true);

        int plotStartY = 0;

        //## xの線を描く！
        Float xEach = plotWidth.floatValue()/xLineCount.floatValue();

        //前に計算した文字列の幅をもっかい計算するという愚行
        Float tmpX = getMeasuredWidth() - plotWidth.floatValue();

        for (int i = 0; i < xLabels.size(); i++) {
            if (xLabels.get(i).equals(String.valueOf(hour))) {
                paint.setStrokeWidth(8);
                paint.setColor(Color.parseColor("#b22222"));

                tmpX += (float)min / 60f * xEach;

                //線を描きます
                canvas.drawLine(tmpX, plotStartY, tmpX, plotHeight.floatValue(), paint);

                return tmpX;
            }

            //次の位置を計算
            tmpX += xEach;
        }

        return 0f;
    }

    /**
     * 過ぎた時間に影をつける
     * @param canvas
     * @param x
     */
    private void drawShade(Canvas canvas, Float x) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);

        Float xPlotStart = getMeasuredWidth() - plotWidth.floatValue();
        Float tmpYTop = plotHeight.floatValue() - getMeasuredHeight();
        Float tmpYBottom = plotHeight.floatValue();

        paint.setColor(Color.parseColor("#dcdcdc"));
        paint.setAlpha(200);

        canvas.drawRect(xPlotStart, tmpYTop, x - 4f, tmpYBottom, paint);
    }

    /**
     * 寝る時刻に線を引く
     * @param canvas
     */
    private void drawSleepLine(Canvas canvas) {
        int hour = sleepHour;
        int min = sleepMin;

        Paint paint = new Paint();
        paint.setAntiAlias(true);

        int plotStartY = 0;

        //## xの線を描く！
        Float xEach = plotWidth.floatValue()/xLineCount.floatValue();

        //前に計算した文字列の幅をもっかい計算するという愚行
        Float tmpX = getMeasuredWidth() - plotWidth.floatValue();

        for (int i = 0; i < xLabels.size(); i++) {
            if (xLabels.get(i).equals(String.valueOf(hour))) {
                paint.setStrokeWidth(8);
                paint.setColor(Color.parseColor("#00bfff"));

                tmpX += (float)min / 60f * xEach;

                //線を描きます
                canvas.drawLine(tmpX, plotStartY, tmpX, plotHeight.floatValue(), paint);

                break;
            }

            //次の位置を計算
            tmpX += xEach;
        }
    }

    /**
     * 棒グラフの値を一度にセットする
     *
     *
     */
    public void setMaxValues(List<Double> values){
        maxValues = values;
    }

    public void setMinValues(List<Double> values){
        minValues = values;
    }

    public void setXLineCount(int num){
        xLineCount = num;
    }

    public void setWakeHour(int hour){
        wakehour = hour;
    }

    public void setSleepHour(int hour){
        maxValue = ((double)hour - wakehour);
    }

    public void setSleepTime(int hour, int min) {
        sleepHour = hour;
        sleepMin = min;
    }

    /**
     * x軸のラベルを一度に全部セットする
     *
     *
     */
    public void setYLabels(List<String> yLabels){
        this.yLabels = yLabels;
    }

}
