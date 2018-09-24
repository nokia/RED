/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model;

public enum FileFormat {
    UNKNOWN(null),
    TXT_OR_ROBOT("robot"),
    TSV("tsv");

    private final String extension;

    private FileFormat(final String extension) {
        this.extension = extension;
    }

    public static FileFormat getByExtension(final String fileExtension) {
        if (fileExtension != null) {
            final String fileExt = fileExtension.toLowerCase();
            if ("txt".equals(fileExt) || "robot".equals(fileExt)) {
                return TXT_OR_ROBOT;
            } else if ("tsv".equals(fileExt)) {
                return TSV;
            }
        }
        return FileFormat.UNKNOWN;
    }

    public String getExtension() {
        return this.extension;
    }
}