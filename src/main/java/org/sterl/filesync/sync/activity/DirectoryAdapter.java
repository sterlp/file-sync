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

    private final FileComperator comperator;

    public DirectoryAdapter(Path sourceDir, Path destinationDir, FileComperator comperator) throws IOException {
        super();
        FileUtil.assertWriteableDirectory(sourceDir);
        FileUtil.createDirectoryIfNeeded(destinationDir);
        this.sourceDir = sourceDir;
        this.destinationDir = destinationDir;
        this.comperator = comperator;
    }
    public DirectoryAdapter(Path sourceDir, Path destinationDir) throws IOException {
        this(sourceDir, destinationDir, new FileComperator());
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
        if (!Files.isDirectory(source)) {
            return handleFile(source);
        }
        return false;
    }

    private boolean handleFile(Path sourceFile) throws IOException {
        final Path destinationFile = resolveDestination(sourceFile);
        if (comperator.isNewer(sourceFile, destinationFile)) {
            FileUtil.createDirectoryIfNeeded(destinationFile.getParent());
            Files.copy(sourceFile, destinationFile, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
            LOGGER.debug("File copied to {}", destinationFile);
            if (comperator.adjust(sourceFile, destinationFile)) {
                LOGGER.info("Time diff adjusted to {}ms directories have a time skew.", comperator.getTimeDiff());
            }
            return true;
        } else {
            return false;
        }
    }

    /*
    private boolean handleDir(Path source) throws IOException {
        final Path newDir = resolveDestination(source);
        boolean result;
        if (!Files.exists(newDir)) {
            Files.createDirectories(newDir);
            LOGGER.debug("Created directory {}", newDir);
            result = true;
        } else {
            result = false;
        }
        return result;
    }*/

    /**
     * Resolve the destination {@link Path} using the given source {@link Path}.
     */
    public Path resolveDestination(Path source) {
        final Path relativeSource = this.sourceDir.relativize(source);
        return this.destinationDir.resolve(relativeSource);
    }
}
