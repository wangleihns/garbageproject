package com.jin.env.garbage.dao.user;

import com.jin.env.garbage.entity.user.GarbageRoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface GarbageRoleDao extends JpaRepository<GarbageRoleEntity, Integer>, JpaSpecificationExecutor<GarbageRoleEntity> {

    GarbageRoleEntity findByRoleCode(String roleCode);

    @Query(nativeQuery = true, value = "select * FROM  garbage_role role " +
            "INNER JOIN  garbage_user_role urole ON role.id = urole.role_id  " +
            "AND  urole.user_id= ?1 AND role.status = 1")
    List<GarbageRoleEntity> findByUserId(Integer userId);
}
