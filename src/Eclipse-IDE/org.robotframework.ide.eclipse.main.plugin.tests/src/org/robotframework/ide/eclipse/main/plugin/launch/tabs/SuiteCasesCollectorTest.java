/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch.tabs;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.rf.ide.core.environment.RobotVersion;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.RobotSuiteFileCreator;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;

public class SuiteCasesCollectorTest {

    @Test
    public void emptyNamesAreReturned_forEmptyFile() throws Exception {
        final RobotSuiteFile file = new RobotSuiteFileCreator(new RobotVersion(3, 1)).build();
        assertThat(SuiteCasesCollector.collectCaseNames(file)).isEmpty();
    }

    @Test
    public void emptyNamesAreReturned_forFileWithoutCaseSection() throws Exception {
        final RobotSuiteFile file = new RobotSuiteFileCreator(new RobotVersion(3, 1)).appendLine("*** Keywords ***")
                .appendLine("keyword")
                .build();
        assertThat(SuiteCasesCollector.collectCaseNames(file)).isEmpty();
    }

    @Test
    public void emptyNamesAreReturned_forFileWithoutCases() throws Exception {
        final RobotSuiteFile file = new RobotSuiteFileCreator(new RobotVersion(3, 1)).appendLine("*** Test Cases ***")
                .build();
        assertThat(SuiteCasesCollector.collectCaseNames(file)).isEmpty();
    }

    @Test
    public void allTestCaseNamesAreReturned_forFileWithTestCaseSection() throws Exception {
        final RobotSuiteFile file = new RobotSuiteFileCreator(new RobotVersion(3, 1)).appendLine("*** Test Cases ***")
                .appendLine("test_case_1")
                .appendLine("test_case_2")
                .appendLine("test_case_3")
                .build();
        assertThat(SuiteCasesCollector.collectCaseNames(file)).containsExactly("test_case_1", "test_case_2",
                "test_case_3");
    }

    @Test
    public void allTestCaseNamesAreReturned_forFileWithSeveralTestCaseSections() throws Exception {
        final RobotSuiteFile file = new RobotSuiteFileCreator(new RobotVersion(3, 1)).appendLine("*** Test Cases ***")
                .appendLine("test_case_1")
                .appendLine("test_case_2")
                .appendLine("test_case_3")
                .appendLine("*** Test Cases ***")
                .appendLine("test_case_4")
                .appendLine("test_case_5")
                .appendLine("*** Test Cases ***")
                .appendLine("test_case_6")
                .build();
        assertThat(SuiteCasesCollector.collectCaseNames(file)).containsExactly("test_case_1", "test_case_2",
                "test_case_3", "test_case_4", "test_case_5", "test_case_6");
    }

    @Test
    public void allTestCaseAndTaskNamesAreReturned_forFileWithTestCaseAndTaskSections() throws Exception {
        final RobotSuiteFile file = new RobotSuiteFileCreator(new RobotVersion(3, 1)).appendLine("*** Test Cases ***")
                .appendLine("test_case_1")
                .appendLine("test_case_2")
                .appendLine("*** Tasks ***")
                .appendLine("task_case_1")
                .appendLine("task_case_2")
                .build();
        assertThat(SuiteCasesCollector.collectCaseNames(file)).containsExactly("test_case_1", "test_case_2",
                "task_case_1", "task_case_2");
    }

    @Test
    public void emptyNamesAreReturned_forFileWithNotSupportedTaskCases() throws Exception {
        final RobotSuiteFile file = new RobotSuiteFileCreator(new RobotVersion(3, 0)).appendLine("*** Tasks ***")
                .appendLine("task_case")
                .build();
        assertThat(SuiteCasesCollector.collectCaseNames(file)).isEmpty();
    }

    @Test
    public void onlyTestCaseNamesAreReturned_forFileWithNotSupportedTaskCases() throws Exception {
        final RobotSuiteFile file = new RobotSuiteFileCreator(new RobotVersion(3, 0)).appendLine("*** Test Cases ***")
                .appendLine("test_case_1")
                .appendLine("test_case_2")
                .appendLine("*** Tasks ***")
                .appendLine("task_case_1")
                .appendLine("task_case_2")
                .build();
        assertThat(SuiteCasesCollector.collectCaseNames(file)).containsExactly("test_case_1", "test_case_2");
    }
}
