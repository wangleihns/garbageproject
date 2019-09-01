package com.jin.env.garbage.dto.position;

public class UserPositionDto {
    private Integer id;
    private Integer emptyPoint;
    private Integer qualitedPoint;
    private Integer noQualitedPoint;
    private String provinceName;
    private String cityName;
    private String countyName;
    private String townName;
    private String villageName;
    private String communityName;
    private String fromType;//来源农村 还是小区
    private Long placeId;

    public UserPositionDto() {
    }

    public UserPositionDto(Long placeId, String provinceName, String cityName, String countyName, String townName, String villageName) {
        this.placeId = placeId;
        this.provinceName = provinceName;
        this.cityName = cityName;
        this.countyName = countyName;
        this.townName = townName;
        this.villageName = villageName;
    }

    public UserPositionDto(Integer placeId, String provinceName, String cityName, String countyName, String communityName) {
        this.placeId = placeId.longValue();
        this.provinceName = provinceName;
        this.cityName = cityName;
        this.countyName = countyName;
        this.communityName = communityName;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getEmptyPoint() {
        return emptyPoint;
    }

    public void setEmptyPoint(Integer emptyPoint) {
        this.emptyPoint = emptyPoint;
    }

    public Integer getQualitedPoint() {
        return qualitedPoint;
    }

    public void setQualitedPoint(Integer qualitedPoint) {
        this.qualitedPoint = qualitedPoint;
    }

    public Integer getNoQualitedPoint() {
        return noQualitedPoint;
    }

    public void setNoQualitedPoint(Integer noQualitedPoint) {
        this.noQualitedPoint = noQualitedPoint;
    }

    public String getProvinceName() {
        return provinceName;
    }

    public void setProvinceName(String provinceName) {
        this.provinceName = provinceName;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public String getCountyName() {
        return countyName;
    }

    public void setCountyName(String countyName) {
        this.countyName = countyName;
    }

    public String getTownName() {
        return townName;
    }

    public void setTownName(String townName) {
        this.townName = townName;
    }

    public String getVillageName() {
        return villageName;
    }

    public void setVillageName(String villageName) {
        this.villageName = villageName;
    }

    public String getCommunityName() {
        return communityName;
    }

    public void setCommunityName(String communityName) {
        this.communityName = communityName;
    }

    public String getFromType() {
        return fromType;
    }

    public void setFromType(String fromType) {
        this.fromType = fromType;
    }

    public Long getPlaceId() {
        return placeId;
    }

    public void setPlaceId(Long placeId) {
        this.placeId = placeId;
    }
}
