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
            " and u.id = ?1")
    List<GarbageResourceEntity> findByResourceByUserId(Integer userId);
}
