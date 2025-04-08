package com.theme.controller;

import com.theme.dto.UserResponseDto;
import com.theme.service.UserRetrieveService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UserRetrieveController {
    private final UserRetrieveService userRetrieveService;

    public UserRetrieveController(UserRetrieveService userRetrieveService) {
        this.userRetrieveService = userRetrieveService;
    }

    @GetMapping("/email")
    public ResponseEntity<UserResponseDto> getUserByEmail(@RequestParam("email") String email) {
        return ResponseEntity.ok(userRetrieveService.getUserByEmail(email));
    }
}
