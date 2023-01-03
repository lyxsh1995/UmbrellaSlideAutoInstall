package com.example.umbrellaslide.installdemo;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Environment;
import android.os.Handler;
import android.os.Process;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.elclcd.systempal.core.SysManager;
import com.elclcd.systempal.core.SysManagerImpl;
import com.example.umbrellaslide.installdemo.getui.IntentService;
import com.example.umbrellaslide.installdemo.getui.PushService;
import com.igexin.sdk.PushManager;
import com.loopj.android.http.AsyncHttpResponseHandler;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;


public class MainActivity extends AppCompatActivity {
    static String apkPath = "";//apk路径；
    private boolean result;
    private boolean isInstall = false;
    public static long um_heatbeat_time = 0;
    PackageManager packageManager;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    public static MainActivity mainActivitythis;
    public static String GTclientid = "";            //推送给服务器本机的ID以便服务器推送消息过来；
    public static String terminal_no = "";
    public static String CONTROL_KEY = "88437049439590280cfb16a048279391"; //md5验证密钥
    public static String serverURL = "https://www.mosunshine.com/Umbrella/Control/";
    private static final String[] permission = new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE};

    public static AsyncHttpResponseHandler asyncHttpResponseHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mainActivitythis = this;
        packageManager = getPackageManager();
        sharedPreferences = getSharedPreferences("terminal", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        ActivityCompat.requestPermissions(this, permission, 123);

        terminal_no = getIntent().getStringExtra("no");
        if (terminal_no != null && !terminal_no.equals("")) {
            editor.putString("no", terminal_no).apply();
        } else {
            terminal_no = sharedPreferences.getString("no", "");
        }
        addLog.addlog("自动升级程序", "启动", "terminal_no", terminal_no);

        asyncHttpResponseHandler = new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                addLog.addlog("自动升级程序", "上传", "成功", statusCode + "");
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                addLog.addlog("自动升级程序", "上传", "失败", statusCode + "");
            }
        };

        TextView textView = (TextView) findViewById(R.id.textView);
        textView.setOnClickListener(new View.OnClickListener() {
            final static int COUNTS = 5;//点击次数
            final static long DURATION = 3 * 1000;//规定有效时间
            long[] mHits = new long[COUNTS];

            @Override
            public void onClick(View v) {
                System.arraycopy(mHits, 1, mHits, 0, mHits.length - 1);
                //实现左移，然后最后一个位置更新距离开机的时间，如果最后一个时间和最开始时间小于DURATION，即连续5次点击
                mHits[mHits.length - 1] = SystemClock.uptimeMillis();
                if (mHits[0] >= (SystemClock.uptimeMillis() - DURATION)) {
                    addLog.addlog("自动升级程序", "人为退出");
                    finish();
                }
            }
        });

        if (!SysManagerImpl.isInitialized()) {
            //openTimerSwitchService 开启"定时开关机"模块,一个设备只允许一个应用开启该功能，多个应用开启会导致功能异常、失效。
            SysManagerImpl.initContext(this, false);
        }

        if (SysManagerImpl.isInitialized()) {
            addLog.addlog("自动升级程序", "加载京东权限模块", "成功");
        }else{
            addLog.addlog("自动升级程序", "加载京东权限模块", "失败");
        }

        PushManager.getInstance().initialize(MainActivity.this, PushService.class);
        // com.getui.demo.DemoIntentService 为第三方自定义的推送服务事件接收类
        PushManager.getInstance().registerPushIntentService(MainActivity.this, IntentService.class);
        if (PushManager.getInstance().isPushTurnedOn(MainActivity.this)) {
            PushManager.getInstance().turnOnPush(MainActivity.this);
        }

        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(60 * 1000);
                    if (System.currentTimeMillis() - um_heatbeat_time > 60 * 1000) {
                        addLog.addlog("自动升级程序", "检测到借伞程序心跳丢失", "启动借伞程序");
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

    @Override
    protected void onDestroy() {
        addLog.addlog("自动升级程序", "退出");
        super.onDestroy();
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

    public static void RebootSystem(Context context)   //重启安卓板；
    {
        addLog.addlog("自动升级程序", "重启安卓板");
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
                install(context, apkPath);
            }
        }
    }

    public static void install(Context context, String apkPath) {
        addLog.addlog("自动升级程序", "自动升级", "开始安装");
        try {
            SysManagerImpl.getInstance().installAPK(apkPath, new SysManager.OnHandleApkListener() {
                @Override
                public void onResult(int i, String s) {
                    addLog.addlog("自动升级程序", i + "", s);
                    Toast.makeText(context, i + s, Toast.LENGTH_SHORT).show();
                    if (i == 0) {
                        addLog.addlog("自动升级程序", "自动升级", "安装成功");
                    } else {
                        addLog.addlog("自动升级程序", "自动升级", "安装失败");
                    }
                    deletandcopy();  //安装成功后删除历史版本 拷贝新版本apk；
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            RebootSystem(context);
                        }
                    }, 3000);
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

