package com.lib.stockchart.paint;

import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;

/**
 * description: 画笔
 * created by kalu on 2017/11/12 3:05
 */
public class StockPaint {

    public static int STOCK_RED = Color.parseColor("#F3575B");
    public static int STOCK_GREEN = Color.parseColor("#64C67A");
    public static int STOCK_GRAY = Color.parseColor("#999999");

    public static int PAINT_WIDTH_LiNE = 4;

    private final static Paint mPaint = new Paint();

    /**
     * 边框
     */
    public static Paint getBorderPaint(int paintWidth) {

        mPaint.setAntiAlias(true);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setFakeBoldText(false);
        mPaint.setColor(Color.BLACK);
        mPaint.setStrokeWidth(paintWidth);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setPathEffect(null);

        return mPaint;
    }

    /**
     * 虚线
     */
    public static Paint getDashPaint() {

        mPaint.setAntiAlias(true);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setFakeBoldText(false);
        mPaint.setColor(Color.GRAY);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(2);
        mPaint.setPathEffect(new DashPathEffect(new float[]{5, 5}, 0));

        return mPaint;
    }

    /**
     * 文字
     */
    public static Paint getTextPaint(Paint.Align align, int textSize) {

        mPaint.setAntiAlias(true);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setFakeBoldText(true);
        mPaint.setColor(Color.BLACK);
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mPaint.setStrokeWidth(0.5f);
        mPaint.setTextAlign(align);
        mPaint.setTextSize(textSize);
        mPaint.setPathEffect(null);

        return mPaint;
    }

    /**
     * 成交量
     */
    public static Paint getTurnoverPaint() {

        mPaint.setAntiAlias(true);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setFakeBoldText(true);
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mPaint.setStrokeWidth(PAINT_WIDTH_LiNE);
        mPaint.setTextAlign(Paint.Align.LEFT);
        mPaint.setTextSize(1);
        mPaint.setPathEffect(null);

        return mPaint;
    }

    public static Paint getLinePaint(int color) {

        mPaint.setAntiAlias(true);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setFakeBoldText(true);
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mPaint.setStrokeWidth(4);
        mPaint.setTextAlign(Paint.Align.LEFT);
        mPaint.setTextSize(1);
        mPaint.setColor(color);
        mPaint.setPathEffect(null);

        return mPaint;
    }

    public static void setPaintColor(int color) {
        mPaint.setColor(color);
    }

    public static void setPaintWidth(float width) {
        mPaint.setStrokeWidth(width);
    }
}
