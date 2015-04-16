package org.robotframework.ide.core.testData.parser.util.lexer.matcher;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.robotframework.ide.core.testData.parser.util.lexer.DataMarked;
import org.robotframework.ide.core.testData.parser.util.lexer.IMatcher;
import org.robotframework.ide.core.testData.parser.util.lexer.MatchResult;
import org.robotframework.ide.core.testData.parser.util.lexer.MatchResult.MatchStatus;
import org.robotframework.ide.core.testData.parser.util.lexer.Position;


/**
 * 
 * @author wypych
 * @see OneByteMatcher
 */
public class TestOneByteMatcher {

    private char expected = 'a';
    private IMatcher matcher = null;


    @Test
    public void test_charactersAreTheSame__shouldReturn__MatchResult_FOUND() {
        // prepare
        byte[] data = new byte[] { (byte) expected };
        int start = 0;
        int end = 0;
        Position position = new Position(start, end);
        DataMarked dataMarked = new DataMarked(data, position);

        // execute
        MatchResult result = matcher.match(dataMarked);

        // verify
        assertThat(result).isNotNull();
        assertThat(result.getMatcher()).isEqualTo(matcher);
        assertThat(result.getMessages()).isEmpty();
        assertThat(result.getParent()).isNull();
        assertThat(result.getPosition()).isEqualTo(position);
        assertThat(result.getStatus()).isEqualTo(MatchStatus.FOUND);
        assertThat(result.getSubResults()).isEmpty();
    }


    @Test
    public void test_charactersAreDifferent__shouldReturn__MatchResultNOT_FOUND() {
        // prepare
        byte[] data = new byte[] { (byte) (expected + 1) };
        int start = 0;
        int end = 0;
        Position position = new Position(start, end);
        DataMarked dataMarked = new DataMarked(data, position);

        // execute
        MatchResult result = matcher.match(dataMarked);

        // verify
        assertThat(result).isNotNull();
        assertThat(result.getMatcher()).isEqualTo(matcher);
        assertThat(result.getMessages()).isEmpty();
        assertThat(result.getParent()).isNull();
        assertThat(result.getPosition()).isEqualTo(position);
        assertThat(result.getStatus()).isEqualTo(MatchStatus.NOT_FOUND);
        assertThat(result.getSubResults()).isEmpty();
    }


    @Before
    public void setUp() {
        matcher = new OneByteMatcher((byte) expected);
    }


    @After
    public void tearDown() {
        matcher = null;
    }
}
