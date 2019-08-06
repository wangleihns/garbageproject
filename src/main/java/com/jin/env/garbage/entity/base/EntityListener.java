package com.jin.env.garbage.entity.base;



import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

/**
 * Created by wangLei on 2018/3/22.
 */
public class EntityListener {

    @PrePersist
    public void prePersist(BaseEntity ae) throws Exception{
        ae.setCreateTime(System.currentTimeMillis());
        ae.setUpdateTime(System.currentTimeMillis());
        ae.setCreateId(id());
        ae.setUpdateId(id());
    }
    @PreUpdate
    public void preUpdate(BaseEntity ae){
        try {
            ae.setUpdateId(id());
        } catch (Exception e) {
            ae.setUpdateId(0);
        }
        ae.setUpdateTime(System.currentTimeMillis());
    }
    private Integer id() throws Exception{
        return 0;
    }
}
