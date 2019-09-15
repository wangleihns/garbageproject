package com.jin.env.garbage.entity.garbage;

import com.jin.env.garbage.entity.base.BaseEntity;

import javax.persistence.*;

@Entity
@Table(name = "garbage_quality_point", schema = "garbage_db", catalog = "")
public class GarbageQualityPointEntity extends BaseEntity {
    private Integer empty;
    private Integer noQualified;
    private Integer qualified;
    private Long provinceId;
    private Long cityId;
    private Long countyId;
    private Long townId;
    private Long placeId;
    private Integer type;
    private String placeName;

    @Basic
    @Column(name = "empty")
    public Integer getEmpty() {
        return empty;
    }

    public void setEmpty(Integer empty) {
        this.empty = empty;
    }

    @Basic
    @Column(name = "no_qualified")
    public Integer getNoQualified() {
        return noQualified;
    }

    public void setNoQualified(Integer noQualified) {
        this.noQualified = noQualified;
    }

    @Basic
    @Column(name = "qualified")
    public Integer getQualified() {
        return qualified;
    }

    public void setQualified(Integer qualified) {
        this.qualified = qualified;
    }

    @Basic
    @Column(name = "place_id")
    public Long getPlaceId() {
        return placeId;
    }

    public void setPlaceId(Long placeId) {
        this.placeId = placeId;
    }

    @Basic
    @Column(name = "type")
    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
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
    @Column(name = "county_id")
    public Long getCountyId() {
        return countyId;
    }

    public void setCountyId(Long countyId) {
        this.countyId = countyId;
    }

    @Basic
    @Column(name = "town_id")
    public Long getTownId() {
        return townId;
    }

    public void setTownId(Long townId) {
        this.townId = townId;
    }

    public String getPlaceName() {
        return placeName;
    }

    public void setPlaceName(String placeName) {
        this.placeName = placeName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GarbageQualityPointEntity that = (GarbageQualityPointEntity) o;

        if (id != that.id) return false;
        if (createId != null ? !createId.equals(that.createId) : that.createId != null) return false;
        if (createTime != null ? !createTime.equals(that.createTime) : that.createTime != null) return false;
        if (updateId != null ? !updateId.equals(that.updateId) : that.updateId != null) return false;
        if (updateTime != null ? !updateTime.equals(that.updateTime) : that.updateTime != null) return false;
        if (empty != null ? !empty.equals(that.empty) : that.empty != null) return false;
        if (noQualified != null ? !noQualified.equals(that.noQualified) : that.noQualified != null) return false;
        if (qualified != null ? !qualified.equals(that.qualified) : that.qualified != null) return false;
        if (placeId != null ? !placeId.equals(that.placeId) : that.placeId != null) return false;
        if (type != null ? !type.equals(that.type) : that.type != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (createId != null ? createId.hashCode() : 0);
        result = 31 * result + (createTime != null ? createTime.hashCode() : 0);
        result = 31 * result + (updateId != null ? updateId.hashCode() : 0);
        result = 31 * result + (updateTime != null ? updateTime.hashCode() : 0);
        result = 31 * result + (empty != null ? empty.hashCode() : 0);
        result = 31 * result + (noQualified != null ? noQualified.hashCode() : 0);
        result = 31 * result + (qualified != null ? qualified.hashCode() : 0);
        result = 31 * result + (placeId != null ? placeId.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        return result;
    }
}
