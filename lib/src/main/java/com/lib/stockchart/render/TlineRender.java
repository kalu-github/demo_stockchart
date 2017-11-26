package com.lib.stockchart.render;

import android.graphics.Canvas;
import android.util.Log;

import com.lib.stockchart.draw.DrawTline;
import com.lib.stockchart.draw.DrawTurnover;
import com.lib.stockchart.draw.IDraw;
import com.lib.stockchart.entry.EntryManager;

/**
 * description: 分时图 + 成交量(不可以滑动, 仅仅显示当天开盘走势信息)
 * created by kalu on 2017/11/9 0:59
 */
public class TlineRender extends BaseRender {

    @Override
    public void onCanvas(Canvas canvas, boolean hightLight, int model) {

        final int pointSum = EntryManager.getInstance().getEntryList().size();
        if (pointSum <= 0) {
            Log.e("TlineRender", "onCanvas ==> 空数据");
            for (IDraw drawing : mDrawList) {
                drawing.onDrawNull(canvas);
            }
        } else {
            Log.e("TlineRender", "onCanvas ==> 有数据");

            final float minPrice = EntryManager.getInstance().calculatePriceMin(1, pointSum);
            final float maxPrice = EntryManager.getInstance().calculatePriceMax(1, pointSum);
            final float turnoverMax = EntryManager.getInstance().calculateTurnoverMax(1, pointSum);
            calculateData(1, pointSum, pointSum);

            for (int i = 0; i < mDrawList.size(); i++) {
                final IDraw temp = mDrawList.get(i);
                temp.onDrawData(this, canvas, pointSum, pointSum, 1, pointSum, minPrice, maxPrice, turnoverMax, getxHighligh(), getyHighligh());
            }
        }
    }

    @Override
    public void addData() {
        mDrawList.clear();
        mDrawList.add(new DrawTline());
        mDrawList.add(new DrawTurnover());
    }
}