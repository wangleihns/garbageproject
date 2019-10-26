package com.jin.env.garbage.service.position;

import com.jin.env.garbage.dao.position.*;
import com.jin.env.garbage.dao.user.GarbageUserDao;
import com.jin.env.garbage.dao.village.GarbageVillageInfoDao;
import com.jin.env.garbage.entity.position.*;
import com.jin.env.garbage.entity.user.GarbageUserEntity;
import com.jin.env.garbage.entity.village.GarbageVillageInfoEntity;
import com.jin.env.garbage.jwt.JwtUtil;
import com.jin.env.garbage.utils.Constants;
import com.jin.env.garbage.utils.HttpClientUtil;
import com.jin.env.garbage.utils.ResponseData;
import com.jin.env.garbage.utils.ResponsePageData;
import net.sf.json.JSONObject;
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
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class GarbageVillageInfoService {
    private Logger logger = LoggerFactory.getLogger(GarbageVillageInfoService.class);
    @Value("${ak}")
    private String ak;
    @Autowired
    private GarbageVillageInfoDao garbageVillageInfoDao;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private GarbageUserDao garbageUserDao;

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
    public ResponseData addGarbageVillageInfo(Integer provinceId, Long cityId, Long countyId, Long townId, Long villageId, String provinceName, String cityName, String countyName, String townName, String villageName, String showTitle) {

        JPositionProvinceEntity jPositionProvinceEntity = jPositionProvinceDao.findByProvinceId(provinceId);
        JPositionCityEntity jPositionCityEntity = jPositionCityDao.findByCityId(cityId);
        JPositionCountyEntity jPositionCountyEntity = jPositionCountyDao.findByCountyId(countyId);
        JPositionTownEntity jPositionTownEntity = jPositionTownDao.findByTownId(townId);
        JPositionVillageEntity jPositionVillageEntity = jPositionVillageDao.findByVillageId(villageId);
        provinceName = jPositionProvinceEntity.getProvinceName();
        cityName = jPositionCityEntity.getCityName();
        countyName = jPositionCountyEntity.getCountyName();
        townName = jPositionTownEntity.getTownName();
        villageName = jPositionVillageEntity.getVillageName();
        String address = provinceName + cityName + countyName + townName + villageName;
        GarbageVillageInfoEntity villageInfoEntity = garbageVillageInfoDao.findByVillageId(villageId);
        if (villageInfoEntity != null ){
            throw new RuntimeException(villageName + " 已经添加到系统了， 不能重复添加");
        }
        String baseUrl = "http://api.map.baidu.com/geocoding/v3/";
        Map<String, Object> map = new HashMap<>();
        map.put("address", address.replaceAll(" ", ""));
        map.put("output", "json");
        map.put("ak", ak);
        String s = null;
        ResponseData responseData = new ResponseData();
        try {
            s = HttpClientUtil.sendGet(baseUrl, map);
            //            String s = "{\"status\":0,\"result\":{\"location\":{\"lng\":115.62896733687058,\"lat\":38.557185021498749},\"precise\":0,\"confidence\":18,\"comprehension\":66,\"level\":\"地产小区\"}}\n";
            JSONObject jsonObject = JSONObject.fromObject(s);
            if (jsonObject.getInt("status") == 0) {
                JSONObject location = jsonObject.getJSONObject("result").getJSONObject("location");
                Double lng = location.getDouble("lng");
                Double lat = location.getDouble("lat");
                GarbageVillageInfoEntity garbageVillageInfoEntity = new GarbageVillageInfoEntity();
                garbageVillageInfoEntity.setProvinceId(provinceId);
                garbageVillageInfoEntity.setProvinceName(provinceName);
                garbageVillageInfoEntity.setCityId(cityId);
                garbageVillageInfoEntity.setCityName(cityName);
                garbageVillageInfoEntity.setCountyId(countyId);
                garbageVillageInfoEntity.setCountyName(countyName);
                garbageVillageInfoEntity.setTownId(townId);
                garbageVillageInfoEntity.setTownName(townName);
                garbageVillageInfoEntity.setVillageId(villageId);
                garbageVillageInfoEntity.setVillageName(villageName);
                garbageVillageInfoEntity.setShowTitle(showTitle);
                garbageVillageInfoEntity.setLongitude(lng);
                garbageVillageInfoEntity.setLatitude(lat);
                garbageVillageInfoEntity.setDetailAddress(address);
                garbageVillageInfoDao.save(garbageVillageInfoEntity);
                responseData.setStatus(Constants.responseStatus.Success.getStatus());
                responseData.setMsg("添加成功");
            }else {
                logger.info(s);
                throw new RuntimeException(jsonObject.getString("msg"));
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("获取经纬度坐标出现错误");
        }
        return responseData;
    }

    public ResponseData garbageVillageInfoList(Integer pageNo, Integer pageSize, Long cityId, Long countryId, Long townId, Long villageId, String searchName, String jwt) {
        Pageable pageable = PageRequest.of(pageNo - 1, pageSize, new Sort(Sort.Direction.DESC,"id"));
        Integer sub = jwtUtil.getSubject(jwt);
        GarbageUserEntity userEntity = garbageUserDao.findById(sub).get();
        List<String> roleCodes = userEntity.getRoles().stream().map(n->n.getRoleCode()).collect(Collectors.toList());
        Page<GarbageVillageInfoEntity> page = garbageVillageInfoDao.findAll(new Specification<GarbageVillageInfoEntity>() {
            @Override
            public Predicate toPredicate(Root<GarbageVillageInfoEntity> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
               List<Predicate> predicates = new ArrayList<>();
               if (!StringUtils.isEmpty(searchName)){
                   predicates.add(criteriaBuilder.like(root.get("villageName"), "%" + searchName + "%"));
               }
               if (roleCodes.contains("VILLAGE_ADMIN")  ){
                    Predicate predicate = criteriaBuilder.equal(root.get("villageId"), userEntity.getVillageId());
                    predicates.add(predicate);
               } else if (roleCodes.contains("TOWN_ADMIN")){
                    Predicate predicate = criteriaBuilder.equal(root.get("townId"), userEntity.getTownId());
                    predicates.add(predicate);
                    if (villageId != null){
                        Predicate predicate1 = criteriaBuilder.equal(root.get("villageId"), villageId);
                        predicates.add(predicate1);
                    }
               } else if (roleCodes.contains("COUNTRY_ADMIN")){
                    Predicate predicate2 = criteriaBuilder.equal(root.get("countyId"), userEntity.getCountryId());
                    predicates.add(predicate2);

                    if (countryId !=null){
                        Predicate predicate = criteriaBuilder.equal(root.get("townId"), townId);
                        predicates.add(predicate);
                    }
                    if (villageId != null){
                        Predicate predicate1 = criteriaBuilder.equal(root.get("villageId"), villageId);
                        predicates.add(predicate1);
                    }

               } else if (roleCodes.contains("CITY_ADMIN")){
                    Predicate predicate = criteriaBuilder.equal(root.get("cityId"), userEntity.getCityId());
                    predicates.add(predicate);
                    if (countryId != null ) {
                        Predicate predicate2 = criteriaBuilder.equal(root.get("countyId"), cityId);
                        predicates.add(predicate2);
                    }
                    if (countryId !=null){
                        Predicate predicate3 = criteriaBuilder.equal(root.get("townId"), townId);
                        predicates.add(predicate3);
                    }
                    if (villageId != null){
                        Predicate predicate1 = criteriaBuilder.equal(root.get("villageId"), villageId);
                        predicates.add(predicate1);
                    }
               } else {
                    Predicate predicate = criteriaBuilder.equal(root.get("provinceId"), userEntity.getProvinceId());
                    predicates.add(predicate);
                    if (cityId !=null){
                        Predicate predicate4 = criteriaBuilder.equal(root.get("cityId"), cityId);
                        predicates.add(predicate4);
                    }
                    if (countryId != null ) {
                        Predicate predicate2 = criteriaBuilder.equal(root.get("countyId"), cityId);
                        predicates.add(predicate2);
                    }
                    if (countryId !=null){
                        Predicate predicate3 = criteriaBuilder.equal(root.get("townId"), townId);
                        predicates.add(predicate3);
                    }
                    if (villageId != null){
                        Predicate predicate1 = criteriaBuilder.equal(root.get("villageId"), villageId);
                        predicates.add(predicate1);
                    }
                }
                return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        }, pageable);
        ResponsePageData responsePageData = new ResponsePageData();
        responsePageData.setPageNo(pageNo);
        responsePageData.setPageSize(pageSize);
        responsePageData.setCount(page.getTotalPages());
        responsePageData.setLastPage(page.isLast());
        responsePageData.setFirstPage(page.isFirst());
        responsePageData.setTotalElement(page.getTotalElements());
        responsePageData.setData(page.getContent());
        responsePageData.setStatus(Constants.responseStatus.Success.getStatus());
        responsePageData.setMsg("列表查询成功");
        return responsePageData;
    }
}
