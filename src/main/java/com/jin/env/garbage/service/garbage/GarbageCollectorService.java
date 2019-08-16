package com.jin.env.garbage.service.garbage;

import com.jin.env.garbage.controller.garbage.GarbageCollectorController;
import com.jin.env.garbage.dao.garbage.GarbageCollectorDao;
import com.jin.env.garbage.dao.garbage.GarbageQualityPointDao;
import com.jin.env.garbage.dao.image.GarbageImageDao;
import com.jin.env.garbage.dao.point.GarbageUserPointDao;
import com.jin.env.garbage.dao.user.GarbageENoDao;
import com.jin.env.garbage.dao.user.GarbageRoleDao;
import com.jin.env.garbage.dao.user.GarbageUserDao;
import com.jin.env.garbage.dto.garbage.CollectorDto;
import com.jin.env.garbage.dto.user.UserVillageDto;
import com.jin.env.garbage.entity.garbage.GarbageCollectorEntity;
import com.jin.env.garbage.entity.garbage.GarbageQualityPointEntity;
import com.jin.env.garbage.entity.image.GarbageImageEntity;
import com.jin.env.garbage.entity.point.GarbageUserPointEntity;
import com.jin.env.garbage.entity.user.GarbageENoEntity;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.*;
import java.util.stream.Collectors;

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

    @Transactional
    public ResponseData addGarbageByCollector(String eNo, String quality, Double weight, Integer imageId, String jwt) {
        Integer sub = jwtUtil.getSubject(jwt);
        //垃圾收集员
        GarbageUserEntity collector = garbageUserDao.findById(sub).get();

        Integer provinceId = collector.getProvinceId();
        Integer cityId = collector.getCityId();
        Integer countryId = collector.getDistrictId();
        Integer townId = collector.getTownId();
        Integer villageId = collector.getVillageId();
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
        collectorEntity.setGarbageFromType(Constants.garbageFrom.GARBAGETRUCK.getType());
        collectorEntity.setGarbageWeight(weight);
        Constants.garbageQuality garbageQuality = null;
        GarbageQualityPointEntity qualityPointEntity = garbageQualityPointDao.findByPlaceIdAndType(villageId, Constants.garbageFromType.TOWN.getType());
        Integer pointScore = 0;
        switch (quality){
            case "1":
                garbageQuality = Constants.garbageQuality.QUALIFIED;
                if (qualityPointEntity == null){
                    pointScore = pointScoreForQualified;
                } else {
                    pointScore = qualityPointEntity.getQualified();
                }
                break;
            case "2":
                garbageQuality = Constants.garbageQuality.NOTQUALIFIED;
                if (qualityPointEntity == null){
                    pointScore = pointScoreForNoQualified;
                } else {
                    pointScore = qualityPointEntity.getNoQualified();
                }
                break;
            default:
                garbageQuality = Constants.garbageQuality.EMPTY;
                if (qualityPointEntity == null){
                    pointScore = pointScoreForEmpty;
                } else {
                    pointScore = qualityPointEntity.getEmpty();
                }
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
            userPointEntity.setCountryName(collector.getDistrictName());
            userPointEntity.setTownName(collector.getTownName());
            userPointEntity.setVillageName(collector.getVillageName());
            userPointEntity.setAddress(userEntity.getAddress());
            userPointEntity.setPhone(userEntity.getPhone());
        }
        //计算总积分
        garbageUserPointDao.save(userPointEntity);
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
        Integer cityId = collector.getCityId();
        Integer countryId = collector.getDistrictId();
        Integer townId = collector.getTownId();
        Integer villageId = collector.getVillageId();
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
        collectorEntity.setGarbageFromType(Constants.garbageFrom.AUTOTRUCK.getType());
        collectorEntity.setGarbageWeight(weight);
        collectorEntity.setCheck(false);
        garbageCollectorDao.save(collectorEntity);
        GarbageImageEntity imageEntity = garbageImageDao.findById(imageId).get();
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
            if ("COMMUNITY_REMARK".equals(roleEntity.getRoleDesc())){
                //评分员可以平分
                flag = true;
            }
            if ("COMMUNITY_ADMIN".equals(roleEntity.getRoleDesc())){
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
                        case "rolecode":
                            property = "roleCode";
                            break;
                        case "rolename":
                            property = "roleName";
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
            if ("COMMUNITY_REMARK".equals(roleEntity.getRoleDesc())){
                //评分员可以平分
                flag = true;
            }
            if ("COMMUNITY_ADMIN".equals(roleEntity.getRoleDesc())){
                //小区管理员可以查看
                flag = true;
            }
        }
        if (!flag){
            throw new RuntimeException("没有权限访问");
        }
        Integer communityId = userEntity.getCommunityId();
        GarbageQualityPointEntity qualityPointEntity = garbageQualityPointDao.findByPlaceIdAndType(communityId, Constants.garbageFromType.COMMUNITY.getType());
        Integer pointScore = 0;
        Constants.garbageQuality garbageQuality = null;
        switch (quality){
            case 1:
                garbageQuality = Constants.garbageQuality.QUALIFIED;
                if (qualityPointEntity == null){
                    pointScore = pointScoreForQualified;
                } else {
                    pointScore = qualityPointEntity.getQualified();
                }
                break;
            case 2:
                garbageQuality = Constants.garbageQuality.NOTQUALIFIED;
                if (qualityPointEntity == null){
                    pointScore = pointScoreForNoQualified;
                } else {
                    pointScore = qualityPointEntity.getNoQualified();
                }
                break;
            default:
                garbageQuality = Constants.garbageQuality.EMPTY;
                if (qualityPointEntity == null){
                    pointScore = pointScoreForEmpty;
                } else {
                    pointScore = qualityPointEntity.getEmpty();
                }
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
            userPointEntity.setCountryName(userEntity.getDistrictName());
            userPointEntity.setTownName(userEntity.getTownName());
            userPointEntity.setVillageName(userEntity.getVillageName());
            userPointEntity.setAddress(userEntity.getAddress());
            userPointEntity.setPhone(userEntity.getPhone());
        }
        //计算总积分
        garbageUserPointDao.save(userPointEntity);
        ResponseData responseData = new ResponseData();
        responseData.setStatus(Constants.responseStatus.Success.getStatus());
        responseData.setMsg("垃圾分类质量判定成功");
        logger.info("垃圾分类质量判定成功");
        return responseData;
    }

    @Transactional
    public ResponseData getGarbageSummaryInfo(Integer pageNo, Integer pageSize, String startTime, String endTime,
                                              String type, String phone, String name, String jwt, String[] orderBys, Integer provinceId,
                                              Integer cityId, Integer countryId, Integer townId, Integer villageId) {
        Integer sub = jwtUtil.getSubject(jwt);
        GarbageUserEntity userEntity = garbageUserDao.findById(sub).get();
        Integer fromType = userEntity.getFromType();
        Long start = 0L;
        Long end = 0L;
        if ("day".equals(type)){
            start = DateFormatUtil.parse(startTime,"yyyy-MM-dd").getTime();
            end = DateFormatUtil.parse(endTime + " 23:59:59", "yyyy-MM-dd HH:mm:ss").getTime();
        } else if ("month".equals(type)){
            start = DateFormatUtil.getFirstDateOfMonth(startTime).getTime();
            end = DateFormatUtil.getLastDateOfMonth(endTime).getTime();
        }else if ("year".equals(type)){
            start = DateFormatUtil.getFirstDayOfYear(startTime).getTime();
            end = DateFormatUtil.getLastDayOfYear(endTime).getTime();
        }else {

        }
        List<GarbageRoleEntity> roleEntityList =userEntity.getRoles().stream().collect(Collectors.toList());

        List<String> roles = roleEntityList.stream().map(garbageRoleEntity -> garbageRoleEntity.getRoleCode()).collect(Collectors.toList());

        Pageable pageable = PageRequest.of(pageNo - 1, pageSize, getGarbageSummaryInfoSort(orderBys));
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
            criteriaQuery.where(criteriaBuilder.equal(root.get("communityId"),userEntity.getCommunityId()));
        } else {
            if (roles.contains("VILLAGE_ADMIN")){
                criteriaQuery.where(criteriaBuilder.equal(root.get("villageId"), userEntity.getVillageId()));
            }
            if (roles.contains("TOWN_ADMIN")){
                criteriaQuery.where(criteriaBuilder.equal(root.get("townId"), userEntity.getTownId()));
                if (villageId !=0){
                    criteriaQuery.where(criteriaBuilder.equal(root.get("villageId"), villageId));
                }
            }
            if (roles.contains("COUNTRY_ADMIN")){
                criteriaQuery.where(criteriaBuilder.equal(root.get("countryId"), userEntity.getDistrictId()));
                if (townId !=0){
                    criteriaQuery.where(criteriaBuilder.equal(root.get("townId"), townId));
                }
                if (villageId !=0){
                    criteriaQuery.where(criteriaBuilder.equal(root.get("villageId"), villageId));
                }
            }

            if (roles.contains("CITY_ADMIN")){
                criteriaQuery.where(criteriaBuilder.equal(root.get("cityId"), userEntity.getCityId()));
                if (countryId !=0){
                    criteriaQuery.where(criteriaBuilder.equal(root.get("countryId"), userEntity.getDistrictId()));
                }
                if (townId !=0){
                    criteriaQuery.where(criteriaBuilder.equal(root.get("townId"), townId));
                }
                if (villageId !=0){
                    criteriaQuery.where(criteriaBuilder.equal(root.get("villageId"), villageId));
                }
            }

            if (roles.contains("PROVINCE_ADMIN")){
                criteriaQuery.where(criteriaBuilder.equal(root.get("provinceId"), userEntity.getProvinceId()));
                if (cityId !=0){
                    criteriaQuery.where(criteriaBuilder.equal(root.get("cityId"), cityId));
                }
                if (countryId !=0){
                    criteriaQuery.where(criteriaBuilder.equal(root.get("countryId"), userEntity.getDistrictId()));
                }
                if (townId !=0){
                    criteriaQuery.where(criteriaBuilder.equal(root.get("townId"), townId));
                }
                if (villageId !=0){
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

    private Sort getGarbageSummaryInfoSort(String[] orderBys){
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
