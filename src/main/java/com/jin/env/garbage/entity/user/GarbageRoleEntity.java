package com.jin.env.garbage.entity.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.jin.env.garbage.entity.base.BaseEntity;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.springframework.context.annotation.Lazy;

import javax.persistence.*;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "garbage_role", schema = "garbage_db", catalog = "")
public class GarbageRoleEntity extends BaseEntity{
    private String roleCode;
    private String roleName;
    private String roleDesc;
    private Integer status;
    private Integer type;
    private Set<GarbageResourceEntity> knResourceEntitySet;
    @Basic
    @Column(name = "role_code")
    public String getRoleCode() {
        return roleCode;
    }

    public void setRoleCode(String roleCode) {
        this.roleCode = roleCode;
    }

    @Basic
    @Column(name = "role_name")
    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    @Basic
    @Column(name = "role_desc")
    public String getRoleDesc() {
        return roleDesc;
    }

    public void setRoleDesc(String roleDesc) {
        this.roleDesc = roleDesc;
    }

    @Basic
    @Column(name = "status")
    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    @ManyToMany @JoinTable(name="garbage_role_resource",joinColumns={@JoinColumn(name="role_id")},inverseJoinColumns={@JoinColumn(name="resource_id")})
    // Fecth策略定义
    @Fetch(FetchMode.SUBSELECT)
    // 集合按id排序
    @OrderBy("path")
    @JsonIgnore
    @Lazy
    public Set<GarbageResourceEntity> getKnResourceEntitySet() {
        return knResourceEntitySet;
    }

    public void setKnResourceEntitySet(Set<GarbageResourceEntity> knResourceEntitySet) {
        this.knResourceEntitySet = knResourceEntitySet;
    }
    @Basic
    @Column(name = "`type`")
    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GarbageRoleEntity that = (GarbageRoleEntity) o;
        return id == that.id &&
                Objects.equals(createId, that.createId) &&
                Objects.equals(createTime, that.createTime) &&
                Objects.equals(updateId, that.updateId) &&
                Objects.equals(updateTime, that.updateTime) &&
                Objects.equals(roleCode, that.roleCode) &&
                Objects.equals(roleName, that.roleName) &&
                Objects.equals(roleDesc, that.roleDesc) &&
                Objects.equals(status, that.status);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, createId, createTime, updateId, updateTime, roleCode, roleName, roleDesc, status);
    }
}
