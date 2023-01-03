package com.example.umbrellaslide.installdemo.getui;

/**
 * Created by Administrator on 2017/6/15.
 */

import android.content.Context;
import android.util.Log;

import com.example.umbrellaslide.installdemo.MainActivity;
import com.example.umbrellaslide.installdemo.addLog;
import com.igexin.sdk.GTIntentService;
import com.igexin.sdk.PushManager;
import com.igexin.sdk.message.GTCmdMessage;
import com.igexin.sdk.message.GTTransmitMessage;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import static android.content.ContentValues.TAG;


/**
 * 继承 GTIntentService 接收来自个推的消息, 所有消息在线程中回调, 如果注册了该服务, 则务必要在 AndroidManifest中声明, 否则无法接受消息<br>
 * onReceiveMessageData 处理透传消息<br>
 * onReceiveClientId 接收 cid <br>
 * onReceiveOnlineState cid 离线上线通知 <br>
 * onReceiveCommandResult 各种事件处理回执 <br>
 */
public class IntentService extends GTIntentService {

    PushProcessing push;

    public IntentService() {

    }

    @Override
    public void onReceiveServicePid(Context context, int pid) {
    }

    @Override
    public void onReceiveMessageData(Context context, GTTransmitMessage msg) {
        //   MainActivity.showMsgInfo("接收到推送消息：",1);
        String appid = msg.getAppid();
        String taskid = msg.getTaskId();
        String messageid = msg.getMessageId();
        byte[] payload = msg.getPayload();
        String pkg = msg.getPkgName();
        String cid = msg.getClientId();
        // 第三方回执调用接口，actionid范围为90000-90999，可根据业务场景执行
        boolean result = PushManager.getInstance().sendFeedbackMessage(context, taskid, messageid, 90001);
        addLog.addlog(TAG, "call sendFeedbackMessage = " + (result ? "success" : "failed"));
        addLog.addlog(TAG, "onReceiveMessageData -> " + "   appid = " + appid + "   taskid = " + taskid + "   messageid = " + messageid + "   pkg = " + pkg + "   cid = " + cid);
        if (payload == null) {
            addLog.addlog(TAG, "receiver payload = null");
        } else {
            try {
                String data = new String(payload);
                addLog.addlog(TAG, "receiver payload = " + data);
                JSONObject object = JSONAnalysis(data);
                push = new PushProcessing();
                push.Processing(context,object);
//                JSONArray obj = new JSONArray(object.optString("actionExt"));
//                String respcode = object.optString("respcode");
                // 测试消息为了观察数据变化
            } catch (Exception e) {

            }
        }
    }

    /**
     * JSON解析方法
     */
    public static JSONObject JSONAnalysis(String string) {
        JSONObject object = null;
        try {
            object = new JSONObject(string);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return object;
    }

    @Override
    public void onReceiveClientId(Context context, String clientid) {
        addLog.addlog(TAG, "onReceiveClientId -> " + "clientid = " + clientid);
        MainActivity.GTclientid = clientid;
        if (!MainActivity.GTclientid.equals("") && !MainActivity.terminal_no.equals("")) {
            JSONObject obj = new JSONObject();
            try {
                obj.put("terminal_no", MainActivity.terminal_no);
                obj.put("getui_token", MainActivity.GTclientid);
                obj.put("sign", MD5Util.string2MD5(obj.toString() + MainActivity.CONTROL_KEY));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            //   System.out.println("------提交个推Token接口返回消息------");
            String result = um_pushtoken(obj);
            if (result != "erro") {
                addLog.addlog("个推", "um_pushtoken", result);
            } else {
                addLog.addlog("个推", "um_pushtoken", "推送失败");
            }
        }
    }

    @Override
    public void onReceiveOnlineState(Context context, boolean online) {
    }

    @Override
    public void onReceiveCommandResult(Context context, GTCmdMessage cmdMessage) {

    }

    /**
     * 提交个推Token
     *
     * @return
     * @throws Exception
     */
    public String um_pushtoken(JSONObject obj) {
        try {
            //创建连接
            URL url = new URL(MainActivity.serverURL + "um_pushtoken");
            HttpURLConnection connection = (HttpURLConnection) url
                    .openConnection();
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestMethod("POST");
            connection.setUseCaches(false);
            connection.setInstanceFollowRedirects(true);
            connection.setRequestProperty("Content-Type",
                    "application/json");
            connection.setConnectTimeout(10000);  //设置连接主机超时（单位：毫秒）
            connection.setReadTimeout(10000);  //设置从主机读取数据超时（单位：毫秒）
            connection.connect();

            //POST请求
            DataOutputStream out = new DataOutputStream(
                    connection.getOutputStream());

            out.write(obj.toString().getBytes());
            // out.writeBytes(obj.toString());
            out.flush();
            out.close();
            //读取响应
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()));
            String lines;
            StringBuffer sb = new StringBuffer("");
            while ((lines = reader.readLine()) != null) {
                lines = new String(lines.getBytes(), "utf-8");
                sb.append(lines);
            }
            System.out.println(sb);
            JSONAnalysis(sb.toString());
            reader.close();
            connection.getInputStream().close();

            // 断开连接
            connection.disconnect();
            return sb.toString();
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return "erro";
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return "erro";
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return "erro";
        }
    }
}