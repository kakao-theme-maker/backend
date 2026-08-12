package com.komentum.post.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

import com.fasterxml.jackson.core.type.TypeReference;
import com.komentum.designcomponent.domain.ComponentType;
import com.komentum.designcomponent.domain.DesignComponent;
import com.komentum.designcomponent.enums.TypeCode;
import com.komentum.designcomponent.repository.ComponentTypeRepository;
import com.komentum.designcomponent.repository.DesignComponentRepository;
import com.komentum.designcomponent.service.seeder.ComponentTypeSeeder;
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
import com.komentum.test.data.MockMultipartFileUtils;
import com.komentum.test.data.MockMultipartFileUtils.ImageExtension;
import com.komentum.test.data.TestDataRemover;
import com.komentum.test.data.scenario.DesignComponentScenarioSupport;
import com.komentum.test.data.scenario.PostScenarioSupport;
import com.komentum.test.data.scenario.UserScenarioSupport;
import com.komentum.test.dto.MockMvcMultipartRequestDto;
import com.komentum.test.dto.MockMvcRequestDto;
import com.komentum.test.dto.TestClientDto;
import com.komentum.test.dto.TestParams;
import com.komentum.user.domain.User;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
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
import org.springframework.util.MultiValueMap;

@SpringBootTest
@EnableTestProfile
@AutoConfigureMockMvc
public class DesignBoardControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private PostRepository postRepository;

  @Autowired
  private DesignBoardRepository designBoardRepository;

  @Autowired
  private DesignComponentRepository designComponentRepository;

  @Autowired
  private ComponentTypeRepository componentTypeRepository;

  @Autowired
  private ComponentTypeSeeder componentTypeSeeder;

  @Autowired
  private MockMvcUtils mockMvcUtils;

  @Autowired
  private FileManager fileManager;

  @Autowired
  private UserScenarioSupport userScenarioSupport;

  @Autowired
  private DesignComponentScenarioSupport designComponentScenarioSupport;

  @Autowired
  private PostScenarioSupport postScenarioSupport;

  @Autowired
  private TestDataRemover testDataRemover;

  UserScenarioSupport.UserScenarioResult userResult;
  DesignComponentScenarioSupport.DesignComponentScenarioResult designComponentResult;
  PostScenarioSupport.Result postResult;
  String expectedImageUrl;

  @BeforeEach
  public void setUp() {
    this.expectedImageUrl = UUID.randomUUID().toString();
    stubImageUploadAndRetrieve(expectedImageUrl);
    userResult = userScenarioSupport.builder() // 사용자 3명
        .withUsers(3)
        .build();
    designComponentResult = designComponentScenarioSupport // 사용자마다 3개의 design components
        .builder(userResult.users())
        .withCountPerUser(3)
        .build();
    Map<User, List<DesignComponent>> designComponentOwnerMap = designComponentResult.designComponents()
        .stream()
        .collect(Collectors.groupingBy(
            DesignComponent::getUser
        ));
    postResult = postScenarioSupport.builder(userResult.users()) // 사용자마다 3개의 design boards
        .withDesignBoardsPerUser(3, designComponentOwnerMap)
        .build();
  }

  @AfterEach
  public void tearDown() {
    testDataRemover.deleteAll();
  }

  private void assertDesignBoard(DesignBoardDetailDto response) {
    // DB assertion
    Post savedPost = postRepository.findById(response.getPostId())
        .orElse(null);
    List<DesignBoard> savedDesignBoards = designBoardRepository.findByPost_PostId(
        response.getPostId());
    assertThat(savedPost).isNotNull();
    assertThat(savedDesignBoards).isNotEmpty();
    // field assertion
    assertThat(response).isNotNull();
    assertThat(response.getTitle()).isEqualTo(savedPost.getTitle());
    assertThat(response.getContent()).isEqualTo(savedPost.getContent());
    assertThat(response.getPreviewImageUrl()).isNotEmpty();
    assertThat(response.getCreatedAt()).isNotBlank();
    assertThat(response.getUserEmail()).isNotBlank();
    assertThat(response.getUserName()).isNotBlank();
    assertThat(response.getComments()).isGreaterThanOrEqualTo(0);
    assertThat(response.getPrefers()).isGreaterThanOrEqualTo(0);
    assertThat(response.getTags()).isNotNull();
  }

  private void stubImageUploadAndRetrieve(String expectedImageUrl) {
    Mockito.when(fileManager.resolveFilePath(anyString()))
        .thenReturn(expectedImageUrl);
    Mockito.when(
            fileManager.uploadFile(any(byte[].class), anyString()))
        .thenReturn(expectedImageUrl);
  }

  private List<DesignBoardDetailDto> requestDesignBoardDetails(
      MultiValueMap<String, String> params, User client) throws Exception {
    return mockMvcUtils.doAuthRequest(
        MockMvcRequestDto.<Void, List<DesignBoardDetailDto>>builder()
            .mockMvc(mockMvc)
            .httpMethod(HttpMethod.GET)
            .path("/api/design-boards/details")
            .params(params)
            .responseType(new TypeReference<>() {
            })
            .clientDto(TestClientDto.fromEntity(client))
            .build()
    );
  }

  private ComponentType getComponentType(TypeCode typeCode) {
    componentTypeSeeder.upsertComponentType();
    return componentTypeRepository.findAllByTypeCodeIn(List.of(typeCode)).get(0);
  }

  @Test
  @DisplayName("when send request, retrieve list of design board infos")
  public void whenSendRequest_retrieveListOfDesignBoards() throws Exception {
    // given
    int pageSize = 2;
    int pageNumber = 0;
    User client = userResult.getFirstUser();
    MultiValueMap<String, String> params = TestParams.withPaging(pageNumber, pageSize);
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
    DesignBoard targetDesignBoard = postResult.designBoards().get(0);
    String requestPath = String.format("/api/design-boards/%d",
        targetDesignBoard.getPost().getPostId());
    User client = userResult.getFirstUser();
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
    assertDesignBoard(response);
  }

  @Test
  @DisplayName("If a pinned post ID is provided, place that post at the top of the first page and return only design boards written by the same author.")
  void findDesignBoardDetails_ifPinnedPostIdExists() throws Exception {
    // given
    DesignBoard targetDesignBoard = postResult.designBoards().get(0);
    Post pinnedPost = targetDesignBoard.getPost();
    MultiValueMap<String, String> params = TestParams.withPaging(0, 5);
    params.add("pinned_post_id", pinnedPost.getPostId().toString());
    User client = userResult.getFirstUser();
    // when
    List<DesignBoardDetailDto> response = mockMvcUtils.doAuthRequest(
        MockMvcRequestDto.<Void, List<DesignBoardDetailDto>>builder()
            .mockMvc(mockMvc)
            .httpMethod(HttpMethod.GET)
            .path("/api/design-boards/details")
            .params(params)
            .responseType(new TypeReference<>() {
            })
            .clientDto(TestClientDto.fromEntity(client))
            .build()
    );
    // then
    assertThat(response).hasSize(3);
    assertThat(response.get(0).getPostId()).isEqualTo(pinnedPost.getPostId());
    for (DesignBoardDetailDto dto : response) {
      assertDesignBoard(dto);
    }
  }

  @Test
  @DisplayName("keyword 검색 조건에 매칭되는 디자인 게시글 목록 정보를 최상단에 반환한다.")
  void findDesignBoards_keywordMatchedPostFirst() throws Exception {
    // given
    String keyword = "design-keyword-" + UUID.randomUUID();
    Post targetPost = postResult.posts().get(postResult.posts().size() - 1);
    targetPost.setTitle(keyword);
    postRepository.save(targetPost);
    MultiValueMap<String, String> params = TestParams.withPaging(0,
        postResult.designBoards().size());
    params.add("keyword", keyword);
    User client = userResult.getFirstUser();
    // when
    List<DesignBoardPreviewDto> response = mockMvcUtils.doAuthRequest(
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
    assertThat(response).hasSize(postResult.designBoards().size());
    assertThat(response.get(0).getPostId()).isEqualTo(targetPost.getPostId());
    assertThat(response)
        .extracting(DesignBoardPreviewDto::getPostId)
        .contains(postResult.posts().get(0).getPostId());
  }

  @Test
  @DisplayName("type_code 검색 조건에 매칭되는 디자인 게시글 목록 정보를 최상단에 반환한다.")
  void findDesignBoards_typeCodeMatchedPostFirst() throws Exception {
    // given
    TypeCode typeCode = TypeCode.CHAT_ROOM_BACKGROUND_IMAGE;
    ComponentType componentType = getComponentType(typeCode);
    DesignComponent targetComponent = designComponentResult.designComponents().get(0);
    String matchedImageUrl = "https://test.com/matched-design-image.png";
    targetComponent.update(matchedImageUrl, true);
    targetComponent.replaceComponentTypes(List.of(componentType));
    designComponentRepository.save(targetComponent);
    List<Long> matchedPostIds = postResult.designBoards().stream()
        .filter(designBoard -> designBoard.getDesignComponent().getDesignComponentId()
            .equals(targetComponent.getDesignComponentId()))
        .map(designBoard -> designBoard.getPost().getPostId())
        .distinct()
        .toList();
    MultiValueMap<String, String> params = TestParams.withPaging(0,
        postResult.designBoards().size());
    params.add("type_code", typeCode.getTypeCode());
    User client = userResult.getFirstUser();
    // when
    List<DesignBoardPreviewDto> response = mockMvcUtils.doAuthRequest(
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
    assertThat(response).hasSize(postResult.designBoards().size());
    assertThat(response.get(0).getPostId()).isIn(matchedPostIds);
    assertThat(response.get(0).getComponentTypes())
        .extracting(componentTypeDto -> componentTypeDto.getTypeCode().getTypeCode())
        .contains(typeCode.getTypeCode());
    assertThat(response)
        .extracting(DesignBoardPreviewDto::getPostId)
        .contains(postResult.posts().get(postResult.posts().size() - 1).getPostId());
  }

  @Test
  @DisplayName("지원하지 않는 type_code 요청 시 400을 반환한다.")
  void findDesignBoards_invalidTypeCode() throws Exception {
    // given
    MultiValueMap<String, String> params = TestParams.withPaging(0, 5);
    params.add("type_code", "invalidTypeCode");
    User client = userResult.getFirstUser();
    // when & then
    mockMvcUtils.doAuthRequest(
        MockMvcRequestDto.<Void, Void>builder()
            .mockMvc(mockMvc)
            .httpMethod(HttpMethod.GET)
            .path("/api/design-boards")
            .params(params)
            .responseType(new TypeReference<>() {
            })
            .clientDto(TestClientDto.fromEntity(client))
            .statusCode(400)
            .build()
    );
  }

  @Test
  @DisplayName("when send request, save and return design board info")
  public void whenSendRequest_saveAndReturnDesignBoard() throws Exception {
    // given
    List<DesignComponent> targetDesignComponents = designComponentResult.designComponents()
        .subList(0, 2);
    User author = userResult.getFirstUser();
    List<String> tagNames = List.of("a", "b");
    List<TagCreateDto> tagCreateDtoList = tagNames.stream()
        .map(tagName -> TagCreateDto.builder().tagName(tagName).build())
        .toList();
    List<Integer> designComponentIds = targetDesignComponents.stream()
        .map(DesignComponent::getDesignComponentId).toList();
    DesignBoardCreateDto createDto = DesignBoardCreateDto.builder()
        .title("test title")
        .content("test content")
        .designComponentIds(designComponentIds)
        .publicFlag(true)
        .postTags(tagCreateDtoList)
        .build();
    MockMultipartFile previewImage = MockMultipartFileUtils
        .generateImageFormData("preview_image", ImageExtension.PNG);
    MockMultipartFile boardInfo = MockMultipartFileUtils
        .generateJsonFormData("board_info", createDto);
    List<MockMultipartFile> formDataList = List.of(boardInfo, previewImage);
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
    assertThat(response.getPreviewImageUrl()).hasSize(
        targetDesignComponents.size() + 1);//대표 이미지 1개 + design component url 목록
    assertDesignBoard(response);
  }

  @Test
  @DisplayName("when send request, update design board info")
  public void whenSendRequest_updateDesignBoard() throws Exception {
    // given
    DesignBoard targetDesignBoard = postResult.designBoards().get(0);
    String requestPath = String.format("/api/design-boards/%d",
        targetDesignBoard.getPost().getPostId());
    List<String> tagNames = List.of("a", "b");
    List<TagUpdateDto> tagUpdateDtoList = tagNames.stream()
        .map(tagName -> TagUpdateDto.builder().tagName(tagName).build())
        .toList();
    User author = targetDesignBoard.getPost().getUser();
    List<Integer> designComponentIds = designComponentResult.designComponents().stream()
        .filter(dc -> dc.getUser().getUserId().equals(author.getUserId()))
        .map(DesignComponent::getDesignComponentId).toList();
    String expectedTitle = UUID.randomUUID().toString();
    String expectedContent = UUID.randomUUID().toString();
    DesignBoardUpdateDto updateDto = DesignBoardUpdateDto.builder()
        .title(expectedTitle)
        .content(expectedContent)
        .publicFlag(false)
        .designComponentIds(designComponentIds.subList(0, 2))
        .postTags(tagUpdateDtoList)
        .build();
    MockMultipartFile previewImage = MockMultipartFileUtils
        .generateImageFormData("preview_image", ImageExtension.PNG);
    MockMultipartFile boardInfo = MockMultipartFileUtils
        .generateJsonFormData("board_info", updateDto);
    List<MockMultipartFile> formDataList = List.of(boardInfo, previewImage);
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
    assertThat(response.getPreviewImageUrl()).hasSize(3);
    assertDesignBoard(response);
  }

  @Test
  @DisplayName("when send request, delete board info")
  public void whenSendRequest_deleteDesignBoard() throws Exception {
    // given
    DesignBoard targetDesignBoard = postResult.designBoards().get(0);
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
