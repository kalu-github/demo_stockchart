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
 * description: K线图
 * created by kalu on 2017/11/9 21:04
 */
public class DrawKline implements IDraw {

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

        if (RenderManager.getInstance().getRenderModel() == RenderManager.MODEL_TLINE_TURNOVER)
            return;

        canvas.save();
        // canvas.clipRect(left, top, right, bottom);
        drawBackground(canvas, -1, -1, -1, true);
        canvas.restore();
    }

    @Override
    public void onDrawData(BaseRender render, Canvas canvas, int pointCount, int pointBegin, int pointEnd, float minPrice, float maxPrice, float maxTurnover, float xHighligh, float yHighligh) {
        //  Log.e("DrawKline", "onDrawData ==> pointSum = " + pointSum + ", pointBegin = " + pointBegin + ", pointEnd = " + pointEnd + ", minPrice = " + minPrice + ", maxPrice = " + maxPrice + ", maxTurnover = " + maxTurnover);

        if (RenderManager.getInstance().getRenderModel() == RenderManager.MODEL_TLINE_TURNOVER)
            return;

        canvas.save();
        // canvas.clipRect(left, top, right, bottom);

        // 1.边框
        drawBackground(canvas, pointCount, pointBegin, pointEnd, false);
        // 2.K线
        drawKline(canvas, pointBegin, pointEnd);
        // 3.mad
        drawMadline(canvas, pointCount, pointBegin, pointEnd);
        // 4.价格
        drawPrice(canvas, minPrice, maxPrice);
        // 高亮坐标
        drawHightlight(canvas, xHighligh, yHighligh, pointCount, pointBegin, pointEnd);

        canvas.restore();
    }

    /**
     * 高亮
     */
    private void drawHightlight(Canvas canvas, float xHighligh, float yHighligh, int pointCount, int pointBegin, int pointEnd) {
        Log.e("DrawKline", "drawHightlight ==> xHighligh = " + xHighligh + ", yHighligh = " + yHighligh + ", pointBegin = " + pointBegin + ", pointEnd = " + pointEnd);

        if (xHighligh == -1f || yHighligh == -1f) return;

        final List<Entry> entryList = EntryManager.getInstance().getEntryList();
        final int pointWidth = EntryManager.getInstance().getPointWidth();

        final Entry entryBegin = entryList.get(pointBegin - 1);
        final float xBegin = entryBegin.getxLabelReal() + pointWidth / 2;

        final Entry entryEnd = entryList.get(pointEnd - 1);
        final float xEnd = entryEnd.getxLabelReal() + pointWidth / 2;
        Log.e("DrawKline", "drawHightlight ==> xBegin = " + xBegin + ", xEnd = " + xEnd);

        final int boardPadding = EntryManager.getInstance().getBoardPadding();

        if (xHighligh <= xBegin) {
            // 横线
            final float y = entryList.get(pointBegin - 1).getOpenReal();
            canvas.drawLine(left + boardPadding, y, right - boardPadding, y, StockPaint.getLinePaint(Color.BLACK));
            // 竖线
            final float x = EntryManager.getInstance().getBoardPadding() + left + pointWidth / 2;
            canvas.drawLine(x, top + boardPadding, x, bottomF - boardPadding - EntryManager.getInstance().getXlabelHeight(), StockPaint.getLinePaint(Color.BLACK));
        } else if (xHighligh >= xEnd) {
            // 横线
            final float y = entryList.get(pointEnd - 1).getOpenReal();
            canvas.drawLine(left + boardPadding, y, right - boardPadding, y, StockPaint.getLinePaint(Color.BLACK));
            // 竖线
            canvas.drawLine(xEnd, top + boardPadding, xEnd, bottomF - boardPadding - EntryManager.getInstance().getXlabelHeight(), StockPaint.getLinePaint(Color.BLACK));
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
                    final float y = entryList.get(i - 1).getOpenReal();
                    canvas.drawLine(left + boardPadding, y, right - boardPadding, y, StockPaint.getLinePaint(Color.BLACK));
                    // 竖线
                    canvas.drawLine(x1, top + boardPadding, x1, bottomF - boardPadding - EntryManager.getInstance().getXlabelHeight(), StockPaint.getLinePaint(Color.BLACK));
                    break;
                }
            }
        }
    }

    /**
     * 背景
     */
    private void drawBackground(Canvas canvas, int entryCount, int entryBegin, int entryEnd, boolean nullData) {

        // X轴显示区域高度
        final int xlabelHeight = EntryManager.getInstance().getXlabelHeight();
        // 图标边框和信息的内边距
        final int SPACE = EntryManager.getInstance().getBoardPadding();
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
     * 蜡烛图
     */
    private void drawKline(Canvas canvas, int pointBegin, int pointEnd) {

        List<Entry> entryList = EntryManager.getInstance().getEntryList();

        final int pointWidth = EntryManager.getInstance().getPointWidth();

        // 画笔宽度
        StockPaint.setPaintWidth(5);

        final int offsetLeft = EntryManager.getInstance().getXoffsetLeft();
        final int offsetRight = EntryManager.getInstance().getXoffsetRight();

        for (int i = pointBegin; i <= pointEnd; i++) {
            Entry entry = entryList.get(i);

            // 1.1 柱子
            final float top = entry.getOpen() > entry.getClose() ? entry.getOpenReal() : entry.getCloseReal();
            final float bottom = entry.getOpen() > entry.getClose() ? entry.getCloseReal() : entry.getOpenReal();

            float left = entry.getxLabelReal() + offsetLeft + offsetRight;
            float right = left + pointWidth;
            // Log.e("DrawKline", "drawKline ==> pointBegin = " + pointBegin + ", pointEnd = " + pointEnd + ", left = " + left + ", right = " + right);

            // 涨停、跌停、或不涨不跌的一字板
            boolean isEqaual = Math.abs(top - bottom) < 1.f;
            int color = isEqaual ? StockPaint.STOCK_RED : StockPaint.STOCK_GREEN;
            StockPaint.setPaintColor(color);
            canvas.drawRect(left, top, right, isEqaual ? (bottom + 2) : bottom, StockPaint.getTurnoverPaint());

            // 1.2 上阴线
            final float top2 = entry.getHighReal();
            final float x2 = (right - left) / 2 + left;
            canvas.drawLine(x2, top2, x2, top, StockPaint.getLinePaint(color));

            // 1.3 下阴线
            final float top3 = entry.getLowReal();
            canvas.drawLine(x2, bottom, x2, top3, StockPaint.getLinePaint(color));
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

        final int offsetLeft = EntryManager.getInstance().getXoffsetLeft();
        final int offsetRight = EntryManager.getInstance().getXoffsetRight();

        for (int i = pointBegin; i <= pointEnd; i++) {

            final Entry entry = entryList.get(i);

            final int tempx = entry.getxLabelReal();
            final float x = tempx + pointWidth / 2 + offsetLeft + offsetRight;
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
