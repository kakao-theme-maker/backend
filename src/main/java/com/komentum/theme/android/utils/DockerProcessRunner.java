package com.komentum.theme.android.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DockerProcessRunner {

  /**
   * consume stream from a docker container
   *
   * @param stream input stream from docker container
   */
  private void consumeStream(InputStream stream) {
    new Thread(() -> {
      try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
        String line;
        while ((line = reader.readLine()) != null) {
          log.info("[Docker] {}", line);
        }
      } catch (IOException e) {
        log.warn("[Docker Failed] Failed to consume stream: {}", e.getMessage());
      }
    }).start();
  }

  /**
   * run the docker command and log standard output and error
   *
   * @param processBuilder process builder with docker command
   */
  public void runDockerProcess(ProcessBuilder processBuilder) {
    try {
      processBuilder.redirectErrorStream(true);
      Process process = processBuilder.start();
      consumeStream(process.getInputStream());
      int exitCode = process.waitFor();
      if (exitCode != 0) {
        String errorMessage =
            "Process exit value is not 0 (command: " + String.join(" ", processBuilder.command())
                + ")";
        log.error(errorMessage);
        throw new IllegalStateException(errorMessage);
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
