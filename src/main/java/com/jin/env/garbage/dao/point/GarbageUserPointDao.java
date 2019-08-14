package com.jin.env.garbage.dao.point;

import com.jin.env.garbage.entity.point.GarbageUserPointEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface GarbageUserPointDao extends JpaRepository<GarbageUserPointEntity, Integer>, JpaSpecificationExecutor<GarbageUserPointEntity> {
    GarbageUserPointEntity findByUserId(Integer userId);
}
