package com.jin.env.garbage.service.user;

import com.jin.env.garbage.dao.position.GarbageCommunityDao;
import com.jin.env.garbage.dao.user.*;
import com.jin.env.garbage.dto.resource.*;
import com.jin.env.garbage.entity.position.GarbageCommunityEntity;
import com.jin.env.garbage.entity.user.*;
import com.jin.env.garbage.jwt.JwtUtil;
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
@SuppressWarnings("ALL")
public class GarbageRoleService {

    @Autowired
    private GarbageRoleDao garbageRoleDao;
    @Autowired
    private GarbageResourceDao garbageResourceDao;

    @Autowired
    private GarbageRoleResourceDao garbageRoleResourceDao;

    @Autowired
    private GarbageRoleCommunityDao garbageRoleCommunityDao;

    @Autowired
    private GarbageCommunityDao garbageCommunityDao;

    @Autowired
    private GarbageUserDao garbageUserDao;

    @Autowired
    private JwtUtil jwtUtil;

    public ResponsePageData roleList(int pageNo, int pageSize, String search, String[] orderBys) {
        Sort sort = getSort(orderBys);
        Pageable pageable = PageRequest.of(pageNo-1, pageSize, sort);
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
            responsePageData.setTotalElement(roleEntityPage.getTotalElements());
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
        Map<Integer, List<ResourceChildrenList>> childResourceMap = new HashMap<>();
        childResourceList.forEach(resourceEntity -> {
            if (childResourceMap.containsKey(resourceEntity.getSupId())){
                List<ResourceChildrenList> list = childResourceMap.get(resourceEntity.getSupId());
                ResourceChildrenList resourceChildrenList = new ResourceChildrenList();
                resourceChildrenList.setId(resourceEntity.getId());
                resourceChildrenList.setLabel(resourceEntity.getName());
                resourceChildrenList.setSeq(resourceEntity.getSeq());
                resourceChildrenList.setSupId(resourceEntity.getSupId());
                resourceChildrenList.setPath(resourceEntity.getPath());
                resourceChildrenList.setIcon(resourceEntity.getIcon());
                resourceChildrenList.setActive(resourceEntity.getActive());
                list.add(resourceChildrenList);
                childResourceMap.put(resourceEntity.getSupId(), list);
            }else {
                List<ResourceChildrenList> list = new ArrayList<>();
                ResourceChildrenList resourceChildrenList = new ResourceChildrenList();
                resourceChildrenList.setId(resourceEntity.getId());
                resourceChildrenList.setLabel(resourceEntity.getName());
                resourceChildrenList.setSeq(resourceEntity.getSeq());
                resourceChildrenList.setSupId(resourceEntity.getSupId());
                resourceChildrenList.setPath(resourceEntity.getPath());
                resourceChildrenList.setIcon(resourceEntity.getIcon());
                resourceChildrenList.setActive(resourceEntity.getActive());
                list.add(resourceChildrenList);
                childResourceMap.put(resourceEntity.getSupId(), list);
            }
    });
        List<ResourceListLabelDto> dtos = new ArrayList<>();
        resourceEntityList.forEach(resourceEntity -> {
            ResourceListLabelDto resourceListLabelDto = new ResourceListLabelDto();
            resourceListLabelDto.setId(resourceEntity.getId());
            resourceListLabelDto.setIcon(resourceEntity.getIcon());
            resourceListLabelDto.setLabel(resourceEntity.getName());
            resourceListLabelDto.setSeq(resourceEntity.getSeq());
            resourceListLabelDto.setSupId(resourceEntity.getSupId());
            resourceListLabelDto.setActive(resourceEntity.getActive());
            resourceListLabelDto.setPath(resourceEntity.getPath());
            resourceListLabelDto.setChildren(childResourceMap.get(resourceEntity.getId()));
            dtos.add(resourceListLabelDto);
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
    public ResponseData addResourceToRole(Integer roleId, Integer[] resourceIds, String jwt) {
        Integer sub = jwtUtil.getSubject(jwt);
        GarbageUserEntity authorUser = garbageUserDao.findById(sub).get();
        List<String> roleCodes = authorUser.getRoles().stream().map(u ->u.getRoleCode()).collect(Collectors.toList());
        if (!roleCodes.contains("SYSTEM_ADMIN")){
            throw new RuntimeException("没有权限进行此授权操作");
        }
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
    public ResponseData addResourceTile(String icon, String name, String path, Integer seq) {
        ResponseData responseData = new ResponseData();
        GarbageResourceEntity resourceEntity = new GarbageResourceEntity();
        resourceEntity.setName(name);
        resourceEntity.setIcon(icon);
        resourceEntity.setPath(path);
        resourceEntity.setSeq(seq);
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
    public ResponseData addSubResourceTile(Integer parentId, String icon, String name, String path, int seq, Integer active, Integer id) {
        ResponseData responseData = new ResponseData();
        GarbageResourceEntity resourceEntity = new GarbageResourceEntity();
        resourceEntity.setName(name);
        resourceEntity.setIcon(icon);
        resourceEntity.setPath(path);
        resourceEntity.setSeq(seq);
        resourceEntity.setSupId(parentId);
        resourceEntity.setActive(active);
        String msg = "资源添加成功";
        if (id != null){
            resourceEntity.setId(id);
            msg = "资源修改成功";
        }
        try {
            garbageResourceDao.save(resourceEntity);
            responseData.setStatus(Constants.responseStatus.Success.getStatus());
            responseData.setMsg(msg);
        } catch (Exception e) {
            e.printStackTrace();
            responseData.setStatus(Constants.responseStatus.Failure.getStatus());
            responseData.setMsg("资源添加或修改失败");
        }
        return responseData;
    }


    @Transactional
    public ResponseData addRoleForCommunity(String roleName, String roleDesc, Boolean isAdmin) {
        //小区管理员
        GarbageRoleEntity roleEntityManager = new GarbageRoleEntity();
        GarbageRoleEntity roleEntityRemark = new GarbageRoleEntity();
        String roleCodeManager = PinYinUtil.converterToSpell(roleName) +"_COMMUNITY_ADMIN";
        String roleNameManager = roleName + "小区管理员";
        String roleNameRemark = roleName + "小区评分员";
        String roleCodeRemark = PinYinUtil.converterToSpell(roleName) +"_COMMUNITY_REMARK";
        ResponseData responseData = new ResponseData();
//        String roleCode = "";
//        if (isAdmin) {
//            roleCode = PinYinUtil.converterToSpell(roleName) +"_COMMUNITY_ADMIN";
//            roleEntity.setRoleCode(roleCode);
//        } else {
//            roleCode = PinYinUtil.converterToSpell(roleName) +"_COMMUNITY_REMARK";
//            roleEntity.setRoleCode(PinYinUtil.converterToSpell(roleName) +"_COMMUNITY_REMARK");
//        }
        List<GarbageRoleEntity> roleEntityList = garbageRoleDao.findByRoleCodeIn(new ArrayList<String>(){{add(roleCodeManager); add(roleCodeRemark);}});
        if (roleEntityList.size() > 0){
            throw  new RuntimeException("角色名称重复， 角色编码重复");
        }
        roleEntityManager.setRoleCode(roleCodeManager);
        roleEntityManager.setRoleDesc(roleDesc);
        roleEntityManager.setStatus(Constants.dataType.ENABLE.getType());
        roleEntityManager.setType(Constants.garbageFromType.COMMUNITY.getType());

        roleEntityRemark.setRoleCode(roleCodeManager);
        roleEntityRemark.setRoleDesc(roleDesc);
        roleEntityRemark.setStatus(Constants.dataType.ENABLE.getType());
        roleEntityRemark.setType(Constants.garbageFromType.COMMUNITY.getType());
        List<GarbageRoleEntity> roles = new ArrayList<>();
        roles.add(roleEntityManager);
        roles.add(roleEntityRemark);
        garbageRoleDao.saveAll(roles);
        responseData.setStatus(Constants.responseStatus.Success.getStatus());
        responseData.setMsg("添加小区角色名称成功");
        return responseData;
    }

    @Transactional
    public ResponseData addResourceAndCommunity(Integer roleId, Set<Integer> resourceIdSet, Set<Integer> communityIdSet, Integer type) {
        ResponseData responseData = new ResponseData();
        List<GarbageRoleResourceEntity> roleResourceEntityList = new ArrayList<>();
        List<GarbageResourceEntity> childresList = garbageResourceDao.findByIdIn(resourceIdSet);
        Set<Integer> parentIds = childresList.stream().map(garbageResourceEntity -> garbageResourceEntity.getSupId()).collect(Collectors.toSet());
        resourceIdSet.addAll(parentIds);
        resourceIdSet.stream().forEach(resourceId ->{
            GarbageRoleResourceEntity roleResourceEntity = new GarbageRoleResourceEntity();
            roleResourceEntity.setResourceId(resourceId);
            roleResourceEntity.setRoleId(roleId);
            roleResourceEntityList.add(roleResourceEntity);
        });
        List<GarbageRoleCommunityEntity> roleCommunityList = new ArrayList<>();
        communityIdSet.stream().forEach(communityId ->{
            GarbageRoleCommunityEntity entity = new GarbageRoleCommunityEntity();
            entity.setRoleId(roleId);
            entity.setCommunityId(communityId);
            roleCommunityList.add(entity);
        });
        try {
            garbageRoleResourceDao.deleteAllByRoleId(roleId);
            garbageRoleCommunityDao.deleteAllByRoleId(roleId);
            garbageRoleResourceDao.saveAll(roleResourceEntityList);
            garbageRoleCommunityDao.saveAll(roleCommunityList);
            responseData.setStatus(Constants.responseStatus.Success.getStatus());
            responseData.setMsg("添加成功");
        } catch (Exception e) {
            e.printStackTrace();
            responseData.setStatus(Constants.responseStatus.Failure.getStatus());
            responseData.setMsg("添加失败");
        }
        return responseData;
    }

    public ResponseData findResourceAndCommunity(Integer type, Integer roleId ) {
        List<GarbageRoleResourceEntity> roleResourceEntities = garbageRoleResourceDao.findByRoleId(roleId);
        List<Integer> resourceIds = roleResourceEntities.stream().map(garbageRoleResourceEntity -> garbageRoleResourceEntity.getResourceId()).collect(Collectors.toList());

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
        Map<Integer, List<ResourceChildrenList>> childResourceMap = new HashMap<>();
        childResourceList.forEach(resourceEntity -> {
            if (childResourceMap.containsKey(resourceEntity.getSupId())){
                List<ResourceChildrenList> list = childResourceMap.get(resourceEntity.getSupId());
                ResourceChildrenList resourceChildrenList = new ResourceChildrenList();
                resourceChildrenList.setId(resourceEntity.getId());
                resourceChildrenList.setLabel(resourceEntity.getName());
                resourceChildrenList.setSeq(resourceEntity.getSeq());
                resourceChildrenList.setSupId(resourceEntity.getSupId());
                resourceChildrenList.setPath(resourceEntity.getPath());
                resourceChildrenList.setIcon(resourceEntity.getIcon());
                resourceChildrenList.setActive(resourceEntity.getActive());
                if (resourceIds.contains(resourceEntity.getId())){
                    resourceChildrenList.setCheck(true);
                } else {
                    resourceChildrenList.setCheck(false);
                }
                list.add(resourceChildrenList);
                childResourceMap.put(resourceEntity.getSupId(), list);
            }else {
                List<ResourceChildrenList> list = new ArrayList<>();
                ResourceChildrenList resourceChildrenList = new ResourceChildrenList();
                resourceChildrenList.setId(resourceEntity.getId());
                resourceChildrenList.setLabel(resourceEntity.getName());
                resourceChildrenList.setSeq(resourceEntity.getSeq());
                resourceChildrenList.setSupId(resourceEntity.getSupId());
                resourceChildrenList.setPath(resourceEntity.getPath());
                resourceChildrenList.setIcon(resourceEntity.getIcon());
                resourceChildrenList.setActive(resourceEntity.getActive());
                if (resourceIds.contains(resourceEntity.getId())){
                    resourceChildrenList.setCheck(true);
                } else {
                    resourceChildrenList.setCheck(false);
                }
                list.add(resourceChildrenList);
                childResourceMap.put(resourceEntity.getSupId(), list);
            }
        });
        List<ResourceListLabelDto> dtos = new ArrayList<>();
        resourceEntityList.forEach(resourceEntity -> {
            ResourceListLabelDto resourceListLabelDto = new ResourceListLabelDto();
            resourceListLabelDto.setId(resourceEntity.getId());
            resourceListLabelDto.setIcon(resourceEntity.getIcon());
            resourceListLabelDto.setLabel(resourceEntity.getName());
            resourceListLabelDto.setSeq(resourceEntity.getSeq());
            resourceListLabelDto.setSupId(resourceEntity.getSupId());
            resourceListLabelDto.setActive(resourceEntity.getActive());
            resourceListLabelDto.setPath(resourceEntity.getPath());
            resourceListLabelDto.setChildren(childResourceMap.get(resourceEntity.getId()));
            if (resourceIds.contains(resourceEntity.getId())){
                resourceListLabelDto.setCheck(true);
            } else {
                resourceListLabelDto.setCheck(false);
            }
            dtos.add(resourceListLabelDto);
        });
       List<GarbageCommunityEntity> communityEntities = garbageCommunityDao.findAll();
       List<GarbageRoleCommunityEntity> roleCommunityEntityList = garbageRoleCommunityDao.findByRoleId(roleId);
       List<Integer> communityIds = roleCommunityEntityList.stream().map(garbageRoleCommunityEntity -> garbageRoleCommunityEntity.getCommunityId()).collect(Collectors.toList());
       List<CommunityResourceDto> communityResourceDtos = new ArrayList<>();
        communityEntities.stream().forEach(garbageCommunityEntity -> {
            CommunityResourceDto dto = new CommunityResourceDto();
            dto.setCommunityName(garbageCommunityEntity.getCommunityName());
            dto.setId(garbageCommunityEntity.getId());
            if (communityIds.contains(garbageCommunityEntity.getId())){
                dto.setCheck(true);
            } else {
                dto.setCheck(false);
            }
            communityResourceDtos.add(dto);
        });
       ResponseData response = new ResponseData();
       Map<String, Object> map = new HashMap<>();
       if (type == 1){
            map.put("resourceList", dtos);
            map.put("communityList", communityResourceDtos);
       } else {
           map.put("resourceList", dtos);
           map.put("communityList", communityResourceDtos);
       }
       response.setMsg("资源列表查询成功");
       response.setData(map);
       response.setStatus(Constants.responseStatus.Success.getStatus());
       return response;
    }

    public ResponseData getCheckResourceAndCommunity(Integer type) {
        return  null;
    }

    public ResponseData roleListForUserManage(Integer userId) {
        List<GarbageRoleEntity> roleEntities = garbageRoleDao.findAll(new Specification<GarbageRoleEntity>() {
            @Override
            public Predicate toPredicate(Root<GarbageRoleEntity> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                return criteriaBuilder.equal(root.get("status"), 1);
            }
        });

        ResponseData response = new ResponseData();
        response.setMsg("资源列表查询成功");
        response.setData(roleEntities);
        response.setStatus(Constants.responseStatus.Success.getStatus());
        return response;
    }

    public ResponseData getTabBarList(String jwt) {
        Integer sub = jwtUtil.getSubject(jwt);
        List<Integer> supList = garbageRoleResourceDao.findResourceSupByUserId(sub);
        List<GarbageResourceEntity> resourceEntityList = garbageResourceDao.findByIdIn(supList);
        List<ResourceListDto> resourceListDtos = new ArrayList<>();
        List<Integer> subList = garbageRoleResourceDao.findResourceSubByUserId(sub);
        List<GarbageResourceEntity> garbageResourceEntityList = garbageResourceDao.findByIdIn(subList);;
        Map<Integer, List<ResourceListChildrenDto>> subResourceMap = new HashMap<>();
        garbageResourceEntityList.stream().forEach(res ->{
            if (subResourceMap.containsKey(res.getSupId())){
                List<ResourceListChildrenDto> childrenDtos = subResourceMap.get(res.getSupId());
                ResourceListChildrenDto resourceListChildrenDto = new ResourceListChildrenDto();
                resourceListChildrenDto.setId(res.getId().toString());
                resourceListChildrenDto.setParentId(res.getSupId().toString());
                resourceListChildrenDto.setName(res.getName());
                resourceListChildrenDto.setCode(res.getCode());
                resourceListChildrenDto.setIcon(res.getIcon());
                resourceListChildrenDto.setPath(res.getPath());
                resourceListChildrenDto.setEnabled(true);
                resourceListChildrenDto.setNoDropdown(false);
                childrenDtos.add(resourceListChildrenDto);
                subResourceMap.put(res.getSupId(), childrenDtos);
            } else {
                List<ResourceListChildrenDto> childrenDtos = new ArrayList<>();
                ResourceListChildrenDto resourceListChildrenDto = new ResourceListChildrenDto();
                resourceListChildrenDto.setId(res.getId().toString());
                resourceListChildrenDto.setParentId(res.getSupId().toString());
                resourceListChildrenDto.setName(res.getName());
                resourceListChildrenDto.setCode(res.getCode());
                resourceListChildrenDto.setIcon(res.getIcon());
                resourceListChildrenDto.setPath(res.getPath());
                resourceListChildrenDto.setEnabled(true);
                resourceListChildrenDto.setNoDropdown(false);
                childrenDtos.add(resourceListChildrenDto);
                subResourceMap.put(res.getSupId(), childrenDtos);
            }
        });
        resourceEntityList.stream().forEach(garbageResourceEntity -> {
            ResourceListDto dto = new ResourceListDto();
            dto.setId(garbageResourceEntity.getId().toString());
            dto.setCode(garbageResourceEntity.getCode());
            dto.setIcon(garbageResourceEntity.getIcon());
            dto.setName(garbageResourceEntity.getName());
            dto.setPath(garbageResourceEntity.getPath());
            dto.setEnabled(true);
            dto.setNoDropdown(true);
            List<ResourceListChildrenDto> children = subResourceMap.get(garbageResourceEntity.getId()) == null? new ArrayList<>():subResourceMap.get(garbageResourceEntity.getId());
            dto.setChildren(children);
            resourceListDtos.add(dto);
        });
        ResponseData responseData = new ResponseData();
        responseData.setData(resourceListDtos);
        responseData.setStatus(Constants.responseStatus.Success.getStatus());
        responseData.setMsg("tarBar 获取成功");
        return responseData;
    }
}
