package com.jin.env.garbage.controller.garbage;

import com.jin.env.garbage.service.garbage.GarbageCollectorService;
import com.jin.env.garbage.utils.ResponseData;
import com.jin.env.garbage.utils.ResponsePageData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpRequest;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping(value = "api/v1/collector/")
public class GarbageCollectorController {
    private Logger logger = LoggerFactory.getLogger(GarbageCollectorController.class);

    @Autowired
    private GarbageCollectorService garbageCollectorService;

    /**
     * 人工上传垃圾信息
     * @param eNo
     * @param quality
     * @param weight
     * @param imageId
     * @return
     */
    @RequestMapping(value = "addGarbageByCollector", method = RequestMethod.POST)
    public ResponseData addGarbageByCollector(String eNo, String quality, Double weight, Integer imageId, HttpServletRequest request){
        String jwt = request.getHeader("Authorization").split(" ")[1];
        Assert.hasText(eNo, "请传入电子卡Id");
        Assert.state(weight != null, "上传垃圾重量");
        Assert.state(imageId != null, "上传图片id");
        Assert.hasText(quality, "请评价垃圾是否合格");
        ResponseData responseData = garbageCollectorService.addGarbageByCollector(eNo, quality, weight, imageId, jwt);
        return  responseData;
    }

    /**
     *  无人值守机器上传
     * @param eNo
     * @param weight
     * @param imageId
     * @param request
     * @return
     */
    @RequestMapping(value = "addGarbageByAuto", method = RequestMethod.POST)
    public ResponseData addGarbageByAuto(String eNo, Double weight, Integer imageId, HttpServletRequest request){
        String jwtArr[] = request.getHeader("Authorization").split(" ");
        String jwt = jwtArr[1];
        Assert.hasText(eNo, "请传入电子卡Id");
        Assert.state(weight != null, "上传垃圾重量");
        Assert.state(imageId != null, "上传图片id");
        ResponseData responseData = garbageCollectorService.addGarbageByAuto(eNo, weight, imageId, jwt);
        return  responseData;
    }


    /**
     * 小区垃圾回收管理
     * @param pageNo
     * @param pageSize
     * @param isCheck
     * @param weight
     * @param point
     * @param quality
     * @param garbageType
     * @param orderBys
     * @param request
     * @return
     */
    @RequestMapping(value = "communityGarbageList", method = RequestMethod.GET)
    public ResponseData communityGarbageList(Integer pageNo, Integer pageSize, Boolean isCheck, Double weight, Integer point,
                                             Integer quality, String type, String keyWord, Integer garbageType, String[] orderBys, HttpServletRequest request ,
                                             String startTime, String endTime, Integer communityId){
        String jwt = request.getHeader("Authorization").split(" ")[1];
        ResponsePageData responseData = garbageCollectorService.communityGarbageList(pageNo,pageSize, isCheck, weight, point, quality, type, keyWord, garbageType, jwt,  orderBys, startTime, endTime,communityId);
        return  responseData;
    }

    /**
     * 农村垃圾回收管理
     * @param pageNo
     * @param pageSize
     * @param isCheck
     * @param weight
     * @param point
     * @param quality
     * @param type
     * @param keyWord
     * @param garbageType
     * @param orderBys
     * @param request
     * @return
     */

    @RequestMapping(value = "villageGarbageList", method = RequestMethod.GET)
    public ResponseData villageGarbageList(Integer pageNo, Integer pageSize, Boolean isCheck, Double weight, Integer point,
                                             Integer quality, String type, String keyWord, Integer garbageType, String[] orderBys, HttpServletRequest request,
                                            String startTime, String endTime, Long cityId, Long countryId, Long townId, Long villageId){
        String jwt = request.getHeader("Authorization").split(" ")[1];
        ResponsePageData responseData = garbageCollectorService.villageGarbageList(pageNo,pageSize, isCheck, weight, point, quality,
                type, keyWord, garbageType, jwt, orderBys, startTime, endTime, cityId,countryId, townId, villageId);
        return  responseData;
    }

