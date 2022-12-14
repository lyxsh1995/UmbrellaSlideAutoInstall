package com.example.umbrellaslide.installdemo;

import android.util.Log;

import com.example.umbrellaslide.installdemo.getui.VeDate;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by jj on 2017/6/9.
 */
public class addLog {
    public static void addlog(String... texts) {
        String fileName = VeDate.getStringDateShort() + ".txt";
        File vFile = new File("");
        try {
            vFile = new File(MainActivity.getInnerSDCardPath() + "/UmLog");
            if (vFile.exists()) {//如果文件存在
            } else {
                String dir = vFile.getAbsolutePath();
                new File(dir).mkdir();//新建文件夹
                vFile.createNewFile();//新建文件
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        File logFile = new File(vFile.getPath() + File.separator + fileName);
        try {
            //如果文件不存在,就动态创建文件
            if (!logFile.exists()) {
                logFile.createNewFile();
            }
            String writeStr = "  " + VeDate.getStringDateTime();
            for (int i = 0; i < texts.length; i++) {
                writeStr += "    " + texts[i];
            }
            //设置为:True,表示写入的时候追加数据
            FileWriter fw = new FileWriter(logFile, true);
            //回车并换行
            fw.write(writeStr + "\r\n");
            fw.close();
            Log.d("addlog",writeStr);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
