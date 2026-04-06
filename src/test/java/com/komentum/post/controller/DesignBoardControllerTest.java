package com.komentum.post.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

import com.fasterxml.jackson.core.type.TypeReference;
import com.komentum.global.utils.FileManager;
import com.komentum.post.domain.DesignBoard;
import com.komentum.post.domain.Post;
import com.komentum.post.dto.DesignBoardDto.DesignBoardCreateDto;
import com.komentum.post.dto.DesignBoardDto.DesignBoardDetailDto;
import com.komentum.post.dto.DesignBoardDto.DesignBoardPreviewDto;
import com.komentum.post.dto.DesignBoardDto.DesignBoardUpdateDto;
import com.komentum.post.dto.TagDto.TagCreateDto;
import com.komentum.post.dto.TagDto.TagResponse;
import com.komentum.post.dto.TagDto.TagUpdateDto;
import com.komentum.post.repository.DesignBoardRepository;
import com.komentum.post.repository.PostRepository;
import com.komentum.test.MockMvcUtils;
import com.komentum.test.config.EnableTestProfile;
import com.komentum.test.data.BoardDetailDataGenerator;
import com.komentum.test.data.MockMultipartFileUtils;
import com.komentum.test.data.MockMultipartFileUtils.ImageExtension;
import com.komentum.test.dto.MockMvcMultipartRequestDto;
import com.komentum.test.dto.MockMvcRequestDto;
import com.komentum.test.dto.TestClientDto;
import com.komentum.theme.component.domain.DesignComponent;
import com.komentum.user.domain.User;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@SpringBootTest
@EnableTestProfile
@AutoConfigureMockMvc
public class DesignBoardControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private BoardDetailDataGenerator boardDetailDataGenerator;

  @Autowired
  private PostRepository postRepository;

  @Autowired
  private DesignBoardRepository designBoardRepository;

  @Autowired
  private MockMvcUtils mockMvcUtils;

  @Autowired
  private FileManager fileManager;

  @BeforeEach
  public void setUp() {
    boardDetailDataGenerator.deleteDesignBoards();
    boardDetailDataGenerator.generateDesignBoards(3, 3, 3);
  }

  @AfterEach
  public void tearDown() {
    boardDetailDataGenerator.deleteDesignBoards();
  }

  private void assertDesignBoard(DesignBoardDetailDto response, String expectedTitle,
      String expectedContent) {
    // field assertion
    assertThat(response)
        .isNotNull();
    assertThat(response.getTitle())
        .isEqualTo(expectedTitle);
    assertThat(response.getContent())
        .isEqualTo(expectedContent);
    // DB assertion
    DesignBoard savedData = designBoardRepository.findByPost_PostId(response.getPostId())
        .orElse(null);
    assertThat(savedData)
        .isNotNull();
    Post post = savedData.getPost();
    assertThat(post.getTitle())
        .isEqualTo(expectedTitle);
    assertThat(post.getContent())
        .isEqualTo(expectedContent);
  }

  @Test
  @DisplayName("when send request, retrieve list of design board infos")
  public void whenSendRequest_retrieveListOfDesignBoards() throws Exception {
    // given
    int pageSize = 2;
    int pageNumber = 0;
    User client = boardDetailDataGenerator.getUsers().get(0);
    MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
    params.add("size", Integer.toString(pageSize));
    params.add("page", Integer.toString(pageNumber));
    // when
    List<DesignBoardPreviewDto> responses = mockMvcUtils.doAuthRequest(
        MockMvcRequestDto.<Void, List<DesignBoardPreviewDto>>builder()
            .mockMvc(mockMvc)
            .httpMethod(HttpMethod.GET)
            .path("/api/design-boards")
            .params(params)
            .responseType(new TypeReference<>() {
            })
            .clientDto(TestClientDto.fromEntity(client))
            .build()
    );
    // then
    assertThat(responses).isNotNull().hasSize(pageSize);
  }

  @Test
  @DisplayName("when send request, retrieve design board by id")
  public void whenSendRequest_retrieveDesignBoardById() throws Exception {
    // given
    DesignBoard targetDesignBoard = boardDetailDataGenerator.getDesignBoards().get(0);
    Post targetPost = targetDesignBoard.getPost();
    String requestPath = String.format("/api/design-boards/%d",
        targetDesignBoard.getPost().getPostId());
    User client = boardDetailDataGenerator.getUsers().get(0);
    // when
    DesignBoardDetailDto response = mockMvcUtils.doAuthRequest(
        MockMvcRequestDto.<Void, DesignBoardDetailDto>builder()
            .mockMvc(mockMvc)
            .httpMethod(HttpMethod.GET)
            .path(requestPath)
            .responseType(new TypeReference<>() {
            })
            .clientDto(TestClientDto.fromEntity(client))
            .build()
    );
    // then
    assertDesignBoard(response, targetPost.getTitle(), targetPost.getContent());
  }

  @Test
  @DisplayName("when send request, save and return design board info")
  public void whenSendRequest_saveAndReturnDesignBoard() throws Exception {
    // given
    String expectedPreviewImageUrl = UUID.randomUUID().toString();
    DesignComponent unsavedBoardDesignComponent = boardDetailDataGenerator.getNonDesignBoardDesignComponents()
        .get(0);
    User author = boardDetailDataGenerator.getUsers().get(0);
    List<String> tagNames = List.of("a", "b");
    List<TagCreateDto> tagCreateDtoList = tagNames.stream()
        .map(tagName -> TagCreateDto.builder().tagName(tagName).build())
        .toList();
    DesignBoardCreateDto createDto = DesignBoardCreateDto.builder()
        .title("test title")
        .content("test content")
        .designComponentId(unsavedBoardDesignComponent.getDesignComponentId())
        .publicFlag(true)
        .postTags(tagCreateDtoList)
        .build();
    MockMultipartFile previewImage = MockMultipartFileUtils
        .generateImageFormData("preview_image", ImageExtension.PNG);
    MockMultipartFile boardInfo = MockMultipartFileUtils
        .generateJsonFormData("board_info", createDto);
    List<MockMultipartFile> formDataList = List.of(boardInfo, previewImage);
    // stub
    Mockito.when(fileManager.resolveFilePath(anyString()))
        .thenReturn(expectedPreviewImageUrl);
    Mockito.when(
            fileManager.uploadFile(any(byte[].class), anyString()))
        .thenReturn(UUID.randomUUID().toString());
    // when
    DesignBoardDetailDto response = mockMvcUtils.doAuthMultipartRequest(
        MockMvcMultipartRequestDto.<DesignBoardDetailDto>builder()
            .mockMvc(mockMvc)
            .path("/api/design-boards")
            .httpMethod(HttpMethod.POST)
            .formDataList(formDataList)
            .clientDto(TestClientDto.fromEntity(author))
            .responseType(new TypeReference<>() {
            })
            .build()
    );
    // then : 필드 및 DB 검증
    assertThat(response.getTags().stream().map(TagResponse::getTagName))
        .containsExactlyInAnyOrderElementsOf(tagNames);
    assertThat(response.getPreviewImageUrl())
        .isEqualTo(expectedPreviewImageUrl);
    assertDesignBoard(response, createDto.getTitle(), createDto.getContent());
  }

  @Test
  @DisplayName("when send request, update design board info")
  public void whenSendRequest_updateDesignBoard() throws Exception {
    // given
    String expectedPreviewImageUrl = UUID.randomUUID().toString();
    DesignBoard targetDesignBoard = boardDetailDataGenerator.getDesignBoards().get(0);
    String requestPath = String.format("/api/design-boards/%d",
        targetDesignBoard.getPost().getPostId());
    List<String> tagNames = List.of("a", "b");
    List<TagUpdateDto> tagUpdateDtoList = tagNames.stream()
        .map(tagName -> TagUpdateDto.builder().tagName(tagName).build())
        .toList();
    User author = targetDesignBoard.getPost().getUser();
    String expectedTitle = UUID.randomUUID().toString();
    String expectedContent = UUID.randomUUID().toString();
    DesignBoardUpdateDto updateDto = DesignBoardUpdateDto.builder()
        .title(expectedTitle)
        .content(expectedContent)
        .publicFlag(false)
        .postTags(tagUpdateDtoList)
        .build();
    MockMultipartFile previewImage = MockMultipartFileUtils
        .generateImageFormData("preview_image", ImageExtension.PNG);
    MockMultipartFile boardInfo = MockMultipartFileUtils
        .generateJsonFormData("board_info", updateDto);
    List<MockMultipartFile> formDataList = List.of(boardInfo, previewImage);
    // stub
    Mockito.when(fileManager.resolveFilePath(anyString()))
        .thenReturn(expectedPreviewImageUrl);
    Mockito.when(
            fileManager.uploadFile(any(byte[].class), anyString()))
        .thenReturn(UUID.randomUUID().toString());
    // when
    DesignBoardDetailDto response = mockMvcUtils.doAuthMultipartRequest(
        MockMvcMultipartRequestDto.<DesignBoardDetailDto>builder()
            .mockMvc(mockMvc)
            .path(requestPath)
            .httpMethod(HttpMethod.PATCH)
            .formDataList(formDataList)
            .clientDto(TestClientDto.fromEntity(author))
            .responseType(new TypeReference<>() {
            })
            .build()
    );
    // then : 필드 및 DB 검증
    assertThat(response.getTags().stream().map(TagResponse::getTagName))
        .containsExactlyInAnyOrderElementsOf(tagNames);
    assertDesignBoard(response, expectedTitle, expectedContent);
  }

  @Test
  @DisplayName("when send request, delete board info")
  public void whenSendRequest_deleteDesignBoard() throws Exception {
    // given
    DesignBoard targetDesignBoard = boardDetailDataGenerator.getDesignBoards().get(0);
    User author = targetDesignBoard.getPost().getUser();
    String requestPath = String.format("/api/design-boards/%d",
        targetDesignBoard.getPost().getPostId());
    // when
    mockMvcUtils.doAuthRequest(
        MockMvcRequestDto.<Void, Void>builder()
            .mockMvc(mockMvc)
            .path(requestPath)
            .httpMethod(HttpMethod.DELETE)
            .clientDto(TestClientDto.fromEntity(author))
            .statusCode(204)
            .responseType(new TypeReference<>() {
            })
            .build()
    );
    // then
    assertThat(designBoardRepository.findById(targetDesignBoard.getDesignBoardId()))
        .isEmpty();
    assertThat(postRepository.findById(targetDesignBoard.getPost().getPostId()))
        .isEmpty();
  }
}
