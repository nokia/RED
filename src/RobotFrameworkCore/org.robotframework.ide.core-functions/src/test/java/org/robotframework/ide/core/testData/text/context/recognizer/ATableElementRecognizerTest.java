package org.robotframework.ide.core.testData.text.context.recognizer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.Reader;
import java.io.StringReader;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robotframework.ide.core.testData.text.context.ContextBuilder.ContextOutput;
import org.robotframework.ide.core.testData.text.context.IContextElement;
import org.robotframework.ide.core.testData.text.context.IContextElementType;
import org.robotframework.ide.core.testData.text.context.OneLineSingleRobotContextPart;
import org.robotframework.ide.core.testData.text.context.TokensLineIterator.LineTokenPosition;
import org.robotframework.ide.core.testData.text.context.recognizer.ExpectedSequenceElement.PriorityType;
import org.robotframework.ide.core.testData.text.context.recognizer.keywordTable.KeywordTableRobotContextType;
import org.robotframework.ide.core.testData.text.lexer.IRobotTokenType;
import org.robotframework.ide.core.testData.text.lexer.RobotSingleCharTokenType;
import org.robotframework.ide.core.testData.text.lexer.RobotToken;
import org.robotframework.ide.core.testData.text.lexer.RobotWordType;
import org.robotframework.ide.core.testData.text.lexer.TxtRobotTestDataLexer;
import org.robotframework.ide.core.testData.text.lexer.helpers.ReadersProvider;
import org.robotframework.ide.core.testData.text.lexer.matcher.RobotTokenMatcher.TokenOutput;
import org.robotframework.ide.core.testHelpers.ClassFieldCleaner;
import org.robotframework.ide.core.testHelpers.ClassFieldCleaner.ForClean;


/**
 * @author wypych
 * @since JDK 1.7 update 74
 * @version Robot Framework 2.9 alpha 2
 * 
 * @see ATableElementRecognizer
 */
public class ATableElementRecognizerTest {

    @ForClean
    @Mock
    protected ReadersProvider readersProvider;

    @ForClean
    @Mock
    protected Reader reader;

    @ForClean
    @Mock
    protected File file;

    @ForClean
    private ATableElementRecognizer contextRecognizer;
    private final IContextElementType buildType = KeywordTableRobotContextType.TABLE_KEYWORD_SETTINGS_RETURN;
    private final ExpectedSequenceElement elementExpectedOne = spy(ExpectedSequenceElement
            .buildMandatory(RobotSingleCharTokenType.SINGLE_DOT));
    private final ExpectedSequenceElement elementExpectedTwo = spy(ExpectedSequenceElement
            .buildMandatory(RobotWordType.DEFAULT_WORD));
    private final ExpectedSequenceElement elementExpectedThreeOptional = spy(ExpectedSequenceElement
            .buildOptional(RobotSingleCharTokenType.SINGLE_COLON));

    private final List<ExpectedSequenceElement> expectedSequence = createSpiedList(Arrays
            .asList(elementExpectedOne, elementExpectedTwo,
                    elementExpectedThreeOptional, elementExpectedOne));


    @Test
    public void test_wasAllMandatoryFound_listWithTwoElement_positionIsOne_OptionalAndOptional_shouldReturn_TRUE() {
        // prepare
        List<ExpectedSequenceElement> elems = spy(new LinkedList<ExpectedSequenceElement>());
        ExpectedSequenceElement elemOne = mock(ExpectedSequenceElement.class);
        when(elemOne.getPriority()).thenReturn(PriorityType.OPTIONAL);
        ExpectedSequenceElement elemTwo = mock(ExpectedSequenceElement.class);
        when(elemTwo.getPriority()).thenReturn(PriorityType.OPTIONAL);
        elems.add(elemOne);
        elems.add(elemTwo);

        // execute
        boolean result = contextRecognizer.wasAllMandatoryFound(elems, 1);

        // verify
        assertThat(result).isTrue();

        InOrder order = inOrder(elems, elemOne, elemTwo);
        order.verify(elems, times(1)).size();
        order.verify(elems, times(1)).get(1);
        order.verify(elemTwo, times(1)).getPriority();
        order.verifyNoMoreInteractions();
    }


