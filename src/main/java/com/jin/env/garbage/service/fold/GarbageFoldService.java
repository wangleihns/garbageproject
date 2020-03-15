package com.jin.env.garbage.service.fold;

import com.jin.env.garbage.dao.fold.GarbageFoldDao;
import com.jin.env.garbage.dao.garbage.GarbageCollectorDao;
import com.jin.env.garbage.dao.image.GarbageImageDao;
import com.jin.env.garbage.dao.user.GarbageENoDao;
import com.jin.env.garbage.dao.user.GarbageUserDao;
import com.jin.env.garbage.dto.fold.FoldDto;
import com.jin.env.garbage.dto.fold.FoldPersonInfoDto;
import com.jin.env.garbage.dto.garbage.CollectorDto;
import com.jin.env.garbage.entity.fold.GarbageFoldEntity;
import com.jin.env.garbage.entity.garbage.GarbageCollectorEntity;
import com.jin.env.garbage.entity.image.GarbageImageEntity;
import com.jin.env.garbage.entity.user.GarbageENoEntity;
import com.jin.env.garbage.entity.user.GarbageRoleEntity;
import com.jin.env.garbage.entity.user.GarbageUserEntity;
import com.jin.env.garbage.jwt.JwtUtil;
import com.jin.env.garbage.utils.Constants;
import com.jin.env.garbage.utils.DateFormatUtil;
import com.jin.env.garbage.utils.ResponseData;
import com.jin.env.garbage.utils.ResponsePageData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class GarbageFoldService {
    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private GarbageUserDao garbageUserDao;

    @Autowired
    private GarbageFoldDao garbageFoldDao;

    @Autowired
    private GarbageENoDao garbageENoDao;

    @Autowired
    private GarbageImageDao garbageImageDao;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private GarbageCollectorDao garbageCollectorDao;

    @Transactional
    public ResponseData remarkFold(String phone, String name, String ids, Integer result, String remark, Integer score, String jwt) {
        ResponseData responseData = new ResponsePageData();
        Integer sub = jwtUtil.getSubject(jwt);
        GarbageUserEntity userEntity = garbageUserDao.findByPhone(phone);
        if (userEntity == null){
            throw new RuntimeException("此手机号不存在");
        }
        GarbageFoldEntity foldEntity = new GarbageFoldEntity();
        foldEntity.setApproverId(sub);
        foldEntity.setResult(result);
        foldEntity.setUserId(userEntity.getId());
        foldEntity.setRemark(remark);
        foldEntity.setScore(score);
        foldEntity = garbageFoldDao.save(foldEntity);
        List<Integer> imageIds = new ArrayList<>();
        String[] a = ids.split(",");
        for (int i = 0; i < a.length; i++) {
            imageIds.add(Integer.valueOf(a[i]));
        }
        List<GarbageImageEntity> imageEntityList = garbageImageDao.findAll(new Specification<GarbageImageEntity>() {
            @Override
            public Predicate toPredicate(Root<GarbageImageEntity> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                Predicate id = root.get("id").in(imageIds);
                Predicate typePredicate =criteriaBuilder.equal(root.get("type"), Constants.image.FOLD.name());
                return criteriaBuilder.and(id, typePredicate);
            }
        });
        for (GarbageImageEntity garbageImageEntity : imageEntityList) {
            garbageImageEntity.setBusId(foldEntity.getId());
        }
        garbageImageDao.saveAll(imageEntityList);
        responseData.setStatus(200);
        responseData.setMsg("提交成功");
        return  responseData;
    }

    public ResponseData checkUserInfo(String phone, String eno) {
        ResponseData responseData = new ResponsePageData();
        GarbageUserEntity userEntity = garbageUserDao.findByPhone(phone);
        if (userEntity == null){
            throw new RuntimeException("此手机号不存在");
        }
        GarbageENoEntity byENo = garbageENoDao.findByENo(eno);
        if (byENo.getUserId() != userEntity.getId()){
            throw new RuntimeException("手机号与电子卡号对应的不是同一个人");
        }
        responseData.setStatus(200);
        responseData.setMsg("信息匹配");
        return  responseData;
    }

    public ResponseData getFoldList(Integer pageNo, Integer pageSize, String phone, String jwt) {
        Integer sub = jwtUtil.getSubject(jwt);
        GarbageUserEntity userEntity = garbageUserDao.findById(sub).get();
        List<String> roleCodes = userEntity.getRoles().stream().map(n->n.getRoleCode()).collect(Collectors.toList());
        StringBuilder builder  = new StringBuilder("select f.id, u.`name`, u.phone, " +
                " CASE f.result " +
                "   WHEN 1 THEN '优' " +
                "   WHEN 2 THEN '良' " +
                "   WHEN 3 THEN '查' " +
                "   END,  " +
                " f.score, remark from garbage_fold f " +
                " INNER JOIN garbage_user u on f.user_id = u.id where 1 = 1");
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
        } else if (roleCodes.contains("RESIDENT")){
            builder.append(" and u.id = "  + userEntity.getId());
        }
        if (!StringUtils.isEmpty(phone)){
            builder.append(" and u.phone like '%" + phone + "%'"  + " or u.name like '%" + phone + "%'");
        }
        Integer start = (pageNo-1)*pageNo;
        Integer end = pageNo*pageSize;
        builder.append(" order by f.id desc limit " + start  + "," +end);
        Query nativeQuery = entityManager.createNativeQuery(builder.toString());
        List<Object[]> data =nativeQuery.getResultList();
        List<FoldDto> dtos = new ArrayList<>();
        for (int i = 0; i < data.size(); i++) {
            FoldDto dto = new FoldDto();
            dto.setId((Integer) data.get(i)[0]);
            dto.setName((String)data.get(i)[1]);
            dto.setPhone((String) data.get(i)[2]);
            dto.setResult((String) data.get(i)[3]);
            dto.setScore((Integer) data.get(i)[4]);
            dto.setRemark((String) data.get(i)[5]);
            dtos.add(dto);
        }
        ResponseData responseData = new ResponseData();
        responseData.setStatus(200);
        responseData.setMsg("查询成功");
        responseData.setData(dtos);
        return  responseData;
    }

    public ResponseData getFoldInfoById(Integer id) {
        ResponseData responseData = new ResponseData();
        GarbageFoldEntity foldEntity = garbageFoldDao.getOne(id);
        List<GarbageImageEntity> imageEntityList = garbageImageDao.findBySourceNameAndTypeAndBusId(GarbageFoldEntity.class.getName(),
                Constants.image.FOLD.name(), foldEntity.getId());
        GarbageUserEntity userEntity = garbageUserDao.getOne(foldEntity.getUserId());
        GarbageUserEntity userEntity1 = garbageUserDao.getOne(foldEntity.getApproverId());
        FoldDto dto = new FoldDto();
        dto.setName(userEntity.getName());
        dto.setPhone(userEntity.getPhone());
//        if (foldEntity.getResult() == 1){
////            dto.setResult("优");
////        }
////        if (foldEntity.getResult() == 2){
////            dto.setResult("良");
////        }
////        if (foldEntity.getResult() == 3){
////            dto.setResult("差");
////        }
        dto.setResult(foldEntity.getResult().toString());
        dto.setRemark(foldEntity.getRemark());
        dto.setScore(foldEntity.getScore());
        dto.setImages(imageEntityList.stream().map(n->n.getImagePath()).collect(Collectors.toList()));

        dto.setApproveName(userEntity1.getName());
        dto.setAppronePhone(userEntity1.getPhone());
        responseData.setStatus(200);
        responseData.setMsg("查询成功");
        responseData.setData(dto);
        return  responseData;
    }

    public ResponseData getUserInfo(String jwt) {
        ResponseData responseData = new ResponseData();
        Integer sub = jwtUtil.getSubject(jwt);
        GarbageUserEntity userEntity = garbageUserDao.findById(sub).get();
        String name = userEntity.getName();
        Set<GarbageRoleEntity> roleEntities = userEntity.getRoles();
        String roleName = "";
        for (GarbageRoleEntity n : roleEntities) {
            roleName = n.getRoleName();
        }
        String placeName = userEntity.getTownName();
        Long start = DateFormatUtil.getFirstTimeOfDay(DateFormatUtil.formatDate(new Date(), "yyyy-MM-dd")).getTime();
        Long end = DateFormatUtil.getLastTimeOfDay(DateFormatUtil.formatDate(new Date(), "yyyy-MM-dd")).getTime();
        List<GarbageFoldEntity> foldEntities= garbageFoldDao.findAll(new Specification<GarbageFoldEntity>() {
            @Override
            public Predicate toPredicate(Root<GarbageFoldEntity> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                Predicate predicate = criteriaBuilder.equal(root.get("userId"), sub);
                Predicate predicateStart = criteriaBuilder.greaterThanOrEqualTo(root.get("createTime"), start);
                Predicate predicateEnd = criteriaBuilder.lessThanOrEqualTo(root.get("createTime"), end);
                return criteriaBuilder.and(predicate, predicateStart, predicateEnd);
            }
        });
        GarbageFoldEntity foldEntity = null;
        if (foldEntities.size() > 0){
            foldEntity = foldEntities.get(0);
        }
        String result = "未参加";
        if (foldEntity !=null){
            if (foldEntity.getResult() == 1){
                result ="优";
            } else if (foldEntity.getResult() == 2){
                result ="良";
            }else{
                result ="差";
            }
        }
        List<GarbageCollectorEntity> garbageCollectorEntityList = garbageCollectorDao.findAll(new Specification<GarbageCollectorEntity>() {
            @Override
            public Predicate toPredicate(Root<GarbageCollectorEntity> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                Predicate predicate = criteriaBuilder.equal(root.get("userId"), sub);
                Predicate predicateStart = criteriaBuilder.greaterThanOrEqualTo(root.get("collectDate"), start);
                Predicate predicateEnd = criteriaBuilder.lessThanOrEqualTo(root.get("collectDate"), end);
                return criteriaBuilder.and(predicate, predicateStart, predicateEnd);
            }
        });
       String garbageResult = "未参加";
       Double kitchenWeight=0.0;
       Double otherWeight = 0.0;
       Double recycleWeight = 0.0;
        for (GarbageCollectorEntity collectorEntity : garbageCollectorEntityList) {
            if (collectorEntity.getGarbageType() == Constants.garbageType.KITCHEN_GARBAGE.getType()){
                garbageResult = collectorEntity.getGarbageQuality() == 1 ?"合格":(collectorEntity.getGarbageQuality() == 2?"不合格":"空桶");
                kitchenWeight = collectorEntity.getGarbageWeight();
            }
            if (collectorEntity.getGarbageType() == Constants.garbageType.OTHER_GARBAGE.getType()){
                garbageResult = collectorEntity.getGarbageQuality() == 1 ?"合格":(collectorEntity.getGarbageQuality() == 2?"不合格":"空桶");
                otherWeight = collectorEntity.getGarbageWeight();
            }

            if (collectorEntity.getGarbageType() == Constants.garbageType.RECYCLEABLE.getType()){
                garbageResult = collectorEntity.getGarbageQuality() == 1 ?"合格":(collectorEntity.getGarbageQuality() == 2?"不合格":"空桶");
                recycleWeight = collectorEntity.getGarbageWeight();
            }
        }
        FoldPersonInfoDto personInfoDto = new FoldPersonInfoDto();
        personInfoDto.setName(name);
        personInfoDto.setPlaceName(placeName);
        personInfoDto.setRoleName(roleName);
        personInfoDto.setFoldResult(result);
        personInfoDto.setGarbageResult(garbageResult);
        personInfoDto.setKitchenWeight(kitchenWeight);
        personInfoDto.setOtherWeight(otherWeight);
        personInfoDto.setRecycleWeight(recycleWeight);

        responseData.setStatus(200);
        responseData.setMsg("查询成功");
        responseData.setData(personInfoDto);
        return  responseData;
    }

    public ResponseData getUserList(Integer pageNo, Integer pageSize, String phone, String jwt) {
        Integer sub = jwtUtil.getSubject(jwt);
        GarbageUserEntity userEntity = garbageUserDao.findById(sub).get();
        List<String> roleCodes = userEntity.getRoles().stream().map(n->n.getRoleCode()).collect(Collectors.toList());
        Pageable pageable = PageRequest.of(pageNo -1, pageSize,new Sort(Sort.Direction.DESC,"id"));
        if (!roleCodes.contains("VILLAGE_ADMIN")){
            throw new RuntimeException("只有村管理员才能查看居民信息");
        }
        Page<GarbageUserEntity> page = garbageUserDao.findAll(new Specification<GarbageUserEntity>() {
            @Override
            public Predicate toPredicate(Root<GarbageUserEntity> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList<>();
                Predicate predicate = criteriaBuilder.equal(root.get("villageId"),userEntity.getVillageId());
                predicates.add(predicate);
                if(!StringUtils.isEmpty(phone)){
                    Predicate predicatePhone = criteriaBuilder.like(root.get("phone"), "%"+phone + "%");
                    Predicate predicateName = criteriaBuilder.like(root.get("name"), "%" + phone + "%");
                    predicates.add(criteriaBuilder.or(predicatePhone, predicateName));
                }
                return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        }, pageable);
        ResponseData responseData = new ResponseData();
        responseData.setStatus(200);
        responseData.setMsg("查询成功");
        responseData.setData(page);
        return  responseData;
    }
}
