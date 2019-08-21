package com.jin.env.garbage.dao.user;

import com.jin.env.garbage.entity.user.GarbageRoleCommunityEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface GarbageRoleCommunityDao extends JpaRepository<GarbageRoleCommunityEntity, Integer>, JpaSpecificationExecutor<GarbageRoleCommunityEntity> {
    void deleteAllByRoleId(Integer roleId);
}
