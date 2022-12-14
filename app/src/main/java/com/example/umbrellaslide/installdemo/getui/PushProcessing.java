package com.example.umbrellaslide.installdemo.getui;

import android.content.ContentValues;
import android.content.ContextWrapper;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;
import android.view.View;

import com.example.umbrellaslide.installdemo.MainActivity;
import com.example.umbrellaslide.installdemo.addLog;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2017/11/17.
 */
public class PushProcessing {
    Cursor curDev = null;
    ContentValues InitialValues = null;
    static ContextWrapper contextWrapper = new ContextWrapper(MainActivity.mainActivitythis);

    public void Processing(JSONObject object) {
        String actionName = object.optString("actionName");
        String terminalNo = object.optString("terminalNo");
        String terminalSerialNo = object.optString("terminalSerialNo");
        callback(object.optString("id"));//通知服务器
        switch (actionName) {
            case "TerminalRestart":    //重启系统；
                addLog.addlog("个推", "接收到重启指令");
                Intent intent = new Intent("com.android.action.reboot");
                contextWrapper.sendBroadcast(intent);
                intent = new Intent("umbrella.REBOOT");
                contextWrapper.sendBroadcast(intent);
                break;
            case "UploadTerminalLog":     //日志上传
                addLog.addlog("个推", "接收到拉取日志指令");
                try {
                    JSONObject obj = new JSONObject(object.optString("actionExt"));
                    String startDate = obj.optString("startDate");
                    String endDate = obj.optString("endDate");
                    String file = "";
                    if (startDate.trim().equals(""))//起始没有
                    {
                        if (!endDate.trim().equals("")) {
                            file = endDate.trim() + ".txt";
                        }
                    } else {
                        if (endDate.trim().equals("")) {
                            file = startDate.trim() + ".txt";
                        } else {
                            if (startDate.trim().equals(endDate.trim())) {
                                file = startDate.trim() + ".txt";
                            } else {
                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                                Date sDate = sdf.parse(startDate.trim());
                                Date eDate = sdf.parse(endDate.trim());
                                Calendar start = Calendar.getInstance();
                                start.setTime(sDate);
                                Long startTIme = start.getTimeInMillis();
                                Calendar end = Calendar.getInstance();
                                end.setTime(eDate);
                                Long endTime = end.getTimeInMillis();
                                Long oneDay = 1000 * 60 * 60 * 24l;
                                Long time = startTIme;
                                while (time <= endTime) {
                                    Date d = new Date(time);
                                    DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                                    file += VeDate.dateToStr(d) + ".txt,";
                                    time += oneDay;
                                }
                            }
                        }
                    }
                    String[] filelist = file.split(",");
                    String fileName = "";
                    for (int i = 0; i < filelist.length; i++) {
                        fileName = filelist[i].toString();
                        if (!fileName.trim().equals("")) {
                            String path = Environment.getExternalStorageDirectory().getPath() + File.separator + "UmLog";
                            try {
                                File vFile = new File("");
                                vFile = new File(Environment.getExternalStorageDirectory().getPath() + "/UmLog");
                                //  File logFile = new File(vFile.getPath() + File.separator + "2017-11-20.txt");
                                Map<String, Object> dataRecord = new HashMap<String, Object>();
                                dataRecord.put("terminalNo", MainActivity.terminal_no);
                                JSONObject json = new JSONObject(dataRecord);
                                uploadLog(path, fileName);
                                addLog.addlog("个推", "终端日志文件上传：", "时间：" + VeDate.getStringDateTime(), "", "日志名称:" + fileName, "");
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            case "TerminalCaptureScreen":   //截屏上传
                try {
                    if (screenshot("screenshot.png")) {
                        onClickUpload("screenshot.png");  //定时抽帧；
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            default:
                break;
        }
    }

    public static void callback(String id) {
        try {
            URL url = new URL("https://www.mosunshine.com/stream/terminal/restart/response?id=" + id);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            int code = connection.getResponseCode();
            if (code == HttpURLConnection.HTTP_OK) {
                //成功
                addLog.addlog("自动升级程序", id, "回调", "成功", code + "");
            } else {
                addLog.addlog("自动升级程序", id, "回调", "失败", code + "");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void uploadLog(String filepath, String filename) {
        File file = new File(filepath, filename);
        if (file.exists() && file.length() > 0) {
            RequestParams params = new RequestParams();
            params.setContentEncoding("UTF-8");
            params.put("terminalNo", MainActivity.terminal_no);
            try {
                params.put("log", file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            try {
                new AsyncHttpClient().post("https://www.mosunshine.com/stream/terminal/log/upload", params, MainActivity.asyncHttpResponseHandler);   //生产地址上传
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public boolean screenshot(String pngName)  //广告视频抽帧
    {
        boolean res = false;
        try {
            // 获取屏幕
            View view = MainActivity.mainActivitythis.getWindow().getDecorView();
            view.setDrawingCacheEnabled(true);
            view.buildDrawingCache();
            Bitmap bitmap = Bitmap.createBitmap(view.getDrawingCache(), 0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
            if (bitmap != null) {
                try {
                    String sdCardPath = Environment.getExternalStorageDirectory().getPath();
                    String filePath1 = sdCardPath + File.separator + pngName;
                    File file1 = new File(filePath1);
                    FileOutputStream os1 = new FileOutputStream(file1);
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, os1);
                    os1.flush();
                    os1.close();
                    res = true;
                } catch (Exception e) {
                    bitmap.recycle();
                }
                bitmap.recycle();
            }
        } catch (Exception e) {

        }
        return res;
    }

    public void onClickUpload(String pngName) {   //广告抽帧图片上传服务器
        try {
            String path = Environment.getExternalStorageDirectory().getPath() + File.separator;
            String file = pngName;
            Map<String, Object> dataRecord = new HashMap<String, Object>();
            dataRecord.put("terminal_no", MainActivity.terminal_no);
            dataRecord.put("capture_time", VeDate.getStringDateTime());
            dataRecord.put("city_no", "0021");
            dataRecord.put("sign", MD5Util.string2MD5(dataRecord.toString() + MainActivity.CONTROL_KEY));
            JSONObject json = new JSONObject(dataRecord);
            upload(path, file, json, MainActivity.asyncHttpResponseHandler);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void upload(String filepath, String filename, JSONObject json, AsyncHttpResponseHandler responseHandler) {
        File file = new File(filepath, filename);
        if (file.exists() && file.length() > 0) {
            String jsonText = json.toString();
            RequestParams params = new RequestParams();
            params.setContentEncoding("UTF-8");
            params.put("desc", jsonText);
            try {
                params.put("file", file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            try {
                new AsyncHttpClient().post("https://www.mosunshine.com/stream/control/um_filenormal", params, responseHandler);   //定时上传
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
