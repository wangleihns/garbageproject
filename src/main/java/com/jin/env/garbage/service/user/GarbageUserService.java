package com.jin.env.garbage.service.user;

import com.jin.env.garbage.controller.user.LoginApiController;
import com.jin.env.garbage.dao.user.GarbageResourceDao;
import com.jin.env.garbage.dao.user.GarbageRoleDao;
import com.jin.env.garbage.dao.user.GarbageUserDao;
import com.jin.env.garbage.entity.user.GarbageResourceEntity;
import com.jin.env.garbage.entity.user.GarbageRoleEntity;
import com.jin.env.garbage.entity.user.GarbageUserEntity;
import com.jin.env.garbage.jwt.JwtUtil;
import com.jin.env.garbage.utils.CommonUtil;
import com.jin.env.garbage.utils.Constants;
import com.jin.env.garbage.utils.ResponseData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class GarbageUserService {
    private Logger logger = LoggerFactory.getLogger(LoginApiController.class);

    @Value(value = "${garbageTokenTime}")
    private Integer garbageTokenTime;

    @Autowired
    private GarbageUserDao garbageUserDao;

    @Autowired
    private GarbageRoleDao garbageRoleDao;

    @Autowired
    private GarbageResourceDao garbageResourceDao;

    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    public ResponseData findByPhoneOrLoginNameOrENoOrIdCard(String password, String username, String from) {
        ResponseData responseData = new ResponseData();
        GarbageUserEntity userEntity = garbageUserDao.findByPhoneOrLoginNameOrENoOrIdCard(username);
        if (userEntity == null){
            throw new RuntimeException("查无此账号，请核对账号");
        }
        if (!userEntity.getAccountNonLocked()){
            throw  new RuntimeException("账号已锁定");
        }
        if (!userEntity.getAccountNonExpired()){
            throw new RuntimeException("账号已过期");
        }
        if (!userEntity.getCredentialsNonExpired()){
            throw new RuntimeException("密码过期");
        }
        if (!userEntity.getEnabled()){
            throw new RuntimeException("账号不可用");
        }
        if(!CommonUtil.md5(password).equals(userEntity.getPassword())){
            throw new RuntimeException("账号或密码不正确");
        } else {
            String accessToken =  "";
            Map<String, Object> token= new HashMap<>();
            if (Constants.loginType.GarbageCar.getType().equals(from)){
                //垃圾车 token 有效时长
                accessToken = jwtUtil.generateJwtToken(userEntity.getId().toString(),"garbage", garbageTokenTime);
            } else {
                //其他客户端使用默认有效时长
                accessToken = jwtUtil.generateJwtToken(userEntity.getId().toString(),"garbage", null);
            }
            String refreshToken = jwtUtil.getRefresh(accessToken);
            redisTemplate.opsForValue().set("accessToken:"+username, accessToken, 2*60*60*1000, TimeUnit.MILLISECONDS); //两小时有效期
            String a =  redisTemplate.opsForValue().get("accessToken:" + username);
            List<GarbageResourceEntity> resourceEntityList = garbageResourceDao.findByResourceByUserId(userEntity.getId());
            token.put("accessToken", accessToken);
            token.put("refreshToken", refreshToken);
            token.put("userEntity", userEntity);
            token.put("resources", resourceEntityList);
            logger.info(a);
            responseData.setMsg("登录成功");
            responseData.setStatus(Constants.loginStatus.LoginSuccess.getStatus());
            responseData.setData(token);
        }
        return responseData;
    }

    @Transactional
    public ResponseData register(String password, String phone) {
        ResponseData responseData = new ResponseData();
        GarbageUserEntity userEntity = new GarbageUserEntity();
        userEntity.setPhone(phone);
        userEntity.setPassword(CommonUtil.md5(password));
        GarbageRoleEntity roleEntity = garbageRoleDao.findByRoleCode("RESIDENT");
        userEntity.getRoles().add(roleEntity);
        garbageUserDao.save(userEntity);
        responseData.setMsg("注册成功");
        return responseData;
    }
}
