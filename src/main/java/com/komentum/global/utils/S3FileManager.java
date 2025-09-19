package com.komentum.global.utils;

import java.util.Objects;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Component
@Profile("!test")
public class S3FileManager implements FileManager {

  private final S3Client s3Client;
  private final String cloudFront;
  private final String bucketName;

  public S3FileManager(@Value("${aws.s3.access-key}") String accessKey,
      @Value("${aws.s3.secret-key}") String secretKey, @Value("${aws.s3.region}") String region,
      @Value("${aws.s3.cloudfront}") String cloudFront,
      @Value("${aws.s3.theme-bucket-name}") String bucketName) {
    this.cloudFront = Objects.requireNonNull(cloudFront, "cloudFront is Null");
    this.bucketName = Objects.requireNonNull(bucketName, "bucketName is Null");
    s3Client = S3Client.builder()
        .region(Region.of(region))
        .credentialsProvider(
            StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)))
        .build();
  }

  private String resolveFilePath(String fileName) {
    if (fileName == null || fileName.trim().isEmpty()) {
      throw new IllegalArgumentException("[S3 File Manager] fileName is null or empty");
    }
    return String.format("https://%s/%s", cloudFront, fileName);
  }

  /**
   * upload a file to s3 bucket
   *
   * @param fileBytes file bytes array
   * @param fileName  name of the file
   */
  public String uploadFile(byte[] fileBytes, String fileName) {
    PutObjectRequest putObjectRequest = PutObjectRequest.builder()
        .bucket(bucketName)
        .key(fileName)
        .contentLength((long) fileBytes.length)
        .build();
    s3Client.putObject(putObjectRequest, RequestBody.fromBytes(fileBytes));
    return resolveFilePath(fileName);
  }

  /**
   * delete a file from s3 bucket
   *
   * @param fileName name of the file
   */
  public void deleteFile(String fileName) {
    DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
        .bucket(bucketName)
        .key(fileName)
        .build();
    s3Client.deleteObject(deleteObjectRequest);
  }

  /**
   * get a file from s3 bucket
   *
   * @param fileName name of the file
   */
  public byte[] downloadFile(String fileName) {
    GetObjectRequest getObjectRequest = GetObjectRequest.builder()
        .bucket(bucketName)
        .key(fileName)
        .build();
    ResponseBytes<GetObjectResponse> responseBytes = s3Client.getObjectAsBytes(getObjectRequest);
    return responseBytes.asByteArray();
  }
}
