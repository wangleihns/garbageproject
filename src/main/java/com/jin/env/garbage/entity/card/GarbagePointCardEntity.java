package com.jin.env.garbage.entity.card;

import com.jin.env.garbage.entity.base.BaseEntity;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Objects;

@Entity
@Table(name = "garbage_point_card", schema = "garbage_db", catalog = "")
public class GarbagePointCardEntity extends BaseEntity{
    private Integer userId;
    private String pointCard;


    @Basic
    @Column(name = "user_id")
    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    @Basic
    @Column(name = "point_card")
    public String getPointCard() {
        return pointCard;
    }

    public void setPointCard(String pointCard) {
        this.pointCard = pointCard;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GarbagePointCardEntity that = (GarbagePointCardEntity) o;
        return id == that.id &&
                Objects.equals(createTime, that.createTime) &&
                Objects.equals(updateTime, that.updateTime) &&
                Objects.equals(updateId, that.updateId) &&
                Objects.equals(userId, that.userId) &&
                Objects.equals(pointCard, that.pointCard);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, createTime, updateTime, updateId, userId, pointCard);
    }
}
