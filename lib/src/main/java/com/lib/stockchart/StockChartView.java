package com.lib.stockchart;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.os.Vibrator;
import android.support.v4.widget.ScrollerCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.animation.Interpolator;

import com.lib.stockchart.entry.Entry;
import com.lib.stockchart.entry.EntryManager;
import com.lib.stockchart.entry.EntryManagerTest;
import com.lib.stockchart.render.RenderManager;

import java.util.List;

import static android.content.Context.VIBRATOR_SERVICE;

/**
 * description: 股票行情图
 * created by kalu on 2017/11/9 0:42
 */
public class StockChartView extends View {

    // 下标索引信息(更新数据, 左右滑动, 放大缩小)
    private int indexEnd = 0; // 结束下标索引
    private int indexBegin = 0; // 起始下标索引
    private int indexCount = 50; // 默认显示50个点
    private int indexMax = 0; // 真实索引最大值
    private float scrollRange = 0; // 滑动距离
    private float scaleCount = 0; // 缩放次数

    // 自定义属性
    private int pointMin = 26;  // 最少显示个数
    private int pointMax = 70;  // 最多显示个数
    private float xoffsetLeft = 0f;
    private float xoffsetRight = 0f;
    private float xoffsetMax = 100f;
    private String loadingStr;
    private float xlabelHeight = 25f;
    private float boardPadding = 5f;

    private final Context mContext = getContext().getApplicationContext();
    private final Vibrator mVibrator = (Vibrator) mContext.getSystemService(VIBRATOR_SERVICE);

    // 反弹速度
    private final ScrollerCompat mScrollerCompat = ScrollerCompat.create(mContext, new Interpolator() {
        public float getInterpolation(float t) {
            t -= 1.0f;
            return t * t * t * t * t + 1.0f;
        }
    });

    // 与手势控制相关的属性
    private boolean canDragXoffset = false;
    // 长按高亮
    private boolean isHighLight = false;

    /**********************************************************************************************/

    public StockChartView(Context context) {
        this(context, null);
    }

