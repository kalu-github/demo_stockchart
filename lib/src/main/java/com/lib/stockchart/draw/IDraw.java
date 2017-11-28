package com.lib.stockchart.draw;

import android.graphics.Canvas;

import com.lib.stockchart.render.BaseRender;

/**
 * description: 接口 - 画具体内容
 * created by kalu on 2017/11/9 22:56
 */
public interface IDraw {

    /**
     * 画布位置信息
     */
    void onDrawInit(int left, int top, int right, int bottom, int width, int height);

    /**
     * 网络加载数据, 等待过程的显示信息
     */
    void onDrawNull(Canvas canvas);

    /**
     * 绘制图形
     */
    void onDrawData(BaseRender render, Canvas canvas, int indexCount, int indexBegin, int indexEnd, float minPrice, float maxPrice, float maxTurnover, float xHighligh, float yHighligh, float xoffsetLeft, float xoffsetRight);
}
