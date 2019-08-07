package com.jin.env.garbage.controller.user;

import com.jin.env.garbage.dao.user.GarbageRoleDao;
import com.jin.env.garbage.entity.user.GarbageRoleEntity;
import com.jin.env.garbage.service.user.GarbageRoleService;
import com.jin.env.garbage.utils.ResponseData;
import com.jin.env.garbage.utils.ResponsePageData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
}
