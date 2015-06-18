package org.robotframework.ide.core.testData.text.lexer.matcher;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.CharBuffer;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.robotframework.ide.core.testData.text.lexer.LinearPositionMarker;
import org.robotframework.ide.core.testData.text.lexer.RobotToken;
import org.robotframework.ide.core.testData.text.lexer.RobotTokenType;
import org.robotframework.ide.core.testData.text.lexer.RobotType;
import org.robotframework.ide.core.testData.text.lexer.matcher.RobotTokenMatcher.TokenOutput;
import org.robotframework.ide.core.testHelpers.CircullarArrayIterator;
import org.robotframework.ide.core.testHelpers.ClassFieldCleaner;
import org.robotframework.ide.core.testHelpers.ClassFieldCleaner.ForClean;

import com.google.common.collect.LinkedListMultimap;


/**
 * 
 * @author wypych
 * @since JDK 1.7 update 74
 * @version Robot Framework 2.9 alpha 2
 * 
 * @see EndOfLineMatcher
 */
public class EndOfLineMatcherTest {

    @ForClean
    private ISingleCharTokenMatcher matcher;


    @Test
    public void test_match_forEmptyTokensStore_and_LineFeedFollowingByCarriageReturn() {
        // prepare
        CharBuffer tempBuffer = CharBuffer.wrap("\n\r");
        TokenOutput output = new TokenOutput();

        // execute & verify
        for (int charIndex = 0; charIndex < tempBuffer.length(); charIndex++) {
            assertThat(matcher.match(output, tempBuffer, charIndex)).isTrue();
            // plus one because we have also End-Of-Line token
            assertThat(output.getTokens()).hasSize(charIndex + 2);
            RobotTokenType[] types;
            if ((charIndex + 1) % 2 == 0) {
                types = new RobotTokenType[] { RobotTokenType.LINE_FEED,
                        RobotTokenType.CARRIAGE_RETURN,
                        RobotTokenType.END_OF_LINE };
            } else {
                types = new RobotTokenType[] { RobotTokenType.LINE_FEED,
                        RobotTokenType.END_OF_LINE };
            }
            assertTokens(output, types, 0, 1);
            assertCurrentPosition(output);
            assertPositionMarkers(output);
        }
    }


    @Test
    public void test_match_forEmptyTokensStore_and_CarriageReturnFollowingByLineFeed() {
        // prepare
        CharBuffer tempBuffer = CharBuffer.wrap("\r\n");
        TokenOutput output = new TokenOutput();

        // execute & verify
        for (int charIndex = 0; charIndex < tempBuffer.length(); charIndex++) {
            assertThat(matcher.match(output, tempBuffer, charIndex)).isTrue();
            // plus one because we have also End-Of-Line token
            assertThat(output.getTokens()).hasSize(charIndex + 2);
            RobotTokenType[] types;
            if ((charIndex + 1) % 2 == 0) {
                types = new RobotTokenType[] { RobotTokenType.CARRIAGE_RETURN,
                        RobotTokenType.LINE_FEED, RobotTokenType.END_OF_LINE };
            } else {
                types = new RobotTokenType[] { RobotTokenType.CARRIAGE_RETURN,
                        RobotTokenType.END_OF_LINE };
            }
            assertTokens(output, types, 0, 1);
            assertCurrentPosition(output);
            assertPositionMarkers(output);
        }
    }


    @Test
    public void test_match_forEmptyTokensStore_and_onlyFiveLineFeeds() {
        // prepare
        CharBuffer tempBuffer = CharBuffer.wrap("\n\n\n\n\n");
        TokenOutput output = new TokenOutput();
        RobotTokenType[] types = new RobotTokenType[] {
                RobotTokenType.LINE_FEED, RobotTokenType.END_OF_LINE };

        // execute & verify
        for (int charIndex = 0; charIndex < tempBuffer.length(); charIndex++) {
            assertThat(matcher.match(output, tempBuffer, charIndex)).isTrue();
            // plus one because we have also End-Of-Line token
            assertThat(output.getTokens()).hasSize((charIndex + 1) * 2);
            assertTokens(output, types, 0, 1);
            assertCurrentPosition(output);
            assertPositionMarkers(output);
        }
    }


    @Test
    public void test_match_forEmptyTokensStore_and_onlyFiveCarriageReturns() {
        // prepare
        CharBuffer tempBuffer = CharBuffer.wrap("\r\r\r\r\r");
        TokenOutput output = new TokenOutput();
        RobotTokenType[] types = new RobotTokenType[] {
                RobotTokenType.CARRIAGE_RETURN, RobotTokenType.END_OF_LINE };

        // execute & verify
        for (int charIndex = 0; charIndex < tempBuffer.length(); charIndex++) {
            assertThat(matcher.match(output, tempBuffer, charIndex)).isTrue();
            // plus one because we have also End-Of-Line token
            assertThat(output.getTokens()).hasSize((charIndex + 1) * 2);
            assertTokens(output, types, 0, 1);
            assertCurrentPosition(output);
            assertPositionMarkers(output);
        }
    }


