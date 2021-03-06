package com.jin.env.garbage.dto.user;

public class UserDto {
    protected Integer userId;
    protected String username;

    protected String phone;
    protected String address;
    protected String eNo;

    protected boolean isCollect;

    public UserDto() {
    }

    public UserDto(Integer userId, String username) {
        this.userId = userId;
        this.username = username;
    }

    public UserDto(Integer userId, String username, String phone, String address) {
        this.userId = userId;
        this.username = username;
        this.phone = phone;
        this.address = address;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String geteNo() {
        return eNo;
    }

    public void seteNo(String eNo) {
        this.eNo = eNo;
    }

    public boolean isCollect() {
        return isCollect;
    }

    public void setCollect(boolean collect) {
        isCollect = collect;
    }
}