    @Test
    public void test_wasAllMandatoryFound_listWithTwoElement_positionIsZero_OptionalAndOptional_shouldReturn_TRUE() {
        // prepare
        List<ExpectedSequenceElement> elems = spy(new LinkedList<ExpectedSequenceElement>());
        ExpectedSequenceElement elemOne = mock(ExpectedSequenceElement.class);
        when(elemOne.getPriority()).thenReturn(PriorityType.OPTIONAL);
        ExpectedSequenceElement elemTwo = mock(ExpectedSequenceElement.class);
        when(elemTwo.getPriority()).thenReturn(PriorityType.OPTIONAL);
        elems.add(elemOne);
        elems.add(elemTwo);

        // execute
        boolean result = contextRecognizer.wasAllMandatoryFound(elems, 0);

        // verify
        assertThat(result).isTrue();

        InOrder order = inOrder(elems, elemOne, elemTwo);
        order.verify(elems, times(1)).size();
        order.verify(elems, times(1)).get(0);
        order.verify(elemOne, times(1)).getPriority();
        order.verify(elems, times(1)).get(1);
        order.verify(elemTwo, times(1)).getPriority();
        order.verifyNoMoreInteractions();
    }


    @Test
    public void test_wasAllMandatoryFound_listWithTwoElement_positionIsOne_MandatoryAndOptional_shouldReturn_TRUE() {
        // prepare
        List<ExpectedSequenceElement> elems = spy(new LinkedList<ExpectedSequenceElement>());
        ExpectedSequenceElement elemOne = mock(ExpectedSequenceElement.class);
        when(elemOne.getPriority()).thenReturn(PriorityType.MANDATORY);
        ExpectedSequenceElement elemTwo = mock(ExpectedSequenceElement.class);
        when(elemTwo.getPriority()).thenReturn(PriorityType.OPTIONAL);
        elems.add(elemOne);
        elems.add(elemTwo);

        // execute
        boolean result = contextRecognizer.wasAllMandatoryFound(elems, 1);

        // verify
        assertThat(result).isTrue();

        InOrder order = inOrder(elems, elemOne, elemTwo);
        order.verify(elems, times(1)).size();
        order.verify(elems, times(1)).get(1);
        order.verify(elemTwo, times(1)).getPriority();
        order.verifyNoMoreInteractions();
    }


    @Test
    public void test_wasAllMandatoryFound_listWithTwoElement_positionIsZero_MandatoryAndOptional_shouldReturn_FALSE() {
        // prepare
        List<ExpectedSequenceElement> elems = spy(new LinkedList<ExpectedSequenceElement>());
        ExpectedSequenceElement elemOne = mock(ExpectedSequenceElement.class);
        when(elemOne.getPriority()).thenReturn(PriorityType.MANDATORY);
        ExpectedSequenceElement elemTwo = mock(ExpectedSequenceElement.class);
        when(elemTwo.getPriority()).thenReturn(PriorityType.OPTIONAL);
        elems.add(elemOne);
        elems.add(elemTwo);

        // execute
        boolean result = contextRecognizer.wasAllMandatoryFound(elems, 0);

        // verify
        assertThat(result).isFalse();

        InOrder order = inOrder(elems, elemOne, elemTwo);
        order.verify(elems, times(1)).size();
        order.verify(elems, times(1)).get(0);
        order.verify(elemOne, times(1)).getPriority();
        order.verifyNoMoreInteractions();
    }


