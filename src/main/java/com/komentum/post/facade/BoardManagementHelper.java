package com.komentum.post.facade;

import com.komentum.global.utils.FileManager;
import com.komentum.post.domain.Post;
import com.komentum.post.dto.PostDto.PostCreateDto;
import com.komentum.post.service.PostService;
import com.komentum.user.domain.User;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Component
@RequiredArgsConstructor
public class BoardManagementHelper {

  private final FileManager fileManager;
  private final PostService postService;

  private String savePreviewImageAndGetFileName(MultipartFile previewImage) {
    try {
      String extension = StringUtils.getFilenameExtension(previewImage.getOriginalFilename());
      if (extension == null || extension.isEmpty()) {
        extension = "bin";
      }
      String randomFileName = UUID.randomUUID() + "." + extension;
      String imageUrl = fileManager.uploadFile(previewImage.getBytes(), randomFileName);
      if (imageUrl == null) {
        throw new RuntimeException("Failed to upload preview image file");
      }
      return randomFileName;
    } catch (Exception e) {
      throw new RuntimeException("Failed to process preview image file", e);
    }
  }

  public Post createPostAndPreviewImage(PostCreateDto postCreateDto, User author,
      MultipartFile previewImage) {
    String savedFileName = null;
    if (previewImage != null && !previewImage.isEmpty()) {
      savedFileName = savePreviewImageAndGetFileName(previewImage);
    }
    return postService.createPost(postCreateDto, author, savedFileName);
  }

  public String findPreviewImageUrl(String fileName) {
    return fileManager.resolveFilePath(fileName);
  }
}
