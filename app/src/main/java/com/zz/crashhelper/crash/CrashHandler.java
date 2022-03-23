package com.zz.crashhelper.crash;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.os.Looper;
import android.os.Process;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by zz on 2021/3/28.
 */
public class CrashHandler implements Thread.UncaughtExceptionHandler {

    private Context context;
    private static CrashHandler crashHandler = new CrashHandler();
    private Thread.UncaughtExceptionHandler defaultUncaughtExceptionHandler;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private Map<String, String> info = new HashMap<>();
    private DateFormat dateFormat = new SimpleDateFormat("yy-MM-dd-hh-mm-ss");
    private BaseHelper baseHelper;

    private CrashHandler() {
    }

    public static CrashHandler getInstance() {
        return crashHandler;
    }

    public void init(Context context, BaseHelper baseHelper) {
        this.context = context;
        this.baseHelper = baseHelper;
        defaultUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
        baseHelper.excute(context, new IHelperListener() {
            @Override
            public void onSuccess() {
                Log.e("CrashHelper--->", "异常上传成功");
            }

            @Override
            public void onFailed() {
                Log.e("CrashHelper--->", "异常上传失败");
            }
        });

    }

    @Override
    public void uncaughtException(@NonNull Thread t, @NonNull Throwable e) {
        Log.e("CrashHelper--->", "异常捕获");
        if (e == null) {
            if (defaultUncaughtExceptionHandler != null) {
                defaultUncaughtExceptionHandler.uncaughtException(t, e);
            }
        } else {
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    Looper.prepare();
                    Toast.makeText(context, "uncaughtException", Toast.LENGTH_LONG).show();
                    Looper.loop();
                }
            });
            //收集日志
            collectErrorInfo();
            //保存日志
            saveErrorInfo(e);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException interruptedException) {
                interruptedException.printStackTrace();
            }
            //会杀掉所有PID一样的进程，比如那些拥有相同UID的应用，统统都会被杀掉。
            Process.killProcess(Process.myPid());
            //是停止程序的虚拟机，杀死当前程序
            System.exit(1);

        }
    }

    /**
     * 手机设备和设备等信息
     */
    private void collectErrorInfo() {
        PackageManager packageManager = context.getPackageManager();
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), PackageManager.GET_ACTIVITIES);
            if (packageInfo != null) {
                String versionName = TextUtils.isEmpty(packageInfo.versionName) ? "未设置versionName" : packageInfo.versionName;
                String versionCode = packageInfo.versionCode + "";

                info.put("versionName", versionName);
                info.put("versionCode", versionCode);
            }

            Field[] fields = Build.class.getFields();
            if(fields !=null && fields.length>0){
                for (Field field : fields) {
                    field.setAccessible(true);
                    info.put(field.getName(), field.get(null).toString());
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 保存错误日志
     *
     * @param throwable
     */
    public void saveErrorInfo(Throwable throwable) {
        StringBuffer stringBuffer = new StringBuffer();
        Iterator<String> iterator = info.keySet().iterator();
        while (iterator.hasNext()) {
            String key = iterator.next();
            String value = info.get(key);
            stringBuffer.append(key + "=" + value + "\n");
        }
        stringBuffer.append("\n------------Crash Log Begin--------------\n");
        //通过StringWriter来获取堆栈信息
        StringWriter stringWriter = new StringWriter();
        //创建一个PrinWriter
        PrintWriter printWriter = new PrintWriter(stringWriter);
        //获取到堆栈信息
        throwable.printStackTrace(printWriter);
        Throwable cause = throwable.getCause();
        while (cause != null) {
            cause.printStackTrace(printWriter);
            cause = throwable.getCause();
        }
        printWriter.close();
        String errorMsg = stringWriter.toString();
        stringBuffer.append(errorMsg);
        stringBuffer.append("\n------------Crash Log End--------------\n");
        String time = dateFormat.format(new Date());
        String fileName = "crash-" + time + ".log";
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            String path = context.getFilesDir() + File.separator + "crash";
//            String path = Environment.getExternalStorageDirectory().toString();
            File file = new File(path);
            if (!file.exists()) {
                file.mkdirs();
            }

            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(new File(path, fileName));
                fos.write(stringBuffer.toString().getBytes());
                fos.flush();
            } catch (FileNotFoundException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            } finally {
                try {
                    if (fos != null) {
                        fos.close();
                    }
                    Log.e("CrashHelper--->", "异常日志保存成功");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    }
}