    @Test
    public void test_wasAllMandatoryFound_listWithTwoElement_positionIsOne_OptionalAndMandatory_shouldReturn_FALSE() {
        // prepare
        List<ExpectedSequenceElement> elems = spy(new LinkedList<ExpectedSequenceElement>());
        ExpectedSequenceElement elemOne = mock(ExpectedSequenceElement.class);
        when(elemOne.getPriority()).thenReturn(PriorityType.OPTIONAL);
        ExpectedSequenceElement elemTwo = mock(ExpectedSequenceElement.class);
        when(elemTwo.getPriority()).thenReturn(PriorityType.MANDATORY);
        elems.add(elemOne);
        elems.add(elemTwo);

        // execute
        boolean result = contextRecognizer.wasAllMandatoryFound(elems, 1);

        // verify
        assertThat(result).isFalse();

        InOrder order = inOrder(elems, elemOne, elemTwo);
        order.verify(elems, times(1)).size();
        order.verify(elems, times(1)).get(1);
        order.verify(elemTwo, times(1)).getPriority();
        order.verifyNoMoreInteractions();
    }


    @Test
    public void test_wasAllMandatoryFound_listWithTwoElement_positionIsZero_OptionalAndMandatory_shouldReturn_FALSE() {
        // prepare
        List<ExpectedSequenceElement> elems = spy(new LinkedList<ExpectedSequenceElement>());
        ExpectedSequenceElement elemOne = mock(ExpectedSequenceElement.class);
        when(elemOne.getPriority()).thenReturn(PriorityType.OPTIONAL);
        ExpectedSequenceElement elemTwo = mock(ExpectedSequenceElement.class);
        when(elemTwo.getPriority()).thenReturn(PriorityType.MANDATORY);
        elems.add(elemOne);
        elems.add(elemTwo);

        // execute
        boolean result = contextRecognizer.wasAllMandatoryFound(elems, 0);

        // verify
        assertThat(result).isFalse();

        InOrder order = inOrder(elems, elemOne, elemTwo);
        order.verify(elems, times(1)).size();
        order.verify(elems, times(1)).get(0);
        order.verify(elemOne, times(1)).getPriority();
        order.verify(elems, times(1)).get(1);
        order.verify(elemTwo, times(1)).getPriority();
        order.verifyNoMoreInteractions();
    }


    @Test
    public void test_wasAllMandatoryFound_listWithOneElement_positionIsZero_PriorityIsMandatory_shouldReturn_FALSE() {
        // prepare
        List<ExpectedSequenceElement> elems = spy(new LinkedList<ExpectedSequenceElement>());
        ExpectedSequenceElement elemOne = mock(ExpectedSequenceElement.class);
        when(elemOne.getPriority()).thenReturn(PriorityType.MANDATORY);
        elems.add(elemOne);

        // execute
        boolean result = contextRecognizer.wasAllMandatoryFound(elems, 0);

        // verify
        assertThat(result).isFalse();

        InOrder order = inOrder(elems, elemOne);
        order.verify(elems, times(1)).size();
        order.verify(elems, times(1)).get(0);
        order.verify(elemOne, times(1)).getPriority();
        order.verifyNoMoreInteractions();
    }


    @Test
    public void test_wasAllMandatoryFound_listWithOneElement_positionIsZero_PriorityIsOptional_shouldReturn_TRUE() {
        // prepare
        List<ExpectedSequenceElement> elems = spy(new LinkedList<ExpectedSequenceElement>());
        ExpectedSequenceElement elemOne = mock(ExpectedSequenceElement.class);
        when(elemOne.getPriority()).thenReturn(PriorityType.OPTIONAL);
        elems.add(elemOne);

        // execute
        boolean result = contextRecognizer.wasAllMandatoryFound(elems, 0);

        // verify
        assertThat(result).isTrue();

        InOrder order = inOrder(elems, elemOne);
        order.verify(elems, times(1)).size();
        order.verify(elems, times(1)).get(0);
        order.verify(elemOne, times(1)).getPriority();
        order.verifyNoMoreInteractions();
    }


    @Test
    public void test_wasAllMandatoryFound_listWithOneElement_positionIsMinusOne_shouldReturn_TRUE() {
        // prepare
        List<ExpectedSequenceElement> elems = spy(new LinkedList<ExpectedSequenceElement>());
        ExpectedSequenceElement elemOne = mock(ExpectedSequenceElement.class);
        elems.add(elemOne);

        // execute
        boolean result = contextRecognizer.wasAllMandatoryFound(elems, -1);

        // verify
        assertThat(result).isTrue();

        InOrder order = inOrder(elems, elemOne);
        order.verify(elems, times(1)).size();
        order.verifyNoMoreInteractions();
    }


