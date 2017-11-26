package com.lib.stockchart.draw;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.Log;

import com.lib.stockchart.render.AbstractRender;
import com.lib.stockchart.entry.Entry;
import com.lib.stockchart.entry.EntryManager;
import com.lib.stockchart.paint.StockPaint;

import java.util.ArrayList;

/**
 * description: 成交量
 * created by kalu on 2017/11/9 1:26
 */
public class DrawTurnover implements IDraw {

    // 绘图区域
    private final RectF mRectF = new RectF();
    private final int SPACE = 10;

    private float[] mPointX = new float[4];
    // 5日均线
    private final ArrayList<Float> ptsMad5 = new ArrayList();
    // 10日均线
    private final ArrayList<Float> ptsMad10 = new ArrayList();

    @Override
    public void onDrawInit(int left1, int top1, int right1, int bottom1,int width1, int height1) {
        Log.e("DrawTurnover", "onInit");

        // 测试, 高度
        final int weightTop = EntryManager.getInstance().getWeightTop();
        final int weightDown = EntryManager.getInstance().getWeightDown();
        final int weightSum = weightTop + weightDown;
        float height = height1 / weightSum;

        // 内边距
        float left = left1;
        float top = top1 + height * weightTop;
        float right = right1;
        float bottom = bottom1;
        mRectF.set(left, top, right, bottom);
        Log.e("temp", "left = " + left + ", top = " + top + ", right = " + right + ", bottom = " + bottom);
    }

    @Override
    public void onDrawNull(Canvas canvas) {
        Log.e("DrawTurnover1", "onDrawNull");
        //canvas.save();

        // 1.边框
        float left1 = mRectF.left;
        float top1 = mRectF.top;
        float right1 = mRectF.right;
        float bottom1 = mRectF.bottom;

        canvas.clipRect(left1 - SPACE, top1 - SPACE, right1 + SPACE, bottom1 + SPACE);
        canvas.drawRect(left1 - SPACE, top1 - SPACE, right1 + SPACE, bottom1 + SPACE, StockPaint.getBorderPaint(5));

        // 4条横线 - 虚线
        float y = (bottom1 - top1) / 2 + top1;
        float startX = left1 - SPACE;
        float stopX = right1 + SPACE;
        canvas.drawLine(startX, y, stopX, y, StockPaint.getDashPaint());

        // 4条竖线 - 虚线
        float temp2 = mRectF.width() / 5;
        for (int i = 1; i < 5; i++) {
            float startY = top1 - SPACE;
            float x = left1 + i * temp2;
            float stopY = bottom1 + SPACE;
            canvas.drawLine(x, startY, x, stopY, StockPaint.getDashPaint());
        }

        // 文字交易量
        final String hintLoadStr = EntryManager.getInstance().getHintLoadStr();
        canvas.drawText(hintLoadStr, mRectF.centerX(), mRectF.centerY(), StockPaint.getTextPaint(Paint.Align.CENTER, 30));

        // 保存
        //canvas.restore();
    }

