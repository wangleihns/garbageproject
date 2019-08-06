package com.jin.env.garbage.dao.position;

import com.jin.env.garbage.entity.position.JPositionTownEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface JPositionTownDao extends JpaRepository<JPositionTownEntity, Integer>, JpaSpecificationExecutor<JPositionTownEntity> {
}
