package com.jin.env.garbage.controller.user;

import com.jin.env.garbage.service.user.GarbageCommunityService;
import com.jin.env.garbage.utils.ResponseData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "api/v1/community/")
public class GarbageCommunityController {

    @Autowired
    private GarbageCommunityService garbageCommunityService;

    /**
     * 获取小区资源
     * @return
     */
    @RequestMapping(value = "getCommunityList", method = RequestMethod.GET)
    public ResponseData getCommunityList(){
        ResponseData responseData = garbageCommunityService.getCommunityList();
        return responseData;
    }

    /**
     * 向角色添加小区资源
     * @param roleId
     * @param communityIds
     * @return
     */
    @RequestMapping(value = "addCommunityToRole", method = RequestMethod.POST)
    public ResponseData addCommunityToRole(Integer roleId, Integer[] communityIds){
        ResponseData responseData = garbageCommunityService.addCommunityToRole(roleId, communityIds);
        return responseData;
    }
}
