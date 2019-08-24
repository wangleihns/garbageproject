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
     * 添加资源
     * @param icon
     * @param label
     * @param path
     * @return
     */
    @RequestMapping(value = "addSubResourceTile", method = RequestMethod.POST)
    public ResponseData addSubResourceTile(String icon, String label, String path, Integer seq){
        Assert.hasText(label, "导航名称");
        ResponseData responseData = garbageRoleService.addResourceTile(icon, label, path, seq);
        return responseData;
    }

    /**
     * 添加资源
     * @param supId
     * @param icon
     * @param label
     * @param path
     * @param seq
     * @return
     */
    @RequestMapping(value = "addResourceTile", method = RequestMethod.POST)
    public ResponseData addResourceTile(Integer supId, String icon, String label, String path, Integer seq, Integer active, Integer id){
        Assert.hasText(label, "导航名称不能为空");
        Assert.hasText(path, "路径不能为空");
        ResponseData responseData = garbageRoleService.addSubResourceTile(supId, icon, label, path ,seq, active, id);
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



}
