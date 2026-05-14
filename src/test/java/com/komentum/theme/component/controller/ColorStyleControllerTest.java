package com.komentum.theme.component.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import com.komentum.test.MockMvcUtils;
import com.komentum.test.config.EnableTestProfile;
import com.komentum.test.data.TestDataRemover;
import com.komentum.test.data.scenario.UserScenarioSupport;
import com.komentum.test.dto.MockMvcRequestDto;
import com.komentum.test.dto.TestClientDto;
import com.komentum.theme.component.domain.ColorStyle;
import com.komentum.theme.component.dto.ColorStyleCreateDto;
import com.komentum.theme.component.dto.ColorStyleResponse;
import com.komentum.theme.component.dto.ColorStyleUpdateRequest;
import com.komentum.theme.component.dto.SeedResult;
import com.komentum.theme.component.enums.StyleCode;
import com.komentum.theme.component.repository.ColorStyleRepository;
import com.komentum.theme.component.service.ColorStyleSeeder;
import com.komentum.user.domain.User;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@SpringBootTest
@EnableTestProfile
@AutoConfigureMockMvc
public class ColorStyleControllerTest {

  @Autowired
  private ColorStyleRepository colorStyleRepository;

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private MockMvcUtils mockMvcUtils;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private ColorStyleSeeder seeder;
  @Autowired
  private UserScenarioSupport userScenarioSupport;
  @Autowired
  private TestDataRemover testDataRemover;

  private void assertColorStyleResponse(ColorStyleResponse response) {
    assertThat(response.getColorStyleId()).isNotNull();
    ColorStyle colorStyle = colorStyleRepository.findById(response.getColorStyleId())
        .orElseThrow();
    compareResponseWithColorStyle(response, colorStyle);
  }

  private void compareResponseWithColorStyle(ColorStyleResponse response, ColorStyle colorStyle) {
    assertThat(colorStyle)
        .extracting(
            ColorStyle::getStyleCode,
            ColorStyle::getName,
            ColorStyle::getExplain
        )
        .containsExactly(
            response.getStyleCode(),
            response.getName(),
            response.getExplain()
        );
  }

  private ColorStyle createColorStyle(StyleCode styleCode) {
    Faker faker = new Faker();
    return colorStyleRepository.save(ColorStyle.builder()
        .name(faker.color().name())
        .explain(faker.lorem().paragraph())
        .styleCode(styleCode)
        .build());
  }

  User rootUser;

  @BeforeEach
  public void setUp() {
    rootUser = userScenarioSupport.builder()
        .withRootUser()
        .build().rootUser();
  }

  @AfterEach
  public void tearDown() {
    testDataRemover.deleteAll();
  }

