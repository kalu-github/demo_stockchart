package com.lib.stockchart.draw;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.TextUtils;

import com.lib.stockchart.entry.Entry;
import com.lib.stockchart.entry.EntryManager;
import com.lib.stockchart.paint.StockPaint;
import com.lib.stockchart.render.BaseRender;

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

    // 5日均线, 10日均线
    private float[] pts5, pts10;

    @Override
    public void onDrawInit(int left1, int top1, int right1, int bottom1, int width1, int height1, float xlabelHeight, float boardPadding) {

        // 测试, 高度
        final int weightTop = EntryManager.getInstance().getWeightTop();
        final int weightDown = EntryManager.getInstance().getWeightDown();
        final int weightSum = weightTop + weightDown;
        float height = height1 / weightSum;

        // 内边距
        this.left = left1 + boardPadding;
        this.top = (int) (top1 + height * weightTop) + boardPadding;
        this.right = right1 - boardPadding;
        this.bottom = bottom1 - boardPadding;
        this.width = right - left;
        this.height = bottom - top;
        //  Log.e("temp", "left = " + left + ", top = " + top + ", right = " + right + ", bottom = " + bottom);
    }

    @Override
    public void onDrawNull(Canvas canvas, String str, float xlabelHeight, float boardPadding) {

        canvas.save();
        drawBackground(canvas, str, boardPadding);
        canvas.restore();
    }

    @Override
    public void onDrawData(BaseRender render, Canvas canvas, int pointMax, int indexBegin, int indexEnd, float minPrice, float maxPrice, float maxTurnover, float xHighligh, float yHighligh, float xoffsetLeft, float xoffsetRight, float xlabelHeight, float boardPadding) {

        final List<Entry> entryList = EntryManager.getInstance().getEntryList();
        if (null == entryList || entryList.size() <= 0) return;

        canvas.save();

        final int pointWidth = EntryManager.getInstance().getPointWidth();

        // 边框
        drawBackground(canvas, null, boardPadding);

        final Entry entryBegin = entryList.get(0);
        final Entry entryEnd = entryList.get(entryList.size() - 1);

        for (int i = indexBegin; i <= indexEnd; i++) {

            final Entry entry = entryList.get(i);
            if (null == entry) continue;

            // 成交量
            drawTurnover(canvas, entry, i == 0 ? entry : entryList.get(i - 1), i, pointWidth, boardPadding, xoffsetLeft, xoffsetRight);
            // mad
            drawMadline(canvas, entry, i, pointWidth, pointMax, indexBegin, indexEnd, xoffsetLeft, xoffsetRight);
            // 高亮
            drawHightlight(canvas, entryBegin, entryEnd, i == 0 ? entry : entryList.get(i - 1), entry, pointWidth, i, xHighligh, yHighligh, indexBegin, indexEnd, boardPadding);
        }

        // 文字交易量
        drawText(canvas, maxTurnover);

        canvas.restore();
    }

    /**
     * 背景
     */
    private void drawBackground(Canvas canvas, String str, float boardPadding) {
        // 1.边框
        canvas.drawRect(left - 2 * boardPadding, top - 2 * boardPadding, right + 2 * boardPadding, bottom + 2 * boardPadding, StockPaint.getBorderPaint(3));

        // 2条横线 - 虚线
        float temp = height / 2;
        final float x1 = left + 2;
        final float x2 = x1 + 5;

        for (int j = 1; j < 2; j++) {

            float Y = top + j * temp;
            float[] ptsDash = new float[]{x1, 0, x2, 0};
            ptsDash[1] = ptsDash[3] = Y;

            while (ptsDash[0] <= right) {
                canvas.drawLines(ptsDash, StockPaint.getDashPaint());

                final float x1Temp = ptsDash[0];
                final float x2Temp = ptsDash[2];
                ptsDash[0] = x1Temp + 15;
                ptsDash[2] = Math.min(x2Temp + 15, right);
            }
        }

        // 4条竖线 - 虚线
        float temp2 = width / 4;
        final float y1 = top + 2;
        final float y2 = top + 5;

        for (int j = 1; j < 4; j++) {

            float X = left + j * temp2;
            float[] ptsDash = new float[]{0, y1, 0, y2};
            ptsDash[0] = ptsDash[2] = X;

            while (ptsDash[1] <= bottom) {
                canvas.drawLines(ptsDash, StockPaint.getDashPaint());

                final float x1Temp = ptsDash[1];
                final float x2Temp = ptsDash[3];
                ptsDash[1] = x1Temp + 15;
                ptsDash[3] = Math.min(x2Temp + 15, bottom);
            }
        }

        // 文字交易量
        if (!TextUtils.isEmpty(str)) {
            canvas.drawText(str, right - width / 2, bottom - height / 2, StockPaint.getTextPaint(Paint.Align.CENTER, 30));
        }
    }

    /**
     * 成交量
     */
    private void drawTurnover(Canvas canvas, Entry entry, Entry oldEntry, int i, float pointWidth, float boardPadding, float xoffsetLeft, float xoffsetRight) {
        // 2.成交量
        // 2.设置画笔颜色
        StockPaint.setPaintWidth(5);

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
                if (entry.getOpen() > oldEntry.getClose()) { // 今日开盘价大于昨日收盘价为涨停
                    StockPaint.setPaintColor(StockPaint.STOCK_RED);
                } else if (entry.getOpen() == oldEntry.getClose()) { // 不涨不跌
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
        // float top = bottom - entry.getVolume() * height / maxTurnover;

        float top = entry.getVolumeReal();

        boolean isMin = Math.abs(top - bottom) < 1.f;
        // 成交量非常小画一条直线
        canvas.drawRect(left, isMin ? (bottom - boardPadding) : top, right, bottom, StockPaint.getTurnoverPaint());
    }

    /**
     * 高亮
     */
    private void drawHightlight(Canvas canvas, Entry entryBegin, Entry entryEnd, Entry entryLast, Entry entry, int pointWidth, int i, float xHighligh, float yHighligh, int pointBegin, int pointEnd, float boardPadding) {

        if (xHighligh == -1f || yHighligh == -1f) return;

        final float xBegin = entryBegin.getxLabelReal() + pointWidth / 2;

        final float xEnd = entryEnd.getxLabelReal() + pointWidth / 2;

        if (i == pointBegin && xHighligh <= xBegin) {
            // 横线
            final float y = entryBegin.getVolumeReal();
            canvas.drawLine(left, y, right, y, StockPaint.getLinePaint(Color.BLACK));
            // 竖线
            final float x = boardPadding + left + pointWidth / 2;
            canvas.drawLine(x, top, x, bottom, StockPaint.getLinePaint(Color.BLACK));
        } else if (i == pointBegin && xHighligh >= xEnd) {
            // 横线
            final float y = entryEnd.getVolumeReal();
            canvas.drawLine(left, y, right, y, StockPaint.getLinePaint(Color.BLACK));
            // 竖线
            canvas.drawLine(xEnd, top, xEnd, bottom, StockPaint.getLinePaint(Color.BLACK));
        } else {
            final float tempx1 = entryLast.getxLabelReal();
            final float x1 = tempx1 + pointWidth / 2;

            final float tempx2 = entry.getxLabelReal();
            final float x2 = tempx2 + pointWidth / 2;
            if (xHighligh > x1 && xHighligh < x2) {
                // 横线
                final float y = entry.getVolumeReal();
                canvas.drawLine(left, y, right, y, StockPaint.getLinePaint(Color.BLACK));
                // 竖线
                canvas.drawLine(x1, top, x1, bottom, StockPaint.getLinePaint(Color.BLACK));
            }
        }
    }

    /**
     * MAD
     */
    private void drawMadline(Canvas canvas, Entry entry, int i, int pointWidth, int pointMax, int pointBegin, int pointEnd, float xoffsetLeft, float xoffsetRight) {

        if (null == pts5 && null == pts10) {
            final int size = 4 * (pointEnd + 1);
            pts5 = new float[size];
            pts10 = new float[size];
        }

        final float tempx = entry.getxLabelReal();
        final float x = tempx + pointWidth / 2 + xoffsetLeft + xoffsetRight;
        final float y1 = entry.getVolumeMa5Real();
        final float y2 = entry.getVolumeMa10Real();

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

        // 容错处理
        if (pointBegin + pointMax > pointEnd) {
            int realEnd = (pointEnd - pointBegin);
            final float x5 = pts5[4 * realEnd + 2];
            final float y5 = pts5[4 * realEnd + 3];
            final float x10 = pts10[4 * realEnd + 2];
            final float y10 = pts10[4 * realEnd + 3];

            for (int j = realEnd + 1; j <= pointMax; j++) {
                pts5[4 * j + 0] = x5;
                pts5[4 * j + 1] = y5;
                pts5[4 * j + 2] = x5;
                pts5[4 * j + 3] = y5;
                pts10[4 * j + 0] = x10;
                pts10[4 * j + 1] = y10;
                pts10[4 * j + 2] = x10;
                pts10[4 * j + 3] = y10;
            }
        }

        if (i != pointEnd) return;
        canvas.drawLines(pts5, StockPaint.getLinePaint(StockPaint.STOCK_RED));
        canvas.drawLines(pts10, StockPaint.getLinePaint(StockPaint.STOCK_GREEN));
    }

    /**
     * 文字信息
     */
    private void drawText(Canvas canvas, float maxTurnover) {
        Paint textPaint = StockPaint.getTextPaint(Paint.Align.LEFT, 20);
        Paint.FontMetrics fontMetrics = textPaint.getFontMetrics();
        float temp = fontMetrics.bottom - fontMetrics.top;
        canvas.drawText(maxTurnover + "手", left, top + temp, textPaint);
    }
}