package com.theme.utils;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;

@Component
public class S3FileManager {
    private AmazonS3 s3Client;

    public S3FileManager(@Value("${aws.s3.access-key}") String accessKey, @Value("${aws.s3.secret-key}") String secretKey){
        AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
        s3Client = AmazonS3ClientBuilder
                .standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion(Regions.US_WEST_2)
                .build();
    }

    /**
     * upload a file to s3 bucket
     * @param fileBytes file byte array
     * @param fileName name of the file
     * @param bucketName name of the bucket to upload the file to
    * */
    public void uploadFile(byte[] fileBytes, String fileName, String bucketName) {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(fileBytes.length);
        PutObjectRequest request = new PutObjectRequest(bucketName, fileName, new ByteArrayInputStream(fileBytes), metadata);
        s3Client.putObject(request);
    }

    /**
     * delete a file from s3 bucket
     * @param fileName name of the file
     * @param bucketName name of the bucket to delete the file from
    * */
    public void deleteFile(String fileName, String bucketName){
        s3Client.deleteObject(bucketName, fileName);
    }

    /**
     * get a file from s3 bucket
     * @param fileName name of the file
     * @param bucketName name of the bucket to get the file from
    * */
    public byte[] downloadFile(String fileName, String bucketName) throws IOException {
        S3Object s3Object = s3Client.getObject(bucketName, fileName);
        try (S3ObjectInputStream inputStream = s3Object.getObjectContent()) {
            return inputStream.readAllBytes();
        }
    }
}
