package com.jin.env.garbage.dao.user;

import com.jin.env.garbage.dto.user.UserCountInMonth;
import com.jin.env.garbage.dto.user.UserDto;
import com.jin.env.garbage.dto.user.UserDtoForLeader;
import com.jin.env.garbage.dto.user.UserVillageDto;
import com.jin.env.garbage.entity.user.GarbageUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GarbageUserDao extends JpaRepository<GarbageUserEntity, Integer>, JpaSpecificationExecutor<GarbageUserEntity> {

    @Query(value = "select u from GarbageUserEntity as u where u.phone=:username  or u.eNo=:username or u.idCard=:username")
    GarbageUserEntity findByPhoneOrENoOrIdCard(@Param(value = "username")String username);

    @Query(value = "select new com.jin.env.garbage.dto.user.UserVillageDto(u.id, u.villageName) from GarbageUserEntity u where u.id in (?1)")
    List<UserVillageDto> getUserVillageNameByIds(List ids);

    @Query(value =  "select new com.jin.env.garbage.dto.user.UserCountInMonth(u.month, count(u.id)) from GarbageUserEntity u where u.year = ?1 and u.month >=?2 and u.month<=?3  group by u.month")
    List<UserCountInMonth> countUserInMonthBetween(Integer year, Integer startMonth, Integer endMonth);

    @Query(value = "SELECT new com.jin.env.garbage.dto.user.UserDto(u.id, u.name, u.phone, u.address ) from  GarbageUserEntity u where u.id in(?1)")
    List<UserDto> selectUserInfoByIdIn(List<Integer> ids);

    @Query(value = "select new com.jin.env.garbage.dto.user.UserDtoForLeader(u.id, u.name, u.phone, u.address, u.dangYuan, u.cunMinDaiBiao, u.streetCommentDaiBiao, u.liangDaiBiaoYiWeiYuan, u.cunLeader, u.cunZuLeader, u.womenExeLeader, u.countryName, u.townName, u.villageName) from GarbageUserEntity u where u.id in(?1)")
    List<UserDtoForLeader> selectUserLeaderInfoByIdIn(List<Integer> ids);

    @Query(value = "select new com.jin.env.garbage.dto.user.UserDto(u.id, u.name, u.phone, CONCAT(u.provinceName, u.cityName, u.countryName, u.townName)) from GarbageUserEntity u where u.id = ?1")
    UserDto selectUserInfoByUserId(Integer id);

    List<GarbageUserEntity> findByPhoneIn(List<String> phones);

    GarbageUserEntity findByPhone(String phone);

    @Query(nativeQuery =  true,
        value = "select * from garbage_user u INNER JOIN garbage_point_card g ON u.id = g.user_id WHERE u.phone = ?1 OR g.point_card = ?1")
    GarbageUserEntity findByPhoneOrEno(String value);
}
