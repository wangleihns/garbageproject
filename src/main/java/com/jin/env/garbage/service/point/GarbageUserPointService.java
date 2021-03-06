package com.jin.env.garbage.service.point;

import com.aliyuncs.exceptions.ClientException;
import com.jin.env.garbage.dao.garbage.GarbageQualityPointDao;
import com.jin.env.garbage.dao.point.GarbagePointRecordDao;
import com.jin.env.garbage.dao.point.GarbageUserPointDao;
import com.jin.env.garbage.dao.position.GarbageCommunityDao;
import com.jin.env.garbage.dao.position.JPositionVillageDao;
import com.jin.env.garbage.dao.user.GarbageUserDao;
import com.jin.env.garbage.dto.point.RedAndBlackRankDto;
import com.jin.env.garbage.dto.point.UserPointRankDto;
import com.jin.env.garbage.dto.position.UserPositionDto;
import com.jin.env.garbage.entity.card.GarbagePointCardEntity;
import com.jin.env.garbage.entity.garbage.GarbageQualityPointEntity;
import com.jin.env.garbage.entity.point.GarbagePointRecordEntity;
import com.jin.env.garbage.entity.point.GarbageUserPointEntity;
import com.jin.env.garbage.entity.position.GarbageCommunityEntity;
import com.jin.env.garbage.entity.position.JPositionVillageEntity;
import com.jin.env.garbage.entity.user.GarbageENoEntity;
import com.jin.env.garbage.entity.user.GarbageRoleEntity;
import com.jin.env.garbage.entity.user.GarbageUserEntity;
import com.jin.env.garbage.jwt.JwtUtil;
import com.jin.env.garbage.service.garbage.GarbageCollectorService;
import com.jin.env.garbage.utils.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.criteria.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@Service
@SuppressWarnings("ALL")
public class GarbageUserPointService {
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private GarbageUserDao garbageUserDao;

    @Autowired
    private GarbageUserPointDao garbageUserPointDao;

    @Autowired
    private GarbageCollectorService collectorService;

    @Autowired
    private GarbageQualityPointDao garbageQualityPointDao;

    @Autowired
    private GarbageCommunityDao garbageCommunityDao;

    @Autowired
    private JPositionVillageDao jPositionVillageDao;

    @Autowired
    private GarbagePointRecordDao garbagePointRecordDao;

    @Autowired
    private EntityManager entityManager;

