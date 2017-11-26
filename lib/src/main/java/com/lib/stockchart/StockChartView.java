package com.lib.stockchart;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.os.Vibrator;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.widget.ScrollerCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.Interpolator;

import com.lib.stockchart.entry.StockDataTest;
import com.lib.stockchart.entry.Entry;
import com.lib.stockchart.entry.EntryManager;
import com.lib.stockchart.compat.GestureMoveActionCompat;
import com.lib.stockchart.render.KlineRender;
import com.lib.stockchart.render.RenderManager;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.VIBRATOR_SERVICE;

/**
 * description: 股票行情图
 * created by kalu on 2017/11/9 0:42
 */
public class StockChartView extends View {

    private static final int KLINE_STATUS_IDLE = 0; // 空闲
    private static final int KLINE_STATUS_RELEASE_BACK = 2; // 放手，回弹到 loading 位置
    private static final int KLINE_STATUS_LOADING = 3; // 加载中
    private static final int KLINE_STATUS_SPRING_BACK = 4; // 加载结束，回弹到初始位置
    private int KLINE_STATUS = KLINE_STATUS_IDLE;

    private Context mContext = getContext().getApplicationContext();
    // 反弹速度
    private final ScrollerCompat scroller = ScrollerCompat.create(mContext, new Interpolator() {
        public float getInterpolation(float t) {
            t -= 1.0f;
            return t * t * t * t * t + 1.0f;
        }
    });

    // 与滚动控制、滑动加载数据相关的属性
    private static final int OVERSCROLL_DURATION = 250; // dragging 松手之后回中的时间，单位：毫秒
    private static final int OVERSCROLL_THRESHOLD = 250; // dragging 的偏移量大于此值时即是一个有效的滑动加载

