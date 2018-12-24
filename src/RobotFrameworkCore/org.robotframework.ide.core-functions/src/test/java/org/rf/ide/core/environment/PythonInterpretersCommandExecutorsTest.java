/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.environment;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.junit.Test;

import com.google.common.io.Files;

public class PythonInterpretersCommandExecutorsTest {

    @Test
    public void allRobotSessionServerFilesAreDefined() {
        final File[] resourceFiles = getResourceFiles("scripts");
        final List<String> robotSessionServerFiles = filterPythonScriptFiles(resourceFiles,
                name -> !name.equals("interruptor.py"));
        assertThat(PythonInterpretersCommandExecutors.SCRIPT_FILES).containsAll(robotSessionServerFiles);
    }

    private static List<String> filterPythonScriptFiles(final File[] files, final Predicate<String> namePredicate) {
        return Arrays.stream(files)
                .filter(file -> Files.getFileExtension(file.getPath()).equals("py"))
                .map(File::getName)
                .filter(namePredicate)
                .collect(Collectors.toList());
    }

    private static File[] getResourceFiles(final String folder) {
        final ClassLoader loader = Thread.currentThread().getContextClassLoader();
        final URL url = loader.getResource(folder);
        final String path = url.getPath();
        return new File(path).listFiles();
    }
}
