package com.jin.env.garbage.service.garbage;

import com.jin.env.garbage.dao.garbage.GarbageQualityPointDao;
import com.jin.env.garbage.dao.user.GarbageUserDao;
import com.jin.env.garbage.entity.garbage.GarbageQualityPointEntity;
import com.jin.env.garbage.entity.point.GarbageUserPointEntity;
import com.jin.env.garbage.entity.user.GarbageENoEntity;
import com.jin.env.garbage.entity.user.GarbageRoleEntity;
import com.jin.env.garbage.entity.user.GarbageUserEntity;
import com.jin.env.garbage.jwt.JwtUtil;
import com.jin.env.garbage.utils.Constants;
import com.jin.env.garbage.utils.ResponseData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GarbageQualityPointService {
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private GarbageUserDao garbageUserDao;

    @Autowired
    private GarbageQualityPointDao garbageQualityPointDao;

    public ResponseData findGarbageQualityPoint(String jwt) {
        Integer sub = jwtUtil.getSubject(jwt);
        GarbageUserEntity userEntity = garbageUserDao.findById(sub).get();
        Long placeId = null;
        if (userEntity.getFromType() == 1){
            placeId = userEntity.getCommunityId();
        } else {
            placeId = userEntity.getVillageId();
        }
        GarbageQualityPointEntity qualityPointEntity = garbageQualityPointDao.findByPlaceIdAndType(placeId, userEntity.getFromType());
        ResponseData responseData = new ResponseData();
        responseData.setMsg("垃圾类型积分查询成功");
        responseData.setData(qualityPointEntity);
        responseData.setStatus(Constants.responseStatus.Success.getStatus());
        return responseData;
    }

    public ResponseData setGarbageQualityPoint(Integer id, Integer quality, Integer noQuality, Integer empty, String jwt) {
        ResponseData responseData = new ResponseData();
        Integer sub = jwtUtil.getSubject(jwt);
        GarbageUserEntity userEntity = garbageUserDao.findById(sub).get();
        Integer fromType = userEntity.getFromType();
        List<GarbageRoleEntity> roleEntities = userEntity.getRoles().stream().collect(Collectors.toList());
        List<String> roleCodes = roleEntities.stream().map(garbageRoleEntity -> garbageRoleEntity.getRoleCode()).collect(Collectors.toList());
        Long placeId = null;
        String placeName = "";
        if (roleCodes.contains("VILLAGE_ADMIN") || roleCodes.stream().filter(n -> n.endsWith("COMMUNITY_ADMIN")).collect(Collectors.toList()).size()>0){
            if (fromType == 1){
                placeId = userEntity.getCommunityId();
                placeName = userEntity.getCommunityName();
            } else {
                placeId = userEntity.getVillageId();
                placeName = userEntity.getVillageName();
            }
            if (id == null){
                GarbageQualityPointEntity pointEntity = new GarbageQualityPointEntity();
                pointEntity.setType(fromType);
                pointEntity.setPlaceId(placeId);
                pointEntity.setNoQualified(noQuality);
                pointEntity.setQualified(quality);
                pointEntity.setEmpty(empty);
                pointEntity.setProvinceId(userEntity.getProvinceId());
                pointEntity.setCityId(userEntity.getCityId());
                pointEntity.setCountyId(userEntity.getCountryId());
                pointEntity.setTownId(userEntity.getTownId());
                pointEntity.setPlaceName(placeName);
                garbageQualityPointDao.save(pointEntity);
                responseData.setMsg("垃圾类型积分设置成功");
                responseData.setData(pointEntity);
                responseData.setStatus(Constants.responseStatus.Success.getStatus());
            } else {
                GarbageQualityPointEntity pointEntity = garbageQualityPointDao.findById(id).get();
                pointEntity.setNoQualified(noQuality);
                pointEntity.setQualified(quality);
                pointEntity.setEmpty(empty);
                garbageQualityPointDao.save(pointEntity);
                responseData.setMsg("垃圾类型积分修改成功");
                responseData.setData(pointEntity);
                responseData.setStatus(Constants.responseStatus.Success.getStatus());
            }
        } else {
            throw  new RuntimeException("没有权限修改地区垃圾分类积分");
        }
        return responseData;
    }
}
