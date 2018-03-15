/*
* Copyright 2016 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring;

import static com.google.common.collect.Iterables.transform;
import static org.assertj.core.api.Assertions.assertThat;
import static org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring.TokensSource.line;
import static org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring.TokensSource.lines;

import java.util.Deque;
import java.util.List;

import org.junit.Test;
import org.rf.ide.core.testdata.text.read.IRobotLineElement;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring.TokensSource.LineElement;

public class RedTokensQueueBuilderTest {

    private final RedTokensQueueBuilder builder = new RedTokensQueueBuilder();

    @Test
    public void thereAreNoTokensQueued_whenAllOfThemLiesBeforeTheRegionOfInterest() {
        final List<RobotLine> lines = lines(
                line(0, new LineElement(0, 0, 0, "aaaa"), new LineElement(0, 4, 4, "bbbb"), new LineElement(0, 8, 8, "cccc")),
                line(1, new LineElement(1, 0, 13, "dddd"), new LineElement(1, 4, 17, "eeee"), new LineElement(1, 8, 21, "ffff")));

        final Deque<IRobotLineElement> queue = builder.buildQueue(28, 8, lines, 2);
        assertThat(queue).isEmpty();
    }

    @Test
    public void thereAreNoTokensQueued_whenAllOfThemLiesAfterTheRegionOfInterest() {
        final List<RobotLine> lines = lines(
                line(1, new LineElement(1, 0, 12, "dddd"), new LineElement(1, 4, 16, "eeee"), new LineElement(1, 8, 20, "ffff")),
                line(2, new LineElement(2, 0, 25, "gggg"), new LineElement(2, 4, 29, "hhhh"), new LineElement(2, 8, 33, "iiii")));

        final Deque<IRobotLineElement> queue = builder.buildQueue(4, 8, lines, 0);
        assertThat(queue).isEmpty();
    }

    @Test
    public void allTokensAreQueued_whenAllOfThemLiesInsideTheRegionOfInterest_1() {
        final List<RobotLine> lines = lines(
                line(0, new LineElement(0, 0, 0, "aaaa"), new LineElement(0, 4, 4, "bbbb"), new LineElement(0, 8, 8, "cccc")),
                line(1, new LineElement(1, 0, 13, "dddd"), new LineElement(1, 4, 17, "eeee"), new LineElement(1, 8, 21, "ffff")),
                line(2, new LineElement(2, 0, 26, "gggg"), new LineElement(2, 4, 30, "hhhh"), new LineElement(2, 8, 34, "iiii")));

        final Deque<IRobotLineElement> queue = builder.buildQueue(0, 100, lines, 0);
        assertThat(transform(queue, IRobotLineElement::getText)).containsExactly("aaaa", "bbbb", "cccc", "\n", "dddd",
                "eeee", "ffff", "\n", "gggg", "hhhh", "iiii", "\n");
    }

    @Test
    public void allTokensAreQueued_whenAllOfThemLiesInsideTheRegionOfInterest_2() {
        final List<RobotLine> lines = lines(
                line(0, new LineElement(0, 0, 0, "aaaa"), new LineElement(0, 4, 4, "bbbb"), new LineElement(0, 8, 8, "cccc")),
                line(1, new LineElement(1, 0, 13, "dddd"), new LineElement(1, 4, 17, "eeee"), new LineElement(1, 8, 21, "ffff")),
                line(2, new LineElement(2, 0, 26, "gggg"), new LineElement(2, 4, 30, "hhhh"), new LineElement(2, 8, 34, "iiii")));

        final Deque<IRobotLineElement> queue = builder.buildQueue(8, 26, lines, 0);
        assertThat(transform(queue, IRobotLineElement::getText)).containsExactly("cccc", "\n", "dddd", "eeee", "ffff",
                "\n", "gggg", "hhhh");
    }

    @Test
    public void firstTokensAreQueued_whenLatterLiesOutsideTheRegionOfInterest() {
        final List<RobotLine> lines = lines(
                line(0, new LineElement(0, 0, 0, "aaaa"), new LineElement(0, 4, 4, "bbbb"), new LineElement(0, 8, 8, "cccc")),
                line(1, new LineElement(1, 0, 13, "dddd"), new LineElement(1, 4, 17, "eeee"), new LineElement(1, 8, 21, "ffff")),
                line(2, new LineElement(2, 0, 26, "gggg"), new LineElement(2, 4, 30, "hhhh"), new LineElement(2, 8, 34, "iiii")));

        final Deque<IRobotLineElement> queue = builder.buildQueue(0, 25, lines, 0);
        assertThat(transform(queue, IRobotLineElement::getText)).containsExactly("aaaa", "bbbb", "cccc", "\n", "dddd",
                "eeee", "ffff");
    }

    @Test
    public void latterTokensAreQueued_whenLatterFirstOutsideTheRegionOfInterest() {
        final List<RobotLine> lines = lines(
                line(0, new LineElement(0, 0, 0, "aaaa"), new LineElement(0, 4, 4, "bbbb"), new LineElement(0, 8, 8, "cccc")),
                line(1, new LineElement(1, 0, 13, "dddd"), new LineElement(1, 4, 17, "eeee"), new LineElement(1, 8, 21, "ffff")),
                line(2, new LineElement(2, 0, 26, "gggg"), new LineElement(2, 4, 30, "hhhh"), new LineElement(2, 8, 34, "iiii")));

        final Deque<IRobotLineElement> queue = builder.buildQueue(13, 25, lines, 0);
        assertThat(transform(queue, IRobotLineElement::getText)).containsExactly("dddd", "eeee", "ffff", "\n", "gggg",
                "hhhh", "iiii");
    }

    @Test
    public void theTokenIsAlsoQueued_whenItStartsBeforeRegionOfInterestButEndsWithinIt() {
        final List<RobotLine> lines = lines(
                line(0, new LineElement(0, 0, 0, "aaaa"), new LineElement(0, 4, 4, "bbbb"), new LineElement(0, 8, 8, "cccc")));

        final Deque<IRobotLineElement> queue = builder.buildQueue(6, 6, lines, 0);
        assertThat(transform(queue, IRobotLineElement::getText)).containsExactly("bbbb", "cccc");
    }

    @Test
    public void theTokenIsAlsoQueued_whenItEndsAfterRegionOfInterestButBeginsWithinIt() {
        final List<RobotLine> lines = lines(
                line(0, new LineElement(0, 0, 0, "aaaa"), new LineElement(0, 4, 4, "bbbb"), new LineElement(0, 8, 8, "cccc")));

        final Deque<IRobotLineElement> queue = builder.buildQueue(0, 6, lines, 0);
        assertThat(transform(queue, IRobotLineElement::getText)).containsExactly("aaaa", "bbbb");
    }
}
