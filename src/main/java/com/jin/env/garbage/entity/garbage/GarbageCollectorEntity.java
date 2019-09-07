package com.jin.env.garbage.entity.garbage;

import com.jin.env.garbage.entity.base.BaseEntity;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "garbage_collector", schema = "garbage_db", catalog = "")
public class GarbageCollectorEntity extends BaseEntity{
    private Integer collectorId;
    private String collectorName;
    private String collectorPhone;
    private Double garbageWeight;
    private Integer garbagePoint;
    private Integer garbageQuality;
    private String garbageImage;
    private Long collectDate;
    private Integer garbageType;
    private String eNo;
    private Integer userId;
    private Long villageId;
    private Integer day;
    private Integer month;
    private Integer year;
    private Long provinceId;
    private Long cityId;
    private Long countryId;
    private Long townId;
    private Integer garbageFromType;
    private Long communityId;

    private Boolean isCheck; //true 审核 false 未审核


    @Basic
    @Column(name = "collector_id")
    public Integer getCollectorId() {
        return collectorId;
    }

    public void setCollectorId(Integer collectorId) {
        this.collectorId = collectorId;
    }

    @Basic
    @Column(name = "collector_name")
    public String getCollectorName() {
        return collectorName;
    }

    public void setCollectorName(String collectorName) {
        this.collectorName = collectorName;
    }
    @Basic
    @Column(name = "collector_phone")
    public String getCollectorPhone() {
        return collectorPhone;
    }

    public void setCollectorPhone(String collectorPhone) {
        this.collectorPhone = collectorPhone;
    }

    @Basic
    @Column(name = "garbage_weight")
    public Double getGarbageWeight() {
        return garbageWeight;
    }

    public void setGarbageWeight(Double garbageWeight) {
        this.garbageWeight = garbageWeight;
    }

    @Basic
    @Column(name = "garbage_point")
    public Integer getGarbagePoint() {
        return garbagePoint;
    }

    public void setGarbagePoint(Integer garbagePoint) {
        this.garbagePoint = garbagePoint;
    }

    @Basic
    @Column(name = "garbage_quality")
    public Integer getGarbageQuality() {
        return garbageQuality;
    }

    public void setGarbageQuality(Integer garbageQuality) {
        this.garbageQuality = garbageQuality;
    }

    @Basic
    @Column(name = "garbage_image")
    public String getGarbageImage() {
        return garbageImage;
    }

    public void setGarbageImage(String garbageImage) {
        this.garbageImage = garbageImage;
    }

    @Basic
    @Column(name = "collect_date")
    public Long getCollectDate() {
        return collectDate;
    }

    public void setCollectDate(Long collectDate) {
        this.collectDate = collectDate;
    }

    @Basic
    @Column(name = "garbage_type")
    public Integer getGarbageType() {
        return garbageType;
    }

    public void setGarbageType(Integer garbageType) {
        this.garbageType = garbageType;
    }

    @Basic
    @Column(name = "e_no")
    public String geteNo() {
        return eNo;
    }

    public void seteNo(String eNo) {
        this.eNo = eNo;
    }

    @Basic
    @Column(name = "user_id")
    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    @Basic
    @Column(name = "village_id")
    public Long getVillageId() {
        return villageId;
    }

    public void setVillageId(Long villageId) {
        this.villageId = villageId;
    }

    @Basic
    @Column(name = "day")
    public Integer getDay() {
        return day;
    }

    public void setDay(Integer day) {
        this.day = day;
    }

    @Basic
    @Column(name = "month")
    public Integer getMonth() {
        return month;
    }

    public void setMonth(Integer month) {
        this.month = month;
    }

    @Basic
    @Column(name = "year")
    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    @Basic
    @Column(name = "province_id")
    public Long getProvinceId() {
        return provinceId;
    }

    public void setProvinceId(Long provinceId) {
        this.provinceId = provinceId;
    }

    @Basic
    @Column(name = "city_id")
    public Long getCityId() {
        return cityId;
    }

    public void setCityId(Long cityId) {
        this.cityId = cityId;
    }

    @Basic
    @Column(name = "country_id")
    public Long getCountryId() {
        return countryId;
    }

    public void setCountryId(Long countryId) {
        this.countryId = countryId;
    }

    @Basic
    @Column(name = "town_id")
    public Long getTownId() {
        return townId;
    }

    public void setTownId(Long townId) {
        this.townId = townId;
    }

    @Basic
    @Column(name = "garbage_from_type")
    public Integer getGarbageFromType() {
        return garbageFromType;
    }

    public void setGarbageFromType(Integer garbageFromType) {
        this.garbageFromType = garbageFromType;
    }

    @Basic
    @Column(name = "community_id")
    public Long getCommunityId() {
        return communityId;
    }

    public void setCommunityId(Long communityId) {
        this.communityId = communityId;
    }

    @Basic
    @Column(name = "is_check")
    public Boolean getCheck() {
        return isCheck;
    }

    public void setCheck(Boolean check) {
        isCheck = check;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GarbageCollectorEntity that = (GarbageCollectorEntity) o;
        return id == that.id &&
                Objects.equals(createId, that.createId) &&
                Objects.equals(createTime, that.createTime) &&
                Objects.equals(updateId, that.updateId) &&
                Objects.equals(updateTime, that.updateTime) &&
                Objects.equals(collectorId, that.collectorId) &&
                Objects.equals(collectorName, that.collectorName) &&
                Objects.equals(garbageWeight, that.garbageWeight) &&
                Objects.equals(garbagePoint, that.garbagePoint) &&
                Objects.equals(garbageQuality, that.garbageQuality) &&
                Objects.equals(garbageImage, that.garbageImage) &&
                Objects.equals(collectDate, that.collectDate) &&
                Objects.equals(eNo, that.eNo) &&
                Objects.equals(userId, that.userId) &&
                Objects.equals(villageId, that.villageId) &&
                Objects.equals(day, that.day) &&
                Objects.equals(month, that.month) &&
                Objects.equals(year, that.year) &&
                Objects.equals(provinceId, that.provinceId) &&
                Objects.equals(cityId, that.cityId) &&
                Objects.equals(countryId, that.countryId) &&
                Objects.equals(townId, that.townId) &&
                Objects.equals(garbageFromType, that.garbageFromType) &&
                Objects.equals(communityId, that.communityId);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, createId, createTime, updateId, updateTime, collectorId, collectorName, garbageWeight, garbagePoint, garbageQuality, garbageImage, collectDate, eNo, userId, villageId, day, month, year, provinceId, cityId, countryId, townId, garbageFromType, communityId);
    }
}
