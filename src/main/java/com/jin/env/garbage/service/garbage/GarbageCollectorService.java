package com.jin.env.garbage.service.garbage;

import com.jin.env.garbage.controller.garbage.GarbageCollectorController;
import com.jin.env.garbage.dao.garbage.GarbageCollectorDao;
import com.jin.env.garbage.dao.garbage.GarbageQualityPointDao;
import com.jin.env.garbage.dao.image.GarbageImageDao;
import com.jin.env.garbage.dao.point.GarbagePointRecordDao;
import com.jin.env.garbage.dao.point.GarbageUserPointDao;
import com.jin.env.garbage.dao.position.*;
import com.jin.env.garbage.dao.user.GarbageENoDao;
import com.jin.env.garbage.dao.user.GarbageRoleCommunityDao;
import com.jin.env.garbage.dao.user.GarbageRoleDao;
import com.jin.env.garbage.dao.user.GarbageUserDao;
import com.jin.env.garbage.dto.garbage.*;
import com.jin.env.garbage.dto.user.UserCountInMonth;
import com.jin.env.garbage.dto.user.UserDto;
import com.jin.env.garbage.dto.user.UserVillageDto;
import com.jin.env.garbage.entity.garbage.GarbageCollectorEntity;
import com.jin.env.garbage.entity.garbage.GarbageQualityPointEntity;
import com.jin.env.garbage.entity.image.GarbageImageEntity;
import com.jin.env.garbage.entity.point.GarbagePointRecordEntity;
import com.jin.env.garbage.entity.point.GarbageUserPointEntity;
import com.jin.env.garbage.entity.position.*;
import com.jin.env.garbage.entity.user.GarbageENoEntity;
import com.jin.env.garbage.entity.user.GarbageRoleCommunityEntity;
import com.jin.env.garbage.entity.user.GarbageRoleEntity;
import com.jin.env.garbage.entity.user.GarbageUserEntity;
import com.jin.env.garbage.jwt.JwtUtil;
import com.jin.env.garbage.utils.Constants;
import com.jin.env.garbage.utils.DateFormatUtil;
import com.jin.env.garbage.utils.ResponseData;
import com.jin.env.garbage.utils.ResponsePageData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@SuppressWarnings("ALL")
@Service
public class GarbageCollectorService {
    private Logger logger = LoggerFactory.getLogger(GarbageCollectorController.class);
    @Value("${pointScoreForQualified}")
    private Integer pointScoreForQualified;

    @Value("${pointScoreForNoQualified}")
    private Integer pointScoreForNoQualified;

    @Value("${pointScoreForEmpty}")
    private Integer pointScoreForEmpty;

    @Autowired
    private GarbageENoDao garbageENoDao;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private GarbageUserDao garbageUserDao;

    @Autowired
    private GarbageQualityPointDao garbageQualityPointDao;

    @Autowired
    private GarbageCollectorDao garbageCollectorDao;

    @Autowired
    private GarbageImageDao garbageImageDao;

    @Autowired
    private GarbageUserPointDao garbageUserPointDao;

    @Autowired
    private GarbageRoleDao garbageRoleDao;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private GarbageRoleCommunityDao garbageRoleCommunityDao;

    @Autowired
    private GarbagePointRecordDao garbagePointRecordDao;
    @Autowired
    private GarbageCommunityDao garbageCommunityDao;

    @Autowired
    private JPositionProvinceDao jPositionProvinceDao;

    @Autowired
    private JPositionCityDao jPositionCityDao;

    @Autowired
    private JPositionCountyDao jPositionCountyDao;

    @Autowired
    private JPositionTownDao jPositionTownDao;

    @Autowired
    private JPositionVillageDao jPositionVillageDao;

    @Transactional
    public ResponseData addGarbageByCollector(String eNo, String quality, Double weight, Integer imageId, String jwt) {
        Integer sub = jwtUtil.getSubject(jwt);
        //垃圾收集员
        GarbageUserEntity collector = garbageUserDao.findById(sub).get();

        Long provinceId = collector.getProvinceId();
        Long cityId = collector.getCityId();
        Long countryId = collector.getCountryId();
        Long townId = collector.getTownId();
        Long villageId = collector.getVillageId();
        Calendar calendar = Calendar.getInstance();
        Integer year = calendar.get(Calendar.YEAR);
        Integer month = calendar.get(Calendar.MONTH);
        Integer day = calendar.get(Calendar.DAY_OF_MONTH);
        GarbageENoEntity eNoEntity = garbageENoDao.findByENo(eNo);
        if (eNoEntity == null) {
            throw new RuntimeException("该卡没有绑定用户信息");
        }
        GarbageCollectorEntity collectorEntity = new GarbageCollectorEntity();
        collectorEntity.setProvinceId(provinceId.longValue());
        collectorEntity.setCityId(cityId);
        collectorEntity.setCountryId(countryId);
        collectorEntity.setTownId(townId);
        collector.setVillageId(villageId);
        collectorEntity.setCollectDate(new Date().getTime());
        collectorEntity.setDay(day);
        collectorEntity.setMonth(month);
        collectorEntity.setYear(year);
        collectorEntity.seteNo(eNo);
        collectorEntity.setUserId(eNoEntity.getUserId());
        collectorEntity.setCollectorId(sub);
        collectorEntity.setCollectorName(collector.getName());
        collectorEntity.setGarbageFromType(Constants.garbageFromType.TOWN.getType());
        BigDecimal bigDecimal = new BigDecimal(weight);
        weight = bigDecimal.setScale(3,BigDecimal.ROUND_HALF_UP).doubleValue();
        collectorEntity.setGarbageWeight(weight);
        Constants.garbageQuality garbageQuality = null;
        GarbageQualityPointEntity qualityPointEntity = garbageQualityPointDao.findByPlaceIdAndType(villageId, Constants.garbageFromType.TOWN.getType());
        Integer pointScore = 0;
        String desc = "";
        switch (quality){
            case "1":
                garbageQuality = Constants.garbageQuality.QUALIFIED;
                if (qualityPointEntity == null){
                    pointScore = pointScoreForQualified;
                } else {
                    pointScore = qualityPointEntity.getQualified();
                }
                desc = "垃圾分类合格积分";
                break;
            case "2":
                garbageQuality = Constants.garbageQuality.NOTQUALIFIED;
                if (qualityPointEntity == null){
                    pointScore = pointScoreForNoQualified;
                } else {
                    pointScore = qualityPointEntity.getNoQualified();
                }
                desc = "垃圾分类不合格积分";
                break;
            default:
                garbageQuality = Constants.garbageQuality.EMPTY;
                if (qualityPointEntity == null){
                    pointScore = pointScoreForEmpty;
                } else {
                    pointScore = qualityPointEntity.getEmpty();
                }
                desc = "垃圾分类空桶积分";
                break;
        }
        collectorEntity.setGarbageQuality(garbageQuality.getType());
        collectorEntity.setGarbagePoint(pointScore);
        collectorEntity.setCheck(true);
        collectorEntity = garbageCollectorDao.save(collectorEntity);
        GarbageImageEntity imageEntity = garbageImageDao.findById(imageId).get();
        imageEntity.setSourceName(GarbageCollectorEntity.class.getName());
        imageEntity.setBusId(collectorEntity.getId());
        //图片类型为垃圾
        imageEntity.setType(Constants.image.GARBAGE_IMAGE.name());
        imageEntity.setAttribute(0);
        garbageImageDao.save(imageEntity);
        GarbageUserPointEntity userPointEntity = garbageUserPointDao.findByUserId(eNoEntity.getUserId());
        GarbageUserEntity userEntity = garbageUserDao.findById(eNoEntity.getUserId()).get();
        if (userPointEntity != null) {
            userPointEntity.setPoint(userPointEntity.getPoint() + pointScore);
        } else {
            userPointEntity = new GarbageUserPointEntity();
            userPointEntity.setUserId(eNoEntity.getUserId());
            userPointEntity.setPoint(pointScore);
            userPointEntity.setProvinceName(collector.getProvinceName());
            userPointEntity.setCityName(collector.getCityName());
            userPointEntity.setCountryName(collector.getCountryName());
            userPointEntity.setTownName(collector.getTownName());
            userPointEntity.setVillageName(collector.getVillageName());
            userPointEntity.setAddress(userEntity.getAddress());
            userPointEntity.setPhone(userEntity.getPhone());
            userPointEntity.setProvinceId(collectorEntity.getProvinceId().longValue());
            userPointEntity.setCityId(collectorEntity.getCityId());
            userPointEntity.setCountryId(collectorEntity.getCountryId());
            userPointEntity.setTownId(collectorEntity.getTownId());
            userPointEntity.setVillageId(collectorEntity.getVillageId());
            userPointEntity.setCommunityId(collectorEntity.getCommunityId());
        }
        //计算总积分
        garbageUserPointDao.save(userPointEntity);
        //生成积分记录
        GarbagePointRecordEntity recordEntity = new GarbagePointRecordEntity();
        recordEntity.setBusId(collectorEntity.getId());
        recordEntity.setSourceName(GarbageCollectorEntity.class.getName());
        recordEntity.setPoint(pointScore);
        recordEntity.setDesc(desc);
        recordEntity.setUserId(eNoEntity.getUserId());
        garbagePointRecordDao.save(recordEntity);

        ResponseData responseData = new ResponseData();
        responseData.setStatus(Constants.responseStatus.Success.getStatus());
        responseData.setMsg("添加成功");
        logger.info("通过人工环卫车上传垃圾信息成功");
        return responseData;
    }

    @Transactional
    public ResponseData addGarbageByAuto(String eNo, Double weight, Integer imageId, String jwt) {
        Integer sub = jwtUtil.getSubject(jwt);
        //垃圾收集员
        GarbageUserEntity collector = garbageUserDao.findById(sub).get();
        Long provinceId = collector.getProvinceId();
        Long cityId = collector.getCityId();
        Long countryId = collector.getCountryId();
        Long townId = collector.getTownId();
        Long villageId = collector.getVillageId();
        Long communityId = collector.getCommunityId();
        Calendar calendar = Calendar.getInstance();
        Integer year = calendar.get(Calendar.YEAR);
        Integer month = calendar.get(Calendar.MONTH);
        Integer day = calendar.get(Calendar.DAY_OF_MONTH);
        GarbageENoEntity eNoEntity = garbageENoDao.findByENo(eNo);
        if (eNoEntity == null) {
            throw new RuntimeException("该卡没有绑定用户信息");
        }
        GarbageCollectorEntity collectorEntity = new GarbageCollectorEntity();
        collectorEntity.setProvinceId(provinceId);
        collectorEntity.setCityId(cityId);
        collectorEntity.setCountryId(countryId);
        collectorEntity.setTownId(townId);
        collectorEntity.setVillageId(villageId);
        collectorEntity.setCommunityId(communityId);
        collectorEntity.setCollectorName(collector.getCommunityName());
        collectorEntity.setCollectDate(new Date().getTime());
        collectorEntity.setCollectorPhone(collector.getPhone());
        collectorEntity.setDay(day);
        collectorEntity.setMonth(month);
        collectorEntity.setYear(year);
        collectorEntity.seteNo(eNo);
        collectorEntity.setUserId(eNoEntity.getUserId());
        collectorEntity.setCollectorId(sub);
        collectorEntity.setCollectorName(collector.getName());
        collectorEntity.setGarbageFromType(Constants.garbageFromType.COMMUNITY.getType());
        BigDecimal bigDecimal = new BigDecimal(weight);
        weight = bigDecimal.setScale(3,BigDecimal.ROUND_HALF_UP).doubleValue();
        collectorEntity.setGarbageWeight(weight);
        collectorEntity.setCheck(false);
        garbageCollectorDao.save(collectorEntity);
        GarbageImageEntity imageEntity = null;
        try {
            imageEntity = garbageImageDao.findById(imageId).get();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("还没有上传图片，请上传图片");
        }
        imageEntity.setSourceName(GarbageCollectorEntity.class.getName());
        imageEntity.setBusId(collectorEntity.getId());
        //图片类型为垃圾
        imageEntity.setType(Constants.image.GARBAGE_IMAGE.name());
        imageEntity.setAttribute(0);
        garbageImageDao.save(imageEntity);
        ResponseData responseData = new ResponseData();
        responseData.setStatus(Constants.responseStatus.Success.getStatus());
        responseData.setMsg("添加成功");
        logger.info("通过无人值守环卫车上传垃圾信息成功");
        return responseData;
    }

