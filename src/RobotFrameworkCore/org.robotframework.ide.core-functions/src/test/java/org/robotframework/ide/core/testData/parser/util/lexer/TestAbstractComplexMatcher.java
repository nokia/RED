package org.robotframework.ide.core.testData.parser.util.lexer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import java.util.LinkedList;
import java.util.List;

import org.junit.After;
import org.junit.Test;
import org.mockito.InOrder;
import org.robotframework.ide.core.testData.parser.util.lexer.AbstractComplexMatcher.ExecuteState;
import org.robotframework.ide.core.testData.parser.util.lexer.MatchResult.MatchStatus;


/**
 * 
 * @author wypych
 * @see AbstractComplexMatcher
 */
public class TestAbstractComplexMatcher {

    private AbstractComplexMatcher complexMatcher;


    @Test
    public void test_twiceMatcherSaysMatchedNextStepIsToCONTINUE__shouldReturnMatchResult__withStatus_FOUND() {
        // prepare
        DummyComplexMatcher dummyMatcher = new DummyComplexMatcher();
        dummyMatcher.useOriginalJudgment = true;
        complexMatcher = spy(dummyMatcher);
        MatchResult matchResult = new MatchResult(dummyMatcher,
                MatchStatus.FOUND);
        Position position = new Position();
        DataMarked dataAndPos = new DataMarked(new byte[0], position);

        IMatcher alwaysSayMatched = mock(IMatcher.class);
        when(alwaysSayMatched.match(any(DataMarked.class))).thenReturn(
                matchResult);
        dummyMatcher.matchesInThisGroup.add(alwaysSayMatched);
        dummyMatcher.matchesInThisGroup.add(alwaysSayMatched);
        ExecuteState execState = spy(new ExecuteState(matchResult));

        // do not process twice
        execState.continueMatching();
        dummyMatcher.addNextIteration(execState, dataAndPos);
        dummyMatcher.addNextIteration(execState, dataAndPos);

        InOrder order = inOrder(complexMatcher, alwaysSayMatched, execState);
        // execute
        MatchResult result = complexMatcher.match(dataAndPos);

        // verify
        assertThat(result).isNotNull();
        assertThat(result.getMatcher()).isNotNull();
        assertThat(result.getMessages()).isEmpty();
        assertThat(result.getParent()).isNull();
        assertThat(result.getPosition()).isEqualTo(position);
        // we are testing what will be inside final result
        assertThat(result.getStatus()).isEqualTo(MatchStatus.NOT_FOUND);
        assertThat(result.getSubResults()).hasSize(2).contains(matchResult)
                .contains(matchResult);

        // verify order
        order.verify(complexMatcher, times(1)).match(dataAndPos);
        order.verify(alwaysSayMatched, times(1)).match(dataAndPos);
        order.verify(complexMatcher, times(1)).judgmentBaseOn(
                any(MatchResult.class), any(DataMarked.class));
        order.verify(alwaysSayMatched, times(1)).match(dataAndPos);
        order.verify(complexMatcher, times(1)).judgmentBaseOn(
                any(MatchResult.class), any(DataMarked.class));
        order.verify(complexMatcher, times(1)).finalJudgment(
                any(MatchResult.class), any(DataMarked.class));
        order.verifyNoMoreInteractions();
    }


    @Test
    public void test_twiceMatcherSaysMatchedButNextStepIsToBREAK__shouldReturnMatchResult__withStatus_FOUND() {
        // prepare
        DummyComplexMatcher dummyMatcher = new DummyComplexMatcher();
        dummyMatcher.useOriginalJudgment = true;
        complexMatcher = spy(dummyMatcher);
        MatchResult matchResult = new MatchResult(dummyMatcher,
                MatchStatus.FOUND);
        Position position = new Position();
        DataMarked dataAndPos = new DataMarked(new byte[0], position);

        IMatcher alwaysSayMatched = mock(IMatcher.class);
        when(alwaysSayMatched.match(any(DataMarked.class))).thenReturn(
                matchResult);
        dummyMatcher.matchesInThisGroup.add(alwaysSayMatched);
        ExecuteState execState = spy(new ExecuteState(matchResult));

        // do not process twice
        execState.breakMatching();
        dummyMatcher.addNextIteration(execState, dataAndPos);

        InOrder order = inOrder(complexMatcher, alwaysSayMatched, execState);
        // execute
        MatchResult result = complexMatcher.match(dataAndPos);

        // verify
        assertThat(result).isNotNull();
        assertThat(result.getMatcher()).isNotNull();
        assertThat(result.getMessages()).isEmpty();
        assertThat(result.getParent()).isNull();
        assertThat(result.getPosition()).isEqualTo(position);
        // we are testing what will be inside final result
        assertThat(result.getStatus()).isEqualTo(MatchStatus.NOT_FOUND);
        assertThat(result.getSubResults()).hasSize(1).contains(matchResult);

        // verify order
        order.verify(complexMatcher, times(1)).match(dataAndPos);
        order.verify(alwaysSayMatched, times(1)).match(dataAndPos);
        order.verify(complexMatcher, times(1)).judgmentBaseOn(
                any(MatchResult.class), any(DataMarked.class));
        order.verify(complexMatcher, times(1)).finalJudgment(
                any(MatchResult.class), any(DataMarked.class));
        order.verifyNoMoreInteractions();
    }


