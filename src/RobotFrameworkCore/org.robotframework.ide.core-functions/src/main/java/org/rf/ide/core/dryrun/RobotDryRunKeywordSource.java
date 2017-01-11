/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.dryrun;

/**
 * @author bembenek
 */
public class RobotDryRunKeywordSource {

    private final String name;

    private final String libraryName;

    private final String filePath;

    private final int lineNumber;

    RobotDryRunKeywordSource(final String name, final String libraryName, final String filePath, final int lineNumber) {
        this.name = name;
        this.libraryName = libraryName;
        this.filePath = filePath;
        this.lineNumber = lineNumber;
    }

    public String getName() {
        return name;
    }

    public String getLibraryName() {
        return libraryName;
    }

    public String getFilePath() {
        return filePath;
    }

    public int getLineNumber() {
        return lineNumber;
    }

}
