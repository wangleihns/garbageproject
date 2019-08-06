package com.jin.env.garbage.entity.position;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "j_position_county", schema = "garbage_db", catalog = "")
public class JPositionCountyEntity {
    private int id;
    private long cityId;
    private long countyId;
    private String countyName;

    @Id
    @Column(name = "id")
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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
    @Column(name = "county_id")
    public long getCountyId() {
        return countyId;
    }

    public void setCountyId(long countyId) {
        this.countyId = countyId;
    }

    @Basic
    @Column(name = "county_name")
    public String getCountyName() {
        return countyName;
    }

    public void setCountyName(String countyName) {
        this.countyName = countyName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JPositionCountyEntity that = (JPositionCountyEntity) o;
        return id == that.id &&
                cityId == that.cityId &&
                countyId == that.countyId &&
                Objects.equals(countyName, that.countyName);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, cityId, countyId, countyName);
    }
}