    @Test
    public void test_oneMatcherSaysMatched__shouldReturnMatchResult__withStatus_FOUND() {
        // prepare
        DummyComplexMatcher dummyMatcher = new DummyComplexMatcher();
        dummyMatcher.useOriginalJudgment = true;
        complexMatcher = spy(dummyMatcher);
        MatchResult matchResult = new MatchResult(dummyMatcher,
                MatchStatus.FOUND);
        Position position = new Position();
        DataMarked dataAndPos = new DataMarked(new byte[0], position);

        IMatcher alwaysSayMatched = mock(IMatcher.class);
        when(alwaysSayMatched.match(any(DataMarked.class))).thenReturn(
                matchResult);
        dummyMatcher.matchesInThisGroup.add(alwaysSayMatched);
        ExecuteState execState = spy(new ExecuteState(matchResult));

        execState.continueMatching();
        dummyMatcher.addNextIteration(execState, dataAndPos);

        InOrder order = inOrder(complexMatcher, alwaysSayMatched, execState);
        // execute
        MatchResult result = complexMatcher.match(dataAndPos);

        // verify
        assertThat(result).isNotNull();
        assertThat(result.getMatcher()).isNotNull();
        assertThat(result.getMessages()).isEmpty();
        assertThat(result.getParent()).isNull();
        assertThat(result.getPosition()).isEqualTo(position);
        // we are testing what will be inside final result
        assertThat(result.getStatus()).isEqualTo(MatchStatus.NOT_FOUND);
        assertThat(result.getSubResults()).hasSize(1).contains(matchResult);

        // verify order
        order.verify(complexMatcher, times(1)).match(dataAndPos);
        order.verify(alwaysSayMatched, times(1)).match(dataAndPos);
        order.verify(complexMatcher, times(1)).judgmentBaseOn(
                any(MatchResult.class), any(DataMarked.class));
        order.verify(complexMatcher, times(1)).finalJudgment(
                any(MatchResult.class), any(DataMarked.class));
        order.verifyNoMoreInteractions();
    }


    @Test
    public void test_noMatchers__shouldReturnMatchResult__withStatus_NOT_FOUND() {
        // prepare
        DummyComplexMatcher dummyMatcher = new DummyComplexMatcher();
        MatchResult result = new MatchResult(dummyMatcher,
                MatchStatus.NOT_FOUND);
        dummyMatcher.finalJudgment = result;

        complexMatcher = spy(dummyMatcher);

        Position position = spy(new Position());
        DataMarked dataWithPosition = spy(new DataMarked(new byte[0], position));

        InOrder order = inOrder(complexMatcher, position, dataWithPosition);

        // execute
        MatchResult execResult = complexMatcher.match(dataWithPosition);

        // verify
        assertThat(execResult).isEqualTo(result);

        // verify order
        order.verify(complexMatcher, times(1)).finalJudgment(
                any(MatchResult.class), any(DataMarked.class));
        order.verifyNoMoreInteractions();
    }

    private class DummyComplexMatcher extends AbstractComplexMatcher {

        private List<ExecuteState> execStates = new LinkedList<ExecuteState>();
        private List<DataMarked> dataMarked = new LinkedList<DataMarked>();
        private MatchResult finalJudgment = new MatchResult(this,
                MatchStatus.NOT_FOUND);
        private boolean useOriginalJudgment = false;


        public void addNextIteration(ExecuteState execState, DataMarked nextPos) {
            this.execStates.add(execState);
            this.dataMarked.add(nextPos);
        }


        @Override
        public ExecuteState judgmentBaseOn(MatchResult currentResults,
                DataMarked dataWithPosition) {

            return execStates.remove(0);
        }


        @Override
        public MatchResult finalJudgment(MatchResult allUpCurrentResults,
                DataMarked dataWithPosition) {
            MatchResult resultToReturn = finalJudgment;
            if (useOriginalJudgment) {
                resultToReturn = allUpCurrentResults;
            }

            return resultToReturn;
        }


        @Override
        public DataMarked nextPosition(MatchResult currentResults,
                DataMarked dataWithPosition) {

            return dataMarked.remove(0);
        }
    }


    @After
    public void tearDown() {
        complexMatcher = null;
    }
}
