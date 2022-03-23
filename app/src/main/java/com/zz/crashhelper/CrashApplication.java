package com.zz.crashhelper;

import android.app.Application;

import com.zz.crashhelper.crash.CrashHandler;
import com.zz.crashhelper.crash.CrashNetHelper;

/**
 * Created by zz on 2021/3/28.
 */
public class CrashApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        CrashHandler.getInstance().init(this, new CrashNetHelper());
    }
}
