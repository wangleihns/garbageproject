package com.jin.env.garbage.dto.user;

public class UserDtoForLeader extends UserDto{
    private Boolean dangYuan;
    private Boolean cunMinDaiBiao;
    private Boolean streetCommentDaiBiao;
    private Boolean liangDaiBiaoYiWeiYuan;
    private Boolean cunLeader;
    private Boolean  cunZuLeader;
    private Boolean womenExeLeader;
    private String countyName;
    private String townName;
    private String villageName;

    public UserDtoForLeader(){}

    public UserDtoForLeader(Integer userId, String username, String phone, String address, Boolean dangYuan,
                            Boolean cunMinDaiBiao,Boolean streetCommentDaiBiao,Boolean liangDaiBiaoYiWeiYuan,
                            Boolean cunLeader,Boolean  cunZuLeader, Boolean womenExeLeader, String countyName,
                            String townName, String villageName){
        this.userId = userId;
        this.username = username;
        this.phone = phone;
        this.address = address;
        this.dangYuan = dangYuan;
        this.cunMinDaiBiao = cunMinDaiBiao;
        this.streetCommentDaiBiao = streetCommentDaiBiao;
        this.liangDaiBiaoYiWeiYuan = liangDaiBiaoYiWeiYuan;
        this.cunLeader = cunLeader;
        this.cunZuLeader = cunZuLeader;
        this.womenExeLeader = womenExeLeader;
        this.countyName = countyName;
        this.townName = townName;
        this.villageName = villageName;
    }

    public Boolean getDangYuan() {
        return dangYuan;
    }

    public void setDangYuan(Boolean dangYuan) {
        this.dangYuan = dangYuan;
    }

    public Boolean getCunMinDaiBiao() {
        return cunMinDaiBiao;
    }

    public void setCunMinDaiBiao(Boolean cunMinDaiBiao) {
        this.cunMinDaiBiao = cunMinDaiBiao;
    }

    public Boolean getStreetCommentDaiBiao() {
        return streetCommentDaiBiao;
    }

    public void setStreetCommentDaiBiao(Boolean streetCommentDaiBiao) {
        this.streetCommentDaiBiao = streetCommentDaiBiao;
    }

    public Boolean getLiangDaiBiaoYiWeiYuan() {
        return liangDaiBiaoYiWeiYuan;
    }

    public void setLiangDaiBiaoYiWeiYuan(Boolean liangDaiBiaoYiWeiYuan) {
        this.liangDaiBiaoYiWeiYuan = liangDaiBiaoYiWeiYuan;
    }

    public Boolean getCunLeader() {
        return cunLeader;
    }

    public void setCunLeader(Boolean cunLeader) {
        this.cunLeader = cunLeader;
    }

    public Boolean getCunZuLeader() {
        return cunZuLeader;
    }

    public void setCunZuLeader(Boolean cunZuLeader) {
        this.cunZuLeader = cunZuLeader;
    }

    public Boolean getWomenExeLeader() {
        return womenExeLeader;
    }

    public void setWomenExeLeader(Boolean womenExeLeader) {
        this.womenExeLeader = womenExeLeader;
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
}
