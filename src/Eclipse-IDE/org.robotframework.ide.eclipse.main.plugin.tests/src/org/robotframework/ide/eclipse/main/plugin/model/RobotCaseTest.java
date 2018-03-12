/*
* Copyright 2016 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.robotframework.ide.eclipse.main.plugin.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.robotframework.ide.eclipse.main.plugin.model.ModelConditions.filePositions;
import static org.robotframework.ide.eclipse.main.plugin.model.ModelConditions.noFilePositions;
import static org.robotframework.ide.eclipse.main.plugin.model.ModelConditions.nullParent;

import java.util.List;

import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.RobotSuiteFileCreator;

public class RobotCaseTest {

    @Test
    public void copyBySerializationTest() {
        for (final RobotCase testCase : createCasesForTest()) {
            
            assertThat(testCase).has(RobotCaseConditions.properlySetParent()).has(filePositions());
            for (final RobotKeywordCall call : testCase.getChildren()) {
                assertThat(call).has(RobotKeywordCallConditions.properlySetParent()).has(filePositions());
            }

            final RobotCase testCaseCopy = ModelElementsSerDe.copy(testCase);

            assertThat(testCaseCopy).isNotSameAs(testCase).has(nullParent()).has(noFilePositions());
            assertThat(testCaseCopy.getChildren().size()).isEqualTo(testCase.getChildren().size());
            for (int i = 0; i < testCaseCopy.getChildren().size(); i++) {
                final RobotKeywordCall call = testCase.getChildren().get(i);
                final RobotKeywordCall callCopy = testCaseCopy.getChildren().get(i);

                assertThat(callCopy).isNotSameAs(call)
                        .has(RobotKeywordCallConditions.properlySetParent())
                        .has(noFilePositions());

                assertThat(callCopy.getName()).isEqualTo(call.getName());
                assertThat(callCopy.getArguments()).containsExactlyElementsOf(call.getArguments());
                assertThat(callCopy.getComment()).isEqualTo(call.getComment());
            }

            assertThat(testCaseCopy.getName()).isEqualTo(testCase.getName());
            assertThat(testCaseCopy.getComment()).isEqualTo(testCase.getComment());
        }
    }

    private static List<RobotCase> createCasesForTest() {
        final RobotSuiteFile model = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("case1")
                .appendLine("  kw  1  2  3")
                .appendLine("  ${x}=  fib  5")
                .appendLine("  kw  1  # c")
                .appendLine("  [Documentation]  # c  d")
                .appendLine("  [Tags]  a  b  # c  d")
                .appendLine("  [Setup]  a  b  # c  d")
                .appendLine("  [Teardown]  a  b  # c  d")
                .appendLine("  [Template]  a  b  # c  d")
                .appendLine("  [Timeout]  a  b  # c  d")
                .appendLine("  [unknown]  a  b  # c  d")
                .appendLine("case2")
                .appendLine("case3")
                .appendLine("  abc")
                .build();
        final RobotCasesSection casesSection = model.findSection(RobotCasesSection.class).get();
        return casesSection.getChildren();
    }
}
