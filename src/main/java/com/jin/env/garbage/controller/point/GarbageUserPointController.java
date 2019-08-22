package com.jin.env.garbage.controller.point;


import com.jin.env.garbage.service.point.GarbageUserPointService;
import com.jin.env.garbage.utils.ResponseData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.util.Assert;
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
                                         Integer cityId, Integer countryId, Integer townId, Integer villageId, String[] orderBys){
        String jwt = request.getHeader("Authorization").split(": ")[1];
        Assert.state(pageNo !=null && pageNo> 0, "请输入页数");
        Assert.state(pageSize != null && pageSize> 0, "输入显示的size");
        ResponseData responseData =garbageUserPointService.getPointRankList(pageNo, pageSize, name, phone, jwt, cityId, countryId, townId, villageId, orderBys);
        return responseData;
    }

    @RequestMapping(value = "redAndBlackRank", method = RequestMethod.GET)
    public ResponseData redAndBlackRank(HttpServletRequest request){
        String jwt = request.getHeader("Authorization").split(": ")[1];
        ResponseData responseData =garbageUserPointService.redAndBlackRank(jwt);
        return responseData;
    }
}
