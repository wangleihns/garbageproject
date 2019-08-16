package com.jin.env.garbage.dto.garbage;

public class CollectorDto {
    private Integer collectorId;
    private String collectorName;
    private String collectorPhone;
    private String address;
    private Long date;
    private Long userCount;
    private Double garbageWeight;
    private Integer day;
    private Integer month;
    private Integer year;
    private String collectDate;

    public CollectorDto() {
    }

    public CollectorDto(Integer collectorId, String collectorName, String collectorPhone, Long date, Long userCount,
                        Double garbageWeight, Integer day, Integer month, Integer year) {
        this.collectorId = collectorId;
        this.collectorName = collectorName;
        this.collectorPhone = collectorPhone;
        this.date = date;
        this.userCount = userCount;
        this.garbageWeight = garbageWeight;
        this.day = day;
        this.month = month;
        this.year = year;
    }

    public Integer getCollectorId() {
        return collectorId;
    }

    public void setCollectorId(Integer collectorId) {
        this.collectorId = collectorId;
    }

    public String getCollectorName() {
        return collectorName;
    }

    public void setCollectorName(String collectorName) {
        this.collectorName = collectorName;
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

    public Long getDate() {
        return date;
    }

    public void setDate(Long date) {
        this.date = date;
    }

    public Long getUserCount() {
        return userCount;
    }

    public void setUserCount(Long userCount) {
        this.userCount = userCount;
    }

    public Double getGarbageWeight() {
        return garbageWeight;
    }

    public void setGarbageWeight(Double garbageWeight) {
        this.garbageWeight = garbageWeight;
    }

    public Integer getDay() {
        return day;
    }

    public void setDay(Integer day) {
        this.day = day;
    }

    public Integer getMonth() {
        return month;
    }

    public void setMonth(Integer month) {
        this.month = month;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public String getCollectDate() {
        return collectDate;
    }

    public void setCollectDate(String collectDate) {
        this.collectDate = collectDate;
    }
}
