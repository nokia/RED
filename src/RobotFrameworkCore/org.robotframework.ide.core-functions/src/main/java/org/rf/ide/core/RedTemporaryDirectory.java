/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core;

import static java.util.stream.Collectors.joining;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.stream.Stream;

public final class RedTemporaryDirectory {

    private static final String DIR_NAME_PREFIX = "RobotTempDir";

    private static Path temporaryDirectory;

    public static File copyRedpydevdPackage() throws IOException {
        copyScriptFile("red_pydevd_package", "redpydevd", "__init__.py");
        copyScriptFile("red_pydevd_package", "redpydevd", "__main__.py");
        copyScriptFile("red_pydevd_package", "redpydevd", "redpydevd.py");
        copyScriptFile("red_pydevd_package", "__init__.py");
        copyScriptFile("red_pydevd_package", "setup.py");
        copyScriptFile("red_pydevd_package", "README.md");
        copyScriptFile("red_pydevd_package", "LICENSE");
        copyScriptFile("red_pydevd_package", "MANIFEST.in");

        return new File(temporaryDirectory.toString() + File.separator + "red_pydevd_package");
    }

    public static File getRedpydevdSetup() throws IOException {
        final Path tempDir = createTemporaryDirectoryIfNotExists();
        return new File(tempDir.toString() + File.separator + "red_pydevd_package" + File.separator + "setup.py");
    }

    public static InputStream getRedpydevdInitAsStream() throws IOException {
        return getScriptFileAsStream("red_pydevd_package", "redpydevd", "__init__.py");
    }

    public static InputStream getRedpydevdFileAsStream() throws IOException {
        return getScriptFileAsStream("red_pydevd_package", "redpydevd", "redpydevd.py");
    }

    public static File copyScriptFile(final String filename) throws IOException {
        try (InputStream source = getScriptFileAsStream(filename)) {
            final File tempFile = getTemporaryFile(filename);
            Files.copy(source, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            return tempFile;
        }
    }

    public static File copyScriptFile(final String dir, final String... filepath) throws IOException {
        try (InputStream source = getScriptFileAsStream(dir, filepath)) {
            final Path tempDir = createTemporaryDirectoryIfNotExists();
            final File dirFile = new File(tempDir.toString() + File.separator + dir + File.separator
                    + Stream.of(filepath).limit(filepath.length - 1).collect(joining(File.separator)));

            if (!dirFile.exists()) {
                final boolean dirsCreated = dirFile.mkdirs();
                if (!dirsCreated) {
                    throw new IOException("Unable to create '" + dirFile.getAbsolutePath() + "' directory");
                }
            }
            final File tempFile = new File(dirFile, filepath[filepath.length - 1]);
            Files.copy(source, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            return tempFile;
        }
    }

    public static InputStream getScriptFileAsStream(final String filename) throws IOException {
        return RedTemporaryDirectory.class.getResourceAsStream("/scripts/" + filename);
    }

    public static InputStream getScriptFileAsStream(final String dir, final String... filepath) throws IOException {
        return RedTemporaryDirectory.class.getResourceAsStream("/scripts/" + dir + "/" + String.join("/", filepath));
    }

    public static File getTemporaryFile(final String filename) throws IOException {
        final Path tempDir = createTemporaryDirectoryIfNotExists();
        return new File(tempDir.toString() + File.separator + filename);
    }

    public static File createTemporaryFile(final String filename) throws IOException {
        final File tempFile = getTemporaryFile(filename);

        if (tempFile.exists()) {
            final boolean deleted = tempFile.delete();
            if (!deleted) {
                throw new IOException("Unable to delete '" + tempFile.getAbsolutePath() + "' file");
            }
        }
        final boolean created = tempFile.createNewFile();
        if (!created) {
            throw new IOException("Unable to create '" + tempFile.getAbsolutePath() + "' file");
        }
        return tempFile;
    }

    public static synchronized Path createTemporaryDirectoryIfNotExists() throws IOException {
        if (temporaryDirectory != null && temporaryDirectory.toFile().exists()) {
            return temporaryDirectory;
        }
        temporaryDirectory = Files.createTempDirectory(DIR_NAME_PREFIX);
        addRemoveTemporaryDirectoryHook();
        return temporaryDirectory;
    }

    private static void addRemoveTemporaryDirectoryHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (temporaryDirectory == null) {
                return;
            }
            try {
                Files.walkFileTree(temporaryDirectory, new SimpleFileVisitor<Path>() {

                    @Override
                    public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs)
                            throws IOException {
                        Files.delete(file);
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult postVisitDirectory(final Path dir, final IOException exc)
                            throws IOException {
                        Files.delete(dir);
                        return FileVisitResult.CONTINUE;
                    }
                });
            } catch (final IOException e) {
                // temporary files and directory will not be removed
            }
        }));
    }

}
