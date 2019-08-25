package com.jin.env.garbage.dao.user;

import com.jin.env.garbage.dto.user.UserCountInMonth;
import com.jin.env.garbage.dto.user.UserVillageDto;
import com.jin.env.garbage.entity.user.GarbageUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;

public interface GarbageUserDao extends JpaRepository<GarbageUserEntity, Integer>, JpaSpecificationExecutor<GarbageUserEntity> {

    @Query(value = "select u from GarbageUserEntity as u where u.phone=:username  or u.eNo=:username or u.idCard=:username")
    GarbageUserEntity findByPhoneOrENoOrIdCard(@Param(value = "username")String username);

    @Query(value = "select new com.jin.env.garbage.dto.user.UserVillageDto(u.id, u.villageName) from GarbageUserEntity u where u.id in (?1)")
    List<UserVillageDto> getUserVillageNameByIds(List ids);

    @Query(value =  "select new com.jin.env.garbage.dto.user.UserCountInMonth(u.month, count(u.id)) from GarbageUserEntity u where u.year = ?1 and u.month >=?2 and u.month<=?3  group by u.month")
    List<UserCountInMonth> countUserInMonthBetween(Integer year, Integer startMonth, Integer endMonth);
}
