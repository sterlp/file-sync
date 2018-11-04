package org.sterl.filesync.sync.activity;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.sterl.filesync.copy.actvity.CopyStrategy;
import org.sterl.filesync.sync.model.CopyFileStatistics;

import lombok.Getter;

/**
 * Visitor which copies one "source" to a different "destination". Not thread save.
 */
@Component
public class CopyFileVisitorBA implements FileVisitor<Path> {
    private final CopyStrategy strategy;
    @Getter
    private volatile CopyFileStatistics stats = new CopyFileStatistics();
    private Set<String> ignoreList;
    private final List<Path> visited = new ArrayList<>();

    @Autowired
    public CopyFileVisitorBA(CopyStrategy copyStrategy, Set<String> ignoreList) throws IOException {
        super();
        this.strategy = copyStrategy;
        this.ignoreList = ignoreList;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path sourceDir, BasicFileAttributes attrs) throws IOException {
        stats.addDirectoryFound();
        if (strategy.created(sourceDir)) {
            stats.addDirectoryCreated();
        }
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path sourceFile, BasicFileAttributes attrs) throws IOException {
        if (ignoreList.contains(sourceFile.getFileName().toString())) {
            stats.addFileIgnored();
        } else {
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
        return FileVisitResult.CONTINUE;
    }
    /**
     * Resets the current stats and returns the old stats.
     * @return the old statistics
     */
    public CopyFileStatistics resetStats() {
        CopyFileStatistics old = this.stats;
        this.stats = new CopyFileStatistics();
        return old;
    }

    public Path getSourceDir() {
        return this.strategy.getSourceDir();
    }
}