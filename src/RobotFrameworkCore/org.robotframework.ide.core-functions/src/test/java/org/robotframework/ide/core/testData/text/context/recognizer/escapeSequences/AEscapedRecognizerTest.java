package org.robotframework.ide.core.testData.text.context.recognizer.escapeSequences;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.robotframework.ide.core.testData.text.context.ContextBuilder.ContextOutput;
import org.robotframework.ide.core.testData.text.context.IContextElement;
import org.robotframework.ide.core.testData.text.context.OneLineSingleRobotContextPart;
import org.robotframework.ide.core.testData.text.context.SimpleRobotContextType;
import org.robotframework.ide.core.testData.text.context.iterator.TokensLineIterator.LineTokenPosition;
import org.robotframework.ide.core.testData.text.lexer.RobotSingleCharTokenType;
import org.robotframework.ide.core.testData.text.lexer.RobotToken;
import org.robotframework.ide.core.testData.text.lexer.RobotWordType;
import org.robotframework.ide.core.testData.text.lexer.matcher.RobotTokenMatcher.TokenOutput;
import org.robotframework.ide.core.testHelpers.ClassFieldCleaner;
import org.robotframework.ide.core.testHelpers.ClassFieldCleaner.ForClean;


/**
 * 
 * @author wypych
 * @since JDK 1.7 update 74
 * @version Robot Framework 2.9 alpha 2
 * 
 * @see AEscapedRecognizer
 */
public class AEscapedRecognizerTest {

    private final SimpleRobotContextType buildType = SimpleRobotContextType.DECLARED_COMMENT;
    private final char lowerCase = 'h';
    private final char upperCase = 'H';

    @ForClean
    private Dummy dummy;


    @Test
    public void test_recognizeLogic_forInCorrectElement_notCorrectWord() {
        // prepare
        RobotToken tokenEscape = mock(RobotToken.class);
        when(tokenEscape.getType()).thenReturn(
                RobotSingleCharTokenType.SINGLE_ESCAPE_BACKSLASH);

        RobotToken wordToken = mock(RobotToken.class);
        when(wordToken.getText()).thenReturn(new StringBuilder(lowerCase));
        when(wordToken.getType()).thenReturn(RobotWordType.UNKNOWN_WORD);
        List<RobotToken> tokens = Arrays.asList(tokenEscape, wordToken);

        LineTokenPosition lineInterval = mock(LineTokenPosition.class);
        when(lineInterval.getLineNumber()).thenReturn(1);
        when(lineInterval.getStart()).thenReturn(0);
        when(lineInterval.getEnd()).thenReturn(tokens.size());

        TokenOutput tokenOutput = mock(TokenOutput.class);
        when(tokenOutput.getTokens()).thenReturn(tokens);

        ContextOutput contextOutput = mock(ContextOutput.class);
        when(contextOutput.getTokenizedContent()).thenReturn(tokenOutput);
        OneLineSingleRobotContextPart singleLineCtx = mock(OneLineSingleRobotContextPart.class);

        dummy = spy(new Dummy());
        doReturn(singleLineCtx).when(dummy).createContext(lineInterval);
        when(dummy.isStartingFromLetter(wordToken)).thenReturn(false);

        // execute
        List<IContextElement> recognize = dummy.recognize(contextOutput,
                lineInterval);

        // verify
        InOrder order = inOrder(lineInterval, contextOutput, tokenOutput,
                tokenEscape, wordToken, singleLineCtx, dummy);
        order.verify(dummy, times(1)).createContext(lineInterval);
        order.verify(contextOutput, times(1)).getTokenizedContent();
        order.verify(tokenOutput, times(1)).getTokens();
        order.verify(lineInterval, times(1)).getStart();

        // iter for escape backslash
        order.verify(lineInterval, times(1)).getEnd();
        order.verify(tokenEscape, times(1)).getType();
        order.verify(singleLineCtx, times(1)).addNextToken(tokenEscape);

        // iter for word
        order.verify(lineInterval, times(1)).getEnd();
        order.verify(wordToken, times(1)).getType();
        order.verify(dummy, times(2)).getCustomWordTypeHandler();
        order.verify(singleLineCtx, times(1)).removeAllContextTokens();
        order.verify(lineInterval, times(1)).getEnd();
        order.verifyNoMoreInteractions();

        assertThat(recognize).isEmpty();
    }


