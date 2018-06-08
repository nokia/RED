/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.rf.ide.core.libraries.ArgumentsDescriptor.createDescriptor;

import java.util.Collection;
import java.util.List;

import org.assertj.core.api.SoftAssertions;
import org.assertj.core.groups.Tuple;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.rf.ide.core.libraries.ArgumentsDescriptor;
import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.model.table.exec.descs.IExecutableRowDescriptor;
import org.rf.ide.core.testdata.model.table.testcases.TestCase;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.RobotSuiteFileCreator;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCasesSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.ArgumentProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.MockReporter.Problem;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

@RunWith(Enclosed.class)
public class GeneralKeywordCallArgumentsValidatorTest {

    private static final String[] ALL = new String[] { "cause", "start", "end", "message" };

    private static final ArgumentProblem DESCRIPTOR_PROBLEM = ArgumentProblem.INVALID_ARGUMENTS_DESCRIPTOR;
    private static final ArgumentProblem ORDER_PROBLEM = ArgumentProblem.POSITIONAL_ARGUMENT_AFTER_NAMED;
    private static final ArgumentProblem NUMBER_PROBLEM = ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS;
    private static final ArgumentProblem NUMBER_KW_PROBLEM = ArgumentProblem.INVALID_NUMBER_OF_NON_KEYWORD_PARAMETERS;
    private static final ArgumentProblem MULTIPLE_MATCH_PROBLEM = ArgumentProblem.MULTIPLE_MATCH_TO_SINGLE_ARG;
    private static final ArgumentProblem OVERRIDDEN_ARG_PROBLEM = ArgumentProblem.OVERRIDDEN_NAMED_ARGUMENT;
    private static final ArgumentProblem MISSING_PROBLEM = ArgumentProblem.NO_VALUE_PROVIDED_FOR_REQUIRED_ARG;
    private static final ArgumentProblem COLLECTION_WARNING = ArgumentProblem.COLLECTION_ARGUMENT_SHOULD_PROVIDE_ARGS;

    public static class InvalidDescriptorsTest {

        @Test
        public void invalidDescriptorIsReported_regardlessWhatArgumentsAreProvided() {
            final List<ArgumentsDescriptor> invalidDescriptors = Lists.newArrayList(
                    createDescriptor("a", "b=1", "**d", "*c"), createDescriptor("a", "**d", "b=1", "*c"),
                    createDescriptor("**d", "a", "b=1", "*c"), createDescriptor("a", "*c", "b=1", "**d"),
                    createDescriptor("*c", "a", "b=1", "**d"), createDescriptor("*c", "*d"),
                    createDescriptor("**c", "**d"), createDescriptor("a", "a=2"));

            final List<RobotKeywordCall> calls = newArrayList(call("1"), call("1", "2"), call("a=1", "b=2"),
                    call("a=1", "1"), call("1", "b=2"), call("@{l}", "2"), call("2", "@{l}"), call("b=2", "a=1", "c=3"),
                    call("1", "&{d}", "c=3"));

            SoftAssertions.assertSoftly(softly -> {
                for (final ArgumentsDescriptor descriptor : invalidDescriptors) {
                    for (final RobotKeywordCall call : calls) {
                        softly.assertThat(problemsOf(call).against(descriptor)).extracting(ALL).containsOnly(problem(
                                DESCRIPTOR_PROBLEM, 28, 35, "Keyword 'keyword' has invalid arguments descriptor"));
                    }
                }
            });
        }
    }

    public static class NoArgsTest {

        private final ArgumentsDescriptor zeroArgs = createDescriptor();

