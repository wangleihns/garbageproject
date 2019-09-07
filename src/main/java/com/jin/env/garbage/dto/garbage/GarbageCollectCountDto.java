package com.jin.env.garbage.dto.garbage;

/**
 * 收集统计报表分析
 */
public class GarbageCollectCountDto {
    private Long placeId;
    private String placeName;
    private Double garbageWeight;
    private Integer userCount;
    private Long count; //分类数量
    private Long participationCount;
    private String participationRate;//参与率
    private Integer qualityCount;
    private String qualityRate;//正确率
    private Integer notQualityCount;
    private String notQualityRate;//不合格率
    private Integer emptyCount;
    private String emptyRate;//空桶率

    private Integer garbageQuality;

    private Integer day;
    private Integer month;
    private Integer year;

    private String collectDate;

    public GarbageCollectCountDto() {
    }

    //分页dto
    public GarbageCollectCountDto(Long placeId, Double garbageWeight, Integer day, Integer month, Integer year, Long participationCount) {
        this.placeId = placeId;
        this.garbageWeight = garbageWeight;
        this.day = day;
        this.month = month;
        this.year = year;
        this.participationCount = participationCount;
    }

    public GarbageCollectCountDto(Long count, Integer garbageQuality) {
        this.count = count;
        this.garbageQuality = garbageQuality;
    }

    public GarbageCollectCountDto(Long placeId, Long count, Integer garbageQuality, Integer day, Integer month, Integer year) {
        this.placeId = placeId;
        this.count = count;
        this.garbageQuality = garbageQuality;
        this.day = day;
        this.month = month;
        this.year = year;
    }

    public Long getPlaceId() {
        return placeId;
    }

    public void setPlaceId(Long placeId) {
        this.placeId = placeId;
    }

    public String getPlaceName() {
        return placeName;
    }

    public void setPlaceName(String placeName) {
        this.placeName = placeName;
    }

    public Double getGarbageWeight() {
        return garbageWeight;
    }

    public void setGarbageWeight(Double garbageWeight) {
        this.garbageWeight = garbageWeight;
    }

    public Integer getUserCount() {
        return userCount;
    }

    public void setUserCount(Integer userCount) {
        this.userCount = userCount;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    public Long getParticipationCount() {
        return participationCount;
    }

    public void setParticipationCount(Long participationCount) {
        this.participationCount = participationCount;
    }

    public String getParticipationRate() {
        return participationRate;
    }

    public void setParticipationRate(String participationRate) {
        this.participationRate = participationRate;
    }

    public Integer getQualityCount() {
        return qualityCount;
    }

    public void setQualityCount(Integer qualityCount) {
        this.qualityCount = qualityCount;
    }

    public String getQualityRate() {
        return qualityRate;
    }

    public void setQualityRate(String qualityRate) {
        this.qualityRate = qualityRate;
    }

    public Integer getNotQualityCount() {
        return notQualityCount;
    }

    public void setNotQualityCount(Integer notQualityCount) {
        this.notQualityCount = notQualityCount;
    }

    public String getNotQualityRate() {
        return notQualityRate;
    }

    public void setNotQualityRate(String notQualityRate) {
        this.notQualityRate = notQualityRate;
    }

    public Integer getEmptyCount() {
        return emptyCount;
    }

    public void setEmptyCount(Integer emptyCount) {
        this.emptyCount = emptyCount;
    }

    public String getEmptyRate() {
        return emptyRate;
    }

    public void setEmptyRate(String emptyRate) {
        this.emptyRate = emptyRate;
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

    public Integer getGarbageQuality() {
        return garbageQuality;
    }

    public void setGarbageQuality(Integer garbageQuality) {
        this.garbageQuality = garbageQuality;
    }
}
