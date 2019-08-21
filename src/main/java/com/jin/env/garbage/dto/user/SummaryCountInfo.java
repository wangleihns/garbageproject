package com.jin.env.garbage.dto.user;

public class SummaryCountInfo {
    private Long userCount;
    private Long collectCount;
    private Long receivedCount;
    private Long pointRechargeCount;

    public Long getUserCount() {
        return userCount;
    }

    public void setUserCount(Long userCount) {
        this.userCount = userCount;
    }

    public Long getCollectCount() {
        return collectCount;
    }

    public void setCollectCount(Long collectCount) {
        this.collectCount = collectCount;
    }

    public Long getReceivedCount() {
        return receivedCount;
    }

    public void setReceivedCount(Long receivedCount) {
        this.receivedCount = receivedCount;
    }

    public Long getPointRechargeCount() {
        return pointRechargeCount;
    }

    public void setPointRechargeCount(Long pointRechargeCount) {
        this.pointRechargeCount = pointRechargeCount;
    }
}
