package com.jin.env.garbage.service.user;

import com.jin.env.garbage.dao.user.GarbageResourceDao;
import com.jin.env.garbage.dao.user.GarbageRoleDao;
import com.jin.env.garbage.entity.user.GarbageResourceEntity;
import com.jin.env.garbage.entity.user.GarbageRoleEntity;
import com.jin.env.garbage.utils.Constants;
import com.jin.env.garbage.utils.ResponseData;
import com.jin.env.garbage.utils.ResponsePageData;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GarbageRoleService {

    @Autowired
    private GarbageRoleDao garbageRoleDao;
    @Autowired
    private GarbageResourceDao garbageResourceDao;

    public ResponsePageData roleList(int pageNo, int pageSize, String search, String[] orderBys) {
        Sort sort = getSort(orderBys);
        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);
        ResponsePageData responsePageData = new ResponsePageData();
        try {
            Page<GarbageRoleEntity> roleEntityPage = garbageRoleDao.findAll(new Specification<GarbageRoleEntity>() {
                @Nullable
                @Override
                public Predicate toPredicate(Root<GarbageRoleEntity> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                    if (!StringUtils.isBlank(search)){
                        criteriaQuery.where(criteriaBuilder.like(root.get("roleName"), "%"+search + "%"));
                    }
                    return null;
                }
            },pageable);
            responsePageData = new ResponsePageData();
            responsePageData.setCount(roleEntityPage.getTotalPages());
            responsePageData.setFirstPage(roleEntityPage.isFirst());
            responsePageData.setLastPage(roleEntityPage.isLast());
            responsePageData.setData(roleEntityPage.getContent());
            responsePageData.setPageNo(pageNo);
            responsePageData.setPageSize(pageSize);
            responsePageData.setStatus(Constants.responseStatus.Success.getStatus());
            responsePageData.setMsg("角色列表查询成功");
        } catch (Exception e) {
            e.printStackTrace();
            responsePageData.setStatus(Constants.responseStatus.Failure.getStatus());
            responsePageData.setMsg("角色列表查询失败");
        }
        return responsePageData;
    }

    private Sort getSort(String[] orderBys){
        Sort sort = null;
        if (orderBys == null || orderBys.length == 0 ){
            sort = Sort.by("id");
        }else {
            sort =   Sort.by((List) Arrays.stream(orderBys).map((it) -> {
                String[] items = it.split(";");
                String property = "";
                Sort.Direction direction = null;
                if (items.length > 1){
                    property = items[0];
                    switch (property){
                        case "rolecode":
                            property = "roleCode";
                            break;
                        case "rolename":
                            property = "roleName";
                            break;
                        default:
                            property = "id";
                            break;

                    }
                    if ("desc".equalsIgnoreCase(items[1])){
                        direction = Sort.Direction.DESC;
                    } else {
                        direction = Sort.Direction.ASC;
                    }
                } else {
                    direction = Sort.Direction.ASC;
                }
                return new Sort.Order(direction, property);
            }).collect(Collectors.toList()));
        }
        return sort;
    }

    public ResponseData resourceList() {
        ResponseData responseData = new ResponseData();
        List<GarbageResourceEntity> resourceEntityList = garbageResourceDao.findAll();
        responseData.setStatus(Constants.responseStatus.Success.getStatus());
        responseData.setMsg("资源列表查询成功");
        responseData.setData(resourceEntityList);
        return  responseData;
    }


    @Transactional
    public ResponseData updateRoleStatus(Integer roleId, Integer status) {
        ResponseData responseData = new ResponseData();
        GarbageRoleEntity roleEntity = garbageRoleDao.findById(roleId).get();
        roleEntity.setStatus(status);
        garbageRoleDao.save(roleEntity);
        responseData.setStatus(Constants.responseStatus.Success.getStatus());
        responseData.setMsg("角色状态更改成功");
        return responseData;
    }
}
