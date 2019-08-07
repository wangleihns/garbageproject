package com.jin.env.garbage.entity.position;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "j_position_province", schema = "garbage_db", catalog = "")
public class JPositionProvinceEntity {
    private int id;
    private int provinceId;
    private String provinceName;

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
    @Column(name = "province_name")
    public String getProvinceName() {
        return provinceName;
    }

    public void setProvinceName(String provinceName) {
        this.provinceName = provinceName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JPositionProvinceEntity that = (JPositionProvinceEntity) o;
        return id == that.id &&
                provinceId == that.provinceId &&
                Objects.equals(provinceName, that.provinceName);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, provinceId, provinceName);
    }
}
