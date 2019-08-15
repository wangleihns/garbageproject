package com.jin.env.garbage.service.garbage;

import com.jin.env.garbage.controller.garbage.GarbageCollectorController;
import com.jin.env.garbage.dao.garbage.GarbageCollectorDao;
import com.jin.env.garbage.dao.garbage.GarbageQualityPointDao;
import com.jin.env.garbage.dao.image.GarbageImageDao;
import com.jin.env.garbage.dao.point.GarbageUserPointDao;
import com.jin.env.garbage.dao.user.GarbageENoDao;
import com.jin.env.garbage.dao.user.GarbageRoleDao;
import com.jin.env.garbage.dao.user.GarbageUserDao;
import com.jin.env.garbage.entity.garbage.GarbageCollectorEntity;
import com.jin.env.garbage.entity.garbage.GarbageQualityPointEntity;
import com.jin.env.garbage.entity.image.GarbageImageEntity;
import com.jin.env.garbage.entity.point.GarbageUserPointEntity;
import com.jin.env.garbage.entity.user.GarbageENoEntity;
import com.jin.env.garbage.entity.user.GarbageRoleEntity;
import com.jin.env.garbage.entity.user.GarbageUserEntity;
import com.jin.env.garbage.jwt.JwtUtil;
import com.jin.env.garbage.utils.Constants;
import com.jin.env.garbage.utils.ResponseData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
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
        GarbageQualityPointEntity qualityPointEntity = garbageQualityPointDao.findByPlaceIdAndType(villageId, Constants.garbagePointFromType.TOWN.getType());
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
        if (userPointEntity == null) {
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

    public ResponseData communityGarbageList(Integer pageNo, Integer pageSize, boolean isCheck, Double weight,
                                             Integer point, Integer quality, String eNo, String name, String phone,
                                             Integer garbageType, String jwt ,String[] orderBys) {
        Integer sub = jwtUtil.getSubject(jwt);
        List<GarbageRoleEntity> roleEntityList = garbageRoleDao.findByUserId(sub);
        Pageable pageable = PageRequest.of(pageNo - 1, pageSize, getCommunityGarbageSort(orderBys));
        Page<GarbageCollectorEntity> page = garbageCollectorDao.findAll(new Specification<GarbageCollectorEntity>() {
            @Override
            public Predicate toPredicate(Root<GarbageCollectorEntity> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
               List<Predicate> predicateList = new ArrayList<>();
                Predicate isCheckPredicate = criteriaBuilder.equal(root.get("isCheck"), isCheck);
                predicateList.add(isCheckPredicate);
                if (weight != null) {
                    Predicate weightPredicate = criteriaBuilder.greaterThanOrEqualTo(root.get("garbageWeight"), weight);
                    predicateList.add(weightPredicate);
                }
                if (point != null) {
                    Predicate pointPredicate = criteriaBuilder.greaterThanOrEqualTo(root.get("garbagePoint"), point);
                    predicateList.add(pointPredicate);
                }
                if (garbageType != 0){
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
                if (StringUtils.isEmpty(name) ){}

                return null;
            }
        }, pageable);
        return null;
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
}
