package com.jin.env.garbage.dto.resource;

import java.util.List;

public class ResourceListLabelDto {
    private Integer id;
    private String icon;
    private String  label;
    private String path;
    private Integer seq;
    private Integer supId;
    private Integer active;
    private List<ResourceChildrenList> children;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Integer getActive() {
        return active;
    }

    public void setActive(Integer active) {
        this.active = active;
    }

    public Integer getSeq() {
        return seq;
    }

    public void setSeq(Integer seq) {
        this.seq = seq;
    }

    public Integer getSupId() {
        return supId;
    }

    public void setSupId(Integer supId) {
        this.supId = supId;
    }

    public List<ResourceChildrenList> getChildren() {
        return children;
    }

    public void setChildren(List<ResourceChildrenList> children) {
        this.children = children;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}

