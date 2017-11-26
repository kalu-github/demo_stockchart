package com.lib.stockchart;

import android.view.MotionEvent;

import com.lib.stockchart.entry.Entry;

/**
 * description: 手势变化监听
 * created by kalu on 2017/11/7 19:15
 */
public class OnStockChartChangeListenerSimple implements OnStockChartChangeListener {

    public void onLeftRefresh() {
    }

    public void onRightRefresh() {
    }

    public void onSingleTap(MotionEvent e, float x, float y) {
    }

    public void onDoubleTap(MotionEvent e, float x, float y) {
    }

    @Override
    public void onHighlight(Entry entry, int entryIndex, float x, float y) {
    }
}
