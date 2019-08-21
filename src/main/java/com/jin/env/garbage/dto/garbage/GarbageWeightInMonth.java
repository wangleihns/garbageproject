package com.jin.env.garbage.dto.garbage;

public class GarbageWeightInMonth {
    private Integer month;
    private Double weight;

    public GarbageWeightInMonth() {
    }

    public GarbageWeightInMonth(Integer month, Double weight) {
        this.month = month;
        this.weight = weight;
    }

    public Integer getMonth() {
        return month;
    }

    public void setMonth(Integer month) {
        this.month = month;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }
}
