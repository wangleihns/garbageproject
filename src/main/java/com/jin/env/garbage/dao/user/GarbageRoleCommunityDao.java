package com.jin.env.garbage.dao.user;

import com.jin.env.garbage.entity.user.GarbageRoleCommunityEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface GarbageRoleCommunityDao extends JpaRepository<GarbageRoleCommunityEntity, Integer>, JpaSpecificationExecutor<GarbageRoleCommunityEntity> {
    @Modifying
    @Query(value = "delete from GarbageRoleCommunityEntity u where  u.roleId = ?1")
    void deleteAllByRoleId(Integer roleId);

    List<GarbageRoleCommunityEntity> findByRoleId(Integer roleId);
}
