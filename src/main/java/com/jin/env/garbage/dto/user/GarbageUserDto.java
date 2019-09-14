package com.jin.env.garbage.dto.user;

import com.jin.env.garbage.entity.image.GarbageImageEntity;
import com.jin.env.garbage.entity.user.GarbageUserEntity;

import java.util.List;

public class GarbageUserDto  extends CollectorInfoDto{
    private GarbageUserEntity garbageUserEntity;
    private String headerImage;
    private String qRCode;

    private List<String> eNos;
    private String roleName;
    private String identityType;

    private String createDate;

    private Integer point;

    private String belongTown;

    public String getBelongTown() {
        return belongTown;
    }

    public void setBelongTown(String belongTown) {
        this.belongTown = belongTown;
    }

    public String getCreateDate() {
        return createDate;
    }

    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }

    public String getIdentityType() {
        return identityType;
    }

    public void setIdentityType(String identityType) {
        this.identityType = identityType;
    }

    public Integer getPoint() {
        return point;
    }

    public void setPoint(Integer point) {
        this.point = point;
    }

    public GarbageUserEntity getGarbageUserEntity() {
        return garbageUserEntity;
    }

    public void setGarbageUserEntity(GarbageUserEntity garbageUserEntity) {
        this.garbageUserEntity = garbageUserEntity;
    }

    public String getHeaderImage() {
        return headerImage;
    }

    public void setHeaderImage(String headerImage) {
        this.headerImage = headerImage;
    }

    public String getqRCode() {
        return qRCode;
    }

    public void setqRCode(String qRCode) {
        this.qRCode = qRCode;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public List<String> geteNos() {
        return eNos;
    }

    public void seteNos(List<String> eNos) {
        this.eNos = eNos;
    }
}
