/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;
import org.rf.ide.core.execution.context.RobotModelTestProvider;
import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.table.testcases.TestCase;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;

/**
 * @author wypych
 */
public class RobotExecutableRowViewTest {

    public static final String FILE_LOCATION = "exec//equalityShow//";

    private static RobotFile testFileModel;

    private final static Map<String, IExecutableStepsHolder<? extends AModelElement<? extends ARobotSectionTable>>> CACHED_UNITS = new HashMap<>(
            0);

    @BeforeClass
    public static void setUp() throws Exception {
        // prepare
        final Path inputFile = Paths.get(RobotExecutableRowViewTest.class
                .getResource(FILE_LOCATION + "RobotExecutableRow_EqualityTestInputFile.robot").toURI());
        testFileModel = RobotModelTestProvider.getModelFile(inputFile, RobotModelTestProvider.getParser());
        final TestCaseTable testCaseTable = testFileModel.getTestCaseTable();
        final List<TestCase> testCases = testCaseTable.getTestCases();
        for (final TestCase tc : testCases) {
            final String tcName = tc.getName().getText();
            if (CACHED_UNITS.containsKey(tcName)) {
                throw new IllegalStateException("Executable Unit with name " + tcName + " already exists in position "
                        + CACHED_UNITS.get(tcName).getName().getFilePosition());
            }

            CACHED_UNITS.put(tcName, tc);
        }
    }

    @Test
    public void given_testEqualityInsideToken_whenTryToSearchEqualitySigns_shouldReturn_emptyView() {
        // prepare
        final TestCase testCase = (TestCase) CACHED_UNITS.get("TestEqualityInsideToken");
        final RobotExecutableRow<TestCase> execRowOne = testCase.getExecutionContext().get(0);

        // execute
        final RobotExecutableRowView view = RobotExecutableRowView.buildView(execRowOne);

        // verify
        assertExecView(view, execRowOne, new HashMap<RobotToken, String>(),
                Arrays.asList("Log", "message=done", "INFO"));
    }

    @Test
    public void given_testEqualityAtBottomOfTextArgument_whenTryToSearchEqualitySigns_shouldReturn_emptyView() {
        // prepare
        final TestCase testCase = (TestCase) CACHED_UNITS.get("TestEqualityAtBottomOfTextArgument");
        final RobotExecutableRow<TestCase> execRowOne = testCase.getExecutionContext().get(0);

        // execute
        final RobotExecutableRowView view = RobotExecutableRowView.buildView(execRowOne);

        // verify
        assertExecView(view, execRowOne, new HashMap<RobotToken, String>(), Arrays.asList("Log", "message=", "INFO"));
    }

    @Test
    public void given_testVariableAssignmentWithoutEquality_whenTryToSearchEqualitySigns_shouldReturn_emptyView() {
        // prepare
        final TestCase testCase = (TestCase) CACHED_UNITS.get("TestVariableAssignmentWithoutEquality");
        final RobotExecutableRow<TestCase> execRowOne = testCase.getExecutionContext().get(0);

        // execute
        final RobotExecutableRowView view = RobotExecutableRowView.buildView(execRowOne);

        // verify
        assertExecView(view, execRowOne, new HashMap<RobotToken, String>(),
                Arrays.asList("${var}", "Set Variable", "10"));
    }

    @Test
    public void given_testVariableAssignmentWithEqualityAtComment_whenTryToSearchEqualitySigns_shouldReturn_emptyView() {
        // prepare
        final TestCase testCase = (TestCase) CACHED_UNITS.get("TestVariableAssignmentWithEqualityAtComment");
        final RobotExecutableRow<TestCase> execRowOne = testCase.getExecutionContext().get(0);

        // execute
        final RobotExecutableRowView view = RobotExecutableRowView.buildView(execRowOne);

        // verify
        assertExecView(view, execRowOne, new HashMap<RobotToken, String>(),
                Arrays.asList("${var}", "Set Variable", "10", "#${var}=", "pNONE as default"));
    }

    @Test
    public void given_testVariableAssignmentWithEqualityAtCommentAndPrettyAlign_whenTryToSearchEqualitySigns_shouldReturn_emptyView() {
        // prepare
        final TestCase testCase = (TestCase) CACHED_UNITS
                .get("TestVariableAssignmentWithEqualityAtCommentAndPrettyAlign");
        final RobotExecutableRow<TestCase> execRowOne = testCase.getExecutionContext().get(0);

        // execute
        final RobotExecutableRowView view = RobotExecutableRowView.buildView(execRowOne);

        // verify
        assertExecView(view, execRowOne, new HashMap<RobotToken, String>(),
                Arrays.asList("${var}", "Set Variable", "10", "#${var} =", "pNONE as default"));
    }

