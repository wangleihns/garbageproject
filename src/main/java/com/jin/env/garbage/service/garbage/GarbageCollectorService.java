package com.jin.env.garbage.service.garbage;

import com.jin.env.garbage.controller.garbage.GarbageCollectorController;
import com.jin.env.garbage.dao.garbage.GarbageCollectorDao;
import com.jin.env.garbage.dao.garbage.GarbageQualityPointDao;
import com.jin.env.garbage.dao.image.GarbageImageDao;
import com.jin.env.garbage.dao.point.GarbagePointRecordDao;
import com.jin.env.garbage.dao.point.GarbageUserPointDao;
import com.jin.env.garbage.dao.user.GarbageENoDao;
import com.jin.env.garbage.dao.user.GarbageRoleCommunityDao;
import com.jin.env.garbage.dao.user.GarbageRoleDao;
import com.jin.env.garbage.dao.user.GarbageUserDao;
import com.jin.env.garbage.dto.garbage.CollectorDto;
import com.jin.env.garbage.dto.garbage.GarbageCollectCountDto;
import com.jin.env.garbage.dto.garbage.GarbageWeightInMonth;
import com.jin.env.garbage.dto.user.UserCountInMonth;
import com.jin.env.garbage.dto.user.UserVillageDto;
import com.jin.env.garbage.entity.garbage.GarbageCollectorEntity;
import com.jin.env.garbage.entity.garbage.GarbageQualityPointEntity;
import com.jin.env.garbage.entity.image.GarbageImageEntity;
import com.jin.env.garbage.entity.point.GarbagePointRecordEntity;
import com.jin.env.garbage.entity.point.GarbageUserPointEntity;
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
import java.text.DecimalFormat;
import java.util.*;
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

    @Transactional
    public ResponseData addGarbageByCollector(String eNo, String quality, Double weight, Integer imageId, String jwt) {
        Integer sub = jwtUtil.getSubject(jwt);
        //垃圾收集员
        GarbageUserEntity collector = garbageUserDao.findById(sub).get();

        Integer provinceId = collector.getProvinceId();
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
        collectorEntity.setProvinceId(provinceId);
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
            userPointEntity.setProvinceId(collectorEntity.getProvinceId());
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
        Integer provinceId = collector.getProvinceId();
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
        collectorEntity.setProvinceId(provinceId);
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
        collectorEntity.setGarbageFromType(Constants.garbageFromType.COMMUNITY.getType());
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
                                             Integer point, Integer quality, String eNo, String name, String phone,
                                             Integer garbageType, String jwt ,String[] orderBys) {
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
        Pageable pageable = PageRequest.of(pageNo - 1, pageSize, getCommunityGarbageSort(orderBys));
        Page<GarbageCollectorEntity> page = garbageCollectorDao.findAll(new Specification<GarbageCollectorEntity>() {
            @Override
            public Predicate toPredicate(Root<GarbageCollectorEntity> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
               List<Predicate> predicateList = new ArrayList<>();
               Predicate predicateFrom = criteriaBuilder.equal(root.get("garbageFromType"), Constants.garbageFromType.COMMUNITY.getType());
                predicateList.add(predicateFrom);
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
                if (!StringUtils.isEmpty(eNo)){
                    Predicate eNoPredicate = criteriaBuilder.equal(root.get("eNo"), eNo);
                    predicateList.add(eNoPredicate);
                }
                if (!StringUtils.isEmpty(name) || !StringUtils.isEmpty(phone)){
                    List<Predicate> predicates = new ArrayList<>();
                    Subquery subquery = criteriaQuery.subquery(GarbageUserEntity.class);
                    Root subRoot = subquery.from(GarbageUserEntity.class);
                    subquery.select(subRoot.get("id"));
                    Predicate predicate = criteriaBuilder.equal(root.get("userId"), subRoot.get("id"));
                    predicates.add(predicate);
                    if (!StringUtils.isEmpty(name)){
                        Predicate namePredicate  = criteriaBuilder.like(subRoot.get("name"), "%" + name + "%");
                        predicates.add(namePredicate);
                    }
                    if (!StringUtils.isEmpty(phone)){
                        Predicate phonePredicate  = criteriaBuilder.like(subRoot.get("phone"), "%" + phone + "%");
                        predicates.add(phonePredicate);
                    }
                    Predicate exists = criteriaBuilder.exists(subquery.where(predicates.toArray(new Predicate[predicates.size()])));
                    predicateList.add(exists);
                }

                return criteriaBuilder.and(predicateList.toArray(new Predicate[predicateList.size()]));
            }
        }, pageable);
        ResponsePageData responsePageData = new ResponsePageData();
        responsePageData.setPageNo(pageNo);
        responsePageData.setPageSize(pageSize);
        responsePageData.setCount(page.getTotalPages());
        responsePageData.setLastPage(page.isLast());
        responsePageData.setFirstPage(page.isFirst());
        responsePageData.setData(page.getContent());
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

    public void tst(){
        System.out.println(222);
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
        Integer communityId = userEntity.getCommunityId();
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
        garbageCollectorDao.save(collectorEntity);

        GarbageUserPointEntity userPointEntity = garbageUserPointDao.findByUserId(collectorEntity.getUserId());
        if (userPointEntity != null) {
            userPointEntity.setPoint(userPointEntity.getPoint() + pointScore);
        } else {
            userPointEntity = new GarbageUserPointEntity();
            userPointEntity.setUserId(collectorEntity.getUserId());
            userPointEntity.setPoint(pointScore);
            userPointEntity.setProvinceName(userEntity.getProvinceName());
            userPointEntity.setCityName(userEntity.getCityName());
            userPointEntity.setCountryName(userEntity.getCountryName());
            userPointEntity.setTownName(userEntity.getTownName());
            userPointEntity.setVillageName(userEntity.getVillageName());
            userPointEntity.setAddress(userEntity.getAddress());
            userPointEntity.setPhone(userEntity.getPhone());
            userPointEntity.setProvinceId(collectorEntity.getProvinceId());
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
                                                     Long cityId, Long countryId, Long townId, Long villageId) {
        Integer sub = jwtUtil.getSubject(jwt);
        GarbageUserEntity userEntity = garbageUserDao.findById(sub).get();
        Integer fromType = userEntity.getFromType();
        Long start = 0L;
        Long end = 0L;
        if ("day".equals(type)){
            start = DateFormatUtil.parse(startTime,"yyyy-MM-dd").getTime();
            end = DateFormatUtil.getLastTimeOfDay(startTime).getTime();
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
        criteriaQuery.where(criteriaBuilder.equal(root.get("garbageFromType"), fromType));
        if (!StringUtils.isEmpty(name)){
            criteriaQuery.where(criteriaBuilder.like(root.get("collectorName"), "%" + name +"%"));
        }
        if (!StringUtils.isEmpty(phone)){
            criteriaQuery.where(criteriaBuilder.like(root.get("collectorPhone"), "%" + phone +"%"));
        }
        if (!StringUtils.isEmpty(startTime)&& !StringUtils.isEmpty(endTime)){
            criteriaQuery.where(criteriaBuilder.greaterThanOrEqualTo(root.<Long>get("collectDate"), finalStart));
            criteriaQuery.where(criteriaBuilder.lessThanOrEqualTo(root.<Long>get("collectDate"), finalEnd));
        }
        if (fromType == 1){
            if (communityIds.size() > 0){
                criteriaQuery.where(root.get("communityId").in(communityIds));
            }

        } else {
            if (roles.contains("VILLAGE_ADMIN")){
                criteriaQuery.where(criteriaBuilder.equal(root.get("villageId"), userEntity.getVillageId()));
            }
            if (roles.contains("TOWN_ADMIN")){
                criteriaQuery.where(criteriaBuilder.equal(root.get("townId"), userEntity.getTownId()));
                if (villageId!=null && villageId !=0){
                    criteriaQuery.where(criteriaBuilder.equal(root.get("villageId"), villageId));
                }
            }
            if (roles.contains("COUNTRY_ADMIN")){
                criteriaQuery.where(criteriaBuilder.equal(root.get("countryId"), userEntity.getCountryId()));
                if (townId!=null && townId !=0){
                    criteriaQuery.where(criteriaBuilder.equal(root.get("townId"), townId));
                }
                if (villageId!=null && villageId !=0){
                    criteriaQuery.where(criteriaBuilder.equal(root.get("villageId"), villageId));
                }
            }

            if (roles.contains("CITY_ADMIN")){
                criteriaQuery.where(criteriaBuilder.equal(root.get("cityId"), userEntity.getCityId()));
                if (countryId!=null &&  countryId !=0){
                    criteriaQuery.where(criteriaBuilder.equal(root.get("countryId"), countryId));
                }
                if (townId!=null && townId !=0){
                    criteriaQuery.where(criteriaBuilder.equal(root.get("townId"), townId));
                }
                if (villageId!=null && villageId !=0){
                    criteriaQuery.where(criteriaBuilder.equal(root.get("villageId"), villageId));
                }
            }

            if (roles.contains("PROVINCE_ADMIN")){
                criteriaQuery.where(criteriaBuilder.equal(root.get("provinceId"), userEntity.getProvinceId()));
                if (cityId != null && cityId !=0){
                    criteriaQuery.where(criteriaBuilder.equal(root.get("cityId"), cityId));
                }
                if (countryId!=null && countryId !=0){
                    criteriaQuery.where(criteriaBuilder.equal(root.get("countryId"), countryId));
                }
                if (townId!=null && townId !=0){
                    criteriaQuery.where(criteriaBuilder.equal(root.get("townId"), townId));
                }
                if (villageId!=null && villageId !=0){
                    criteriaQuery.where(criteriaBuilder.equal(root.get("villageId"), villageId));
                }
            }
        }
        if ("day".equals(type)){
            criteriaQuery.groupBy(root.get("collectorId"), root.get("day"), root.get("month"), root.get("year"));
        } else if ("month".equals(type)){
            criteriaQuery.groupBy(root.get("collectorId"), root.get("month"), root.get("year"));
        }else{
            criteriaQuery.groupBy(root.get("collectorId"), root.get("year"));
        }
        TypedQuery<CollectorDto> createQuery = entityManager.createQuery(criteriaQuery);
        createQuery.setFirstResult(pageNo*pageSize);
        createQuery.setMaxResults(pageSize);
        List<CollectorDto> counts = entityManager.createQuery(criteriaQuery).getResultList();
        PageImpl<CollectorDto> page = new PageImpl<>(counts, pageable, Long.valueOf(counts.size()));
        List<Integer> collectorIds = page.getContent().stream().map(dto->dto.getCollectorId()).collect(Collectors.toList());
        List<UserVillageDto> villageDtos = garbageUserDao.getUserVillageNameByIds(collectorIds);
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


    public ResponseData getGarbageCollectSummaryInfoInPlace(Integer pageNo, Integer pageSize, String startTime, String endTime, String type, Integer provinceId, Long cityId, Long countryId, Long townId, Long villageId, String[] orderBys, String jwt) {
        Integer sub = jwtUtil.getSubject(jwt);
        GarbageUserEntity userEntity = garbageUserDao.findById(sub).get();
        Integer fromType = userEntity.getFromType();// 0农村  1城市
        Long start = 0L;
        Long end = 0L;
        if ("day".equals(type)){
            start = DateFormatUtil.getFirstTimeOfDay(startTime).getTime();
            end = DateFormatUtil.getFirstTimeOfDay(startTime).getTime();
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
        List<Integer> communityIds = getCommunityResource(roleEntityList);
        Long userCount = garbageUserDao.count(new Specification<GarbageUserEntity>() {
            @Nullable
            @Override
            public Predicate toPredicate(Root<GarbageUserEntity> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList<>();
                if (fromType == 1){
                    //小区
                    if (communityIds.size() > 0){
                        Predicate predicate =  root.get("communityId").in(communityIds);
                        predicates.add(predicate);
                    }
                }else{
                    if (roles.contains("VILLAGE_ADMIN")){
                        if (townId !=null && townId != 0 ){
                            Predicate predicateVillageId= criteriaBuilder.equal(root.get("villageId"), userEntity.getVillageId());
                            predicates.add(predicateVillageId);
                        }
                    }
                    if (roles.contains("TOWN_ADMIN")){
                        if (townId !=null && townId != 0 ){
                            Predicate predicateTownId= criteriaBuilder.equal(root.get("townId"), userEntity.getTownId());
                            predicates.add(predicateTownId);
                        }
                        if (townId !=null && townId != 0 ){
                            Predicate predicateVillageId= criteriaBuilder.equal(root.get("villageId"), villageId);
                            predicates.add(predicateVillageId);
                        }
                    }
                    if (roles.contains("COUNTRY_ADMIN")){
                        if (countryId !=null && countryId != 0){
                            Predicate predicateCountryId = criteriaBuilder.equal(root.get("districtId"), userEntity.getCountryId());
                            predicates.add(predicateCountryId);
                        }
                        if (townId !=null && townId != 0 ){
                            Predicate predicateTownId= criteriaBuilder.equal(root.get("townId"), townId);
                            predicates.add(predicateTownId);
                        }
                        if (townId !=null && townId != 0 ){
                            Predicate predicateTownId= criteriaBuilder.equal(root.get("townId"), townId);
                            predicates.add(predicateTownId);
                        }
                        if (townId !=null && townId != 0 ){
                            Predicate predicateVillageId= criteriaBuilder.equal(root.get("villageId"), villageId);
                            predicates.add(predicateVillageId);
                        }
                    }
                    if (roles.contains("CITY_ADMIN")){
                        if (cityId !=null && cityId != 0){
                            Predicate predicateCityId = criteriaBuilder.equal(root.get("cityId"), userEntity.getCityId());
                            predicates.add(predicateCityId);
                        }
                        if (countryId !=null && countryId != 0){
                            Predicate predicateCountryId = criteriaBuilder.equal(root.get("districtId"), countryId);
                            predicates.add(predicateCountryId);
                        }
                        if (townId !=null && townId != 0 ){
                            Predicate predicateTownId= criteriaBuilder.equal(root.get("townId"), townId);
                            predicates.add(predicateTownId);
                        }
                        if (townId !=null && townId != 0 ){
                            Predicate predicateTownId= criteriaBuilder.equal(root.get("townId"), townId);
                            predicates.add(predicateTownId);
                        }
                        if (townId !=null && townId != 0 ){
                            Predicate predicateVillageId= criteriaBuilder.equal(root.get("villageId"), villageId);
                            predicates.add(predicateVillageId);
                        }
                    }
                    if (roles.contains("PROVINCE_ADMIN")){
                        if (provinceId !=null && provinceId != 0){
                            Predicate predicateProvinceId = criteriaBuilder.equal(root.get("provinceId"), userEntity.getProvinceId());
                            predicates.add(predicateProvinceId);
                        }
                        if (cityId !=null && cityId != 0){
                            Predicate predicateCityId = criteriaBuilder.equal(root.get("cityId"), cityId);
                            predicates.add(predicateCityId);
                        }
                        if (countryId !=null && countryId != 0){
                            Predicate predicateCountryId = criteriaBuilder.equal(root.get("districtId"), countryId);
                            predicates.add(predicateCountryId);
                        }
                        if (townId !=null && townId != 0 ){
                            Predicate predicateTownId= criteriaBuilder.equal(root.get("townId"), townId);
                            predicates.add(predicateTownId);
                        }
                        if (townId !=null && townId != 0 ){
                            Predicate predicateTownId= criteriaBuilder.equal(root.get("townId"), townId);
                            predicates.add(predicateTownId);
                        }
                        if (townId !=null && townId != 0 ){
                            Predicate predicateVillageId= criteriaBuilder.equal(root.get("villageId"), villageId);
                            predicates.add(predicateVillageId);
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
        if (provinceId != null &&provinceId !=0){
            criteriaQuery.multiselect( root.get("provinceId"),criteriaBuilder.count(root.get("garbageQuality")), root.get("garbageQuality"));
        }
        if (cityId !=null && cityId !=0){
            criteriaQuery.multiselect( root.get("cityId"),criteriaBuilder.count(root.get("garbageQuality")), root.get("garbageQuality"));
        }
        if (countryId !=null && countryId !=0){
            criteriaQuery.multiselect( root.get("countryId"),criteriaBuilder.count(root.get("garbageQuality")), root.get("garbageQuality"));
        }
        if (townId!=null && townId !=0){
            criteriaQuery.multiselect( root.get("townId"),criteriaBuilder.count(root.get("garbageQuality")), root.get("garbageQuality"));
        }
        if (villageId!=null && villageId !=0){
            criteriaQuery.multiselect( root.get("villageId"),criteriaBuilder.count(root.get("garbageQuality")), root.get("garbageQuality"));
        }
        if (fromType == 1){
            criteriaQuery.multiselect( root.get("communityId"),criteriaBuilder.count(root.get("garbageQuality")), root.get("garbageQuality"));
        }

        if (!StringUtils.isEmpty(startTime)&& !StringUtils.isEmpty(endTime)){
            criteriaQuery.where(criteriaBuilder.greaterThanOrEqualTo(root.<Long>get("collectDate"), start));
            criteriaQuery.where(criteriaBuilder.lessThanOrEqualTo(root.<Long>get("collectDate"), end));
        }
        criteriaQuery.where(criteriaBuilder.isTrue(root.get("check")));
        if (fromType == 1){
            if (communityIds.size() > 0){
                Predicate predicate =  root.get("communityId").in(communityIds);
                criteriaQuery.where(predicate);
            }
        } else {
            if (roles.contains("VILLAGE_ADMIN")){
                criteriaQuery.where(criteriaBuilder.equal(root.get("villageId"), userEntity.getVillageId()));
            }
            if (roles.contains("TOWN_ADMIN")){
                criteriaQuery.where(criteriaBuilder.equal(root.get("townId"), userEntity.getTownId()));
                if (villageId!=null && villageId !=0){
                    criteriaQuery.where(criteriaBuilder.equal(root.get("villageId"), villageId));
                }
            }
            if (roles.contains("COUNTRY_ADMIN")){
                criteriaQuery.where(criteriaBuilder.equal(root.get("countryId"), userEntity.getCountryId()));
                if (townId!=null && townId !=0){
                    criteriaQuery.where(criteriaBuilder.equal(root.get("townId"), townId));
                }
                if (villageId!=null && villageId !=0){
                    criteriaQuery.where(criteriaBuilder.equal(root.get("villageId"), villageId));
                }
            }

            if (roles.contains("CITY_ADMIN")){
                criteriaQuery.where(criteriaBuilder.equal(root.get("cityId"), userEntity.getCityId()));
                if (countryId!=null &&  countryId !=0){
                    criteriaQuery.where(criteriaBuilder.equal(root.get("countryId"), countryId));
                }
                if (townId!=null && townId !=0){
                    criteriaQuery.where(criteriaBuilder.equal(root.get("townId"), townId));
                }
                if (villageId!=null && villageId !=0){
                    criteriaQuery.where(criteriaBuilder.equal(root.get("villageId"), villageId));
                }
            }

            if (roles.contains("PROVINCE_ADMIN")){
                criteriaQuery.where(criteriaBuilder.equal(root.get("provinceId"), userEntity.getProvinceId()));
                if (cityId != null && cityId !=0){
                    criteriaQuery.where(criteriaBuilder.equal(root.get("cityId"), cityId));
                }
                if (countryId!=null && countryId !=0){
                    criteriaQuery.where(criteriaBuilder.equal(root.get("countryId"), countryId));
                }
                if (townId!=null && townId !=0){
                    criteriaQuery.where(criteriaBuilder.equal(root.get("townId"), townId));
                }
                if (villageId!=null && villageId !=0){
                    criteriaQuery.where(criteriaBuilder.equal(root.get("villageId"), villageId));
                }
            }
        }
        if (fromType == 1){
            //小区
            criteriaQuery.where(criteriaBuilder.equal(root.get("garbageFromType"), Constants.garbageFromType.COMMUNITY.getType()));
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
            criteriaQuery.where(criteriaBuilder.equal(root.get("garbageFromType"), Constants.garbageFromType.TOWN.getType()));
            if ("day".equals(type)){
                if (provinceId!=null && provinceId !=0){
                    criteriaQuery.groupBy(root.get("garbageQuality"), root.get("day"), root.get("month"), root.get("year"), root.get("provinceId"));
                }
                if (cityId !=null && cityId !=0){
                    criteriaQuery.groupBy(root.get("garbageQuality"), root.get("day"), root.get("month"), root.get("year"),root.get("provinceId"), root.get("cityId"));
                }
                if (countryId !=null && countryId !=0){
                    criteriaQuery.groupBy(root.get("garbageQuality"), root.get("day"), root.get("month"), root.get("year"),root.get("provinceId"), root.get("cityId"), root.get("countryId"));
                }
                if (townId !=null && townId !=0){
                    criteriaQuery.groupBy(root.get("garbageQuality"), root.get("day"), root.get("month"), root.get("year"),root.get("provinceId"), root.get("cityId"), root.get("countryId"), root.get("townId"));
                }
                if (villageId !=null && villageId !=0){
                    criteriaQuery.groupBy(root.get("garbageQuality"), root.get("day"), root.get("month"), root.get("year"),root.get("provinceId"), root.get("cityId"), root.get("countryId"), root.get("townId"), root.get("villageId"));
                }

            } else if ("month".equals(type)){
                if (provinceId!=null && provinceId !=0){
                    criteriaQuery.groupBy(root.get("garbageQuality"), root.get("month"), root.get("year"), root.get("provinceId"));
                }
                if (cityId !=null && cityId !=0){
                    criteriaQuery.groupBy(root.get("garbageQuality"),  root.get("month"), root.get("year"),root.get("provinceId"), root.get("cityId"));
                }
                if (countryId !=null && countryId !=0){
                    criteriaQuery.groupBy(root.get("garbageQuality"), root.get("month"), root.get("year"),root.get("provinceId"), root.get("cityId"), root.get("countryId"));
                }
                if (townId !=null && townId !=0){
                    criteriaQuery.groupBy(root.get("garbageQuality"), root.get("month"), root.get("year"),root.get("provinceId"), root.get("cityId"), root.get("countryId"), root.get("townId"));
                }
                if (villageId !=null && villageId !=0){
                    criteriaQuery.groupBy(root.get("garbageQuality"),  root.get("month"), root.get("year"),root.get("provinceId"), root.get("cityId"), root.get("countryId"), root.get("townId"), root.get("villageId"));
                }
            }else{
                if (provinceId!=null && provinceId !=0){
                    criteriaQuery.groupBy(root.get("garbageQuality"), root.get("year"), root.get("provinceId"));
                }
                if (cityId !=null && cityId !=0){
                    criteriaQuery.groupBy(root.get("garbageQuality"), root.get("year"),root.get("provinceId"), root.get("cityId"));
                }
                if (countryId !=null && countryId !=0){
                    criteriaQuery.groupBy(root.get("garbageQuality"), root.get("year"),root.get("provinceId"), root.get("cityId"), root.get("countryId"));
                }
                if (townId !=null && townId !=0){
                    criteriaQuery.groupBy(root.get("garbageQuality"), root.get("year"),root.get("provinceId"), root.get("cityId"), root.get("countryId"), root.get("townId"));
                }
                if (villageId !=null && villageId !=0){
                    criteriaQuery.groupBy(root.get("garbageQuality"),  root.get("year"),root.get("provinceId"), root.get("cityId"), root.get("countryId"), root.get("townId"), root.get("villageId"));
                }
            }
        }
        List<GarbageCollectCountDto> garbageQualityCount = entityManager.createQuery(criteriaQuery).getResultList();

        Map<String, Long> qualityMap = new HashMap<>();
        for (GarbageCollectCountDto dto: garbageQualityCount){
            qualityMap.put(dto.getPlaceId() + "-" + dto.getGarbageQuality(), dto.getCount());
        }
        int sum = garbageQualityCount.stream().mapToInt(dto -> dto.getCount().intValue()).sum();
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<GarbageCollectCountDto> cq = cb.createQuery(GarbageCollectCountDto.class);
        Root<GarbageCollectorEntity> rootPlace = cq.from(GarbageCollectorEntity.class);
        List<Expression<?>> expressionList = new ArrayList<>();
        if (provinceId != null &&provinceId !=0){
            cq.multiselect( rootPlace.get("provinceId"), cb.sum(rootPlace.get("garbageWeight")) ,rootPlace.get("day"), rootPlace.get("month"), rootPlace.get("year"));
            expressionList.add(rootPlace.get("provinceId"));
        }
        if (cityId !=null && cityId !=0){
            cq.multiselect( rootPlace.get("cityId"), cb.sum(rootPlace.get("garbageWeight")), rootPlace.get("day"), rootPlace.get("month"), rootPlace.get("year"));
            expressionList.add(rootPlace.get("cityId"));
        }
        if (countryId !=null && countryId !=0){
            cq.multiselect( rootPlace.get("countryId"), cb.sum(rootPlace.get("garbageWeight")), rootPlace.get("day"), rootPlace.get("month"), rootPlace.get("year"));
            expressionList.add(rootPlace.get("countryId"));
        }
        if (townId!=null && townId !=0){
            cq.multiselect( rootPlace.get("townId"), cb.sum(rootPlace.get("garbageWeight")), rootPlace.get("day"), rootPlace.get("month"), rootPlace.get("year"));
            expressionList.add(rootPlace.get("townId"));
        }
        if (villageId!=null && villageId !=0){
            cq.multiselect( rootPlace.get("villageId"), cb.sum(rootPlace.get("garbageWeight")), rootPlace.get("day"), rootPlace.get("month"), rootPlace.get("year"));
            expressionList.add(rootPlace.get("villageId"));
        }
        if (fromType == 1){
            cq.multiselect( rootPlace.get("communityId"), cb.sum(rootPlace.get("garbageWeight")), rootPlace.get("day"), rootPlace.get("month"), rootPlace.get("year"));
            expressionList.add(rootPlace.get("communityId"));
        }

        if (!StringUtils.isEmpty(startTime)&& !StringUtils.isEmpty(endTime)){
            cq.where(cb.greaterThanOrEqualTo(rootPlace.<Long>get("collectDate"), start));
            cq.where(cb.lessThanOrEqualTo(rootPlace.<Long>get("collectDate"), end));
        }

        if (fromType == 1){
            cq.where(cb.equal(rootPlace.get("garbageFromType"), Constants.garbageFromType.COMMUNITY.getType()));

        } else {
            cq.where(cb.equal(rootPlace.get("garbageFromType"), Constants.garbageFromType.TOWN.getType()));
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
        cq.groupBy(expressionList);

        TypedQuery<GarbageCollectCountDto> query = entityManager.createQuery(cq);
        query.setFirstResult((pageNo-1)*pageSize);
        query.setMaxResults(pageSize);
        List<GarbageCollectCountDto> dtos = query.getResultList();
        Pageable pageable = PageRequest.of(pageNo-1, pageSize, getGarbageCollectorSummaryInfo(orderBys));
        PageImpl<GarbageCollectCountDto> page = new PageImpl<>(dtos, pageable, Long.valueOf(dtos.size()));
        page.getContent().forEach(dto-> {
            Integer qualityCount = qualityMap.get(dto.getPlaceId() + "-" + Constants.garbageQuality.QUALIFIED.getType()) == null ?0:qualityMap.get(dto.getPlaceId() + "-" + Constants.garbageQuality.QUALIFIED.getType()).intValue();
            dto.setQualityCount(qualityCount);
            Integer emptyCount = qualityMap.get(dto.getPlaceId() + "-" + Constants.garbageQuality.EMPTY.getType()) == null?0: qualityMap.get(dto.getPlaceId() + "-" + Constants.garbageQuality.EMPTY.getType()).intValue();
            dto.setEmptyCount(emptyCount);
            Integer notQualityCount = qualityMap.get(dto.getPlaceId() + "-" + Constants.garbageQuality.NOTQUALIFIED.getType()) == null ? 0:qualityMap.get(dto.getPlaceId() + "-" + Constants.garbageQuality.NOTQUALIFIED.getType()).intValue();
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
            String emptuRate = df.format(emptyCount.doubleValue()/sum);
            String notQualityRate = df.format(notQualityCount.doubleValue()/sum);
            String qualityRate = df.format(qualityCount.doubleValue()/sum);
            dto.setEmptyRate(emptuRate);
            dto.setNotQualityRate(notQualityRate);
            dto.setQualityRate(qualityRate);
            dtos.add(dto);
        });
        ResponsePageData responsePageData = new ResponsePageData();
        responsePageData.setPageNo(pageNo);
        responsePageData.setPageSize(pageSize);
        responsePageData.setCount(page.getTotalPages());
        responsePageData.setLastPage(page.isLast());
        responsePageData.setFirstPage(page.isFirst());
        responsePageData.setStatus(Constants.responseStatus.Success.getStatus());
        responsePageData.setMsg("查询成功");
        responsePageData.setData(page.getContent());
        return responsePageData;
    }

    public ResponseData getNotSentGarbageInfoToSystemUser(Integer pageNo, Integer pageSize, String startTime, String endTime, String phone, String name, String jwt, String[] orderBys) {
        Integer sub = jwtUtil.getSubject(jwt);
        GarbageUserEntity userEntity = garbageUserDao.findById(sub).get();
        Integer fromType = userEntity.getFromType();
        List<GarbageRoleEntity> roleEntityList =userEntity.getRoles().stream().collect(Collectors.toList());
        List<Integer> communityIds = getCommunityResource(roleEntityList);
        List<String> roleCodes = roleEntityList.stream().map(role -> role.getRoleCode()).collect(Collectors.toList());
        Long  start = DateFormatUtil.getFirstTimeOfDay(startTime).getTime();
        Long  end = DateFormatUtil.getLastTimeOfDay(endTime).getTime();
        Pageable pageable = PageRequest.of(pageNo-1, pageSize, getGarbageCollectorSummaryInfo(orderBys));
        Page<GarbageUserEntity> garbageUserEntityPage = garbageUserDao.findAll(new Specification<GarbageUserEntity>() {
            @Nullable
            @Override
            public Predicate toPredicate(Root<GarbageUserEntity> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicateList = new ArrayList<>();
                if (!StringUtils.isEmpty(name)){
                    Predicate predicate = criteriaBuilder.like(root.get("name") , "%" + name + "%");
                    predicateList.add(predicate);
                }
                if (!StringUtils.isEmpty(phone)){
                    Predicate predicate = criteriaBuilder.like(root.get("phone") , "%" + phone + "%");
                    predicateList.add(predicate);
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
                    if (roleCodes.contains("VILLAGE_ADMIN")){
                        Predicate villagePredicate = criteriaBuilder.equal(subRoot.get("villageId"), userEntity.getVillageId());
                        selectList.add(villagePredicate);
                    }
                    if (roleCodes.contains("TOWN_ADMIN")){
                        Predicate townPredicate = criteriaBuilder.equal(subRoot.get("townId"), userEntity.getTownId());
                        selectList.add(townPredicate);
                    }
                    if (roleCodes.contains("COUNTRY_ADMIN")){
                        Predicate countryPredicate = criteriaBuilder.equal(subRoot.get("countryId"), userEntity.getCountryId());
                        selectList.add(countryPredicate);
                    }
                    if (roleCodes.contains("CITY_ADMIN")){
                        Predicate cityPredicate = criteriaBuilder.equal(subRoot.get("cityId"), userEntity.getCityId());
                        selectList.add(cityPredicate);
                    }
                    if (roleCodes.contains("PROVINCE_ADMIN")){
                        Predicate ProvincePredicate = criteriaBuilder.equal(subRoot.get("provinceId"), userEntity.getProvinceId());
                        selectList.add(ProvincePredicate);
                    }
                    if (fromType == 1){
                        if (communityIds.size() > 0){
                            Predicate predicate =  subRoot.get("communityId").in(communityIds);
                            selectList.add(predicate);
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
}
