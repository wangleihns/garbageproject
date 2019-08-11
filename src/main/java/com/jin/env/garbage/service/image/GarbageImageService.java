package com.jin.env.garbage.service.image;

import com.jin.env.garbage.dao.image.GarbageImageDao;
import com.jin.env.garbage.entity.image.GarbageImageEntity;
import com.jin.env.garbage.utils.Constants;
import com.jin.env.garbage.utils.OssUpLoad;
import com.jin.env.garbage.utils.ResponseData;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@Service
public class GarbageImageService {
    private Logger logger = LoggerFactory.getLogger(GarbageImageService.class);

    @Autowired
    private GarbageImageDao garbageImageDao;
    @Transactional
    public ResponseData uploadImg(MultipartFile multipartFile) {
        ResponseData responseData = new ResponseData();
        String root_fileName = multipartFile.getOriginalFilename();
        String suffix  = FilenameUtils.getExtension(root_fileName);
        String uuid = UUID.randomUUID().toString();
        logger.info("上传图片:name={},type={}", root_fileName);
        String newName = uuid + "." + suffix;
        Long fileSize = multipartFile.getSize();
        InputStream inputStream = null;
        try {
            inputStream = multipartFile.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String path = OssUpLoad.uploadImage(inputStream,newName);
        GarbageImageEntity imageEntity = new GarbageImageEntity();
        imageEntity.setImageName(newName);
        imageEntity.setOriginalFilename(root_fileName);
        imageEntity.setImageSize(fileSize);
        imageEntity.setImagePath(path);
        garbageImageDao.save(imageEntity);
        responseData.setStatus(Constants.responseStatus.Success.getStatus());
        responseData.setMsg("上传成功");
        return responseData;
    }
}
