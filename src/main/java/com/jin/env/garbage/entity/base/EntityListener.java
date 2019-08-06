package com.jin.env.garbage.entity.base;



import com.jin.env.garbage.jwt.JwtUtil;
import com.jin.env.garbage.utils.SpringContextUtil;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.servlet.http.HttpServletRequest;

/**
 * Created by wangLei on 2018/3/22.
 */
public class EntityListener {

    public EntityListener() {
    }

    /**
     * @PrePersist- 在新实体持久化之前（添加到EntityManager）
        @PostPersist- 在数据库中存储新实体（在commit或期间flush）
        @PostLoad - 从数据库中检索实体后。
        @PreUpdate- 当一个实体被识别为被修改时EntityManager
        @PostUpdate- 更新数据库中的实体（在commit或期间flush）
        @PreRemove - 在EntityManager中标记要删除的实体时
        @PostRemove- 从数据库中删除实体（在commit或期间flush）
     * @param ae
     * @throws Exception
     */
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
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = requestAttributes.getRequest();
        String token = request.getHeader("Authorization");
        if (token == null){
            //硬件不传token
            //表示直接由硬件系统添加
            return 1;
        }
        String jwt = token.split(": ")[1];
        String sub = null;
        JwtUtil jwtUtil = (JwtUtil) SpringContextUtil.getBean("jwtUtil");
        try {
            sub = jwtUtil.getSubject(jwt);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
        return Integer.valueOf(sub);
    }
}
