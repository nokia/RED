package org.robotframework.ide.core.testHelpers;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.robotframework.ide.core.testData.text.lexer.IRobotTokenType;
import org.robotframework.ide.core.testData.text.lexer.FilePosition;
import org.robotframework.ide.core.testData.text.lexer.MultipleCharTokenType;
import org.robotframework.ide.core.testData.text.lexer.NumberType;
import org.robotframework.ide.core.testData.text.lexer.RobotSingleCharTokenType;
import org.robotframework.ide.core.testData.text.lexer.RobotToken;
import org.robotframework.ide.core.testData.text.lexer.RobotWordType;
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
        LinkedListMultimap<IRobotTokenType, Integer> tokensPosition = output
                .getTokensPosition();
        assertThat(tokensPosition).isNotNull();
        List<RobotToken> tokens = output.getTokens();
        Set<IRobotTokenType> typeSetFound = new HashSet<>();
        for (int i = 0; i < tokens.size(); i++) {
            RobotToken cToken = tokens.get(i);
            assertThat(tokensPosition.get(cToken.getType())).contains(i);
            typeSetFound.add(cToken.getType());
        }

        Set<IRobotTokenType> typeSet = tokensPosition.keySet();
        assertThat(typeSet).hasSameSizeAs(typeSetFound);
        assertThat(typeSet).hasSameElementsAs(typeSetFound);
    }


    public static void assertTokensForUnknownWords(List<RobotToken> tokens,
            IRobotTokenType[] types, int startTokenPos,
            FilePosition linePos,
            String[] textForCorrespondingUnknownWords) {
        int typesLength = types.length;
        CircullarArrayIterator<IRobotTokenType> iter = new CircullarArrayIterator<>(
                types);

        assertThat(tokens).isNotNull();

        assertThat(tokens).isNotEmpty();
        assertThat((tokens.size() - startTokenPos) % typesLength).isEqualTo(0);
        int correspondingTextIndex = 0;
        int line = linePos.getLine();
        int column = linePos.getColumn();
        for (int tokId = startTokenPos; tokId < tokens.size(); tokId++) {
            RobotToken robotToken = tokens.get(tokId);
            assertThat(robotToken).isNotNull();
            IRobotTokenType type = iter.next();
            assertThat(robotToken.getType()).isEqualTo(type);

            if (type == RobotSingleCharTokenType.END_OF_LINE) {
                assertThat(robotToken.getText()).isNull();
                assertStartPosition(robotToken, line, column);
                assertEndPosition(robotToken, line, column);
                line++;
                column = FilePosition.THE_FIRST_COLUMN;
            } else {
                if ((robotToken.getType() == RobotSingleCharTokenType.UNKNOWN || robotToken
                        .getType() == RobotWordType.UNKNOWN_WORD)
                        && correspondingTextIndex < textForCorrespondingUnknownWords.length) {
                    assertThat(robotToken.getText().toString())
                            .isEqualTo(
                                    textForCorrespondingUnknownWords[correspondingTextIndex]);
                    correspondingTextIndex++;
                } else if (MultipleCharTokenType.class
                        .isAssignableFrom(robotToken.getType().getClass())) {
                    assertThat(robotToken.getText().toString()).matches(
                            "["
                                    + ((MultipleCharTokenType) robotToken
                                            .getType()).getWrappedType()
                                            .toWrite() + "]+");

                } else if (robotToken.getType().getClass() != MultipleCharTokenType.class) {
                    assertThat(robotToken.getText().toString())
                            .isEqualToIgnoringCase(type.toWrite());
                } else {
                    throw new UnsupportedOperationException(
                            "Not implemented yet!");
                }
                assertStartPosition(robotToken, line, column);
                if (robotToken.getText() != null) {
                    column += robotToken.getText().length();
                } else {
                    column++;
                }
                assertEndPosition(robotToken, line, column);
            }
        }

    }


    public static void assertTokensForUnknownWords(TokenOutput out,
            IRobotTokenType[] types, int startTokenPos, int startLine,
            String[] textForCorrespondingUnknownWords) {
        List<RobotToken> tokens = out.getTokens();
        assertTokensForUnknownWords(tokens, types, startTokenPos,
                new FilePosition(startLine,
                        FilePosition.THE_FIRST_COLUMN),
                textForCorrespondingUnknownWords);
    }


    public static void assertTokens(TokenOutput out, IRobotTokenType[] types,
            int startTokenPos, int startLine) {
        List<RobotToken> tokens = out.getTokens();
        int typesLength = types.length;
        CircullarArrayIterator<IRobotTokenType> iter = new CircullarArrayIterator<>(
                types);

        assertThat(tokens).isNotNull();
        assertThat(tokens).isNotEmpty();
        assertThat((tokens.size() - startTokenPos) % typesLength).isEqualTo(0);
        int line = startLine;
        int column = FilePosition.THE_FIRST_COLUMN;
        for (int tokId = startTokenPos; tokId < tokens.size(); tokId++) {
            RobotToken robotToken = tokens.get(tokId);
            assertThat(robotToken).isNotNull();
            IRobotTokenType type = iter.next();
            assertThat(robotToken.getType()).isEqualTo(type);

            if (type == RobotSingleCharTokenType.END_OF_LINE) {
                assertThat(robotToken.getText()).isNull();
                assertStartPosition(robotToken, line, column);
                assertEndPosition(robotToken, line, column);
                line++;
                column = FilePosition.THE_FIRST_COLUMN;
            } else {
                if (robotToken.getType().getClass() != MultipleCharTokenType.class
                        && robotToken.getType().isWriteable()) {
                    assertThat(robotToken.getText().toString()).isEqualTo(
                            type.toWrite());
                } else if (MultipleCharTokenType.class
                        .isAssignableFrom(robotToken.getType().getClass())) {
                    assertThat(robotToken.getText().toString()).matches(
                            "["
                                    + ((MultipleCharTokenType) robotToken
                                            .getType()).getWrappedType()
                                            .toWrite() + "]+");
                } else if (robotToken.getType() == NumberType.NUMBER_WITH_SIGN) {
                    assertThat(robotToken.getText().toString()
                            .matches("^[-][0-9]+$"));
                } else if (robotToken.getType() == NumberType.NUMBER_WITHOUT_SIGN) {
                    assertThat(robotToken.getText().toString()
                            .matches("^[0-9]+$"));
                } else {
                    throw new UnsupportedOperationException(
                            "Not implemented yet!");
                }
                assertStartPosition(robotToken, line, column);
                if (robotToken.getText() != null) {
                    column += robotToken.getText().length();
                } else {
                    column++;
                }
                assertEndPosition(robotToken, line, column);
            }
        }
    }


    public static void assertCurrentPosition(TokenOutput output) {
        FilePosition currentMarker = output.getCurrentMarker();
        assertThat(currentMarker).isNotNull();
        LinkedListMultimap<IRobotTokenType, Integer> tokensPosition = output
                .getTokensPosition();
        assertThat(tokensPosition).isNotNull();
        List<Integer> crTokens = tokensPosition
                .get(RobotSingleCharTokenType.CARRIAGE_RETURN);
        List<Integer> lfTokens = tokensPosition
                .get(RobotSingleCharTokenType.LINE_FEED);
        int line = Math.max(crTokens.size(), lfTokens.size());
        assertThat(currentMarker.getLine()).isEqualTo(line + 1);
        assertThat(currentMarker.getColumn()).isEqualTo(currentColumn(output));
    }


    private static int currentColumn(TokenOutput output) {
        List<RobotToken> tokens = output.getTokens();
        int column = FilePosition.THE_FIRST_COLUMN;
        if (!tokens.isEmpty()) {
            RobotToken robotToken = tokens.get(tokens.size() - 1);
            column = robotToken.getEndPosition().getColumn();
            if (robotToken.getType() == RobotSingleCharTokenType.END_OF_LINE) {
                column = FilePosition.THE_FIRST_COLUMN;
            }
        }

        return column;
    }


    public static void assertStartPosition(RobotToken token, int line,
            int column) {
        FilePosition startPosition = token.getStartPosition();
        assertThat(startPosition).isNotNull();
        assertThat(startPosition.getLine()).isEqualTo(line);
        assertThat(startPosition.getColumn()).isEqualTo(column);
    }


    public static void assertEndPosition(RobotToken token, int line, int column) {
        FilePosition endPosition = token.getEndPosition();
        assertThat(endPosition).isNotNull();
        assertThat(endPosition.getLine()).isEqualTo(line);
        assertThat(endPosition.getColumn()).isEqualTo(column);
    }


    public static void assertIsTotalEmpty(final TokenOutput output) {
        assertThat(output.getTokens()).isEmpty();
        assertThat(output.getTokensPosition().asMap()).isEmpty();
        assertCurrentMarkerPosition(output,
                FilePosition.THE_FIRST_LINE,
                FilePosition.THE_FIRST_COLUMN);
    }


    public static void assertCurrentMarkerPosition(final TokenOutput out,
            int line, int column) {
        FilePosition currentMarker = out.getCurrentMarker();
        assertThat(currentMarker).isNotNull();
        assertThat(currentMarker.getLine()).isEqualTo(line);
        assertThat(currentMarker.getColumn()).isEqualTo(column);
    }


    public static TokenOutput createTokenOutputWithTwoTabulatorsInside() {
        TokenOutput output = new TokenOutput();
        FilePosition beginMarker = output.getCurrentMarker();

        RobotToken tabulatorTokenOne = new RobotToken(beginMarker,
                new StringBuilder("\t"));
        tabulatorTokenOne.setType(RobotSingleCharTokenType.SINGLE_TABULATOR);
        output.getTokens().add(tabulatorTokenOne);
        output.getTokensPosition().put(
                RobotSingleCharTokenType.SINGLE_TABULATOR, 0);

        RobotToken tabulatorTokenTwo = new RobotToken(
                tabulatorTokenOne.getEndPosition(), new StringBuilder("\t"));
        tabulatorTokenTwo.setType(RobotSingleCharTokenType.SINGLE_TABULATOR);
        output.getTokens().add(tabulatorTokenTwo);
        output.getTokensPosition().put(
                RobotSingleCharTokenType.SINGLE_TABULATOR, 1);

        output.setCurrentMarker(tabulatorTokenTwo.getEndPosition());

        return output;
    }


    public static TokenOutput createTokenOutputWithTwoAsterisksInside() {
        TokenOutput output = new TokenOutput();
        FilePosition beginMarker = output.getCurrentMarker();

        RobotToken asteriskTokenOne = new RobotToken(beginMarker,
                new StringBuilder("*"));
        asteriskTokenOne.setType(RobotSingleCharTokenType.SINGLE_ASTERISK);
        output.getTokens().add(asteriskTokenOne);
        output.getTokensPosition().put(
                RobotSingleCharTokenType.SINGLE_ASTERISK, 0);

        RobotToken asteriskTokenTwo = new RobotToken(
                asteriskTokenOne.getEndPosition(), new StringBuilder("*"));
        asteriskTokenTwo.setType(RobotSingleCharTokenType.SINGLE_ASTERISK);
        output.getTokens().add(asteriskTokenTwo);
        output.getTokensPosition().put(
                RobotSingleCharTokenType.SINGLE_ASTERISK, 1);

        output.setCurrentMarker(asteriskTokenTwo.getEndPosition());

        return output;
    }
}
