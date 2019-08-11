package com.jin.env.garbage.utils;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClient;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.PutObjectResult;

import java.io.InputStream;
import java.net.URL;

/**
 * Created by abc on 2018/7/23.
 */
public class OssUpLoad {

//    private static String endpoint = "oss-cn-beijing.aliyuncs.com";
//    private static String accessKeyId = "LTAIbQlC2y2XQCJK";
//    private static String accessKeySecret = "BvJLIZljfAqV7cvPj3YzTAKwe72g4z";
//
//    private static String bucketName = "windhouse";

    private static String endpoint = PropertiesUtils.properties.getProperty("endpoint");
    private static String accessKeyId = PropertiesUtils.properties.getProperty("accessKeyId");
    private static String accessKeySecret = PropertiesUtils.properties.getProperty("accessKeySecret");
    private static String bucketName = PropertiesUtils.properties.getProperty("bucketName");

//    public static void main(String[] args) throws  Exception{
//        File file = new File("/Users/changzhaoliang/Desktop/a3.jpg");
//        FileInputStream inputStream = new FileInputStream(file);
//        MultipartFile multipartFile = new MockMultipartFile(file.getName(), inputStream);
//
//        if (!multipartFile.isEmpty()) {
//            String fileName = multipartFile.getName();
//            // 获取文件的后缀名
//            String suffixName = fileName.substring(fileName.lastIndexOf("."));
//
//            fileName = new Date().getTime()+ suffixName;
//            String src=OssUpload.upload2(multipartFile.getInputStream(),fileName);
//            System.out.print(src);
//        }
//    }

    public static String uploadImage(InputStream is, String fileName) {
        // 创建OSSClient实例
        String src = null;
        try {
            OSSClient ossClient = new OSSClient(endpoint, accessKeyId, accessKeySecret);
            PutObjectResult putObjectResult = ossClient.putObject(bucketName, fileName, is);
            // 关闭client
            ossClient.shutdown();
            src = "http://"+ bucketName + "." + endpoint + "/" + fileName;
        } catch (OSSException e) {
            e.printStackTrace();
            throw e;
        } catch (ClientException e) {
            e.printStackTrace();
            throw e;
        }
        return src;
    }

    public static Boolean deleteSingleFile(String imageName){
        // 创建OSSClient实例。
        Boolean flag = false;
        try {
            OSSClient ossClient = new OSSClient(endpoint, accessKeyId, accessKeySecret);
            // 删除文件。
            ossClient.deleteObject(bucketName, imageName);
            flag = true;
            // 关闭OSSClient。
            ossClient.shutdown();
        } catch (OSSException e) {
            e.printStackTrace();
            throw e;
        } catch (ClientException e) {
            e.printStackTrace();
            throw e;
        }
        return flag;
    }
}