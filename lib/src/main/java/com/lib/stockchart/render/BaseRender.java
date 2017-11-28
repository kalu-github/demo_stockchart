package com.lib.stockchart.render;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.util.Log;

import com.lib.stockchart.draw.DrawKline;
import com.lib.stockchart.draw.DrawTline;
import com.lib.stockchart.draw.DrawTurnover;
import com.lib.stockchart.draw.IDraw;
import com.lib.stockchart.entry.Entry;
import com.lib.stockchart.entry.EntryManager;
import com.lib.stockchart.paint.StockPaint;

import java.util.ArrayList;
import java.util.List;

/**
 * description: 当前类描述信息
 * created by kalu on 2017/11/11 15:59
 */
public abstract class BaseRender {

    // 默认-1, 不显示高亮
    private float xHighligh = -1f;
    private float yHighligh = -1f;

    public float getxHighligh() {
        return xHighligh;
    }

    public void setxHighligh(float xHighligh) {
        this.xHighligh = xHighligh;
    }

    public float getyHighligh() {
        return yHighligh;
    }

    public void setyHighligh(float yHighligh) {
        this.yHighligh = yHighligh;
    }

    // 股票指标列表
    private final List<IDraw> mDrawList = new ArrayList<>();
    // 显示区域
    int left, top, right, bottom, width, height;

    public void onSizeChanged(int left, int top, int right, int bottom) {
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
        this.width = right - left;
        this.height = bottom - top;
        Log.e("BaseRender", "left = " + left + ", top = " + top + ", right = " + right + ", bottom = " + bottom + ", width = " + width + ", height = " + height);

        for (IDraw drawing : mDrawList) {
            drawing.onDrawInit(this.left, this.top, this.right, this.bottom, this.width, this.height);
        }
    }

    /**
     * 计算数据
     */
    void calculateData(int indexBegin, int indexEnd, int indexCount) {

        final int weightTop = EntryManager.getInstance().getWeightTop();
        final int weightDown = EntryManager.getInstance().getWeightDown();
        final int weightSum = weightTop + weightDown;

        // 内边框边距
        final int boardPadding = EntryManager.getInstance().getBoardPadding();
        final int xlabelHeight = EntryManager.getInstance().getXlabelHeight();
        final float bottomSrc = height / weightSum * weightTop - boardPadding - xlabelHeight;
        final float heightSrc = bottomSrc - top - boardPadding;

        final float priceMin = EntryManager.getInstance().calculatePriceMin(indexBegin, indexEnd);
        final float priceMax = EntryManager.getInstance().calculatePriceMax(indexBegin, indexEnd);
        final float turnoverMax = EntryManager.getInstance().calculateTurnoverMax(indexBegin, indexEnd);
        float priceSum = priceMax - priceMin;

        // Log.e("BaseRender", "caculateZoom ==> pointCount = " + pointCount);

        // 点与点间隙
        final int pointSpace = EntryManager.getInstance().getPointSpace();
        // 每个点宽度
        final int pointWidth = (width - 2 * boardPadding - (indexCount - 1) * pointSpace) / indexCount;
        EntryManager.getInstance().setPointWidth(pointWidth);

        final List<Entry> entryList = EntryManager.getInstance().getEntryList();

//        final int offsetLeft = EntryManager.getInstance().getOffsetLeft();
//        final int offsetRight = EntryManager.getInstance().getOffsetRight();
        final int realBegin = left + boardPadding;

        for (int i = indexBegin; i <= indexEnd; i++) {

            final Entry entry = entryList.get(i);

            if (i == indexBegin) {
                // Log.e("KlineRender", "1temp = " + realBegin + ", pointWidth = " + pointWidth + ", pointSpace = " + pointSpace);
                entry.setxLabelReal(realBegin);
            } else if (i == indexEnd) {
                final int realEnd = right - boardPadding - pointWidth;
                //  Log.e("KlineRender", "3temp = " + realEnd + ", pointWidth = " + pointWidth + ", pointSpace = " + pointSpace);
                entry.setxLabelReal(realEnd);
            } else {
                int temp = realBegin + (i - indexBegin) * (pointWidth + pointSpace);
                //   Log.e("KlineRender", "2temp = " + temp + ", pointWidth = " + pointWidth + ", pointSpace = " + pointSpace);
                entry.setxLabelReal(temp);
            }

            // 价格
            entry.setOpenReal(bottomSrc - (entry.getOpen() - priceMin) * heightSrc / priceSum);
            entry.setCloseReal(bottomSrc - (entry.getClose() - priceMin) * heightSrc / priceSum);
            entry.setHighReal(bottomSrc - (entry.getHigh() - priceMin) * heightSrc / priceSum);
            entry.setLowReal(bottomSrc - (entry.getLow() - priceMin) * heightSrc / priceSum);
            entry.setMa5Real(bottomSrc - (entry.getMa5() - priceMin) * heightSrc / priceSum);
            entry.setMa10Real(bottomSrc - (entry.getMa10() - priceMin) * heightSrc / priceSum);
            entry.setMa20Real(bottomSrc - (entry.getMa20() - priceMin) * heightSrc / priceSum);

            // 成交量
            entry.setVolumeReal((int) (bottomSrc - entry.getVolume() * height / turnoverMax));
            entry.setVolumeMa5Real((int) (bottomSrc - entry.getVolumeMa5() * height / turnoverMax));
            entry.setVolumeMa10Real((int) (bottomSrc - entry.getVolumeMa10() * height / turnoverMax));
        }
    }

    public List<IDraw> getDrawList() {
        return mDrawList;
    }

    /**********************************************************************************************/

    public abstract void onCanvas(Canvas canvas, int indexBegin, int indexEnd, int indexCount, int indexCountMax);

    public void clearData() {
        mDrawList.clear();
    }

    public abstract void addData();
}