package com.jin.env.garbage.utils;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsRequest;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Random;

/**
 * Created by abc on 2018/5/28.
 */

public class SmsUtil {

    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    private static SmsUtil smsUtil = null;
    public boolean sendSms(String phoneNum,String randomCode) throws ClientException{
        boolean flag = false;
        final String accessKeyId = PropertiesUtils.getInstall().properties.getProperty("accessKey");
        final String accessKeySecret = PropertiesUtils.getInstall().properties.getProperty("accessSecret");
        final String templateCode = PropertiesUtils.getInstall().properties.getProperty("templateCode");
        final String signName = PropertiesUtils.getInstall().properties.getProperty("signName");

        System.setProperty("sun.net.client.defaultConnectTimeout", "10000");
        System.setProperty("sun.net.client.defaultReadTimeout", "10000");
        //初始化ascClient需要的几个参数
        final String product = "Dysmsapi";//短信API产品名称（短信产品名固定，无需修改）
        final String domain = "dysmsapi.aliyuncs.com";//短信API产品域名（接口地址固定，无需修改）

        IClientProfile profile = DefaultProfile.getProfile("cn-hangzhou", accessKeyId, accessKeySecret);
        DefaultProfile.addEndpoint("cn-hangzhou", "cn-hangzhou", product, domain);
        IAcsClient acsClient = new DefaultAcsClient(profile);
        //组装请求对象
        SendSmsRequest request = new SendSmsRequest();
        //使用post提交
        request.setMethod(MethodType.POST);
        request.setPhoneNumbers(phoneNum);
        request.setSignName(signName);
        request.setTemplateCode(templateCode);
        //可选:模板中的变量替换JSON串,如模板内容为"亲爱的${name},您的验证码为${code}"时,此处的值为
        //友情提示:如果JSON中需要带换行符,请参照标准的JSON协议对换行符的要求,比如短信内容中包含\r\n的情况在JSON中需要表示成\\r\\n,否则会导致JSON在服务端解析失败
        request.setTemplateParam("{ \"code\":"+randomCode+"}");
        SendSmsResponse sendSmsResponse = acsClient.getAcsResponse(request);
        if(sendSmsResponse.getCode() != null && sendSmsResponse.getCode().equals("OK")) {
            //请求成功
            flag = true;
        }else {
            throw new RuntimeException(sendSmsResponse.getMessage());
        }
        return flag;
    }


    /**
     * 获取随机验证码
     *
     * @param length
     * @return
     */
    public  String randomCode(final int length) {
        char[] chars = "1234567980".toCharArray();
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            char c = chars[random.nextInt(chars.length)];
            sb.append(c);
        }
        if (sb.toString().startsWith("0")){
            return randomCode(6);
        } else {
            return sb.toString();
        }
    }

    public static SmsUtil getSmsUtil(){
        if (smsUtil == null){
            smsUtil = new SmsUtil();
        }
        return smsUtil;
    }
}
