package com.lib.stockchart;

import android.app.Application;
import android.content.Context;

/**
 * Created by kalu on 2017/11/12.
 */

public class App extends Application {

    public static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();

        mContext = getApplicationContext();
    }
}
