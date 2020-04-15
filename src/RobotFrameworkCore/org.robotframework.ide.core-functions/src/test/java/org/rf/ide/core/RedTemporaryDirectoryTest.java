/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core;


import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.google.common.io.Files;

public class RedTemporaryDirectoryTest {

    @Test
    public void allRobotSessionServerFilesAreCreated() throws Exception {
        final Set<File> createdSessionServerFiles = RedTemporaryDirectory.copySessionServerFiles();
        final Set<String> sessionServerFileNames = Arrays.stream(getResourceFiles("/scripts"))
                .filter(RedTemporaryDirectoryTest::isRobotSessionServerFile)
                .map(File::getName)
                .collect(Collectors.toSet());

        assertThat(createdSessionServerFiles).extracting(f -> f.getName())
                .containsExactlyInAnyOrderElementsOf(sessionServerFileNames);
    }

    private static File[] getResourceFiles(final String folder) throws Exception {
        final String path = RedTemporaryDirectory.class.getResource(folder).getFile();
        return new File(URLDecoder.decode(path, "UTF-8")).listFiles();
    }

    private static boolean isRobotSessionServerFile(final File file) {
        final String extension = Files.getFileExtension(file.getPath());
        return extension.equals("jar") || extension.equals("py") && !file.getName().equals("interruptor.py");
    }
}
