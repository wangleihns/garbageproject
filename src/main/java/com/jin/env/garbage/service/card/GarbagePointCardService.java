package com.jin.env.garbage.service.card;

import com.jin.env.garbage.dao.card.GarbagePointCardDao;
import com.jin.env.garbage.dao.user.GarbageUserDao;
import com.jin.env.garbage.entity.card.GarbagePointCardEntity;
import com.jin.env.garbage.entity.user.GarbageRoleEntity;
import com.jin.env.garbage.entity.user.GarbageUserEntity;
import com.jin.env.garbage.jwt.JwtUtil;
import com.jin.env.garbage.utils.Constants;
import com.jin.env.garbage.utils.ReadExcelUtil;
import com.jin.env.garbage.utils.ResponseData;
import org.apache.commons.io.FilenameUtils;
import org.apache.http.message.BasicNameValuePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class GarbagePointCardService {
    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private GarbageUserDao garbageUserDao;

    @Autowired
    private GarbagePointCardDao garbagePointCardDao;

    public ResponseData addPointCardBatch(MultipartFile file, String jwt) {
        ResponseData responseData = new ResponseData();
        Integer sub = jwtUtil.getSubject(jwt);
        GarbageUserEntity userEntity = garbageUserDao.findById(sub).get();
        List<GarbageRoleEntity> roleEntities = userEntity.getRoles().stream().collect(Collectors.toList());
        List<String> roleCodes = roleEntities.stream().map(garbageRoleEntity -> garbageRoleEntity.getRoleCode()).collect(Collectors.toList());
        if (roleCodes.contains("VILLAGE_ADMIN") || roleCodes.stream().filter(n -> n.endsWith("COMMUNITY_ADMIN")).collect(Collectors.toList()).size() > 0) {
            String root_fileName = file.getOriginalFilename();
            String suffix = FilenameUtils.getExtension(root_fileName);
            InputStream is = null;
            try {
                is = file.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
            List<BasicNameValuePair> basicNameValuePairs = ReadExcelUtil.readPointCardExcel(is, suffix);
            List<String> phones = basicNameValuePairs.stream().map(n -> n.getName()).collect(Collectors.toList());
            Map<String, List<String>> collectPhoneMap = phones.stream().collect(Collectors.groupingBy(Function.identity()));
            List<String> values = basicNameValuePairs.stream().map(n -> n.getValue()).collect(Collectors.toList());
            List<String> pp = new ArrayList<>();
            for (String key : collectPhoneMap.keySet()) {
                List<String> p = collectPhoneMap.get(key);
                if (p.size() > 1) {
                    pp.add(p.get(0));
                }
            }
            if (pp.size() > 0) {
                String ph = pp.stream().collect(Collectors.joining(","));
                throw new RuntimeException("导入的excel表格中手机号出现重复，请核查 " + ph);
            }
            List<GarbagePointCardEntity> pointCardEntities = garbagePointCardDao.findByPointCardIn(values);
            if (pointCardEntities.size() > 0) {
                String ph = pointCardEntities.stream().map(n -> n.getPointCard()).collect(Collectors.toList()).stream().collect(Collectors.joining(","));
                throw new RuntimeException("导入的excel表格中积分卡与数据库中原有的积分卡出现重复，请核查 " + ph);
            }
            List<GarbageUserEntity> userEntities  = garbageUserDao.findByPhoneIn(phones);
            Map<String, Integer> userMap = userEntities.stream().collect(Collectors.toMap(GarbageUserEntity::getPhone, GarbageUserEntity::getId));
            List<GarbagePointCardEntity> cardEntityList = new ArrayList<>();
            basicNameValuePairs.forEach(b ->{
                GarbagePointCardEntity cardEntity = new GarbagePointCardEntity();
                cardEntity.setUserId(userMap.get(b.getName()));
                cardEntity.setPointCard(b.getValue());
                cardEntityList.add(cardEntity);
            });
            garbagePointCardDao.saveAll(cardEntityList);
            responseData.setMsg("数据批量导入成功");
            responseData.setStatus(Constants.responseStatus.Success.getStatus());
        } else {
            throw new RuntimeException("该用户没有权限批量导入数据");
        }
        return responseData;
    }
}