    @Test
    public void test_wasAllMandatoryFound_listWithOneElement_positionIsTwo_shouldReturn_TRUE() {
        // prepare
        List<ExpectedSequenceElement> elems = spy(new LinkedList<ExpectedSequenceElement>());
        ExpectedSequenceElement elemOne = mock(ExpectedSequenceElement.class);
        elems.add(elemOne);

        // execute
        boolean result = contextRecognizer.wasAllMandatoryFound(elems, 2);

        // verify
        assertThat(result).isTrue();

        InOrder order = inOrder(elems, elemOne);
        order.verify(elems, times(1)).size();
        order.verifyNoMoreInteractions();
    }


    @Test
    public void test_wasAllMandatoryFound_emptyList_positionIsMinusOne_shouldReturn_TRUE() {
        // prepare
        List<ExpectedSequenceElement> elems = spy(new LinkedList<ExpectedSequenceElement>());
        ExpectedSequenceElement elemOne = mock(ExpectedSequenceElement.class);
        elems.add(elemOne);
        doReturn(0).when(elems).size();

        // execute
        boolean result = contextRecognizer.wasAllMandatoryFound(elems, -1);

        // verify
        assertThat(result).isTrue();

        InOrder order = inOrder(elems, elemOne);
        order.verify(elems, times(1)).size();
        order.verifyNoMoreInteractions();
    }


    @Test
    public void test_recognize_onlyTheFirst_SecondAndFourTokenIsPresent_shouldReturn_listWithOneContext()
            throws Exception {
        // prepare
        contextRecognizer = spy(new DummyWithModifiableList());

        LineTokenPosition lineInterval = mock(LineTokenPosition.class);
        when(lineInterval.getStart()).thenReturn(0);
        when(lineInterval.getEnd()).thenReturn(3);
        OneLineSingleRobotContextPart context = spy(new OneLineSingleRobotContextPart(
                1));

        TokenOutput tokenOutput = createInputForContext(".default.");
        tokenOutput = spiedWithSpiesInside(tokenOutput);

        List<RobotToken> tokens = tokenOutput.getTokens();
        RobotToken theFirstToken = tokens.get(0);
        RobotToken theSecondToken = tokens.get(1);
        RobotToken theThirdToken = tokens.get(2);

        doReturn(context).when(contextRecognizer).createContext(lineInterval);
        ContextOutput currentContext = mock(ContextOutput.class);
        when(currentContext.getTokenizedContent()).thenReturn(tokenOutput);

        // execute
        List<IContextElement> foundContexts = contextRecognizer.recognize(
                currentContext, lineInterval);

        // verify
        assertThat(foundContexts).hasSize(1);

        InOrder order = inOrder(contextRecognizer, currentContext, tokenOutput,
                tokens, lineInterval, theFirstToken, elementExpectedOne,
                context, theSecondToken, elementExpectedTwo, theThirdToken,
                expectedSequence, elementExpectedThreeOptional);

        order.verify(contextRecognizer, times(1)).recognize(currentContext,
                lineInterval);
        order.verify(contextRecognizer, times(1)).createContext(lineInterval);
        order.verify(currentContext, times(1)).getTokenizedContent();
        order.verify(tokenOutput, times(1)).getTokens();
        order.verify(expectedSequence, times(1)).get(0);

        // for loop begin
        order.verify(lineInterval, times(1)).getStart();
        order.verify(lineInterval, times(1)).getEnd();
        order.verify(tokens, times(1)).get(0);
        order.verify(theFirstToken, times(1)).getType();
        order.verify(elementExpectedOne, times(1)).getType();
        order.verify(context, times(1)).addNextToken(theFirstToken);
        order.verify(expectedSequence, times(1)).get(1);

        // // second iteration
        order.verify(lineInterval, times(1)).getEnd();
        order.verify(tokens, times(1)).get(1);
        order.verify(theSecondToken, times(1)).getType();
        order.verify(elementExpectedTwo, times(1)).getType();
        order.verify(context, times(1)).addNextToken(theSecondToken);
        order.verify(expectedSequence, times(1)).get(2);

        // // third iteration optional is not present
        order.verify(lineInterval, times(1)).getEnd();
        order.verify(tokens, times(1)).get(2);
        order.verify(theThirdToken, times(1)).getType();
        order.verify(elementExpectedThreeOptional, times(1)).getType();
        order.verify(elementExpectedThreeOptional, times(1)).getPriority();
        order.verify(expectedSequence, times(1)).get(3);
        // // re-check
        order.verify(lineInterval, times(1)).getEnd();
        order.verify(tokens, times(1)).get(2);
        order.verify(theThirdToken, times(1)).getType();
        order.verify(elementExpectedOne, times(1)).getType();
        order.verify(context, times(1)).addNextToken(theThirdToken);
        order.verify(context, times(1)).setType(buildType);
        order.verify(contextRecognizer, times(1)).createContext(lineInterval);

        // last check for ends iteration
        order.verify(lineInterval, times(1)).getEnd();
        // was mandatory all found check
        order.verify(contextRecognizer, times(1)).wasAllMandatoryFound(
                expectedSequence, 0);
        order.verify(expectedSequence, times(1)).size();
        order.verify(expectedSequence, times(1)).get(0);
        order.verify(elementExpectedOne, times(1)).getPriority();

        order.verifyNoMoreInteractions();
    }


