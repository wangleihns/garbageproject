package com.jin.env.garbage.dto.user;

public class UserDto {
    private Integer userId;
    private String username;

    public UserDto() {
    }

    public UserDto(Integer userId, String username) {
        this.userId = userId;
        this.username = username;
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
}
