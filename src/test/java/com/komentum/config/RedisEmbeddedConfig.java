package com.komentum.config;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.PostMapping;
import redis.embedded.RedisServer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import org.springframework.util.StringUtils;
import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.net.ServerSocket;


@Configuration
public class RedisEmbeddedConfig {
    private static final int REDIS_PORT = 6379;
    private RedisServer redisServer;

    @PostConstruct
    public void configRedisServer() throws IOException{
        int port = REDIS_PORT;
        if(isProcessRunning(getProcess(port))){
            port = getAvailablePort();
        }

        redisServer = new RedisServer(port);
        redisServer.start();
    }
    @PreDestroy
    public void stopRedisServer() throws IOException {
        if(redisServer != null){
            redisServer.stop();
        }
    }

    public int getAvailablePort() {
        try (ServerSocket socket = new ServerSocket(0)) {
            socket.setReuseAddress(true);
            return socket.getLocalPort();
        } catch (IOException e) {
            throw new IllegalStateException("No available port", e);
        }
    }

    private Process getProcess(int port) throws IOException{
        String os = System.getProperty("os.name").toLowerCase();

        if(os.contains("win")){
            String command = String.format("netstat -ano | find \"LISTEN\" | find \"%d\"", port);
            String[] shell = {"cmd.exe", "/y", "/c", command};
            return Runtime.getRuntime().exec(shell);
        }

        String command =String.format("netstat -nat | grep LISTEN | grep %d", port);
        String[] shell = {"/bin/sh", "-c", command};
        return Runtime.getRuntime().exec(shell);
    }

    private boolean isProcessRunning(Process process){
        String line;
        StringBuilder stringBuilder = new StringBuilder();

        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()))){
            while((line = bufferedReader.readLine()) != null){
                stringBuilder.append(line);
            }
        }catch (Exception e){
            throw new RuntimeException("process running read fail");
        }
        return StringUtils.hasText(stringBuilder.toString());

    }


}
