package org.sterl.filesync.copy.actvity;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sterl.filesync.file.FileUtil;

import lombok.Getter;

public class SimpleCopyBA implements CopyStrategy {
    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleCopyBA.class);
    @Getter
    private final Path sourceDir;
    @Getter
    private final Path destinationDir;

    public SimpleCopyBA(Path sourceDir, Path destinationDir) throws IOException {
        super();
        if (!Files.exists(sourceDir)) {
            throw new IllegalArgumentException("Source directory has to exist but not found: " + sourceDir);
        }
        if (Files.exists(destinationDir)) {
            if (!Files.isDirectory(destinationDir)) {
                throw new IllegalArgumentException("Distination is not a directory: " + destinationDir);
            } else if(!Files.isWritable(destinationDir)) {
                throw new IllegalArgumentException("Cannot write to: " + destinationDir);
            }
        } else {
            // well if we don't have the destination DIR we create it, which also checks if we can write
            Files.createDirectories(destinationDir);
        }
        this.sourceDir = sourceDir;
        this.destinationDir = destinationDir;
    }

    @Override
    public boolean created(Path source) throws IOException {
        return copy(source);
    }
    @Override
    public boolean changed(Path source) throws IOException {
        return copy(source);
    }
    
    @Override
    public boolean deleted(Path source) throws IOException {
        return FileUtil.delete(destinationDir.resolve(sourceDir.relativize(source))) != 0;
    }
    
    boolean copy(Path source) throws IOException {
        if (Files.isDirectory(source)) {
            return handleDir(source);
        } else {
            return handleFile(source);
        }
    }
    
    private boolean handleFile(Path sourceFile) throws IOException {
        final Path relativeSourceFile = sourceDir.relativize(sourceFile);
        final Path desitnationFile = destinationDir.resolve(relativeSourceFile);
        boolean result;
        if (Files.exists(desitnationFile) && FileUtil.isSameFile(sourceFile, desitnationFile)) {
            result = false;
        } else {
            final Path desitnationDir = desitnationFile.getParent();
            if (desitnationDir != null && !Files.exists(desitnationDir)) {
                Files.createDirectories(desitnationDir);
            }
            Files.copy(sourceFile, desitnationFile, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
            LOGGER.debug("File copied to {}", desitnationFile);
            result = true;
        }
        return result;
    }

    boolean handleDir(Path source) throws IOException {
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