    /**
     *  用户获取小区列表
     * @param request
     * @param countyId
     * @return
     */
    @RequestMapping(value = "getCommunityListForUser", method = RequestMethod.GET)
    public ResponseData getCommunityListForUser(HttpServletRequest request, Long countyId){
        String jwt = request.getHeader("Authorization").split(" ")[1];
        ResponseData responseData = garbageCollectorService.getCommunityListForUser(countyId,jwt);
        return  responseData;
    }



    /**
     * 人工核查垃圾分类质量
     * @param id
     * @param quality
     * @param garbageType
     * @param request
     * @return
     */
    @RequestMapping(value = "remarkCommunityGarbage", method = RequestMethod.POST)
    public ResponseData remarkCommunityGarbage(Integer id, Integer quality, Integer garbageType, HttpServletRequest request){
        String jwt = request.getHeader("Authorization").split(" ")[1];
        Assert.state(quality != null, "请选择评分结果");
        Assert.state(garbageType != null, "请选择评分结果");
        ResponseData responseData = garbageCollectorService.remarkCommunityGarbage(id, quality, garbageType,jwt);
        return  responseData;
    }

    /**
     * 收集员数据统报表
     * @param pageNo
     * @param pageSize
     * @param startTime
     * @param endTime
     * @param type
     * @param phone
     * @param name
     * @param orderBys
     * @param cityId
     * @param countryId
     * @param townId
     * @param villageId
     * @param request
     * @return
     */
    @RequestMapping(value = "getGarbageCollectorSummaryInfo", method = RequestMethod.GET)
    public ResponseData getGarbageCollectorSummaryInfo(Integer pageNo, Integer pageSize, String startTime, String endTime, String type,
                                              String phone, String name, String[] orderBys,
                                                       Long cityId, Long countryId, Long townId, Long villageId, Long communityId,  HttpServletRequest request){
        String jwt = request.getHeader("Authorization").split(" ")[1];
        ResponseData responseData = garbageCollectorService.getGarbageCollectSummaryInfo(pageNo, pageSize,startTime, endTime, type, phone, name,jwt, orderBys, cityId, countryId, townId, villageId, communityId);
        return  responseData;
    }

    /**
     * 收集统计报表分析
     * @param pageNo
     * @param pageSize
     * @param startTime
     * @param endTime
     * @param type
     * @param orderBys
     * @param cityId
     * @param countryId
     * @param townId
     * @param villageId
     * @param request
     * @return
     */
    @RequestMapping(value = "getGarbageCollectSummaryInfoInPlace", method = RequestMethod.GET)
    public ResponseData getGarbageCollectSummaryInfoInPlace(Integer pageNo, Integer pageSize, String startTime, String endTime, String type,
                                      String[] orderBys, Long cityId, Long countryId, Long townId, Long villageId, Integer communityId, HttpServletRequest request){
        String jwt = request.getHeader("Authorization").split(" ")[1];
        ResponseData responseData = garbageCollectorService.getGarbageCollectSummaryInfoInPlace(pageNo, pageSize, startTime, endTime, type,
                cityId, countryId, townId, villageId,communityId, orderBys, jwt);
        return  responseData;
    }

    /**
     * 未上传用户列表
     * @param pageNo
     * @param pageSize
     * @param startTime
     * @param endTime
     * @param type
     * @param keyWord
     * @param orderBys
     * @param request
     * @return
     */
    @RequestMapping(value = "getNotSentGarbageInfoToSystemUser", method = RequestMethod.GET)
    public ResponseData getNotSentGarbageInfoToSystemUser(Integer pageNo, Integer pageSize, String startTime, String endTime,String type, String keyWord,
                                                          Long cityId, Long countryId, Long townId, Long villageId, Long communityId,
                                                          String[] orderBys , HttpServletRequest request){
        String jwt = request.getHeader("Authorization").split(" ")[1];
        ResponseData responseData = garbageCollectorService.getNotSentGarbageInfoToSystemUser(pageNo, pageSize, startTime, endTime, type, keyWord, jwt, orderBys,cityId,countryId,townId, villageId, communityId);
        return  responseData;
    }

    /**
     * 每月的垃圾累计值
     * @return
     */
    @RequestMapping(value = "getGarbageWeightCurrentYear", method = RequestMethod.GET)
    public ResponseData getGarbageWeightCurrentYear(){
        ResponseData responseData = garbageCollectorService.getGarbageWeightCurrentYear();
        return  responseData;
    }

}
