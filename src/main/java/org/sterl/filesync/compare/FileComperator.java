package org.sterl.filesync.compare;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;

import org.sterl.filesync.file.FileUtil;

import lombok.Getter;

/**
 * Helper class which handles the time diff supported by different directories.
 */
public class FileComperator {
    @Getter
    private long timeDiff = 0;

    public boolean adjust(Path sourceFile, Path destinationFile) throws IOException {
        long diff = Files.getLastModifiedTime(sourceFile).toMillis()
                - Files.getLastModifiedTime(destinationFile).toMillis();

        return adjustTimeDiff(diff);
    }

    public boolean adjustTimeDiff(long newDiff) {
        if (newDiff > timeDiff && newDiff <= 1000) {
            timeDiff = newDiff;
            return true;
        } else {
            return false;
        }
    }

    public boolean isNewer(BasicFileAttributes attrs, Path destinationFile) throws IOException {
        if (!Files.exists(destinationFile)) return true;

        long time = FileUtil.compareModifiedTime(attrs, destinationFile);
        return time > timeDiff;
    }

    public boolean isNewer(Path sourceFile, Path destinationFile) throws IOException {
        if (!Files.exists(sourceFile)) return false;

        BasicFileAttributes sourceAtt = Files
                .getFileAttributeView(sourceFile, BasicFileAttributeView.class)
                .readAttributes();
        return isNewer(sourceAtt, destinationFile);
    }
}
