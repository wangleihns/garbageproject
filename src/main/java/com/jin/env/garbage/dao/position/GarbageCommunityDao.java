package com.jin.env.garbage.dao.position;

import com.jin.env.garbage.entity.position.GarbageCommunityEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface GarbageCommunityDao extends JpaRepository<GarbageCommunityEntity, Integer>, JpaSpecificationExecutor<GarbageCommunityEntity> {
}
