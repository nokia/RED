package org.robotframework.ide.core.testData.text.lexer.matcher;

import java.nio.CharBuffer;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.text.lexer.FilePosition;
import org.robotframework.ide.core.testData.text.lexer.LowLevelTypesProvider;
import org.robotframework.ide.core.testData.text.lexer.RobotToken;
import org.robotframework.ide.core.testData.text.lexer.RobotSingleCharTokenType;
import org.robotframework.ide.core.testData.text.lexer.IRobotTokenType;
import org.robotframework.ide.core.testData.text.lexer.RobotWordType;
import org.robotframework.ide.core.testData.text.lexer.TxtRobotTestDataLexer;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.LinkedListMultimap;


/**
 * This class take responsibility for builds up expected tokens. It the first
 * take char by char in
 * {@link #offerChar(CharBuffer, int, FilePosition)} method and after in
 * {@link #buildTokens()} it returns all matched elements.
 * 
 * @author wypych
 * @since JDK 1.7 update 74
 * @version Robot Framework 2.9 alpha 2
 * 
 * @see TxtRobotTestDataLexer
 */
public class RobotTokenMatcher {

    private TokenOutput output = new TokenOutput();
    private List<ISingleCharTokenMatcher> oneCharTokenMatchers = new LinkedList<>();


    /**
     * Constructor to use only for testing propose.
     * <p/>
     * Note: do not use this constructor to merge two or more method
     * {@link #buildTokens()} invocations.
     * 
     * @param output
     *            used in tests
     */
    @VisibleForTesting
    protected RobotTokenMatcher(final TokenOutput output) {
        this.output = output;
    }


    public RobotTokenMatcher() {
        oneCharTokenMatchers.add(new EndOfLineMatcher());
        oneCharTokenMatchers.add(new WhitespaceMatcher());
        oneCharTokenMatchers.add(new PipeMatcher());
        oneCharTokenMatchers.add(new AsteriskMatcher());
        oneCharTokenMatchers.add(new HashCommentMatcher());
        oneCharTokenMatchers.add(new ScalarVariableBeginSignMatcher());
        oneCharTokenMatchers.add(new ListVariableBeginSignMatcher());
        oneCharTokenMatchers.add(new EnvironmentVariableBeginSignMatcher());
        oneCharTokenMatchers.add(new DictionaryVariableBeginSignMatcher());
        oneCharTokenMatchers.add(new EqualSignMatcher());
        oneCharTokenMatchers.add(new VariableBeginCurlySignMatcher());
        oneCharTokenMatchers.add(new VariableEndCurlySignMatcher());
        oneCharTokenMatchers.add(new IndexBeginSquareSignMatcher());
        oneCharTokenMatchers.add(new IndexEndSquareSignMatcher());
        oneCharTokenMatchers.add(new ColonSignMatcher());
        oneCharTokenMatchers.add(new QuoteMarkSignMatcher());
        oneCharTokenMatchers.add(new DotSignMatcher());
        oneCharTokenMatchers.add(new EscapeBackslashSignMatcher());

        oneCharTokenMatchers = Collections
                .unmodifiableList(oneCharTokenMatchers);
    }


    /**
     * 
     * @param tempBuffer
     *            bytes to consume
     * @param charIndex
     *            current byte index to consume
     */
    public void offerChar(final CharBuffer tempBuffer, final int charIndex) {
        boolean wasUsed = false;

        for (ISingleCharTokenMatcher matcher : oneCharTokenMatchers) {
            if (matcher.match(output, tempBuffer, charIndex)) {
                wasUsed = true;
                break;
            }
        }

        if (!wasUsed) {
            convertCharToUnknownToken(tempBuffer, charIndex);
        } else {
            tryToRecognizeUnknownTokens(output);
        }
    }


