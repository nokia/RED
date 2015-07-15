package org.robotframework.ide.core.testData.text.context.recognizer.variables;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.robotframework.ide.core.testData.text.context.OneLineSingleRobotContextPart;
import org.robotframework.ide.core.testData.text.context.SimpleRobotContextType;
import org.robotframework.ide.core.testData.text.context.iterator.TokensLineIterator.LineTokenPosition;
import org.robotframework.ide.core.testData.text.context.recognizer.variables.AVariableRecognizer;
import org.robotframework.ide.core.testData.text.lexer.IRobotTokenType;
import org.robotframework.ide.core.testData.text.lexer.RobotSingleCharTokenType;
import org.robotframework.ide.core.testData.text.lexer.RobotToken;
import org.robotframework.ide.core.testHelpers.ClassFieldCleaner;
import org.robotframework.ide.core.testHelpers.ClassFieldCleaner.ForClean;


/**
 * 
 * @author wypych
 * @since JDK 1.7 update 74
 * @version Robot Framework 2.9 alpha 2
 * 
 * @see AVariableRecognizer
 */
public class AVariableRecognizerTest {

    @ForClean
    private Dummy dummy;
    private static SimpleRobotContextType buildType = SimpleRobotContextType.EMPTY_LINE;
    private static IRobotTokenType recognizationChar = RobotSingleCharTokenType.SINGLE_DOT;


    @Test
    public void test_mergeAndAddInTail_emptyListToMerge() {
        // prepare
        OneLineSingleRobotContextPart toGetTokens = mock(OneLineSingleRobotContextPart.class);
        OneLineSingleRobotContextPart context = mock(OneLineSingleRobotContextPart.class);

        RobotToken tokenOne = mock(RobotToken.class);
        when(tokenOne.getType()).thenReturn(recognizationChar);
        RobotToken tokenTwo = mock(RobotToken.class);
        when(tokenTwo.getType()).thenReturn(
                RobotSingleCharTokenType.SINGLE_VARIABLE_BEGIN_CURLY_BRACKET);

        List<RobotToken> tokens = Arrays.asList();
        when(context.getContextTokens()).thenReturn(tokens);

        // execute
        dummy.mergeAndAddInTail(toGetTokens, context);

        // verify
        InOrder order = inOrder(toGetTokens, context);
        order.verify(context, times(1)).getContextTokens();
        order.verifyNoMoreInteractions();
    }


    @Test
    public void test_mergeAndAddInTail_twoElementListToMerge() {
        // prepare
        OneLineSingleRobotContextPart toGetTokens = mock(OneLineSingleRobotContextPart.class);
        OneLineSingleRobotContextPart context = mock(OneLineSingleRobotContextPart.class);

        RobotToken tokenOne = mock(RobotToken.class);
        when(tokenOne.getType()).thenReturn(recognizationChar);
        RobotToken tokenTwo = mock(RobotToken.class);
        when(tokenTwo.getType()).thenReturn(
                RobotSingleCharTokenType.SINGLE_VARIABLE_BEGIN_CURLY_BRACKET);

        List<RobotToken> tokens = Arrays.asList(tokenOne, tokenTwo);
        when(context.getContextTokens()).thenReturn(tokens);

        // execute
        dummy.mergeAndAddInTail(toGetTokens, context);

        // verify
        InOrder order = inOrder(toGetTokens, context, tokenOne, tokenTwo);
        order.verify(context, times(1)).getContextTokens();
        order.verify(toGetTokens, times(1)).addNextToken(tokenOne);
        order.verify(toGetTokens, times(1)).addNextToken(tokenTwo);
        order.verifyNoMoreInteractions();
    }


    @SuppressWarnings("unchecked")
    @Test
    public void test_containsBeginRecognizationTypeAndCurrlySigns_sizeEquals_3_bothCorrect() {
        // prepare
        OneLineSingleRobotContextPart context = mock(OneLineSingleRobotContextPart.class);
        RobotToken tokenOne = mock(RobotToken.class);
        when(tokenOne.getType()).thenReturn(recognizationChar);
        RobotToken tokenTwo = mock(RobotToken.class);
        when(tokenTwo.getType()).thenReturn(
                RobotSingleCharTokenType.SINGLE_VARIABLE_BEGIN_CURLY_BRACKET);

        @SuppressWarnings("rawtypes")
        List tokens = mock(List.class);
        when(tokens.isEmpty()).thenReturn(false);
        when(tokens.size()).thenReturn(3);
        when(tokens.get(0)).thenReturn(tokenOne);
        when(tokens.get(1)).thenReturn(tokenTwo);
        when(context.getContextTokens()).thenReturn(tokens);

        // execute
        boolean result = dummy
                .containsBeginRecognizationTypeAndCurrlySigns(context);

        // verify
        assertThat(result).isTrue();
    }


