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
     * @param eNo  电子卡
     * @param quality 垃圾分类的质量
     * @param weight  重量
     * @param imageId 图片
     * @param garbageType 垃圾类型
     * @param request
     * @return
     */
    @RequestMapping(value = "addGarbageByCollector", method = RequestMethod.POST)
    public ResponseData addGarbageByCollector(String eNo, String quality, Double weight, Integer imageId, Integer garbageType, HttpServletRequest request){
        String jwt = request.getHeader("Authorization").split(" ")[1];
        Assert.hasText(eNo, "请传入电子卡Id");
        Assert.state(weight != null, "上传垃圾重量");
        Assert.state(imageId != null, "上传图片id");
        Assert.hasText(quality, "请评价垃圾是否合格");
        ResponseData responseData = garbageCollectorService.addGarbageByCollector(eNo, quality, weight, imageId, garbageType, jwt);
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
    public ResponseData addGarbageByAuto(String eNo, Double weight, Integer garbageType, Integer imageId, HttpServletRequest request){
        String jwtArr[] = request.getHeader("Authorization").split(" ");
        String jwt = jwtArr[1];
        Assert.hasText(eNo, "请传入电子卡Id");
        Assert.state(weight != null, "上传垃圾重量");
        Assert.state(imageId != null, "上传图片id");
        Assert.state(garbageType != null, "垃圾类型必传");
        ResponseData responseData = garbageCollectorService.addGarbageByAuto(eNo, weight, imageId, jwt, garbageType);
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
                                            String startTime, String endTime, String name, Long cityId, Long countryId, Long townId, Long villageId, Integer sType){
        Long start = System.currentTimeMillis();
        String jwt = request.getHeader("Authorization").split(" ")[1];
        ResponsePageData responseData = garbageCollectorService.villageGarbageList(pageNo,pageSize, isCheck, weight, point, quality,
                type, keyWord, garbageType, jwt, orderBys, startTime, endTime, name, cityId,countryId, townId, villageId, sType);
        System.out.println(System.currentTimeMillis() - start);
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
    public ResponseData getGarbageWeightCurrentYear(HttpServletRequest request){
        String jwt = request.getHeader("Authorization").split(" ")[1];
        ResponseData responseData = garbageCollectorService.getGarbageWeightCurrentYear(jwt);
        return  responseData;
    }

    @RequestMapping(value = "userCollectDataAnalysis", method = RequestMethod.GET)
    public  ResponseData userCollectDataAnalysis(Integer pageNo, Integer pageSize, String startTime, String endTime,String type, String keyWord,
                                                 Long cityId, Long countryId, Long townId, Long villageId, Long communityId,
                                                 String[] orderBys , HttpServletRequest request){
        String jwt = request.getHeader("Authorization").split(" ")[1];
        ResponseData responseData = garbageCollectorService.userCollectDataAnalysis(pageNo, pageSize, startTime, endTime, type, keyWord, jwt, orderBys,cityId,countryId,townId, villageId, communityId);
        return  responseData;
    }

    /**
     * 大数据中心 近七天的数据展示图表
     * @param request
     * @return
     */
    @RequestMapping(value = "garbageCollectInWeek", method = RequestMethod.GET)
    public ResponseData garbageCollectInWeek(HttpServletRequest request){
        String jwt = request.getHeader("Authorization").split(" ")[1];
        ResponseData responseData = garbageCollectorService.garbageCollectInWeek(jwt);
        return  responseData;
    }

    /**
     *  大数据中心 滚动列表
     *  garbageType 垃圾类型
     * @param request
     * @return
     */
    @RequestMapping(value = "getRollingGarbageInfo", method = RequestMethod.GET)
    public ResponseData getRollingGarbageInfo(HttpServletRequest request, Integer garbageType, Long villageId){
        String jwt = request.getHeader("Authorization").split(" ")[1];
        Assert.state(garbageType != null, "垃圾类型不能为空");
        ResponseData responseData = garbageCollectorService.getRollingGarbageInfo(jwt, garbageType, villageId);
        return  responseData;
    }

    /**
     *  大数据中心 滚动列表 参与人次
     * @param
     *
     * @return
     */
    @RequestMapping(value = "getRollingTotalPersonPartIn", method = RequestMethod.GET)
    public ResponseData getRollingTotalPersonPartIn(HttpServletRequest request, Integer garbageType, Long villageId){
        String jwt = request.getHeader("Authorization").split(" ")[1];

        ResponseData responseData = garbageCollectorService.getRollingTotalPersonPartIn(jwt, garbageType, villageId);
        return  responseData;
    }
    @RequestMapping(value = "getRollingTotalWeight", method = RequestMethod.GET)
    public ResponseData getRollingTotalWeight(Integer garbageType, HttpServletRequest request){
        String jwt = request.getHeader("Authorization").split(" ")[1];
        ResponseData responseData = garbageCollectorService.getRollingTotalWeight(jwt, garbageType);
        return  responseData;
    }


    @RequestMapping(value = "bigDataMapPointInfo", method = RequestMethod.GET)
    public ResponseData bigDataMapPointInfo(HttpServletRequest request){
        String jwt = request.getHeader("Authorization").split(" ")[1];
        ResponseData responseData = garbageCollectorService.bigDataMapPointInfo(jwt);
        return  responseData;
    }

    @RequestMapping(value = "getBigDataShowTitleInfo", method = RequestMethod.GET)
    public ResponseData getBigDataShowTitleInfo(HttpServletRequest request){
        String jwt = request.getHeader("Authorization").split(" ")[1];
        ResponseData responseData = garbageCollectorService.getBigDataShowTitleInfo(jwt);
        return  responseData;
    }

    /**
     * 接口地址： /api/v1/collector/checkUserCollectToday
     * 检查当前电子卡用户当天上传次数
     * @param eno 电子卡id
     * @return
     */
    @RequestMapping(value = "checkUserCollectToday", method = RequestMethod.GET)
    public ResponseData checkUserCollectToday(String eno){
        ResponseData responseData = garbageCollectorService.checkUserCollectToday(eno);
        return  responseData;
    }

    @RequestMapping(value = "addRecycleGarbage", method = RequestMethod.POST)
    public ResponseData addRecycleGarbage( Double weight, Integer garbageType, HttpServletRequest request){
        String jwt = request.getHeader("Authorization").split(" ")[1];
        ResponseData responseData = garbageCollectorService.addRecycleGarbage(weight, garbageType, jwt);
        return  responseData;
    }

    /**
     * 重新评分
     * @param id
     * @param quality
     * @return
     */
    @RequestMapping(value = "remarkAgain", method = RequestMethod.POST)
    public ResponseData remarkAgain(Integer id, Integer quality, String remark, HttpServletRequest request){
        String jwt = request.getHeader("Authorization").split(" ")[1];
        Assert.state(id != null, "id 必传");
        Assert.state(quality != null, "重新评定结果必传");
//        Assert.hasText(remark, "更改原因必传");
        ResponseData responseData = garbageCollectorService.remarkAgain(id, quality,  remark ,jwt);
        return  responseData;
    }

    /**
     *
     * @param pageNo
     * @param pageSize
     * @param startTime
     * @param endTime
     * @param cityId
     * @param countryId
     * @param townId
     * @param villageId
     * @param communityId
     * @param orderBys
     * @param request
     * @return
     */

    @RequestMapping(value = "dataComparisonAndAnalysis", method = RequestMethod.GET)
    public ResponseData dataComparisonAndAnalysis(Integer pageNo, Integer pageSize, String startTime, String endTime,
           Long cityId, Long countryId, Long townId, Long villageId, Long communityId,
           String[] orderBys , HttpServletRequest request){
        String jwt = request.getHeader("Authorization").split(" ")[1];

        ResponseData responseData = garbageCollectorService.dataComparisonAndAnalysis(pageNo, pageSize,  startTime ,endTime ,cityId,
                countryId, townId, villageId,communityId, jwt , orderBys);
        return  responseData;
    }
}
