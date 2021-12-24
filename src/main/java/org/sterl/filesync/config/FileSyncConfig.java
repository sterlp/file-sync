package org.sterl.filesync.config;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Configuration
@ConfigurationProperties("sync")
@Data  @AllArgsConstructor @NoArgsConstructor @Builder
public class FileSyncConfig {
    @Value("${sync.source}")
    private Path sourceDir;
    @Value("${sync.destination}")
    private Path destinationDir;
    @Builder.Default
    private Set<String> ignoreList = new HashSet<>();
    @Builder.Default
    private int maxChangeListenDepth = 10;
    @Builder.Default
    private int maxChangeListenDirectories = 1000;

    public static FileSyncConfig of(Path source, Path destination) {
        return FileSyncConfig.builder()
                             .sourceDir(source)
                             .destinationDir(destination)
                             .build()
                             .defaultIgnore();
    }

    public boolean isNotIgnored(Path file) {
        return !isIgnored(file);
    }
    public boolean isIgnored(Path file) {
        return ignoreList.contains(file.getFileName().toString());
    }

    public FileSyncConfig ignore(String toIgnore) {
        ignoreList.add(toIgnore);
        return this;
    }
    
    public FileSyncConfig defaultIgnore() {
        ignoreList.add(".DS_Store");
        return this;
    }
}
