package com.jin.env.garbage.service.user;

import com.jin.env.garbage.dao.user.GarbageResourceDao;
import com.jin.env.garbage.dao.user.GarbageRoleDao;
import com.jin.env.garbage.dao.user.GarbageRoleResourceDao;
import com.jin.env.garbage.dto.resource.ResourceListDto;
import com.jin.env.garbage.entity.user.GarbageResourceEntity;
import com.jin.env.garbage.entity.user.GarbageRoleEntity;
import com.jin.env.garbage.entity.user.GarbageRoleResourceEntity;
import com.jin.env.garbage.utils.Constants;
import com.jin.env.garbage.utils.PinYinUtil;
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

import javax.persistence.criteria.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class GarbageRoleService {

    @Autowired
    private GarbageRoleDao garbageRoleDao;
    @Autowired
    private GarbageResourceDao garbageResourceDao;

    @Autowired
    private GarbageRoleResourceDao garbageRoleResourceDao;

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
            sort =   Sort.by(Arrays.stream(orderBys).map((it) -> {
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
        //主标题
        List<GarbageResourceEntity> resourceEntityList = garbageResourceDao.findAll(new Specification<GarbageResourceEntity>() {
            @Override
            public Predicate toPredicate(Root<GarbageResourceEntity> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                return criteriaBuilder.equal(root.get("supId"), 0);
            }
        });
        List<Sort.Order> orders = new ArrayList<>();
        orders.add(Sort.Order.asc("seq"));
        //子标题
        List<GarbageResourceEntity> childResourceList = garbageResourceDao.findAll(new Specification<GarbageResourceEntity>() {
            @Override
            public Predicate toPredicate(Root<GarbageResourceEntity> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                return criteriaBuilder.notEqual(root.get("supId"), 0);
            }
        }, Sort.by(orders));
        Map<Integer, List<GarbageResourceEntity>> childResourceMap = new HashMap<>();
        childResourceList.forEach(resourceEntity -> {
            if (childResourceMap.containsKey(resourceEntity.getSupId())){
                List<GarbageResourceEntity> list = childResourceMap.get(resourceEntity.getSupId());
                list.add(resourceEntity);
                childResourceMap.put(resourceEntity.getSupId(), list);
            }else {
                List<GarbageResourceEntity> list = new ArrayList<>();
                list.add(resourceEntity);
                childResourceMap.put(resourceEntity.getSupId(), list);
            }
    });
        List<ResourceListDto> dtos = new ArrayList<>();
        resourceEntityList.forEach(resourceEntity -> {
            ResourceListDto dto = new ResourceListDto();
            dto.setId(resourceEntity.getId());
            dto.setCode(resourceEntity.getCode());
            dto.setIcon(resourceEntity.getIcon());
            dto.setName(resourceEntity.getName());
            dto.setPath(resourceEntity.getPath());
            dto.setNoDropdown(false);
            dto.setEnabled(true);
            dto.setChildren(childResourceMap.get(resourceEntity.getId()));
            dtos.add(dto);
        });
        responseData.setData(dtos);
        responseData.setStatus(Constants.responseStatus.Success.getStatus());
        responseData.setMsg("资源列表查询成功");
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


    @Transactional
    public ResponseData addResourceToRole(Integer roleId, Integer[] resourceIds) {
        ResponseData responseData = new ResponseData();
        garbageRoleResourceDao.deleteAllByRoleId(roleId);
        List<GarbageRoleResourceEntity> roleResourceEntityList = new ArrayList<>();
        Arrays.stream(resourceIds).forEach(resourceId ->{
            GarbageRoleResourceEntity roleResourceEntity = new GarbageRoleResourceEntity();
            roleResourceEntity.setResourceId(resourceId);
            roleResourceEntity.setRoleId(roleId);
            roleResourceEntityList.add(roleResourceEntity);
        });
        try {
            garbageRoleResourceDao.saveAll(roleResourceEntityList);
            responseData.setStatus(Constants.responseStatus.Success.getStatus());
            responseData.setMsg("添加成功");
        } catch (Exception e) {
            e.printStackTrace();
            responseData.setStatus(Constants.responseStatus.Failure.getStatus());
            responseData.setMsg("添加失败");
        }
        return responseData;
    }

    @Transactional
    public ResponseData addResourceTile(String icon, String name, String path) {
        ResponseData responseData = new ResponseData();
        GarbageResourceEntity resourceEntity = new GarbageResourceEntity();
        resourceEntity.setName(name);
        resourceEntity.setIcon(icon);
        resourceEntity.setPath(path);
        resourceEntity.setSeq(1);
        resourceEntity.setSupId(0);
        try {
            garbageResourceDao.save(resourceEntity);
            responseData.setStatus(Constants.responseStatus.Success.getStatus());
            responseData.setMsg("主资源添加成功");
        } catch (Exception e) {
            e.printStackTrace();
            responseData.setStatus(Constants.responseStatus.Failure.getStatus());
            responseData.setMsg("添加失败");
        }
        return responseData;
    }

    @Transactional
    public ResponseData addSubResourceTile(int parentId, String icon, String name, String path, int seq) {
        ResponseData responseData = new ResponseData();
        GarbageResourceEntity resourceEntity = new GarbageResourceEntity();
        resourceEntity.setName(name);
        resourceEntity.setIcon(icon);
        resourceEntity.setPath(path);
        resourceEntity.setSeq(seq);
        resourceEntity.setSupId(parentId);
        try {
            garbageResourceDao.save(resourceEntity);
            responseData.setStatus(Constants.responseStatus.Success.getStatus());
            responseData.setMsg("子资源添加成功");
        } catch (Exception e) {
            e.printStackTrace();
            responseData.setStatus(Constants.responseStatus.Failure.getStatus());
            responseData.setMsg("子资源添加失败");
        }
        return responseData;
    }

    public ResponseData getCommunityList(Integer placeId) {
        ResponseData responseData = new ResponseData();
        List<GarbageResourceEntity> communityList = garbageResourceDao.findBySupIdAndFtType(placeId, "community");
        responseData.setData(communityList);
        responseData.setStatus(Constants.responseStatus.Success.getStatus());
        responseData.setMsg("获取小区资源成功");
        return  responseData;
    }

    @Transactional
    public ResponseData addCommunity(Integer placeId, String communityName) {
        ResponseData responseData = new ResponseData();
        GarbageResourceEntity resourceEntity = new GarbageResourceEntity();
        resourceEntity.setName(communityName);
        resourceEntity.setSeq(1);
        resourceEntity.setSupId(placeId);
        try {
            garbageResourceDao.save(resourceEntity);
            responseData.setStatus(Constants.responseStatus.Success.getStatus());
            responseData.setMsg("添加小区资源成功");
        } catch (Exception e) {
            e.printStackTrace();
            responseData.setStatus(Constants.responseStatus.Failure.getStatus());
            responseData.setMsg("添加失败");
        }
        return responseData;
    }

    @Transactional
    public ResponseData addRoleForCommunity(String roleName, String roleDesc, Boolean isAdmin) {
        GarbageRoleEntity roleEntity = new GarbageRoleEntity();
        ResponseData responseData = new ResponseData();
        roleEntity.setRoleName(roleName);
        if (isAdmin) {
            roleEntity.setRoleCode(PinYinUtil.converterToSpell(roleName) +"COMMUNITY_ADMIN");
        } else {
            roleEntity.setRoleDesc(PinYinUtil.converterToSpell(roleName) +"COMMUNITY_REMARK");
        }
        roleEntity.setRoleDesc(roleDesc);
        roleEntity.setStatus(Constants.dataType.ENABLE.getType());
        roleEntity.setType(Constants.garbageFromType.COMMUNITY.getType());
        garbageRoleDao.save(roleEntity);
        responseData.setStatus(Constants.responseStatus.Success.getStatus());
        responseData.setMsg("添加小区角色名称成功");
        return responseData;
    }
}
