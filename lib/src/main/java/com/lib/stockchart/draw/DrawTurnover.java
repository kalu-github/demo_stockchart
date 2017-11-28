package com.lib.stockchart.draw;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.Log;

import com.lib.stockchart.render.BaseRender;
import com.lib.stockchart.entry.Entry;
import com.lib.stockchart.entry.EntryManager;
import com.lib.stockchart.paint.StockPaint;

import java.util.ArrayList;
import java.util.List;

/**
 * description: 成交量
 * created by kalu on 2017/11/9 1:26
 */
public class DrawTurnover implements IDraw {

    // 绘图区域
    private final RectF mRectF = new RectF();
    private final int SPACE = 10;

    @Override
    public void onDrawInit(int left1, int top1, int right1, int bottom1, int width1, int height1) {
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
    public void onDrawData(BaseRender render, Canvas canvas, int pointCount, int pointBegin, int pointEnd, float minPrice, float maxPrice, float maxTurnover, float xHighligh, float yHighligh) {

        canvas.save();

        // Log.e("DrawTurnover1", "onDrawData ==> minIndex = "+minIndex+", maxIndex = "+maxIndex+", minPrice = "+minPrice+", maxPrice = "+maxPrice+", maxTurnover = "+maxTurnover);

        final EntryManager entryManager = EntryManager.getInstance();
        final int pointWidth = EntryManager.getInstance().getPointWidth();

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


        final int offsetLeft = EntryManager.getInstance().getXoffsetLeft();
        final int offsetRight = EntryManager.getInstance().getXoffsetRight();

        // 2.成交量
        // 2.设置画笔颜色
        StockPaint.setPaintWidth(5);
        // 2.循环遍历
        for (int i = pointBegin; i <= pointEnd; i++) {

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
//                    if (entry.getOpen() > entryManager.getPreClose()) {
//                        StockPaint.setPaintColor(StockPaint.STOCK_RED);
//                    } else if (entry.getOpen() == entryManager.getPreClose()) {
//                        StockPaint.setPaintColor(StockPaint.STOCK_GRAY);
//                    } else {
                    StockPaint.setPaintColor(StockPaint.STOCK_GREEN);
//                    }
                }
            }

            float left = entry.getxLabelReal() + offsetLeft + offsetRight;
            float right = left + pointWidth;
            float bottom = mRectF.bottom;
            float top = bottom - entry.getVolume() * mRectF.height() / maxTurnover;

            boolean isMin = Math.abs(top - bottom) < 1.f;
            // 成交量非常小画一条直线
            canvas.drawRect(left, isMin ? (bottom - SPACE) : top, right, bottom, StockPaint.getTurnoverPaint());
        }

        // drawMadline(canvas, pointCount, pointBegin, pointEnd);

        // 文字交易量
        Paint textPaint = StockPaint.getTextPaint(Paint.Align.LEFT, 20);
        Paint.FontMetrics fontMetrics = textPaint.getFontMetrics();
        float temp = fontMetrics.bottom - fontMetrics.top;
        canvas.drawText(maxTurnover + "手", mRectF.left, mRectF.top + temp, textPaint);

        // 高亮坐标
        drawHightlight(canvas, xHighligh, yHighligh, pointCount, pointBegin, pointEnd);

