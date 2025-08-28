package com.komentum.theme.utils;

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
public class S3FileManager {

  private final S3Client s3Client;
  private final String cloudFront;

  public S3FileManager(@Value("${aws.s3.access-key}") String accessKey,
      @Value("${aws.s3.secret-key}") String secretKey, @Value("${aws.s3.region}") String region,
      @Value("${aws.s3.cloudfront}") String cloudFront) {
    if (cloudFront == null || cloudFront.trim().isEmpty()) {
      throw new IllegalArgumentException("[S3 File Manager] cloudFront is null or empty");
    }
    this.cloudFront = cloudFront;
    s3Client = S3Client.builder()
        .region(Region.of(region))
        .credentialsProvider(
            StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)))
        .build();
  }

  public String resolveFilePath(String fileName) {
    if (fileName == null || fileName.trim().isEmpty()) {
      throw new IllegalArgumentException("[S3 File Manager] fileName is null or empty");
    }
    return String.format("https://%s/%s", cloudFront, fileName);
  }

  /**
   * upload a file to s3 bucket
   *
   * @param fileBytes  file byte array
   * @param fileName   name of the file
   * @param bucketName name of the bucket to upload the file to
   */
  public String uploadFile(byte[] fileBytes, String fileName, String bucketName) {
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
   * @param fileName   name of the file
   * @param bucketName name of the bucket to delete the file from
   */
  public void deleteFile(String fileName, String bucketName) {
    DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
        .bucket(bucketName)
        .key(fileName)
        .build();
    s3Client.deleteObject(deleteObjectRequest);
  }

  /**
   * get a file from s3 bucket
   *
   * @param fileName   name of the file
   * @param bucketName name of the bucket to get the file from
   */
  public byte[] downloadFile(String fileName, String bucketName) {
    GetObjectRequest getObjectRequest = GetObjectRequest.builder()
        .bucket(bucketName)
        .key(fileName)
        .build();
    ResponseBytes<GetObjectResponse> responseBytes = s3Client.getObjectAsBytes(getObjectRequest);
    return responseBytes.asByteArray();
  }
}
