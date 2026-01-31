package com.komentum.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.komentum.auth.JwtUtils;
import com.komentum.config.EnableTestProfile;
import com.komentum.test.UserDataGenerator;
import com.komentum.user.domain.User;
import com.komentum.user.dto.LocalLoginRequestDto;
import com.komentum.user.dto.PasswordChangeRequsetDto;
import com.komentum.user.dto.UserAuthResponse;
import com.komentum.user.repository.UserRepository;
import com.komentum.user.service.UserAuthService;
import org.checkerframework.checker.units.qual.A;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@EnableTestProfile
@AutoConfigureMockMvc
@SpringBootTest
class UserAuthControllerTest {
    String email = "admin1@gmail.com";
    String password ="qwer123!";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserDataGenerator userDataGenerator;

    @Autowired
    private JwtUtils jwtUtils;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    User user;

    @BeforeEach
    void setUp(){
        userDataGenerator.deleteAllUsers();
        userDataGenerator.generateTestLocalUser(email, password);
        user = userRepository.findByUserEmail(email).orElseThrow();
    }

    @AfterEach
    void tearDown(){
        userDataGenerator.deleteAllUsers();
    }

    @Autowired
    private UserAuthController userAuthController;

    @Test
    @DisplayName("회원가입")
    void signUpWithLocalTest() throws Exception{
        //given
        String signUpEmail = "admin1@gmail.com";
        String signUpPassword ="qwer123!";
        LocalLoginRequestDto localLoginRequestDto
                = LocalLoginRequestDto.builder()
                .email(signUpEmail)
                        .password(signUpPassword)
                                .build();
        //when
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/api/auth/local/sign-up")
                .content(objectMapper.writeValueAsString(localLoginRequestDto))
                .contentType(MediaType.APPLICATION_JSON); //웬만하면 하는 게 좋음
        //then
        mockMvc.perform(request)
                .andExpect(status().is2xxSuccessful())
                .andReturn().getResponse().getContentAsString();
        assertThat(userRepository.findByUserEmail(email)).isPresent();
    }

    @Test
    @DisplayName("로그인 jwt 토큰 검증")
    void signInWithLocalTest() throws Exception {
        //given
        LocalLoginRequestDto localLoginRequestDto =
                LocalLoginRequestDto.builder()
                        .email(email)
                        .password(password)
                        .build();
        //when
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/api/auth/local/sign-in")
                .content(objectMapper.writeValueAsString(localLoginRequestDto))
                .contentType(MediaType.APPLICATION_JSON);

        //then
        UserAuthResponse userAuthResponse = objectMapper.readValue((mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString()), UserAuthResponse.class );

        //jwt token이 정상적인가
        assertThat(jwtUtils.validateToken(userAuthResponse.getAccessToken())).isTrue();

    }

    @Test
    @DisplayName("비밀번호 변경")
    void changePassword() throws Exception{
        //given
        String userEmail = "admin1@gmail.com";
        String newPassword = "123qwer!";

        PasswordChangeRequsetDto requsetDto = PasswordChangeRequsetDto.builder().
            currentPassword(password)
            .newPassword(newPassword)
            .build();

        String token = jwtUtils.generateAccessToken(user.getPublicUserId());

        //when
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.patch("/api/users/me/password")
            .content(objectMapper.writeValueAsString(requsetDto))
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer "+token);

        //then
        mockMvc.perform(request)
            .andExpect(status().is2xxSuccessful());

        User updatedUSer = userRepository.findByUserEmail(userEmail).orElseThrow();


        assertThat(updatedUSer.matchPassword(newPassword, passwordEncoder)).isTrue();
    }
}