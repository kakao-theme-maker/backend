package com.komentum.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

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
   * @param command docker command
   */
  public void runDockerProcess(String[] command) {
    try {
      ProcessBuilder processBuilder = new ProcessBuilder(command);
      processBuilder.redirectErrorStream(true);
      Process process = processBuilder.start();
      consumeStream(process.getInputStream());
      process.waitFor();
      if (process.exitValue() != 0) {
        String errorMessage =
            "Process exit value is not 0 (command: " + String.join(" ", command) + ")";
        log.error(errorMessage);
        throw new Exception(errorMessage);
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
