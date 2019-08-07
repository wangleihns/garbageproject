package com.jin.env.garbage.dao.user;

import com.jin.env.garbage.entity.user.GarbageRoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface GarbageRoleDao extends JpaRepository<GarbageRoleEntity, Integer>, JpaSpecificationExecutor<GarbageRoleEntity> {

    GarbageRoleEntity findByRoleCode(String roleCode);
}
