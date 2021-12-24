package org.sterl.filesync.sync.activity;

import java.nio.file.FileVisitor;
import java.nio.file.Path;

import org.sterl.filesync.sync.model.CopyFileStatistics;

import lombok.Getter;

public abstract class FileVisitorStrategy implements FileVisitor<Path> {

    @Getter
    protected CopyFileStatistics stats = new CopyFileStatistics();
    /**
     * Resets the current stats and returns the old stats.
     * @return the old statistics
     */
    public CopyFileStatistics resetStats() {
        CopyFileStatistics old = this.stats;
        this.stats = new CopyFileStatistics();
        return old;
    }
    
    public abstract Path getSourceDir();
}