    @Test
    public void test_recognizeLogic_forInCorrectElement_notBackslash() {
        // prepare
        RobotToken tokenNotEscape = mock(RobotToken.class);
        when(tokenNotEscape.getType()).thenReturn(
                RobotSingleCharTokenType.LINE_FEED);

        RobotToken wordToken = mock(RobotToken.class);
        when(wordToken.getType()).thenReturn(RobotWordType.UNKNOWN_WORD);
        List<RobotToken> tokens = Arrays.asList(tokenNotEscape, wordToken);

        LineTokenPosition lineInterval = mock(LineTokenPosition.class);
        when(lineInterval.getLineNumber()).thenReturn(1);
        when(lineInterval.getStart()).thenReturn(0);
        when(lineInterval.getEnd()).thenReturn(tokens.size());

        TokenOutput tokenOutput = mock(TokenOutput.class);
        when(tokenOutput.getTokens()).thenReturn(tokens);

        ContextOutput contextOutput = mock(ContextOutput.class);
        when(contextOutput.getTokenizedContent()).thenReturn(tokenOutput);
        OneLineSingleRobotContextPart singleLineCtx = mock(OneLineSingleRobotContextPart.class);

        dummy = spy(new Dummy());
        doReturn(singleLineCtx).when(dummy).createContext(lineInterval);
        when(dummy.isStartingFromLetter(wordToken)).thenReturn(true);

        // execute
        List<IContextElement> recognize = dummy.recognize(contextOutput,
                lineInterval);

        // verify
        InOrder order = inOrder(lineInterval, contextOutput, tokenOutput,
                tokenNotEscape, wordToken, singleLineCtx, dummy);
        order.verify(dummy, times(1)).recognize(contextOutput, lineInterval);
        order.verify(dummy, times(1)).createContext(lineInterval);
        order.verify(contextOutput, times(1)).getTokenizedContent();
        order.verify(tokenOutput, times(1)).getTokens();
        order.verify(lineInterval, times(1)).getStart();

        // iter for not escape backslash
        order.verify(lineInterval, times(1)).getEnd();
        order.verify(tokenNotEscape, times(1)).getType();
        order.verify(singleLineCtx, times(1)).removeAllContextTokens();

        // iter for word token
        order.verify(lineInterval, times(1)).getEnd();
        order.verify(wordToken, times(1)).getType();
        order.verify(lineInterval, times(1)).getEnd();

        order.verifyNoMoreInteractions();

        assertThat(recognize).isEmpty();
    }


    @Test
    public void test_recognizeLogic_forPossitiveCase() {
        // prepare
        RobotToken tokenEscape = mock(RobotToken.class);
        when(tokenEscape.getType()).thenReturn(
                RobotSingleCharTokenType.SINGLE_ESCAPE_BACKSLASH);

        RobotToken wordToken = mock(RobotToken.class);
        when(wordToken.getType()).thenReturn(RobotWordType.UNKNOWN_WORD);
        when(wordToken.getText()).thenReturn(
                new StringBuilder().append(upperCase));
        List<RobotToken> tokens = Arrays.asList(tokenEscape, wordToken);

        LineTokenPosition lineInterval = mock(LineTokenPosition.class);
        when(lineInterval.getLineNumber()).thenReturn(1);
        when(lineInterval.getStart()).thenReturn(0);
        when(lineInterval.getEnd()).thenReturn(tokens.size());

        TokenOutput tokenOutput = mock(TokenOutput.class);
        when(tokenOutput.getTokens()).thenReturn(tokens);

        ContextOutput contextOutput = mock(ContextOutput.class);
        when(contextOutput.getTokenizedContent()).thenReturn(tokenOutput);
        OneLineSingleRobotContextPart singleLineCtx = mock(OneLineSingleRobotContextPart.class);

        dummy = spy(new Dummy());
        doReturn(singleLineCtx).when(dummy).createContext(lineInterval);

        // execute
        List<IContextElement> recognize = dummy.recognize(contextOutput,
                lineInterval);

        // verify
        InOrder order = inOrder(lineInterval, contextOutput, tokenOutput,
                tokenEscape, wordToken, singleLineCtx, dummy);
        order.verify(dummy, times(1)).createContext(lineInterval);
        order.verify(contextOutput, times(1)).getTokenizedContent();
        order.verify(tokenOutput, times(1)).getTokens();
        order.verify(lineInterval, times(1)).getStart();

        // iter for escape backslash
        order.verify(lineInterval, times(1)).getEnd();
        order.verify(tokenEscape, times(1)).getType();
        order.verify(singleLineCtx, times(1)).addNextToken(tokenEscape);

        // iter for word
        order.verify(lineInterval, times(1)).getEnd();
        order.verify(wordToken, times(1)).getType();
        order.verify(dummy, times(2)).getCustomWordTypeHandler();
        order.verify(lineInterval, times(1)).getEnd();
        order.verifyNoMoreInteractions();

        assertThat(recognize).containsSequence(singleLineCtx);
    }