        // 保存
        canvas.restore();
    }

    /**
     * 高亮
     */
    private void drawHightlight(Canvas canvas, float xHighligh, float yHighligh, int pointCount, int pointBegin, int pointEnd) {

        if (xHighligh == -1f || yHighligh == -1f) return;

        final List<Entry> entryList = EntryManager.getInstance().getEntryList();
        final int pointWidth = EntryManager.getInstance().getPointWidth();

        final Entry entryBegin = entryList.get(pointBegin);
        final float xBegin = entryBegin.getxLabelReal() + pointWidth / 2;

        final Entry entryEnd = entryList.get(pointEnd - 1);
        final float xEnd = entryEnd.getxLabelReal() + pointWidth / 2;

        final int boardPadding = EntryManager.getInstance().getBoardPadding();

        if (xHighligh <= xBegin) {
            // 横线
            final float y = entryList.get(pointBegin - 1).getVolumeReal();
            canvas.drawLine(mRectF.left + boardPadding, y, mRectF.right - boardPadding, y, StockPaint.getLinePaint(Color.BLACK));
            // 竖线
            final float x = EntryManager.getInstance().getBoardPadding() + mRectF.left + pointWidth / 2;
            canvas.drawLine(x, mRectF.top + boardPadding, x, mRectF.bottom - boardPadding, StockPaint.getLinePaint(Color.BLACK));
        } else if (xHighligh >= xEnd) {
            // 横线
            final float y = entryList.get(pointEnd - 1).getVolumeReal();
            canvas.drawLine(mRectF.left + boardPadding, y, mRectF.right - boardPadding, y, StockPaint.getLinePaint(Color.BLACK));
            // 竖线
            canvas.drawLine(xEnd, mRectF.top + boardPadding, xEnd, mRectF.bottom - boardPadding, StockPaint.getLinePaint(Color.BLACK));
        } else {
            for (int i = pointBegin; i < Math.min(pointEnd, pointBegin + pointCount); i++) {

                final Entry entry1 = entryList.get(i);
                final int tempx1 = entry1.getxLabelReal();
                final float x1 = tempx1 + pointWidth / 2;

                final Entry entry2 = entryList.get(i + 1);
                final int tempx2 = entry2.getxLabelReal();
                final float x2 = tempx2 + pointWidth / 2;

                if (xHighligh > x1 && xHighligh < x2) {
                    // 横线
                    final float y = entryList.get(i - 1).getVolumeReal();
                    canvas.drawLine(mRectF.left + boardPadding, y, mRectF.right - boardPadding, y, StockPaint.getLinePaint(Color.BLACK));
                    // 竖线
                    canvas.drawLine(x1, mRectF.top + boardPadding, x1, mRectF.bottom - boardPadding, StockPaint.getLinePaint(Color.BLACK));
                    break;
                }
            }
        }
    }

    /**
     * MAD
     */
    private void drawMadline(Canvas canvas, int pointCount, int pointBegin, int pointEnd) {

        final List<Entry> entryList = EntryManager.getInstance().getEntryList();
        final int pointWidth = EntryManager.getInstance().getPointWidth();
        final int boardPadding = EntryManager.getInstance().getBoardPadding();

        final int offsetLeft = EntryManager.getInstance().getXoffsetLeft();
        final int offsetRight = EntryManager.getInstance().getXoffsetRight();

        // 5日均线
        final float[] pts5 = new float[(pointCount+1) * 4];
        // 10日均线
        final float[] pts10 = new float[(pointCount+1) * 4];

        for (int i = pointBegin; i <= pointEnd; i++) {

            final Entry entry = entryList.get(i);

            final int tempx = entry.getxLabelReal();
            final float x = tempx + pointWidth / 2 + offsetLeft + offsetRight;
            final float y1 = mRectF.top + entry.getVolumeMa5Real() + boardPadding;
            final float y2 = mRectF.top + entry.getVolumeMa10Real() + boardPadding;
            // Log.e("uuuu", "i = " + (i - 1) + ", x = " + x + ", y1 = " + y1 + ", y2 = " + y2);

            final int tempi = i - pointBegin;
            if (i == pointBegin) {
                pts5[4 * tempi + 0] = x;
                pts5[4 * tempi + 1] = y1;
                pts5[4 * tempi + 2] = x;
                pts5[4 * tempi + 3] = y1;
                pts10[4 * tempi + 0] = x;
                pts10[4 * tempi + 1] = y2;
                pts10[4 * tempi + 2] = x;
                pts10[4 * tempi + 3] = y2;
            } else {
                pts5[4 * tempi + 0] = pts5[4 * (tempi - 1) + 2];
                pts5[4 * tempi + 1] = pts5[4 * (tempi - 1) + 3];
                pts5[4 * tempi + 2] = x;
                pts5[4 * tempi + 3] = y1;
                pts10[4 * tempi + 0] = pts10[4 * (tempi - 1) + 2];
                pts10[4 * tempi + 1] = pts10[4 * (tempi - 1) + 3];
                pts10[4 * tempi + 2] = x;
                pts10[4 * tempi + 3] = y2;
            }
        }

        canvas.drawLines(pts5, StockPaint.getLinePaint(StockPaint.STOCK_RED));
        canvas.drawLines(pts10, StockPaint.getLinePaint(StockPaint.STOCK_GREEN));
    }
}