/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.validation;

import java.util.Objects;

import com.google.common.base.Optional;
import com.google.common.collect.Range;

public final class ProblemPosition {

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

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof ProblemPosition) {
            final ProblemPosition that = (ProblemPosition) obj;
            return this.line == that.line && Objects.equals(this.range, that.range);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(line, range);
    }
}
