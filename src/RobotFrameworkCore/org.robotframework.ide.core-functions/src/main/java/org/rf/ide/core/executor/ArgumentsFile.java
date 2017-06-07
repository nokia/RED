/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.executor;

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

import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
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
        // it's deprecated although we don't need security here, so md5 is fine
        @SuppressWarnings("deprecation")
        final HashFunction md5Hasher = Hashing.md5();
        final HashCode hash = md5Hasher.hashString(content, Charsets.UTF_8);

        final String fileName = "args_" + Strings.padStart(Integer.toHexString(hash.asInt()), 8, '0') + ".arg";
        final Path dir = RobotRuntimeEnvironment.createTemporaryDirectory();

        for (final File existingArgFile : dir.toFile().listFiles((d, name) -> name.equals(fileName))) {
            final HashCode candidateHash = md5Hasher
                    .hashString(Files.asCharSource(existingArgFile, Charsets.UTF_8).read(), Charsets.UTF_8);
            if (hash.equals(candidateHash)) {
                return existingArgFile;
            }
        }

        final File filePath = dir.resolve(fileName).toFile();
        writeTo(filePath);
        return filePath;
    }

    private void writeTo(final File file) throws IOException {
        if (!file.exists()) {
            file.createNewFile();
        }
        Files.asCharSink(file, Charsets.UTF_8).write(generateContent());
    }
}
