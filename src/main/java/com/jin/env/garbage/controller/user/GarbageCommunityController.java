package com.jin.env.garbage.controller.user;

import com.jin.env.garbage.service.user.GarbageCommunityService;
import com.jin.env.garbage.utils.ResponseData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

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
    public ResponseData getCommunityList(Integer pageNo, Integer pageSize, String search, HttpServletRequest request){
        ResponseData responseData = garbageCommunityService.getCommunityList(pageNo, pageSize, search);
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
        Assert.state(roleId != null, "角色id 不能为空");
        Assert.state(communityIds.length > 0, "请选择小区资源");
        ResponseData responseData = garbageCommunityService.addCommunityToRole(roleId, communityIds);
        return responseData;
    }

    @RequestMapping(value = "updateCommunityStatus", method = RequestMethod.POST)
    public ResponseData updateCommunityStatus(Integer communityId, Integer status){
        Assert.state(communityId != null, "小区id 不能为空");
        Assert.state(status != null , "请输入小区数据状态");
        ResponseData responseData = garbageCommunityService.updateCommunityStatus(communityId, status);
        return responseData;
    }

    @RequestMapping(value = "updateCommunity", method = RequestMethod.POST)
    public ResponseData updateCommunity(Integer communityId, String communityName, String address, String desc, Integer status){
        Assert.state(communityId != null, "小区id 不能为空");
        ResponseData responseData = garbageCommunityService.updateCommunity(communityId, communityName, address, desc, status);
        return responseData;
    }
}
