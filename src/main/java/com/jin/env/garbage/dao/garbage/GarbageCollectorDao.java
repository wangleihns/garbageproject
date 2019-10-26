package com.jin.env.garbage.dao.garbage;

import com.jin.env.garbage.dto.garbage.GarbageRollingDto;
import com.jin.env.garbage.dto.garbage.GarbageWeightInMonth;
import com.jin.env.garbage.dto.garbage.UserCollectRightAndWeightDto;
import com.jin.env.garbage.dto.garbage.WeekCollectDto;
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

    @Query(nativeQuery = true,
        value = " SELECT SUM(garbage_weight) as weigth, " +
                " CASE garbage_type " +
                "     WHEN 1 THEN '易腐垃圾' " +
                "     WHEN 2 THEN '其他垃圾' " +
                "     WHEN 3 THEN '可回收垃圾' " +
                "     ELSE '有害垃圾' " +
                " END as garbageType, " +
                " FROM_UNIXTIME(collect_date / 1000,'%Y-%m-%d') as collectDate  " +
                " FROM " +
                " garbage_collector " +
                " WHERE village_id = ?1 " +
                " AND garbage_from_type = ?2 " +
                " AND collect_date BETWEEN ?3 AND ?4 " +
                " GROUP BY " +
                " garbage_type, `day` "
    )
    List<WeekCollectDto> sumGarbageWeightByVillage(Long villageId, Integer garbageFromType, Long startTime, Long endTime);

    @Query(nativeQuery = true,
            value = " SELECT SUM(garbage_weight) as weigth, " +
                    " CASE garbage_type " +
                    "     WHEN 1 THEN '易腐垃圾' " +
                    "     WHEN 2 THEN '其他垃圾' " +
                    "     WHEN 3 THEN '可回收垃圾' " +
                    "     ELSE '有害垃圾' " +
                    " END as garbageType, " +
                    " FROM_UNIXTIME(collect_date / 1000,'%Y-%m-%d') as collectDate  " +
                    " FROM " +
                    " garbage_collector " +
                    " WHERE town_id = ?1 " +
                    " AND garbage_from_type = ?2 " +
                    " AND collect_date BETWEEN ?3 AND ?4 " +
                    " GROUP BY " +
                    " garbage_type, `day` "
    )
    List<WeekCollectDto> sumGarbageWeightByTown(Long townId, Integer garbageFromType, Long startTime, Long endTime);

    @Query(nativeQuery = true,
            value = " SELECT SUM(garbage_weight) as weigth, " +
                    " CASE garbage_type " +
                    "     WHEN 1 THEN '易腐垃圾' " +
                    "     WHEN 2 THEN '其他垃圾' " +
                    "     WHEN 3 THEN '可回收垃圾' " +
                    "     ELSE '有害垃圾' " +
                    " END as garbageType, " +
                    " FROM_UNIXTIME(collect_date / 1000,'%Y-%m-%d') as collectDate  " +
                    " FROM " +
                    " garbage_collector " +
                    " WHERE country_id = ?1 " +
                    " AND garbage_from_type = ?2 " +
                    " AND collect_date BETWEEN ?3 AND ?4 " +
                    " GROUP BY " +
                    " garbage_type, `day` "
    )
    List<WeekCollectDto> sumGarbageWeightByCounty(Long countryId, Integer garbageFromType, Long startTime, Long endTime);


    @Query(nativeQuery = true,
            value = " SELECT SUM(garbage_weight) as weigth, " +
                    " CASE garbage_type " +
                    "     WHEN 1 THEN '易腐垃圾' " +
                    "     WHEN 2 THEN '其他垃圾' " +
                    "     WHEN 3 THEN '可回收垃圾' " +
                    "     ELSE '有害垃圾' " +
                    " END as garbageType, " +
                    " FROM_UNIXTIME(collect_date / 1000,'%Y-%m-%d') as collectDate  " +
                    " FROM " +
                    " garbage_collector " +
                    " WHERE city_id = ?1 " +
                    " AND garbage_from_type = ?2 " +
                    " AND collect_date BETWEEN ?3 AND ?4 " +
                    " GROUP BY " +
                    " garbage_type, `day` "
    )
    List<WeekCollectDto> sumGarbageWeightByCity(Long cityId, Integer garbageFromType, Long startTime, Long endTime);

    @Query(nativeQuery = true,
            value = " SELECT SUM(garbage_weight) as weigth, " +
                    " CASE garbage_type " +
                    "     WHEN 1 THEN '易腐垃圾' " +
                    "     WHEN 2 THEN '其他垃圾' " +
                    "     WHEN 3 THEN '可回收垃圾' " +
                    "     ELSE '有害垃圾' " +
                    " END as garbageType, " +
                    " FROM_UNIXTIME(collect_date / 1000,'%Y-%m-%d') as collectDate  " +
                    " FROM " +
                    " garbage_collector " +
                    " WHERE province_id = ?1 " +
                    " AND garbage_from_type = ?2 " +
                    " AND collect_date BETWEEN ?3 AND ?4 " +
                    " GROUP BY " +
                    " garbage_type, `day` "
    )
    List<WeekCollectDto> sumGarbageWeightByProvince(Long provinceId, Integer garbageFromType, Long startTime, Long endTime);

    @Query(nativeQuery = true,
            value = " SELECT SUM(garbage_weight) as weigth, " +
                    " CASE garbage_type " +
                    "     WHEN 1 THEN '易腐垃圾' " +
                    "     WHEN 2 THEN '其他垃圾' " +
                    "     WHEN 3 THEN '可回收垃圾' " +
                    "     ELSE '有害垃圾' " +
                    " END as garbageType, " +
                    " FROM_UNIXTIME(collect_date / 1000,'%Y-%m-%d') as collectDate  " +
                    " FROM " +
                    " garbage_collector " +
                    " WHERE community_id = ?1 " +
                    " AND garbage_from_type = ?2 " +
                    " AND collect_date BETWEEN ?3 AND ?4 " +
                    " GROUP BY " +
                    " garbage_type, `day` "
    )
    List<WeekCollectDto> sumGarbageWeightByCommunity(Long communityId, Integer garbageFromType, Long startTime, Long endTime);

    @Query(nativeQuery = true,
        value = "SELECT IFNULL(count(1), 0) from ( " +
                " SELECT user_id from garbage_collector " +
                " WHERE collect_date BETWEEN ?1 AND ?2" +
                " GROUP BY user_id" +
                " ) t"
    )
    Long getTodayUserCount(Long startTime, Long endTime);


    @Query(nativeQuery = true,
            value = " SELECT IFNULL(count(1), 0) from (" +
                    " select user_id  from garbage_collector GROUP BY user_id" +
                    " )t "
    )
    Long getTotalUserPartIn();

    @Query(nativeQuery = true,
        value = " select ROUND((select IFNULL(count(1), 0) from garbage_collector WHERE garbage_quality  = 1)/count(1), 3) as rightRate," +
                " ROUND(SUM(garbage_weight),2) as totalWeight " +
                " from garbage_collector"
    )
    UserCollectRightAndWeightDto getRightAndGarbageWeight();

    @Query(nativeQuery = true,
        value = "SELECT " +
                " county.county_name countyName," +
                " town.town_name as townName," +
                " v.village_name as villageName, " +
                " u.address as address," +
                " g.garbage_weight as weight," +
                " im.image_path as imagePath," +
                " FROM_UNIXTIME(collect_date / 1000,'%m-%d') as collectDate " +
                " FROM " +
                " garbage_collector g " +
                " INNER JOIN garbage_image im ON g.id = im.bus_id " +
                " INNER JOIN j_position_county county on g.country_id = county.county_id " +
                " INNER JOIN j_position_town town on g.town_id = town.town_id " +
                " INNER JOIN j_position_village v on v.village_id = g.village_id " +
                " INNER JOIN garbage_user u on u.id = g.user_id " +
                " AND im.source_name = ?1 " +
                " AND g.garbage_quality = 1  and g.village_id = ?2" +
                " AND g.garbage_type = ?3 ORDER BY g.id desc " +
                " LIMIT 50;"
    )
    List<GarbageRollingDto>  getTopRollingVillageGarbageInfoByVillageId(String sourceName, Long villageId, Integer garbageType);


    @Query(nativeQuery = true,
            value = "SELECT " +
                    " county.county_name countyName," +
                    " town.town_name as townName," +
                    " v.village_name as villageName, " +
                    " u.address as address," +
                    " g.garbage_weight as weight," +
                    " im.image_path as imagePath," +
                    " FROM_UNIXTIME(collect_date / 1000,'%m-%d') as collectDate " +
                    " FROM " +
                    " garbage_collector g " +
                    " INNER JOIN garbage_image im ON g.id = im.bus_id " +
                    " INNER JOIN j_position_county county on g.country_id = county.county_id " +
                    " INNER JOIN j_position_town town on g.town_id = town.town_id " +
                    " INNER JOIN j_position_village v on v.village_id = g.village_id " +
                    " INNER JOIN garbage_user u on u.id = g.user_id " +
                    " AND im.source_name = ?1 " +
                    " AND g.garbage_quality = 1  and g.town_id = ?2 " +
                    " AND g.garbage_type = ?3ORDER BY g.id desc " +
                    " LIMIT 50;"
    )
    List<GarbageRollingDto>  getTopRollingVillageGarbageInfoByTownId(String sourceName, Long townId, Integer garbageType);


    @Query(nativeQuery = true,
            value = "SELECT " +
                    " county.county_name countyName," +
                    " town.town_name as townName," +
                    " v.village_name as villageName, " +
                    " u.address as address," +
                    " g.garbage_weight as weight," +
                    " im.image_path as imagePath," +
                    " FROM_UNIXTIME(collect_date / 1000,'%m-%d') as collectDate " +
                    " FROM " +
                    " garbage_collector g " +
                    " INNER JOIN garbage_image im ON g.id = im.bus_id " +
                    " INNER JOIN j_position_county county on g.country_id = county.county_id " +
                    " INNER JOIN j_position_town town on g.town_id = town.town_id " +
                    " INNER JOIN j_position_village v on v.village_id = g.village_id " +
                    " INNER JOIN garbage_user u on u.id = g.user_id " +
                    " AND im.source_name = ?1 " +
                    " AND g.garbage_quality = 1  and g.country_id = ?2 " +
                    " AND g.garbage_type = ?3 ORDER BY g.id desc " +
                    " LIMIT 50;"
    )
    List<GarbageRollingDto>  getTopRollingVillageGarbageInfoByCountyId(String sourceName, Long countyId, Integer garbageType);


    @Query(nativeQuery = true,
            value = "SELECT " +
                    " county.county_name countyName," +
                    " town.town_name as townName," +
                    " v.village_name as villageName, " +
                    " u.address as address," +
                    " g.garbage_weight as weight," +
                    " im.image_path as imagePath," +
                    " FROM_UNIXTIME(collect_date / 1000,'%m-%d') as collectDate " +
                    " FROM " +
                    " garbage_collector g " +
                    " INNER JOIN garbage_image im ON g.id = im.bus_id " +
                    " INNER JOIN j_position_county county on g.country_id = county.county_id " +
                    " INNER JOIN j_position_town town on g.town_id = town.town_id " +
                    " INNER JOIN j_position_village v on v.village_id = g.village_id " +
                    " INNER JOIN garbage_user u on u.id = g.user_id " +
                    " AND im.source_name = ?1 " +
                    " AND g.garbage_quality = 1  and g.city_id = ?2 " +
                    " AND g.garbage_type = ?3 ORDER BY g.id desc " +
                    " LIMIT 50;"
    )
    List<GarbageRollingDto>  getTopRollingVillageGarbageInfoByCityId(String sourceName, Long cityId, Integer garbageType);


    @Query(nativeQuery = true,
            value = "SELECT " +
                    " county.county_name countyName," +
                    " town.town_name as townName," +
                    " v.village_name as villageName, " +
                    " u.address as address," +
                    " g.garbage_weight as weight," +
                    " im.image_path as imagePath," +
                    " FROM_UNIXTIME(collect_date / 1000,'%m-%d') as collectDate " +
                    " FROM " +
                    " garbage_collector g " +
                    " INNER JOIN garbage_image im ON g.id = im.bus_id " +
                    " INNER JOIN j_position_county county on g.country_id = county.county_id " +
                    " INNER JOIN j_position_town town on g.town_id = town.town_id " +
                    " INNER JOIN j_position_village v on v.village_id = g.village_id " +
                    " INNER JOIN garbage_user u on u.id = g.user_id " +
                    " AND im.source_name = ?1 " +
                    " AND g.garbage_quality = 1  and g.province_id = ?2 " +
                    " AND g.garbage_type = ?3 ORDER BY g.id desc " +
                    " LIMIT 50;"
    )
    List<GarbageRollingDto>  getTopRollingVillageGarbageInfoByProvinceId(String sourceName, Long provinceId, Integer garbageType);



    @Query(nativeQuery = true,
            value = "SELECT " +
                    " county.county_name countyName," +
                    " town.town_name as townName," +
                    " cm.community_name as villageName," +
                    " u.address as address," +
                    " g.garbage_weight as weight," +
                    " im.image_path as imagePath," +
                    " FROM_UNIXTIME(collect_date / 1000,'%m-%d') as collectDate " +
                    " FROM " +
                    " garbage_collector g " +
                    " INNER JOIN garbage_image im ON g.id = im.bus_id " +
                    " INNER JOIN j_position_county county on g.country_id = county.county_id " +
                    " INNER JOIN j_position_town town on g.town_id = town.town_id " +
                    " INNER JOIN garbage_community cm on cm.id = g.community_id " +
                    " INNER JOIN garbage_user u on u.id = g.user_id " +
                    " AND im.source_name = ?1 " +
                    " AND g.garbage_quality = 1 AND g.community_id IN (?2) " +
                    " AND g.garbage_type = ?3 ORDER BY g.id desc " +
                    " LIMIT 50;"
    )
    List<GarbageRollingDto>  getTopRollingCommunityGarbageInfo(String sourceName, List<Integer> communityIds, Integer garbageType);



}
