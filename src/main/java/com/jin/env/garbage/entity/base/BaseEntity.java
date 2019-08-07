package com.jin.env.garbage.entity.base;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.Column;
import javax.persistence.EntityListeners;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

/**
 * Created by wangLei on 2018/3/22.
 */

@MappedSuperclass
@EntityListeners({EntityListener.class})
public abstract class BaseEntity extends IdEntity{

    protected Integer createId;
    protected Long createTime;
    protected Integer updateId;
    protected Long updateTime;

    @JsonIgnore
    @Column(name="create_id",length=13,updatable=false)
    public Integer getCreateId(){
        return createId;
    }
    public void setCreateId(Integer createId){
        this.createId=createId;
    }
    @JsonIgnore
    @Column(name="create_time", length=13, updatable=false)
    public Long getCreateTime(){
        return createTime;
    }
    public void setCreateTime(Long createTime){
        this.createTime=createTime;
    }
    @JsonIgnore
    @Column(name="update_id",length=13)
    public Integer getUpdateId(){
        return updateId;
    }
    public void setUpdateId(Integer updateId){
        this.updateId=updateId;
    }
    @JsonIgnore
    @Column(name="update_time",length=13)
    public Long getUpdateTime(){
        return updateTime;
    }
    public void setUpdateTime(Long updateTime){
        this.updateTime=updateTime;
    }
    @Transient
    public boolean isEmptyString(Object s){
        return (s==null)||(s.toString().trim().length()==0)||s.toString().trim().equalsIgnoreCase("null");
    }
}
