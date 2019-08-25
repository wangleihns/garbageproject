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
}
