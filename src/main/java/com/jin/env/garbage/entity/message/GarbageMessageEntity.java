package com.jin.env.garbage.entity.message;

import com.jin.env.garbage.entity.base.BaseEntity;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Objects;

@Entity
@Table(name = "garbage_message", schema = "garbage_db", catalog = "")
public class GarbageMessageEntity  extends BaseEntity{
    private String msgContent;
    private String phone;
    private String placeName;
    private String date;
    private Integer type;
    private String quality;
    private Integer status;
    private String returnMessage;
    private String requestId;
    private String bizId;
    private String code;
    private Long villageId;
    private Integer score;
    private Integer sumScore;
    private Integer fromType;

    @Basic
    @Column(name = "msg_content")
    public String getMsgContent() {
        return msgContent;
    }

    public void setMsgContent(String msgContent) {
        this.msgContent = msgContent;
    }

    @Basic
    @Column(name = "phone")
    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    @Basic
    @Column(name = "place_name")
    public String getPlaceName() {
        return placeName;
    }

    public void setPlaceName(String placeName) {
        this.placeName = placeName;
    }

    @Basic
    @Column(name = "date")
    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    @Basic
    @Column(name = "type")
    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    @Basic
    @Column(name = "quality")
    public String getQuality() {
        return quality;
    }

    public void setQuality(String quality) {
        this.quality = quality;
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
    @Column(name = "return_message")
    public String getReturnMessage() {
        return returnMessage;
    }

    public void setReturnMessage(String returnMessage) {
        this.returnMessage = returnMessage;
    }

    @Basic
    @Column(name = "request_id")
    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    @Basic
    @Column(name = "biz_id")
    public String getBizId() {
        return bizId;
    }

    public void setBizId(String bizId) {
        this.bizId = bizId;
    }

    @Basic
    @Column(name = "code")
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @Basic
    @Column(name = "village_id")
    public Long getVillageId() {
        return villageId;
    }

    public void setVillageId(Long villageId) {
        this.villageId = villageId;
    }

    @Basic
    @Column(name = "from_type")
    public Integer getFromType() {
        return fromType;
    }

    public void setFromType(Integer fromType) {
        this.fromType = fromType;
    }

    @Basic
    @Column(name = "score")
    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    @Basic
    @Column(name = "sum_score")
    public Integer getSumScore() {
        return sumScore;
    }

    public void setSumScore(Integer sumScore) {
        this.sumScore = sumScore;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GarbageMessageEntity that = (GarbageMessageEntity) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(createId, that.createId) &&
                Objects.equals(createTime, that.createTime) &&
                Objects.equals(updateId, that.updateId) &&
                Objects.equals(updateTime, that.updateTime) &&
                Objects.equals(msgContent, that.msgContent) &&
                Objects.equals(phone, that.phone) &&
                Objects.equals(placeName, that.placeName) &&
                Objects.equals(date, that.date) &&
                Objects.equals(type, that.type) &&
                Objects.equals(quality, that.quality) &&
                Objects.equals(status, that.status) &&
                Objects.equals(returnMessage, that.returnMessage) &&
                Objects.equals(requestId, that.requestId) &&
                Objects.equals(bizId, that.bizId) &&
                Objects.equals(code, that.code) &&
                Objects.equals(villageId, that.villageId);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, createId, createTime, updateId, updateTime, msgContent, phone, placeName, date, type, quality, status, returnMessage, requestId, bizId, code, villageId);
    }
}
