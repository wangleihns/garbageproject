package com.jin.env.garbage.entity.fold;

import com.jin.env.garbage.entity.base.BaseEntity;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Objects;

@Entity
@Table(name = "garbage_fold", schema = "garbage_db", catalog = "")
public class GarbageFoldEntity extends BaseEntity{
    private Integer userId;
    private Integer approverId;
    private Integer result;
    private String remark;
    private Integer score;


    @Basic
    @Column(name = "user_id")
    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer useId) {
        this.userId = useId;
    }

    @Basic
    @Column(name = "approver_id")
    public Integer getApproverId() {
        return approverId;
    }

    public void setApproverId(Integer approverId) {
        this.approverId = approverId;
    }

    @Basic
    @Column(name = "result")
    public Integer getResult() {
        return result;
    }

    public void setResult(Integer result) {
        this.result = result;
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
    @Column(name = "score")
    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GarbageFoldEntity that = (GarbageFoldEntity) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(createId, that.createId) &&
                Objects.equals(createTime, that.createTime) &&
                Objects.equals(updateTime, that.updateTime) &&
                Objects.equals(updateId, that.updateId) &&
                Objects.equals(userId, that.userId) &&
                Objects.equals(approverId, that.approverId) &&
                Objects.equals(result, that.result) &&
                Objects.equals(remark, that.remark);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, createId, createTime, updateTime, updateId, userId, approverId, result, remark);
    }
}
