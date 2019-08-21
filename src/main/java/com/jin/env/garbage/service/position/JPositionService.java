package com.jin.env.garbage.service.position;

import com.jin.env.garbage.dao.position.*;
import com.jin.env.garbage.entity.position.*;
import com.jin.env.garbage.utils.Constants;
import com.jin.env.garbage.utils.HttpClientUtil;
import com.jin.env.garbage.utils.ResponseData;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class JPositionService {
   private  Logger logger = LoggerFactory.getLogger(JPositionService.class);
   @Value("${ak}")
    private String ak;
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
    private GarbageCommunityDao garbageCommunityDao;

   public ResponseData<JPositionProvinceEntity> getProvince(){
       ResponseData responseData = new ResponseData();
       List<JPositionProvinceEntity> provinceEntityList = null;
       try {
           provinceEntityList = jPositionProvinceDao.findAll();
           responseData.setData(provinceEntityList);
           responseData.setStatus(Constants.responseStatus.Success.getStatus());
           responseData.setMsg("查询成功");
       } catch (Exception e) {
           responseData.setStatus(Constants.responseStatus.Failure.getStatus());
           responseData.setMsg("查询失败");
           e.printStackTrace();
       }
       return responseData;
   }

    public ResponseData getCityListByProvinceId(Integer provinceId) {
        ResponseData responseData = new ResponseData();
        List<JPositionCityEntity> jPositionCityEntityList = null;
        try {
            jPositionCityEntityList = jPositionCityDao.findAll(new Specification<JPositionCityEntity>() {
                @Override
                public Predicate toPredicate(Root<JPositionCityEntity> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                    return criteriaBuilder.equal(root.get("provinceId"), provinceId);
                }
            });
            responseData.setData(jPositionCityEntityList);
            responseData.setStatus(Constants.responseStatus.Success.getStatus());
            responseData.setMsg("查询成功");
        } catch (Exception e) {
            responseData.setStatus(Constants.responseStatus.Failure.getStatus());
            responseData.setMsg("查询失败");
            e.printStackTrace();
        }
        return responseData;
    }

    public ResponseData getCountyListByCityId(Integer cityId) {
        ResponseData responseData = new ResponseData();
        List<JPositionCountyEntity> jPositionCountyEntityList = null;
        try {
            jPositionCountyEntityList = jPositionCountyDao.findAll(new Specification<JPositionCountyEntity>() {
                @Override
                public Predicate toPredicate(Root<JPositionCountyEntity> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                    return criteriaBuilder.equal(root.get("provinceId"), cityId);
                }
            });
            responseData.setData(jPositionCountyEntityList);
            responseData.setStatus(Constants.responseStatus.Success.getStatus());
            responseData.setMsg("查询成功");
        } catch (Exception e) {
            responseData.setStatus(Constants.responseStatus.Failure.getStatus());
            responseData.setMsg("查询失败");
            e.printStackTrace();
        }
        return responseData;

    }

    public ResponseData getTownListByCountyId(Integer countyId) {
        ResponseData responseData = new ResponseData();
        List<JPositionTownEntity> jPositionTownEntityList = null;
        try {
            jPositionTownEntityList = jPositionTownDao.findAll(new Specification<JPositionTownEntity>() {
                @Override
                public Predicate toPredicate(Root<JPositionTownEntity> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                    return criteriaBuilder.equal(root.get("countyId"), countyId);
                }
            });
            responseData.setData(jPositionTownEntityList);
            responseData.setStatus(Constants.responseStatus.Success.getStatus());
            responseData.setMsg("查询成功");
        } catch (Exception e) {
            responseData.setStatus(Constants.responseStatus.Failure.getStatus());
            responseData.setMsg("查询失败");
            e.printStackTrace();
        }
        return responseData;
    }

    public ResponseData getVillageListByTownId(Integer townId) {
        ResponseData responseData = new ResponseData();
        List<JPositionVillageEntity> jPositionVillageEntityList = null;
        try {
            jPositionVillageEntityList = jPositionVillageDao.findAll(new Specification<JPositionVillageEntity>() {
                @Override
                public Predicate toPredicate(Root<JPositionVillageEntity> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                    return criteriaBuilder.equal(root.get("townId"), townId);
                }
            });
            responseData.setData(jPositionVillageEntityList);
            responseData.setStatus(Constants.responseStatus.Success.getStatus());
            responseData.setMsg("查询成功");
        } catch (Exception e) {
            responseData.setStatus(Constants.responseStatus.Failure.getStatus());
            responseData.setMsg("查询失败");
            e.printStackTrace();
        }
        return responseData;
    }

    @Transactional
    public ResponseData addVillage(Integer townId, String villageName) {
        ResponseData responseData = new ResponseData();
       List<JPositionVillageEntity> jPositionVillageEntityList = jPositionVillageDao.findAll(new Specification<JPositionVillageEntity>() {
           @Override
           public Predicate toPredicate(Root<JPositionVillageEntity> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
               Predicate townId1 = criteriaBuilder.equal(root.get("townId"), townId);
               criteriaQuery.where(townId1).orderBy(criteriaBuilder.desc(root.get("id")));
               return null;
           }
       });
        try {
           if (jPositionVillageEntityList.size() > 0){
               JPositionVillageEntity villageEntity = jPositionVillageEntityList.get(0);
               Long villageId = villageEntity.getVillageId() + 1;
               JPositionVillageEntity v = new JPositionVillageEntity();
               v.setTownId(townId);
               v.setVillageId(villageId);
               v.setVillageName(villageName);
               jPositionVillageDao.save(v);
           } else {
               JPositionVillageEntity v = new JPositionVillageEntity();
               v.setTownId(townId);
               v.setVillageId(townId + 1);
               v.setVillageName(villageName);
               jPositionVillageDao.save(v);
           }
            responseData.setStatus(Constants.responseStatus.Success.getStatus());
            responseData.setMsg("添加成功");
        } catch (Exception e) {
            e.printStackTrace();
            responseData.setStatus(Constants.responseStatus.Failure.getStatus());
            responseData.setMsg("添加失败");
        }
        return  responseData;
    }

    @Transactional
    public ResponseData addCommunity(Integer provinceId, Integer cityId, Integer countryId, String address, String communityName) {
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
                GarbageCommunityEntity communityEntity = new GarbageCommunityEntity();
                communityEntity.setProvinceId(provinceId);
                communityEntity.setCityId(cityId);
                communityEntity.setCountryId(countryId);
                communityEntity.setCommunityName(communityName);
                communityEntity.setAddress(address);
                communityEntity.setLat(lat);
                communityEntity.setLon(lng);
                communityEntity.setStatus(Constants.dataType.ENABLE.getType());
                garbageCommunityDao.save(communityEntity);
                responseData.setStatus(Constants.responseStatus.Success.getStatus());
                responseData.setMsg("添加成功");
            } else {
                logger.info(s);
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("获取小区经纬度坐标出现错误");
        }
         return null;
    }
}
