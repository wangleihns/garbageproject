package com.jin.env.garbage.dao.remark;

import com.jin.env.garbage.entity.remark.GarbageRemarkAgainRecordEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface GarbageRemarkAgainRecordDao extends JpaRepository<GarbageRemarkAgainRecordEntity, Integer>, JpaSpecificationExecutor<GarbageRemarkAgainRecordEntity> {
}