    private void convertCharToUnknownToken(final CharBuffer tempBuffer,
            final int charIndex) {
        boolean useAsNewSingleUnknownToken = true;

        char c = tempBuffer.get(charIndex);

        List<RobotToken> tokens = output.getTokens();
        if (!tokens.isEmpty()) {
            int lastTokenIndex = tokens.size() - 1;
            RobotToken lastToken = tokens.get(lastTokenIndex);
            if (lastToken.getType() == RobotSingleCharTokenType.UNKNOWN) {
                RobotToken newUnknownToken = new RobotToken(
                        lastToken.getStartPosition(), lastToken.getText()
                                .append(c));
                newUnknownToken.setType(RobotSingleCharTokenType.UNKNOWN);
                output.setCurrentMarker(newUnknownToken.getEndPosition());
                tokens.set(lastTokenIndex, newUnknownToken);

                useAsNewSingleUnknownToken = false;
            }
        }

        if (useAsNewSingleUnknownToken) {
            StringBuilder text = new StringBuilder().append(c);
            RobotToken unknownToken = new RobotToken(output.getCurrentMarker(),
                    text);
            unknownToken.setType(RobotSingleCharTokenType.UNKNOWN);
            output.setCurrentMarker(unknownToken.getEndPosition());
            output.getTokensPosition().put(RobotSingleCharTokenType.UNKNOWN,
                    tokens.size());
            tokens.add(unknownToken);
        }
    }


    /**
     * Note: After invoke this method next output will not include tokens
     * previously matched
     * 
     * @return all matched tokens
     */
    public TokenOutput buildTokens() {
        TokenOutput old = output;
        tryToRecognizeUnknownTokens(old);
        output = new TokenOutput();

        return old;
    }


    @VisibleForTesting
    protected List<ISingleCharTokenMatcher> getDeclaredSingleCharMatchers() {
        return oneCharTokenMatchers;
    }


    private void tryToRecognizeUnknownTokens(TokenOutput old) {
        LinkedListMultimap<IRobotTokenType, Integer> tokensPosition = old
                .getTokensPosition();
        List<Integer> listOfUnknown = tokensPosition
                .get(RobotSingleCharTokenType.UNKNOWN);
        if (listOfUnknown != null) {
            List<RobotToken> tokens = old.getTokens();
            for (int i = 0; i < listOfUnknown.size(); i++) {
                Integer tokenPosition = listOfUnknown.get(i);
                RobotToken unknownRobotToken = tokens.get(tokenPosition);
                RobotToken wordToken = convertToWordType(unknownRobotToken);
                tokensPosition.put(wordToken.getType(), tokenPosition);
                tokens.set(tokenPosition, wordToken);
            }

        }

        tokensPosition.removeAll(RobotSingleCharTokenType.UNKNOWN);
    }


    private RobotToken convertToWordType(RobotToken unknownRobotToken) {
        StringBuilder text = unknownRobotToken.getText();
        RobotToken token = new RobotToken(unknownRobotToken.getStartPosition(),
                text);
        IRobotTokenType type = RobotWordType.getToken(text);
        if (type == RobotWordType.UNKNOWN_WORD) {
            type = LowLevelTypesProvider.getTokenType(text);
        }

        token.setType(type);

        return token;
    }

    /**
     * Output of token matching process.
     * 
     * @author wypych
     * @since JDK 1.7 update 74
     * @version Robot Framework 2.9 alpha 2
     * 
     */
    public static class TokenOutput {

        private LinkedListMultimap<IRobotTokenType, Integer> tokenTypeToPositionOfOcurrancy = LinkedListMultimap
                .create();
        private FilePosition currentMarker = FilePosition
                .createMarkerForFirstLineAndColumn();
        private List<RobotToken> tokens = new LinkedList<>();


        /**
         * 
         * @return mapping between type of token and it position in
         *         {@link #tokens} list.
         */
        public LinkedListMultimap<IRobotTokenType, Integer> getTokensPosition() {
            return tokenTypeToPositionOfOcurrancy;
        }


        /**
         * 
         * @return current position in file
         */
        public FilePosition getCurrentMarker() {
            return currentMarker;
        }


        public void setCurrentMarker(final FilePosition newMarker) {
            this.currentMarker = newMarker;
        }


        /**
         * 
         * @return sequence of tokens matched currently
         */
        public List<RobotToken> getTokens() {
            return tokens;
        }
    }

}
