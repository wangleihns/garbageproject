package com.jin.env.garbage.controller.user;

import com.aliyuncs.exceptions.ClientException;
import com.jin.env.garbage.jwt.JwtUtil;
import com.jin.env.garbage.utils.Constants;
import com.jin.env.garbage.utils.ResponseData;
import com.jin.env.garbage.utils.SmsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping(value = "api/v1/user/")
public class LoginApiController {
    private Logger logger = LoggerFactory.getLogger(LoginApiController.class);

    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private RedisTemplate redisTemplate;

    @RequestMapping(value = "login",method = RequestMethod.POST)
    public  ResponseData login(String password, String username){

        return null;
    }



    @RequestMapping(value = "refreshAccessToken",method = RequestMethod.GET)
    public ResponseData refreshAccessToken(String jwt){
        ResponseData responseData = new ResponseData();
        responseData.setStatus(Constants.loginStatus.LoginSuccess.getStatus());
        String accessToken = null;
        String refreshToken = null;
        try {
            String sub = jwtUtil.getSubject(jwt);
            accessToken = jwtUtil.generateJwtToken(sub,"demo");
//            KnUserEntity userEntity = knUserEntityService.findByUserId(Long.valueOf(sub));
//            String username = userEntity.getPhone();
//            redisTemplate.opsForValue().set("accessToken:"+username, accessToken, 2*60*60*1000, TimeUnit.MILLISECONDS); //两小时有效期
//            refreshToken = jwtUtil.getRefresh(accessToken);
        } catch (Exception e) {
            e.printStackTrace();
            responseData.setStatus(500);
            responseData.setMsg("token 不合法");
            return responseData;
        }
        Map<String, String> token= new HashMap<>();
        token.put("accessToken",accessToken);
        token.put("refreshToken",refreshToken);
        responseData.setMsg("refresh success");
        responseData.setData(token);
        return responseData;
    }

    @RequestMapping(value = "getVerificationCode",method = RequestMethod.GET)
    public ResponseData getVerificationCode(String phoneNum){
        ResponseData responseData = new ResponseData();
        SmsUtil smsUtil = SmsUtil.getSmsUtil();
        String randomCode = smsUtil.randomCode(6);
//        randomCode = "123456";
        boolean flag = false;
        try {
            flag =  smsUtil.sendSms(phoneNum,randomCode);
        } catch (ClientException e) {
            e.printStackTrace();
            responseData.setStatus(500);
            responseData.setMsg("短信模板配置不正确，请校验");
            logger.info("短信模板配置不正确，请校验");
            return responseData;
        }
        if (flag){
            redisTemplate.opsForValue().set("randomCode:"+phoneNum, randomCode,30*60*1000, TimeUnit.MILLISECONDS);
            responseData.setStatus(200);
            responseData.setMsg("短信验证码");
            responseData.setData(randomCode);
        }
        return responseData;
    }
}
