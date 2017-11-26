package com.lib.stockchart.draw;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.Log;

import com.lib.stockchart.entry.Entry;
import com.lib.stockchart.entry.EntryManager;
import com.lib.stockchart.paint.StockPaint;
import com.lib.stockchart.render.AbstractRender;

import java.util.ArrayList;

/**
 * description: K线图
 * created by kalu on 2017/11/9 21:04
 */
public class DrawKline implements IDraw {

    // X轴坐标
    private final float[] X_AXIS = new float[4];
    // 5日均线
    private final ArrayList<Float> MAD_5 = new ArrayList();
    // 10日均线
    private final ArrayList<Float> MAD_10 = new ArrayList();

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
        canvas.save();
        // canvas.clipRect(left, top, right, bottom);
        drawBackground(canvas, -1, -1, -1, true);
        canvas.restore();
    }

    @Override
    public void onDrawData(AbstractRender render, Canvas canvas, int pointSum, int pointCount, int pointBegin, int pointEnd, float minPrice, float maxPrice, float maxTurnover, boolean hightLight, int model) {
        Log.e("DrawKline", "onDrawData ==> pointSum = " + pointSum + ", pointBegin = " + pointBegin + ", pointEnd = " + pointEnd + ", minPrice = " + minPrice + ", maxPrice = " + maxPrice + ", maxTurnover = " + maxTurnover);

        canvas.save();
        // canvas.clipRect(left, top, right, bottom);

        // 数据集合
        MAD_5.clear();
        MAD_10.clear();

        // 1.边框
        drawBackground(canvas, pointCount, pointBegin, pointEnd, false);
        // 2.K线
        drawKline(render, canvas, pointBegin, pointEnd, minPrice, maxPrice, hightLight);
        // 3.mad
        drawMadline(canvas);
        // 4.价格
        drawPrice(canvas, minPrice, maxPrice);

        canvas.restore();
    }

    /**
     * 高亮
     */
    private void drawHightlight(Canvas canvas, boolean hightLight, int pointIndex) {

        if (!hightLight) return;

        float[] pointHighlightX = EntryManager.getInstance().getPointHighlightX();
        final float x = pointHighlightX[1];
        if (X_AXIS[0] <= x && x <= X_AXIS[2]) {

            Log.e("DrawKline", "drawHightlight ==> pointIndex = " + pointIndex);
            EntryManager.getInstance().setPointHighlight(pointIndex);

            final float y = EntryManager.getInstance().getPointHighlightY()[1];
            // 横线
            canvas.drawLine(left, y, right, y, StockPaint.getLinePaint(Color.BLACK));
            // 竖线
            canvas.drawLine(x, top, x, bottomF - 50, StockPaint.getLinePaint(Color.BLACK));
        }
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
        final int xaxisHeight = EntryManager.getInstance().getXaxisHeight();
        // 图标边框和信息的内边距
        final int SPACE = EntryManager.getInstance().getPadding();
        canvas.drawRect(left - SPACE, top - SPACE, right + SPACE, bottomF + SPACE - xaxisHeight, StockPaint.getBorderPaint(3));

        // 4条横线 - 虚线
        float temp = (bottomF - top - xaxisHeight) / 5;
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
            float stopY = bottomF + SPACE - xaxisHeight;
            canvas.drawLine(x, startY, x, stopY, StockPaint.getDashPaint());
        }

        if (nullData) {
            // 文字交易量
            final String hintLoadStr = EntryManager.getInstance().getHintLoadStr();
            canvas.drawText(hintLoadStr, width / 2, heightF / 2, StockPaint.getTextPaint(Paint.Align.CENTER, 30));
        } else {

            ArrayList<Entry> entryList = EntryManager.getInstance().getEntryList();

            Paint textPaint4 = StockPaint.getTextPaint(Paint.Align.LEFT, 20);
            Paint.FontMetrics fontMetrics4 = textPaint4.getFontMetrics();
            float temp4 = fontMetrics4.bottom - fontMetrics4.top - 1.5f * SPACE;
            // X轴坐标 - 最小值
            String xLabelMin = entryList.get(entryBegin).getXLabel();
            canvas.drawText(xLabelMin, left, bottomF - temp4, textPaint4);
            // X轴坐标 - 最大值
            String xLabelMax = entryList.get(entryEnd - 1).getXLabel();
            canvas.drawText(xLabelMax, right, bottomF - temp4, StockPaint.getTextPaint(Paint.Align.RIGHT, 20));

            final int spet = entryCount / 5;
            for (int i = entryBegin; i < entryEnd; i++) {

                final Entry entry = entryList.get(i);

                Paint textPaint = StockPaint.getTextPaint(Paint.Align.LEFT, 20);
                Paint.FontMetrics fontMetrics = textPaint.getFontMetrics();
                float temp3 = fontMetrics.bottom - fontMetrics.top - 1.5f * SPACE;
                if (entryBegin + spet == i) {
                    String xLabel = entry.getXLabel();
                    canvas.drawText(xLabel, left + width / 5, bottomF - temp3, StockPaint.getTextPaint(Paint.Align.CENTER, 20));
                } else if (entryBegin + 2 * spet == i) {
                    String xLabel = entry.getXLabel();
                    canvas.drawText(xLabel, left + 2 * (width / 5), bottomF - temp3, StockPaint.getTextPaint(Paint.Align.CENTER, 20));
                } else if (entryBegin + 3 * spet == i) {
                    String xLabel = entry.getXLabel();
                    canvas.drawText(xLabel, left + 3 * (width / 5), bottomF - temp3, StockPaint.getTextPaint(Paint.Align.CENTER, 20));
                } else if (entryBegin + 4 * spet == i) {
                    String xLabel = entry.getXLabel();
                    canvas.drawText(xLabel, left + 4 * (width / 5), bottomF - temp3, StockPaint.getTextPaint(Paint.Align.CENTER, 20));
                }
            }
        }
    }

    /**
     * 蜡烛图
     */
    private void drawKline(AbstractRender render, Canvas canvas, int pointBegin, int pointEnd, float minPrice, float maxPrice, boolean hightLight) {

        ArrayList<Entry> entryList = EntryManager.getInstance().getEntryList();

        // 图标边框和信息的内边距
        final int SPACE = EntryManager.getInstance().getPadding();

        // 画笔宽度
        StockPaint.setPaintWidth(5);
        float priceSum = maxPrice - minPrice;

        final float bottomSrc = bottomF - 50;
        final float leftSrc = left;
        final float rightSrc = right;
        final float heightSrc = bottomSrc - top;

        for (int i = pointBegin; i < pointEnd; i++) {
            Entry entry = entryList.get(i);

            // 1 画蜡烛图
            X_AXIS[0] = i;
            X_AXIS[1] = 0;
            X_AXIS[2] = i + 1;
            X_AXIS[3] = 0;
            render.mapPoints(X_AXIS);

            // 1.1 柱子
            final boolean isDown = entry.getOpen() > entry.getClose();
            final float topValue1 = isDown ? entry.getOpen() : entry.getClose();
            final float top = bottomSrc - (topValue1 - minPrice) * heightSrc / priceSum;
            final float bottomValue1 = isDown ? entry.getClose() : entry.getOpen();
            final float bottom = bottomSrc - (bottomValue1 - minPrice) * heightSrc / priceSum;
            float left = X_AXIS[0] + SPACE / 2;
            float right = X_AXIS[2] - SPACE / 2;
            // 左移边界
            if (left < leftSrc) {
                left = leftSrc;
            }
            if (left > rightSrc) {
                continue;
            }
            // 右移边界
            if (right > rightSrc) {
                right = rightSrc;
            }
            if (right < leftSrc) {
                continue;
            }
            // 涨停、跌停、或不涨不跌的一字板
            boolean isEqaual = Math.abs(top - bottom) < 1.f;
            int color = isEqaual ? StockPaint.STOCK_RED : StockPaint.STOCK_GREEN;
            StockPaint.setPaintColor(color);
            canvas.drawRect(left, top, right, isEqaual ? (bottom + 2) : bottom, StockPaint.getTurnoverPaint());

            // 1.2 上阴线
            final float top2 = bottomSrc - (entry.getHigh() - minPrice) * heightSrc / priceSum;
            final float x2 = (right - left) / 2 + left;
            canvas.drawLine(x2, top2, x2, top, StockPaint.getLinePaint(color));

            // 1.3 下阴线
            final float top3 = bottomSrc - (entry.getLow() - minPrice) * heightSrc / priceSum;
            canvas.drawLine(x2, bottom, x2, top3, StockPaint.getLinePaint(color));

            // Mad5
            final float x1 = left + (right - left) / 2;
            final float y1 = bottomSrc - (entry.getMa5() - minPrice) * heightSrc / priceSum;
            if (MAD_5.size() == 0) {
                MAD_5.add(x1);
                MAD_5.add(y1);
                MAD_5.add(x1);
                MAD_5.add(y1);
            } else {
                final int size = MAD_5.size();
                MAD_5.add(MAD_5.get(size - 2));
                MAD_5.add(MAD_5.get(size - 1));
                MAD_5.add(x1);
                MAD_5.add(y1);
            }

            // Mad10
            final float y10 = bottomSrc - (entry.getMa10() - minPrice) * heightSrc / priceSum;
            if (MAD_10.size() == 0) {
                MAD_10.add(x1);
                MAD_10.add(y10);
                MAD_10.add(x1);
                MAD_10.add(y10);
            } else {
                final int size = MAD_10.size();
                MAD_10.add(MAD_10.get(size - 2));
                MAD_10.add(MAD_10.get(size - 1));
                MAD_10.add(x1);
                MAD_10.add(y10);
            }

            // 高亮坐标
            drawHightlight(canvas, hightLight, i);
        }
    }

    /**
     * MAD
     */
    private void drawMadline(Canvas canvas) {
        // 5日均线
        final float[] pts5 = new float[MAD_5.size()];
        for (int i = 0; i < MAD_5.size(); i++) {
            pts5[i] = MAD_5.get(i);
        }
        canvas.drawLines(pts5, StockPaint.getLinePaint(StockPaint.STOCK_RED));

        // 10日均线
        final float[] pts10 = new float[MAD_10.size()];
        for (int i = 0; i < MAD_10.size(); i++) {
            pts10[i] = MAD_10.get(i);
        }
        canvas.drawLines(pts10, StockPaint.getLinePaint(StockPaint.STOCK_GREEN));
    }

    /**
     * 价格
     */
    private void drawPrice(Canvas canvas, float minPrice, float maxPrice) {
        Paint textPaint = StockPaint.getTextPaint(Paint.Align.LEFT, 20);
        Paint.FontMetrics fontMetrics = textPaint.getFontMetrics();
        float temp5 = fontMetrics.bottom - fontMetrics.top;
        // 最高价
        canvas.drawText(maxPrice + "元", left, top + temp5, textPaint);
        // 最低价
        canvas.drawText(minPrice + "元", left, bottomF - 30 - temp5, textPaint);
    }
}
