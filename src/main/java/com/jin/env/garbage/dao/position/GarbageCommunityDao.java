package com.jin.env.garbage.dao.position;

import com.jin.env.garbage.dto.position.UserPositionDto;
import com.jin.env.garbage.entity.position.GarbageCommunityEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface GarbageCommunityDao extends JpaRepository<GarbageCommunityEntity, Integer>, JpaSpecificationExecutor<GarbageCommunityEntity> {
    List<GarbageCommunityEntity> findByCountryId(Long countyId);

    @Query(value = "select new com.jin.env.garbage.dto.position.UserPositionDto(com.id, p.provinceName, city.cityName, cou.countyName, com.communityName) " +
            " from GarbageCommunityEntity com, JPositionCountyEntity cou, JPositionCityEntity city, JPositionProvinceEntity p" +
            " where com.countryId = cou.countyId and cou.cityId = city.cityId and city.provinceId = p.provinceId and com.id in (?1)")
    List<UserPositionDto> selectCommunity(List<Long> placeIds);

    @Query(nativeQuery = true, value = "select com.* from garbage_community com LEFT JOIN garbage_role_community rc on com.id = rc.community_id where rc.role_id in(?1)")
    List<GarbageCommunityEntity> findByRoleIds(List<Integer> roleIds);
}