    public ResponsePageData communityGarbageList(Integer pageNo, Integer pageSize, Boolean isCheck, Double weight,
                                             Integer point, Integer quality, String type, String keyWord,
                                             Integer garbageType, String jwt ,String[] orderBys, String startTime, String endTime, Integer communityId) {
        Integer sub = jwtUtil.getSubject(jwt);
        List<GarbageRoleEntity> roleEntityList = garbageRoleDao.findByUserId(sub);
        Boolean flag = true;
        for (GarbageRoleEntity roleEntity:roleEntityList) {
            if (roleEntity.getRoleCode().endsWith("COMMUNITY_REMARK")){
                //评分员可以平分
                flag = true;
            }
            if (roleEntity.getRoleCode().endsWith("COMMUNITY_ADMIN")){
                //小区管理员可以查看
                flag = true;
            }
        }
        if (!flag){
            throw new RuntimeException("没有权限访问");
        }
        Long startDate = 0L;
        Long endDate = 0L;
        if (!StringUtils.isEmpty(startTime) && !StringUtils.isEmpty(endTime)){
             startDate = DateFormatUtil.getFirstTimeOfDay(startTime).getTime();
             endDate = DateFormatUtil.getLastTimeOfDay(endTime).getTime();
        }

        Pageable pageable = PageRequest.of(pageNo - 1, pageSize, getCommunityGarbageSort(orderBys));
        Long finalStartDate = startDate;
        Long finalEndDate = endDate;
        Page<GarbageCollectorEntity> page = garbageCollectorDao.findAll(new Specification<GarbageCollectorEntity>() {
            @Override
            public Predicate toPredicate(Root<GarbageCollectorEntity> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
               List<Predicate> predicateList = new ArrayList<>();
               //小区
               Predicate predicateFrom = criteriaBuilder.equal(root.get("garbageFromType"), Constants.garbageFromType.COMMUNITY.getType());
                predicateList.add(predicateFrom);
                if(!StringUtils.isEmpty(startTime) && !StringUtils.isEmpty(endTime)){
                    Predicate startPredicate = criteriaBuilder.greaterThanOrEqualTo(root.get("collectDate"), finalStartDate);
                    Predicate endPredicate = criteriaBuilder.lessThanOrEqualTo(root.get("collectDate"), finalEndDate);
                    predicateList.add(startPredicate);
                    predicateList.add(endPredicate);

                }
                if (communityId !=null && communityId !=0){
                    Predicate communityIdPredicate = criteriaBuilder.equal(root.get("communityId"), communityId);
                    predicateList.add(communityIdPredicate);
                }

                if (isCheck){
                   Predicate isCheckPredicate = criteriaBuilder.isTrue(root.get("check"));
                   predicateList.add(isCheckPredicate);
               } else {
                   Predicate isCheckPredicate = criteriaBuilder.isFalse(root.get("check"));
                   predicateList.add(isCheckPredicate);
               }
                if (weight != null) {
                    Predicate weightPredicate = criteriaBuilder.greaterThanOrEqualTo(root.get("garbageWeight"), weight);
                    predicateList.add(weightPredicate);
                }
                if (point != null) {
                    Predicate pointPredicate = criteriaBuilder.greaterThanOrEqualTo(root.get("garbagePoint"), point);
                    predicateList.add(pointPredicate);
                }
                if (garbageType!= null && garbageType != 0){
                    //厨余垃圾--1    其他垃圾--2  所有-- 0
                    Predicate garbageTypePredicate = criteriaBuilder.greaterThanOrEqualTo(root.get("garbageType"), garbageType);
                    predicateList.add(garbageTypePredicate);
                }
                Constants.garbageQuality garbageQuality  = null;
                switch (quality){
                    case 1:
                        garbageQuality = Constants.garbageQuality.QUALIFIED;
                        break;
                    case 2:
                        garbageQuality = Constants.garbageQuality.NOTQUALIFIED;
                        break;
                    case 3:
                        garbageQuality = Constants.garbageQuality.EMPTY;
                        break;
                    default:
                        garbageQuality = null;
                        break;
                }
                if (garbageQuality != null) {
                    Predicate qualityPredicate = criteriaBuilder.equal(root.get("garbageQuality"), garbageQuality.getType());
                    predicateList.add(qualityPredicate);
                }
                if (!StringUtils.isEmpty(type) && "eNo".equals(type) && !StringUtils.isEmpty(keyWord)){
                    Predicate eNoPredicate = criteriaBuilder.equal(root.get("eNo"), keyWord);
                    predicateList.add(eNoPredicate);
                }
                if (!StringUtils.isEmpty(keyWord) && "name".equals(type) || "phone".equals(type) ){
                    List<Predicate> predicates = new ArrayList<>();
                    Subquery subquery = criteriaQuery.subquery(GarbageUserEntity.class);
                    Root subRoot = subquery.from(GarbageUserEntity.class);
                    subquery.select(subRoot.get("id"));
                    Predicate predicate = criteriaBuilder.equal(root.get("userId"), subRoot.get("id"));
                    predicates.add(predicate);
                    if ("name".equals(type)){
                        Predicate namePredicate  = criteriaBuilder.like(subRoot.get("name"), "%" + keyWord + "%");
                        predicates.add(namePredicate);
                    }
                    if ("phone".equals(type)){
                        Predicate phonePredicate  = criteriaBuilder.like(subRoot.get("phone"), "%" + keyWord + "%");
                        predicates.add(phonePredicate);
                    }
                    Predicate exists = criteriaBuilder.exists(subquery.where(predicates.toArray(new Predicate[predicates.size()])));
                    predicateList.add(exists);
                }

                return criteriaBuilder.and(predicateList.toArray(new Predicate[predicateList.size()]));
            }
        }, pageable);
        List<Integer> userIds = page.getContent().stream().map(garbageCollectorEntity -> garbageCollectorEntity.getUserId()).collect(Collectors.toList());
        List<UserDto> userDtos = new ArrayList<>();
        if (userIds.size() > 0){
            userDtos = garbageUserDao.selectUserInfoByIdIn(userIds);
        }
        List<Long> communityIds = page.getContent().stream().map(garbageCollectorEntity -> garbageCollectorEntity.getCommunityId()).distinct().collect(Collectors.toList());
        Map<Integer, String> userMap = userDtos.stream().collect(Collectors.toMap(UserDto::getUserId, UserDto::getUsername));
        List<CommunityGarbageCollectDto> dtos = new ArrayList<>();
        List<GarbageCommunityEntity> communityEntities = garbageCommunityDao.findAll(new Specification<GarbageCommunityEntity>() {
            @Nullable
            @Override
            public Predicate toPredicate(Root<GarbageCommunityEntity> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList<>();
                if (communityIds.size() > 0){
                    Predicate predicate = root.get("id").in(communityIds);
                    predicates.add(predicate);
                }
                return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        });
        Map<Integer, String> communityMap = communityEntities.stream().collect(Collectors.toMap(GarbageCommunityEntity::getId, GarbageCommunityEntity::getCommunityName));
        List<GarbageImageEntity> imageEntityList = garbageImageDao.findBySourceNameAndType(GarbageCollectorEntity.class.getName(), Constants.image.GARBAGE_IMAGE.name());
        Map<Integer, String> imagePathMap = imageEntityList.stream().collect(Collectors.toMap(GarbageImageEntity::getBusId, GarbageImageEntity::getImagePath));
        page.getContent().stream().forEach(garbageCollectorEntity -> {
            CommunityGarbageCollectDto dto = new CommunityGarbageCollectDto();
            dto.setId(garbageCollectorEntity.getId());
            dto.setUserId(garbageCollectorEntity.getUserId());
            dto.setUsername(userMap.get(garbageCollectorEntity.getUserId()));
            dto.setType(garbageCollectorEntity.getGarbageFromType() == 0?"农村":"小区");
            dto.setAddress(communityMap.get(garbageCollectorEntity.getCommunityId()));
            String qualityString = "";
            String garbgetTypeString = "";
            if (isCheck){
                if(garbageCollectorEntity.getGarbageQuality() == 1){
                    qualityString = "合格";
                } else if(garbageCollectorEntity.getGarbageQuality() == 2){
                    qualityString = "不合格";
                } else {
                    qualityString = "空桶";
                }
                if (garbageCollectorEntity.getGarbageType() == 1){
                    garbgetTypeString = "厨余垃圾";
                } else {
                    garbgetTypeString = "其他垃圾";
                }
            }
            dto.setQualityType(qualityString);
            dto.setGarbageType(garbgetTypeString);
            dto.setPoint(garbageCollectorEntity.getGarbagePoint());
            dto.setWeight(garbageCollectorEntity.getGarbageWeight());
            dto.setCollectorId(garbageCollectorEntity.getCollectorId());
            dto.setCollectorName(garbageCollectorEntity.getCollectorName());
            dto.setCollectDate(DateFormatUtil.formatDate(new Date(garbageCollectorEntity.getCollectDate()), "yyyy-MM-dd"));
            dto.setImage(imagePathMap.get(garbageCollectorEntity.getId()));
            dto.setCheck(garbageCollectorEntity.getCheck()?"已评分":"未评分");
            dtos.add(dto);
        });
        ResponsePageData responsePageData = new ResponsePageData();
        responsePageData.setPageNo(pageNo);
        responsePageData.setPageSize(pageSize);
        responsePageData.setCount(page.getTotalPages());
        responsePageData.setTotalElement(page.getTotalElements());
        responsePageData.setLastPage(page.isLast());
        responsePageData.setFirstPage(page.isFirst());
        responsePageData.setData(dtos);
        responsePageData.setStatus(Constants.responseStatus.Success.getStatus());
        responsePageData.setMsg("列表查询成功");
        return responsePageData;
    }
    private Sort getCommunityGarbageSort(String[] orderBys){
        Sort sort = null;
        if (orderBys == null || orderBys.length == 0 ){
            sort = Sort.by("id").descending();
        }else {
            sort =   Sort.by(Arrays.stream(orderBys).map((it) -> {
                String[] items = it.split(";");
                String property = "";
                Sort.Direction direction = null;
                if (items.length > 1){
                    property = items[0];
                    switch (property){
                        case "garbageweight":
                            property = "garbageWeight";
                            break;
                        case "garbagepoint":
                            property = "garbagePoint";
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

    @Transactional
    public ResponseData remarkCommunityGarbage(Integer id, Integer quality,Integer garbageType, String jwt) {
        Integer sub = jwtUtil.getSubject(jwt);
        //评分员
        GarbageUserEntity userEntity = garbageUserDao.findById(sub).get();
        List<GarbageRoleEntity> roleEntities = userEntity.getRoles().stream().collect(Collectors.toList());
        Boolean flag = false;
        for (GarbageRoleEntity roleEntity:roleEntities) {
            if (roleEntity.getRoleCode().endsWith("COMMUNITY_REMARK")){
                //评分员可以平分
                flag = true;
            }
            if (roleEntity.getRoleCode().endsWith("COMMUNITY_ADMIN")){
                //小区管理员可以查看
                flag = true;
            }
        }
        if (!flag){
            throw new RuntimeException("没有权限访问");
        }
        Long communityId = userEntity.getCommunityId();
        GarbageQualityPointEntity qualityPointEntity = garbageQualityPointDao.findByPlaceIdAndType(communityId.longValue(), Constants.garbageFromType.COMMUNITY.getType());
        Integer pointScore = 0;
        String desc = "";
        Constants.garbageQuality garbageQuality = null;
        switch (quality){
            case 1:
                garbageQuality = Constants.garbageQuality.QUALIFIED;
                if (qualityPointEntity == null){
                    pointScore = pointScoreForQualified;
                } else {
                    pointScore = qualityPointEntity.getQualified();
                }
                desc = "垃圾分类人工审核合格积分";
                break;
            case 2:
                garbageQuality = Constants.garbageQuality.NOTQUALIFIED;
                if (qualityPointEntity == null){
                    pointScore = pointScoreForNoQualified;
                } else {
                    pointScore = qualityPointEntity.getNoQualified();
                }
                desc = "垃圾分类人工审核不合格积分";
                break;
            default:
                garbageQuality = Constants.garbageQuality.EMPTY;
                if (qualityPointEntity == null){
                    pointScore = pointScoreForEmpty;
                } else {
                    pointScore = qualityPointEntity.getEmpty();
                }
                desc = "垃圾分类人工审核空桶积分";
                break;
        }
        GarbageCollectorEntity collectorEntity = garbageCollectorDao.findById(id).get();
        collectorEntity.setGarbageQuality(garbageQuality.getType());
        collectorEntity.setGarbagePoint(pointScore);
        collectorEntity.setGarbageType(garbageType);
        collectorEntity.setCheck(true);
        garbageCollectorDao.save(collectorEntity);
        GarbageUserEntity garbageUserEntity = garbageUserDao.findById(collectorEntity.getUserId()).get();
        GarbageCommunityEntity communityEntity = garbageCommunityDao.findById(collectorEntity.getCommunityId().intValue()).get();
        GarbageUserPointEntity userPointEntity = garbageUserPointDao.findByUserId(collectorEntity.getUserId());
        if (userPointEntity != null) {
            userPointEntity.setPoint(userPointEntity.getPoint() + pointScore);
        } else {
            userPointEntity = new GarbageUserPointEntity();
            userPointEntity.setUserId(collectorEntity.getUserId());
            userPointEntity.setUserName(garbageUserEntity.getName());
            userPointEntity.setPoint(pointScore);
            userPointEntity.setProvinceName(userEntity.getProvinceName());
            userPointEntity.setCityName(userEntity.getCityName());
            userPointEntity.setCountryName(userEntity.getCountryName());
            userPointEntity.setTownName(userEntity.getTownName());
            userPointEntity.setVillageName(userEntity.getVillageName());
            userPointEntity.setAddress(userEntity.getAddress());
            userPointEntity.setPhone(garbageUserEntity.getPhone());
            userPointEntity.setProvinceId(collectorEntity.getProvinceId());
            userPointEntity.setCityId(collectorEntity.getCityId());
            userPointEntity.setCountryId(collectorEntity.getCountryId());
            userPointEntity.setTownId(collectorEntity.getTownId());
            userPointEntity.setVillageId(collectorEntity.getVillageId());
            userPointEntity.setCommunityId(collectorEntity.getCommunityId());
            userPointEntity.setCommunityName(communityEntity.getCommunityName());
        }
        //计算总积分
        garbageUserPointDao.save(userPointEntity);

        //生成积分记录
        GarbagePointRecordEntity recordEntity = new GarbagePointRecordEntity();
        recordEntity.setBusId(collectorEntity.getId());
        recordEntity.setSourceName(GarbageCollectorEntity.class.getName());
        recordEntity.setPoint(pointScore);
        recordEntity.setDesc(desc);
        recordEntity.setUserId(collectorEntity.getUserId());
        garbagePointRecordDao.save(recordEntity);

        ResponseData responseData = new ResponseData();
        responseData.setStatus(Constants.responseStatus.Success.getStatus());
        responseData.setMsg("垃圾分类质量判定成功");
        logger.info("垃圾分类质量判定成功");
        return responseData;
    }

    @Transactional
    public ResponseData getGarbageCollectSummaryInfo(Integer pageNo, Integer pageSize, String startTime, String endTime,
                                                     String type, String phone, String name, String jwt, String[] orderBys,
                                                     Long cityId, Long countryId, Long townId, Long villageId, Long communityId) {
        Integer sub = jwtUtil.getSubject(jwt);
        GarbageUserEntity userEntity = garbageUserDao.findById(sub).get();
        Integer fromType = userEntity.getFromType();
        Long start = 0L;
        Long end = 0L;
        if ("day".equals(type)){
            start = DateFormatUtil.parse(startTime,"yyyy-MM-dd").getTime();
            end = DateFormatUtil.getLastTimeOfDay(endTime).getTime();
        } else if ("month".equals(type)){
            start = DateFormatUtil.getFirstDateOfMonth(startTime).getTime();
            end = DateFormatUtil.getLastDateOfMonth(endTime).getTime();
        }else if ("year".equals(type)){
            start = DateFormatUtil.getFirstDayOfYear(startTime).getTime();
            end = DateFormatUtil.getLastDayOfYear(endTime).getTime();
        }else {
            //其他
        }
        List<GarbageRoleEntity> roleEntityList =userEntity.getRoles().stream().collect(Collectors.toList());

        List<String> roles = roleEntityList.stream().map(garbageRoleEntity -> garbageRoleEntity.getRoleCode()).collect(Collectors.toList());
        List<Integer> communityIds = getCommunityResource(roleEntityList);
        Pageable pageable = PageRequest.of(pageNo - 1, pageSize, getGarbageCollectorSummaryInfo(orderBys));
        Long finalStart = start;
        Long finalEnd = end;

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<CollectorDto> criteriaQuery = criteriaBuilder.createQuery(CollectorDto.class);
        Root<GarbageCollectorEntity> root = criteriaQuery.from(GarbageCollectorEntity.class);
        criteriaQuery.multiselect(root.get("collectorId"),root.get("collectorName"), root.get("collectorPhone"),root.get("collectDate"),
                criteriaBuilder.count(root.get("userId")), criteriaBuilder.sum(root.get("garbageWeight")), root.get("day"),
                root.get("month"), root.get("year"));
        List<Predicate> predicates = new ArrayList<>();
        List<Expression<?>> expressions = new ArrayList<>();
        expressions.add(root.get("collectorId"));
        predicates.add(criteriaBuilder.equal(root.get("garbageFromType"), fromType));
        if (!StringUtils.isEmpty(name)){
            Predicate pre = criteriaBuilder.like(root.get("collectorName"), "%" + name +"%");
            predicates.add(pre);
        }
        if (!StringUtils.isEmpty(phone)){
            Predicate predicate = criteriaBuilder.like(root.get("collectorPhone"), "%" + phone +"%");
            predicates.add(predicate);
        }
        if (!StringUtils.isEmpty(startTime)&& !StringUtils.isEmpty(endTime)){
            Predicate predicate = criteriaBuilder.and(criteriaBuilder.greaterThanOrEqualTo(root.<Long>get("collectDate"), finalStart),
                    criteriaBuilder.lessThanOrEqualTo(root.<Long>get("collectDate"), finalEnd));
            predicates.add(predicate);
        }
        if (fromType == 1){
            if (communityId != null ){
                Predicate predicate = criteriaBuilder.equal(root.get("communityId"), communityId);
                predicates.add(predicate);
                expressions.add(root.get("communityId"));
            } else {
                if (communityIds.size() > 0){
                    Predicate predicate = root.get("communityId").in(communityIds);
                    predicates.add(predicate);
                    expressions.add(root.get("communityId"));
                }
            }


        } else {
            if (roles.contains("VILLAGE_ADMIN")){
                Predicate predicate = criteriaBuilder.equal(root.get("villageId"), userEntity.getVillageId());
                predicates.add(predicate);
                expressions.add(root.get("villageId"));
            }
            if (roles.contains("TOWN_ADMIN")){
                if (villageId!=null){
                    Predicate predicate1 = criteriaBuilder.equal(root.get("villageId"), villageId);
                    predicates.add(predicate1);
                    expressions.add(root.get("villageId"));
                } else {
                    Predicate predicate = criteriaBuilder.equal(root.get("townId"), userEntity.getTownId());
                    predicates.add(predicate);
                    expressions.add(root.get("townId"));
                }
            }
            if (roles.contains("COUNTRY_ADMIN")){
                if (townId!=null){
                    Predicate predicate = criteriaBuilder.equal(root.get("townId"), townId);
                    predicates.add(predicate);
                    expressions.add(root.get("townId"));
                } else if (townId!=null && villageId!=null && villageId !=0){
                    Predicate predicate = criteriaBuilder.equal(root.get("villageId"), villageId);
                    predicates.add(predicate);
                    expressions.add(root.get("villageId"));
                }else {
                    Predicate pre = criteriaBuilder.equal(root.get("countryId"), userEntity.getCountryId());
                    predicates.add(pre);
                    expressions.add(root.get("countryId"));
                }
            }

            if (roles.contains("CITY_ADMIN")){
                if (countryId!=null){
                    Predicate predicate1 = criteriaBuilder.equal(root.get("countryId"), countryId);
                    predicates.add(predicate1);
                    expressions.add(root.get("countryId"));
                } else if (countryId!=null && townId!=null){
                    Predicate predicate1 = criteriaBuilder.equal(root.get("townId"), townId);
                    predicates.add(predicate1);
                    expressions.add(root.get("townId"));
                } else if (countryId!=null && townId!=null && villageId!=null && villageId !=0){
                    Predicate predicate1 = criteriaBuilder.equal(root.get("villageId"), villageId);
                    predicates.add(predicate1);
                    expressions.add(root.get("villageId"));
                } else {
                    Predicate predicate =criteriaBuilder.equal(root.get("cityId"), userEntity.getCityId());
                    predicates.add(predicate);
                    expressions.add(root.get("cityId"));
                }
            }
            if (roles.contains("PROVINCE_ADMIN")){
                if (cityId != null && cityId !=0){
                    Predicate predicate1 = criteriaBuilder.equal(root.get("cityId"), cityId);
                    predicates.add(predicate1);
                    expressions.add(root.get("cityId"));
                } else if (cityId != null && countryId!=null && countryId !=0){
                    Predicate predicate1 = criteriaBuilder.equal(root.get("countryId"), countryId);
                    predicates.add(predicate1);
                    expressions.add(root.get("countryId"));
                } else if (cityId != null && countryId!=null && townId!=null && townId !=0){
                    Predicate predicate1 = criteriaBuilder.equal(root.get("townId"), townId);
                    predicates.add(predicate1);
                    expressions.add(root.get("townId"));
                } else if (cityId != null && countryId!=null && townId!=null && villageId!=null && villageId !=0){
                    Predicate predicate1 = criteriaBuilder.equal(root.get("villageId"), villageId);
                    predicates.add(predicate1);
                    expressions.add(root.get("villageId"));
                } else {
                    Predicate predicate = criteriaBuilder.equal(root.get("provinceId"), userEntity.getProvinceId());
                    predicates.add(predicate);
                    expressions.add(root.get("provinceId"));
                }
            }
        }
        criteriaQuery.where(predicates.toArray(new Predicate[predicates.size()]));
        if ("day".equals(type)){
            expressions.add(root.get("day"));
            expressions.add(root.get("month"));
            expressions.add(root.get("year"));
        } else if ("month".equals(type)){
            expressions.add(root.get("month"));
            expressions.add( root.get("year"));
        }else{
            expressions.add(root.get("year"));
        }
        criteriaQuery.groupBy(expressions);
        TypedQuery<CollectorDto> createQuery = entityManager.createQuery(criteriaQuery);
        createQuery.setFirstResult(pageNo*pageSize);
        createQuery.setMaxResults(pageSize);
        List<CollectorDto> counts = entityManager.createQuery(criteriaQuery).getResultList();
        PageImpl<CollectorDto> page = new PageImpl<>(counts, pageable, Long.valueOf(counts.size()));
        List<Integer> collectorIds = page.getContent().stream().map(dto->dto.getCollectorId()).collect(Collectors.toList());
        List<UserVillageDto> villageDtos = new ArrayList<>();
        if (collectorIds.size() > 0){
            villageDtos  = garbageUserDao.getUserVillageNameByIds(collectorIds);
        }

        Map<Integer, String> map = villageDtos.stream().collect(Collectors.toMap(UserVillageDto::getId, UserVillageDto::getVillageName));
        for (CollectorDto dto:page.getContent()) {
            dto.setAddress(map.get(dto.getCollectorId()));
            if ("day".equals(type)){
                dto.setCollectDate(dto.getYear() + "-" + dto.getMonth() + "-" + dto.getDay());
            }else if ("month".equals(type)){
                dto.setCollectDate(dto.getYear() + "-" + dto.getMonth());
            } else {
                dto.setCollectDate(dto.getYear() + "");
            }
        }
        ResponsePageData responseData = new ResponsePageData();
        responseData.setData(page.getContent());
        responseData.setStatus(Constants.responseStatus.Success.getStatus());
        responseData.setFirstPage(page.isFirst());
        responseData.setLastPage(page.isLast());
        responseData.setCount(page.getTotalPages());
        responseData.setTotalElement(page.getTotalElements());
        responseData.setPageSize(pageSize);
        responseData.setPageNo(pageNo);
        responseData.setMsg("数据统计信息查询成功");
        return responseData;
    }

    private Sort getGarbageCollectorSummaryInfo(String[] orderBys){
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


    public ResponseData getGarbageCollectSummaryInfoInPlace(Integer pageNo, Integer pageSize, String startTime, String endTime, String type,  Long cityId, Long countryId, Long townId, Long villageId, Integer communityId, String[] orderBys, String jwt) {
        Integer sub = jwtUtil.getSubject(jwt);
        GarbageUserEntity userEntity = garbageUserDao.findById(sub).get();
        Integer fromType = userEntity.getFromType();// 0农村  1城市
        Long start = 0L;
        Long end = 0L;
        if ("day".equals(type)){
            start = DateFormatUtil.getFirstTimeOfDay(startTime).getTime();
            end = DateFormatUtil.getFirstTimeOfDay(endTime).getTime();
        } else if ("month".equals(type)){
            start = DateFormatUtil.getFirstDateOfMonth(startTime).getTime();
            end = DateFormatUtil.getLastDateOfMonth(endTime).getTime();
        }else if ("year".equals(type)){
            start = DateFormatUtil.getFirstDayOfYear(startTime).getTime();
            end = DateFormatUtil.getLastDayOfYear(endTime).getTime();
        }else {
            //其他
        }
        List<GarbageRoleEntity> roleEntityList =userEntity.getRoles().stream().collect(Collectors.toList());
        List<String> roles = roleEntityList.stream().map(garbageRoleEntity -> garbageRoleEntity.getRoleCode()).collect(Collectors.toList());
        //用户数
        Long provinceId = userEntity.getProvinceId();
        List<Integer> communityIds = getCommunityResource(roleEntityList);
        Long userCount = garbageUserDao.count(new Specification<GarbageUserEntity>() {
            @Nullable
            @Override
            public Predicate toPredicate(Root<GarbageUserEntity> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList<>();
                if (fromType == 1){
                    //小区
                    if (communityId !=null){
                        Predicate predicate = criteriaBuilder.equal(root.get("communityId"), communityId);
                        predicates.add(predicate);
                    } else {
                        if (communityIds.size() > 0){
                            Predicate predicate =  root.get("communityId").in(communityIds);
                            predicates.add(predicate);
                        }
                    }

                }else{
                    if (roles.contains("VILLAGE_ADMIN")){
                        Predicate predicateVillageId= criteriaBuilder.equal(root.get("villageId"), userEntity.getVillageId());
                        predicates.add(predicateVillageId);
                    }
                    if (roles.contains("TOWN_ADMIN")){
                        if (villageId !=null && villageId != 0 ){
                            Predicate predicateVillageId= criteriaBuilder.equal(root.get("villageId"), villageId);
                            predicates.add(predicateVillageId);
                        } else {
                            Predicate predicateTownId= criteriaBuilder.equal(root.get("townId"), userEntity.getTownId());
                            predicates.add(predicateTownId);
                        }
                    }
                    if (roles.contains("COUNTRY_ADMIN")){
                        if (townId !=null ){
                            Predicate predicateTownId= criteriaBuilder.equal(root.get("townId"), townId);
                            predicates.add(predicateTownId);
                        } else if (townId !=null && villageId !=null && villageId != 0 ){
                            Predicate predicateTownId= criteriaBuilder.equal(root.get("villageId"), townId);
                            predicates.add(predicateTownId);
                        } else {
                            Predicate predicateCountryId = criteriaBuilder.equal(root.get("districtId"), userEntity.getCountryId());
                            predicates.add(predicateCountryId);
                        }
                    }
                    if (roles.contains("CITY_ADMIN")){
                        if (countryId !=null && countryId != 0){
                            Predicate predicateCountryId = criteriaBuilder.equal(root.get("districtId"), countryId);
                            predicates.add(predicateCountryId);
                        } else if (countryId !=null && townId !=null && townId != 0 ){
                            Predicate predicateTownId= criteriaBuilder.equal(root.get("townId"), townId);
                            predicates.add(predicateTownId);
                        } else if (countryId !=null && townId !=null && villageId !=null && villageId != 0 ){
                            Predicate predicateTownId= criteriaBuilder.equal(root.get("villageId"), villageId);
                            predicates.add(predicateTownId);
                        } else {
                            Predicate predicateCityId = criteriaBuilder.equal(root.get("cityId"), userEntity.getCityId());
                            predicates.add(predicateCityId);
                        }
                    }
                    if (roles.contains("PROVINCE_ADMIN")){
                        if (cityId !=null && cityId != 0){
                            Predicate predicateCityId = criteriaBuilder.equal(root.get("cityId"), cityId);
                            predicates.add(predicateCityId);
                        } else if (cityId !=null && countryId !=null && countryId != 0){
                            Predicate predicateCountryId = criteriaBuilder.equal(root.get("districtId"), countryId);
                            predicates.add(predicateCountryId);
                        } else if (cityId !=null && countryId !=null && townId !=null && townId != 0 ){
                            Predicate predicateTownId= criteriaBuilder.equal(root.get("townId"), townId);
                            predicates.add(predicateTownId);
                        } else if (cityId !=null && countryId !=null && townId !=null && villageId !=null && villageId != 0 ){
                            Predicate predicateVillage= criteriaBuilder.equal(root.get("villageId"), villageId);
                            predicates.add(predicateVillage);
                        } else {
                            Predicate predicateProvinceId = criteriaBuilder.equal(root.get("provinceId"), userEntity.getProvinceId());
                            predicates.add(predicateProvinceId);
                        }
                    }
                }
                return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        });
        final Long finalStart = start;
        final Long finalEnd = end;

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<GarbageCollectCountDto> criteriaQuery = criteriaBuilder.createQuery(GarbageCollectCountDto.class);
        Root<GarbageCollectorEntity> root = criteriaQuery.from(GarbageCollectorEntity.class);
        if (roles.contains("PROVINCE_ADMIN")){
            if (cityId != null){
                criteriaQuery.multiselect( root.get("cityId"),criteriaBuilder.count(root.get("garbageQuality")), root.get("garbageQuality"), root.get("day"), root.get("month"), root.get("year"));
            }
            else if (cityId != null &&countryId !=null){
                criteriaQuery.multiselect( root.get("countryId"),criteriaBuilder.count(root.get("garbageQuality")), root.get("garbageQuality"), root.get("day"), root.get("month"), root.get("year"));
            }
            else if (cityId !=null && countryId !=null && townId != null ){
                criteriaQuery.multiselect( root.get("townId"),criteriaBuilder.count(root.get("garbageQuality")), root.get("garbageQuality"), root.get("day"), root.get("month"), root.get("year"));
            }
            else if (cityId !=null && countryId !=null && townId !=null && villageId != null){
                criteriaQuery.multiselect( root.get("villageId"),criteriaBuilder.count(root.get("garbageQuality")), root.get("garbageQuality"), root.get("day"), root.get("month"), root.get("year"));
            } else {
                criteriaQuery.multiselect( root.get("provinceId"),criteriaBuilder.count(root.get("garbageQuality")), root.get("garbageQuality"), root.get("day"), root.get("month"), root.get("year"));
            }
        }
        if (roles.contains("CITY_ADMIN")){
            if (countryId != null){
                criteriaQuery.multiselect( root.get("countryId"),criteriaBuilder.count(root.get("garbageQuality")), root.get("garbageQuality"), root.get("day"), root.get("month"), root.get("year"));

            } else if (countryId != null && townId !=null){
                criteriaQuery.multiselect( root.get("townId"),criteriaBuilder.count(root.get("garbageQuality")), root.get("garbageQuality"), root.get("day"), root.get("month"), root.get("year"));
            } else if (countryId !=null && townId != null &&villageId !=null ){
                criteriaQuery.multiselect( root.get("townId"),criteriaBuilder.count(root.get("garbageQuality")), root.get("garbageQuality"), root.get("day"), root.get("month"), root.get("year"));
            } else{
                criteriaQuery.multiselect( root.get("cityId"),criteriaBuilder.count(root.get("garbageQuality")), root.get("garbageQuality"), root.get("day"), root.get("month"), root.get("year"));
            }
        }
        if (roles.contains("COUNTRY_ADMIN")){
            if (townId != null ){
                criteriaQuery.multiselect( root.get("townId"),criteriaBuilder.count(root.get("garbageQuality")), root.get("garbageQuality"), root.get("day"), root.get("month"), root.get("year"));

            } else if (townId != null && villageId !=null){
                criteriaQuery.multiselect( root.get("villageId"),criteriaBuilder.count(root.get("garbageQuality")), root.get("garbageQuality"), root.get("day"), root.get("month"), root.get("year"));

            } else {
                criteriaQuery.multiselect( root.get("countryId"),criteriaBuilder.count(root.get("garbageQuality")), root.get("garbageQuality"), root.get("day"), root.get("month"), root.get("year"));
            }
        }
        if (roles.contains("TOWN_ADMIN")){
            if (villageId != null){
                criteriaQuery.multiselect( root.get("villageId"),criteriaBuilder.count(root.get("garbageQuality")), root.get("garbageQuality"), root.get("day"), root.get("month"), root.get("year"));
            } else {
                criteriaQuery.multiselect( root.get("townId"),criteriaBuilder.count(root.get("garbageQuality")), root.get("garbageQuality"), root.get("day"), root.get("month"), root.get("year"));
            }
        }
        if (roles.contains("VILLAGE_ADMIN")){
            criteriaQuery.multiselect( root.get("villageId"),criteriaBuilder.count(root.get("garbageQuality")), root.get("garbageQuality"), root.get("day"), root.get("month"), root.get("year"));
        }
        if (fromType == 1){
            criteriaQuery.multiselect( root.get("communityId"),criteriaBuilder.count(root.get("garbageQuality")), root.get("garbageQuality"), root.get("day"), root.get("month"), root.get("year"));
        }

        List<Predicate> predicateList = new ArrayList<>();
        if (!StringUtils.isEmpty(startTime)&& !StringUtils.isEmpty(endTime)){
            Predicate startPredicate = criteriaBuilder.greaterThanOrEqualTo(root.<Long>get("collectDate"), start);
            Predicate endPredicate = criteriaBuilder.lessThanOrEqualTo(root.<Long>get("collectDate"), end);
            predicateList.add(startPredicate);
            predicateList.add(endPredicate);
        }
        predicateList.add(criteriaBuilder.isTrue(root.get("check")));
        if (fromType == 1){
            if (communityId != null ){
                Predicate predicate = criteriaBuilder.equal(root.get("communityId"), communityId);
                predicateList.add(predicate);
            } else {
                if (communityIds.size() > 0){
                    Predicate predicate =  root.get("communityId").in(communityIds);
                    predicateList.add(predicate);
                }
            }
            Predicate predicate = criteriaBuilder.equal(root.get("garbageFromType"), Constants.garbageFromType.COMMUNITY.getType());
            predicateList.add(predicate);
        } else {
            Predicate predicateType =  criteriaBuilder.equal(root.get("garbageFromType"), Constants.garbageFromType.TOWN.getType());
            predicateList.add(predicateType);
            if (roles.contains("VILLAGE_ADMIN")){
                Predicate predicate = criteriaBuilder.equal(root.get("villageId"), userEntity.getVillageId());
                predicateList.add(predicate);
            }
            if (roles.contains("TOWN_ADMIN")){
                Predicate predicate = criteriaBuilder.equal(root.get("townId"), userEntity.getTownId());
                predicateList.add(predicate);
                if (villageId!=null && villageId !=0){
                    Predicate predicate1 = criteriaBuilder.equal(root.get("villageId"), villageId);
                    predicateList.add(predicate1);
                }
            }
            if (roles.contains("COUNTRY_ADMIN")){
                Predicate predicate =  criteriaBuilder.equal(root.get("countryId"), userEntity.getCountryId());
                predicateList.add(predicate);
                if (townId!=null && townId !=0){
                    Predicate predicateTown =  criteriaBuilder.equal(root.get("townId"), townId);
                    predicateList.add(predicateTown);
                }
                if (villageId!=null && villageId !=0){
                    Predicate predicateVillage = criteriaBuilder.equal(root.get("villageId"), villageId);
                    predicateList.add(predicateVillage);
                }
            }

            if (roles.contains("CITY_ADMIN")){
                if (countryId!=null &&  countryId !=0){
                    Predicate predicateCountry = criteriaBuilder.equal(root.get("countryId"), countryId);
                    predicateList.add(predicateCountry);
                } else if (countryId!=null && townId!=null && townId !=0){
                    Predicate predicateTown = criteriaBuilder.equal(root.get("townId"), townId);
                    predicateList.add(predicateTown);
                } else if (countryId!=null && townId!=null && villageId!=null && villageId !=0){
                    Predicate predicateVillage = criteriaBuilder.equal(root.get("villageId"), villageId);
                    predicateList.add(predicateVillage);
                } else {
                    Predicate predicate = criteriaBuilder.equal(root.get("cityId"), userEntity.getCityId());
                    predicateList.add(predicate);
                }
            }
            if (roles.contains("PROVINCE_ADMIN")){
                if (cityId != null && cityId !=0){
                    Predicate predicateCity =criteriaBuilder.equal(root.get("cityId"), cityId);
                    predicateList.add(predicateCity);
                } else if (cityId != null && countryId!=null && countryId !=0){
                    Predicate predicateCountry =criteriaBuilder.equal(root.get("countryId"), countryId);
                    predicateList.add(predicateCountry);
                } else if (cityId != null && countryId!=null && townId!=null && townId !=0){
                    Predicate predicateTown =criteriaBuilder.equal(root.get("townId"), townId);
                    predicateList.add(predicateTown);
                } else if ( cityId != null && countryId!=null && townId!=null && villageId!=null && villageId !=0){
                    Predicate predicateVillage =criteriaBuilder.equal(root.get("villageId"), villageId);
                    predicateList.add(predicateVillage);
                } else {
                    Predicate predicate =criteriaBuilder.equal(root.get("provinceId"), userEntity.getProvinceId());
                    predicateList.add(predicate);
                }
            }
        }
        if (fromType == 1){
            //小区
            criteriaQuery.where(predicateList.toArray(new Predicate[predicateList.size()]));
            if ("day".equals(type)){
                criteriaQuery.groupBy(root.get("garbageQuality"), root.get("day"), root.get("month"), root.get("year"),root.get("communityId"));
            }
            if ("month".equals(type)){
                criteriaQuery.groupBy(root.get("garbageQuality"), root.get("month"), root.get("year"),root.get("communityId"));
            }
            if ("year".equals(type)){
                criteriaQuery.groupBy(root.get("garbageQuality"), root.get("year"),root.get("communityId"));
            }
        } else {
            //农村
            criteriaQuery.where(predicateList.toArray(new Predicate[predicateList.size()]));
            if ("day".equals(type)){
                if (roles.contains("PROVINCE_ADMIN")){
                    criteriaQuery.groupBy(root.get("garbageQuality"), root.get("day"), root.get("month"), root.get("year"), root.get("provinceId"));
                }
                if (roles.contains("CITY_ADMIN")){
                    criteriaQuery.groupBy(root.get("garbageQuality"), root.get("day"), root.get("month"), root.get("year"),root.get("provinceId"), root.get("cityId"));
                }
                if (roles.contains("COUNTRY_ADMIN")){
                    criteriaQuery.groupBy(root.get("garbageQuality"), root.get("day"), root.get("month"), root.get("year"),root.get("provinceId"), root.get("cityId"), root.get("countryId"));
                }
                if (roles.contains("TOWN_ADMIN")){
                    criteriaQuery.groupBy(root.get("garbageQuality"), root.get("day"), root.get("month"), root.get("year"),root.get("provinceId"), root.get("cityId"), root.get("countryId"), root.get("townId"));
                }
                if (roles.contains("VILLAGE_ADMIN")){
                    criteriaQuery.groupBy(root.get("garbageQuality"), root.get("day"), root.get("month"), root.get("year"),root.get("provinceId"), root.get("cityId"), root.get("countryId"), root.get("townId"), root.get("villageId"));
                }

            } else if ("month".equals(type)){
                if (roles.contains("PROVINCE_ADMIN")){
                    criteriaQuery.groupBy(root.get("garbageQuality"), root.get("month"), root.get("year"), root.get("provinceId"));
                }
                if (roles.contains("CITY_ADMIN")){
                    criteriaQuery.groupBy(root.get("garbageQuality"),  root.get("month"), root.get("year"),root.get("provinceId"), root.get("cityId"));
                }
                if (roles.contains("COUNTRY_ADMIN")){
                    criteriaQuery.groupBy(root.get("garbageQuality"), root.get("month"), root.get("year"),root.get("provinceId"), root.get("cityId"), root.get("countryId"));
                }
                if (roles.contains("TOWN_ADMIN")){
                    criteriaQuery.groupBy(root.get("garbageQuality"), root.get("month"), root.get("year"),root.get("provinceId"), root.get("cityId"), root.get("countryId"), root.get("townId"));
                }
                if (roles.contains("VILLAGE_ADMIN")){
                    criteriaQuery.groupBy(root.get("garbageQuality"),  root.get("month"), root.get("year"),root.get("provinceId"), root.get("cityId"), root.get("countryId"), root.get("townId"), root.get("villageId"));
                }
            }else{
                if (roles.contains("PROVINCE_ADMIN")){
                    criteriaQuery.groupBy(root.get("garbageQuality"), root.get("year"), root.get("provinceId"));
                }
                if (roles.contains("CITY_ADMIN")){
                    criteriaQuery.groupBy(root.get("garbageQuality"), root.get("year"),root.get("provinceId"), root.get("cityId"));
                }
                if (roles.contains("COUNTRY_ADMIN")){
                    criteriaQuery.groupBy(root.get("garbageQuality"), root.get("year"),root.get("provinceId"), root.get("cityId"), root.get("countryId"));
                }
                if (roles.contains("TOWN_ADMIN")){
                    criteriaQuery.groupBy(root.get("garbageQuality"), root.get("year"),root.get("provinceId"), root.get("cityId"), root.get("countryId"), root.get("townId"));
                }
                if (roles.contains("VILLAGE_ADMIN")){
                    criteriaQuery.groupBy(root.get("garbageQuality"),  root.get("year"),root.get("provinceId"), root.get("cityId"), root.get("countryId"), root.get("townId"), root.get("villageId"));
                }
            }
        }
        List<GarbageCollectCountDto> garbageQualityCountList = entityManager.createQuery(criteriaQuery).getResultList();

        Map<String, Long> qualityMap = new HashMap<>();
        for (GarbageCollectCountDto dto: garbageQualityCountList){
            if ("day".equals(type)){
                qualityMap.put(dto.getPlaceId() + "-" + dto.getGarbageQuality() + "-" + dto.getDay() + "-" + dto.getMonth() + "-"+ dto.getYear(), dto.getCount());
            } else if ("month".equals(type)){
                qualityMap.put(dto.getPlaceId() + "-" + dto.getGarbageQuality() + "-" + dto.getMonth() + "-"+ dto.getYear(), dto.getCount());
            } else {
                qualityMap.put(dto.getPlaceId() + "-" + dto.getGarbageQuality() + "-"+ dto.getYear(), dto.getCount());
            }
        }
        Map<String, Long> totalCountMap = new HashMap<>();
        garbageQualityCountList.stream().forEach( n->{
            if ("day".equals(type)){
                String key = n.getPlaceId()+"-" + n.getDay()+ "-"+ n.getMonth()+"-" + n.getYear();
                if (totalCountMap.containsKey(key)){
                    totalCountMap.put(key, totalCountMap.get(key) + (n.getCount() == null?0: n.getCount()));
                } else {
                    totalCountMap.put(key, n.getCount());
                }
            } else if ("month".equals(type)){
                String key = n.getPlaceId()+ "-"+ n.getMonth()+"-" + n.getYear();
                if (totalCountMap.containsKey(key)){
                    totalCountMap.put(key, totalCountMap.get(key) + (n.getCount() == null?0: n.getCount()));
                } else {
                    totalCountMap.put(key, n.getCount());
                }
            } else {
                String key = n.getPlaceId() + "-" + n.getYear();
                if (totalCountMap.containsKey(key)){
                    totalCountMap.put(key, totalCountMap.get(key) + (n.getCount() == null?0: n.getCount()));
                } else {
                    totalCountMap.put(key, n.getCount());
                }
            }
        });

        // 统计 重量 参与用户 分页
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<GarbageCollectCountDto> cq = cb.createQuery(GarbageCollectCountDto.class);
        Root<GarbageCollectorEntity> rootPlace = cq.from(GarbageCollectorEntity.class);
        List<Expression<?>> expressionList = new ArrayList<>();
        List<Predicate> predicates = new ArrayList<>();
        Map<Long, String> placeNameMap = new HashMap<>();
        if (roles.contains("PROVINCE_ADMIN")){
            if (cityId !=null){
                cq.multiselect( rootPlace.get("cityId"), cb.sum(rootPlace.get("garbageWeight")), rootPlace.get("day"), rootPlace.get("month"), rootPlace.get("year"), cb.countDistinct(root.get("userId")));
                expressionList.add(rootPlace.get("cityId"));
                JPositionCityEntity cityEntity = jPositionCityDao.findByCityId(cityId);
                placeNameMap.put(cityId, cityEntity.getCityName());
                predicates.add(cb.equal(rootPlace.get("cityId"), cityId));
            } else if (cityId !=null && countryId !=null){
                cq.multiselect( rootPlace.get("countryId"), cb.sum(rootPlace.get("garbageWeight")), rootPlace.get("day"), rootPlace.get("month"), rootPlace.get("year"), cb.countDistinct(root.get("userId")));
                expressionList.add(rootPlace.get("countryId"));
                JPositionCountyEntity countyEntity = jPositionCountyDao.findByCountyId(countryId);
                placeNameMap.put(countryId, countyEntity.getCountyName());
                predicates.add(cb.equal(rootPlace.get("countryId"), countryId));
            } else if (cityId !=null && countryId !=null && townId !=null){
                cq.multiselect( rootPlace.get("townId"), cb.sum(rootPlace.get("garbageWeight")), rootPlace.get("day"), rootPlace.get("month"), rootPlace.get("year"), cb.countDistinct(root.get("userId")));
                expressionList.add(rootPlace.get("townId"));
                JPositionTownEntity jPositionTownEntity = jPositionTownDao.findByTownId(townId);
                placeNameMap.put(townId, jPositionTownEntity.getTownName());
                predicates.add(cb.equal(rootPlace.get("townId"), townId));
            } else if (cityId !=null && countryId !=null && townId !=null && villageId != null){
                cq.multiselect( rootPlace.get("villageId"), cb.sum(rootPlace.get("garbageWeight")), rootPlace.get("day"), rootPlace.get("month"), rootPlace.get("year"), cb.countDistinct(root.get("userId")));
                expressionList.add(rootPlace.get("villageId"));
                JPositionVillageEntity villageEntity = jPositionVillageDao.findByVillageId(villageId);
                placeNameMap.put(villageId, villageEntity.getVillageName());
                predicates.add(cb.equal(rootPlace.get("villageId"), villageId));
            } else {
                cq.multiselect( rootPlace.get("provinceId"), cb.sum(rootPlace.get("garbageWeight")) ,rootPlace.get("day"), rootPlace.get("month"), rootPlace.get("year"), cb.countDistinct(root.get("userId")));
                expressionList.add(rootPlace.get("provinceId"));
                JPositionProvinceEntity provinceEntity = jPositionProvinceDao.findByProvinceId(provinceId.intValue());
                placeNameMap.put(provinceId.longValue(), provinceEntity.getProvinceName());
                predicates.add(cb.equal(rootPlace.get("provinceId"), provinceId));
            }
        }
        if (roles.contains("CITY_ADMIN")){
            if (countryId != null ){
                cq.multiselect( rootPlace.get("countryId"), cb.sum(rootPlace.get("garbageWeight")), rootPlace.get("day"), rootPlace.get("month"), rootPlace.get("year"), cb.countDistinct(root.get("userId")));
                expressionList.add(rootPlace.get("countryId"));
                JPositionCountyEntity countyEntity = jPositionCountyDao.findByCountyId(countryId);
                placeNameMap.put(countryId, countyEntity.getCountyName());
                predicates.add(cb.equal(rootPlace.get("countryId"), countryId));
            } else if (countryId != null &&townId !=null){
                cq.multiselect( rootPlace.get("townId"), cb.sum(rootPlace.get("garbageWeight")), rootPlace.get("day"), rootPlace.get("month"), rootPlace.get("year"), cb.countDistinct(root.get("userId")));
                expressionList.add(rootPlace.get("townId"));
                JPositionTownEntity jPositionTownEntity = jPositionTownDao.findByTownId(townId);
                placeNameMap.put(townId, jPositionTownEntity.getTownName());
                predicates.add(cb.equal(rootPlace.get("townId"), townId));
            } else if (countryId !=null &&townId !=null && villageId !=null ){
                cq.multiselect( rootPlace.get("villageId"), cb.sum(rootPlace.get("garbageWeight")), rootPlace.get("day"), rootPlace.get("month"), rootPlace.get("year"), cb.countDistinct(root.get("userId")));
                expressionList.add(rootPlace.get("villageId"));
                JPositionVillageEntity villageEntity = jPositionVillageDao.findByVillageId(villageId);
                placeNameMap.put(villageId, villageEntity.getVillageName());
                predicates.add(cb.equal(rootPlace.get("villageId"), villageId));
            } else {
                cq.multiselect( rootPlace.get("cityId"), cb.sum(rootPlace.get("garbageWeight")), rootPlace.get("day"), rootPlace.get("month"), rootPlace.get("year"), cb.countDistinct(root.get("userId")));
                expressionList.add(rootPlace.get("cityId"));
                JPositionCityEntity cityEntity = jPositionCityDao.findByCityId(cityId);
                placeNameMap.put(cityId, cityEntity.getCityName());
                predicates.add(cb.equal(rootPlace.get("cityId"), cityId));
            }
        }
        if (roles.contains("COUNTRY_ADMIN")){
            if (townId != null ){
                cq.multiselect( rootPlace.get("townId"), cb.sum(rootPlace.get("garbageWeight")), rootPlace.get("day"), rootPlace.get("month"), rootPlace.get("year"), cb.countDistinct(root.get("userId")));
                expressionList.add(rootPlace.get("townId"));
                JPositionTownEntity jPositionTownEntity = jPositionTownDao.findByTownId(townId);
                placeNameMap.put(townId, jPositionTownEntity.getTownName());
                predicates.add(cb.equal(rootPlace.get("townId"), townId));
            } else if (townId !=null && villageId !=null ){
                cq.multiselect( rootPlace.get("villageId"), cb.sum(rootPlace.get("garbageWeight")), rootPlace.get("day"), rootPlace.get("month"), rootPlace.get("year"), cb.countDistinct(root.get("userId")));
                expressionList.add(rootPlace.get("villageId"));
                JPositionVillageEntity villageEntity = jPositionVillageDao.findByVillageId(villageId);
                placeNameMap.put(villageId, villageEntity.getVillageName());
                predicates.add(cb.equal(rootPlace.get("villageId"), villageId));
            } else {
                cq.multiselect( rootPlace.get("countryId"), cb.sum(rootPlace.get("garbageWeight")), rootPlace.get("day"), rootPlace.get("month"), rootPlace.get("year"), cb.countDistinct(root.get("userId")));
                expressionList.add(rootPlace.get("countryId"));
                JPositionCountyEntity countyEntity = jPositionCountyDao.findByCountyId(countryId);
                placeNameMap.put(countryId, countyEntity.getCountyName());
                predicates.add(cb.equal(rootPlace.get("countryId"), countryId));
            }
        }
        if (roles.contains("TOWN_ADMIN")){
            if (villageId !=null){
                cq.multiselect( rootPlace.get("villageId"), cb.sum(rootPlace.get("garbageWeight")), rootPlace.get("day"), rootPlace.get("month"), rootPlace.get("year"), cb.countDistinct(root.get("userId")));
                expressionList.add(rootPlace.get("villageId"));
                JPositionVillageEntity villageEntity = jPositionVillageDao.findByVillageId(villageId);
                placeNameMap.put(villageId, villageEntity.getVillageName());
                predicates.add(cb.equal(rootPlace.get("villageId"), villageId));
            } else {
                cq.multiselect( rootPlace.get("townId"), cb.sum(rootPlace.get("garbageWeight")), rootPlace.get("day"), rootPlace.get("month"), rootPlace.get("year"), cb.countDistinct(root.get("userId")));
                expressionList.add(rootPlace.get("townId"));
                JPositionTownEntity jPositionTownEntity = jPositionTownDao.findByTownId(townId);
                placeNameMap.put(townId, jPositionTownEntity.getTownName());
                predicates.add(cb.equal(rootPlace.get("townId"), townId));
            }
        }
        if (roles.contains("VILLAGE_ADMIN")){
            cq.multiselect( rootPlace.get("villageId"), cb.sum(rootPlace.get("garbageWeight")), rootPlace.get("day"), rootPlace.get("month"), rootPlace.get("year"), cb.countDistinct(root.get("userId")));
            expressionList.add(rootPlace.get("villageId"));
            JPositionVillageEntity villageEntity = jPositionVillageDao.findByVillageId(villageId);
            placeNameMap.put(villageId, villageEntity.getVillageName());
            predicates.add(cb.equal(rootPlace.get("villageId"), villageId));
        }
        if (fromType == 1){
            cq.multiselect( rootPlace.get("communityId"), cb.sum(rootPlace.get("garbageWeight")), rootPlace.get("day"), rootPlace.get("month"), rootPlace.get("year"), cb.countDistinct(root.get("userId")));
            expressionList.add(rootPlace.get("communityId"));
            if (communityId !=null){
                GarbageCommunityEntity communityEntity = garbageCommunityDao.findById(communityId).get();
                predicates.add(cb.equal(rootPlace.get("communityId"), communityId));
                placeNameMap.put(communityId.longValue(), communityEntity.getCommunityName());
            } else {
                if (communityIds.size() > 0){
                    predicates.add(rootPlace.get("communityId").in(communityIds));
                    List<GarbageCommunityEntity> communityEntities = garbageCommunityDao.findByIdIn(communityIds);
                    communityEntities.forEach(n->{
                        placeNameMap.put(n.getId().longValue(), n.getCommunityName());
                    });
                }
            }

        }

        if (!StringUtils.isEmpty(startTime)&& !StringUtils.isEmpty(endTime)){
            Predicate predicateStart = cb.greaterThanOrEqualTo(rootPlace.<Long>get("collectDate"), start);
            Predicate predicateEnd = cb.lessThanOrEqualTo(rootPlace.<Long>get("collectDate"), end);
            predicates.add(predicateStart);
            predicates.add(predicateEnd);
        }

        if (fromType == 1){
            Predicate predicate = cb.equal(rootPlace.get("garbageFromType"), Constants.garbageFromType.COMMUNITY.getType());
            predicates.add(predicate);

        } else {
            Predicate predicate = cb.equal(rootPlace.get("garbageFromType"), Constants.garbageFromType.TOWN.getType());
            predicates.add(predicate);
        }
        if ("day".equals(type)){
            expressionList.add(rootPlace.get("day"));
            expressionList.add(rootPlace.get("month"));
            expressionList.add( rootPlace.get("year"));
        }
        if ("month".equals(type)){
            expressionList.add(rootPlace.get("month"));
            expressionList.add( rootPlace.get("year"));
        }
        if ("year".equals(type)){
            expressionList.add( rootPlace.get("year"));
        }
        cq.where(predicates.toArray(new Predicate[predicates.size()]));
        cq.groupBy(expressionList);

        TypedQuery<GarbageCollectCountDto> query = entityManager.createQuery(cq);
        query.setFirstResult((pageNo-1)*pageSize);
        query.setMaxResults(pageSize);
        List<GarbageCollectCountDto> dtos = query.getResultList();
        Pageable pageable = PageRequest.of(pageNo-1, pageSize, getGarbageCollectorSummaryInfo(orderBys));
        PageImpl<GarbageCollectCountDto> page = new PageImpl<>(dtos, pageable, Long.valueOf(dtos.size()));
        page.getContent().forEach(dto-> {
            Integer qualityCount = 0;
            Integer emptyCount = 0;
            Integer notQualityCount = 0;
            String qualityKey = "";
            String emptyKey = "";
            String noQualityKey = "";
            String totalKey = "";
            Integer sum = 0;
            if ("day".equals(type)){
                qualityKey = dto.getPlaceId() + "-" + Constants.garbageQuality.QUALIFIED.getType() + "-" + dto.getDay() + "-" + dto.getMonth() + "-" + dto.getYear();
                noQualityKey = dto.getPlaceId() + "-" + Constants.garbageQuality.NOTQUALIFIED.getType() + "-" + dto.getDay() + "-" + dto.getMonth() + "-" + dto.getYear();
                emptyKey = dto.getPlaceId() + "-" + Constants.garbageQuality.EMPTY.getType() + "-" + dto.getDay() + "-" + dto.getMonth() + "-" + dto.getYear();
                qualityCount = qualityMap.get(qualityKey) == null?0:qualityMap.get(qualityKey).intValue();
                notQualityCount = qualityMap.get(noQualityKey) == null?0:qualityMap.get(noQualityKey).intValue();
                emptyCount = qualityMap.get(emptyKey) == null?0:qualityMap.get(emptyKey).intValue();
                totalKey = dto.getPlaceId() + "-" + dto.getDay() + "-" + dto.getMonth() + "-" + dto.getYear();
                sum = totalCountMap.get(totalKey) == null?0:totalCountMap.get(totalKey).intValue();
            } else if ("month".equals(type)){
                qualityKey = dto.getPlaceId() + "-" + Constants.garbageQuality.QUALIFIED.getType() + "-" + dto.getMonth() + "-" + dto.getYear();
                noQualityKey = dto.getPlaceId() + "-" + Constants.garbageQuality.NOTQUALIFIED.getType() + "-" + dto.getMonth() + "-" + dto.getYear();
                emptyKey = dto.getPlaceId() + "-" + Constants.garbageQuality.EMPTY.getType() + "-" + dto.getMonth() + "-" + dto.getYear();
                qualityCount = qualityMap.get(qualityKey) == null?0:qualityMap.get(qualityKey).intValue();
                notQualityCount = qualityMap.get(noQualityKey) == null?0:qualityMap.get(noQualityKey).intValue();
                emptyCount = qualityMap.get(emptyKey) == null?0:qualityMap.get(emptyKey).intValue();
                totalKey = dto.getPlaceId() + "-" + dto.getMonth() + "-" + dto.getYear();
                sum = totalCountMap.get(totalKey) == null?0:totalCountMap.get(totalKey).intValue();
            } else {
                qualityKey = dto.getPlaceId() + "-" + Constants.garbageQuality.QUALIFIED.getType() + "-" + dto.getYear();
                noQualityKey = dto.getPlaceId() + "-" + Constants.garbageQuality.NOTQUALIFIED.getType() + "-" + dto.getYear();
                emptyKey = dto.getPlaceId() + "-" + Constants.garbageQuality.EMPTY.getType() + "-" + dto.getYear();
                qualityCount = qualityMap.get(qualityKey) == null?0:qualityMap.get(qualityKey).intValue();
                notQualityCount = qualityMap.get(noQualityKey) == null?0:qualityMap.get(noQualityKey).intValue();
                emptyCount = qualityMap.get(emptyKey) == null?0:qualityMap.get(emptyKey).intValue();
                totalKey = dto.getPlaceId() + "-" + dto.getYear();
                sum = totalCountMap.get(totalKey) == null?0:totalCountMap.get(totalKey).intValue();
            }
            dto.setPlaceName(placeNameMap.get(dto.getPlaceId()));
            dto.setQualityCount(qualityCount);
            dto.setEmptyCount(emptyCount);
            dto.setNotQualityCount(notQualityCount);
            dto.setUserCount(userCount.intValue());
            if ("day".equals(type)){
                dto.setCollectDate(dto.getYear() + "-" + dto.getMonth() + "-" + dto.getDay());
            }else if ("month".equals(type)){
                dto.setCollectDate(dto.getYear() + "-" + dto.getMonth());
            } else {
                dto.setCollectDate(dto.getYear() + "");
            }
            DecimalFormat df = new DecimalFormat("0.00%");
            String emptuRate = "0%";
            String notQualityRate = "0%";
            String qualityRate = "0%";
            if(sum !=0){
                emptuRate = df.format(emptyCount.doubleValue()/sum);
                notQualityRate = df.format(notQualityCount.doubleValue()/sum);
                qualityRate = df.format(qualityCount.doubleValue()/sum);
            }
            String participationRate = "0%";
            if (userCount != 0){
                participationRate =  df.format(dto.getParticipationCount().doubleValue()/userCount);
            }
            dto.setParticipationRate(participationRate);
            dto.setEmptyRate(emptuRate);
            dto.setNotQualityRate(notQualityRate);
            dto.setQualityRate(qualityRate);
            BigDecimal bigDecimal = new BigDecimal(dto.getGarbageWeight());
            Double w = bigDecimal.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
            dto.setGarbageWeight(w);
            dtos.add(dto);
        });
        ResponsePageData responsePageData = new ResponsePageData();
        responsePageData.setPageNo(pageNo);
        responsePageData.setPageSize(pageSize);
        responsePageData.setCount(page.getTotalPages());
        responsePageData.setLastPage(page.isLast());
        responsePageData.setFirstPage(page.isFirst());
        responsePageData.setTotalElement(page.getTotalElements());
        responsePageData.setStatus(Constants.responseStatus.Success.getStatus());
        responsePageData.setMsg("查询成功");
        responsePageData.setData(page.getContent());
        return responsePageData;
    }

    public ResponseData getNotSentGarbageInfoToSystemUser(Integer pageNo, Integer pageSize, String startTime, String endTime, String type, String keyWord, String jwt,
                                                          String[] orderBys, Long cityId, Long countryId, Long townId, Long villageId, Long communityId) {
        Integer sub = jwtUtil.getSubject(jwt);
        GarbageUserEntity userEntity = garbageUserDao.findById(sub).get();
        Integer fromType = userEntity.getFromType();
        List<GarbageRoleEntity> roleEntityList =userEntity.getRoles().stream().collect(Collectors.toList());
        List<Integer> communityIds = getCommunityResource(roleEntityList);
        List<String> roles = roleEntityList.stream().map(role -> role.getRoleCode()).collect(Collectors.toList());
        Long  start = DateFormatUtil.getFirstTimeOfDay(startTime).getTime();
        Long  end = DateFormatUtil.getLastTimeOfDay(endTime).getTime();
        Pageable pageable = PageRequest.of(pageNo-1, pageSize, getGarbageCollectorSummaryInfo(orderBys));
        Page<GarbageUserEntity> garbageUserEntityPage = garbageUserDao.findAll(new Specification<GarbageUserEntity>() {
            @Nullable
            @Override
            public Predicate toPredicate(Root<GarbageUserEntity> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicateList = new ArrayList<>();
                if (roles.contains("PROVINCE_ADMIN")){
                    if (cityId !=null){
                        Predicate predicate = criteriaBuilder.equal(root.get("cityId"), cityId);
                        predicateList.add(predicate);
                    }else if (cityId !=null && countryId !=null){
                        Predicate predicate = criteriaBuilder.equal(root.get("countryId"), countryId);
                        predicateList.add(predicate);
                    } else if (cityId !=null && countryId !=null && townId !=null){
                        Predicate predicate = criteriaBuilder.equal(root.get("townId"), townId);
                        predicateList.add(predicate);
                    }else if (cityId !=null && countryId !=null && townId !=null && villageId != null){
                        Predicate predicate = criteriaBuilder.equal(root.get("villageId"), villageId);
                        predicateList.add(predicate);
                    } else {
                        Predicate predicate = criteriaBuilder.equal(root.get("provinceId"), userEntity.getProvinceId());
                        predicateList.add(predicate);
                    }
                }
                if (roles.contains("CITY_ADMIN")){
                    if (countryId !=null){
                        Predicate predicate = criteriaBuilder.equal(root.get("countryId"), countryId);
                        predicateList.add(predicate);
                    } else if (countryId !=null && townId != null) {
                        Predicate predicate = criteriaBuilder.equal(root.get("townId"), townId);
                        predicateList.add(predicate);
                    } else if (countryId != null && townId != null && villageId != null){
                        Predicate predicate = criteriaBuilder.equal(root.get("villageId"), villageId);
                        predicateList.add(predicate);
                    } else {
                        Predicate predicate = criteriaBuilder.equal(root.get("cityId"), userEntity.getCityId());
                        predicateList.add(predicate);
                    }
                }
                if (roles.contains("COUNTRY_ADMIN")){
                    if (townId != null){
                        Predicate predicate = criteriaBuilder.equal(root.get("townId"), townId);
                        predicateList.add(predicate);
                    } else if (townId !=null && villageId !=null){
                        Predicate predicate = criteriaBuilder.equal(root.get("villageId"), villageId);
                        predicateList.add(predicate);
                    } else {
                        Predicate predicate = criteriaBuilder.equal(root.get("countryId"), userEntity.getCountryId());
                        predicateList.add(predicate);
                    }
                }
                if (roles.contains("TOWN_ADMIN")){
                    if (villageId !=null){
                        Predicate predicate = criteriaBuilder.equal(root.get("villageId"), villageId);
                        predicateList.add(predicate);
                    } else {
                        Predicate predicate = criteriaBuilder.equal(root.get("townId"), userEntity.getTownId());
                        predicateList.add(predicate);
                    }
                }
                if (roles.contains("VILLAGE_ADMIN")){
                    Predicate predicate = criteriaBuilder.equal(root.get("villageId"), userEntity.getVillageId());
                    predicateList.add(predicate);
                }
                if (fromType == 1){
                    if (communityIds.size() > 0){
                        if (communityId != null ){
                            Predicate predicate = criteriaBuilder.equal(root.get("communityId"), communityId);
                            predicateList.add(predicate);
                        } else {
                            Predicate predicate =  root.get("communityId").in(communityIds);
                            predicateList.add(predicate);
                        }
                    }
                }
                if (!StringUtils.isEmpty(type)){
                    if ("name".equals(type) && !StringUtils.isEmpty(keyWord)){
                        Predicate predicate = criteriaBuilder.like(root.get("name") , "%" + keyWord + "%");
                        predicateList.add(predicate);
                    }
                    if ("phone".equals(type) && !StringUtils.isEmpty(keyWord)){
                        Predicate predicate = criteriaBuilder.like(root.get("phone") , "%" + keyWord + "%");
                        predicateList.add(predicate);
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


                        Predicate exits =  criteriaBuilder.exists(subquery.where(selectList.toArray(new Predicate[selectList.size()])));
                        predicateList.add(exits);
                    }
                }
                if (!StringUtils.isEmpty(startTime) && !StringUtils.isEmpty(endTime)){
                    List<Predicate> selectList = new ArrayList<>();
                    Subquery subquery = criteriaQuery.subquery(GarbageCollectorEntity.class);
                    Root<GarbageCollectorEntity> subRoot = subquery.from(GarbageCollectorEntity.class);
                    subquery.select(subRoot.get("userId"));
                    Predicate equal = criteriaBuilder.equal(root.get("id"), subRoot.get("userId"));
                    selectList.add(equal);
                    Predicate fromTypePre = criteriaBuilder.equal(subRoot.get("garbageFromType"), fromType );
                    selectList.add(fromTypePre);
                    Predicate startP = criteriaBuilder.greaterThanOrEqualTo(subRoot.get("collectDate"), start);
                    Predicate endP = criteriaBuilder.lessThanOrEqualTo(subRoot.get("collectDate"), end);
                    selectList.add(startP);
                    selectList.add(endP);
                    if (roles.contains("PROVINCE_ADMIN")){
                        if (cityId !=null){
                            Predicate predicate = criteriaBuilder.equal(subRoot.get("cityId"), cityId);
                            selectList.add(predicate);
                        }else if (cityId !=null && countryId !=null){
                            Predicate predicate = criteriaBuilder.equal(subRoot.get("countryId"), countryId);
                            predicateList.add(predicate);
                        } else if (cityId !=null && countryId !=null && townId !=null){
                            Predicate predicate = criteriaBuilder.equal(subRoot.get("townId"), townId);
                            selectList.add(predicate);
                        }else if (cityId !=null && countryId !=null && townId !=null && villageId != null){
                            Predicate predicate = criteriaBuilder.equal(subRoot.get("villageId"), villageId);
                            selectList.add(predicate);
                        } else {
                            Predicate predicate = criteriaBuilder.equal(subRoot.get("provinceId"), userEntity.getProvinceId());
                            selectList.add(predicate);
                        }
                    }
                    if (roles.contains("CITY_ADMIN")){
                        if (countryId !=null){
                            Predicate predicate = criteriaBuilder.equal(subRoot.get("countryId"), countryId);
                            selectList.add(predicate);
                        } else if (countryId !=null && townId != null) {
                            Predicate predicate = criteriaBuilder.equal(subRoot.get("townId"), townId);
                            selectList.add(predicate);
                        } else if (countryId != null && townId != null && villageId != null){
                            Predicate predicate = criteriaBuilder.equal(subRoot.get("villageId"), villageId);
                            selectList.add(predicate);
                        } else {
                            Predicate predicate = criteriaBuilder.equal(subRoot.get("cityId"), userEntity.getCityId());
                            selectList.add(predicate);
                        }
                    }
                    if (roles.contains("COUNTRY_ADMIN")){
                        if (townId != null){
                            Predicate predicate = criteriaBuilder.equal(subRoot.get("townId"), townId);
                            selectList.add(predicate);
                        } else if (townId !=null && villageId !=null){
                            Predicate predicate = criteriaBuilder.equal(subRoot.get("villageId"), villageId);
                            selectList.add(predicate);
                        } else {
                            Predicate predicate = criteriaBuilder.equal(subRoot.get("countryId"), userEntity.getCountryId());
                            selectList.add(predicate);
                        }
                    }
                    if (roles.contains("TOWN_ADMIN")){
                        if (villageId !=null){
                            Predicate predicate = criteriaBuilder.equal(subRoot.get("villageId"), villageId);
                            selectList.add(predicate);
                        } else {
                            Predicate predicate = criteriaBuilder.equal(subRoot.get("townId"), userEntity.getTownId());
                            selectList.add(predicate);
                        }
                    }
                    if (roles.contains("VILLAGE_ADMIN")){
                        Predicate predicate = criteriaBuilder.equal(subRoot.get("villageId"), userEntity.getVillageId());
                        selectList.add(predicate);
                    }
                    if (fromType == 1){
                        if (communityIds.size() > 0){
                            if (communityId != null ){
                                Predicate predicate = criteriaBuilder.equal(subRoot.get("communityId"), communityId);
                                selectList.add(predicate);
                            } else {
                                Predicate predicate =  subRoot.get("communityId").in(communityIds);
                                selectList.add(predicate);
                            }
                        }
                    }
                    Predicate not = criteriaBuilder.not(criteriaBuilder.exists(subquery.where(selectList.toArray(new Predicate[selectList.size()]))));
                    predicateList.add(not);
                }
                return criteriaBuilder.and(predicateList.toArray(new Predicate[predicateList.size()]));
            }
        }, pageable);
        ResponsePageData responsePageData = new ResponsePageData();
        responsePageData.setPageNo(pageNo);
        responsePageData.setPageSize(pageSize);
        responsePageData.setCount(garbageUserEntityPage.getTotalPages());
        responsePageData.setLastPage(garbageUserEntityPage.isLast());
        responsePageData.setFirstPage(garbageUserEntityPage.isFirst());
        responsePageData.setTotalElement(garbageUserEntityPage.getTotalElements());
        responsePageData.setStatus(Constants.responseStatus.Success.getStatus());
        responsePageData.setMsg("查询成功");
        responsePageData.setData(garbageUserEntityPage.getContent());
        return responsePageData;
    }

    public ResponseData getGarbageWeightCurrentYear() {
        Calendar calendar = Calendar.getInstance();
        Integer year = calendar.get(Calendar.YEAR);
        Integer month = calendar.get(Calendar.MONTH);
        List<GarbageWeightInMonth> weightInMonths = null;
        if (month < 5){
            weightInMonths  = garbageCollectorDao.getGarbageWeightInMonthBetween(year, 0, month);
        } else {
            weightInMonths  = garbageCollectorDao.getGarbageWeightInMonthBetween(year, month - 5, month );
        }
        weightInMonths.forEach(weightInMonth->{
            weightInMonth.setTime(year + "-" + (weightInMonth.getMonth() + 1));
        });
        ResponseData responseData = new ResponseData();
        responseData.setData(weightInMonths);
        responseData.setStatus(Constants.responseStatus.Success.getStatus());
        responseData.setMsg("统计信息获取成功");
        return responseData;
    }

    public  List<Integer>  getCommunityResource(List<GarbageRoleEntity> roleEntityList){
        List<GarbageRoleEntity> collect = roleEntityList.stream().filter(role -> role.getRoleCode().endsWith("COMMUNITY_ADMIN") || role.getRoleCode().endsWith("COMMUNITY_REMARK")).collect(Collectors.toList());
        List<Integer> roleIds = collect.stream().map(role -> role.getId()).collect(Collectors.toList());
        List<GarbageRoleCommunityEntity> roleCommunityEntityList = garbageRoleCommunityDao.findAll(new Specification<GarbageRoleCommunityEntity>() {
            @Nullable
            @Override
            public Predicate toPredicate(Root<GarbageRoleCommunityEntity> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
               Predicate predicate = null;
                if (roleIds.size()> 0){
                    predicate =  root.get("roleId").in(roleIds)  ;
               }
                return predicate;
            }
        });
        List<Integer> communityIds = roleCommunityEntityList.stream().map(garbageRoleCommunityEntity -> garbageRoleCommunityEntity.getCommunityId()).collect(Collectors.toList());
        return communityIds;
    }

    public ResponsePageData villageGarbageList(Integer pageNo, Integer pageSize, Boolean isCheck, Double weight, Integer point, Integer quality, String type,
                                               String keyWord,  Integer garbageType, String jwt, String[] orderBys,
                                                String startTime, String endTime, Long cityId, Long countyId, Long townId, Long villageId) {
        Integer sub = jwtUtil.getSubject(jwt);
        GarbageUserEntity userEntity = garbageUserDao.findById(sub).get();
        if(isCheck == null){
            isCheck = true;
        }
        if (quality == null){
            quality = 0;
        }
        List<GarbageRoleEntity> roleEntityList = userEntity.getRoles().stream().collect(Collectors.toList());
        List<String> roleCodes = roleEntityList.stream().map(role -> role.getRoleCode()).collect(Collectors.toList());
        Long startDate = DateFormatUtil.getFirstTimeOfDay(startTime).getTime();
        Long endDate = DateFormatUtil.getLastTimeOfDay(endTime).getTime();
        Pageable pageable = PageRequest.of(pageNo - 1, pageSize, getCommunityGarbageSort(orderBys));
        Boolean finalIsCheck = isCheck;
        Integer finalQuality = quality;
        Page<GarbageCollectorEntity> page = garbageCollectorDao.findAll(new Specification<GarbageCollectorEntity>() {
            @Override
            public Predicate toPredicate(Root<GarbageCollectorEntity> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicateList = new ArrayList<>();
                //小区
                Predicate predicateFrom = criteriaBuilder.equal(root.get("garbageFromType"), Constants.garbageFromType.TOWN.getType());
                predicateList.add(predicateFrom);
                if(!StringUtils.isEmpty(startTime) && !StringUtils.isEmpty(endTime)){
                    Predicate startPredicate = criteriaBuilder.greaterThanOrEqualTo(root.get("collectDate"), startDate);
                    Predicate endPredicate = criteriaBuilder.lessThanOrEqualTo(root.get("collectDate"), endDate);
                    predicateList.add(startPredicate);
                    predicateList.add(endPredicate);

                }
                if (finalIsCheck){
                    Predicate isCheckPredicate = criteriaBuilder.isTrue(root.get("check"));
                    predicateList.add(isCheckPredicate);
                } else {
                    Predicate isCheckPredicate = criteriaBuilder.isFalse(root.get("check"));
                    predicateList.add(isCheckPredicate);
                }
                if (weight != null) {
                    Predicate weightPredicate = criteriaBuilder.greaterThanOrEqualTo(root.get("garbageWeight"), weight);
                    predicateList.add(weightPredicate);
                }
                if (point != null) {
                    Predicate pointPredicate = criteriaBuilder.greaterThanOrEqualTo(root.get("garbagePoint"), point);
                    predicateList.add(pointPredicate);
                }
                if (garbageType!= null && garbageType != 0){
                    //厨余垃圾--1    其他垃圾--2  所有-- 0
                    Predicate garbageTypePredicate = criteriaBuilder.greaterThanOrEqualTo(root.get("garbageType"), garbageType);
                    predicateList.add(garbageTypePredicate);
                }
                Constants.garbageQuality garbageQuality  = null;
                switch (finalQuality){
                    case 1:
                        garbageQuality = Constants.garbageQuality.QUALIFIED;
                        break;
                    case 2:
                        garbageQuality = Constants.garbageQuality.NOTQUALIFIED;
                        break;
                    case 3:
                        garbageQuality = Constants.garbageQuality.EMPTY;
                        break;
                    default:
                        garbageQuality = null;
                        break;
                }
                if (garbageQuality != null) {
                    Predicate qualityPredicate = criteriaBuilder.equal(root.get("garbageQuality"), garbageQuality.getType());
                    predicateList.add(qualityPredicate);
                }
                if (!StringUtils.isEmpty(type) && "eNo".equals(type) && !StringUtils.isEmpty(keyWord)){
                    Predicate eNoPredicate = criteriaBuilder.equal(root.get("eNo"), keyWord);
                    predicateList.add(eNoPredicate);
                }
                if (!StringUtils.isEmpty(keyWord) && "name".equals(type) || "phone".equals(type) ){
                    List<Predicate> predicates = new ArrayList<>();
                    Subquery subquery = criteriaQuery.subquery(GarbageUserEntity.class);
                    Root subRoot = subquery.from(GarbageUserEntity.class);
                    subquery.select(subRoot.get("id"));
                    Predicate predicate = criteriaBuilder.equal(root.get("userId"), subRoot.get("id"));
                    predicates.add(predicate);
                    if ("name".equals(type)){
                        Predicate namePredicate  = criteriaBuilder.like(subRoot.get("name"), "%" + keyWord + "%");
                        predicates.add(namePredicate);
                    }
                    if ("phone".equals(type)){
                        Predicate phonePredicate  = criteriaBuilder.like(subRoot.get("phone"), "%" + keyWord + "%");
                        predicates.add(phonePredicate);
                    }
                    Predicate exists = criteriaBuilder.exists(subquery.where(predicates.toArray(new Predicate[predicates.size()])));
                    predicateList.add(exists);
                }
                if (cityId !=null){
                    Predicate cityPredicate = criteriaBuilder.equal(root.get("cityId"), cityId);
                    predicateList.add(cityPredicate);
                }
                if (countyId !=null){
                    Predicate countyPredicate = criteriaBuilder.equal(root.get("countryId"), countyId);
                    predicateList.add(countyPredicate);
                }
                if (townId !=null){
                    Predicate townIdPredicate = criteriaBuilder.equal(root.get("townId"), townId);
                    predicateList.add(townIdPredicate);
                }
                if (villageId !=null){
                    Predicate villageIdPredicate = criteriaBuilder.equal(root.get("villageId"), villageId);
                    predicateList.add(villageIdPredicate);
                }

                return criteriaBuilder.and(predicateList.toArray(new Predicate[predicateList.size()]));
            }
        }, pageable);
        List<Integer> userIds = page.getContent().stream().map(garbageCollectorEntity -> garbageCollectorEntity.getUserId()).collect(Collectors.toList());
        List<UserDto> userDtos = new ArrayList<>();
        if (userIds.size() > 0){
            userDtos = garbageUserDao.selectUserInfoByIdIn(userIds);
        }
        List<Long> communityIds = page.getContent().stream().map(garbageCollectorEntity -> garbageCollectorEntity.getCommunityId()).collect(Collectors.toList());
        Map<Integer, String> userMap = userDtos.stream().collect(Collectors.toMap(UserDto::getUserId, UserDto::getUsername));
        List<CommunityGarbageCollectDto> dtos = new ArrayList<>();
        List<GarbageCommunityEntity> communityEntities = garbageCommunityDao.findAll(new Specification<GarbageCommunityEntity>() {
            @Nullable
            @Override
            public Predicate toPredicate(Root<GarbageCommunityEntity> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList<>();
                if (communityIds.size() > 0){
                    Predicate predicate = root.get("id").in(communityIds);
                    predicates.add(predicate);
                }
                return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        });
        Map<Integer, String> communityMap = communityEntities.stream().collect(Collectors.toMap(GarbageCommunityEntity::getId, GarbageCommunityEntity::getCommunityName));
        List<GarbageImageEntity> imageEntityList = garbageImageDao.findBySourceNameAndType(GarbageCollectorEntity.class.getName(), Constants.image.GARBAGE_IMAGE.name());
        Map<Integer, String> imagePathMap = imageEntityList.stream().collect(Collectors.toMap(GarbageImageEntity::getBusId, GarbageImageEntity::getImagePath));
        page.getContent().stream().forEach(garbageCollectorEntity -> {
            CommunityGarbageCollectDto dto = new CommunityGarbageCollectDto();
            dto.setId(garbageCollectorEntity.getId());
            dto.setUserId(garbageCollectorEntity.getUserId());
            dto.setUsername(userMap.get(garbageCollectorEntity.getUserId()));
            dto.setType(garbageCollectorEntity.getGarbageFromType() == 0?"农村":"小区");
            dto.setAddress(communityMap.get(garbageCollectorEntity.getCommunityId()));
            String qualityString = "";
            if(garbageCollectorEntity.getGarbageQuality() == 1){
                qualityString = "合格";
            } else if(garbageCollectorEntity.getGarbageQuality() == 2){
                qualityString = "不合格";
            } else {
                qualityString = "空桶";
            }
            dto.setQualityType(qualityString);
            String garbgetTypeString = "";
            if (garbageCollectorEntity.getGarbageType() == 1){
                garbgetTypeString = "厨余垃圾";
            } else {
                garbgetTypeString = "其他垃圾";
            }
            dto.setGarbageType(garbgetTypeString);
            dto.setPoint(garbageCollectorEntity.getGarbagePoint());
            dto.setWeight(garbageCollectorEntity.getGarbageWeight());
            dto.setCollectorId(garbageCollectorEntity.getCollectorId());
            dto.setCollectorName(garbageCollectorEntity.getCollectorName());
            dto.setCollectDate(DateFormatUtil.formatDate(new Date(garbageCollectorEntity.getCollectDate()), "yyyy-MM-dd"));
            dto.setImage(imagePathMap.get(garbageCollectorEntity.getId()));
            dto.setCheck(garbageCollectorEntity.getCheck() == true?"已评分":"未评分");
            dtos.add(dto);
        });
        ResponsePageData responsePageData = new ResponsePageData();
        responsePageData.setPageNo(pageNo);
        responsePageData.setPageSize(pageSize);
        responsePageData.setCount(page.getTotalPages());
        responsePageData.setLastPage(page.isLast());
        responsePageData.setFirstPage(page.isFirst());
        responsePageData.setTotalElement(page.getTotalElements());
        responsePageData.setData(dtos);
        responsePageData.setStatus(Constants.responseStatus.Success.getStatus());
        responsePageData.setMsg("列表查询成功");
        return responsePageData;
    }

    public ResponseData getCommunityListForUser(Long countyId, String jwt) {
        ResponseData responseData = new ResponseData();
        List<GarbageCommunityEntity> communityEntities = garbageCommunityDao.findByCountryId(countyId);
        responseData.setData(communityEntities);
        responseData.setStatus(Constants.responseStatus.Success.getStatus());
        responseData.setMsg("小区列表成功");
        return responseData;
    }

    public ResponseData userCollectDataAnalysis(Integer pageNo, Integer pageSize, String startTime, String endTime, String type, String keyWord, String jwt, String[] orderBys,
                                                Long cityId, Long countryId, Long townId, Long villageId, Long communityId) {
        Integer sub = jwtUtil.getSubject(jwt);
        GarbageUserEntity userEntity = garbageUserDao.findById(sub).get();
        Integer fromType = userEntity.getFromType();
        List<GarbageRoleEntity> roleEntityList =userEntity.getRoles().stream().collect(Collectors.toList());
        List<Integer> communityIds = getCommunityResource(roleEntityList);
        List<String> roles = roleEntityList.stream().map(role -> role.getRoleCode()).collect(Collectors.toList());
        Long  start = DateFormatUtil.getFirstTimeOfDay(startTime).getTime();
        Long  end = DateFormatUtil.getLastTimeOfDay(endTime).getTime();
        Pageable pageable = PageRequest.of(pageNo-1, pageSize, userCollectDataAnalysisSort(orderBys));

        //分页统计dto
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<UserCollectDataAnalysisDto> cq = cb.createQuery(UserCollectDataAnalysisDto.class);
        Root<GarbageCollectorEntity> root = cq.from(GarbageCollectorEntity.class);
        List<Predicate> predicates = new ArrayList<>();

        //垃圾质量统计dto
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<UserCollectDataAnalysisDto> criteriaQuery = criteriaBuilder.createQuery(UserCollectDataAnalysisDto.class);
        Root<GarbageCollectorEntity> rootP = criteriaQuery.from(GarbageCollectorEntity.class);
        List<Predicate> predicateList = new ArrayList<>();

        List<Expression<?>> expressionList = new ArrayList<>();
        List<Expression<?>> expressionListQuality = new ArrayList<>();
        Predicate predicateNotnull = cb.isNotNull(root.get("garbageQuality"));
        predicates.add(predicateNotnull);
        if (fromType == 1){
            Predicate garbageFromType = cb.equal(root.get("garbageFromType"), Constants.garbageFromType.COMMUNITY.getType());
            predicates.add(garbageFromType);
        } else {
            Predicate garbageFromType = cb.equal(root.get("garbageFromType"), Constants.garbageFromType.TOWN.getType());
            predicates.add(garbageFromType);
        }
        cq.multiselect(root.get("userId"), cb.count(root.get("userId")), cb.sum(root.get("garbageWeight")), root.get("day"), root.get("month"), root.get("year"));
        expressionList.add(root.get("userId"));
//        expressionList.add(root.get("day"));
//        expressionList.add(root.get("month"));
//        expressionList.add(root.get("year"));
        Predicate checkPredicate = cb.isTrue(root.get("check"));
        if (!StringUtils.isEmpty(startTime) && !StringUtils.isEmpty(endTime)){
            Predicate predicateStart = cb.greaterThanOrEqualTo(root.get("collectDate"), start);
            Predicate predicateEnd = cb.lessThanOrEqualTo(root.get("collectDate"), end);
            predicates.add(predicateStart);
            predicates.add(predicateEnd);
        }
        if (roles.contains("PROVINCE_ADMIN")){
            if (cityId != null){
                Predicate predicate = cb.equal(root.get("cityId"), cityId);
                Predicate predicateP = criteriaBuilder.equal(rootP.get("cityId"), cityId);
                predicates.add(predicate);
                predicateList.add(predicateP);
                expressionList.add(root.get("cityId"));
                expressionListQuality.add(root.get("cityId"));
            } else if (cityId !=null && countryId !=null){
                expressionList.add(root.get("countryId"));
                expressionListQuality.add(root.get("countryId"));
                Predicate predicate = cb.equal(root.get("countryId"), countryId);
                Predicate predicateP = criteriaBuilder.equal(rootP.get("countryId"), countryId);
                predicates.add(predicate);
                predicateList.add(predicateP);
            } else if (cityId !=null && countryId !=null && townId != null){
                expressionList.add(root.get("townId"));
                expressionListQuality.add(root.get("townId"));
                Predicate predicate = cb.equal(root.get("townId"), townId);
                Predicate predicateP = criteriaBuilder.equal(rootP.get("townId"), townId);
                predicates.add(predicate);
                predicateList.add(predicateP);
            } else if (cityId !=null && countryId !=null && townId != null && villageId !=null){
                expressionList.add(root.get("villageId"));
                expressionListQuality.add(root.get("villageId"));
                Predicate predicate = cb.equal(root.get("villageId"), villageId);
                Predicate predicateP = criteriaBuilder.equal(rootP.get("villageId"), villageId);
                predicates.add(predicate);
                predicateList.add(predicateP);
            } else {
                expressionList.add(root.get("provinceId"));
                expressionListQuality.add(root.get("provinceId"));
                Predicate predicate = cb.equal(root.get("provinceId"), userEntity.getProvinceId());
                Predicate predicateP = criteriaBuilder.equal(rootP.get("provinceId"), userEntity.getProvinceId());
                predicates.add(predicate);
                predicateList.add(predicateP);
            }
        }
        if (roles.contains("CITY_ADMIN")){
            if (countryId !=null){
                expressionList.add(root.get("countryId"));
                expressionListQuality.add(root.get("countryId"));
                Predicate predicate = cb.equal(root.get("countryId"), countryId);
                Predicate predicateP = criteriaBuilder.equal(rootP.get("countryId"), countryId);
                predicates.add(predicate);
                predicateList.add(predicateP);
            } else if (countryId !=null && townId != null){
                expressionList.add(root.get("townId"));
                expressionListQuality.add(root.get("townId"));
                Predicate predicate = cb.equal(root.get("townId"), townId);
                Predicate predicateP = criteriaBuilder.equal(rootP.get("townId"), townId);
                predicates.add(predicate);
                predicateList.add(predicateP);
            } else if (countryId !=null && townId != null && villageId !=null){
                expressionList.add(root.get("villageId"));
                expressionListQuality.add(root.get("villageId"));
                Predicate predicate = cb.equal(root.get("villageId"), villageId);
                Predicate predicateP = criteriaBuilder.equal(rootP.get("villageId"), villageId);
                predicates.add(predicate);
                predicateList.add(predicateP);
            } else {
                expressionList.add(root.get("cityId"));
                expressionListQuality.add(root.get("cityId"));
                Predicate predicate = cb.equal(root.get("cityId"), userEntity.getCityId());
                Predicate predicateP = criteriaBuilder.equal(rootP.get("cityId"), userEntity.getCityId());
                predicates.add(predicate);
                predicateList.add(predicateP);
            }
        }
        if (roles.contains("COUNTRY_ADMIN")){
            if (townId != null){
                expressionList.add(root.get("townId"));
                expressionListQuality.add(root.get("townId"));
                Predicate predicate = cb.equal(root.get("townId"), townId);
                Predicate predicateP = criteriaBuilder.equal(rootP.get("townId"), townId);
                predicates.add(predicate);
                predicateList.add(predicateP);
            } else if (countryId !=null && townId != null && villageId !=null){
                expressionList.add(root.get("villageId"));
                expressionListQuality.add(root.get("villageId"));
                Predicate predicate = cb.equal(root.get("villageId"), villageId);
                Predicate predicateP = criteriaBuilder.equal(rootP.get("villageId"), villageId);
                predicates.add(predicate);
                predicateList.add(predicateP);
            } else {
                expressionList.add(root.get("countryId"));
                expressionListQuality.add(root.get("countryId"));
                Predicate predicate = cb.equal(root.get("countryId"), userEntity.getCountryId());
                Predicate predicateP = criteriaBuilder.equal(rootP.get("countryId"), userEntity.getCountryId());
                predicates.add(predicate);
                predicateList.add(predicateP);
            }
        }
        if(roles.contains("TOWN_ADMIN")){
            if (villageId !=null){
                expressionList.add(root.get("villageId"));
                expressionListQuality.add(root.get("villageId"));
                Predicate predicate = cb.equal(root.get("villageId"), villageId);
                Predicate predicateP = criteriaBuilder.equal(rootP.get("villageId"), villageId);
                predicates.add(predicate);
                predicateList.add(predicateP);
            } else {
                expressionList.add(root.get("townId"));
                expressionListQuality.add(root.get("townId"));
                Predicate predicate = cb.equal(root.get("townId"), userEntity.getTownId());
                Predicate predicateP = criteriaBuilder.equal(rootP.get("townId"),  userEntity.getTownId());
                predicates.add(predicate);
                predicateList.add(predicateP);
            }
        }
        if (roles.contains("VILLAGE_ADMIN")){
            expressionList.add(root.get("villlageId"));
            expressionListQuality.add(root.get("villlageId"));
            Predicate predicate = cb.equal(root.get("villageId"), userEntity.getVillageId());
            Predicate predicateP = criteriaBuilder.equal(rootP.get("villageId"), userEntity.getVillageId());
            predicates.add(predicate);
            predicateList.add(predicateP);
        }
        if (fromType ==1){
            expressionList.add(root.get("communityId"));
            expressionListQuality.add(root.get("communityId"));
            if (communityId != null){
                Predicate predicate = cb.equal(root.get("communityId"), communityId);
                Predicate predicateP = criteriaBuilder.equal(rootP.get("communityId"), communityId);
                predicates.add(predicate);
                predicateList.add(predicateP);
            } else {
                if (communityIds .size()> 0){
                   Predicate predicate = root.get("communityId").in(communityIds);
                   Predicate predicateP = rootP.get("communityId").in(communityIds);
                   predicates.add(predicate);
                   predicateList.add(predicateP);
                }
            }
        }
        cq.groupBy(expressionList);

        if (!StringUtils.isEmpty(type) && !StringUtils.isEmpty(keyWord)){
            List<Predicate> selectList = new ArrayList<>();
            Subquery subquery = cq.subquery(GarbageUserEntity.class);
            Root suRoot = subquery.from(GarbageUserEntity.class);
            subquery.select(suRoot.get("id"));
            Predicate equal =  cb.equal(root.get("userId"), suRoot.get("id"));
            predicates.add(equal);
            if ("name".equals(type)){
                Predicate name = cb.like(suRoot.get("name"), "%" + keyWord + "%");
                selectList.add(name);
            }
            if ("phone".equals(type)){
                Predicate phone = cb.like(suRoot.get("phone"), "%" + keyWord + "%");
                selectList.add(phone);
            }
            Predicate exists = cb.exists(subquery.where(selectList.toArray(new Predicate[selectList.size()])));
            predicates.add(exists);
        }
        //分页
        cq.where(predicates.toArray(new Predicate[predicates.size()]));
        TypedQuery<UserCollectDataAnalysisDto> query = entityManager.createQuery(cq);
        query.setFirstResult((pageNo-1)*pageSize);
        query.setMaxResults(pageSize);
        List<UserCollectDataAnalysisDto> dtos = query.getResultList();
        PageImpl<UserCollectDataAnalysisDto> page = new PageImpl<>(dtos, pageable, Long.valueOf(dtos.size()));
        List<GarbageUserEntity> userEntities = garbageUserDao.findAll();
        Map<Integer,GarbageUserEntity> userEntityMap = userEntities.stream().collect(Collectors.toMap(GarbageUserEntity::getId, Function.identity()));

        //分类统计
        criteriaQuery.multiselect(rootP.get("userId"), rootP.get("garbageQuality"), criteriaBuilder.count(rootP.get("garbageQuality")), rootP.get("day"), rootP.get("month"), rootP.get("year"));
        if (fromType == 1){
            Predicate garbageFromType = criteriaBuilder.equal(rootP.get("garbageFromType"), Constants.garbageFromType.COMMUNITY.getType());
            predicateList.add(garbageFromType);
        } else {
            Predicate garbageFromType = criteriaBuilder.equal(rootP.get("garbageFromType"), Constants.garbageFromType.TOWN.getType());
            predicateList.add(garbageFromType);
        }
        if (!StringUtils.isEmpty(startTime) && !StringUtils.isEmpty(endTime)){
            Predicate predicateStart = criteriaBuilder.greaterThanOrEqualTo(rootP.get("collectDate"), start);
            Predicate predicateEnd = criteriaBuilder.lessThanOrEqualTo(rootP.get("collectDate"), end);
            predicateList.add(predicateStart);
            predicateList.add(predicateEnd);
        }
        Predicate predicatePNotnull = criteriaBuilder.isNotNull(rootP.get("garbageQuality"));
        predicateList.add(predicatePNotnull);

        expressionListQuality.add(rootP.get("userId"));
//        expressionListQuality.add(root.get("day"));
//        expressionListQuality.add(root.get("month"));
//        expressionListQuality.add(root.get("year"));
        expressionListQuality.add(rootP.get("garbageQuality"));
        criteriaQuery.where(predicateList.toArray(new Predicate[predicateList.size()]));
        criteriaQuery.groupBy(expressionListQuality);

        TypedQuery<UserCollectDataAnalysisDto> queryD = entityManager.createQuery(criteriaQuery);
        List<UserCollectDataAnalysisDto> QualityDtos = queryD.getResultList();
        Map<String, Long> qualityMap = new HashMap<>();
        Map<Integer, Long> totalMap = new HashMap<>();
        QualityDtos.forEach(dto->{
            qualityMap.put(dto.getUserId() + "-" + dto.getQualityType(), dto.getCount());
            if (totalMap.containsKey(dto.getUserId())){
                totalMap.put(dto.getUserId(), dto.getCount() + totalMap.get(dto.getUserId()));
            } else {
                totalMap.put(dto.getUserId(), dto.getCount());
            }
        });
        List<UserCollectDataAnalysisDto> dtoList = new ArrayList<>();
        page.getContent().forEach(n->{
            UserCollectDataAnalysisDto dto = new UserCollectDataAnalysisDto();
            dto.setUserId(n.getUserId());
            GarbageUserEntity garbageUserEntity = userEntityMap.get(n.getUserId());
            String placeName = garbageUserEntity.getProvinceName() + garbageUserEntity.getCityName() + garbageUserEntity.getCountryName();
            String belongTown = (garbageUserEntity.getTownName() == null ?"": garbageUserEntity.getTownName()) +
                    (garbageUserEntity.getVillageName() == null ?"":garbageUserEntity.getVillageName()) +
                    (garbageUserEntity.getCommunityName() == null ?"":garbageUserEntity.getCommunityName());
            dto.setBelongTown(belongTown);
            dto.setAddress(garbageUserEntity.getAddress());
            dto.setPlaceName(placeName);
            dto.setName(garbageUserEntity.getName());
            BigDecimal weightBigDecimal = new BigDecimal(n.getTotalWeight());
            Double weight = weightBigDecimal.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
            dto.setTotalWeight(weight);
            Long qualityCount = qualityMap.get(n.getUserId() + "-" + Constants.garbageQuality.QUALIFIED.getType());
            Long noQualityCount = qualityMap.get(n.getUserId() + "-" + Constants.garbageQuality.NOTQUALIFIED.getType());
            Long emptyCount = qualityMap.get(n.getUserId() + "-" + Constants.garbageQuality.EMPTY.getType());
            dto.setQualityCount(qualityCount == null?0:qualityCount);
            dto.setNoQualityCount(noQualityCount == null?0:noQualityCount);
            dto.setEmptyCount(emptyCount == null?0:emptyCount);
            Long totalCount = totalMap.get(n.getUserId()) == null ?0L:totalMap.get(n.getUserId());
            dto.setTotalCount(totalCount);
            if (totalCount == 0){
                dto.setQualityRate("0%");
            } else {
                BigDecimal bigDecimal = new BigDecimal(dto.getQualityCount().doubleValue()*100/totalCount);
                Double d = bigDecimal.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                dto.setQualityRate(d + "%");
            }
            dtoList.add(dto);
        });
        ResponsePageData responsePageData = new ResponsePageData();
        responsePageData.setPageNo(pageNo);
        responsePageData.setPageSize(pageSize);
        responsePageData.setCount(page.getTotalPages());
        responsePageData.setLastPage(page.isLast());
        responsePageData.setFirstPage(page.isFirst());
        responsePageData.setData(dtoList);
        responsePageData.setTotalElement(page.getTotalElements());
        responsePageData.setStatus(Constants.responseStatus.Success.getStatus());
        responsePageData.setMsg("列表查询成功");
        return responsePageData;
    }

    public Sort userCollectDataAnalysisSort(String[] orderBys){
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
}
