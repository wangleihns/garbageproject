package com.jin.env.garbage.controller.user;

import com.jin.env.garbage.service.user.GarbageRoleService;
import com.jin.env.garbage.utils.ResponseData;
import com.jin.env.garbage.utils.ResponsePageData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping(value = "api/v1/role/")
public class GarbageRoleController {
    @Autowired
    private GarbageRoleService garbageRoleService;

    /**
     * 角色管理
     * @param pageSize
     * @param pageNo
     * @param search
     * @param orderBys
     * @return
     */
    @RequestMapping(value = "roleList", method = RequestMethod.GET)
    public ResponseData roleList(Integer pageSize, Integer pageNo,String search, String  ...orderBys){
        Assert.state(pageNo != null, "pageNo 不能为空");
        Assert.state(pageSize != null, "pageSize 不能为空");
        ResponseData responsePageData =garbageRoleService.roleList(pageNo, pageSize,search, orderBys);
        return responsePageData;
    }

    /**
     * 禁用 或启用角色
     * @param roleId
     * @param status
     * @return
     */
    @RequestMapping(value = "updateRoleStatus", method = RequestMethod.POST)
    public ResponseData updateRoleStatus(Integer roleId, Integer status){
        ResponseData responseData =garbageRoleService.updateRoleStatus(roleId, status);
        return responseData;
    }

    /**
     * 添加小区角色
     * @param roleName
     * @param roleDesc
     * @param isAdmin
     * @return
     */
    @RequestMapping(value = "addRoleForCommunity", method = RequestMethod.POST)
    public ResponseData addRoleForCommunity(String roleName,String roleDesc,  Boolean isAdmin){
        ResponseData responseData =garbageRoleService.addRoleForCommunity(roleName,roleDesc, isAdmin);
        return responseData;
    }

    /**
     *  获取用户拥有的角色
     *  未完善 ======
     * @param request
     * @return
     */
    @RequestMapping(value = "roleListForUserManage", method = RequestMethod.GET)
    public ResponseData roleListForUserManage(HttpServletRequest request, Integer userId){
        Assert.state(userId != null, "用户id不能为空");
        ResponseData responsePageData =garbageRoleService.roleListForUserManage(userId);
        return responsePageData;
    }
}
