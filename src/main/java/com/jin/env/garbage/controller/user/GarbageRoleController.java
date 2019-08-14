package com.jin.env.garbage.controller.user;

import com.jin.env.garbage.service.user.GarbageRoleService;
import com.jin.env.garbage.utils.ResponseData;
import com.jin.env.garbage.utils.ResponsePageData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "api/v1/role/")
public class GarbageRoleController {
    @Autowired
    private GarbageRoleService garbageRoleService;

    @RequestMapping(value = "roleList", method = RequestMethod.GET)
    public ResponsePageData roleList(int pageSize, int pageNo,String search, String  ...orderBys){
        ResponsePageData responsePageData =garbageRoleService.roleList(pageNo, pageSize,search, orderBys);
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

    public ResponseData addRoleForCommunity(String roleCode, String roleName, Boolean isAdmin){
        ResponseData responseData =garbageRoleService.addRoleForCommunity(roleCode, roleName, isAdmin);
        return responseData;
    }
}
