package com.komentum.post.controller;

import com.komentum.global.dto.PageableRequestDto;
import com.komentum.post.dto.ThemeBoardDto.ThemeBoardCreateDto;
import com.komentum.post.dto.ThemeBoardDto.ThemeBoardDetailDto;
import com.komentum.post.dto.ThemeBoardDto.ThemeBoardPreviewDto;
import com.komentum.post.dto.ThemeBoardDto.ThemeBoardUpdateDto;
import com.komentum.post.facade.ThemeBoardManagementFacade;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/theme-boards")
@RequiredArgsConstructor
public class ThemeBoardController {

  private final ThemeBoardManagementFacade themeBoardManagementFacade;

  @GetMapping
  public ResponseEntity<List<ThemeBoardPreviewDto>> getPosts(
      @Valid @ModelAttribute PageableRequestDto pageableRequestDto) {
    return ResponseEntity.ok(
        themeBoardManagementFacade.findThemeBoardPreviews(pageableRequestDto.toPageable()));
  }

  @GetMapping("/{board_id}")
  public ResponseEntity<ThemeBoardDetailDto> getPost(@PathVariable("board_id") Long boardId) {
    return ResponseEntity.ok(themeBoardManagementFacade.findThemeBoardDetail(boardId));
  }

  @PostMapping
  public ResponseEntity<ThemeBoardDetailDto> createPost(
      @RequestPart("board_info") ThemeBoardCreateDto createDto,
      @RequestPart("preview_image") MultipartFile profileImage) {
    return ResponseEntity.ok(
        themeBoardManagementFacade.createThemeBoardWithTags(createDto, profileImage));
  }

  @PutMapping("/{board_id}")
  public ResponseEntity<ThemeBoardDetailDto> updatePost(@PathVariable("board_id") Long boardId,
      @RequestBody ThemeBoardUpdateDto updateDto) {
    return ResponseEntity.ok(
        themeBoardManagementFacade.updateThemeBoardWithTags(boardId, updateDto));
  }

  @DeleteMapping("/{board_id}")
  public ResponseEntity<Void> deletePost(@PathVariable("board_id") Long boardId) {
    themeBoardManagementFacade.deleteThemeBoard(boardId);
    return ResponseEntity.noContent().build();
  }
}
