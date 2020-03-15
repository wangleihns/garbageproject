package com.jin.env.garbage.dao.message;

import com.jin.env.garbage.entity.message.GarbageMessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface GarbageMessageDao  extends JpaRepository<GarbageMessageEntity, Integer>, JpaSpecificationExecutor<GarbageMessageEntity> {
}
