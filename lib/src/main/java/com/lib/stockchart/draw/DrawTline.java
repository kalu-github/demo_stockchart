package com.lib.stockchart.draw;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;

import com.lib.stockchart.entry.Entry;
import com.lib.stockchart.entry.EntryManager;
import com.lib.stockchart.paint.StockPaint;
import com.lib.stockchart.render.BaseRender;
import com.lib.stockchart.render.RenderManager;

import java.util.ArrayList;
import java.util.List;

/**
 * description: 分时图
 * created by kalu on 2017/11/9 21:04
 */
public class DrawTline implements IDraw {

    private float left;
    private float top;
    private float right;
    private float bottom;
    private float width;
    private float height;

    // 5日均线, 10日均线, 20日均线
    private float[] pts5, pts10, pts20;

    @Override
    public void onDrawInit(int left1, int top1, int right1, int bottom1, int width1, int height1, float xlabelHeight, float boardPadding) {

        // 测试, 高度
        final int weightTop = EntryManager.getInstance().getWeightTop();
        final int weightDown = EntryManager.getInstance().getWeightDown();
        final int weightSum = weightTop + weightDown;
        final int height = height1 / weightSum;

        // 内边距
        this.left = left1 + boardPadding;
        this.top = top1 + boardPadding;
        this.right = right1 - boardPadding;
        this.bottom = height * weightTop - xlabelHeight - boardPadding;
        this.width = width1;
        this.height = bottom - top;
        //Log.e("temp", "left = " + left + ", top = " + top + ", right = " + right + ", bottom = " + bottom);
    }

    @Override
    public void onDrawNull(Canvas canvas, String str, float xlabelHeight, float boardPadding) {

        if (RenderManager.getInstance().getRenderModel() == RenderManager.MODEL_KLINE_TURNOVER)
            return;

        canvas.save();
        drawBackground(canvas, str, boardPadding);
        canvas.restore();
    }

