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
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import javax.persistence.criteria.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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
        GarbageUserEntity userEntity = garbageUserDao.findByPhoneOrENoOrIdCard(username);
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
    public ResponseData register(String jsonParam) {
        ResponseData responseData = new ResponseData();
        jsonParam = "{\n" +
                 "\t\n" +
                 "\tloginName:'15158099385'\n" +
                 "\tpassword:'123456',\n" +
                 "\trepeatPassword:'123456',\n" +
                 "\tphone:'15158099385',\n" +
                 "\tname:'zhangsan',\n" +
                 "\tcompany:'ddd公司'\n" +
                 "\teNo:'21212',\n" +
                 "\tidCard:'1234567199900890X',\n" +
                 "\tsex:1,\n" +
                 "\tcleaner:false,\n" +
                 "\tprovinceId:1,\n" +
                 "\tprovinceName:'ddd',\n" +
                 "\tcityId:2,\n" +
                 "\tcityName:'杭州',\n" +
                 "\tdistrictId:3,\n" +
                 "\tdistrictName:'滨江',\n" +
                 "\ttownId:4;\n" +
                 "\ttownName:'ddddddd',\n" +
                 "\tvillageId:5,\n" +
                 "\tvillageName:'gg',\n" +
                 "\taddress:'ggggg'\n" +
                 "\tisCollector:true\n" +
                 "}";
        GarbageUserEntity userEntity = new GarbageUserEntity();
        JSONObject object = JSONObject.fromObject(jsonParam);
        String password = object.getString("password");
        String repeatPassword = object.getString("repeatPassword");
        String name = object.getString("name");
        String company = object.getString("company");
        String eNo = object.getString("eNo");
        String idCard = object.getString("idCard");
        Integer sex = object.getInt("sex");
        Boolean cleaner = object.getBoolean("cleaner");
        Integer provinceId = object.getInt("provinceId");
        String provinceName = object.getString("provinceName");
        Integer cityId = object.getInt("cityId");
        String cityName = object.getString("cityName");
        Integer districtId = object.getInt("districtId");
        String districtName = object.getString("districtName");
        Integer townId = object.getInt("townId");
        String townName = object.getString("townName");
        Integer villageId = object.getInt("villageId");
        String villageName = object.getString("villageName");
        String address = object.getString("address");
        Boolean isCollector = object.getBoolean("isCollector");
        Assert.state(repeatPassword.equals(password), "密码与重复密码不一致");
        userEntity.setLoginName(object.getString("loginName"));
        userEntity.setPhone(object.getString("phone"));
        userEntity.setPassword(CommonUtil.md5(password));
        userEntity.setName(name);
        userEntity.setCompany(company);
        userEntity.setCredentialsNonExpired(true);
        userEntity.setAccountNonExpired(true);
        userEntity.setAccountNonLocked(true);
        userEntity.setEnabled(true);
        userEntity.seteNo(eNo);
        userEntity.setIdCard(idCard);
        userEntity.setSex(sex);
        userEntity.setCleaner(cleaner);
        userEntity.setProvinceId(provinceId);
        userEntity.setProvinceName(provinceName);
        userEntity.setCityId(cityId);
        userEntity.setCityName(cityName);
        userEntity.setDistrictId(districtId);
        userEntity.setDistrictName(districtName);
        userEntity.setTownId(townId);
        userEntity.setTownName(townName);
        userEntity.setVillageId(villageId);
        userEntity.setVillageName(villageName);
        userEntity.setAddress(address);
        GarbageRoleEntity roleEntity = null;
        if (!isCollector){
            //普通居民
            roleEntity = garbageRoleDao.findByRoleCode("RESIDENT");
        } else {
            //垃圾收集员
            roleEntity = garbageRoleDao.findByRoleCode("COLLECTOR");
        }
        userEntity.getRoles().add(roleEntity);
        garbageUserDao.save(userEntity);
        responseData.setMsg("注册成功");
        return responseData;
    }

    @Transactional
    public ResponseData updatePassword(String token, String oldPassword, String newPassword) {
        ResponseData responseData = new ResponseData();
        try {
            Integer sub = jwtUtil.getSubject(token);
            GarbageUserEntity userEntity = garbageUserDao.findById(sub).get();
            if (!CommonUtil.md5(oldPassword).equals(userEntity.getPassword())){
                throw new RuntimeException("原始密码不正确");
            }
            userEntity.setPassword(CommonUtil.md5(newPassword));
            garbageUserDao.save(userEntity);
            responseData.setStatus(Constants.loginStatus.LoginSuccess.getStatus());
            responseData.setMsg("密码修改成功");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        return  responseData;
    }

    public ResponseData collectorList(String jwt) {
        ResponseData responseData = new ResponseData();
        Integer sub = jwtUtil.getSubject(jwt);
        List<GarbageRoleEntity> roleEntityList = garbageRoleDao.findByUserId(sub);
        List<String> roleCodeList = roleEntityList.stream().map(garbageRoleEntity -> garbageRoleEntity.getRoleCode()).collect(Collectors.toList());
        List<GarbageUserEntity> userEntityList = garbageUserDao.findAll(new Specification<GarbageUserEntity>() {
            @Override
            public Predicate toPredicate(Root<GarbageUserEntity> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                if (roleCodeList.contains("")){
                    //村
                }
                if (roleCodeList.contains("")){

                }
                if (roleCodeList.contains("")){

                }
                if (roleCodeList.contains("")){

                }
                if (roleCodeList.contains("")){

                }
                return null;
            }
        });
        responseData.setData(userEntityList);
        return responseData;

    }
}
