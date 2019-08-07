package com.jin.env.garbage.entity.user;

import com.jin.env.garbage.entity.base.BaseEntity;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "garbage_resource", schema = "garbage_db", catalog = "")
public class GarbageResourceEntity  extends BaseEntity{
    private String active;
    private String code;
    private Long depth;
    private String description;
    private String enTitle;
    private String icon;
    private String markName;
    private String name;
    private String path;
    private Long seq;
    private Long supId;
    private String type;
    private String url;
    private String version;
    private String ftType;


    @Basic
    @Column(name = "active")
    public String getActive() {
        return active;
    }

    public void setActive(String active) {
        this.active = active;
    }

    @Basic
    @Column(name = "code")
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @Basic
    @Column(name = "depth")
    public Long getDepth() {
        return depth;
    }

    public void setDepth(Long depth) {
        this.depth = depth;
    }

    @Basic
    @Column(name = "description")
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Basic
    @Column(name = "en_title")
    public String getEnTitle() {
        return enTitle;
    }

    public void setEnTitle(String enTitle) {
        this.enTitle = enTitle;
    }

    @Basic
    @Column(name = "icon")
    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    @Basic
    @Column(name = "mark_name")
    public String getMarkName() {
        return markName;
    }

    public void setMarkName(String markName) {
        this.markName = markName;
    }

    @Basic
    @Column(name = "name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Basic
    @Column(name = "path")
    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Basic
    @Column(name = "seq")
    public Long getSeq() {
        return seq;
    }

    public void setSeq(Long seq) {
        this.seq = seq;
    }

    @Basic
    @Column(name = "sup_id")
    public Long getSupId() {
        return supId;
    }

    public void setSupId(Long supId) {
        this.supId = supId;
    }

    @Basic
    @Column(name = "type")
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Basic
    @Column(name = "url")
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Basic
    @Column(name = "version")
    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @Basic
    @Column(name = "ft_type")
    public String getFtType() {
        return ftType;
    }

    public void setFtType(String ftType) {
        this.ftType = ftType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GarbageResourceEntity that = (GarbageResourceEntity) o;
        return id == that.id &&
                Objects.equals(createId, that.createId) &&
                Objects.equals(createTime, that.createTime) &&
                Objects.equals(updateId, that.updateId) &&
                Objects.equals(updateTime, that.updateTime) &&
                Objects.equals(active, that.active) &&
                Objects.equals(code, that.code) &&
                Objects.equals(depth, that.depth) &&
                Objects.equals(description, that.description) &&
                Objects.equals(enTitle, that.enTitle) &&
                Objects.equals(icon, that.icon) &&
                Objects.equals(markName, that.markName) &&
                Objects.equals(name, that.name) &&
                Objects.equals(path, that.path) &&
                Objects.equals(seq, that.seq) &&
                Objects.equals(supId, that.supId) &&
                Objects.equals(type, that.type) &&
                Objects.equals(url, that.url) &&
                Objects.equals(version, that.version) &&
                Objects.equals(ftType, that.ftType);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, createId, createTime, updateId, updateTime, active, code, depth, description, enTitle, icon, markName, name, path, seq, supId, type, url, version, ftType);
    }
}
