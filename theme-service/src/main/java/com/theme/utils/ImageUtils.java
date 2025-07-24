package com.theme.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Slf4j
@Component
public class ImageUtils {
    public byte[] loadImageBytes(String imageUrl) {
        try {
            Path imagePath = Paths.get(imageUrl);
            return Files.readAllBytes(imagePath);
        }catch (IOException e) {
            log.error(e.getMessage());
            return null;
        }
    }
}
