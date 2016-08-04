package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring;

import static com.google.common.collect.Iterables.transform;
import static org.assertj.core.api.Assertions.assertThat;
import static org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring.TokensSource.line;
import static org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring.TokensSource.lines;
import static org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring.TokensSource.toTokenContents;

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
                line(1, new LineElement(1, 0, 12, "dddd"), new LineElement(1, 4, 16, "eeee"), new LineElement(1, 8, 20, "ffff")));

        final Deque<IRobotLineElement> queue = builder.buildQueue(28, 8, lines, 2);
        assertThat(queue).isEmpty();
    }
    
    @Test
    public void thereAreNoTokensQueued_whenAllOfThemLiesAfterTheRegionOfInterest() {
        final List<RobotLine> lines = lines(
                line(1, new LineElement(1, 0, 12, "dddd"), new LineElement(1, 4, 16, "eeee"), new LineElement(1, 8, 20, "ffff")),
                line(2, new LineElement(2, 0, 24, "gggg"), new LineElement(2, 4, 28, "hhhh"), new LineElement(2, 8, 32, "iiii")));

        final Deque<IRobotLineElement> queue = builder.buildQueue(4, 8, lines, 0);
        assertThat(queue).isEmpty();
    }

    @Test
    public void allTokensAreQueued_whenAllOfThemLiesInsideTheRegionOfInterest_1() {
        final List<RobotLine> lines = lines(
                line(0, new LineElement(0, 0, 0, "aaaa"), new LineElement(0, 4, 4, "bbbb"), new LineElement(0, 8, 8, "cccc")),
                line(1, new LineElement(1, 0, 12, "dddd"), new LineElement(1, 4, 16, "eeee"), new LineElement(1, 8, 20, "ffff")),
                line(2, new LineElement(2, 0, 24, "gggg"), new LineElement(2, 4, 28, "hhhh"), new LineElement(2, 8, 32, "iiii")));

        final Deque<IRobotLineElement> queue = builder.buildQueue(0, 100, lines, 0);
        assertThat(transform(queue, toTokenContents())).containsExactly("aaaa", "bbbb", "cccc", "dddd", "eeee", "ffff",
                "gggg", "hhhh", "iiii");
    }

    @Test
    public void allTokensAreQueued_whenAllOfThemLiesInsideTheRegionOfInterest_2() {
        final List<RobotLine> lines = lines(
                line(0, new LineElement(0, 0, 0, "aaaa"), new LineElement(0, 4, 4, "bbbb"), new LineElement(0, 8, 8, "cccc")),
                line(1, new LineElement(1, 0, 12, "dddd"), new LineElement(1, 4, 16, "eeee"), new LineElement(1, 8, 20, "ffff")),
                line(2, new LineElement(2, 0, 24, "gggg"), new LineElement(2, 4, 28, "hhhh"), new LineElement(2, 8, 32, "iiii")));

        final Deque<IRobotLineElement> queue = builder.buildQueue(8, 24, lines, 0);
        assertThat(transform(queue, toTokenContents())).containsExactly("cccc", "dddd", "eeee", "ffff", "gggg", "hhhh");
    }

    @Test
    public void firstTokensAreQueued_whenLatterLiesOutsideTheRegionOfInterst() {
        final List<RobotLine> lines = lines(
                line(0, new LineElement(0, 0, 0, "aaaa"), new LineElement(0, 4, 4, "bbbb"), new LineElement(0, 8, 8, "cccc")),
                line(1, new LineElement(1, 0, 12, "dddd"), new LineElement(1, 4, 16, "eeee"), new LineElement(1, 8, 20, "ffff")),
                line(2, new LineElement(2, 0, 24, "gggg"), new LineElement(2, 4, 28, "hhhh"), new LineElement(2, 8, 32, "iiii")));

        final Deque<IRobotLineElement> queue = builder.buildQueue(0, 24, lines, 0);
        assertThat(transform(queue, toTokenContents())).containsExactly("aaaa", "bbbb", "cccc", "dddd", "eeee", "ffff");
    }

    @Test
    public void latterTokensAreQueued_whenLatterFirstOutsideTheRegionOfInterst() {
        final List<RobotLine> lines = lines(
                line(0, new LineElement(0, 0, 0, "aaaa"), new LineElement(0, 4, 4, "bbbb"), new LineElement(0, 8, 8, "cccc")),
                line(1, new LineElement(1, 0, 12, "dddd"), new LineElement(1, 4, 16, "eeee"), new LineElement(1, 8, 20, "ffff")),
                line(2, new LineElement(2, 0, 24, "gggg"), new LineElement(2, 4, 28, "hhhh"), new LineElement(2, 8, 32, "iiii")));

        final Deque<IRobotLineElement> queue = builder.buildQueue(12, 24, lines, 0);
        assertThat(transform(queue, toTokenContents())).containsExactly("dddd", "eeee", "ffff", "gggg", "hhhh", "iiii");
    }

    @Test
    public void theTokenIsAlsoQueued_whenItStartsBeforeRegionOfInterstButEndsWithinIt() {
        final List<RobotLine> lines = lines(
                line(0, new LineElement(0, 0, 0, "aaaa"), new LineElement(0, 4, 4, "bbbb"), new LineElement(0, 8, 8, "cccc")));

        final Deque<IRobotLineElement> queue = builder.buildQueue(6, 6, lines, 0);
        assertThat(transform(queue, toTokenContents())).containsExactly("bbbb", "cccc");
    }

    @Test
    public void theTokenIsAlsoQueued_whenItEndsAfterRegionOfInterstButBeginsWithinIt() {
        final List<RobotLine> lines = lines(
                line(0, new LineElement(0, 0, 0, "aaaa"), new LineElement(0, 4, 4, "bbbb"), new LineElement(0, 8, 8, "cccc")));

        final Deque<IRobotLineElement> queue = builder.buildQueue(0, 6, lines, 0);
        assertThat(transform(queue, toTokenContents())).containsExactly("aaaa", "bbbb");
    }
}
