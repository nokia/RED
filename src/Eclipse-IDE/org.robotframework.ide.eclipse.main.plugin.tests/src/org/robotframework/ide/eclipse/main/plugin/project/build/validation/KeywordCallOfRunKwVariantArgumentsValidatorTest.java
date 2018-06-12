package org.robotframework.ide.eclipse.main.plugin.project.build.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.rf.ide.core.libraries.ArgumentsDescriptor.createDescriptor;
import static org.robotframework.ide.eclipse.main.plugin.project.build.validation.KeywordCallArgumentsValidatorTest.ALL;
import static org.robotframework.ide.eclipse.main.plugin.project.build.validation.KeywordCallArgumentsValidatorTest.call;
import static org.robotframework.ide.eclipse.main.plugin.project.build.validation.KeywordCallArgumentsValidatorTest.problem;

import java.util.Collection;
import java.util.List;

import org.assertj.core.api.SoftAssertions;
import org.junit.Test;
import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.model.table.exec.descs.IExecutableRowDescriptor;
import org.rf.ide.core.testdata.model.table.testcases.TestCase;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.ArgumentProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.KeywordCallArgumentsValidatorTest.ValidationStep;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.MockReporter.Problem;


public class KeywordCallOfRunKwVariantArgumentsValidatorTest {

    private static final ArgumentProblem NUMBER_PROBLEM = ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS;

    @Test
    public void noProblemsReportedTest() {
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(problemsOf(call("1", "2")).against(createDescriptor("a", "b"))).isEmpty();
            softly.assertThat(problemsOf(call("a=1", "2")).against(createDescriptor("a", "b"))).isEmpty();
            softly.assertThat(problemsOf(call("1", "a=2")).against(createDescriptor("a", "b"))).isEmpty();
            softly.assertThat(problemsOf(call("&{d}", "2")).against(createDescriptor("a", "b"))).isEmpty();
        });
    }

    @Test
    public void invalidNoOfArgumentsReported_whenNumberOfCallSiteArgumentsDoesNotMatch() {
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(problemsOf(call()).against(createDescriptor("a", "b"))).extracting(ALL).containsOnly(
                    problem(NUMBER_PROBLEM, 28, 35, "Keyword 'keyword' expects 2 arguments but 0 are provided"));
            softly.assertThat(problemsOf(call("1")).against(createDescriptor("a", "b"))).extracting(ALL).containsOnly(
                    problem(NUMBER_PROBLEM, 28, 35, "Keyword 'keyword' expects 2 arguments but 1 is provided"));
            softly.assertThat(problemsOf(call("1", "2", "3")).against(createDescriptor("a", "b"))).extracting(ALL).containsOnly(
                    problem(NUMBER_PROBLEM, 28, 35, "Keyword 'keyword' expects 2 arguments but 3 are provided"));

            softly.assertThat(problemsOf(call()).against(createDescriptor("a", "b", "*c"))).extracting(ALL).containsOnly(
                    problem(NUMBER_PROBLEM, 28, 35, "Keyword 'keyword' expects at least 2 arguments but 0 are provided"));
            softly.assertThat(problemsOf(call("1")).against(createDescriptor("a", "b", "*c"))).extracting(ALL).containsOnly(
                    problem(NUMBER_PROBLEM, 28, 35, "Keyword 'keyword' expects at least 2 arguments but 1 is provided"));
        });
    }

    private static ValidationStep problemsOf(final RobotKeywordCall call) {
        return descriptor -> {
            final MockReporter reporter = new MockReporter();

            @SuppressWarnings("unchecked")
            final RobotExecutableRow<TestCase> executable = (RobotExecutableRow<TestCase>) call.getLinkedElement();
            final IExecutableRowDescriptor<?> executableRowDescriptor = executable.buildLineDescription();
            final RobotToken definingToken = executableRowDescriptor.getAction().getToken();
            final List<RobotToken> argumentTokens = executableRowDescriptor.getKeywordArguments();

            final KeywordCallOfRunKwVariantArgumentsValidator validator = new KeywordCallOfRunKwVariantArgumentsValidator(
                    call.getSuiteFile().getFile(), definingToken, reporter, descriptor, argumentTokens);
            validator.validate(null);

            final Collection<Problem> problems = reporter.getReportedProblems();
            if (!problems.isEmpty()) {
                // all the problems should be reported in line 3 (if anything)
                assertThat(problems).extracting("line").containsOnly(3);
            }
            return problems;
        };
    }
}
