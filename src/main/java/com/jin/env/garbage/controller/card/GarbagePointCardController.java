package com.jin.env.garbage.controller.card;

import com.jin.env.garbage.service.card.GarbagePointCardService;
import com.jin.env.garbage.utils.ResponseData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping(value = "/api/v1/card/")
public class GarbagePointCardController {

    @Autowired
    private GarbagePointCardService garbagePointCardService;

    @RequestMapping(value = "addPointCardBatch", method = RequestMethod.POST)
    public ResponseData addPointCardBatch(MultipartFile file, HttpServletRequest request){
        String jwt = request.getHeader("Authorization").split(" ")[1];
        long fileSize = file.getSize();
        io.jsonwebtoken.lang.Assert.state(fileSize <= 8388608, "上传文件过大，文件大小应在8M以内");
        ResponseData  responseData =  garbagePointCardService.addPointCardBatch(file, jwt);
        return responseData;
    }
}
