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

  private String saveProfileImageAndGetFileName(MultipartFile profileImage) {
    try {
      String extension = StringUtils.getFilenameExtension(profileImage.getOriginalFilename());
      if (extension == null || extension.isEmpty()) {
        extension = "bin";
      }
      String randomFileName = UUID.randomUUID() + "." + extension;
      String imageUrl = fileManager.uploadFile(profileImage.getBytes(), randomFileName);
      if (imageUrl == null) {
        throw new RuntimeException("Failed to upload profile image file");
      }
      return randomFileName;
    } catch (Exception e) {
      throw new RuntimeException("Failed to process profile image file", e);
    }
  }

  public Post createPostAndProfileImage(PostCreateDto postCreateDto, User author,
      MultipartFile profileImage) {
    String savedFileName = null;
    if (profileImage != null && !profileImage.isEmpty()) {
      savedFileName = saveProfileImageAndGetFileName(profileImage);
    }
    return postService.createPost(postCreateDto, author, savedFileName);
  }

  public String findProfileImageUrl(String fileName) {
    return fileManager.resolveFilePath(fileName);
  }

  public String findProfileImageUrl(Long postId) {
    Post targetPost = postService.getPostByPostId(postId);
    return fileManager.resolveFilePath(targetPost.getProfileImageName());
  }
}
