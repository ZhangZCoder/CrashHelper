package com.zz.crashhelper.crash;

import android.content.Context;

/**
 * Created by zz on 2021/3/28.
 */
public class CrashEmailHelper implements BaseHelper{

    @Override
    public void excute(Context context, IHelperListener iHelperListener) {
        //发送Email
    }

    @Override
    public boolean deleteFile() {
        return false;
    }
}
