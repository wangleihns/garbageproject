package com.jin.env.garbage.controller.user;

import com.aliyuncs.exceptions.ClientException;
import com.jin.env.garbage.entity.user.GarbageUserEntity;
import com.jin.env.garbage.jwt.JwtUtil;
import com.jin.env.garbage.service.user.GarbageUserService;
import com.jin.env.garbage.utils.Constants;
import com.jin.env.garbage.utils.ResponseData;
import com.jin.env.garbage.utils.ResponsePageData;
import com.jin.env.garbage.utils.SmsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
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

    @Autowired
    private GarbageUserService garbageUserService;

    @RequestMapping(value = "login",method = RequestMethod.POST)
    public  ResponseData login(String password, String username, String from){
        Assert.hasText(username, "登录名不能为空");
        Assert.hasText(password, "密码不能为空");
        ResponseData responseData = garbageUserService.findByPhoneOrLoginNameOrENoOrIdCard(password, username, from);
        return responseData;
    }

    @RequestMapping(value = "register",method = RequestMethod.POST)
    public ResponseData register(String jsonParam){
        Assert.hasText(jsonParam,"注册信息不能为空");
        ResponseData responseData = garbageUserService.register(jsonParam);
        return responseData;
    }



    @RequestMapping(value = "refreshAccessToken",method = RequestMethod.GET)
    public ResponseData refreshAccessToken(String jwt){
        ResponseData responseData = new ResponseData();
        responseData.setStatus(Constants.loginStatus.LoginSuccess.getStatus());
        String accessToken = null;
        String refreshToken = null;
        try {
            Integer sub = jwtUtil.getSubject(jwt);
            accessToken = jwtUtil.generateJwtToken(sub.toString(),"garbage", null);
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

    @RequestMapping(value = "updatePassword", method = RequestMethod.POST)
    public ResponseData updatePassword(String oldPassword, String newPassword, String repeatPassword, HttpServletRequest request){
        Assert.hasText(oldPassword, "原始密码不能为空");
        Assert.hasText(newPassword, "新密码不能空");
        Assert.hasText(repeatPassword, "重复密码不能为空");
        Assert.state(newPassword.equals(repeatPassword), "新密码与重复密码不一致");
        String jwt = request.getHeader("Authorization").split(": ")[1];
        ResponseData responseData = garbageUserService.updatePassword(jwt, oldPassword, newPassword);
        return responseData;
    }

    @RequestMapping(value = "collectorList", method = RequestMethod.GET)
    public ResponsePageData collectorList(String name, String phone, String idCard, String value, Integer provinceId,
                                          Integer cityId, Integer countryId, Integer townId, Integer villageId,
                                          String[] orderBys, HttpServletRequest request){
        String jwt = request.getHeader("Authorization").split(": ")[1];
        Integer pageNo = Integer.valueOf(request.getParameter("pageNo"));
        Integer pageSize = Integer.valueOf(request.getParameter("pageSize"));
        ResponsePageData responsePageData = garbageUserService.collectorList(name, phone, idCard, value,provinceId, cityId, countryId, townId, villageId, jwt, pageNo, pageSize, orderBys);
        return responsePageData;
    }

    @RequestMapping(value = "getUserInfoById", method = RequestMethod.GET)
    public ResponseData getUserInfoById(HttpServletRequest request){
        String jwt = request.getHeader("Authorization").split(": ")[1];
        ResponseData responseData = garbageUserService.getUserInfoById(jwt);
        return responseData;
    }

    @RequestMapping(value = "deleteUserById", method = RequestMethod.DELETE)
    public ResponseData deleteUserById(HttpServletRequest request, Integer status){
        String jwt = request.getHeader("Authorization").split(": ")[1];
        ResponseData responseData = garbageUserService.deleteUserById(jwt, status);
        return responseData;
    }

    @RequestMapping(value = "updateUserInfo", method = RequestMethod.POST)
    public ResponseData updateUserInfo(GarbageUserEntity userEntity, Integer fileId){
        ResponseData responseData = garbageUserService.updateUserInfo(userEntity, fileId);
        return responseData;
    }

    @RequestMapping(value = "residentList", method = RequestMethod.GET)
    public ResponseData residentList(String name, String phone, String idCard, String eNo, Integer provinceId,
                                     Integer cityId, Integer countryId,  Integer townId, Integer villageId,
                                     String roleCode, String[] orderBys, HttpServletRequest request){
        String jwt = request.getHeader("Authorization").split(": ")[1];
        Integer pageNo = Integer.valueOf(request.getParameter("pageNo"));
        Integer pageSize = Integer.valueOf(request.getParameter("pageSize"));
        ResponseData responseData = garbageUserService.residentList(name, phone, idCard, eNo, provinceId, cityId, countryId, townId, villageId, roleCode, jwt, pageNo, pageSize, orderBys);
        return responseData;
    }
}
