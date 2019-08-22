package com.jin.env.garbage.dto.resource;

import java.util.List;

public class UserResourceDto {
    private Integer id;
    private String name;
    private String url;
    private String icon;
    private Integer suqId;
    private List<UserResourceDto> dtos;

    public UserResourceDto() {
    }

    public UserResourceDto(Integer id, String name, String url, String icon, Integer suqId) {
        this.id = id;
        this.name = name;
        this.url = url;
        this.icon = icon;
        this.suqId = suqId;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public List<UserResourceDto> getDtos() {
        return dtos;
    }

    public void setDtos(List<UserResourceDto> dtos) {
        this.dtos = dtos;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public Integer getSuqId() {
        return suqId;
    }

    public void setSuqId(Integer suqId) {
        this.suqId = suqId;
    }
}
