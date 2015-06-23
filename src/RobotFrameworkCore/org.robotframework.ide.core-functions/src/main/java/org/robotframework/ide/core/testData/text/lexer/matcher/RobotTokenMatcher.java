package org.robotframework.ide.core.testData.text.lexer.matcher;

import java.nio.CharBuffer;
import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.text.lexer.LinearPositionMarker;
import org.robotframework.ide.core.testData.text.lexer.RobotToken;
import org.robotframework.ide.core.testData.text.lexer.RobotType;

import com.google.common.collect.LinkedListMultimap;


/**
 * This class take responsibility for builds up expected tokens. It the first
 * take char by char in
 * {@link #offerChar(CharBuffer, int, LinearPositionMarker)} method and after in
 * {@link #buildTokens()} it returns all matched elements.
 * 
 * @author wypych
 * @since JDK 1.7 update 74
 * @version Robot Framework 2.9 alpha 2
 * 
 */
public class RobotTokenMatcher {

    private TokenOutput output = new TokenOutput();
    private List<ISingleCharTokenMatcher> oneCharTokenMatchers = new LinkedList<>();


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
    }


    public void offerChar(final CharBuffer tempBuffer, final int charIndex) {
        boolean wasUsed = false;

        for (ISingleCharTokenMatcher matcher : oneCharTokenMatchers) {
            if (matcher.match(output, tempBuffer, charIndex)) {
                wasUsed = true;
                break;
            }
        }

        if (wasUsed) {

        }
        // TODO: if not used maybe exception?
    }


    public TokenOutput buildTokens() {
        TokenOutput old = output;
        output = new TokenOutput();

        return old;
    }

    public static class TokenOutput {

        private LinkedListMultimap<RobotType, Integer> tokenTypeToPositionOfOcurrancy = LinkedListMultimap
                .create();
        private LinearPositionMarker currentMarker = LinearPositionMarker
                .createMarkerForFirstLineAndColumn();
        private List<RobotToken> tokens = new LinkedList<>();


        public LinkedListMultimap<RobotType, Integer> getTokensPosition() {
            return tokenTypeToPositionOfOcurrancy;
        }


        public LinearPositionMarker getCurrentMarker() {
            return currentMarker;
        }


        public void setCurrentMarker(final LinearPositionMarker newMarker) {
            this.currentMarker = newMarker;
        }


        public List<RobotToken> getTokens() {
            return tokens;
        }
    }

}
