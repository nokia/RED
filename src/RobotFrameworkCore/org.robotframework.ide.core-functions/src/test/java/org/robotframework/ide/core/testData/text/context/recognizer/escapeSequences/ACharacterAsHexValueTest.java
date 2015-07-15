package org.robotframework.ide.core.testData.text.context.recognizer.escapeSequences;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import java.io.FileNotFoundException;
import java.io.IOException;
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
import org.robotframework.ide.core.testData.text.context.recognizer.escapeSequences.ACharacterAsHexValue;
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
 * @see ACharacterAsHexValue
 */
public class ACharacterAsHexValueTest {

    private final SimpleRobotContextType buildType = SimpleRobotContextType.DECLARED_COMMENT;
    private final char startChar = 'w';
    private final int numberOfHexChars = 2;

    @ForClean
    private Dummy dummy;


    @Test
    public void test_extractTextForTokenWhichContainsIt() {
        // prepare
        RobotToken token = mock(RobotToken.class);
        StringBuilder text = new StringBuilder("foobar");
        when(token.getText()).thenReturn(text);

        // execute
        String extractText = dummy.extractText(token);

        // verify
        assertThat(extractText).isEqualTo(text.toString());
    }


    @Test
    public void test_recognizeLogic_forInCorrectElement_notCorrectWord() {
        // prepare
        RobotToken tokenEscape = mock(RobotToken.class);
        when(tokenEscape.getType()).thenReturn(
                RobotSingleCharTokenType.SINGLE_ESCAPE_BACKSLASH);

        RobotToken wordToken = mock(RobotToken.class);
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
        when(dummy.isStartingFromLetterAndHexNumber(wordToken)).thenReturn(
                false);

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
        order.verify(dummy, times(1)).isStartingFromLetterAndHexNumber(
                wordToken);
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
        when(dummy.isStartingFromLetterAndHexNumber(wordToken))
                .thenReturn(true);

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
    public void test_recognizeLogic_forCorrectElement() {
        // prepare
        RobotToken tokenEscape = mock(RobotToken.class);
        when(tokenEscape.getType()).thenReturn(
                RobotSingleCharTokenType.SINGLE_ESCAPE_BACKSLASH);

        RobotToken wordToken = mock(RobotToken.class);
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
        when(dummy.isStartingFromLetterAndHexNumber(wordToken))
                .thenReturn(true);

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
        order.verify(dummy, times(1)).isStartingFromLetterAndHexNumber(
                wordToken);
        order.verify(singleLineCtx, times(1)).addNextToken(wordToken);
        order.verify(singleLineCtx, times(1)).setType(buildType);
        order.verify(lineInterval, times(1)).getEnd();
        order.verifyNoMoreInteractions();

        assertThat(recognize).containsSequence(singleLineCtx);
    }


    @Test
    public void test_isStartingFromLetterAndThenHex_textLengthIsBelowExpected_shouldReturn_false() {
        String text = "0";
        assertThat(text.length()).isLessThan(numberOfHexChars);
        assertThat(dummy.isStartingFromLetterAndThenHex(text)).isFalse();
    }


    @Test
    public void test_isStartingFromLetterAndThenHex_textIsNull_shouldReturn_false() {
        assertThat(dummy.isStartingFromLetterAndThenHex(null)).isFalse();
    }


    @Test
    public void test_isHexValue_forG0_text_shouldReturn_false() {
        assertThat(dummy.isHex(startChar + "G0", numberOfHexChars)).isFalse();
    }


    @Test
    public void test_isHexValue_for0G_text_shouldReturn_false() {
        assertThat(dummy.isHex(startChar + "0G", numberOfHexChars)).isFalse();
    }


    @Test
    public void test_isHexCombinationsOfNumbersAndLetters() {
        for (int i = 0; i <= 9; i++) {
            for (int j = 'a'; j <= 'f'; j++) {
                String hex = "" + startChar + i + "" + j;
                assertThat(dummy.isHex(hex, numberOfHexChars)).isTrue();
            }
        }

        for (int i = 0; i <= 9; i++) {
            for (int j = 'A'; j <= 'F'; j++) {
                String hex = "" + startChar + i + "" + j;
                assertThat(dummy.isHex(hex, numberOfHexChars)).isTrue();
            }
        }

        for (int i = 'a'; i <= 'f'; i++) {
            for (int j = 0; j <= 9; j++) {
                String hex = "" + startChar + i + "" + j;
                assertThat(dummy.isHex(hex, numberOfHexChars)).isTrue();
            }
        }

        for (int i = 'A'; i <= 'A'; i++) {
            for (int j = 0; j <= 9; j++) {
                String hex = "" + startChar + i + "" + j;
                assertThat(dummy.isHex(hex, numberOfHexChars)).isTrue();
            }
        }
    }


    @Test
    public void test_isHex_combinationsOfNumbersOnly_allPossibilities() {
        for (int i = 0; i <= 9; i++) {
            for (int j = 0; j <= 9; j++) {
                String hex = "" + startChar + i + "" + j;
                assertThat(dummy.isHex(hex, numberOfHexChars)).isTrue();
            }
        }
    }


    @Test
    public void test_isHex_combinationsOfLetterHexOnly_allPossibilities()
            throws FileNotFoundException, IOException {
        for (int i = 'a'; i <= 'f'; i++) {
            for (int j = 'a'; j <= 'f'; j++) {
                String hex = "" + i + "" + j;
                assertThat(dummy.isHex(hex, numberOfHexChars)).isTrue();
            }
        }

        for (int i = 'a'; i <= 'f'; i++) {
            for (int j = 'A'; j <= 'F'; j++) {
                String hex = "" + i + "" + j;
                assertThat(dummy.isHex(hex, numberOfHexChars)).isTrue();
            }
        }

        for (int i = 'A'; i <= 'A'; i++) {
            for (int j = 'a'; j <= 'f'; j++) {
                String hex = "" + i + "" + j;
                assertThat(dummy.isHex(hex, numberOfHexChars)).isTrue();
            }
        }

        for (int i = 'A'; i <= 'A'; i++) {
            for (int j = 'A'; j <= 'F'; j++) {
                String hex = "" + i + "" + j;
                assertThat(dummy.isHex(hex, numberOfHexChars)).isTrue();
            }
        }
    }


    @Test
    public void test_forIsHex_upperCaseLetters_from_A_to_F_shouldReturn_true() {
        for (char i = 'A'; i <= 'F'; i++) {
            assertThat(dummy.isHex(i)).isTrue();
        }
    }


    @Test
    public void test_forIsHex_lowerCaseLetters_from_a_to_f_shouldReturn_true() {
        for (char i = 'a'; i <= 'f'; i++) {
            assertThat(dummy.isHex(i)).isTrue();
        }
    }


    @Test
    public void test_forIsHex_numbers_from_0_to_9_shouldReturn_true() {
        for (char i = '0'; i <= '9'; i++) {
            assertThat(dummy.isHex(i)).isTrue();
        }
    }


    @Test
    public void test_getType() {
        assertThat(dummy.getContextType()).isEqualTo(buildType);
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

    private class Dummy extends ACharacterAsHexValue {

        public Dummy() {
            super(buildType, startChar, numberOfHexChars);
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
