package com.example.umbrellaslide.installdemo.getui;




import java.io.File;
import java.io.FileInputStream;
import java.math.BigInteger;
import java.security.MessageDigest;

/** *
 * 采用MD5加密解密
 * Created by Administrator on 2017/4/7.
 */
public class MD5Util {

    /***
     * MD5加码 生成32位md5码
     */
    public static String string2MD5(String inStr) {
            try {
                MessageDigest md = MessageDigest.getInstance("MD5");
                md.update(inStr.getBytes());
                byte b[] = md.digest();

                int i;

                StringBuffer buf = new StringBuffer("");
                for (int offset = 0; offset < b.length; offset++) {
                    i = b[offset];
                    if (i < 0)
                        i += 256;
                    if (i < 16)
                        buf.append("0");
                    buf.append(Integer.toHexString(i));
                }
                inStr = buf.toString();
            } catch (Exception e) {
                e.printStackTrace();

            }
            return inStr;
        }

    public static String getMd5(String path) {
        BigInteger bi = null;
        String md5str="";
        try {
            byte[] buffer = new byte[8192];
            int len = 0;
            MessageDigest md = MessageDigest.getInstance("MD5");
            //  String str=    DigestUtils.md5Hex( new FileInputStream(path));
            File f = new File(path);
            FileInputStream fis = new FileInputStream(f);
            while ((len = fis.read(buffer)) != -1) {
                md.update(buffer, 0, len);
            }
            fis.close();
            byte[] b = md.digest();
            md5str=   byte2hex(b);

            //  bi = new BigInteger(1, b);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return   md5str;// bi.toString(16);
    }

    public static String byte2hex(byte[] b) {//二行制转字符串
        String hs="";
        String stmp="";
        for (int n=0;n<b.length;n++)
        {
            stmp=(Integer.toHexString(b[n] & 0XFF));
            if (stmp.length()==1) hs=hs+"0"+stmp;
            else hs=hs+stmp;
            // if (n<b.length-1)  hs=hs+"";
        }
        return hs.toUpperCase();
    }
}
