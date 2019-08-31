package com.jin.env.garbage.dao.image;

import com.jin.env.garbage.entity.image.GarbageImageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface GarbageImageDao extends JpaRepository<GarbageImageEntity, Integer>, JpaSpecificationExecutor<GarbageImageEntity>{
    GarbageImageEntity findBySourceNameAndBusIdAndType(String sourceName, Integer busId, String type);

    int deleteBySourceNameAndBusIdAndType(String sourceName, Integer busId, String type);

    List<GarbageImageEntity> findBySourceNameAndType(String sourceName, String type);
}
