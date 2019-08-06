package com.jin.env.garbage.dao.position;

import com.jin.env.garbage.entity.position.JPositionCityEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface JPositionCityDao extends JpaRepository<JPositionCityEntity, Integer>, JpaSpecificationExecutor<JPositionCityEntity> {
}
