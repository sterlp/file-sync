package org.sterl.filesync.sync.activity;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;
import org.sterl.filesync.config.FileSyncConfig;
import org.sterl.filesync.sync.model.CopyFileStatistics;

import lombok.Getter;

/**
 * Visitor which copies one "source" to a different "destination". Not thread save.
 */
@Component
public class MasterSlaveFileVisitorBA extends FileVisitorStrategy {
    private final DirectoryAdapter strategy;
    private final FileSyncConfig config;
    private final List<Path> visited = new ArrayList<>();

    public MasterSlaveFileVisitorBA(FileSyncConfig config) throws IOException {
        super();
        this.config = config;
        this.strategy = new DirectoryAdapter(config.getSourceDir(), config.getDestinationDir());
    }
    
    @Override
    public FileVisitResult preVisitDirectory(Path sourceDir, BasicFileAttributes attrs) throws IOException {
        if (attrs.isDirectory()) {
            stats.addDirectoryFound();
            if (strategy.created(sourceDir)) {
                stats.addDirectoryCreated();
            }
        }
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path sourceFile, BasicFileAttributes attrs) throws IOException {
        if (config.isIgnored(sourceFile)) {
            stats.addFileIgnored();
        } else if (attrs.isRegularFile()) {
            stats.addFileFound();
            visited.add(sourceFile);
            if (strategy.changed(sourceFile)) {
                stats.addFileCopied();
            } else {
                stats.addNotChangedFile();
            }
        }
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        visited.add(dir);
        final Path toVerify = strategy.getDestinationDir().resolve(strategy.getSourceDir().relativize(dir));
        long deletedAmount = new RemoveOrphanBA(toVerify, dir, visited).call().longValue();
        stats.addDeletedResources(deletedAmount);
        visited.clear();
        return FileVisitResult.CONTINUE;
    }

    public Path getSourceDir() {
        return this.strategy.getSourceDir();
    }

}