/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.execution;

import static com.google.common.collect.Streams.zip;
import static java.util.stream.Collectors.joining;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;

import org.rf.ide.core.RedTemporaryDirectory;

import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;

public class ArgumentsFile {

    private final List<String> argNames;

    private final List<String> argValues;

    ArgumentsFile() {
        this.argNames = new ArrayList<>();
        this.argValues = new ArrayList<>();
    }

    void addLine(final String argumentName) {
        addLine(argumentName, "");
    }

    void addLine(final String argumentName, final String value) {
        argNames.add(argumentName);
        argValues.add(value);
    }

    void addCommentLine(final String comment) {
        if (comment.startsWith("#")) {
            argNames.add(comment);
        } else {
            argNames.add("# " + comment);
        }
        argValues.add("");
    }

    public String generateContent() {
        if (argNames.isEmpty() || argValues.isEmpty()) {
            return "";
        }
        final Optional<String> longestArg = argNames.stream()
                .filter(line -> !line.startsWith("#"))
                .max(Comparator.comparingInt(String::length));

        final BiFunction<String, String, String> joiningFunction =
                (arg, val) -> arg.startsWith("#") ? arg
                        : (Strings.padEnd(arg, longestArg.get().length() + 1, ' ') + val);
        return zip(argNames.stream(), argValues.stream(), joiningFunction)
                .map(String::trim)
                .collect(joining("\n"));
    }

    File writeToTemporaryOrUseAlreadyExisting() throws IOException {
        final String content = generateContent();
        final HashCode hash = hashString(content);

        final String fileName = "args_" + Strings.padStart(Integer.toHexString(hash.asInt()), 8, '0') + ".arg";
        final Path dir = RedTemporaryDirectory.createTemporaryDirectoryIfNotExists();

        for (final File existingArgFile : dir.toFile().listFiles((d, name) -> name.equals(fileName))) {
            final HashCode candidateHash = hashString(readFrom(existingArgFile));
            if (hash.equals(candidateHash)) {
                return existingArgFile;
            }
        }

        final File filePath = dir.resolve(fileName).toFile();
        writeTo(filePath, content);
        return filePath;
    }

    @SuppressWarnings("deprecation")
    private static HashCode hashString(final String content) {
        // it's deprecated although we don't need security here, so md5 is fine
        return Hashing.md5().hashString(content, Charsets.UTF_8);
    }

    private static String readFrom(final File file) throws IOException {
        return Files.asCharSource(file, Charsets.UTF_8).read();
    }

    private static void writeTo(final File file, final String content) throws IOException {
        if (!file.exists()) {
            final boolean created = file.createNewFile();
            if (!created) {
                throw new IllegalStateException("The arguments file '" + file.getName() + "' already exists");
            }
        }
        Files.asCharSink(file, Charsets.UTF_8).write(content);
    }
}
