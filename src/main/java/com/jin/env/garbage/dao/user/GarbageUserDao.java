package com.jin.env.garbage.dao.user;

import com.jin.env.garbage.entity.user.GarbageUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GarbageUserDao extends JpaRepository<GarbageUserEntity, Integer>, JpaSpecificationExecutor<GarbageUserEntity> {

    @Query(value = "select u from GarbageUserEntity as u where u.phone=:username  or u.eNo=:username or u.idCard=:username")
    GarbageUserEntity findByPhoneOrENoOrIdCard(@Param(value = "username")String username);
}
