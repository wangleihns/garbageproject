package com.jin.env.garbage.controller.position;

import com.jin.env.garbage.service.position.JPositionService;
import com.jin.env.garbage.utils.ResponseData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "api/v1/position/")
public class JPositionController {
    private Logger logger = LoggerFactory.getLogger(JPositionController.class);

    @Autowired
    private JPositionService jPositionService;


    @RequestMapping(value = "getProvinceList", method = RequestMethod.GET)
    public ResponseData getProvinceList(){
        return jPositionService.getProvince();
    }

    @RequestMapping(value = "getCityListByProvinceId", method = RequestMethod.GET)
    public ResponseData getCityListByProvinceId(Integer provinceId){
        return jPositionService.getCityListByProvinceId(provinceId);
    }

    @RequestMapping(value = "getCountyListByCityId", method = RequestMethod.GET)
    public ResponseData getCountyListByCityId(Long cityId){
        return jPositionService.getCountyListByCityId(cityId);
    }

    @RequestMapping(value = "getTownListByCountyId", method = RequestMethod.GET)
    public ResponseData getTownListByCountyId(Long countyId){
        return jPositionService.getTownListByCountyId(countyId);
    }

    @RequestMapping(value = "getVillageListByTownId", method = RequestMethod.GET)
    public ResponseData getVillageListByTownId(Long townId){
        return jPositionService.getVillageListByTownId(townId);
    }

    @RequestMapping(value = "addVillage", method = RequestMethod.POST)
    public ResponseData addVillage(Integer townId, String villageName){
        return jPositionService.addVillage(townId,villageName);
    }
    @RequestMapping(value = "addCommunity", method = RequestMethod.POST)
    public ResponseData addCommunity(Integer provinceId, Long cityId, Long countryId, Long townId,  String address, String communityName, String desc, String fullAddress){
        return jPositionService.addCommunity(provinceId,cityId,countryId, townId, address, communityName, desc, fullAddress);
    }
}
