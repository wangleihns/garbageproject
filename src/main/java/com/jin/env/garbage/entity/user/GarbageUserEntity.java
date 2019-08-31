package com.jin.env.garbage.entity.user;

import com.jin.env.garbage.entity.base.BaseEntity;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.springframework.context.annotation.Lazy;

import javax.persistence.*;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "garbage_user", schema = "garbage_db", catalog = "")
@DynamicUpdate(value = true)
@DynamicInsert(value = true)
public class GarbageUserEntity extends BaseEntity{
    private String email;
    private String phone;
    private String loginName;
    private String password;
    private String name;
    private Integer status;
    private Boolean accountNonExpired;
    private Boolean credentialsNonExpired;
    private Boolean accountNonLocked;
    private Boolean enabled;
    private String userType;
    private String company;
    private String eNo;
    private String idCard;
    private Integer sex;
    private Boolean cleaner;
    private Integer provinceId;
    private String provinceName;
    private Long cityId;
    private String cityName;
    private Long countryId;
    private String countryName;
    private Long townId;
    private String townName;
    private Long villageId;
    private String villageName;
    private String address;
    private Integer communityId;

    private String communityName;

    private Integer fromType;

    private Set<GarbageRoleEntity> roles=new HashSet<>(); // 有序的关联对象集合

    private List<GarbageENoEntity> eNos;

    private Integer day;
    private Integer month;
    private Integer year;

    @Basic
    @Column(name = "email")
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Basic
    @Column(name = "phone")
    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    @Basic
    @Column(name = "login_name")
    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
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
    @Column(name = "password")
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Basic
    @Column(name = "status")
    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    @Basic
    @Column(name = "account_non_expired")
    public Boolean getAccountNonExpired() {
        return accountNonExpired;
    }

    public void setAccountNonExpired(Boolean accountNonExpired) {
        this.accountNonExpired = accountNonExpired;
    }

    @Basic
    @Column(name = "credentials_non_expired")
    public Boolean getCredentialsNonExpired() {
        return credentialsNonExpired;
    }

    public void setCredentialsNonExpired(Boolean credentialsNonExpired) {
        this.credentialsNonExpired = credentialsNonExpired;
    }

    @Basic
    @Column(name = "account_non_locked")
    public Boolean getAccountNonLocked() {
        return accountNonLocked;
    }

    public void setAccountNonLocked(Boolean accountNonLocked) {
        this.accountNonLocked = accountNonLocked;
    }

    @Basic
    @Column(name = "enabled")
    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    @Basic
    @Column(name = "user_type")
    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    @Basic
    @Column(name = "company")
    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    @Basic
    @Column(name = "e_no")
    public String geteNo() {
        return eNo;
    }

    public void seteNo(String eNo) {
        this.eNo = eNo;
    }

    @Basic
    @Column(name = "id_card")
    public String getIdCard() {
        return idCard;
    }

    public void setIdCard(String idCard) {
        this.idCard = idCard;
    }

    @Basic
    @Column(name = "sex")
    public Integer getSex() {
        return sex;
    }

    public void setSex(Integer sex) {
        this.sex = sex;
    }

    @Basic
    @Column(name = "cleaner")
    public Boolean getCleaner() {
        return cleaner;
    }

    public void setCleaner(Boolean cleaner) {
        this.cleaner = cleaner;
    }

    @Basic
    @Column(name = "province_id")
    public Integer getProvinceId() {
        return provinceId;
    }

    public void setProvinceId(Integer provinceId) {
        this.provinceId = provinceId;
    }

    @Basic
    @Column(name = "province_name")
    public String getProvinceName() {
        return provinceName;
    }

    public void setProvinceName(String provinceName) {
        this.provinceName = provinceName;
    }

    @Basic
    @Column(name = "city_id")
    public Long getCityId() {
        return cityId;
    }

    public void setCityId(Long cityId) {
        this.cityId = cityId;
    }

    @Basic
    @Column(name = "city_name")
    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    @Basic
    @Column(name = "country_id")
    public Long getCountryId() {
        return countryId;
    }

    public void setCountryId(Long countryId) {
        this.countryId = countryId;
    }

