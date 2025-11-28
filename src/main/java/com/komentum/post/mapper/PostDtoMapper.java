package com.komentum.post.mapper;

import com.komentum.post.dto.DesignBoardDto.DesignBoardCreateDto;
import com.komentum.post.dto.DesignBoardDto.DesignBoardUpdateDto;
import com.komentum.post.dto.PostDto.PostCreateDto;
import com.komentum.post.dto.PostDto.PostUpdateDto;
import com.komentum.post.dto.ThemeBoardDto.ThemeBoardCreateDto;
import com.komentum.post.dto.ThemeBoardDto.ThemeBoardUpdateDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PostDtoMapper {

  // ThemeBoard
  PostCreateDto toPostCreateDto(ThemeBoardCreateDto dto);

  PostUpdateDto toPostUpdateDto(ThemeBoardUpdateDto dto);

  // DesignBoard
  PostCreateDto toPostCreateDto(DesignBoardCreateDto dto);

  PostUpdateDto toPostUpdateDto(DesignBoardUpdateDto dto);
}
