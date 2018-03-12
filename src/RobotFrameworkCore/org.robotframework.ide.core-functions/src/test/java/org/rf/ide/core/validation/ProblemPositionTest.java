/*
* Copyright 2017 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.rf.ide.core.validation;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Objects;
import java.util.Optional;

import org.junit.Test;
import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.model.FileRegion;

import com.google.common.collect.Range;

public class ProblemPositionTest {

    @Test
    public void problemPositionIsCorrectlyConstructedFromFileRegion() {
        final FileRegion region = new FileRegion(new FilePosition(5, 3, 70), new FilePosition(5, 8, 75));
        final ProblemPosition position = ProblemPosition.fromRegion(region);

        assertThat(position.getLine()).isEqualTo(5);
        assertThat(position.getRange()).contains(Range.closed(70, 75));
    }

    @Test
    public void positionGettersTest() {
        assertThat(new ProblemPosition(42).getLine()).isEqualTo(42);
        assertThat(new ProblemPosition(42, Range.closed(100, 200)).getLine()).isEqualTo(42);

        assertThat(new ProblemPosition(42).getRange()).isEmpty();
        assertThat(new ProblemPosition(42, Range.closed(100, 200)).getRange()).contains(Range.closed(100, 200));
    }

    @Test
    public void stringRepresentationTest() {
        assertThat(new ProblemPosition(42).toString()).isEqualTo("Line 42, offset: [empty]");
        assertThat(new ProblemPosition(42, Range.closed(100, 200)).toString()).isEqualTo("Line 42, offset: [100, 200]");
    }

    @Test
    public void equalityTests() {
        assertThat(new ProblemPosition(5).equals(new ProblemPosition(5))).isTrue();
        assertThat(
                new ProblemPosition(5, Range.closed(100, 200)).equals(new ProblemPosition(5, Range.closed(100, 200))))
                        .isTrue();

        assertThat(new ProblemPosition(5).equals(new Object())).isFalse();
        assertThat(new ProblemPosition(5).equals(new ProblemPosition(6))).isFalse();
        assertThat(new ProblemPosition(5).equals(new ProblemPosition(5, Range.closed(100, 200)))).isFalse();
        assertThat(
                new ProblemPosition(5, Range.closed(100, 200)).equals(new ProblemPosition(5, Range.closed(101, 200))))
                        .isFalse();
        assertThat(
                new ProblemPosition(5, Range.closed(100, 200)).equals(new ProblemPosition(5, Range.closed(100, 201))))
                        .isFalse();
    }

    @Test
    public void hashCodeIsConstructedFromBothLineAndOffsets() {
        assertThat(new ProblemPosition(5).hashCode()).isEqualTo(Objects.hash(5, Optional.empty()));
        assertThat(new ProblemPosition(5, Range.closed(100, 200)).hashCode())
                .isEqualTo(Objects.hash(5, Range.closed(100, 200)));

    }

}
