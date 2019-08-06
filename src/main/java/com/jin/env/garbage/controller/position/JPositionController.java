package com.jin.env.garbage.controller.position;

import com.jin.env.garbage.utils.ResponseData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "api/v1/position/")
public class JPositionController {
    private Logger logger = LoggerFactory.getLogger(JPositionController.class);

    @RequestMapping(value = "getProvince", method = RequestMethod.GET)
    public ResponseData getProvince(){
        return null;
    }
}
