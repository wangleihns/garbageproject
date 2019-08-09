package com.jin.env.garbage.dto.resource;

import com.jin.env.garbage.entity.user.GarbageResourceEntity;

import java.util.List;

public class ResourceListDto {
    private Integer id;
    private String name;
    private String code;
    private String icon;
    private String path;
    private Boolean noDropdown;
    private Boolean isEnabled;
    private List<GarbageResourceEntity> children;

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

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Boolean getNoDropdown() {
        return noDropdown;
    }

    public void setNoDropdown(Boolean noDropdown) {
        this.noDropdown = noDropdown;
    }

    public Boolean getEnabled() {
        return isEnabled;
    }

    public void setEnabled(Boolean enabled) {
        isEnabled = enabled;
    }

    public List<GarbageResourceEntity> getChildren() {
        return children;
    }

    public void setChildren(List<GarbageResourceEntity> children) {
        this.children = children;
    }
}
