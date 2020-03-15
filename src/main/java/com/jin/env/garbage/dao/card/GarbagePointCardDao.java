package com.jin.env.garbage.dao.card;

import com.jin.env.garbage.entity.card.GarbagePointCardEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface GarbagePointCardDao extends JpaRepository<GarbagePointCardEntity, Integer>, JpaSpecificationExecutor<GarbagePointCardEntity>{
    List<GarbagePointCardEntity> findByPointCardIn(List<String> cards);
}
