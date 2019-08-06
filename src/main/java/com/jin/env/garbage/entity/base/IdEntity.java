package com.jin.env.garbage.entity.base;

import javax.persistence.*;
import javax.validation.groups.Default;
import java.io.Serializable;

/**
 * Created by wangLei on 2018/3/22.
 */
@MappedSuperclass
public abstract class IdEntity implements Serializable {
    private static final long serialVersionUID=2498902831272177631L;
    protected Long id;
    @Id
    @GeneratedValue(generator="SEQUENCE", strategy= GenerationType.TABLE)
    @TableGenerator(name="SEQUENCE", table="garbage_table_sequence", initialValue=10, allocationSize=1)
    public Long getId(){
        return id;
    }
    public void setId(Long id){
        this.id=id;
    }

    /** 设置数据有效性* */
    public enum ActiveType{
        DISABLE,ENABLE
    }
    public static abstract interface Update extends Default {
    }
    public static abstract interface Save extends Default {
    }
}
