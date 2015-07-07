package org.robotframework.ide.core.testData.text.context.recognizer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.internal.stubbing.answers.Returns;
import org.mockito.invocation.InvocationOnMock;
import org.robotframework.ide.core.testData.text.context.AggregatedOneLineRobotContexts;
import org.robotframework.ide.core.testData.text.context.OneLineSingleRobotContextPart;
import org.robotframework.ide.core.testData.text.context.recognizer.AVariableRecognizer.ContextElementComparator;
import org.robotframework.ide.core.testData.text.lexer.FilePosition;
import org.robotframework.ide.core.testData.text.lexer.RobotToken;
import org.robotframework.ide.core.testHelpers.AnswerRecorder;
import org.robotframework.ide.core.testHelpers.ClassFieldCleaner;
import org.robotframework.ide.core.testHelpers.ClassFieldCleaner.ForClean;


/**
 * 
 * @author wypych
 * @since JDK 1.7 update 74
 * @version Robot Framework 2.9 alpha 2
 * 
 * @see ContextElementComparator
 */
public class ContextElementComparatorTest {

    @ForClean
    private ContextElementComparator dummy;


    @SuppressWarnings("unchecked")
    @Test
    public void test_compare_logicBothParamAreNot_OneLineSingleContext() {
        // prepare
        AggregatedOneLineRobotContexts contextTwo = mock(AggregatedOneLineRobotContexts.class);
        AggregatedOneLineRobotContexts contextOne = mock(AggregatedOneLineRobotContexts.class);

        @SuppressWarnings("rawtypes")
        AnswerRecorder recorder = new AnswerRecorder(new Returns(0));

        // execute
        dummy = spy(new ContextElementComparator());
        doAnswer(recorder).when(dummy).compareFilePosition(
                any(FilePosition.class), any(FilePosition.class));
        int dummyResult = dummy.compare(contextOne, contextTwo);

        // verify
        assertThat(dummyResult).isEqualTo(0);

        List<InvocationOnMock> invocations = recorder.getInvocations();
        assertThat(invocations).hasSize(1);
        InvocationOnMock mockInvoke = invocations.get(0);
        assertThat(mockInvoke.getArguments()).hasSize(2);
        FilePosition argumentAtFirst = mockInvoke.getArgumentAt(0,
                FilePosition.class);
        assertThat(argumentAtFirst.getLine()).isEqualTo(-1);
        assertThat(argumentAtFirst.getColumn()).isEqualTo(-1);

        FilePosition argumentAtSecond = mockInvoke.getArgumentAt(1,
                FilePosition.class);
        assertThat(argumentAtSecond.getLine()).isEqualTo(-1);
        assertThat(argumentAtSecond.getColumn()).isEqualTo(-1);

        InOrder order = inOrder(dummy, contextOne, contextTwo);
        order.verify(dummy, times(1)).compare(contextOne, contextTwo);
        order.verify(dummy, times(1)).compareFilePosition(
                any(FilePosition.class), any(FilePosition.class));
        order.verifyNoMoreInteractions();

    }


    @SuppressWarnings("unchecked")
    @Test
    public void test_compare_logicTheSecondParamIsNot_OneLineSingleContext_theFirstIsOk() {
        // prepare
        AggregatedOneLineRobotContexts contextTwo = mock(AggregatedOneLineRobotContexts.class);

        OneLineSingleRobotContextPart contextOne = mock(OneLineSingleRobotContextPart.class);
        RobotToken tokenFromContextOne = mock(RobotToken.class);
        FilePosition posTokenFromContextOne = mock(FilePosition.class);
        when(tokenFromContextOne.getStartPosition()).thenReturn(
                posTokenFromContextOne);
        List<RobotToken> toksOne = mock(List.class);
        when(toksOne.get(0)).thenReturn(tokenFromContextOne);
        when(contextOne.getContextTokens()).thenReturn(toksOne);
        @SuppressWarnings("rawtypes")
        AnswerRecorder recorder = new AnswerRecorder(new Returns(0));

        // execute
        dummy = spy(new ContextElementComparator());
        doAnswer(recorder).when(dummy).compareFilePosition(
                any(FilePosition.class), any(FilePosition.class));
        int dummyResult = dummy.compare(contextOne, contextTwo);

        // verify
        assertThat(dummyResult).isEqualTo(0);

        List<InvocationOnMock> invocations = recorder.getInvocations();
        assertThat(invocations).hasSize(1);
        InvocationOnMock mockInvoke = invocations.get(0);
        assertThat(mockInvoke.getArguments()).hasSize(2);
        FilePosition argumentAtSecond = mockInvoke.getArgumentAt(1,
                FilePosition.class);
        assertThat(argumentAtSecond.getLine()).isEqualTo(-1);
        assertThat(argumentAtSecond.getColumn()).isEqualTo(-1);
        FilePosition argumentAtFirst = mockInvoke.getArgumentAt(0,
                FilePosition.class);
        assertThat(argumentAtFirst).isSameAs(posTokenFromContextOne);

        InOrder order = inOrder(dummy, contextOne, contextTwo, toksOne,
                tokenFromContextOne);
        order.verify(dummy, times(1)).compare(contextOne, contextTwo);
        order.verify(contextOne, times(1)).getContextTokens();
        order.verify(toksOne, times(1)).get(0);
        order.verify(tokenFromContextOne, times(1)).getStartPosition();
        order.verify(dummy, times(1)).compareFilePosition(
                any(FilePosition.class), any(FilePosition.class));
        order.verifyNoMoreInteractions();

    }


