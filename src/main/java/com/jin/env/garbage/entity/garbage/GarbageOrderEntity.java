package com.jin.env.garbage.entity.garbage;

import com.jin.env.garbage.entity.base.BaseEntity;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "garbage_order", schema = "garbage_db", catalog = "")
public class GarbageOrderEntity extends BaseEntity{
    private Integer orderNo;
    private String goodsName;
    private Integer goodsCount;
    private Double goodsPrice;
    private String collectorName;
    private String collectorPhone;
    private String receiver;
    private Long addTime;
    private Integer collectStatus;


    @Basic
    @Column(name = "order_no")
    public Integer getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(Integer orderNo) {
        this.orderNo = orderNo;
    }

    @Basic
    @Column(name = "goods_name")
    public String getGoodsName() {
        return goodsName;
    }

    public void setGoodsName(String goodsName) {
        this.goodsName = goodsName;
    }

    @Basic
    @Column(name = "goods_count")
    public Integer getGoodsCount() {
        return goodsCount;
    }

    public void setGoodsCount(Integer goodsCount) {
        this.goodsCount = goodsCount;
    }

    @Basic
    @Column(name = "goods_price")
    public Double getGoodsPrice() {
        return goodsPrice;
    }

    public void setGoodsPrice(Double goodsPrice) {
        this.goodsPrice = goodsPrice;
    }

    @Basic
    @Column(name = "collector_name")
    public String getCollectorName() {
        return collectorName;
    }

    public void setCollectorName(String collectorName) {
        this.collectorName = collectorName;
    }

    @Basic
    @Column(name = "collector_phone")
    public String getCollectorPhone() {
        return collectorPhone;
    }

    public void setCollectorPhone(String collectorPhone) {
        this.collectorPhone = collectorPhone;
    }

    @Basic
    @Column(name = "receiver")
    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    @Basic
    @Column(name = "add_time")
    public Long getAddTime() {
        return addTime;
    }

    public void setAddTime(Long addTime) {
        this.addTime = addTime;
    }

    @Basic
    @Column(name = "collect_status")
    public Integer getCollectStatus() {
        return collectStatus;
    }

    public void setCollectStatus(Integer collectStatus) {
        this.collectStatus = collectStatus;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GarbageOrderEntity that = (GarbageOrderEntity) o;
        return id == that.id &&
                Objects.equals(createId, that.createId) &&
                Objects.equals(createTime, that.createTime) &&
                Objects.equals(updateId, that.updateId) &&
                Objects.equals(updateTime, that.updateTime) &&
                Objects.equals(orderNo, that.orderNo) &&
                Objects.equals(goodsName, that.goodsName) &&
                Objects.equals(goodsCount, that.goodsCount) &&
                Objects.equals(goodsPrice, that.goodsPrice) &&
                Objects.equals(collectorName, that.collectorName) &&
                Objects.equals(collectorPhone, that.collectorPhone) &&
                Objects.equals(receiver, that.receiver) &&
                Objects.equals(addTime, that.addTime) &&
                Objects.equals(collectStatus, that.collectStatus);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, createId, createTime, updateId, updateTime, orderNo, goodsName, goodsCount, goodsPrice, collectorName, collectorPhone, receiver, addTime, collectStatus);
    }
}
