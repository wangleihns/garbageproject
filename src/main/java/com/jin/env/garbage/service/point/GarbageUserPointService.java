package com.jin.env.garbage.service.point;

import com.jin.env.garbage.dao.garbage.GarbageQualityPointDao;
import com.jin.env.garbage.dao.point.GarbageUserPointDao;
import com.jin.env.garbage.dao.position.GarbageCommunityDao;
import com.jin.env.garbage.dao.position.JPositionVillageDao;
import com.jin.env.garbage.dao.user.GarbageUserDao;
import com.jin.env.garbage.dto.point.RedAndBlackRankDto;
import com.jin.env.garbage.dto.position.UserPositionDto;
import com.jin.env.garbage.entity.garbage.GarbageQualityPointEntity;
import com.jin.env.garbage.entity.point.GarbageUserPointEntity;
import com.jin.env.garbage.entity.position.GarbageCommunityEntity;
import com.jin.env.garbage.entity.position.JPositionVillageEntity;
import com.jin.env.garbage.entity.user.GarbageRoleEntity;
import com.jin.env.garbage.entity.user.GarbageUserEntity;
import com.jin.env.garbage.jwt.JwtUtil;
import com.jin.env.garbage.service.garbage.GarbageCollectorService;
import com.jin.env.garbage.utils.Constants;
import com.jin.env.garbage.utils.ResponseData;
import com.jin.env.garbage.utils.ResponsePageData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

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

    public ResponseData getPointRankList(Integer pageNo, Integer pageSize, String name, String phone, String jwt, Integer cityId, Integer countryId, Integer townId, Integer villageId, String[] orderBys) {
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
                if (!StringUtils.isEmpty(name)){
                    Predicate predicate = criteriaBuilder.like(root.get("userName"), "%" + name + "%");
                    predicates.add(predicate);
                }
                if (!StringUtils.isEmpty(phone)){
                    Predicate predicate = criteriaBuilder.like(root.get("userName"), "%" + name + "%");
                    predicates.add(predicate);
                }
                Subquery subquery = criteriaQuery.subquery(GarbageUserEntity.class);
                Root subRoot = subquery.from(GarbageUserEntity.class);
                subquery.select(subRoot.get("id"));
                Predicate equal = criteriaBuilder.equal(root.get("userId"), subRoot.get("id"));
                List<Predicate> selectList = new ArrayList<>();
                if (fromType == 1){
                    if (communityIds.size()> 0){
                        Predicate predicate = subRoot.get("communityId").in(communityIds);
                        selectList.add(predicate);
                    }
                }else {
                    if (roleCodes.contains("VILLAGE_ADMIN")){

                        Predicate predicateVillageId= criteriaBuilder.equal(subRoot.get("villageId"), userEntity.getVillageId());
                        selectList.add(predicateVillageId);

                    }
                    if (roleCodes.contains("TOWN_ADMIN")){

                        Predicate predicateTownId= criteriaBuilder.equal(subRoot.get("townId"), userEntity.getTownId());
                        selectList.add(predicateTownId);

                        if (villageId !=null && villageId != 0 ){
                            Predicate predicateVillageId= criteriaBuilder.equal(subRoot.get("villageId"), villageId);
                            selectList.add(predicateVillageId);
                        }
                    }
                    if (roleCodes.contains("COUNTRY_ADMIN")){

                        Predicate predicateCountryId = criteriaBuilder.equal(subRoot.get("countryId"), userEntity.getCountryId());
                        selectList.add(predicateCountryId);

                        if (townId !=null && townId != 0 ){
                            Predicate predicateTownId= criteriaBuilder.equal(subRoot.get("townId"), townId);
                            selectList.add(predicateTownId);
                        }
                        if (townId !=null && townId != 0 ){
                            Predicate predicateTownId= criteriaBuilder.equal(subRoot.get("townId"), townId);
                            selectList.add(predicateTownId);
                        }
                        if (townId !=null && townId != 0 ){
                            Predicate predicateVillageId= criteriaBuilder.equal(subRoot.get("villageId"), villageId);
                            selectList.add(predicateVillageId);
                        }
                    }
                    if (roleCodes.contains("CITY_ADMIN")){

                        Predicate predicateCityId = criteriaBuilder.equal(subRoot.get("cityId"), userEntity.getCityId());
                        selectList.add(predicateCityId);

                        if (countryId !=null && countryId != 0){
                            Predicate predicateCountryId = criteriaBuilder.equal(subRoot.get("countryId"), countryId);
                            selectList.add(predicateCountryId);
                        }
                        if (townId !=null && townId != 0 ){
                            Predicate predicateTownId= criteriaBuilder.equal(subRoot.get("townId"), townId);
                            selectList.add(predicateTownId);
                        }
                        if (townId !=null && townId != 0 ){
                            Predicate predicateTownId= criteriaBuilder.equal(subRoot.get("townId"), townId);
                            selectList.add(predicateTownId);
                        }
                        if (townId !=null && townId != 0 ){
                            Predicate predicateVillageId= criteriaBuilder.equal(subRoot.get("villageId"), villageId);
                            selectList.add(predicateVillageId);
                        }
                    }
                    if (roleCodes.contains("PROVINCE_ADMIN")){

                        Predicate predicateProvinceId = criteriaBuilder.equal(subRoot.get("provinceId"), userEntity.getProvinceId());
                        selectList.add(predicateProvinceId);

                        if (cityId !=null && cityId != 0){
                            Predicate predicateCityId = criteriaBuilder.equal(subRoot.get("cityId"), cityId);
                            selectList.add(predicateCityId);
                        }
                        if (countryId !=null && countryId != 0){
                            Predicate predicateCountryId = criteriaBuilder.equal(subRoot.get("countryId"), countryId);
                            selectList.add(predicateCountryId);
                        }
                        if (townId !=null && townId != 0 ){
                            Predicate predicateTownId= criteriaBuilder.equal(subRoot.get("townId"), townId);
                            selectList.add(predicateTownId);
                        }
                        if (townId !=null && townId != 0 ){
                            Predicate predicateTownId= criteriaBuilder.equal(subRoot.get("townId"), townId);
                            selectList.add(predicateTownId);
                        }
                        if (townId !=null && townId != 0 ){
                            Predicate predicateVillageId= criteriaBuilder.equal(subRoot.get("villageId"), villageId);
                            selectList.add(predicateVillageId);
                        }
                    }
                }
                Predicate predicateExits = criteriaBuilder.exists(subquery.where(selectList.toArray(new Predicate[selectList.size()])));
                predicates.add(predicateExits);
                return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        },pageable);
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
                if (communityIds.size() > 0){
                    Predicate predicate = root.get("communityId").in(communityIds);
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
                return criteriaBuilder.and(predicateList.toArray(new Predicate[predicateList.size()]));
            }
        }, pageableFirst);

        userPointLastPage = garbageUserPointDao.findAll(new Specification<GarbageUserPointEntity>() {
            @Override
            public Predicate toPredicate(Root<GarbageUserPointEntity> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicateList = new ArrayList<>();
                if (communityIds.size() > 0){
                    Predicate predicate = root.get("communityId").in(communityIds);
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
                return criteriaBuilder.and(predicateList.toArray(new Predicate[predicateList.size()]));
            }
        }, pageableLast);

        List<RedAndBlackRankDto> redRankDtos = new ArrayList<>();
        userPointFirstPage.getContent().forEach(garbageUserPointEntity -> {
            RedAndBlackRankDto dto = new RedAndBlackRankDto();
            if (fromType == 1){
                dto.setPlaceName(garbageUserPointEntity.getCommunityName());
            } else {
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
                if (roleCodes.contains("PROVINCE_ADMIN")){
                    dto.setPlaceName(garbageUserPointEntity.getProvinceName());
                }
                dto.setPoint(garbageUserPointEntity.getPoint());
                dto.setUserName(garbageUserPointEntity.getUserName());
                redRankDtos.add(dto);
            }
        });

        List<RedAndBlackRankDto> blackRankDtos = new ArrayList<>();
        userPointLastPage.getContent().forEach(garbageUserPointEntity -> {
            RedAndBlackRankDto dto = new RedAndBlackRankDto();
            if (fromType == 1){
                dto.setPlaceName(garbageUserPointEntity.getCommunityName());
            } else {
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
                if (roleCodes.contains("PROVINCE_ADMIN")){
                    dto.setPlaceName(garbageUserPointEntity.getProvinceName());
                }
                dto.setPoint(garbageUserPointEntity.getPoint());
                dto.setUserName(garbageUserPointEntity.getUserName());
                blackRankDtos.add(dto);
            }

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
//            dtos = garbageCommunityDao.selectCommunity(placeIds);
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
}
