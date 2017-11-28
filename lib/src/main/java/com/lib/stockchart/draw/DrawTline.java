package com.lib.stockchart.draw;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
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

    private int left;
    private int top;
    private int right;
    private int bottomF;
    private int width;
    private int heightF;

    @Override
    public void onDrawInit(int left1, int top1, int right1, int bottom1, int width1, int height1) {

        // 测试, 高度
        final int weightTop = EntryManager.getInstance().getWeightTop();
        final int weightDown = EntryManager.getInstance().getWeightDown();
        final int weightSum = weightTop + weightDown;
        final int height = height1 / weightSum;

        // 内边距
        left = left1;
        top = top1;
        right = right1;
        bottomF = height * weightTop;
        width = width1;
        heightF = bottomF - top;
        Log.e("temp", "left = " + left + ", top = " + top + ", right = " + right + ", bottom = " + bottomF);
    }

    @Override
    public void onDrawNull(Canvas canvas) {

        Log.e("DrawTline", "onDrawNull ==> 1");
        if (RenderManager.getInstance().getRenderModel() == RenderManager.MODEL_KLINE_TURNOVER)
            return;
        Log.e("DrawTline", "onDrawNull ==> 2");

        canvas.save();
        // canvas.clipRect(left, top, right, bottom);
        drawBackground(canvas, -1, -1, -1, true);
        canvas.restore();
    }

    @Override
    public void onDrawData(BaseRender render, Canvas canvas, int pointCount, int pointBegin, int pointEnd, float minPrice, float maxPrice, float maxTurnover, float xHighligh, float yHighligh, float xoffsetLeft, float xoffsetRight) {
        //  Log.e("DrawKline", "onDrawData ==> pointSum = " + pointSum + ", pointBegin = " + pointBegin + ", pointEnd = " + pointEnd + ", minPrice = " + minPrice + ", maxPrice = " + maxPrice + ", maxTurnover = " + maxTurnover);

        if (RenderManager.getInstance().getRenderModel() == RenderManager.MODEL_KLINE_TURNOVER)
            return;

        canvas.save();
        // canvas.clipRect(left, top, right, bottom);

        // 1.边框
        drawBackground(canvas, pointCount, pointBegin, pointEnd, false);
        // 3.mad
        drawMadline(canvas, pointCount, pointBegin, pointEnd);
        // 4.价格
        drawPrice(canvas, minPrice, maxPrice);
        // 高亮坐标
        drawHightlight(canvas, xHighligh, yHighligh);

        canvas.restore();
    }

    /**
     * 高亮
     */
    private void drawHightlight(Canvas canvas, float xHighligh, float yHighligh) {

        if (xHighligh == -1f || yHighligh == -1f) return;

//        float[] pointHighlightX = EntryManager.getInstance().getPointHighlightX();
//        final float x = pointHighlightX[1];
//        if (X_AXIS[0] <= x && x <= X_AXIS[2]) {
//
//            Log.e("DrawKline", "drawHightlight ==> pointIndex = " + pointIndex);
//            EntryManager.getInstance().setPointHighlight(pointIndex);

//            final float y = EntryManager.getInstance().getPointHighlightY()[1];
        final float boardPadding = EntryManager.getInstance().getBoardPadding();
        // 横线
        canvas.drawLine(left + boardPadding, yHighligh, right - boardPadding, yHighligh, StockPaint.getLinePaint(Color.BLACK));
        // 竖线
        canvas.drawLine(xHighligh, top + boardPadding, xHighligh, bottomF - boardPadding - EntryManager.getInstance().getXlabelHeight(), StockPaint.getLinePaint(Color.BLACK));
//        }
    }

    /**
     * 背景
     *
     * @param canvas
     * @param nullData
     * @param entryBegin
     * @param entryEnd
     */
    private void drawBackground(Canvas canvas, int entryCount, int entryBegin, int entryEnd, boolean nullData) {

        // X轴显示区域高度
        final float xlabelHeight = EntryManager.getInstance().getXlabelHeight();
        // 图标边框和信息的内边距
        final float SPACE = EntryManager.getInstance().getBoardPadding();
        canvas.drawRect(left - SPACE, top - SPACE, right + SPACE, bottomF + SPACE - xlabelHeight, StockPaint.getBorderPaint(3));

        // 4条横线 - 虚线
        float temp = (bottomF - top - xlabelHeight) / 5;
        for (int i = 1; i < 5; i++) {
            float startX = left - SPACE;
            float Y = top + i * temp;
            float stopX = right + SPACE;
            canvas.drawLine(startX, Y, stopX, Y, StockPaint.getDashPaint());
        }

        // 4条竖线 - 虚线
        float temp2 = (right - left) / 5;
        for (int i = 1; i < 5; i++) {
            float startY = top - SPACE;
            float x = left + i * temp2;
            float stopY = bottomF + SPACE - xlabelHeight;
            canvas.drawLine(x, startY, x, stopY, StockPaint.getDashPaint());
        }

        if (nullData) {
            // 文字交易量
            final String hintLoadStr = EntryManager.getInstance().getHintLoadStr();
            canvas.drawText(hintLoadStr, width / 2, heightF / 2, StockPaint.getTextPaint(Paint.Align.CENTER, 30));
        } else {

            List<Entry> entryList = EntryManager.getInstance().getEntryList();

            final Paint.FontMetrics fontMetrics = StockPaint.getTextPaint(Paint.Align.LEFT, 20).getFontMetrics();
            final float fontHeight = fontMetrics.bottom - fontMetrics.top;

            float y = bottomF - (xlabelHeight > fontHeight ? xlabelHeight - fontHeight : 0);

            final int spet = entryCount / 5;
            final int min = Math.min(entryBegin + entryCount, entryEnd);
            for (int i = entryBegin; i < min; i++) {

                // 第1个
                if (i == entryBegin) {
                    String xLabelMin = entryList.get(i).getXLabel();
                    canvas.drawText(xLabelMin, left, y, StockPaint.getTextPaint(Paint.Align.LEFT, 20));
                }
                // 第6个
                else if (i == (min - 1)) {
                    String xLabelMax = entryList.get(i).getXLabel();
                    canvas.drawText(xLabelMax, right, y, StockPaint.getTextPaint(Paint.Align.RIGHT, 20));
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
    private void drawMadline(Canvas canvas, int pointCount, int pointBegin, int pointEnd) {

        final List<Entry> entryList = EntryManager.getInstance().getEntryList();
        final int pointWidth = EntryManager.getInstance().getPointWidth();

        // 5日均线
        final float[] pts5 = new float[(pointCount+1) * 4];
        // 10日均线
        final float[] pts10 = new float[(pointCount+1) * 4];
        // 20日均线
        final float[] pts20 = new float[(pointCount+1) * 4];

        for (int i = pointBegin; i <= pointEnd; i++) {

            final Entry entry = entryList.get(i);

            final float tempx = entry.getxLabelReal();
            final float x = tempx + pointWidth / 2;
            final float y1 = entry.getMa5Real();
            final float y2 = entry.getMa10Real();
            final float y3 = entry.getMa20Real();
            Log.e("lll", "i = " + (i - 1) + ", x = " + x + ", y1 = " + y1 + ", y2 = " + y2 + ", y3 = " + y3);

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

        Paint textPaint = StockPaint.getTextPaint(Paint.Align.LEFT, 20);
        // 最高价
        final Paint.FontMetrics fontMetrics = textPaint.getFontMetrics();
        float y1 = top + EntryManager.getInstance().getBoardPadding() + (fontMetrics.bottom - fontMetrics.top) / 2;
        canvas.drawText(maxPrice + "元", left, y1, textPaint);
        // 最低价
        float y2 = bottomF - EntryManager.getInstance().getXlabelHeight() - EntryManager.getInstance().getBoardPadding();
        canvas.drawText(minPrice + "元", left, y2, textPaint);
    }
}