    @Basic
    @Column(name = "country_name")
    public String getCountryName() {
        return countryName;
    }

    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }

    @Basic
    @Column(name = "town_id")
    public Long getTownId() {
        return townId;
    }

    public void setTownId(Long townId) {
        this.townId = townId;
    }

    @Basic
    @Column(name = "town_name")
    public String getTownName() {
        return townName;
    }

    public void setTownName(String townName) {
        this.townName = townName;
    }

    @Basic
    @Column(name = "village_id")
    public Long getVillageId() {
        return villageId;
    }

    public void setVillageId(Long villageId) {
        this.villageId = villageId;
    }

    @Basic
    @Column(name = "village_name")
    public String getVillageName() {
        return villageName;
    }

    public void setVillageName(String villageName) {
        this.villageName = villageName;
    }

    @Basic
    @Column(name = "address")
    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    // 多对多定义
    @ManyToMany
    @JoinTable(name="garbage_user_role", joinColumns={@JoinColumn(name="user_id")}, inverseJoinColumns={@JoinColumn(name="role_id")})
    // Fecth策略定义
    @Fetch(FetchMode.JOIN)
    // 集合按id排序
    @OrderBy("id ASC")
    public Set<GarbageRoleEntity> getRoles() {
        return roles;
    }

    public void setRoles(Set<GarbageRoleEntity> roles) {
        this.roles = roles;
    }

    @OneToMany
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    public List<GarbageENoEntity> geteNos() {
        return eNos;
    }

    public void seteNos(List<GarbageENoEntity> eNos) {
        this.eNos = eNos;
    }

    @Basic
    @Column(name = "community_id")
    public Integer getCommunityId() {
        return communityId;
    }

    public void setCommunityId(Integer communityId) {
        this.communityId = communityId;
    }

    @Basic
    @Column(name = "from_type")
    public Integer getFromType() {
        return fromType;
    }

    public void setFromType(Integer fromType) {
        this.fromType = fromType;
    }

    @Basic
    @Column(name = "day")
    public Integer getDay() {
        return day;
    }

    public void setDay(Integer day) {
        this.day = day;
    }

    @Basic
    @Column(name = "month")
    public Integer getMonth() {
        return month;
    }

    public void setMonth(Integer month) {
        this.month = month;
    }

    @Basic
    @Column(name = "year")
    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    @Basic
    @Column(name = "community_name")
    public String getCommunityName() {
        return communityName;
    }

    public void setCommunityName(String communityName) {
        this.communityName = communityName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GarbageUserEntity that = (GarbageUserEntity) o;
        return id == that.id &&
                sex == that.sex &&
                Objects.equals(createId, that.createId) &&
                Objects.equals(createTime, that.createTime) &&
                Objects.equals(updateId, that.updateId) &&
                Objects.equals(updateTime, that.updateTime) &&
                Objects.equals(email, that.email) &&
                Objects.equals(phone, that.phone) &&
                Objects.equals(loginName, that.loginName) &&
                Objects.equals(name, that.name) &&
                Objects.equals(status, that.status) &&
                Objects.equals(accountNonExpired, that.accountNonExpired) &&
                Objects.equals(credentialsNonExpired, that.credentialsNonExpired) &&
                Objects.equals(accountNonLocked, that.accountNonLocked) &&
                Objects.equals(enabled, that.enabled) &&
                Objects.equals(userType, that.userType) &&
                Objects.equals(company, that.company) &&
                Objects.equals(eNo, that.eNo) &&
                Objects.equals(idCard, that.idCard) &&
                Objects.equals(cleaner, that.cleaner) &&
                Objects.equals(provinceId, that.provinceId) &&
                Objects.equals(provinceName, that.provinceName) &&
                Objects.equals(cityId, that.cityId) &&
                Objects.equals(cityName, that.cityName) &&
                Objects.equals(countryId, that.countryId) &&
                Objects.equals(countryName, that.countryName) &&
                Objects.equals(townId, that.townId) &&
                Objects.equals(townName, that.townName) &&
                Objects.equals(villageId, that.villageId) &&
                Objects.equals(villageName, that.villageName) &&
                Objects.equals(address, that.address);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, createId, createTime, updateId, updateTime, email, phone, loginName, name, status, accountNonExpired, credentialsNonExpired, accountNonLocked, enabled, userType, company, eNo, idCard, sex, cleaner, provinceId, provinceName, cityId, cityName, countryId, countryName, townId, townName, villageId, villageName, address);
    }
}
