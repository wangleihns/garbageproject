package com.jin.env.garbage.entity.position;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "j_position_town", schema = "garbage_db", catalog = "")
public class JPositionTownEntity {
    private int id;
    private long countyId;
    private long townId;
    private String townName;

    @Id
    @Column(name = "id")
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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
    @Column(name = "town_id")
    public long getTownId() {
        return townId;
    }

    public void setTownId(long townId) {
        this.townId = townId;
    }

    @Basic
    @Column(name = "town_name")
    public String getTownName() {
        return townName;
    }

    public void setTownName(String townName) {
        this.townName = townName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JPositionTownEntity that = (JPositionTownEntity) o;
        return id == that.id &&
                countyId == that.countyId &&
                townId == that.townId &&
                Objects.equals(townName, that.townName);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, countyId, townId, townName);
    }
}
