package com.jin.env.garbage.dao.garbage;

import com.jin.env.garbage.dto.garbage.GarbageWeightInMonth;
import com.jin.env.garbage.entity.garbage.GarbageCollectorEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface GarbageCollectorDao extends JpaRepository<GarbageCollectorEntity, Integer>, JpaSpecificationExecutor<GarbageCollectorEntity> {

    @Query(value = "select new com.jin.env.garbage.dto.garbage.GarbageWeightInMonth(u.month, sum(u.garbageWeight)) from GarbageCollectorEntity u where u.year = ?1 and u.month >=?2 and u.month <=?3 group by  u.month")
    List<GarbageWeightInMonth> getGarbageWeightInMonthBetween(Integer year, Integer startMonth, Integer endMonth);


    @Query(nativeQuery = true, value = "SELECT COUNT(qualified) FROM  garbage_collector  WHERE  collect_date BETWEEN ?1 AND  ?2")
    Long countQualityToday(Long start, Long end);

    @Query(value = " select sum(c.garbageWeight) from GarbageCollectorEntity c where c.collectDate between ?1 and ?2")
    Double countGarbageWeight(Long start, Long end);
}