    @SuppressWarnings("unchecked")
    @Test
    public void test_compare_logicTheFirstParamIsNot_OneLineSingleContext_theSecondIsOk() {
        // prepare
        AggregatedOneLineRobotContexts contextOne = mock(AggregatedOneLineRobotContexts.class);

        OneLineSingleRobotContextPart contextTwo = mock(OneLineSingleRobotContextPart.class);
        RobotToken tokenFromContextTwo = mock(RobotToken.class);
        FilePosition posTokenFromContextTwo = mock(FilePosition.class);
        when(tokenFromContextTwo.getStartPosition()).thenReturn(
                posTokenFromContextTwo);
        List<RobotToken> toksTwo = mock(List.class);
        when(toksTwo.get(0)).thenReturn(tokenFromContextTwo);
        when(contextTwo.getContextTokens()).thenReturn(toksTwo);
        @SuppressWarnings("rawtypes")
        AnswerRecorder recorder = new AnswerRecorder(new Returns(0));

        // execute
        dummy = spy(new ContextElementComparator());
        doAnswer(recorder).when(dummy).compareFilePosition(
                any(FilePosition.class), any(FilePosition.class));
        int dummyResult = dummy.compare(contextOne, contextTwo);

        // verify
        assertThat(dummyResult).isEqualTo(0);

        List<InvocationOnMock> invocations = recorder.getInvocations();
        assertThat(invocations).hasSize(1);
        InvocationOnMock mockInvoke = invocations.get(0);
        assertThat(mockInvoke.getArguments()).hasSize(2);
        FilePosition argumentAtFirst = mockInvoke.getArgumentAt(0,
                FilePosition.class);
        assertThat(argumentAtFirst.getLine()).isEqualTo(-1);
        assertThat(argumentAtFirst.getColumn()).isEqualTo(-1);
        FilePosition argumentAtSecond = mockInvoke.getArgumentAt(1,
                FilePosition.class);
        assertThat(argumentAtSecond).isSameAs(posTokenFromContextTwo);

        InOrder order = inOrder(dummy, contextOne, contextTwo, toksTwo,
                tokenFromContextTwo);
        order.verify(dummy, times(1)).compare(contextOne, contextTwo);
        order.verify(contextTwo, times(1)).getContextTokens();
        order.verify(toksTwo, times(1)).get(0);
        order.verify(tokenFromContextTwo, times(1)).getStartPosition();
        order.verify(dummy, times(1)).compareFilePosition(
                any(FilePosition.class), any(FilePosition.class));
        order.verifyNoMoreInteractions();

    }


    @Test
    public void test_compare_logicCheckFor_theSameTypeOneLineSingleContext() {
        // prepare
        OneLineSingleRobotContextPart contextOne = mock(OneLineSingleRobotContextPart.class);
        RobotToken tokenFromContextOne = mock(RobotToken.class);
        FilePosition posTokenFromContextOne = mock(FilePosition.class);
        when(tokenFromContextOne.getStartPosition()).thenReturn(
                posTokenFromContextOne);
        @SuppressWarnings("unchecked")
        List<RobotToken> toksOne = mock(List.class);
        when(toksOne.get(0)).thenReturn(tokenFromContextOne);
        when(contextOne.getContextTokens()).thenReturn(toksOne);

        OneLineSingleRobotContextPart contextTwo = mock(OneLineSingleRobotContextPart.class);
        RobotToken tokenFromContextTwo = mock(RobotToken.class);
        FilePosition posTokenFromContextTwo = mock(FilePosition.class);
        when(tokenFromContextTwo.getStartPosition()).thenReturn(
                posTokenFromContextTwo);
        @SuppressWarnings("unchecked")
        List<RobotToken> toksTwo = mock(List.class);
        when(toksTwo.get(0)).thenReturn(tokenFromContextTwo);
        when(contextTwo.getContextTokens()).thenReturn(toksTwo);

        // execute
        dummy = spy(new ContextElementComparator());
        when(
                dummy.compareFilePosition(posTokenFromContextOne,
                        posTokenFromContextTwo)).thenReturn(0);
        int dummyResult = dummy.compare(contextOne, contextTwo);

        // verify
        assertThat(dummyResult).isEqualTo(0);
        InOrder order = inOrder(dummy, contextOne, toksOne,
                tokenFromContextOne, contextTwo, toksTwo, tokenFromContextTwo);
        order.verify(dummy, times(1)).compare(contextOne, contextTwo);
        order.verify(contextOne, times(1)).getContextTokens();
        order.verify(toksOne, times(1)).get(0);
        order.verify(tokenFromContextOne, times(1)).getStartPosition();
        order.verify(contextTwo, times(1)).getContextTokens();
        order.verify(toksTwo, times(1)).get(0);
        order.verify(tokenFromContextTwo, times(1)).getStartPosition();
        order.verify(dummy, times(1)).compareFilePosition(
                posTokenFromContextOne, posTokenFromContextTwo);
        order.verifyNoMoreInteractions();
    }