    @Test
    public void test_recognize_onlyTheFirstAndSecondTokenIsPresent_shouldReturn_anEmptyList()
            throws Exception {
        // prepare
        contextRecognizer = spy(new DummyWithModifiableList());

        LineTokenPosition lineInterval = mock(LineTokenPosition.class);
        when(lineInterval.getStart()).thenReturn(0);
        when(lineInterval.getEnd()).thenReturn(2);
        OneLineSingleRobotContextPart context = spy(new OneLineSingleRobotContextPart(
                1));

        TokenOutput tokenOutput = createInputForContext(".default");
        tokenOutput = spiedWithSpiesInside(tokenOutput);

        List<RobotToken> tokens = tokenOutput.getTokens();
        RobotToken theFirstToken = tokens.get(0);
        RobotToken theSecondToken = tokens.get(1);

        doReturn(context).when(contextRecognizer).createContext(lineInterval);
        ContextOutput currentContext = mock(ContextOutput.class);
        when(currentContext.getTokenizedContent()).thenReturn(tokenOutput);

        // execute
        List<IContextElement> foundContexts = contextRecognizer.recognize(
                currentContext, lineInterval);

        // verify
        assertThat(foundContexts).isEmpty();

        InOrder order = inOrder(contextRecognizer, currentContext, tokenOutput,
                tokens, lineInterval, theFirstToken, elementExpectedOne,
                context, theSecondToken, elementExpectedTwo, expectedSequence);

        order.verify(contextRecognizer, times(1)).recognize(currentContext,
                lineInterval);
        order.verify(contextRecognizer, times(1)).createContext(lineInterval);
        order.verify(currentContext, times(1)).getTokenizedContent();
        order.verify(tokenOutput, times(1)).getTokens();
        order.verify(expectedSequence, times(1)).get(0);

        // for loop begin
        order.verify(lineInterval, times(1)).getStart();
        order.verify(lineInterval, times(1)).getEnd();
        order.verify(tokens, times(1)).get(0);
        order.verify(theFirstToken, times(1)).getType();
        order.verify(elementExpectedOne, times(1)).getType();
        order.verify(context, times(1)).addNextToken(theFirstToken);
        order.verify(expectedSequence, times(1)).get(1);

        // // second iteration
        order.verify(lineInterval, times(1)).getEnd();
        order.verify(tokens, times(1)).get(1);
        order.verify(theSecondToken, times(1)).getType();
        order.verify(elementExpectedTwo, times(1)).getType();
        order.verify(context, times(1)).addNextToken(theSecondToken);
        order.verify(expectedSequence, times(1)).get(2);

        // last check for ends iteration
        order.verify(lineInterval, times(1)).getEnd();
        // was mandatory all found check
        order.verify(contextRecognizer, times(1)).wasAllMandatoryFound(
                expectedSequence, 2);

        order.verify(expectedSequence, times(1)).size();
        order.verify(expectedSequence, times(1)).get(3);
        order.verify(elementExpectedOne, times(1)).getPriority();

        order.verifyNoMoreInteractions();
    }


