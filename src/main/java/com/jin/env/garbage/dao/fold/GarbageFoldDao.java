package com.jin.env.garbage.dao.fold;

import com.jin.env.garbage.entity.fold.GarbageFoldEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface GarbageFoldDao extends JpaRepository<GarbageFoldEntity, Integer>, JpaSpecificationExecutor<GarbageFoldEntity> {

}
