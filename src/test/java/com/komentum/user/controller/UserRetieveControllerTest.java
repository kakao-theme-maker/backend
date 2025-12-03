package com.komentum.user.controller;


import com.komentum.config.EnableTestProfile;
import com.komentum.user.dto.UserResponseDto;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@EnableTestProfile
@AutoConfigureMockMvc
@SpringBootTest
public class UserRetieveControllerTest {


  @Autowired
  private MockMvc mockMvc;


  @Test
  @DisplayName("유저 조회")
  void inquiryUserTest() throws Exception{
    //given
    String userEmail = "admin1@gmail.com";
    String userName = "admin";
    String userProfileUrl = "https://example";
    int uploads = 3;
    int followers = 120;
    int following = 103;
    LocalDateTime createdAt = LocalDateTime.of(2025, 12, 25, 14, 28);

    UserResponseDto userResponseDto =
        UserResponseDto.builder()
            .userEmail(userEmail)
            .userName(userName)
            .userProfileUrl(userProfileUrl)
            .uploads(uploads)
            .followers(followers)
            .following(following)
            .build();

    //when
    MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get("/api/users/${email}");


  }

}
