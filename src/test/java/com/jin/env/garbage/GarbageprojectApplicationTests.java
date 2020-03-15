package com.jin.env.garbage;

import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.aliyuncs.exceptions.ClientException;
import com.jin.env.garbage.dao.message.GarbageMessageDao;
import com.jin.env.garbage.dao.point.GarbageUserPointDao;
import com.jin.env.garbage.dao.user.GarbageUserDao;
import com.jin.env.garbage.dao.village.GarbageVillageInfoDao;
import com.jin.env.garbage.entity.message.GarbageMessageEntity;
import com.jin.env.garbage.entity.point.GarbageUserPointEntity;
import com.jin.env.garbage.entity.user.GarbageUserEntity;
import com.jin.env.garbage.entity.village.GarbageVillageInfoEntity;
import com.jin.env.garbage.utils.*;
import net.sf.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.junit4.SpringRunner;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GarbageprojectApplicationTests {

	@Test
	public void contextLoads() {
		List<Integer> list = new ArrayList() {
			{
				add(1);
				add(2);
				add(1);
			}
		};
		List<Integer> collect = list.stream().distinct().collect(Collectors.toList());
		collect.forEach(n->{
			System.out.printf(n + "");
		});
	}


	@Autowired
	private GarbageUserDao userDao;
	@Autowired
	private GarbageUserPointDao garbageUserPointDao;
	@Test
	public void testA() {
//		InputStream inputStream = null;
//		try {
//			inputStream = QRCodeUtil.initQRCode("{name:'12345'}");
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		String url = OssUpLoad.uploadImage(inputStream, UUID.randomUUID().toString()+".png");
//		System.out.println(url);
//		List<GarbageUserEntity> userEntities = userDao.findAll();
//		Map<Integer, String> userNameMap = userEntities.stream().collect(Collectors.toMap(GarbageUserEntity::getId, GarbageUserEntity::getName));
//		List<GarbageUserPointEntity> pointEntities = garbageUserPointDao.findAll();
//
//		pointEntities.forEach(n -> {
//			n.setUserName(userNameMap.get(n.getUserId()));
//		});
//		System.out.println(1);
//		garbageUserPointDao.saveAll(pointEntities);
	}

	@Autowired
	private GarbageVillageInfoDao garbageVillageInfoDao;
	@Test
	public void test() throws  Exception{
		System.out.println("15158099385".length());
		//SmsUtil.getSmsUtil().sendGarbageNotice("63151379", "春江街道", "2019-12-05","很好", 1);
		// 330183002229
		GarbageVillageInfoEntity villageInfoEntity = garbageVillageInfoDao.findByVillageId(330183002229L);
		if (villageInfoEntity.getSendMsg() == 1){
			System.out.println(1);
		} else{
			System.out.println(0);
		}

	}
	@Autowired
	GarbageMessageDao garbageMessageDao;
}
