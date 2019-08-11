package com.jin.env.garbage.dao.user;

import com.jin.env.garbage.entity.user.GarbageENoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface GarbageENoDao extends JpaRepository<GarbageENoEntity, Integer>, JpaSpecificationExecutor<GarbageENoEntity> {
    List<GarbageENoEntity> findByUserId(Integer userId);
}
