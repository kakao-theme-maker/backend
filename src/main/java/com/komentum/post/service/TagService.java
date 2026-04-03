package com.komentum.post.service;

import com.komentum.post.domain.Post;
import com.komentum.post.domain.Tag;
import com.komentum.post.dto.TagDto.TagCreateDto;
import com.komentum.post.dto.TagDto.TagUpdateDto;
import com.komentum.post.repository.TagRepository;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class TagService {

  public final TagRepository tagRepository;

  public List<Tag> findAllByPostId(Long postId) {
    return tagRepository.findAllByPost_PostId(postId);
  }

  public List<Tag> getTagsByPostId(Long postId) {
    return tagRepository.findAllByPost_PostId(postId);
  }

  public Map<Long, List<Tag>> getTagPerPosts(List<Long> postIds) {
    return tagRepository.fetchJoinAllByPostIds(postIds)
        .stream()
        .collect(Collectors.groupingBy(t -> t.getPost().getPostId()));
  }

  @Transactional
  public void synchronizeTags(Post post, List<TagUpdateDto> updateDtoList) {
    List<Tag> prevTags = findAllByPostId(post.getPostId());
    // 덮어쓸 태그 목록
    Set<String> newTagNames = updateDtoList.stream()
        .map(TagUpdateDto::getTagName)
        .filter(StringUtils::hasText)
        .collect(Collectors.toSet());
    // 기존 태그 목록
    Set<String> prevTagNames = prevTags.stream()
        .map(Tag::getTagName)
        .filter(StringUtils::hasText)
        .collect(Collectors.toSet());
    // 삭제 대상
    List<Tag> tagsToDelete = prevTags.stream()
        .filter(tag -> !newTagNames.contains(tag.getTagName()))
        .toList();
    tagRepository.deleteAll(tagsToDelete);
    // 추가할 태그 목록
    List<Tag> tagsToAdd = newTagNames.stream()
        .filter(tagName -> !prevTagNames.contains(tagName))
        .map(tagName -> Tag.builder()
            .tagName(tagName)
            .post(post)
            .build())
        .toList();
    tagRepository.saveAll(tagsToAdd);
  }

  @Transactional
  public List<Tag> createTags(Post targetPost, List<TagCreateDto> tagCreateDtoList) {
    List<Tag> tags = tagCreateDtoList.stream()
        .map(tag -> Tag.createTransient(tag, targetPost)).toList();
    return tagRepository.saveAll(tags);
  }

  @Transactional
  public Tag updateTag(Long tagId, TagUpdateDto updateDto) {
    Tag targetTag = tagRepository.findById(tagId)
        .orElseThrow(() -> new RuntimeException("Tag not found"));
    targetTag.update(updateDto);
    return tagRepository.save(targetTag);
  }

  @Transactional
  public void deleteTag(Long tagId) {
    if (!tagRepository.existsById(tagId)) {
      throw new RuntimeException("Tag not found");
    }
    tagRepository.deleteById(tagId);
  }

}