    @Override
    public void onDrawData(BaseRender render, Canvas canvas, int pointMax, int indexBegin, int indexEnd, float minPrice, float maxPrice, float maxTurnover, float xHighligh, float yHighligh, float xoffsetLeft, float xoffsetRight, float xlabelHeight, float boardPadding) {
        //  Log.e("DrawKline", "onDrawData ==> pointSum = " + pointSum + ", pointBegin = " + pointBegin + ", pointEnd = " + pointEnd + ", minPrice = " + minPrice + ", maxPrice = " + maxPrice + ", maxTurnover = " + maxTurnover);

        if (RenderManager.getInstance().getRenderModel() == RenderManager.MODEL_KLINE_TURNOVER)
            return;

        final List<Entry> entryList = EntryManager.getInstance().getEntryList();
        if (null == entryList || entryList.size() <= 0) return;

        canvas.save();
        // canvas.clipRect(left, top, right, bottom);

        final int pointWidth = EntryManager.getInstance().getPointWidth();

        // 边框
        drawBackground(canvas, null, boardPadding);
        // X轴
        drawXlabel(canvas, boardPadding);

        final Entry entryBegin = entryList.get(0);
        final Entry entryEnd = entryList.get(entryList.size() - 1);

        for (int i = indexBegin; i <= indexEnd; i++) {

            final Entry entry = entryList.get(i);
            if (null == entry) continue;

            // mad
            drawMadline(canvas, entry, i, pointWidth, indexBegin, indexEnd, xoffsetLeft, xoffsetRight);
            // 高亮
            drawHightlight(canvas, entryBegin, entryEnd, i == 0 ? entry : entryList.get(i - 1), entry, pointWidth, i, xHighligh, yHighligh, indexBegin, indexEnd, boardPadding);
        }

        // 价格
        drawPrice(canvas, minPrice, maxPrice);

        canvas.restore();
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
            final float y = entryBegin.getOpenReal();
            canvas.drawLine(left, y, right, y, StockPaint.getLinePaint(Color.BLACK));
            // 竖线
            final float x = boardPadding + left + pointWidth / 2;
            canvas.drawLine(x, top, x, bottom, StockPaint.getLinePaint(Color.BLACK));
        } else if (i == pointBegin && xHighligh >= xEnd) {
            // 横线
            final float y = entryEnd.getOpenReal();
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
                final float y = entry.getOpenReal();
                canvas.drawLine(left, y, right, y, StockPaint.getLinePaint(Color.BLACK));
                // 竖线
                canvas.drawLine(x1, top, x1, bottom, StockPaint.getLinePaint(Color.BLACK));
            }
        }
    }

    /**
     * 画X轴坐标
     */
    private void drawXlabel(Canvas canvas, float boardPadding) {

        final Paint.FontMetrics fontMetrics = StockPaint.getTextPaint(Paint.Align.LEFT, 20).getFontMetrics();
        final float fontHeight = fontMetrics.bottom - fontMetrics.top;
        float y = bottom + fontHeight * 4 / 3;

        canvas.drawText("9:00", left - boardPadding, y, StockPaint.getTextPaint(Paint.Align.LEFT, 20));
        canvas.drawText("10:30", left + width / 4, y, StockPaint.getTextPaint(Paint.Align.CENTER, 20));
        canvas.drawText("11:30/13:00", left + 2 * (width / 4), y, StockPaint.getTextPaint(Paint.Align.CENTER, 20));
        canvas.drawText("14:00", left + 3 * (width / 4), y, StockPaint.getTextPaint(Paint.Align.CENTER, 20));
        canvas.drawText("15:00", right + boardPadding, y, StockPaint.getTextPaint(Paint.Align.RIGHT, 20));
    }

    /**
     * 背景
     */
    private void drawBackground(Canvas canvas, String str, float boardPadding) {

        // X轴显示区域高度
        final float boardTemp = 2 * boardPadding;
        canvas.drawRect(left - boardTemp, top - boardTemp, right + boardTemp, bottom + boardTemp, StockPaint.getBorderPaint(3));

        // 4条横线 - 虚线
        float temp = height / 4;
        final float x1 = left + 2;
        final float x2 = x1 + 5;

        for (int j = 1; j < 4; j++) {

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

        if (TextUtils.isEmpty(str)) return;
        // 文字交易量
        canvas.drawText(str, right - width / 2, bottom - height / 2, StockPaint.getTextPaint(Paint.Align.CENTER, 30));
    }

    /**
     * MAD
     */
    private void drawMadline(Canvas canvas, Entry entry, int j, int pointWidth, int indexBegin, int indexEnd, float xoffsetLeft, float xoffsetRight) {

        if (null == pts5 && null == pts10 && null == pts20) {
            final int size = 4 * (indexEnd + 1);
            pts5 = new float[size];
            pts10 = new float[size];
            pts20 = new float[size];
        }

        final float tempx = entry.getxLabelReal();
        final float x = tempx + pointWidth / 2 + xoffsetLeft + xoffsetRight;
        Log.e("DrawTline", "drawMadline ==> j = " + j + ", indexBegin = " + indexBegin + ", indexEnd = " + indexEnd + ", x = " + x);
        final float y1 = entry.getMa5Real();
        final float y2 = entry.getMa10Real();
        final float y3 = entry.getMa20Real();

        //  final int tempi = j - indexBegin;
        if (j == indexBegin) {
            pts5[4 * j + 0] = x;
            pts5[4 * j + 1] = y1;
            pts5[4 * j + 2] = x;
            pts5[4 * j + 3] = y1;
            pts10[4 * j + 0] = x;
            pts10[4 * j + 1] = y2;
            pts10[4 * j + 2] = x;
            pts10[4 * j + 3] = y2;
            pts20[4 * j + 0] = x;
            pts20[4 * j + 1] = y3;
            pts20[4 * j + 2] = x;
            pts20[4 * j + 3] = y3;
        } else {
            pts5[4 * j + 0] = pts5[4 * (j - 1) + 2];
            pts5[4 * j + 1] = pts5[4 * (j - 1) + 3];
            pts5[4 * j + 2] = x;
            pts5[4 * j + 3] = y1;
            pts10[4 * j + 0] = pts10[4 * (j - 1) + 2];
            pts10[4 * j + 1] = pts10[4 * (j - 1) + 3];
            pts10[4 * j + 2] = x;
            pts10[4 * j + 3] = y2;
            pts20[4 * j + 0] = pts20[4 * (j - 1) + 2];
            pts20[4 * j + 1] = pts20[4 * (j - 1) + 3];
            pts20[4 * j + 2] = x;
            pts20[4 * j + 3] = y3;
        }

        if (j != indexEnd) return;
        canvas.drawLines(pts5, StockPaint.getLinePaint(StockPaint.STOCK_RED));
        canvas.drawLines(pts10, StockPaint.getLinePaint(StockPaint.STOCK_GREEN));
        canvas.drawLines(pts20, StockPaint.getLinePaint(Color.BLUE));
    }

    /**
     * 价格
     */
    private void drawPrice(Canvas canvas, float minPrice, float maxPrice) {

        if (RenderManager.getInstance().getRenderModel() == RenderManager.MODEL_KLINE_TURNOVER)
            return;

        Paint textPaint = StockPaint.getTextPaint(Paint.Align.LEFT, 20);
        // 最高价
        final Paint.FontMetrics fontMetrics = textPaint.getFontMetrics();
        final float fontHeight = fontMetrics.bottom - fontMetrics.top;
        float y1 = top + fontHeight * 2 / 3;
        canvas.drawText(maxPrice + "元", left, y1, textPaint);
        // 最低价
        float y2 = bottom - fontHeight / 5;
        canvas.drawText(minPrice + "元", left, y2, textPaint);
    }
}