package com.komentum.post.service;

import com.komentum.post.domain.Post;
import com.komentum.post.domain.Tag;
import com.komentum.post.dto.TagDto.TagCreateDto;
import com.komentum.post.dto.TagDto.TagUpdateDto;
import com.komentum.post.repository.TagRepository;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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

  public List<Tag> synchronizeTags(Post post, List<TagUpdateDto> updateDtoList) {
    // delete tags that updateDtoList doesn't contain
    List<Tag> prevTags = findAllByPostId(post.getPostId());
    Map<String, TagUpdateDto> tagNameMap = updateDtoList.stream()
        .collect(Collectors.toMap(TagUpdateDto::getTagName, Function.identity()));
    List<Tag> tagsToDelete = prevTags.stream()
        .filter(t -> !tagNameMap.containsKey(t.getTagName()))
        .toList();
    tagRepository.deleteAll(tagsToDelete);
    // create tags that prev tag list doesn't contain
    Map<String, Tag> prevTagMap = prevTags.stream()
        .collect(Collectors.toMap(Tag::getTagName, Function.identity()));
    List<Tag> tagsToAdd = updateDtoList.stream()
        .filter(t -> !prevTagMap.containsKey(t.getTagName()))
        .map(t -> Tag.createTransient(t, post))
        .toList();
    return tagRepository.saveAll(tagsToAdd);
  }

  public List<Tag> createTags(Post targetPost, List<TagCreateDto> tagCreateDtoList) {
    List<Tag> tags = tagCreateDtoList.stream()
        .map(tag -> Tag.createTransient(tag, targetPost)).toList();
    return tagRepository.saveAll(tags);
  }

  public Tag updateTag(Long tagId, TagUpdateDto updateDto) {
    Tag targetTag = tagRepository.findById(tagId)
        .orElseThrow(() -> new RuntimeException("Tag not found"));
    targetTag.update(updateDto);
    return tagRepository.save(targetTag);
  }

  public void deleteTag(Long tagId) {
    if (!tagRepository.existsById(tagId)) {
      throw new RuntimeException("Tag not found");
    }
    tagRepository.deleteById(tagId);
  }

}
