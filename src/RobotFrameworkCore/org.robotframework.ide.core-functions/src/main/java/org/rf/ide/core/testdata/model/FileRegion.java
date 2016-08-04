/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model;

import com.google.common.collect.Range;

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
        return Range.closed(start.getOffset(), end.getOffset()).contains(offset);
    }

    public boolean isBetweenLines(final int line) {
        if (line > FilePosition.NOT_SET) {
            return Range.closed(start.getLine(), end.getLine()).contains(line);
        }

        return false;
    }

    @Override
    public String toString() {
        return String.format("FileRegion [start=%s, end=%s]", start, end);
    }

}
