package com.jin.env.garbage.entity.remark;

import com.jin.env.garbage.entity.base.BaseEntity;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Objects;

@Entity
@Table(name = "garbage_remark_again_record", schema = "garbage_db", catalog = "")
public class GarbageRemarkAgainRecordEntity extends BaseEntity{

    private Integer oldQuality;
    private Integer oldPoint;
    private Integer newQuality;
    private Integer newPoint;
    private String remark;
    private Integer busId;
    private String sourceName;


    @Basic
    @Column(name = "old_quality")
    public Integer getOldQuality() {
        return oldQuality;
    }

    public void setOldQuality(Integer oldQuality) {
        this.oldQuality = oldQuality;
    }

    @Basic
    @Column(name = "old_point")
    public Integer getOldPoint() {
        return oldPoint;
    }

    public void setOldPoint(Integer oldPoint) {
        this.oldPoint = oldPoint;
    }

    @Basic
    @Column(name = "new_quality")
    public Integer getNewQuality() {
        return newQuality;
    }

    public void setNewQuality(Integer newQuality) {
        this.newQuality = newQuality;
    }

    @Basic
    @Column(name = "new_point")
    public Integer getNewPoint() {
        return newPoint;
    }

    public void setNewPoint(Integer newPoint) {
        this.newPoint = newPoint;
    }

    @Basic
    @Column(name = "remark")
    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    @Basic
    @Column(name = "bus_id")
    public Integer getBusId() {
        return busId;
    }

    public void setBusId(Integer busId) {
        this.busId = busId;
    }

    @Basic
    @Column(name = "source_name")
    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GarbageRemarkAgainRecordEntity that = (GarbageRemarkAgainRecordEntity) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(createId, that.createId) &&
                Objects.equals(createTime, that.createTime) &&
                Objects.equals(updateId, that.updateId) &&
                Objects.equals(updateTime, that.updateTime) &&
                Objects.equals(oldQuality, that.oldQuality) &&
                Objects.equals(oldPoint, that.oldPoint) &&
                Objects.equals(newQuality, that.newQuality) &&
                Objects.equals(newPoint, that.newPoint) &&
                Objects.equals(remark, that.remark) &&
                Objects.equals(busId, that.busId) &&
                Objects.equals(sourceName, that.sourceName);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, createId, createTime, updateId, updateTime, oldQuality, oldPoint, newQuality, newPoint, remark, busId, sourceName);
    }
}
