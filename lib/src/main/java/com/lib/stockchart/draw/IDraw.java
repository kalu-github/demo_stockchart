package com.lib.stockchart.draw;

import android.graphics.Canvas;

import com.lib.stockchart.render.AbstractRender;

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
     *
     * @param model 绘制模式
     *              1:K线图+成交量 RenderManager.MODEL_KLLINE_TURNOVER
     *              2:K线图+MACD+RSI+KDJ+BOLL RenderManager.MODEL_KLLINE_MACD_RSI_KDJ_BOLL
     *              3:分时图+成交量 RenderManager.MODEL_TLLINE_TURNOVER
     */
    void onDrawData(AbstractRender render, Canvas canvas, int pointCount, int pointVisableCount, int pointBegin, int pointEnd, float minPrice, float maxPrice, float maxTurnover, boolean hightLight, int model);
}
