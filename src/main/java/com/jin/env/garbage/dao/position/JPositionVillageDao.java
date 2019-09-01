package com.jin.env.garbage.dao.position;

import com.jin.env.garbage.dto.position.UserPositionDto;
import com.jin.env.garbage.entity.position.JPositionVillageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface JPositionVillageDao extends JpaRepository<JPositionVillageEntity, Integer>, JpaSpecificationExecutor<JPositionVillageEntity> {
    JPositionVillageEntity findByVillageId(Long villageId);



    @Query(value = "select new com.jin.env.garbage.dto.position.UserPositionDto(v.villageId,p.provinceName, city.cityName, c.countyName, t.townName, v.villageName) from JPositionVillageEntity v" +
            " left join JPositionTownEntity t on v.townId = t.townId" +
            " left join JPositionCountyEntity c on c.countyId = t.countyId " +
            " left join JPositionCityEntity city on city.cityId = c.cityId " +
            " left join JPositionProvinceEntity p on p.provinceId = city.provinceId " +
            " where v.villageId in (?1)" )
    List<UserPositionDto> selectPosition(List<Long> placeIds);
}
