package org.sterl.filesync.config;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Configuration
@ConfigurationProperties("sync")
@Data @AllArgsConstructor @NoArgsConstructor
public class FileSyncConfig {
    @Value("${sync.source}")
    private Path sourceDir;
    @Value("${sync.destination}")
    private Path destinationDir;
    private Set<String> ignoreList = new HashSet<>();
    private int maxChangeListenDepth = 10;
    private int maxChangeListenDirectories = 1000;

    public boolean isNotIgnored(Path file) {
        return !ignoreList.contains(file.getFileName().toString());
    }

    public FileSyncConfig ignore(String toIgnore) {
        ignoreList.add(toIgnore);
        return this;
    }
}
