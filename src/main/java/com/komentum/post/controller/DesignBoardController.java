package com.komentum.post.controller;

import com.komentum.global.dto.PageableRequestDto;
import com.komentum.post.dto.DesignBoardDto;
import com.komentum.post.dto.DesignBoardDto.DesignBoardCreateDto;
import com.komentum.post.dto.DesignBoardDto.DesignBoardDetailDto;
import com.komentum.post.dto.DesignBoardDto.DesignBoardPreviewDto;
import com.komentum.post.facade.DesignBoardManagementFacade;
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
@RequestMapping("/api/design-boards")
@RequiredArgsConstructor
public class DesignBoardController {

  private final DesignBoardManagementFacade designBoardManagementFacade;

  @GetMapping
  public ResponseEntity<List<DesignBoardPreviewDto>> findDesignBoards(
      @ModelAttribute PageableRequestDto pageableRequestDto) {
    return ResponseEntity.ok(
        designBoardManagementFacade.findBoardPreviews(pageableRequestDto.toPageable()));
  }

  @GetMapping("/{board_id}")
  public ResponseEntity<DesignBoardDetailDto> findDesignBoardDetail(
      @PathVariable("board_id") Long boardId
  ) {
    return ResponseEntity.ok(designBoardManagementFacade.findBoardDetail(boardId));
  }

  @PostMapping
  public ResponseEntity<DesignBoardDetailDto> createDesignBoard(
      @RequestPart("board_info") DesignBoardCreateDto createDto,
      @RequestPart("profile_image") MultipartFile profileImage
  ) {
    return ResponseEntity.ok(
        designBoardManagementFacade.createBoardWithTags(createDto, profileImage));
  }

  @PutMapping("/{board_id}")
  public ResponseEntity<DesignBoardDetailDto> updateDesignBoard(
      @PathVariable("board_id") Long boardId,
      @RequestBody DesignBoardDto.DesignBoardUpdateDto updateDto
  ) {
    return ResponseEntity.ok(designBoardManagementFacade.updateBoardWithTags(boardId, updateDto));
  }

  @DeleteMapping("/{board_id}")
  public ResponseEntity<DesignBoardDetailDto> deleteDesignBoard(
      @PathVariable("board_id") Long boardId) {
    designBoardManagementFacade.deleteBoardDetailWithPost(boardId);
    return ResponseEntity.noContent().build();
  }
}