    @Override
    public void onDrawData(AbstractRender render, Canvas canvas, int pointSum, int pointCount, int pointBegin, int pointEnd, float minPrice, float maxPrice, float maxTurnover, boolean hightLight, int model) {

        canvas.save();

        ptsMad5.clear();
        ptsMad10.clear();

        // Log.e("DrawTurnover1", "onDrawData ==> minIndex = "+minIndex+", maxIndex = "+maxIndex+", minPrice = "+minPrice+", maxPrice = "+maxPrice+", maxTurnover = "+maxTurnover);

        final EntryManager entryManager = EntryManager.getInstance();

        // 1.边框
        float left1 = mRectF.left;
        float top1 = mRectF.top;
        float right1 = mRectF.right;
        float bottom1 = mRectF.bottom;

        canvas.clipRect(left1 - SPACE, top1 - SPACE, right1 + SPACE, bottom1 + SPACE);
        canvas.drawRect(left1 - SPACE, top1 - SPACE, right1 + SPACE, bottom1 + SPACE, StockPaint.getBorderPaint(5));

        // 4条横线 - 虚线
        float y = (bottom1 - top1) / 2 + top1;
        float startX = left1 - SPACE;
        float stopX = right1 + SPACE;
        canvas.drawLine(startX, y, stopX, y, StockPaint.getDashPaint());

        // 4条竖线 - 虚线
        float temp2 = mRectF.width() / 5;
        for (int i = 1; i < 5; i++) {
            float startY = top1 - SPACE;
            float x = left1 + i * temp2;
            float stopY = bottom1 + SPACE;
            canvas.drawLine(x, startY, x, stopY, StockPaint.getDashPaint());
        }

        // 2.成交量
        // 2.设置画笔颜色
        StockPaint.setPaintWidth(5);
        // 2.循环遍历
        for (int i = pointBegin; i < pointEnd; i++) {

            // 当前点的数据
            Entry entry = entryManager.getEntryList().get(i);

            // 设置 涨、跌的颜色
            // 今日收盘价大于今日开盘价为涨
            if (entry.getClose() > entry.getOpen()) {
                StockPaint.setPaintColor(StockPaint.STOCK_RED);
            }
            // 今日收盘价小于今日开盘价为跌
            else if (entry.getClose() > entry.getOpen()) {
                StockPaint.setPaintColor(StockPaint.STOCK_GREEN);
            }
            // 今日收盘价等于今日开盘价有涨停、跌停、不涨不跌三种情况
            else {
                if (i > 0) {
                    if (entry.getOpen() > entryManager.getEntryList().get(i - 1).getClose()) { // 今日开盘价大于昨日收盘价为涨停
                        StockPaint.setPaintColor(StockPaint.STOCK_RED);
                    } else if (entry.getOpen() == entryManager.getEntryList().get(i - 1).getClose()) { // 不涨不跌
                        StockPaint.setPaintColor(StockPaint.STOCK_GRAY);
                    } else { // 否则为跌停
                        StockPaint.setPaintColor(StockPaint.STOCK_GREEN);
                    }
                } else {
                    if (entry.getOpen() > entryManager.getPreClose()) {
                        StockPaint.setPaintColor(StockPaint.STOCK_RED);
                    } else if (entry.getOpen() == entryManager.getPreClose()) {
                        StockPaint.setPaintColor(StockPaint.STOCK_GRAY);
                    } else {
                        StockPaint.setPaintColor(StockPaint.STOCK_GREEN);
                    }
                }
            }

            // 当前交易量
            // 计算 成交量的矩形卓坐标
            mPointX[0] = i;
            mPointX[1] = 0;
            mPointX[2] = i + 1;
            mPointX[3] = 0;
            render.mapPoints(mPointX);

            float left = mPointX[0] + SPACE / 2;
            float bottom = mRectF.bottom;
            float top = bottom - entry.getVolume() * mRectF.height() / maxTurnover;
            float right = mPointX[2] - SPACE / 2;
            Log.e("temp22", "left = " + left + ", top = " + top + ", right = " + right + ", bottom = " + bottom);

            // 左移边界
            if (left < left1) {
                left = left1;
            }
            if (left > right1) {
                continue;
            }

            // 右移边界
            if (right > right1) {
                right = right1;
            }
            if (right < left1) {
                continue;
            }

            boolean isMin = Math.abs(top - bottom) < 1.f;
            // 成交量非常小画一条直线
            canvas.drawRect(left, isMin ? (bottom - SPACE) : top, right, bottom, StockPaint.getTurnoverPaint());

            // Mad5
            final float x1 = left + (right - left) / 2;
            final float y1 = (float) (bottom - entry.getVolumeMa5() * mRectF.height() / maxTurnover);
            if (ptsMad5.size() == 0) {
                ptsMad5.add(x1);
                ptsMad5.add(y1);
                ptsMad5.add(x1);
                ptsMad5.add(y1);
            } else {
                final int size = ptsMad5.size();
                ptsMad5.add(ptsMad5.get(size - 2));
                ptsMad5.add(ptsMad5.get(size - 1));
                ptsMad5.add(x1);
                ptsMad5.add(y1);
            }

            // Mad10
            final float y10 = (float) (bottom - entry.getVolumeMa10() * mRectF.height() / maxTurnover);
            if (ptsMad10.size() == 0) {
                ptsMad10.add(x1);
                ptsMad10.add(y10);
                ptsMad10.add(x1);
                ptsMad10.add(y10);
            } else {
                final int size = ptsMad10.size();
                ptsMad10.add(ptsMad10.get(size - 2));
                ptsMad10.add(ptsMad10.get(size - 1));
                ptsMad10.add(x1);
                ptsMad10.add(y10);
            }

            // 高亮坐标
            drawHightlight(canvas, hightLight, i);
        }

        // 5日均线
        final float[] pts5 = new float[ptsMad5.size()];
        for (int i = 0; i < ptsMad5.size(); i++) {
            pts5[i] = ptsMad5.get(i);
        }
        canvas.drawLines(pts5, StockPaint.getLinePaint(Color.BLACK));

        // 10日均线
        final float[] pts10 = new float[ptsMad10.size()];
        for (int i = 0; i < ptsMad10.size(); i++) {
            pts10[i] = ptsMad10.get(i);
        }
        canvas.drawLines(pts10, StockPaint.getLinePaint(Color.RED));

        // 文字交易量
        Paint textPaint = StockPaint.getTextPaint(Paint.Align.LEFT, 20);
        Paint.FontMetrics fontMetrics = textPaint.getFontMetrics();
        float temp = fontMetrics.bottom - fontMetrics.top;
        canvas.drawText(maxTurnover + "手", mRectF.left, mRectF.top + temp, textPaint);

        // 保存
        canvas.restore();
    }

    /**
     * 高亮
     */
    private void drawHightlight(Canvas canvas, boolean hightLight, int pointIndex) {

        if (!hightLight) return;

        float[] pointHighlightX = EntryManager.getInstance().getPointHighlightX();
        final float x = pointHighlightX[1];
        if (mPointX[0] <= x && x <= mPointX[2]) {

            Log.e("DrawTurnover", "drawHightlight ==> pointIndex = " + pointIndex);
            EntryManager.getInstance().setPointHighlight(pointIndex);

            final float y = EntryManager.getInstance().getPointHighlightY()[1];
            // 横线
            canvas.drawLine(mRectF.left, y, mRectF.right, y, StockPaint.getLinePaint(Color.BLACK));
            // 竖线
            canvas.drawLine(x, mRectF.top, x, mRectF.bottom, StockPaint.getLinePaint(Color.BLACK));
        }
    }
}