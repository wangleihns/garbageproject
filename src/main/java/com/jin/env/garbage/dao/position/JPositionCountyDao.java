package com.jin.env.garbage.dao.position;

import com.jin.env.garbage.entity.position.JPositionCountyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface JPositionCountyDao extends JpaRepository<JPositionCountyEntity, Integer>, JpaSpecificationExecutor<JPositionCountyEntity> {
    JPositionCountyEntity findByCountyId(Long countyId);
}
