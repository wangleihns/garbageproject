package com.jin.env.garbage.dao.user;

import com.jin.env.garbage.entity.user.GarbageResourceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface GarbageResourceDao extends JpaRepository<GarbageResourceEntity, Integer>, JpaSpecificationExecutor<GarbageResourceEntity> {

    @Query(nativeQuery =  true, value = "SELECT gr.* FROM garbage_resource gr " +
            " LEFT JOIN garbage_role_resource grr on gr.id = grr.resource_id" +
            " LEFT JOIN garbage_role  r on r.id = grr.role_id " +
            " LEFT JOIN garbage_user_role ur on  r.id = ur.role_id" +
            " LEFT JOIN garbage_user u on ur.user_id = u.id " +
            " and u.id = ?1 AND  r.status = 1")
    List<GarbageResourceEntity> findByResourceByUserId(Integer userId);

    /**
     * 获取小区的资源id
     * @param roleIds
     * @return
     */
    @Query(nativeQuery =  true, value = "select DISTINCT r.id from garbage_resource r inner join garbage_role_resource rr" +
            " on r.id = rr.resource_id AND  rr.role_id IN ?1 AND  r.ft_type = 'community'")
    List<Integer> getAllCommunityIdsByRoleIds(List<Integer> roleIds);


    List<GarbageResourceEntity> findBySupIdAndFtType(Integer supId, String ft_type);
}
