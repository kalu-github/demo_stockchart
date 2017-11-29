package com.lib.stockchart;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.os.Vibrator;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.widget.ScrollerCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.animation.Interpolator;

import com.lib.stockchart.entry.StockDataTest;
import com.lib.stockchart.entry.Entry;
import com.lib.stockchart.entry.EntryManager;
import com.lib.stockchart.render.RenderManager;

import java.util.ArrayList;
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

    // 自定义属性
    private int pointCountMin = 70;
    private int pointCountMax = 26;
    private float xoffsetLeft = 0f;
    private float xoffsetRight = 0f;
    private float xoffsetMax = 100f;
    private String loadingStr;
    private float xlabelHeight = 25f;
    private float boardPadding = 5f;

    // 时间戳
//    private long timestamp = System.currentTimeMillis();


    private Context mContext = getContext().getApplicationContext();
    // 反弹速度
    private final ScrollerCompat scroller = ScrollerCompat.create(mContext, new Interpolator() {
        public float getInterpolation(float t) {
            t -= 1.0f;
            return t * t * t * t * t + 1.0f;
        }
    });

    // 与滚动控制、滑动加载数据相关的属性
    // 与手势控制相关的属性
    private boolean canDragXoffset = false;
    //    private boolean enableRightRefresh = true;
