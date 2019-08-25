package com.jin.env.garbage.entity.user;

import com.jin.env.garbage.entity.base.BaseEntity;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "garbage_role_resource", schema = "garbage_db", catalog = "")
public class GarbageRoleResourceEntity  extends BaseEntity{
    private int id;
    private Integer roleId;
    private Integer resourceId;

    @Basic
    @Column(name = "role_id")
    public Integer getRoleId() {
        return roleId;
    }

    public void setRoleId(Integer roleId) {
        this.roleId = roleId;
    }

    @Basic
    @Column(name = "resource_id")
    public Integer getResourceId() {
        return resourceId;
    }

    public void setResourceId(Integer resourceId) {
        this.resourceId = resourceId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GarbageRoleResourceEntity that = (GarbageRoleResourceEntity) o;
        return id == that.id &&
                Objects.equals(roleId, that.roleId) &&
                Objects.equals(resourceId, that.resourceId);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, roleId, resourceId);
    }
}
