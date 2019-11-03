package com.jin.env.garbage.controller.position;

import com.jin.env.garbage.service.position.GarbageVillageInfoService;
import com.jin.env.garbage.utils.ResponseData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping(value = "api/v1/village/")
public class GarbageVillageInfoController {
    @Autowired
    private GarbageVillageInfoService garbageVillageInfoService;

    @RequestMapping(value = "addGarbageVillageInfo", method = RequestMethod.POST)
    public ResponseData addGarbageVillageInfo(Integer provinceId, Long cityId, Long countyId, Long townId, Long villageId,
                String provinceName, String cityName, String countyName, String townName, String villageName, String showTitle){
        Assert.state(provinceId != null, "省份id不能为空");
        Assert.state(cityId != null, "城市id不能为空");
        Assert.state(countyId != null, "区县id不能为空");
        Assert.state(townId != null, "乡镇id不能为空");
        Assert.state(villageId != null, "村id不能为空");
        Assert.hasText(showTitle, "大数据展示屏标题不能为空");
        ResponseData responsePageData =garbageVillageInfoService.addGarbageVillageInfo(provinceId, cityId, countyId,townId, villageId,
                provinceName, cityName, countyName, townName, villageName, showTitle);
        return responsePageData;
    }

    @RequestMapping(value = "garbageVillageInfoList", method = RequestMethod.GET)
    public ResponseData garbageVillageInfoList(Integer pageNo, Integer pageSize,
                                               Long cityId, Long countyId, Long townId, Long villageId, String search, HttpServletRequest request){
        String jwt = request.getHeader("Authorization").split(" ")[1];
        ResponseData responsePageData  = garbageVillageInfoService.garbageVillageInfoList(pageNo, pageSize, cityId,countyId, townId,villageId, search, jwt);
        return  responsePageData;
    }
}
