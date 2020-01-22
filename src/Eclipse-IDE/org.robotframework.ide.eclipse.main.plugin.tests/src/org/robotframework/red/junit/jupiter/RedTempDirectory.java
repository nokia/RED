/*
 * Copyright 2020 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.junit.jupiter;

import static java.nio.file.FileVisitResult.CONTINUE;
import static java.util.stream.Collectors.joining;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Consumer;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.io.TempDir;
import org.junit.platform.commons.support.ModifierSupport;

/**
 * FIXME: this temporary workaround for tycho-surefire issues with JUnit5 versions. To be removed
 * once it would be possible to use TempDirectory as provided by JUnit5
 */
public class RedTempDirectory implements BeforeAllCallback, BeforeEachCallback, AfterEachCallback, AfterAllCallback {

    public static File createNewFile(final File tempFolder, final String name) throws IOException {
        final File file = new File(tempFolder, name);
        file.createNewFile();
        return file;
    }

    public static File createNewDir(final File tempFolder, final String name) throws IOException {
        final File file = new File(tempFolder, name);
        file.mkdirs();
        return file;
    }

    private static final Namespace NAMESPACE = Namespace.create(RedTempDirectory.class);

    private static final String KEY = "temp.dir";

    private static final String TEMP_DIR_PREFIX = "junit";

    @Override
    public void beforeAll(final ExtensionContext context) throws Exception {
        FieldsSupport.handleFields(context.getRequiredTestClass(), true, TempDir.class,
                setNewTempDirectory(context, null));
    }

    @Override
    public void beforeEach(final ExtensionContext context) throws Exception {
        FieldsSupport.handleFields(context.getRequiredTestClass(), false, TempDir.class,
                setNewTempDirectory(context, context.getRequiredTestInstance()));
    }

    @Override
    public void afterEach(final ExtensionContext context) throws Exception {
        final CloseablePath dir = context.getStore(NAMESPACE).get(createStoreKey(context), CloseablePath.class);
        if (dir != null) {
            dir.close();
        }
    }

    @Override
    public void afterAll(final ExtensionContext context) throws Exception {
        final CloseablePath dir = context.getStore(NAMESPACE).get(createStoreKey(context), CloseablePath.class);
        if (dir != null) {
            dir.close();
        }
    }

    private static Consumer<Field> setNewTempDirectory(final ExtensionContext context, final Object testInstance) {
        return field -> {
            assertValidFieldCandidate(field);
            try {
                FieldsSupport.makeAccessible(field).set(testInstance, getPathOrFile(field.getType(), context));
            } catch (final Throwable t) {
                throw new RuntimeException(t);
            }
        };
    }

    private static void assertValidFieldCandidate(final Field field) {
        assertSupportedType("field", field.getType());
        if (ModifierSupport.isPrivate(field)) {
            throw new ExtensionConfigurationException("@TempDir field [" + field + "] must not be private.");
        }
    }

    private static void assertSupportedType(final String target, final Class<?> type) {
        if (type != Path.class && type != File.class) {
            throw new ExtensionConfigurationException("Can only resolve @TempDir " + target + " of type "
                    + Path.class.getName() + " or " + File.class.getName() + " but was: " + type.getName());
        }
    }

    private static Object getPathOrFile(final Class<?> type, final ExtensionContext context) {
        final Path path = context.getStore(NAMESPACE)
                .getOrComputeIfAbsent(createStoreKey(context), key -> createTempDir(), CloseablePath.class)
                .get();
        return type == Path.class ? path : path.toFile();
    }

    private static String createStoreKey(final ExtensionContext context) {
        final String className = context.getRequiredTestClass().getSimpleName();
        return KEY + ":" + context.getTestMethod().map(method -> className + "#" + method.getName()).orElse(className);
    }

    private static CloseablePath createTempDir() {
        try {
            return new CloseablePath(Files.createTempDirectory(TEMP_DIR_PREFIX));
        } catch (final Exception ex) {
            throw new ExtensionConfigurationException("Failed to create default temp directory", ex);
        }
    }

    private static class CloseablePath {

        private final Path dir;

        CloseablePath(final Path dir) {
            this.dir = dir;
        }

        Path get() {
            return dir;
        }

        void close() throws IOException {
            final SortedMap<Path, IOException> failures = deleteAllFilesAndDirectories();
            if (!failures.isEmpty()) {
                throw createIOExceptionWithAttachedFailures(failures);
            }
        }

        private SortedMap<Path, IOException> deleteAllFilesAndDirectories() throws IOException {
            if (Files.notExists(dir)) {
                return Collections.emptySortedMap();
            }

            final SortedMap<Path, IOException> failures = new TreeMap<>();
            Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {

                @Override
                public FileVisitResult visitFile(final Path file, final BasicFileAttributes attributes) {
                    return deleteAndContinue(file);
                }

                @Override
                public FileVisitResult postVisitDirectory(final Path dir, final IOException exc) {
                    return deleteAndContinue(dir);
                }

                private FileVisitResult deleteAndContinue(final Path path) {
                    try {
                        Files.delete(path);
                    } catch (final IOException ex) {
                        failures.put(path, ex);
                    }
                    return CONTINUE;
                }
            });
            return failures;
        }

        private IOException createIOExceptionWithAttachedFailures(final SortedMap<Path, IOException> failures) {
            final String joinedPaths = failures.keySet()
                    .stream()
                    .peek(this::tryToDeleteOnExit)
                    .map(this::relativizeSafely)
                    .map(String::valueOf)
                    .collect(joining(", "));
            final IOException exception = new IOException("Failed to delete temp directory " + dir.toAbsolutePath()
                    + ". The following paths could not be deleted (see suppressed exceptions for details): "
                    + joinedPaths);
            failures.values().forEach(exception::addSuppressed);
            return exception;
        }

        private void tryToDeleteOnExit(final Path path) {
            try {
                path.toFile().deleteOnExit();
            } catch (final UnsupportedOperationException ignore) {
            }
        }

        private Path relativizeSafely(final Path path) {
            try {
                return dir.relativize(path);
            } catch (final IllegalArgumentException e) {
                return path;
            }
        }
    }

}
           return path;
            }
        }
    }

}