    @Test
    public void given_testVariableAssignmentWithEqualityAtCommentAndBreak_whenTryToSearchEqualitySigns_shouldReturn_emptyView() {
        // prepare
        final TestCase testCase = (TestCase) CACHED_UNITS.get("TestVariableAssignmentWithEqualityAtCommentAndBreak");
        final RobotExecutableRow<TestCase> execRowOne = testCase.getExecutionContext().get(0);

        // execute
        final RobotExecutableRowView view = RobotExecutableRowView.buildView(execRowOne);

        // verify
        assertExecView(view, execRowOne, new HashMap<RobotToken, String>(),
                Arrays.asList("${var}", "Set Variable", "10", "#${var}", "=", "pNONE as default"));
    }

    @Test
    public void given_testVariableAssignmentWithEquality_whenTryToSearchEqualitySigns_shouldReturn_oneElementView() {
        // prepare
        final TestCase testCase = (TestCase) CACHED_UNITS.get("TestVariableAssignmentWithEquality");
        final RobotExecutableRow<TestCase> execRowOne = testCase.getExecutionContext().get(0);

        // execute
        final RobotExecutableRowView view = RobotExecutableRowView.buildView(execRowOne);

        // verify
        final Map<RobotToken, String> views = new HashMap<>();
        views.put(execRowOne.getElementTokens().get(0), "=");
        assertExecView(view, execRowOne, views, Arrays.asList("${var}=", "Set Variable", "10"));
    }

    @Test
    public void given_testVariableAssignmentWithEqualityAndPrettyAlign_whenTryToSearchEqualitySigns_shouldReturn_oneElementView() {
        // prepare
        final TestCase testCase = (TestCase) CACHED_UNITS.get("TestVariableAssignmentWithEqualityAndPrettyAlign");
        final RobotExecutableRow<TestCase> execRowOne = testCase.getExecutionContext().get(0);

        // execute
        final RobotExecutableRowView view = RobotExecutableRowView.buildView(execRowOne);

        // verify
        final Map<RobotToken, String> views = new HashMap<>();
        views.put(execRowOne.getElementTokens().get(0), " =");
        assertExecView(view, execRowOne, views, Arrays.asList("${var} =", "Set Variable", "10"));
    }

    @Test
    public void given_testVariableAssignmentWithEquality_andPipeLineSeparated_whenTryToSearchEqualitySigns_shouldReturn_oneElementView() {
        // prepare
        final TestCase testCase = (TestCase) CACHED_UNITS
                .get("TestVariableAssignmentWithEquality_andPipeLineSeparated");
        final RobotExecutableRow<TestCase> execRowOne = testCase.getExecutionContext().get(0);

        // execute
        final RobotExecutableRowView view = RobotExecutableRowView.buildView(execRowOne);

        // verify
        final Map<RobotToken, String> views = new HashMap<>();
        views.put(execRowOne.getElementTokens().get(0), "=");
        assertExecView(view, execRowOne, views, Arrays.asList("${var}=", "Set Variable", "10\t"));
    }

    @Test
    public void given_testVariableAssignmentWithEqualityAndPrettyAlignDouble_andPipeLineSeparated_whenTryToSearchEqualitySigns_shouldReturn_oneElementView() {
        // prepare
        final TestCase testCase = (TestCase) CACHED_UNITS
                .get("TestVariableAssignmentWithEqualityAndPrettyAlignDouble_andPipeLineSeparated");
        final RobotExecutableRow<TestCase> execRowOne = testCase.getExecutionContext().get(0);

        // execute
        final RobotExecutableRowView view = RobotExecutableRowView.buildView(execRowOne);

        // verify
        final Map<RobotToken, String> views = new HashMap<>();
        views.put(execRowOne.getElementTokens().get(0), "  =");
        assertExecView(view, execRowOne, views, Arrays.asList("${var}  =", "Set Variable", "10"));
    }

    @Test
    public void given_testTwoVariablesAllCombinationOfEqualityAndAssignment_andPipeLineSeparated_whenTryToSearchEqualitySigns_shouldReturn_differentViews() {
        // prepare
        final TestCase testCase = (TestCase) CACHED_UNITS.get("TestTwoVariablesAllCombinationOfEqualityAndAssignment");
        final RobotExecutableRow<TestCase> execRowOne = testCase.getExecutionContext().get(0);
        final RobotExecutableRow<TestCase> execRowTwo = testCase.getExecutionContext().get(1);
        final RobotExecutableRow<TestCase> execRowThree = testCase.getExecutionContext().get(2);
        final RobotExecutableRow<TestCase> execRowFour = testCase.getExecutionContext().get(3);

        // execute
        final RobotExecutableRowView viewExecOne = RobotExecutableRowView.buildView(execRowOne);
        final RobotExecutableRowView viewExecTwo = RobotExecutableRowView.buildView(execRowTwo);
        final RobotExecutableRowView viewExecThree = RobotExecutableRowView.buildView(execRowThree);
        final RobotExecutableRowView viewExecFour = RobotExecutableRowView.buildView(execRowFour);

        // verify
        //// verify line one
        assertExecView(viewExecOne, execRowOne, new HashMap<RobotToken, String>(0),
                Arrays.asList("${err}", "${out}", "Execute Command", "getOut", "both"));

        //// verify line two
        final Map<RobotToken, String> viewsLineTwo = new HashMap<>();
        viewsLineTwo.put(execRowTwo.getElementTokens().get(0), "=");
        assertExecView(viewExecTwo, execRowTwo, viewsLineTwo,
                Arrays.asList("${err}=", "${out}", "Execute Command", "getOut", "both"));

        //// verify line three
        final Map<RobotToken, String> viewsLineThree = new HashMap<>();
        viewsLineThree.put(execRowThree.getElementTokens().get(1), "=");
        assertExecView(viewExecThree, execRowThree, viewsLineThree,
                Arrays.asList("${err}", "${out}=", "Execute Command", "getOut", "both"));

        //// verify line four
        final Map<RobotToken, String> viewsLineFour = new HashMap<>();
        viewsLineFour.put(execRowFour.getElementTokens().get(0), "=");
        viewsLineFour.put(execRowFour.getElementTokens().get(1), "=");
        assertExecView(viewExecFour, execRowFour, viewsLineFour,
                Arrays.asList("${err}=", "${out}=", "Execute Command", "getOut", "both"));
    }

