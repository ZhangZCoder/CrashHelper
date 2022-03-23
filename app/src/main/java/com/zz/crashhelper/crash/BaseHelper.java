package com.zz.crashhelper.crash;

import android.content.Context;

/**
 * Created by zz on 2021/3/28.
 */
public interface BaseHelper {

    void excute(Context context, IHelperListener iHelperListener);


    boolean deleteFile();
}