  @Test
  @DisplayName("ColorStyle 생성 테스트")
  void createColorStyle_success() throws Exception {
    // Given
    ColorStyleCreateDto createRequest = ColorStyleCreateDto.builder()
        .explain("test")
        .name("test")
        .styleCode(StyleCode.CHAT_ROOM_MENU_ICON_COLOR)
        .build();
    MockHttpServletRequestBuilder requestBuilder = mockMvcUtils.addAuthentication(
        MockMvcRequestBuilders
            .post("/api/color-styles")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(createRequest)),
        TestClientDto.fromEntity(rootUser)
    );
    // When
    ColorStyleResponse response = mockMvcUtils.parseResponse(
        mockMvc.perform(requestBuilder).andExpect(status().isOk()),
        new TypeReference<>() {
        });
    // then
    assertThat(response)
        .extracting(
            ColorStyleResponse::getName,
            ColorStyleResponse::getStyleCode,
            ColorStyleResponse::getExplain
        )
        .containsExactly(
            createRequest.getName(),
            createRequest.getStyleCode(),
            createRequest.getExplain()
        );
    assertColorStyleResponse(response);
  }

  @Test
  @DisplayName("ColorStyle 조회 테스트")
  void getColorStyle_success() throws Exception {
    // Given
    ColorStyle savedColorStyle = createColorStyle(StyleCode.BACKGROUND_COLOR);
    MockHttpServletRequestBuilder requestBuilder = mockMvcUtils.addAuthentication(
        MockMvcRequestBuilders
            .get("/api/color-styles/{id}", savedColorStyle.getColorStyleId())
            .contentType(MediaType.APPLICATION_JSON),
        TestClientDto.fromEntity(rootUser)
    );
    // When
    ColorStyleResponse response = mockMvcUtils.parseResponse(
        mockMvc.perform(requestBuilder).andExpect(status().isOk()),
        new TypeReference<>() {
        });
    // then
    compareResponseWithColorStyle(response, savedColorStyle);
  }

  @Test
  @DisplayName("ColorStyle 전체 조회 테스트")
  void getAllColorStyles_success() throws Exception {
    // Given
    Map<Integer, ColorStyle> expectedMap = IntStream.range(0, 5)
        .mapToObj(i -> createColorStyle(StyleCode.BACKGROUND_COLOR))
        .collect(Collectors.toMap(
            ColorStyle::getColorStyleId,
            Function.identity()));
    // When
    List<ColorStyleResponse> responses = mockMvcUtils.doAuthRequest(
        MockMvcRequestDto.<Void, List<ColorStyleResponse>>builder()
            .mockMvc(mockMvc)
            .path("/api/color-styles")
            .httpMethod(HttpMethod.GET)
            .clientDto(TestClientDto.fromEntity(rootUser))
            .statusCode(200)
            .responseType(new TypeReference<>() {
            })
            .build()
    );
    // then
    assertThat(responses).hasSize(expectedMap.size());
    for (ColorStyleResponse res : responses) {
      ColorStyle expected = expectedMap.get(res.getColorStyleId());
      assertThat(expected).isNotNull();
      compareResponseWithColorStyle(res, expected);
    }
  }

  @Test
  @DisplayName("ColorStyle 수정 테스트")
  void updateColorStyle_success() throws Exception {
    // Given
    ColorStyle savedColorStyle = createColorStyle(StyleCode.BACKGROUND_COLOR);
    ColorStyleUpdateRequest updateRequest = ColorStyleUpdateRequest.builder()
        .explain(UUID.randomUUID().toString())
        .name(UUID.randomUUID().toString())
        .styleCode(StyleCode.BACKGROUND_COLOR)
        .build();
    // When
    ColorStyleResponse response = mockMvcUtils.doAuthRequest(
        MockMvcRequestDto.<ColorStyleUpdateRequest, ColorStyleResponse>builder()
            .mockMvc(mockMvc)
            .path(String.format("/api/color-styles/%d", savedColorStyle.getColorStyleId()))
            .httpMethod(HttpMethod.PUT)
            .clientDto(TestClientDto.fromEntity(rootUser))
            .body(updateRequest)
            .statusCode(200)
            .responseType(new TypeReference<>() {
            })
            .build()
    );
    // then
    assertThat(response)
        .extracting(
            ColorStyleResponse::getName,
            ColorStyleResponse::getStyleCode,
            ColorStyleResponse::getExplain
        )
        .containsExactly(
            updateRequest.getName(),
            updateRequest.getStyleCode(),
            updateRequest.getExplain()
        );
    assertColorStyleResponse(response);
  }

  @Test
  @DisplayName("color style 시드 데이터를 생성한다")
  public void upsertColorStyleBySeed_onlyInsert() throws Exception {
    // given
    List<ColorStyle> seeds = seeder.readSeed();
    // when
    SeedResult response = mockMvcUtils.doAuthRequest(
        MockMvcRequestDto.<Void, SeedResult>builder()
            .mockMvc(mockMvc)
            .httpMethod(HttpMethod.PUT)
            .path("/api/color-styles/seed")
            .clientDto(TestClientDto.fromEntity(rootUser))
            .statusCode(200)
            .responseType(new TypeReference<>() {
            })
            .build()
    );
    // then
    assertThat(response.getCreated()).isEqualTo(seeds.size());
  }

  @Test
  @DisplayName("color style 시드 데이터를 주입 시, 시드에 변경이 없으면 갱신하지 않는다")
  public void upsertColorStyleBySeed_shouldNotUpdateWhenSeedUnchanged() throws Exception {
    // given
    seeder.upsertColorStyleSeed();
    // when
    SeedResult response = mockMvcUtils.doAuthRequest(
        MockMvcRequestDto.<Void, SeedResult>builder()
            .mockMvc(mockMvc)
            .httpMethod(HttpMethod.PUT)
            .path("/api/color-styles/seed")
            .clientDto(TestClientDto.fromEntity(rootUser))
            .statusCode(200)
            .responseType(new TypeReference<>() {
            })
            .build()
    );
    // then
    assertThat(response.getUpdated()).isEqualTo(0);
    assertThat(response.getCreated()).isEqualTo(0);
  }
}
