package com.komentum.post.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

import com.fasterxml.jackson.core.type.TypeReference;
import com.komentum.config.EnableTestProfile;
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
import com.komentum.test.BoardDetailDataGenerator;
import com.komentum.test.MockMvcUtils;
import com.komentum.theme.component.domain.DesignComponent;
import com.komentum.user.domain.User;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
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
    DesignBoard savedData = designBoardRepository.findByPost_PostId(response.getBoardId())
        .orElse(null);
    assertThat(savedData)
        .isNotNull();
    Post post = savedData.getPost();
    assertThat(post.getTitle())
        .isEqualTo(expectedTitle);
    assertThat(post.getContent())
        .isEqualTo(expectedContent);
  }

  private void assertDesignBoard(DesignBoardDetailDto response, String expectedTitle,
      String expectedContent, List<String> expectedTags) {
    // field assertion
    assertThat(response.getTags()).extracting(TagResponse::getTagName)
        .containsExactlyElementsOf(expectedTags);
    // field and DB assertion
    assertDesignBoard(response, expectedTitle, expectedContent);
  }

  @Test
  @DisplayName("when send request, retrieve list of design board infos")
  public void whenSendRequest_retrieveListOfDesignBoards() throws Exception {
    // given
    int pageSize = 2;
    int pageNumber = 0;
    User client = boardDetailDataGenerator.getUsers().get(0);
    MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
    params.add("pageSize", Integer.toString(pageSize));
    params.add("pageNumber", Integer.toString(pageNumber));
    // when
    List<DesignBoardPreviewDto> responses = mockMvcUtils.requestGet(mockMvc,
        "/api/design-boards", params, client.getUserEmail(),
        new TypeReference<>() {
        });
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
    DesignBoardDetailDto response = mockMvcUtils.requestGet(mockMvc, requestPath, null,
        client.getUserEmail(), new TypeReference<>() {
        });
    // then
    assertDesignBoard(response, targetPost.getTitle(), targetPost.getContent());
  }

  @Test
  @DisplayName("when send request, save and return design board info")
  public void whenSendRequest_saveAndReturnDesignBoard() throws Exception {
    // given
    String requestPath = "/api/design-boards";
    DesignComponent unsavedBoardDesignComponent = boardDetailDataGenerator.getNonDesignBoardDesignComponents()
        .get(0);
    User author = boardDetailDataGenerator.getUsers().get(0);
    List<String> tags = IntStream.range(0, 5).mapToObj(i -> UUID.randomUUID().toString()).toList();
    DesignBoardCreateDto createDto = DesignBoardCreateDto.builder()
        .title("test title")
        .content("test content")
        .designComponentId(unsavedBoardDesignComponent.getDesignComponentId())
        .postTags(tags.stream().map(t -> TagCreateDto.builder().tagName(t).build()).toList())
        .userEmail(author.getUserEmail())
        .publicFlag(true)
        .build();
    MockMultipartFile boardInfo = mockMvcUtils.jsonToTestFormData("board_info", createDto);
    MockMultipartFile profileImage = mockMvcUtils.fileToTestFormData("profile_image",
        "profile_image.png", MediaType.IMAGE_PNG, "test data".getBytes());
    List<MockMultipartFile> formDataList = List.of(boardInfo, profileImage);
    // stub
    String expectedProfileImageUrl = UUID.randomUUID().toString();
    Mockito.when(fileManager.resolveFilePath(anyString()))
        .thenReturn(expectedProfileImageUrl);
    Mockito.when(
            fileManager.uploadFile(any(byte[].class), anyString()))
        .thenReturn(UUID.randomUUID().toString());
    // when
    DesignBoardDetailDto response = mockMvcUtils.performMultipartRequest(mockMvc, requestPath,
        HttpMethod.POST, null, author.getUserEmail(), formDataList, new TypeReference<>() {
        });
    // then : 필드 및 DB 검증
    assertDesignBoard(response, createDto.getTitle(), createDto.getContent(), tags);
  }

  @Test
  @DisplayName("when send request, update design board info")
  public void whenSendRequest_updateDesignBoard() throws Exception {
    // given
    DesignBoard targetDesignBoard = boardDetailDataGenerator.getDesignBoards().get(0);
    String requestPath = String.format("/api/design-boards/%d",
        targetDesignBoard.getPost().getPostId());
    User author = targetDesignBoard.getPost().getUser();
    String expectedTitle = UUID.randomUUID().toString();
    String expectedContent = UUID.randomUUID().toString();
    List<String> expectedTags = IntStream.range(0, 5).mapToObj(i -> UUID.randomUUID().toString())
        .toList();
    DesignBoardUpdateDto updateDto = DesignBoardUpdateDto.builder()
        .title(expectedTitle)
        .content(expectedContent)
        .postTags(
            expectedTags.stream().map(t -> TagUpdateDto.builder().tagName(t).build()).toList())
        .userEmail(author.getUserEmail())
        .publicFlag(false)
        .build();
    // when
    DesignBoardDetailDto response = mockMvcUtils.requestPut(mockMvc, requestPath, null,
        author.getUserEmail(), updateDto, new TypeReference<>() {
        });
    // then : 필드 및 DB 검증
    assertDesignBoard(response, expectedTitle, expectedContent, expectedTags);
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
    mockMvcUtils.requestDelete(mockMvc, requestPath, null, author.getUserEmail(), null,
        new TypeReference<Void>() {
        });
    // then
    assertThat(designBoardRepository.findById(targetDesignBoard.getDesignBoardId()))
        .isEmpty();
    assertThat(postRepository.findById(targetDesignBoard.getPost().getPostId()))
        .isEmpty();
  }
}
