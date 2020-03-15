package com.jin.env.garbage.controller.export;

import com.jin.env.garbage.service.garbage.GarbageCollectorService;
import com.jin.env.garbage.service.point.GarbageUserPointService;
import com.jin.env.garbage.utils.ExportExcelUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/export/")
public class ExportController {

    @Autowired
    private GarbageUserPointService garbageUserPointService;

    @Autowired
    private GarbageCollectorService garbageCollectorService;

    @RequestMapping(value = "exportPointList")
    public void exportPointList(HttpServletRequest request, HttpServletResponse response, String type, String keyWord,
                                Long cityId, Long countryId, Long townId, Long villageId, Integer communityId){
        String jwt = request.getHeader("Authorization").split(" ")[1];
        List<Object[]> data = garbageUserPointService.exportPointList(type, keyWord, cityId, countryId, townId, villageId, communityId, jwt);
        List<String> headers = new ArrayList<>();
        headers.add("姓名");
        headers.add("手机号");
        headers.add("所属区域");
        headers.add("详细地址");
        headers.add("积分");
        ExportExcelUtils.exportExcel(response, headers, "积分光荣榜","积分光荣榜",new Integer[]{0,0,0,4}, data);
    }
    @RequestMapping(value = "exportNotSendGarbageToSystem")
    public void exportNotSendGarbageToSystem(HttpServletRequest request, HttpServletResponse response, String type, String keyWord, String startTime, String endTime){
        String jwt = request.getHeader("Authorization").split(" ")[1];
        List<Object[]> data = garbageCollectorService.exportNotSendGarbageToSystem(type, keyWord,startTime, endTime, jwt);
        List<String> headers = new ArrayList<>();
        headers.add("姓名");
        headers.add("手机号");
        headers.add("省");
        headers.add("市");
        headers.add("区县");
        headers.add("乡镇");
        headers.add("村");
        headers.add("详细地址");
        ExportExcelUtils.exportExcel(response, headers, "未上传用户","未上传用户" + startTime + "至"+ endTime, new Integer[]{0,0,0,7},  data);
    }

    @RequestMapping(value = "exportVillageGarbageList")
    public void exportVillageGarbageList(HttpServletRequest request, HttpServletResponse response,
                                         Boolean isCheck, Double weight, Integer point, Integer quality, String type,
                                         String keyWord,  Integer garbageType, String startTime, String endTime, String name,
                                         Long cityId, Long countryId, Long townId, Long villageId, Integer sType){
        String jwt = request.getHeader("Authorization").split(" ")[1];
        List<Object[]> data = garbageCollectorService.exportVillageGarbageList(isCheck, weight, point, quality, type, keyWord, garbageType,startTime, endTime, name,
                cityId, countryId, townId, villageId,jwt, sType);
        List<String> headers = new ArrayList<>();
        headers.add("姓名");
        headers.add("手机号");
        headers.add("电子卡卡号");
        headers.add("省");
        headers.add("市");
        headers.add("区县");
        headers.add("乡镇");
        headers.add("村");
        headers.add("详细地址");
        headers.add("合格情况");
        headers.add("积分");
        headers.add("重量");
        headers.add("垃圾类型");
        headers.add("收集员姓名");
        headers.add("收集员手机号");
        headers.add("收集日期");
        headers.add("评分结果");
        ExportExcelUtils.exportExcel(response, headers, "垃圾收集管理","垃圾收集管理" + startTime + "至"+ endTime, new Integer[]{0,0,0,16},  data);
    }

    @RequestMapping(value = "exportGarbageCollectorSummaryInfo")
    public void exportGarbageCollectorSummaryInfo(HttpServletRequest request, HttpServletResponse response,String startTime, String endTime, String type,
                                                   String phone, String name,
                                                   Long cityId, Long countryId, Long townId, Long villageId, Long communityId){
        String jwt = request.getHeader("Authorization").split(" ")[1];
        List<Object[]> data = garbageCollectorService.exportGarbageCollectorSummaryInfo(type, phone, name, startTime, endTime, cityId, countryId , townId, villageId, communityId, jwt);
        List<String> headers = new ArrayList<>();
        headers.add("收集日期");
        headers.add("收集员姓名");
        headers.add("收集员手机号");
        headers.add("省");
        headers.add("市");
        headers.add("区县");
        headers.add("乡镇");
        headers.add("村");
        headers.add("详细地址");
        headers.add("收集总户数");
        headers.add("收集总重量");
        ExportExcelUtils.exportExcel(response, headers, "收集员数据统计报表","收集员数据统计报表" + startTime + "至"+ endTime, new Integer[]{0,0,0,10},  data);

    }

    @RequestMapping(value = "exportUserCollectDataAnalysis")
    public void exportUserCollectDataAnalysis(HttpServletRequest request, HttpServletResponse response, String startTime, String endTime,String type, String keyWord,
                                              Long cityId, Long countryId, Long townId, Long villageId, Long communityId){
        String jwt = request.getHeader("Authorization").split(" ")[1];
        List<Object[]> data = garbageCollectorService.exportUserCollectDataAnalysis(type, keyWord,startTime, endTime, cityId, countryId, townId, villageId, communityId, jwt);
        List<String> headers = new ArrayList<>();
        headers.add("姓名");
        headers.add("手机号");
        headers.add("省");
        headers.add("市");
        headers.add("区县");
        headers.add("乡镇");
        headers.add("村");
        headers.add("详细地址");
        headers.add("总数");
        headers.add("合格数");
        headers.add("不合格数");
        headers.add("空桶");
        headers.add("合格率");
        headers.add("垃圾重量");
        ExportExcelUtils.exportExcel(response, headers, "用户采集报表分析","用户采集报表分析" + startTime + "至"+ endTime, new Integer[]{0,0,0,13},  data);
    }

    @RequestMapping(value = "exportDataComparisonAndAnalysis", method = RequestMethod.GET)
    public void exportDataComparisonAndAnalysis(HttpServletRequest request, HttpServletResponse response, String startTime, String endTime,
                                                 Long cityId, Long countryId, Long townId, Long villageId, Long communityId){

        String jwt = request.getHeader("Authorization").split(" ")[1];
        List<Object[]> data = garbageCollectorService.exportDataComparisonAndAnalysis(startTime, endTime, cityId, countryId, townId, villageId, communityId, jwt);
        List<String> headers = new ArrayList<>();
        headers.add("地址名称");
        headers.add("厨余垃圾重量");
        headers.add("参与户数");
        headers.add("总户数");
        headers.add("参与率");
        headers.add("合格率");
        headers.add("不合格率");
        headers.add("空桶率");
        headers.add("地址编号");
        ExportExcelUtils.exportExcel(response, headers, "数据评比分析","数据评比分析" + startTime + "至"+ endTime, new Integer[]{0,0,0,8},  data);

    }
}