    public StockChartView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StockChartView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        // 自定义属性
        final Resources.Theme theme = context.getTheme();
        final TypedArray a = theme.obtainStyledAttributes(attrs, R.styleable.StockChartView, defStyleAttr, defStyleAttr);
        try {

            indexCount = a.getInt(R.styleable.StockChartView_scv_point_count, 50);
            pointMax = a.getInt(R.styleable.StockChartView_scv_point_max, 75);
            pointMin = a.getInt(R.styleable.StockChartView_scv_point_min, 25);
            xoffsetMax = a.getDimension(R.styleable.StockChartView_scv_xoffset_max, 100f);
            final String tempStr = a.getString(R.styleable.StockChartView_scv_hint_load);
            loadingStr = TextUtils.isEmpty(tempStr) ? "正在加载信息" : tempStr;
            boardPadding = a.getDimension(R.styleable.StockChartView_scv_board_padding, 5f);
            xlabelHeight = a.getDimension(R.styleable.StockChartView_scv_xlabel_height, 25f);

            int weightTop = a.getInt(R.styleable.StockChartView_scv_weight_top, 5);
            EntryManager.getInstance().setWeightTop(weightTop);
            int weightDown = a.getInt(R.styleable.StockChartView_scv_weight_down, 2);
            EntryManager.getInstance().setWeightDown(weightDown);
            float pointSpace = a.getDimension(R.styleable.StockChartView_scv_point_space, 10f);
            EntryManager.getInstance().setPointSpace(pointSpace);

            canDragXoffset = a.getBoolean(R.styleable.StockChartView_scv_xoffset_enable, false);

        } finally {
            a.recycle();
        }
    }

    /**********************************************************************************************/

    // 计算高亮
    private float lastX;

    @Override
    public boolean onTouchEvent(MotionEvent e) {

        mSimpleOnGestureListener.onTouchEvent(e);
        mSimpleOnScaleGestureListener.onTouchEvent(e);

        switch (e.getAction()) {

            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                lastX = e.getX();
                break;
            case MotionEvent.ACTION_MOVE:
            case MotionEvent.ACTION_HOVER_MOVE:
                // 高亮
                if (!isHighLight) break;

                final float x = e.getX();
                final float y = e.getY();

                if (Math.abs(x - lastX) > 10) {

                    lastX = x;
                    RenderManager.getInstance().getRenderDraw().setxHighligh(x);
                    RenderManager.getInstance().getRenderDraw().setyHighligh(y);
                    getParent().requestDisallowInterceptTouchEvent(true);
                    invalidate();
                }

                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL: {

                scaleCount = 0;
                scrollRange = 0;
                getParent().requestDisallowInterceptTouchEvent(false);

                // 取消高亮
                if (isHighLight) {
                    RenderManager.getInstance().getRenderDraw().setxHighligh(-1f);
                    RenderManager.getInstance().getRenderDraw().setyHighligh(-1f);
                    invalidate();
                    isHighLight = false;
                }

                // todo
                if (canDragXoffset) {
                    if (xoffsetRight != 0) {
                        xoffsetLeft = 0f;
                        xoffsetRight = 0f;
                        invalidate();
                    }
                }
                break;
            }
        }
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // Log.e("StockChartView", "onDraw ==>");

        final int model = RenderManager.getInstance().getRenderModel();

        if (model == RenderManager.MODEL_TLINE_TURNOVER) {
            RenderManager.getInstance().getRenderDraw().onCanvas(canvas, 0, 0, indexMax, 0, 0, loadingStr, xlabelHeight, boardPadding);
        } else if (model == RenderManager.MODEL_KLINE_TURNOVER) {
            RenderManager.getInstance().getRenderDraw().onCanvas(canvas, pointMax, indexBegin, indexEnd, xoffsetLeft, xoffsetRight, loadingStr, xlabelHeight, boardPadding);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        //  Log.e("StockChartView", "onSizeChanged ==>");

        int left = getLeft() + getPaddingLeft();
        int top = getTop() + getPaddingTop();
        int right = getRight() - getPaddingRight();
        int bottom = getBottom() - getPaddingBottom();
        RenderManager.getInstance().getRenderDraw().onSizeChanged(left, top, right, bottom, xlabelHeight, boardPadding);

        // IDE预览模式下, 添加测试数据
        if (!isInEditMode()) return;
        final List<Entry> entries = EntryManagerTest.parseKLineData(EntryManagerTest.KLINE);
        addDataSetChanged(entries);
    }

    @Override
    public void computeScroll() {
        super.computeScroll();

        // scroller.computeScrollOffset())
        final int x = mScrollerCompat.getCurrX();
    }

    /**********************************************************************************************/

    /**
     * 清空数据
     */
    public void clearDataSetChanged() {
        EntryManager.getInstance().resetData();
        indexBegin = 0;
        indexMax = 0;
        indexEnd = 0;
    }

    /**
     * 添加数据
     *
     * @param entryData 数据集合
     */
    public void addDataSetChanged(List<Entry> entryData) {

        if (null == entryData || entryData.size() == 0) return;

        // 1.计算下标索引
        if (RenderManager.getInstance().getRenderModel() == RenderManager.MODEL_TLINE_TURNOVER) {
            indexBegin = 0;
            indexMax = entryData.size() - 1;
            indexEnd = indexMax;
        } else {
            indexEnd = entryData.size() - 1;
            indexBegin = indexEnd - indexCount + 1;
            indexMax = entryData.size() - 1;
        }

        // 2.填充数据
        EntryManager.getInstance().addData(entryData);
        // 3.界面刷新
        invalidate();
        //Log.e("StockChartView", "notifyDataSetChanged");
    }

    private final GestureDetector mSimpleOnGestureListener = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {

        @Override
        public void onLongPress(MotionEvent e) {

            isHighLight = true;
            mVibrator.vibrate(25);

            final float x = e.getX();
            final float y = e.getY();

            RenderManager.getInstance().getRenderDraw().setxHighligh(x);
            RenderManager.getInstance().getRenderDraw().setyHighligh(y);
            getParent().requestDisallowInterceptTouchEvent(true);
            mVibrator.vibrate(25);
            invalidate();
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            // Log.e("kaluyyyy", "onScroll ==> distanceX = " + distanceX);

            if (RenderManager.getInstance().getRenderModel() != RenderManager.MODEL_KLINE_TURNOVER
                    || indexMax == 0
                    || e1.getPointerCount() != 1
                    || e2.getPointerCount() != 1
                    || isHighLight
                    || Math.abs(distanceY) > Math.abs(distanceX)) return false;

            // 左划大于0
            if (distanceX > 10f) {

                // 滚动
                if (indexEnd == indexMax && canDragXoffset) {

                    if (Math.abs(xoffsetRight) >= xoffsetMax) {
                        // Log.e("rrrrr", "滑倒最右侧了");
                        return false;
                    }

                    xoffsetRight = xoffsetRight - distanceX;
                    xoffsetLeft = 0;
                    getParent().requestDisallowInterceptTouchEvent(true);
                    invalidate();
                } else {

                    scrollRange += distanceX;
                    final int pointWidth = EntryManager.getInstance().getPointWidth();
                    final float pointSpace = EntryManager.getInstance().getPointSpace();

                    if (scrollRange >= (pointSpace + pointWidth)) {
                        scrollRange = 0;
                        final int indexEndTemp = indexEnd + 1;
                        if (indexEndTemp >= indexMax) return false;

                        final int indexBeginTemp = indexBegin + 1;
                        if (indexBeginTemp <= 0) return false;

                        indexEnd = indexEndTemp;
                        indexBegin = indexBeginTemp;
                        getParent().requestDisallowInterceptTouchEvent(true);
                        invalidate();
                    }
                }
            }
            // 右划小于0
            else if (distanceX < -10f) {

                if (indexBegin <= 0 && canDragXoffset) {

                    if (xoffsetLeft >= xoffsetMax) {

                        //Log.e("rrrrr", "滑倒最左侧了");
                        return false;
                    }

                    // Log.e("rrrrr", "左侧刷新");
                    xoffsetLeft = xoffsetLeft + distanceX;
                    xoffsetRight = 0;
                    getParent().requestDisallowInterceptTouchEvent(true);
                    invalidate();
                } else {

                    scrollRange += distanceX;
                    final int pointWidth = EntryManager.getInstance().getPointWidth();
                    final float pointSpace = EntryManager.getInstance().getPointSpace();
                    if (Math.abs(scrollRange) >= (pointSpace + pointWidth)) {
                        scrollRange = 0;
                        final int indexEndTemp = indexEnd - 1;
                        if (indexEndTemp >= indexMax) return false;

                        final int indexBeginTemp = indexBegin - 1;
                        if (indexBeginTemp <= 0) return false;

                        indexEnd = indexEndTemp;
                        indexBegin = indexBeginTemp;

                        getParent().requestDisallowInterceptTouchEvent(true);
                        invalidate();
                    }
                }
            }
            return false;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

//            final int pointSum = EntryManager.getInstance().getEntryList().size();
//            if (pointSum == 0) return super.onFling(e1, e2, velocityX, velocityY);
//
//            for (int i = 1; i < 100; i++) {
//
//                final int pointBegin = EntryManager.getInstance().getPointBegin();
//                final int pointEnd = EntryManager.getInstance().getPointEnd();
//
//                final int temp1 = pointEnd - 1;
//                if (temp1 > pointSum) return super.onFling(e1, e2, velocityX, velocityY);
//
//                final int temp2 = pointBegin - 1;
//                if (temp2 <= 1) return super.onFling(e1, e2, velocityX, velocityY);
//
//                EntryManager.getInstance().setPointEnd(temp1);
//                EntryManager.getInstance().setPointBegin(temp2);
//                RenderManager.getInstance().getKlineRender().caculateZoom();
//                invalidate();
//
//            }

            return false;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            if (listener != null) {
                listener.onDoubleTap(e, e.getX(), e.getY());
            }
            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            if (listener != null) {
                listener.onSingleTap(e, e.getX(), e.getY());
            }
            return true;
        }
    });

    private final ScaleGestureDetector mSimpleOnScaleGestureListener = new ScaleGestureDetector(getContext(), new ScaleGestureDetector.SimpleOnScaleGestureListener() {

        @Override
        public boolean onScale(ScaleGestureDetector detector) {

            if (isHighLight || indexMax <= 0)
                return true;

            if (RenderManager.getInstance().getRenderModel() == RenderManager.MODEL_TLINE_TURNOVER)
                return true;

            scaleCount = scaleCount + 1;
            if (scaleCount <= 3)
                return true;
            scaleCount = 0;

            float f = detector.getScaleFactor();

            if (f < 1.0f) {
                // Log.e("yt", "onScale ==> 缩小 " + f);

                // 最右边
                if (indexEnd >= indexMax) {

                    final int indexBeginTemp1 = indexBegin - 1;
                    if (indexBeginTemp1 <= 0) return true;

                    final int indexCountTemp1 = indexCount + 2;
                    if (indexCountTemp1 >= pointMax) return true;

                    indexCount = indexCountTemp1;
                    indexBegin = indexBeginTemp1;
                    getParent().requestDisallowInterceptTouchEvent(true);
                    invalidate();
                }
                // 最左边
                else if (indexBegin <= 0) {
                    final int indexEndTemp2 = indexEnd + 2;
                    if (indexEndTemp2 >= indexMax) return true;

                    final int indexCountTemp2 = indexCount + 2;
                    if (indexCountTemp2 >= pointMax) return true;

                    indexCount = indexCountTemp2;
                    indexEnd = indexEndTemp2;
                    getParent().requestDisallowInterceptTouchEvent(true);
                    invalidate();
                } else {
                    final int indexEndTemp3 = indexEnd + 1;
                    if (indexEndTemp3 >= indexMax) return true;

                    final int indexBeginTemp3 = indexBegin - 1;
                    if (indexBeginTemp3 <= 0) return true;

                    final int indexCountTemp3 = indexCount + 2;
                    if (indexCountTemp3 >= pointMax) return true;

                    indexCount = indexCountTemp3;
                    indexBegin = indexBeginTemp3;
                    indexEnd = indexEndTemp3;
                    getParent().requestDisallowInterceptTouchEvent(true);
                    invalidate();
                }
            } else if (f > 1.0f) {
                // Log.e("yt", "onScale ==> 放大 " + f);

                // 最右边
                if (indexEnd >= indexMax) {

                    final int indexBeginTemp4 = indexBegin + 2;
                    if (indexBeginTemp4 <= 0) return true;

                    final int indexCountTemp4 = indexCount - 2;
                    if (indexCountTemp4 <= pointMin) return true;

                    indexCount = indexCountTemp4;
                    indexBegin = indexBeginTemp4;
                    getParent().requestDisallowInterceptTouchEvent(true);
                    invalidate();
                }
                // 最左边
                else if (indexBegin <= 0) {
                    final int indexEndTemp5 = indexEnd + 2;
                    if (indexEndTemp5 >= indexMax) return true;

                    final int indexCountTemp5 = indexCount - 2;
                    if (indexCountTemp5 <= pointMin) return true;

                    indexCount = indexCountTemp5;
                    indexEnd = indexEndTemp5;
                    getParent().requestDisallowInterceptTouchEvent(true);
                    invalidate();
                } else {
                    final int indexEndTemp6 = indexEnd - 1;
                    if (indexEndTemp6 >= indexMax) return true;

                    final int indexBeginTemp6 = indexBegin + 1;
                    if (indexBeginTemp6 <= 0) return true;

                    final int indexCountTemp6 = indexCount - 2;
                    if (indexCountTemp6 <= pointMin) return true;

                    indexCount = indexCountTemp6;
                    indexBegin = indexBeginTemp6;
                    indexEnd = indexEndTemp6;
                    getParent().requestDisallowInterceptTouchEvent(true);
                    invalidate();
                }
            }
            return true;
        }
    });

    /**
     * 加载完成
     *
     * @param reverse 是否反转滚动的方向
     */
    public void refreshComplete(boolean reverse) {
//        final int overScrollOffset = (int) RenderManager.getInstance().getKlineRender().getOverScrollOffset();
//
//        if (overScrollOffset == 0) return;
//        KLINE_STATUS = KLINE_STATUS_SPRING_BACK;
//        lastFlingX = 0;
//        scroller.startScroll(0, 0, reverse ? -overScrollOffset : overScrollOffset, 0, OVERSCROLL_DURATION);
    }

    /**********************************************************************************************/

    @Override
    protected void onDetachedFromWindow() {
        RenderManager.getInstance().getRenderDraw().clearData();
        super.onDetachedFromWindow();
    }

    @Override
    protected void onAttachedToWindow() {
        RenderManager.getInstance().getRenderDraw().addData();
        super.onAttachedToWindow();
    }

    @Override
    public void onStartTemporaryDetach() {
        RenderManager.getInstance().getRenderDraw().addData();
        super.onStartTemporaryDetach();
    }

    @Override
    public void onFinishTemporaryDetach() {
        RenderManager.getInstance().getRenderDraw().clearData();
        super.onFinishTemporaryDetach();
    }

    /**********************************************************************************************/

    private OnStockChartChangeListener listener;

    public void setOnStockChartChangeListener(OnStockChartChangeListener listener) {
        this.listener = listener;
    }
}