    @SuppressWarnings("unchecked")
    @Test
    public void test_containsBeginRecognizationTypeAndCurrlySigns_sizeEquals_3_butTokenTwoIsNotCorrect() {
        // prepare
        OneLineSingleRobotContextPart context = mock(OneLineSingleRobotContextPart.class);
        RobotToken tokenOne = mock(RobotToken.class);
        when(tokenOne.getType()).thenReturn(recognizationChar);
        RobotToken tokenTwo = mock(RobotToken.class);
        when(tokenTwo.getType()).thenReturn(
                RobotSingleCharTokenType.END_OF_LINE);

        @SuppressWarnings("rawtypes")
        List tokens = mock(List.class);
        when(tokens.isEmpty()).thenReturn(false);
        when(tokens.size()).thenReturn(3);
        when(tokens.get(0)).thenReturn(tokenOne);
        when(tokens.get(1)).thenReturn(tokenTwo);
        when(context.getContextTokens()).thenReturn(tokens);

        // execute
        boolean result = dummy
                .containsBeginRecognizationTypeAndCurrlySigns(context);

        // verify
        assertThat(result).isFalse();

        InOrder order = inOrder(context, tokens, tokenOne, tokenTwo);
        order.verify(context, times(1)).getContextTokens();
        order.verify(tokens, times(1)).size();
        order.verify(tokens, times(1)).get(0);
        order.verify(tokenOne, times(1)).getType();
        order.verify(tokens, times(1)).get(1);
        order.verify(tokenTwo, times(1)).getType();
        order.verifyNoMoreInteractions();
    }


    @SuppressWarnings("unchecked")
    @Test
    public void test_containsBeginRecognizationTypeAndCurrlySigns_sizeEquals_2_bothCorrect() {
        // prepare
        OneLineSingleRobotContextPart context = mock(OneLineSingleRobotContextPart.class);
        RobotToken tokenOne = mock(RobotToken.class);
        when(tokenOne.getType()).thenReturn(recognizationChar);
        RobotToken tokenTwo = mock(RobotToken.class);
        when(tokenTwo.getType()).thenReturn(
                RobotSingleCharTokenType.SINGLE_VARIABLE_BEGIN_CURLY_BRACKET);

        @SuppressWarnings("rawtypes")
        List tokens = mock(List.class);
        when(tokens.isEmpty()).thenReturn(false);
        when(tokens.size()).thenReturn(2);
        when(tokens.get(0)).thenReturn(tokenOne);
        when(tokens.get(1)).thenReturn(tokenTwo);
        when(context.getContextTokens()).thenReturn(tokens);

        // execute
        boolean result = dummy
                .containsBeginRecognizationTypeAndCurrlySigns(context);

        // verify
        assertThat(result).isTrue();
    }


    @SuppressWarnings("unchecked")
    @Test
    public void test_containsBeginRecognizationTypeAndCurrlySigns_sizeEquals_2_butTokenTwoIsNotCorrect() {
        // prepare
        OneLineSingleRobotContextPart context = mock(OneLineSingleRobotContextPart.class);
        RobotToken tokenOne = mock(RobotToken.class);
        when(tokenOne.getType()).thenReturn(recognizationChar);
        RobotToken tokenTwo = mock(RobotToken.class);
        when(tokenTwo.getType()).thenReturn(
                RobotSingleCharTokenType.END_OF_LINE);

        @SuppressWarnings("rawtypes")
        List tokens = mock(List.class);
        when(tokens.isEmpty()).thenReturn(false);
        when(tokens.size()).thenReturn(2);
        when(tokens.get(0)).thenReturn(tokenOne);
        when(tokens.get(1)).thenReturn(tokenTwo);
        when(context.getContextTokens()).thenReturn(tokens);

        // execute
        boolean result = dummy
                .containsBeginRecognizationTypeAndCurrlySigns(context);

        // verify
        assertThat(result).isFalse();

        InOrder order = inOrder(context, tokens, tokenOne, tokenTwo);
        order.verify(context, times(1)).getContextTokens();
        order.verify(tokens, times(1)).size();
        order.verify(tokens, times(1)).get(0);
        order.verify(tokenOne, times(1)).getType();
        order.verify(tokens, times(1)).get(1);
        order.verify(tokenTwo, times(1)).getType();
        order.verifyNoMoreInteractions();
    }


