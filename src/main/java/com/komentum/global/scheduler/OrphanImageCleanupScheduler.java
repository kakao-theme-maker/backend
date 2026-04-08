package com.komentum.global.scheduler;

import com.komentum.global.service.OrphanImageCleanupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


@Component
@Slf4j
@RequiredArgsConstructor
public class OrphanImageCleanupScheduler {

  private final OrphanImageCleanupService orphanImageCleanupService;

  @Scheduled(cron = "0 0 3 * * 0")
  public void scheduleOrphanImageCleanup() {
    log.info("=== Scheduled orphan image cleanup started ===");
    try {
      int deletedCount = orphanImageCleanupService.cleanupOrphanImages();
      log.info("=== Scheduled orphan image cleanup finished. Deleted: {} files ===", deletedCount);
    } catch (Exception e) {
      log.error("=== Scheduled orphan image cleanup failed ===", e);
    }
  }
}