        @Test
        public void noProblemsReported_whenThereAreNoArgumentsAtCallSite() {
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(problemsOf(call()).against(zeroArgs)).isEmpty();
            });
        }

        @Test
        public void invalidOrderIsReported_whenPositionalArgumentsAreUsedAfterNamedOne() {
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(problemsOf(call("&{d}", "2")).against(zeroArgs)).extracting(ALL).containsOnly(
                            problem(ORDER_PROBLEM, 47, 48, "Positional argument cannot be used after named arguments"));
                softly.assertThat(problemsOf(call("&{d}", "b=2")).against(zeroArgs)).extracting(ALL).containsOnly(
                            problem(ORDER_PROBLEM, 47, 50, "Positional argument cannot be used after named arguments. "
                                        + "Although this argument looks like named one, it isn't because there is no "
                                        + "'b' argument in the keyword definition"));
            });
        }

        @Test
        public void invalidNoOfArgumentsReported_whenThereIsAtLeastOneArgument() {
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(problemsOf(call("1")).against(zeroArgs)).extracting(ALL).containsOnly(
                        problem(NUMBER_PROBLEM, 28, 35,
                                "Invalid number of arguments. Keyword 'keyword' expects 0 arguments but 1 is provided"));
                softly.assertThat(problemsOf(call("1", "2")).against(zeroArgs)).extracting(ALL).containsOnly(
                        problem(NUMBER_PROBLEM, 28, 35,
                                "Invalid number of arguments. Keyword 'keyword' expects 0 arguments but 2 are provided"));
                softly.assertThat(problemsOf(call("a=1")).against(zeroArgs)).extracting(ALL).containsOnly(
                        problem(NUMBER_PROBLEM, 28, 35,
                                "Invalid number of arguments. Keyword 'keyword' expects 0 arguments but 1 is provided"));
                softly.assertThat(problemsOf(call("a=1", "b=2")).against(zeroArgs)).extracting(ALL).containsOnly(
                        problem(NUMBER_PROBLEM, 28, 35,
                                "Invalid number of arguments. Keyword 'keyword' expects 0 arguments but 2 are provided"));
                softly.assertThat(problemsOf(call("a=1", "2")).against(zeroArgs)).extracting(ALL).containsOnly(
                        problem(NUMBER_PROBLEM, 28, 35,
                                "Invalid number of arguments. Keyword 'keyword' expects 0 arguments but 2 are provided"));
                softly.assertThat(problemsOf(call("${s}")).against(zeroArgs)).extracting(ALL).containsOnly(
                        problem(NUMBER_PROBLEM, 28, 35,
                                "Invalid number of arguments. Keyword 'keyword' expects 0 arguments but 1 is provided"));
                softly.assertThat(problemsOf(call("@{l}", "2")).against(zeroArgs)).extracting(ALL).containsOnly(
                        problem(NUMBER_PROBLEM, 28, 35,
                                "Invalid number of arguments. Keyword 'keyword' expects 0 arguments but at least 1 is provided"));
                softly.assertThat(problemsOf(call("@{l}[0]")).against(zeroArgs)).extracting(ALL).containsOnly(
                        problem(NUMBER_PROBLEM, 28, 35,
                                "Invalid number of arguments. Keyword 'keyword' expects 0 arguments but 1 is provided"));
                softly.assertThat(problemsOf(call("&{d}[a]")).against(zeroArgs)).extracting(ALL).containsOnly(
                        problem(NUMBER_PROBLEM, 28, 35,
                                "Invalid number of arguments. Keyword 'keyword' expects 0 arguments but 1 is provided"));
            });
        }

        @Test
        public void listsAndDictionariesAreReported_thatTheyHaveToBeEmpty() {
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(problemsOf(call("@{l}")).against(zeroArgs)).extracting(ALL).containsOnly(
                        problem(COLLECTION_WARNING, 39, 43, "List argument '@{l}' has to be empty"));
                softly.assertThat(problemsOf(call("@{l}", "@{l}")).against(zeroArgs)).extracting(ALL).containsOnly(
                        problem(COLLECTION_WARNING, 39, 43, "List argument '@{l}' has to be empty"),
                        problem(COLLECTION_WARNING, 47, 51, "List argument '@{l}' has to be empty"));
                softly.assertThat(problemsOf(call("&{d}")).against(zeroArgs)).extracting(ALL).containsOnly(
                        problem(COLLECTION_WARNING, 39, 43, "Dictionary argument '&{d}' has to be empty"));
                softly.assertThat(problemsOf(call("&{d}", "&{d}")).against(zeroArgs)).extracting(ALL).containsOnly(
                        problem(COLLECTION_WARNING, 39, 43, "Dictionary argument '&{d}' has to be empty"),
                        problem(COLLECTION_WARNING, 47, 51, "Dictionary argument '&{d}' has to be empty"));
            });
        }
    }

    public static class OnlyRequiredArgsTest {

        private final ArgumentsDescriptor oneArg = createDescriptor("a");

        private final ArgumentsDescriptor twoArgs = createDescriptor("a", "b");

        private final ArgumentsDescriptor threeArgs = createDescriptor("a", "b", "c");

        @Test
        public void noProblemsReported_whenArgumentsAreUsedProperly() {
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(problemsOf(call("1")).against(oneArg)).isEmpty();
                softly.assertThat(problemsOf(call("a=1")).against(oneArg)).isEmpty();
                softly.assertThat(problemsOf(call("x=1")).against(oneArg)).isEmpty();
                softly.assertThat(problemsOf(call("${s}")).against(oneArg)).isEmpty();
                softly.assertThat(problemsOf(call("@{l}[0]")).against(oneArg)).isEmpty();
                softly.assertThat(problemsOf(call("&{d}[key]")).against(oneArg)).isEmpty();

                softly.assertThat(problemsOf(call("1", "2", "3")).against(threeArgs)).isEmpty();
                softly.assertThat(problemsOf(call("a=1", "b=2", "c=3")).against(threeArgs)).isEmpty();
                softly.assertThat(problemsOf(call("a=1", "c=3", "b=2")).against(threeArgs)).isEmpty();
                softly.assertThat(problemsOf(call("b=2", "a=1", "c=3")).against(threeArgs)).isEmpty();
                softly.assertThat(problemsOf(call("b=2", "c=3", "a=1")).against(threeArgs)).isEmpty();
                softly.assertThat(problemsOf(call("c=3", "b=2", "a=1")).against(threeArgs)).isEmpty();
                softly.assertThat(problemsOf(call("c=3", "a=1", "b=2")).against(threeArgs)).isEmpty();
                softly.assertThat(problemsOf(call("x=1", "y=2", "z=3")).against(threeArgs)).isEmpty();
                softly.assertThat(problemsOf(call("x=1", "y=2", "c=3")).against(threeArgs)).isEmpty();
                softly.assertThat(problemsOf(call("${s}", "${s}1", "${s}2")).against(threeArgs)).isEmpty();
            });
        }

        @Test
        public void overriddenNamedArgumentsAreReported() {
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(problemsOf(call("a=1", "a=2")).against(oneArg)).extracting(ALL).containsOnly(
                        problem(OVERRIDDEN_ARG_PROBLEM, 39, 42,
                            "Argument 'a' is passed multiple times using named syntax. This value will never be used"));
                softly.assertThat(problemsOf(call("a=1", "b=2", "c=3", "c=4", "b=5", "a=6")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(OVERRIDDEN_ARG_PROBLEM, 39, 42,
                            "Argument 'a' is passed multiple times using named syntax. This value will never be used"),
                        problem(OVERRIDDEN_ARG_PROBLEM, 46, 49,
                            "Argument 'b' is passed multiple times using named syntax. This value will never be used"),
                        problem(OVERRIDDEN_ARG_PROBLEM, 53, 56,
                            "Argument 'c' is passed multiple times using named syntax. This value will never be used"));
            });
        }

        @Test
        public void invalidOrderIsReported_whenPositionalArgumentsAreUsedAfterNamedOne() {
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(problemsOf(call("a=1", "2")).against(oneArg)).extracting(ALL).containsOnly(
                        problem(ORDER_PROBLEM, 46, 47, "Positional argument cannot be used after named arguments"));
                softly.assertThat(problemsOf(call("a=1", "b=2")).against(oneArg)).extracting(ALL).containsOnly(
                        problem(ORDER_PROBLEM, 46, 49, "Positional argument cannot be used after named arguments. "
                                        + "Although this argument looks like named one, it isn't because there is no "
                                        + "'b' argument in the keyword definition"));
                softly.assertThat(problemsOf(call("&{d}", "2")).against(oneArg)).extracting(ALL).containsOnly(
                        problem(ORDER_PROBLEM, 47, 48, "Positional argument cannot be used after named arguments"));
                softly.assertThat(problemsOf(call("&{d}", "b=2")).against(oneArg)).extracting(ALL).containsOnly(
                        problem(ORDER_PROBLEM, 47, 50, "Positional argument cannot be used after named arguments. "
                                        + "Although this argument looks like named one, it isn't because there is no "
                                        + "'b' argument in the keyword definition"));
                
                softly.assertThat(problemsOf(call("a=1", "b=2", "x=3")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(ORDER_PROBLEM, 53, 56, "Positional argument cannot be used after named arguments. "
                                        + "Although this argument looks like named one, it isn't because there is no "
                                        + "'x' argument in the keyword definition"));
                softly.assertThat(problemsOf(call("a=1", "x=2", "${s}")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(ORDER_PROBLEM, 46, 49, "Positional argument cannot be used after named arguments. "
                                        + "Although this argument looks like named one, it isn't because there is no "
                                        + "'x' argument in the keyword definition"),
                        problem(ORDER_PROBLEM, 53, 57, "Positional argument cannot be used after named arguments"));
                softly.assertThat(problemsOf(call("a=1", "2", "3")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(ORDER_PROBLEM, 46, 47, "Positional argument cannot be used after named arguments"),
                        problem(ORDER_PROBLEM, 51, 52, "Positional argument cannot be used after named arguments"));
                softly.assertThat(problemsOf(call("1", "&{d}", "3")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(ORDER_PROBLEM, 52, 53, "Positional argument cannot be used after named arguments"));
                softly.assertThat(problemsOf(call("1", "&{d}", "3", "4")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(ORDER_PROBLEM, 52, 53, "Positional argument cannot be used after named arguments"),
                        problem(ORDER_PROBLEM, 57, 58, "Positional argument cannot be used after named arguments"));
                softly.assertThat(problemsOf(call("1", "&{d}", "x=3")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(ORDER_PROBLEM, 52, 55, "Positional argument cannot be used after named arguments. "
                                        + "Although this argument looks like named one, it isn't because there is no "
                                        + "'x' argument in the keyword definition"));
            });
        }

        @Test
        public void invalidNoOfArgumentsReported_whenNumberOfCallSiteArgumentsDoesNotMatchNumberOfRequired() {
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(problemsOf(call()).against(oneArg)).extracting(ALL).containsOnly(
                        problem(NUMBER_PROBLEM, 28, 35,
                                "Invalid number of arguments. Keyword 'keyword' expects 1 argument but 0 are provided"));
                softly.assertThat(problemsOf(call("1", "2")).against(oneArg)).extracting(ALL).containsOnly(
                        problem(NUMBER_PROBLEM, 28, 35,
                                "Invalid number of arguments. Keyword 'keyword' expects 1 argument but 2 are provided"));
                softly.assertThat(problemsOf(call("x=1", "y=2")).against(oneArg)).extracting(ALL).containsOnly(
                        problem(NUMBER_PROBLEM, 28, 35,
                                "Invalid number of arguments. Keyword 'keyword' expects 1 argument but 2 are provided"));
                softly.assertThat(problemsOf(call("@{l}", "2", "3")).against(oneArg)).extracting(ALL).containsOnly(
                        problem(NUMBER_PROBLEM, 28, 35,
                                "Invalid number of arguments. Keyword 'keyword' expects 1 argument but at least 2 are provided"));

                softly.assertThat(problemsOf(call()).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(NUMBER_PROBLEM, 28, 35,
                                "Invalid number of arguments. Keyword 'keyword' expects 3 arguments but 0 are provided"));
                softly.assertThat(problemsOf(call("1")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(NUMBER_PROBLEM, 28, 35,
                                "Invalid number of arguments. Keyword 'keyword' expects 3 arguments but 1 is provided"));
                softly.assertThat(problemsOf(call("1", "2")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(NUMBER_PROBLEM, 28, 35,
                                "Invalid number of arguments. Keyword 'keyword' expects 3 arguments but 2 are provided"));
                softly.assertThat(problemsOf(call("1", "2", "3", "4")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(NUMBER_PROBLEM, 28, 35,
                                "Invalid number of arguments. Keyword 'keyword' expects 3 arguments but 4 are provided"));
                softly.assertThat(problemsOf(call("1", "@{l}", "3", "4", "5")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(NUMBER_PROBLEM, 28, 35,
                                "Invalid number of arguments. Keyword 'keyword' expects 3 arguments but at least 4 are provided"));
                softly.assertThat(problemsOf(call("a=1")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(NUMBER_PROBLEM, 28, 35,
                                "Invalid number of arguments. Keyword 'keyword' expects 3 arguments but 1 is provided"));
                softly.assertThat(problemsOf(call("a=1", "b=2")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(NUMBER_PROBLEM, 28, 35,
                                "Invalid number of arguments. Keyword 'keyword' expects 3 arguments but 2 are provided"));
                softly.assertThat(problemsOf(call("${s}")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(NUMBER_PROBLEM, 28, 35,
                                "Invalid number of arguments. Keyword 'keyword' expects 3 arguments but 1 is provided"));
                softly.assertThat(problemsOf(call("${s}", "${l}")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(NUMBER_PROBLEM, 28, 35,
                                "Invalid number of arguments. Keyword 'keyword' expects 3 arguments but 2 are provided"));
            });
        }

        @Test
        public void multipleMatchesAreReported_whenArgIsPassedPositionallyAndByName() {
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(problemsOf(call("1", "a=2")).against(oneArg)).extracting(ALL).containsOnly(
                        problem(MULTIPLE_MATCH_PROBLEM, 44, 47, "Argument 'a' has value already passed (1)"));

                softly.assertThat(problemsOf(call("1", "a=2")).against(twoArgs)).extracting(ALL).containsOnly(
                        problem(MULTIPLE_MATCH_PROBLEM, 44, 47, "Argument 'a' has value already passed (1)"));

                softly.assertThat(problemsOf(call("1", "a=2", "c=3")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(MULTIPLE_MATCH_PROBLEM, 44, 47, "Argument 'a' has value already passed (1)"));
                softly.assertThat(problemsOf(call("1", "a=2")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(MULTIPLE_MATCH_PROBLEM, 44, 47, "Argument 'a' has value already passed (1)"));
                softly.assertThat(problemsOf(call("1", "a=2", "b=3", "c=4")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(MULTIPLE_MATCH_PROBLEM, 44, 47, "Argument 'a' has value already passed (1)"));
                softly.assertThat(problemsOf(call("1", "2", "a=3", "b=4")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(MULTIPLE_MATCH_PROBLEM, 49, 52, "Argument 'a' has value already passed (1)"),
                        problem(MULTIPLE_MATCH_PROBLEM, 56, 59, "Argument 'b' has value already passed (2)"));
            });
        }

        @Test
        public void listsAreReported_whenUsedAsArgumentInOrderToProvideArguments() {
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(problemsOf(call("@{l}")).against(oneArg)).extracting(ALL).containsOnly(
                        problem(COLLECTION_WARNING, 39, 43, "List argument '@{l}' has to contain 1 item"));
                softly.assertThat(problemsOf(call("@{l}", "@{l}")).against(oneArg)).extracting(ALL).containsOnly(
                        problem(COLLECTION_WARNING, 39, 43, "List argument '@{l}' has to be empty"),
                        problem(COLLECTION_WARNING, 47, 51, "List argument '@{l}' has to contain 1 item"));

                softly.assertThat(problemsOf(call("@{l}")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(COLLECTION_WARNING, 39, 43, "List argument '@{l}' has to contain 3 items"));
                softly.assertThat(problemsOf(call("@{l}", "@{l}")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(COLLECTION_WARNING, 39, 43, "List argument '@{l}' has to be empty"),
                        problem(COLLECTION_WARNING, 47, 51, "List argument '@{l}' has to contain 3 items"));
                softly.assertThat(problemsOf(call("1", "@{l}")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(COLLECTION_WARNING, 44, 48, "List argument '@{l}' has to contain 2 items"));
                softly.assertThat(problemsOf(call("1", "@{l}", "3")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(COLLECTION_WARNING, 44, 48, "List argument '@{l}' has to contain 1 item"));
                softly.assertThat(problemsOf(call("1", "@{l}", "3", "4")).against(threeArgs)).extracting(ALL).containsOnly(
                                problem(COLLECTION_WARNING, 44, 48, "List argument '@{l}' has to be empty"));
            });
        }

        @Test
        public void dictionariesAreReported_whenUsedAsArgumentInOrderToProvideArguments() {
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(problemsOf(call("&{d}")).against(oneArg)).extracting(ALL).containsOnly(
                        problem(COLLECTION_WARNING, 39, 43,
                                "Dictionary argument '&{d}' has to contain 1 mapping. Required key: (a)"));
                softly.assertThat(problemsOf(call("&{d}", "&{d}")).against(oneArg)).extracting(ALL).containsOnly(
                        problem(COLLECTION_WARNING, 39, 43, "Dictionary argument '&{d}' has to be empty"),
                        problem(COLLECTION_WARNING, 47, 51,
                                "Dictionary argument '&{d}' has to contain 1 mapping. Required key: (a)"));
                softly.assertThat(problemsOf(call("&{d}", "a=2")).against(oneArg)).extracting(ALL).containsOnly(
                        problem(COLLECTION_WARNING, 39, 43, "Dictionary argument '&{d}' has to be empty"));

                softly.assertThat(problemsOf(call("&{d}")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(COLLECTION_WARNING, 39, 43,
                                "Dictionary argument '&{d}' has to contain 3 mappings. Required keys: (a, b, c)"));
                softly.assertThat(problemsOf(call("&{d}", "&{d}")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(COLLECTION_WARNING, 39, 43, "Dictionary argument '&{d}' has to be empty"),
                        problem(COLLECTION_WARNING, 47, 51,
                                "Dictionary argument '&{d}' has to contain 3 mappings. Required keys: (a, b, c)"));
                softly.assertThat(problemsOf(call("1", "&{d}")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(COLLECTION_WARNING, 44, 48,
                                "Dictionary argument '&{d}' has to contain 2 mappings. Required keys: (b, c), forbidden key: (a)"));
                softly.assertThat(problemsOf(call("1", "&{d}", "b=2", "c=3")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(COLLECTION_WARNING, 44, 48, "Dictionary argument '&{d}' has to be empty"));
                softly.assertThat(problemsOf(call("1", "2", "&{d}")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(COLLECTION_WARNING, 49, 53,
                                "Dictionary argument '&{d}' has to contain 1 mapping. Required key: (c), forbidden keys: (a, b)"));
            });
        }
    }

    public static class OnlyDefaultArgsTest {

        private final ArgumentsDescriptor oneArg = createDescriptor("a=1");

        private final ArgumentsDescriptor twoArgs = createDescriptor("a=1", "b=2");

        private final ArgumentsDescriptor threeArgs = createDescriptor("a=1", "b=2", "c=3");

        @Test
        public void noProblemsReported_whenArgumentsAreUsedProperly() {
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(problemsOf(call()).against(oneArg)).isEmpty();
                softly.assertThat(problemsOf(call("1")).against(oneArg)).isEmpty();
                softly.assertThat(problemsOf(call("a=1")).against(oneArg)).isEmpty();
                softly.assertThat(problemsOf(call("x=1")).against(oneArg)).isEmpty();
                softly.assertThat(problemsOf(call("${s}")).against(oneArg)).isEmpty();
                softly.assertThat(problemsOf(call("@{l}[0]")).against(oneArg)).isEmpty();
                softly.assertThat(problemsOf(call("&{d}[a]")).against(oneArg)).isEmpty();

                softly.assertThat(problemsOf(call()).against(threeArgs)).isEmpty();
                softly.assertThat(problemsOf(call("1")).against(threeArgs)).isEmpty();
                softly.assertThat(problemsOf(call("1", "2")).against(threeArgs)).isEmpty();
                softly.assertThat(problemsOf(call("1", "2", "3")).against(threeArgs)).isEmpty();
                softly.assertThat(problemsOf(call("a=1", "b=2", "c=3")).against(threeArgs)).isEmpty();
                softly.assertThat(problemsOf(call("a=1", "c=3", "b=2")).against(threeArgs)).isEmpty();
                softly.assertThat(problemsOf(call("b=2", "a=1", "c=3")).against(threeArgs)).isEmpty();
                softly.assertThat(problemsOf(call("b=2", "c=3", "a=1")).against(threeArgs)).isEmpty();
                softly.assertThat(problemsOf(call("c=3", "b=2", "a=1")).against(threeArgs)).isEmpty();
                softly.assertThat(problemsOf(call("c=3", "a=1", "b=2")).against(threeArgs)).isEmpty();
                softly.assertThat(problemsOf(call("a=1", "b=2")).against(threeArgs)).isEmpty();
                softly.assertThat(problemsOf(call("a=1", "c=3")).against(threeArgs)).isEmpty();
                softly.assertThat(problemsOf(call("b=2", "a=1")).against(threeArgs)).isEmpty();
                softly.assertThat(problemsOf(call("b=2", "c=3")).against(threeArgs)).isEmpty();
                softly.assertThat(problemsOf(call("c=3", "b=2")).against(threeArgs)).isEmpty();
                softly.assertThat(problemsOf(call("c=3", "a=1")).against(threeArgs)).isEmpty();
                softly.assertThat(problemsOf(call("a=1")).against(threeArgs)).isEmpty();
                softly.assertThat(problemsOf(call("b=2")).against(threeArgs)).isEmpty();
                softly.assertThat(problemsOf(call("c=3")).against(threeArgs)).isEmpty();
                softly.assertThat(problemsOf(call("x=1")).against(threeArgs)).isEmpty();
                softly.assertThat(problemsOf(call("x=1", "y=2")).against(threeArgs)).isEmpty();
                softly.assertThat(problemsOf(call("x=1", "y=2", "z=3")).against(threeArgs)).isEmpty();
                softly.assertThat(problemsOf(call("x=1", "b=2", "c=3")).against(threeArgs)).isEmpty();
                softly.assertThat(problemsOf(call("${s}")).against(threeArgs)).isEmpty();
                softly.assertThat(problemsOf(call("${s}", "${s}", "${s}")).against(threeArgs)).isEmpty();
                softly.assertThat(problemsOf(call("${s}", "${s}1", "${s}2")).against(threeArgs)).isEmpty();
            });
        }

        @Test
        public void overriddenNamedArgumentsAreReported() {
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(problemsOf(call("a=1", "a=2")).against(oneArg)).extracting(ALL).containsOnly(
                        problem(OVERRIDDEN_ARG_PROBLEM, 39, 42,
                            "Argument 'a' is passed multiple times using named syntax. This value will never be used"));
                softly.assertThat(problemsOf(call("a=1", "b=2", "c=3", "c=4", "b=5", "a=6")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(OVERRIDDEN_ARG_PROBLEM, 39, 42,
                            "Argument 'a' is passed multiple times using named syntax. This value will never be used"),
                        problem(OVERRIDDEN_ARG_PROBLEM, 46, 49,
                            "Argument 'b' is passed multiple times using named syntax. This value will never be used"),
                        problem(OVERRIDDEN_ARG_PROBLEM, 53, 56,
                            "Argument 'c' is passed multiple times using named syntax. This value will never be used"));
            });
        }

        @Test
        public void invalidOrderIsReported_whenPositionalArgumentsAreUsedAfterNamedOne() {
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(problemsOf(call("a=1", "2")).against(oneArg)).extracting(ALL).containsOnly(
                        problem(ORDER_PROBLEM, 46, 47, "Positional argument cannot be used after named arguments"));
                softly.assertThat(problemsOf(call("a=1", "b=2")).against(oneArg)).extracting(ALL).containsOnly(
                        problem(ORDER_PROBLEM, 46, 49, "Positional argument cannot be used after named arguments. "
                                        + "Although this argument looks like named one, it isn't because there is no "
                                        + "'b' argument in the keyword definition"));
                softly.assertThat(problemsOf(call("&{d}", "2")).against(oneArg)).extracting(ALL).containsOnly(
                        problem(ORDER_PROBLEM, 47, 48, "Positional argument cannot be used after named arguments"));
                softly.assertThat(problemsOf(call("&{d}", "b=2")).against(oneArg)).extracting(ALL).containsOnly(
                        problem(ORDER_PROBLEM, 47, 50, "Positional argument cannot be used after named arguments. "
                                        + "Although this argument looks like named one, it isn't because there is no "
                                        + "'b' argument in the keyword definition"));

                softly.assertThat(problemsOf(call("a=1", "b=2", "x=3")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(ORDER_PROBLEM, 53, 56, "Positional argument cannot be used after named arguments. "
                                        + "Although this argument looks like named one, it isn't because there is no "
                                        + "'x' argument in the keyword definition"));
                softly.assertThat(problemsOf(call("a=1", "x=2", "${s}")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(ORDER_PROBLEM, 46, 49, "Positional argument cannot be used after named arguments. "
                                        + "Although this argument looks like named one, it isn't because there is no "
                                        + "'x' argument in the keyword definition"),
                        problem(ORDER_PROBLEM, 53, 57, "Positional argument cannot be used after named arguments"));
                softly.assertThat(problemsOf(call("a=1", "2", "3")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(ORDER_PROBLEM, 46, 47, "Positional argument cannot be used after named arguments"),
                        problem(ORDER_PROBLEM, 51, 52, "Positional argument cannot be used after named arguments"));
                softly.assertThat(problemsOf(call("1", "&{d}", "3")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(ORDER_PROBLEM, 52, 53, "Positional argument cannot be used after named arguments"));
                softly.assertThat(problemsOf(call("1", "&{d}", "3", "4")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(ORDER_PROBLEM, 52, 53, "Positional argument cannot be used after named arguments"),
                        problem(ORDER_PROBLEM, 57, 58, "Positional argument cannot be used after named arguments"));
                softly.assertThat(problemsOf(call("1", "&{d}", "x=3")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(ORDER_PROBLEM, 52, 55, "Positional argument cannot be used after named arguments. "
                                        + "Although this argument looks like named one, it isn't because there is no "
                                        + "'x' argument in the keyword definition"));
            });
        }

        @Test
        public void invalidNoOfArgumentsReported_whenNumberOfCallSiteArgumentsExceedsMaxNumberOfArgs() {
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(problemsOf(call("1", "2")).against(oneArg)).extracting(ALL).containsOnly(
                        problem(NUMBER_PROBLEM, 28, 35,
                        "Invalid number of arguments. Keyword 'keyword' expects from 0 to 1 argument but 2 are provided"));
                softly.assertThat(problemsOf(call("x=1", "y=2")).against(oneArg)).extracting(ALL).containsOnly(
                        problem(NUMBER_PROBLEM, 28, 35,
                        "Invalid number of arguments. Keyword 'keyword' expects from 0 to 1 argument but 2 are provided"));
                softly.assertThat(problemsOf(call("@{l}", "2", "3")).against(oneArg)).extracting(ALL).containsOnly(
                        problem(NUMBER_PROBLEM, 28, 35,
                        "Invalid number of arguments. Keyword 'keyword' expects from 0 to 1 argument but at least 2 are provided"));

                softly.assertThat(problemsOf(call("1", "2", "3", "4")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(NUMBER_PROBLEM, 28, 35,
                        "Invalid number of arguments. Keyword 'keyword' expects from 0 to 3 arguments but 4 are provided"));
                softly.assertThat(problemsOf(call("${s}", "${l}", "${d}", "${x}")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(NUMBER_PROBLEM, 28, 35,
                        "Invalid number of arguments. Keyword 'keyword' expects from 0 to 3 arguments but 4 are provided"));
                softly.assertThat(problemsOf(call("1", "@{l}", "3", "4", "5")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(NUMBER_PROBLEM, 28, 35,
                        "Invalid number of arguments. Keyword 'keyword' expects from 0 to 3 arguments but at least 4 are provided"));
            });
        }

        @Test
        public void multipleMatchesAreReported_whenArgIsPassedPositionallyAndByName() {
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(problemsOf(call("1", "a=2")).against(oneArg)).extracting(ALL).containsOnly(
                        problem(MULTIPLE_MATCH_PROBLEM, 44, 47, "Argument 'a' has value already passed (1)"));

                softly.assertThat(problemsOf(call("1", "a=2")).against(twoArgs)).extracting(ALL).containsOnly(
                        problem(MULTIPLE_MATCH_PROBLEM, 44, 47, "Argument 'a' has value already passed (1)"));

                softly.assertThat(problemsOf(call("1", "a=2")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(MULTIPLE_MATCH_PROBLEM, 44, 47, "Argument 'a' has value already passed (1)"));
                softly.assertThat(problemsOf(call("1", "a=2", "c=3")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(MULTIPLE_MATCH_PROBLEM, 44, 47, "Argument 'a' has value already passed (1)"));
                softly.assertThat(problemsOf(call("1", "a=2", "b=3", "c=4")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(MULTIPLE_MATCH_PROBLEM, 44, 47, "Argument 'a' has value already passed (1)"));
                softly.assertThat(problemsOf(call("1", "2", "a=3", "b=4")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(MULTIPLE_MATCH_PROBLEM, 49, 52, "Argument 'a' has value already passed (1)"),
                        problem(MULTIPLE_MATCH_PROBLEM, 56, 59, "Argument 'b' has value already passed (2)"));
            });
        }

        @Test
        public void listsAreReported_whenUsedAsArgumentInOrderToProvideMultipleArguments() {
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(problemsOf(call("@{l}")).against(oneArg)).extracting(ALL).containsOnly(
                        problem(COLLECTION_WARNING, 39, 43, "List argument '@{l}' has to contain from 0 to 1 item"));
                softly.assertThat(problemsOf(call("@{l}", "@{l}")).against(oneArg)).extracting(ALL).containsOnly(
                        problem(COLLECTION_WARNING, 39, 43, "List argument '@{l}' has to be empty"),
                        problem(COLLECTION_WARNING, 47, 51, "List argument '@{l}' has to contain from 0 to 1 item"));

                softly.assertThat(problemsOf(call("@{l}")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(COLLECTION_WARNING, 39, 43, "List argument '@{l}' has to contain from 0 to 3 items"));
                softly.assertThat(problemsOf(call("@{l}", "@{l}")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(COLLECTION_WARNING, 39, 43, "List argument '@{l}' has to be empty"),
                        problem(COLLECTION_WARNING, 47, 51, "List argument '@{l}' has to contain from 0 to 3 items"));
                softly.assertThat(problemsOf(call("1", "@{l}")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(COLLECTION_WARNING, 44, 48, "List argument '@{l}' has to contain from 0 to 2 items"));
                softly.assertThat(problemsOf(call("1", "@{l}", "3")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(COLLECTION_WARNING, 44, 48, "List argument '@{l}' has to contain from 0 to 1 item"));
                softly.assertThat(problemsOf(call("1", "@{l}", "3", "4")).against(threeArgs)).extracting(ALL).containsOnly(
                                problem(COLLECTION_WARNING, 44, 48, "List argument '@{l}' has to be empty"));
            });
        }

        @Test
        public void dictionariesAreReported_whenUsedAsArgumentInOrderToProvideMultipleArguments() {
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(problemsOf(call("&{d}")).against(oneArg)).extracting(ALL).containsOnly(
                        problem(COLLECTION_WARNING, 39, 43,
                                "Dictionary argument '&{d}' has to contain from 0 to 1 mapping. Possible key: (a)"));
                softly.assertThat(problemsOf(call("&{d}", "&{d}")).against(oneArg)).extracting(ALL).containsOnly(
                        problem(COLLECTION_WARNING, 39, 43, "Dictionary argument '&{d}' has to be empty"),
                        problem(COLLECTION_WARNING, 47, 51,
                                "Dictionary argument '&{d}' has to contain from 0 to 1 mapping. Possible key: (a)"));
                softly.assertThat(problemsOf(call("&{d}", "a=2")).against(oneArg)).extracting(ALL).containsOnly(
                        problem(COLLECTION_WARNING, 39, 43, "Dictionary argument '&{d}' has to be empty"));

                softly.assertThat(problemsOf(call("&{d}")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(COLLECTION_WARNING, 39, 43,
                                "Dictionary argument '&{d}' has to contain from 0 to 3 mappings. Possible keys: (a, b, c)"));
                softly.assertThat(problemsOf(call("&{d}", "&{d}")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(COLLECTION_WARNING, 39, 43, "Dictionary argument '&{d}' has to be empty"),
                        problem(COLLECTION_WARNING, 47, 51,
                                "Dictionary argument '&{d}' has to contain from 0 to 3 mappings. Possible keys: (a, b, c)"));
                softly.assertThat(problemsOf(call("1", "&{d}")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(COLLECTION_WARNING, 44, 48,
                                "Dictionary argument '&{d}' has to contain from 0 to 2 mappings. Possible keys: (b, c), forbidden key: (a)"));
                softly.assertThat(problemsOf(call("1", "&{d}", "b=2", "c=3")).against(threeArgs)).extracting(ALL).containsOnly(
                                problem(COLLECTION_WARNING, 44, 48, "Dictionary argument '&{d}' has to be empty"));
                softly.assertThat(problemsOf(call("1", "&{d}", "c=3")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(COLLECTION_WARNING, 44, 48,
                                "Dictionary argument '&{d}' has to contain from 0 to 1 mapping. Possible key: (b), forbidden key: (a)"));
                softly.assertThat(problemsOf(call("1", "2", "&{d}")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(COLLECTION_WARNING, 49, 53,
                                "Dictionary argument '&{d}' has to contain from 0 to 1 mapping. Possible key: (c), forbidden keys: (a, b)"));
            });
        }
    }

    public static class OnlyVarargTest {

        private final ArgumentsDescriptor desc = createDescriptor("*vararg");

        @Test
        public void noProblemsReported_whenArgumentsAreUsedProperly() {
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(problemsOf(call()).against(desc)).isEmpty();
                softly.assertThat(problemsOf(call("1")).against(desc)).isEmpty();
                softly.assertThat(problemsOf(call("1", "2")).against(desc)).isEmpty();
                softly.assertThat(problemsOf(call("1", "2", "3")).against(desc)).isEmpty();
                softly.assertThat(problemsOf(call("1", "2", "3", "4")).against(desc)).isEmpty();
                softly.assertThat(problemsOf(call("a=1")).against(desc)).isEmpty();
                softly.assertThat(problemsOf(call("a=1", "b=2")).against(desc)).isEmpty();
                softly.assertThat(problemsOf(call("a=1", "b=2", "c=3")).against(desc)).isEmpty();
                softly.assertThat(problemsOf(call("x=1")).against(desc)).isEmpty();
                softly.assertThat(problemsOf(call("x=1", "2")).against(desc)).isEmpty();
                softly.assertThat(problemsOf(call("x=1", "b=2")).against(desc)).isEmpty();
                softly.assertThat(problemsOf(call("x=1", "x=2")).against(desc)).isEmpty();
                softly.assertThat(problemsOf(call("1", "x=2")).against(desc)).isEmpty();
                softly.assertThat(problemsOf(call("x=1", "y=2", "z=3")).against(desc)).isEmpty();
                softly.assertThat(problemsOf(call("x=1", "z=3", "y=2")).against(desc)).isEmpty();
                softly.assertThat(problemsOf(call("z=3", "y=2", "x=1")).against(desc)).isEmpty();
                softly.assertThat(problemsOf(call("a=1", "y=2", "z=3")).against(desc)).isEmpty();
                softly.assertThat(problemsOf(call("x=1", "y=2", "c=3")).against(desc)).isEmpty();
                softly.assertThat(problemsOf(call("x=1    b=2", "z=3")).against(desc)).isEmpty();
                softly.assertThat(problemsOf(call("x=1", "b=2", "${s}")).against(desc)).isEmpty();
                softly.assertThat(problemsOf(call("x=1", "y=2", "z=3", "z=4", "y=5", "x=6")).against(desc)).isEmpty();
                softly.assertThat(problemsOf(call("${s}")).against(desc)).isEmpty();
                softly.assertThat(problemsOf(call("${s}", "${s}2", "${s}3")).against(desc)).isEmpty();
                softly.assertThat(problemsOf(call("@{l}")).against(desc)).isEmpty();
                softly.assertThat(problemsOf(call("@{l}", "@{l}")).against(desc)).isEmpty();
                softly.assertThat(problemsOf(call("@{l}", "@{l}")).against(desc)).isEmpty();
                softly.assertThat(problemsOf(call("@{l}", "2", "3")).against(desc)).isEmpty();
                softly.assertThat(problemsOf(call("1", "@{l}")).against(desc)).isEmpty();
                softly.assertThat(problemsOf(call("1", "@{l}", "3")).against(desc)).isEmpty();
                softly.assertThat(problemsOf(call("1", "@{l}", "3", "4")).against(desc)).isEmpty();
                softly.assertThat(problemsOf(call("1", "@{l}", "3", "4", "5")).against(desc)).isEmpty();
                softly.assertThat(problemsOf(call("@{l}[0]")).against(desc)).isEmpty();
                softly.assertThat(problemsOf(call("${l}")).against(desc)).isEmpty();
                softly.assertThat(problemsOf(call("${l}", "${l}", "${l}")).against(desc)).isEmpty();
                softly.assertThat(problemsOf(call("&{d}[x]")).against(desc)).isEmpty();
                softly.assertThat(problemsOf(call("${d}")).against(desc)).isEmpty();
                softly.assertThat(problemsOf(call("${d}", "${d}", "${d}")).against(desc)).isEmpty();
            });
        }

        @Test
        public void invalidOrderIsReported_whenArgumentIsGivenAfterDictionary() {
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(problemsOf(call("&{d}", "2")).against(desc)).extracting(ALL).containsOnly(
                        problem(ORDER_PROBLEM, 47, 48, "Positional argument cannot be used after named arguments"));
                softly.assertThat(problemsOf(call("&{d}", "b=2")).against(desc)).extracting(ALL).containsOnly(
                        problem(ORDER_PROBLEM, 47, 50, "Positional argument cannot be used after named arguments. "
                                        + "Although this argument looks like named one, it isn't because there is no "
                                        + "'b' argument in the keyword definition"));
                softly.assertThat(problemsOf(call("1", "&{d}", "3")).against(desc)).extracting(ALL).containsOnly(
                        problem(ORDER_PROBLEM, 52, 53, "Positional argument cannot be used after named arguments"));
                softly.assertThat(problemsOf(call("1", "&{d}", "3", "4")).against(desc)).extracting(ALL).containsOnly(
                        problem(ORDER_PROBLEM, 52, 53, "Positional argument cannot be used after named arguments"),
                        problem(ORDER_PROBLEM, 57, 58, "Positional argument cannot be used after named arguments"));
                softly.assertThat(problemsOf(call("1", "&{d}", "c=3")).against(desc)).extracting(ALL).containsOnly(
                        problem(ORDER_PROBLEM, 52, 55, "Positional argument cannot be used after named arguments. "
                                        + "Although this argument looks like named one, it isn't because there is no "
                                        + "'c' argument in the keyword definition"));
                softly.assertThat(problemsOf(call("1", "&{d}", "c=3", "d=4")).against(desc)).extracting(ALL).containsOnly(
                        problem(ORDER_PROBLEM, 52, 55, "Positional argument cannot be used after named arguments. "
                                        + "Although this argument looks like named one, it isn't because there is no "
                                        + "'c' argument in the keyword definition"),
                        problem(ORDER_PROBLEM, 59, 62, "Positional argument cannot be used after named arguments. "
                                        + "Although this argument looks like named one, it isn't because there is no "
                                        + "'d' argument in the keyword definition"));
            });
        }

        @Test
        public void dictionariesAreReportedToBeEmpty_whenUsedAsLastArgument() {
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(problemsOf(call("&{d}")).against(desc)).extracting(ALL).containsOnly(
                        problem(COLLECTION_WARNING, 39, 43, "Dictionary argument '&{d}' has to be empty"));
                softly.assertThat(problemsOf(call("&{d}", "&{d}")).against(desc)).extracting(ALL).containsOnly(
                        problem(COLLECTION_WARNING, 39, 43, "Dictionary argument '&{d}' has to be empty"),
                        problem(COLLECTION_WARNING, 47, 51, "Dictionary argument '&{d}' has to be empty"));
                softly.assertThat(problemsOf(call("1", "&{d}")).against(desc)).extracting(ALL).containsOnly(
                        problem(COLLECTION_WARNING, 44, 48, "Dictionary argument '&{d}' has to be empty"));
            });
        }
    }

    public static class OnlyKwargTest {

        private final ArgumentsDescriptor desc = createDescriptor("**kwargs");

        @Test
        public void noProblemsReported_whenArgumentsAreUsedProperly() {
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(problemsOf(call()).against(desc)).isEmpty();
                softly.assertThat(problemsOf(call("a=1")).against(desc)).isEmpty();
                softly.assertThat(problemsOf(call("a=1", "b=2")).against(desc)).isEmpty();
                softly.assertThat(problemsOf(call("a=1", "b=2", "c=3")).against(desc)).isEmpty();
                softly.assertThat(problemsOf(call("a=1", "c=3", "b=2")).against(desc)).isEmpty();
                softly.assertThat(problemsOf(call("c=3", "a=1", "b=2")).against(desc)).isEmpty();
                softly.assertThat(problemsOf(call("a=1", "b=2", "c=3", "c=4", "b=5", "a=6")).against(desc)).isEmpty();
                softly.assertThat(problemsOf(call("&{d}")).against(desc)).isEmpty();
                softly.assertThat(problemsOf(call("&{d}", "&{d}")).against(desc)).isEmpty();
                softly.assertThat(problemsOf(call("a=1", "&{d}")).against(desc)).isEmpty();
                softly.assertThat(problemsOf(call("&{d}", "c=3")).against(desc)).isEmpty();
                softly.assertThat(problemsOf(call("a=1", "&{d}", "c=3")).against(desc)).isEmpty();
            });
        }

        @Test
        public void invalidOrderIsReported_whenPositionalArgumentsAreUsedAfterNamed() {
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(problemsOf(call("a=1", "2")).against(desc)).extracting(ALL).containsOnly(
                        problem(ORDER_PROBLEM, 46, 47, "Positional argument cannot be used after named arguments"));
                softly.assertThat(problemsOf(call("a=1", "b=2", "${s}")).against(desc)).extracting(ALL).containsOnly(
                        problem(ORDER_PROBLEM, 53, 57, "Positional argument cannot be used after named arguments"));
                softly.assertThat(problemsOf(call("&{d}", "2")).against(desc)).extracting(ALL).containsOnly(
                        problem(ORDER_PROBLEM, 47, 48, "Positional argument cannot be used after named arguments"));
                softly.assertThat(problemsOf(call("1", "&{d}", "3")).against(desc)).extracting(ALL).containsOnly(
                        problem(ORDER_PROBLEM, 52, 53, "Positional argument cannot be used after named arguments"));
                softly.assertThat(problemsOf(call("1", "&{d}", "3", "4")).against(desc)).extracting(ALL).containsOnly(
                        problem(ORDER_PROBLEM, 52, 53, "Positional argument cannot be used after named arguments"),
                        problem(ORDER_PROBLEM, 57, 58, "Positional argument cannot be used after named arguments"));
            });
        }

        @Test
        public void invalidNoOfNonKeywordArgumentsReported() {
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(problemsOf(call("1")).against(desc)).extracting(ALL).containsExactly(
                        problem(NUMBER_KW_PROBLEM, 28, 35,
                            "Invalid number of non-keyword arguments. Keyword 'keyword' expects 0 non-keyword arguments but 1 is provided"));
                softly.assertThat(problemsOf(call("1", "2")).against(desc)).extracting(ALL).containsExactly(
                        problem(NUMBER_KW_PROBLEM, 28, 35,
                            "Invalid number of non-keyword arguments. Keyword 'keyword' expects 0 non-keyword arguments but 2 are provided"));
                softly.assertThat(problemsOf(call("1", "2", "3")).against(desc)).extracting(ALL).containsExactly(
                        problem(NUMBER_KW_PROBLEM, 28, 35,
                            "Invalid number of non-keyword arguments. Keyword 'keyword' expects 0 non-keyword arguments but 3 are provided"));
                softly.assertThat(problemsOf(call("1", "2", "3", "4")).against(desc)).extracting(ALL).containsExactly(
                        problem(NUMBER_KW_PROBLEM, 28, 35,
                            "Invalid number of non-keyword arguments. Keyword 'keyword' expects 0 non-keyword arguments but 4 are provided"));
                softly.assertThat(problemsOf(call("1", "b=2")).against(desc)).extracting(ALL).containsExactly(
                        problem(NUMBER_KW_PROBLEM, 28, 35,
                            "Invalid number of non-keyword arguments. Keyword 'keyword' expects 0 non-keyword arguments but 1 is provided"));
                softly.assertThat(problemsOf(call("${s}")).against(desc)).extracting(ALL).containsExactly(
                        problem(NUMBER_KW_PROBLEM, 28, 35,
                            "Invalid number of non-keyword arguments. Keyword 'keyword' expects 0 non-keyword arguments but 1 is provided"));
                softly.assertThat(problemsOf(call("${s}", "${s}", "${s}")).against(desc)).extracting(ALL).containsExactly(
                        problem(NUMBER_KW_PROBLEM, 28, 35,
                            "Invalid number of non-keyword arguments. Keyword 'keyword' expects 0 non-keyword arguments but 3 are provided"));
                softly.assertThat(problemsOf(call("${l}[0]")).against(desc)).extracting(ALL).containsExactly(
                        problem(NUMBER_KW_PROBLEM, 28, 35,
                            "Invalid number of non-keyword arguments. Keyword 'keyword' expects 0 non-keyword arguments but 1 is provided"));
                softly.assertThat(problemsOf(call("${d}[a]")).against(desc)).extracting(ALL).containsExactly(
                        problem(NUMBER_KW_PROBLEM, 28, 35,
                            "Invalid number of non-keyword arguments. Keyword 'keyword' expects 0 non-keyword arguments but 1 is provided"));
                softly.assertThat(problemsOf(call("@{l}", "2", "3")).against(desc)).extracting(ALL).containsExactly(
                        problem(NUMBER_KW_PROBLEM, 28, 35,
                            "Invalid number of non-keyword arguments. Keyword 'keyword' expects 0 non-keyword arguments but at least 2 are provided"));
                softly.assertThat(problemsOf(call("1", "@{l}")).against(desc)).extracting(ALL).containsExactly(
                        problem(NUMBER_KW_PROBLEM, 28, 35,
                            "Invalid number of non-keyword arguments. Keyword 'keyword' expects 0 non-keyword arguments but at least 1 is provided"));
                softly.assertThat(problemsOf(call("1", "@{l}", "3")).against(desc)).extracting(ALL).containsExactly(
                        problem(NUMBER_KW_PROBLEM, 28, 35,
                            "Invalid number of non-keyword arguments. Keyword 'keyword' expects 0 non-keyword arguments but at least 2 are provided"));
                softly.assertThat(problemsOf(call("1", "@{l}", "3", "4")).against(desc)).extracting(ALL).containsExactly(
                        problem(NUMBER_KW_PROBLEM, 28, 35,
                            "Invalid number of non-keyword arguments. Keyword 'keyword' expects 0 non-keyword arguments but at least 3 are provided"));
                softly.assertThat(problemsOf(call("1", "@{l}", "3", "4", "5")).against(desc)).extracting(ALL).containsExactly(
                        problem(NUMBER_KW_PROBLEM, 28, 35,
                            "Invalid number of non-keyword arguments. Keyword 'keyword' expects 0 non-keyword arguments but at least 4 are provided"));
                softly.assertThat(problemsOf(call("1", "&{d}")).against(desc)).extracting(ALL).containsExactly(
                        problem(NUMBER_KW_PROBLEM, 28, 35,
                            "Invalid number of non-keyword arguments. Keyword 'keyword' expects 0 non-keyword arguments but 1 is provided"));
                softly.assertThat(problemsOf(call("1", "&{d}", "c=3")).against(desc)).extracting(ALL).containsExactly(
                        problem(NUMBER_KW_PROBLEM, 28, 35,
                            "Invalid number of non-keyword arguments. Keyword 'keyword' expects 0 non-keyword arguments but 1 is provided"));
                softly.assertThat(problemsOf(call("1", "&{d}", "c=3", "d=4")).against(desc)).extracting(ALL).containsExactly(
                        problem(NUMBER_KW_PROBLEM, 28, 35,
                            "Invalid number of non-keyword arguments. Keyword 'keyword' expects 0 non-keyword arguments but 1 is provided"));
            });
        }
    }

    public static class TwoArgsRequiredAndOptionalTest {

        private final ArgumentsDescriptor twoArgs = createDescriptor("a", "b=2");

        private final ArgumentsDescriptor fourArgs = createDescriptor("a", "b", "c=3", "d=4");

        @Test
        public void noProblemsReported_whenArgumentsAreUsedProperly() {
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(problemsOf(call("1")).against(twoArgs)).isEmpty();
                softly.assertThat(problemsOf(call("1", "2")).against(twoArgs)).isEmpty();
                softly.assertThat(problemsOf(call("x=1")).against(twoArgs)).isEmpty();
                softly.assertThat(problemsOf(call("x=1", "y=2")).against(twoArgs)).isEmpty();
                softly.assertThat(problemsOf(call("a=1")).against(twoArgs)).isEmpty();
                softly.assertThat(problemsOf(call("a=1", "b=2")).against(twoArgs)).isEmpty();
                softly.assertThat(problemsOf(call("${s}")).against(twoArgs)).isEmpty();
                softly.assertThat(problemsOf(call("@{l}[0]")).against(twoArgs)).isEmpty();
                softly.assertThat(problemsOf(call("&{d}[a]")).against(twoArgs)).isEmpty();

                softly.assertThat(problemsOf(call("1", "2")).against(fourArgs)).isEmpty();
                softly.assertThat(problemsOf(call("1", "2", "3")).against(fourArgs)).isEmpty();
                softly.assertThat(problemsOf(call("1", "2", "3", "4")).against(fourArgs)).isEmpty();
                softly.assertThat(problemsOf(call("x=1", "y=2")).against(fourArgs)).isEmpty();
                softly.assertThat(problemsOf(call("x=1", "y=2", "z=3")).against(fourArgs)).isEmpty();
                softly.assertThat(problemsOf(call("a=1", "b=2", "c=3")).against(fourArgs)).isEmpty();
                softly.assertThat(problemsOf(call("a=1", "c=3", "b=2")).against(fourArgs)).isEmpty();
                softly.assertThat(problemsOf(call("c=3", "a=1", "b=2")).against(fourArgs)).isEmpty();
                softly.assertThat(problemsOf(call("${s}1", "${s}2", "${s}3")).against(fourArgs)).isEmpty();
                softly.assertThat(problemsOf(call("${s}", "${s}", "${s}", "${s}")).against(fourArgs)).isEmpty();
            });
        }

        @Test
        public void overriddenNamedArgumentsAreReported() {
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(problemsOf(call("a=1", "a=2")).against(twoArgs)).extracting(ALL).containsOnly(
                        problem(OVERRIDDEN_ARG_PROBLEM, 39, 42,
                                "Argument 'a' is passed multiple times using named syntax. This value will never be used"));
                softly.assertThat(problemsOf(call("a=1", "b=2", "c=3",  "b=4", "a=5")).against(fourArgs)).extracting(ALL).containsOnly(
                        problem(OVERRIDDEN_ARG_PROBLEM, 39, 42,
                                "Argument 'a' is passed multiple times using named syntax. This value will never be used"),
                        problem(OVERRIDDEN_ARG_PROBLEM, 46, 49,
                                "Argument 'b' is passed multiple times using named syntax. This value will never be used"));
            });
        }

        @Test
        public void invalidOrderIsReported_whenPositionalArgumentsAreUsedAfterNamedOne() {
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(problemsOf(call("a=1", "2")).against(twoArgs)).extracting(ALL).containsOnly(
                        problem(ORDER_PROBLEM, 46, 47, "Positional argument cannot be used after named arguments"));
                softly.assertThat(problemsOf(call("a=1", "x=2")).against(twoArgs)).extracting(ALL).containsOnly(
                        problem(ORDER_PROBLEM, 46, 49, "Positional argument cannot be used after named arguments. "
                                    + "Although this argument looks like named one, it isn't because there is no "
                                    + "'x' argument in the keyword definition"));
                softly.assertThat(problemsOf(call("&{d}", "2")).against(twoArgs)).extracting(ALL).containsOnly(
                        problem(ORDER_PROBLEM, 47, 48, "Positional argument cannot be used after named arguments"));
                softly.assertThat(problemsOf(call("&{d}", "x=2")).against(twoArgs)).extracting(ALL).containsOnly(
                        problem(ORDER_PROBLEM, 47, 50, "Positional argument cannot be used after named arguments. "
                                    + "Although this argument looks like named one, it isn't because there is no "
                                    + "'x' argument in the keyword definition"));

                softly.assertThat(problemsOf(call("a=1", "b=2", "x=3")).against(fourArgs)).extracting(ALL).containsOnly(
                        problem(ORDER_PROBLEM, 53, 56, "Positional argument cannot be used after named arguments. "
                                    + "Although this argument looks like named one, it isn't because there is no "
                                    + "'x' argument in the keyword definition"));
                softly.assertThat(problemsOf(call("a=1", "x=2", "c=3")).against(fourArgs)).extracting(ALL).containsOnly(
                        problem(ORDER_PROBLEM, 46, 49, "Positional argument cannot be used after named arguments. "
                                    + "Although this argument looks like named one, it isn't because there is no "
                                    + "'x' argument in the keyword definition"));
                softly.assertThat(problemsOf(call("a=1", "x=2", "${s}")).against(fourArgs)).extracting(ALL).containsOnly(
                    problem(ORDER_PROBLEM, 46, 49, "Positional argument cannot be used after named arguments. "
                                    + "Although this argument looks like named one, it isn't because there is no "
                                    + "'x' argument in the keyword definition"),
                    problem(ORDER_PROBLEM, 53, 57, "Positional argument cannot be used after named arguments"));
                softly.assertThat(problemsOf( call("1", "&{d}", "3")).against(fourArgs)).extracting(ALL).containsOnly(
                    problem(ORDER_PROBLEM, 52, 53, "Positional argument cannot be used after named arguments"));
                softly.assertThat(problemsOf( call("1", "&{d}", "3", "4")).against(fourArgs)).extracting(ALL).containsOnly(
                    problem(ORDER_PROBLEM, 52, 53, "Positional argument cannot be used after named arguments"),
                    problem(ORDER_PROBLEM, 57, 58, "Positional argument cannot be used after named arguments"));
            });
        }

        @Test
        public void invalidNoOfArgumentsReported_whenNumberOfCallSiteArgumentsIsTooLowOrTooHigh() {
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(problemsOf(call()).against(twoArgs)).extracting(ALL).containsOnly(
                        problem(NUMBER_PROBLEM, 28, 35,
                                "Invalid number of arguments. Keyword 'keyword' expects from 1 to 2 arguments but 0 are provided"));
                softly.assertThat(problemsOf(call("1", "2", "3")).against(twoArgs)).extracting(ALL).containsOnly(
                        problem(NUMBER_PROBLEM, 28, 35,
                                "Invalid number of arguments. Keyword 'keyword' expects from 1 to 2 arguments but 3 are provided"));
                softly.assertThat(problemsOf(call("@{l}", "2", "3", "4")).against(twoArgs)).extracting(ALL).containsOnly(
                        problem(NUMBER_PROBLEM, 28, 35,
                                "Invalid number of arguments. Keyword 'keyword' expects from 1 to 2 arguments but at least 3 are provided"));

                softly.assertThat(problemsOf(call()).against(fourArgs)).extracting(ALL).containsOnly(
                        problem(NUMBER_PROBLEM, 28, 35,
                                "Invalid number of arguments. Keyword 'keyword' expects from 2 to 4 arguments but 0 are provided"));
                softly.assertThat(problemsOf(call("1")).against(fourArgs)).extracting(ALL).containsOnly(
                        problem(NUMBER_PROBLEM, 28, 35,
                                "Invalid number of arguments. Keyword 'keyword' expects from 2 to 4 arguments but 1 is provided"));
                softly.assertThat(problemsOf(call("1", "2", "3", "4", "5")).against(fourArgs)).extracting(ALL).containsOnly(
                        problem(NUMBER_PROBLEM, 28, 35,
                                "Invalid number of arguments. Keyword 'keyword' expects from 2 to 4 arguments but 5 are provided"));
                softly.assertThat(problemsOf(call("1", "@{l}", "3", "4", "5", "6")).against(fourArgs)).extracting(ALL).containsOnly(
                        problem(NUMBER_PROBLEM, 28, 35,
                                "Invalid number of arguments. Keyword 'keyword' expects from 2 to 4 arguments but at least 5 are provided"));
                softly.assertThat(problemsOf(call("1", "2", "3", "4", "5", "&{d}")).against(fourArgs)).extracting(ALL).containsOnly(
                        problem(NUMBER_PROBLEM, 28, 35,
                                "Invalid number of arguments. Keyword 'keyword' expects from 2 to 4 arguments but 5 are provided"));
                softly.assertThat(problemsOf(call("${s}")).against(fourArgs)).extracting(ALL).containsOnly(
                        problem(NUMBER_PROBLEM, 28, 35,
                                "Invalid number of arguments. Keyword 'keyword' expects from 2 to 4 arguments but 1 is provided"));
            });
        }

        @Test
        public void multipleMatchesAreReported_whenArgIsPassedPositionallyAndByName() {
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(problemsOf(call("1", "a=2")).against(twoArgs)).extracting(ALL).containsOnly(
                        problem(MULTIPLE_MATCH_PROBLEM, 44, 47, "Argument 'a' has value already passed (1)"));

                softly.assertThat(problemsOf(call("1", "a=2", "c=3")).against(fourArgs)).extracting(ALL).containsOnly(
                        problem(MULTIPLE_MATCH_PROBLEM, 44, 47, "Argument 'a' has value already passed (1)"));
                softly.assertThat(problemsOf(call("1", "a=2", "b=2", "c=3")).against(fourArgs)).extracting(ALL).containsOnly(
                        problem(MULTIPLE_MATCH_PROBLEM, 44, 47, "Argument 'a' has value already passed (1)"));
                softly.assertThat(problemsOf(call("1", "2", "a=3", "b=4")).against(fourArgs)).extracting(ALL).containsOnly(
                        problem(MULTIPLE_MATCH_PROBLEM, 49, 52, "Argument 'a' has value already passed (1)"),
                        problem(MULTIPLE_MATCH_PROBLEM, 56, 59, "Argument 'b' has value already passed (2)"));
            });
        }

        @Test
        public void missingArgumentsAreReported_whenItIsNotProvidedAtCallSite() {
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(problemsOf(call("b=1")).against(twoArgs)).extracting(ALL).containsOnly(
                        problem(MISSING_PROBLEM, 28, 35, "Keyword 'keyword' requires (a) argument to be specified"));

                softly.assertThat(problemsOf(call("b=1", "d=2")).against(fourArgs)).extracting(ALL).containsOnly(
                        problem(MISSING_PROBLEM, 28, 35, "Keyword 'keyword' requires (a) argument to be specified"));
                softly.assertThat(problemsOf(call("1", "d=2")).against(fourArgs)).extracting(ALL).containsOnly(
                        problem(MISSING_PROBLEM, 28, 35, "Keyword 'keyword' requires (b) argument to be specified"));
            });
        }

        @Test
        public void listsAreReported_whenUsedAsArgumentInOrderToProvideArguments() {
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(problemsOf(call("@{l}")).against(twoArgs)).extracting(ALL).containsOnly(
                        problem(COLLECTION_WARNING, 39, 43, "List argument '@{l}' has to contain from 1 to 2 items"));
                softly.assertThat(problemsOf(call("@{l}", "@{l}")).against(twoArgs)).extracting(ALL).containsOnly(
                        problem(COLLECTION_WARNING, 39, 43, "List argument '@{l}' has to be empty"),
                        problem(COLLECTION_WARNING, 47, 51, "List argument '@{l}' has to contain from 1 to 2 items"));
                softly.assertThat(problemsOf(call("@{l}", "2", "3")).against(twoArgs)).extracting(ALL).containsOnly(
                        problem(COLLECTION_WARNING, 39, 43, "List argument '@{l}' has to be empty"));

                softly.assertThat(problemsOf(call("@{l}")).against(fourArgs)).extracting(ALL).containsOnly(
                        problem(COLLECTION_WARNING, 39, 43, "List argument '@{l}' has to contain from 2 to 4 items"));
                softly.assertThat(problemsOf(call("@{l}", "@{l}")).against(fourArgs)).extracting(ALL).containsOnly(
                        problem(COLLECTION_WARNING, 39, 43, "List argument '@{l}' has to be empty"),
                        problem(COLLECTION_WARNING, 47, 51, "List argument '@{l}' has to contain from 2 to 4 items"));
                softly.assertThat(problemsOf(call("1", "@{l}")).against(fourArgs)).extracting(ALL).containsOnly(
                        problem(COLLECTION_WARNING, 44, 48, "List argument '@{l}' has to contain from 1 to 3 items"));
                softly.assertThat(problemsOf(call("1", "@{l}", "3")).against(fourArgs)).extracting(ALL).containsOnly(
                        problem(COLLECTION_WARNING, 44, 48, "List argument '@{l}' has to contain from 0 to 2 items"));
                softly.assertThat(problemsOf(call("1", "@{l}", "3", "4")).against(fourArgs)).extracting(ALL).containsOnly(
                                problem(COLLECTION_WARNING, 44, 48,
                                        "List argument '@{l}' has to contain from 0 to 1 item"));
                softly.assertThat(problemsOf(call("1", "@{l}", "3", "4", "5")).against(fourArgs)).extracting(ALL).containsOnly(
                                problem(COLLECTION_WARNING, 44, 48, "List argument '@{l}' has to be empty"));
            });
        }

        @Test
        public void dictionariesAreReported_whenUsedAsArgumentInOrderToProvideArguments() {
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(problemsOf(call("&{d}")).against(twoArgs)).extracting(ALL).containsOnly(
                        problem(COLLECTION_WARNING, 39, 43,
                                "Dictionary argument '&{d}' has to contain from 1 to 2 mappings. Required key: (a), possible key: (b)"));
                softly.assertThat(problemsOf(call("&{d}", "&{d}")).against(twoArgs)).extracting(ALL).containsOnly(
                        problem(COLLECTION_WARNING, 39, 43, "Dictionary argument '&{d}' has to be empty"),
                        problem(COLLECTION_WARNING, 47, 51,
                                "Dictionary argument '&{d}' has to contain from 1 to 2 mappings. Required key: (a), possible key: (b)"));
                softly.assertThat(problemsOf(call("&{d}", "a=2")).against(twoArgs)).extracting(ALL).containsOnly(
                        problem(COLLECTION_WARNING, 39, 43,
                                "Dictionary argument '&{d}' has to contain from 0 to 1 mapping. Possible key: (b)"));

                softly.assertThat(problemsOf(call("&{d}")).against(fourArgs)).extracting(ALL).containsOnly(
                        problem(COLLECTION_WARNING, 39, 43,
                                "Dictionary argument '&{d}' has to contain from 2 to 4 mappings. Required keys: (a, b), possible keys: (c, d)"));
                softly.assertThat(problemsOf(call("&{d}", "&{d}")).against(fourArgs)).extracting(ALL).containsOnly(
                        problem(COLLECTION_WARNING, 39, 43, "Dictionary argument '&{d}' has to be empty"),
                        problem(COLLECTION_WARNING, 47, 51,
                                "Dictionary argument '&{d}' has to contain from 2 to 4 mappings. Required keys: (a, b), possible keys: (c, d)"));
                softly.assertThat(problemsOf(call("1", "&{d}")).against(fourArgs)).extracting(ALL).containsOnly(
                        problem(COLLECTION_WARNING, 44, 48,
                                "Dictionary argument '&{d}' has to contain from 1 to 3 mappings. Required key: (b), possible keys: (c, d), forbidden key: (a)"));
                softly.assertThat(problemsOf(call("1", "&{d}", "b=2", "c=3")).against(fourArgs)).extracting(ALL).containsOnly(
                        problem(COLLECTION_WARNING, 44, 48,
                                "Dictionary argument '&{d}' has to contain from 0 to 1 mapping. Possible key: (d), forbidden key: (a)"));
                softly.assertThat(problemsOf(call("1", "&{d}", "b=2", "c=3", "d=3")).against(fourArgs)).extracting(ALL).containsOnly(
                        problem(COLLECTION_WARNING, 44, 48, "Dictionary argument '&{d}' has to be empty"));
            });
        }
    }

    public static class TwoArgsRequiredAndVarargTest {

        private final ArgumentsDescriptor twoArgs = createDescriptor("a", "*vararg");

        private final ArgumentsDescriptor threeArgs = createDescriptor("a", "b", "*vararg");

        @Test
        public void noProblemsReported_whenArgumentsAreUsedProperly() {
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(problemsOf(call("1")).against(twoArgs)).isEmpty();
                softly.assertThat(problemsOf(call("1", "2")).against(twoArgs)).isEmpty();
                softly.assertThat(problemsOf(call("x=1")).against(twoArgs)).isEmpty();
                softly.assertThat(problemsOf(call("x=1", "y=2")).against(twoArgs)).isEmpty();
                softly.assertThat(problemsOf(call("a=1")).against(twoArgs)).isEmpty();
                softly.assertThat(problemsOf(call("${s}")).against(twoArgs)).isEmpty();
                softly.assertThat(problemsOf(call("@{l}", "2")).against(twoArgs)).isEmpty();
                softly.assertThat(problemsOf(call("@{l}", "2", "3", "4")).against(twoArgs)).isEmpty();
                softly.assertThat(problemsOf(call("@{l}[0]")).against(twoArgs)).isEmpty();
                softly.assertThat(problemsOf(call("&{d}[a]")).against(twoArgs)).isEmpty();

                softly.assertThat(problemsOf(call("1", "2")).against(threeArgs)).isEmpty();
                softly.assertThat(problemsOf(call("1", "2", "3")).against(threeArgs)).isEmpty();
                softly.assertThat(problemsOf(call("1", "2", "3", "4")).against(threeArgs)).isEmpty();
                softly.assertThat(problemsOf(call("x=1", "y=2")).against(threeArgs)).isEmpty();
                softly.assertThat(problemsOf(call("x=1", "y=2", "z=3")).against(threeArgs)).isEmpty();
                softly.assertThat(problemsOf(call("a=1", "b=2")).against(threeArgs)).isEmpty();
                softly.assertThat(problemsOf(call("1", "@{l}", "3")).against(threeArgs)).isEmpty();
                softly.assertThat(problemsOf(call("1", "@{l}", "3", "4", "5")).against(threeArgs)).isEmpty();
                softly.assertThat(problemsOf(call("${s}1", "${s}2", "${s}3")).against(threeArgs)).isEmpty();
                softly.assertThat(problemsOf(call("${s}", "${s}", "${s}", "${s}")).against(threeArgs)).isEmpty();
            });
        }

        @Test
        public void overriddenNamedArgumentsAreReported() {
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(problemsOf(call("a=1", "a=2")).against(twoArgs)).extracting(ALL).containsOnly(
                        problem(OVERRIDDEN_ARG_PROBLEM, 39, 42,
                            "Argument 'a' is passed multiple times using named syntax. This value will never be used"));
                softly.assertThat(problemsOf(call("a=1", "b=2", "b=3", "a=4")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(OVERRIDDEN_ARG_PROBLEM, 39, 42,
                            "Argument 'a' is passed multiple times using named syntax. This value will never be used"),
                        problem(OVERRIDDEN_ARG_PROBLEM, 46, 49,
                            "Argument 'b' is passed multiple times using named syntax. This value will never be used"));
            });
        }

        @Test
        public void invalidOrderIsReported_whenPositionalArgumentsAreUsedAfterNamedOne() {
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(problemsOf(call("a=1", "2")).against(twoArgs)).extracting(ALL).containsOnly(
                        problem(ORDER_PROBLEM, 46, 47, "Positional argument cannot be used after named arguments"));
                softly.assertThat(problemsOf(call("a=1", "b=2")).against(twoArgs)).extracting(ALL).containsOnly(
                        problem(ORDER_PROBLEM, 46, 49, "Positional argument cannot be used after named arguments. "
                                        + "Although this argument looks like named one, it isn't because there is no "
                                        + "'b' argument in the keyword definition"));
                softly.assertThat(problemsOf(call("&{d}", "2")).against(twoArgs)).extracting(ALL).containsOnly(
                        problem(ORDER_PROBLEM, 47, 48, "Positional argument cannot be used after named arguments"));
                softly.assertThat(problemsOf(call("&{d}", "b=2")).against(twoArgs)).extracting(ALL).containsOnly(
                        problem(ORDER_PROBLEM, 47, 50, "Positional argument cannot be used after named arguments. "
                                    + "Although this argument looks like named one, it isn't because there is no "
                                    + "'b' argument in the keyword definition"));

                
                softly.assertThat(problemsOf(call("x=1", "a=2", "3")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(ORDER_PROBLEM, 53, 54, "Positional argument cannot be used after named arguments"));
                softly.assertThat(problemsOf(call("a=1", "b=2", "c=3")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(ORDER_PROBLEM, 53, 56, "Positional argument cannot be used after named arguments. "
                                    + "Although this argument looks like named one, it isn't because there is no "
                                    + "'c' argument in the keyword definition"));
                softly.assertThat(problemsOf(call("a=1", "x=2", "3")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(ORDER_PROBLEM, 46, 49, "Positional argument cannot be used after named arguments. "
                                    + "Although this argument looks like named one, it isn't because there is no "
                                    + "'x' argument in the keyword definition"),
                        problem(ORDER_PROBLEM, 53, 54, "Positional argument cannot be used after named arguments"));
                softly.assertThat(problemsOf(call("a=1", "x=2", "${s}")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(ORDER_PROBLEM, 46, 49, "Positional argument cannot be used after named arguments. "
                                    + "Although this argument looks like named one, it isn't because there is no "
                                    + "'x' argument in the keyword definition"),
                        problem(ORDER_PROBLEM, 53, 57, "Positional argument cannot be used after named arguments"));
                softly.assertThat(problemsOf(call("1", "&{d}", "3")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(ORDER_PROBLEM, 52, 53, "Positional argument cannot be used after named arguments"));
                softly.assertThat(problemsOf(call("1", "&{d}", "3", "4")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(ORDER_PROBLEM, 52, 53, "Positional argument cannot be used after named arguments"),
                        problem(ORDER_PROBLEM, 57, 58, "Positional argument cannot be used after named arguments"));
            });
        }

        @Test
        public void invalidNoOfArgumentsReported_whenNumberOfCallSiteArgumentsIsTooLow() {
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(problemsOf(call()).against(twoArgs)).extracting(ALL).containsOnly(
                        problem(NUMBER_PROBLEM, 28, 35,
                                "Invalid number of arguments. Keyword 'keyword' expects at least 1 argument but 0 are provided"));

                softly.assertThat(problemsOf(call()).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(NUMBER_PROBLEM, 28, 35,
                                "Invalid number of arguments. Keyword 'keyword' expects at least 2 arguments but 0 are provided"));
                softly.assertThat(problemsOf(call("1")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(NUMBER_PROBLEM, 28, 35,
                                "Invalid number of arguments. Keyword 'keyword' expects at least 2 arguments but 1 is provided"));
                softly.assertThat(problemsOf(call("a=1")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(NUMBER_PROBLEM, 28, 35,
                                "Invalid number of arguments. Keyword 'keyword' expects at least 2 arguments but 1 is provided"));
                softly.assertThat(problemsOf(call("${s}")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(NUMBER_PROBLEM, 28, 35,
                                "Invalid number of arguments. Keyword 'keyword' expects at least 2 arguments but 1 is provided"));
            });
        }

        @Test
        public void multipleMatchesAreReported_whenArgIsPassedPositionallyAndByName() {
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(problemsOf(call("1", "a=2")).against(twoArgs)).extracting(ALL).containsOnly(
                        problem(MULTIPLE_MATCH_PROBLEM, 44, 47, "Argument 'a' has value already passed (1)"));

                softly.assertThat(problemsOf(call("1", "a=2")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(MULTIPLE_MATCH_PROBLEM, 44, 47, "Argument 'a' has value already passed (1)"));
                softly.assertThat(problemsOf(call("1", "2", "a=3", "b=4")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(MULTIPLE_MATCH_PROBLEM, 49, 52, "Argument 'a' has value already passed (1)"),
                        problem(MULTIPLE_MATCH_PROBLEM, 56, 59, "Argument 'b' has value already passed (2)"));
            });
        }

        @Test
        public void listsAreReported_whenUsedAsArgumentInOrderToProvideArguments() {
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(problemsOf(call("@{l}")).against(twoArgs)).extracting(ALL).containsOnly(
                        problem(COLLECTION_WARNING, 39, 43, "List argument '@{l}' has to contain at least 1 item"));
                softly.assertThat(problemsOf(call("@{l}", "@{l}")).against(twoArgs)).extracting(ALL).containsOnly(
                        problem(COLLECTION_WARNING, 39, 43, "List argument '@{l}' has to be empty"),
                        problem(COLLECTION_WARNING, 47, 51, "List argument '@{l}' has to contain at least 1 item"));

                softly.assertThat(problemsOf(call("@{l}")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(COLLECTION_WARNING, 39, 43, "List argument '@{l}' has to contain at least 2 items"));
                softly.assertThat(problemsOf(call("@{l}", "@{l}")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(COLLECTION_WARNING, 39, 43, "List argument '@{l}' has to be empty"),
                        problem(COLLECTION_WARNING, 47, 51, "List argument '@{l}' has to contain at least 2 items"));
                softly.assertThat(problemsOf(call("1", "@{l}")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(COLLECTION_WARNING, 44, 48, "List argument '@{l}' has to contain at least 1 item"));
            });
        }

        @Test
        public void dictionariesAreReported_whenUsedAsArgumentInOrderToProvideArguments() {
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(problemsOf(call("&{d}")).against(twoArgs)).extracting(ALL).containsOnly(
                        problem(COLLECTION_WARNING, 39, 43,
                                "Dictionary argument '&{d}' has to contain 1 mapping. Required key: (a)"));
                softly.assertThat(problemsOf(call("&{d}", "&{d}")).against(twoArgs)).extracting(ALL).containsOnly(
                        problem(COLLECTION_WARNING, 39, 43, "Dictionary argument '&{d}' has to be empty"),
                        problem(COLLECTION_WARNING, 47, 51,
                                "Dictionary argument '&{d}' has to contain 1 mapping. Required key: (a)"));
                softly.assertThat(problemsOf(call("&{d}", "a=2")).against(twoArgs)).extracting(ALL).containsOnly(
                        problem(COLLECTION_WARNING, 39, 43, "Dictionary argument '&{d}' has to be empty"));

                softly.assertThat(problemsOf(call("&{d}")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(COLLECTION_WARNING, 39, 43,
                                "Dictionary argument '&{d}' has to contain 2 mappings. Required keys: (a, b)"));
                softly.assertThat(problemsOf(call("&{d}", "&{d}")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(COLLECTION_WARNING, 39, 43, "Dictionary argument '&{d}' has to be empty"),
                        problem(COLLECTION_WARNING, 47, 51,
                                "Dictionary argument '&{d}' has to contain 2 mappings. Required keys: (a, b)"));
                softly.assertThat(problemsOf(call("1", "&{d}")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(COLLECTION_WARNING, 44, 48,
                                "Dictionary argument '&{d}' has to contain 1 mapping. Required key: (b), forbidden key: (a)"));
                softly.assertThat(problemsOf(call("1", "2", "3", "4", "&{d}")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(COLLECTION_WARNING, 59, 63, "Dictionary argument '&{d}' has to be empty"));
            });
        }
    }

    public static class TwoArgsRequiredAndKwargTest {

        private final ArgumentsDescriptor twoArgs = createDescriptor("a", "**kwarg");

        private final ArgumentsDescriptor threeArgs = createDescriptor("a", "b", "**kwarg");

        @Test
        public void noProblemsReported_whenArgumentsAreUsedProperly() {
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(problemsOf(call("1")).against(twoArgs)).isEmpty();
                softly.assertThat(problemsOf(call("a=1")).against(twoArgs)).isEmpty();
                softly.assertThat(problemsOf(call("a=1", "b=2")).against(twoArgs)).isEmpty();
                softly.assertThat(problemsOf(call("a=1", "x=2")).against(twoArgs)).isEmpty();
                softly.assertThat(problemsOf(call("1", "x=2", "kwarg=3", "z=4")).against(twoArgs)).isEmpty();
                softly.assertThat(problemsOf(call("${s}")).against(twoArgs)).isEmpty();
                softly.assertThat(problemsOf(call("@{l}[0]")).against(twoArgs)).isEmpty();
                softly.assertThat(problemsOf(call("&{d}", "a=2")).against(twoArgs)).isEmpty();
                softly.assertThat(problemsOf(call("&{d}[a]")).against(twoArgs)).isEmpty();

                softly.assertThat(problemsOf(call("1", "2")).against(threeArgs)).isEmpty();
                softly.assertThat(problemsOf(call("a=1", "b=2")).against(threeArgs)).isEmpty();
                softly.assertThat(problemsOf(call("b=1", "a=2")).against(threeArgs)).isEmpty();
                softly.assertThat(problemsOf(call("b=1", "a=2", "c=3")).against(threeArgs)).isEmpty();
                softly.assertThat(problemsOf(call("b=1", "a=2", "x=3")).against(threeArgs)).isEmpty();
            });
        }

        @Test
        public void overriddenNamedArgumentsAreReported() {
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(problemsOf(call("a=1", "a=2")).against(twoArgs)).extracting(ALL).containsOnly(
                        problem(OVERRIDDEN_ARG_PROBLEM, 39, 42,
                                "Argument 'a' is passed multiple times using named syntax. This value will never be used"));
                softly.assertThat(problemsOf(call("a=1", "b=2", "b=3", "a=4")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(OVERRIDDEN_ARG_PROBLEM, 39, 42,
                                "Argument 'a' is passed multiple times using named syntax. This value will never be used"),
                        problem(OVERRIDDEN_ARG_PROBLEM, 46, 49,
                                "Argument 'b' is passed multiple times using named syntax. This value will never be used"));
            });
        }

        @Test
        public void invalidOrderIsReported_whenPositionalArgumentsAreUsedAfterNamedOne() {
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(problemsOf(call("a=1", "2")).against(twoArgs)).extracting(ALL).containsOnly(
                        problem(ORDER_PROBLEM, 46, 47, "Positional argument cannot be used after named arguments"));
                softly.assertThat(problemsOf(call("&{d}", "2")).against(twoArgs)).extracting(ALL).containsOnly(
                        problem(ORDER_PROBLEM, 47, 48, "Positional argument cannot be used after named arguments"));

                softly.assertThat(problemsOf(call("x=1", "a=2", "3")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(ORDER_PROBLEM, 53, 54, "Positional argument cannot be used after named arguments"));
                softly.assertThat(problemsOf(call("a=1", "x=2", "3")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(ORDER_PROBLEM, 53, 54, "Positional argument cannot be used after named arguments"));
                softly.assertThat(problemsOf(call("a=1", "x=2", "${s}")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(ORDER_PROBLEM, 53, 57, "Positional argument cannot be used after named arguments"));
                softly.assertThat(problemsOf(call("1", "&{d}", "3")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(ORDER_PROBLEM, 52, 53, "Positional argument cannot be used after named arguments"));
                softly.assertThat(problemsOf(call("1", "&{d}", "3", "4")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(ORDER_PROBLEM, 52, 53, "Positional argument cannot be used after named arguments"),
                        problem(ORDER_PROBLEM, 57, 58, "Positional argument cannot be used after named arguments"));
            });
        }

        @Test
        public void invalidNoOfNonKeywordArgumentsReported() {
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(problemsOf(call()).against(twoArgs)).extracting(ALL).containsOnly(
                        problem(NUMBER_KW_PROBLEM, 28, 35,
                                "Invalid number of non-keyword arguments. Keyword 'keyword' expects 1 non-keyword argument but 0 are provided"));
                softly.assertThat(problemsOf(call("1", "2")).against(twoArgs)).extracting(ALL).containsOnly(
                        problem(NUMBER_KW_PROBLEM, 28, 35,
                                "Invalid number of non-keyword arguments. Keyword 'keyword' expects 1 non-keyword argument but 2 are provided"));
                softly.assertThat(problemsOf(call("@{l}", "2", "3", "4")).against(twoArgs)).extracting(ALL).containsOnly(
                        problem(NUMBER_KW_PROBLEM, 28, 35,
                                "Invalid number of non-keyword arguments. Keyword 'keyword' expects 1 non-keyword argument but at least 3 are provided"));
                softly.assertThat(problemsOf(call("x=1")).against(twoArgs)).extracting(ALL).containsOnly(
                        problem(NUMBER_KW_PROBLEM, 28, 35,
                                "Invalid number of non-keyword arguments. Keyword 'keyword' expects 1 non-keyword argument but 0 are provided"));
                softly.assertThat(problemsOf(call("x=1", "y=2")).against(twoArgs)).extracting(ALL).containsOnly(
                        problem(NUMBER_KW_PROBLEM, 28, 35,
                                "Invalid number of non-keyword arguments. Keyword 'keyword' expects 1 non-keyword argument but 0 are provided"));

                softly.assertThat(problemsOf(call()).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(NUMBER_KW_PROBLEM, 28, 35,
                                "Invalid number of non-keyword arguments. Keyword 'keyword' expects 2 non-keyword arguments but 0 are provided"));
                softly.assertThat(problemsOf(call("1")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(NUMBER_KW_PROBLEM, 28, 35,
                                "Invalid number of non-keyword arguments. Keyword 'keyword' expects 2 non-keyword arguments but 1 is provided"));
                softly.assertThat(problemsOf(call("1", "2", "3")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(NUMBER_KW_PROBLEM, 28, 35,
                                "Invalid number of non-keyword arguments. Keyword 'keyword' expects 2 non-keyword arguments but 3 are provided"));
                softly.assertThat(problemsOf(call("1", "2", "3", "4")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(NUMBER_KW_PROBLEM, 28, 35,
                                "Invalid number of non-keyword arguments. Keyword 'keyword' expects 2 non-keyword arguments but 4 are provided"));
                softly.assertThat(problemsOf(call("1", "2", "3", "4", "5")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(NUMBER_KW_PROBLEM, 28, 35,
                                "Invalid number of non-keyword arguments. Keyword 'keyword' expects 2 non-keyword arguments but 5 are provided"));
                softly.assertThat(problemsOf(call("x=1")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(NUMBER_KW_PROBLEM, 28, 35,
                                "Invalid number of non-keyword arguments. Keyword 'keyword' expects 2 non-keyword arguments but 0 are provided"));
                softly.assertThat(problemsOf(call("x=1", "y=2")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(NUMBER_KW_PROBLEM, 28, 35,
                                "Invalid number of non-keyword arguments. Keyword 'keyword' expects 2 non-keyword arguments but 0 are provided"));
                softly.assertThat(problemsOf(call("x=1", "y=2", "z=3")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(NUMBER_KW_PROBLEM, 28, 35,
                                "Invalid number of non-keyword arguments. Keyword 'keyword' expects 2 non-keyword arguments but 0 are provided"));
                softly.assertThat(problemsOf(call("a=1", "x=2", "c=3")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(NUMBER_KW_PROBLEM, 28, 35,
                                "Invalid number of non-keyword arguments. Keyword 'keyword' expects 2 non-keyword arguments but 1 is provided"));
                softly.assertThat(problemsOf(call("1", "@{l}", "3", "4", "5")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(NUMBER_KW_PROBLEM, 28, 35,
                                "Invalid number of non-keyword arguments. Keyword 'keyword' expects 2 non-keyword arguments but at least 4 are provided"));
                softly.assertThat(problemsOf(call("${l}")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(NUMBER_KW_PROBLEM, 28, 35,
                                "Invalid number of non-keyword arguments. Keyword 'keyword' expects 2 non-keyword arguments but 1 is provided"));
                softly.assertThat(problemsOf(call("1", "2", "3", "4", "5", "&{d}")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(NUMBER_KW_PROBLEM, 28, 35,
                                "Invalid number of non-keyword arguments. Keyword 'keyword' expects 2 non-keyword arguments but 5 are provided"));
                softly.assertThat(problemsOf(call("${s}1", "${s}2", "${s}3")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(NUMBER_KW_PROBLEM, 28, 35,
                                "Invalid number of non-keyword arguments. Keyword 'keyword' expects 2 non-keyword arguments but 3 are provided"));
                softly.assertThat(problemsOf(call("${l}", "${l}", "${l}")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(NUMBER_KW_PROBLEM, 28, 35,
                                "Invalid number of non-keyword arguments. Keyword 'keyword' expects 2 non-keyword arguments but 3 are provided"));
            });

        }

        @Test
        public void multipleMatchesAreReported_whenArgIsPassedPositionallyAndByName() {
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(problemsOf(call("1", "a=2")).against(twoArgs)).extracting(ALL).containsOnly(
                        problem(MULTIPLE_MATCH_PROBLEM, 44, 47, "Argument 'a' has value already passed (1)"));

                softly.assertThat(problemsOf(call("1", "a=2")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(MULTIPLE_MATCH_PROBLEM, 44, 47, "Argument 'a' has value already passed (1)"));
                softly.assertThat(problemsOf(call("1", "2", "a=3", "b=4")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(MULTIPLE_MATCH_PROBLEM, 49, 52, "Argument 'a' has value already passed (1)"),
                        problem(MULTIPLE_MATCH_PROBLEM, 56, 59, "Argument 'b' has value already passed (2)"));
            });
        }

        @Test
        public void listsAreReported_whenUsedAsArgumentInOrderToProvideArguments() {
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(problemsOf(call("@{l}")).against(twoArgs)).extracting(ALL).containsOnly(
                        problem(COLLECTION_WARNING, 39, 43, "List argument '@{l}' has to contain 1 item"));
                softly.assertThat(problemsOf(call("@{l}", "@{l}")).against(twoArgs)).extracting(ALL).containsOnly(
                        problem(COLLECTION_WARNING, 39, 43, "List argument '@{l}' has to be empty"),
                        problem(COLLECTION_WARNING, 47, 51, "List argument '@{l}' has to contain 1 item"));
                softly.assertThat(problemsOf(call("@{l}", "2")).against(twoArgs)).extracting(ALL).containsOnly(
                        problem(COLLECTION_WARNING, 39, 43, "List argument '@{l}' has to be empty"));

                 softly.assertThat(problemsOf(call("@{l}")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(COLLECTION_WARNING, 39, 43, "List argument '@{l}' has to contain 2 items"));
                softly.assertThat(problemsOf(call("@{l}", "@{l}")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(COLLECTION_WARNING, 39, 43, "List argument '@{l}' has to be empty"),
                        problem(COLLECTION_WARNING, 47, 51, "List argument '@{l}' has to contain 2 items"));
                softly.assertThat(problemsOf(call("1", "@{l}")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(COLLECTION_WARNING, 44, 48, "List argument '@{l}' has to contain 1 item"));
                softly.assertThat(problemsOf(call("1", "@{l}", "3")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(COLLECTION_WARNING, 44, 48, "List argument '@{l}' has to be empty"));
            });
        }

        @Test
        public void dictionariesAreReported_whenUsedAsArgumentInOrderToProvideArguments() {
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(problemsOf(call("&{d}")).against(twoArgs)).extracting(ALL).containsOnly(
                        problem(COLLECTION_WARNING, 39, 43,
                                "Dictionary argument '&{d}' has to contain at least 1 mapping. Required key: (a)"));
                softly.assertThat(problemsOf(call("&{d}", "&{d}")).against(twoArgs)).extracting(ALL).containsOnly(
                        problem(COLLECTION_WARNING, 39, 43, "Dictionary argument '&{d}' has to be empty"),
                        problem(COLLECTION_WARNING, 47, 51,
                                "Dictionary argument '&{d}' has to contain at least 1 mapping. Required key: (a)"));
                softly.assertThat(problemsOf(call("&{d}", "b=2")).against(twoArgs)).extracting(ALL).containsOnly(
                        problem(COLLECTION_WARNING, 39, 43,
                                "Dictionary argument '&{d}' has to contain at least 1 mapping. Required key: (a)"));
                softly.assertThat(problemsOf(call("1", "&{d}")).against(twoArgs)).extracting(ALL).containsOnly(
                        problem(COLLECTION_WARNING, 44, 48, "Dictionary argument '&{d}' cannot have key: (a)"));

                softly.assertThat(problemsOf(call("&{d}")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(COLLECTION_WARNING, 39, 43,
                                "Dictionary argument '&{d}' has to contain at least 2 mappings. Required keys: (a, b)"));
                softly.assertThat(problemsOf(call("&{d}", "&{d}")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(COLLECTION_WARNING, 39, 43, "Dictionary argument '&{d}' has to be empty"),
                        problem(COLLECTION_WARNING, 47, 51,
                                "Dictionary argument '&{d}' has to contain at least 2 mappings. Required keys: (a, b)"));
                softly.assertThat(problemsOf(call("1", "&{d}")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(COLLECTION_WARNING, 44, 48,
                                "Dictionary argument '&{d}' has to contain at least 1 mapping. Required key: (b), forbidden key: (a)"));
            });
        }
    }

    public static class TwoArgsOptionalAndVarargsTest {

        private final ArgumentsDescriptor twoArgs = createDescriptor("a=1", "*vararg");

        private final ArgumentsDescriptor threeArgs = createDescriptor("a=1", "b=2", "*vararg");

        @Test
        public void noProblemsReported_whenArgumentsAreUsedProperly() {
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(problemsOf(call()).against(twoArgs)).isEmpty();
                softly.assertThat(problemsOf(call("1")).against(twoArgs)).isEmpty();
                softly.assertThat(problemsOf(call("1", "2")).against(twoArgs)).isEmpty();
                softly.assertThat(problemsOf(call("1", "2", "3")).against(twoArgs)).isEmpty();
                softly.assertThat(problemsOf(call("x=1")).against(twoArgs)).isEmpty();
                softly.assertThat(problemsOf(call("x=1", "y=2")).against(twoArgs)).isEmpty();
                softly.assertThat(problemsOf(call("a=1")).against(twoArgs)).isEmpty();
                softly.assertThat(problemsOf(call("${s}")).against(twoArgs)).isEmpty();
                softly.assertThat(problemsOf(call("@{l}")).against(twoArgs)).isEmpty();
                softly.assertThat(problemsOf(call("@{l}", "@{l}")).against(twoArgs)).isEmpty();
                softly.assertThat(problemsOf(call("@{l}", "2")).against(twoArgs)).isEmpty();
                softly.assertThat(problemsOf(call("@{l}", "2", "3", "4")).against(twoArgs)).isEmpty();
                softly.assertThat(problemsOf(call("@{l}[0]")).against(twoArgs)).isEmpty();
                softly.assertThat(problemsOf(call("&{d}[a]")).against(twoArgs)).isEmpty();

                softly.assertThat(problemsOf(call()).against(threeArgs)).isEmpty();
                softly.assertThat(problemsOf(call("1")).against(threeArgs)).isEmpty();
                softly.assertThat(problemsOf(call("1", "2")).against(threeArgs)).isEmpty();
                softly.assertThat(problemsOf(call("1", "2", "3")).against(threeArgs)).isEmpty();
                softly.assertThat(problemsOf(call("1", "2", "3", "4")).against(threeArgs)).isEmpty();
                softly.assertThat(problemsOf(call("1", "2", "3", "4", "5")).against(threeArgs)).isEmpty();
                softly.assertThat(problemsOf(call("x=1")).against(threeArgs)).isEmpty();
                softly.assertThat(problemsOf(call("x=1", "y=2")).against(threeArgs)).isEmpty();
                softly.assertThat(problemsOf(call("x=1", "y=2", "z=3")).against(threeArgs)).isEmpty();
                softly.assertThat(problemsOf(call("a=1")).against(threeArgs)).isEmpty();
                softly.assertThat(problemsOf(call("a=1", "b=2")).against(threeArgs)).isEmpty();
                softly.assertThat(problemsOf(call("b=2", "a=1")).against(threeArgs)).isEmpty();
                softly.assertThat(problemsOf(call("${s}1", "${s}2", "${s}3")).against(threeArgs)).isEmpty();
                softly.assertThat(problemsOf(call("@{l}")).against(threeArgs)).isEmpty();
                softly.assertThat(problemsOf(call("@{l}", "@{l}")).against(threeArgs)).isEmpty();
                softly.assertThat(problemsOf(call("1", "@{l}")).against(threeArgs)).isEmpty();
                softly.assertThat(problemsOf(call("1", "@{l}", "3")).against(threeArgs)).isEmpty();
                softly.assertThat(problemsOf(call("1", "@{l}", "3", "4", "5")).against(threeArgs)).isEmpty();
            });
        }

        @Test
        public void overriddenNamedArgumentsAreReported() {
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(problemsOf(call("a=1", "a=2")).against(twoArgs)).extracting(ALL).containsOnly(
                        problem(OVERRIDDEN_ARG_PROBLEM, 39, 42,
                                "Argument 'a' is passed multiple times using named syntax. This value will never be used"));
                softly.assertThat(problemsOf(call("a=1", "b=2", "b=3", "a=4")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(OVERRIDDEN_ARG_PROBLEM, 39, 42,
                                "Argument 'a' is passed multiple times using named syntax. This value will never be used"),
                        problem(OVERRIDDEN_ARG_PROBLEM, 46, 49,
                                "Argument 'b' is passed multiple times using named syntax. This value will never be used"));
            });
        }

        @Test
        public void invalidOrderIsReported_whenPositionalArgumentsAreUsedAfterNamedOne() {
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(problemsOf(call("a=1", "2")).against(twoArgs)).extracting(ALL).containsOnly(
                        problem(ORDER_PROBLEM, 46, 47, "Positional argument cannot be used after named arguments"));
                softly.assertThat(problemsOf(call("a=1", "b=2")).against(twoArgs)).extracting(ALL).containsOnly(
                        problem(ORDER_PROBLEM, 46, 49, "Positional argument cannot be used after named arguments. "
                                + "Although this argument looks like named one, it isn't because there is no "
                                + "'b' argument in the keyword definition"));
                softly.assertThat(problemsOf(call("&{d}", "2")).against(twoArgs)).extracting(ALL).containsOnly(
                        problem(ORDER_PROBLEM, 47, 48, "Positional argument cannot be used after named arguments"));
                softly.assertThat(problemsOf(call("&{d}", "b=2")).against(twoArgs)).extracting(ALL).containsOnly(
                        problem(ORDER_PROBLEM, 47, 50, "Positional argument cannot be used after named arguments. "
                                + "Although this argument looks like named one, it isn't because there is no "
                                + "'b' argument in the keyword definition"));

                softly.assertThat(problemsOf(call("x=1", "b=2", "3")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(ORDER_PROBLEM, 53, 54, "Positional argument cannot be used after named arguments"));
                softly.assertThat(problemsOf(call("a=1", "b=2", "x=3")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(ORDER_PROBLEM, 53, 56, "Positional argument cannot be used after named arguments. "
                                + "Although this argument looks like named one, it isn't because there is no "
                                + "'x' argument in the keyword definition"));
                softly.assertThat(problemsOf(call("a=1", "x=2", "c=3")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(ORDER_PROBLEM, 46, 49, "Positional argument cannot be used after named arguments. "
                                + "Although this argument looks like named one, it isn't because there is no "
                                + "'x' argument in the keyword definition"),
                        problem(ORDER_PROBLEM, 53, 56, "Positional argument cannot be used after named arguments. "
                                + "Although this argument looks like named one, it isn't because there is no "
                                + "'c' argument in the keyword definition"));
                softly.assertThat(problemsOf(call("a=1", "x=2", "${s}")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(ORDER_PROBLEM, 46, 49, "Positional argument cannot be used after named arguments. "
                                + "Although this argument looks like named one, it isn't because there is no "
                                + "'x' argument in the keyword definition"),
                        problem(ORDER_PROBLEM, 53, 57, "Positional argument cannot be used after named arguments"));
                softly.assertThat(problemsOf(call("1", "&{d}", "3")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(ORDER_PROBLEM, 52, 53, "Positional argument cannot be used after named arguments"));
                softly.assertThat(problemsOf(call("1", "&{d}", "3", "4")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(ORDER_PROBLEM, 52, 53, "Positional argument cannot be used after named arguments"),
                        problem(ORDER_PROBLEM, 57, 58, "Positional argument cannot be used after named arguments"));
            });
        }

        @Test
        public void multipleMatchesAreReported_whenArgIsPassedPositionallyAndByName() {
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(problemsOf(call("1", "a=2")).against(twoArgs)).extracting(ALL).containsOnly(
                        problem(MULTIPLE_MATCH_PROBLEM, 44, 47, "Argument 'a' has value already passed (1)"));

                softly.assertThat(problemsOf(call("1", "a=2")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(MULTIPLE_MATCH_PROBLEM, 44, 47, "Argument 'a' has value already passed (1)"));
                softly.assertThat(problemsOf(call("1", "2", "a=3", "b=4")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(MULTIPLE_MATCH_PROBLEM, 49, 52, "Argument 'a' has value already passed (1)"),
                        problem(MULTIPLE_MATCH_PROBLEM, 56, 59, "Argument 'b' has value already passed (2)"));
            });
        }

        @Test
        public void dictionariesAreReported_whenUsedAsArgumentInOrderToProvideArguments() {
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(problemsOf(call("&{d}")).against(twoArgs)).extracting(ALL).containsOnly(
                        problem(COLLECTION_WARNING, 39, 43,
                                "Dictionary argument '&{d}' has to contain from 0 to 1 mapping. Possible key: (a)"));
                softly.assertThat(problemsOf(call("&{d}", "&{d}")).against(twoArgs)).extracting(ALL).containsOnly(
                        problem(COLLECTION_WARNING, 39, 43, "Dictionary argument '&{d}' has to be empty"),
                        problem(COLLECTION_WARNING, 47, 51,
                                "Dictionary argument '&{d}' has to contain from 0 to 1 mapping. Possible key: (a)"));
                softly.assertThat(problemsOf(call("&{d}", "a=2")).against(twoArgs)).extracting(ALL).containsOnly(
                        problem(COLLECTION_WARNING, 39, 43, "Dictionary argument '&{d}' has to be empty"));
                softly.assertThat(problemsOf(call("1", "&{d}")).against(twoArgs)).extracting(ALL).containsOnly(
                        problem(COLLECTION_WARNING, 44, 48, "Dictionary argument '&{d}' has to be empty"));

                softly.assertThat(problemsOf(call("&{d}")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(COLLECTION_WARNING, 39, 43,
                                "Dictionary argument '&{d}' has to contain from 0 to 2 mappings. Possible keys: (a, b)"));
                softly.assertThat(problemsOf(call("&{d}", "&{d}")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(COLLECTION_WARNING, 39, 43, "Dictionary argument '&{d}' has to be empty"),
                        problem(COLLECTION_WARNING, 47, 51,
                                "Dictionary argument '&{d}' has to contain from 0 to 2 mappings. Possible keys: (a, b)"));
                softly.assertThat(problemsOf(call("1", "&{d}")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(COLLECTION_WARNING, 44, 48,
                                "Dictionary argument '&{d}' has to contain from 0 to 1 mapping. Possible key: (b), forbidden key: (a)"));
                softly.assertThat(problemsOf(call("1", "2", "3", "4", "5", "&{d}")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(COLLECTION_WARNING, 64, 68, "Dictionary argument '&{d}' has to be empty"));
            });
        }
    }

    public static class TwoArgsOptionalAndKwargsTest {

        private final ArgumentsDescriptor twoArgs = createDescriptor("a=1", "**kwarg");

        private final ArgumentsDescriptor threeArgs = createDescriptor("a=1", "b=2", "**kwarg");

        @Test
        public void noProblemsReported_whenArgumentsAreUsedProperly() {
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(problemsOf(call()).against(twoArgs)).isEmpty();
                softly.assertThat(problemsOf(call("1")).against(twoArgs)).isEmpty();
                softly.assertThat(problemsOf(call("x=1")).against(twoArgs)).isEmpty();
                softly.assertThat(problemsOf(call("x=1", "y=2")).against(twoArgs)).isEmpty();
                softly.assertThat(problemsOf(call("a=1")).against(twoArgs)).isEmpty();
                softly.assertThat(problemsOf(call("a=1", "x=2")).against(twoArgs)).isEmpty();
                softly.assertThat(problemsOf(call("1", "x=2", "kwargs=3", "z=4")).against(twoArgs)).isEmpty();
                softly.assertThat(problemsOf(call("${s}")).against(twoArgs)).isEmpty();
                softly.assertThat(problemsOf(call("&{d}")).against(twoArgs)).isEmpty();
                softly.assertThat(problemsOf(call("&{d}", "&{d}")).against(twoArgs)).isEmpty();
                softly.assertThat(problemsOf(call("&{d}", "a=2")).against(twoArgs)).isEmpty();
                softly.assertThat(problemsOf(call("&{d}", "x=2")).against(twoArgs)).isEmpty();
                softly.assertThat(problemsOf(call("@{l}[0]")).against(twoArgs)).isEmpty();
                softly.assertThat(problemsOf(call("&{d}[a]")).against(twoArgs)).isEmpty();

                softly.assertThat(problemsOf(call()).against(threeArgs)).isEmpty();
                softly.assertThat(problemsOf(call("1")).against(threeArgs)).isEmpty();
                softly.assertThat(problemsOf(call("1", "2")).against(threeArgs)).isEmpty();
                softly.assertThat(problemsOf(call("x=1")).against(threeArgs)).isEmpty();
                softly.assertThat(problemsOf(call("x=1", "y=2")).against(threeArgs)).isEmpty();
                softly.assertThat(problemsOf(call("x=1", "y=2", "z=3")).against(threeArgs)).isEmpty();
                softly.assertThat(problemsOf(call("a=1", "b=2")).against(threeArgs)).isEmpty();
                softly.assertThat(problemsOf(call("b=1", "a=2")).against(threeArgs)).isEmpty();
                softly.assertThat(problemsOf(call("a=1", "b=2", "x=3")).against(threeArgs)).isEmpty();
                softly.assertThat(problemsOf(call("a=1", "x=2", "c=3")).against(threeArgs)).isEmpty();
                softly.assertThat(problemsOf(call("${s}")).against(threeArgs)).isEmpty();
                softly.assertThat(problemsOf(call("&{d}")).against(threeArgs)).isEmpty();
                softly.assertThat(problemsOf(call("&{d}", "&{d}")).against(threeArgs)).isEmpty();
            });
        }

        @Test
        public void overriddenNamedArgumentsAreReported() {
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(problemsOf(call("a=1", "a=2")).against(twoArgs)).extracting(ALL).containsOnly(
                        problem(OVERRIDDEN_ARG_PROBLEM, 39, 42,
                                "Argument 'a' is passed multiple times using named syntax. This value will never be used"));
                softly.assertThat(problemsOf(call("a=1", "b=2", "b=3", "a=4")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(OVERRIDDEN_ARG_PROBLEM, 39, 42,
                                "Argument 'a' is passed multiple times using named syntax. This value will never be used"),
                        problem(OVERRIDDEN_ARG_PROBLEM, 46, 49,
                                "Argument 'b' is passed multiple times using named syntax. This value will never be used"));
            });
        }

        @Test
        public void invalidOrderIsReported_whenPositionalArgumentsAreUsedAfterNamedOne() {
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(problemsOf(call("a=1", "2")).against(twoArgs)).extracting(ALL).containsOnly(
                        problem(ORDER_PROBLEM, 46, 47, "Positional argument cannot be used after named arguments"));
                softly.assertThat(problemsOf(call("&{d}", "2")).against(twoArgs)).extracting(ALL).containsOnly(
                        problem(ORDER_PROBLEM, 47, 48, "Positional argument cannot be used after named arguments"));

                softly.assertThat(problemsOf(call("x=1", "b=2", "3")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(ORDER_PROBLEM, 53, 54, "Positional argument cannot be used after named arguments"));
                softly.assertThat(problemsOf(call("a=1", "x=2", "${s}")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(ORDER_PROBLEM, 53, 57, "Positional argument cannot be used after named arguments"));
                softly.assertThat(problemsOf(call("1", "&{d}", "3")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(ORDER_PROBLEM, 52, 53, "Positional argument cannot be used after named arguments"));
                softly.assertThat(problemsOf(call("1", "&{d}", "3", "4")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(ORDER_PROBLEM, 52, 53, "Positional argument cannot be used after named arguments"),
                        problem(ORDER_PROBLEM, 57, 58, "Positional argument cannot be used after named arguments"));
            });
        }

        @Test
        public void invalidNoOfNonKeywordArgumentsReported() {
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(problemsOf(call("1", "2")).against(twoArgs)).extracting(ALL).containsExactly(problem(
                        NUMBER_KW_PROBLEM, 28, 35,
                        "Invalid number of non-keyword arguments. Keyword 'keyword' expects from 0 to 1 non-keyword argument but 2 are provided"));
                softly.assertThat(problemsOf(call("@{l}", "2", "3", "4")).against(twoArgs)).extracting(ALL).containsExactly(problem(
                        NUMBER_KW_PROBLEM, 28, 35,
                                "Invalid number of non-keyword arguments. Keyword 'keyword' expects from 0 to 1 non-keyword argument but at least 3 are provided"));

                softly.assertThat(problemsOf(call("1", "2", "3")).against(threeArgs)).extracting(ALL).containsExactly(problem(
                        NUMBER_KW_PROBLEM, 28, 35,
                        "Invalid number of non-keyword arguments. Keyword 'keyword' expects from 0 to 2 non-keyword arguments but 3 are provided"));
                softly.assertThat(problemsOf(call("1", "2", "3", "4")).against(threeArgs)).extracting(ALL).containsExactly(problem(
                        NUMBER_KW_PROBLEM, 28, 35,
                        "Invalid number of non-keyword arguments. Keyword 'keyword' expects from 0 to 2 non-keyword arguments but 4 are provided"));
                softly.assertThat(problemsOf(call("1", "2", "3", "4", "5")).against(threeArgs)).extracting(ALL).containsExactly(problem(
                        NUMBER_KW_PROBLEM, 28, 35,
                        "Invalid number of non-keyword arguments. Keyword 'keyword' expects from 0 to 2 non-keyword arguments but 5 are provided"));
                softly.assertThat(problemsOf(call("${s}", "${s}2", "${s}3")).against(threeArgs)).extracting(ALL).containsExactly(problem(
                        NUMBER_KW_PROBLEM, 28, 35,
                        "Invalid number of non-keyword arguments. Keyword 'keyword' expects from 0 to 2 non-keyword arguments but 3 are provided"));
                softly.assertThat(problemsOf(call("1", "@{l}", "3", "4", "5")).against(threeArgs)).extracting(ALL).containsExactly(problem(
                        NUMBER_KW_PROBLEM, 28, 35,
                        "Invalid number of non-keyword arguments. Keyword 'keyword' expects from 0 to 2 non-keyword arguments but at least 4 are provided"));
                softly.assertThat(problemsOf(call("1", "2", "3", "4", "5", "&{d}")).against(threeArgs)).extracting(ALL).containsExactly(problem(
                        NUMBER_KW_PROBLEM, 28, 35,
                        "Invalid number of non-keyword arguments. Keyword 'keyword' expects from 0 to 2 non-keyword arguments but 5 are provided"));
            });
        }

        @Test
        public void multipleMatchesAreReported_whenArgIsPassedPositionallyAndByName() {
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(problemsOf(call("1", "a=2")).against(twoArgs)).extracting(ALL).containsOnly(
                        problem(MULTIPLE_MATCH_PROBLEM, 44, 47, "Argument 'a' has value already passed (1)"));

                softly.assertThat(problemsOf(call("1", "a=2")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(MULTIPLE_MATCH_PROBLEM, 44, 47, "Argument 'a' has value already passed (1)"));
                softly.assertThat(problemsOf(call("1", "2", "a=3", "b=4")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(MULTIPLE_MATCH_PROBLEM, 49, 52, "Argument 'a' has value already passed (1)"),
                        problem(MULTIPLE_MATCH_PROBLEM, 56, 59, "Argument 'b' has value already passed (2)"));
            });
        }

        @Test
        public void listsAreReported_whenUsedAsArgumentInOrderToProvideArguments() {
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(problemsOf(call("@{l}")).against(twoArgs)).extracting(ALL).containsOnly(
                        problem(COLLECTION_WARNING, 39, 43, "List argument '@{l}' has to contain from 0 to 1 item"));
                softly.assertThat(problemsOf(call("@{l}", "@{l}")).against(twoArgs)).extracting(ALL).containsOnly(
                        problem(COLLECTION_WARNING, 39, 43, "List argument '@{l}' has to be empty"),
                        problem(COLLECTION_WARNING, 47, 51, "List argument '@{l}' has to contain from 0 to 1 item"));
                softly.assertThat(problemsOf(call("@{l}", "2")).against(twoArgs)).extracting(ALL).containsOnly(
                        problem(COLLECTION_WARNING, 39, 43, "List argument '@{l}' has to be empty"));
                
                softly.assertThat(problemsOf(call("@{l}")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(COLLECTION_WARNING, 39, 43, "List argument '@{l}' has to contain from 0 to 2 items"));
                softly.assertThat(problemsOf(call("@{l}", "@{l}")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(COLLECTION_WARNING, 39, 43, "List argument '@{l}' has to be empty"),
                        problem(COLLECTION_WARNING, 47, 51, "List argument '@{l}' has to contain from 0 to 2 items"));
                softly.assertThat(problemsOf(call("1", "@{l}")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(COLLECTION_WARNING, 44, 48, "List argument '@{l}' has to contain from 0 to 1 item"));
                softly.assertThat(problemsOf(call("1", "@{l}", "3")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(COLLECTION_WARNING, 44, 48, "List argument '@{l}' has to be empty"));
            });
        }

        @Test
        public void dictionariesAreReported_whenUsedAsArgumentInOrderToProvideArguments() {
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(problemsOf(call("1", "&{d}")).against(twoArgs)).extracting(ALL).containsOnly(
                        problem(COLLECTION_WARNING, 44, 48, "Dictionary argument '&{d}' cannot have key: (a)"));

                softly.assertThat(problemsOf(call("1", "2", "&{d}")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(COLLECTION_WARNING, 49, 53, "Dictionary argument '&{d}' cannot have keys: (a, b)"));
            });
        }
    }

    public static class TwoArgsVarargsAndKwargsTest {

        private final ArgumentsDescriptor twoArgs = createDescriptor("*vararg", "**kwarg");

        @Test
        public void noProblemsReported_whenArgumentsAreUsedProperly() {
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(problemsOf(call("1")).against(twoArgs)).isEmpty();
                softly.assertThat(problemsOf(call("1", "2")).against(twoArgs)).isEmpty();
                softly.assertThat(problemsOf(call("1", "2", "3")).against(twoArgs)).isEmpty();
                softly.assertThat(problemsOf(call("1", "2", "3", "4")).against(twoArgs)).isEmpty();
                softly.assertThat(problemsOf(call("1", "2", "3", "4", "5")).against(twoArgs)).isEmpty();
                softly.assertThat(problemsOf(call("x=1")).against(twoArgs)).isEmpty();
                softly.assertThat(problemsOf(call("x=1", "y=2")).against(twoArgs)).isEmpty();
                softly.assertThat(problemsOf(call("x=1", "y=2", "z=3")).against(twoArgs)).isEmpty();
                softly.assertThat(problemsOf(call("a=1")).against(twoArgs)).isEmpty();
                softly.assertThat(problemsOf(call("a=1", "b=2")).against(twoArgs)).isEmpty();
                softly.assertThat(problemsOf(call("b=1", "a=2")).against(twoArgs)).isEmpty();
                softly.assertThat(problemsOf(call("a=1", "a=2")).against(twoArgs)).isEmpty();
                softly.assertThat(problemsOf(call("a=1", "x=2")).against(twoArgs)).isEmpty();
                softly.assertThat(problemsOf(call("a=1", "b=2", "x=3")).against(twoArgs)).isEmpty();
                softly.assertThat(problemsOf(call("a=1", "x=2", "c=3")).against(twoArgs)).isEmpty();
                softly.assertThat(problemsOf(call("a=1", "b=2", "b=3", "a=4")).against(twoArgs)).isEmpty();
                softly.assertThat(problemsOf(call("1", "a=2")).against(twoArgs)).isEmpty();
                softly.assertThat(problemsOf(call("1", "2", "a=3", "b=4")).against(twoArgs)).isEmpty();
                softly.assertThat(problemsOf(call("${s}")).against(twoArgs)).isEmpty();
                softly.assertThat(problemsOf(call("${s}1", "${s}2", "${s}3")).against(twoArgs)).isEmpty();
                softly.assertThat(problemsOf(call("@{l}")).against(twoArgs)).isEmpty();
                softly.assertThat(problemsOf(call("@{l}", "@{l}")).against(twoArgs)).isEmpty();
                softly.assertThat(problemsOf(call("@{l}", "2")).against(twoArgs)).isEmpty();
                softly.assertThat(problemsOf(call("@{l}", "2", "3")).against(twoArgs)).isEmpty();
                softly.assertThat(problemsOf(call("1", "@{l}")).against(twoArgs)).isEmpty();
                softly.assertThat(problemsOf(call("1", "@{l}", "3")).against(twoArgs)).isEmpty();
                softly.assertThat(problemsOf(call("1", "@{l}", "3", "4")).against(twoArgs)).isEmpty();
                softly.assertThat(problemsOf(call("@{l}", "${l}", "${l}")).against(twoArgs)).isEmpty();
                softly.assertThat(problemsOf(call("&{d}")).against(twoArgs)).isEmpty();
                softly.assertThat(problemsOf(call("&{d}", "&{d}")).against(twoArgs)).isEmpty();
                softly.assertThat(problemsOf(call("&{d}", "a=2")).against(twoArgs)).isEmpty();
                softly.assertThat(problemsOf(call("&{d}", "x=2")).against(twoArgs)).isEmpty();
                softly.assertThat(problemsOf(call("1", "&{d}")).against(twoArgs)).isEmpty();
                softly.assertThat(problemsOf(call("1", "2", "3", "4", "&{d}")).against(twoArgs)).isEmpty();
                softly.assertThat(problemsOf(call("@{l}[0]")).against(twoArgs)).isEmpty();
                softly.assertThat(problemsOf(call("&{d}[a]")).against(twoArgs)).isEmpty();
            });
        }

        @Test
        public void invalidOrderIsReported_whenPositionalArgumentsAreUsedAfterNamedOne() {
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(problemsOf(call("a=1", "2")).against(twoArgs)).extracting(ALL).containsOnly(
                        problem(ORDER_PROBLEM, 46, 47, "Positional argument cannot be used after named arguments"));
                softly.assertThat(problemsOf(call("a=1", "x=2", "3")).against(twoArgs)).extracting(ALL).containsOnly(
                        problem(ORDER_PROBLEM, 53, 54, "Positional argument cannot be used after named arguments"));
                softly.assertThat(problemsOf(call("a=1", "x=2", "${s}")).against(twoArgs)).extracting(ALL).containsOnly(
                        problem(ORDER_PROBLEM, 53, 57, "Positional argument cannot be used after named arguments"));
                softly.assertThat(problemsOf(call("&{d}", "2")).against(twoArgs)).extracting(ALL).containsOnly(
                        problem(ORDER_PROBLEM, 47, 48, "Positional argument cannot be used after named arguments"));
                softly.assertThat(problemsOf(call("1", "&{d}", "3")).against(twoArgs)).extracting(ALL).containsOnly(
                        problem(ORDER_PROBLEM, 52, 53, "Positional argument cannot be used after named arguments"));
                softly.assertThat(problemsOf(call("1", "&{d}", "3", "4")).against(twoArgs)).extracting(ALL).containsOnly(
                        problem(ORDER_PROBLEM, 52, 53, "Positional argument cannot be used after named arguments"),
                        problem(ORDER_PROBLEM, 57, 58, "Positional argument cannot be used after named arguments"));
            });
        }
    }

    public static class ThreeArgsRequiredOptionalAndVarargsTest {

        private final ArgumentsDescriptor threeArgs = createDescriptor("a", "b=2", "*vararg");

        private final ArgumentsDescriptor fiveArgs = createDescriptor("a", "b", "c=3", "d=4", "*vararg");

        @Test
        public void noProblemsReported_whenArgumentsAreUsedProperly() {
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(problemsOf(call("1")).against(threeArgs)).isEmpty();
                softly.assertThat(problemsOf(call("1", "2")).against(threeArgs)).isEmpty();
                softly.assertThat(problemsOf(call("1", "2", "3")).against(threeArgs)).isEmpty();
                softly.assertThat(problemsOf(call("a=1")).against(threeArgs)).isEmpty();
                softly.assertThat(problemsOf(call("a=1", "b=2")).against(threeArgs)).isEmpty();
                softly.assertThat(problemsOf(call("x=1")).against(threeArgs)).isEmpty();
                softly.assertThat(problemsOf(call("1", "x=2", "vararg=3", "z=4")).against(threeArgs)).isEmpty();
                softly.assertThat(problemsOf(call("${s}")).against(threeArgs)).isEmpty();
                softly.assertThat(problemsOf(call("1")).against(threeArgs)).isEmpty();
                softly.assertThat(problemsOf(call("@{l}", "2")).against(threeArgs)).isEmpty();
                softly.assertThat(problemsOf(call("@{l}", "2", "3", "4")).against(threeArgs)).isEmpty();
                softly.assertThat(problemsOf(call("@{l}[0]")).against(threeArgs)).isEmpty();
                softly.assertThat(problemsOf(call("&{d}[a]")).against(threeArgs)).isEmpty();

                softly.assertThat(problemsOf(call("1", "2")).against(fiveArgs)).isEmpty();
                softly.assertThat(problemsOf(call("1", "2", "3")).against(fiveArgs)).isEmpty();
                softly.assertThat(problemsOf(call("1", "2", "3", "4")).against(fiveArgs)).isEmpty();
                softly.assertThat(problemsOf(call("1", "2", "3", "4", "5")).against(fiveArgs)).isEmpty();
                softly.assertThat(problemsOf(call("x=1", "y=2")).against(fiveArgs)).isEmpty();
                softly.assertThat(problemsOf(call("x=1", "y=2", "z=3")).against(fiveArgs)).isEmpty();
                softly.assertThat(problemsOf(call("a=1", "b=2")).against(fiveArgs)).isEmpty();
                softly.assertThat(problemsOf(call("b=1", "a=2")).against(fiveArgs)).isEmpty();
                softly.assertThat(problemsOf(call("a=1", "b=2", "c=3")).against(fiveArgs)).isEmpty();
                softly.assertThat(problemsOf(call("${s}1", "${s}2", "${s}3")).against(fiveArgs)).isEmpty();
                softly.assertThat(problemsOf(call("1", "@{l}", "3")).against(fiveArgs)).isEmpty();
                softly.assertThat(problemsOf(call("1", "@{l}", "3", "4", "5")).against(fiveArgs)).isEmpty();
            });
        }

        @Test
        public void overriddenNamedArgumentsAreReported() {
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(problemsOf(call("a=1", "a=2")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(OVERRIDDEN_ARG_PROBLEM, 39, 42,
                                "Argument 'a' is passed multiple times using named syntax. This value will never be used"));
                softly.assertThat(problemsOf(call("a=1", "b=2", "b=3", "a=4")).against(fiveArgs)).extracting(ALL).containsOnly(
                        problem(OVERRIDDEN_ARG_PROBLEM, 39, 42,
                                "Argument 'a' is passed multiple times using named syntax. This value will never be used"),
                        problem(OVERRIDDEN_ARG_PROBLEM, 46, 49,
                                "Argument 'b' is passed multiple times using named syntax. This value will never be used"));
            });
        }

        @Test
        public void invalidOrderIsReported_whenPositionalArgumentsAreUsedAfterNamedOne() {
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(problemsOf(call("a=1", "2")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(ORDER_PROBLEM, 46, 47, "Positional argument cannot be used after named arguments"));
                softly.assertThat(problemsOf(call("a=1", "x=2")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(ORDER_PROBLEM, 46, 49, "Positional argument cannot be used after named arguments. "
                                + "Although this argument looks like named one, it isn't because there is no "
                                + "'x' argument in the keyword definition"));
                softly.assertThat(problemsOf(call("&{d}", "2")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(ORDER_PROBLEM, 47, 48, "Positional argument cannot be used after named arguments"));
                softly.assertThat(problemsOf(call("&{d}", "x=2")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(ORDER_PROBLEM, 47, 50, "Positional argument cannot be used after named arguments. "
                                + "Although this argument looks like named one, it isn't because there is no "
                                + "'x' argument in the keyword definition"));

                softly.assertThat(problemsOf(call("x=1", "a=2", "3")).against(fiveArgs)).extracting(ALL).containsOnly(
                        problem(ORDER_PROBLEM, 53, 54, "Positional argument cannot be used after named arguments"));
                softly.assertThat(problemsOf(call("a=1", "b=2", "x=3")).against(fiveArgs)).extracting(ALL).containsOnly(
                        problem(ORDER_PROBLEM, 53, 56, "Positional argument cannot be used after named arguments. "
                                + "Although this argument looks like named one, it isn't because there is no "
                                + "'x' argument in the keyword definition"));
                softly.assertThat(problemsOf(call("a=1", "x=2", "c=3")).against(fiveArgs)).extracting(ALL).containsOnly(
                        problem(ORDER_PROBLEM, 46, 49, "Positional argument cannot be used after named arguments. "
                                + "Although this argument looks like named one, it isn't because there is no "
                                + "'x' argument in the keyword definition"));
                softly.assertThat(problemsOf(call("a=1", "x=2", "${s}")).against(fiveArgs)).extracting(ALL).containsOnly(
                        problem(ORDER_PROBLEM, 46, 49, "Positional argument cannot be used after named arguments. "
                                + "Although this argument looks like named one, it isn't because there is no "
                                + "'x' argument in the keyword definition"),
                        problem(ORDER_PROBLEM, 53, 57, "Positional argument cannot be used after named arguments"));
                softly.assertThat(problemsOf(call("1", "&{d}", "3")).against(fiveArgs)).extracting(ALL).containsOnly(
                        problem(ORDER_PROBLEM, 52, 53, "Positional argument cannot be used after named arguments"));
                softly.assertThat(problemsOf(call("1", "&{d}", "3", "4")).against(fiveArgs)).extracting(ALL).containsOnly(
                        problem(ORDER_PROBLEM, 52, 53, "Positional argument cannot be used after named arguments"),
                        problem(ORDER_PROBLEM, 57, 58, "Positional argument cannot be used after named arguments"));
            });
        }

        @Test
        public void invalidNoOfArgumentsReported_whenNumberOfCallSiteArgumentsExceedsMaxNumberOfArgs() {
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(problemsOf(call()).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(NUMBER_PROBLEM, 28, 35,
                        "Invalid number of arguments. Keyword 'keyword' expects at least 1 argument but 0 are provided"));

                softly.assertThat(problemsOf(call()).against(fiveArgs)).extracting(ALL).containsOnly(
                        problem(NUMBER_PROBLEM, 28, 35,
                        "Invalid number of arguments. Keyword 'keyword' expects at least 2 arguments but 0 are provided"));
                softly.assertThat(problemsOf(call("1")).against(fiveArgs)).extracting(ALL).containsOnly(
                        problem(NUMBER_PROBLEM, 28, 35,
                        "Invalid number of arguments. Keyword 'keyword' expects at least 2 arguments but 1 is provided"));
                softly.assertThat(problemsOf(call("a=1")).against(fiveArgs)).extracting(ALL).containsOnly(
                        problem(NUMBER_PROBLEM, 28, 35,
                        "Invalid number of arguments. Keyword 'keyword' expects at least 2 arguments but 1 is provided"));
                softly.assertThat(problemsOf(call("${s}")).against(fiveArgs)).extracting(ALL).containsOnly(
                        problem(NUMBER_PROBLEM, 28, 35,
                        "Invalid number of arguments. Keyword 'keyword' expects at least 2 arguments but 1 is provided"));
            });
        }

        @Test
        public void multipleMatchesAreReported_whenArgIsPassedPositionallyAndByName() {
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(problemsOf(call("1", "a=2")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(MULTIPLE_MATCH_PROBLEM, 44, 47, "Argument 'a' has value already passed (1)"));

                softly.assertThat(problemsOf(call("1", "a=2")).against(fiveArgs)).extracting(ALL).containsOnly(
                        problem(MULTIPLE_MATCH_PROBLEM, 44, 47, "Argument 'a' has value already passed (1)"));
                softly.assertThat(problemsOf(call("1", "2", "a=3", "b=4")).against(fiveArgs)).extracting(ALL).containsOnly(
                                problem(MULTIPLE_MATCH_PROBLEM, 49, 52, "Argument 'a' has value already passed (1)"),
                        problem(MULTIPLE_MATCH_PROBLEM, 56, 59, "Argument 'b' has value already passed (2)"));
            });
        }

        @Test
        public void missingArgumentsAreReported_whenItIsNotProvidedAtCallSite() {
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(problemsOf(call("b=1")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(MISSING_PROBLEM, 28, 35, "Keyword 'keyword' requires (a) argument to be specified"));

                softly.assertThat(problemsOf(call("c=1", "d=2")).against(fiveArgs)).extracting(ALL).containsOnly(
                        problem(MISSING_PROBLEM, 28, 35, "Keyword 'keyword' requires (a, b) arguments to be specified"));
                softly.assertThat(problemsOf(call("1", "d=2")).against(fiveArgs)).extracting(ALL).containsOnly(
                        problem(MISSING_PROBLEM, 28, 35, "Keyword 'keyword' requires (b) argument to be specified"));
            });
        }

        @Test
        public void listsAreReported_whenUsedAsArgumentInOrderToProvideMultipleArguments() {
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(problemsOf(call("@{l}")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(COLLECTION_WARNING, 39, 43, "List argument '@{l}' has to contain at least 1 item"));
                softly.assertThat(problemsOf(call("@{l}", "@{l}")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(COLLECTION_WARNING, 39, 43, "List argument '@{l}' has to be empty"),
                        problem(COLLECTION_WARNING, 47, 51, "List argument '@{l}' has to contain at least 1 item"));

                softly.assertThat(problemsOf(call("@{l}")).against(fiveArgs)).extracting(ALL).containsOnly(
                        problem(COLLECTION_WARNING, 39, 43, "List argument '@{l}' has to contain at least 2 items"));
                softly.assertThat(problemsOf(call("@{l}", "@{l}")).against(fiveArgs)).extracting(ALL).containsOnly(
                        problem(COLLECTION_WARNING, 39, 43, "List argument '@{l}' has to be empty"),
                        problem(COLLECTION_WARNING, 47, 51, "List argument '@{l}' has to contain at least 2 items"));
                softly.assertThat(problemsOf(call("1", "@{l}")).against(fiveArgs)).extracting(ALL).containsOnly(
                        problem(COLLECTION_WARNING, 44, 48, "List argument '@{l}' has to contain at least 1 item"));
            });
        }

        @Test
        public void dictionariesAreReported_whenUsedAsArgumentInOrderToProvideMultipleArguments() {
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(problemsOf(call("&{d}")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(COLLECTION_WARNING, 39, 43,
                                "Dictionary argument '&{d}' has to contain from 1 to 2 mappings. Required key: (a), possible key: (b)"));
                softly.assertThat(problemsOf(call("&{d}", "&{d}")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(COLLECTION_WARNING, 39, 43, "Dictionary argument '&{d}' has to be empty"),
                        problem(COLLECTION_WARNING, 47, 51,
                                "Dictionary argument '&{d}' has to contain from 1 to 2 mappings. Required key: (a), possible key: (b)"));
                softly.assertThat(problemsOf(call("1", "&{d}")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(COLLECTION_WARNING, 44, 48,
                                "Dictionary argument '&{d}' has to contain from 0 to 1 mapping. Possible key: (b), forbidden key: (a)"));
                softly.assertThat(problemsOf(call("&{d}", "a=2")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(COLLECTION_WARNING, 39, 43,
                                "Dictionary argument '&{d}' has to contain from 0 to 1 mapping. Possible key: (b)"));

                softly.assertThat(problemsOf(call("&{d}")).against(fiveArgs)).extracting(ALL).containsOnly(
                        problem(COLLECTION_WARNING, 39, 43,
                                "Dictionary argument '&{d}' has to contain from 2 to 4 mappings. Required keys: (a, b), possible keys: (c, d)"));
                softly.assertThat(problemsOf(call("&{d}", "&{d}")).against(fiveArgs)).extracting(ALL).containsOnly(
                        problem(COLLECTION_WARNING, 39, 43, "Dictionary argument '&{d}' has to be empty"),
                        problem(COLLECTION_WARNING, 47, 51,
                                "Dictionary argument '&{d}' has to contain from 2 to 4 mappings. Required keys: (a, b), possible keys: (c, d)"));
                softly.assertThat(problemsOf(call("1", "&{d}")).against(fiveArgs)).extracting(ALL).containsOnly(
                        problem(COLLECTION_WARNING, 44, 48,
                                "Dictionary argument '&{d}' has to contain from 1 to 3 mappings. Required key: (b), possible keys: (c, d), forbidden key: (a)"));
                softly.assertThat(problemsOf(call("1", "2", "3", "4", "5", "&{d}")).against(fiveArgs)).extracting(ALL).containsOnly(
                        problem(COLLECTION_WARNING, 64, 68, "Dictionary argument '&{d}' has to be empty"));
            });
        }
    }

    public static class ThreeArgsRequiredOptionalAndKwargsTest {

        private final ArgumentsDescriptor threeArgs = createDescriptor("a", "b=2", "**kwarg");

        private final ArgumentsDescriptor fiveArgs = createDescriptor("a", "b", "c=3", "d=4", "**kwarg");

        @Test
        public void noProblemsReported_whenArgumentsAreUsedProperly() {
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(problemsOf(call("1")).against(threeArgs)).isEmpty();
                softly.assertThat(problemsOf(call("1", "2")).against(threeArgs)).isEmpty();
                softly.assertThat(problemsOf(call("a=1")).against(threeArgs)).isEmpty();
                softly.assertThat(problemsOf(call("a=1", "x=2")).against(threeArgs)).isEmpty();
                softly.assertThat(problemsOf(call("1", "x=2", "kwarg=3", "z=4")).against(threeArgs)).isEmpty();
                softly.assertThat(problemsOf(call("${s}")).against(threeArgs)).isEmpty();
                softly.assertThat(problemsOf(call("&{d}", "a=2")).against(threeArgs)).isEmpty();
                softly.assertThat(problemsOf(call("@{l}[0]")).against(threeArgs)).isEmpty();
                softly.assertThat(problemsOf(call("&{d}[a]")).against(threeArgs)).isEmpty();

                softly.assertThat(problemsOf(call("1", "2")).against(fiveArgs)).isEmpty();
                softly.assertThat(problemsOf(call("1", "2", "3")).against(fiveArgs)).isEmpty();
                softly.assertThat(problemsOf(call("1", "2", "3", "4")).against(fiveArgs)).isEmpty();
                softly.assertThat(problemsOf(call("a=1", "b=2")).against(fiveArgs)).isEmpty();
                softly.assertThat(problemsOf(call("b=1", "a=2")).against(fiveArgs)).isEmpty();
                softly.assertThat(problemsOf(call("a=1", "b=2", "x=3")).against(fiveArgs)).isEmpty();
                softly.assertThat(problemsOf(call("${s}1", "${s}2", "${s}3")).against(fiveArgs)).isEmpty();
            });
        }

        @Test
        public void overriddenNamedArgumentsAreReported() {
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(problemsOf(call("a=1", "a=2")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(OVERRIDDEN_ARG_PROBLEM, 39, 42,
                                "Argument 'a' is passed multiple times using named syntax. This value will never be used"));
                softly.assertThat(problemsOf(call("a=1", "b=2", "b=3", "a=4")).against(fiveArgs)).extracting(ALL).containsOnly(
                        problem(OVERRIDDEN_ARG_PROBLEM, 39, 42,
                                "Argument 'a' is passed multiple times using named syntax. This value will never be used"),
                        problem(OVERRIDDEN_ARG_PROBLEM, 46, 49,
                                "Argument 'b' is passed multiple times using named syntax. This value will never be used"));
            });
        }

        @Test
        public void invalidOrderIsReported_whenPositionalArgumentsAreUsedAfterNamedOne() {
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(problemsOf(call("a=1", "2")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(ORDER_PROBLEM, 46, 47, "Positional argument cannot be used after named arguments"));
                softly.assertThat(problemsOf(call("&{d}", "2")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(ORDER_PROBLEM, 47, 48, "Positional argument cannot be used after named arguments"));

                softly.assertThat(problemsOf(call("x=1", "a=2", "3")).against(fiveArgs)).extracting(ALL).containsOnly(
                        problem(ORDER_PROBLEM, 53, 54, "Positional argument cannot be used after named arguments"));
                softly.assertThat(problemsOf(call("a=1", "x=2", "${s}")).against(fiveArgs)).extracting(ALL).containsOnly(
                        problem(ORDER_PROBLEM, 53, 57, "Positional argument cannot be used after named arguments"));
                softly.assertThat(problemsOf(call("1", "&{d}", "3")).against(fiveArgs)).extracting(ALL).containsOnly(
                        problem(ORDER_PROBLEM, 52, 53, "Positional argument cannot be used after named arguments"));
                softly.assertThat(problemsOf(call("1", "&{d}", "3", "4")).against(fiveArgs)).extracting(ALL).containsOnly(
                        problem(ORDER_PROBLEM, 52, 53, "Positional argument cannot be used after named arguments"),
                        problem(ORDER_PROBLEM, 57, 58, "Positional argument cannot be used after named arguments"));
            });
        }

        @Test
        public void invalidNoOfArgumentsReported_whenNumberOfCallSiteArgumentsExceedsMaxNumberOfArgs() {
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(problemsOf(call()).against(threeArgs)).extracting(ALL).containsOnly(problem(
                        NUMBER_KW_PROBLEM, 28, 35,
                        "Invalid number of non-keyword arguments. Keyword 'keyword' expects from 1 to 2 non-keyword arguments but 0 are provided"));
                softly.assertThat(problemsOf(call("1", "2", "3")).against(threeArgs)).extracting(ALL).containsOnly(problem(
                        NUMBER_KW_PROBLEM, 28, 35,
                        "Invalid number of non-keyword arguments. Keyword 'keyword' expects from 1 to 2 non-keyword arguments but 3 are provided"));
                softly.assertThat(problemsOf(call("x=1")).against(threeArgs)).extracting(ALL).containsOnly(problem(
                        NUMBER_KW_PROBLEM, 28, 35,
                        "Invalid number of non-keyword arguments. Keyword 'keyword' expects from 1 to 2 non-keyword arguments but 0 are provided"));
                softly.assertThat(problemsOf(call("x=1", "y=2")).against(threeArgs)).extracting(ALL).containsOnly(problem(
                        NUMBER_KW_PROBLEM, 28, 35,
                        "Invalid number of non-keyword arguments. Keyword 'keyword' expects from 1 to 2 non-keyword arguments but 0 are provided"));
                softly.assertThat(problemsOf(call("@{l}", "2", "3", "4")).against(threeArgs)).extracting(ALL).containsOnly(problem(
                        NUMBER_KW_PROBLEM, 28, 35,
                        "Invalid number of non-keyword arguments. Keyword 'keyword' expects from 1 to 2 non-keyword arguments but at least 3 are provided"));

                softly.assertThat(problemsOf(call()).against(fiveArgs)).extracting(ALL).containsOnly(problem(
                        NUMBER_KW_PROBLEM, 28, 35,
                        "Invalid number of non-keyword arguments. Keyword 'keyword' expects from 2 to 4 non-keyword arguments but 0 are provided"));
                softly.assertThat(problemsOf(call("1")).against(fiveArgs)).extracting(ALL).containsOnly(problem(
                        NUMBER_KW_PROBLEM, 28, 35,
                        "Invalid number of non-keyword arguments. Keyword 'keyword' expects from 2 to 4 non-keyword arguments but 1 is provided"));
                softly.assertThat(problemsOf(call("1", "2", "3", "4", "5")).against(fiveArgs)).extracting(ALL).containsOnly(problem(
                        NUMBER_KW_PROBLEM, 28, 35,
                        "Invalid number of non-keyword arguments. Keyword 'keyword' expects from 2 to 4 non-keyword arguments but 5 are provided"));
                softly.assertThat(problemsOf(call("x=1")).against(fiveArgs)).extracting(ALL).containsOnly(problem(
                        NUMBER_KW_PROBLEM, 28, 35,
                        "Invalid number of non-keyword arguments. Keyword 'keyword' expects from 2 to 4 non-keyword arguments but 0 are provided"));
                softly.assertThat(problemsOf(call("x=1", "y=2")).against(fiveArgs)).extracting(ALL).containsOnly(problem(
                        NUMBER_KW_PROBLEM, 28, 35,
                        "Invalid number of non-keyword arguments. Keyword 'keyword' expects from 2 to 4 non-keyword arguments but 0 are provided"));
                softly.assertThat(problemsOf(call("x=1", "y=2", "z=3")).against(fiveArgs)).extracting(ALL).containsOnly(problem(
                        NUMBER_KW_PROBLEM, 28, 35,
                        "Invalid number of non-keyword arguments. Keyword 'keyword' expects from 2 to 4 non-keyword arguments but 0 are provided"));
                softly.assertThat(problemsOf(call("${s}")).against(fiveArgs)).extracting(ALL).containsOnly(problem(
                        NUMBER_KW_PROBLEM, 28, 35,
                        "Invalid number of non-keyword arguments. Keyword 'keyword' expects from 2 to 4 non-keyword arguments but 1 is provided"));
                softly.assertThat(problemsOf(call("1", "@{l}", "3", "4", "5", "6")).against(fiveArgs)).extracting(ALL).containsOnly(problem(
                        NUMBER_KW_PROBLEM, 28, 35,
                        "Invalid number of non-keyword arguments. Keyword 'keyword' expects from 2 to 4 non-keyword arguments but at least 5 are provided"));
                softly.assertThat(problemsOf(call("1", "2", "3", "4", "5", "&{d}")).against(fiveArgs)).extracting(ALL).containsOnly(problem(
                        NUMBER_KW_PROBLEM, 28, 35,
                        "Invalid number of non-keyword arguments. Keyword 'keyword' expects from 2 to 4 non-keyword arguments but 5 are provided"));
            });
        }

        @Test
        public void multipleMatchesAreReported_whenArgIsPassedPositionallyAndByName() {
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(problemsOf(call("1", "a=2")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(MULTIPLE_MATCH_PROBLEM, 44, 47, "Argument 'a' has value already passed (1)"));

                softly.assertThat(problemsOf(call("1", "a=2")).against(fiveArgs)).extracting(ALL).containsOnly(
                        problem(MULTIPLE_MATCH_PROBLEM, 44, 47, "Argument 'a' has value already passed (1)"));
                softly.assertThat(problemsOf(call("1", "2", "a=3", "b=4")).against(fiveArgs)).extracting(ALL).containsOnly(
                        problem(MULTIPLE_MATCH_PROBLEM, 49, 52, "Argument 'a' has value already passed (1)"),
                        problem(MULTIPLE_MATCH_PROBLEM, 56, 59, "Argument 'b' has value already passed (2)"));
            });
        }

        @Test
        public void missingArgumentsAreReported_whenItIsNotProvidedAtCallSite() {
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(problemsOf(call("b=1")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(MISSING_PROBLEM, 28, 35, "Keyword 'keyword' requires (a) argument to be specified"));

                softly.assertThat(problemsOf(call("c=1", "d=2")).against(fiveArgs)).extracting(ALL).containsOnly(
                        problem(MISSING_PROBLEM, 28, 35, "Keyword 'keyword' requires (a, b) arguments to be specified"));
                softly.assertThat(problemsOf(call("1", "d=2")).against(fiveArgs)).extracting(ALL).containsOnly(
                        problem(MISSING_PROBLEM, 28, 35, "Keyword 'keyword' requires (b) argument to be specified"));
                softly.assertThat(problemsOf(call("a=1", "x=2", "c=3")).against(fiveArgs)).extracting(ALL).containsOnly(
                        problem(MISSING_PROBLEM, 28, 35, "Keyword 'keyword' requires (b) argument to be specified"));
            });
        }

        @Test
        public void listsAreReported_whenUsedAsArgumentInOrderToProvideMultipleArguments() {
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(problemsOf(call("@{l}")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(COLLECTION_WARNING, 39, 43, "List argument '@{l}' has to contain from 1 to 2 items"));
                softly.assertThat(problemsOf(call("@{l}", "@{l}")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(COLLECTION_WARNING, 39, 43, "List argument '@{l}' has to be empty"),
                        problem(COLLECTION_WARNING, 47, 51, "List argument '@{l}' has to contain from 1 to 2 items"));
                softly.assertThat(problemsOf(call("@{l}", "2")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(COLLECTION_WARNING, 39, 43, "List argument '@{l}' has to contain from 0 to 1 item"));
                softly.assertThat(problemsOf(call("@{l}", "2", "3")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(COLLECTION_WARNING, 39, 43, "List argument '@{l}' has to be empty"));

                softly.assertThat(problemsOf(call("@{l}")).against(fiveArgs)).extracting(ALL).containsOnly(
                        problem(COLLECTION_WARNING, 39, 43, "List argument '@{l}' has to contain from 2 to 4 items"));
                softly.assertThat(problemsOf(call("@{l}", "@{l}")).against(fiveArgs)).extracting(ALL).containsOnly(
                        problem(COLLECTION_WARNING, 39, 43, "List argument '@{l}' has to be empty"),
                        problem(COLLECTION_WARNING, 47, 51, "List argument '@{l}' has to contain from 2 to 4 items"));
                softly.assertThat(problemsOf(call("1", "@{l}")).against(fiveArgs)).extracting(ALL).containsOnly(
                        problem(COLLECTION_WARNING, 44, 48, "List argument '@{l}' has to contain from 1 to 3 items"));
                softly.assertThat(problemsOf(call("1", "@{l}", "3")).against(fiveArgs)).extracting(ALL).containsOnly(
                        problem(COLLECTION_WARNING, 44, 48, "List argument '@{l}' has to contain from 0 to 2 items"));
                softly.assertThat(problemsOf(call("1", "@{l}", "3", "4", "5")).against(fiveArgs)).extracting(ALL).containsOnly(
                        problem(COLLECTION_WARNING, 44, 48, "List argument '@{l}' has to be empty"));
            });
        }

        @Test
        public void dictionariesAreReported_whenUsedAsArgumentInOrderToProvideMultipleArguments() {
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(problemsOf(call("&{d}")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(COLLECTION_WARNING, 39, 43, "Dictionary argument '&{d}' has to contain at least 1 mapping. Required key: (a)"));
                softly.assertThat(problemsOf(call("&{d}", "&{d}")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(COLLECTION_WARNING, 39, 43, "Dictionary argument '&{d}' has to be empty"),
                        problem(COLLECTION_WARNING, 47, 51,
                                "Dictionary argument '&{d}' has to contain at least 1 mapping. Required key: (a)"));
                softly.assertThat(problemsOf(call("1", "&{d}")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(COLLECTION_WARNING, 44, 48, "Dictionary argument '&{d}' cannot have key: (a)"));
                softly.assertThat(problemsOf(call("&{d}", "b=2")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(COLLECTION_WARNING, 39, 43, "Dictionary argument '&{d}' has to contain at least 1 mapping. Required key: (a)"));
                
                softly.assertThat(problemsOf(call("&{d}")).against(fiveArgs)).extracting(ALL).containsOnly(
                        problem(COLLECTION_WARNING, 39, 43, "Dictionary argument '&{d}' has to contain at least 2 mappings. Required keys: (a, b)"));
                softly.assertThat(problemsOf(call("&{d}", "&{d}")).against(fiveArgs)).extracting(ALL).containsOnly(
                        problem(COLLECTION_WARNING, 39, 43, "Dictionary argument '&{d}' has to be empty"),
                        problem(COLLECTION_WARNING, 47, 51,
                                "Dictionary argument '&{d}' has to contain at least 2 mappings. Required keys: (a, b)"));
                softly.assertThat(problemsOf(call("1", "&{d}")).against(fiveArgs)).extracting(ALL).containsOnly(
                        problem(COLLECTION_WARNING, 44, 48, "Dictionary argument '&{d}' has to contain at least 1 mapping. Required key: (b), forbidden key: (a)"));
            });
        }
    }

    public static class ThreeArgsRequiredVarargsAndKwargsTest {

        private final ArgumentsDescriptor threeArgs = createDescriptor("a", "*vararg", "**kwarg");

        private final ArgumentsDescriptor fiveArgs = createDescriptor("a", "b", "c", "*vararg", "**kwarg");

        @Test
        public void noProblemsReported_whenArgumentsAreUsedProperly() {
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(problemsOf(call("1")).against(threeArgs)).isEmpty();
                softly.assertThat(problemsOf(call("1", "2")).against(threeArgs)).isEmpty();
                softly.assertThat(problemsOf(call("1", "2", "3")).against(threeArgs)).isEmpty();
                softly.assertThat(problemsOf(call("a=1")).against(threeArgs)).isEmpty();
                softly.assertThat(problemsOf(call("a=1", "x=2")).against(threeArgs)).isEmpty();
                softly.assertThat(problemsOf(call("x=1", "a=2")).against(threeArgs)).isEmpty();
                softly.assertThat(problemsOf(call("1", "x=2", "vararg=3", "kwarg=4")).against(threeArgs)).isEmpty();
                softly.assertThat(problemsOf(call("${s}")).against(threeArgs)).isEmpty();
                softly.assertThat(problemsOf(call("@{l}", "2")).against(threeArgs)).isEmpty();
                softly.assertThat(problemsOf(call("@{l}", "2", "3")).against(threeArgs)).isEmpty();
                softly.assertThat(problemsOf(call("@{l}", "2", "3", "4")).against(threeArgs)).isEmpty();
                softly.assertThat(problemsOf(call("&{d}", "a=2")).against(threeArgs)).isEmpty();
                softly.assertThat(problemsOf(call("@{l}[0]")).against(threeArgs)).isEmpty();
                softly.assertThat(problemsOf(call("&{d}[a]")).against(threeArgs)).isEmpty();

                softly.assertThat(problemsOf(call("1", "2", "3")).against(fiveArgs)).isEmpty();
                softly.assertThat(problemsOf(call("1", "2", "3", "4")).against(fiveArgs)).isEmpty();
                softly.assertThat(problemsOf(call("1", "2", "3", "4", "5")).against(fiveArgs)).isEmpty();
                softly.assertThat(problemsOf(call("a=1", "c=2", "b=3")).against(fiveArgs)).isEmpty();
                softly.assertThat(problemsOf(call("b=1", "c=2", "a=3")).against(fiveArgs)).isEmpty();
                softly.assertThat(problemsOf(call("a=1", "b=2", "c=3", "x=4")).against(fiveArgs)).isEmpty();
                softly.assertThat(problemsOf(call("a=1", "x=2", "c=3", "b=4")).against(fiveArgs)).isEmpty();
                softly.assertThat(problemsOf(call("${s}1", "${s}2", "${s}3")).against(fiveArgs)).isEmpty();
                softly.assertThat(problemsOf(call("1", "@{l}", "3", "4")).against(fiveArgs)).isEmpty();
                softly.assertThat(problemsOf(call("1", "@{l}", "3", "4", "5")).against(fiveArgs)).isEmpty();
                softly.assertThat(problemsOf(call("1", "@{l}", "3", "4", "5", "6")).against(fiveArgs)).isEmpty();
            });
        }

        @Test
        public void overriddenNamedArgumentsAreReported() {
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(problemsOf(call("a=1", "a=2")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(OVERRIDDEN_ARG_PROBLEM, 39, 42,
                                "Argument 'a' is passed multiple times using named syntax. This value will never be used"));
                softly.assertThat(problemsOf(call("a=1", "b=2", "c=3", "b=4", "a=5")).against(fiveArgs)).extracting(ALL).containsOnly(
                        problem(OVERRIDDEN_ARG_PROBLEM, 39, 42,
                                "Argument 'a' is passed multiple times using named syntax. This value will never be used"),
                        problem(OVERRIDDEN_ARG_PROBLEM, 46, 49,
                                "Argument 'b' is passed multiple times using named syntax. This value will never be used"));
            });
        }

        @Test
        public void invalidOrderIsReported_whenPositionalArgumentsAreUsedAfterNamedOne() {
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(problemsOf(call("a=1", "2")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(ORDER_PROBLEM, 46, 47, "Positional argument cannot be used after named arguments"));
                softly.assertThat(problemsOf(call("&{d}", "2")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(ORDER_PROBLEM, 47, 48, "Positional argument cannot be used after named arguments"));

                softly.assertThat(problemsOf(call("x=1", "a=2", "3")).against(fiveArgs)).extracting(ALL).containsOnly(
                        problem(ORDER_PROBLEM, 53, 54, "Positional argument cannot be used after named arguments"));
                softly.assertThat(problemsOf(call("a=1", "x=2", "${s}")).against(fiveArgs)).extracting(ALL).containsOnly(
                        problem(ORDER_PROBLEM, 53, 57, "Positional argument cannot be used after named arguments"));
                softly.assertThat(problemsOf(call("1", "&{d}", "3")).against(fiveArgs)).extracting(ALL).containsOnly(
                        problem(ORDER_PROBLEM, 52, 53, "Positional argument cannot be used after named arguments"));
                softly.assertThat(problemsOf(call("1", "&{d}", "3", "4")).against(fiveArgs)).extracting(ALL).containsOnly(
                        problem(ORDER_PROBLEM, 52, 53, "Positional argument cannot be used after named arguments"),
                        problem(ORDER_PROBLEM, 57, 58, "Positional argument cannot be used after named arguments"));
            });
        }

        @Test
        public void invalidNoOfArgumentsReported_whenNumberOfCallSiteArgumentsExceedsMaxNumberOfArgs() {
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(problemsOf(call()).against(threeArgs)).extracting(ALL).containsOnly(problem(
                        NUMBER_KW_PROBLEM, 28, 35,
                        "Invalid number of non-keyword arguments. Keyword 'keyword' expects at least 1 non-keyword argument but 0 are provided"));
                softly.assertThat(problemsOf(call("x=1")).against(threeArgs)).extracting(ALL).containsOnly(problem(
                        NUMBER_KW_PROBLEM, 28, 35,
                        "Invalid number of non-keyword arguments. Keyword 'keyword' expects at least 1 non-keyword argument but 0 are provided"));
                softly.assertThat(problemsOf(call("x=1", "y=2")).against(threeArgs)).extracting(ALL).containsOnly(problem(
                        NUMBER_KW_PROBLEM, 28, 35,
                        "Invalid number of non-keyword arguments. Keyword 'keyword' expects at least 1 non-keyword argument but 0 are provided"));

                softly.assertThat(problemsOf(call()).against(fiveArgs)).extracting(ALL).containsOnly(problem(
                        NUMBER_KW_PROBLEM, 28, 35,
                        "Invalid number of non-keyword arguments. Keyword 'keyword' expects at least 3 non-keyword arguments but 0 are provided"));
                softly.assertThat(problemsOf(call("1")).against(fiveArgs)).extracting(ALL).containsOnly(problem(
                        NUMBER_KW_PROBLEM, 28, 35,
                        "Invalid number of non-keyword arguments. Keyword 'keyword' expects at least 3 non-keyword arguments but 1 is provided"));
                softly.assertThat(problemsOf(call("1", "2")).against(fiveArgs)).extracting(ALL).containsOnly(problem(
                        NUMBER_KW_PROBLEM, 28, 35,
                        "Invalid number of non-keyword arguments. Keyword 'keyword' expects at least 3 non-keyword arguments but 2 are provided"));
                softly.assertThat(problemsOf(call("x=1")).against(fiveArgs)).extracting(ALL).containsOnly(problem(
                        NUMBER_KW_PROBLEM, 28, 35,
                        "Invalid number of non-keyword arguments. Keyword 'keyword' expects at least 3 non-keyword arguments but 0 are provided"));
                softly.assertThat(problemsOf(call("x=1", "y=2")).against(fiveArgs)).extracting(ALL).containsOnly(problem(
                        NUMBER_KW_PROBLEM, 28, 35,
                        "Invalid number of non-keyword arguments. Keyword 'keyword' expects at least 3 non-keyword arguments but 0 are provided"));
                softly.assertThat(problemsOf(call("x=1", "y=2", "z=3")).against(fiveArgs)).extracting(ALL).containsOnly(problem(
                        NUMBER_KW_PROBLEM, 28, 35,
                        "Invalid number of non-keyword arguments. Keyword 'keyword' expects at least 3 non-keyword arguments but 0 are provided"));
                softly.assertThat(problemsOf(call("a=1", "b=2")).against(fiveArgs)).extracting(ALL).containsOnly(problem(
                        NUMBER_KW_PROBLEM, 28, 35,
                        "Invalid number of non-keyword arguments. Keyword 'keyword' expects at least 3 non-keyword arguments but 2 are provided"));
                softly.assertThat(problemsOf(call("b=1", "a=2")).against(fiveArgs)).extracting(ALL).containsOnly(problem(
                        NUMBER_KW_PROBLEM, 28, 35,
                        "Invalid number of non-keyword arguments. Keyword 'keyword' expects at least 3 non-keyword arguments but 2 are provided"));
                softly.assertThat(problemsOf(call("c=1", "d=2")).against(fiveArgs)).extracting(ALL).containsOnly(problem(
                        NUMBER_KW_PROBLEM, 28, 35,
                        "Invalid number of non-keyword arguments. Keyword 'keyword' expects at least 3 non-keyword arguments but 1 is provided"));
                softly.assertThat(problemsOf(call("1", "d=2")).against(fiveArgs)).extracting(ALL).containsOnly(problem(
                        NUMBER_KW_PROBLEM, 28, 35,
                        "Invalid number of non-keyword arguments. Keyword 'keyword' expects at least 3 non-keyword arguments but 1 is provided"));
                softly.assertThat(problemsOf(call("${s}")).against(fiveArgs)).extracting(ALL).containsOnly(problem(
                        NUMBER_KW_PROBLEM, 28, 35,
                        "Invalid number of non-keyword arguments. Keyword 'keyword' expects at least 3 non-keyword arguments but 1 is provided"));
            });
        }

        @Test
        public void multipleMatchesAreReported_whenArgIsPassedPositionallyAndByName() {
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(problemsOf(call("1", "a=2")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(MULTIPLE_MATCH_PROBLEM, 44, 47, "Argument 'a' has value already passed (1)"));

                softly.assertThat(problemsOf(call("1", "a=2")).against(fiveArgs)).extracting(ALL).containsOnly(
                        problem(MULTIPLE_MATCH_PROBLEM, 44, 47, "Argument 'a' has value already passed (1)"));
                softly.assertThat(problemsOf(call("1", "2", "a=3", "b=4")).against(fiveArgs)).extracting(ALL).containsOnly(
                        problem(MULTIPLE_MATCH_PROBLEM, 49, 52, "Argument 'a' has value already passed (1)"),
                        problem(MULTIPLE_MATCH_PROBLEM, 56, 59, "Argument 'b' has value already passed (2)"));
            });
        }

        @Test
        public void listsAreReported_whenUsedAsArgumentInOrderToProvideMultipleArguments() {
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(problemsOf(call("@{l}")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(COLLECTION_WARNING, 39, 43, "List argument '@{l}' has to contain at least 1 item"));
                softly.assertThat(problemsOf(call("@{l}", "@{l}")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(COLLECTION_WARNING, 39, 43, "List argument '@{l}' has to be empty"),
                        problem(COLLECTION_WARNING, 47, 51, "List argument '@{l}' has to contain at least 1 item"));

                softly.assertThat(problemsOf(call("@{l}")).against(fiveArgs)).extracting(ALL).containsOnly(
                        problem(COLLECTION_WARNING, 39, 43, "List argument '@{l}' has to contain at least 3 items"));
                softly.assertThat(problemsOf(call("@{l}", "@{l}")).against(fiveArgs)).extracting(ALL).containsOnly(
                        problem(COLLECTION_WARNING, 39, 43, "List argument '@{l}' has to be empty"),
                        problem(COLLECTION_WARNING, 47, 51, "List argument '@{l}' has to contain at least 3 items"));
                softly.assertThat(problemsOf(call("1", "@{l}")).against(fiveArgs)).extracting(ALL).containsOnly(
                        problem(COLLECTION_WARNING, 44, 48, "List argument '@{l}' has to contain at least 2 items"));
                softly.assertThat(problemsOf(call("1", "@{l}", "3")).against(fiveArgs)).extracting(ALL).containsOnly(
                        problem(COLLECTION_WARNING, 44, 48, "List argument '@{l}' has to contain at least 1 item"));
            });
        }

        @Test
        public void dictionariesAreReported_whenUsedAsArgumentInOrderToProvideMultipleArguments() {
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(problemsOf(call("&{d}")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(COLLECTION_WARNING, 39, 43, "Dictionary argument '&{d}' has to contain at least 1 mapping. Required key: (a)"));
                softly.assertThat(problemsOf(call("&{d}", "&{d}")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(COLLECTION_WARNING, 39, 43, "Dictionary argument '&{d}' has to be empty"),
                        problem(COLLECTION_WARNING, 47, 51,
                                "Dictionary argument '&{d}' has to contain at least 1 mapping. Required key: (a)"));
                softly.assertThat(problemsOf(call("1", "&{d}")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(COLLECTION_WARNING, 44, 48, "Dictionary argument '&{d}' cannot have key: (a)"));
                softly.assertThat(problemsOf(call("&{d}", "b=2")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(COLLECTION_WARNING, 39, 43, "Dictionary argument '&{d}' has to contain at least 1 mapping. Required key: (a)"));
                
                softly.assertThat(problemsOf(call("&{d}")).against(fiveArgs)).extracting(ALL).containsOnly(
                        problem(COLLECTION_WARNING, 39, 43, "Dictionary argument '&{d}' has to contain at least 3 mappings. Required keys: (a, b, c)"));
                softly.assertThat(problemsOf(call("&{d}", "&{d}")).against(fiveArgs)).extracting(ALL).containsOnly(
                        problem(COLLECTION_WARNING, 39, 43, "Dictionary argument '&{d}' has to be empty"),
                        problem(COLLECTION_WARNING, 47, 51,
                                "Dictionary argument '&{d}' has to contain at least 3 mappings. Required keys: (a, b, c)"));
                softly.assertThat(problemsOf(call("1", "&{d}")).against(fiveArgs)).extracting(ALL).containsOnly(
                        problem(COLLECTION_WARNING, 44, 48, "Dictionary argument '&{d}' has to contain at least 2 mappings. Required keys: (b, c), forbidden key: (a)"));
                softly.assertThat(problemsOf(call("1", "2", "3", "4", "5", "&{d}")).against(fiveArgs)).extracting(ALL).containsOnly(
                        problem(COLLECTION_WARNING, 64, 68, "Dictionary argument '&{d}' cannot have keys: (a, b, c)"));
            });
        }
    }

    public static class ThreeArgsOptionalVarargsAndKwargsTest {

        private final ArgumentsDescriptor threeArgs = createDescriptor("a=1", "*vararg", "**kwarg");

        private final ArgumentsDescriptor fiveArgs = createDescriptor("a=1", "b=2", "c=3", "*vararg", "**kwarg");

        @Test
        public void noProblemsReported_whenArgumentsAreUsedProperly() {
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(problemsOf(call()).against(threeArgs)).isEmpty();
                softly.assertThat(problemsOf(call("1")).against(threeArgs)).isEmpty();
                softly.assertThat(problemsOf(call("1", "2")).against(threeArgs)).isEmpty();
                softly.assertThat(problemsOf(call("1", "2", "3")).against(threeArgs)).isEmpty();
                softly.assertThat(problemsOf(call("x=1")).against(threeArgs)).isEmpty();
                softly.assertThat(problemsOf(call("x=1", "y=2")).against(threeArgs)).isEmpty();
                softly.assertThat(problemsOf(call("a=1")).against(threeArgs)).isEmpty();
                softly.assertThat(problemsOf(call("b=1")).against(threeArgs)).isEmpty();
                softly.assertThat(problemsOf(call("a=1", "x=2")).against(threeArgs)).isEmpty();
                softly.assertThat(problemsOf(call("1", "x=2", "vararg=3", "kwarg=4")).against(threeArgs)).isEmpty();
                softly.assertThat(problemsOf(call("${s}")).against(threeArgs)).isEmpty();
                softly.assertThat(problemsOf(call("@{l}")).against(threeArgs)).isEmpty();
                softly.assertThat(problemsOf(call("@{l}", "@{l}")).against(threeArgs)).isEmpty();
                softly.assertThat(problemsOf(call("@{l}", "2")).against(threeArgs)).isEmpty();
                softly.assertThat(problemsOf(call("@{l}", "2", "3")).against(threeArgs)).isEmpty();
                softly.assertThat(problemsOf(call("@{l}", "2", "3", "4")).against(threeArgs)).isEmpty();
                softly.assertThat(problemsOf(call("&{d}")).against(threeArgs)).isEmpty();
                softly.assertThat(problemsOf(call("&{d}", "&{d}")).against(threeArgs)).isEmpty();
                softly.assertThat(problemsOf(call("&{d}", "x=2")).against(threeArgs)).isEmpty();
                softly.assertThat(problemsOf(call("&{d}", "a=2")).against(threeArgs)).isEmpty();
                softly.assertThat(problemsOf(call("@{l}[0]")).against(threeArgs)).isEmpty();
                softly.assertThat(problemsOf(call("&{d}[a]")).against(threeArgs)).isEmpty();

                softly.assertThat(problemsOf(call()).against(fiveArgs)).isEmpty();
                softly.assertThat(problemsOf(call("1")).against(fiveArgs)).isEmpty();
                softly.assertThat(problemsOf(call("1", "2")).against(fiveArgs)).isEmpty();
                softly.assertThat(problemsOf(call("1", "2", "3")).against(fiveArgs)).isEmpty();
                softly.assertThat(problemsOf(call("1", "2", "3", "4")).against(fiveArgs)).isEmpty();
                softly.assertThat(problemsOf(call("1", "2", "3", "4", "5")).against(fiveArgs)).isEmpty();
                softly.assertThat(problemsOf(call("x=1")).against(fiveArgs)).isEmpty();
                softly.assertThat(problemsOf(call("x=1", "y=2")).against(fiveArgs)).isEmpty();
                softly.assertThat(problemsOf(call("x=1", "y=2", "z=3")).against(fiveArgs)).isEmpty();
                softly.assertThat(problemsOf(call("a=1", "b=2")).against(fiveArgs)).isEmpty();
                softly.assertThat(problemsOf(call("b=1", "a=2")).against(fiveArgs)).isEmpty();
                softly.assertThat(problemsOf(call("a=1", "c=2", "b=3")).against(fiveArgs)).isEmpty();
                softly.assertThat(problemsOf(call("b=1", "c=2", "a=3")).against(fiveArgs)).isEmpty();
                softly.assertThat(problemsOf(call("c=1", "d=2")).against(fiveArgs)).isEmpty();
                softly.assertThat(problemsOf(call("1", "d=2")).against(fiveArgs)).isEmpty();
                softly.assertThat(problemsOf(call("a=1", "b=2", "c=3", "x=4")).against(fiveArgs)).isEmpty();
                softly.assertThat(problemsOf(call("a=1", "x=2", "b=3", "c=4")).against(fiveArgs)).isEmpty();
                softly.assertThat(problemsOf(call("${s}1", "${s}2", "${s}3")).against(fiveArgs)).isEmpty();
                softly.assertThat(problemsOf(call("@{l}")).against(fiveArgs)).isEmpty();
                softly.assertThat(problemsOf(call("@{l}", "@{l}")).against(fiveArgs)).isEmpty();
                softly.assertThat(problemsOf(call("1", "@{l}")).against(fiveArgs)).isEmpty();
                softly.assertThat(problemsOf(call("1", "@{l}", "3")).against(fiveArgs)).isEmpty();
                softly.assertThat(problemsOf(call("1", "@{l}", "3", "4", "5")).against(fiveArgs)).isEmpty();
                softly.assertThat(problemsOf(call("1", "@{l}", "3", "4", "5", "6")).against(fiveArgs)).isEmpty();
                softly.assertThat(problemsOf(call("&{d}")).against(fiveArgs)).isEmpty();
                softly.assertThat(problemsOf(call("&{d}", "&{d}")).against(fiveArgs)).isEmpty();
            });
        }

        @Test
        public void overriddenNamedArgumentsAreReported() {
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(problemsOf(call("a=1", "a=2")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(OVERRIDDEN_ARG_PROBLEM, 39, 42,
                                "Argument 'a' is passed multiple times using named syntax. This value will never be used"));
                softly.assertThat(problemsOf(call("a=1", "b=2", "c=3", "b=5", "a=6")).against(fiveArgs)).extracting(ALL).containsOnly(
                        problem(OVERRIDDEN_ARG_PROBLEM, 39, 42,
                                "Argument 'a' is passed multiple times using named syntax. This value will never be used"),
                        problem(OVERRIDDEN_ARG_PROBLEM, 46, 49,
                                "Argument 'b' is passed multiple times using named syntax. This value will never be used"));
            });
        }

        @Test
        public void invalidOrderIsReported_whenPositionalArgumentsAreUsedAfterNamedOne() {
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(problemsOf(call("a=1", "2")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(ORDER_PROBLEM, 46, 47, "Positional argument cannot be used after named arguments"));
                softly.assertThat(problemsOf(call("&{d}", "2")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(ORDER_PROBLEM, 47, 48, "Positional argument cannot be used after named arguments"));

                softly.assertThat(problemsOf(call("x=1", "a=2", "3")).against(fiveArgs)).extracting(ALL).containsOnly(
                        problem(ORDER_PROBLEM, 53, 54, "Positional argument cannot be used after named arguments"));
                softly.assertThat(problemsOf(call("a=1", "x=2", "${s}")).against(fiveArgs)).extracting(ALL).containsOnly(
                        problem(ORDER_PROBLEM, 53, 57, "Positional argument cannot be used after named arguments"));
                softly.assertThat(problemsOf(call("1", "&{d}", "3")).against(fiveArgs)).extracting(ALL).containsOnly(
                        problem(ORDER_PROBLEM, 52, 53, "Positional argument cannot be used after named arguments"));
                softly.assertThat(problemsOf(call("1", "&{d}", "3", "4")).against(fiveArgs)).extracting(ALL).containsOnly(
                        problem(ORDER_PROBLEM, 52, 53, "Positional argument cannot be used after named arguments"),
                        problem(ORDER_PROBLEM, 57, 58, "Positional argument cannot be used after named arguments"));
            });
        }

        @Test
        public void multipleMatchesAreReported_whenArgIsPassedPositionallyAndByName() {
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(problemsOf(call("1", "a=2")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(MULTIPLE_MATCH_PROBLEM, 44, 47, "Argument 'a' has value already passed (1)"));

                softly.assertThat(problemsOf(call("1", "a=2")).against(fiveArgs)).extracting(ALL).containsOnly(
                        problem(MULTIPLE_MATCH_PROBLEM, 44, 47, "Argument 'a' has value already passed (1)"));
                softly.assertThat(problemsOf(call("1", "2", "a=3", "b=4")).against(fiveArgs)).extracting(ALL).containsOnly(
                        problem(MULTIPLE_MATCH_PROBLEM, 49, 52, "Argument 'a' has value already passed (1)"),
                        problem(MULTIPLE_MATCH_PROBLEM, 56, 59, "Argument 'b' has value already passed (2)"));
            });
        }

        @Test
        public void dictionariesAreReported_whenUsedAsArgumentInOrderToProvideMultipleArguments() {
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(problemsOf(call("1", "&{d}")).against(threeArgs)).extracting(ALL).containsOnly(
                        problem(COLLECTION_WARNING, 44, 48, "Dictionary argument '&{d}' cannot have key: (a)"));
                
                softly.assertThat(problemsOf(call("1", "&{d}")).against(fiveArgs)).extracting(ALL).containsOnly(
                        problem(COLLECTION_WARNING, 44, 48, "Dictionary argument '&{d}' cannot have key: (a)"));
                softly.assertThat(problemsOf(call("1", "2", "3", "4", "5", "&{d}")).against(fiveArgs)).extracting(ALL).containsOnly(
                        problem(COLLECTION_WARNING, 64, 68, "Dictionary argument '&{d}' cannot have keys: (a, b, c)"));
            });
        }
    }

    public static class FourArgsRequiredOptionalVarargsAndKwargsTest {

        private final ArgumentsDescriptor fourArgs = createDescriptor("a", "b=2", "*vararg", "**kwarg");

        private final ArgumentsDescriptor sixArgs = createDescriptor("a", "b", "c=3", "d=4", "*vararg", "**kwarg");

        @Test
        public void noProblemsReported_whenArgumentsAreUsedProperly() {
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(problemsOf(call("1")).against(fourArgs)).isEmpty();
                softly.assertThat(problemsOf(call("1", "2")).against(fourArgs)).isEmpty();
                softly.assertThat(problemsOf(call("1", "2", "3")).against(fourArgs)).isEmpty();
                softly.assertThat(problemsOf(call("a=1")).against(fourArgs)).isEmpty();
                softly.assertThat(problemsOf(call("a=1", "x=2")).against(fourArgs)).isEmpty();
                softly.assertThat(problemsOf(call("1", "x=2", "vararg=3", "kwarg=4")).against(fourArgs)).isEmpty();
                softly.assertThat(problemsOf(call("${s}")).against(fourArgs)).isEmpty();
                softly.assertThat(problemsOf(call("@{l}", "2")).against(fourArgs)).isEmpty();
                softly.assertThat(problemsOf(call("@{l}", "2", "3")).against(fourArgs)).isEmpty();
                softly.assertThat(problemsOf(call("@{l}", "2", "3", "4")).against(fourArgs)).isEmpty();
                softly.assertThat(problemsOf(call("&{d}", "a=2")).against(fourArgs)).isEmpty();
                softly.assertThat(problemsOf(call("@{l}[0]")).against(fourArgs)).isEmpty();
                softly.assertThat(problemsOf(call("&{d}[a]")).against(fourArgs)).isEmpty();

                softly.assertThat(problemsOf(call("1", "2")).against(sixArgs)).isEmpty();
                softly.assertThat(problemsOf(call("1", "2", "3")).against(sixArgs)).isEmpty();
                softly.assertThat(problemsOf(call("1", "2", "3", "4")).against(sixArgs)).isEmpty();
                softly.assertThat(problemsOf(call("1", "2", "3", "4", "5")).against(sixArgs)).isEmpty();
                softly.assertThat(problemsOf(call("a=1", "b=2")).against(sixArgs)).isEmpty();
                softly.assertThat(problemsOf(call("b=1", "a=2")).against(sixArgs)).isEmpty();
                softly.assertThat(problemsOf(call("a=1", "c=2", "b=3")).against(sixArgs)).isEmpty();
                softly.assertThat(problemsOf(call("b=1", "c=2", "a=3")).against(sixArgs)).isEmpty();
                softly.assertThat(problemsOf(call("a=1", "b=2", "c=3", "x=4")).against(sixArgs)).isEmpty();
                softly.assertThat(problemsOf(call("a=1", "x=2", "c=3", "b=4")).against(sixArgs)).isEmpty();
                softly.assertThat(problemsOf(call("${s}1", "${s}2", "${s}3")).against(sixArgs)).isEmpty();
                softly.assertThat(problemsOf(call("1", "@{l}", "3")).against(sixArgs)).isEmpty();
                softly.assertThat(problemsOf(call("1", "@{l}", "3", "4", "5")).against(sixArgs)).isEmpty();
                softly.assertThat(problemsOf(call("1", "@{l}", "3", "4", "5", "6")).against(sixArgs)).isEmpty();
            });
        }

        @Test
        public void overridenNamedArgumentsAreReported() {
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(problemsOf(call("a=1", "a=2")).against(fourArgs)).extracting(ALL).containsOnly(
                        problem(OVERRIDDEN_ARG_PROBLEM, 39, 42,
                                "Argument 'a' is passed multiple times using named syntax. This value will never be used"));
                softly.assertThat(problemsOf(call("a=1", "b=2", "c=3", "b=5", "a=6")).against(sixArgs)).extracting(ALL).containsOnly(
                        problem(OVERRIDDEN_ARG_PROBLEM, 39, 42,
                                "Argument 'a' is passed multiple times using named syntax. This value will never be used"),
                        problem(OVERRIDDEN_ARG_PROBLEM, 46, 49,
                                "Argument 'b' is passed multiple times using named syntax. This value will never be used"));
            });
        }

        @Test
        public void invalidOrderIsReported_whenPositionalArgumentsAreUsedAfterNamedOne() {
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(problemsOf(call("a=1", "2")).against(fourArgs)).extracting(ALL).containsOnly(
                        problem(ORDER_PROBLEM, 46, 47, "Positional argument cannot be used after named arguments"));
                softly.assertThat(problemsOf(call("&{d}", "2")).against(fourArgs)).extracting(ALL).containsOnly(
                        problem(ORDER_PROBLEM, 47, 48, "Positional argument cannot be used after named arguments"));

                softly.assertThat(problemsOf(call("x=1", "a=2", "3")).against(sixArgs)).extracting(ALL).containsOnly(
                        problem(ORDER_PROBLEM, 53, 54, "Positional argument cannot be used after named arguments"));
                softly.assertThat(problemsOf(call("a=1", "x=2", "${s}")).against(sixArgs)).extracting(ALL).containsOnly(
                        problem(ORDER_PROBLEM, 53, 57, "Positional argument cannot be used after named arguments"));
                softly.assertThat(problemsOf(call("1", "&{d}", "3")).against(sixArgs)).extracting(ALL).containsOnly(
                        problem(ORDER_PROBLEM, 52, 53, "Positional argument cannot be used after named arguments"));
                softly.assertThat(problemsOf(call("1", "&{d}", "3", "4")).against(sixArgs)).extracting(ALL).containsOnly(
                        problem(ORDER_PROBLEM, 52, 53, "Positional argument cannot be used after named arguments"),
                        problem(ORDER_PROBLEM, 57, 58, "Positional argument cannot be used after named arguments"));
            });
        }

        @Test
        public void invalidNoOfArgumentsReported_whenNumberOfCallSiteArgumentsExceedsMaxNumberOfArgs() {
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(problemsOf(call()).against(fourArgs)).extracting(ALL).containsOnly(problem(
                        NUMBER_KW_PROBLEM, 28, 35,
                        "Invalid number of non-keyword arguments. Keyword 'keyword' expects at least 1 non-keyword argument but 0 are provided"));
                softly.assertThat(problemsOf(call("x=1")).against(fourArgs)).extracting(ALL).containsOnly(problem(
                        NUMBER_KW_PROBLEM, 28, 35,
                        "Invalid number of non-keyword arguments. Keyword 'keyword' expects at least 1 non-keyword argument but 0 are provided"));
                softly.assertThat(problemsOf(call("x=1", "y=2")).against(fourArgs)).extracting(ALL).containsOnly(problem(
                        NUMBER_KW_PROBLEM, 28, 35,
                        "Invalid number of non-keyword arguments. Keyword 'keyword' expects at least 1 non-keyword argument but 0 are provided"));

                softly.assertThat(problemsOf(call()).against(sixArgs)).extracting(ALL).containsOnly(problem(
                        NUMBER_KW_PROBLEM, 28, 35,
                        "Invalid number of non-keyword arguments. Keyword 'keyword' expects at least 2 non-keyword arguments but 0 are provided"));
                softly.assertThat(problemsOf(call("1")).against(sixArgs)).extracting(ALL).containsOnly(problem(
                        NUMBER_KW_PROBLEM, 28, 35,
                        "Invalid number of non-keyword arguments. Keyword 'keyword' expects at least 2 non-keyword arguments but 1 is provided"));
                softly.assertThat(problemsOf(call("x=1")).against(sixArgs)).extracting(ALL).containsOnly(problem(
                        NUMBER_KW_PROBLEM, 28, 35,
                        "Invalid number of non-keyword arguments. Keyword 'keyword' expects at least 2 non-keyword arguments but 0 are provided"));
                softly.assertThat(problemsOf(call("x=1", "y=2")).against(sixArgs)).extracting(ALL).containsOnly(problem(
                        NUMBER_KW_PROBLEM, 28, 35,
                        "Invalid number of non-keyword arguments. Keyword 'keyword' expects at least 2 non-keyword arguments but 0 are provided"));
                softly.assertThat(problemsOf(call("x=1", "y=2", "z=2")).against(sixArgs)).extracting(ALL).containsOnly(problem(
                        NUMBER_KW_PROBLEM, 28, 35,
                        "Invalid number of non-keyword arguments. Keyword 'keyword' expects at least 2 non-keyword arguments but 0 are provided"));
                softly.assertThat(problemsOf(call("${s}")).against(sixArgs)).extracting(ALL).containsOnly(problem(
                        NUMBER_KW_PROBLEM, 28, 35,
                        "Invalid number of non-keyword arguments. Keyword 'keyword' expects at least 2 non-keyword arguments but 1 is provided"));
            });
        }

        @Test
        public void multipleMatchesAreReported_whenArgIsPassedPositionallyAndByName() {
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(problemsOf(call("1", "a=2")).against(fourArgs)).extracting(ALL).containsOnly(
                        problem(MULTIPLE_MATCH_PROBLEM, 44, 47, "Argument 'a' has value already passed (1)"));

                softly.assertThat(problemsOf(call("1", "a=2")).against(sixArgs)).extracting(ALL).containsOnly(
                        problem(MULTIPLE_MATCH_PROBLEM, 44, 47, "Argument 'a' has value already passed (1)"));
                softly.assertThat(problemsOf(call("1", "2", "a=3", "b=4")).against(sixArgs)).extracting(ALL).containsOnly(
                        problem(MULTIPLE_MATCH_PROBLEM, 49, 52, "Argument 'a' has value already passed (1)"),
                        problem(MULTIPLE_MATCH_PROBLEM, 56, 59, "Argument 'b' has value already passed (2)"));
            });
        }

        @Test
        public void missingArgumentsAreReported_whenItIsNotProvidedAtCallSite() {
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(problemsOf(call("b=1")).against(fourArgs)).extracting(ALL).containsOnly(
                        problem(MISSING_PROBLEM, 28, 35, "Keyword 'keyword' requires (a) argument to be specified"));

                softly.assertThat(problemsOf(call("c=1", "d=2")).against(sixArgs)).extracting(ALL).containsOnly(
                        problem(MISSING_PROBLEM, 28, 35, "Keyword 'keyword' requires (a, b) arguments to be specified"));
                softly.assertThat(problemsOf(call("1", "d=2")).against(sixArgs)).extracting(ALL).containsOnly(
                        problem(MISSING_PROBLEM, 28, 35, "Keyword 'keyword' requires (b) argument to be specified"));
            });
        }

        @Test
        public void listsAreReported_whenUsedAsArgumentInOrderToProvideMultipleArguments() {
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(problemsOf(call("@{l}")).against(fourArgs)).extracting(ALL).containsOnly(
                        problem(COLLECTION_WARNING, 39, 43, "List argument '@{l}' has to contain at least 1 item"));
                softly.assertThat(problemsOf(call("@{l}", "@{l}")).against(fourArgs)).extracting(ALL).containsOnly(
                        problem(COLLECTION_WARNING, 39, 43, "List argument '@{l}' has to be empty"),
                        problem(COLLECTION_WARNING, 47, 51, "List argument '@{l}' has to contain at least 1 item"));

                softly.assertThat(problemsOf(call("@{l}")).against(sixArgs)).extracting(ALL).containsOnly(
                        problem(COLLECTION_WARNING, 39, 43, "List argument '@{l}' has to contain at least 2 items"));
                softly.assertThat(problemsOf(call("@{l}", "@{l}")).against(sixArgs)).extracting(ALL).containsOnly(
                        problem(COLLECTION_WARNING, 39, 43, "List argument '@{l}' has to be empty"),
                        problem(COLLECTION_WARNING, 47, 51, "List argument '@{l}' has to contain at least 2 items"));
                softly.assertThat(problemsOf(call("1", "@{l}")).against(sixArgs)).extracting(ALL).containsOnly(
                        problem(COLLECTION_WARNING, 44, 48, "List argument '@{l}' has to contain at least 1 item"));
            });
        }

        @Test
        public void dictionariesAreReported_whenUsedAsArgumentInOrderToProvideMultipleArguments() {
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(problemsOf(call("&{d}")).against(fourArgs)).extracting(ALL).containsOnly(
                        problem(COLLECTION_WARNING, 39, 43, "Dictionary argument '&{d}' has to contain at least 1 mapping. Required key: (a)"));
                softly.assertThat(problemsOf(call("&{d}", "&{d}")).against(fourArgs)).extracting(ALL).containsOnly(
                        problem(COLLECTION_WARNING, 39, 43, "Dictionary argument '&{d}' has to be empty"),
                        problem(COLLECTION_WARNING, 47, 51,
                                "Dictionary argument '&{d}' has to contain at least 1 mapping. Required key: (a)"));
                softly.assertThat(problemsOf(call("1", "&{d}")).against(fourArgs)).extracting(ALL).containsOnly(
                        problem(COLLECTION_WARNING, 44, 48, "Dictionary argument '&{d}' cannot have key: (a)"));
                softly.assertThat(problemsOf(call("&{d}", "b=2")).against(fourArgs)).extracting(ALL).containsOnly(
                        problem(COLLECTION_WARNING, 39, 43, "Dictionary argument '&{d}' has to contain at least 1 mapping. Required key: (a)"));
                
                softly.assertThat(problemsOf(call("&{d}")).against(sixArgs)).extracting(ALL).containsOnly(
                        problem(COLLECTION_WARNING, 39, 43, "Dictionary argument '&{d}' has to contain at least 2 mappings. Required keys: (a, b)"));
                softly.assertThat(problemsOf(call("&{d}", "&{d}")).against(sixArgs)).extracting(ALL).containsOnly(
                        problem(COLLECTION_WARNING, 39, 43, "Dictionary argument '&{d}' has to be empty"),
                        problem(COLLECTION_WARNING, 47, 51,
                                "Dictionary argument '&{d}' has to contain at least 2 mappings. Required keys: (a, b)"));
                softly.assertThat(problemsOf(call("1", "&{d}")).against(sixArgs)).extracting(ALL).containsOnly(
                        problem(COLLECTION_WARNING, 44, 48, "Dictionary argument '&{d}' has to contain at least 1 mapping. Required key: (b), forbidden key: (a)"));
                softly.assertThat(problemsOf(call("1", "2", "3", "4", "5", "&{d}")).against(sixArgs)).extracting(ALL).containsOnly(
                        problem(COLLECTION_WARNING, 64, 68, "Dictionary argument '&{d}' cannot have keys: (a, b, c, d)"));
            });
        }
    }

    private static RobotKeywordCall call(final String... args) {
        final String separator = Strings.repeat(" ", 4);

        return new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("test")
                .appendLine(separator + "keyword" + separator + String.join(separator, args))
                .build()
                .findSection(RobotCasesSection.class)
                .get()
                .getChildren()
                .get(0)
                .getChildren()
                .get(0);
    }

    private static Tuple problem(final Object... properties) {
        // adding synonym for better readablity
        return tuple(properties);
    }

    private static ValidationStep problemsOf(final RobotKeywordCall call) {
        return descriptor -> {
            final MockReporter reporter = new MockReporter();

            @SuppressWarnings("unchecked")
            final RobotExecutableRow<TestCase> executable = (RobotExecutableRow<TestCase>) call.getLinkedElement();
            final IExecutableRowDescriptor<?> executableRowDescriptor = executable.buildLineDescription();
            final RobotToken definingToken = executableRowDescriptor.getAction().getToken();
            final List<RobotToken> argumentTokens = executableRowDescriptor.getKeywordArguments();

            final GeneralKeywordCallArgumentsValidator validator = new GeneralKeywordCallArgumentsValidator(
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

    @FunctionalInterface
    private static interface ValidationStep {

        Collection<Problem> against(ArgumentsDescriptor descriptor);
    }
}
