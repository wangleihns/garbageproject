package com.jin.env.garbage.dao.point;

import com.jin.env.garbage.entity.point.GarbagePointRecordEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface GarbagePointRecordDao extends JpaRepository<GarbagePointRecordEntity, Integer>, JpaSpecificationExecutor<GarbagePointRecordEntity> {

    GarbagePointRecordEntity  findBySourceNameAndBusId(String sourceName, Integer busId);
}
