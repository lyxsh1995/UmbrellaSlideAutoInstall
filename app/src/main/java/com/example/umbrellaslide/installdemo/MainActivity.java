package com.example.umbrellaslide.installdemo;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Environment;
import android.os.Handler;
import android.os.Process;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
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
    String apkPath = "";//apk路径；
    private boolean result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (!SysManagerImpl.isInitialized()) {
            //openTimerSwitchService 开启"定时开关机"模块,一个设备只允许一个应用开启该功能，多个应用开启会导致功能异常、失效。
            SysManagerImpl.initContext(this,false);
        }
        try {
            onSilentInstall();
        } catch (Exception e) {
            //看门狗；看门狗(WATCHDOG)功能是当系统发生严重错误，不能自行恢复时，看门狗能够让系统重置恢复正常,
            // 确保系统长时间可靠运行。系统看门狗的喂狗最长间隔为 12s, 请在 12s 之内发出清除 watchdog 广播。
            RebootSystem();
            e.printStackTrace();
        }
    }

    public void onSilentInstall() {
//        try {  //关闭旧app  自动安装新app并重启安卓板；
//            ActivityManager am = (ActivityManager) getApplicationContext().getSystemService(getApplicationContext().ACTIVITY_SERVICE);
//            List<ActivityManager.RunningAppProcessInfo> myappprocess = am.getRunningAppProcesses();
//            for (ActivityManager.RunningAppProcessInfo info : myappprocess) {
//
//                if (info.processName.equals("com.example.umbrellaslide.installdemo")) {
//
//                    int pid = info.pid;
//                    Process.killProcess(pid);
//                    break;
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        File sdDir = null;
        boolean sdCardExist = Environment.getExternalStorageState()
                .equals(android.os.Environment.MEDIA_MOUNTED);//判断sd卡是否存在
        if (sdCardExist) {
            sdDir = Environment.getExternalStorageDirectory();//获取跟目录
        }

        File vFile = new File(sdDir.toString() + "/" + "UpdateSlide");
        List<String> extPaths = getExtSDCardPath();
        for (String path : extPaths) {
            vFile = new File(path + "/" + "UpdateLite");
        }
        vFile = new File(MainActivity.getInnerSDCardPath() + "/UpdateLite");  //改用内置sd卡
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
        if (apkPath.equals("")) {
            RebootSystem();
        }

        if (!isRoot()) {
            Toast.makeText(this, "没有ROOT权限，不能使用秒装", Toast.LENGTH_SHORT).show();
            RebootSystem();
        }

        jdinstall(apkPath);
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                SilentInstall installHelper = new SilentInstall();
//                result = installHelper.install(apkPath);
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        if (result) {
//                            Toast.makeText(MainActivity.this, "安装成功！", Toast.LENGTH_SHORT).show();
//                            deletandcopy(new File(apkPath));  //安装成功后删除历史版本 拷贝新版本apk；
//                            //看门狗；看门狗(WATCHDOG)功能是当系统发生严重错误，不能自行恢复时，看门狗能够让系统重置恢复正常,
//                            // 确保系统长时间可靠运行。系统看门狗的喂狗最长间隔为 12s, 请在 12s 之内发出清除 watchdog 广播。
//                            try {
//                                Thread.sleep(2000);
//                            } catch (Exception e) {
//
//                            }
//                            RebootSystem();
//                            //  doStartApplicationWithPackageName("com.android.umbrella");
//                        } else {
//                            Toast.makeText(MainActivity.this, "安装失败！", Toast.LENGTH_SHORT).show();
//                            RebootSystem();
//                        }
//                    }
//                });
//            }
//        }).start();

    }


    public void deletandcopy(File newfile) {
        try {
            //删除 apk；
            File vFile = new File("");
            List<String> extPaths = getExtSDCardPath();


            //删除升级文件夹里文件；
            vFile = new File("");
            extPaths = getExtSDCardPath();
            for (String path : extPaths) {
                vFile = new File(path + "/" + "UpdateLite");
            }
            vFile = new File(MainActivity.getInnerSDCardPath() + "/UpdateLite");  //改用内置sd卡
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
  /*  public void onForwardToAccessibility(View view) {

    }
    public void onSmartInstall(View view) {

    }*/

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

    private void RebootSystem()   //重启安卓板；
    {
        PackageManager packageManager = getPackageManager();
        Intent intent = packageManager.getLaunchIntentForPackage("com.khkj.administrator.umbrellalite");
        startActivity(intent);
        intent = new Intent("com.android.action.reboot");
        sendBroadcast(intent);
//        SysManagerImpl.getInstance().reboot();
    }

    public boolean jdinstall(String apkPat){
        try {
            SysManagerImpl.getInstance().installAPK(apkPat, new SysManager.OnHandleApkListener() {
                @Override
                public void onResult(int i, String s) {
                    Log.d(i+"",s);
                    if(i == 0 ){
                        deletandcopy(new File(apkPath));  //安装成功后删除历史版本 拷贝新版本apk；
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                RebootSystem();
                            }
                        },3000);
                    }
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }
}

