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

    /**
     * 积分排行榜
     * @param pageNo
     * @param pageSize
     * @param name
     * @param phone
     * @param request
     * @param cityId
     * @param countryId
     * @param townId
     * @param villageId
     * @param orderBys
     * @return
     */
    @RequestMapping(value = "getPointRankList", method = RequestMethod.GET)
    public ResponseData getPointRankList(Integer pageNo, Integer pageSize, String name, String phone, HttpServletRequest request,
                                         Integer cityId, Integer countryId, Integer townId, Integer villageId, String[] orderBys){
        String jwt = request.getHeader("Authorization").split(" ")[1];
        Assert.state(pageNo !=null && pageNo> 0, "请输入页数");
        Assert.state(pageSize != null && pageSize> 0, "输入显示的size");
        ResponseData responseData =garbageUserPointService.getPointRankList(pageNo, pageSize, name, phone, jwt, cityId, countryId, townId, villageId, orderBys);
        return responseData;
    }

    /**
     * 主页红黑榜
     * @param request
     * @return
     */
    @RequestMapping(value = "redAndBlackRank", method = RequestMethod.GET)
    public ResponseData redAndBlackRank(HttpServletRequest request){
        String jwt = request.getHeader("Authorization").split(" ")[1];
        ResponseData responseData =garbageUserPointService.redAndBlackRank(jwt);
        return responseData;
    }

    /**
     * 获取每个地区设定的合格，不合格，空桶 对应积分
     * @param placeId
     * @param type
     * @return
     */
    @RequestMapping(value = "findPlacePointByPlaceId", method = RequestMethod.GET)
    public ResponseData findPlacePointByPlaceId(Integer pageNo, Integer pageSize, Integer type, HttpServletRequest request, String search, Long cityId, Long countyId, Long townId, Long placeId){
        String jwt = request.getHeader("Authorization").split(" ")[1];
        ResponseData responseData =garbageUserPointService.findPlacePointByPlaceId(pageNo, pageSize,type, jwt, search, cityId, countyId, townId, placeId);
        return responseData;
    }
}
