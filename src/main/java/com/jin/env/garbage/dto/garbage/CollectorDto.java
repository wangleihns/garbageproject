package com.jin.env.garbage.dto.garbage;

public class CollectorDto {
    private String collecctorName;
    private String collectorPhone;
    private String address;
    private String date;
    private Integer userCount;
    private Double garbageWeight;

    public String getCollecctorName() {
        return collecctorName;
    }

    public void setCollecctorName(String collecctorName) {
        this.collecctorName = collecctorName;
    }

    public String getCollectorPhone() {
        return collectorPhone;
    }

    public void setCollectorPhone(String collectorPhone) {
        this.collectorPhone = collectorPhone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Integer getUserCount() {
        return userCount;
    }

    public void setUserCount(Integer userCount) {
        this.userCount = userCount;
    }

    public Double getGarbageWeight() {
        return garbageWeight;
    }

    public void setGarbageWeight(Double garbageWeight) {
        this.garbageWeight = garbageWeight;
    }
}
