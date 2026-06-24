package com.komentum.post.service.transaction;

import com.komentum.designcomponent.domain.ComponentType;
import com.komentum.designcomponent.enums.TypeCode;
import com.komentum.post.domain.Post;
import com.komentum.post.domain.Tag;
import com.komentum.post.domain.enums.PostType;
import com.komentum.post.dto.BoardComponentTypeDto;
import com.komentum.post.dto.PostDto.PostCreateDto;
import com.komentum.post.dto.PostDto.PostUpdateDto;
import com.komentum.post.dto.ThemeBoardDto.ThemeBoardCreateDto;
import com.komentum.post.dto.ThemeBoardDto.ThemeBoardDetailDto;
import com.komentum.post.dto.ThemeBoardDto.ThemeBoardUpdateDto;
import com.komentum.post.dto.query.ThemeBoardQuery;
import com.komentum.post.facade.BoardManagementHelper;
import com.komentum.post.mapper.PostDtoMapper;
import com.komentum.post.mapper.ThemeBoardMapperSupport;
import com.komentum.post.repository.ThemeBoardRepository;
import com.komentum.post.repository.ThemeBoardRepositorySupport;
import com.komentum.post.service.PostService;
import com.komentum.post.service.TagService;
import com.komentum.post.service.ThemeBoardService;
import com.komentum.theme.core.domain.ThemeComponent;
import com.komentum.theme.core.domain.ThemeImage;
import com.komentum.theme.core.service.ThemeImageService;
import com.komentum.user.domain.User;
import com.komentum.user.service.UserEntityFinder;
import jakarta.persistence.EntityNotFoundException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ThemeBoardTransactionService {

  private final PostDtoMapper postDtoMapper;
  private final BoardManagementHelper helper;
  private final ThemeBoardMapperSupport themeBoardMapperSupport;
  private final ThemeBoardService themeBoardService;
  private final PostService postService;
  private final TagService tagService;
  private final UserEntityFinder userEntityFinder;
  private final ThemeBoardRepositorySupport themeBoardRepositorySupport;
  private final ThemeBoardRepository themeBoardRepository;
  private final ThemeImageService themeImageService;

  @Transactional(readOnly = true)
  public ThemeBoardDetailDto findThemeBoardDetail(Long postId, String userIdentifier) {
    if (!themeBoardRepository.existsByPost_PostId(postId)) {
      throw new EntityNotFoundException("cannot find theme board with post id = " + postId);
    }
    ThemeBoardQuery.Detail detail = themeBoardRepositorySupport
        .findThemeBoardQueryDetail(postId, userEntityFinder.findUserEntity(userIdentifier));
    List<Tag> tags = tagService.findAllByPostId(postId);
    List<ThemeImage> themeImages = themeImageService.findWithComponentTypeByThemeComponentIds(
        List.of(detail.getThemeComponentId()));
    return themeBoardMapperSupport.toThemeBoardDetailDto(detail, tags, helper,
        toComponentTypeDtos(themeImages));
  }

  private List<BoardComponentTypeDto> toComponentTypeDtos(List<ThemeImage> themeImages) {
    Map<TypeCode, BoardComponentTypeDto> componentTypeMap = new LinkedHashMap<>();
    for (ThemeImage themeImage : themeImages) {
      ComponentType componentType = themeImage.getComponentType();
      componentTypeMap.putIfAbsent(componentType.getTypeCode(),
          BoardComponentTypeDto.from(componentType));
    }
    return List.copyOf(componentTypeMap.values());
  }

  @Transactional
  public Post saveThemeBoardAndReturnPost(
      ThemeBoardCreateDto createDto,
      ThemeComponent themeComponent,
      User author,
      String previewImageName) {
    PostCreateDto postCreateDto = postDtoMapper.toPostCreateDto(createDto);
    Post savedPost = postService
        .createPost(postCreateDto, author, previewImageName, PostType.THEME_BOARD);
    if (createDto.getPostTags() != null) {
      tagService.createTags(savedPost, createDto.getPostTags());
    }
    themeBoardService.save(savedPost, themeComponent);
    return savedPost;
  }

  @Transactional
  public String updateThemeBoardAndGetOldFileName(
      Long postId,
      ThemeBoardUpdateDto updateDto,
      String previewImageName
  ) {
    if (!themeBoardRepository.existsByPost_PostId(postId)) {
      throw new EntityNotFoundException("cannot find theme board with post id = " + postId);
    }
    PostUpdateDto postUpdateDto = postDtoMapper.toPostUpdateDto(updateDto, previewImageName);
    Post targetPost = postService.getPostByPostId(postId);
    String oldImageName = targetPost.getPreviewImageName();
    targetPost.update(postUpdateDto);
    if (updateDto.getPostTags() != null) {
      tagService.synchronizeTags(targetPost, updateDto.getPostTags());
    }
    return oldImageName;
  }
}
