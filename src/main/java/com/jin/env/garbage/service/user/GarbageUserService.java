package com.jin.env.garbage.service.user;

import com.jin.env.garbage.controller.user.LoginApiController;
import com.jin.env.garbage.dao.image.GarbageImageDao;
import com.jin.env.garbage.dao.user.GarbageENoDao;
import com.jin.env.garbage.dao.user.GarbageResourceDao;
import com.jin.env.garbage.dao.user.GarbageRoleDao;
import com.jin.env.garbage.dao.user.GarbageUserDao;
import com.jin.env.garbage.dto.user.GarbageUserDto;
import com.jin.env.garbage.dto.user.QRcodeDto;
import com.jin.env.garbage.entity.image.GarbageImageEntity;
import com.jin.env.garbage.entity.user.GarbageENoEntity;
import com.jin.env.garbage.entity.user.GarbageResourceEntity;
import com.jin.env.garbage.entity.user.GarbageRoleEntity;
import com.jin.env.garbage.entity.user.GarbageUserEntity;
import com.jin.env.garbage.jwt.JwtUtil;
import com.jin.env.garbage.utils.*;
import net.sf.json.JSONObject;
import org.apache.tomcat.util.bcel.Const;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import javax.persistence.criteria.*;
import javax.persistence.metamodel.EntityType;
import java.io.InputStream;
import java.util.*;
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
    private GarbageImageDao garbageImageDao;

    @Autowired
    private GarbageENoDao garbageENoDao;

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
                 "\teNo:'21212,23456',\n" +
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
                "\tfileId:1\n" +
                 "}";
        GarbageUserEntity userEntity = new GarbageUserEntity();
        JSONObject object = JSONObject.fromObject(jsonParam);
        String password = object.getString("password");
        String repeatPassword = object.getString("repeatPassword");
        String name = object.getString("name");
        String company = object.getString("company");
        String eNos = object.getString("eNo");
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
        Integer fileId = object.getInt("fileId");
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
        userEntity = garbageUserDao.save(userEntity);
        //图片保存
        GarbageImageEntity garbageImageEntity = garbageImageDao.findById(fileId).get();
        garbageImageEntity.setSourceName(GarbageUserEntity.class.getName());
        garbageImageEntity.setBusId(userEntity.getId());
        garbageImageEntity.setType(Constants.image.HEADER.name());
        garbageImageEntity.setAttribute(0);
        garbageImageDao.save(garbageImageEntity);
        //保存eno
        Integer userId = userEntity.getId();
        String[] eNoElements = eNos.split(",");
        List<GarbageENoEntity> eNoEntityList = new ArrayList<>();
        Arrays.stream(eNoElements).forEach(e->{
            GarbageENoEntity garbageENoEntity = new GarbageENoEntity();
            garbageENoEntity.setUserId(userId);
            garbageENoEntity.seteNo(e);
            eNoEntityList.add(garbageENoEntity);
        });
        garbageENoDao.saveAll(eNoEntityList);

        //生成二维码
        QRcodeDto dto = new QRcodeDto();
        dto.setId(userEntity.getId());
        dto.seteNo(eNos);
        dto.setName(name);
        dto.setProvinceName(provinceName);
        dto.setCityName(cityName);
        dto.setCountryName(districtName);
        dto.setTownName(townName);
        dto.setVillageName(villageName);
        InputStream inputStream = QRCodeUtil.initQRCode(JSONObject.fromObject(dto).toString());
        String fileName = UUID.randomUUID().toString() + ".png";
        String url = OssUpLoad.uploadImage(inputStream, fileName);
        GarbageImageEntity qrCodeImage = new GarbageImageEntity();
        qrCodeImage.setSourceName(GarbageUserEntity.class.getName());
        qrCodeImage.setBusId(userId);
        qrCodeImage.setOriginalFilename(fileName);
        qrCodeImage.setImageName(fileName);
        qrCodeImage.setImagePath(url);
        qrCodeImage.setType(Constants.image.QRCODE.name());
        qrCodeImage.setAttribute(0);
        garbageImageDao.save(qrCodeImage);
        responseData.setMsg("注册成功");
        responseData.setStatus(Constants.responseStatus.Success.getStatus());
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

    public ResponsePageData collectorList(String name, String phone, String idCard, String value, Integer provinceId,
                                          Integer cityId, Integer countryId, Integer townId, Integer villageId,
                                          String jwt, Integer pageNo, Integer pageSize, String[] orderBys) {
        ResponsePageData responsePageData = new ResponsePageData();
        Pageable pageable = PageRequest.of(pageSize, pageSize, getCollectorSort(orderBys));
        Integer sub = jwtUtil.getSubject(jwt);
        GarbageUserEntity userEntity = garbageUserDao.findById(sub).get();
        List<GarbageRoleEntity> roleEntityList = garbageRoleDao.findByUserId(sub);
        List<String> roleCodeList = roleEntityList.stream().map(garbageRoleEntity -> garbageRoleEntity.getRoleCode()).collect(Collectors.toList());
        Page<GarbageUserEntity> page = garbageUserDao.findAll(new Specification<GarbageUserEntity>() {
            @Override
            public Predicate toPredicate(Root<GarbageUserEntity> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicateList = new ArrayList<>();
                //正常用户
                Predicate status = criteriaBuilder.equal(root.get("status"), 1);
                predicateList.add(status);
                if (roleCodeList.contains("COLLECTOR")){
                    //垃圾收集员
                    Predicate predicate = criteriaBuilder.equal(root.get("id"), sub);
                    predicateList.add(predicate);
                }
                if (roleCodeList.contains("VILLAGE_ADMIN")){
                    //村
                    Predicate predicate = criteriaBuilder.equal(root.get("villageId"), userEntity.getVillageId());
                    predicateList.add(predicate);

                }
                if (roleCodeList.contains("TOWN_ADMIN")){
                    //乡镇
                    Predicate predicate = criteriaBuilder.equal(root.get("townId"), townId);
                    predicateList.add(predicate);
                    if (villageId !=null){
                        Predicate villageIdPredicate = criteriaBuilder.equal(root.get("villageId"), villageId);
                        predicateList.add(villageIdPredicate);
                    }
                }
                if (roleCodeList.contains("COUNTRY_ADMIN")){
                    // 县/区
                    Predicate predicate = criteriaBuilder.equal(root.get("districtId"), countryId);
                    predicateList.add(predicate);
                    if (townId !=null){
                        Predicate townIdPredicate = criteriaBuilder.equal(root.get("townId"), townId);
                        predicateList.add(townIdPredicate);
                    }
                    if (villageId !=null){
                        Predicate villageIdPredicate = criteriaBuilder.equal(root.get("villageId"), villageId);
                        predicateList.add(villageIdPredicate);
                    }
                }
                if (roleCodeList.contains("CITY_ADMIN")){
                    //市
                    Predicate predicate = criteriaBuilder.equal(root.get("cityId"), cityId);
                    predicateList.add(predicate);
                    if (countryId !=null){
                        Predicate countryIdPredicate = criteriaBuilder.equal(root.get("districtId"), countryId);
                        predicateList.add(countryIdPredicate);
                    }
                    if (townId !=null){
                        Predicate townIdPredicate = criteriaBuilder.equal(root.get("townId"), townId);
                        predicateList.add(townIdPredicate);
                    }
                    if (villageId !=null){
                        Predicate villageIdPredicate = criteriaBuilder.equal(root.get("villageId"), villageId);
                        predicateList.add(villageIdPredicate);
                    }
                }
                if (roleCodeList.contains("PROVINCE_ADMIN")){
                    //省
                    Predicate predicate = criteriaBuilder.equal(root.get("provinceId"), userEntity.getProvinceId());
                    predicateList.add(predicate);
                    if (cityId !=null) {
                        Predicate cityIdPredicate = criteriaBuilder.equal(root.get("cityId"), cityId);
                        predicateList.add(cityIdPredicate);
                    }
                    if (countryId !=null){
                        Predicate countryIdPredicate = criteriaBuilder.equal(root.get("districtId"), countryId);
                        predicateList.add(countryIdPredicate);
                    }
                    if (townId !=null){
                        Predicate townIdPredicate = criteriaBuilder.equal(root.get("townId"), townId);
                        predicateList.add(townIdPredicate);
                    }
                    if (villageId !=null){
                        Predicate villageIdPredicate = criteriaBuilder.equal(root.get("villageId"), villageId);
                        predicateList.add(villageIdPredicate);
                    }
                }
                if (!StringUtils.isEmpty(name)) {
                    Predicate predicateName = criteriaBuilder.like(root.get("name"), "%" + name + "%");
                    predicateList.add(predicateName);
                }
                if (!StringUtils.isEmpty(phone)) {
                    Predicate predicateName = criteriaBuilder.like(root.get("phone"), "%" + phone + "%");
                    predicateList.add(predicateName);
                }
                if (!StringUtils.isEmpty(idCard)) {
                    Predicate predicateName = criteriaBuilder.like(root.get("idCard"), "%" + idCard + "%");
                    predicateList.add(predicateName);
                }

                return criteriaBuilder.and(predicateList.toArray(new Predicate[predicateList.size()]));
            }
        }, pageable);
        responsePageData.setPageSize(pageSize);
        responsePageData.setPageNo(pageNo);
        responsePageData.setLastPage(page.isLast());
        responsePageData.setFirstPage(page.isFirst());
        responsePageData.setData(page.getContent());
        responsePageData.setMsg("垃圾收集员列表查询成功");
        responsePageData.setStatus(Constants.responseStatus.Success.getStatus());
        return responsePageData;

    }

    private Sort getCollectorSort(String[] orderBys){
        Sort sort = null;
        if (orderBys == null || orderBys.length == 0 ){
            sort = Sort.by("id");
        }else {
            sort =   Sort.by(Arrays.stream(orderBys).map((it) -> {
                String[] items = it.split(";");
                String property = "";
                Sort.Direction direction = null;
                if (items.length > 1){
                    property = items[0];
                    switch (property){
                        case "name":
                            property = "name";
                            break;
                        case "phone":
                            property = "phone";
                            break;
                        case "address":
                            property = "address";
                            break;
                        case "createtime":
                            property = "createTime";
                            break;
                        default:
                            property = "id";
                            break;

                    }
                    if ("desc".equalsIgnoreCase(items[1])){
                        direction = Sort.Direction.DESC;
                    } else {
                        direction = Sort.Direction.ASC;
                    }
                } else {
                    direction = Sort.Direction.ASC;
                }
                return new Sort.Order(direction, property);
            }).collect(Collectors.toList()));
        }
        return sort;
    }

    public ResponseData getUserInfoById(String jwt) {
        Integer sub = jwtUtil.getSubject(jwt);
        ResponseData responseData = new ResponseData();
        try {
            GarbageUserEntity userEntity = garbageUserDao.findById(sub).get();
            List<GarbageImageEntity> imageEntityList = garbageImageDao.findAll(new Specification<GarbageImageEntity>() {
                @Nullable
                @Override
                public Predicate toPredicate(Root<GarbageImageEntity> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                    Predicate predicate = criteriaBuilder.equal(root.get("sourceName"), GarbageUserEntity.class);
                    Predicate predicateId = criteriaBuilder.equal(root.get("id"), userEntity.getId());
                    Predicate predicateType = criteriaBuilder.equal(root.get("type"), Constants.image.HEADER);
                    return criteriaBuilder.and(predicate, predicateId, predicateType);
                }
            });

            GarbageUserDto dto = new GarbageUserDto();
            dto.setGarbageUserEntity(userEntity);
            if (imageEntityList.size()> 0){
                dto.setHeaderImage(imageEntityList.get(0).getImagePath());
            }
            List<GarbageImageEntity> qRCodeList = garbageImageDao.findAll(new Specification<GarbageImageEntity>() {
                @Nullable
                @Override
                public Predicate toPredicate(Root<GarbageImageEntity> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                    Predicate predicate = criteriaBuilder.equal(root.get("sourceName"), GarbageUserEntity.class);
                    Predicate predicateId = criteriaBuilder.equal(root.get("id"), userEntity.getId());
                    Predicate predicateType = criteriaBuilder.equal(root.get("type"), Constants.image.QRCODE);
                    return criteriaBuilder.and(predicate, predicateId, predicateType);
                }
            });
            if (imageEntityList.size()> 0){
                dto.setqRCode(qRCodeList.get(0).getImagePath());
            }
            List<GarbageENoEntity> eNoEntityList = garbageENoDao.findByUserId(sub);
            List<String> eNos = eNoEntityList.stream().map(garbageENoEntity -> garbageENoEntity.geteNo()).collect(Collectors.toList());
            dto.seteNos(eNos);
            responseData.setStatus(Constants.responseStatus.Success.getStatus());
            responseData.setMsg("查询成功");
        } catch (Exception e) {
            e.printStackTrace();
            responseData.setStatus(Constants.responseStatus.Failure.getStatus());
            responseData.setMsg("查询失败");
        }
        return  responseData;
    }

    public ResponseData deleteUserById(String jwt, Integer status) {
        Integer sub = jwtUtil.getSubject(jwt);
        ResponseData responseData = new ResponseData();
        GarbageUserEntity userEntity = garbageUserDao.findById(sub).get();
        userEntity.setStatus(status);
        garbageUserDao.save(userEntity);
        responseData.setStatus(Constants.responseStatus.Success.getStatus());
        responseData.setMsg("删除成功");
        return responseData;
    }

    @Transactional
    public ResponseData updateUserInfo(GarbageUserEntity userEntity, Integer fileId) {
        ResponseData responseData = new ResponseData();
        try {
            garbageUserDao.save(userEntity);
            GarbageImageEntity garbageImageEntity = garbageImageDao.findById(fileId).get();
            if (garbageImageEntity.getBusId() == userEntity.getId()){
                //没有更改图片
            } else {
                //更改图片
                garbageImageDao.deleteBySourceNameAndBusIdAndType(GarbageUserEntity.class.getName(), userEntity.getId(), Constants.image.HEADER.name());
                garbageImageEntity.setBusId(userEntity.getId());
                garbageImageEntity.setSourceName(GarbageUserEntity.class.getName());
                garbageImageEntity.setType(Constants.image.HEADER.name());
                garbageImageDao.save(garbageImageEntity);
            }
            responseData.setStatus(Constants.responseStatus.Success.getStatus());
            responseData.setMsg("更新成功");
        } catch (Exception e) {
            e.printStackTrace();
            throw  e;
        }
        return responseData;
    }

    public ResponsePageData residentList(String name, String phone, String idCard, String eNo,
                                     Integer provinceId, Integer cityId, Integer countryId, Integer townId,
                                     Integer villageId, String roleCode, String jwt, Integer pageNo,
                                     Integer pageSize, String[] orderBys) {
        Pageable pageable = PageRequest.of(pageNo -1, pageSize, getCollectorSort(orderBys));
        Integer sub = jwtUtil.getSubject(jwt);
        GarbageUserEntity userEntity = garbageUserDao.findById(sub).get();
        List<GarbageRoleEntity> roleEntityList = garbageRoleDao.findByUserId(sub);
        List<String> roleCodeList = roleEntityList.stream().map(garbageRoleEntity -> garbageRoleEntity.getRoleCode()).collect(Collectors.toList());
        Page<GarbageUserEntity> page = garbageUserDao.findAll(new Specification<GarbageUserEntity>() {
            @Nullable
            @Override
            public Predicate toPredicate(Root<GarbageUserEntity> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {

                List<Predicate> predicateList = new ArrayList<>();
                //正常用户
                Predicate status = criteriaBuilder.equal(root.get("status"), 1);
                predicateList.add(status);
                if (roleCodeList.contains("COLLECTOR")){
                    //垃圾收集员
                    Predicate predicate = criteriaBuilder.equal(root.get("id"), sub);
                    predicateList.add(predicate);
                }
                if (roleCodeList.contains("VILLAGE_ADMIN")){
                    //村
                    Predicate predicate = criteriaBuilder.equal(root.get("villageId"), userEntity.getVillageId());
                    predicateList.add(predicate);

                }
                if (roleCodeList.contains("TOWN_ADMIN")){
                    //乡镇
                    Predicate predicate = criteriaBuilder.equal(root.get("townId"), userEntity.getTownId());
                    predicateList.add(predicate);
                    if (villageId !=null){
                        Predicate villageIdPredicate = criteriaBuilder.equal(root.get("villageId"), villageId);
                        predicateList.add(villageIdPredicate);
                    }
                }
                if (roleCodeList.contains("COUNTRY_ADMIN")){
                    // 县/区
                    Predicate predicate = criteriaBuilder.equal(root.get("districtId"), userEntity.getDistrictId());
                    predicateList.add(predicate);
                    if (townId !=null){
                        Predicate townIdPredicate = criteriaBuilder.equal(root.get("townId"), townId);
                        predicateList.add(townIdPredicate);
                    }
                    if (villageId !=null){
                        Predicate villageIdPredicate = criteriaBuilder.equal(root.get("villageId"), villageId);
                        predicateList.add(villageIdPredicate);
                    }
                }
                if (roleCodeList.contains("CITY_ADMIN")){
                    //市
                    Predicate predicate = criteriaBuilder.equal(root.get("cityId"), userEntity.getCityId());
                    predicateList.add(predicate);
                    if (countryId !=null){
                        Predicate countryIdPredicate = criteriaBuilder.equal(root.get("districtId"), countryId);
                        predicateList.add(countryIdPredicate);
                    }
                    if (townId !=null){
                        Predicate townIdPredicate = criteriaBuilder.equal(root.get("townId"), townId);
                        predicateList.add(townIdPredicate);
                    }
                    if (villageId !=null){
                        Predicate villageIdPredicate = criteriaBuilder.equal(root.get("villageId"), villageId);
                        predicateList.add(villageIdPredicate);
                    }
                }
                if (roleCodeList.contains("PROVINCE_ADMIN")){
                    //省
                    Predicate predicate = criteriaBuilder.equal(root.get("provinceId"), provinceId);
                    predicateList.add(predicate);
                    if (cityId !=null) {
                        Predicate cityIdPredicate = criteriaBuilder.equal(root.get("cityId"), cityId);
                        predicateList.add(cityIdPredicate);
                    }
                    if (countryId !=null){
                        Predicate countryIdPredicate = criteriaBuilder.equal(root.get("districtId"), countryId);
                        predicateList.add(countryIdPredicate);
                    }
                    if (townId !=null){
                        Predicate townIdPredicate = criteriaBuilder.equal(root.get("townId"), townId);
                        predicateList.add(townIdPredicate);
                    }
                    if (villageId !=null){
                        Predicate villageIdPredicate = criteriaBuilder.equal(root.get("villageId"), villageId);
                        predicateList.add(villageIdPredicate);
                    }
                }
                if (!StringUtils.isEmpty(name)) {
                    Predicate predicateName = criteriaBuilder.like(root.get("name"), "%" + name + "%");
                    predicateList.add(predicateName);
                }
                if (!StringUtils.isEmpty(phone)) {
                    Predicate predicateName = criteriaBuilder.like(root.get("phone"), "%" + phone + "%");
                    predicateList.add(predicateName);
                }
                if (!StringUtils.isEmpty(idCard)) {
                    Predicate predicateName = criteriaBuilder.like(root.get("idCard"), "%" + idCard + "%");
                    predicateList.add(predicateName);
                }
                if (!StringUtils.isEmpty(eNo)){
                    Join< GarbageUserEntity, GarbageENoEntity> eNoJoin = root.join("eNos", JoinType.INNER);
                    Predicate eNoPredicate = criteriaBuilder.equal(eNoJoin.get("eNo"), eNo);
                    predicateList.add(eNoPredicate);
                }
                Join<GarbageUserEntity, GarbageRoleEntity> roleEntityJoin = root.join("roles", JoinType.INNER);
                if (!StringUtils.isEmpty(roleCode) && roleCode.equals("0")){
                    //所有
                    List<String> roleList = new ArrayList<>();
                    roleList.add("RESIDENT");
                    roleList.add("COLLECTOR");
                    Predicate predicateIn = roleEntityJoin.get("roleCode").in(roleList);
                    predicateList.add(predicateIn);
                } else {
                    Predicate rolePredicate = criteriaBuilder.equal(roleEntityJoin.get("roleCode"), roleCode);
                    predicateList.add(rolePredicate);
                }
                return criteriaBuilder.and(predicateList.toArray(new Predicate[predicateList.size()]));
            }
        }, pageable);
        List<GarbageImageEntity> imageEntityList = garbageImageDao.findAll(new Specification<GarbageImageEntity>() {
            @Nullable
            @Override
            public Predicate toPredicate(Root<GarbageImageEntity> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                Predicate predicateHeader = criteriaBuilder.equal(root.get("type"), Constants.image.HEADER.name());
                Predicate predicateImage = criteriaBuilder.equal(root.get("type"), Constants.image.QRCODE.name());
                return criteriaBuilder.or(predicateHeader, predicateImage);
            }
        });
        Map<String, String> map = new HashMap<>();
        imageEntityList.forEach(garbageImageEntity -> {
            map.put(garbageImageEntity.getBusId() + "-" + garbageImageEntity.getType(), garbageImageEntity.getImagePath());
        });
        List<GarbageUserDto> dtos = new ArrayList<>();
        page.getContent().forEach(user -> {
            GarbageUserDto dto = new GarbageUserDto();
            dto.setGarbageUserEntity(user);
            dto.setHeaderImage(map.get(user.getId() + "-" + Constants.image.HEADER.name()));
            dto.setqRCode(map.get(user.getId() + "-" + Constants.image.QRCODE.name()));
            dto.seteNos(user.geteNos().stream().map(noEntity-> noEntity.geteNo()).collect(Collectors.toList()));
            dtos.add(dto);
        });
        ResponsePageData pageData = new ResponsePageData();
        pageData.setStatus(Constants.responseStatus.Success.getStatus());
        pageData.setMsg("查询成功");
        pageData.setFirstPage(page.isFirst());
        pageData.setLastPage(page.isLast());
        pageData.setPageNo(pageNo);
        pageData.setPageSize(pageSize);
        pageData.setCount(page.getTotalPages());
        pageData.setData(dtos);
        return pageData;
    }
}
