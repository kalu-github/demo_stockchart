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
     *
     * @param left         左
     * @param top          上
     * @param right        右
     * @param bottom       下
     * @param width        宽
     * @param height       高
     * @param boardPadding 边框内边距
     */
    void onDrawInit(int left, int top, int right, int bottom, int width, int height, float boardPadding);

    /**
     * 网络加载数据, 等待过程的显示信息
     *
     * @param canvas       画布
     * @param str          提示信息
     * @param xlabelHeight K线图, 分时图, x坐标轴文字信息显示区域高度
     * @param boardPadding 边框内边距
     */
    void onDrawNull(Canvas canvas, String str, float xlabelHeight, float boardPadding);

    /**
     * 绘制图形
     *
     * @param render       着色管理
     * @param canvas       画布
     * @param indexCount   要显示的个数
     * @param indexBegin   x轴起始索引
     * @param indexEnd     x轴结束索引
     * @param minPrice     y轴最低价
     * @param maxPrice     y轴最高价
     * @param maxTurnover  成交量
     * @param xHighligh    x高亮坐标
     * @param yHighligh    y高亮坐标
     * @param xoffsetLeft  刷新, 左侧位移
     * @param xoffsetRight 刷新, 右侧位移
     * @param xlabelHeight K线图, 分时图, x坐标轴文字信息显示区域高度
     * @param boardPadding 边框内边距
     */
    void onDrawData(BaseRender render, Canvas canvas, int indexCount, int indexBegin, int indexEnd, float minPrice, float maxPrice, float maxTurnover, float xHighligh, float yHighligh, float xoffsetLeft, float xoffsetRight, float xlabelHeight, float boardPadding);
}