    @Test
    public void test_createContext() {
        LineTokenPosition pos = mock(LineTokenPosition.class);
        when(pos.getLineNumber()).thenReturn(1);

        OneLineSingleRobotContextPart l = dummy.createContext(pos);
        assertThat(l).isNotNull();
        assertThat(l.getLineNumber()).isEqualTo(1);
    }


    @Test
    public void test_isStartingFromLetter_checkForUperCase_HthenOtherText_expected_forRobotToken() {
        RobotToken token = mock(RobotToken.class);
        when(token.getText()).thenReturn(new StringBuilder("H_other_text"));
        assertThat(dummy.isStartingFromLetter(token)).isTrue();
    }


    @Test
    public void test_isStartingFromLetter_checkForUpperCase_H_expected_forRobotToken() {
        RobotToken token = mock(RobotToken.class);
        when(token.getText()).thenReturn(new StringBuilder("H"));
        assertThat(dummy.isStartingFromLetter(token)).isTrue();
    }


    @Test
    public void test_isStartingFromLetter_checkForLowerCase_HthenOtherText_expected_forRobotToken() {
        RobotToken token = mock(RobotToken.class);
        when(token.getText()).thenReturn(new StringBuilder("h_other_text"));
        assertThat(dummy.isStartingFromLetter(token)).isTrue();
    }


    @Test
    public void test_isStartingFromLetter_checkForLowerCase_H_expected_forRobotToken() {
        RobotToken token = mock(RobotToken.class);
        when(token.getText()).thenReturn(new StringBuilder("h"));
        assertThat(dummy.isStartingFromLetter(token)).isTrue();
    }


    @Test
    public void test_isStartingFromLetter_checkForNotNumber_inRobotTokenMethod() {
        RobotToken token = mock(RobotToken.class);
        when(token.getText()).thenReturn(new StringBuilder("12132121"));
        assertThat(dummy.isStartingFromLetter(token)).isFalse();
    }


    @Test
    public void test_isStartingFromLetter_checkForEmptyString_inRobotToken() {
        RobotToken token = mock(RobotToken.class);
        when(token.getText()).thenReturn(new StringBuilder(""));
        assertThat(dummy.isStartingFromLetter(token)).isFalse();
    }


    @Test
    public void test_isStartingFromLetter_checkForNull_textInRobotToken() {
        RobotToken token = mock(RobotToken.class);
        when(token.getText()).thenReturn(null);
        assertThat(dummy.isStartingFromLetter(token)).isFalse();
    }


    @Test
    public void test_isStartingFromLetter_checkForUperCase_HthenOtherText_expected() {
        assertThat(dummy.isStartingFromLetter("H_other_text")).isTrue();
    }


    @Test
    public void test_isStartingFromLetter_checkForUpperCase_H_expected() {
        assertThat(dummy.isStartingFromLetter("H")).isTrue();
    }


    @Test
    public void test_isStartingFromLetter_checkForLowerCase_HthenOtherText_expected() {
        assertThat(dummy.isStartingFromLetter("h_other_text")).isTrue();
    }


    @Test
    public void test_isStartingFromLetter_checkForLowerCase_H_expected() {
        assertThat(dummy.isStartingFromLetter("h")).isTrue();
    }


    @Test
    public void test_isStartingFromLetter_checkForNotNumber() {
        assertThat(dummy.isStartingFromLetter("12132121")).isFalse();
    }


    @Test
    public void test_isStartingFromLetter_checkForEmptyString() {
        assertThat(dummy.isStartingFromLetter("")).isFalse();
    }


    @Test
    public void test_isStartingFromLetter_checkForNullText() {
        assertThat(dummy.isStartingFromLetter((String) null)).isFalse();
    }


    @Test
    public void test_extractTextForTokenWithoutText() {
        // prepare
        RobotToken token = mock(RobotToken.class);
        when(token.getText()).thenReturn(null);

        // execute
        String extractText = dummy.extractText(token);

        // verify
        assertThat(extractText).isNull();
    }


    @Test
    public void test_extractTextForTokenWithNullText() {
        // prepare
        RobotToken token = mock(RobotToken.class);
        when(token.getText()).thenReturn(null);

        // execute
        String extractText = dummy.extractText(token);

        // verify
        assertThat(extractText).isNull();
    }


    @Test
    public void test_getContextType_shouldReturn_setBuildType() {
        assertThat(dummy.getContextType()).isEqualTo(buildType);
    }

    private class Dummy extends AEscapedRecognizer {

        public Dummy() {
            super(buildType, lowerCase, upperCase);
        }


        @Override
        public boolean isStartingFromLetter(final RobotToken token) {
            return isStartingFromLetter(extractText(token));
        }
    }


    @Before
    public void setUp() {
        dummy = new Dummy();
    }


    @After
    public void tearDown() throws Exception {
        ClassFieldCleaner.init(this);
    }
}
