package com.jin.env.garbage.dao.garbage;

import com.jin.env.garbage.dto.garbage.GarbageWeightInMonth;
import com.jin.env.garbage.entity.garbage.GarbageCollectorEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface GarbageCollectorDao extends JpaRepository<GarbageCollectorEntity, Integer>, JpaSpecificationExecutor<GarbageCollectorEntity> {

    @Query(value = "select new com.jin.env.garbage.dto.garbage.GarbageWeightInMonth(u.month, sum(u.garbageWeight)) from GarbageCollectorEntity u where u.year = ?1 group by  u.month")
    List<GarbageWeightInMonth> getGarbageWeightInMonth(Integer year);
}
