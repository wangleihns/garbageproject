package com.jin.env.garbage.controller.user;

import com.jin.env.garbage.entity.user.GarbageResourceEntity;
import com.jin.env.garbage.service.user.GarbageRoleService;
import com.jin.env.garbage.utils.ResponseData;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.criteria.Predicate;
import javax.servlet.http.HttpServletRequest;
import java.util.HashSet;
import java.util.Set;

@RestController
@RequestMapping(value = "/api/v1/resource/")
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
    public ResponseData addResourceToRole(Integer roleId, Integer[] resourceIds, HttpServletRequest request){
        String jwt = request.getHeader("Authorization").split(" ")[1];
        Assert.state(roleId !=null, "请选择角色");
        Assert.state(resourceIds.length > 0, "请选择资源");
        ResponseData responseData = garbageRoleService.addResourceToRole(roleId, resourceIds, jwt);
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


    /**
     * Integer roleId, Integer[] resourceIds, Integer[] communityIds, Integer type
     * @return
     */

    @RequestMapping(value = "addResourceAndCommunity", method = RequestMethod.POST)
    public ResponseData addResourceAndCommunity(String value){
        JSONObject jsonObject = JSONObject.fromObject(value);
        Integer roleId = jsonObject.getInt("roleId");
        JSONArray resourceIds = jsonObject.getJSONArray("resourceIds");
        JSONArray communityIds = jsonObject.getJSONArray("communityIds");
        Integer type = jsonObject.getInt("type");
        Assert.state(roleId !=null, "请选择角色");
        if (type == 1){
            Assert.state(resourceIds.size() > 0, "请选择资源");
            Assert.state(communityIds.size() > 0, "请选择小区资源");
        } else {
            Assert.state(resourceIds.size() > 0, "请选择资源");
        }

        Set<Integer> resourceIdSet = new HashSet<>();
        resourceIds.stream().forEach(id-> resourceIdSet.add((Integer) id));
        Set<Integer> communityIdSet = new HashSet<>();
        communityIds.stream().forEach(id-> communityIdSet.add((Integer) id));

        ResponseData responseData = garbageRoleService.addResourceAndCommunity(roleId, resourceIdSet, communityIdSet, type);
        return responseData;
    }

    /**
     * 获取资源列表
     * @param type
     * @param roleId
     * @return
     */
    @RequestMapping(value = "findResourceAndCommunity", method = RequestMethod.GET)
    public ResponseData findResourceAndCommunity(Integer type, Integer roleId){
        ResponseData responseData = garbageRoleService.findResourceAndCommunity(type, roleId);
        return responseData;
    }

    @RequestMapping(value = "getCheckResourceAndCommunity", method = RequestMethod.GET)
    public ResponseData getCheckResourceAndCommunity(Integer type){
        ResponseData responseData = garbageRoleService.getCheckResourceAndCommunity(type);
        return responseData;
    }

    /**
     *  左侧tarbarList 列表
     * @param request
     * @return
     */
    @RequestMapping(value = "getTabBarList", method = RequestMethod.GET)
    public ResponseData getTabBarList(HttpServletRequest request){
        String jwt = request.getHeader("Authorization").split(" ")[1];
        ResponseData responseData = garbageRoleService.getTabBarList(jwt);
        return responseData;
    }


}
