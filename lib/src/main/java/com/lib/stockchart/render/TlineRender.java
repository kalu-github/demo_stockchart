package com.lib.stockchart.render;

import android.graphics.Canvas;
import android.graphics.RectF;
import android.util.Log;

import com.lib.stockchart.draw.DrawTline;
import com.lib.stockchart.draw.DrawTurnover;
import com.lib.stockchart.draw.IDraw;
import com.lib.stockchart.entry.Entry;
import com.lib.stockchart.entry.EntryManager;

import java.util.ArrayList;

/**
 * description: 分时图 + 成交量(不可以滑动, 仅仅显示当天开盘走势信息)
 * created by kalu on 2017/11/9 0:59
 */
public class TlineRender extends AbstractRender {

    // 显示区域
    private int left, top, right, bottom, width, height;

    // 股票指标列表
    private final ArrayList<IDraw> mDrawList = new ArrayList<>();

    @Override
    public void onSizeChanged(int left, int top, int right, int bottom) {
        Log.e("TlineRender", "onSizeChanged");

        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
        this.width = right - left;
        this.height = bottom - top;

        Log.e("TlineRender", "left = " + left + ", top = " + top + ", right = " + right + ", bottom = " + bottom);

        for (IDraw drawing : mDrawList) {
            drawing.onDrawInit(left, top, right, bottom, width, height);
        }
    }

    @Override
    public void onCanvas(Canvas canvas, boolean hightLight, int model) {
        Log.e("TlineRender", "onCanvas");

        final int pointSum = EntryManager.getInstance().getEntryList().size();

        // 准备操作(边框, 数据加载提示信息)
        final ArrayList<Entry> entryList = EntryManager.getInstance().getEntryList();
        if (null != entryList && pointSum > 0) {
            Log.e("TlineRender", "onCanvas ==> 有数据");

            final float minPrice = EntryManager.getInstance().calculatePriceMin();
            final float maxPrice = EntryManager.getInstance().calculatePriceMax();
            final float turnoverMax = EntryManager.getInstance().calculateTurnoverMax();

            for (int i = 0; i < mDrawList.size(); i++) {

                final IDraw temp = mDrawList.get(i);
                temp.onDrawData(this, canvas, pointSum, pointSum, 0, pointSum, minPrice, maxPrice, turnoverMax, hightLight, model);
            }
        } else {
            Log.e("TlineRender", "onCanvas ==> 空数据");

            for (IDraw drawing : mDrawList) {
                drawing.onDrawNull(canvas);
            }
        }
    }

    @Override
    public void clearData() {
        mDrawList.clear();
    }

    @Override
    public void addData() {
        if (mDrawList.size() != 0) return;
        mDrawList.add(new DrawTline());
        mDrawList.add(new DrawTurnover());
    }

    @Override
    public boolean canScroll(float dx) {
        return false;
    }

    @Override
    public boolean canDragging(float dx) {
        return false;
    }
}