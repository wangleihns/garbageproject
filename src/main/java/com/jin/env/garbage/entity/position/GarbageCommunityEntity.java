package com.jin.env.garbage.entity.position;

import com.jin.env.garbage.entity.base.BaseEntity;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "garbage_community", schema = "garbage_db", catalog = "")
public class GarbageCommunityEntity extends BaseEntity{
    private Integer provinceId;
    private Long cityId;
    private Long countryId;
    private Long townId;
    private String communityName;
    private Double lat;
    private Double lon;
    private String address;
    private Integer status;
    private String desc;
    private String fullAddress;

    private Integer sendMsg;
    @Basic
    @Column(name = "province_id")
    public Integer getProvinceId() {
        return provinceId;
    }

    public void setProvinceId(Integer provinceId) {
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
    @Column(name = "community_name")
    public String getCommunityName() {
        return communityName;
    }

    public void setCommunityName(String communityName) {
        this.communityName = communityName;
    }

    @Basic
    @Column(name = "lat")
    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    @Basic
    @Column(name = "lon")
    public Double getLon() {
        return lon;
    }

    public void setLon(Double lon) {
        this.lon = lon;
    }

    @Basic
    @Column(name = "address")
    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Basic
    @Column(name = "status")
    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    @Basic
    @Column(name = "`desc`")
    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    @Basic
    @Column(name = "full_address")
    public String getFullAddress() {
        return fullAddress;
    }

    public void setFullAddress(String fullAddress) {
        this.fullAddress = fullAddress;
    }

    @Basic
    @Column(name = "send_msg")
    public Integer getSendMsg() {
        return sendMsg;
    }

    public void setSendMsg(Integer sendMsg) {
        this.sendMsg = sendMsg;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GarbageCommunityEntity that = (GarbageCommunityEntity) o;
        return id == that.id &&
                Objects.equals(createId, that.createId) &&
                Objects.equals(createTime, that.createTime) &&
                Objects.equals(updateId, that.updateId) &&
                Objects.equals(updateTime, that.updateTime) &&
                Objects.equals(provinceId, that.provinceId) &&
                Objects.equals(cityId, that.cityId) &&
                Objects.equals(countryId, that.countryId) &&
                Objects.equals(communityName, that.communityName) &&
                Objects.equals(lat, that.lat) &&
                Objects.equals(lon, that.lon) &&
                Objects.equals(status, that.status);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, createId, createTime, updateId, updateTime, provinceId, cityId, countryId, communityName, lat, lon, status);
    }
}
