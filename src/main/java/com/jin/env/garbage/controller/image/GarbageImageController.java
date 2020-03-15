package com.jin.env.garbage.controller.image;


import com.jin.env.garbage.entity.image.GarbageImageEntity;
import com.jin.env.garbage.service.image.GarbageImageService;
import com.jin.env.garbage.utils.OssUpLoad;
import com.jin.env.garbage.utils.ResponseData;
import io.jsonwebtoken.lang.Assert;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@RestController
@RequestMapping(value = "api/v1/image/")
public class GarbageImageController {
    private Logger logger = LoggerFactory.getLogger(GarbageImageController.class);
    @Value("${maxImageSize}")
    private Integer maxImageSize;

    @Autowired
    private GarbageImageService garbageImageService;

    @RequestMapping(value = "uploadImg",method = RequestMethod.POST)
    public ResponseData uploadImg(@RequestParam("file") MultipartFile multipartFile, HttpServletRequest request) {
        Assert.state(!(multipartFile.isEmpty() ||
                StringUtils.isBlank(multipartFile.getOriginalFilename())),"图片不能为空");
        long fileSize = multipartFile.getSize();
        Assert.state(fileSize <= maxImageSize, "上传图片过大");
        ResponseData responseData = garbageImageService.uploadImg(multipartFile);
        return responseData;
    }


    @RequestMapping(value = "uploadFoldImg",method = RequestMethod.POST)
    public ResponseData uploadFoldImg(@RequestParam("file") MultipartFile multipartFile, HttpServletRequest request) {
        Assert.state(!(multipartFile.isEmpty() ||
                StringUtils.isBlank(multipartFile.getOriginalFilename())),"图片不能为空");
        long fileSize = multipartFile.getSize();
        Assert.state(fileSize <= maxImageSize, "上传图片过大");
        ResponseData responseData = garbageImageService.uploadFoldImg(multipartFile);
        return responseData;
    }

}
