package org.robotframework.ide.core.testData.text;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.robotframework.ide.core.testData.text.TxtRobotFileLexer.TokenizatorOutput;
import org.robotframework.ide.core.testData.text.contexts.AtLeastDoubleSpaceSeparatorSearcher;
import org.robotframework.ide.core.testData.text.contexts.CommentsSearcher;
import org.robotframework.ide.core.testData.text.contexts.DotContinoueBlockSearcher;
import org.robotframework.ide.core.testData.text.contexts.EscapedCharsBlockSearcher;
import org.robotframework.ide.core.testData.text.contexts.KeywordsTableHeaderSearcher;
import org.robotframework.ide.core.testData.text.contexts.PipeLineSeparatorSearcher;
import org.robotframework.ide.core.testData.text.contexts.SettingsTableHeaderSearcher;
import org.robotframework.ide.core.testData.text.contexts.TabulatedSeparatorSearcher;
import org.robotframework.ide.core.testData.text.contexts.TestCaseTableHeaderSearcher;
import org.robotframework.ide.core.testData.text.contexts.VariableTableHeaderSearcher;


public class ContextsMatcher {

    private final MultiThreadUtility<List<RobotTokenContext>, AContextMatcher> multiThread = new MultiThreadUtility<>();


    public void matchContexts(TokenizatorOutput tokenizatedOutput)
            throws InterruptedException, ExecutionException {
        Map<ContextType, List<RobotTokenContext>> contexts = new LinkedHashMap<>();
        List<AContextMatcher> matchersForIndependElements = createIndependentContextMatchers(tokenizatedOutput);

        List<List<RobotTokenContext>> computed = multiThread
                .compute(matchersForIndependElements);
        fillContextMap(contexts, computed);

        computed = null;
        matchersForIndependElements = null;
        // computed = multiThread.compute(matchers);
        // for (List<RobotTokenContext> context : computed) {
        // contexts.addAll(context);
        // }
        // merge contexts per line
        // temp solution adding it to one
        tokenizatedOutput.getContexts().putAll(contexts);
    }


    private List<AContextMatcher> createIndependentContextMatchers(
            TokenizatorOutput tokenizatedOutput) {
        List<AContextMatcher> matchersForNotDependElements = new LinkedList<>();
        matchersForNotDependElements.add(new SettingsTableHeaderSearcher(
                tokenizatedOutput));
        matchersForNotDependElements.add(new VariableTableHeaderSearcher(
                tokenizatedOutput));
        matchersForNotDependElements.add(new TestCaseTableHeaderSearcher(
                tokenizatedOutput));
        matchersForNotDependElements.add(new KeywordsTableHeaderSearcher(
                tokenizatedOutput));
        matchersForNotDependElements
                .add(new CommentsSearcher(tokenizatedOutput));
        matchersForNotDependElements.add(new PipeLineSeparatorSearcher(
                tokenizatedOutput));
        matchersForNotDependElements
                .add(new AtLeastDoubleSpaceSeparatorSearcher(tokenizatedOutput));
        matchersForNotDependElements.add(new TabulatedSeparatorSearcher(
                tokenizatedOutput));
        matchersForNotDependElements.add(new DotContinoueBlockSearcher(
                tokenizatedOutput));
        matchersForNotDependElements.add(new EscapedCharsBlockSearcher(
                tokenizatedOutput));
        return matchersForNotDependElements;
    }


    private void fillContextMap(
            Map<ContextType, List<RobotTokenContext>> contexts,
            List<List<RobotTokenContext>> computed) {
        for (List<RobotTokenContext> context : computed) {
            if (context != null && !context.isEmpty()) {
                RobotTokenContext ctx = context.get(0);
                List<RobotTokenContext> elements = contexts.get(ctx
                        .getContext());
                if (elements == null) {
                    contexts.put(ctx.getContext(), context);
                } else {
                    elements.addAll(context);
                }
            }
        }
    }
}
