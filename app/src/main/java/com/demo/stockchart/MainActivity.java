package com.demo.stockchart;

import android.annotation.SuppressLint;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;

import com.lib.stockchart.OnStockChartChangeListener;
import com.lib.stockchart.OnStockChartChangeListenerSimple;
import com.lib.stockchart.StockChartView;
import com.lib.stockchart.entry.Entry;
import com.lib.stockchart.entry.EntryManager;
import com.lib.stockchart.entry.StockDataTest;
import com.lib.stockchart.render.RenderManager;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private int temp = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;

    @SuppressLint("StaticFieldLeak")
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

    @SuppressLint("StaticFieldLeak")
    private void loadData() {
        new AsyncTask<Void, Void, ArrayList<Entry>>() {

            @Override
            protected void onPreExecute() {
                EntryManager.getInstance().resetData();
            }

            @Override
            protected ArrayList<Entry> doInBackground(Void... params) {

                String kLineData = "";
                try {
                    InputStream in = getResources().getAssets().open("kline1.txt");
                    int length = in.available();
                    byte[] buffer = new byte[length];
                    in.read(buffer);
                    kLineData = new String(buffer, "UTF-8");
                } catch (Exception e) {
                    e.printStackTrace();
                }

                final ArrayList<Entry> entries = StockDataTest.parseKLineData(kLineData);
                return entries;
            }

            @Override
            protected void onPostExecute(final ArrayList<Entry> aVoid) {

                final StockChartView mStockChartView = findViewById(R.id.kLineLayout);

                final int model = RenderManager.getInstance().getRenderModel();
                if (model == RenderManager.MODEL_TLINE_TURNOVER) {
                    final List<Entry> entries = aVoid.subList(0, 100);
                    mStockChartView.addDataSetChanged(entries);
                } else if (model == RenderManager.MODEL_KLINE_TURNOVER) {
                    mStockChartView.addDataSetChanged(aVoid);


                }
            }
        }.execute();



    }
}
