package com.komentum.post.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import com.komentum.post.domain.Prefer;
import com.komentum.post.dto.BoardComponentTypeDto;
import com.komentum.post.dto.DesignBoardDto.DesignBoardCreateDto;
import com.komentum.post.dto.DesignBoardDto.DesignBoardDetailDto;
import com.komentum.post.dto.DesignBoardDto.DesignBoardPreviewDto;
import com.komentum.post.dto.DesignBoardDto.DesignBoardUpdateDto;
import com.komentum.post.dto.DesignBoardDto.DesignComponentMappingDto;
import com.komentum.post.dto.TagDto.TagCreateDto;
import com.komentum.post.dto.TagDto.TagResponse;
import com.komentum.post.dto.TagDto.TagUpdateDto;
import com.komentum.post.repository.DesignBoardRepository;
import com.komentum.post.repository.PostRepository;
import com.komentum.post.repository.PreferRepository;
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
import com.komentum.test.fixture.component.DesignComponentFixture;
import com.komentum.test.fixture.post.DesignBoardFixture;
import com.komentum.user.domain.Follow;
import com.komentum.user.domain.User;
import com.komentum.user.repository.FollowRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
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
  private PreferRepository preferRepository;

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

  @Autowired
  private FollowRepository followRepository;

  UserScenarioSupport.UserScenarioResult userResult;
  DesignComponentScenarioSupport.DesignComponentScenarioResult designComponentResult;
  PostScenarioSupport.Result postResult;
  String expectedImageUrl;

  @BeforeEach
  public void setUp() {
    componentTypeSeeder.upsertComponentType();
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
    Mockito.when(fileManager.resolvePublicFileUrl(anyString()))
        .thenReturn(expectedImageUrl);
    Mockito.when(
            fileManager.uploadPublicFile(any(byte[].class), anyString()))
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

  private List<DesignBoardPreviewDto> requestDesignBoardPreviews(
      MultiValueMap<String, String> params, User client) throws Exception {
    return mockMvcUtils.doAuthRequest(
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
  }

  private void prepareDesignBoardSortData() {
    LocalDateTime baseDate = LocalDateTime.of(2025, 1, 1, 0, 0);
    List<Prefer> prefers = new ArrayList<>();
    for (int i = 0; i < postResult.posts().size(); i++) {
      Post post = postResult.posts().get(i);
      post.setCreatedAt(baseDate.plusDays(i));
      for (int j = 0; j < i % 3; j++) {
        prefers.add(Prefer.createTransient(post, userResult.users().get(j)));
      }
    }
    postRepository.saveAll(postResult.posts());
    preferRepository.saveAll(prefers);
  }

  private Comparator<DesignBoardPreviewDto> designBoardComparator(String sortType) {
    Comparator<DesignBoardPreviewDto> createdAtDesc = Comparator.comparing(
        DesignBoardPreviewDto::getCreatedAt, Comparator.reverseOrder());
    Comparator<DesignBoardPreviewDto> postIdDesc = Comparator.comparing(
        DesignBoardPreviewDto::getPostId, Comparator.reverseOrder());
    return switch (sortType == null ? "CREATED_DESC" : sortType) {
      case "CREATED_ASC" -> Comparator.comparing(DesignBoardPreviewDto::getCreatedAt)
          .thenComparing(postIdDesc);
      case "CREATED_DESC" -> createdAtDesc.thenComparing(postIdDesc);
      case "PREFER_ASC" -> Comparator.comparing(DesignBoardPreviewDto::getPrefers)
          .thenComparing(createdAtDesc)
          .thenComparing(postIdDesc);
      case "PREFER_DESC" -> Comparator.comparing(
              DesignBoardPreviewDto::getPrefers, Comparator.reverseOrder())
          .thenComparing(createdAtDesc)
          .thenComparing(postIdDesc);
      default -> throw new IllegalArgumentException("unsupported sort type: " + sortType);
    };
  }

  private ComponentType getComponentType(TypeCode typeCode) {
    return componentTypeRepository.findAllByTypeCodeIn(List.of(typeCode)).get(0);
  }

  /**
   * 테스트 에셋 목록을 기본 타입 하나를 갖는 게시글 매핑 요청으로 변환한다.
   */
  private List<DesignComponentMappingDto> mappingsFor(List<Integer> componentIds) {
    TypeCode typeCode = componentTypeRepository.findAll().get(0).getTypeCode();
    return componentIds.stream()
        .map(id -> DesignComponentMappingDto.builder()
            .designComponentId(id).typeCodes(List.of(typeCode)).build())
        .toList();
  }

  /**
   * 기존 게시글에서 사용하지 않는 타입을 삭제해 DB에 없는 유효한 TypeCode를 준비한다.
   */
  private TypeCode removeUnusedComponentType() {
    List<TypeCode> usedTypeCodes = postResult.designBoards().stream()
        .map(board -> board.getComponentType().getTypeCode()).toList();
    ComponentType unusedType = componentTypeRepository.findAll().stream()
        .filter(type -> !usedTypeCodes.contains(type.getTypeCode()))
        .findFirst().orElseThrow();
    componentTypeRepository.delete(unusedType);
    componentTypeRepository.flush();
    return unusedType.getTypeCode();
  }

  /**
   * 게시글 생성 또는 수정 요청을 수행하고 예상 상태 코드를 검증한다.
   */
  private DesignBoardDetailDto requestBoardMutation(HttpMethod method, Long postId,
      Object request, User author, int statusCode) throws Exception {
    String path = postId == null ? "/api/design-boards" : "/api/design-boards/" + postId;
    List<MockMultipartFile> formData = List.of(
        MockMultipartFileUtils.generateJsonFormData("boardInfo", request),
        MockMultipartFileUtils.generateImageFormData("previewImage", ImageExtension.PNG));
    if (statusCode >= 400) {
      Map<String, String> errors = mockMvcUtils.doAuthMultipartRequest(
          MockMvcMultipartRequestDto.<Map<String, String>>builder()
              .mockMvc(mockMvc)
              .path(path)
              .httpMethod(method)
              .formDataList(formData)
              .clientDto(TestClientDto.fromEntity(author))
              .statusCode(statusCode)
              .responseType(new TypeReference<>() {
              })
              .build());
      assertThat(errors).isNotEmpty();
      return null;
    }
    return mockMvcUtils.doAuthMultipartRequest(
        MockMvcMultipartRequestDto.<DesignBoardDetailDto>builder()
            .mockMvc(mockMvc)
            .path(path)
            .httpMethod(method)
            .formDataList(formData)
            .clientDto(TestClientDto.fromEntity(author))
            .statusCode(statusCode)
            .responseType(new TypeReference<>() {
            })
            .build());
  }

  /**
   * 소유자 인증으로 에셋을 비활성화한다.
   */
  private void deleteComponent(DesignComponent component) throws Exception {
    mockMvc.perform(mockMvcUtils.addAuthentication(
            delete("/api/design-components/{id}", component.getDesignComponentId()),
            TestClientDto.fromEntity(component.getUser())))
        .andExpect(status().isNoContent());
  }

  /**
   * 지정한 사용자 인증으로 디자인 게시글 상세를 조회한다.
   */
  private DesignBoardDetailDto requestDesignBoardDetail(Long postId, User client) throws Exception {
    return mockMvcUtils.doAuthRequest(
        MockMvcRequestDto.<Void, DesignBoardDetailDto>builder()
            .mockMvc(mockMvc)
            .httpMethod(HttpMethod.GET)
            .path("/api/design-boards/" + postId)
            .clientDto(TestClientDto.fromEntity(client))
            .responseType(new TypeReference<>() {
            })
            .build());
  }

  @Test
  @DisplayName("일부 에셋 삭제 후 게시글 단건·목록은 활성 ID·이미지·타입만 표시하고 대표 이미지를 교체한다")
  void partiallyInactiveDesignPost_onlyExposesActiveAssets() throws Exception {
    Post targetPost = postResult.posts().get(0);
    List<DesignComponent> components = designComponentResult.designComponents().stream()
        .filter(component -> component.getUser().getUserId().equals(targetPost.getUser().getUserId()))
        .sorted(Comparator.comparing(DesignComponent::getDesignComponentId))
        .toList();
    List<TypeCode> typeCodes = List.of(TypeCode.COMMON_ICO_THEME,
        TypeCode.MAINVIEW_STYLE_PRIMARY_BACKGROUND_IMAGE, TypeCode.TABBAR_STYLE_BACKGROUND_IMAGE);
    List<DesignBoard> targetBoards = designBoardRepository.findByPost_PostId(targetPost.getPostId());
    for (int i = 0; i < components.size(); i++) {
      ComponentType componentType = getComponentType(typeCodes.get(i));
      Integer componentId = components.get(i).getDesignComponentId();
      targetBoards.stream()
          .filter(board -> board.getDesignComponent().getDesignComponentId().equals(componentId))
          .forEach(board -> board.setComponentType(componentType));
    }
    designBoardRepository.saveAllAndFlush(targetBoards);
    targetPost.setPreviewImageName("partial-composite.png");
    postRepository.saveAndFlush(targetPost);
    Mockito.when(fileManager.resolvePublicFileUrl(anyString()))
        .thenAnswer(invocation -> "http://mocked-url/" + invocation.getArgument(0, String.class));
    deleteComponent(components.get(0));

    String activePreviewUrl = "http://mocked-url/" + components.get(1).getFileName();
    List<Integer> activeIds = components.subList(1, 3).stream()
        .map(DesignComponent::getDesignComponentId).toList();
    List<TypeCode> activeTypes = typeCodes.subList(1, 3);
    List<DesignBoardPreviewDto> previews = requestDesignBoardPreviews(TestParams.withPaging(0, 100),
        targetPost.getUser()).stream()
        .filter(dto -> dto.getPostId().equals(targetPost.getPostId()))
        .toList();
    assertThat(previews).singleElement().satisfies(dto -> {
      assertThat(dto.getPreviewImageUrl()).isEqualTo(activePreviewUrl);
      assertThat(dto.getComponentTypes()).extracting(BoardComponentTypeDto::getTypeCode)
          .containsExactlyInAnyOrderElementsOf(activeTypes);
    });

    DesignBoardDetailDto detail = requestDesignBoardDetail(targetPost.getPostId(), targetPost.getUser());
    DesignBoardDetailDto listedDetail = requestDesignBoardDetails(TestParams.withPaging(0, 100),
        targetPost.getUser()).stream()
        .filter(dto -> dto.getPostId().equals(targetPost.getPostId()))
        .findFirst().orElseThrow();
    for (DesignBoardDetailDto dto : List.of(detail, listedDetail)) {
      assertThat(dto.getDesignComponentMappings()).extracting(DesignComponentMappingDto::getDesignComponentId)
          .containsExactlyInAnyOrderElementsOf(activeIds);
      assertThat(dto.getPreviewImageUrl()).hasSize(3).first().isEqualTo(activePreviewUrl);
      assertThat(dto.getPreviewImageUrl()).contains("http://mocked-url/" + components.get(2).getFileName())
          .doesNotContain("http://mocked-url/" + components.get(0).getFileName(),
              "http://mocked-url/partial-composite.png");
      assertThat(dto.getComponentTypes()).extracting(BoardComponentTypeDto::getTypeCode)
          .containsExactlyInAnyOrderElementsOf(activeTypes);
    }
    assertThat(designBoardRepository.findByPost_PostId(targetPost.getPostId())).hasSize(3);
    assertThat(designComponentRepository.findById(components.get(0).getDesignComponentId()))
        .get().extracting(DesignComponent::getIsActive).isEqualTo(false);
  }

  @Test
  @DisplayName("모든 에셋 삭제 후 게시글 단건은 404이며 목록은 페이징 전에 게시글을 제외한다")
  void whollyInactiveDesignPosts_areHiddenBeforePagination() throws Exception {
    Post targetPost = postResult.posts().stream().max(Comparator.comparing(Post::getPostId))
        .orElseThrow();
    for (DesignComponent component : designComponentResult.designComponents()) {
      if (component.getUser().getUserId().equals(targetPost.getUser().getUserId())) {
        deleteComponent(component);
      }
    }
    List<Long> hiddenPostIds = postResult.posts().stream()
        .filter(post -> post.getUser().getUserId().equals(targetPost.getUser().getUserId()))
        .map(Post::getPostId).toList();
    mockMvc.perform(get("/api/design-boards/{postId}", targetPost.getPostId()))
        .andExpect(status().isNotFound());
    assertThat(requestDesignBoardPreviews(TestParams.withPaging(0, 100), targetPost.getUser()))
        .extracting(DesignBoardPreviewDto::getPostId)
        .doesNotContainAnyElementsOf(hiddenPostIds);
    assertThat(requestDesignBoardDetails(TestParams.withPaging(0, 100), targetPost.getUser()))
        .extracting(DesignBoardDetailDto::getPostId)
        .doesNotContainAnyElementsOf(hiddenPostIds);
    assertThat(requestDesignBoardPreviews(TestParams.withPaging(0, 1), targetPost.getUser())).hasSize(1);
    assertThat(requestDesignBoardDetails(TestParams.withPaging(0, 1), targetPost.getUser())).hasSize(1);
    assertThat(postRepository.findById(targetPost.getPostId())).isPresent();
    assertThat(designBoardRepository.findByPost_PostId(targetPost.getPostId())).hasSize(3);
  }

  @Test
  @DisplayName("삭제된 에셋을 사용한 게시글 생성은 파일 업로드 전에 404로 거절한다")
  void createDesignBoard_inactiveComponentIsRejectedBeforeUpload() throws Exception {
    DesignComponent component = designComponentResult.designComponents().get(0);
    deleteComponent(component);
    long postCount = postRepository.count();
    long boardCount = designBoardRepository.count();
    DesignBoardCreateDto createDto = DesignBoardCreateDto.builder()
        .title("inactive asset").content("must not be saved")
        .designComponentMappings(mappingsFor(List.of(component.getDesignComponentId())))
        .postTags(List.of()).build();
    Mockito.clearInvocations(fileManager);
    mockMvcUtils.doAuthMultipartRequest(MockMvcMultipartRequestDto.<Void>builder()
        .mockMvc(mockMvc).path("/api/design-boards").httpMethod(HttpMethod.POST)
        .clientDto(TestClientDto.fromEntity(component.getUser())).statusCode(404)
        .formDataList(List.of(MockMultipartFileUtils.generateJsonFormData("boardInfo", createDto),
            MockMultipartFileUtils.generateImageFormData("previewImage", ImageExtension.PNG)))
        .responseType(new TypeReference<>() {
        }).build());
    Mockito.verify(fileManager, Mockito.never()).uploadPublicFile(any(byte[].class), anyString());
    assertThat(postRepository.count()).isEqualTo(postCount);
    assertThat(designBoardRepository.count()).isEqualTo(boardCount);
  }

  @Test
  @DisplayName("삭제된 에셋을 새로 연결하는 게시글 수정은 404이며 기존 데이터가 유지된다")
  void updateDesignBoard_newInactiveComponentIsRejectedBeforeMutation() throws Exception {
    Post targetPost = postResult.posts().get(0);
    DesignComponent replacement = designComponentRepository.saveAndFlush(
        DesignComponentFixture.designComponent(targetPost.getUser(), "inactive-replacement.png"));
    deleteComponent(replacement);
    List<Integer> originalIds = designBoardRepository.findByPost_PostId(targetPost.getPostId()).stream()
        .map(board -> board.getDesignComponent().getDesignComponentId()).toList();
    String originalTitle = targetPost.getTitle();
    String originalContent = targetPost.getContent();
    String originalPreview = targetPost.getPreviewImageName();
    long boardCount = designBoardRepository.count();
    DesignBoardUpdateDto updateDto = DesignBoardUpdateDto.builder()
        .title("must roll back").content("must roll back")
        .designComponentMappings(mappingsFor(List.of(originalIds.get(0), replacement.getDesignComponentId())))
        .postTags(List.of(TagUpdateDto.builder().tagName("must roll back").build())).build();
    mockMvcUtils.doAuthMultipartRequest(MockMvcMultipartRequestDto.<Void>builder()
        .mockMvc(mockMvc).path("/api/design-boards/" + targetPost.getPostId()).httpMethod(HttpMethod.PATCH)
        .clientDto(TestClientDto.fromEntity(targetPost.getUser())).statusCode(404)
        .formDataList(List.of(MockMultipartFileUtils.generateJsonFormData("boardInfo", updateDto),
            MockMultipartFileUtils.generateImageFormData("previewImage", ImageExtension.PNG)))
        .responseType(new TypeReference<>() {
        }).build());
    Post unchanged = postRepository.findById(targetPost.getPostId()).orElseThrow();
    assertThat(unchanged.getTitle()).isEqualTo(originalTitle);
    assertThat(unchanged.getContent()).isEqualTo(originalContent);
    assertThat(unchanged.getPreviewImageName()).isEqualTo(originalPreview);
    assertThat(designBoardRepository.findByPost_PostId(targetPost.getPostId()))
        .extracting(board -> board.getDesignComponent().getDesignComponentId())
        .containsExactlyInAnyOrderElementsOf(originalIds);
    assertThat(designBoardRepository.count()).isEqualTo(boardCount);
    assertThat(requestDesignBoardDetail(targetPost.getPostId(), targetPost.getUser()).getTags()).isEmpty();
  }

  @Test
  @DisplayName("기존 삭제된 에셋 연결을 유지하는 게시글 수정은 성공하고 활성 에셋만 응답한다")
  void updateDesignBoard_retainedInactiveComponentRemainsLinked() throws Exception {
    Post targetPost = postResult.posts().get(0);
    List<DesignComponent> components = designComponentResult.designComponents().stream()
        .filter(component -> component.getUser().getUserId().equals(targetPost.getUser().getUserId()))
        .toList();
    deleteComponent(components.get(0));
    List<Integer> originalIds = components.stream().map(DesignComponent::getDesignComponentId).toList();
    DesignBoardUpdateDto updateDto = DesignBoardUpdateDto.builder()
        .title("retained inactive asset").designComponentMappings(mappingsFor(originalIds))
        .postTags(List.of()).build();
    DesignBoardDetailDto response = mockMvcUtils.doAuthMultipartRequest(
        MockMvcMultipartRequestDto.<DesignBoardDetailDto>builder()
            .mockMvc(mockMvc).path("/api/design-boards/" + targetPost.getPostId()).httpMethod(HttpMethod.PATCH)
            .clientDto(TestClientDto.fromEntity(targetPost.getUser()))
            .formDataList(List.of(MockMultipartFileUtils.generateJsonFormData("boardInfo", updateDto)))
            .responseType(new TypeReference<>() {
            }).build());
    assertThat(response.getTitle()).isEqualTo("retained inactive asset");
    assertThat(response.getPreviewImageUrl()).hasSize(3);
    assertThat(designBoardRepository.findByPost_PostId(targetPost.getPostId()))
        .extracting(board -> board.getDesignComponent().getDesignComponentId())
        .containsExactlyInAnyOrderElementsOf(originalIds);
  }

  @Test
  @DisplayName("삭제된 에셋 연결을 제거하면 기존 합성 대표 이미지 대신 활성 에셋을 복사한 이미지를 저장한다")
  void updateDesignBoard_removingInactiveLinksReplacesOwnedPreview()
      throws Exception {
    Post targetPost = postResult.posts().get(0);
    List<DesignComponent> components = designComponentResult.designComponents().stream()
        .filter(component -> component.getUser().getUserId().equals(targetPost.getUser().getUserId()))
        .sorted(Comparator.comparing(DesignComponent::getDesignComponentId)).toList();
    String oldPreview = "owned-post-composite.png";
    targetPost.setPreviewImageName(oldPreview);
    postRepository.saveAndFlush(targetPost);
    deleteComponent(components.get(0));
    Mockito.when(fileManager.resolvePublicFileUrl(anyString()))
        .thenAnswer(invocation -> "http://mocked-url/" + invocation.getArgument(0, String.class));
    Mockito.when(fileManager.downloadFile(components.get(1).getFileName()))
        .thenReturn(new byte[]{1, 2, 3});
    Mockito.clearInvocations(fileManager);
    DesignBoardUpdateDto updateDto = DesignBoardUpdateDto.builder()
        .designComponentMappings(mappingsFor(List.of(components.get(2).getDesignComponentId(),
            components.get(1).getDesignComponentId())))
        .build();
    DesignBoardDetailDto response = mockMvcUtils.doAuthMultipartRequest(
        MockMvcMultipartRequestDto.<DesignBoardDetailDto>builder()
            .mockMvc(mockMvc).path("/api/design-boards/" + targetPost.getPostId()).httpMethod(HttpMethod.PATCH)
            .clientDto(TestClientDto.fromEntity(targetPost.getUser()))
            .formDataList(List.of(MockMultipartFileUtils.generateJsonFormData("boardInfo", updateDto)))
            .responseType(new TypeReference<>() {
            }).build());
    String newPreview = postRepository.findById(targetPost.getPostId()).orElseThrow()
        .getPreviewImageName();
    assertThat(newPreview).isNotBlank().isNotEqualTo(oldPreview)
        .isNotIn(components.stream().map(DesignComponent::getFileName).toList());
    assertThat(response.getPreviewImageUrl()).hasSize(3).first()
        .isEqualTo("http://mocked-url/" + newPreview);
    assertThat(response.getPreviewImageUrl())
        .doesNotContain("http://mocked-url/" + oldPreview,
            "http://mocked-url/" + components.get(0).getFileName());
    Mockito.verify(fileManager).downloadFile(components.get(1).getFileName());
    Mockito.verify(fileManager).deleteFile(oldPreview);
  }

  @Test
  @DisplayName("존재하지 않는 디자인 게시글 단건 조회는 404를 반환한다")
  void findDesignBoard_missingPostReturnsNotFound() throws Exception {
    mockMvc.perform(get("/api/design-boards/{postId}", Long.MAX_VALUE))
        .andExpect(status().isNotFound());
  }

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  @DisplayName("수정 결과에 활성 에셋이 없는 게시글은 파일 업로드와 데이터 변경 전에 404로 거절한다")
  void updateDesignBoard_withoutActiveResultIsRejectedBeforeMutation(boolean alreadyHidden)
      throws Exception {
    Post targetPost = postResult.posts().get(0);
    List<DesignComponent> components = designComponentResult.designComponents().stream()
        .filter(component -> component.getUser().getUserId().equals(targetPost.getUser().getUserId()))
        .toList();
    for (DesignComponent component : alreadyHidden ? components : List.of(components.get(0))) {
      deleteComponent(component);
    }
    DesignBoardUpdateDto updateDto = DesignBoardUpdateDto.builder()
        .title("must stay hidden").content("must not change")
        .designComponentMappings(alreadyHidden ? null
            : mappingsFor(List.of(components.get(0).getDesignComponentId())))
        .postTags(List.of()).build();
    Mockito.clearInvocations(fileManager);
    mockMvcUtils.doAuthMultipartRequest(MockMvcMultipartRequestDto.<Void>builder()
        .mockMvc(mockMvc).path("/api/design-boards/" + targetPost.getPostId()).httpMethod(HttpMethod.PATCH)
        .clientDto(TestClientDto.fromEntity(targetPost.getUser())).statusCode(404)
        .formDataList(List.of(MockMultipartFileUtils.generateJsonFormData("boardInfo", updateDto),
            MockMultipartFileUtils.generateImageFormData("previewImage", ImageExtension.PNG)))
        .responseType(new TypeReference<>() {
        }).build());
    Mockito.verify(fileManager, Mockito.never()).uploadPublicFile(any(byte[].class), anyString());
    Post unchanged = postRepository.findById(targetPost.getPostId()).orElseThrow();
    assertThat(unchanged.getTitle()).isEqualTo(targetPost.getTitle());
    assertThat(unchanged.getContent()).isEqualTo(targetPost.getContent());
    assertThat(unchanged.getPreviewImageName()).isEqualTo(targetPost.getPreviewImageName());
    assertThat(designBoardRepository.findByPost_PostId(targetPost.getPostId()))
        .extracting(board -> board.getDesignComponent().getDesignComponentId())
        .containsExactlyInAnyOrderElementsOf(components.stream()
            .map(DesignComponent::getDesignComponentId).toList());
  }

  /**
   * 디자인 게시글 목록의 페이지 크기와 에셋 ID가 없는 HTTP 응답을 검증한다.
   */
  @Test
  @DisplayName("디자인 게시글 목록은 요청한 페이지 크기로 반환하고 designComponentId를 노출하지 않는다")
  public void whenSendRequest_retrieveListOfDesignBoards() throws Exception {
    // given
    int pageSize = 2;
    int pageNumber = 0;
    User client = userResult.getFirstUser();
    MultiValueMap<String, String> params = TestParams.withPaging(pageNumber, pageSize);
    // when
    ResultActions result = mockMvc.perform(mockMvcUtils.addAuthentication(
            get("/api/design-boards").params(params), TestClientDto.fromEntity(client)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[*].designComponentId").doesNotHaveJsonPath());
    List<DesignBoardPreviewDto> responses = mockMvcUtils.parseResponse(result, new TypeReference<>() {
    });
    // then
    assertThat(responses).isNotNull().hasSize(pageSize);
  }

  @ParameterizedTest
  @NullSource
  @ValueSource(strings = {"CREATED_ASC", "CREATED_DESC", "PREFER_ASC", "PREFER_DESC"})
  @DisplayName("디자인 게시글 목록을 생성일 또는 좋아요 수 기준으로 정렬한다.")
  void findDesignBoards_sortType(String sortType) throws Exception {
    // given
    prepareDesignBoardSortData();
    User client = userResult.getFirstUser();
    MultiValueMap<String, String> params = TestParams.withPaging(0,
        postResult.posts().size());
    if (sortType != null) {
      params.add("sortType", sortType);
    }
    // when
    List<DesignBoardPreviewDto> response = requestDesignBoardPreviews(params, client);
    // then
    assertThat(response)
        .hasSize(postResult.posts().size())
        .isSortedAccordingTo(designBoardComparator(sortType));

    MultiValueMap<String, String> repeatedParams = TestParams.withPaging(0,
        postResult.posts().size());
    repeatedParams.add("sortType", sortType == null ? "CREATED_DESC" : sortType);
    assertThat(requestDesignBoardPreviews(repeatedParams, client))
        .containsExactlyElementsOf(response);
  }

  @Test
  @DisplayName("디자인 게시글 단건 상세는 최상위 에셋 ID 없이 에셋별 매핑을 반환한다")
  public void whenSendRequest_retrieveDesignBoardById() throws Exception {
    // given
    DesignBoard targetDesignBoard = postResult.designBoards().get(0);
    String requestPath = String.format("/api/design-boards/%d",
        targetDesignBoard.getPost().getPostId());
    List<Integer> expectedComponentIds = postResult.designBoards().stream()
        .filter(board -> board.getPost().getPostId().equals(targetDesignBoard.getPost().getPostId()))
        .map(board -> board.getDesignComponent().getDesignComponentId())
        .toList();
    User client = userResult.getFirstUser();
    // when
    ResultActions result = mockMvc.perform(mockMvcUtils.addAuthentication(
            get(requestPath), TestClientDto.fromEntity(client)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.designComponentId").doesNotHaveJsonPath())
        .andExpect(jsonPath("$.designComponentMappings[*].designComponentId")
            .value(containsInAnyOrder(expectedComponentIds.toArray())));
    DesignBoardDetailDto response = mockMvcUtils.parseResponse(result, new TypeReference<>() {
    });
    // then
    assertDesignBoard(response);
  }

  /**
   * 비인증 요청에서도 디자인 게시글 단건 상세 정보와 false인 사용자별 상태값을 반환하는지 검증한다.
   */
  @Test
  @DisplayName("비인증 사용자가 디자인 게시글 단건 상세를 조회하면 상세 정보와 false인 사용자별 상태값을 반환한다")
  void findDesignBoardByPostId_withoutAuthentication_returnsDetail() throws Exception {
    // given
    Post targetPost = postResult.designBoards().get(0).getPost();
    // when
    ResultActions result = mockMvc.perform(get("/api/design-boards/{postId}", targetPost.getPostId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.liked").exists())
        .andExpect(jsonPath("$.liked").value(false))
        .andExpect(jsonPath("$.bookmarked").exists())
        .andExpect(jsonPath("$.bookmarked").value(false))
        .andExpect(jsonPath("$.following").exists())
        .andExpect(jsonPath("$.following").value(false));
    DesignBoardDetailDto response = mockMvcUtils.parseResponse(result, new TypeReference<>() {
    });
    // then
    assertThat(response.getPostId()).isEqualTo(targetPost.getPostId());
    assertThat(response.getTitle()).isEqualTo(targetPost.getTitle());
    assertThat(response.getContent()).isEqualTo(targetPost.getContent());
    assertDesignBoard(response);
  }

  @Test
  @DisplayName("디자인 게시글 단건 상세 조회 시 현재 사용자의 작성자 팔로우 여부를 반환한다")
  void findDesignBoardByPostId_returnsFollowingStatus() throws Exception {
    // given
    DesignBoard targetDesignBoard = postResult.designBoards().get(0);
    User author = targetDesignBoard.getPost().getUser();
    User followingClient = userResult.users().stream()
        .filter(user -> !user.getUserId().equals(author.getUserId()))
        .findFirst()
        .orElseThrow();
    User nonFollowingClient = userResult.users().stream()
        .filter(user -> !user.getUserId().equals(author.getUserId()))
        .filter(user -> !user.getUserId().equals(followingClient.getUserId()))
        .findFirst()
        .orElseThrow();
    followRepository.saveAndFlush(new Follow(followingClient, author));
    // when & then
    mockMvc.perform(mockMvcUtils.addAuthentication(
            get("/api/design-boards/{postId}", targetDesignBoard.getPost().getPostId()),
            TestClientDto.fromEntity(followingClient)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.following").exists())
        .andExpect(jsonPath("$.following").value(true));
    mockMvc.perform(mockMvcUtils.addAuthentication(
            get("/api/design-boards/{postId}", targetDesignBoard.getPost().getPostId()),
            TestClientDto.fromEntity(nonFollowingClient)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.following").exists())
        .andExpect(jsonPath("$.following").value(false));
  }

  @Test
  @DisplayName("If a pinned post ID is provided, place that post at the top of the first page and return only design boards written by the same author.")
  void findDesignBoardDetails_ifPinnedPostIdExists() throws Exception {
    // given
    DesignBoard targetDesignBoard = postResult.designBoards().get(0);
    Post pinnedPost = targetDesignBoard.getPost();
    MultiValueMap<String, String> params = TestParams.withPaging(0, 5);
    params.add("pinnedPostId", pinnedPost.getPostId().toString());
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

  /**
   * 비인증 요청에서도 디자인 게시글 상세 목록의 모든 항목과 false인 사용자별 상태값을 반환하는지 검증한다.
   */
  @Test
  @DisplayName("비인증 상세 목록은 최상위 에셋 ID 없이 에셋별 매핑과 false인 사용자별 상태값을 반환한다")
  void findDesignBoardDetails_withoutAuthentication_returnsDetails() throws Exception {
    // given
    List<Long> expectedPostIds = postResult.designBoards().stream()
        .map(designBoard -> designBoard.getPost().getPostId())
        .distinct()
        .toList();
    List<Integer> expectedComponentIds = postResult.designBoards().stream()
        .map(board -> board.getDesignComponent().getDesignComponentId())
        .toList();
    MultiValueMap<String, String> params = TestParams.withPaging(0, expectedPostIds.size());
    // when
    ResultActions result = mockMvc.perform(get("/api/design-boards/details").params(params))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[*].designComponentId").doesNotHaveJsonPath())
        .andExpect(jsonPath("$[*].designComponentMappings[*].designComponentId")
            .value(containsInAnyOrder(expectedComponentIds.toArray())));
    List<DesignBoardDetailDto> response = mockMvcUtils.parseResponse(result, new TypeReference<>() {
    });
    // then
    assertThat(response).isNotEmpty().hasSize(expectedPostIds.size());
    assertThat(response).extracting(DesignBoardDetailDto::getPostId)
        .containsExactlyInAnyOrderElementsOf(expectedPostIds);
    for (int i = 0; i < response.size(); i++) {
      assertDesignBoard(response.get(i));
      result.andExpect(jsonPath("$[" + i + "].liked").exists())
          .andExpect(jsonPath("$[" + i + "].liked").value(false))
          .andExpect(jsonPath("$[" + i + "].bookmarked").exists())
          .andExpect(jsonPath("$[" + i + "].bookmarked").value(false))
          .andExpect(jsonPath("$[" + i + "].following").exists())
          .andExpect(jsonPath("$[" + i + "].following").value(false));
    }
  }

  @Test
  @DisplayName("디자인 게시글 상세 목록 조회 시 현재 사용자의 작성자 팔로우 여부를 반환한다")
  void findDesignBoardDetails_returnsFollowingStatus() throws Exception {
    // given
    DesignBoard targetDesignBoard = postResult.designBoards().get(0);
    Post targetPost = targetDesignBoard.getPost();
    User author = targetPost.getUser();
    User followingClient = userResult.users().stream()
        .filter(user -> !user.getUserId().equals(author.getUserId()))
        .findFirst()
        .orElseThrow();
    User nonFollowingClient = userResult.users().stream()
        .filter(user -> !user.getUserId().equals(author.getUserId()))
        .filter(user -> !user.getUserId().equals(followingClient.getUserId()))
        .findFirst()
        .orElseThrow();
    followRepository.saveAndFlush(new Follow(followingClient, author));
    MultiValueMap<String, String> params = TestParams.withPaging(0, 1);
    params.add("pinnedPostId", targetPost.getPostId().toString());
    // when & then
    mockMvc.perform(mockMvcUtils.addAuthentication(
            get("/api/design-boards/details").params(params),
            TestClientDto.fromEntity(followingClient)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].following").exists())
        .andExpect(jsonPath("$[0].following").value(true));
    mockMvc.perform(mockMvcUtils.addAuthentication(
            get("/api/design-boards/details").params(params),
            TestClientDto.fromEntity(nonFollowingClient)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].following").exists())
        .andExpect(jsonPath("$[0].following").value(false));
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
        postResult.posts().size());
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
    assertThat(response).hasSize(postResult.posts().size());
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
    designComponentRepository.save(targetComponent);
    List<DesignBoard> matchedBoards = postResult.designBoards().stream()
        .filter(board -> board.getDesignComponent().getDesignComponentId()
            .equals(targetComponent.getDesignComponentId()))
        .toList();
    matchedBoards.forEach(board -> board.setComponentType(componentType));
    designBoardRepository.saveAllAndFlush(matchedBoards);
    List<Long> matchedPostIds = postResult.designBoards().stream()
        .filter(designBoard -> designBoard.getDesignComponent().getDesignComponentId()
            .equals(targetComponent.getDesignComponentId()))
        .map(designBoard -> designBoard.getPost().getPostId())
        .distinct()
        .toList();
    MultiValueMap<String, String> params = TestParams.withPaging(0,
        postResult.posts().size());
    params.add("typeCode", typeCode.getTypeCode());
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
    assertThat(response).hasSize(postResult.posts().size());
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
    params.add("typeCode", "invalidTypeCode");
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
    String expectedTitle = "t".repeat(100);
    String expectedContent = "c".repeat(2000);
    DesignBoardCreateDto createDto = DesignBoardCreateDto.builder()
        .title(expectedTitle)
        .content(expectedContent)
        .designComponentMappings(mappingsFor(designComponentIds))
        .publicFlag(true)
        .postTags(tagCreateDtoList)
        .build();
    MockMultipartFile previewImage = MockMultipartFileUtils
        .generateImageFormData("previewImage", ImageExtension.PNG);
    MockMultipartFile boardInfo = MockMultipartFileUtils
        .generateJsonFormData("boardInfo", createDto);
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
    assertThat(response.getTitle()).isEqualTo(expectedTitle);
    assertThat(response.getContent()).isEqualTo(expectedContent);
    Post savedPost = postRepository.findById(response.getPostId()).orElseThrow();
    assertThat(savedPost.getTitle()).isEqualTo(expectedTitle);
    assertThat(savedPost.getContent()).isEqualTo(expectedContent);
    assertDesignBoard(response);
  }

  @ParameterizedTest(name = "{0}")
  @ValueSource(strings = {"title", "content"})
  @DisplayName("디자인 게시글 생성 시 제목 또는 본문이 최대 길이를 초과하면 400을 반환한다")
  void createDesignBoard_exceedingTextLimit_returnsBadRequest(String invalidField)
      throws Exception {
    // given
    User author = userResult.getFirstUser();
    DesignComponent targetDesignComponent = designComponentScenarioSupport
        .builder(List.of(author))
        .withCountPerUser(1)
        .build()
        .designComponents()
        .get(0);
    DesignBoardCreateDto createDto = DesignBoardCreateDto.builder()
        .title("title".equals(invalidField) ? "t".repeat(101) : "t".repeat(100))
        .content("content".equals(invalidField) ? "c".repeat(2001) : "c".repeat(2000))
        .designComponentMappings(mappingsFor(List.of(targetDesignComponent.getDesignComponentId())))
        .publicFlag(true)
        .postTags(List.of())
        .build();
    MockMultipartFile boardInfo = MockMultipartFileUtils
        .generateJsonFormData("boardInfo", createDto);
    // when & then
    Map<String, String> response = mockMvcUtils.doAuthMultipartRequest(
        MockMvcMultipartRequestDto.<Map<String, String>>builder()
            .mockMvc(mockMvc)
            .path("/api/design-boards")
            .httpMethod(HttpMethod.POST)
            .formDataList(List.of(boardInfo))
            .clientDto(TestClientDto.fromEntity(author))
            .statusCode(400)
            .responseType(new TypeReference<>() {
            })
            .build()
    );
    assertThat(response).containsKey(invalidField);
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
    String expectedTitle = "t".repeat(100);
    String expectedContent = "c".repeat(2000);
    DesignBoardUpdateDto updateDto = DesignBoardUpdateDto.builder()
        .title(expectedTitle)
        .content(expectedContent)
        .publicFlag(false)
        .designComponentMappings(mappingsFor(designComponentIds.subList(0, 2)))
        .postTags(tagUpdateDtoList)
        .build();
    MockMultipartFile previewImage = MockMultipartFileUtils
        .generateImageFormData("previewImage", ImageExtension.PNG);
    MockMultipartFile boardInfo = MockMultipartFileUtils
        .generateJsonFormData("boardInfo", updateDto);
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
    assertThat(response.getTitle()).isEqualTo(expectedTitle);
    assertThat(response.getContent()).isEqualTo(expectedContent);
    Post savedPost = postRepository.findById(response.getPostId()).orElseThrow();
    assertThat(savedPost.getTitle()).isEqualTo(expectedTitle);
    assertThat(savedPost.getContent()).isEqualTo(expectedContent);
    assertDesignBoard(response);
  }

  @Test
  @DisplayName("TypeCode 매핑을 JSON으로 저장하고 응답 계약과 중복 없는 이미지·게시글 조회를 검증한다")
  void createDesignBoard_multipleTypesPerAsset() throws Exception {
    User author = userResult.getFirstUser();
    Integer assetId = designComponentResult.designComponents().get(0).getDesignComponentId();
    List<TypeCode> typeCodes = List.of(TypeCode.TABBAR_STYLE_BACKGROUND_IMAGE,
        TypeCode.CHAT_ROOM_BACKGROUND_IMAGE);
    List<DesignComponentMappingDto> mappings = List.of(new DesignComponentMappingDto(assetId, typeCodes));
    Map<String, Object> request = Map.of("title", "multiple types", "content", "content",
        "publicFlag", true, "designComponentMappings", List.of(Map.of("designComponentId", assetId,
            "typeCodes", List.of("tabbarStyleBackgroundImage", "chatRoomBackgroundImage"))));
    ResultActions result = mockMvc.perform(mockMvcUtils.addAuthentication(
            multipart("/api/design-boards")
                .file(MockMultipartFileUtils.generateJsonFormData("boardInfo", request))
                .file(MockMultipartFileUtils.generateImageFormData("previewImage", ImageExtension.PNG)),
            TestClientDto.fromEntity(author)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.designComponentMappings[0].designComponentId").value(assetId))
        .andExpect(jsonPath("$.designComponentMappings[0].typeCodes")
            .value(containsInAnyOrder("tabbarStyleBackgroundImage", "chatRoomBackgroundImage")))
        .andExpect(jsonPath("$.designComponents").doesNotHaveJsonPath())
        .andExpect(jsonPath("$.designComponentMappings[0].componentTypeIds").doesNotHaveJsonPath());
    DesignBoardDetailDto response = mockMvcUtils.parseResponse(result, new TypeReference<>() {
    });

    assertThat(designBoardRepository.findByPost_PostId(response.getPostId()))
        .extracting(board -> board.getComponentType().getTypeCode())
        .containsExactlyInAnyOrderElementsOf(typeCodes);
    assertThat(response.getDesignComponentMappings()).usingRecursiveComparison()
        .ignoringCollectionOrder().isEqualTo(mappings);
    assertThat(response.getComponentTypes()).hasSize(2);
    assertThat(response.getPreviewImageUrl()).hasSize(2);

    List<DesignBoardPreviewDto> previews = requestDesignBoardPreviews(TestParams.withPaging(0, 100),
        author);
    assertThat(previews.stream().filter(dto -> dto.getPostId().equals(response.getPostId())))
        .hasSize(1);
    List<DesignBoardDetailDto> details = requestDesignBoardDetails(TestParams.withPaging(0, 100),
        author);
    DesignBoardDetailDto detail = details.stream()
        .filter(dto -> dto.getPostId().equals(response.getPostId())).findFirst().orElseThrow();
    assertThat(detail.getPreviewImageUrl()).hasSize(2);
    assertThat(detail.getDesignComponentMappings()).usingRecursiveComparison()
        .ignoringCollectionOrder().isEqualTo(mappings);
  }

  @Test
  @DisplayName("같은 에셋도 게시글마다 서로 다른 타입으로 분류한다")
  void createDesignBoard_sameAssetDifferentPostTypes() throws Exception {
    User author = userResult.getFirstUser();
    Integer assetId = designComponentResult.designComponents().get(0).getDesignComponentId();
    List<TypeCode> typeCodes = componentTypeRepository.findAll().stream()
        .limit(2).map(ComponentType::getTypeCode).toList();
    List<DesignBoardDetailDto> responses = new ArrayList<>();
    for (TypeCode typeCode : typeCodes) {
      responses.add(requestBoardMutation(HttpMethod.POST, null,
          DesignBoardCreateDto.builder().title("independent types").content("content")
              .designComponentMappings(List.of(new DesignComponentMappingDto(assetId, List.of(typeCode))))
              .publicFlag(true).build(), author, 200));
    }
    for (int index = 0; index < responses.size(); index++) {
      assertThat(responses.get(index).getDesignComponentMappings().get(0).getTypeCodes())
          .containsExactly(typeCodes.get(index));
      assertThat(designBoardRepository.findByPost_PostId(responses.get(index).getPostId()))
          .extracting(board -> board.getComponentType().getTypeCode())
          .containsExactly(typeCodes.get(index));
    }
  }

  @Test
  @DisplayName("에셋별 타입 매핑을 응답 그대로 재저장하고 타입을 삭제하거나 유지할 수 있다")
  void updateDesignBoard_replaceAndPreserveTypeMappings() throws Exception {
    DesignBoard target = postResult.designBoards().get(0);
    User author = target.getPost().getUser();
    Long postId = target.getPost().getPostId();
    List<TypeCode> typeCodes = componentTypeRepository.findAll().stream()
        .limit(2).map(ComponentType::getTypeCode).toList();
    List<DesignComponentMappingDto> mappings = List.of(
        new DesignComponentMappingDto(target.getDesignComponent().getDesignComponentId(), typeCodes));

    DesignBoardDetailDto response = requestBoardMutation(HttpMethod.PATCH, postId,
        DesignBoardUpdateDto.builder().designComponentMappings(mappings).build(), author, 200);
    response = requestBoardMutation(HttpMethod.PATCH, postId,
        DesignBoardUpdateDto.builder().designComponentMappings(response.getDesignComponentMappings()).build(),
        author, 200);
    assertThat(response.getDesignComponentMappings()).usingRecursiveComparison()
        .ignoringCollectionOrder().isEqualTo(mappings);

    mappings = List.of(new DesignComponentMappingDto(target.getDesignComponent().getDesignComponentId(),
        List.of(typeCodes.get(1))));
    requestBoardMutation(HttpMethod.PATCH, postId,
        DesignBoardUpdateDto.builder().designComponentMappings(mappings).build(), author, 200);
    response = requestBoardMutation(HttpMethod.PATCH, postId,
        DesignBoardUpdateDto.builder().title("mapping unchanged").build(), author, 200);
    assertThat(response.getDesignComponentMappings()).usingRecursiveComparison()
        .ignoringCollectionOrder().isEqualTo(mappings);
    assertThat(designBoardRepository.findByPost_PostId(postId))
        .extracting(board -> board.getComponentType().getTypeCode())
        .containsExactly(typeCodes.get(1));
  }

  @ParameterizedTest
  @ValueSource(strings = {"missingMappings", "emptyMappings", "emptyTypes", "missingAssetId",
      "nullMapping", "nullType", "duplicateType", "duplicateMapping", "missingAsset", "missingType"})
  @DisplayName("잘못된 에셋별 타입 매핑으로 게시글을 생성하면 실패하고 저장하지 않는다")
  void createDesignBoard_invalidMappings(String scenario) throws Exception {
    Integer assetId = designComponentResult.designComponents().get(0).getDesignComponentId();
    TypeCode typeCode = componentTypeRepository.findAll().get(0).getTypeCode();
    List<DesignComponentMappingDto> mappings = switch (scenario) {
      case "missingMappings" -> null;
      case "emptyMappings" -> List.of();
      case "emptyTypes" -> List.of(new DesignComponentMappingDto(assetId, List.of()));
      case "missingAssetId" -> List.of(new DesignComponentMappingDto(null, List.of(typeCode)));
      case "nullMapping" -> java.util.Collections.singletonList(null);
      case "nullType" -> List.of(new DesignComponentMappingDto(assetId,
          java.util.Collections.singletonList(null)));
      case "duplicateType" -> List.of(new DesignComponentMappingDto(assetId, List.of(typeCode, typeCode)));
      case "duplicateMapping" -> List.of(new DesignComponentMappingDto(assetId,
              List.of(TypeCode.TABBAR_STYLE_BACKGROUND_IMAGE, TypeCode.CHAT_ROOM_BACKGROUND_IMAGE)),
          new DesignComponentMappingDto(assetId, List.of(TypeCode.CHAT_ROOM_BACKGROUND_IMAGE)));
      case "missingAsset" -> List.of(new DesignComponentMappingDto(Integer.MAX_VALUE, List.of(typeCode)));
      case "missingType" -> List.of(new DesignComponentMappingDto(assetId,
          List.of(removeUnusedComponentType())));
      default -> throw new IllegalArgumentException(scenario);
    };
    long initialPostCount = postRepository.count();
    long initialMappingCount = designBoardRepository.count();
    Mockito.clearInvocations(fileManager);
    requestBoardMutation(HttpMethod.POST, null,
        DesignBoardCreateDto.builder().title("invalid mappings").content("content")
            .designComponentMappings(mappings).build(), userResult.getFirstUser(),
        scenario.equals("missingAsset") || scenario.equals("missingType") ? 404 : 400);
    Mockito.verify(fileManager, Mockito.never()).uploadPublicFile(any(byte[].class), anyString());
    assertThat(postRepository.count()).isEqualTo(initialPostCount);
    assertThat(designBoardRepository.count()).isEqualTo(initialMappingCount);
  }

  /**
   * 올바른 타입 문자열이 아닌 원본 JSON의 값을 저장 전에 거절하는지 검증한다.
   */
  @ParameterizedTest(name = "{0} (숫자 JSON: {1})")
  @CsvSource({"not-a-type, false", "0, true", "0, false"})
  @DisplayName("매핑의 TypeCode가 올바른 문자열이 아니면 400을 반환하고 저장·업로드하지 않는다")
  void createDesignBoard_invalidTypeCodeInMappingReturnsBadRequest(String typeCode,
      boolean numeric) throws Exception {
    Integer assetId = designComponentResult.designComponents().get(0).getDesignComponentId();
    Object invalidTypeCode = numeric ? Integer.valueOf(typeCode) : typeCode;
    Map<String, Object> request = Map.of("title", "invalid type", "content", "content",
        "designComponentMappings", List.of(Map.of("designComponentId", assetId,
            "typeCodes", List.of(invalidTypeCode))));
    long initialPostCount = postRepository.count();
    long initialMappingCount = designBoardRepository.count();
    Mockito.clearInvocations(fileManager);

    requestBoardMutation(HttpMethod.POST, null, request, userResult.getFirstUser(), 400);

    assertThat(postRepository.count()).isEqualTo(initialPostCount);
    assertThat(designBoardRepository.count()).isEqualTo(initialMappingCount);
    Mockito.verify(fileManager, Mockito.never()).uploadPublicFile(any(byte[].class), anyString());
  }

  /**
   * 숫자 TypeCode를 포함한 원본 JSON 수정 요청이 기존 게시글과 매핑을 변경하지 않는지 검증한다.
   */
  @Test
  @DisplayName("매핑의 TypeCode가 JSON 숫자이면 400을 반환하고 기존 제목·매핑을 유지한다")
  void updateDesignBoard_numericTypeCodeLeavesPostUnchanged() throws Exception {
    DesignBoard target = postResult.designBoards().get(0);
    Long postId = target.getPost().getPostId();
    String originalTitle = target.getPost().getTitle();
    List<Long> originalMappingIds = designBoardRepository.findByPost_PostId(postId).stream()
        .map(DesignBoard::getDesignBoardId).toList();
    Map<String, Object> request = Map.of("title", "must not be saved",
        "designComponentMappings", List.of(Map.of("designComponentId",
            target.getDesignComponent().getDesignComponentId(), "typeCodes", List.of(0))));
    Mockito.clearInvocations(fileManager);

    requestBoardMutation(HttpMethod.PATCH, postId, request, target.getPost().getUser(), 400);

    assertThat(postRepository.findById(postId).orElseThrow().getTitle()).isEqualTo(originalTitle);
    assertThat(designBoardRepository.findByPost_PostId(postId))
        .extracting(DesignBoard::getDesignBoardId)
        .containsExactlyInAnyOrderElementsOf(originalMappingIds);
    Mockito.verify(fileManager, Mockito.never()).uploadPublicFile(any(byte[].class), anyString());
  }

  @ParameterizedTest
  @ValueSource(strings = {"emptyMappings", "missingType"})
  @DisplayName("매핑 수정 검증에 실패하면 기존 제목과 타입 매핑을 유지한다")
  void updateDesignBoard_invalidMappingsLeavesPostUnchanged(String scenario) throws Exception {
    DesignBoard target = postResult.designBoards().get(0);
    Long postId = target.getPost().getPostId();
    String originalTitle = target.getPost().getTitle();
    List<Long> originalMappingIds = designBoardRepository.findByPost_PostId(postId).stream()
        .map(DesignBoard::getDesignBoardId).toList();
    DesignComponentMappingDto valid = new DesignComponentMappingDto(
        target.getDesignComponent().getDesignComponentId(),
        List.of(target.getComponentType().getTypeCode()));
    List<DesignComponentMappingDto> mappings = switch (scenario) {
      case "missingType" -> List.of(valid,
          new DesignComponentMappingDto(valid.getDesignComponentId(),
              List.of(removeUnusedComponentType())));
      case "emptyMappings" -> List.of();
      default -> throw new IllegalArgumentException(scenario);
    };
    Mockito.clearInvocations(fileManager);
    requestBoardMutation(HttpMethod.PATCH, postId,
        DesignBoardUpdateDto.builder().title("must not be saved")
            .postTags(List.of(TagUpdateDto.builder().tagName("must not be saved").build()))
            .designComponentMappings(mappings).build(), target.getPost().getUser(),
        scenario.startsWith("missing") ? 404 : 400);
    Mockito.verify(fileManager, Mockito.never()).uploadPublicFile(any(byte[].class), anyString());
    assertThat(postRepository.findById(postId).orElseThrow().getTitle()).isEqualTo(originalTitle);
    assertThat(designBoardRepository.findByPost_PostId(postId))
        .extracting(DesignBoard::getDesignBoardId)
        .containsExactlyInAnyOrderElementsOf(originalMappingIds);
  }

  @Test
  @DisplayName("DB는 게시글·에셋·타입 조합의 중복 저장을 차단한다")
  void saveDesignBoard_duplicateTripleRejectedByDatabase() {
    DesignBoard target = postResult.designBoards().get(0);
    DesignBoard duplicate = DesignBoardFixture.designBoard(target.getPost(),
        target.getDesignComponent(), target.getComponentType());
    assertThatThrownBy(() -> designBoardRepository.saveAndFlush(duplicate))
        .isInstanceOf(DataIntegrityViolationException.class);
  }

  @ParameterizedTest(name = "{0}")
  @ValueSource(strings = {"title", "content"})
  @DisplayName("디자인 게시글 수정 시 제목 또는 본문이 최대 길이를 초과하면 400을 반환한다")
  void updateDesignBoard_exceedingTextLimit_returnsBadRequest(String invalidField)
      throws Exception {
    // given
    DesignBoard targetDesignBoard = postResult.designBoards().get(0);
    User author = targetDesignBoard.getPost().getUser();
    List<Integer> designComponentIds = designComponentResult.designComponents().stream()
        .filter(component -> component.getUser().getUserId().equals(author.getUserId()))
        .map(DesignComponent::getDesignComponentId)
        .toList();
    DesignBoardUpdateDto updateDto = DesignBoardUpdateDto.builder()
        .title("title".equals(invalidField) ? "t".repeat(101) : "t".repeat(100))
        .content("content".equals(invalidField) ? "c".repeat(2001) : "c".repeat(2000))
        .designComponentMappings(mappingsFor(designComponentIds))
        .postTags(List.of())
        .build();
    MockMultipartFile boardInfo = MockMultipartFileUtils
        .generateJsonFormData("boardInfo", updateDto);
    // when & then
    Map<String, String> response = mockMvcUtils.doAuthMultipartRequest(
        MockMvcMultipartRequestDto.<Map<String, String>>builder()
            .mockMvc(mockMvc)
            .path(String.format("/api/design-boards/%d", targetDesignBoard.getPost().getPostId()))
            .httpMethod(HttpMethod.PATCH)
            .formDataList(List.of(boardInfo))
            .clientDto(TestClientDto.fromEntity(author))
            .statusCode(400)
            .responseType(new TypeReference<>() {
            })
            .build()
    );
    assertThat(response).containsKey(invalidField);
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