    @Test
    public void test_compareFilePosition_tokenOne_hasTheSameLineAndColumnLike_tokenSecond() {
        // prepare
        FilePosition o1 = mock(FilePosition.class);
        when(o1.getLine()).thenReturn(1);
        when(o1.getColumn()).thenReturn(1);

        FilePosition o2 = mock(FilePosition.class);
        when(o2.getLine()).thenReturn(1);
        when(o2.getColumn()).thenReturn(1);
        dummy = spy(dummy);

        // execute
        int i = dummy.compareFilePosition(o1, o2);

        // verify
        assertThat(i).isEqualTo(0);

        InOrder order = inOrder(dummy, o1, o2);
        order.verify(o1, times(1)).getLine();
        order.verify(o2, times(1)).getLine();
        order.verify(dummy, times(1)).compareInts(1, 1);
        order.verify(o1, times(1)).getColumn();
        order.verify(o2, times(1)).getColumn();
        order.verify(dummy, times(1)).compareInts(1, 1);
        order.verifyNoMoreInteractions();
    }


    @Test
    public void test_compareFilePosition_tokenOne_hasDifferentColumnThan_tokenSecond() {
        // prepare
        FilePosition o1 = mock(FilePosition.class);
        when(o1.getLine()).thenReturn(1);
        when(o1.getColumn()).thenReturn(2);

        FilePosition o2 = mock(FilePosition.class);
        when(o2.getLine()).thenReturn(1);
        when(o2.getColumn()).thenReturn(1);
        dummy = spy(dummy);

        // execute
        int i = dummy.compareFilePosition(o1, o2);

        // verify
        assertThat(i).isEqualTo(1);

        InOrder order = inOrder(dummy, o1, o2);
        order.verify(o1, times(1)).getLine();
        order.verify(o2, times(1)).getLine();
        order.verify(dummy, times(1)).compareInts(1, 1);
        order.verify(o1, times(1)).getColumn();
        order.verify(o2, times(1)).getColumn();
        order.verify(dummy, times(1)).compareInts(2, 1);
        order.verifyNoMoreInteractions();
    }


    @Test
    public void test_compareFilePosition_tokenOne_hasDifferentLineThan_tokenSecond() {
        // prepare
        FilePosition o1 = mock(FilePosition.class);
        when(o1.getLine()).thenReturn(1);
        when(o1.getColumn()).thenReturn(1);

        FilePosition o2 = mock(FilePosition.class);
        when(o2.getLine()).thenReturn(2);
        when(o2.getColumn()).thenReturn(1);
        dummy = spy(dummy);

        // execute
        int i = dummy.compareFilePosition(o1, o2);

        // verify
        assertThat(i).isEqualTo(-1);

        InOrder order = inOrder(dummy, o1, o2);
        order.verify(o1, times(1)).getLine();
        order.verify(o2, times(1)).getLine();
        order.verify(dummy, times(1)).compareInts(1, 2);
        order.verifyNoMoreInteractions();
    }


    @Test
    public void test_compareInts_bothTheSame() {
        // prepare
        int paramOne = 1;
        int paramTwo = 1;

        // execute
        int result = dummy.compareInts(paramOne, paramTwo);

        // verify
        assertThat(result).isEqualTo(Integer.compare(paramOne, paramTwo));
    }


    @Test
    public void test_compareInts_theFirstParamIsGreater() {
        // prepare
        int paramOne = 2;
        int paramTwo = 1;

        // execute
        int result = dummy.compareInts(paramOne, paramTwo);

        // verify
        assertThat(result).isEqualTo(Integer.compare(paramOne, paramTwo));
    }


    @Test
    public void test_compareInts_theSecondParamIsGreater() {
        // prepare
        int paramOne = 1;
        int paramTwo = 2;

        // execute
        int result = dummy.compareInts(paramOne, paramTwo);

        // verify
        assertThat(result).isEqualTo(Integer.compare(paramOne, paramTwo));
    }


    @Before
    public void setUp() {
        dummy = new ContextElementComparator();
    }


    @After
    public void tearDown() throws Exception {
        ClassFieldCleaner.init(this);
    }
}
