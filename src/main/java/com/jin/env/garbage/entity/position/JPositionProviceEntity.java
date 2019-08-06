package com.jin.env.garbage.entity.position;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "j_position_provice", schema = "garbage_db", catalog = "")
public class JPositionProviceEntity {
    private int id;
    private int proviceId;
    private String proviceName;

    @Id
    @Column(name = "id")
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Basic
    @Column(name = "provice_id")
    public int getProviceId() {
        return proviceId;
    }

    public void setProviceId(int proviceId) {
        this.proviceId = proviceId;
    }

    @Basic
    @Column(name = "provice_name")
    public String getProviceName() {
        return proviceName;
    }

    public void setProviceName(String proviceName) {
        this.proviceName = proviceName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JPositionProviceEntity that = (JPositionProviceEntity) o;
        return id == that.id &&
                proviceId == that.proviceId &&
                Objects.equals(proviceName, that.proviceName);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, proviceId, proviceName);
    }
}