    @SuppressWarnings("unchecked")
    @Test
    public void test_containsBeginRecognizationTypeAndCurrlySigns_sizeLessThan2() {
        // prepare
        OneLineSingleRobotContextPart context = mock(OneLineSingleRobotContextPart.class);

        @SuppressWarnings("rawtypes")
        List tokens = mock(List.class);
        when(tokens.isEmpty()).thenReturn(false);
        when(tokens.size()).thenReturn(1);
        when(context.getContextTokens()).thenReturn(tokens);

        // execute
        boolean result = dummy
                .containsBeginRecognizationTypeAndCurrlySigns(context);

        // verify
        assertThat(result).isFalse();

        InOrder order = inOrder(context, tokens);
        order.verify(context, times(1)).getContextTokens();
        order.verify(tokens, times(1)).size();
        order.verifyNoMoreInteractions();
    }


    @SuppressWarnings("unchecked")
    @Test
    public void test_wasPreviousTokenOfBeginType_oneTokenAndInExpectedPositionAndType_shouldReturn_TRUE() {
        // prepare
        OneLineSingleRobotContextPart context = mock(OneLineSingleRobotContextPart.class);
        RobotToken token = mock(RobotToken.class);
        when(token.getType()).thenReturn(recognizationChar);

        @SuppressWarnings("rawtypes")
        List tokens = mock(List.class);
        when(tokens.isEmpty()).thenReturn(false);
        when(tokens.size()).thenReturn(1);
        when(tokens.get(0)).thenReturn(token);
        when(context.getContextTokens()).thenReturn(tokens);

        // execute
        boolean result = dummy.wasPreviousTokenOfBeginType(context);

        // verify
        assertThat(result).isTrue();
        InOrder order = inOrder(context, tokens, token);
        order.verify(context, times(1)).getContextTokens();
        order.verify(tokens, times(1)).isEmpty();
        order.verify(tokens, times(1)).size();
        order.verify(tokens, times(1)).get(0);
        order.verify(token, times(1)).getType();
        order.verifyNoMoreInteractions();
    }


    @SuppressWarnings("unchecked")
    @Test
    public void test_wasPreviousTokenOfBeginType_tokenDifferentFromExpectedType_shouldReturn_FALSE() {
        // prepare
        OneLineSingleRobotContextPart context = mock(OneLineSingleRobotContextPart.class);
        RobotToken token = mock(RobotToken.class);
        when(token.getType()).thenReturn(RobotSingleCharTokenType.END_OF_LINE);

        @SuppressWarnings("rawtypes")
        List tokens = mock(List.class);
        when(tokens.isEmpty()).thenReturn(false);
        when(tokens.size()).thenReturn(1);
        when(tokens.get(0)).thenReturn(token);
        when(context.getContextTokens()).thenReturn(tokens);

        // execute
        boolean result = dummy.wasPreviousTokenOfBeginType(context);

        // verify
        assertThat(result).isFalse();
        InOrder order = inOrder(context, tokens, token);
        order.verify(context, times(1)).getContextTokens();
        order.verify(tokens, times(1)).isEmpty();
        order.verify(tokens, times(1)).size();
        order.verify(tokens, times(1)).get(0);
        order.verify(token, times(1)).getType();
        order.verifyNoMoreInteractions();
    }


    @SuppressWarnings("unchecked")
    @Test
    public void test_wasPreviousTokenOfBeginType_emptyContext_shouldReturn_FALSE() {
        // prepare
        OneLineSingleRobotContextPart context = mock(OneLineSingleRobotContextPart.class);
        @SuppressWarnings("rawtypes")
        List tokens = mock(List.class);
        when(tokens.isEmpty()).thenReturn(true);
        when(context.getContextTokens()).thenReturn(tokens);

        // execute
        boolean result = dummy.wasPreviousTokenOfBeginType(context);

        // verify
        assertThat(result).isFalse();
        InOrder order = inOrder(context, tokens);
        order.verify(context, times(1)).getContextTokens();
        order.verify(tokens, times(1)).isEmpty();
        order.verifyNoMoreInteractions();
    }


    @Test
    public void test_createContext_shouldNotReturn_NULL() {
        // prepare
        LineTokenPosition lineInterval = mock(LineTokenPosition.class);
        when(lineInterval.getLineNumber()).thenReturn(1);

        // execute
        OneLineSingleRobotContextPart o = dummy.createContext(lineInterval);

        // verify
        assertThat(o).isNotNull();
        assertThat(o.getLineNumber()).isEqualTo(1);
    }


    @Test
    public void test_getContextType() {
        assertThat(dummy.getContextType()).isEqualTo(buildType);
    }

    public static class Dummy extends AVariableRecognizer {

        public Dummy() {
            super(buildType, recognizationChar);
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
