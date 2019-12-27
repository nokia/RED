/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.read.postfixes;

import static com.google.common.collect.Sets.newHashSet;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.rf.ide.core.environment.RobotVersion;
import org.rf.ide.core.testdata.RobotParser;
import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.RobotFileOutput;
import org.rf.ide.core.testdata.model.RobotFileOutput.Status;
import org.rf.ide.core.testdata.model.RobotProjectHolder;
import org.rf.ide.core.testdata.model.table.tasks.Task;
import org.rf.ide.core.testdata.model.table.testcases.TestCase;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public class TemplateArgumentsFixerTest {

    @Test
    public void argumentsAreAddedToTestCaseWithTemplate() throws Exception {
        final RobotFile modelFile = getModelFile("TestCaseWithTemplate.robot");

        assertThat(modelFile.getSettingTable().isPresent()).isFalse();
        assertThat(modelFile.getVariableTable().isPresent()).isFalse();
        assertThat(modelFile.getTestCaseTable().isPresent()).isTrue();
        assertThat(modelFile.getTasksTable().isPresent()).isFalse();
        assertThat(modelFile.getKeywordTable().isPresent()).isFalse();

        final List<TestCase> cases = modelFile.getTestCaseTable().getTestCases();
        assertThat(cases).hasSize(1);

        final TestCase testCase = cases.get(0);
        final List<AModelElement<TestCase>> elements = testCase.getElements();
        assertThat(elements).hasSize(5);

        assertThat(elements.get(0).getModelType()).isEqualTo(ModelType.TEST_CASE_TEMPLATE);
        assertLine(elements.get(0), RobotTokenType.TEST_CASE_TEMPLATE_ARGUMENT, newHashSet());
        assertThat(elements.get(1).getModelType()).isEqualTo(ModelType.TEST_CASE_EXECUTABLE_ROW);
        assertLine(elements.get(1), RobotTokenType.TEST_CASE_TEMPLATE_ARGUMENT, newHashSet(0, 1));
        assertThat(elements.get(2).getModelType()).isEqualTo(ModelType.TEST_CASE_EXECUTABLE_ROW);
        assertLine(elements.get(2), RobotTokenType.TEST_CASE_TEMPLATE_ARGUMENT, newHashSet(0, 1, 2));
        assertThat(elements.get(3).getModelType()).isEqualTo(ModelType.TEST_CASE_EXECUTABLE_ROW);
        assertLine(elements.get(3), RobotTokenType.TEST_CASE_TEMPLATE_ARGUMENT, newHashSet(0));
        assertThat(elements.get(4).getModelType()).isEqualTo(ModelType.TEST_CASE_EXECUTABLE_ROW);
        assertLine(elements.get(4), RobotTokenType.TEST_CASE_TEMPLATE_ARGUMENT, newHashSet(0, 1, 2, 3, 4));
    }

    @Test
    public void argumentsAreAddedToTaskWithTemplate() throws Exception {
        final RobotFile modelFile = getModelFile("TaskWithTemplate.robot");

        assertThat(modelFile.getSettingTable().isPresent()).isFalse();
        assertThat(modelFile.getVariableTable().isPresent()).isFalse();
        assertThat(modelFile.getTestCaseTable().isPresent()).isFalse();
        assertThat(modelFile.getTasksTable().isPresent()).isTrue();
        assertThat(modelFile.getKeywordTable().isPresent()).isFalse();

        final List<Task> tasks = modelFile.getTasksTable().getTasks();
        assertThat(tasks).hasSize(1);

        final Task task = tasks.get(0);
        final List<AModelElement<Task>> elements = task.getElements();
        assertThat(elements).hasSize(5);

        assertThat(elements.get(0).getModelType()).isEqualTo(ModelType.TASK_TEMPLATE);
        assertLine(elements.get(0), RobotTokenType.TASK_TEMPLATE_ARGUMENT, newHashSet());
        assertThat(elements.get(1).getModelType()).isEqualTo(ModelType.TASK_EXECUTABLE_ROW);
        assertLine(elements.get(1), RobotTokenType.TASK_TEMPLATE_ARGUMENT, newHashSet(0, 1));
        assertThat(elements.get(2).getModelType()).isEqualTo(ModelType.TASK_EXECUTABLE_ROW);
        assertLine(elements.get(2), RobotTokenType.TASK_TEMPLATE_ARGUMENT, newHashSet(0, 1, 2));
        assertThat(elements.get(3).getModelType()).isEqualTo(ModelType.TASK_EXECUTABLE_ROW);
        assertLine(elements.get(3), RobotTokenType.TASK_TEMPLATE_ARGUMENT, newHashSet(0));
        assertThat(elements.get(4).getModelType()).isEqualTo(ModelType.TASK_EXECUTABLE_ROW);
        assertLine(elements.get(4), RobotTokenType.TASK_TEMPLATE_ARGUMENT, newHashSet(0, 1, 2, 3, 4));
    }

    @Test
    public void argumentsAreNotAddedToTestCaseWithDisabledTemplate() throws Exception {
        final RobotFile modelFile = getModelFile("TestCaseWithDisabledTemplate.robot");

        assertThat(modelFile.getSettingTable().isPresent()).isTrue();
        assertThat(modelFile.getVariableTable().isPresent()).isFalse();
        assertThat(modelFile.getTestCaseTable().isPresent()).isTrue();
        assertThat(modelFile.getTasksTable().isPresent()).isFalse();
        assertThat(modelFile.getKeywordTable().isPresent()).isFalse();

        final List<TestCase> cases = modelFile.getTestCaseTable().getTestCases();
        assertThat(cases).hasSize(1);

        final TestCase testCase = cases.get(0);
        final List<AModelElement<TestCase>> elements = testCase.getElements();
        assertThat(elements).hasSize(4);

        assertThat(elements.get(0).getModelType()).isEqualTo(ModelType.TEST_CASE_TEMPLATE);
        assertLine(elements.get(0), RobotTokenType.TEST_CASE_TEMPLATE_ARGUMENT, newHashSet());
        assertThat(elements.get(1).getModelType()).isEqualTo(ModelType.TEST_CASE_EXECUTABLE_ROW);
        assertLine(elements.get(1), RobotTokenType.TEST_CASE_TEMPLATE_ARGUMENT, newHashSet());
        assertThat(elements.get(2).getModelType()).isEqualTo(ModelType.TEST_CASE_EXECUTABLE_ROW);
        assertLine(elements.get(2), RobotTokenType.TEST_CASE_TEMPLATE_ARGUMENT, newHashSet());
        assertThat(elements.get(3).getModelType()).isEqualTo(ModelType.TEST_CASE_EXECUTABLE_ROW);
        assertLine(elements.get(3), RobotTokenType.TEST_CASE_TEMPLATE_ARGUMENT, newHashSet());
    }

    @Test
    public void argumentsAreNotAddedToTaskWithDisabledTemplate() throws Exception {
        final RobotFile modelFile = getModelFile("TaskWithDisabledTemplate.robot");

        assertThat(modelFile.getSettingTable().isPresent()).isTrue();
        assertThat(modelFile.getVariableTable().isPresent()).isFalse();
        assertThat(modelFile.getTestCaseTable().isPresent()).isFalse();
        assertThat(modelFile.getTasksTable().isPresent()).isTrue();
        assertThat(modelFile.getKeywordTable().isPresent()).isFalse();

        final List<Task> tasks = modelFile.getTasksTable().getTasks();
        assertThat(tasks).hasSize(1);

        final Task task = tasks.get(0);
        final List<AModelElement<Task>> elements = task.getElements();
        assertThat(elements).hasSize(4);

        assertThat(elements.get(0).getModelType()).isEqualTo(ModelType.TASK_TEMPLATE);
        assertLine(elements.get(0), RobotTokenType.TASK_TEMPLATE_ARGUMENT, newHashSet());
        assertThat(elements.get(1).getModelType()).isEqualTo(ModelType.TASK_EXECUTABLE_ROW);
        assertLine(elements.get(1), RobotTokenType.TASK_TEMPLATE_ARGUMENT, newHashSet());
        assertThat(elements.get(2).getModelType()).isEqualTo(ModelType.TASK_EXECUTABLE_ROW);
        assertLine(elements.get(2), RobotTokenType.TASK_TEMPLATE_ARGUMENT, newHashSet());
        assertThat(elements.get(3).getModelType()).isEqualTo(ModelType.TASK_EXECUTABLE_ROW);
        assertLine(elements.get(3), RobotTokenType.TASK_TEMPLATE_ARGUMENT, newHashSet());
    }

    @Test
    public void argumentsAreAddedToTestCaseWithForLoop() throws Exception {
        final RobotFile modelFile = getModelFile("TestCaseWithFor.robot");

        assertThat(modelFile.getSettingTable().isPresent()).isFalse();
        assertThat(modelFile.getVariableTable().isPresent()).isFalse();
        assertThat(modelFile.getTestCaseTable().isPresent()).isTrue();
        assertThat(modelFile.getTasksTable().isPresent()).isFalse();
        assertThat(modelFile.getKeywordTable().isPresent()).isFalse();

        final List<TestCase> cases = modelFile.getTestCaseTable().getTestCases();
        assertThat(cases).hasSize(1);

        final TestCase testCase = cases.get(0);
        final List<AModelElement<TestCase>> elements = testCase.getElements();
        assertThat(elements).hasSize(10);

        assertThat(elements.get(0).getModelType()).isEqualTo(ModelType.TEST_CASE_TEMPLATE);
        assertLine(elements.get(0), RobotTokenType.TEST_CASE_TEMPLATE_ARGUMENT, newHashSet());
        assertThat(elements.get(1).getModelType()).isEqualTo(ModelType.TEST_CASE_EXECUTABLE_ROW);
        assertLine(elements.get(1), RobotTokenType.TEST_CASE_TEMPLATE_ARGUMENT, newHashSet());
        assertThat(elements.get(2).getModelType()).isEqualTo(ModelType.TEST_CASE_EXECUTABLE_ROW);
        assertLine(elements.get(2), RobotTokenType.TEST_CASE_TEMPLATE_ARGUMENT, newHashSet(1, 2, 3));
        assertThat(elements.get(3).getModelType()).isEqualTo(ModelType.TEST_CASE_EXECUTABLE_ROW);
        assertLine(elements.get(3), RobotTokenType.TEST_CASE_TEMPLATE_ARGUMENT, newHashSet(1, 2));
        assertThat(elements.get(4).getModelType()).isEqualTo(ModelType.TEST_CASE_EXECUTABLE_ROW);
        assertLine(elements.get(4), RobotTokenType.TEST_CASE_TEMPLATE_ARGUMENT, newHashSet());
        assertThat(elements.get(5).getModelType()).isEqualTo(ModelType.TEST_CASE_EXECUTABLE_ROW);
        assertLine(elements.get(5), RobotTokenType.TEST_CASE_TEMPLATE_ARGUMENT, newHashSet());
        assertThat(elements.get(6).getModelType()).isEqualTo(ModelType.TEST_CASE_EXECUTABLE_ROW);
        assertLine(elements.get(6), RobotTokenType.TEST_CASE_TEMPLATE_ARGUMENT, newHashSet(1, 2, 3, 4));
        assertThat(elements.get(7).getModelType()).isEqualTo(ModelType.TEST_CASE_EXECUTABLE_ROW);
        assertLine(elements.get(7), RobotTokenType.TEST_CASE_TEMPLATE_ARGUMENT, newHashSet(1, 2, 3));
        assertThat(elements.get(8).getModelType()).isEqualTo(ModelType.TEST_CASE_EXECUTABLE_ROW);
        assertLine(elements.get(8), RobotTokenType.TEST_CASE_TEMPLATE_ARGUMENT, newHashSet(1, 2));
        assertThat(elements.get(9).getModelType()).isEqualTo(ModelType.TEST_CASE_EXECUTABLE_ROW);
        assertLine(elements.get(9), RobotTokenType.TEST_CASE_TEMPLATE_ARGUMENT, newHashSet(1));
    }

    private List<String> assertLine(final AModelElement<?> element, final RobotTokenType templateType,
            final Set<Integer> argIndexes) {
        for (int i = 0; i < element.getElementTokens().size(); i++) {
            final RobotToken robotToken = element.getElementTokens().get(i);
            if (argIndexes.contains(i)) {
                assertThat(robotToken.getTypes()).contains(templateType);
            } else {
                assertThat(robotToken.getTypes()).doesNotContain(templateType);
            }
        }
        return element.getElementTokens().stream().map(RobotToken::getText).collect(toList());
    }

    private RobotFile getModelFile(final String fileName) throws Exception {
        // prepare
        final RobotProjectHolder projectHolder = mock(RobotProjectHolder.class);
        final String mainPath = "parser/bugs/";
        final File file = new File(RobotParser.class.getResource(mainPath + fileName).toURI());
        when(projectHolder.shouldBeParsed(file)).thenReturn(true);

        // execute
        final RobotParser parser = new RobotParser(projectHolder, new RobotVersion(3, 1));
        final List<RobotFileOutput> parsed = parser.parse(file);

        // verify
        assertThat(parsed).hasSize(1);
        final RobotFileOutput robotFileOutput = parsed.get(0);
        assertThat(robotFileOutput.getStatus()).isEqualTo(Status.PASSED);
        return robotFileOutput.getFileModel();
    }
}
