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
    private boolean onTouch = false;
    private boolean onLongPress = false;
    private boolean onDoubleFingerPress = false;
    private boolean onVerticalMove = false;
    private boolean onDragging = true;
    private boolean enableLeftRefresh = true;
    private boolean enableRightRefresh = true;

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
            int pointMin = a.getInt(R.styleable.StockChartView_scv_point_min, 25);
            EntryManager.getInstance().setPointMin(pointMin);
            int pointSpace = a.getDimensionPixelSize(R.styleable.StockChartView_scv_point_space, 10);
            EntryManager.getInstance().setPointSpace(pointSpace);
            final int boardPadding = a.getDimensionPixelSize(R.styleable.StockChartView_scv_board_padding, 5);
            EntryManager.getInstance().setBoardPadding(boardPadding);
            final int xlabelHeight = a.getDimensionPixelSize(R.styleable.StockChartView_scv_xlabel_height, 25);
            EntryManager.getInstance().setXlabelHeight(xlabelHeight);
            final int xoffsetMax = a.getDimensionPixelSize(R.styleable.StockChartView_scv_xoffset_max, 100);
            EntryManager.getInstance().setXoffsetMax(xoffsetMax);

            canDragXoffset = a.getBoolean(R.styleable.StockChartView_scv_xoffset_enable, false);

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
//                    EntryManager.getInstance().setPointHighlightX(false, x);
//                    EntryManager.getInstance().setPointHighlightY(-2f, y);

                    RenderManager.getInstance().getKlineRender().setxHighligh(x);
                    RenderManager.getInstance().getTlineRender().setxHighligh(x);
                    RenderManager.getInstance().getKlineRender().setyHighligh(y);
                    RenderManager.getInstance().getTlineRender().setyHighligh(y);
                    postInvalidate();

                    if (EntryManager.getInstance().isHighlightMove()) {
                        // Log.e("ooooooo1", "移动 ==> x = " + x + ", x1 = " + EntryManager.getInstance().getPointHighlightX()[0] + ", x2 = " + EntryManager.getInstance().getPointHighlightX()[1]);
                        postInvalidate();

//                        if (null != listener) {
//                            final int pointHighlight = EntryManager.getInstance().getPointHighlight();
//                            final Entry entry = EntryManager.getInstance().getEntryList().get(pointHighlight);
//                            listener.onHighlight(entry, pointHighlight, x, y);
//                        }
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
//                EntryManager.getInstance().setPointHighlightX(true, -1);
//                EntryManager.getInstance().setPointHighlightY(-1f, -1f);
//                invalidate();
                RenderManager.getInstance().getKlineRender().setxHighligh(-1f);
                RenderManager.getInstance().getTlineRender().setxHighligh(-1f);
                RenderManager.getInstance().getKlineRender().setyHighligh(-1f);
                RenderManager.getInstance().getTlineRender().setyHighligh(-1f);
                postInvalidate();

                // todo
                if (canDragXoffset) {
                    final int xoffsetRight = EntryManager.getInstance().getXoffsetRight();
                    if (xoffsetRight != 0) {
                        EntryManager.getInstance().setXoffsetRight(0);
                        EntryManager.getInstance().setXoffsetLeft(0);
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
            RenderManager.getInstance().getTlineRender().onCanvas(canvas, onLongPress, model);
        } else if (model == RenderManager.MODEL_KLINE_TURNOVER) {

          //  Log.e("StockChartView", "onDraw KLLINE");
            RenderManager.getInstance().getKlineRender().onCanvas(canvas, onLongPress, model);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        int left = getLeft() + getPaddingLeft();
        int top = getTop() + getPaddingTop();
        int right = getRight() - getPaddingRight();
        int bottom = getBottom() - getPaddingBottom();
      //  Log.e("StockChartView", "left = " + left + ", top = " + top + ", right = " + right + ", bottom = " + bottom);
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

    public void notifyDataSetChanged(List<Entry> entryData) {

        EntryManager.getInstance().addData(entryData);
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
                final float x = e.getRawX();
                final float y = e.getRawY();
                Log.e("kaluyyyy", "onLongPress ==> x = " + x + ", y = " + y);

                //  Log.e("ooooooo1", "长按 ==> x1 = " + EntryManager.getInstance().getPointHighlightX()[0] + ", x2 = " + EntryManager.getInstance().getPointHighlightX()[1]);
                RenderManager.getInstance().getKlineRender().setxHighligh(x);
                RenderManager.getInstance().getTlineRender().setxHighligh(x);
                RenderManager.getInstance().getKlineRender().setyHighligh(y);
                RenderManager.getInstance().getTlineRender().setyHighligh(y);

                postInvalidate();

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
            // Log.e("kaluyyyy", "onScroll ==> distanceX = " + distanceX);

            if (e1.getPointerCount() != 1 || e2.getPointerCount() != 1) return false;

            final int pointSum = EntryManager.getInstance().getEntryList().size();
            if (pointSum == 0) return false;

            if (RenderManager.getInstance().getRenderModel() == RenderManager.MODEL_KLINE_TURNOVER) {

                final int pointBegin = EntryManager.getInstance().getPointBegin();
                final int pointEnd = EntryManager.getInstance().getPointEnd();

                // 左划大于0
                if (distanceX > 10f) {

                    // 滚动
                    if (pointEnd == pointSum && canDragXoffset) {

                        final int offsetRight = EntryManager.getInstance().getXoffsetRight();
                        final int xoffsetMax = EntryManager.getInstance().getXoffsetMax();
                        if (Math.abs(offsetRight) >= xoffsetMax) {
                            Log.e("rrrrr", "滑倒最右侧了");
                            return false;
                        }

                        EntryManager.getInstance().setXoffsetRight((int) (offsetRight - distanceX));
                        EntryManager.getInstance().setXoffsetLeft(0);
                        // Log.e("rrrrr", "右侧刷新 ==> offsetRight = " + EntryManager.getInstance().getOffsetRight() + ", xoffsetMax = " + xoffsetMax);

                        // RenderManager.getInstance().getKlineRender().caculateZoom();
                        postInvalidate();
                    } else {

                        final int temp1 = pointEnd + 1;
                        if (temp1 > pointSum) return false;

                        final int temp2 = pointBegin + 1;
                        if (temp2 <= 1) return false;

                        EntryManager.getInstance().setPointEnd(temp1);
                        EntryManager.getInstance().setPointBegin(temp2);
                        postInvalidate();
                    }
                }
                // 右划小于0
                else if (distanceX < -10f) {

                    if (pointBegin == 1 && canDragXoffset) {

                        final int offsetLeft = EntryManager.getInstance().getXoffsetLeft();
                        final int xoffsetMax = EntryManager.getInstance().getXoffsetMax();
                        if (offsetLeft >= xoffsetMax) {

                            Log.e("rrrrr", "滑倒最左侧了");
                            return false;
                        }

                        Log.e("rrrrr", "左侧刷新");
                        EntryManager.getInstance().setXoffsetLeft((int) (offsetLeft + distanceX));
                        EntryManager.getInstance().setXoffsetRight(0);

                        // RenderManager.getInstance().getKlineRender().caculateZoom();
                        postInvalidate();
                    } else {

                        final int temp1 = pointEnd - 1;
                        if (temp1 > pointSum) return false;

                        final int temp2 = pointBegin - 1;
                        if (temp2 <= 1) return false;

                        EntryManager.getInstance().setPointEnd(temp1);
                        EntryManager.getInstance().setPointBegin(temp2);
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
    });

    private final ScaleGestureDetector scaleDetector = new ScaleGestureDetector(getContext(), new ScaleGestureDetector.SimpleOnScaleGestureListener() {

        @Override
        public boolean onScale(ScaleGestureDetector detector) {

            // K线图才会缩放
            if (RenderManager.getInstance().getRenderModel() == RenderManager.MODEL_KLINE_TURNOVER) {
                float f = detector.getScaleFactor();

                if (f < 1.0f) {
                    Log.e("yt", "onScale ==> 缩小 " + f);


                    final int pointSum = EntryManager.getInstance().getEntryList().size();
                    //  Log.e("yt", "缩小 ==> pointSum = " + pointSum);
                    if (pointSum == 0) return super.onScale(detector);

                    final int pointEnd = EntryManager.getInstance().getPointEnd();
                    // Log.e("yt", "缩小 ==> pointEnd = " + pointEnd);
                    if (pointEnd == pointSum) return super.onScale(detector);

                    final int pointCount = EntryManager.getInstance().getPointCount();
//                    final int pointMax = EntryManager.getInstance().getPointMax();
//                    final int pointMin = EntryManager.getInstance().getPointMin();
//                   // Log.e("yt", "缩小 ==> pointCount = " + pointCount + ", pointMax = " + pointMax + ", pointMin = " + pointMin);
//                    if (pointCount >= pointMax || pointCount <= pointMin) return super.onScale(detector);

                    final int tempCount = pointCount + 2;
                    EntryManager.getInstance().setPointCount(tempCount);
                    final int pointBegin = EntryManager.getInstance().getPointBegin();
                    EntryManager.getInstance().setPointBegin(pointBegin - 1);
                    EntryManager.getInstance().setPointEnd(pointEnd + 1);

                    postInvalidate();

                } else if (f > 1.0f) {
                    Log.e("yt", "onScale ==> 放大 " + f);

                    final int pointSum = EntryManager.getInstance().getEntryList().size();
                    // Log.e("yt", "addZoom ==> pointSum = " + pointSum);
                    if (pointSum == 0) return super.onScale(detector);

                    final int pointEnd = EntryManager.getInstance().getPointEnd();
                    //  Log.e("yt", "addZoom ==> pointEnd = " + pointEnd);
                    if (pointEnd == pointSum) return super.onScale(detector);

                    final int pointCount = EntryManager.getInstance().getPointCount();
//                    final int pointMax = EntryManager.getInstance().getPointMax();
//                    final int pointMin = EntryManager.getInstance().getPointMin();
//                 //   Log.e("yt", "addZoom ==> pointCount = " + pointCount + ", pointMax = " + pointMax + ", pointMin = " + pointMin);
//                    if (pointCount >= pointMax || pointCount <= pointMin)
//                        return super.onScale(detector);

                    final int tempCount = pointCount - 2;
                    EntryManager.getInstance().setPointCount(tempCount);
                    final int pointBegin = EntryManager.getInstance().getPointBegin();
                    EntryManager.getInstance().setPointBegin(pointBegin + 1);
                    EntryManager.getInstance().setPointEnd(pointEnd - 1);

                    postInvalidate();
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