package com.jin.env.garbage.dao.garbage;

import com.jin.env.garbage.entity.garbage.GarbageOrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface GarbageOrderDao extends JpaRepository<GarbageOrderEntity, Integer>, JpaSpecificationExecutor<GarbageOrderEntity> {
}
