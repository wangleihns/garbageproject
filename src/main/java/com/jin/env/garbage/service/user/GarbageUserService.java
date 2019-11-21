package com.jin.env.garbage.service.user;

import com.jin.env.garbage.controller.user.LoginApiController;
import com.jin.env.garbage.dao.garbage.GarbageCollectorDao;
import com.jin.env.garbage.dao.image.GarbageImageDao;
import com.jin.env.garbage.dao.point.GarbageUserPointDao;
import com.jin.env.garbage.dao.position.*;
import com.jin.env.garbage.dao.user.GarbageENoDao;
import com.jin.env.garbage.dao.user.GarbageResourceDao;
import com.jin.env.garbage.dao.user.GarbageRoleDao;
import com.jin.env.garbage.dao.user.GarbageUserDao;
import com.jin.env.garbage.dao.village.GarbageVillageInfoDao;
import com.jin.env.garbage.dto.garbage.UserCollectCountDto;
import com.jin.env.garbage.dto.garbage.UserCollectRightAndWeightDto;
import com.jin.env.garbage.dto.garbage.UserCollectRightAndWeightDto2;
import com.jin.env.garbage.dto.resource.ResourceListChildrenDto;
import com.jin.env.garbage.dto.resource.ResourceListDto;
import com.jin.env.garbage.dto.resource.UserResourceDto;
import com.jin.env.garbage.dto.user.*;
import com.jin.env.garbage.entity.garbage.GarbageCollectorEntity;
import com.jin.env.garbage.entity.image.GarbageImageEntity;
import com.jin.env.garbage.entity.point.GarbageUserPointEntity;
import com.jin.env.garbage.entity.position.*;
import com.jin.env.garbage.entity.user.GarbageENoEntity;
import com.jin.env.garbage.entity.user.GarbageResourceEntity;
import com.jin.env.garbage.entity.user.GarbageRoleEntity;
import com.jin.env.garbage.entity.user.GarbageUserEntity;
import com.jin.env.garbage.entity.village.GarbageVillageInfoEntity;
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

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.criteria.*;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@SuppressWarnings(value = "ALL")
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

    @Autowired
    private JPositionVillageDao jPositionVillageDao;

    @Autowired
    private JPositionTownDao jPositionTownDao;

    @Autowired
    private JPositionCountyDao jPositionCountyDao;

    @Autowired
    private JPositionCityDao jPositionCityDao;

    @Autowired
    private JPositionProvinceDao jPositionProvinceDao;

    @Autowired
    private GarbageUserPointDao garbageUserPointDao;

    @Autowired
    private GarbageVillageInfoDao garbageVillageInfoDao;

    @Autowired
   private EntityManager entityManager;

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
//            List<UserResourceDto> subResourceList = garbageResourceDao.getUserSubResourceInfoList();
//            Map<Integer, List<UserResourceDto>> collectMap = subResourceList.stream().collect(Collectors.groupingBy(UserResourceDto::getSuqId));
//            List<UserResourceDto> userResourceDtos = new ArrayList<>();
//            resourceEntityList.stream().forEach(resourceEntity -> {
//                UserResourceDto dto = new UserResourceDto();
//                dto.setDtos(collectMap.get(resourceEntity.getId()));
//                dto.setIcon(resourceEntity.getIcon());
//                dto.setName(resourceEntity.getName());
//                dto.setUrl(resourceEntity.getUrl());
//                userResourceDtos.add(dto);
//            });
            List<ResourceListDto> resourceListDtos = new ArrayList<>();
            List<GarbageResourceEntity> garbageResourceEntityList = garbageResourceDao.findBySupIdNot(userEntity.getId(),0);
            Map<Integer, List<ResourceListChildrenDto>> subResourceMap = new HashMap<>();
            garbageResourceEntityList.stream().forEach(res ->{
                if (subResourceMap.containsKey(res.getSupId())){
                    List<ResourceListChildrenDto> childrenDtos = subResourceMap.get(res.getSupId());
                    ResourceListChildrenDto resourceListChildrenDto = new ResourceListChildrenDto();
                    resourceListChildrenDto.setId(res.getId().toString());
                    resourceListChildrenDto.setParentId(res.getSupId().toString());
                    resourceListChildrenDto.setName(res.getName());
                    resourceListChildrenDto.setCode(res.getCode());
                    resourceListChildrenDto.setIcon(res.getIcon());
                    resourceListChildrenDto.setPath(res.getPath());
                    resourceListChildrenDto.setEnabled(true);
                    resourceListChildrenDto.setNoDropdown(false);
                    childrenDtos.add(resourceListChildrenDto);
                    subResourceMap.put(res.getSupId(), childrenDtos);
                } else {
                    List<ResourceListChildrenDto> childrenDtos = new ArrayList<>();
                    ResourceListChildrenDto resourceListChildrenDto = new ResourceListChildrenDto();
                    resourceListChildrenDto.setId(res.getId().toString());
                    resourceListChildrenDto.setParentId(res.getSupId().toString());
                    resourceListChildrenDto.setName(res.getName());
                    resourceListChildrenDto.setCode(res.getCode());
                    resourceListChildrenDto.setIcon(res.getIcon());
                    resourceListChildrenDto.setPath(res.getPath());
                    resourceListChildrenDto.setEnabled(true);
                    resourceListChildrenDto.setNoDropdown(false);
                    childrenDtos.add(resourceListChildrenDto);
                    subResourceMap.put(res.getSupId(), childrenDtos);
                }
            });
            resourceEntityList.stream().forEach(garbageResourceEntity -> {
                ResourceListDto dto = new ResourceListDto();
                dto.setId(garbageResourceEntity.getId().toString());
                dto.setCode(garbageResourceEntity.getCode());
                dto.setIcon(garbageResourceEntity.getIcon());
                dto.setName(garbageResourceEntity.getName());
                dto.setPath(garbageResourceEntity.getPath());
                dto.setEnabled(true);
                dto.setNoDropdown(true);
                List<ResourceListChildrenDto> children = subResourceMap.get(garbageResourceEntity.getId()) == null? new ArrayList<>():subResourceMap.get(garbageResourceEntity.getId());
                dto.setChildren(children);
                resourceListDtos.add(dto);
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
//                token.put("resources", resourceListDtos);
            }
            String refreshToken = jwtUtil.getRefresh(accessToken);
            redisTemplate.opsForValue().set("accessToken:"+ userEntity.getId(), accessToken, 2*60*60*1000, TimeUnit.MILLISECONDS); //两小时有效期
            String a =  redisTemplate.opsForValue().get("accessToken:" + username);
            redisTemplate.opsForValue().set("refreshToken:" + userEntity.getId(), refreshToken, 7*24*60*60*1000, TimeUnit.MILLISECONDS);
            token.put("accessToken", accessToken);
            token.put("refreshToken", refreshToken);
            token.put("resources", resourceListDtos);
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
//        jsonParam = "{\n" +
//                 "\t\n" +
//                 "\tloginName:'15158099385'\n" +
//                 "\tpassword:'123456',\n" +
//                 "\trepeatPassword:'123456',\n" +
//                 "\tphone:'15158099385',\n" +
//                 "\tname:'zhangsan',\n" +
//                 "\tcompany:'ddd公司'\n" +
//                 "\teNo:'21212,23456',\n" +
//                 "\tidCard:'1234567199900890X',\n" +
//                 "\tsex:1,\n" +
//                 "\tcleaner:false,\n" +
//                 "\tprovinceId:1,\n" +
//                 "\tprovinceName:'ddd',\n" +
//                 "\tcityId:2,\n" +
//                 "\tcityName:'杭州',\n" +
//                 "\tdistrictId:3,\n" +
//                 "\tdistrictName:'滨江',\n" +
//                 "\ttownId:4;\n" +
//                 "\ttownName:'ddddddd',\n" +
//                 "\tvillageId:5,\n" +
//                 "\tvillageName:'gg',\n" +
//                 "\taddress:'ggggg'\n" +
//                 "\tisCollector:true\n" +
//                 "\tfromType:1\n" +
//                "\tfileId:1\n" +
//                 "}";
        GarbageUserEntity userEntity = new GarbageUserEntity();
        JSONObject object = JSONObject.fromObject(jsonParam);
        String password = object.getString("password");
        String repeatPassword = object.getString("repeatPassword");
        String name = object.getString("name");
        String company = object.getString("company");
        String eNo = object.getString("eNo");
        String idCard = object.getString("idCard");
        Integer sex = object.getInt("sex");
        Boolean cleaner = object.getInt("cleaner") == 1? true:false;
        Long provinceId = object.getLong("provinceId");
        Long cityId = object.getLong("cityId");
        Long countryId = object.getLong("countryId");
        Long townId = object.getLong("townId");
        Long villageId = object.getLong("villageId");
        String address = object.getString("address");
        Boolean isCollector = object.getInt("isCollector") == 1?true:false;
        Integer fileId = !StringUtils.hasText(object.getString("fileId"))? null : object.getInt("fileId");
        String loginName = object.getString("loginName");
        Integer fromType = object.getInt("fromType");  //用户性质
        String phone = object.getString("phone");
        Assert.state(repeatPassword.equals(password), "密码与重复密码不一致");
        Assert.state(isCollector != null, "是否是收集员必传" );
        Assert.hasText(loginName, "用户名必传");
        Assert.hasText(password, "密码必传");
        Assert.hasText(repeatPassword, "重复密码必传");
        Assert.hasText(name, "姓名必传");
        Assert.hasText(eNo, "电子卡Id必传");
        Assert.hasText(phone, "手机号必填");
        Assert.state(sex !=null, "性别必传");
        Assert.state(provinceId != null, "用户所在省必传");
        Assert.state(cityId != null, "用户所在市必传");
        Assert.state(countryId != null, "用户所在区县必传");
        Assert.state(townId != null, "用户所在乡镇必传");
        Assert.state(fromType!=null, "用户性质必传");
        if (fromType == 1){
            Assert.state(!StringUtils.isEmpty(object.get("communityId")), "用户所在小区必传");
            Integer communityId= object.getInt("communityId");
            GarbageCommunityEntity communityEntity = garbageCommunityDao.findById(communityId).get();
            if (communityEntity == null){
                throw new RuntimeException("请将该小区加入系统中，生成系统数据");
            }
            userEntity.setCommunityId(communityId.longValue());
            userEntity.setCommunityName(communityEntity.getCommunityName());
            userEntity.setFromType(Constants.garbageFromType.COMMUNITY.getType());
        } else {
            Assert.state(villageId != null, "用户所在村必传");
            userEntity.setFromType(Constants.garbageFromType.TOWN.getType());
            GarbageVillageInfoEntity villageInfoEntity = garbageVillageInfoDao.findByVillageId(villageId);
            if (villageInfoEntity == null){
                throw new RuntimeException("请将该农村信息加入系统，生成系统数据");
            }
        }

        JPositionProvinceEntity provinceEntity = jPositionProvinceDao.findByProvinceId(provinceId.intValue());
        String provinceName = provinceEntity.getProvinceName();
        JPositionCityEntity cityEntity = jPositionCityDao.findByCityId(cityId);
        String cityName = cityEntity.getCityName();
        JPositionCountyEntity countyEntity = jPositionCountyDao.findByCountyId(countryId);
        String countryName = countyEntity.getCountyName();
        JPositionTownEntity townEntity = jPositionTownDao.findByTownId(townId);
        String townName = townEntity.getTownName();
        JPositionVillageEntity villageEntity = jPositionVillageDao.findByVillageId(villageId);
        String villageName = villageEntity.getVillageName();

        GarbageUserEntity u = garbageUserDao.findByPhone(phone);
        if (u != null){
            throw new RuntimeException("手机号重复");
        }
        List<GarbageENoEntity> eNoEs = garbageENoDao.findByENoIn(eNo.split(","));
        if (eNoEs.size() > 0){
            throw new RuntimeException("电子卡id重复， 请核查");
        }

        userEntity.setLoginName(loginName);
        userEntity.setPhone(phone);
        userEntity.setPassword(CommonUtil.md5(password));
        userEntity.setName(name);
        userEntity.setCompany(company);
        userEntity.setCredentialsNonExpired(true);
        userEntity.setAccountNonExpired(true);
        userEntity.setAccountNonLocked(true);
        userEntity.setEnabled(true);
        userEntity.setStatus(1);
        userEntity.setUserType("1"); //手动注册
        if (!StringUtils.isEmpty(idCard)){
            userEntity.setIdCard(idCard);
        }
        userEntity.setSex(sex);
        userEntity.setCleaner(cleaner);
        userEntity.setProvinceId(provinceId);
        userEntity.setProvinceName(provinceName);
        userEntity.setCityId(cityId);
        userEntity.setCityName(cityName);
        userEntity.setCountryId(countryId);
        userEntity.setCountryName(countryName);
        userEntity.setTownId(townId);
        userEntity.setTownName(townName);
        userEntity.setVillageId(villageId);
        userEntity.setVillageName(villageName);
        userEntity.setAddress(address);
        userEntity.setDangYuan(false);
        userEntity.setCunMinDaiBiao(false);
        userEntity.setCunZuLeader(false);
        userEntity.setStreetCommentDaiBiao(false);
        userEntity.setLiangDaiBiaoYiWeiYuan(false);
        userEntity.setCunLeader(false);
        userEntity.setWomenExeLeader(false);
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
        if(fileId != null){
            GarbageImageEntity garbageImageEntity = garbageImageDao.findById(fileId).get();
            garbageImageEntity.setSourceName(GarbageUserEntity.class.getName());
            garbageImageEntity.setBusId(userEntity.getId());
            garbageImageEntity.setType(Constants.image.HEADER.name());
            garbageImageEntity.setAttribute(0);
            garbageImageDao.save(garbageImageEntity);
        }
        //保存eno
        Integer userId = userEntity.getId();
        String[] eNoElements = eNo.split(",");
        List<GarbageENoEntity> eNoEntityList = new ArrayList<>();
        Arrays.stream(eNoElements).forEach(e->{
            GarbageENoEntity garbageENoEntity = new GarbageENoEntity();
            garbageENoEntity.setUserId(userId);
            garbageENoEntity.seteNo(e);
            garbageENoEntity.setStatus(1);
            eNoEntityList.add(garbageENoEntity);
        });
        garbageENoDao.saveAll(eNoEntityList);

        //生成二维码
        QRcodeDto dto = new QRcodeDto();
        dto.setId(userEntity.getId());
        dto.seteNo(eNo);
        dto.setName(name);
        dto.setProvinceName(provinceName);
        dto.setCityName(cityName);
        dto.setCountryName(countryName);
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

    public ResponseData collectorList(String type, String keyWord, Long cityId, Long countryId, Long townId, Long villageId,
                                         Integer communityId, String jwt, Integer pageNo, Integer pageSize, String[] orderBys) {
        ResponsePageData responsePageData = new ResponsePageData();
        Pageable pageable = PageRequest.of(pageNo -1, pageSize, getCollectorSort(orderBys));
        Integer sub = jwtUtil.getSubject(jwt);
        GarbageUserEntity userEntity = garbageUserDao.findById(sub).get();
        Integer fromType = userEntity.getFromType();//农村 小区
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
                    if (villageId !=null){
                        Predicate villageIdPredicate = criteriaBuilder.equal(root.get("villageId"), villageId);
                        predicateList.add(villageIdPredicate);
                    } else {
                        //乡镇
                        Predicate predicate = criteriaBuilder.equal(root.get("townId"), userEntity.getTownId());
                        predicateList.add(predicate);
                    }
                }
                if (roleCodeList.contains("COUNTRY_ADMIN")){
                    // 县/区

                    if (townId !=null){
                        Predicate townIdPredicate = criteriaBuilder.equal(root.get("townId"), townId);
                        predicateList.add(townIdPredicate);
                    } else if (townId !=null && villageId !=null){
                        Predicate villageIdPredicate = criteriaBuilder.equal(root.get("villageId"), villageId);
                        predicateList.add(villageIdPredicate);
                    } else {
                        Predicate predicate = criteriaBuilder.equal(root.get("districtId"), userEntity.getCountryId());
                        predicateList.add(predicate);
                    }
                }
                if (roleCodeList.contains("CITY_ADMIN")){
                    //市

                    if (countryId !=null){
                        Predicate countryIdPredicate = criteriaBuilder.equal(root.get("districtId"), countryId);
                        predicateList.add(countryIdPredicate);
                    } else if (countryId !=null && townId !=null){
                        Predicate townIdPredicate = criteriaBuilder.equal(root.get("townId"), townId);
                        predicateList.add(townIdPredicate);
                    } else if (countryId !=null && townId !=null && villageId !=null){
                        Predicate villageIdPredicate = criteriaBuilder.equal(root.get("villageId"), villageId);
                        predicateList.add(villageIdPredicate);
                    } else {
                        Predicate predicate = criteriaBuilder.equal(root.get("cityId"), userEntity.getCityId());
                        predicateList.add(predicate);
                    }
                }
                if (roleCodeList.contains("PROVINCE_ADMIN")){
                    //省
                    if (cityId !=null) {
                        Predicate cityIdPredicate = criteriaBuilder.equal(root.get("cityId"), cityId);
                        predicateList.add(cityIdPredicate);
                    } else if (cityId !=null && countryId !=null){
                        Predicate countryIdPredicate = criteriaBuilder.equal(root.get("districtId"), countryId);
                        predicateList.add(countryIdPredicate);
                    } else if (cityId !=null && countryId !=null && townId !=null){
                        Predicate townIdPredicate = criteriaBuilder.equal(root.get("townId"), townId);
                        predicateList.add(townIdPredicate);
                    } else if(cityId !=null && countryId !=null && townId !=null &&  villageId !=null){
                        Predicate villageIdPredicate = criteriaBuilder.equal(root.get("villageId"), villageId);
                        predicateList.add(villageIdPredicate);
                    } else {
                        Predicate predicate = criteriaBuilder.equal(root.get("provinceId"), userEntity.getProvinceId());
                        predicateList.add(predicate);
                    }
                }
                if (!StringUtils.isEmpty(type)){
                    if ("name".equals(type) && !StringUtils.isEmpty(keyWord)){
                        Predicate predicateName = criteriaBuilder.like(root.get("name"), "%" + keyWord + "%");
                        predicateList.add(predicateName);
                    }
                    if ("phone".equals(type) && !StringUtils.isEmpty(keyWord)){
                        Predicate predicateName = criteriaBuilder.like(root.get("phone"), "%" + keyWord + "%");
                        predicateList.add(predicateName);
                    }
                    if ("eNo".equals(type) && !StringUtils.isEmpty(keyWord)){
                        List<Predicate> selectList = new ArrayList<>();
                        Subquery subquery = criteriaQuery.subquery(GarbageENoEntity.class);
                        Root<GarbageCollectorEntity> subRoot = subquery.from(GarbageENoEntity.class);
                        subquery.select(subRoot.get("userId"));
                        Predicate equal = criteriaBuilder.equal(root.get("id"), subRoot.get("userId"));
                        selectList.add(equal);
                        Predicate predicateENo = criteriaBuilder.like(subRoot.get("eNo"), "%" + keyWord + "%");
                        selectList.add(predicateENo);
                        Predicate exists =criteriaBuilder.exists(subquery.where(selectList.toArray(new Predicate[selectList.size()])));
                        predicateList.add(exists);
                    }
                    if ("idCard".equals(type) && !StringUtils.isEmpty(keyWord)){
                        Predicate predicateName = criteriaBuilder.like(root.get("idCard"), "%" + keyWord + "%");
                        predicateList.add(predicateName);
                    }
                }
                //查看小区评分员
                if (fromType == 1){
                    if (communityId != null){
                        Predicate predicate = criteriaBuilder.equal(root.get("communityId"), communityId);
                        predicateList.add(predicate);
                    } else {
                        if (communityIds.size() > 0){
                            Predicate communityPredicate = root.get("communityId").in(communityIds);
                            predicateList.add(communityPredicate);
                        }
                    }
                }
                Join<GarbageUserEntity, GarbageRoleEntity> roleEntityJoin = root.join("roles", JoinType.INNER);
                List<String> roleList = new ArrayList<>();
                roleList.add("COLLECTOR");
                Predicate predicateIn = roleEntityJoin.get("roleCode").in(roleList);
                predicateList.add(predicateIn);
                return criteriaBuilder.and(predicateList.toArray(new Predicate[predicateList.size()]));
            }
        }, pageable);
        List<GarbageImageEntity> imageEntityList = garbageImageDao.findBySourceNameAndType(GarbageUserEntity.class.getName(), Constants.image.HEADER.name());
        Map<Integer, String> headerMap = imageEntityList.stream().collect(Collectors.toMap(GarbageImageEntity::getBusId, GarbageImageEntity::getImagePath));
        List<CollectorInfoDto> dtos = new ArrayList<>();
        page.getContent().forEach(n ->{
            CollectorInfoDto dto = new CollectorInfoDto();
            dto.setUserId(n.getId());
            dto.setName(n.getName());
            dto.setLoginName(n.getLoginName());
            dto.setCollectDate(DateFormatUtil.formatDate(new Date(n.getCreateTime()), "yyyy-MM-dd"));
            dto.setPhone(n.getPhone());
            String placeName = n.getProvinceName() + n.getCityName() + n.getCountryName();
            String address = n.getTownName() == null ?"": n.getTownName() +
                    n.getVillageName() == null ?"":n.getVillageName() +
                    n.getAddress() == null ?"":n.getAddress();
            dto.setPlaceName(placeName);
            dto.setAddress(address);
            dto.setSex(n.getSex());
            dto.setProvinceId(n.getProvinceId());
            dto.setProvinceName(n.getProvinceName());
            dto.setCityId(n.getCityId());
            dto.setCityName(n.getCityName());
            dto.setCountryId(n.getCountryId());
            dto.setCountryName(n.getCountryName());
            dto.setTownId(n.getTownId());
            dto.setTownName(n.getTownName());
            dto.setVillageId(n.getVillageId());
            dto.setVillageName(n.getVillageName());
            dto.setTownName(n.getTownName());
            dto.setCommunityId(n.getCommunityId());
            dto.setCommunityName(n.getCommunityName());
            String path = headerMap.get(n.getId());
            dto.setImage(path);
            dtos.add(dto);
        });
        responsePageData.setPageSize(pageSize);
        responsePageData.setPageNo(pageNo);
        responsePageData.setLastPage(page.isLast());
        responsePageData.setFirstPage(page.isFirst());
        responsePageData.setData(dtos);
        responsePageData.setCount(page.getTotalPages());
        responsePageData.setTotalElement(page.getTotalElements());
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

    public ResponseData deleteUserById(Integer userId, Integer status) {
        ResponseData responseData = new ResponseData();
        GarbageUserEntity userEntity = garbageUserDao.findById(userId).get();
        userEntity.setStatus(status);
        garbageUserDao.save(userEntity);
        responseData.setStatus(Constants.responseStatus.Success.getStatus());
        responseData.setMsg("删除成功");
        return responseData;
    }

    @Transactional
    public ResponseData updateUserInfo(Integer userId, String name, Integer sex, String idCard, String phone, Long provinceId, String provinceName,
                                       Long cityId, String cityName, Long countryId, String countryName,
                                       Long townId, String townName, Long villageId, String villageName,
                                       String address,  Integer fileId) {
        ResponseData responseData = new ResponseData();
        try {
            GarbageUserEntity userEntity = garbageUserDao.findById(userId).get();
            userEntity.setId(userId);
            userEntity.setName(name);
            userEntity.setSex(sex);
            userEntity.setIdCard(idCard);
            userEntity.setPhone(phone);
            userEntity.setAddress(address);
//            userEntity.setProvinceId(provinceId);
//            userEntity.setProvinceName(provinceName);
//            userEntity.setCityId(cityId);
//            userEntity.setCityName(cityName);
//            userEntity.setCountryId(countryId);
//            userEntity.setCountryName(countryName);
//            userEntity.setTownId(townId);
//            userEntity.setTownName(townName);
//            userEntity.setVillageId(villageId);
//            userEntity.setVillageName(villageName);
            garbageUserDao.save(userEntity);
            if (fileId != null){
                GarbageImageEntity garbageImageEntity = garbageImageDao.findById(fileId).get();
                if (garbageImageEntity == null){
                    throw new RuntimeException("上传的图片有问题，请重新上传");
                }
                if (garbageImageEntity.getBusId() == userEntity.getId()){
                    //没有更改图片
                } else {
                    //更改图片
                    garbageImageDao.deleteBySourceNameAndBusIdAndType(GarbageUserEntity.class.getName(), userEntity.getId(), Constants.image.HEADER.name());
                    //保存头像
                    garbageImageEntity.setBusId(userEntity.getId());
                    garbageImageEntity.setSourceName(GarbageUserEntity.class.getName());
                    garbageImageEntity.setType(Constants.image.HEADER.name());
                    garbageImageDao.save(garbageImageEntity);
                }
            }
            responseData.setStatus(Constants.responseStatus.Success.getStatus());
            responseData.setMsg("更新成功");
        } catch (Exception e) {
            e.printStackTrace();
            throw  e;
        }
        return responseData;
    }

    public ResponseData residentList(String type, String keyWord,Long provinceId,
                                         Long cityId, Long countryId,  Long townId, Long villageId,
                                         Integer communityId, String roleCode, String jwt, Integer pageNo,
                                         Integer pageSize, String[] orderBys) {
        Pageable pageable = PageRequest.of(pageNo -1, pageSize, getCollectorSort(orderBys));
        Integer sub = jwtUtil.getSubject(jwt);
        GarbageUserEntity userEntity = garbageUserDao.findById(sub).get();
        Integer fromType = userEntity.getFromType();
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
                    if (villageId !=null){
                        Predicate villageIdPredicate = criteriaBuilder.equal(root.get("villageId"), villageId);
                        predicateList.add(villageIdPredicate);
                    } else {
                        Predicate predicate = criteriaBuilder.equal(root.get("townId"), userEntity.getTownId());
                        predicateList.add(predicate);
                    }
                }
                if (roleCodeList.contains("COUNTRY_ADMIN")){
                    // 县/区

                    if (townId !=null){
                        Predicate townIdPredicate = criteriaBuilder.equal(root.get("townId"), townId);
                        predicateList.add(townIdPredicate);
                    } else if (townId !=null && villageId !=null){
                        Predicate villageIdPredicate = criteriaBuilder.equal(root.get("villageId"), villageId);
                        predicateList.add(villageIdPredicate);
                    } else {
                        Predicate predicate = criteriaBuilder.equal(root.get("districtId"), userEntity.getCountryId());
                        predicateList.add(predicate);
                    }
                }
                if (roleCodeList.contains("CITY_ADMIN")){
                    //市
                    if (countryId !=null){
                        Predicate countryIdPredicate = criteriaBuilder.equal(root.get("districtId"), countryId);
                        predicateList.add(countryIdPredicate);
                    } else if (countryId !=null && townId !=null){
                        Predicate townIdPredicate = criteriaBuilder.equal(root.get("townId"), townId);
                        predicateList.add(townIdPredicate);
                    } else if (countryId !=null && townId !=null && villageId !=null){
                        Predicate villageIdPredicate = criteriaBuilder.equal(root.get("villageId"), villageId);
                        predicateList.add(villageIdPredicate);
                    } else {
                        Predicate predicate = criteriaBuilder.equal(root.get("cityId"), userEntity.getCityId());
                        predicateList.add(predicate);
                    }
                }
                if (roleCodeList.contains("PROVINCE_ADMIN")){
                    //省
                    if (cityId !=null) {
                        Predicate cityIdPredicate = criteriaBuilder.equal(root.get("cityId"), cityId);
                        predicateList.add(cityIdPredicate);
                    } else if (cityId !=null &&countryId !=null){
                        Predicate countryIdPredicate = criteriaBuilder.equal(root.get("districtId"), countryId);
                        predicateList.add(countryIdPredicate);
                    } else if (cityId !=null &&countryId !=null && townId !=null){
                        Predicate townIdPredicate = criteriaBuilder.equal(root.get("townId"), townId);
                        predicateList.add(townIdPredicate);
                    } else if (cityId !=null &&countryId !=null && townId !=null && villageId !=null){
                        Predicate villageIdPredicate = criteriaBuilder.equal(root.get("villageId"), villageId);
                        predicateList.add(villageIdPredicate);
                    } else {
                        Predicate predicate = criteriaBuilder.equal(root.get("provinceId"), provinceId);
                        predicateList.add(predicate);
                    }
                }
                if (!StringUtils.isEmpty(type) && !StringUtils.isEmpty(keyWord))
                if ("name".equals(type)) {
                    Predicate predicateName = criteriaBuilder.like(root.get("name"), "%" + keyWord + "%");
                    predicateList.add(predicateName);
                }
                if ("phone".equals(type)) {
                    Predicate predicateName = criteriaBuilder.like(root.get("phone"), "%" + keyWord + "%");
                    predicateList.add(predicateName);
                }
                if ("idCard".equals(type)) {
                    Predicate predicateName = criteriaBuilder.like(root.get("idCard"), "%" + keyWord + "%");
                    predicateList.add(predicateName);
                }
                if ("address".equals(type)){
                    Predicate predicateAddress = criteriaBuilder.like(root.get("address"), "%" + keyWord + "%");
                    predicateList.add(predicateAddress);
                }
                if ("eNo".equals(type)){
                    Join< GarbageUserEntity, GarbageENoEntity> eNoJoin = root.join("eNos", JoinType.INNER);
                    Predicate eNoPredicate = criteriaBuilder.like(eNoJoin.get("eNo"), "%"+keyWord+"%");
                    predicateList.add(eNoPredicate);
                }

                Join<GarbageUserEntity, GarbageRoleEntity> roleEntityJoin = root.join("roles", JoinType.INNER);
                if (StringUtils.isEmpty(roleCode)){
                    //所有   村管理员-- VILLAGE_ADMIN      居民 -- RESIDENT
                    List<String> roleList = new ArrayList<>();
                    roleList.add("RESIDENT");
                    roleList.add("VILLAGE_ADMIN");
                    Predicate predicateIn = roleEntityJoin.get("roleCode").in(roleList);
                    predicateList.add(predicateIn);
                } else {
                    Predicate rolePredicate = criteriaBuilder.equal(roleEntityJoin.get("roleCode"), roleCode);
                    predicateList.add(rolePredicate);
                }

                if (fromType == 1){     //查看小区居民
                    if (communityId != null ){
                        Predicate predicate = criteriaBuilder.equal(root.get("communityId"),communityId);
                        predicateList.add(predicate);
                    } else {
                        if (communityIds.size() > 0){
                            Predicate communityPredicate = root.get("communityId").in(communityIds);
                            predicateList.add(communityPredicate);
                        }
                    }
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

        List<Integer> userIds = page.getContent().stream().map(n-> n.getId()).collect(Collectors.toList());
        List<GarbageUserPointEntity> userPointEntities = null;
        if (userIds.size() > 0){
            userPointEntities = garbageUserPointDao.findByUserIdIn(userIds);
        } else {
            userPointEntities = new ArrayList<>();
        }
        Map<Integer, Integer> userPointMap = userPointEntities.stream().collect(Collectors.toMap(GarbageUserPointEntity::getUserId, GarbageUserPointEntity::getPoint));
        List<GarbageUserDto> dtos = new ArrayList<>();
        page.getContent().forEach(user -> {
            GarbageUserDto dto = new GarbageUserDto();
            dto.setName(user.getName());
            dto.setPhone(user.getPhone());
            String placeName = user.getProvinceName() + user.getCityName() + user.getCountryName();
            String belongTown = (user.getTownName() == null ? "": user.getTownName()) +
                    (user.getVillageName() == null ?"":user.getVillageName()) +
                    (user.getCommunityName() == null ?"":user.getCommunityName());
            dto.setBelongTown(belongTown);
            dto.setAddress(user.getAddress());
            dto.setPlaceName(placeName);
            dto.setCreateDate(DateFormatUtil.formatDate(new Date(user.getCreateTime()), "yyyy-MM-dd"));
            List<GarbageRoleEntity> roleEntities = user.getRoles().stream().collect(Collectors.toList());
            String roleName = "";
            if (roleEntities.size() > 1){
                roleName = roleEntities.get(roleEntities.size() -1).getRoleName();
            } else {
                roleName = roleEntities.get(0).getRoleName();
            }
            dto.setSex(user.getSex());
            dto.setRoleName(roleName);
            dto.setIdentityType(roleName);
            dto.setUserId(user.getId());
            dto.setProvinceId(user.getProvinceId());
            dto.setProvinceName(user.getProvinceName());
            dto.setCityId(user.getCityId());
            dto.setCityName(user.getCityName());
            dto.setCountryId(user.getCountryId());
            dto.setCountryName(user.getCountryName());
            dto.setTownId(user.getTownId());
            dto.setTownName(user.getTownName());
            dto.setVillageId(user.getVillageId());
            dto.setVillageName(user.getVillageName());
            dto.setTownName(user.getTownName());
            dto.setCommunityId(user.getCommunityId());
            dto.setCommunityName(user.getCommunityName());
            dto.setPoint(userPointMap.get(user.getId()) == null?0:userPointMap.get(user.getId()));
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
        pageData.setTotalElement(page.getTotalElements());
        pageData.setData(dtos);
        return pageData;
    }

    public ResponseData getSummaryInfoInManagerCenter(String jwt) {
        Integer sub = jwtUtil.getSubject(jwt);
        GarbageUserEntity userEntity = garbageUserDao.findById(sub).get();
        Integer fromType = userEntity.getFromType();
        List<Integer> communityIds = garbageCollectorService.getCommunityResource(userEntity.getRoles().stream().collect(Collectors.toList()));
        List<String> roleCodes = userEntity.getRoles().stream().map(garbageRoleEntity -> garbageRoleEntity.getRoleCode()).collect(Collectors.toList());
        //会员的注册量
        Long count = garbageUserDao.count(new Specification<GarbageUserEntity>() {
            @Override
            public Predicate toPredicate(Root<GarbageUserEntity> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicateList = new ArrayList<>();
                if(fromType == 0){
                    if (roleCodes.size() == 1 && "RESIDENT".equals(roleCodes.get(0)) || roleCodes.contains("COLLECTOR")){
                        Predicate predicate = criteriaBuilder.equal(root.get("villageId"), userEntity.getVillageId());
                        predicateList.add(predicate);
                    }
                    if (roleCodes.contains("VILLAGE_ADMIN")){
                        //查看本村的注册量
                        Predicate predicate = criteriaBuilder.equal(root.get("villageId"), userEntity.getVillageId());
                        predicateList.add(predicate);
                    }
                    if (roleCodes.contains("TOWN_ADMIN")){
                        Predicate predicate = criteriaBuilder.equal(root.get("townId"), userEntity.getTownId());
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
                    if (roleCodes.stream().filter(roleCode-> roleCode.endsWith("COMMUNITY_ADMIN")).count() > 0 ){
                        //小区管理员
                        if (communityIds.size() > 0){
                            predicateList.add(root.get("communityId").in(communityIds));
                        }
                    } else{
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
                if (fromType == 0){
                    if (roleCodes.size() == 1 && "RESIDENT".equals(roleCodes.get(0)) || roleCodes.contains("COLLECTOR")){
                        Predicate predicate = criteriaBuilder.equal(root.get("villageId"), userEntity.getVillageId());
                        predicateList.add(predicate);
                    }
                    if (roleCodes.contains("VILLAGE_ADMIN")){
                        //查看本村的注册量
                        Predicate predicate = criteriaBuilder.equal(root.get("villageId"), userEntity.getVillageId());
                        predicateList.add(predicate);
                    }
                    if (roleCodes.contains("TOWN_ADMIN")){
                        Predicate predicate = criteriaBuilder.equal(root.get("townId"), userEntity.getTownId());
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
                    if (roleCodes.stream().filter(roleCode-> roleCode.endsWith("COMMUNITY_ADMIN")).count() > 0 ){
                        //小区管理员
                        if (communityIds.size() > 0){
                            predicateList.add(root.get("communityId").in(communityIds));
                        }
                    } else{
                        Predicate predicate = criteriaBuilder.equal(root.get("communityId"), userEntity.getCommunityId());
                        predicateList.add(predicate);
                    }
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

    public ResponseData getRegisterUserCountInMonth(String jwt) {
        Integer sub = jwtUtil.getSubject(jwt);
        GarbageUserEntity userEntity = garbageUserDao.findById(sub).get();
        Integer fromType = userEntity.getFromType();
        List<String> roleCodes = userEntity.getRoles().stream().map(garbageRoleEntity -> garbageRoleEntity.getRoleCode()).collect(Collectors.toList());
        List<Integer> communityIds = garbageCollectorService.getCommunityResource(userEntity.getRoles().stream().collect(Collectors.toList()));
        Calendar calendar = Calendar.getInstance();
        Integer year = calendar.get(Calendar.YEAR);
        Integer month = calendar.get(Calendar.MONTH);
        List<UserCountInMonth> userCountInMonths = new ArrayList<>();
//        if (month < 5){
//            userCountInMonths = garbageUserDao.countUserInMonthBetween(year, 0, month);
//        } else {
//            userCountInMonths = garbageUserDao.countUserInMonthBetween(year, month - 5, month);
//        }
//        userCountInMonths.forEach(userCountInMonth -> {
//            userCountInMonth.setTime(year + "-" + (userCountInMonth.getMonth() + 1));
//        });

        StringBuilder builder = new StringBuilder("");

        builder.append("SELECT u.`month`, count(1) from garbage_user u where  u.`year` = ?1 ");
        if (roleCodes.size() ==1 && "RESIDENT".equals(roleCodes.get(0)) || roleCodes.contains("COLLECTOR")){
            if (fromType == 1){
                builder.append(" and u.community_id = " +  userEntity.getCommunityId());
            }else {
                builder.append(" and u.village_id = " +  userEntity.getVillageId());
            }
        }
        if (roleCodes.stream().filter(n -> n.endsWith("COMMUNITY_ADMIN")).count()> 0){
            if (communityIds.size() > 0){
                builder.append(" and u.community_id in (");
                for (int i = 0; i < communityIds.size(); i++) {
                    if (i == communityIds.size() - 1){
                        builder.append( communityIds.get(i) + " )");
                    } else {
                        builder.append(communityIds.get(i) +  ",");
                    }
                }
            }
        }
        if (roleCodes.stream().filter(n->n.endsWith("COMMUNITY_REMARK")).count() > 0){
            builder.append(" and u.community_id = " +  userEntity.getCommunityId());
        }
        if (roleCodes.contains("VILLAGE_ADMIN")  ) {
            builder.append(" and u.village_id = " +  userEntity.getVillageId());
        } else if (roleCodes.contains("TOWN_ADMIN")){
            builder.append(" and u.town_id = " +  userEntity.getTownId());
        }else if (roleCodes.contains("COUNTRY_ADMIN")){
            builder.append(" and u.country_d = " +  userEntity.getCountryId());
        }else if (roleCodes.contains("CITY_ADMIN")){
            builder.append(" and u.city_id = " +  userEntity.getCityId());
        }else if (roleCodes.contains("PROVINCE_ADMIN")){
            builder.append(" and u.province_id = " +  userEntity.getProvinceId());
        } else {

        }
        if (month < 5){
            builder.append(" and u.`month` < 5");
        } else {
            builder.append(" and u.`month` BETWEEN (?2 - 5) and ?2");
        }

        builder.append(" group by u.`month` ");

        List<Object[]> data = entityManager.createNativeQuery(builder.toString())
                .setParameter(1, year)
                .setParameter(2,month).getResultList();
        for (int i = 0; i < data.size(); i++) {
            UserCountInMonth countInMonth = new UserCountInMonth();
            Integer m = (Integer) data.get(i)[0];
            BigInteger count  = (BigInteger) data.get(i)[1];
            countInMonth.setTime(year + "-" + (m+1) );
            countInMonth.setCount(count.longValue());
            userCountInMonths.add(countInMonth);
        }
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

    @Deprecated
    /**
     * 废弃
     */
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

        Long startTime = DateFormatUtil.getFirstTimeOfDay(DateFormatUtil.formatDate(new Date(), "yyyy-MM-dd")).getTime();
        Long endTime = DateFormatUtil.getLastTimeOfDay(DateFormatUtil.formatDate(new Date(), "yyyy-MM-dd")).getTime();
        List<GarbageCollectorEntity> list = garbageCollectorDao.findAll(new Specification<GarbageCollectorEntity>() {
            @Override
            public Predicate toPredicate(Root<GarbageCollectorEntity> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList<>();
                if (!StringUtils.isEmpty(eNo)){
                    Predicate predicate =criteriaBuilder.equal(root.get("eNo"), eNo);
                    predicates.add(predicate);
                }
                Predicate startPredicate = criteriaBuilder.greaterThanOrEqualTo(root.get("collectDate"), startTime);
                Predicate predicateEnd = criteriaBuilder.lessThanOrEqualTo(root.get("collectDate"), endTime);
                predicates.add(startPredicate);
                predicates.add(predicateEnd);
                return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        });
        ResponseData responseData = new ResponseData();
        if (list.size() > 0){
            userDto.setCollect(true);
        } else {
            userDto.setCollect(false);
        }
        userDto.seteNo(eNo);
        responseData.setStatus(Constants.responseStatus.Success.getStatus());
        responseData.setMsg("用户信息获取成功");
        responseData.setData(userDto);
        return responseData;
    }

    public ResponseData userManagement(Integer pageNo, Integer pageSize, String name, String phone, String jwt, String[] orderBys) {
        Integer sub = jwtUtil.getSubject(jwt);
        GarbageUserEntity userEntity = garbageUserDao.findById(sub).get();
        Integer fromType = userEntity.getFromType();
        List<GarbageRoleEntity> roleEntityList = userEntity.getRoles().stream().collect(Collectors.toList());
        List<String> roleCodes = roleEntityList.stream().map(garbageRoleEntity -> garbageRoleEntity.getRoleCode()).collect(Collectors.toList());
        Pageable pageable = PageRequest.of(pageNo - 1, pageSize, userManagement(orderBys));
        List<Integer> communityIds = new ArrayList<>();
        if (fromType ==1){
            communityIds =garbageCollectorService.getCommunityResource(userEntity.getRoles().stream().collect(Collectors.toList()));
        }
        List<Integer> finalCommunityIds = communityIds;
        Page<GarbageUserEntity> page = garbageUserDao.findAll(new Specification<GarbageUserEntity>() {
            @Override
            public Predicate toPredicate(Root<GarbageUserEntity> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList<>();
                if (fromType == 1){
                    if (roleCodes.stream().filter(n-> n.endsWith("COMMUNITY_ADMIN")).count() > 0){
                        if (finalCommunityIds.size()> 0){
                            Predicate predicate = root.get("id").in(finalCommunityIds);
                            predicates.add(predicate);
                        }
                    }
                    if ( roleCodes.stream().filter(n-> n.endsWith("COMMUNITY_REMARK")).count() > 0){
                        predicates.add(criteriaBuilder.equal(root.get("id"), userEntity.getCommunityId()));
                    }
                } else {
                    if (roleCodes.size() ==1 && "RESIDENT".equals(roleCodes.get(0)) || roleCodes.contains("COLLECTOR")){
                        predicates.add(criteriaBuilder.equal(root.get("villageId"), userEntity.getVillageId()));
                    }
                    if (roleCodes.contains("VILLAGE_ADMIN")  ) {
                        predicates.add(criteriaBuilder.equal(root.get("villageId"), userEntity.getVillageId()));
                    } else if (roleCodes.contains("TOWN_ADMIN")){
                        predicates.add(criteriaBuilder.equal(root.get("townId"), userEntity.getTownId()));
                    }else if (roleCodes.contains("COUNTRY_ADMIN")){
                        predicates.add(criteriaBuilder.equal(root.get("countryId"), userEntity.getCountryId()));
                    }else if (roleCodes.contains("CITY_ADMIN")){
                        predicates.add(criteriaBuilder.equal(root.get("cityId"), userEntity.getCityId()));
                    } else {
                        predicates.add(criteriaBuilder.equal(root.get("provinceId"), userEntity.getProvinceId()));
                    }
                }
                if (!StringUtils.isEmpty(name)){
                    Predicate predicate = criteriaBuilder.like(root.get("name"), "%" + name + "%");
                    predicates.add(predicate);
                }
                if (!StringUtils.isEmpty(phone)){
                    Predicate predicate = criteriaBuilder.like(root.get("phone"), "%" + phone + "%");
                    predicates.add(predicate);
                }
                return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        }, pageable);
        ResponsePageData responsePageData = new ResponsePageData();
        responsePageData.setPageNo(pageNo);
        responsePageData.setPageSize(pageSize);
        responsePageData.setCount(page.getTotalPages());
        responsePageData.setTotalElement(page.getTotalElements());
        responsePageData.setLastPage(page.isLast());
        responsePageData.setFirstPage(page.isFirst());
        responsePageData.setData(page.getContent());
        responsePageData.setStatus(Constants.responseStatus.Success.getStatus());
        responsePageData.setMsg("用戶列表查询成功");
        return responsePageData;
    }

    private Sort userManagement(String[] orderBys){
        Sort sort = null;
        if (orderBys == null || orderBys.length == 0 ){
            sort = Sort.by("id").descending();
        }else {
            sort =   Sort.by(Arrays.stream(orderBys).map((it) -> {
                String[] items = it.split(";");
                String property = "";
                Sort.Direction direction = null;
                return new Sort.Order(direction, property);
            }).collect(Collectors.toList()));
        }
        return sort;
    }

    public ResponseData addRoleToUser(Integer userId, Integer[] roleId, String jwt) {
        Integer sub = jwtUtil.getSubject(jwt);
        GarbageUserEntity authorUser = garbageUserDao.findById(sub).get();
        GarbageUserEntity userEntity = garbageUserDao.findById(userId).get();
        List<String> roleCodes = authorUser.getRoles().stream().map(u ->u.getRoleCode()).collect(Collectors.toList());
        if (roleCodes.contains("SYSTEM_ADMIN")||roleCodes.contains("COUNTRY_ADMIN")||roleCodes.contains("CITY_ADMIN") ||roleCodes.contains("PROVINCE_ADMIN") ){
            List<Integer> roleIds = Arrays.stream(roleId).collect(Collectors.toList());
            roleIds.add(1);
            List<GarbageRoleEntity> roleEntities = garbageRoleDao.findByIdIn(roleIds);
            userEntity.setRoles(new HashSet<>(roleEntities));
            garbageUserDao.save(userEntity);
        } else {
            throw new RuntimeException("没有权限进行此授权操作");
        }

        ResponseData responseData = new ResponseData();
        responseData.setStatus(Constants.responseStatus.Success.getStatus());
        responseData.setMsg("角色授权成功");
        responseData.setData(userEntity);
        return responseData;
    }

    @Transactional
    public ResponseData addUserBatch(MultipartFile file, String jwt) {
        ResponseData responseData = new ResponseData();
        Integer sub = jwtUtil.getSubject(jwt);
        GarbageUserEntity userEntity = garbageUserDao.findById(sub).get();
        List<GarbageRoleEntity> roleEntities = userEntity.getRoles().stream().collect(Collectors.toList());
        List<String> roleCodes = roleEntities.stream().map(garbageRoleEntity -> garbageRoleEntity.getRoleCode()).collect(Collectors.toList());
        if (roleCodes.contains("VILLAGE_ADMIN") || roleCodes.stream().filter(n-> n.endsWith("COMMUNITY_ADMIN")).collect(Collectors.toList()).size() > 0){
            String root_fileName = file.getOriginalFilename();
            String suffix  = FilenameUtils.getExtension(root_fileName);
            InputStream is = null;
            try {
                is = file.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
            List<GarbageUserEntity> garbageUserEntityList = new ArrayList<>();
            List<UserExcelDto> list = ReadExcelUtil.readExcel(is, suffix);
            List<String> phones = list.stream().map(u->u.getPhone()).collect(Collectors.toList());
            List<GarbageUserEntity> userEntityList = garbageUserDao.findByPhoneIn(phones);
            if (userEntityList.size() > 0){
                String p = phones.stream().collect(Collectors.joining(","));
                throw new RuntimeException("导入的手机号出现重复，请核查 " + p);
            }

            Calendar calendar  = Calendar.getInstance();
            list.forEach(u ->{
                GarbageUserEntity garbageUserEntity = new GarbageUserEntity();
                garbageUserEntity.setName(u.getName());
                garbageUserEntity.setLoginName(u.getPhone());
                garbageUserEntity.setPhone(u.getPhone());
                garbageUserEntity.setPassword(CommonUtil.md5(u.getPassword()));
                garbageUserEntity.setStatus(Constants.dataType.ENABLE.getType());
                garbageUserEntity.setCredentialsNonExpired(true);
                garbageUserEntity.setAccountNonExpired(true);
                garbageUserEntity.setAccountNonLocked(true);
                garbageUserEntity.setEnabled(true);
                garbageUserEntity.setUserType("2");   //2系统导入   1手动注册
                garbageUserEntity.setCompany(u.getCompany());
                garbageUserEntity.setSex("男".equals(u.getSex())? 1:0);
                garbageUserEntity.setCleaner("是".equals(u.getCleaner())?true:false);
                garbageUserEntity.setDangYuan("是".equals(u.getDangYuan())?true:false);
                garbageUserEntity.setCunMinDaiBiao("是".equals(u.getCunMinDaiBiao())?true:false);
                garbageUserEntity.setCunZuLeader("是".equals(u.getCunZuLeader())?true:false);
                garbageUserEntity.setStreetCommentDaiBiao("是".equals(u.getStreetCommentDaiBiao())?true:false);
                garbageUserEntity.setLiangDaiBiaoYiWeiYuan("是".equals(u.getLiangDaiBiaoYiWeiYuan())?true:false);
                garbageUserEntity.setCunLeader("是".equals(u.getCunLeader())?true:false);
                garbageUserEntity.setWomenExeLeader("是".equals(u.getWomenExeLeader())?true:false);
                garbageUserEntity.setProvinceId(userEntity.getProvinceId());
                garbageUserEntity.setProvinceName(userEntity.getProvinceName());
                garbageUserEntity.setCityName(userEntity.getCityName());
                garbageUserEntity.setCityId(userEntity.getCityId());
                garbageUserEntity.setCountryId(userEntity.getCountryId());
                garbageUserEntity.setCountryName(userEntity.getCountryName());
                garbageUserEntity.setTownName(userEntity.getTownName());
                garbageUserEntity.setTownId(userEntity.getTownId());
                garbageUserEntity.setVillageName(userEntity.getVillageName());
                garbageUserEntity.setVillageId(userEntity.getVillageId());
                garbageUserEntity.setCommunityName(userEntity.getCommunityName());
                garbageUserEntity.setCommunityId(userEntity.getCommunityId());
                garbageUserEntity.setFromType(userEntity.getFromType());
                garbageUserEntity.setAddress(u.getAddress());
                if (!StringUtils.isEmpty(u.getIdCard())){
                    garbageUserEntity.setIdCard(u.getIdCard());
                }
                garbageUserEntity.setDay(calendar.get(Calendar.DAY_OF_MONTH));
                garbageUserEntity.setMonth(calendar.get(Calendar.MONTH));
                garbageUserEntity.setYear(calendar.get(Calendar.YEAR));
                GarbageRoleEntity roleEntity  = garbageRoleDao.findByRoleCode("RESIDENT");;
                garbageUserEntity.getRoles().add(roleEntity);
                String[] eNoElements = u.geteNo().split(",");
                List<GarbageENoEntity> eNoEntityList = new ArrayList<>();
                Arrays.stream(eNoElements).forEach(e->{
                    GarbageENoEntity garbageENoEntity = new GarbageENoEntity();
                    garbageENoEntity.seteNo(e);
                    garbageENoEntity.setStatus(1);
                    eNoEntityList.add(garbageENoEntity);
                });
                garbageUserEntity.seteNos(eNoEntityList);

                garbageUserEntityList.add(garbageUserEntity);
            });
            garbageUserDao.saveAll(garbageUserEntityList);
            responseData.setMsg("数据批量导入成功");
            responseData.setStatus(Constants.responseStatus.Success.getStatus());
        } else {
            throw new RuntimeException("该用户没有权限批量导入数据");
        }
        return responseData;
    }

    public ResponseData totalCountUserInfoAndGarbageWeight(Long id, String jwt) {
        Integer sub = jwtUtil.getSubject(jwt);
        GarbageUserEntity userEntity = garbageUserDao.findById(sub).get();
        Integer fromType = userEntity.getFromType();
        List<GarbageRoleEntity> roleEntities = userEntity.getRoles().stream().collect(Collectors.toList());
        List<Integer> ids = garbageCollectorService.getCommunityResource(roleEntities);
        List<String> roleCodes = roleEntities.stream().map(garbageRoleEntity -> garbageRoleEntity.getRoleCode()).collect(Collectors.toList());
        Long totalUser = garbageUserDao.count(new Specification<GarbageUserEntity>() {
            @Override
            public Predicate toPredicate(Root<GarbageUserEntity> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                    List<Predicate> predicates = new ArrayList<>();
                predicates.add(criteriaBuilder.equal(root.get("fromType"), fromType));
                if (roleCodes.size() ==1 && "RESIDENT".equals(roleCodes.get(0))){
                    predicates.add(criteriaBuilder.equal(root.get("villageId"), userEntity.getVillageId()));
                }

                if (roleCodes.contains("COLLECTOR") || roleCodes.contains("VILLAGE_ADMIN")  ) {
                   predicates.add(criteriaBuilder.equal(root.get("villageId"), userEntity.getVillageId()));
                } else if (roleCodes.contains("TOWN_ADMIN")) {
                    if (id !=null){
                        predicates.add(criteriaBuilder.equal(root.get("villageId"), id));
                    }
                    predicates.add(criteriaBuilder.equal(root.get("townId"), userEntity.getTownId()));
                } else if (roleCodes.contains("COUNTRY_ADMIN")){
                    if (id !=null){
                        predicates.add(criteriaBuilder.equal(root.get("villageId"), id));
                    }
                    predicates.add(criteriaBuilder.equal(root.get("countryId"), userEntity.getCountryId()));
                } else if (roleCodes.contains("CITY_ADMIN")){
                    if (id !=null){
                        predicates.add(criteriaBuilder.equal(root.get("villageId"), id));
                    }
                    predicates.add(criteriaBuilder.equal(root.get("cityId"), userEntity.getCityId()));
                } else if (roleCodes.contains("PROVINCE_ADMIN")){
                    if (id !=null){
                        predicates.add(criteriaBuilder.equal(root.get("villageId"), id));
                    }
                    predicates.add(criteriaBuilder.equal(root.get("provinceId"), userEntity.getProvinceId()));
                } else {
                    if (id != null){
                        predicates.add(criteriaBuilder.equal(root.get("communityId"), id));
                    } else {
                        if (ids.size()> 0){
                            predicates.add(root.get("communityId").in(ids));
                        }
                    }
                }
                return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        });


        Long startTime = DateFormatUtil.getFirstTimeOfDay(DateFormatUtil.formatDate(new Date(), "yyyy-MM-dd")).getTime();
        Long endTime = DateFormatUtil.getLastTimeOfDay(DateFormatUtil.formatDate(new Date(), "yyyy-MM-dd")).getTime();
        Long todayCount = garbageCollectorDao.count(new Specification<GarbageCollectorEntity>() {
            @Override
            public Predicate toPredicate(Root<GarbageCollectorEntity> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList<>();
                predicates.add(criteriaBuilder.equal(root.get("garbageFromType"), fromType));
                if (startTime !=null){
                    Predicate predicate = criteriaBuilder.greaterThanOrEqualTo(root.get("collectDate"), startTime);
                    predicates.add(predicate);
                }
                if (endTime != null){
                    Predicate predicate = criteriaBuilder.lessThanOrEqualTo(root.get("collectDate"), endTime);
                    predicates.add(predicate);
                }
                if (roleCodes.size() ==1 && "RESIDENT".equals(roleCodes.get(0))){
                    predicates.add(criteriaBuilder.equal(root.get("villageId"), userEntity.getVillageId()));
                }
                if (roleCodes.contains("COLLECTOR") || roleCodes.contains("VILLAGE_ADMIN")  ) {
                    predicates.add(criteriaBuilder.equal(root.get("villageId"), userEntity.getVillageId()));
                } else if (roleCodes.contains("TOWN_ADMIN")) {
                    if (id !=null){
                        predicates.add(criteriaBuilder.equal(root.get("villageId"), id));
                    }
                    predicates.add(criteriaBuilder.equal(root.get("townId"), userEntity.getTownId()));
                } else if (roleCodes.contains("COUNTRY_ADMIN")){
                    if (id !=null){
                        predicates.add(criteriaBuilder.equal(root.get("villageId"), id));
                    }
                    predicates.add(criteriaBuilder.equal(root.get("countryId"), userEntity.getCountryId()));
                } else if (roleCodes.contains("CITY_ADMIN")){
                    if (id !=null){
                        predicates.add(criteriaBuilder.equal(root.get("villageId"), id));
                    }
                    predicates.add(criteriaBuilder.equal(root.get("cityId"), userEntity.getCityId()));
                } else if (roleCodes.contains("PROVINCE_ADMIN")){
                    if (id !=null){
                        predicates.add(criteriaBuilder.equal(root.get("villageId"), id));
                    }
                    predicates.add(criteriaBuilder.equal(root.get("provinceId"), userEntity.getProvinceId()));
                } else {
                    if (id != null){
                        predicates.add(criteriaBuilder.equal(root.get("communityId"), id));
                    } else {
                        if (ids.size()> 0){
                            predicates.add(root.get("communityId").in(ids));
                        }
                    }
                }
//                criteriaQuery.groupBy(root.get("userId"));
                return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        });
        Long totalUserPartIn = garbageCollectorDao.count(new Specification<GarbageCollectorEntity>() {
            @Override
            public Predicate toPredicate(Root<GarbageCollectorEntity> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList<>();
                predicates.add(criteriaBuilder.equal(root.get("garbageFromType"), fromType));
                if (roleCodes.size() ==1 && "RESIDENT".equals(roleCodes.get(0))){
                    predicates.add(criteriaBuilder.equal(root.get("villageId"), userEntity.getVillageId()));
                }
                if (roleCodes.contains("COLLECTOR") || roleCodes.contains("VILLAGE_ADMIN")  ) {
                    predicates.add(criteriaBuilder.equal(root.get("villageId"), userEntity.getVillageId()));
                } else if (roleCodes.contains("TOWN_ADMIN")) {
                    if (id !=null){
                        predicates.add(criteriaBuilder.equal(root.get("villageId"), id));
                    }
                    predicates.add(criteriaBuilder.equal(root.get("townId"), userEntity.getTownId()));
                } else if (roleCodes.contains("COUNTRY_ADMIN")){
                    if (id !=null){
                        predicates.add(criteriaBuilder.equal(root.get("villageId"), id));
                    }
                    predicates.add(criteriaBuilder.equal(root.get("countryId"), userEntity.getCountryId()));
                } else if (roleCodes.contains("CITY_ADMIN")){
                    if (id !=null){
                        predicates.add(criteriaBuilder.equal(root.get("villageId"), id));
                    }
                    predicates.add(criteriaBuilder.equal(root.get("cityId"), userEntity.getCityId()));
                } else if (roleCodes.contains("PROVINCE_ADMIN")){
                    if (id !=null){
                        predicates.add(criteriaBuilder.equal(root.get("villageId"), id));
                    }
                    predicates.add(criteriaBuilder.equal(root.get("provinceId"), userEntity.getProvinceId()));
                } else {
                    if (id != null){
                        predicates.add(criteriaBuilder.equal(root.get("communityId"), id));
                    } else {
                        if (ids.size()> 0){
                            predicates.add(root.get("communityId").in(ids));
                        }
                    }
                }
//                criteriaQuery.groupBy(root.get("userId"));
                return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        });

        StringBuilder stringBuilder = new StringBuilder(" SELECT count(1) FROM ( " +
                " SELECT user_id FROM garbage_collector  WHERE garbage_from_type = 0 ");
        stringBuilder.append("  and collect_date >= " + startTime + " and collect_date <= " + endTime);
        if (roleCodes.size() ==1 && "RESIDENT".equals(roleCodes.get(0))){
            stringBuilder.append( " and village_id = " + userEntity.getVillageId());
        }
        if (roleCodes.contains("COLLECTOR") || roleCodes.contains("VILLAGE_ADMIN")){
            stringBuilder.append( " and village_id = " + userEntity.getVillageId());
        }  else if (roleCodes.contains("TOWN_ADMIN")){
            if (id != null){
                stringBuilder.append( " and village_id = " + id);
            } else {
                stringBuilder.append( " and town_id = " + userEntity.getTownId());
            }
        } else if (roleCodes.contains("COUNTRY_ADMIN")){
            if (id !=null){
                stringBuilder.append( " and village_id = " + id);
            }
            stringBuilder.append( " and country_id = " + userEntity.getCountryId());
        } else if (roleCodes.contains("CITY_ADMIN")){
            if (id !=null){
                stringBuilder.append( " and village_id = " + id);
            }
            stringBuilder.append( " and city_id = " + userEntity.getCityId());

        } else if (roleCodes.contains("PROVINCE_ADMIN")){
            if (id !=null){
                stringBuilder.append( " and village_id = " + id);
            }
            stringBuilder.append( " and province_id = " + userEntity.getProvinceId());
        } else {
            if (id != null){
                stringBuilder.append( " and community_id = " + id);
            } else {
                if (ids.size()> 0){
                    stringBuilder.append( " and community_id in ( ");
                    for (int i = 0; i <ids.size() ; i++) {
                        Integer dd = ids.get(i);
                        if (i == ids.size() -1){
                            stringBuilder.append(  dd + " )");
                        } else {
                            stringBuilder.append(  dd + ", ");
                        }
                    }
                }
            }
        }
        stringBuilder.append(" GROUP BY user_id ) t");

        BigInteger singleResult1 =(BigInteger) entityManager.createNativeQuery(stringBuilder.toString()).getSingleResult();
        Double d = 0.0;
        if(singleResult1 == null){
            d = 0.0;
        } else {
            d = singleResult1.doubleValue()/totalUser;
        }
        BigDecimal bigDecimal = new BigDecimal(d);
        double partInRate = bigDecimal.setScale(4, BigDecimal.ROUND_HALF_UP).doubleValue();

        StringBuilder subQuery = new StringBuilder("");
        StringBuilder sql = new StringBuilder( " select ROUND((select IFNULL(count(1), 0) from garbage_collector WHERE garbage_quality  = 1  ");

        sql.append("and garbage_from_type = " + fromType);
        subQuery.append("and garbage_from_type = " + fromType);
        if (roleCodes.size() ==1 && "RESIDENT".equals(roleCodes.get(0))){
            sql.append( " and village_id = " + userEntity.getVillageId());
            subQuery.append( " and village_id = " + userEntity.getVillageId());
        }
        if (roleCodes.contains("COLLECTOR") || roleCodes.contains("VILLAGE_ADMIN")  ) {
            sql.append( " and village_id = " + userEntity.getVillageId());
            subQuery.append( " and village_id = " + userEntity.getVillageId());
        } else if (roleCodes.contains("TOWN_ADMIN")) {
            if (id !=null){
                sql.append( " and village_id = " + id);
                subQuery.append( " and village_id = " + id);
            }
            sql.append( " and town_id = " + userEntity.getTownId());
            subQuery.append( " and town_id = " + userEntity.getTownId());
        } else if (roleCodes.contains("COUNTRY_ADMIN")){
            if (id !=null){
                sql.append( " and village_id = " + id);
                subQuery.append( " and village_id = " + id);
            }
            sql.append( " and country_id = " + userEntity.getCountryId());
            subQuery.append( " and country_id = " + userEntity.getCountryId());
        } else if (roleCodes.contains("CITY_ADMIN")){
            if (id !=null){
                sql.append( " and village_id = " + id);
                subQuery.append( " and village_id = " + id);
            }
            sql.append( " and city_id = " + userEntity.getCityId());
            subQuery.append( " and city_id = " + userEntity.getCityId());
        } else if (roleCodes.contains("PROVINCE_ADMIN")){
            if (id !=null){
                sql.append( " and village_id = " + id);
                subQuery.append( " and village_id = " + id);
            }
            sql.append( " and province_id = " + userEntity.getProvinceId());
            subQuery.append( " and province_id = " + userEntity.getProvinceId());
        } else {
            if (id != null){
                sql.append( " and community_id = " + id);
                subQuery.append( " and community_id = " + id);
            } else {
                if (ids.size()> 0){
                    sql.append( " and community_id in ( ");
                    subQuery.append( " and community_id in ( ");
                    for (int i = 0; i <ids.size() ; i++) {
                        Integer dd = ids.get(i);
                        if (i == ids.size() -1){
                            sql.append(  dd + " )");
                            subQuery.append(  dd + " )");
                        } else {
                            sql.append(  dd + ", ");
                            subQuery.append(  dd + ", ");
                        }
                    }
                }
            }
        }

        sql.append(" and collect_date BETWEEN "+ startTime+" AND " + endTime);
        subQuery.append(" and  collect_date BETWEEN "+ startTime+" AND " + endTime);
        sql.append(" )/count(1), 3) as rightRate, ").append(" ROUND(SUM(garbage_weight),2) as totalWeight ").append(" from garbage_collector where 1 = 1 ").append(subQuery.toString());
        Object[] singleResult = (Object[]) entityManager.createNativeQuery(sql.toString()).getSingleResult();
//        UserCollectRightAndWeightDto rightAndWeightDto = garbageCollectorDao.getRightAndGarbageWeight();
        UserCollectCountDto dto = new UserCollectCountDto();
        if (singleResult[0] != null){
            BigDecimal bigDecimal1 = (BigDecimal) singleResult[0];
            bigDecimal1.setScale(4, RoundingMode.HALF_UP);
            dto.setRightRate(bigDecimal1.doubleValue());
        } else {
            dto.setRightRate(0.0);
        }
        if (singleResult[1] != null) {
            dto.setTotalWeight((Double)singleResult[1]);
        } else {
            dto.setTotalWeight(0.0);
        }
        dto.setTodayCount(todayCount);
        dto.setResidentCount(totalUser);
        dto.setTotalUser(totalUser);
        dto.setPartInRate(partInRate);
        ResponseData response = new ResponseData();
        response.setMsg("统计信息查询成功");
        response.setData(dto);
        response.setStatus(Constants.responseStatus.Success.getStatus());
        return response;
    }

    public ResponseData UpdateUserBatch(MultipartFile file, String jwt) {
        ResponseData responseData = new ResponseData();
        Integer sub = jwtUtil.getSubject(jwt);
        GarbageUserEntity userEntity = garbageUserDao.findById(sub).get();
        List<GarbageRoleEntity> roleEntities = userEntity.getRoles().stream().collect(Collectors.toList());
        List<String> roleCodes = roleEntities.stream().map(garbageRoleEntity -> garbageRoleEntity.getRoleCode()).collect(Collectors.toList());
        if (roleCodes.contains("VILLAGE_ADMIN") || roleCodes.stream().filter(n-> n.endsWith("COMMUNITY_ADMIN")).collect(Collectors.toList()).size() > 0){
            String root_fileName = file.getOriginalFilename();
            String suffix  = FilenameUtils.getExtension(root_fileName);
            InputStream is = null;
            try {
                is = file.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
            List<GarbageUserEntity> garbageUserEntityList = new ArrayList<>();
            List<UserExcelDto> list = ReadExcelUtil.readExcel(is, suffix);
            List<String> phones = list.stream().map(u->u.getPhone()).collect(Collectors.toList());
            List<GarbageUserEntity> userEntityList = garbageUserDao.findByPhoneIn(phones);
            list.forEach(u ->{
                GarbageUserEntity garbageUserEntity = garbageUserDao.findByPhone(u.getPhone());
                garbageUserEntity.setName(u.getName());
                garbageUserEntity.setStatus(Constants.dataType.ENABLE.getType());
                garbageUserEntity.setCredentialsNonExpired(true);
                garbageUserEntity.setAccountNonExpired(true);
                garbageUserEntity.setAccountNonLocked(true);
                garbageUserEntity.setEnabled(true);
                garbageUserEntity.setUserType("2");   //2系统导入   1手动注册
                garbageUserEntity.setCompany(u.getCompany());
                garbageUserEntity.setSex("男".equals(u.getSex())? 1:0);
                garbageUserEntity.setCleaner("是".equals(u.getCleaner())?true:false);
                garbageUserEntity.setDangYuan("是".equals(u.getDangYuan())?true:false);
                garbageUserEntity.setCunMinDaiBiao("是".equals(u.getCunMinDaiBiao())?true:false);
                garbageUserEntity.setCunZuLeader("是".equals(u.getCunZuLeader())?true:false);
                garbageUserEntity.setStreetCommentDaiBiao("是".equals(u.getStreetCommentDaiBiao())?true:false);
                garbageUserEntity.setLiangDaiBiaoYiWeiYuan("是".equals(u.getLiangDaiBiaoYiWeiYuan())?true:false);
                garbageUserEntity.setCunLeader("是".equals(u.getCunLeader())?true:false);
                garbageUserEntity.setWomenExeLeader("是".equals(u.getWomenExeLeader())?true:false);
                garbageUserEntity.setAddress(u.getAddress());
                if (!StringUtils.isEmpty(u.getIdCard())){
                    garbageUserEntity.setIdCard(u.getIdCard());
                }
                GarbageRoleEntity roleEntity  = garbageRoleDao.findByRoleCode("RESIDENT");;
                garbageUserEntity.getRoles().add(roleEntity);
                String[] eNoElements = u.geteNo().split(",");
                List<GarbageENoEntity> eNoEntityList = new ArrayList<>();
                Arrays.stream(eNoElements).forEach(e->{
                    List<GarbageENoEntity> garbageENoEntityList = garbageENoDao.findByUserId(garbageUserEntity.getId());
                    if (garbageENoEntityList.size() > 0){
                        GarbageENoEntity garbageENoEntity = garbageENoEntityList.get(0);
                        garbageENoEntity.seteNo(e);
                        garbageENoEntity.setStatus(1);
                        eNoEntityList.add(garbageENoEntity);
                    } else {
                        GarbageENoEntity garbageENoEntity = new GarbageENoEntity();
                        garbageENoEntity.seteNo(e);
                        garbageENoEntity.setStatus(1);
                        garbageENoEntity.setUserId(garbageUserEntity.getId());
                        eNoEntityList.add(garbageENoEntity);
                    }
                });
                garbageUserEntity.seteNos(eNoEntityList);

                garbageUserEntityList.add(garbageUserEntity);
            });
            garbageUserDao.saveAll(garbageUserEntityList);
            responseData.setMsg("数据批量导入成功");
            responseData.setStatus(Constants.responseStatus.Success.getStatus());
        } else {
            throw new RuntimeException("该用户没有权限批量导入数据");
        }
        return responseData;
    }
}
