[戳我下载 ==>](https://pan.baidu.com/s/1sl3FUs1)

##
## 图片预览
![image](https://github.com/153437803/StockChart/blob/master/Screenrecorder-2017-12-05.gif ) 

##
## 项目结构
![image](https://github.com/153437803/StockChart/blob/master/20171130002727.png )

##
## 使用方法
```
1.添加数据, 即刷新界面
StockChartView.addDataSetChanged(List<Entey>);
2.删除数据
StockChartView.clearDataSetChanged();
3.监听
StockChartView.setOnStockChartChangeListener(new OnStockChartChangeListener() {

                        # 暂时无效果
                        @Override
                        public void onLeftRefresh() {
                            
                        }

                        # 暂时无效果
                        @Override
                        public void onRightRefresh() {

                        }
                        
                        # 暂时无效果
                        @Override
                        public void onHighlight(Entry entry, int entryIndex, float x, float y) {

                        }

                        @Override
                        public void onSingleTap(MotionEvent e, float x, float y) {

                        }

                        @Override
                        public void onDoubleTap(MotionEvent e, float x, float y) {

                        }
});
```
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
    <!-- 点与点间隙 -->
    <attr name="scv_point_space" format="dimension|reference" />
    <!-- 内边框边距 -->
    <attr name="scv_board_padding" format="dimension|reference" />
    <!-- X轴显示区域高度 -->
    <attr name="scv_xlabel_height" format="dimension|reference" />
    <!-- 刷新拖拽最大位移  -->
    <attr name="scv_xoffset_max" format="dimension|reference" />
    <!-- 是否可以刷新  -->
    <attr name="scv_xoffset_enable" format="boolean" />
</declare-styleable>
```
```
<com.lib.stockchart.StockChartView
            android:id="@+id/kLineLayout"
            android:layout_width="match_parent"
            android:layout_height="400dp"
            android:background="#44999999"
            android:paddingBottom="10dp"
            android:paddingLeft="10dp"
            android:paddingRight="10dp"
            android:paddingTop="10dp"
            app:scv_hint_load="努力加载数据中..."
            app:scv_point_count="50"
            app:scv_point_max="80"
            app:scv_point_min="25"
            app:scv_point_space="5dp"
            app:scv_weight_down="1"
            app:scv_weight_top="4"
            app:scv_xlabel_height="10dp"
            app:scv_xoffset_enable="true"
            app:scv_xoffset_max="80dp" />
```
```
todo list：

1.横竖屏切换，无画面（已解决）
  虚线设置setLayerType(View.LAYER_TYPE_SOFTWARE, null); 横屏切换ondraw()不执行
2.高亮（已解决）
3.滑动（已解决）

1.rsi mal
2.左右拖拽刷新
```
