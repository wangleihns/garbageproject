package com.jin.env.garbage.entity.position;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "j_position_city", schema = "garbage_db", catalog = "")
public class JPositionCityEntity {
    private int id;
    private int provinceId;
    private long cityId;
    private String cityName;

    @Id
    @Column(name = "id")
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Basic
    @Column(name = "province_id")
    public int getProvinceId() {
        return provinceId;
    }

    public void setProvinceId(int provinceId) {
        this.provinceId = provinceId;
    }

    @Basic
    @Column(name = "city_id")
    public long getCityId() {
        return cityId;
    }

    public void setCityId(long cityId) {
        this.cityId = cityId;
    }

    @Basic
    @Column(name = "city_name")
    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JPositionCityEntity that = (JPositionCityEntity) o;
        return id == that.id &&
                provinceId == that.provinceId &&
                cityId == that.cityId &&
                Objects.equals(cityName, that.cityName);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, provinceId, cityId, cityName);
    }
}
