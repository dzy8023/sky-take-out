package com.sky.utils;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.errors.MinioException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;

@Data
@AllArgsConstructor
@Slf4j
public class MinOssUtil {
    private String endpoint;
    private String accessKeyId;
    private String accessKeySecret;
    private String bucketName;

    public String upload(byte[] bytes, String objectName) {
        log.info("endpoint:{}", endpoint);
        //创建OssClient实例
        MinioClient minioClient = new MinioClient.Builder()
                .endpoint(endpoint)
                .credentials(accessKeyId, accessKeySecret)
                .build();
        try {
            //创建PutObject请求
            minioClient.putObject(PutObjectArgs
                    .builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .stream(new ByteArrayInputStream(bytes), bytes.length, -1)
                    .build());
        } catch (Exception e) {
            log.error("添加存储对象异常：{}", e.getMessage());
//            throw new MinioException("添加存储对象异常",e.getMessage());
        }
//        finally {
//            if(minioClient!=null){
//                log.info("关闭minio客户端");
//            }

        //文件访问路径规则 Endpoint.BucketName/ObjectName
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder
                .append(endpoint)
                .append("/")
                .append(bucketName)
                .append("/")
                .append(objectName);
        log.info("文件上传到:{}", stringBuilder.toString());
        return stringBuilder.toString();
    }

    //文件删除
    public String delete(String name) {
        //创建OssClient实例
        MinioClient minioClient = new MinioClient.Builder()
                .endpoint(endpoint)
                .credentials(accessKeyId, accessKeySecret)
                .build();
        try {
            log.info("删除文件:{}", name);
            name = name.substring(endpoint.length() + bucketName.length() + 2);
            System.out.println(name);
            //删除文件
            minioClient.removeObject(RemoveObjectArgs.builder().bucket(bucketName)
                    .object(name).build());

        } catch (Exception e) {
            log.error("删除存储对象异常：{}", e.getMessage());
//            throw new MinioException("添加存储对象异常",e.getMessage());
        }
        return "删除成功";
    }
}

