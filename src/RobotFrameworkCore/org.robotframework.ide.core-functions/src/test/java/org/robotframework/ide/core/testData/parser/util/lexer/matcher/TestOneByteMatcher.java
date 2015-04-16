package org.robotframework.ide.core.testData.parser.util.lexer.matcher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.atIndex;

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


    @Test
    public void test_doNotHaveEnoughDataToRead__shouldReturn__messageAnd__MatchResultNOT_FOUND() {
        // prepare
        byte[] data = new byte[] { (byte) expected };
        int start = 0;
        int end = 4;
        Position position = new Position(start, end);
        DataMarked dataMarked = new DataMarked(data, position);

        // execute
        MatchResult result = matcher.match(dataMarked);

        // verify
        assertThat(result).isNotNull();
        assertThat(result.getMatcher()).isEqualTo(matcher);
        assertThat(result.getMessages()).hasSize(1)
                .contains(
                        "Do not have enough data to read, expected end position "
                                + end + " but we have " + data.length
                                + " bytes.", atIndex(0));
        assertThat(result.getParent()).isNull();
        assertThat(result.getPosition()).isEqualTo(position);
        assertThat(result.getStatus()).isEqualTo(MatchStatus.NOT_FOUND);
        assertThat(result.getSubResults()).isEmpty();
    }


    @Test
    public void test_endPositionIsLessThanZero__shouldReturn__messageAnd__MatchResultNOT_FOUND() {
        // prepare
        byte[] data = new byte[] { (byte) expected };
        int start = 0;
        int end = -1;
        Position position = new Position(start, end);
        DataMarked dataMarked = new DataMarked(data, position);

        // execute
        MatchResult result = matcher.match(dataMarked);

        // verify
        assertThat(result).isNotNull();
        assertThat(result.getMatcher()).isEqualTo(matcher);
        assertThat(result.getMessages()).hasSize(1).contains(
                "End position " + end + " is below zero.", atIndex(0));
        assertThat(result.getParent()).isNull();
        assertThat(result.getPosition()).isEqualTo(position);
        assertThat(result.getStatus()).isEqualTo(MatchStatus.NOT_FOUND);
        assertThat(result.getSubResults()).isEmpty();
    }


    @Test
    public void test_startPositionIsLessThanZero__shouldReturn__messageAnd__MatchResultNOT_FOUND() {
        // prepare
        byte[] data = new byte[] { (byte) expected };
        int start = -1;
        int end = 0;
        Position position = new Position(start, end);
        DataMarked dataMarked = new DataMarked(data, position);

        // execute
        MatchResult result = matcher.match(dataMarked);

        // verify
        assertThat(result).isNotNull();
        assertThat(result.getMatcher()).isEqualTo(matcher);
        assertThat(result.getMessages()).hasSize(1).contains(
                "Start position " + start + " is below zero.", atIndex(0));
        assertThat(result.getParent()).isNull();
        assertThat(result.getPosition()).isEqualTo(position);
        assertThat(result.getStatus()).isEqualTo(MatchStatus.NOT_FOUND);
        assertThat(result.getSubResults()).isEmpty();
    }


    @Test
    public void test_startPositionIsGreaterThanEndPos__shouldReturn__messageAnd__MatchResultNOT_FOUND() {
        // prepare
        byte[] data = new byte[] { (byte) expected };
        int start = 1;
        int end = 0;
        Position position = new Position(start, end);
        DataMarked dataMarked = new DataMarked(data, position);

        // execute
        MatchResult result = matcher.match(dataMarked);

        // verify
        assertThat(result).isNotNull();
        assertThat(result.getMatcher()).isEqualTo(matcher);
        assertThat(result.getMessages()).hasSize(1).contains(
                "Start position " + start + " is greater than end position "
                        + end, atIndex(0));
        assertThat(result.getParent()).isNull();
        assertThat(result.getPosition()).isEqualTo(position);
        assertThat(result.getStatus()).isEqualTo(MatchStatus.NOT_FOUND);
        assertThat(result.getSubResults()).isEmpty();
    }


    @Test
    public void test_emptyDataTable__shouldReturn__messageAnd__MatchResultNOT_FOUND() {
        // prepare
        byte[] data = new byte[0];
        int start = 0;
        int end = 0;
        Position position = new Position(start, end);
        DataMarked dataMarked = new DataMarked(data, position);

        // execute
        MatchResult result = matcher.match(dataMarked);

        // verify
        assertThat(result).isNotNull();
        assertThat(result.getMatcher()).isEqualTo(matcher);
        assertThat(result.getMessages()).hasSize(1).contains(
                "No data available for matching byte [" + expected + "]",
                atIndex(0));
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
