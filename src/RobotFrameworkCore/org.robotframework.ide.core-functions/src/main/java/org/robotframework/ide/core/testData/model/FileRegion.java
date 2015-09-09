/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see licence.txt file for details.
 */
package org.robotframework.ide.core.testData.model;

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


    @Override
    public String toString() {
        return String.format("FileRegion [start=%s, end=%s]", start, end);
    }

}