    @Test
    public void test_recognize_noExpectedTokensArePresent_shouldReturn_anEmptyList()
            throws Exception {
        // prepare
        contextRecognizer = spy(new DummyWithModifiableList());

        LineTokenPosition lineInterval = mock(LineTokenPosition.class);
        when(lineInterval.getStart()).thenReturn(0);
        when(lineInterval.getEnd()).thenReturn(1);
        OneLineSingleRobotContextPart context = spy(new OneLineSingleRobotContextPart(
                1));

        TokenOutput tokenOutput = createInputForContext("foobar");
        tokenOutput = spiedWithSpiesInside(tokenOutput);

        List<RobotToken> tokens = tokenOutput.getTokens();
        RobotToken theFirstToken = tokens.get(0);

        doReturn(context).when(contextRecognizer).createContext(lineInterval);
        ContextOutput currentContext = mock(ContextOutput.class);
        when(currentContext.getTokenizedContent()).thenReturn(tokenOutput);

        // execute
        List<IContextElement> foundContexts = contextRecognizer.recognize(
                currentContext, lineInterval);

        // verify
        assertThat(foundContexts).isEmpty();

        InOrder order = inOrder(contextRecognizer, currentContext, tokenOutput,
                tokens, lineInterval, theFirstToken, elementExpectedOne,
                context, expectedSequence);

        order.verify(contextRecognizer, times(1)).recognize(currentContext,
                lineInterval);
        order.verify(contextRecognizer, times(1)).createContext(lineInterval);
        order.verify(currentContext, times(1)).getTokenizedContent();
        order.verify(tokenOutput, times(1)).getTokens();
        // for content check
        order.verify(expectedSequence, times(1)).get(0);
        order.verify(lineInterval, times(1)).getStart();
        order.verify(lineInterval, times(1)).getEnd();
        order.verify(tokens, times(1)).get(0);
        order.verify(theFirstToken, times(1)).getType();
        order.verify(elementExpectedOne, times(1)).getType();
        order.verify(elementExpectedOne, times(1)).getPriority();
        order.verify(context, times(1)).removeAllContextTokens();

        // next iteration
        order.verify(lineInterval, times(1)).getEnd();
        order.verify(tokens, times(1)).get(0);
        order.verify(theFirstToken, times(1)).getType();
        order.verify(elementExpectedOne, times(1)).getType();
        order.verify(elementExpectedOne, times(1)).getPriority();
        order.verify(context, times(1)).removeAllContextTokens();

        // last check for ends iteration
        order.verify(lineInterval, times(1)).getEnd();
        // was mandatory all found check
        order.verify(contextRecognizer, times(1)).wasAllMandatoryFound(
                expectedSequence, 0);

        order.verify(expectedSequence, times(1)).size();
        order.verify(expectedSequence, times(1)).get(0);
        order.verify(elementExpectedOne, times(1)).getPriority();

        order.verifyNoMoreInteractions();
    }


