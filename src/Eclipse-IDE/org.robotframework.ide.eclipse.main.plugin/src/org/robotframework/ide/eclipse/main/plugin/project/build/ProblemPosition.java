/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see licence.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build;

import com.google.common.base.Optional;
import com.google.common.collect.Range;

public class ProblemPosition {

    private final int line;

    private final Optional<Range<Integer>> range;

    public ProblemPosition(final int line) {
        this(line, null);
    }

    public ProblemPosition(final int line, final Range<Integer> range) {
        this.line = line;
        this.range = Optional.fromNullable(range);
    }

    public int getLine() {
        return line;
    }

    public Optional<Range<Integer>> getRange() {
        return range;
    }

    @Override
    public String toString() {
        return "Line " + line + ", offset: [" + getOffset(range) + "]";
    }

    private String getOffset(final Optional<Range<Integer>> range) {
        if (range.isPresent()) {
            return range.get().lowerEndpoint() + ", " + range.get().upperEndpoint();
        }
        return "empty";
    }
}
