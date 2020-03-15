package com.jin.env.garbage.dto.garbage;

public class DataComparisonAndAnalysisDto {

    private String placeName;
    private Double weight;
    private Integer partInCount;
    private Integer total;
    private Double participationRate;
    private Double qualityRate;
    private Double notQualityRate;
    private Double emptyRate;

    public String getPlaceName() {
        return placeName;
    }

    public void setPlaceName(String placeName) {
        this.placeName = placeName;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public Integer getPartInCount() {
        return partInCount;
    }

    public void setPartInCount(Integer partInCount) {
        this.partInCount = partInCount;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public Double getParticipationRate() {
        return participationRate;
    }

    public void setParticipationRate(Double participationRate) {
        this.participationRate = participationRate;
    }

    public Double getQualityRate() {
        return qualityRate;
    }

    public void setQualityRate(Double qualityRate) {
        this.qualityRate = qualityRate;
    }

    public Double getNotQualityRate() {
        return notQualityRate;
    }

    public void setNotQualityRate(Double notQualityRate) {
        this.notQualityRate = notQualityRate;
    }

    public Double getEmptyRate() {
        return emptyRate;
    }

    public void setEmptyRate(Double emptyRate) {
        this.emptyRate = emptyRate;
    }
}
