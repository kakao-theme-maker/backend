package com.komentum.post.service;

import com.komentum.post.domain.Post;
import com.komentum.post.domain.Tag;
import com.komentum.post.dto.TagDto.TagCreateDto;
import com.komentum.post.dto.TagDto.TagUpdateDto;
import com.komentum.post.repository.PostRepository;
import com.komentum.post.repository.TagRepository;
import jakarta.ws.rs.NotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TagService {

  public final TagRepository tagRepository;
  public final PostRepository postRepository;

  public List<Tag> findAllByPostId(Long postId) {
    return tagRepository.findAllByPost_PostId(postId);
  }

  public List<Tag> createTag(Long postId, TagCreateDto createDto) {
    Post targetPost = postRepository.findById(postId)
        .orElseThrow(() -> new NotFoundException("Post not found"));
    List<Tag> tags = createDto.getTagNames().stream()
        .map(tagName -> Tag.createTransient(tagName, targetPost)).toList();
    return tagRepository.saveAll(tags);
  }

  public Tag updateTag(Long tagId, TagUpdateDto updateDto) {
    Tag targetTag = tagRepository.findById(tagId)
        .orElseThrow(() -> new NotFoundException("Tag not found"));
    targetTag.update(updateDto);
    return tagRepository.save(targetTag);
  }

  public void deleteTag(Long tagId) {
    if (!tagRepository.existsById(tagId)) {
      throw new NotFoundException("Tag not found");
    }
    tagRepository.deleteById(tagId);
  }

}
