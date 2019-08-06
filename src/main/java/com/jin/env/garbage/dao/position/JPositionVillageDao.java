package com.jin.env.garbage.dao.position;

import com.jin.env.garbage.entity.position.JPositionVillageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface JPositionVillageDao extends JpaRepository<JPositionVillageEntity, Integer>, JpaSpecificationExecutor<JPositionVillageEntity> {
}