    private void assertPositionMarkers(TokenOutput output) {
        LinkedListMultimap<RobotType, Integer> tokensPosition = output
                .getTokensPosition();
        assertThat(tokensPosition).isNotNull();
        List<RobotToken> tokens = output.getTokens();
        for (int i = 0; i < tokens.size(); i++) {
            RobotToken cToken = tokens.get(i);
            assertThat(tokensPosition.get(cToken.getType())).contains(i);
        }
    }


    private void assertCurrentPosition(TokenOutput output) {
        LinearPositionMarker currentMarker = output.getCurrentMarker();
        assertThat(currentMarker).isNotNull();
        LinkedListMultimap<RobotType, Integer> tokensPosition = output
                .getTokensPosition();
        assertThat(tokensPosition).isNotNull();
        List<Integer> crTokens = tokensPosition
                .get(RobotTokenType.CARRIAGE_RETURN);
        List<Integer> lfTokens = tokensPosition.get(RobotTokenType.LINE_FEED);
        int line = Math.max(crTokens.size(), lfTokens.size());
        assertThat(currentMarker.getLine()).isEqualTo(line + 1);
        assertThat(currentMarker.getColumn()).isEqualTo(
                ((crTokens.size() + lfTokens.size()) % line) + 1);
    }


    private void assertTokens(TokenOutput out, RobotTokenType[] types,
            int startTokenPos, int startLine) {
        List<RobotToken> tokens = out.getTokens();
        int typesLength = types.length;
        CircullarArrayIterator<RobotTokenType> iter = new CircullarArrayIterator<>(
                types);

        assertThat(tokens).isNotNull();
        assertThat(tokens).isNotEmpty();
        assertThat((tokens.size() - startTokenPos) % typesLength).isEqualTo(0);
        int line = startLine;
        int column = LinearPositionMarker.THE_FIRST_COLUMN;
        for (int tokId = startTokenPos; tokId < tokens.size(); tokId++) {
            RobotToken robotToken = tokens.get(tokId);
            assertThat(robotToken).isNotNull();
            RobotType type = iter.next();
            assertThat(robotToken.getType()).isEqualTo(type);

            if (type == RobotTokenType.END_OF_LINE) {
                assertThat(robotToken.getText()).isNull();
                assertStartPosition(robotToken, line, column);
                assertEndPosition(robotToken, line, column);
                line++;
                column = LinearPositionMarker.THE_FIRST_COLUMN;
            } else {
                assertThat(robotToken.getText().toString()).isEqualTo(
                        type.toWrite());
                assertStartPosition(robotToken, line, column);
                column++;
                assertEndPosition(robotToken, line, column);
            }
        }
    }


    private void assertStartPosition(RobotToken token, int line, int column) {
        LinearPositionMarker startPosition = token.getStartPosition();
        assertThat(startPosition).isNotNull();
        assertThat(startPosition.getLine()).isEqualTo(line);
        assertThat(startPosition.getColumn()).isEqualTo(column);
    }


    private void assertEndPosition(RobotToken token, int line, int column) {
        LinearPositionMarker endPosition = token.getEndPosition();
        assertThat(endPosition).isNotNull();
        assertThat(endPosition.getLine()).isEqualTo(line);
        assertThat(endPosition.getColumn()).isEqualTo(column);
    }


    @Test
    public void test_match_forNotEndOfLineChars_shouldAlwaysReturn_FALSE() {
        // prepare
        CharBuffer tempBuffer = CharBuffer.wrap("abcdefgh");
        TokenOutput output = new TokenOutput();

        // execute & verify
        for (int charIndex = 0; charIndex < tempBuffer.length(); charIndex++) {
            assertThat(matcher.match(output, tempBuffer, charIndex)).isFalse();
            assertThat(output.getTokens()).isEmpty();
            assertThat(output.getTokensPosition().asMap()).isEmpty();
            assertCurrentMarkerPosition(output,
                    LinearPositionMarker.THE_FIRST_LINE,
                    LinearPositionMarker.THE_FIRST_COLUMN);
        }
    }


    private void assertCurrentMarkerPosition(TokenOutput out, int line,
            int column) {
        LinearPositionMarker currentMarker = out.getCurrentMarker();
        assertThat(currentMarker).isNotNull();
        assertThat(currentMarker.getLine()).isEqualTo(line);
        assertThat(currentMarker.getColumn()).isEqualTo(column);
    }


    @Before
    public void setUp() {
        matcher = new EndOfLineMatcher();
    }


    @After
    public void tearDown() throws IllegalArgumentException,
            IllegalAccessException {
        ClassFieldCleaner.init(this);
    }
}
