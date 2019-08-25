package com.jin.env.garbage.entity.user;

import com.jin.env.garbage.entity.base.BaseEntity;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "garbage_role_community", schema = "garbage_db", catalog = "")
public class GarbageRoleCommunityEntity  extends BaseEntity{
    private int id;
    private Integer roleId;
    private Integer communityId;


    @Basic
    @Column(name = "role_id")
    public Integer getRoleId() {
        return roleId;
    }

    public void setRoleId(Integer roleId) {
        this.roleId = roleId;
    }

    @Basic
    @Column(name = "community_id")
    public Integer getCommunityId() {
        return communityId;
    }

    public void setCommunityId(Integer communityId) {
        this.communityId = communityId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GarbageRoleCommunityEntity that = (GarbageRoleCommunityEntity) o;
        return id == that.id &&
                Objects.equals(roleId, that.roleId) &&
                Objects.equals(communityId, that.communityId);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, roleId, communityId);
    }
}
