package com.jin.env.garbage.dao.village;

import com.jin.env.garbage.entity.village.GarbageVillageInfoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface GarbageVillageInfoDao extends JpaRepository<GarbageVillageInfoEntity, Integer>, JpaSpecificationExecutor<GarbageVillageInfoEntity> {
    GarbageVillageInfoEntity findByVillageId(Long villageId);

}
