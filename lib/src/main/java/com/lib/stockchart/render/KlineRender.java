package com.lib.stockchart.render;

import android.graphics.Canvas;
import android.graphics.RectF;
import android.util.Log;

import com.lib.stockchart.draw.DrawKline;
import com.lib.stockchart.draw.DrawTurnover;
import com.lib.stockchart.draw.IDraw;
import com.lib.stockchart.entry.Entry;
import com.lib.stockchart.entry.EntryManager;

import java.util.ArrayList;
import java.util.List;

/**
 * description: K线图 + 成交量
 * created by kalu on 2017/11/9 0:59
 */
public class KlineRender extends AbstractRender {

    // 显示区域
    private int left, top, right, bottom, width, height;

    private final float[] extremumY = new float[2];
    private final float[] contentPts = new float[2];

    // 股票指标列表
    private final List<IDraw> mDrawList = new ArrayList<>();

    @Override
    public void onSizeChanged(int left, int top, int right, int bottom) {

        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
        this.width = right - left;
        this.height = bottom - top;

        for (IDraw drawing : mDrawList) {
            drawing.onDrawInit(this.left, this.top, this.right, this.bottom, this.width, this.height);
        }
    }

    @Override
    public void caculateZoom() {
        postMatrixTouch(width, EntryManager.getInstance().getPointCount());
        computeExtremumValue(extremumY, EntryManager.getInstance().getMinY(), EntryManager.getInstance().getDeltaY());
        postMatrixValue(width, height, extremumY[0], extremumY[1]);
        postMatrixOffset(left, top);
    }

    @Override
    public void clearData() {
        mDrawList.clear();
    }

    @Override
    public void addData() {
        if (mDrawList.size() != 0) return;
        mDrawList.add(new DrawKline());
        mDrawList.add(new DrawTurnover());
    }

    @Override
    public void onCanvas(Canvas canvas, boolean hightLight, int model) {
        Log.e("KlineRender", "onCanvas");

        // 1.滑倒最左边了, 或者滑倒最右边了
//        final int pointBegin1 = EntryManager.getInstance().getPointBegin();
//        final int pointEnd1 = EntryManager.getInstance().getPointEnd();
//        final int size1 = EntryManager.getInstance().getEntryList().size();
        // if (pointBegin1 <= 0 || pointEnd1 >= size1 - 1) return;

        final int pointSum = EntryManager.getInstance().getEntryList().size();
        final int pointCount = EntryManager.getInstance().getPointCount();

        // 准备操作(边框, 数据加载提示信息)
        final ArrayList<Entry> entryList = EntryManager.getInstance().getEntryList();
        if (null != entryList && entryList.size() > 0) {
            Log.e("KlineRender", "onCanvas ==> 有数据");

            // 1.计算当前显示区域内的 X 轴范围
            contentPts[0] = left;
            contentPts[1] = 0;
            invertMapPoints(contentPts);

            // 2.计算真实坐标
            final int tempBegin = contentPts[0] <= 0 ? 0 : (int) contentPts[0];
            EntryManager.getInstance().setPointBegin(tempBegin);
            final int tempEndCache = tempBegin + EntryManager.getInstance().getPointCount();
            final int tempEnd = tempEndCache > pointSum ? pointSum : tempEndCache;
            EntryManager.getInstance().setPointEnd(tempEnd);

            // 计算当前显示区域内 entry 在 Y 轴上的最小值和最大值
            computeExtremumValue(extremumY, EntryManager.getInstance().getMinY(), EntryManager.getInstance().getDeltaY());
            postMatrixValue(width, height, extremumY[0], extremumY[1]);

            final int pointBegin = EntryManager.getInstance().getPointBegin();
            final int pointEnd = EntryManager.getInstance().getPointEnd();

            final float minPrice = EntryManager.getInstance().calculatePriceMin(pointBegin, pointEnd);
            final float maxPrice = EntryManager.getInstance().calculatePriceMax(pointBegin, pointEnd);
            final float turnoverMax = EntryManager.getInstance().calculateTurnoverMax(pointBegin, pointEnd);

            for (int i = 0; i < mDrawList.size(); i++) {
                final IDraw temp = mDrawList.get(i);
                temp.onDrawData(this, canvas, pointSum, pointCount, pointBegin, pointEnd, minPrice, maxPrice, turnoverMax, hightLight, model);
            }
        } else {
            Log.e("KlineRender", "onCanvas ==> 空数据");

            for (IDraw drawing : mDrawList) {
                drawing.onDrawNull(canvas);
            }
        }
    }

    @Override
    public void addZoom(int x) {
        if (EntryManager.getInstance().getEntryList().size() == 0) {
            return;
        }

        final int pointCount = EntryManager.getInstance().getPointCount();

        if (pointCount < 40) {
            return;
        }

        final int tempCount = pointCount - 2;
        EntryManager.getInstance().setPointCount(tempCount);

        zoom(tempCount, left, right, width, x);
    }

    @Override
    public void minusZoom(int x) {
        if (EntryManager.getInstance().getEntryList().size() == 0) {
            return;
        }

        final int pointCount = EntryManager.getInstance().getPointCount();
        if (pointCount > 100) {
            return;
        }

        final int tempCount = pointCount + 2;
        EntryManager.getInstance().setPointCount(tempCount);

        zoom(tempCount, left, right, width, x);
    }
}