    @Test
    public void test_recognize_onlyTheFirstTokenIsPresent_shouldReturn_anEmptyList()
            throws Exception {
        // prepare
        contextRecognizer = spy(new DummyWithModifiableList());

        LineTokenPosition lineInterval = mock(LineTokenPosition.class);
        when(lineInterval.getStart()).thenReturn(0);
        when(lineInterval.getEnd()).thenReturn(1);
        OneLineSingleRobotContextPart context = spy(new OneLineSingleRobotContextPart(
                1));

        TokenOutput tokenOutput = createInputForContext(".");
        tokenOutput = spiedWithSpiesInside(tokenOutput);

        List<RobotToken> tokens = tokenOutput.getTokens();
        RobotToken theFirstToken = tokens.get(0);

        doReturn(context).when(contextRecognizer).createContext(lineInterval);
        ContextOutput currentContext = mock(ContextOutput.class);
        when(currentContext.getTokenizedContent()).thenReturn(tokenOutput);

        // execute
        List<IContextElement> foundContexts = contextRecognizer.recognize(
                currentContext, lineInterval);

        // verify
        assertThat(foundContexts).isEmpty();

        InOrder order = inOrder(contextRecognizer, currentContext, tokenOutput,
                tokens, lineInterval, theFirstToken, elementExpectedOne,
                context, expectedSequence, elementExpectedTwo);

        order.verify(contextRecognizer, times(1)).recognize(currentContext,
                lineInterval);
        order.verify(contextRecognizer, times(1)).createContext(lineInterval);
        order.verify(currentContext, times(1)).getTokenizedContent();
        order.verify(tokenOutput, times(1)).getTokens();
        order.verify(expectedSequence, times(1)).get(0);
        order.verify(lineInterval, times(1)).getStart();
        order.verify(lineInterval, times(1)).getEnd();
        order.verify(tokens, times(1)).get(0);
        order.verify(theFirstToken, times(1)).getType();
        order.verify(elementExpectedOne, times(1)).getType();
        order.verify(context, times(1)).addNextToken(theFirstToken);
        order.verify(expectedSequence, times(1)).get(1);

        // last check for ends iteration
        order.verify(lineInterval, times(1)).getEnd();
        // was mandatory all found check
        order.verify(contextRecognizer, times(1)).wasAllMandatoryFound(
                expectedSequence, 1);

        order.verify(expectedSequence, times(1)).size();
        order.verify(expectedSequence, times(1)).get(1);
        order.verify(elementExpectedTwo, times(1)).getPriority();

        order.verifyNoMoreInteractions();
    }


    private static <E> List<E> createSpiedList(List<E> k) {
        List<E> l = new LinkedList<>();
        for (E e : k) {
            l.add(e);
        }
        l = spy(l);
        return l;
    }


    private TokenOutput spiedWithSpiesInside(final TokenOutput tokenOutput) {
        TokenOutput t = spy(new TokenOutput());
        List<RobotToken> tokens = tokenOutput.getTokens();
        tokens = spy(tokens);
        doReturn(tokens).when(t).getTokens();
        Object[] array = new Object[tokens.size()];
        for (int i = 0; i < tokens.size(); i++) {
            RobotToken mocked = spy(tokens.get(i));
            doReturn(mocked).when(tokens).get(i);
            array[i] = mocked;
        }

        doReturn(array).when(tokens).toArray();

        return t;
    }


    private TokenOutput createInputForContext(String text) throws Exception {
        when(readersProvider.create(file)).thenReturn(new StringReader(text));
        doCallRealMethod().when(readersProvider).newCharBuffer(anyInt());
        TxtRobotTestDataLexer lexer = new TxtRobotTestDataLexer(readersProvider);
        TokenOutput tokenOutput = lexer.extractTokens(file);
        return tokenOutput;
    }


    @Test(expected = UnsupportedOperationException.class)
    public void test_getExpectedElements_unmodifiableCheck() {
        contextRecognizer.getExpectedElements().add(elementExpectedOne);
    }


    @Test
    public void test_getExpectedElements_contentCheck() {
        assertThat(contextRecognizer.getExpectedElements())
                .containsExactlyElementsOf(expectedSequence);
    }


