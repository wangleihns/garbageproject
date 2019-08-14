package com.jin.env.garbage.dao.garbage;

import com.jin.env.garbage.entity.garbage.GarbageCollectorEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface GarbageCollectorDao extends JpaRepository<GarbageCollectorEntity, Integer>, JpaSpecificationExecutor<GarbageCollectorEntity> {
}
