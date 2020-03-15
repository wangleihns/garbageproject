package com.jin.env.garbage.service.garbage;

import com.jin.env.garbage.controller.garbage.GarbageCollectorController;
import com.jin.env.garbage.dao.garbage.GarbageCollectorDao;
import com.jin.env.garbage.dao.garbage.GarbageQualityPointDao;
import com.jin.env.garbage.dao.image.GarbageImageDao;
import com.jin.env.garbage.dao.message.GarbageMessageDao;
import com.jin.env.garbage.dao.point.GarbagePointRecordDao;
import com.jin.env.garbage.dao.point.GarbageUserPointDao;
import com.jin.env.garbage.dao.position.*;
import com.jin.env.garbage.dao.remark.GarbageRemarkAgainRecordDao;
import com.jin.env.garbage.dao.user.GarbageENoDao;
import com.jin.env.garbage.dao.user.GarbageRoleCommunityDao;
import com.jin.env.garbage.dao.user.GarbageRoleDao;
import com.jin.env.garbage.dao.user.GarbageUserDao;
import com.jin.env.garbage.dao.village.GarbageVillageInfoDao;
import com.jin.env.garbage.dto.garbage.*;
import com.jin.env.garbage.dto.user.UserDto;
import com.jin.env.garbage.dto.user.UserDtoForLeader;
import com.jin.env.garbage.dto.user.UserVillageDto;
import com.jin.env.garbage.entity.garbage.GarbageCollectorEntity;
import com.jin.env.garbage.entity.garbage.GarbageQualityPointEntity;
import com.jin.env.garbage.entity.image.GarbageImageEntity;
import com.jin.env.garbage.entity.message.GarbageMessageEntity;
import com.jin.env.garbage.entity.point.GarbagePointRecordEntity;
import com.jin.env.garbage.entity.point.GarbageUserPointEntity;
import com.jin.env.garbage.entity.position.*;
import com.jin.env.garbage.entity.remark.GarbageRemarkAgainRecordEntity;
import com.jin.env.garbage.entity.user.GarbageENoEntity;
import com.jin.env.garbage.entity.user.GarbageRoleCommunityEntity;
import com.jin.env.garbage.entity.user.GarbageRoleEntity;
import com.jin.env.garbage.entity.user.GarbageUserEntity;
import com.jin.env.garbage.entity.village.GarbageVillageInfoEntity;
import com.jin.env.garbage.jwt.JwtUtil;
import com.jin.env.garbage.utils.*;
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
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    @Autowired
    private GarbageVillageInfoDao garbageVillageInfoDao;

    @Autowired
    private GarbageMessageDao garbageMessageDao;

    @Autowired
    private GarbageRemarkAgainRecordDao garbageRemarkAgainRecordDao;

    @Transactional
    public ResponseData addGarbageByCollector(String eNo, String quality, Double weight, Integer imageId, Integer garbageType, String jwt) {
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
        } else {
            GarbageUserEntity userEntity = garbageUserDao.findById(eNoEntity.getUserId()).get();
            if (!collector.getVillageId().equals(userEntity.getVillageId())){
                throw new RuntimeException("非同一小区不能上传");
            }
        }
        GarbageCollectorEntity collectorEntity = new GarbageCollectorEntity();
        collectorEntity.setProvinceId(provinceId.longValue());
        collectorEntity.setCityId(cityId);
        collectorEntity.setCountryId(countryId);
        collectorEntity.setTownId(townId);
        collectorEntity.setVillageId(villageId);
        collectorEntity.setCollectorPhone(collector.getPhone());
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
        Integer sumScore = 0;
        String desc = "";
        String qualitySms = "";
        String sendRemark = "";
        switch (quality){
            case "1":
                garbageQuality = Constants.garbageQuality.QUALIFIED;
                if (qualityPointEntity == null){
                    pointScore = pointScoreForQualified;
                } else {
                    pointScore = qualityPointEntity.getQualified();
                }
                desc = "垃圾分类合格积分";
                qualitySms = Constants.sMsModelResult.QUALITY.getContent();
                sendRemark = Constants.sMsModelRemark.QUALITY.getContent();
                break;
            case "2":
                garbageQuality = Constants.garbageQuality.NOTQUALIFIED;
                if (qualityPointEntity == null){
                    pointScore = pointScoreForNoQualified;
                } else {
                    pointScore = qualityPointEntity.getNoQualified();
                }
                desc = "垃圾分类不合格积分";
                qualitySms = Constants.sMsModelResult.NOTQUALITY.getContent();
                sendRemark = Constants.sMsModelRemark.NOTQUALITY.getContent();
                break;
            default:
                garbageQuality = Constants.garbageQuality.EMPTY;
                if (qualityPointEntity == null){
                    pointScore = pointScoreForEmpty;
                } else {
                    pointScore = qualityPointEntity.getEmpty();
                }
                desc = "垃圾分类空桶积分";
                qualitySms = Constants.sMsModelResult.EMPTY.getContent();
                sendRemark = Constants.sMsModelRemark.EMPTY.getContent();
                break;
        }
        Constants.garbageType gT =  null;
        switch (garbageType){
            case 1:
                gT = Constants.garbageType.KITCHEN_GARBAGE;
                break;
            case 2:
                gT = Constants.garbageType.OTHER_GARBAGE;
                break;
            case 3:
                gT = Constants.garbageType.RECYCLEABLE;
                break;
            case 4:
                gT = Constants.garbageType.DANGER_GARBAGE;
                break;
        }
        //获取当前垃圾卡的类型
        GarbageENoEntity garbageENoEntity = garbageENoDao.findByENo(eNo);
        //garbageType = -1 表示通过卡的类型区分垃圾类型
        if (garbageENoEntity != null && garbageType == -1){
            if (garbageENoEntity.geteNoType() == Constants.eNoType.KITCHEN_CARD.getType()){
                gT = Constants.garbageType.KITCHEN_GARBAGE;
            }
            if (garbageENoEntity.geteNoType() == Constants.eNoType.OTHER_CARD.getType()){
                gT = Constants.garbageType.OTHER_GARBAGE;
            }
            if (garbageENoEntity.geteNoType() == Constants.eNoType.RECYCLEABLE_CARD.getType()){
                gT = Constants.garbageType.RECYCLEABLE;
            }
            if (garbageENoEntity.geteNoType() == Constants.eNoType.DANGER_CARD.getType()){
                gT = Constants.garbageType.DANGER_GARBAGE;
            }
        }
        collectorEntity.setGarbageQuality(garbageQuality.getType());
        collectorEntity.setGarbagePoint(pointScore);
        collectorEntity.setCheck(true);
        collectorEntity.setGarbageType(gT.getType());
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
            sumScore = userPointEntity.getPoint() + pointScore;
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
            userPointEntity.setUserName(userEntity.getName());
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
        String placeName = userEntity.getTownName();
        String date = DateFormatUtil.formatDate(new Date(), "yyyy-MM-dd");


        GarbageVillageInfoEntity villageInfoEntity = garbageVillageInfoDao.findByVillageId(villageId);

        if (villageInfoEntity.getSendMsg() == 1){
            //需要发送短信
            //发送短信通知  ${placeName} 提示您，您在${date}的易腐垃圾分类${quality} ，请继续保持！ 感谢您对生活垃圾分类工作的支持!
            //${placeName} 提示您，您在${date}的易腐垃圾分类${quality} ，请您重视垃圾分类！ 感谢您对生活垃圾分类工作的支持!
            //${placeName} 提示您，您在${date}的易腐垃圾分类为${quality} ，请您积极参与垃圾分类！ 感谢您对生活垃圾分类工作的支持!
            //${placeName} 提示您，您在${date}的易腐垃圾分类为${quality} ，今日新增积分${score}，目前总分${sumScore}，请您积极参与垃圾分类！ 感谢您对生活垃圾分类工作的支持!
            String messageContent = placeName + "提示您,您在" + date +"的易腐垃圾分类" + qualitySms  + ",今日新增积分" + pointScore +",目前总分" + sumScore + "," + sendRemark + "！感谢您对生活垃圾分类工作的支持！";

            if (userEntity.getPhone()!= null && userEntity.getPhone().length() == 11 ){
//                try {
//                    SmsUtil.getSmsUtil().sendGarbageNotice(userEntity.getPhone(), placeName, date, qualitySms, Integer.valueOf(quality));
//                } catch (ClientException e) {
//                    e.printStackTrace();
//                }
                GarbageMessageEntity messageEntity = new GarbageMessageEntity();
                messageEntity.setPhone(userEntity.getPhone());
                messageEntity.setPlaceName(placeName);
                messageEntity.setQuality(qualitySms);
                messageEntity.setType(Integer.valueOf(quality));
                messageEntity.setDate(date);
                messageEntity.setMsgContent(messageContent);
                messageEntity.setScore(pointScore);
                messageEntity.setSumScore(sumScore);
                messageEntity.setStatus(Constants.messageStatus.NOTSEND.getCode());
                messageEntity.setVillageId(villageId);
                messageEntity.setFromType(0);
                garbageMessageDao.save(messageEntity);
            }
        }
        ResponseData responseData = new ResponseData();
        responseData.setStatus(Constants.responseStatus.Success.getStatus());
        responseData.setMsg("添加成功");
        logger.info("通过人工环卫车上传垃圾信息成功");
        return responseData;
    }

    @Transactional
    public ResponseData addGarbageByAuto(String eNo, Double weight, Integer imageId, String jwt, Integer garbageType) {
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
        GarbageUserEntity u = garbageUserDao.findById(eNoEntity.getUserId()).get();
        if (u != null && !u.getCommunityId().equals(communityId)){
            throw new RuntimeException("非同一小区不能上传");
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
        Constants.garbageType  gType = null;
        if (garbageType == 1){
            gType = Constants.garbageType.KITCHEN_GARBAGE;
        } else if (garbageType == 2){
            gType = Constants.garbageType.OTHER_GARBAGE;
        } else if (garbageType == 3){
            gType = Constants.garbageType.RECYCLEABLE;
        } else {
            gType = Constants.garbageType.DANGER_GARBAGE;
        }

        //获取当前垃圾卡的类型
        GarbageENoEntity garbageENoEntity = garbageENoDao.findByENo(eNo);
        if (garbageENoEntity != null && !StringUtils.isEmpty(garbageType)){
            if (garbageENoEntity.geteNoType() == Constants.eNoType.KITCHEN_CARD.getType()){
                gType = Constants.garbageType.KITCHEN_GARBAGE;
            }
            if (garbageENoEntity.geteNoType() == Constants.eNoType.OTHER_CARD.getType()){
                gType = Constants.garbageType.OTHER_GARBAGE;
            }
            if (garbageENoEntity.geteNoType() == Constants.eNoType.RECYCLEABLE_CARD.getType()){
                gType = Constants.garbageType.RECYCLEABLE;
            }
            if (garbageENoEntity.geteNoType() == Constants.eNoType.DANGER_CARD.getType()){
                gType = Constants.garbageType.DANGER_GARBAGE;
            }
        }

        collectorEntity.setGarbageType(gType.getType());
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
        GarbageUserEntity userEntity = garbageUserDao.findById(sub).get();
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
        List<Integer> communityIds = getCommunityResource(userEntity.getRoles().stream().collect(Collectors.toList()));
        List<String> roleCodes = userEntity.getRoles().stream().map(n -> n.getRoleCode()).collect(Collectors.toList());
        Page<GarbageCollectorEntity> page = garbageCollectorDao.findAll(new Specification<GarbageCollectorEntity>() {
            @Override
            public Predicate toPredicate(Root<GarbageCollectorEntity> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
               List<Predicate> predicateList = new ArrayList<>();
               //小区
                if (roleCodes.stream().filter(n -> n.endsWith("COMMUNITY_ADMIN")).count()> 0){
                    if (communityIds.size() > 0){
                        predicateList.add(root.get("communityId").in(communityIds));
                    }
                }
                if (roleCodes.stream().filter(n->n.endsWith("COMMUNITY_REMARK")).count() > 0){
                    predicateList.add(criteriaBuilder.equal(root.get("communityId"), userEntity.getCommunityId()));
                }
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
//        List<Long> communityIds = page.getContent().stream().map(garbageCollectorEntity -> garbageCollectorEntity.getCommunityId()).distinct().collect(Collectors.toList());
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
            dto.setAddress(communityMap.get(garbageCollectorEntity.getCommunityId().intValue()));
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
        Integer sumScore = 0;
        String desc = "";
        String qualitySms = "";
        String sendRemark = "";
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
                qualitySms = Constants.sMsModelResult.QUALITY.getContent() ;
                sendRemark = Constants.sMsModelRemark.QUALITY.getContent();
                break;
            case 2:
                garbageQuality = Constants.garbageQuality.NOTQUALIFIED;
                if (qualityPointEntity == null){
                    pointScore = pointScoreForNoQualified;
                } else {
                    pointScore = qualityPointEntity.getNoQualified();
                }
                desc = "垃圾分类人工审核不合格积分";
                qualitySms = Constants.sMsModelResult.NOTQUALITY.getContent() ;
                sendRemark = Constants.sMsModelRemark.NOTQUALITY.getContent();
                break;
            default:
                garbageQuality = Constants.garbageQuality.EMPTY;
                if (qualityPointEntity == null){
                    pointScore = pointScoreForEmpty;
                } else {
                    pointScore = qualityPointEntity.getEmpty();
                }
                desc = "垃圾分类人工审核空桶积分";
                qualitySms = Constants.sMsModelResult.EMPTY.getContent();
                sendRemark = Constants.sMsModelRemark.EMPTY.getContent();
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
            sumScore = userPointEntity.getPoint() + pointScore;
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

        String placeName = userEntity.getTownName();
        String date = DateFormatUtil.formatDate(new Date(), "yyyy-MM-dd");




        if (communityEntity.getSendMsg() == 1){
            //需要发送短信
            //发送短信通知  ${placeName} 提示您，您在${date}的易腐垃圾分类${quality} ，请继续保持！ 感谢您对生活垃圾分类工作的支持!
            //${placeName} 提示您，您在${date}的易腐垃圾分类${quality} ，请您重视垃圾分类！ 感谢您对生活垃圾分类工作的支持!
            //${placeName} 提示您，您在${date}的易腐垃圾分类为${quality} ，请您积极参与垃圾分类！ 感谢您对生活垃圾分类工作的支持!
//            String messageContent = placeName + "提示您，您在" + date +"的易腐垃圾分类" + qualitySms  + "," + sendRemark + "！感谢您对生活垃圾分类工作的支持！";
            String messageContent = placeName + "提示您，您在" + date +"的易腐垃圾分类" + qualitySms  + ",今日新增积分" + pointScore +",目前总分" + sumScore + "," + sendRemark + "！感谢您对生活垃圾分类工作的支持！";

            if (userEntity.getPhone()!= null && userEntity.getPhone().length() == 11 ){
//            try {
//                SmsUtil.getSmsUtil().sendGarbageNotice(userEntity.getPhone(), placeName, date, qualitySms, Integer.valueOf(quality));
//            } catch (ClientException e) {
//                e.printStackTrace();
//            }
                GarbageMessageEntity messageEntity = new GarbageMessageEntity();
                messageEntity.setPhone(userEntity.getPhone());
                messageEntity.setPlaceName(placeName);
                messageEntity.setQuality(qualitySms);
                messageEntity.setType(Integer.valueOf(quality));
                messageEntity.setDate(date);
                messageEntity.setMsgContent(messageContent);
                messageEntity.setScore(pointScore);
                messageEntity.setSumScore(sumScore);
                messageEntity.setStatus(Constants.messageStatus.NOTSEND.getCode());
                messageEntity.setVillageId(communityId);
                messageEntity.setFromType(1);
                garbageMessageDao.save(messageEntity);
            }
        }

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
                criteriaBuilder.countDistinct(root.get("userId")), criteriaBuilder.sum(root.get("garbageWeight")), root.get("day"),
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
        createQuery.setFirstResult((pageNo-1)*pageSize);
        createQuery.setMaxResults(pageSize);
        List<CollectorDto> data = createQuery.getResultList();
        List<CollectorDto> counts = entityManager.createQuery(criteriaQuery).getResultList();
        PageImpl<CollectorDto> page = new PageImpl<>(data, pageable, Long.valueOf(counts.size()));
        List<Integer> collectorIds = page.getContent().stream().map(dto->dto.getCollectorId()).collect(Collectors.toList());
        List<UserVillageDto> villageDtos = new ArrayList<>();
        if (collectorIds.size() > 0){
            villageDtos  = garbageUserDao.getUserVillageNameByIds(collectorIds);
        }

        Map<Integer, String> map = villageDtos.stream().collect(Collectors.toMap(UserVillageDto::getId, UserVillageDto::getVillageName));
        for (CollectorDto dto:page.getContent()) {
            dto.setAddress(map.get(dto.getCollectorId()));
            if (dto.getGarbageWeight() != null ){
                BigDecimal bg = new BigDecimal(dto.getGarbageWeight());
                double f1 = bg.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                dto.setGarbageWeight(f1);
            } else {
                dto.setGarbageWeight(0D);
            }
            if ("day".equals(type)){
                dto.setCollectDate(dto.getYear() + "-" + (dto.getMonth() + 1) + "-" + dto.getDay());
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
        //用户数
        Long provinceId = userEntity.getProvinceId();
        List<Integer> communityIds = getCommunityResource(roleEntityList);
        Long userCount = garbageUserDao.count(new Specification<GarbageUserEntity>() {
            @Nullable
            @Override
            public Predicate toPredicate(Root<GarbageUserEntity> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList<>();
                Predicate predicateStatus = criteriaBuilder.equal(root.get("status"), 1);
                predicates.add(predicateStatus);
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
                Long pId = userEntity.getProvinceId();
                JPositionProvinceEntity provinceEntity = jPositionProvinceDao.findByProvinceId(pId.intValue());
                placeNameMap.put(pId.longValue(), provinceEntity.getProvinceName());
                predicates.add(cb.equal(rootPlace.get("provinceId"), pId));
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
                Long city_Id = userEntity.getCityId();
                JPositionCityEntity cityEntity = jPositionCityDao.findByCityId(city_Id);
                placeNameMap.put(city_Id, cityEntity.getCityName());
                predicates.add(cb.equal(rootPlace.get("cityId"), city_Id));
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
                Long ctId = userEntity.getCountryId();
                JPositionCountyEntity countyEntity = jPositionCountyDao.findByCountyId(ctId);
                placeNameMap.put(ctId, countyEntity.getCountyName());
                predicates.add(cb.equal(rootPlace.get("countryId"), ctId));
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
                Long tId = userEntity.getTownId();
                JPositionTownEntity jPositionTownEntity = jPositionTownDao.findByTownId(tId);
                placeNameMap.put(tId, jPositionTownEntity.getTownName());
                predicates.add(cb.equal(rootPlace.get("townId"), tId));
            }
        }
        if (roles.contains("VILLAGE_ADMIN")){
            cq.multiselect( rootPlace.get("villageId"), cb.sum(rootPlace.get("garbageWeight")), rootPlace.get("day"), rootPlace.get("month"), rootPlace.get("year"), cb.countDistinct(root.get("userId")));
            expressionList.add(rootPlace.get("villageId"));
            Long vId = userEntity.getVillageId();
            JPositionVillageEntity villageEntity = jPositionVillageDao.findByVillageId(vId);
            placeNameMap.put(vId, villageEntity.getVillageName());
            predicates.add(cb.equal(rootPlace.get("villageId"), vId));
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
                dto.setCollectDate(dto.getYear() + "-" + (dto.getMonth() + 1) + "-" + dto.getDay());
            }else if ("month".equals(type)){
                dto.setCollectDate(dto.getYear() + "-" + (dto.getMonth() +1));
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
        List<GarbageCollectorEntity> collectorEntities = garbageCollectorDao.findAll(new Specification<GarbageCollectorEntity>() {
            @Override
            public Predicate toPredicate(Root<GarbageCollectorEntity> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                List<Predicate> selectList = new ArrayList<>();
                Predicate fromTypePre = criteriaBuilder.equal(root.get("garbageFromType"), fromType );
                selectList.add(fromTypePre);
                Predicate startP = criteriaBuilder.greaterThanOrEqualTo(root.get("collectDate"), start);
                Predicate endP = criteriaBuilder.lessThanOrEqualTo(root.get("collectDate"), end);
                selectList.add(startP);
                selectList.add(endP);
                if (roles.contains("PROVINCE_ADMIN")){
                    if (cityId !=null){
                        Predicate predicate = criteriaBuilder.equal(root.get("cityId"), cityId);
                        selectList.add(predicate);
                    }else if (cityId !=null && countryId !=null){
                        Predicate predicate = criteriaBuilder.equal(root.get("countryId"), countryId);
                        selectList.add(predicate);
                    } else if (cityId !=null && countryId !=null && townId !=null){
                        Predicate predicate = criteriaBuilder.equal(root.get("townId"), townId);
                        selectList.add(predicate);
                    }else if (cityId !=null && countryId !=null && townId !=null && villageId != null){
                        Predicate predicate = criteriaBuilder.equal(root.get("villageId"), villageId);
                        selectList.add(predicate);
                    } else {
                        Predicate predicate = criteriaBuilder.equal(root.get("provinceId"), userEntity.getProvinceId());
                        selectList.add(predicate);
                    }
                }
                if (roles.contains("CITY_ADMIN")){
                    if (countryId !=null){
                        Predicate predicate = criteriaBuilder.equal(root.get("countryId"), countryId);
                        selectList.add(predicate);
                    } else if (countryId !=null && townId != null) {
                        Predicate predicate = criteriaBuilder.equal(root.get("townId"), townId);
                        selectList.add(predicate);
                    } else if (countryId != null && townId != null && villageId != null){
                        Predicate predicate = criteriaBuilder.equal(root.get("villageId"), villageId);
                        selectList.add(predicate);
                    } else {
                        Predicate predicate = criteriaBuilder.equal(root.get("cityId"), userEntity.getCityId());
                        selectList.add(predicate);
                    }
                }
                if (roles.contains("COUNTRY_ADMIN")){
                    if (townId != null){
                        Predicate predicate = criteriaBuilder.equal(root.get("townId"), townId);
                        selectList.add(predicate);
                    } else if (townId !=null && villageId !=null){
                        Predicate predicate = criteriaBuilder.equal(root.get("villageId"), villageId);
                        selectList.add(predicate);
                    } else {
                        Predicate predicate = criteriaBuilder.equal(root.get("countryId"), userEntity.getCountryId());
                        selectList.add(predicate);
                    }
                }
                if (roles.contains("TOWN_ADMIN")){
                    if (villageId !=null){
                        Predicate predicate = criteriaBuilder.equal(root.get("villageId"), villageId);
                        selectList.add(predicate);
                    } else {
                        Predicate predicate = criteriaBuilder.equal(root.get("townId"), userEntity.getTownId());
                        selectList.add(predicate);
                    }
                }
                if (roles.contains("VILLAGE_ADMIN")){
                    Predicate predicate = criteriaBuilder.equal(root.get("villageId"), userEntity.getVillageId());
                    selectList.add(predicate);
                }
                if (fromType == 1){
                    if (communityIds.size() > 0){
                        if (communityId != null ){
                            Predicate predicate = criteriaBuilder.equal(root.get("communityId"), communityId);
                            selectList.add(predicate);
                        } else {
                            Predicate predicate =  root.get("communityId").in(communityIds);
                            selectList.add(predicate);
                        }
                    }
                }
                return criteriaBuilder.and(selectList.toArray(new Predicate[selectList.size()]));
            }
        });
        Set<Integer> userIds = collectorEntities.stream().map(n-> n.getUserId()).collect(Collectors.toSet());

        Pageable pageable = PageRequest.of(pageNo-1, pageSize, getGarbageCollectorSummaryInfo(orderBys));
        Page<GarbageUserEntity> garbageUserEntityPage = garbageUserDao.findAll(new Specification<GarbageUserEntity>() {
            @Nullable
            @Override
            public Predicate toPredicate(Root<GarbageUserEntity> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicateList = new ArrayList<>();
                predicateList.add(criteriaBuilder.equal(root.get("status"), Constants.dataType.ENABLE.getType()));
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
                if (userIds.size() > 0){
                    Predicate not = criteriaBuilder.not(root.get("id").in(userIds));
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

    public ResponseData getGarbageWeightCurrentYear(String jwt) {
        Integer sub = jwtUtil.getSubject(jwt);
        GarbageUserEntity userEntity = garbageUserDao.findById(sub).get();
        Integer fromType = userEntity.getFromType();
        List<GarbageRoleEntity> roleEntityList =userEntity.getRoles().stream().collect(Collectors.toList());
        List<Integer> communityIds = getCommunityResource(roleEntityList);
        List<String> roleCodes = roleEntityList.stream().map(role -> role.getRoleCode()).collect(Collectors.toList());
        Calendar calendar = Calendar.getInstance();
        Integer year = calendar.get(Calendar.YEAR);
        Integer month = calendar.get(Calendar.MONTH);
        List<GarbageWeightInMonth> weightInMonths = new ArrayList<>();
//        if (month < 5){
//            weightInMonths  = garbageCollectorDao.getGarbageWeightInMonthBetween(year, 0, month);
//        } else {
//            weightInMonths  = garbageCollectorDao.getGarbageWeightInMonthBetween(year, month - 5, month );
//        }
        StringBuilder builder = new StringBuilder("");

        builder.append("SELECT u.`month`, ROUND(sum(u.garbage_weight),2) from garbage_collector u where  u.`year` = ?1 ");
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
        } else if (roleCodes.contains("PROVINCE_ADMIN")){
            builder.append(" and u.province_id = " +  userEntity.getProvinceId());
        } else {

        }
        if (month < 5){
            builder.append(" and u.`month` < 5");
        } else {
            builder.append(" and u.`month` BETWEEN (?2 - 5) and ?2");
        }

        builder.append(" group by u.`month` ");

        Query nativeQuery = entityManager.createNativeQuery(builder.toString());
        nativeQuery.setParameter(1, year);
        if (month > 5){
            nativeQuery.setParameter(2,month);
        }
        List<Object[]> data =nativeQuery.getResultList();
        for (int i = 0; i < data.size(); i++) {
            GarbageWeightInMonth weightInMonth = new GarbageWeightInMonth();
            Integer m = (Integer) data.get(i)[0];
            Double weight  = (Double) data.get(i)[1];
            weightInMonth.setTime(year + "-" + (m + 1) );
            weightInMonth.setWeight(weight);
            weightInMonth.setMonth(m+1);
            weightInMonths.add(weightInMonth);
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
        List<GarbageRoleCommunityEntity> roleCommunityEntityList = null;
        if (roleIds.size() > 0){
            roleCommunityEntityList = garbageRoleCommunityDao.findAll(new Specification<GarbageRoleCommunityEntity>() {
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
        }else {
            roleCommunityEntityList = new ArrayList<>();
        }

        Set<Integer> communityIds = roleCommunityEntityList.stream().map(garbageRoleCommunityEntity -> garbageRoleCommunityEntity.getCommunityId()).collect(Collectors.toSet());
        List<Integer> ids = communityIds.stream().collect(Collectors.toList());
        return ids;
    }

    public ResponsePageData villageGarbageList(Integer pageNo, Integer pageSize, Boolean isCheck, Double weight, Integer point, Integer quality, String type,
                                               String keyWord, Integer garbageType, String jwt, String[] orderBys,
                                               String startTime, String endTime, String name, Long cityId, Long countyId, Long townId, Long villageId, Integer sType) {
        Long start = System.currentTimeMillis();
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
                    //厨余垃圾--1    其他垃圾--2  3 可回收  4危险
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
                if (!StringUtils.isEmpty(name)){
                    List<Predicate> predicates = new ArrayList<>();
                    Subquery subquery = criteriaQuery.subquery(GarbageUserEntity.class);
                    Root subRoot = subquery.from(GarbageUserEntity.class);
                    subquery.select(subRoot.get("id"));
                    Predicate predicate = criteriaBuilder.equal(root.get("collectorId"), subRoot.get("id"));
                    predicates.add(predicate);
                    Predicate namePredicate  = criteriaBuilder.like(subRoot.get("name"), "%" + name + "%");
                    predicates.add(namePredicate);
                    Subquery where = subquery.where(predicates.toArray(new Predicate[predicates.size()]));
                    Predicate exists = criteriaBuilder.exists(where);
                    predicateList.add(exists);
                }
                if (!StringUtils.isEmpty(keyWord) && "name".equals(type) || "phone".equals(type) || sType != null){
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
                    if (sType != null){
                        if (sType == 1){
                            //党员
                            Predicate dyPredicate = criteriaBuilder.isTrue(subRoot.get("dangYuan"));
                            predicates.add(dyPredicate);
                        }
                        if (sType == 2){
                            Predicate cmdbPredicate = criteriaBuilder.isTrue(subRoot.get("cunMinDaiBiao"));
                            predicates.add(cmdbPredicate);
                        }
                        if (sType == 3){
                            Predicate jdysdbbPredicate = criteriaBuilder.isTrue(subRoot.get("streetCommentDaiBiao"));
                            predicates.add(jdysdbbPredicate);
                        }
                        if (sType == 4){
                            Predicate ldbywyPredicate = criteriaBuilder.isTrue(subRoot.get("liangDaiBiaoYiWeiYuan"));
                            predicates.add(ldbywyPredicate);
                        }
                        if (sType == 5){
                            Predicate cldPredicate = criteriaBuilder.isTrue(subRoot.get("cunLeader"));
                            predicates.add(cldPredicate);
                        }
                        if (sType == 6){
                            Predicate cunZuLeaderPredicate = criteriaBuilder.isTrue(subRoot.get("cunZuLeader"));
                            predicates.add(cunZuLeaderPredicate);
                        }
                        if (sType == 7){
                            Predicate womenExeLeaderPredicate = criteriaBuilder.isTrue(subRoot.get("womenExeLeader"));
                            predicates.add(womenExeLeaderPredicate);
                        }
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
        System.out.println("--1" + (System.currentTimeMillis() - start));
        List<Integer> userIds = page.getContent().stream().map(garbageCollectorEntity -> garbageCollectorEntity.getUserId()).collect(Collectors.toList());
//        List<UserDto> userDtos = new ArrayList<>();
        List<UserDtoForLeader> userDtos = new ArrayList<>();
        if (userIds.size() > 0){
//            userDtos = garbageUserDao.selectUserInfoByIdIn(userIds);
            userDtos = garbageUserDao.selectUserLeaderInfoByIdIn(userIds);
        }
        List<Long> communityIds = page.getContent().stream().filter(n-> n.getCommunityId() != null).map(garbageCollectorEntity -> garbageCollectorEntity.getCommunityId()).collect(Collectors.toList());
//        Map<Integer, String> userMap = userDtos.stream().collect(Collectors.toMap(UserDto::getUserId, UserDto::getUsername));
//        Map<Integer, String> userAddressMap = userDtos.stream().collect(Collectors.toMap(UserDto::getUserId, UserDto::getAddress));
        Map<Integer, UserDtoForLeader> userDtoForLeaderMap = userDtos.stream().collect(Collectors.toMap(UserDtoForLeader::getUserId, Function.identity()));
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
        System.out.println("--2" + (System.currentTimeMillis() - start));
        List<Integer> busIds = page.getContent().stream().map(n-> n.getId()).collect(Collectors.toList());
        Map<Integer, String> communityMap = communityEntities.stream().collect(Collectors.toMap(GarbageCommunityEntity::getId, GarbageCommunityEntity::getCommunityName));
        List<GarbageImageEntity> imageEntityList = garbageImageDao.findBySourceNameAndTypeAndBusIdIn(GarbageCollectorEntity.class.getName(), Constants.image.GARBAGE_IMAGE.name(), busIds);
        Map<Integer, String> imagePathMap = imageEntityList.stream().collect(Collectors.toMap(GarbageImageEntity::getBusId, GarbageImageEntity::getImagePath));
        page.getContent().stream().forEach(garbageCollectorEntity -> {
            CommunityGarbageCollectDto dto = new CommunityGarbageCollectDto();
            UserDtoForLeader userDtoForLeader = userDtoForLeaderMap.get(garbageCollectorEntity.getUserId());
            dto.setId(garbageCollectorEntity.getId());
            dto.setUserId(garbageCollectorEntity.getUserId());
            dto.setUsername(userDtoForLeader.getUsername());
            dto.setUserPhone(userDtoForLeader.getPhone());
            dto.setCountyName(userDtoForLeader.getCountyName());
            dto.setVillageName(userDtoForLeader.getVillageName());
            dto.setTownName(userDtoForLeader.getTownName());
//            dto.setType(garbageCollectorEntity.getGarbageFromType() == 0?"农村":"小区");
            String s_type = "";
            if (userDtoForLeader.getDangYuan()){
                s_type += "党员";
            }
            if (userDtoForLeader.getCunMinDaiBiao()){
                s_type += " 村民代表";
            }
            if (userDtoForLeader.getStreetCommentDaiBiao()){
                s_type += " 街道议事代表";
            }
            if (userDtoForLeader.getLiangDaiBiaoYiWeiYuan()){
                s_type += " 两代表一委员";
            }
            if (userDtoForLeader.getCunLeader()){
                s_type += " 村干部";
            }
            if (userDtoForLeader.getCunZuLeader()){
                s_type += " 村组长";
            }
            if (userDtoForLeader.getWomenExeLeader()){
                s_type += " 妇女执委";
            }
            if ("".equals(s_type)){
                s_type = "群众";
            }
            dto.setType(s_type);
            dto.setAddress(userDtoForLeader.getAddress());
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
            dto.setCollectPhone(garbageCollectorEntity.getCollectorPhone());
            dtos.add(dto);
        });
        System.out.println("--3" + (System.currentTimeMillis() - start));
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
        List<GarbageCommunityEntity> communityEntities = garbageCommunityDao.findByCountyId(countyId);
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
            expressionList.add(root.get("villageId"));
            expressionListQuality.add(root.get("villageId"));
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
        int size = query.getResultList().size();
        query.setFirstResult((pageNo-1)*pageSize);
        query.setMaxResults(pageSize);
        List<UserCollectDataAnalysisDto> dtos = query.getResultList();
        PageImpl<UserCollectDataAnalysisDto> page = new PageImpl<>(dtos, pageable, Long.valueOf(dtos.size()));
        List<GarbageUserEntity> userEntities = garbageUserDao.findAll(new Specification<GarbageUserEntity>() {
            @Override
            public Predicate toPredicate(Root<GarbageUserEntity> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                List<Predicate> pList = new ArrayList<>();
                if (roles.contains("VILLAGE_ADMIN")){
                    Predicate predicate = criteriaBuilder.equal(root.get("villageId"), userEntity.getVillageId());
                    pList.add(predicate);
                }
                if (roles.contains("TOWN_ADMIN")){
                    Predicate predicate = criteriaBuilder.equal(root.get("townId"), userEntity.getTownId());
                    pList.add(predicate);
                }
                if (roles.contains("COUNTRY_ADMIN")){
                    Predicate predicate = criteriaBuilder.equal(root.get("countryId"), userEntity.getCountryId());
                    pList.add(predicate);
                }
                if (roles.contains("CITY_ADMIN")){
                    Predicate predicate = criteriaBuilder.equal(root.get("cityId"), userEntity.getCityId());
                    pList.add(predicate);
                }
                if (roles.contains("PROVINCE_ADMIN")){
                    Predicate predicate = criteriaBuilder.equal(root.get("provinceId"), userEntity.getCityId());
                    pList.add(predicate);
                }
                return criteriaBuilder.and(pList.toArray(new Predicate[pList.size()]));
            }
        });
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
        responsePageData.setTotalElement(Long.valueOf(size));
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

    public ResponseData userCollectDataAnalysisNew(Integer pageNo, Integer pageSize, String startTime, String endTime, String type, String keyWord, String jwt, String[] orderBys,
                                                   Long cityId, Long countryId, Long townId, Long villageId, Long communityId){
        Integer sub  = jwtUtil.getSubject(jwt);
        GarbageUserEntity userEntity = garbageUserDao.findById(sub).get();
        Integer fromType = userEntity.getFromType(); // 0农村  1小区
        List<GarbageRoleEntity> roleEntityList = userEntity.getRoles().stream().collect(Collectors.toList());
        List<String> roleCodes = roleEntityList.stream().filter(garbageRoleEntity-> !garbageRoleEntity.getRoleCode().contains("COMMUNITY")).map(garbageRoleEntity -> garbageRoleEntity.getRoleCode()).collect(Collectors.toList());
        List<Integer> communityIds = getCommunityResource(roleEntityList);
        Long  start = DateFormatUtil.getFirstTimeOfDay(startTime).getTime();
        Long  end = DateFormatUtil.getLastTimeOfDay(endTime).getTime();
        StringBuilder builder = new StringBuilder("");
        builder.append(" SELECT t.`name`,t.phone,t.province_name,t.city_name,t.country_name,t.town_name,t.village_name,t.address,COUNT(1),SUM(t.hg),SUM(t.nhg),SUM(t.kt)," +
                " concat(ROUND(sum(hg)*100/count(1),2), '%')," +
                " ROUND(SUM(t.garbage_weight),2) from ( " +
                " SELECT u.`name`,u.phone,u.province_name,u.city_name,u.country_name,u.town_name,u.village_name,u.address, " +
                " CASE c.garbage_quality" +
                "   WHEN 1 then 1" +
                "   ELSE 0" +
                " END as hg," +
                " CASE c.garbage_quality" +
                "   WHEN 2 then 1" +
                "   ELSE 0" +
                " END as nhg," +
                " CASE c.garbage_quality" +
                "   WHEN 3 then 1" +
                "   ELSE 0" +
                " END as kt, c.garbage_weight, c.user_id FROM garbage_collector c " +
                " INNER JOIN garbage_user u on c.user_id = u.id WHERE 1=1 ");

        if (start!=null){
            builder.append(" and  c.collect_date >= " + start  +" and c.collect_date <= " + end);
        }
        if ("name".equals(type)){
            builder.append(" and  u.name like '%"+keyWord + "%'");
        }
        if ("phone".equals(type)){
            builder.append(" and  u.phone like '%"+keyWord + "%'");
        }
        if (roleCodes.contains("VILLAGE_ADMIN")){
            builder.append(" and u.village_id = " + userEntity.getVillageId());
        }
        if (roleCodes.contains("TOWN_ADMIN")){
            if (villageId != null){
                builder.append(" and u.village_id = " + villageId);
            }
            builder.append(" and  u.town_id = " + userEntity.getTownId());
        }
        if (roleCodes.contains("COUNTRY_ADMIN")){
            if (villageId != null){
                builder.append(" and  u.village_id = " + villageId);
            }
            if (townId != null){
                builder.append(" and  u.town_id = " + townId);
            }
            builder.append(" and  u.country_id = " + userEntity.getCountryId());
        }
        if (roleCodes.contains("CITY_ADMIN")){
            if (villageId != null){
                builder.append(" and  u.village_id = " + villageId);
            }
            if (townId != null){
                builder.append(" and  u.town_id = " + townId);
            }
            if (countryId != null){
                builder.append(" and  u.country_id = " + countryId);
            }
            builder.append(" u.city_id = " + userEntity.getCityId());
        }
        if (roleCodes.contains("PROVINCE_ADMIN")){
            if (villageId != null){
                builder.append(" u.village_id = " + villageId);
            }
            if (townId != null){
                builder.append(" and  u.town_id = " + townId);
            }
            if (countryId != null){
                builder.append(" and  u.country_id = " + countryId);
            }
            if (cityId != null){
                builder.append(" and  u.city_id = " + cityId);
            }
            builder.append(" and  u.province_id = " + userEntity.getProvinceId());
        }
        if (fromType == 1){
            if (communityIds.size()> 0){
                builder.append( " and u.community_id in ( ");
                for (int i = 0; i <communityIds.size() ; i++) {
                    Integer dd = communityIds.get(i);
                    if (i == communityIds.size() -1){
                        builder.append(  dd + " )");
                    } else {
                        builder.append(  dd + ", ");
                    }
                }
            }
        }
        builder.append(" ) t  GROUP BY t.user_id ");
        Long count = (Long) entityManager.createNativeQuery("select count(1) from ( " + builder.toString()).getSingleResult();
        Query nativeQuery = entityManager.createNativeQuery(builder.toString());
        int startSize = (pageNo -1)* pageSize;
        nativeQuery.setFirstResult(startSize);
        nativeQuery.setMaxResults(pageSize);
        List<Object[]> data = nativeQuery.getResultList();
        List<UserCollectDataAnalysisDto> dtoList = new ArrayList<>();
        data.forEach( n->{
            UserCollectDataAnalysisDto dto = new UserCollectDataAnalysisDto();
        });
        ResponsePageData responseData = new ResponsePageData();
        responseData.setStatus(Constants.responseStatus.Success.getStatus());
        responseData.setMsg("列表查询成功");
        responseData.setTotalElement(count);
        responseData.setPageNo(pageNo);
        responseData.setPageSize(pageSize);
        return responseData;
    }

    public ResponseData garbageCollectInWeek(String jwt) {
        Integer sub = jwtUtil.getSubject(jwt);
        GarbageUserEntity userEntity = garbageUserDao.findById(sub).get();
        Integer fromType = userEntity.getFromType();
        List<String> roleCodes = userEntity.getRoles().stream().map(r->r.getRoleCode()).collect(Collectors.toList());
        List<WeekCollectDto> dtoList = new ArrayList<>();
        Long endTime = DateFormatUtil.getLastTimeOfDay(DateFormatUtil.formatDate(new Date(), "yyyy-MM-dd")).getTime();
        Long startTime = endTime - 7*24*3600*1000;
        if (roleCodes.contains("VILLAGE_ADMIN")  || roleCodes.contains("COLLECTOR")){
            Long villageId = userEntity.getVillageId();
            dtoList = garbageCollectorDao.sumGarbageWeightByVillage(villageId, fromType, startTime, endTime);
        } else if (roleCodes.contains("TOWN_ADMIN")){
            Long townId = userEntity.getTownId();
            dtoList = garbageCollectorDao.sumGarbageWeightByTown(townId, fromType, startTime, endTime);
        } else if (roleCodes.contains("COUNTRY_ADMIN")){
            Long countyId = userEntity.getCountryId();
            dtoList = garbageCollectorDao.sumGarbageWeightByCounty(countyId, fromType, startTime, endTime);
        } else if (roleCodes.contains("CITY_ADMIN")){
            Long cityId = userEntity.getCityId();
            dtoList = garbageCollectorDao.sumGarbageWeightByCity(cityId, fromType, startTime, endTime);
        }else if (roleCodes.contains("PROVINCE_ADMIN")){
            Long provinceId = userEntity.getProvinceId();
            dtoList = garbageCollectorDao.sumGarbageWeightByProvince(provinceId, fromType, startTime, endTime);
        } else if (roleCodes.stream().filter(r -> r.endsWith("COMMUNITY_ADMIN")).count() > 0){
            Long communityId = userEntity.getCommunityId();
            dtoList = garbageCollectorDao.sumGarbageWeightByCommunity(communityId, fromType, startTime, endTime);
        } else if (roleCodes.stream().filter(r -> r.endsWith("COMMUNITY_REMARK")).count() > 0){
            Long communityId = userEntity.getCommunityId();
            dtoList = garbageCollectorDao.sumGarbageWeightByCommunity(communityId, fromType, startTime, endTime);
        }
        ResponseData responseData = new ResponseData();
        responseData.setMsg("近七天内的垃圾统计分析");
        responseData.setData(dtoList);
        responseData.setStatus(Constants.responseStatus.Success.getStatus());
        return responseData;
    }

    public ResponseData getRollingGarbageInfo(String jwt, Integer garbageType, Long villageId) {
        Integer sub = jwtUtil.getSubject(jwt);
        GarbageUserEntity userEntity = garbageUserDao.findById(sub).get();
        List<String> roleCodes = userEntity.getRoles().stream().map(n->n.getRoleCode()).collect(Collectors.toList());
        Integer fromType = userEntity.getFromType();
        List<GarbageRollingDto> rollingDtos = new ArrayList<>();
        List<Integer> garbageTypes = new ArrayList<>();
        if (garbageType == 3){
            garbageTypes.add(5);
            garbageTypes.add(6);
            garbageTypes.add(7);
            garbageTypes.add(8);
            garbageTypes.add(9);
        } else {
            garbageTypes.add(garbageType);
        }
        if (villageId != null){
            if (garbageType == 3){
                rollingDtos = garbageCollectorDao.getTopRollingVillageRecycleGarbageInfoByVillageId(villageId, garbageTypes);
            } else {
                rollingDtos = garbageCollectorDao.getTopRollingVillageGarbageInfoByVillageId(GarbageCollectorEntity.class.getName(),villageId, garbageTypes);
            }
        } else {
            if (fromType == 1){
                List<Integer> communityIds = getCommunityResource(userEntity.getRoles().stream().collect(Collectors.toList()));
                if (communityIds.size() > 0){
                    rollingDtos = garbageCollectorDao.getTopRollingCommunityGarbageInfo(GarbageCollectorEntity.class.getName(), communityIds, garbageTypes);
                }
            } else {
                if (roleCodes.size() ==1 && "RESIDENT".equals(roleCodes.get(0))){
                    if (garbageType == 3){
                        rollingDtos = garbageCollectorDao.getTopRollingVillageRecycleGarbageInfoByVillageId(userEntity.getVillageId(), garbageTypes);
                    } else {
                        rollingDtos = garbageCollectorDao.getTopRollingVillageGarbageInfoByVillageId(GarbageCollectorEntity.class.getName(),userEntity.getVillageId(), garbageTypes);
                    }
                } else {
                    if (roleCodes.contains("COLLECTOR") || roleCodes.contains("VILLAGE_ADMIN")  ) {
                        if (garbageType == 3){
                            rollingDtos = garbageCollectorDao.getTopRollingVillageRecycleGarbageInfoByVillageId(userEntity.getVillageId(), garbageTypes);
                        } else {
                            rollingDtos = garbageCollectorDao.getTopRollingVillageGarbageInfoByVillageId(GarbageCollectorEntity.class.getName(),userEntity.getVillageId(), garbageTypes);
                        }
                    } else if (roleCodes.contains("TOWN_ADMIN")) {
                        if (garbageType == 3){
                            rollingDtos = garbageCollectorDao.getTopRollingVillageRecycleGarbageInfoByTownId(userEntity.getVillageId(), garbageTypes);
                        } else {
                            rollingDtos = garbageCollectorDao.getTopRollingVillageGarbageInfoByTownId(GarbageCollectorEntity.class.getName(),userEntity.getTownId(), garbageTypes);
                        }
                    } else if (roleCodes.contains("COUNTRY_ADMIN")){
                        if (garbageType == 3){
                            rollingDtos = garbageCollectorDao.getTopRollingVillageRecycleGarbageInfoByCountyId(userEntity.getVillageId(), garbageTypes);
                        } else {
                            rollingDtos = garbageCollectorDao.getTopRollingVillageGarbageInfoByCountyId(GarbageCollectorEntity.class.getName(),userEntity.getCountryId(), garbageTypes);
                        }
                    } else if (roleCodes.contains("CITY_ADMIN")){
                        if (garbageType == 3){
                            rollingDtos = garbageCollectorDao.getTopRollingVillageRecycleGarbageInfoByCityId(userEntity.getVillageId(), garbageTypes);
                        } else {
                            rollingDtos = garbageCollectorDao.getTopRollingVillageGarbageInfoByCityId(GarbageCollectorEntity.class.getName(),userEntity.getCityId(), garbageTypes);
                        }
                    } else {
                        if (garbageType == 3){
                            rollingDtos = garbageCollectorDao.getTopRollingVillageRecycleGarbageInfoByProvinceId(userEntity.getVillageId(), garbageTypes);
                        }else {
                            rollingDtos = garbageCollectorDao.getTopRollingVillageGarbageInfoByProvinceId(GarbageCollectorEntity.class.getName(),userEntity.getProvinceId(), garbageTypes);
                        }
                    }
                }
            }
        }


        ResponseData responseData = new ResponseData();
        responseData.setMsg("当前易腐垃圾滚动信息");
        responseData.setData(rollingDtos);
        responseData.setStatus(Constants.responseStatus.Success.getStatus());
        return responseData;
    }

    public ResponseData getRollingTotalPersonPartIn(String jwt, Integer garbageType, Long villageId) {
        Integer sub = jwtUtil.getSubject(jwt);
        GarbageUserEntity userEntity = garbageUserDao.findById(sub).get();
        Integer fromType = userEntity.getFromType();
        List<String> roleCodes = userEntity.getRoles().stream().map(n->n.getRoleCode()).collect(Collectors.toList());
        Long count = garbageCollectorDao.count(new Specification<GarbageCollectorEntity>() {
            @Override
            public Predicate toPredicate(Root<GarbageCollectorEntity> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList<>();
                if (garbageType == null){
                    predicates.add(criteriaBuilder.equal(root.get("garbageType"), Constants.garbageType.KITCHEN_GARBAGE.getType()));
                } else {
                    Constants.garbageType gEum = null;
                    switch (garbageType){
                        case 1:
                            gEum = Constants.garbageType.KITCHEN_GARBAGE;
                            break;
                        case 2:
                            gEum = Constants.garbageType.OTHER_GARBAGE;
                            break;
                        case 3:
                            gEum = Constants.garbageType.RECYCLEABLE;
                            break;
                        case 4:
                            gEum = Constants.garbageType.DANGER_GARBAGE;
                            break;
                    }
                    predicates.add(criteriaBuilder.equal(root.get("garbageType"), gEum.getType()));
                }
                if (roleCodes.size() ==1 && "RESIDENT".equals(roleCodes.get(0)) || roleCodes.contains("COLLECTOR")){
                    if (fromType == 1){
                        predicates.add(criteriaBuilder.equal(root.get("communityId"), userEntity.getCommunityId()));
                    }else {
                        predicates.add(criteriaBuilder.equal(root.get("villageId"), userEntity.getVillageId()));
                    }
                }
                if (roleCodes.contains("VILLAGE_ADMIN")  ) {
                    predicates.add(criteriaBuilder.equal(root.get("villageId"), userEntity.getVillageId()));
                } else if (roleCodes.contains("TOWN_ADMIN")){
                    if (villageId != null){
                        predicates.add(criteriaBuilder.equal(root.get("villageId"), villageId));
                    }
                    predicates.add(criteriaBuilder.equal(root.get("townId"), userEntity.getTownId()));
                }else if (roleCodes.contains("COUNTRY_ADMIN")){
                    if (villageId != null){
                        predicates.add(criteriaBuilder.equal(root.get("villageId"), villageId));
                    }
                    predicates.add(criteriaBuilder.equal(root.get("countryId"), userEntity.getCountryId()));
                }else if (roleCodes.contains("CITY_ADMIN")){
                    if (villageId != null){
                        predicates.add(criteriaBuilder.equal(root.get("villageId"), villageId));
                    }
                    predicates.add(criteriaBuilder.equal(root.get("cityId"), userEntity.getCityId()));
                } else {
                    if (villageId != null){
                        predicates.add(criteriaBuilder.equal(root.get("villageId"), villageId));
                    }
                    predicates.add(criteriaBuilder.equal(root.get("provinceId"), userEntity.getProvinceId()));
                }
                criteriaQuery.groupBy(root.get("userId"));
                return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        });
        ResponseData responseData = new ResponseData();
        responseData.setMsg("当前易腐垃圾滚动信息 累计参与人数");
        responseData.setData(count);
        responseData.setStatus(Constants.responseStatus.Success.getStatus());
        return responseData;
    }

    public ResponseData bigDataMapPointInfo(String jwt) {
        Integer sub = jwtUtil.getSubject(jwt);
        GarbageUserEntity userEntity = garbageUserDao.findById(sub).get();
        Integer fromType = userEntity.getFromType();
        List<CollectMapDataDto> dtos = new ArrayList<>();
        List<String> roleCodes = userEntity.getRoles().stream().map(n->n.getRoleCode()).collect(Collectors.toList());
        if (fromType == 0){
            List<GarbageVillageInfoEntity> villageInfoEntityList = garbageVillageInfoDao.findAll(new Specification<GarbageVillageInfoEntity>() {
                @Override
                public Predicate toPredicate(Root<GarbageVillageInfoEntity> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                    List<Predicate> predicates = new ArrayList<>();
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
                    return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
                }
            });
            villageInfoEntityList.forEach(n->{
                CollectMapDataDto dto = new CollectMapDataDto();
                dto.setFromType(fromType);
                dto.setId(n.getVillageId());
                dto.setLat(n.getLatitude());
                dto.setLon(n.getLongitude());
                dto.setName(n.getVillageName());
                dtos.add(dto);
            });

        } else {
            List<Integer> communityIds = getCommunityResource(userEntity.getRoles().stream().collect(Collectors.toList()));
            List<GarbageCommunityEntity> communityEntityList = garbageCommunityDao.findAll(new Specification<GarbageCommunityEntity>() {
                @Override
                public Predicate toPredicate(Root<GarbageCommunityEntity> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                    List<Predicate> predicates = new ArrayList<>();
                    if (fromType == 1){
                        if (roleCodes.stream().filter(n-> n.endsWith("COMMUNITY_ADMIN")).count() > 0){
                            if (communityIds.size()> 0){
                                Predicate predicate = root.get("id").in(communityIds);
                                predicates.add(predicate);
                            }
                        }
                        if ( roleCodes.stream().filter(n-> n.endsWith("COMMUNITY_REMARK")).count() > 0){
                            predicates.add(criteriaBuilder.equal(root.get("id"), userEntity.getCommunityId()));
                        }
                    } else {
                        if (roleCodes.contains("COUNTRY_ADMIN")){
                            predicates.add(criteriaBuilder.equal(root.get("countryId"), userEntity.getCountryId()));
                        }else if (roleCodes.contains("CITY_ADMIN")){
                            predicates.add(criteriaBuilder.equal(root.get("cityId"), userEntity.getCityId()));
                        } else {
                            predicates.add(criteriaBuilder.equal(root.get("provinceId"), userEntity.getProvinceId()));
                        }
                    }
                    return criteriaBuilder.and(new Predicate[predicates.size()]);
                }
            });
            communityEntityList.forEach(n ->{
                CollectMapDataDto dto = new CollectMapDataDto();
                dto.setFromType(fromType);
                dto.setId(n.getId().longValue());
                dto.setLat(n.getLat());
                dto.setLon(n.getLon());
                dto.setName(n.getCommunityName());
                dtos.add(dto);
            });
        }
        ResponseData responseData = new ResponseData();
        responseData.setMsg("地图中的坐标点数");
        responseData.setData(dtos);
        responseData.setStatus(Constants.responseStatus.Success.getStatus());
        return responseData;
    }

    public ResponseData getRollingTotalWeight(String jwt, Integer garbageType) {
        Integer sub = jwtUtil.getSubject(jwt);
        GarbageUserEntity userEntity = garbageUserDao.findById(sub).get();
        Integer fromType = userEntity.getFromType();
        List<String> roleCodes = userEntity.getRoles().stream().map(n->n.getRoleCode()).collect(Collectors.toList());
        Constants.garbageType gEum = null;
        if (garbageType == null){
            gEum = Constants.garbageType.KITCHEN_GARBAGE;
        } else {
            switch (garbageType){
                case 1:
                    gEum = Constants.garbageType.KITCHEN_GARBAGE;
                    break;
                case 2:
                    gEum = Constants.garbageType.OTHER_GARBAGE;
                    break;
                case 3:
                    gEum = Constants.garbageType.RECYCLEABLE;
                    break;
                case 4:
                    gEum = Constants.garbageType.DANGER_GARBAGE;
                    break;
            }
        }
        StringBuilder builder = new StringBuilder(" SELECT  ROUND(SUM(garbage_weight),2) from garbage_collector WHERE garbage_type = " + gEum.getType());

        if (roleCodes.size() ==1 && "RESIDENT".equals(roleCodes.get(0)) || roleCodes.contains("COLLECTOR")){
            if (fromType == 1){
                builder.append(" and community_id = " +  userEntity.getCommunityId());
            }else {
                builder.append(" and village_id = " +  userEntity.getVillageId());
            }
        }

        if (roleCodes.contains("VILLAGE_ADMIN")  ) {
            builder.append(" and village_id = " +  userEntity.getVillageId());
        } else if (roleCodes.contains("TOWN_ADMIN")){
            builder.append(" and town_id = " +  userEntity.getTownId());
        }else if (roleCodes.contains("COUNTRY_ADMIN")){
            builder.append(" and country_d = " +  userEntity.getCountryId());
        }else if (roleCodes.contains("CITY_ADMIN")){
            builder.append(" and city_id = " +  userEntity.getCityId());
        } else {
            builder.append(" and province_id = " +  userEntity.getProvinceId());
        }
        Double singleResult =(Double) entityManager.createNativeQuery(builder.toString()).getSingleResult();
        ResponseData responseData = new ResponseData();
        responseData.setMsg("当前易腐垃圾滚动信息 累计参与人数");
        responseData.setData(singleResult);
        responseData.setStatus(Constants.responseStatus.Success.getStatus());
        return responseData;
    }

    public ResponseData getBigDataShowTitleInfo(String jwt) {
        Integer sub = jwtUtil.getSubject(jwt);
        GarbageUserEntity userEntity = garbageUserDao.findById(sub).get();
        Integer fromType = userEntity.getFromType();
        List<String> roleCodes = userEntity.getRoles().stream().map(n -> n.getRoleCode()).collect(Collectors.toList());
        String showTitle = "垃圾分类大数据平台";
        if (fromType == 1) {
            GarbageCommunityEntity communityEntity = garbageCommunityDao.findById(userEntity.getCommunityId().intValue()).get();
            showTitle = communityEntity.getCommunityName() + "垃圾分类大数据平台";
        } else {

            if (roleCodes.size() == 1 && "RESIDENT".equals(roleCodes.get(0)) || roleCodes.contains("COLLECTOR")) {
                GarbageVillageInfoEntity entity = garbageVillageInfoDao.findByVillageId(userEntity.getVillageId());
                showTitle = entity.getShowTitle();
            }
            if (roleCodes.contains("VILLAGE_ADMIN")) {
                GarbageVillageInfoEntity entity = garbageVillageInfoDao.findByVillageId(userEntity.getVillageId());
                showTitle = entity.getShowTitle();
            } else if (roleCodes.contains("TOWN_ADMIN")) {
                JPositionTownEntity townEntity = jPositionTownDao.findByTownId(userEntity.getTownId());
                showTitle = townEntity.getTownName() + "垃圾分类大数据平台";
            } else if (roleCodes.contains("COUNTRY_ADMIN")) {
                JPositionCountyEntity countyEntity = jPositionCountyDao.findByCountyId(userEntity.getCountryId());
                showTitle = countyEntity.getCountyName() + "垃圾分类大数据平台";
            } else if (roleCodes.contains("CITY_ADMIN")) {
                JPositionCityEntity cityEntity = jPositionCityDao.findByCityId(userEntity.getCityId());
                showTitle = cityEntity.getCityName() + "垃圾分类大数据平台";
            } else {
                JPositionProvinceEntity provinceEntity = jPositionProvinceDao.findByProvinceId(userEntity.getProvinceId().intValue());
                showTitle = provinceEntity.getProvinceName() + "垃圾分类大数据平台";
            }
        }
        ResponseData responseData = new ResponseData();
        responseData.setMsg("大数据展示的标题");
        responseData.setData(showTitle);
        responseData.setStatus(Constants.responseStatus.Success.getStatus());
        return responseData;
    }

    public ResponseData checkUserCollectToday(String eno) {
        Long startTime = DateFormatUtil.getFirstTimeOfDay(DateFormatUtil.formatDate(new Date(), "yyyy-MM-dd")).getTime();
        Long endTime = DateFormatUtil.getLastTimeOfDay(DateFormatUtil.formatDate(new Date(), "yyyy-MM-dd")).getTime();
        List<GarbageCollectorEntity> list = garbageCollectorDao.findAll(new Specification<GarbageCollectorEntity>() {
            @Override
            public Predicate toPredicate(Root<GarbageCollectorEntity> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList<>();
                if (!StringUtils.isEmpty(eno)){
                    Predicate predicate =criteriaBuilder.equal(root.get("eNo"), eno);
                    predicates.add(predicate);
                }
                Predicate startPredicate = criteriaBuilder.greaterThanOrEqualTo(root.get("collectDate"), startTime);
                Predicate predicateEnd = criteriaBuilder.lessThanOrEqualTo(root.get("collectDate"), startTime);
                predicates.add(startPredicate);
                predicates.add(predicateEnd);
                return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        });
        ResponseData responseData = new ResponseData();
        if (list.size() > 0){
            throw new RuntimeException("今天已经上传过了，不能再上传了");
        }
        responseData.setData(true);
        responseData.setStatus(Constants.responseStatus.Success.getStatus());
        responseData.setMsg("今天还没有上传过，可以上传");
        return responseData;
    }

    public List<Object[]> exportNotSendGarbageToSystem(String type, String keyWord, String startTime, String endTime, String jwt) {
        Integer sub  = jwtUtil.getSubject(jwt);
        GarbageUserEntity userEntity = garbageUserDao.findById(sub).get();
        Integer fromType = userEntity.getFromType(); // 0农村  1小区
        List<GarbageRoleEntity> roleEntityList = userEntity.getRoles().stream().collect(Collectors.toList());
        List<String> roleCodes = roleEntityList.stream().filter(garbageRoleEntity-> !garbageRoleEntity.getRoleCode().contains("COMMUNITY")).map(garbageRoleEntity -> garbageRoleEntity.getRoleCode()).collect(Collectors.toList());
        List<Integer> communityIds = getCommunityResource(roleEntityList);
        StringBuilder builder = new StringBuilder("");
        builder.append(" SELECT u.`name`, u.phone, u.province_name, u.city_name, u.country_name, u.town_name,");
        if (fromType == 1){
            builder.append(" u.community_name ,");
        } else {
            builder.append(" u.village_name, ");
        }
        builder.append(" u.address from garbage_user u WHERE 1 = 1  and u.status = 1 ");
        if ( "name".equals(type)){
            builder.append(" and u.name like '%" + keyWord + "%'");
        }
        if ("phone".equals(type)){
            builder.append(" and u.phone like '%" + keyWord +"%'");
        }
        if (roleCodes.contains("VILLAGE_ADMIN")){
            builder.append(" and u.village_id = " + userEntity.getVillageId());
        }
        if (roleCodes.contains("TOWN_ADMIN")){
            builder.append(" and  u.town_id = " + userEntity.getTownId());
        }
        if (roleCodes.contains("COUNTRY_ADMIN")){
            builder.append(" and  u.country_id = " + userEntity.getCountryId());
        }
        if (roleCodes.contains("CITY_ADMIN")){
            builder.append(" u.city_id = " + userEntity.getCityId());
        }
        if (roleCodes.contains("PROVINCE_ADMIN")){
            builder.append(" and  u.province_id = " + userEntity.getProvinceId());
        }
        if (fromType == 1){
            if (communityIds.size()> 0){
                builder.append( " and u.community_id in ( ");
                for (int i = 0; i <communityIds.size() ; i++) {
                    Integer dd = communityIds.get(i);
                    if (i == communityIds.size() -1){
                        builder.append(  dd + " )");
                    } else {
                        builder.append(  dd + ", ");
                    }
                }
            }
        }
        builder.append(" and u.id not in ( SELECT DISTINCT c.user_id from garbage_collector c where 1 = 1");
        if (startTime != null && endTime !=null){
            Long  start = DateFormatUtil.getFirstTimeOfDay(startTime).getTime();
            Long  end = DateFormatUtil.getLastTimeOfDay(endTime).getTime();
            builder.append(" and c.collect_date >=" + start + " and c.collect_date <= " + end);
        }
        builder.append(" and c.garbage_from_type = 0");
        if (roleCodes.contains("VILLAGE_ADMIN")){
            builder.append(" and c.village_id = " + userEntity.getVillageId());
        }
        if (roleCodes.contains("TOWN_ADMIN")){
            builder.append(" and  c.town_id = " + userEntity.getTownId());
        }
        if (roleCodes.contains("COUNTRY_ADMIN")){
            builder.append(" and  c.country_id = " + userEntity.getCountryId());
        }
        if (roleCodes.contains("CITY_ADMIN")){
            builder.append(" c.city_id = " + userEntity.getCityId());
        }
        if (roleCodes.contains("PROVINCE_ADMIN")){
            builder.append(" and  c.province_id = " + userEntity.getProvinceId());
        }
        if (fromType == 1){
            if (communityIds.size()> 0){
                builder.append( " and c.community_id in ( ");
                for (int i = 0; i <communityIds.size() ; i++) {
                    Integer dd = communityIds.get(i);
                    if (i == communityIds.size() -1){
                        builder.append(  dd + " )");
                    } else {
                        builder.append(  dd + ", ");
                    }
                }
            }
        }
        builder.append(")");
        List<Object[]> data = entityManager.createNativeQuery(builder.toString()).getResultList();
        return data;
    }

    public List<Object[]> exportVillageGarbageList(Boolean isCheck, Double weight, Integer point, Integer quality, String type, String keyWord, Integer garbageType, String startTime, String endTime, String name,  Long cityId, Long countryId, Long townId, Long villageId, String jwt,  Integer sType) {
        Integer sub  = jwtUtil.getSubject(jwt);
        GarbageUserEntity userEntity = garbageUserDao.findById(sub).get();
        Integer fromType = userEntity.getFromType(); // 0农村  1小区
        List<GarbageRoleEntity> roleEntityList = userEntity.getRoles().stream().collect(Collectors.toList());
        List<String> roleCodes = roleEntityList.stream().filter(garbageRoleEntity-> !garbageRoleEntity.getRoleCode().contains("COMMUNITY")).map(garbageRoleEntity -> garbageRoleEntity.getRoleCode()).collect(Collectors.toList());
        Long  start = DateFormatUtil.getFirstTimeOfDay(startTime).getTime();
        Long  end = DateFormatUtil.getLastTimeOfDay(endTime).getTime();
        StringBuilder builder = new StringBuilder("");
        builder.append("SELECT u.`name` as uname, u.phone, c.e_no, u.province_name, u.city_name,u.country_name,u.town_name, u.village_name, u.address, " +
                " CASE garbage_quality " +
                "   WHEN 1 then '合格' " +
                "   WHEN 2 THEN '不合格' " +
                "   ELSE '空桶' " +
                "END, c.garbage_point, c.garbage_weight, " +
                " CASE c.garbage_type " +
                "    WHEN 1 THEN '厨余垃圾'" +
                "    WHEN 2 THEN '其他垃圾'" +
                "    WHEN 3 THEN '可回收垃圾'" +
                "    WHEN 4 THEN '有害垃圾'" +
                "    WHEN 5 THEN '可回收- 铝金属'" +
                "    WHEN 6 THEN '可回收-铁'" +
                "    WHEN 7 THEN '可回收-铜'" +
                "    WHEN 8 THEN '可回收-纸张'" +
                "    when 9 then '可回收-瓶子'" +
                "    ELSE '可回收其他'" +
                " END," +
                " c.collector_name, c.collector_phone as collectPhone, FROM_UNIXTIME(c.collect_date / 1000,'%Y-%m-%d')," +
                " CASE c.is_check" +
                "   WHEN 1 THEN '已评分'" +
                "   ELSE '未评分'" +
                " END " +
                " FROM garbage_collector c " +
                " INNER JOIN garbage_user u ON u.id = c.user_id " +
                " WHERE 1 =1 and c.garbage_from_type = 0 " +
                "  AND c.is_check = 1 " +
                " AND c.collect_date >= " + start +
                " AND c.collect_date <= " + end );
        if (weight != null){
            builder.append(" AND c.garbage_weight >= " + weight);
        }
        if (point != null){
            builder.append(" AND c.garbage_point >=" + point);
        }
        if (garbageType != null){
            builder.append(" AND c.garbage_type = " + garbageType);
        }
        if (quality != null){
            builder.append(" AND c.garbage_quality = " + quality);
        }
        if ( "name".equals(type)){
            builder.append(" and u.name like '%" + keyWord + "%'");
        }
        if ("phone".equals(type)){
            builder.append(" and u.phone like '%" + keyWord +"%'");
        }
        if ("eNo".equals(type)){
            builder.append(" and ( (EXISTS ( SELECT n.user_id from garbage_e_no n WHERE n.user_id = u.id and n.e_no LIKE '%"+keyWord+"%')))");
        }
        if (!StringUtils.isEmpty(name)){
            builder.append(" AND c.collector_name LIKE '%"+name+"%'");
        }
        if (roleCodes.contains("VILLAGE_ADMIN")){
            builder.append(" and u.village_id = " + userEntity.getVillageId());
        }
        if (roleCodes.contains("TOWN_ADMIN")){
            if (villageId != null){
                builder.append(" and u.village_id = " + villageId);
            }
            builder.append(" and  u.town_id = " + userEntity.getTownId());
        }
        if (roleCodes.contains("COUNTRY_ADMIN")){
            if (villageId != null){
                builder.append(" and  u.village_id = " + villageId);
            }
            if (townId != null){
                builder.append(" and  u.town_id = " + townId);
            }
            builder.append(" and  u.country_id = " + userEntity.getCountryId());
        }
        if (roleCodes.contains("CITY_ADMIN")){
            if (villageId != null){
                builder.append(" and  u.village_id = " + villageId);
            }
            if (townId != null){
                builder.append(" and  u.town_id = " + townId);
            }
            if (countryId != null){
                builder.append(" and  u.country_id = " + countryId);
            }
            builder.append(" u.city_id = " + userEntity.getCityId());
        }
        if (roleCodes.contains("PROVINCE_ADMIN")){
            if (villageId != null){
                builder.append(" u.village_id = " + villageId);
            }
            if (townId != null){
                builder.append(" and  u.town_id = " + townId);
            }
            if (countryId != null){
                builder.append(" and  u.country_id = " + countryId);
            }
            if (cityId != null){
                builder.append(" and  u.city_id = " + cityId);
            }
            builder.append(" and  u.province_id = " + userEntity.getProvinceId());
        }
        if (sType != null){
            if (sType == 1){
                builder.append(" and u.dang_yuan = 1");
            }
            if (sType == 2){
                builder.append(" and u.cun_min_dai_biao = 1");
            }
            if (sType == 3){
                builder.append(" and u.street_comment_dai_biao = 1");
            }
            if (sType == 4){
                builder.append(" and u.liang_dai_biao_yi_wei_yuan = 1");
            }
            if (sType == 5){
                builder.append(" and u.cun_leader = 1");
            }
            if (sType == 6){
                builder.append(" and u.cun_zu_leader = 1" );
            }
            if (sType == 7){
                builder.append(" and u.women_exe_leader = 1");
            }
        }
        builder.append(" order by c.garbage_point DESC;");
        List<Object[]> data = entityManager.createNativeQuery(builder.toString()).getResultList();
        return data;
    }

    public List<Object[]> exportGarbageCollectorSummaryInfo(String type, String phone, String name, String startTime, String endTime, Long cityId, Long countryId, Long townId, Long villageId, Long communityId, String jwt) {
        Integer sub  = jwtUtil.getSubject(jwt);
        GarbageUserEntity userEntity = garbageUserDao.findById(sub).get();
        Integer fromType = userEntity.getFromType(); // 0农村  1小区
        List<GarbageRoleEntity> roleEntityList = userEntity.getRoles().stream().collect(Collectors.toList());
        List<String> roleCodes = roleEntityList.stream().filter(garbageRoleEntity-> !garbageRoleEntity.getRoleCode().contains("COMMUNITY")).map(garbageRoleEntity -> garbageRoleEntity.getRoleCode()).collect(Collectors.toList());
        List<Integer> communityIds = getCommunityResource(roleEntityList);
        Long  start = DateFormatUtil.getFirstTimeOfDay(startTime).getTime();
        Long  end = DateFormatUtil.getLastTimeOfDay(endTime).getTime();
        StringBuilder builder = new StringBuilder(" SELECT ");
        String groupBy = "";
        if ("day".equals(type)){
            builder.append(" FROM_UNIXTIME(c.collect_date / 1000,'%Y-%m-%d'),");
            groupBy +=" c.year, c.month, c.day";
        } else if ("month".equals(type)){
            builder.append(" FROM_UNIXTIME(c.collect_date / 1000,'%Y-%m'),");
            groupBy +=" c.year, c.month";
        }else {
            builder.append(" FROM_UNIXTIME(c.collect_date / 1000,'%Y'),");
            groupBy +=" c.year";
        }

        builder.append(" c.collector_name, c.collector_phone, " +
                " u.province_name,u.city_name,u.country_name,u.town_name,");
        if (fromType == 0){
            builder.append(" u.village_name, ");
        } else {
            builder.append(" u.community_name, ");
        }
        builder.append(" u.address, count(1), ROUND(SUM(c.garbage_weight),2)  " +
                " FROM garbage_collector c  " +
                " INNER JOIN garbage_user u on u.id = c.collector_id where 1 = 1");

        if (roleCodes.contains("VILLAGE_ADMIN")){
            builder.append(" and u.village_id = " + userEntity.getVillageId());
            groupBy +=" ,c.village_id";
        }
        if (roleCodes.contains("TOWN_ADMIN")){
            if (villageId != null){
                builder.append(" and u.village_id = " + villageId);
                groupBy +=" ,c.village_id";
            }
            builder.append(" and  u.town_id = " + userEntity.getTownId());
            groupBy +=" ,c.town_id";
        }
        if (roleCodes.contains("COUNTRY_ADMIN")){
            if (villageId != null){
                builder.append(" and  u.village_id = " + villageId);
                groupBy +=" ,c.village_id";
            }
            if (townId != null){
                builder.append(" and  u.town_id = " + townId);
                groupBy +=" ,c.town_id";
            }
            builder.append(" and  u.country_id = " + userEntity.getCountryId());
            groupBy +=" ,c.country_id";
        }
        if (roleCodes.contains("CITY_ADMIN")){
            if (villageId != null){
                builder.append(" and  u.village_id = " + villageId);
                groupBy +=" ,c.village_id";
            }
            if (townId != null){
                builder.append(" and  u.town_id = " + townId);
                groupBy +=" ,c.town_id";
            }
            if (countryId != null){
                builder.append(" and  u.country_id = " + countryId);
                groupBy +=" ,c.country_id";
            }
            builder.append(" u.city_id = " + userEntity.getCityId());
            groupBy +=" ,c.city_id";
        }
        if (roleCodes.contains("PROVINCE_ADMIN")){
            if (villageId != null){
                builder.append(" u.village_id = " + villageId);
                groupBy +=" ,c.village_id";
            }
            if (townId != null){
                builder.append(" and  u.town_id = " + townId);
                groupBy +=" ,c.town_id";
            }
            if (countryId != null){
                builder.append(" and  u.country_id = " + countryId);
                groupBy +=" ,c.country_id";
            }
            if (cityId != null){
                builder.append(" and  u.city_id = " + cityId);
                groupBy +=" ,c.city_id";
            }
            builder.append(" and  u.province_id = " + userEntity.getProvinceId());
            groupBy +=" ,c.province_id";
        }
        if (fromType == 1){
            if (communityIds.size()> 0){
                builder.append( " and u.community_id in ( ");
                for (int i = 0; i <communityIds.size() ; i++) {
                    Integer dd = communityIds.get(i);
                    if (i == communityIds.size() -1){
                        builder.append(  dd + " )");
                    } else {
                        builder.append(  dd + ", ");
                    }
                }
            }
        }
        if (!StringUtils.isEmpty(phone)){
            builder.append(" and c.collector_phone like '%"+phone+"%'");
        }
        if (!StringUtils.isEmpty(name)){
            builder.append(" and c.collector_name like '%"+name+"%'");
        }
        builder.append(" AND c.collect_date BETWEEN "+ start +" AND " + end);
        builder.append(" GROUP BY c.collector_id ," + groupBy);
        List<Object[]> data = entityManager.createNativeQuery(builder.toString()).getResultList();
        return data;
    }

    public List<Object[]> exportUserCollectDataAnalysis(String type, String keyWord, String startTime, String endTime, Long cityId, Long countryId, Long townId, Long villageId, Long communityId, String jwt) {
        Integer sub  = jwtUtil.getSubject(jwt);
        GarbageUserEntity userEntity = garbageUserDao.findById(sub).get();
        Integer fromType = userEntity.getFromType(); // 0农村  1小区
        List<GarbageRoleEntity> roleEntityList = userEntity.getRoles().stream().collect(Collectors.toList());
        List<String> roleCodes = roleEntityList.stream().filter(garbageRoleEntity-> !garbageRoleEntity.getRoleCode().contains("COMMUNITY")).map(garbageRoleEntity -> garbageRoleEntity.getRoleCode()).collect(Collectors.toList());
        List<Integer> communityIds = getCommunityResource(roleEntityList);
        Long  start = DateFormatUtil.getFirstTimeOfDay(startTime).getTime();
        Long  end = DateFormatUtil.getLastTimeOfDay(endTime).getTime();
        StringBuilder builder = new StringBuilder("");
        builder.append(" SELECT t.`name`,t.phone,t.province_name,t.city_name,t.country_name,t.town_name,t.village_name,t.address,COUNT(1),SUM(t.hg),SUM(t.nhg),SUM(t.kt)," +
                " concat(ROUND(sum(hg)*100/count(1),2), '%')," +
                " ROUND(SUM(t.garbage_weight),2) from ( " +
                " SELECT u.`name`,u.phone,u.province_name,u.city_name,u.country_name,u.town_name,u.village_name,u.address, " +
                " CASE c.garbage_quality" +
                "   WHEN 1 then 1" +
                "   ELSE 0" +
                " END as hg," +
                " CASE c.garbage_quality" +
                "   WHEN 2 then 1" +
                "   ELSE 0" +
                " END as nhg," +
                " CASE c.garbage_quality" +
                "   WHEN 3 then 1" +
                "   ELSE 0" +
                " END as kt, c.garbage_weight, c.user_id FROM garbage_collector c " +
                " INNER JOIN garbage_user u on c.user_id = u.id WHERE 1=1 ");

        if (start!=null){
            builder.append(" and  c.collect_date >= " + start  +" and c.collect_date <= " + end);
        }
        if ("name".equals(type)){
            builder.append(" and  u.name like '%"+keyWord + "%'");
        }
        if ("phone".equals(type)){
            builder.append(" and  u.phone like '%"+keyWord + "%'");
        }
        if (roleCodes.contains("VILLAGE_ADMIN")){
            builder.append(" and u.village_id = " + userEntity.getVillageId());
        }
        if (roleCodes.contains("TOWN_ADMIN")){
            if (villageId != null){
                builder.append(" and u.village_id = " + villageId);
            }
            builder.append(" and  u.town_id = " + userEntity.getTownId());
        }
        if (roleCodes.contains("COUNTRY_ADMIN")){
            if (villageId != null){
                builder.append(" and  u.village_id = " + villageId);
            }
            if (townId != null){
                builder.append(" and  u.town_id = " + townId);
            }
            builder.append(" and  u.country_id = " + userEntity.getCountryId());
        }
        if (roleCodes.contains("CITY_ADMIN")){
            if (villageId != null){
                builder.append(" and  u.village_id = " + villageId);
            }
            if (townId != null){
                builder.append(" and  u.town_id = " + townId);
            }
            if (countryId != null){
                builder.append(" and  u.country_id = " + countryId);
            }
            builder.append(" u.city_id = " + userEntity.getCityId());
        }
        if (roleCodes.contains("PROVINCE_ADMIN")){
            if (villageId != null){
                builder.append(" u.village_id = " + villageId);
            }
            if (townId != null){
                builder.append(" and  u.town_id = " + townId);
            }
            if (countryId != null){
                builder.append(" and  u.country_id = " + countryId);
            }
            if (cityId != null){
                builder.append(" and  u.city_id = " + cityId);
            }
            builder.append(" and  u.province_id = " + userEntity.getProvinceId());
        }
        if (fromType == 1){
            if (communityIds.size()> 0){
                builder.append( " and u.community_id in ( ");
                for (int i = 0; i <communityIds.size() ; i++) {
                    Integer dd = communityIds.get(i);
                    if (i == communityIds.size() -1){
                        builder.append(  dd + " )");
                    } else {
                        builder.append(  dd + ", ");
                    }
                }
            }
        }

        builder.append(" ) t  GROUP BY t.user_id ");
        List<Object[]> data = entityManager.createNativeQuery(builder.toString()).getResultList();
        return data;
    }

    public ResponseData addRecycleGarbage(Double weight, Integer garbageType, String jwt) {
        Integer sub  = jwtUtil.getSubject(jwt);
        GarbageUserEntity userEntity = garbageUserDao.findById(sub).get();
        Integer fromType = userEntity.getFromType(); // 0农村  1小区
        List<GarbageRoleEntity> roleEntityList = userEntity.getRoles().stream().collect(Collectors.toList());
        List<String> roleCodes = roleEntityList.stream().filter(garbageRoleEntity-> !garbageRoleEntity.getRoleCode().contains("COMMUNITY")).map(garbageRoleEntity -> garbageRoleEntity.getRoleCode()).collect(Collectors.toList());
        List<Integer> communityIds = getCommunityResource(roleEntityList);
        if (!roleCodes.contains("COLLECTOR")){
            throw new  RuntimeException("没有权限上传");
        }
        Long provinceId = userEntity.getProvinceId();
        Long cityId = userEntity.getCityId();
        Long countryId = userEntity.getCountryId();
        Long townId = userEntity.getTownId();
        Long villageId = userEntity.getVillageId();
        Long communityId = userEntity.getCommunityId();
        Calendar calendar = Calendar.getInstance();
        Integer year = calendar.get(Calendar.YEAR);
        Integer month = calendar.get(Calendar.MONTH);
        Integer day = calendar.get(Calendar.DAY_OF_MONTH);
        GarbageCollectorEntity collectorEntity = new GarbageCollectorEntity();
        collectorEntity.setProvinceId(provinceId.longValue());
        collectorEntity.setCityId(cityId);
        collectorEntity.setCountryId(countryId);
        collectorEntity.setTownId(townId);
        collectorEntity.setVillageId(villageId);
        collectorEntity.setCollectorPhone(userEntity.getPhone());
        collectorEntity.setCollectDate(new Date().getTime());
        collectorEntity.setDay(day);
        collectorEntity.setMonth(month);
        collectorEntity.setYear(year);
        List<GarbageENoEntity> eNoEntities = userEntity.geteNos();
        collectorEntity.seteNo(eNoEntities.get(0).geteNo());
        collectorEntity.setUserId(userEntity.getId());
        collectorEntity.setCollectorId(sub);
        collectorEntity.setCollectorName(userEntity.getName());
        collectorEntity.setGarbageFromType(fromType);
        BigDecimal bigDecimal = new BigDecimal(weight);
        weight = bigDecimal.setScale(3,BigDecimal.ROUND_HALF_UP).doubleValue();
        collectorEntity.setGarbageWeight(weight);
        collectorEntity.setGarbageType(garbageType);
        collectorEntity.setCheck(true);
        collectorEntity.setGarbageQuality(Constants.garbageQuality.QUALIFIED.getType());
        garbageCollectorDao.save(collectorEntity);
        ResponseData responseData = new ResponseData();
        responseData.setData(true);
        responseData.setStatus(Constants.responseStatus.Success.getStatus());
        responseData.setMsg("可回收垃圾上传成功");
        return responseData;
    }

    public ResponseData remarkAgain(Integer id, Integer quality, String remark, String jwt) {
        Integer sub  = jwtUtil.getSubject(jwt);
        GarbageUserEntity userEntity = garbageUserDao.findById(sub).get();
        Integer fromType = userEntity.getFromType(); // 0农村  1小区
        List<GarbageRoleEntity> roleEntityList = userEntity.getRoles().stream().collect(Collectors.toList());
        List<String> roleCodes = roleEntityList.stream().filter(garbageRoleEntity-> !garbageRoleEntity.getRoleCode().contains("COMMUNITY")).map(garbageRoleEntity -> garbageRoleEntity.getRoleCode()).collect(Collectors.toList());
        List<Integer> communityIds = getCommunityResource(roleEntityList);
        if (!roleCodes.contains("VILLAGE_ADMIN")){
            throw new  RuntimeException("没有权限重新核定评分");
        }
        GarbageCollectorEntity collectorEntity = garbageCollectorDao.findById(id).get();
        Long placeId = null;
        if (fromType == 1){
            placeId = userEntity.getCommunityId();
        } else {
            placeId = userEntity.getVillageId();
        }
        GarbageQualityPointEntity qualityPointEntity = garbageQualityPointDao.findByPlaceIdAndType(placeId, fromType);
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
        GarbagePointRecordEntity pointRecordEntity = garbagePointRecordDao.findBySourceNameAndBusId(GarbageCollectorEntity.class.getName(), id);
        GarbageRemarkAgainRecordEntity garbageRemarkAgainRecordEntity = new GarbageRemarkAgainRecordEntity();
        garbageRemarkAgainRecordEntity.setOldQuality(collectorEntity.getGarbageQuality());
        garbageRemarkAgainRecordEntity.setOldPoint(pointRecordEntity.getPoint());
        garbageRemarkAgainRecordEntity.setNewQuality(garbageQuality.getType());
        garbageRemarkAgainRecordEntity.setNewPoint(pointScore);
        garbageRemarkAgainRecordEntity.setSourceName(GarbageCollectorEntity.class.getName());
        garbageRemarkAgainRecordEntity.setBusId(id);
        garbageRemarkAgainRecordEntity.setRemark(remark);

        garbageRemarkAgainRecordDao.save(garbageRemarkAgainRecordEntity);

        GarbageUserPointEntity userPointEntity = garbageUserPointDao.findByUserId(collectorEntity.getUserId());
        userPointEntity.setPoint(userPointEntity.getPoint() - pointScore);
        garbageUserPointDao.save(userPointEntity);

        pointRecordEntity.setPoint(pointScore);
        pointRecordEntity.setDesc(desc);
        garbagePointRecordDao.save(pointRecordEntity);

        ResponseData responseData = new ResponseData();
        responseData.setData(true);
        responseData.setStatus(Constants.responseStatus.Success.getStatus());
        responseData.setMsg("垃圾分类质量重新评定成功");
        return responseData;
    }

    public ResponseData dataComparisonAndAnalysis(Integer pageNo, Integer pageSize, String startTime, String endTime, Long cityId, Long countryId, Long townId, Long villageId, Long communityId, String jwt, String[] orderBys) {
        Integer sub  = jwtUtil.getSubject(jwt);
        GarbageUserEntity userEntity = garbageUserDao.findById(sub).get();
        Integer fromType = userEntity.getFromType(); // 0农村  1小区
        List<GarbageRoleEntity> roleEntityList = userEntity.getRoles().stream().collect(Collectors.toList());
        List<String> roleCodes = roleEntityList.stream().filter(garbageRoleEntity-> !garbageRoleEntity.getRoleCode().contains("COMMUNITY")).map(garbageRoleEntity -> garbageRoleEntity.getRoleCode()).collect(Collectors.toList());
        List<Integer> communityIds = getCommunityResource(roleEntityList);
        Long  start = DateFormatUtil.getFirstTimeOfDay(startTime).getTime();
        Long  end = DateFormatUtil.getLastTimeOfDay(endTime).getTime();
        String column  = "";
        String sql = "";
        String pColumn = "";
        Long placeId = null;
        String whereColumn = "";
        if (roleCodes.contains("TOWN_ADMIN")){
            column = "village_id";
            sql = " INNER JOIN j_position_village v  on v.village_id = g.village_id";
            pColumn = "v.village_name";
            whereColumn = "town_id";
            placeId = userEntity.getTownId();
        } else if (roleCodes.contains("COUNTRY_ADMIN")){
            column = "town_id";
            sql = " INNER JOIN j_position_town town on town.town_id = g.town_id";
            pColumn = "town.town_name";
            whereColumn = "country_id";
            placeId = userEntity.getCountryId();
        } else if (roleCodes.contains("CITY_ADMIN")){
            column = "country_id";
            sql = " INNER JOIN j_position_county county on county.county_id = g." + column;
            pColumn = "county.county_name";
            whereColumn = "city_id";
            placeId = userEntity.getCityId();
        } else if (roleCodes.contains("PROVINCE_ADMIN")){
            column = "city_id";
            sql = " INNER JOIN j_position_city city on city.city_id = g." + column;
            pColumn = "city.city_name";
            whereColumn = "province_id";
            placeId = userEntity.getProvinceId();
        }
        StringBuilder builder = new StringBuilder("");
        builder.append("SELECT "+ pColumn+", g.weight,g.partInCount,u.total,ROUND(partInCount / u.total, 4) as partInRate, g.hegelv,g.buhege,g.kongtong, ");

        builder.append(" g." + column + " from ( SELECT  c.town_id, round(SUM(garbage_weight), 2) AS weight, COUNT(DISTINCT user_id) AS partInCount, " +
                " ROUND(SUM(CASE garbage_quality WHEN 1 THEN 1 ELSE 0 END) / COUNT(user_id),4) AS 'hegelv', " +
                " ROUND(SUM(CASE garbage_quality WHEN 2 THEN 1 ELSE 0 END) / COUNT(user_id),4) AS 'buhege'," +
                " ROUND(SUM(CASE garbage_quality WHEN 3 THEN 1  ELSE 0 END) / COUNT(user_id),4) AS 'kongtong', " +
                column +" "+
                " FROM  garbage_collector c WHERE c.collect_date >= "+start+" AND c.collect_date <= "+end+" and c.garbage_from_type = " + fromType +
                " GROUP BY " + column + ") g " +
                " INNER JOIN ( SELECT  " + column + ", count(id) AS total FROM  garbage_user GROUP BY " + column +" ) " +
                " u on u."+ column +"=" + "g." + column  + sql +
                " where g."+ whereColumn +"=" + placeId );

        System.out.println(builder.toString());
        BigInteger count = (BigInteger) entityManager.createNativeQuery("select count(1) from ( " + builder.toString() + " ) t").getSingleResult();
        Query nativeQuery = entityManager.createNativeQuery(builder.toString());
        int startSize = (pageNo -1)* pageSize;
        nativeQuery.setFirstResult(startSize);
        nativeQuery.setMaxResults(pageSize);
        List<Object[]> data = nativeQuery.getResultList();
        List<DataComparisonAndAnalysisDto> dtos = new ArrayList<>();
        data.forEach(n->{
            DataComparisonAndAnalysisDto dto = new DataComparisonAndAnalysisDto();
            dto.setPlaceName((String) n[0]);
            Double w = (Double) n[1];
            dto.setWeight(w);
            BigInteger pInt = (BigInteger) n[2];
            dto.setPartInCount(pInt.intValue());
            BigInteger tInt = (BigInteger) n[3];
            dto.setTotal(tInt.intValue());
            BigDecimal partRate = (BigDecimal) n[4];
            dto.setParticipationRate(partRate.doubleValue());
            BigDecimal qRate = (BigDecimal) n[5];
            dto.setQualityRate(qRate.doubleValue());
            BigDecimal nQRate = (BigDecimal) n[6];
            dto.setNotQualityRate(nQRate.doubleValue());
            BigDecimal eRate = (BigDecimal) n[7];
            dto.setEmptyRate(eRate.doubleValue());
            dtos.add(dto);
        });
        ResponsePageData responseData = new ResponsePageData();
        responseData.setStatus(Constants.responseStatus.Success.getStatus());
        responseData.setMsg("列表查询成功");
        responseData.setTotalElement(count.longValue());
        responseData.setPageNo(pageNo);
        responseData.setData(dtos);
        responseData.setPageSize(pageSize);
        return responseData;
    }

    public List<Object[]> exportDataComparisonAndAnalysis(String startTime, String endTime, Long cityId, Long countryId, Long townId, Long villageId, Long communityId, String jwt) {
        Integer sub  = jwtUtil.getSubject(jwt);
        GarbageUserEntity userEntity = garbageUserDao.findById(sub).get();
        Integer fromType = userEntity.getFromType(); // 0农村  1小区
        List<GarbageRoleEntity> roleEntityList = userEntity.getRoles().stream().collect(Collectors.toList());
        List<String> roleCodes = roleEntityList.stream().filter(garbageRoleEntity-> !garbageRoleEntity.getRoleCode().contains("COMMUNITY")).map(garbageRoleEntity -> garbageRoleEntity.getRoleCode()).collect(Collectors.toList());
        List<Integer> communityIds = getCommunityResource(roleEntityList);
        Long  start = DateFormatUtil.getFirstTimeOfDay(startTime).getTime();
        Long  end = DateFormatUtil.getLastTimeOfDay(endTime).getTime();
        String column  = "";
        String sql = "";
        String pColumn = "";
        Long placeId = null;
        String whereColumn = "";
        if (roleCodes.contains("TOWN_ADMIN")){
            column = "village_id";
            sql = " INNER JOIN j_position_village v  on v.village_id = g.village_id";
            pColumn = "v.village_name";
            whereColumn = "town_id";
            placeId = userEntity.getTownId();
        } else if (roleCodes.contains("COUNTRY_ADMIN")){
            column = "town_id";
            sql = " INNER JOIN j_position_town town on town.town_id = g.town_id";
            pColumn = "town.town_name";
            whereColumn = "country_id";
            placeId = userEntity.getCountryId();
        } else if (roleCodes.contains("CITY_ADMIN")){
            column = "country_id";
            sql = " INNER JOIN j_position_county county on county.county_id = g." + column;
            pColumn = "county.county_name";
            whereColumn = "city_id";
            placeId = userEntity.getCityId();
        } else if (roleCodes.contains("PROVINCE_ADMIN")){
            column = "city_id";
            sql = " INNER JOIN j_position_city city on city.city_id = g." + column;
            pColumn = "city.city_name";
            whereColumn = "province_id";
            placeId = userEntity.getProvinceId();
        }
        StringBuilder builder = new StringBuilder("");
        builder.append("SELECT "+ pColumn+", g.weight,g.partInCount,u.total,ROUND(partInCount / u.total, 4) as partInRate, g.hegelv,g.buhege,g.kongtong, ");

        builder.append(" g." + column + " from ( SELECT  c.town_id, round(SUM(garbage_weight), 2) AS weight, COUNT(DISTINCT user_id) AS partInCount, " +
                " ROUND(SUM(CASE garbage_quality WHEN 1 THEN 1 ELSE 0 END) / COUNT(user_id),4) AS 'hegelv', " +
                " ROUND(SUM(CASE garbage_quality WHEN 2 THEN 1 ELSE 0 END) / COUNT(user_id),4) AS 'buhege'," +
                " ROUND(SUM(CASE garbage_quality WHEN 3 THEN 1  ELSE 0 END) / COUNT(user_id),4) AS 'kongtong', " +
                column +" "+
                " FROM  garbage_collector c WHERE c.collect_date >= "+start+" AND c.collect_date <= "+end+" and c.garbage_from_type = " + fromType +
                " GROUP BY " + column + ") g " +
                " INNER JOIN ( SELECT  " + column + ", count(id) AS total FROM  garbage_user GROUP BY " + column +" ) " +
                " u on u."+ column +"=" + "g." + column  + sql +
                " where g."+ whereColumn +"=" + placeId );

        System.out.println(builder.toString());
        Query nativeQuery = entityManager.createNativeQuery(builder.toString());
        List<Object[]> data = nativeQuery.getResultList();
        return data;
    }
}