    @Test
    public void test_createExpectedInsideSquareBrackets_withThreeElements() {
        // prepare
        IRobotTokenType[] types = new IRobotTokenType[] {
                RobotSingleCharTokenType.SINGLE_ASTERISK,
                RobotSingleCharTokenType.SINGLE_EQUAL };

        // execute
        List<ExpectedSequenceElement> elems = ATableElementRecognizer
                .createExpectedInsideSquareBrackets(types);

        // verify
        assertThat(elems).hasSize(types.length + 2);
        assertExpectedSequenceElement(
                new ExpectedSequenceElement(
                        RobotSingleCharTokenType.SINGLE_POSITION_INDEX_BEGIN_SQUARE_BRACKET,
                        PriorityType.MANDATORY), elems.get(0));
        assertExpectedSequenceElement(new ExpectedSequenceElement(types[0],
                PriorityType.MANDATORY), elems.get(1));
        assertExpectedSequenceElement(new ExpectedSequenceElement(types[1],
                PriorityType.MANDATORY), elems.get(2));
        assertExpectedSequenceElement(
                new ExpectedSequenceElement(
                        RobotSingleCharTokenType.SINGLE_POSITION_INDEX_END_SQUARE_BRACKET,
                        PriorityType.MANDATORY), elems.get(3));
    }


    @Test
    public void test_createExpectedInsideSquareBrackets_emptyList() {
        // execute
        List<ExpectedSequenceElement> elems = ATableElementRecognizer
                .createExpectedInsideSquareBrackets(new IRobotTokenType[0]);

        // verify
        assertThat(elems).isEmpty();
    }


    @Test
    public void test_createExpectedWithOptionalColonAsLast_withThreeElements() {
        // prepare
        IRobotTokenType[] types = new IRobotTokenType[] {
                RobotSingleCharTokenType.SINGLE_ASTERISK,
                RobotSingleCharTokenType.SINGLE_EQUAL };

        // execute
        List<ExpectedSequenceElement> elems = ATableElementRecognizer
                .createExpectedWithOptionalColonAsLast(types);

        // verify
        assertThat(elems).hasSize(types.length + 1);
        assertExpectedSequenceElement(new ExpectedSequenceElement(types[0],
                PriorityType.MANDATORY), elems.get(0));
        assertExpectedSequenceElement(new ExpectedSequenceElement(types[1],
                PriorityType.MANDATORY), elems.get(1));
        assertExpectedSequenceElement(new ExpectedSequenceElement(
                RobotSingleCharTokenType.SINGLE_COLON, PriorityType.OPTIONAL),
                elems.get(2));
    }


    @Test
    public void test_createExpectedWithOptionalColonAsLast_emptyList() {
        // execute
        List<ExpectedSequenceElement> elems = ATableElementRecognizer
                .createExpectedWithOptionalColonAsLast(new IRobotTokenType[0]);

        // verify
        assertThat(elems).isEmpty();
    }


    private void assertExpectedSequenceElement(
            final ExpectedSequenceElement expected,
            final ExpectedSequenceElement got) {
        assertThat(got.getPriority()).isEqualTo(expected.getPriority());
        assertThat(got.getType()).isEqualTo(expected.getType());
    }


    @Test
    public void test_getContextType() {
        assertThat(contextRecognizer.getContextType()).isEqualTo(buildType);
    }


    @Test
    public void test_createContext() {
        // prepare
        LineTokenPosition ltp = mock(LineTokenPosition.class);
        when(ltp.getLineNumber()).thenReturn(2);

        // execute
        OneLineSingleRobotContextPart p = contextRecognizer.createContext(ltp);

        // verify
        assertThat(p).isNotNull();
        assertThat(p.getLineNumber()).isEqualTo(2);

        InOrder order = inOrder(ltp);
        order.verify(ltp, times(1)).getLineNumber();
        order.verifyNoMoreInteractions();
    }


    @After
    public void tearDown() throws Exception {
        ClassFieldCleaner.init(this);
    }


    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        contextRecognizer = spy(new Dummy());
    }

    private class Dummy extends ATableElementRecognizer {

        public Dummy() {
            super(buildType, expectedSequence);
        }
    }

    private class DummyWithModifiableList extends ATableElementRecognizer {

        public DummyWithModifiableList() {
            super(expectedSequence, buildType);
        }
    }
}
