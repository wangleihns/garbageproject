package com.jin.env.garbage.service.point;

import com.jin.env.garbage.dao.point.GarbageUserPointDao;
import com.jin.env.garbage.dao.user.GarbageUserDao;
import com.jin.env.garbage.entity.point.GarbageUserPointEntity;
import com.jin.env.garbage.entity.user.GarbageUserEntity;
import com.jin.env.garbage.jwt.JwtUtil;
import com.jin.env.garbage.utils.ResponseData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.Arrays;
import java.util.stream.Collectors;

@Service
public class GarbageUserPointService {
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private GarbageUserDao garbageUserDao;

    @Autowired
    private GarbageUserPointDao garbageUserPointDao;

    public ResponseData getPointRankList(Integer pageNo, Integer pageSize, String name, String phone, String jwt, Integer provinceId, Integer cityId, Integer countryId, Integer townId, Integer villageId, String[] orderBys) {
        Integer sub  = jwtUtil.getSubject(jwt);
        GarbageUserEntity userEntity = garbageUserDao.findById(sub).get();
        Integer fromType = userEntity.getFromType(); // 0农村  1小区
        Pageable pageable = PageRequest.of(pageNo - 1, pageSize, getPointRankListSort(orderBys));
        Page<GarbageUserPointEntity> page = garbageUserPointDao.findAll(new Specification<GarbageUserPointEntity>() {
            @Nullable
            @Override
            public Predicate toPredicate(Root<GarbageUserPointEntity> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {

                return null;
            }
        },pageable);
        return null;
    }

    private Sort getPointRankListSort(String[] orderBys){
        Sort sort = null;
        if (orderBys == null || orderBys.length == 0 ){
            sort = Sort.by("id").descending();
        }else {
            sort =   Sort.by(Arrays.stream(orderBys).map((it) -> {
                String[] items = it.split(";");
                String property = "";
                Sort.Direction direction = null;
                return new Sort.Order(direction, property);
            }).collect(Collectors.toList()));
        }
        return sort;
    }
}
