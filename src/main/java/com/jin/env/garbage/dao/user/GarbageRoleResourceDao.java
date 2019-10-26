package com.jin.env.garbage.dao.user;

import com.jin.env.garbage.entity.user.GarbageRoleResourceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface GarbageRoleResourceDao extends JpaRepository<GarbageRoleResourceEntity, Integer>, JpaSpecificationExecutor<GarbageRoleResourceEntity> {
    @Modifying
    @Query(value = "delete from GarbageRoleResourceEntity u where u.roleId = ?1")
    void deleteAllByRoleId(Integer roleId);

    List<GarbageRoleResourceEntity> findByRoleId(Integer roleId);

    /**
     * 获取主资源
     * @param userId
     * @return
     */
    @Query(nativeQuery = true , value = " select DISTINCT resource_id from garbage_role_resource rr " +
            " INNER JOIN garbage_user_role ur on rr.role_id = ur.role_id " +
            " INNER JOIN garbage_resource res on rr.resource_id = res.id and res.sup_id = 0 " +
            " WHERE ur.user_id = ?1")
    List<Integer> findResourceSupByUserId(Integer userId);

    /**
     * 获取子资源
     * @param userId
     * @return
     */
    @Query(nativeQuery = true , value = " select DISTINCT resource_id from garbage_role_resource rr " +
            " INNER JOIN garbage_user_role ur on rr.role_id = ur.role_id " +
            " INNER JOIN garbage_resource res on rr.resource_id = res.id and res.sup_id <> 0 " +
            " WHERE ur.user_id = ?1")
    List<Integer> findResourceSubByUserId(Integer userId);
}
