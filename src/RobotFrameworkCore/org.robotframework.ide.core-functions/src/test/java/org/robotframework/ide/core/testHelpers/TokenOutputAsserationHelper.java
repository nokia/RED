package org.robotframework.ide.core.testHelpers;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.robotframework.ide.core.testData.text.lexer.LinearPositionMarker;
import org.robotframework.ide.core.testData.text.lexer.RobotToken;
import org.robotframework.ide.core.testData.text.lexer.RobotTokenType;
import org.robotframework.ide.core.testData.text.lexer.RobotType;
import org.robotframework.ide.core.testData.text.lexer.matcher.RobotTokenMatcher.TokenOutput;

import com.google.common.collect.LinkedListMultimap;


/**
 * Common assertions for {@link TokenOutput} object contents.
 * 
 * @author wypych
 * @since JDK 1.7 update 74
 * @version Robot Framework 2.9 alpha 2
 * 
 */
public class TokenOutputAsserationHelper {

    public static void assertPositionMarkers(TokenOutput output) {
        LinkedListMultimap<RobotType, Integer> tokensPosition = output
                .getTokensPosition();
        assertThat(tokensPosition).isNotNull();
        List<RobotToken> tokens = output.getTokens();
        for (int i = 0; i < tokens.size(); i++) {
            RobotToken cToken = tokens.get(i);
            assertThat(tokensPosition.get(cToken.getType())).contains(i);
        }
    }


    public static void assertTokens(TokenOutput out, RobotTokenType[] types,
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


    public static void assertCurrentPosition(TokenOutput output) {
        LinearPositionMarker currentMarker = output.getCurrentMarker();
        assertThat(currentMarker).isNotNull();
        LinkedListMultimap<RobotType, Integer> tokensPosition = output
                .getTokensPosition();
        assertThat(tokensPosition).isNotNull();
        List<Integer> crTokens = tokensPosition
                .get(RobotTokenType.CARRIAGE_RETURN);
        List<Integer> lfTokens = tokensPosition.get(RobotTokenType.LINE_FEED);
        int line = Math.max(crTokens.size(), lfTokens.size());
        if (line == 0) {
            line = 1;
        }
        assertThat(currentMarker.getLine()).isEqualTo(line + 1);
        assertThat(currentMarker.getColumn()).isEqualTo(
                ((crTokens.size() + lfTokens.size()) % line) + 1);
    }


    public static void assertStartPosition(RobotToken token, int line,
            int column) {
        LinearPositionMarker startPosition = token.getStartPosition();
        assertThat(startPosition).isNotNull();
        assertThat(startPosition.getLine()).isEqualTo(line);
        assertThat(startPosition.getColumn()).isEqualTo(column);
    }


    public static void assertEndPosition(RobotToken token, int line, int column) {
        LinearPositionMarker endPosition = token.getEndPosition();
        assertThat(endPosition).isNotNull();
        assertThat(endPosition.getLine()).isEqualTo(line);
        assertThat(endPosition.getColumn()).isEqualTo(column);
    }


    public static void assertIsTotalEmpty(final TokenOutput output) {
        assertThat(output.getTokens()).isEmpty();
        assertThat(output.getTokensPosition().asMap()).isEmpty();
        assertCurrentMarkerPosition(output,
                LinearPositionMarker.THE_FIRST_LINE,
                LinearPositionMarker.THE_FIRST_COLUMN);
    }


    public static void assertCurrentMarkerPosition(final TokenOutput out,
            int line, int column) {
        LinearPositionMarker currentMarker = out.getCurrentMarker();
        assertThat(currentMarker).isNotNull();
        assertThat(currentMarker.getLine()).isEqualTo(line);
        assertThat(currentMarker.getColumn()).isEqualTo(column);
    }
}
