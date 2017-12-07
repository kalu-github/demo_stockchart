package com.demo.stockchart;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.lib.stockchart.OnStockChartChangeListenerSimple;
import com.lib.stockchart.StockChartView;
import com.lib.stockchart.entry.Entry;
import com.lib.stockchart.entry.EntryManagerTest;
import com.lib.stockchart.render.RenderManager;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private int temp = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loadData();

        findViewById(R.id.c).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setRequestedOrientation(temp);
                temp = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
            }
        });

        findViewById(R.id.a).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RenderManager.getInstance().setRenderModel(RenderManager.MODEL_KLINE_TURNOVER);
                final StockChartView mStockChartView = findViewById(R.id.kLineLayout);
                mStockChartView.clearDataSetChanged();
                mStockChartView.postInvalidate();

                loadData();
            }
        });

        findViewById(R.id.b).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                RenderManager.getInstance().setRenderModel(RenderManager.MODEL_TLINE_TURNOVER);
                final StockChartView mStockChartView = findViewById(R.id.kLineLayout);
                mStockChartView.clearDataSetChanged();
                mStockChartView.postInvalidate();

                loadData();
            }
        });

        final StockChartView mStockChartView = findViewById(R.id.kLineLayout);
        mStockChartView.setOnStockChartChangeListener(new OnStockChartChangeListenerSimple() {

            @Override
            public void onLeftRefresh() {

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mStockChartView.refreshComplete(false);
                    }
                }, 1000);
            }

            @Override
            public void onRightRefresh() {

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mStockChartView.refreshComplete(false);
                    }
                }, 1000);
            }
        });
    }

    private void loadData() {

        final List<Entry> aVoid = EntryManagerTest.parseKLineData(EntryManagerTest.KLINE);
        final StockChartView mStockChartView = findViewById(R.id.kLineLayout);
        mStockChartView.addDataSetChanged(aVoid);
    }
}
