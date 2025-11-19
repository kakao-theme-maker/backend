package com.komentum.post.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

import com.fasterxml.jackson.core.type.TypeReference;
import com.komentum.config.EnableTestProfile;
import com.komentum.global.utils.FileManager;
import com.komentum.post.domain.DesignBoard;
import com.komentum.post.dto.DesignBoardDto.DesignBoardCreateDto;
import com.komentum.post.dto.DesignBoardDto.DesignBoardDetailDto;
import com.komentum.post.dto.DesignBoardDto.DesignBoardPreviewDto;
import com.komentum.post.dto.TagDto.TagCreateDto;
import com.komentum.post.dto.TagDto.TagResponse;
import com.komentum.post.repository.DesignBoardRepository;
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
    String requestPath = String.format("/api/design-boards/%d",
        targetDesignBoard.getDesignBoardId());
    User client = boardDetailDataGenerator.getUsers().get(0);
    // when
    DesignBoardDetailDto response = mockMvcUtils.requestGet(mockMvc, requestPath, null,
        client.getUserEmail(), new TypeReference<>() {
        });
    // then
    assertThat(response.getBoardId())
        .isEqualTo(targetDesignBoard.getDesignBoardId());
    assertThat((long) response.getDesignComponentId())
        .isEqualTo(targetDesignBoard.getDesignBoardId());
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
    // then
    assertThat(response.getProfileImageUrl()).isEqualTo(expectedProfileImageUrl);
    assertThat(response.getDesignComponentId()).isEqualTo(
        (long) unsavedBoardDesignComponent.getDesignComponentId());
    assertThat(response.getTags())
        .extracting(TagResponse::getTagName)
        .containsExactlyInAnyOrder(tags.toArray(new String[]{}));
  }
}
