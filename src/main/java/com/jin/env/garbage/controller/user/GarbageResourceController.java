package com.jin.env.garbage.controller.user;

import com.jin.env.garbage.entity.user.GarbageResourceEntity;
import com.jin.env.garbage.service.user.GarbageRoleService;
import com.jin.env.garbage.utils.ResponseData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.criteria.Predicate;

@RestController
@RequestMapping(value = "/api/v1/resource")
public class GarbageResourceController {
    @Autowired
    private GarbageRoleService garbageRoleService;

    /**
     * 给角色赋予资源
     * @param roleId
     * @param resourceIds
     * @return
     */
    @RequestMapping(value = "addResourceToRole", method = RequestMethod.POST)
    public ResponseData addResourceToRole(Integer roleId, Integer[] resourceIds){
        Assert.state(roleId !=null, "请选择角色");
        Assert.state(resourceIds.length > 0, "请选择资源");
        ResponseData responseData = garbageRoleService.addResourceToRole(roleId, resourceIds);
        return responseData;
    }

    /**
     * 添加主资源
     * @param icon
     * @param name
     * @param path
     * @return
     */
    @RequestMapping(value = "addResourceTile", method = RequestMethod.POST)
    public ResponseData addResourceTile(String icon, String name, String path){
        Assert.hasText("name", "导航名称");
        ResponseData responseData = garbageRoleService.addResourceTile(icon, name, path);
        return responseData;
    }

    /**
     * 添加子资源
     * @param parentId
     * @param icon
     * @param name
     * @param path
     * @param seq
     * @return
     */
    @RequestMapping(value = "addSubResourceTile", method = RequestMethod.POST)
    public ResponseData addSubResourceTile(int parentId, String icon, String name, String path, int seq){
        Assert.hasText(name, "导航名称");
        Assert.hasText(path, "路径不能为空");
        ResponseData responseData = garbageRoleService.addSubResourceTile(parentId, icon, name, path ,seq);
        return responseData;
    }

    /**
     * 资源树
     * @return
     */
    @RequestMapping(value = "resourceList", method = RequestMethod.GET)
    public ResponseData resourceList(){
        ResponseData responseData = garbageRoleService.resourceList();
        return responseData;
    }


    /**
     * 获取小区资源
     * @param placeId
     * @return
     */
    @RequestMapping(value = "getCommunityList", method = RequestMethod.GET)
    public ResponseData getCommunityList(Integer placeId){
        Assert.state(placeId !=null, "请选择地区Id");
        ResponseData responseData = garbageRoleService.getCommunityList(placeId);
        return responseData;
    }

    /**
     * 添加小区资源
     * @param placeId
     * @param communityName
     * @return
     */
    @RequestMapping(value = "addCommunity", method = RequestMethod.GET)
    public ResponseData addCommunity(Integer placeId, String communityName){
        Assert.state(placeId !=null, "请选择地区Id");
        Assert.hasText(communityName, "导航名称");
        ResponseData responseData = garbageRoleService.addCommunity(placeId, communityName);
        return responseData;
    }
}
