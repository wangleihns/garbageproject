package com.jin.env.garbage.controller.point;


import com.jin.env.garbage.service.point.GarbageUserPointService;
import com.jin.env.garbage.utils.ResponseData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping(value = "api/v1/point/")
public class GarbageUserPointController {
    @Autowired
    private GarbageUserPointService garbageUserPointService;

    @RequestMapping(value = "getPointRankList", method = RequestMethod.GET)
    public ResponseData getPointRankList(Integer pageNo, Integer pageSize, String name, String phone, HttpServletRequest request,
                                         Integer provinceId, Integer cityId, Integer countryId, Integer townId, Integer villageId, String[] orderBys){
        String jwt = request.getHeader("Authorization").split(": ")[1];
        ResponseData responseData =garbageUserPointService.getPointRankList(pageNo, pageSize, name, phone, jwt, provinceId, cityId, countryId, townId, villageId, orderBys);
        return responseData;
    }
}
