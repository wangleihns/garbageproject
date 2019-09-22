package com.jin.env.garbage.dao.user;

import com.jin.env.garbage.dto.resource.ResourceListDto;
import com.jin.env.garbage.dto.resource.UserResourceDto;
import com.jin.env.garbage.entity.user.GarbageResourceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Set;

public interface GarbageResourceDao extends JpaRepository<GarbageResourceEntity, Integer>, JpaSpecificationExecutor<GarbageResourceEntity> {

    /**
     * 主资源
     * @param userId
     * @return
     */
    @Query(nativeQuery =  true, value = "SELECT gr.* FROM garbage_resource gr " +
            " LEFT JOIN garbage_role_resource grr on gr.id = grr.resource_id" +
            " LEFT JOIN garbage_role  r on r.id = grr.role_id " +
            " LEFT JOIN garbage_user_role ur on  r.id = ur.role_id" +
            " LEFT JOIN garbage_user u on ur.user_id = u.id " +
            " WHERE u.id = ?1 AND  r.status = 1 AND gr.sup_id = 0")
    List<GarbageResourceEntity> findByResourceByUserId(Integer userId);

    /**
     * 所有子资源
     * @return
     */
    @Query(value = "select new com.jin.env.garbage.dto.resource.UserResourceDto(r.id, r.name, r.url, r.icon, r.supId) " +
            " from GarbageResourceEntity r  where r.supId <> 0")
    List<UserResourceDto> getUserSubResourceInfoList();

    List<GarbageResourceEntity> findByIdIn(Set<Integer> ids);

    @Query(nativeQuery = true , value = "SELECT gr.* FROM garbage_resource gr " +
            " LEFT JOIN garbage_role_resource grr on gr.id = grr.resource_id" +
            " LEFT JOIN garbage_role  r on r.id = grr.role_id " +
            " LEFT JOIN garbage_user_role ur on  r.id = ur.role_id" +
            " LEFT JOIN garbage_user u on ur.user_id = u.id " +
            " WHERE u.id = ?1 AND  r.status = 1 AND gr.sup_id <> ?2")
    List<GarbageResourceEntity> findBySupIdNot(Integer userId, Integer supId);
}
