package org.sterl.filesync.sync.activity;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Copies all files from a given directory to a different directory.
 */
abstract class AbstractDirectorySync implements Callable<Integer> {
    /** List of paths which can be ignored during the check */
    protected final List<String> ignoreList = new ArrayList<>();
    protected final Path sourceDir;

    /**
     * @param sourceDir the directory to process
     * @param ignoreList the files to ignore relative to the source directory
     */
    protected AbstractDirectorySync(Path sourceDir, List<String> ignoreList) {
        super();
        this.sourceDir = sourceDir;
        if (ignoreList != null && !ignoreList.isEmpty()) {
            this.ignoreList.addAll(ignoreList);
        }
    }

    @Override
    public Integer call() throws IOException {
        Iterator<Path> paths = Files.list(sourceDir).iterator();
        int result = 0;
        while (paths.hasNext()) {
            final Path toCheck = paths.next();
            String pathString = sourceDir.relativize(toCheck).toString();
            if (!ignoreList.contains(pathString) && !ignoreList.contains(toCheck.getFileName().toString())) {
                result += handle(toCheck);
            }
        }
        return result;
    }

    protected abstract int handle(Path path) throws IOException;
}
