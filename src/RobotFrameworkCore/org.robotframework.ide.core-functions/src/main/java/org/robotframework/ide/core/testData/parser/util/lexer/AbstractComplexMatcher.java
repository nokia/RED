package org.robotframework.ide.core.testData.parser.util.lexer;

import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.parser.util.lexer.MatchResult.MatchStatus;


/**
 * Base class for more complex matchers (i.e. AND, OR operations), with
 * backwards moving possibility.
 * 
 * @author wypych
 * @serial RobotFramework 2.8.6
 * @serial 1.0
 */
public abstract class AbstractComplexMatcher implements IMatcher {

    /**
     * sub matchers used to give final judgment about result
     */
    protected final List<IMatcher> matchesInThisGroup = new LinkedList<IMatcher>();


    /**
     * says base on current results if matching should be still continue and
     * about current result of them
     * 
     * @param currentResults
     * @param dataWithPosition
     * @return
     */
    public abstract ExecuteState judgmentBaseOn(
            final MatchResult currentResults, final DataMarked dataWithPosition);


    /**
     * Last statement about result of matching
     * 
     * @param allUpCurrentResults
     *            usually in case of many search in the same level its parent
     *            with included sub results
     * @param dataWithPosition
     *            last position
     * @return
     */
    public abstract MatchResult finalJudgment(
            final MatchResult allUpCurrentResults,
            final DataMarked dataWithPosition);


    /**
     * says base on last results about next position to check
     * 
     * @param currentResults
     * @param dataWithPosition
     * @return
     */
    public abstract DataMarked nextPosition(final MatchResult currentResults,
            final DataMarked dataWithPosition);


    @Override
    public MatchResult match(final DataMarked dataWithPosition) {
        MatchResult matchResult = new MatchResult(this, MatchStatus.NOT_FOUND);

        DataMarked currentPosition = dataWithPosition;
        for (IMatcher currentMatcher : matchesInThisGroup) {
            // matching process
            MatchResult currentMatchResult = currentMatcher
                    .match(currentPosition);
            matchResult.addSubResult(currentMatchResult);

            // check if we still need to do something with this data
            ExecuteState execState = judgmentBaseOn(currentMatchResult,
                    currentPosition);

            NextStep nextStep = execState.getNextStep();
            if (nextStep == NextStep.CONTINOUE) {
                currentPosition = nextPosition(currentMatchResult,
                        currentPosition);
            } else if (nextStep == NextStep.BREAK) {
                break;
            }
        }

        // compute what to say to the world
        matchResult = finalJudgment(matchResult, currentPosition);

        return matchResult;
    }

    /**
     * Says about matching process result and next step to perform by this
     * matcher base on results
     * 
     * @author wypych
     * @serial RobotFramework 2.8.6
     * @serial 1.0
     * 
     */
    public static class ExecuteState {

        private NextStep nextStep = NextStep.CONTINOUE;
        private final MatchResult result;


        public ExecuteState(final MatchResult result) {
            this.result = result;
        }


        /**
         * says that in anyway base on current result matching process should
         * continue
         */
        public void continueMatching() {
            nextStep = NextStep.CONTINOUE;
        }


        /**
         * says that in anyway base on current result matching process should
         * break
         */
        public void breakMatching() {
            nextStep = NextStep.BREAK;
        }


        public MatchResult getResult() {
            return this.result;
        }


        private NextStep getNextStep() {
            return this.nextStep;
        }
    }

    private static enum NextStep {
        BREAK, CONTINOUE
    }
}
