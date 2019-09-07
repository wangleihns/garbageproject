package com.jin.env.garbage.service.user;

import com.jin.env.garbage.controller.user.LoginApiController;
import com.jin.env.garbage.dao.garbage.GarbageCollectorDao;
import com.jin.env.garbage.dao.image.GarbageImageDao;
import com.jin.env.garbage.dao.position.GarbageCommunityDao;
import com.jin.env.garbage.dao.user.GarbageENoDao;
import com.jin.env.garbage.dao.user.GarbageResourceDao;
import com.jin.env.garbage.dao.user.GarbageRoleDao;
import com.jin.env.garbage.dao.user.GarbageUserDao;
import com.jin.env.garbage.dto.resource.UserResourceDto;
import com.jin.env.garbage.dto.user.*;
import com.jin.env.garbage.entity.garbage.GarbageCollectorEntity;
import com.jin.env.garbage.entity.image.GarbageImageEntity;
import com.jin.env.garbage.entity.position.GarbageCommunityEntity;
import com.jin.env.garbage.entity.user.GarbageENoEntity;
import com.jin.env.garbage.entity.user.GarbageResourceEntity;
import com.jin.env.garbage.entity.user.GarbageRoleEntity;
import com.jin.env.garbage.entity.user.GarbageUserEntity;
import com.jin.env.garbage.jwt.JwtUtil;
import com.jin.env.garbage.service.garbage.GarbageCollectorService;
import com.jin.env.garbage.utils.*;
import net.sf.json.JSONObject;
import org.apache.commons.io.FilenameUtils;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
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
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.criteria.*;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class GarbageUserService {
    private Logger logger = LoggerFactory.getLogger(LoginApiController.class);

    @Value(value = "${garbageTokenTime}")
    private Long garbageTokenTime;
    @Value(value = "${noGarbageTokenTime}")
    private Long noGarbageTokenTime ;

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

    @Autowired
    private GarbageCollectorDao garbageCollectorDao;

    @Autowired
    private GarbageCollectorService garbageCollectorService;

    @Autowired
    private GarbageCommunityDao garbageCommunityDao;

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
            Map<String, Object> token= new HashMap<>();
            //组资源
            List<GarbageResourceEntity> resourceEntityList = garbageResourceDao.findByResourceByUserId(userEntity.getId());
            //子资源
            List<UserResourceDto> subResourceList = garbageResourceDao.getUserSubResourceInfoList();
            Map<Integer, List<UserResourceDto>> collectMap = subResourceList.stream().collect(Collectors.groupingBy(UserResourceDto::getSuqId));
            List<UserResourceDto> userResourceDtos = new ArrayList<>();
            resourceEntityList.stream().forEach(resourceEntity -> {
                UserResourceDto dto = new UserResourceDto();
                dto.setDtos(collectMap.get(resourceEntity.getId()));
                dto.setIcon(resourceEntity.getIcon());
                dto.setName(resourceEntity.getName());
                dto.setUrl(resourceEntity.getUrl());
                userResourceDtos.add(dto);
            });
            List<GarbageRoleEntity> roleEntityList = userEntity.getRoles().stream().collect(Collectors.toList());
            List<Integer> roleIds = roleEntityList.stream().map(roleEntity->roleEntity.getId()).collect(Collectors.toList());
            List<GarbageCommunityEntity> communityEntities = new ArrayList<>();
            if (roleIds.size()> 0){
              communityEntities = garbageCommunityDao.findByRoleIds(roleIds);
            }
            List<NameValuePair> communityList = new ArrayList<>();
            communityEntities.stream().forEach(n->{
                NameValuePair nameValuePair = new BasicNameValuePair(n.getId().toString(), n.getCommunityName());
                communityList.add(nameValuePair);
            });
            token.put("communityEntities",communityList);
            String accessToken =  "";

            if (Constants.loginType.GarbageCar.getType().equals(from)){
                //垃圾车 token 有效时长
                accessToken = jwtUtil.generateJwtToken(userEntity.getId().toString(),"garbage", garbageTokenTime);
                token.put("userEntity", userEntity);
            } else if (Constants.loginType.NoGarbageCar.getType().equals(from)){
                accessToken = jwtUtil.generateJwtToken(userEntity.getId().toString(),"garbage", noGarbageTokenTime);
                token.put("userEntity", userEntity);
            } else {
                //其他客户端使用默认有效时长
                accessToken = jwtUtil.generateJwtToken(userEntity.getId().toString(),"garbage", null);
                token.put("userEntity", userEntity);
                token.put("resources", userResourceDtos);
            }
            String refreshToken = jwtUtil.getRefresh(accessToken);
            redisTemplate.opsForValue().set("accessToken:"+userEntity.getPhone(), accessToken, 2*60*60*1000, TimeUnit.MILLISECONDS); //两小时有效期
            String a =  redisTemplate.opsForValue().get("accessToken:" + username);
            redisTemplate.opsForValue().set("refreshToken:" +userEntity.getPhone(), refreshToken, 7*24*60*60*1000, TimeUnit.MILLISECONDS);
            token.put("accessToken", accessToken);
            token.put("refreshToken", refreshToken);
            token.put("resources", userResourceDtos);
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
        Long provinceId = object.getLong("provinceId");
        String provinceName = object.getString("provinceName");
        Long cityId = object.getLong("cityId");
        String cityName = object.getString("cityName");
        Long districtId = object.getLong("districtId");
        String districtName = object.getString("districtName");
        Long townId = object.getLong("townId");
        String townName = object.getString("townName");
        Long villageId = object.getLong("villageId");
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
        userEntity.setCountryId(districtId);
        userEntity.setCountryName(districtName);
        userEntity.setTownId(townId);
        userEntity.setTownName(townName);
        userEntity.setVillageId(villageId);
        userEntity.setVillageName(villageName);
        userEntity.setAddress(address);
        userEntity.setFromType(Constants.garbageFromType.TOWN.getType());
        Calendar calendar  = Calendar.getInstance();
        userEntity.setDay(calendar.get(Calendar.DAY_OF_MONTH));
        userEntity.setMonth(calendar.get(Calendar.MONTH));
        userEntity.setYear(calendar.get(Calendar.YEAR));
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
        List<Integer> communityIds = garbageCollectorService.getCommunityResource(roleEntityList);
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
                //查看小区评分员
                if (communityIds.size() > 0){
                    Predicate communityPredicate = root.get("communityId").in(communityIds);
                    predicateList.add(communityPredicate);
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
        List<Integer> roleIds = roleEntityList.stream().map(role-> role.getId()).collect(Collectors.toList());
        List<String> roleCodeList = roleEntityList.stream().map(garbageRoleEntity -> garbageRoleEntity.getRoleCode()).collect(Collectors.toList());

        List<Integer> communityIds = garbageCollectorService.getCommunityResource(roleEntityList);

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
                    Predicate predicate = criteriaBuilder.equal(root.get("districtId"), userEntity.getCountryId());
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
                //查看小区居民
                if (communityIds.size() > 0){
                    Predicate communityPredicate = root.get("communityId").in(communityIds);
                    predicateList.add(communityPredicate);
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

    public ResponseData getSummaryInfoInManagerCenter(String jwt) {
        Integer sub = jwtUtil.getSubject(jwt);
        GarbageUserEntity userEntity = garbageUserDao.findById(sub).get();
        Integer fromType = userEntity.getFromType();
        List<String> roleCodes = userEntity.getRoles().stream().map(garbageRoleEntity -> garbageRoleEntity.getRoleCode()).collect(Collectors.toList());
        //会员的注册量
        Long count = garbageUserDao.count(new Specification<GarbageUserEntity>() {
            @Override
            public Predicate toPredicate(Root<GarbageUserEntity> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicateList = new ArrayList<>();
                if(fromType == 0){
                    if (roleCodes.contains("VILLAGE_ADMIN")|| roleCodes.contains("RESIDENT") || roleCodes.contains("COLLECTOR")){
                        //查看本村的注册量
                        Predicate predicate = criteriaBuilder.equal(root.get("villageId"), userEntity.getVillageId());
                        predicateList.add(predicate);
                    }
                    if (roleCodes.contains("TOWN_ADMIN")){
                        Predicate predicate = criteriaBuilder.equal(root.get("townId"), userEntity.getVillageId());
                        predicateList.add(predicate);
                    }
                    if (roleCodes.contains("COUNTRY_ADMIN")){
                        Predicate predicate = criteriaBuilder.equal(root.get("countryId"), userEntity.getCountryId());
                        predicateList.add(predicate);
                    }
                    if (roleCodes.contains("CITY_ADMIN")){
                        Predicate predicate = criteriaBuilder.equal(root.get("cityId"), userEntity.getCityId());
                        predicateList.add(predicate);
                    }
                    if (roleCodes.contains("PROVINCE_ADMIN")){
                        Predicate predicate = criteriaBuilder.equal(root.get("provinceId"), userEntity.getProvinceId());
                        predicateList.add(predicate);
                    }
                } else {
                    if (roleCodes.stream().filter(roleCode-> roleCode.endsWith("COMMUNITY_ADMIN")).count() > 0 || roleCodes.stream().filter(roleCode-> roleCode.endsWith("COMMUNITY_REMARK")).count() > 0){
                        //小区管理员
                        Predicate predicate = criteriaBuilder.equal(root.get("communityId"), userEntity.getCommunityId());
                        predicateList.add(predicate);
                    }
                }
                if (roleCodes.contains("SYSTEM_ADMIN")){

                }
                return criteriaBuilder.and(predicateList.toArray(new Predicate[predicateList.size()]));
            }
        });
        String todayString = DateFormatUtil.formatDate(new Date(), "yyyy-MM-dd");
        Long start = DateFormatUtil.getFirstTimeOfDay(todayString).getTime();
        Long end = DateFormatUtil.getLastTimeOfDay(todayString).getTime();
        Long collectCount = garbageCollectorDao.count(new Specification<GarbageCollectorEntity>() {
            @Override
            public Predicate toPredicate(Root<GarbageCollectorEntity> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicateList = new ArrayList<>();
                Predicate startPredcate = criteriaBuilder.between(root.get("collectDate"), start, end);
                predicateList.add(startPredcate);
                if (roleCodes.contains("VILLAGE_ADMIN")|| roleCodes.contains("RESIDENT") || roleCodes.contains("COLLECTOR")){
                    //查看本村的注册量
                    Predicate predicate = criteriaBuilder.equal(root.get("villageId"), userEntity.getVillageId());
                    predicateList.add(predicate);
                }
                if (roleCodes.contains("TOWN_ADMIN")){
                    Predicate predicate = criteriaBuilder.equal(root.get("townId"), userEntity.getVillageId());
                    predicateList.add(predicate);
                }
                if (roleCodes.contains("COUNTRY_ADMIN")){
                    Predicate predicate = criteriaBuilder.equal(root.get("countryId"), userEntity.getCountryId());
                    predicateList.add(predicate);
                }
                if (roleCodes.contains("CITY_ADMIN")){
                    Predicate predicate = criteriaBuilder.equal(root.get("cityId"), userEntity.getCityId());
                    predicateList.add(predicate);
                }
                if (roleCodes.contains("PROVINCE_ADMIN")){
                    Predicate predicate = criteriaBuilder.equal(root.get("provinceId"), userEntity.getProvinceId());
                    predicateList.add(predicate);
                }
                if (roleCodes.stream().filter(roleCode-> roleCode.endsWith("COMMUNITY_ADMIN")).count() > 0 || roleCodes.stream().filter(roleCode-> roleCode.endsWith("COMMUNITY_REMARK")).count() > 0){
                    //小区管理员
                    Predicate predicate = criteriaBuilder.equal(root.get("communityId"), userEntity.getCommunityId());
                    predicateList.add(predicate);
                }
                if (roleCodes.contains("SYSTEM_ADMIN")){

                }
                return criteriaBuilder.and(predicateList.toArray(new Predicate[predicateList.size()]));
            }
        });
        SummaryCountInfo countInfo = new SummaryCountInfo();
        countInfo.setCollectCount(collectCount);
        countInfo.setUserCount(count);
        countInfo.setPointRechargeCount(0L);
        countInfo.setReceivedCount(0L);
        ResponseData responseData = new ResponseData();
        responseData.setData(countInfo);
        responseData.setStatus(Constants.responseStatus.Success.getStatus());
        responseData.setMsg("统计信息获取成功");
        return responseData;
    }

    public ResponseData getRegisterUserCountInMonth() {
        Calendar calendar = Calendar.getInstance();
        Integer year = calendar.get(Calendar.YEAR);
        Integer month = calendar.get(Calendar.MONTH);
        List<UserCountInMonth> userCountInMonths = null;
        if (month < 5){
            userCountInMonths = garbageUserDao.countUserInMonthBetween(year, 0, month);
        } else {
            userCountInMonths = garbageUserDao.countUserInMonthBetween(year, month - 5, month);
        }
        userCountInMonths.forEach(userCountInMonth -> {
            userCountInMonth.setTime(year + "-" + (userCountInMonth.getMonth() + 1));
        });
        ResponseData responseData = new ResponseData();
        responseData.setData(userCountInMonths);
        responseData.setStatus(Constants.responseStatus.Success.getStatus());
        responseData.setMsg("统计信息获取成功");
        return responseData;
    }

    public ResponseData getSummaryInfoInBigDataCenter() {
        Long userCount = garbageUserDao.count();
        String todayString = DateFormatUtil.formatDate(new Date(), "yyyy-MM-dd");
        Long start = DateFormatUtil.getFirstTimeOfDay(todayString).getTime();
        Long end = DateFormatUtil.getLastTimeOfDay(todayString).getTime();
        Long participationCount = garbageCollectorDao.count(new Specification<GarbageCollectorEntity>() {
            @Nullable
            @Override
            public Predicate toPredicate(Root<GarbageCollectorEntity> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                Predicate collectDate = criteriaBuilder.between(root.get("collectDate"), start, end);

                return collectDate;
            }
        });
        Long qualityCount = garbageCollectorDao.countQualityToday(start, end);
        String qualityRate = "0%";
        DecimalFormat df = new DecimalFormat("0.00%");
        if (participationCount > 0){
            qualityRate = df.format(qualityCount.doubleValue()/participationCount);
        } else {
            qualityRate = "0%";
        }
        String participationRate = df.format(participationCount.doubleValue()/userCount);
        Double garbageWeight = garbageCollectorDao.countGarbageWeight(start, end);
        Map<String, Object> map = new HashMap<>();
        map.put("userCount", userCount);
        map.put("permanentCount", userCount);
        map.put("participationCount", participationCount);
        map.put("qualityRate", qualityRate);
        map.put("participationRate", participationRate);
        map.put("garbageWeight", garbageWeight);
        ResponseData responseData = new ResponseData();
        responseData.setData(map);
        responseData.setStatus(Constants.responseStatus.Success.getStatus());
        responseData.setMsg("查询成功");
        return  responseData;
    }

    public ResponseData insertUserInfoBatch(MultipartFile multipartFile) {
        InputStream inputStream = null;

        try {
            inputStream = multipartFile.getInputStream();
            String fileName = multipartFile.getOriginalFilename();
            String suffix  = FilenameUtils.getExtension(fileName);
            List<NameValuePair> list = ReadExcelUtil.readExcel(inputStream, suffix);
            list.stream().forEach(n->{
                logger.info("--- > " + n.getValue());
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        ResponseData responseData = new ResponseData();
        return responseData;
    }

    public ResponseData refreshAccessToken(String refreshToken, String jwt) {
        ResponseData responseData = new ResponseData();
        Integer sub = jwtUtil.getSubject(jwt);
        GarbageUserEntity userEntity = garbageUserDao.findById(sub).get();
        responseData.setStatus(Constants.loginStatus.LoginSuccess.getStatus());
        String accessToken = null;
        String refreshTokenNew = null;
        try {
            String a =  redisTemplate.opsForValue().get("accessToken:" + userEntity.getPhone());
            if (!a.equals(refreshToken)){
                throw new RuntimeException("refreshToken 不合法");
            }
            accessToken = jwtUtil.generateJwtToken(sub.toString(),"garbage", null);
            String phone = userEntity.getPhone();
            redisTemplate.opsForValue().set("accessToken:"+phone, accessToken, 2*60*60*1000, TimeUnit.MILLISECONDS); //两小时有效期
            refreshTokenNew = jwtUtil.getRefresh(accessToken);
            redisTemplate.opsForValue().set("refreshToken:" +phone, refreshTokenNew, 7*24*60*60*1000, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            e.printStackTrace();
            responseData.setStatus(500);
            responseData.setMsg("token 不合法");
            return responseData;
        }
        Map<String, String> token= new HashMap<>();
        token.put("accessToken",accessToken);
        token.put("refreshToken",refreshTokenNew);
        responseData.setMsg("refresh success");
        responseData.setData(token);
        return responseData;
    }


    public ResponseData getUserInfoByEno(String eNo) {
        GarbageENoEntity eNoEntity = garbageENoDao.findByENo(eNo);
        if (eNoEntity == null){
            throw  new RuntimeException("此电子卡未录入系统中，请联系管理员");
        }
        UserDto userDto = garbageUserDao.selectUserInfoByUserId(eNoEntity.getUserId());
        userDto.seteNo(eNo);
        ResponseData responseData = new ResponseData();
        responseData.setStatus(Constants.responseStatus.Success.getStatus());
        responseData.setMsg("用户信息获取成功");
        responseData.setData(userDto);
        return responseData;
    }
}
