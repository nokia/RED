/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model;

public class FileRegion {

    private FilePosition start;

    private FilePosition end;

    public FileRegion(FilePosition start, FilePosition end) {
        this.start = start;
        this.end = end;
    }

    public FilePosition getStart() {
        return start;
    }

    public void setStart(FilePosition start) {
        this.start = start;
    }

    public FilePosition getEnd() {
        return end;
    }

    public void setEnd(FilePosition end) {
        this.end = end;
    }

    public boolean isInside(final int offset) {
        boolean result = false;

        int startOffset = start.getOffset();
        int endOffset = end.getOffset();
        result = (offset >= startOffset && offset <= endOffset);

        return result;
    }

    @Override
    public String toString() {
        return String.format("FileRegion [start=%s, end=%s]", start, end);
    }

}
