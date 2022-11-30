package com.example.umbrellaslide.installdemo;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Environment;
import android.os.Handler;
import android.os.Process;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.elclcd.systempal.core.SysManager;
import com.elclcd.systempal.core.SysManagerImpl;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    static String apkPath = "";//apk路径；
    private boolean result;
    private boolean isInstall = false;
    public static long um_heatbeat_time = 0;
    PackageManager packageManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        packageManager = getPackageManager();

        TextView textView = (TextView) findViewById(R.id.textView);
        textView.setOnClickListener(new View.OnClickListener() {
            final static int COUNTS = 5;//点击次数
            final static long DURATION = 3 * 1000;//规定有效时间
            long[] mHits = new long[COUNTS];

            @Override
            public void onClick(View v) {
                /**
                 * 实现双击方法
                 * src 拷贝的源数组
                 * srcPos 从源数组的那个位置开始拷贝.
                 * dst 目标数组
                 * dstPos 从目标数组的那个位子开始写数据
                 * length 拷贝的元素的个数
                 */
                System.arraycopy(mHits, 1, mHits, 0, mHits.length - 1);
                //实现左移，然后最后一个位置更新距离开机的时间，如果最后一个时间和最开始时间小于DURATION，即连续5次点击
                mHits[mHits.length - 1] = SystemClock.uptimeMillis();
                if (mHits[0] >= (SystemClock.uptimeMillis() - DURATION)) {
                    finish();
                }
            }
        });

        if (!SysManagerImpl.isInitialized()) {
            //openTimerSwitchService 开启"定时开关机"模块,一个设备只允许一个应用开启该功能，多个应用开启会导致功能异常、失效。
            SysManagerImpl.initContext(this, false);
        }

        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(60 * 1000);
                    if (System.currentTimeMillis() - um_heatbeat_time > 60 * 1000) {
                        Intent intent = packageManager.getLaunchIntentForPackage("com.android.umbrella");
                        startActivity(intent);
                        intent = packageManager.getLaunchIntentForPackage("com.khkj.administrator.umbrellalite");
                        startActivity(intent);
                    }
                } catch (Exception ignored) {
                }
            }

        }).start();

        jdinstall(MainActivity.this);
    }


    public static void deletandcopy() {
        try {
            //删除 apk；
            File vFile = new File(MainActivity.getInnerSDCardPath() + "/Update");  //改用内置sd卡
            if (vFile.exists()) {//如果文件存在
                File temp = null;
                for (int i = 0; i < vFile.list().length; i++) {
                    temp = new File(vFile.getPath() + File.separator + vFile.list()[i]);
                    if (temp.isFile()) {
                        temp.delete();
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("复制整个文件夹内容操作出错");
            e.printStackTrace();
        }
    }

    /**
     * 判断手机是否拥有Root权限。
     *
     * @return 有root权限返回true，否则返回false。
     */
    public boolean isRoot() {
        boolean bool = false;
        try {
            bool = new File("/system/bin/su").exists() || new File("/system/xbin/su").exists();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bool;
    }

    /**
     * 获取内置SD卡路径
     */
    static String getInnerSDCardPath() {
        return Environment.getExternalStorageDirectory().getPath();
    }

    /**
     * 获取外置SD卡路径
     *
     * @return 应该就一条记录或空
     */
    public List<String> getExtSDCardPath() {
        List<String> lResult = new ArrayList<String>();
        try {
            Runtime rt = Runtime.getRuntime();
            java.lang.Process proc = rt.exec("mount");
            InputStream is = proc.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line;
            while ((line = br.readLine()) != null) {
                if (line.contains("extsd")) {
                    String[] arr = line.split(" ");
                    String path = arr[1];
                    File file = new File(path);
                    if (file.isDirectory()) {
                        lResult.add(path);
                    }
                }
            }
            isr.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lResult;
    }

    private static void RebootSystem(Context context)   //重启安卓板；
    {
        PackageManager packageManager = context.getPackageManager();
        try {
            Intent intent = packageManager.getLaunchIntentForPackage("com.android.umbrella");
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            Intent intent = packageManager.getLaunchIntentForPackage("com.khkj.administrator.umbrellalite");
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Intent intent = new Intent("com.android.action.reboot");
        context.sendBroadcast(intent);
        if (SysManagerImpl.isInitialized()) {
            SysManagerImpl.getInstance().reboot();
        }
    }

    public static void jdinstall(Context context) {
        File vFile = new File(MainActivity.getInnerSDCardPath() + "/Update");  //改用内置sd卡
        if (vFile.exists()) {//如果文件存在
            File temp = null;
            for (int i = 0; i < vFile.list().length; i++) {
                temp = new File(vFile.getPath() + File.separator + vFile.list()[i]);
                if (temp.isFile()) {
                    if (temp.getName().endsWith(".APK")) {  //检查APK名称；
                        apkPath = temp.getAbsolutePath().toString();
                    }
                }
            }
        }
        if (!apkPath.equals("")) {
            if (SysManagerImpl.isInitialized()) {
                install(context,apkPath);
            }
        }
    }

    public static void install(Context context,String apkPath){
        try {
            SysManagerImpl.getInstance().installAPK(apkPath, new SysManager.OnHandleApkListener() {
                @Override
                public void onResult(int i, String s) {
                    Log.d(i + "", s);
                    Toast.makeText(context, i + s, Toast.LENGTH_SHORT).show();
                    if (i == 0) {
                        deletandcopy();  //安装成功后删除历史版本 拷贝新版本apk；
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                RebootSystem(context);
                            }
                        }, 3000);
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 判断应用是否在运行
     *
     * @return
     */
    public static boolean isAppRunning(Context context, String pageName) {
        boolean isAppRunning = false;
        try {
            ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningTaskInfo> list = activityManager.getRunningTasks(100);

            for (ActivityManager.RunningTaskInfo info : list) {
                if (info.topActivity.getPackageName().equals(pageName) || info.baseActivity.getPackageName().equals(pageName)) {
                    isAppRunning = true;
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return isAppRunning;
    }
}

