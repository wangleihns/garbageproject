package com.jin.env.garbage.dto.garbage;

public class UserCollectDataAnalysisDto {
    private Integer userId;
    private String name;
    private String placeName;
    private String address;
    private Long totalCount;
    private Double totalWeight;
    private Long qualityCount;
    private Long noQualityCount;
    private Long emptyCount;
    private String qualityRate;
    private Integer day;
    private Integer month;
    private Integer year;

    private Long count;
    private Integer qualityType;


    public UserCollectDataAnalysisDto() {
    }

    public UserCollectDataAnalysisDto(Integer userId, Integer qualityType, Long totalCount, Double totalWeight, Integer day, Integer month, Integer year) {
        this.userId = userId;
        this.totalCount = totalCount;
        this.qualityType = qualityType;
        this.totalWeight = totalWeight;
        this.day = day;
        this.month = month;
        this.year = year;
    }

    public UserCollectDataAnalysisDto(Integer userId,Long count, Integer day, Integer month, Integer year ) {
        this.userId = userId;
        this.day = day;
        this.month = month;
        this.year = year;
        this.count = count;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPlaceName() {
        return placeName;
    }

    public void setPlaceName(String placeName) {
        this.placeName = placeName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Long totalCount) {
        this.totalCount = totalCount;
    }

    public Double getTotalWeight() {
        return totalWeight;
    }

    public void setTotalWeight(Double totalWeight) {
        this.totalWeight = totalWeight;
    }

    public Long getQualityCount() {
        return qualityCount;
    }

    public void setQualityCount(Long qualityCount) {
        this.qualityCount = qualityCount;
    }

    public Long getNoQualityCount() {
        return noQualityCount;
    }

    public void setNoQualityCount(Long noQualityCount) {
        this.noQualityCount = noQualityCount;
    }

    public Long getEmptyCount() {
        return emptyCount;
    }

    public void setEmptyCount(Long emptyCount) {
        this.emptyCount = emptyCount;
    }

    public String getQualityRate() {
        return qualityRate;
    }

    public void setQualityRate(String qualityRate) {
        this.qualityRate = qualityRate;
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

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    public Integer getQualityType() {
        return qualityType;
    }

    public void setQualityType(Integer qualityType) {
        this.qualityType = qualityType;
    }
}
