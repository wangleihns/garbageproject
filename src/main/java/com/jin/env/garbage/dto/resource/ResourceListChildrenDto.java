package com.jin.env.garbage.dto.resource;



import java.util.ArrayList;
import java.util.List;

public class ResourceListChildrenDto {
    private String id;
    private String name;
    private String code;
    private String icon;
    private String path;
    private Boolean noDropdown;
    private Boolean isEnabled;
    private String parentId;

    private List<String> children = new ArrayList<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
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

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public List<String> getChildren() {
        return children;
    }

    public void setChildren(List<String> children) {
        this.children = children;
    }
}