    private int lastFlingX = 0;
    private int lastScrollDx = 0;
    private int lastEntrySize = 0; // 上一次的 entry 列表大小，用于判断是否成功加载了数据
    // 与手势控制相关的属性
    private boolean onTouch = false;
    private boolean onLongPress = false;
    private boolean onDoubleFingerPress = false;
    private boolean onVerticalMove = false;
    private boolean onDragging = false;
    private boolean enableLeftRefresh = false;
    private boolean enableRightRefresh = false;

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
            int weightTop = a.getInt(R.styleable.StockChartView_scv_weight_top, 5);
            EntryManager.getInstance().setWeightTop(weightTop);
            int weightDown = a.getInt(R.styleable.StockChartView_scv_weight_down, 2);
            EntryManager.getInstance().setWeightDown(weightDown);
            String hintLoad = a.getString(R.styleable.StockChartView_scv_hint_load);
            EntryManager.getInstance().setHintLoadStr(hintLoad);
            int pointCount = a.getInt(R.styleable.StockChartView_scv_point_count, 50);
            EntryManager.getInstance().setPointCount(pointCount);
            int pointMax = a.getInt(R.styleable.StockChartView_scv_point_max, 100);
            EntryManager.getInstance().setPointMax(pointMax);
            int pointMin = a.getInt(R.styleable.StockChartView_scv_point_min, 40);
            EntryManager.getInstance().setPointMin(pointMin);
        } finally {
            a.recycle();
        }

        gestureDetector.setIsLongpressEnabled(true);

        int touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        gestureCompat.setTouchSlop(touchSlop);
    }

    /**********************************************************************************************/

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        boolean onHorizontalMove = gestureCompat.onTouchEvent(event, event.getX(), event.getY());
        final int action = MotionEventCompat.getActionMasked(event);

        onVerticalMove = false;

        if (action == MotionEvent.ACTION_MOVE) {
            if (!onHorizontalMove && !onLongPress && !onDoubleFingerPress && gestureCompat.isDragging()) {
                onTouch = false;
                onVerticalMove = true;
            }
        }

        getParent().requestDisallowInterceptTouchEvent(!onVerticalMove);

        return super.dispatchTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {

        switch (MotionEventCompat.getActionMasked(e)) {
            case MotionEvent.ACTION_DOWN: {
                onTouch = true;
                onDragging = false;
                break;
            }

            case MotionEvent.ACTION_POINTER_DOWN: {
                onDoubleFingerPress = true;
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                onDragging = true;
                if (onLongPress) {

                    // 高亮
                    final float x = e.getX();
                    final float y = e.getY();
                    EntryManager.getInstance().setPointHighlightX(false, x);
                    EntryManager.getInstance().setPointHighlightY(-2f, y);
                    if (EntryManager.getInstance().isHighlightMove()) {
                        // Log.e("ooooooo1", "移动 ==> x = " + x + ", x1 = " + EntryManager.getInstance().getPointHighlightX()[0] + ", x2 = " + EntryManager.getInstance().getPointHighlightX()[1]);
                        invalidate();

                        if (null != listener) {
                            final int pointHighlight = EntryManager.getInstance().getPointHighlight();
                            final Entry entry = EntryManager.getInstance().getEntryList().get(pointHighlight);
                            listener.onHighlight(entry, pointHighlight, x, y);
                        }
                    }
                }
                break;
            }

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL: {
                onLongPress = false;
                onDoubleFingerPress = false;
                onTouch = false;
                onDragging = false;

                // 取消高亮
                EntryManager.getInstance().setPointHighlightX(true, -1);
                EntryManager.getInstance().setPointHighlightY(-1f, -1f);
                invalidate();
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
        Log.e("StockChartView", "onDraw");

        final int model = RenderManager.getInstance().getRenderModel();

        if (model == RenderManager.MODEL_TLINE_TURNOVER) {

            Log.e("StockChartView", "onDraw TLLINE");
            RenderManager.getInstance().getTlineRender().onCanvas(canvas, onLongPress, model);
        } else if (model == RenderManager.MODEL_KLINE_TURNOVER) {

            Log.e("StockChartView", "onDraw KLLINE");
            RenderManager.getInstance().getKlineRender().onCanvas(canvas, onLongPress, model);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        Log.e("StockChartView", "onSizeChanged");

        int left = getLeft() + getPaddingLeft();
        int top = getTop() + getPaddingTop();
        int right = getRight() - getPaddingRight();
        int bottom = getBottom() - getPaddingBottom();
        Log.e("StockChartView", "left = " + left + ", top = " + top + ", right = " + right + ", bottom = " + bottom);
        RenderManager.getInstance().getKlineRender().onSizeChanged(left, top, right, bottom);
        RenderManager.getInstance().getTlineRender().onSizeChanged(left, top, right, bottom);

        // 横屏
//        if (getContext().getApplicationContext().getResources().getConfiguration().orientation != Configuration.ORIENTATION_PORTRAIT) {
//
//            final boolean b = getVisibility() == View.VISIBLE;
//
//            Log.e("StockChartView", "onSizeChanged , 横屏 " + b);
//        }

        // IDE预览模式下, 添加测试数据
        if (isInEditMode()) {
            final ArrayList<Entry> entries = StockDataTest.parseKLineData(StockDataTest.KLINE);
            notifyDataSetChanged(entries);
        }
    }

    @Override
    public void computeScroll() {
        if (onVerticalMove) {
            return;
        }

        if (scroller.computeScrollOffset()) {
            final int x = scroller.getCurrX();
            final int dx = x - lastFlingX;
            lastFlingX = x;

            if (onTouch) {
                scroller.abortAnimation();
            } else {
                if (KLINE_STATUS == KLINE_STATUS_RELEASE_BACK) {
                    // 更新滚动的距离，用于拖动松手后回中
                    RenderManager.getInstance().getKlineRender().updateCurrentTransX(dx);
                    RenderManager.getInstance().getKlineRender().updateOverScrollOffset(dx);
                    postInvalidate();
                } else if (KLINE_STATUS == KLINE_STATUS_SPRING_BACK) {

                    // 更新滚动的距离，用于加载数据完成后滚动或者回中
                    if (EntryManager.getInstance().getEntryList().size() > lastEntrySize) {

                        // 滚动
                        RenderManager.getInstance().getKlineRender().scroll(dx);
                        postInvalidate();
                    } else {

                        // 更新滚动的距离，用于拖动松手后回中
                        RenderManager.getInstance().getKlineRender().updateCurrentTransX(dx);
                        RenderManager.getInstance().getKlineRender().updateOverScrollOffset(dx);
                        postInvalidate();
                    }

                } else {
                    // 滚动
                    RenderManager.getInstance().getKlineRender().scroll(dx);
                    postInvalidate();
                }
            }
        } else {
            final float overScrollOffset = RenderManager.getInstance().getKlineRender().getOverScrollOffset();

            if (!onTouch && overScrollOffset != 0 && KLINE_STATUS == KLINE_STATUS_IDLE) {
                lastScrollDx = 0;
                float dx = overScrollOffset;

                if (Math.abs(overScrollOffset) > OVERSCROLL_THRESHOLD) {
                    if (enableLeftRefresh && overScrollOffset > 0) {
                        lastScrollDx = (int) overScrollOffset - OVERSCROLL_THRESHOLD;

                        dx = lastScrollDx;
                    }

                    if (enableRightRefresh && overScrollOffset < 0) {
                        lastScrollDx = (int) overScrollOffset + OVERSCROLL_THRESHOLD;

                        dx = lastScrollDx;
                    }
                }

                KLINE_STATUS = KLINE_STATUS_RELEASE_BACK;
                lastFlingX = 0;
                scroller.startScroll(0, 0, (int) dx, 0, OVERSCROLL_DURATION);
                // ViewCompat.postInvalidateOnAnimation(this);

            } else if (KLINE_STATUS == KLINE_STATUS_RELEASE_BACK) {
                KLINE_STATUS = KLINE_STATUS_LOADING;

                if (listener != null) {
                    lastEntrySize = EntryManager.getInstance().getEntryList().size();
                    if (lastScrollDx > 0) {
                        listener.onLeftRefresh();
                    } else if (lastScrollDx < 0) {
                        listener.onRightRefresh();
                    }
                } else {
                    // 加载完成
                    refreshComplete(false);
                }
            } else {
                KLINE_STATUS = KLINE_STATUS_IDLE;
            }
        }
    }

    /**********************************************************************************************/

    public void notifyDataSetChanged(List<Entry> entryData) {

        EntryManager.getInstance().addData(entryData);
        RenderManager.getInstance().getKlineRender().caculateZoom();
        postInvalidate();
        Log.e("StockChartView", "notifyDataSetChanged");
    }

    private final GestureDetector gestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
        @Override
        public void onLongPress(MotionEvent e) {
            if (onTouch) {
                // 获取Vibrate对象
                final Context context = getContext().getApplicationContext();
                Vibrator vibrator = (Vibrator) context.getSystemService(VIBRATOR_SERVICE);
                vibrator.vibrate(25);
                onLongPress = true;

                // 高亮
                final float x = e.getX();
                final float y = e.getY();
                EntryManager.getInstance().setPointHighlightX(true, x);
                EntryManager.getInstance().setPointHighlightY(y, y);

                //  Log.e("ooooooo1", "长按 ==> x1 = " + EntryManager.getInstance().getPointHighlightX()[0] + ", x2 = " + EntryManager.getInstance().getPointHighlightX()[1]);
                invalidate();

//                if (null != listener) {
//                    final int pointHighlight = EntryManager.getInstance().getPointHighlight();
//                    final Entry entry = EntryManager.getInstance().getEntryList().get(pointHighlight);
//                    listener.onHighlight(entry, pointHighlight, x, y);
//                }
            }
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

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {

            if (!onLongPress && !onDoubleFingerPress && !onVerticalMove) {
                if (onDragging && !RenderManager.getInstance().getKlineRender().canScroll(distanceX) && RenderManager.getInstance().getKlineRender().canDragging(distanceX)) {

                    // 拖动，不同于滚动，当 K 线图到达边界时，依然可以滑动，用来支持加载更多
//                    final KlineRender render = RenderManager.getInstance().getKlineRender();
//                    if (render.getMaxScrollOffset() < 0 || distanceX < 0) {
//                        render.updateCurrentTransX(distanceX);
//                        render.updateOverScrollOffset(distanceX);
//                        invalidate();
//                    }
                } else {
                    // 滚动
                    RenderManager.getInstance().getKlineRender().scroll(distanceX);
                    invalidate();
                }
                return true;
            } else {
                return false;
            }
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            lastFlingX = 0;
            if (!onLongPress && !onDoubleFingerPress && !onVerticalMove && RenderManager.getInstance().getKlineRender().canScroll(0)) {

                scroller.fling(0, 0, (int) -velocityX, 0,
                        Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE);
                return true;
            } else {
                return false;
            }
        }
    });

    private float lastScale = 1f;
    private final ScaleGestureDetector scaleDetector = new ScaleGestureDetector(getContext(), new ScaleGestureDetector.SimpleOnScaleGestureListener() {

        @Override
        public boolean onScale(ScaleGestureDetector detector) {

            if (RenderManager.getInstance().getRenderModel() == RenderManager.MODEL_KLINE_TURNOVER) {
                float temp = detector.getScaleFactor();
                //Log.e("kalurrrrr", "onScale ==> " + temp);

                if (Math.abs(Math.abs(lastScale) - Math.abs(temp)) > 0.1f) {
                    if (temp < 1.0f) {
                        RenderManager.getInstance().getKlineRender().minusZoom((int) detector.getFocusX());
                        lastScale = temp;
                        invalidate();
                    } else if (temp > 1.0f) {
                        RenderManager.getInstance().getKlineRender().addZoom((int) detector.getFocusX());
                        lastScale = temp;
                        invalidate();
                    }
                }
            }

            return super.onScale(detector);
        }
    });

    private GestureMoveActionCompat gestureCompat = new GestureMoveActionCompat(null);

    /**
     * 加载完成
     *
     * @param reverse 是否反转滚动的方向
     */
    public void refreshComplete(boolean reverse) {
        final int overScrollOffset = (int) RenderManager.getInstance().getKlineRender().getOverScrollOffset();

        if (overScrollOffset == 0) return;
        KLINE_STATUS = KLINE_STATUS_SPRING_BACK;
        lastFlingX = 0;
        scroller.startScroll(0, 0, reverse ? -overScrollOffset : overScrollOffset, 0, OVERSCROLL_DURATION);
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