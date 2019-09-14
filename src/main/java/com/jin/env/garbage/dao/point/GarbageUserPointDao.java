package com.jin.env.garbage.dao.point;

import com.jin.env.garbage.entity.point.GarbageUserPointEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface GarbageUserPointDao extends JpaRepository<GarbageUserPointEntity, Integer>, JpaSpecificationExecutor<GarbageUserPointEntity> {
    GarbageUserPointEntity findByUserId(Integer userId);

    List<GarbageUserPointEntity> findByUserIdIn(List<Integer> userids);
}
