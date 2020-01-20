/*
* Copyright 2018 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.robotframework.ide.eclipse.main.plugin.project.build.validation;

import static org.rf.ide.core.libraries.ArgumentsDescriptor.createDescriptor;
import static org.robotframework.ide.eclipse.main.plugin.project.build.validation.KeywordCallArgumentsValidatorTest.ALL;
import static org.robotframework.ide.eclipse.main.plugin.project.build.validation.KeywordCallArgumentsValidatorTest.call;
import static org.robotframework.ide.eclipse.main.plugin.project.build.validation.KeywordCallArgumentsValidatorTest.problem;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.ArgumentProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.KeywordCallArgumentsValidatorTest.ValidationStep;


public class KeywordCallArgumentsOfRunKwVariantValidatorTest {

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
        return KeywordCallArgumentsValidatorTest.problemsOf(call, KeywordCallArgumentsOfRunKwVariantValidator::new);
    }
}
