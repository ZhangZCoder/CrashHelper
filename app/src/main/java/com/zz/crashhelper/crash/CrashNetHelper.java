package com.zz.crashhelper.crash;

import android.content.Context;

import java.io.File;

/**
 * Created by zz on 2021/3/28.
 */
public class CrashNetHelper implements BaseHelper{
    private Context context;
    @Override
    public void excute(Context context, IHelperListener iHelperListener) {
        this.context = context;
        //上传日志,成功则执行下面删除
        if (deleteFile()){
            iHelperListener.onSuccess();
        }else {
            iHelperListener.onFailed();
        }
    }

    @Override
    public boolean deleteFile() {
        String filePath = context.getFilesDir() + File.separator + "crash";
        File file = new File(filePath);
        File[] files = file.listFiles();
        if (files ==null || files.length == 0)
            return true;
        for (File file1 : files) {
            file1.delete();
            return true;
        }
        return true;
    }
}
