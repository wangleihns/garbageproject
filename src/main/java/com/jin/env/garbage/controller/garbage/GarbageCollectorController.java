package com.jin.env.garbage.controller.garbage;

import com.jin.env.garbage.service.garbage.GarbageCollectorService;
import com.jin.env.garbage.utils.ResponseData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;

@Controller
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
        String jwt = request.getHeader("Authorization").split(": ")[1];
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
        String jwt = request.getHeader("Authorization").split(": ")[1];
        ResponseData responseData = garbageCollectorService.addGarbageByAuto(eNo, weight, imageId, jwt);
        return  responseData;
    }



    @RequestMapping(value = "communityGarbageList", method = RequestMethod.POST)
    public ResponseData communityGarbageList(Integer pageNo, Integer pageSize, boolean isCheck, double weight, Integer point,
                                             Integer quality, String eNo, String name, String phone, Integer garbageType, String[] orderBys, HttpServletRequest request ){
        String jwt = request.getHeader("Authorization").split(": ")[1];
        ResponseData responseData = garbageCollectorService.communityGarbageList(pageNo,pageSize, isCheck, weight, point,
                quality, eNo, name, phone, garbageType, jwt,  orderBys);
        return  responseData;
    }
}
