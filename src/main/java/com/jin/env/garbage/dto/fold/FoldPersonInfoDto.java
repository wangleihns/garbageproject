package com.jin.env.garbage.dto.fold;

public class FoldPersonInfoDto {
    private String name;
    private String placeName;
    private String roleName;
    private String foldResult;
    private String garbageResult;
    private Double kitchenWeight;
    private Double otherWeight;
    private Double recycleWeight;

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

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getFoldResult() {
        return foldResult;
    }

    public void setFoldResult(String foldResult) {
        this.foldResult = foldResult;
    }

    public String getGarbageResult() {
        return garbageResult;
    }

    public void setGarbageResult(String garbageResult) {
        this.garbageResult = garbageResult;
    }

    public Double getKitchenWeight() {
        return kitchenWeight;
    }

    public void setKitchenWeight(Double kitchenWeight) {
        this.kitchenWeight = kitchenWeight;
    }

    public Double getOtherWeight() {
        return otherWeight;
    }

    public void setOtherWeight(Double otherWeight) {
        this.otherWeight = otherWeight;
    }

    public Double getRecycleWeight() {
        return recycleWeight;
    }

    public void setRecycleWeight(Double recycleWeight) {
        this.recycleWeight = recycleWeight;
    }
}
