package com.lib.stockchart.render;

import android.graphics.Canvas;
import android.util.Log;

import com.lib.stockchart.draw.DrawKline;
import com.lib.stockchart.draw.DrawTline;
import com.lib.stockchart.draw.DrawTurnover;
import com.lib.stockchart.draw.IDraw;
import com.lib.stockchart.entry.EntryManager;

/**
 * description: K线图 + 成交量
 * created by kalu on 2017/11/9 0:59
 */
public class KlineRender extends BaseRender {

    @Override
    public void onCanvas(Canvas canvas, boolean hightLight, int model) {
        Log.e("KlineRender", "onCanvas");

        final int pointSum = EntryManager.getInstance().getEntryList().size();
        if (pointSum <= 0) {
            for (IDraw drawing : mDrawList) {
                drawing.onDrawNull(canvas);
            }
        } else {

            final int pointCount = EntryManager.getInstance().getPointCount();
            final int pointBegin = EntryManager.getInstance().getPointBegin();
            final int pointEnd = EntryManager.getInstance().getPointEnd();
            calculateData(pointBegin, pointEnd, pointCount);
            Log.e("KlineRender", "onCanvas ==> pointBegin = " + pointBegin + ", pointEnd = " + pointEnd);

            final float minPrice = EntryManager.getInstance().calculatePriceMin(pointBegin - 1, pointEnd);
            final float maxPrice = EntryManager.getInstance().calculatePriceMax(pointBegin - 1, pointEnd);
            final float turnoverMax = EntryManager.getInstance().calculateTurnoverMax(pointBegin - 1, pointEnd);

            for (int i = 0; i < mDrawList.size(); i++) {
                final IDraw temp = mDrawList.get(i);
                temp.onDrawData(this, canvas, pointSum, pointCount, pointBegin, pointEnd, minPrice, maxPrice, turnoverMax, getxHighligh(), getyHighligh());
            }
        }
    }

    @Override
    public void addData() {
        mDrawList.clear();
        mDrawList.add(new DrawKline());
        mDrawList.add(new DrawTurnover());
    }
}
