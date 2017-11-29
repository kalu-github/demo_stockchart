package com.lib.stockchart.draw;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.TextUtils;
import android.util.Log;

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
        // canvas.clipRect(left, top, right, bottom);
        drawBackground(canvas, -1, -1, -1, str, boardPadding);
        canvas.restore();
    }

    @Override
    public void onDrawData(BaseRender render, Canvas canvas, int pointCount, int pointBegin, int pointEnd, float minPrice, float maxPrice, float maxTurnover, float xHighligh, float yHighligh, float xoffsetLeft, float xoffsetRight, float xlabelHeight, float boardPadding) {
        //  Log.e("DrawKline", "onDrawData ==> pointSum = " + pointSum + ", pointBegin = " + pointBegin + ", pointEnd = " + pointEnd + ", minPrice = " + minPrice + ", maxPrice = " + maxPrice + ", maxTurnover = " + maxTurnover);

        if (RenderManager.getInstance().getRenderModel() == RenderManager.MODEL_KLINE_TURNOVER)
            return;

        canvas.save();
        // canvas.clipRect(left, top, right, bottom);

        // 1.边框
        drawBackground(canvas, pointCount, pointBegin, pointEnd, null, boardPadding);
        // 3.mad
        drawMadline(canvas, pointCount, pointBegin, pointEnd, xoffsetLeft, xoffsetRight);
        // 4.价格
        drawPrice(canvas, minPrice, maxPrice);
        // 高亮坐标
        drawHightlight(canvas, xHighligh, yHighligh, pointBegin, pointEnd, boardPadding);

        canvas.restore();
    }

    /**
     * 高亮
     */
    private void drawHightlight(Canvas canvas, float xHighligh, float yHighligh, int pointBegin, int pointEnd, float boardPadding) {

        if (RenderManager.getInstance().getRenderModel() == RenderManager.MODEL_KLINE_TURNOVER)
            return;

        if (xHighligh == -1f || yHighligh == -1f) return;

        final List<Entry> entryList = EntryManager.getInstance().getEntryList();
        final int pointWidth = EntryManager.getInstance().getPointWidth();

        final Entry entryBegin = entryList.get(pointBegin);
        final float xBegin = entryBegin.getxLabelReal() + pointWidth / 2;

        final Entry entryEnd = entryList.get(pointEnd);
        final float xEnd = entryEnd.getxLabelReal() + pointWidth / 2;

        if (xHighligh <= xBegin) {
            // 横线
            final float y = entryList.get(pointBegin).getOpenReal();
            canvas.drawLine(left, y, right, y, StockPaint.getLinePaint(Color.BLACK));
            // 竖线
            final float x = boardPadding + left + pointWidth / 2;
            canvas.drawLine(x, top, x, bottom, StockPaint.getLinePaint(Color.BLACK));
        } else if (xHighligh >= xEnd) {
            // 横线
            final float y = entryList.get(pointEnd).getOpenReal();
            canvas.drawLine(left, y, right, y, StockPaint.getLinePaint(Color.BLACK));
            // 竖线
            canvas.drawLine(xEnd, top, xEnd, bottom, StockPaint.getLinePaint(Color.BLACK));
        } else {
            for (int i = pointBegin; i <= pointEnd; i++) {

                final Entry entry1 = entryList.get(i);
                final float tempx1 = entry1.getxLabelReal();
                final float x1 = tempx1 + pointWidth / 2;

                final Entry entry2 = entryList.get(i + 1);
                final float tempx2 = entry2.getxLabelReal();
                final float x2 = tempx2 + pointWidth / 2;

                if (xHighligh > x1 && xHighligh < x2) {
                    // 横线
                    final float y = entryList.get(i).getOpenReal();
                    canvas.drawLine(left, y, right, y, StockPaint.getLinePaint(Color.BLACK));
                    // 竖线
                    canvas.drawLine(x1, top, x1, bottom, StockPaint.getLinePaint(Color.BLACK));
                    break;
                }
            }
        }
    }

    /**
     * 背景
     */
    private void drawBackground(Canvas canvas, int entryCount, int entryBegin, int entryEnd, String str, float boardPadding) {

        if (RenderManager.getInstance().getRenderModel() == RenderManager.MODEL_KLINE_TURNOVER)
            return;

        // X轴显示区域高度
        final float boardTemp = 2 * boardPadding;
        canvas.drawRect(left - boardTemp, top - boardTemp, right + boardTemp, bottom + boardTemp, StockPaint.getBorderPaint(3));

        // 4条横线 - 虚线
        float temp = height / 5;
        final float x1 = left + 2;
        final float x2 = x1 + 5;

        for (int j = 1; j < 5; j++) {

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
        float temp2 = width / 5;
        final float y1 = top + 2;
        final float y2 = top + 5;

        for (int j = 1; j < 5; j++) {

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

        if (!TextUtils.isEmpty(str)) {
            // 文字交易量
            canvas.drawText(str, right - width / 2, bottom - height / 2, StockPaint.getTextPaint(Paint.Align.CENTER, 30));
        } else {

            List<Entry> entryList = EntryManager.getInstance().getEntryList();

            final Paint.FontMetrics fontMetrics = StockPaint.getTextPaint(Paint.Align.LEFT, 20).getFontMetrics();
            final float fontHeight = fontMetrics.bottom - fontMetrics.top;
            float y = bottom + fontHeight * 4 / 3;

            final int spet = entryCount / 5;
            for (int i = entryBegin; i <= entryEnd; i++) {

                // 第1个
                if (i == entryBegin) {
                    String xLabelMin = entryList.get(i).getXLabel();
                    canvas.drawText(xLabelMin, left - boardPadding, y, StockPaint.getTextPaint(Paint.Align.LEFT, 20));
                }
                // 第6个
                else if (i == entryEnd) {
                    String xLabelMax = entryList.get(i).getXLabel();
                    canvas.drawText(xLabelMax, right + boardPadding, y, StockPaint.getTextPaint(Paint.Align.RIGHT, 20));
                }
                // 第2个
                else if (entryBegin + spet == i) {
                    String xLabel = entryList.get(i).getXLabel();
                    canvas.drawText(xLabel, left + width / 5, y, StockPaint.getTextPaint(Paint.Align.CENTER, 20));
                }
                // 第3个
                else if (entryBegin + 2 * spet == i) {
                    String xLabel = entryList.get(i).getXLabel();
                    canvas.drawText(xLabel, left + 2 * (width / 5), y, StockPaint.getTextPaint(Paint.Align.CENTER, 20));
                }
                // 第4个
                else if (entryBegin + 3 * spet == i) {
                    String xLabel = entryList.get(i).getXLabel();
                    canvas.drawText(xLabel, left + 3 * (width / 5), y, StockPaint.getTextPaint(Paint.Align.CENTER, 20));
                }
                // 第5个
                else if (entryBegin + 4 * spet == i) {
                    String xLabel = entryList.get(i).getXLabel();
                    canvas.drawText(xLabel, left + 4 * (width / 5), y, StockPaint.getTextPaint(Paint.Align.CENTER, 20));
                }
            }
        }
    }

    /**
     * MAD
     */
    private void drawMadline(Canvas canvas, int pointCount, int pointBegin, int pointEnd, float xoffsetLeft, float xoffsetRight) {

        if (RenderManager.getInstance().getRenderModel() == RenderManager.MODEL_KLINE_TURNOVER)
            return;

        final List<Entry> entryList = EntryManager.getInstance().getEntryList();
        final int pointWidth = EntryManager.getInstance().getPointWidth();

        // 5日均线
        final float[] pts5 = new float[(pointCount + 1) * 4];
        // 10日均线
        final float[] pts10 = new float[(pointCount + 1) * 4];
        // 20日均线
        final float[] pts20 = new float[(pointCount + 1) * 4];

        for (int i = pointBegin; i <= pointEnd; i++) {

            final Entry entry = entryList.get(i);

            final float tempx = entry.getxLabelReal();
            final float x = tempx + pointWidth / 2 + xoffsetLeft + xoffsetRight;
            final float y1 = entry.getMa5Real();
            final float y2 = entry.getMa10Real();
            final float y3 = entry.getMa20Real();
            //  Log.e("lll", "i = " + (i - 1) + ", x = " + x + ", y1 = " + y1 + ", y2 = " + y2 + ", y3 = " + y3);

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
                pts20[4 * tempi + 0] = x;
                pts20[4 * tempi + 1] = y3;
                pts20[4 * tempi + 2] = x;
                pts20[4 * tempi + 3] = y3;
            } else {
                pts5[4 * tempi + 0] = pts5[4 * (tempi - 1) + 2];
                pts5[4 * tempi + 1] = pts5[4 * (tempi - 1) + 3];
                pts5[4 * tempi + 2] = x;
                pts5[4 * tempi + 3] = y1;
                pts10[4 * tempi + 0] = pts10[4 * (tempi - 1) + 2];
                pts10[4 * tempi + 1] = pts10[4 * (tempi - 1) + 3];
                pts10[4 * tempi + 2] = x;
                pts10[4 * tempi + 3] = y2;
                pts20[4 * tempi + 0] = pts20[4 * (tempi - 1) + 2];
                pts20[4 * tempi + 1] = pts20[4 * (tempi - 1) + 3];
                pts20[4 * tempi + 2] = x;
                pts20[4 * tempi + 3] = y3;
            }
        }

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