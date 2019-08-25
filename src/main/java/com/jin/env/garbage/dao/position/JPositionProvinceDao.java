package com.jin.env.garbage.dao.position;

import com.jin.env.garbage.entity.position.JPositionProvinceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface JPositionProvinceDao extends JpaRepository<JPositionProvinceEntity, Integer>, JpaSpecificationExecutor<JPositionProvinceEntity> {
    JPositionProvinceEntity findByProvinceId(Integer provinceId);
}
