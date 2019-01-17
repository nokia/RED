/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.read.postfixes;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.net.URISyntaxException;
import java.util.List;

import org.junit.Test;
import org.rf.ide.core.environment.RobotVersion;
import org.rf.ide.core.testdata.RobotParser;
import org.rf.ide.core.testdata.RobotParser.RobotParserConfig;
import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.RobotFileOutput;
import org.rf.ide.core.testdata.model.RobotFileOutput.Status;
import org.rf.ide.core.testdata.model.RobotProjectHolder;
import org.rf.ide.core.testdata.model.table.keywords.UserKeyword;
import org.rf.ide.core.testdata.model.table.tasks.Task;
import org.rf.ide.core.testdata.model.table.testcases.TestCase;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;

public class EmptyLinesInExecutableTablesFixerOutputCheckTest {

    @Test
    public void thereIsOnlyDocumentationEmptyRowAndExecutionRow_whenEmptyLineIsInsideDocumentationOfTestCase()
            throws URISyntaxException {
        final RobotFile modelFile = getModelFile("DocumentationWithEmptyLinesInTestCase.robot");

        assertThat(modelFile.getSettingTable().isPresent()).isFalse();
        assertThat(modelFile.getVariableTable().isPresent()).isFalse();
        assertThat(modelFile.getTestCaseTable().isPresent()).isTrue();
        assertThat(modelFile.getTasksTable().isPresent()).isFalse();
        assertThat(modelFile.getKeywordTable().isPresent()).isFalse();

        final List<TestCase> cases = modelFile.getTestCaseTable().getTestCases();
        assertThat(cases).hasSize(1);

        final TestCase testCase = cases.get(0);
        final List<AModelElement<TestCase>> elements = testCase.getElements();
        assertThat(elements).hasSize(4);

        assertThat(elements.get(0).getModelType()).isEqualTo(ModelType.TEST_CASE_DOCUMENTATION);
        assertThat(cellsOf(elements.get(0))).containsExactly("[Documentation]", "...", "first line", "", "...",
                "second line");
        assertThat(elements.get(1).getModelType()).isEqualTo(ModelType.TEST_CASE_EXECUTABLE_ROW);
        assertThat(cellsOf(elements.get(1))).containsExactly("Log", "1");
        assertThat(elements.get(2).getModelType()).isEqualTo(ModelType.EMPTY_LINE);
        assertThat(cellsOf(elements.get(2))).containsExactly("    ");
        assertThat(elements.get(3).getModelType()).isEqualTo(ModelType.TEST_CASE_EXECUTABLE_ROW);
        assertThat(cellsOf(elements.get(3))).containsExactly("Log", "2");
    }

    @Test
    public void thereIsOnlyDocumentationEmptyRowAndExecutionRow_whenEmptyLineIsInsideDocumentationOfTask()
            throws URISyntaxException {
        final RobotFile modelFile = getModelFile("DocumentationWithEmptyLinesInTask.robot");

        assertThat(modelFile.getSettingTable().isPresent()).isFalse();
        assertThat(modelFile.getVariableTable().isPresent()).isFalse();
        assertThat(modelFile.getTestCaseTable().isPresent()).isFalse();
        assertThat(modelFile.getTasksTable().isPresent()).isTrue();
        assertThat(modelFile.getKeywordTable().isPresent()).isFalse();

        final List<Task> tasks = modelFile.getTasksTable().getTasks();
        assertThat(tasks).hasSize(1);

        final Task task = tasks.get(0);
        final List<AModelElement<Task>> elements = task.getElements();
        assertThat(elements).hasSize(4);

        assertThat(elements.get(0).getModelType()).isEqualTo(ModelType.TASK_DOCUMENTATION);
        assertThat(cellsOf(elements.get(0))).containsExactly("[Documentation]", "...", "first line", "", "...",
                "second line");
        assertThat(elements.get(1).getModelType()).isEqualTo(ModelType.TASK_EXECUTABLE_ROW);
        assertThat(cellsOf(elements.get(1))).containsExactly("Log", "1");
        assertThat(elements.get(2).getModelType()).isEqualTo(ModelType.EMPTY_LINE);
        assertThat(cellsOf(elements.get(2))).containsExactly("    ");
        assertThat(elements.get(3).getModelType()).isEqualTo(ModelType.TASK_EXECUTABLE_ROW);
        assertThat(cellsOf(elements.get(3))).containsExactly("Log", "2");
    }

    @Test
    public void thereIsOnlyDocumentationEmptyRowAndExecutionRow_whenEmptyLineIsInsideDocumentationOfUserKeyword()
            throws URISyntaxException {
        final RobotFile modelFile = getModelFile("DocumentationWithEmptyLinesInUserKeyword.robot");

        assertThat(modelFile.getSettingTable().isPresent()).isFalse();
        assertThat(modelFile.getVariableTable().isPresent()).isFalse();
        assertThat(modelFile.getTestCaseTable().isPresent()).isFalse();
        assertThat(modelFile.getTasksTable().isPresent()).isFalse();
        assertThat(modelFile.getKeywordTable().isPresent()).isTrue();

        final List<UserKeyword> keywords = modelFile.getKeywordTable().getKeywords();
        assertThat(keywords).hasSize(1);

        final UserKeyword keyword = keywords.get(0);
        final List<AModelElement<UserKeyword>> elements = keyword.getElements();
        assertThat(elements).hasSize(4);

        assertThat(elements.get(0).getModelType()).isEqualTo(ModelType.USER_KEYWORD_DOCUMENTATION);
        assertThat(cellsOf(elements.get(0))).containsExactly("[Documentation]", "...", "first line", "", "...",
                "second line");
        assertThat(elements.get(1).getModelType()).isEqualTo(ModelType.USER_KEYWORD_EXECUTABLE_ROW);
        assertThat(cellsOf(elements.get(1))).containsExactly("Log", "1");
        assertThat(elements.get(2).getModelType()).isEqualTo(ModelType.EMPTY_LINE);
        assertThat(cellsOf(elements.get(2))).containsExactly("    ");
        assertThat(elements.get(3).getModelType()).isEqualTo(ModelType.USER_KEYWORD_EXECUTABLE_ROW);
        assertThat(cellsOf(elements.get(3))).containsExactly("Log", "2");
    }

    private List<String> cellsOf(final AModelElement<?> element) {
        return element.getElementTokens().stream().map(RobotToken::getText).collect(toList());
    }

    private RobotFile getModelFile(final String fileName) throws URISyntaxException {
        final RobotProjectHolder projectHolder = mock(RobotProjectHolder.class);
        final File file = new File(RobotParser.class.getResource("parser/bugs/" + fileName).toURI());
        when(projectHolder.shouldBeLoaded(file)).thenReturn(true);

        final RobotParser parser = RobotParser.create(projectHolder,
                RobotParserConfig.allImportsLazy(new RobotVersion(3, 1)));
        final List<RobotFileOutput> parsed = parser.parse(file);

        // verify
        assertThat(parsed).hasSize(1);
        final RobotFileOutput robotFileOutput = parsed.get(0);
        assertThat(robotFileOutput.getStatus()).isEqualTo(Status.PASSED);
        return robotFileOutput.getFileModel();
    }
}
