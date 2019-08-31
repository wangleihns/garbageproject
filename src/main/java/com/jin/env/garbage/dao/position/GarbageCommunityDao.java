package com.jin.env.garbage.dao.position;

import com.jin.env.garbage.entity.position.GarbageCommunityEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface GarbageCommunityDao extends JpaRepository<GarbageCommunityEntity, Integer>, JpaSpecificationExecutor<GarbageCommunityEntity> {
    List<GarbageCommunityEntity> findByCountryId(Long countyId);
}
