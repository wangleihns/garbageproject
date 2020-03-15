package com.jin.env.garbage.controller.fold;

import com.jin.env.garbage.service.fold.GarbageFoldService;
import com.jin.env.garbage.utils.ResponseData;
import io.jsonwebtoken.lang.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;


import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping(value = "api/v1/fold/")
public class GarbageFoldController {
    @Autowired
    private GarbageFoldService garbageFoldService;

    @RequestMapping(value = "remarkFold", method =RequestMethod.POST)
    public ResponseData remarkFold(String phone, String name, String ids, Integer result,String remark, Integer score, HttpServletRequest request){
        Assert.hasText(phone, "手机号不能为空");
        Assert.hasText(name, "姓名不能为空");
        Assert.state(ids.split(",").length > 0, "必须上传图片");
        String jwt = request.getHeader("Authorization").split(" ")[1];
        ResponseData  responseData =  garbageFoldService.remarkFold(phone, name, ids, result, remark, score, jwt);
        return responseData;
    }

    @RequestMapping(value = "checkUserInfo", method =RequestMethod.GET)
    public ResponseData checkUserInfo(String phone, String eno){
        Assert.hasText(phone, "手机号不能为空");
        Assert.hasText(eno, "eno不能为空");
        ResponseData  responseData =  garbageFoldService.checkUserInfo(phone, eno);
        return responseData;
    }

    @RequestMapping(value = "getFoldList", method =RequestMethod.GET)
    public ResponseData getFoldList(Integer pageNo, Integer pageSize, String phone, HttpServletRequest request){
        String jwt = request.getHeader("Authorization").split(" ")[1];
        ResponseData  responseData =  garbageFoldService.getFoldList(pageNo, pageSize,phone, jwt);
        return responseData;
    }

    @RequestMapping(value = "getFoldInfoById", method =RequestMethod.GET)
    public ResponseData getFoldInfoById(Integer id){
        ResponseData  responseData =  garbageFoldService.getFoldInfoById(id);
        return responseData;
    }

    @RequestMapping(value = "getUserInfo", method =RequestMethod.GET)
    public ResponseData getUserInfo(HttpServletRequest request){
        String jwt = request.getHeader("Authorization").split(" ")[1];
        ResponseData  responseData =  garbageFoldService.getUserInfo(jwt);
        return responseData;
    }

    @RequestMapping(value = "getUserList", method =RequestMethod.GET)
    public ResponseData getUserList(Integer pageNo, Integer pageSize, String phone, HttpServletRequest request){
        String jwt = request.getHeader("Authorization").split(" ")[1];
        ResponseData  responseData =  garbageFoldService.getUserList(pageNo, pageSize,phone, jwt);
        return responseData;
    }
}
