package com.jin.env.garbage.service.point;

import com.jin.env.garbage.dao.point.GarbageUserPointDao;
import com.jin.env.garbage.dao.user.GarbageUserDao;
import com.jin.env.garbage.dto.point.RedAndBlackRankDto;
import com.jin.env.garbage.entity.point.GarbageUserPointEntity;
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
                redRankDtos.add(dto);
            }
        });

        List<RedAndBlackRankDto> blackRankDtos = new ArrayList<>();
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
}
