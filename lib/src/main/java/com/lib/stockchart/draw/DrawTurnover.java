package com.lib.stockchart.draw;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.text.TextUtils;
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

    private float left;
    private float top;
    private float right;
    private float bottom;
    private float width;
    private float height;

    @Override
    public void onDrawInit(int left1, int top1, int right1, int bottom1, int width1, int height1, float xlabelHeight, float boardPadding) {

        // 测试, 高度
        final int weightTop = EntryManager.getInstance().getWeightTop();
        final int weightDown = EntryManager.getInstance().getWeightDown();
        final int weightSum = weightTop + weightDown;
        float height = height1 / weightSum;

        // 内边距
        this.left = left1 + boardPadding;
        this.top = (int) (top1 + height * weightTop) + boardPadding + xlabelHeight;
        this.right = right1 - boardPadding;
        this.bottom = bottom1 - boardPadding;
        this.width = right - left;
        this.height = bottom - top;
        Log.e("temp", "left = " + left + ", top = " + top + ", right = " + right + ", bottom = " + bottom);
    }

    @Override
    public void onDrawNull(Canvas canvas, String str, float xlabelHeight, float boardPadding) {
        //canvas.save();

        drawBackground(canvas, str, boardPadding);
        // 保存
        //canvas.restore();
    }

    @Override
    public void onDrawData(BaseRender render, Canvas canvas, int pointCount, int pointBegin, int pointEnd, float minPrice, float maxPrice, float maxTurnover, float xHighligh, float yHighligh, float xoffsetLeft, float xoffsetRight, float xlabelHeight, float boardPadding) {
        canvas.save();

        // Log.e("DrawTurnover1", "onDrawData ==> minIndex = "+minIndex+", maxIndex = "+maxIndex+", minPrice = "+minPrice+", maxPrice = "+maxPrice+", maxTurnover = "+maxTurnover);

        final EntryManager entryManager = EntryManager.getInstance();
        final int pointWidth = EntryManager.getInstance().getPointWidth();

        drawBackground(canvas, null, boardPadding);

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

            float left = entry.getxLabelReal() + xoffsetLeft + xoffsetRight;
            float right = left + pointWidth;
            float top = bottom - entry.getVolume() * height / maxTurnover;

            boolean isMin = Math.abs(top - bottom) < 1.f;
            // 成交量非常小画一条直线
            canvas.drawRect(left, isMin ? (bottom - boardPadding) : top, right, bottom, StockPaint.getTurnoverPaint());
        }

        // drawMadline(canvas, pointCount, pointBegin, pointEnd);

        // 文字交易量
        Paint textPaint = StockPaint.getTextPaint(Paint.Align.LEFT, 20);
        Paint.FontMetrics fontMetrics = textPaint.getFontMetrics();
        float temp = fontMetrics.bottom - fontMetrics.top;
        canvas.drawText(maxTurnover + "手", left, top + temp, textPaint);

        // 高亮坐标
        drawHightlight(canvas, xHighligh, yHighligh, pointCount, pointBegin, pointEnd, xlabelHeight, boardPadding);

        // 保存
        canvas.restore();
    }

    /**
     * 背景
     */
    private void drawBackground(Canvas canvas, String str, float boardPadding) {
        // 1.边框
        canvas.drawRect(left - 2 * boardPadding, top - 2 * boardPadding, right + 2 * boardPadding, bottom + 2 * boardPadding, StockPaint.getBorderPaint(3));

        // 4条横线 - 虚线
        float y = (bottom - top) / 2 + top;
        float startX = left - boardPadding;
        float stopX = right + boardPadding;
        canvas.drawLine(startX, y, stopX, y, StockPaint.getDashPaint());

        // 4条竖线 - 虚线
        float temp2 = width / 5;
        for (int i = 1; i < 5; i++) {
            float startY = top - boardPadding;
            float x = left + i * temp2;
            float stopY = bottom + boardPadding;
            canvas.drawLine(x, startY, x, stopY, StockPaint.getDashPaint());
        }

        // 文字交易量
        if (!TextUtils.isEmpty(str)) {
            canvas.drawText(str, right - width / 2, bottom - height / 2, StockPaint.getTextPaint(Paint.Align.CENTER, 30));
        }
    }

    /**
     * 高亮
     */
    private void drawHightlight(Canvas canvas, float xHighligh, float yHighligh, int pointCount, int pointBegin, int pointEnd, float xlabelHeight, float boardPadding) {

        if (xHighligh == -1f || yHighligh == -1f) return;

        final List<Entry> entryList = EntryManager.getInstance().getEntryList();
        final int pointWidth = EntryManager.getInstance().getPointWidth();

        final Entry entryBegin = entryList.get(pointBegin - 1);
        final float xBegin = entryBegin.getxLabelReal() + pointWidth / 2;

        final Entry entryEnd = entryList.get(pointEnd - 1);
        final float xEnd = entryEnd.getxLabelReal() + pointWidth / 2;

        if (xHighligh <= xBegin) {
            // 横线
            final float y = entryList.get(pointBegin - 1).getOpenReal();
            canvas.drawLine(left, y, right, y, StockPaint.getLinePaint(Color.BLACK));
            // 竖线
            final float x = boardPadding + left + pointWidth / 2;
            canvas.drawLine(x, top, x, bottom, StockPaint.getLinePaint(Color.BLACK));
        } else if (xHighligh >= xEnd) {
            // 横线
            final float y = entryList.get(pointEnd - 1).getOpenReal();
            canvas.drawLine(left, y, right, y, StockPaint.getLinePaint(Color.BLACK));
            // 竖线
            canvas.drawLine(xEnd, top, xEnd, bottom, StockPaint.getLinePaint(Color.BLACK));
        } else {
            for (int i = pointBegin; i < Math.min(pointEnd, pointBegin + pointCount); i++) {

                final Entry entry1 = entryList.get(i);
                final float tempx1 = entry1.getxLabelReal();
                final float x1 = tempx1 + pointWidth / 2;

                final Entry entry2 = entryList.get(i + 1);
                final float tempx2 = entry2.getxLabelReal();
                final float x2 = tempx2 + pointWidth / 2;

                if (xHighligh > x1 && xHighligh < x2) {
                    // 横线
                    final float y = entryList.get(i - 1).getOpenReal();
                    canvas.drawLine(left, y, right, y, StockPaint.getLinePaint(Color.BLACK));
                    // 竖线
                    canvas.drawLine(x1, top, x1, bottom, StockPaint.getLinePaint(Color.BLACK));
                    break;
                }
            }
        }
    }

    /**
     * MAD
     */
    private void drawMadline(Canvas canvas, int pointCount, int pointBegin, int pointEnd, float xoffsetLeft, float xoffsetRight, float xlabelHeight, float boardPadding) {

        final List<Entry> entryList = EntryManager.getInstance().getEntryList();
        final float pointWidth = EntryManager.getInstance().getPointWidth();

        // 5日均线
        final float[] pts5 = new float[(pointCount + 1) * 4];
        // 10日均线
        final float[] pts10 = new float[(pointCount + 1) * 4];

        for (int i = pointBegin; i <= pointEnd; i++) {

            final Entry entry = entryList.get(i);

            final float tempx = entry.getxLabelReal();
            final float x = tempx + pointWidth / 2 + xoffsetLeft + xoffsetRight;
            final float y1 = top + entry.getVolumeMa5Real() + boardPadding;
            final float y2 = top + entry.getVolumeMa10Real() + boardPadding;
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