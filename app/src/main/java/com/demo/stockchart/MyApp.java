package com.demo.stockchart;

import com.lib.stockchart.App;
import com.squareup.leakcanary.LeakCanary;

/**
 * Created by kalu on 2017/11/29.
 */

public class MyApp extends App {

    @Override
    public void onCreate() {
        super.onCreate();

        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        LeakCanary.install(this);
    }
}
