package org.sterl.filesync.sync.model;

import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
public class CopyFileStatistics {
    private long filesFoundCount = 0;
    private long copiedFilesCount = 0;
    /** Filed ignored because of black listening */
    private long ignoredFileCount = 0;
    /** Count not changed files */
    private long notChangedFileCount = 0;
    private long directoriesFoundCount = 0;
    private long directoriesCreatedCount = 0;
    private long deletedResourcesCount = 0;
    
    public long addFileFound() {
        return ++filesFoundCount;
    }
    public long addFileCopied() {
        return ++copiedFilesCount;
    }
    public long addFileIgnored() {
        return ++ignoredFileCount;
    }
    public long addNotChangedFile() {
        return ++notChangedFileCount;
    }
    
    public long addDirectoryFound() {
        return ++directoriesFoundCount;
    }
    public long addDirectoryCreated() {
        return ++directoriesCreatedCount;
    }
    public long addDeletedResources(long amount) {
        deletedResourcesCount += amount;
        return deletedResourcesCount;
    }
    public void addFilesCopied(int filesCopied) {
        this.copiedFilesCount += filesCopied;
    }
}