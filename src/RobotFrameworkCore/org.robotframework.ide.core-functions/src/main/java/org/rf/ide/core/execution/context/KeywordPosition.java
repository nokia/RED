/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.execution.context;

public class KeywordPosition {

    private final int lineNumber;

    private final String filePath;

    public KeywordPosition(final String filePath, final int lineNumber) {
        this.filePath = filePath;
        this.lineNumber = lineNumber;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public String getFilePath() {
        return filePath;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((filePath == null) ? 0 : filePath.hashCode());
        result = prime * result + lineNumber;
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final KeywordPosition other = (KeywordPosition) obj;
        if (filePath == null) {
            if (other.filePath != null) {
                return false;
            }
        } else if (!filePath.equals(other.filePath)) {
            return false;
        }
        if (lineNumber != other.lineNumber) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return String.format("KeywordPosition [lineNumber=%s, filePath=%s]", lineNumber, filePath);
    }

}