    @Test
    public void given_TestTwoVariablesAllCombinationOfEqualityAndAssignment_andPipeLineSeparated_andPipeLineSeparated_whenTryToSearchEqualitySigns_shouldReturn_differentViews() {
        // prepare
        final TestCase testCase = (TestCase) CACHED_UNITS
                .get("TestTwoVariablesAllCombinationOfEqualityAndAssignment_andPipeLineSeparated");
        final RobotExecutableRow<TestCase> execRowOne = testCase.getExecutionContext().get(0);
        final RobotExecutableRow<TestCase> execRowTwo = testCase.getExecutionContext().get(1);
        final RobotExecutableRow<TestCase> execRowThree = testCase.getExecutionContext().get(2);
        final RobotExecutableRow<TestCase> execRowFour = testCase.getExecutionContext().get(3);
        final RobotExecutableRow<TestCase> execRowFive = testCase.getExecutionContext().get(4);

        // execute
        final RobotExecutableRowView viewExecOne = RobotExecutableRowView.buildView(execRowOne);
        final RobotExecutableRowView viewExecTwo = RobotExecutableRowView.buildView(execRowTwo);
        final RobotExecutableRowView viewExecThree = RobotExecutableRowView.buildView(execRowThree);
        final RobotExecutableRowView viewExecFour = RobotExecutableRowView.buildView(execRowFour);
        final RobotExecutableRowView viewExecFive = RobotExecutableRowView.buildView(execRowFive);

        // verify
        //// verify line one
        assertExecView(viewExecOne, execRowOne, new HashMap<RobotToken, String>(0),
                Arrays.asList("${err}", "${out}", "Execute Command", "getOut", "both"));

        //// verify line two
        final Map<RobotToken, String> viewsLineTwo = new HashMap<>();
        viewsLineTwo.put(execRowTwo.getElementTokens().get(0), " =");
        assertExecView(viewExecTwo, execRowTwo, viewsLineTwo,
                Arrays.asList("${err} =", "${out}", "Execute Command", "getOut", "both"));

        //// verify line three
        final Map<RobotToken, String> viewsLineThree = new HashMap<>();
        viewsLineThree.put(execRowThree.getElementTokens().get(1), " =");
        assertExecView(viewExecThree, execRowThree, viewsLineThree,
                Arrays.asList("${err}", "${out} =", "Execute Command", "getOut", "both"));

        //// verify line four
        final Map<RobotToken, String> viewsLineFour = new HashMap<>();
        viewsLineFour.put(execRowFour.getElementTokens().get(0), " =");
        viewsLineFour.put(execRowFour.getElementTokens().get(1), " =");
        assertExecView(viewExecFour, execRowFour, viewsLineFour,
                Arrays.asList("${err} =", "${out} =", "Execute Command", "getOut", "both"));

        //// verify line five
        final Map<RobotToken, String> viewsLineFive = new HashMap<>();
        viewsLineFive.put(execRowFive.getElementTokens().get(0), "  =");
        viewsLineFive.put(execRowFive.getElementTokens().get(1), "  =");
        assertExecView(viewExecFive, execRowFive, viewsLineFive,
                Arrays.asList("${err}  =", "${out}  =", "Execute Command", "getOut", "both"));

    }

    private void assertExecView(final RobotExecutableRowView view, final RobotExecutableRow<?> execRow,
            final Map<RobotToken, String> additionalTextTokens, final List<String> tokensView) {
        assertThat(view.getTokensWithSuffix().asMap()).hasSameSizeAs(additionalTextTokens);
        for (final RobotToken t : additionalTextTokens.keySet()) {
            assertThat(view.getTokensWithSuffix().get(t).get(0).getText()).isEqualTo(additionalTextTokens.get(t));
        }

        assertThat(execRow.getElementTokens()).hasSameSizeAs(tokensView);
        final List<RobotToken> elementTokens = execRow.getElementTokens();
        int size = tokensView.size();
        for (int i = 0; i < size; i++) {
            assertThat(view.getTokenRepresentation(elementTokens.get(i))).isEqualTo(tokensView.get(i));
        }
    }
}
