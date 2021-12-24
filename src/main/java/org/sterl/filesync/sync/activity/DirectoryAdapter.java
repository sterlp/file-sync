package org.sterl.filesync.sync.activity;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sterl.filesync.compare.FileComperator;
import org.sterl.filesync.file.FileUtil;

import lombok.Getter;

public class DirectoryAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(DirectoryAdapter.class);
    @Getter
    private final Path sourceDir;
    @Getter
    private final Path destinationDir;

    private final FileComperator comperator = new FileComperator();

    public DirectoryAdapter(Path sourceDir, Path destinationDir) {
        super();
        FileUtil.assertWriteableDirectory(sourceDir);
        try {
            FileUtil.createDirectoryIfNeeded(destinationDir);
        } catch (IOException e) {
            throw new IllegalArgumentException("Destination dir: " + destinationDir, e);
        }
        this.sourceDir = sourceDir;
        this.destinationDir = destinationDir;
    }

    public boolean created(Path source) throws IOException {
        return copy(source);
    }
    
    public boolean changed(Path source) throws IOException {
        return copy(source);
    }
    
    public boolean deleted(Path source) throws IOException {
        return FileUtil.delete(destinationDir.resolve(sourceDir.relativize(source))) != 0;
    }

    private boolean copy(Path source) throws IOException {
        if (Files.isDirectory(source)) {
            return handleDir(source);
        } else {
            return handleFile(source);
        }
    }

    private boolean handleFile(Path sourceFile) throws IOException {
        final Path relativeSourceFile = sourceDir.relativize(sourceFile);
        final Path destinationFile = destinationDir.resolve(relativeSourceFile);
        boolean result;
        if (comperator.isSameFile(sourceFile, destinationFile)) {
            result = false;
        } else {
            FileUtil.createDirectoryIfNeeded(destinationFile.getParent());
            Files.copy(sourceFile, destinationFile, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
            LOGGER.debug("File copied to {}", destinationFile);
            if (comperator.adjust(sourceFile, destinationFile)) {
                LOGGER.info("Time diff adjusted to {}ms directories have a time skew.", comperator.getTimeDiff());
            }
            result = true;
        }
        return result;
    }

    private boolean handleDir(Path source) throws IOException {
        final Path relativeSourceDir = this.sourceDir.relativize(source);
        final Path newDir = this.destinationDir.resolve(relativeSourceDir);
        boolean result;
        if (!Files.exists(newDir)) {
            Files.createDirectories(newDir);
            LOGGER.debug("Created directory {}", newDir);
            result = true;
        } else {
            result = false;
        }
        return result;
    }
}
