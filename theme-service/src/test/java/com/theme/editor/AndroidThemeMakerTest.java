package com.theme.editor;

import com.theme.android.editor.AndroidThemeMaker;
import com.theme.theme.domain.ThemeComponent;
import com.theme.theme.repository.ThemeComponentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class AndroidThemeMakerTest {
    @Autowired
    AndroidThemeMaker androidThemeMaker;

    @Autowired
    ThemeComponentRepository themeComponentRepository;

    @Test
    void makeTheme() {
        // given
        themeComponentRepository.save(ThemeComponent.builder()
                .themeName("test theme")
                .userEmail("test@test.com")
                .isDone(true)
                .versionNumber("0.0.0")
                .versionName("test version")
                .build());
        // when & then
        assertDoesNotThrow(() -> androidThemeMaker.makeTheme(1));
    }
}