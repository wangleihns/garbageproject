package com.jin.env.garbage.dto.garbage;

public class UserCollectCountDto {
    private Long totalUser;
    private Long residentCount;
    private Long todayCount;
    private Double  partInRate;
    private Double rightRate;
    private Double totalWeight;

    public Long getTotalUser() {
        return totalUser;
    }

    public void setTotalUser(Long totalUser) {
        this.totalUser = totalUser;
    }

    public Long getResidentCount() {
        return residentCount;
    }

    public void setResidentCount(Long residentCount) {
        this.residentCount = residentCount;
    }

    public Long getTodayCount() {
        return todayCount;
    }

    public void setTodayCount(Long todayCount) {
        this.todayCount = todayCount;
    }

    public Double getPartInRate() {
        return partInRate;
    }

    public void setPartInRate(Double partInRate) {
        this.partInRate = partInRate;
    }

    public Double getRightRate() {
        return rightRate;
    }

    public void setRightRate(Double rightRate) {
        this.rightRate = rightRate;
    }

    public Double getTotalWeight() {
        return totalWeight;
    }

    public void setTotalWeight(Double totalWeight) {
        this.totalWeight = totalWeight;
    }
}
