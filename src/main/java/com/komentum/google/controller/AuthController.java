package com.komentum.google.controller;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.komentum.google.dto.GoogleLoginRequestDto;
import com.komentum.google.service.GoogleOAuthService;
import com.komentum.user.dto.UserAuthResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final GoogleOAuthService googleOAuthService;

    @PostMapping("/google/sign-in")
    public ResponseEntity<?> googleLogin(@RequestBody GoogleLoginRequestDto requestDto){
        try {
            UserAuthResponse response = googleOAuthService.processGoogleAuth(requestDto.idToken());
            return ResponseEntity.ok(response);
        }catch (RuntimeException e){
            return ResponseEntity.badRequest().body("Google Login failed" + e.getMessage());
        }
    }
// 구현중
//    @PostMapping("/google/sign-out")
//    public ResponseEntity<?> googleLogout(@RequestHeader String authorization){
//
//    }

}