//    private boolean enableLeftRefresh = true;
//    private boolean onDragging = true;
//    private boolean onVerticalMove = false;
//    private boolean onDoubleFingerPress = false;
//    private boolean onTouch = false;
    // 长按高亮
    private boolean isLongPress = false;

    /**********************************************************************************************/

    public StockChartView(Context context) {
        this(context, null);
    }

    public StockChartView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StockChartView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        // 重置数据
        EntryManager.getInstance().resetData();

        // 自定义属性
        final Resources.Theme theme = context.getTheme();
        final TypedArray a = theme.obtainStyledAttributes(attrs, R.styleable.StockChartView, defStyleAttr, defStyleAttr);
        try {

            indexCount = a.getInt(R.styleable.StockChartView_scv_point_count, 50);
            pointCountMax = a.getInt(R.styleable.StockChartView_scv_point_max, 75);
            pointCountMin = a.getInt(R.styleable.StockChartView_scv_point_min, 25);
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

        gestureDetector.setIsLongpressEnabled(true);
    }

    /**********************************************************************************************/

    @Override
    public boolean onTouchEvent(MotionEvent e) {

        switch (e.getAction()) {

            case MotionEvent.ACTION_MOVE:
            case MotionEvent.ACTION_HOVER_MOVE:
                // 高亮
                if (isLongPress) {
                    // 高亮
                    final float x = e.getX();
                    final float y = e.getY();

                    if (RenderManager.getInstance().getRenderModel() == RenderManager.MODEL_KLINE_TURNOVER) {
                        RenderManager.getInstance().getKlineRender().setxHighligh(x);
                        RenderManager.getInstance().getKlineRender().setyHighligh(y);
                        postInvalidate();
                    } else if (RenderManager.getInstance().getRenderModel() == RenderManager.MODEL_TLINE_TURNOVER) {
                        RenderManager.getInstance().getTlineRender().setxHighligh(x);
                        RenderManager.getInstance().getTlineRender().setyHighligh(y);
                        postInvalidate();
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL: {

                // 取消高亮
                if (isLongPress) {
                    if (RenderManager.getInstance().getRenderModel() == RenderManager.MODEL_KLINE_TURNOVER) {
                        RenderManager.getInstance().getKlineRender().setxHighligh(-1f);
                        RenderManager.getInstance().getKlineRender().setyHighligh(-1f);
                        postInvalidate();
                    } else if (RenderManager.getInstance().getRenderModel() == RenderManager.MODEL_TLINE_TURNOVER) {
                        RenderManager.getInstance().getTlineRender().setxHighligh(-1f);
                        RenderManager.getInstance().getTlineRender().setyHighligh(-1f);
                        postInvalidate();
                    }
                    isLongPress = false;
                }


                // todo
                if (canDragXoffset) {
                    if (xoffsetRight != 0) {
                        xoffsetLeft = 0f;
                        xoffsetRight = 0f;
                        postInvalidate();
                    }
                }
                break;
            }
        }

        gestureDetector.onTouchEvent(e);
        scaleDetector.onTouchEvent(e);
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // Log.e("StockChartView", "onDraw");

        final int model = RenderManager.getInstance().getRenderModel();

        if (model == RenderManager.MODEL_TLINE_TURNOVER) {
            // Log.e("StockChartView", "onDraw TLLINE");
            RenderManager.getInstance().getTlineRender().onCanvas(canvas, 0, indexMax, indexMax, indexMax, 0, 0, loadingStr, xlabelHeight, boardPadding);
        } else if (model == RenderManager.MODEL_KLINE_TURNOVER) {
            //  Log.e("StockChartView", "onDraw KLLINE");
            RenderManager.getInstance().getKlineRender().onCanvas(canvas, indexBegin, indexEnd, indexCount, indexMax, xoffsetLeft, xoffsetRight, loadingStr, xlabelHeight, boardPadding);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        int left = getLeft() + getPaddingLeft();
        int top = getTop() + getPaddingTop();
        int right = getRight() - getPaddingRight();
        int bottom = getBottom() - getPaddingBottom();
        RenderManager.getInstance().getKlineRender().onSizeChanged(left, top, right, bottom, xlabelHeight, boardPadding);
        RenderManager.getInstance().getTlineRender().onSizeChanged(left, top, right, bottom, xlabelHeight, boardPadding);

        // 横屏
//        if (getContext().getApplicationContext().getResources().getConfiguration().orientation != Configuration.ORIENTATION_PORTRAIT) {
//            final boolean b = getVisibility() == View.VISIBLE;
//
//            Log.e("StockChartView", "onSizeChanged , 横屏 " + b);
//        }

        // IDE预览模式下, 添加测试数据
//        if (isInEditMode()) {
//            final ArrayList<Entry> entries = StockDataTest.parseKLineData(StockDataTest.KLINE);
//            notifyDataSetChanged(entries);
//        }
    }

//    @Override
//    public void computeScroll() {
//        if (onVerticalMove) {
//            return;
//        }
//
//        if (scroller.computeScrollOffset()) {
//            final int x = scroller.getCurrX();
//            final int dx = x - lastFlingX;
//            lastFlingX = x;
//
//            if (onTouch) {
//                scroller.abortAnimation();
//            } else {
//                if (KLINE_STATUS == KLINE_STATUS_RELEASE_BACK) {
//                    // 更新滚动的距离，用于拖动松手后回中
//                    RenderManager.getInstance().getKlineRender().updateCurrentTransX(dx);
//                    RenderManager.getInstance().getKlineRender().updateOverScrollOffset(dx);
//                    postInvalidate();
//                } else if (KLINE_STATUS == KLINE_STATUS_SPRING_BACK) {
//
//                    // 更新滚动的距离，用于加载数据完成后滚动或者回中
//                    if (EntryManager.getInstance().getEntryList().size() > lastEntrySize) {
//
//                        // 滚动
//                        RenderManager.getInstance().getKlineRender().scroll(dx);
//                        postInvalidate();
//                    } else {
//
//                        // 更新滚动的距离，用于拖动松手后回中
//                        RenderManager.getInstance().getKlineRender().updateCurrentTransX(dx);
//                        RenderManager.getInstance().getKlineRender().updateOverScrollOffset(dx);
//                        postInvalidate();
//                    }
//
//                } else {
//                    // 滚动
//                    RenderManager.getInstance().getKlineRender().scroll(dx);
//                    postInvalidate();
//                }
//            }
//        } else {
//            final float overScrollOffset = RenderManager.getInstance().getKlineRender().getOverScrollOffset();
//
//            if (!onTouch && overScrollOffset != 0 && KLINE_STATUS == KLINE_STATUS_IDLE) {
//                lastScrollDx = 0;
//                float dx = overScrollOffset;
//
//                if (Math.abs(overScrollOffset) > OVERSCROLL_THRESHOLD) {
//                    if (enableLeftRefresh && overScrollOffset > 0) {
//                        lastScrollDx = (int) overScrollOffset - OVERSCROLL_THRESHOLD;
//
//                        dx = lastScrollDx;
//                    }
//
//                    if (enableRightRefresh && overScrollOffset < 0) {
//                        lastScrollDx = (int) overScrollOffset + OVERSCROLL_THRESHOLD;
//
//                        dx = lastScrollDx;
//                    }
//                }
//
//                KLINE_STATUS = KLINE_STATUS_RELEASE_BACK;
//                lastFlingX = 0;
//                scroller.startScroll(0, 0, (int) dx, 0, OVERSCROLL_DURATION);
//                // ViewCompat.postInvalidateOnAnimation(this);
//
//            } else if (KLINE_STATUS == KLINE_STATUS_RELEASE_BACK) {
//                KLINE_STATUS = KLINE_STATUS_LOADING;
//
//                if (listener != null) {
//                    lastEntrySize = EntryManager.getInstance().getEntryList().size();
//                    if (lastScrollDx > 0) {
//                        listener.onLeftRefresh();
//                    } else if (lastScrollDx < 0) {
//                        listener.onRightRefresh();
//                    }
//                } else {
//                    // 加载完成
//                    refreshComplete(false);
//                }
//            } else {
//                KLINE_STATUS = KLINE_STATUS_IDLE;
//            }
//        }
//    }

    /**********************************************************************************************/

    public void clearDataSetChanged() {
        EntryManager.getInstance().resetData();
        indexBegin = 0;
        indexMax = 0;
        indexEnd = 0;
    }

    public void notifyDataSetChanged(List<Entry> entryData) {

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
        postInvalidate();

        Log.e("StockChartView", "notifyDataSetChanged");
    }

    private final GestureDetector gestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {

        /**
         * 长按高亮显示
         */
        @Override
        public void onLongPress(MotionEvent e) {

            if (isLongPress) return;
            isLongPress = true;

            final Context context = getContext().getApplicationContext();
            Vibrator vibrator = (Vibrator) context.getSystemService(VIBRATOR_SERVICE);
            vibrator.vibrate(25);

            // 高亮
            final float x = e.getX();
            final float y = e.getY();
            // Log.e("kaluyyyy", "onShowPress1 ==> x = " + e.getX());

            if (RenderManager.getInstance().getRenderModel() == RenderManager.MODEL_KLINE_TURNOVER) {
                RenderManager.getInstance().getKlineRender().setxHighligh(x);
                RenderManager.getInstance().getKlineRender().setyHighligh(y);
                postInvalidate();
            } else if (RenderManager.getInstance().getRenderModel() == RenderManager.MODEL_TLINE_TURNOVER) {
                RenderManager.getInstance().getTlineRender().setxHighligh(x);
                RenderManager.getInstance().getTlineRender().setyHighligh(y);
                postInvalidate();
            }

//                if (null != listener) {
//                    final int pointHighlight = EntryManager.getInstance().getPointHighlight();
//                    final Entry entry = EntryManager.getInstance().getEntryList().get(pointHighlight);
//                    listener.onHighlight(entry, pointHighlight, x, y);
//                }
        }

        /**
         * 左右滑动
         */
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {

            if (indexMax == 0 || Math.abs(distanceY) > Math.abs(distanceX) || e1.getPointerCount() != 1 || e2.getPointerCount() != 1)
                return false;
            Log.e("kaluyyyy", "onScroll ==> distanceX = " + distanceX);

            if (RenderManager.getInstance().getRenderModel() == RenderManager.MODEL_KLINE_TURNOVER) {

                // 左划大于0
                if (distanceX > 10f) {

                    // 滚动
                    if (indexEnd == indexMax && canDragXoffset) {

                        if (Math.abs(xoffsetRight) >= xoffsetMax) {
                            Log.e("rrrrr", "滑倒最右侧了");
                            return false;
                        }

                        xoffsetRight = xoffsetRight - distanceX;
                        xoffsetLeft = 0;
                        postInvalidate();
                    } else {

                        final int indexEndTemp = indexEnd + 1;
                        if (indexEndTemp >= indexMax) return false;

                        final int indexBeginTemp = indexBegin + 1;
                        if (indexBeginTemp <= 0) return false;

                        indexEnd = indexEndTemp;
                        indexBegin = indexBeginTemp;
                        postInvalidate();
                    }
                }
                // 右划小于0
                else if (distanceX < -10f) {

                    if (indexBegin <= 0 && canDragXoffset) {

                        if (xoffsetLeft >= xoffsetMax) {

                            Log.e("rrrrr", "滑倒最左侧了");
                            return false;
                        }

                        Log.e("rrrrr", "左侧刷新");
                        xoffsetLeft = xoffsetLeft + distanceX;
                        xoffsetRight = 0;
                        postInvalidate();
                    } else {

                        final int indexEndTemp = indexEnd - 1;
                        if (indexEndTemp >= indexMax) return false;

                        final int indexBeginTemp = indexBegin - 1;
                        if (indexBeginTemp <= 0) return false;

                        indexEnd = indexEndTemp;
                        indexBegin = indexBeginTemp;

                        postInvalidate();
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
//                postInvalidate();
//
//            }

            return super.onFling(e1, e2, velocityX, velocityY);
        }

        @Override
        public void onShowPress(MotionEvent e) {
            super.onShowPress(e);
            Log.e("kaluyyyy", "onShowPress ==> x = " + e.getX());
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

    private final ScaleGestureDetector scaleDetector = new ScaleGestureDetector(getContext(), new ScaleGestureDetector.SimpleOnScaleGestureListener() {

        @Override
        public boolean onScale(ScaleGestureDetector detector) {

            if (indexMax <= 0 || indexEnd >= indexMax || indexBegin <= 0)
                return super.onScale(detector);

            // K线图才会缩放
            if (RenderManager.getInstance().getRenderModel() == RenderManager.MODEL_KLINE_TURNOVER) {

//                final long temp = System.currentTimeMillis();
//                Log.e("ooooo", "onScale ==> temp = " + temp + ", timestamp = " + timestamp);
//                if (temp - timestamp <= 15) return super.onScale(detector);
//                timestamp = temp;

                float f = detector.getScaleFactor();

                if (f < 1.0f) {
                    // Log.e("yt", "onScale ==> 缩小 " + f);

                    final int indexEndTemp = indexEnd + 1;
                    if (indexEndTemp >= indexMax) return false;

                    final int indexBeginTemp = indexBegin - 1;
                    if (indexBeginTemp <= 0) return false;

                    final int indexCountTemp = indexCount + 2;
                    if (indexCountTemp >= pointCountMax) return false;

                    indexCount = indexCountTemp;
                    indexBegin = indexBeginTemp;
                    indexEnd = indexEndTemp;

                    invalidate();

                } else if (f > 1.0f) {
                    Log.e("yt", "onScale ==> 放大 " + f);

                    final int indexEndTemp = indexEnd - 1;
                    if (indexEndTemp >= indexMax) return false;

                    final int indexBeginTemp = indexBegin + 1;
                    if (indexBeginTemp <= 0) return false;

                    final int indexCountTemp = indexCount - 2;
                    if (indexCountTemp <= pointCountMin) return false;

                    indexCount = indexCountTemp;
                    indexBegin = indexBeginTemp;
                    indexEnd = indexEndTemp;

                    invalidate();
                }
            }

            return super.onScale(detector);
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
        RenderManager.getInstance().getKlineRender().clearData();
        RenderManager.getInstance().getTlineRender().clearData();
        super.onDetachedFromWindow();
    }

    @Override
    protected void onAttachedToWindow() {
        RenderManager.getInstance().getKlineRender().addData();
        RenderManager.getInstance().getTlineRender().addData();
        super.onAttachedToWindow();
    }

    @Override
    public void onStartTemporaryDetach() {
        RenderManager.getInstance().getKlineRender().addData();
        RenderManager.getInstance().getTlineRender().addData();
        super.onStartTemporaryDetach();
    }

    @Override
    public void onFinishTemporaryDetach() {
        RenderManager.getInstance().getKlineRender().clearData();
        RenderManager.getInstance().getTlineRender().clearData();
        super.onFinishTemporaryDetach();
    }

    /**********************************************************************************************/

    private OnStockChartChangeListener listener;

    public void setOnStockChartChangeListener(OnStockChartChangeListener listener) {
        this.listener = listener;
    }
}