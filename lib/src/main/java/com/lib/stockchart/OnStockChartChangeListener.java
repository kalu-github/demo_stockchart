package com.lib.stockchart;

import android.view.MotionEvent;

import com.lib.stockchart.entry.Entry;

/**
 * description: 手势变化监听
 * created by kalu on 2017/11/7 19:15
 */
public interface OnStockChartChangeListener {

    // 刷新数据, 左划
    void onLeftRefresh();

    // 加载更多, 右划
    void onRightRefresh();

    // 单击
    void onSingleTap(MotionEvent e, float x, float y);

    // 双击
    void onDoubleTap(MotionEvent e, float x, float y);

    // 高亮
    void onHighlight(Entry entry, int entryIndex, float x, float y);
}
