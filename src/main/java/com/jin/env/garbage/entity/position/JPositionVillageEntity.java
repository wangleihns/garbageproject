package com.jin.env.garbage.entity.position;

import com.jin.env.garbage.entity.base.BaseEntity;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "j_position_village", schema = "garbage_db", catalog = "")
public class JPositionVillageEntity extends BaseEntity{
    private long townId;
    private long villageId;
    private String villageName;


    @Basic
    @Column(name = "town_id")
    public long getTownId() {
        return townId;
    }

    public void setTownId(long townId) {
        this.townId = townId;
    }

    @Basic
    @Column(name = "village_id")
    public long getVillageId() {
        return villageId;
    }

    public void setVillageId(long villageId) {
        this.villageId = villageId;
    }

    @Basic
    @Column(name = "village_name")
    public String getVillageName() {
        return villageName;
    }

    public void setVillageName(String villageName) {
        this.villageName = villageName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JPositionVillageEntity that = (JPositionVillageEntity) o;
        return id == that.id &&
                townId == that.townId &&
                villageId == that.villageId &&
                Objects.equals(villageName, that.villageName);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, townId, villageId, villageName);
    }
}
