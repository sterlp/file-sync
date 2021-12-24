package org.sterl.filesync.sync.activity;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * Copies all files from a given directory to a different directory.
 */
class CopyAllFilesFromToBA extends AbstractDirectorySync {
    private final DirectoryAdapter directoryAdapter;

    CopyAllFilesFromToBA(Path sourceDir, Path destinationDir, List<String> ignoreList) throws IOException {
        super(sourceDir, ignoreList);
        directoryAdapter = new DirectoryAdapter(sourceDir, destinationDir);
    }

    @Override
    protected int handle(Path path) throws IOException {
        return directoryAdapter.changed(path) ? 1 : 0;
    }
}
