package com.jin.env.garbage.service.user;

import com.jin.env.garbage.dao.position.GarbageCommunityDao;
import com.jin.env.garbage.dao.user.GarbageRoleCommunityDao;
import com.jin.env.garbage.entity.position.GarbageCommunityEntity;
import com.jin.env.garbage.entity.user.GarbageRoleCommunityEntity;
import com.jin.env.garbage.utils.Constants;
import com.jin.env.garbage.utils.ResponseData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class GarbageCommunityService {
    @Autowired
    private GarbageCommunityDao garbageCommunityDao;

    @Autowired
    private GarbageRoleCommunityDao garbageRoleCommunityDao;

    public ResponseData getCommunityList() {
        ResponseData responseData = new ResponseData();
        List<GarbageCommunityEntity> garbageCommunityEntityList = garbageCommunityDao.findAll();
        responseData.setData(garbageCommunityEntityList);
        responseData.setStatus(Constants.responseStatus.Success.getStatus());
        responseData.setMsg("获取小区资源成功");
        return  responseData;
    }

    @Transactional
    public ResponseData addCommunityToRole(Integer roleId, Integer[] communityIds) {
        ResponseData responseData = new ResponseData();
        garbageRoleCommunityDao.deleteAllByRoleId(roleId);
        List<GarbageRoleCommunityEntity> roleCommunityList = new ArrayList<>();
        Arrays.stream(communityIds).forEach(communityId ->{
            GarbageRoleCommunityEntity entity = new GarbageRoleCommunityEntity();
            entity.setRoleId(roleId);
            entity.setCommunityId(communityId);
            roleCommunityList.add(entity);
        });
        garbageRoleCommunityDao.saveAll(roleCommunityList);
        responseData.setStatus(Constants.responseStatus.Success.getStatus());
        responseData.setMsg("添加成功");
        return responseData;
    }
}
