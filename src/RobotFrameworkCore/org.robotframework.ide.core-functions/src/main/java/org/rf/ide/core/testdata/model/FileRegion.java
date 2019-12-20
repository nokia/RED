/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.rf.ide.core.testdata.model.table.RobotTokenPositionComparator;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;

import com.google.common.collect.Range;

public class FileRegion {

    private FilePosition start;

    private FilePosition end;

    public FileRegion(final FilePosition start, final FilePosition end) {
        this.start = start;
        this.end = end;
    }

    public FilePosition getStart() {
        return start;
    }

    public void setStart(final FilePosition start) {
        this.start = start;
    }

    public FilePosition getEnd() {
        return end;
    }

    public void setEnd(final FilePosition end) {
        this.end = end;
    }

    public boolean isInside(final int offset) {
        return Range.closed(start.getOffset(), end.getOffset()).contains(offset);
    }

    public boolean containsLine(final int line) {
        if (line > FilePosition.NOT_SET) {
            return Range.closed(start.getLine(), end.getLine()).contains(line);
        }

        return false;
    }

    @Override
    public String toString() {
        return String.format("FileRegion [start=%s, end=%s]", start, end);
    }

    @Override
    public int hashCode() {
        return Objects.hash(start, end);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj != null && obj.getClass() == FileRegion.class) {
            final FileRegion that = (FileRegion) obj;
            return Objects.equals(this.start, that.start) && Objects.equals(this.end, that.end);
        }
        return false;
    }

    public static class FileRegionSplitter {

        public List<FileRegion> splitContinuousRegions(final List<RobotToken> tokens) {
            Collections.sort(tokens, new RobotTokenPositionComparator());
            final List<FileRegion> regions = new ArrayList<>(0);

            RobotToken lastTokenWithPosition = null;
            int lastLine = -1;
            for (final RobotToken token : tokens) {
                if (token.getFilePosition().getOffset() == FilePosition.NOT_SET) {
                    break;
                } else {
                    if (lastLine == -1) {
                        regions.add(new FileRegion(token.getFilePosition(), null));
                    } else if (lastLine == token.getLineNumber() || lastLine + 1 == token.getLineNumber()) {
                        regions.get(regions.size() - 1).setEnd(
                                new FilePosition(token.getLineNumber(), token.getEndColumn(), token.getEndOffset()));
                    } else {
                        regions.get(regions.size() - 1).setEnd(new FilePosition(lastTokenWithPosition.getLineNumber(),
                                lastTokenWithPosition.getEndColumn(), lastTokenWithPosition.getEndOffset()));
                        regions.add(new FileRegion(token.getFilePosition(), null));
                    }

                    lastTokenWithPosition = token;
                    lastLine = token.getLineNumber();
                }
            }

            if (regions.isEmpty()) {
                regions.add(new FileRegion(FilePosition.createNotSet(), FilePosition.createNotSet()));
            } else {
                final FileRegion lastRegion = regions.get(regions.size() - 1);
                if (lastRegion.getEnd() == null) {
                    lastRegion.setEnd(new FilePosition(lastTokenWithPosition.getLineNumber(),
                            lastTokenWithPosition.getEndColumn(), lastTokenWithPosition.getEndOffset()));
                }
            }

            return regions;
        }
    }
}
