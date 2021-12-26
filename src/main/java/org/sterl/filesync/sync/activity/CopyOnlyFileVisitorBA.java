package org.sterl.filesync.sync.activity;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;
import org.sterl.filesync.compare.FileComperator;
import org.sterl.filesync.config.FileSyncConfig;

/**
 * Ensures that in the end we have the same files in both directories.
 */
@Component
public class CopyOnlyFileVisitorBA extends FileVisitorStrategy {
    private final DirectoryAdapter strategy;
    private final FileSyncConfig config;
    private final List<String> visited = new ArrayList<>();
    private final FileComperator comperator = new FileComperator();

    public CopyOnlyFileVisitorBA(FileSyncConfig config) throws IOException {
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

            final Path destinationFile = strategy.resolveDestination(sourceFile);
            if (comperator.isNewer(attrs, destinationFile)) {
                strategy.changed(sourceFile);
                stats.addFileCopied();
                visited.add(sourceFile.getFileName().toString());
                comperator.adjust(sourceFile, destinationFile);
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
        final Path toVerify = strategy.getDestinationDir().resolve(strategy.getSourceDir().relativize(dir));
        visited.addAll(config.getIgnoreList());
        int syncedBack = new CopyAllFilesFromToBA(toVerify, dir, comperator, visited).call();
        visited.clear();
        stats.addFilesCopied(syncedBack);
        return FileVisitResult.CONTINUE;
    }

    public Path getSourceDir() {
        return this.strategy.getSourceDir();
    }

}