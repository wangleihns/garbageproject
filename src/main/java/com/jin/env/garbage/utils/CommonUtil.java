package com.jin.env.garbage.utils;

import com.alibaba.druid.support.json.JSONUtils;
import org.springframework.util.Base64Utils;

import javax.servlet.http.HttpServletRequest;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class CommonUtil {
    public static Map<String,Object>  base64Decode(String jwt){
        String[] info = jwt.split("\\.");
        String message = new String(Base64Utils.decode(info[1].getBytes()));
        Map<String,Object> map = (Map<String,Object>) JSONUtils.parse(message);
        return map;
    }

    /**
     * 检查token 是否过期
     */
    public static boolean checkTokenExp(HttpServletRequest request){
        String header = request.getHeader("Authorization");
        String accessToken = header.split(" ")[1];
        Map<String,Object> map = base64Decode(accessToken);
        Long currentTime = new Date().getTime()/1000;
        Object v = map.get("exp");
        Long exp = Long.valueOf(v+"");
        if (currentTime >= exp) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 检查签名是否正确
     * @param request
     * @return
     */
    public static boolean checkSignVail(HttpServletRequest request){
        Map<String, String[]> parameterMap = request.getParameterMap();
        String sign = request.getHeader("sign");
        String secret = Constants.signSecret.Secret.getContent();
        String v_sign = sign(secret,parameterMap);
        if (v_sign.equals(sign.toUpperCase())){
            return true;
        } else {
            return false;
        }
    }

    public static String sign(String secret,Map<String,String[]> params){
        //按参数名asscic码排序
        List<String> names=new ArrayList();
        names.addAll(params.keySet());
        java.util.Collections.sort(names);
        String strSign = "";
        for(String key:names){
            strSign+=key;
        }
        strSign+=secret;
        return md5(strSign).toUpperCase();
    }

    /**
     * 偶尔加密会出现问题
     * @param str
     * @return
     */
    public static String getMD5(String str) {
        try {
            // 生成一个MD5加密计算摘要
            MessageDigest md = MessageDigest.getInstance("MD5");
            // 计算md5函数
            md.update(str.getBytes());
            // digest()最后确定返回md5 hash值，返回值为8为字符串。因为md5 hash值是16位的hex值，实际上就是8位的字符
            // BigInteger函数则将8位的字符串转换成16位hex值，用字符串来表示；得到字符串形式的hash值
            return new BigInteger(1, md.digest()).toString(16);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String md5(String str){
        MessageDigest md5 = null;
        try{
            md5 = MessageDigest.getInstance("MD5");
        }catch (Exception e){
            System.out.println(e.toString());
            e.printStackTrace();
            return "";
        }
        char[] charArray = str.toCharArray();
        byte[] byteArray = new byte[charArray.length];
        for (int i = 0; i < charArray.length; i++)
            byteArray[i] = (byte) charArray[i];
        byte[] md5Bytes = md5.digest(byteArray);
        StringBuffer hexValue = new StringBuffer();
        for (int i = 0; i < md5Bytes.length; i++){
            int val = ((int) md5Bytes[i]) & 0xff;
            if (val < 16)
                hexValue.append("0");
            hexValue.append(Integer.toHexString(val));
        }
        return hexValue.toString().toUpperCase();
    }
}
