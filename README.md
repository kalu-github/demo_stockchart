![image](https://github.com/153437803/StockChart/blob/master/Screenrecorder-2017-11-22-21-45-58-295_20171122220313.gif )  

```
    <declare-styleable name="StockChartView">
        <!-- 权重 - 上面 -->
        <attr name="scv_weight_top" format="integer" />
        <!-- 权重 - 下面 -->
        <attr name="scv_weight_down" format="integer" />
        <!-- 加载提示信息 -->
        <attr name="scv_hint_load" format="string|reference" />
        <!-- 默认显示多少个点 -->
        <attr name="scv_point_count" format="integer" />
        <!-- 最多显示多少个点 -->
        <attr name="scv_point_max" format="integer" />
        <!-- 最少显示多少个点 -->
        <attr name="scv_point_min" format="integer" />
    </declare-styleable>
```

```
<com.lib.stockchart.StockChartView
            android:id="@+id/kLineLayout"
            android:layout_width="match_parent"
            android:layout_height="400dp"
            android:background="@android:color/holo_orange_dark"
            android:paddingBottom="10dp"
            android:paddingLeft="10dp"
            android:paddingRight="10dp"
            android:paddingTop="10dp"
            app:scv_hint_load="努力加载数据中..."
            app:scv_weight_down="1"
            app:scv_weight_top="4" />
```

```
存在问题：

1.滑动
2.高亮
3.横竖屏切换，无画面
```
```
todo list：

1.rsi mal 分时图
```
