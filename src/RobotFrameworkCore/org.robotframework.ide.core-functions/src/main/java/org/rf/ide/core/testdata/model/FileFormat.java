/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model;

import java.io.File;
import java.util.Optional;

public enum FileFormat {
    UNKNOWN(null),
    TXT_OR_ROBOT("robot"),
    TSV("tsv");

    public static FileFormat getByFile(final File file) {
        return getByExtension(getExtension(file));
    }

    private static String getExtension(final File processedFile) {
        return Optional.ofNullable(processedFile)
                .map(File::getName)
                .filter(name -> name.contains("."))
                .map(name -> name.substring(name.lastIndexOf('.') + 1))
                .map(String::toLowerCase)
                .orElse("");
    }

    private static FileFormat getByExtension(final String fileExtension) {
        switch (fileExtension) {
            case "txt":
            case "robot":
            case "resource":
                return TXT_OR_ROBOT;
            case "tsv":
                return TSV;
            default:
                return FileFormat.UNKNOWN;
        }
    }

    private final String extension;

    private FileFormat(final String extension) {
        this.extension = extension;
    }

    public String getExtension() {
        return this.extension;
    }
}