package com.jin.env.garbage.service.position;

import com.jin.env.garbage.dao.position.JPositionProvinceDao;
import com.jin.env.garbage.entity.position.JPositionProvinceEntity;
import com.jin.env.garbage.utils.ResponseData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JPositionService {
   private  Logger logger = LoggerFactory.getLogger(JPositionService.class);

   @Autowired
   private JPositionProvinceDao jPositionProvinceDao;

   public ResponseData<JPositionProvinceEntity> getProvince(){
       ResponseData responseData = new ResponseData();
       List<JPositionProvinceEntity> provinceEntityList = jPositionProvinceDao.findAll();
       return responseData;
   }
}
