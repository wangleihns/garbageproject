package com.jin.env.garbage.dao.garbage;

import com.jin.env.garbage.entity.garbage.GarbageQualityPointEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface GarbageQualityPointDao extends JpaRepository<GarbageQualityPointEntity, Integer>, JpaSpecificationExecutor<GarbageQualityPointEntity> {

    GarbageQualityPointEntity findByPlaceIdAndType(Integer placeId, Integer type);

}
