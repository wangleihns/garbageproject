package com.jin.env.garbage.utils;

import com.aliyun.oss.OSSClient;

import java.io.InputStream;

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

    public static String upload2(InputStream is, String fileName) {
        // 创建OSSClient实例
        OSSClient ossClient = new OSSClient(endpoint, accessKeyId, accessKeySecret);
        ossClient.putObject(bucketName, fileName, is);
        // 关闭client
        ossClient.shutdown();
        String src ="http://"+ bucketName + "." + endpoint + "/" + fileName;
        return src;
    }
}