    public ResponseData getPointRankList(Integer pageNo, Integer pageSize, String type, String keyWord, String jwt, Long cityId, Long countryId, Long townId, Long villageId, Integer communityId, String[] orderBys) {
        Integer sub  = jwtUtil.getSubject(jwt);
        GarbageUserEntity userEntity = garbageUserDao.findById(sub).get();
        Integer fromType = userEntity.getFromType(); // 0农村  1小区
        List<GarbageRoleEntity> roleEntityList = userEntity.getRoles().stream().collect(Collectors.toList());
        List<String> roleCodes = roleEntityList.stream().filter(garbageRoleEntity-> !garbageRoleEntity.getRoleCode().contains("COMMUNITY")).map(garbageRoleEntity -> garbageRoleEntity.getRoleCode()).collect(Collectors.toList());
        List<Integer> communityIds = collectorService.getCommunityResource(roleEntityList);
        Pageable pageable = PageRequest.of(pageNo - 1, pageSize, getPointRankListSort(orderBys));
        Page<GarbageUserPointEntity> page = garbageUserPointDao.findAll(new Specification<GarbageUserPointEntity>() {
            @Nullable
            @Override
            public Predicate toPredicate(Root<GarbageUserPointEntity> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList<>();
                if (!StringUtils.isEmpty(type)){
                    if (!StringUtils.isEmpty(keyWord) && "name".equals(type)){
                        Predicate predicate = criteriaBuilder.like(root.get("userName"), "%" + keyWord + "%");
                        predicates.add(predicate);
                    }
                    if (!StringUtils.isEmpty(keyWord) && "phone".equals(type)){
                        Predicate predicate = criteriaBuilder.like(root.get("phone"), "%" + keyWord + "%");
                        predicates.add(predicate);
                    }
                    if (!StringUtils.isEmpty(keyWord) && "eNo".equals(type)){
                        Subquery subquery = criteriaQuery.subquery(GarbageENoEntity.class);
                        Root subRoot = subquery.from(GarbageENoEntity.class);
                        subquery.select(subRoot.get("userId"));
                        Predicate equal = criteriaBuilder.equal(root.get("userId"), subRoot.get("userId"));
                        List<Predicate> selectList = new ArrayList<>();
                        selectList.add(equal);
                        Predicate elike = criteriaBuilder.like(subRoot.get("eNo"), "%" + keyWord + "%");
                        selectList.add(elike);
                        Predicate predicateExits = criteriaBuilder.exists(subquery.where(selectList.toArray(new Predicate[selectList.size()])));
                        predicates.add(predicateExits);
                    }
                }
                if (fromType == 1){
                    if (communityId != null){
                        Predicate predicate = criteriaBuilder.equal(root.get("communityId"), communityId);
                        predicates.add(predicate);
                    } else {
                        if (communityIds.size()> 0){
                            Predicate predicate = root.get("communityId").in(communityIds);
                            predicates.add(predicate);
                        }
                    }
                }else {
                    if (roleCodes.contains("VILLAGE_ADMIN")){
                        Predicate predicateVillageId= criteriaBuilder.equal(root.get("villageId"), userEntity.getVillageId());
                        predicates.add(predicateVillageId);
                    }
                    if (roleCodes.contains("TOWN_ADMIN")){
                        if ( townId != null && villageId !=null){
                            Predicate predicateVillageId= criteriaBuilder.equal(root.get("villageId"), villageId);
                            predicates.add(predicateVillageId);
                        } else {
                            Predicate predicateTownId= criteriaBuilder.equal(root.get("townId"), userEntity.getTownId());
                            predicates.add(predicateTownId);
                        }
                    }
                    if (roleCodes.contains("COUNTRY_ADMIN")){
                        if (townId !=null ){
                            Predicate predicateTownId= criteriaBuilder.equal(root.get("townId"), townId);
                            predicates.add(predicateTownId);
                        } else if (countryId != null && townId !=null && villageId !=null){
                            Predicate predicateTownId= criteriaBuilder.equal(root.get("townId"), townId);
                            predicates.add(predicateTownId);
                        }else {
                            Predicate predicateCountryId = criteriaBuilder.equal(root.get("countryId"), userEntity.getCountryId());
                            predicates.add(predicateCountryId);
                        }
                    }
                    if (roleCodes.contains("CITY_ADMIN")){
                        if (countryId !=null){
                            Predicate predicateCountryId = criteriaBuilder.equal(root.get("countryId"), countryId);
                            predicates.add(predicateCountryId);
                        } else if (cityId !=null && countryId !=null && townId !=null){
                            Predicate predicateTownId= criteriaBuilder.equal(root.get("townId"), townId);
                            predicates.add(predicateTownId);
                        } else if (cityId !=null && countryId !=null && townId !=null && villageId != null){
                            Predicate predicateTownId= criteriaBuilder.equal(root.get("villageId"), villageId);
                            predicates.add(predicateTownId);
                        } else {
                            Predicate predicateCityId = criteriaBuilder.equal(root.get("cityId"), userEntity.getCityId());
                            predicates.add(predicateCityId);
                        }
                    }
                    if (roleCodes.contains("PROVINCE_ADMIN")){
                        if (cityId !=null){
                            Predicate predicateCityId = criteriaBuilder.equal(root.get("cityId"), cityId);
                            predicates.add(predicateCityId);
                        } else if (cityId !=null && countryId !=null){
                            Predicate predicateCountryId = criteriaBuilder.equal(root.get("countryId"), countryId);
                            predicates.add(predicateCountryId);
                        } else if (cityId !=null && countryId !=null && townId !=null){
                            Predicate predicateTownId= criteriaBuilder.equal(root.get("townId"), townId);
                            predicates.add(predicateTownId);
                        } else if (cityId !=null && countryId !=null && townId !=null && villageId != null  ){
                            Predicate predicateTownId= criteriaBuilder.equal(root.get("villageId"), villageId);
                            predicates.add(predicateTownId);
                        } else {
                            Predicate predicateProvinceId = criteriaBuilder.equal(root.get("provinceId"), userEntity.getProvinceId());
                            predicates.add(predicateProvinceId);
                        }
                    }
                }
                return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        },pageable);
        List<UserPointRankDto> rankDtos = new ArrayList<>();
        Integer rank = (pageNo - 1)*pageSize;
        for (GarbageUserPointEntity pointEntity :page.getContent()) {
            rank ++;
            UserPointRankDto dto = new UserPointRankDto();
            dto.setRank(rank);
            dto.setUserName(pointEntity.getUserName());
            dto.setPhone(pointEntity.getPhone());
            dto.setPoint(pointEntity.getPoint());
            String planceName = pointEntity.getProvinceName() + pointEntity.getCityName() + pointEntity.getCountryName();
            dto.setPlaceName(planceName);
            String address = pointEntity.getTownName() == null ?"": pointEntity.getTownName() +
                    pointEntity.getVillageName() == null ?"":pointEntity.getVillageName() +
                    pointEntity.getCommunityName() == null ?"":pointEntity.getCommunityName();
            dto.setAddress(address);
            rankDtos.add(dto);
        }
        ResponsePageData responsePageData = new ResponsePageData();
        responsePageData.setPageNo(pageNo);
        responsePageData.setPageSize(pageSize);
        responsePageData.setCount(page.getTotalPages());
        responsePageData.setLastPage(page.isLast());
        responsePageData.setFirstPage(page.isFirst());
        responsePageData.setTotalElement(page.getTotalElements());
        responsePageData.setStatus(Constants.responseStatus.Success.getStatus());
        responsePageData.setMsg("查询成功");
        responsePageData.setData(rankDtos);
        return responsePageData;
    }

    private Sort getPointRankListSort(String[] orderBys){
        Sort sort = null;
        if (orderBys == null || orderBys.length == 0 ){
            sort = Sort.by("point").descending();
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

    public ResponseData redAndBlackRank(String jwt) {
        Integer sub = jwtUtil.getSubject(jwt);
        GarbageUserEntity userEntity = garbageUserDao.findById(sub).get();
        Integer fromType = userEntity.getFromType();
        Pageable pageableFirst = PageRequest.of(0, 10, redAndBlackRankSort("first"));
        Pageable pageableLast = PageRequest.of(0, 10, redAndBlackRankSort("last"));
        Page<GarbageUserPointEntity> userPointFirstPage = null;
        Page<GarbageUserPointEntity> userPointLastPage = null;
        List<GarbageRoleEntity> roleEntityList = userEntity.getRoles().stream().collect(Collectors.toList());
        List<Integer> communityIds = collectorService.getCommunityResource(roleEntityList);
        List<String> roleCodes = roleEntityList.stream().map(garbageRoleEntity -> garbageRoleEntity.getRoleCode()).collect(Collectors.toList());
        userPointFirstPage  = garbageUserPointDao.findAll(new Specification<GarbageUserPointEntity>() {
            @Override
            public Predicate toPredicate(Root<GarbageUserPointEntity> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicateList = new ArrayList<>();
                if (fromType == 1) {
                    if (roleCodes.stream().filter(n -> n.endsWith("COMMUNITY_ADMIN")).count() > 0) {
                        if (communityIds.size() > 0) {
                            Predicate predicate = root.get("communityId").in(communityIds);
                            predicateList.add(predicate);
                        }
                    }
                    if (roleCodes.stream().filter(n -> n.endsWith("COMMUNITY_REMARK")).count() > 0) {
                        predicateList.add(criteriaBuilder.equal(root.get("communityId"), userEntity.getCommunityId()));
                    }
                    if (roleCodes.size() == 1 && "RESIDENT".equals(roleCodes.get(0))){
                        Predicate predicate = criteriaBuilder.equal(root.get("communityId"), userEntity.getCommunityId());
                        predicateList.add(predicate);
                    }
                } else {
                    if (roleCodes.size() == 1 && "RESIDENT".equals(roleCodes.get(0)) || roleCodes.contains("COLLECTOR")){
                        Predicate predicate = criteriaBuilder.equal(root.get("villageId"), userEntity.getVillageId());
                        predicateList.add(predicate);
                    }
                    if (roleCodes.contains("VILLAGE_ADMIN")){
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
                }

                return criteriaBuilder.and(predicateList.toArray(new Predicate[predicateList.size()]));
            }
        }, pageableFirst);

        userPointLastPage = garbageUserPointDao.findAll(new Specification<GarbageUserPointEntity>() {
            @Override
            public Predicate toPredicate(Root<GarbageUserPointEntity> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicateList = new ArrayList<>();
                if (fromType == 1) {
                    if (roleCodes.stream().filter(n -> n.endsWith("COMMUNITY_ADMIN")).count() > 0) {
                        if (communityIds.size() > 0) {
                            Predicate predicate = root.get("communityId").in(communityIds);
                            predicateList.add(predicate);
                        }
                    }
                    if (roleCodes.stream().filter(n -> n.endsWith("COMMUNITY_REMARK")).count() > 0) {
                        predicateList.add(criteriaBuilder.equal(root.get("communityId"), userEntity.getCommunityId()));
                    }
                    if (roleCodes.size() == 1 && "RESIDENT".equals(roleCodes.get(0))){
                        Predicate predicate = criteriaBuilder.equal(root.get("communityId"), userEntity.getCommunityId());
                        predicateList.add(predicate);
                    }
                } else {
                    if (roleCodes.size() == 1 && "RESIDENT".equals(roleCodes.get(0)) || roleCodes.contains("COLLECTOR")){
                        Predicate predicate = criteriaBuilder.equal(root.get("villageId"), userEntity.getVillageId());
                        predicateList.add(predicate);
                    }
                    if (roleCodes.contains("VILLAGE_ADMIN")){
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
                }
                return criteriaBuilder.and(predicateList.toArray(new Predicate[predicateList.size()]));
            }
        }, pageableLast);

        List<RedAndBlackRankDto> redRankDtos = new ArrayList<>();
        userPointFirstPage.getContent().forEach(garbageUserPointEntity -> {
            RedAndBlackRankDto dto = new RedAndBlackRankDto();
            if (fromType == 1){
                dto.setPlaceName(garbageUserPointEntity.getCommunityName());
            } else {
                if (roleCodes.size() == 1 && "RESIDENT".equals(roleCodes.get(0)) || roleCodes.contains("COLLECTOR")){
                    dto.setPlaceName(garbageUserPointEntity.getVillageName());
                }
                if (roleCodes.contains("VILLAGE_ADMIN")){
                    dto.setPlaceName(garbageUserPointEntity.getVillageName());
                }
                if (roleCodes.contains("TOWN_ADMIN")){
                    dto.setPlaceName(garbageUserPointEntity.getTownName());
                }
                if (roleCodes.contains("COUNTRY_ADMIN")){
                    dto.setPlaceName(garbageUserPointEntity.getCountryName());
                }
                if (roleCodes.contains("CITY_ADMIN")){
                    dto.setPlaceName(garbageUserPointEntity.getCityName());
                }
                if (roleCodes.contains("PROVINCE_ADMIN")|| roleCodes.contains("SYSTEM_ADMIN")){
                    dto.setPlaceName(garbageUserPointEntity.getProvinceName());
                }
            }
            dto.setPoint(garbageUserPointEntity.getPoint());
            dto.setUserName(garbageUserPointEntity.getUserName());
            redRankDtos.add(dto);
        });

        List<RedAndBlackRankDto> blackRankDtos = new ArrayList<>();
        userPointLastPage.getContent().forEach(garbageUserPointEntity -> {
            RedAndBlackRankDto dto = new RedAndBlackRankDto();
            if (fromType == 1){
                dto.setPlaceName(garbageUserPointEntity.getCommunityName());
            } else {
                if (roleCodes.size() == 1 && "RESIDENT".equals(roleCodes.get(0)) || roleCodes.contains("COLLECTOR")){
                    dto.setPlaceName(garbageUserPointEntity.getVillageName());
                }
                if (roleCodes.contains("VILLAGE_ADMIN")){
                    dto.setPlaceName(garbageUserPointEntity.getVillageName());
                }
                if (roleCodes.contains("TOWN_ADMIN")){
                    dto.setPlaceName(garbageUserPointEntity.getTownName());
                }
                if (roleCodes.contains("COUNTRY_ADMIN")){
                    dto.setPlaceName(garbageUserPointEntity.getCountryName());
                }
                if (roleCodes.contains("CITY_ADMIN")){
                    dto.setPlaceName(garbageUserPointEntity.getCityName());
                }
                if (roleCodes.contains("PROVINCE_ADMIN") || roleCodes.contains("SYSTEM_ADMIN")){
                    dto.setPlaceName(garbageUserPointEntity.getProvinceName());
                }
            }
            dto.setPoint(garbageUserPointEntity.getPoint());
            dto.setUserName(garbageUserPointEntity.getUserName());
            blackRankDtos.add(dto);
        });
        Map<String, List<RedAndBlackRankDto>> redAndBlackRankMap = new HashMap<>();
        redAndBlackRankMap.put("redRank", redRankDtos);
        redAndBlackRankMap.put("blackRank", blackRankDtos);
        ResponseData responseData = new ResponseData();
        responseData.setMsg("红黑榜查询成功");
        responseData.setStatus(Constants.responseStatus.Success.getStatus());
        responseData.setData(redAndBlackRankMap);
        return  responseData;
    }

    private Sort redAndBlackRankSort(String type){
        Sort sort = null;
        if ("first".equals(type) ){
            sort = Sort.by("point").descending();
        }else {
            sort = Sort.by("point").ascending();
        }
        return sort;
    }


    public ResponseData findPlacePointByPlaceId(Integer pageNo, Integer pageSize,Integer type, String jwt, String search, Long cityId, Long countyId, Long townId, Long placeId ) {
        Integer sub = jwtUtil.getSubject(jwt);
        //当前用户
        GarbageUserEntity userEntity = garbageUserDao.findById(sub).get();
        Integer fromType = userEntity.getFromType(); // 0农村  1小区
        List<GarbageRoleEntity> roleEntityList = userEntity.getRoles().stream().collect(Collectors.toList());
        List<String> roles = roleEntityList.stream().filter(garbageRoleEntity-> !garbageRoleEntity.getRoleCode().contains("COMMUNITY")).map(garbageRoleEntity -> garbageRoleEntity.getRoleCode()).collect(Collectors.toList());
        List<Integer> communityIds = collectorService.getCommunityResource(roleEntityList);
        Constants.garbageFromType garbageFromType = null;
        switch (type){
            case 1:
                garbageFromType = Constants.garbageFromType.COMMUNITY;
                break;
            default:
                garbageFromType = Constants.garbageFromType.TOWN;
        }
        Pageable pageable= PageRequest.of((pageNo -1), pageSize, new Sort(Sort.Direction.DESC, "id"));
        Page<GarbageQualityPointEntity> page = garbageQualityPointDao.findAll(new Specification<GarbageQualityPointEntity>() {
            @Override
            public Predicate toPredicate(Root<GarbageQualityPointEntity> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList<>();
                if (!StringUtils.isEmpty(search)){
                    Predicate predicate = criteriaBuilder.like(root.get("placeName"), "%" + search + "%");
                    predicates.add(predicate);
                }
                if (fromType == 1){
                    //小区
                    Predicate fromPredicate = criteriaBuilder.equal(root.get("type"), fromType);
                    predicates.add(fromPredicate);
                    if (communityIds.size() > 0){
                        Predicate predicate =  root.get("placeId").in(communityIds);
                        predicates.add(predicate);
                    }
                }else{
                    Predicate fromPredicate = criteriaBuilder.equal(root.get("type"), fromType);
                    predicates.add(fromPredicate);
                    if (roles.contains("VILLAGE_ADMIN")){
                        Predicate predicateVillageId= criteriaBuilder.equal(root.get("placeId"), userEntity.getVillageId());
                        predicates.add(predicateVillageId);

                    }
                    if (roles.contains("TOWN_ADMIN")){

                        Predicate predicateTownId= criteriaBuilder.equal(root.get("townId"), userEntity.getTownId());
                        predicates.add(predicateTownId);

                        if (placeId !=null && placeId != 0 ){
                            Predicate predicateVillageId= criteriaBuilder.equal(root.get("villageId"), placeId);
                            predicates.add(predicateVillageId);
                        }
                    }
                    if (roles.contains("COUNTRY_ADMIN")){
                        Predicate predicateCountryId = criteriaBuilder.equal(root.get("countyId"), userEntity.getCountryId());
                        predicates.add(predicateCountryId);
                        if (townId !=null && townId != 0 ){
                            Predicate predicateTownId= criteriaBuilder.equal(root.get("townId"), townId);
                            predicates.add(predicateTownId);
                        }
                        if (placeId !=null && placeId != 0 ){
                            Predicate predicateVillageId= criteriaBuilder.equal(root.get("villageId"), placeId);
                            predicates.add(predicateVillageId);
                        }
                    }
                    if (roles.contains("CITY_ADMIN")){

                        Predicate predicateCityId = criteriaBuilder.equal(root.get("cityId"), userEntity.getCityId());
                        predicates.add(predicateCityId);

                        if (countyId !=null && countyId != 0){
                            Predicate predicateCountryId = criteriaBuilder.equal(root.get("countyId"), countyId);
                            predicates.add(predicateCountryId);
                        }
                        if (townId !=null && townId != 0 ){
                            Predicate predicateTownId= criteriaBuilder.equal(root.get("townId"), townId);
                            predicates.add(predicateTownId);
                        }
                        if (placeId !=null && placeId != 0 ){
                            Predicate predicateVillageId= criteriaBuilder.equal(root.get("villageId"), placeId);
                            predicates.add(predicateVillageId);
                        }
                    }
                    if (roles.contains("PROVINCE_ADMIN")){
                        Predicate predicateProvinceId = criteriaBuilder.equal(root.get("provinceId"), userEntity.getProvinceId());
                        predicates.add(predicateProvinceId);

                        if (cityId !=null && cityId != 0){
                            Predicate predicateCityId = criteriaBuilder.equal(root.get("cityId"), cityId);
                            predicates.add(predicateCityId);
                        }
                        if (countyId !=null && countyId != 0){
                            Predicate predicateCountryId = criteriaBuilder.equal(root.get("countyId"), countyId);
                            predicates.add(predicateCountryId);
                        }
                        if (townId !=null && townId != 0 ){
                            Predicate predicateTownId= criteriaBuilder.equal(root.get("townId"), townId);
                            predicates.add(predicateTownId);
                        }
                        if (placeId !=null && placeId != 0 ){
                            Predicate predicateVillageId= criteriaBuilder.equal(root.get("villageId"), placeId);
                            predicates.add(predicateVillageId);
                        }
                    }
                }
                return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        },pageable);
        List<Long> placeIds = page.getContent().stream().map(garbageQualityPointEntity -> garbageQualityPointEntity.getPlaceId()).collect(Collectors.toList());
        List<UserPositionDto> dtos = new ArrayList<>();
        Map<Long, GarbageQualityPointEntity> map = page.getContent().stream().collect(Collectors.toMap(GarbageQualityPointEntity::getPlaceId, Function.identity()));
        if (garbageFromType.getType() == 1){
            //小区
            dtos = garbageCommunityDao.selectCommunity(placeIds);
        } else {
            // 农村
            if (placeIds.size() > 0){
               dtos = jPositionVillageDao.selectPosition(placeIds);
            }

        }
        dtos.stream().forEach(dto->{
            GarbageQualityPointEntity qualityPointEntity = map.get(dto.getPlaceId());
            dto.setEmptyPoint(qualityPointEntity.getEmpty());
            dto.setNoQualitedPoint(qualityPointEntity.getNoQualified());
            dto.setQualitedPoint(qualityPointEntity.getQualified());
            dto.setFromType(qualityPointEntity.getType() == 1?"小区":"农村");
        });
        ResponsePageData responsePageData = new ResponsePageData();
        responsePageData.setPageNo(pageNo);
        responsePageData.setPageSize(pageSize);
        responsePageData.setCount(page.getTotalPages());
        responsePageData.setLastPage(page.isLast());
        responsePageData.setFirstPage(page.isFirst());
        responsePageData.setStatus(Constants.responseStatus.Success.getStatus());
        responsePageData.setMsg("查询成功");
        responsePageData.setData(dtos);
        return responsePageData;
    }

    public List<Object[]> exportPointList(String type, String keyWord, Long cityId, Long countryId, Long townId, Long villageId, Integer communityId, String jwt) {
        Integer sub  = jwtUtil.getSubject(jwt);
        GarbageUserEntity userEntity = garbageUserDao.findById(sub).get();
        Integer fromType = userEntity.getFromType(); // 0农村  1小区
        List<GarbageRoleEntity> roleEntityList = userEntity.getRoles().stream().collect(Collectors.toList());
        List<String> roleCodes = roleEntityList.stream().filter(garbageRoleEntity-> !garbageRoleEntity.getRoleCode().contains("COMMUNITY")).map(garbageRoleEntity -> garbageRoleEntity.getRoleCode()).collect(Collectors.toList());
        List<Integer> communityIds = collectorService.getCommunityResource(roleEntityList);

        StringBuilder builder = new StringBuilder(" SELECT p.user_name, p.phone, " +
                " concat_ws( p.province_name, p.city_name, p.country_name, p.town_name, p.village_name, p.community_name)," +
                " p.address, p.point " +
                " FROM garbage_user_point p INNER JOIN garbage_user u ON p.user_id = u.id WHERE 1=1 and u.from_type= " + fromType);
        if ( "name".equals(type)){
            builder.append(" and u.name like '%" + keyWord +"%'");
        }
        if ("phone".equals(type)){
            builder.append(" and u.phone like '%" + keyWord +"%'");
        }
        if("eNo".equals(type)){
            builder.append(" AND EXISTS ( "+
                    " SELECT  e.user_id FROM garbage_e_no e WHERE u.id = e.user_id AND e.e_no LIKE '%"+keyWord+"%' )");
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
            if (communityId != null){
                builder.append( " and u.community_id = " + communityId);
            } else {
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
        }
        builder.append(" order by p.point desc");
        List<Object[]> data = entityManager.createNativeQuery(builder.toString()).getResultList();
        return data;
    }


    public ResponseData getPointUserInfo(String name, String eNo, String jwt) {
        List<GarbageUserPointEntity> userEntities = garbageUserPointDao.findAll(new Specification<GarbageUserPointEntity>() {
            @Override
            public Predicate toPredicate(Root<GarbageUserPointEntity> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList<>();
                if (!StringUtils.isEmpty(name)){
                    Predicate predicate = criteriaBuilder.like(root.get("userName"), "%" + name + "%");
                    predicates.add(predicate);
                }
                if (!StringUtils.isEmpty(eNo)){
                    List<Predicate> predicateList = new ArrayList<>();
                    Subquery subquery = criteriaQuery.subquery(GarbagePointCardEntity.class);
                    Root subRoot = subquery.from(GarbagePointCardEntity.class);
                    subquery.select(subRoot.get("userId"));
                    Predicate predicate = criteriaBuilder.equal(root.get("userId"), subRoot.get("userId"));
                    predicateList.add(predicate);
                    Predicate eNoPredicate  = criteriaBuilder.equal(subRoot.get("pointCard"), eNo);
                    predicateList.add(eNoPredicate);
                    Predicate exists = criteriaBuilder.exists(subquery.where(predicateList.toArray(new Predicate[predicateList.size()])));
                    predicates.add(exists);
                }
                return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        });
        ResponseData responseData = new ResponseData();
        responseData.setMsg("用户信息查询查询成功");
        responseData.setStatus(Constants.responseStatus.Success.getStatus());
        responseData.setData(userEntities);
        return  responseData;
    }

    @Transactional
    public ResponseData buyGoodsByPoint(Integer pointFrom, Integer point, String desc, String eNo, String jwt) {
        Integer sub  = jwtUtil.getSubject(jwt);
        //店主
        GarbageUserEntity userEntity = garbageUserDao.findById(sub).get();
        Integer fromType = userEntity.getFromType(); // 0农村  1小区
        List<GarbageRoleEntity> roleEntityList = userEntity.getRoles().stream().collect(Collectors.toList());
        List<String> roles = roleEntityList.stream().filter(garbageRoleEntity-> !garbageRoleEntity.getRoleCode().contains("COMMUNITY")).map(garbageRoleEntity -> garbageRoleEntity.getRoleCode()).collect(Collectors.toList());
        List<Integer> communityIds = collectorService.getCommunityResource(roleEntityList);
        if (!roles.contains("SHOP")){
            throw new RuntimeException("没有权限调用商品购买接口");
        }
        //购买者
        GarbageUserEntity buyer = garbageUserDao.findByPhoneOrEno(eNo);
        if (buyer == null){
            throw new RuntimeException("查无此人，请核对手机号或者电子卡号");
        }
        synchronized (this){
            GarbageUserPointEntity buyerPoint = garbageUserPointDao.findByUserId(buyer.getId());
            if (buyerPoint == null){
                throw new RuntimeException("该用户的积分为0，不能完成此次购买");
            }
            if (buyerPoint.getPoint() == 0){
                throw new RuntimeException("该用户的积分为0，不能完成此次购买");
            }
            if (buyerPoint.getPoint() - point < 0){
                throw new RuntimeException("该用户的积分不够，不能完成此次购买");
            }
            buyerPoint.setPoint(buyerPoint.getPoint() - point);
            garbageUserPointDao.save(buyerPoint);
            //生成积分记录
            GarbagePointRecordEntity buyerRecord = new GarbagePointRecordEntity();
            buyerRecord.setUserId(buyer.getId());
            buyerRecord.setPoint(-point);
            buyerRecord.setDesc("兑换商品扣减积分：" + point + "; " + desc);
            buyerRecord.setSourceName(GarbageUserPointEntity.class.getName());
            buyerRecord.setBusId(userEntity.getId());
            garbagePointRecordDao.save(buyerRecord);
            // 发送短信
            try {
                SmsUtil.getSmsUtil().sendGarbageExchangeGoodsPoint(buyer.getPhone(), buyer.getTownName(), DateFormatUtil.formatDate(new Date(), "yyyy-MM-dd"), point, buyerPoint.getPoint());
            } catch (ClientException e) {
                e.printStackTrace();
            }

            //店主
            GarbageUserPointEntity userPointEntity = garbageUserPointDao.findByUserId(userEntity.getId());
            if (userPointEntity != null){
                userPointEntity.setPoint(userPointEntity.getPoint() + point);
                garbageUserPointDao.save(buyerPoint);
            } else {
                userPointEntity = new GarbageUserPointEntity();
                userPointEntity.setUserId(userEntity.getId());
                userPointEntity.setUserName(userEntity.getName());
                userPointEntity.setPoint(point);
                userPointEntity.setProvinceName(userEntity.getProvinceName());
                userPointEntity.setCityName(userEntity.getCityName());
                userPointEntity.setCountryName(userEntity.getCountryName());
                userPointEntity.setTownName(userEntity.getTownName());
                userPointEntity.setVillageName(userEntity.getVillageName());
                userPointEntity.setAddress(userEntity.getAddress());
                userPointEntity.setPhone(userEntity.getPhone());
                userPointEntity.setProvinceId(userEntity.getProvinceId());
                userPointEntity.setCityId(userEntity.getCityId());
                userPointEntity.setCountryId(userEntity.getCountryId());
                userPointEntity.setTownId(userEntity.getTownId());
                userPointEntity.setVillageId(userEntity.getVillageId());
                userPointEntity.setCommunityId(userEntity.getCommunityId());
                userPointEntity.setCommunityName(userEntity.getCommunityName());
            }
            garbageUserPointDao.save(userPointEntity);
            //生成积分记录
            GarbagePointRecordEntity shopRecord = new GarbagePointRecordEntity();
            shopRecord.setUserId(userEntity.getId());
            shopRecord.setPoint(point);
            shopRecord.setDesc("兑换商品增加积分：" + point + "; " + desc);
            shopRecord.setSourceName(GarbageUserPointEntity.class.getName());
            shopRecord.setBusId(buyer.getId());
            garbagePointRecordDao.save(shopRecord);
        }
        ResponseData responseData = new ResponseData();
        responseData.setMsg("积分兑换成功");
        responseData.setStatus(Constants.responseStatus.Success.getStatus());
        return  responseData;
    }
}
