package com.komentum.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

  public static String authRequiredPath = "/must/auth";

  @GetMapping("/allow/all")
  public ResponseEntity<Boolean> publicMethod() {
    return ResponseEntity.ok(true);
  }

  @GetMapping("/allow/get")
  public ResponseEntity<Boolean> privateMethod() {
    return ResponseEntity.ok(true);
  }

  @GetMapping("/must/auth")
  public ResponseEntity<Boolean> mustAuth() {
    return ResponseEntity.ok(true);
  }
}
