package com.jin.env.garbage;

import com.jin.env.garbage.dao.user.GarbageUserDao;
import com.jin.env.garbage.entity.user.GarbageUserEntity;
import com.jin.env.garbage.utils.OssUpLoad;
import com.jin.env.garbage.utils.QRCodeUtil;
import net.sf.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.io.InputStream;
import java.util.Random;
import java.util.UUID;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GarbageprojectApplicationTests {

	@Test
	public void contextLoads() {
	}


	@Autowired
	private GarbageUserDao userDao;
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
	}
}
