package com.lib.stockchart.render;

import android.graphics.Canvas;
import android.util.Log;

import com.lib.stockchart.draw.DrawKline;
import com.lib.stockchart.draw.DrawTline;
import com.lib.stockchart.draw.DrawTurnover;
import com.lib.stockchart.draw.IDraw;
import com.lib.stockchart.entry.EntryManager;

import java.util.List;

/**
 * description: K线图 + 成交量
 * created by kalu on 2017/11/9 0:59
 */
public class KlineRender extends BaseRender {

    @Override
    public void onCanvas(Canvas canvas, int indexBegin, int indexEnd, int indexCount, int indexMax) {
        Log.e("KlineRender", "onCanvas");

        final List<IDraw> drawList = getDrawList();

        if (indexMax <= 0) {
            for (IDraw drawing : drawList) {
                drawing.onDrawNull(canvas);
            }
        } else {

            calculateData(indexBegin, indexEnd, indexCount);
            Log.e("KlineRender", "onCanvas ==> indexBegin = " + indexBegin + ", indexEnd = " + indexEnd+", indexCount = "+indexCount);

            final float minPrice = EntryManager.getInstance().calculatePriceMin(indexBegin, indexEnd);
            final float maxPrice = EntryManager.getInstance().calculatePriceMax(indexBegin, indexEnd);
            final float turnoverMax = EntryManager.getInstance().calculateTurnoverMax(indexBegin, indexEnd);

            for (int i = 0; i < drawList.size(); i++) {
                final IDraw temp = drawList.get(i);
                temp.onDrawData(this, canvas, indexCount, indexBegin, indexEnd, minPrice, maxPrice, turnoverMax, getxHighligh(), getyHighligh());
            }
        }
    }

    @Override
    public void addData() {

        final List<IDraw> drawList = getDrawList();
        drawList.clear();
        drawList.add(new DrawKline());
        drawList.add(new DrawTurnover());
    }
}
