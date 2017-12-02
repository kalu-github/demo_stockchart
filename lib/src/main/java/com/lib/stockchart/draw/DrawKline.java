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
 * description: K线图
 * created by kalu on 2017/11/9 21:04
 */
public class DrawKline implements IDraw {

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
        //  Log.e("DrawKline", "left = " + left + ", top = " + top + ", right = " + right + ", bottom = " + bottom);
    }

    @Override
    public void onDrawNull(Canvas canvas, String str, float xlabelHeight, float boardPadding) {

        if (RenderManager.getInstance().getRenderModel() == RenderManager.MODEL_TLINE_TURNOVER)
            return;

        //  Log.e("DrawKline", "onDrawNull ==> str = " + str + ", xlabelHeight = " + xlabelHeight + ", boardPadding = " + boardPadding);

        canvas.save();
        drawBackground(canvas, str, boardPadding);
        canvas.restore();
    }

    @Override
    public void onDrawData(BaseRender render, Canvas canvas, int pointMax, int indexBegin, int indexEnd, float minPrice, float maxPrice, float maxTurnover, float xHighligh, float yHighligh, float xoffsetLeft, float xoffsetRight, float xlabelHeight, float boardPadding) {

        if (RenderManager.getInstance().getRenderModel() == RenderManager.MODEL_TLINE_TURNOVER)
            return;
        //  Log.e("DrawKline", "onDrawData ==>");

        canvas.save();
        // canvas.clipRect(left, top, right, bottom);

        final List<Entry> entryList = EntryManager.getInstance().getEntryList();
        final int pointWidth = EntryManager.getInstance().getPointWidth();

        // 边框
        drawBackground(canvas, null, boardPadding);

        for (int i = indexBegin; i <= indexEnd; i++) {

            final Entry entry = entryList.get(i);

            // X轴, 需要循环
            drawXlabel(canvas, i, indexBegin, indexEnd, boardPadding);
            // 2.K线, 需要循环
            drawKline(canvas, entry, pointWidth, xoffsetLeft, xoffsetRight);
            // 3.mad, 需要循环
            drawMadline(canvas, entry, i, pointMax, indexBegin, indexEnd, xoffsetLeft, xoffsetRight);
            // 高亮坐标, 需要循环
            drawHightlight(canvas, entryList, pointWidth, i, xHighligh, yHighligh, indexBegin, indexEnd, boardPadding);
        }

        // 4.价格
        drawPrice(canvas, minPrice, maxPrice);
        canvas.restore();
    }

    /**
     * 高亮
     */
    private void drawHightlight(Canvas canvas, List<Entry> entryList, int pointWidth, int i, float xHighligh, float yHighligh, int pointBegin, int pointEnd, float boardPadding) {

        if (RenderManager.getInstance().getRenderModel() == RenderManager.MODEL_TLINE_TURNOVER)
            return;

        if (xHighligh == -1f || yHighligh == -1f) return;

        final Entry entryBegin = entryList.get(pointBegin);
        final float xBegin = entryBegin.getxLabelReal() + pointWidth / 2;

        final Entry entryEnd = entryList.get(pointEnd);
        final float xEnd = entryEnd.getxLabelReal() + pointWidth / 2;

        if (i == pointBegin && xHighligh <= xBegin) {
            // 横线
            final float y = entryList.get(pointBegin).getOpenReal();
            canvas.drawLine(left, y, right, y, StockPaint.getLinePaint(Color.BLACK));
            // 竖线
            final float x = boardPadding + left + pointWidth / 2;
            canvas.drawLine(x, top, x, bottom, StockPaint.getLinePaint(Color.BLACK));
        } else if (i == pointBegin && xHighligh >= xEnd) {
            // 横线
            final float y = entryList.get(pointEnd).getOpenReal();
            canvas.drawLine(left, y, right, y, StockPaint.getLinePaint(Color.BLACK));
            // 竖线
            canvas.drawLine(xEnd, top, xEnd, bottom, StockPaint.getLinePaint(Color.BLACK));
        } else {
            final Entry entry1 = entryList.get(i - 1);
            final float tempx1 = entry1.getxLabelReal();
            final float x1 = tempx1 + pointWidth / 2;

            final Entry entry2 = entryList.get(i);
            final float tempx2 = entry2.getxLabelReal();
            final float x2 = tempx2 + pointWidth / 2;
            if (xHighligh > x1 && xHighligh < x2) {
                // 横线
                final float y = entryList.get(i).getOpenReal();
                canvas.drawLine(left, y, right, y, StockPaint.getLinePaint(Color.BLACK));
                // 竖线
                canvas.drawLine(x1, top, x1, bottom, StockPaint.getLinePaint(Color.BLACK));
            }
        }
    }

    /**
     * 背景
     */
    private void drawBackground(Canvas canvas, String str, float boardPadding) {

        if (RenderManager.getInstance().getRenderModel() == RenderManager.MODEL_TLINE_TURNOVER)
            return;
        //  Log.e("DrawKline", "drawBackground ==>");

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

        if (TextUtils.isEmpty(str)) return;
        // 文字交易量
        canvas.drawText(str, right - width / 2, bottom - height / 2, StockPaint.getTextPaint(Paint.Align.CENTER, 30));
    }

    /**
     * 画X轴坐标
     */
    private void drawXlabel(Canvas canvas, int i, int indexBegin, int indexEnd, float boardPadding) {
        List<Entry> entryList = EntryManager.getInstance().getEntryList();

        final Paint.FontMetrics fontMetrics = StockPaint.getTextPaint(Paint.Align.LEFT, 20).getFontMetrics();
        final float fontHeight = fontMetrics.bottom - fontMetrics.top;
        float y = bottom + fontHeight * 4 / 3;

        final int spet = (indexEnd - indexBegin + 1) / 5;
        // 第1个
        if (i == indexBegin) {
            String xLabelMin = entryList.get(i).getXLabel();
            canvas.drawText(xLabelMin, left - boardPadding, y, StockPaint.getTextPaint(Paint.Align.LEFT, 20));
        }
        // 第6个
        else if (i == indexEnd) {
            String xLabelMax = entryList.get(i).getXLabel();
            canvas.drawText(xLabelMax, right + boardPadding, y, StockPaint.getTextPaint(Paint.Align.RIGHT, 20));
        }
        // 第2个
        else if (indexBegin + spet == i) {
            String xLabel = entryList.get(i).getXLabel();
            canvas.drawText(xLabel, left + width / 5, y, StockPaint.getTextPaint(Paint.Align.CENTER, 20));
        }
        // 第3个
        else if (indexBegin + 2 * spet == i) {
            String xLabel = entryList.get(i).getXLabel();
            canvas.drawText(xLabel, left + 2 * (width / 5), y, StockPaint.getTextPaint(Paint.Align.CENTER, 20));
        }
        // 第4个
        else if (indexBegin + 3 * spet == i) {
            String xLabel = entryList.get(i).getXLabel();
            canvas.drawText(xLabel, left + 3 * (width / 5), y, StockPaint.getTextPaint(Paint.Align.CENTER, 20));
        }
        // 第5个
        else if (indexBegin + 4 * spet == i) {
            String xLabel = entryList.get(i).getXLabel();
            canvas.drawText(xLabel, left + 4 * (width / 5), y, StockPaint.getTextPaint(Paint.Align.CENTER, 20));
        }
    }

    /**
     * 蜡烛图
     */
    private void drawKline(Canvas canvas, Entry entry, int pointWidth, float xoffsetLeft, float xoffsetRight) {

        if (RenderManager.getInstance().getRenderModel() == RenderManager.MODEL_TLINE_TURNOVER)
            return;

        // 画笔宽度
        StockPaint.setPaintWidth(5);

        // 1.1 柱子
        final float top = entry.getOpen() > entry.getClose() ? entry.getOpenReal() : entry.getCloseReal();
        final float bottom = entry.getOpen() > entry.getClose() ? entry.getCloseReal() : entry.getOpenReal();

        float left = entry.getxLabelReal() + xoffsetLeft + xoffsetRight;
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

    /**
     * MAD
     */
    private void drawMadline(Canvas canvas, Entry entry, int j, int pointMax, int pointBegin, int pointEnd, float xoffsetLeft, float xoffsetRight) {

        if (RenderManager.getInstance().getRenderModel() == RenderManager.MODEL_TLINE_TURNOVER)
            return;

        final int pointWidth = EntryManager.getInstance().getPointWidth();

        if (null == pts5 && null == pts10 && null == pts20) {
            final int size = 4 * (pointMax + 1);
            pts5 = new float[size];
            pts10 = new float[size];
            pts20 = new float[size];
        }

        final float tempx = entry.getxLabelReal();
        final float x = tempx + pointWidth / 2 + xoffsetLeft + xoffsetRight;
        final float y1 = entry.getMa5Real();
        final float y2 = entry.getMa10Real();
        final float y3 = entry.getMa20Real();

        final int tempi = j - pointBegin;
        if (j == pointBegin) {
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

        // 容错处理
        if (pointBegin + pointMax > pointEnd) {
            int realEnd = (pointEnd - pointBegin);
            final float x5 = pts5[4 * realEnd + 2];
            final float y5 = pts5[4 * realEnd + 3];
            final float x10 = pts10[4 * realEnd + 2];
            final float y10 = pts10[4 * realEnd + 3];
            final float x20 = pts20[4 * realEnd + 2];
            final float y20 = pts20[4 * realEnd + 3];

            for (int i = realEnd + 1; i <= pointMax; i++) {
                pts5[4 * i + 0] = x5;
                pts5[4 * i + 1] = y5;
                pts5[4 * i + 2] = x5;
                pts5[4 * i + 3] = y5;
                pts10[4 * i + 0] = x10;
                pts10[4 * i + 1] = y10;
                pts10[4 * i + 2] = x10;
                pts10[4 * i + 3] = y10;
                pts20[4 * i + 0] = x20;
                pts20[4 * i + 1] = y20;
                pts20[4 * i + 2] = x20;
                pts20[4 * i + 3] = y20;
            }
        }

        if (j != pointEnd) return;
        canvas.drawLines(pts5, StockPaint.getLinePaint(StockPaint.STOCK_RED));
        canvas.drawLines(pts10, StockPaint.getLinePaint(StockPaint.STOCK_GREEN));
        canvas.drawLines(pts20, StockPaint.getLinePaint(Color.BLUE));
    }

    /**
     * 价格
     */
    private void drawPrice(Canvas canvas, float minPrice, float maxPrice) {

        if (RenderManager.getInstance().getRenderModel() == RenderManager.MODEL_TLINE_TURNOVER)
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
