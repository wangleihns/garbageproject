package com.jin.env.garbage.dao.user;

import com.jin.env.garbage.entity.user.GarbageRoleResourceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface GarbageRoleResourceDao extends JpaRepository<GarbageRoleResourceEntity, Integer>, JpaSpecificationExecutor<GarbageRoleResourceEntity> {
}
