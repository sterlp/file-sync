package org.sterl.filesync.compare;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.sterl.filesync.file.FileUtil;

import lombok.Getter;

public class FileComperator {
    @Getter
    private long timeDiff = 0;

    public boolean isSameFile(Path sourceFile, Path destinationFile) throws IOException {
        return FileUtil.hasFileChanged(sourceFile, destinationFile, timeDiff);
    }

    public boolean adjust(Path sourceFile, Path destinationFile) throws IOException {
        long diff = Math.abs(Files.getLastModifiedTime(sourceFile).toMillis()
                - Files.getLastModifiedTime(destinationFile).toMillis());

        if (diff > timeDiff && diff <= 1000) {
            timeDiff = diff;
            return true;
        } else {
            return false;
        }
    }
}
