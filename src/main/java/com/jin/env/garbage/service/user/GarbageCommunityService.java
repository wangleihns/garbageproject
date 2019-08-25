package com.jin.env.garbage.service.user;

import com.jin.env.garbage.dao.position.GarbageCommunityDao;
import com.jin.env.garbage.dao.user.GarbageRoleCommunityDao;
import com.jin.env.garbage.entity.position.GarbageCommunityEntity;
import com.jin.env.garbage.entity.user.GarbageRoleCommunityEntity;
import com.jin.env.garbage.utils.Constants;
import com.jin.env.garbage.utils.ResponseData;
import com.jin.env.garbage.utils.ResponsePageData;
import org.apache.tomcat.util.bcel.Const;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class GarbageCommunityService {
    @Autowired
    private GarbageCommunityDao garbageCommunityDao;

    @Autowired
    private GarbageRoleCommunityDao garbageRoleCommunityDao;

    public ResponsePageData getCommunityList(Integer pageNo, Integer pageSize, String search) {
        Pageable pageable = PageRequest.of(pageNo - 1, pageSize, new Sort(Sort.Direction.DESC, "id"));
        Page<GarbageCommunityEntity> communityEntitiesPage = garbageCommunityDao.findAll(new Specification<GarbageCommunityEntity>() {
            @Nullable
            @Override
            public Predicate toPredicate(Root<GarbageCommunityEntity> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList<>();
//                Predicate predicateStatus = criteriaBuilder.equal(root.get("status"), Constants.dataType.ENABLE.getType());
//                predicates.add(predicateStatus);
                if (!StringUtils.isEmpty(search)){
                    Predicate predicate = criteriaBuilder.like(root.get("communityName"), "%"+ search + "%");
                    predicates.add(predicate);
                }
                return  criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        }, pageable);
        ResponsePageData responsePageData = new ResponsePageData();
        responsePageData.setCount(communityEntitiesPage.getTotalPages());
        responsePageData.setFirstPage(communityEntitiesPage.isFirst());
        responsePageData.setLastPage(communityEntitiesPage.isLast());
        responsePageData.setData(communityEntitiesPage.getContent());
        responsePageData.setPageNo(pageNo);
        responsePageData.setPageSize(pageSize);
        responsePageData.setTotalElement(communityEntitiesPage.getTotalElements());
        responsePageData.setStatus(Constants.responseStatus.Success.getStatus());
        responsePageData.setMsg("获取小区资源成功");
        return  responsePageData;
    }

    @Transactional
    public ResponseData addCommunityToRole(Integer roleId, Integer[] communityIds) {
        ResponseData responseData = new ResponseData();
        garbageRoleCommunityDao.deleteAllByRoleId(roleId);
        List<GarbageRoleCommunityEntity> roleCommunityList = new ArrayList<>();
        Arrays.stream(communityIds).forEach(communityId ->{
            GarbageRoleCommunityEntity entity = new GarbageRoleCommunityEntity();
            entity.setRoleId(roleId);
            entity.setCommunityId(communityId);
            roleCommunityList.add(entity);
        });
        garbageRoleCommunityDao.saveAll(roleCommunityList);
        responseData.setStatus(Constants.responseStatus.Success.getStatus());
        responseData.setMsg("添加成功");
        return responseData;
    }

    public ResponseData updateCommunityStatus(Integer communityId, Integer status) {
        GarbageCommunityEntity communityEntity = garbageCommunityDao.findById(communityId).get();
        communityEntity.setStatus(status);
        garbageCommunityDao.save(communityEntity);
        ResponseData responseData = new ResponseData();
        responseData.setStatus(Constants.responseStatus.Success.getStatus());
        responseData.setMsg("修改成功");
        return responseData;
    }

    public ResponseData updateCommunity(Integer communityId, String communityName, String address, String desc, Integer status) {
        GarbageCommunityEntity communityEntity = garbageCommunityDao.findById(communityId).get();
        communityEntity.setStatus(status);
//        communityEntity.setAddress(address);
        communityEntity.setCommunityName(communityName);
        communityEntity.setDesc(desc);
        garbageCommunityDao.save(communityEntity);
        ResponseData responseData = new ResponseData();
        responseData.setStatus(Constants.responseStatus.Success.getStatus());
        responseData.setMsg("修改成功");
        return responseData;
    }
}
