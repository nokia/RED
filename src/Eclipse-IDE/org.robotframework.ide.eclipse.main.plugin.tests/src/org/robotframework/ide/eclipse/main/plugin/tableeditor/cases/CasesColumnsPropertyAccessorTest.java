/*
* Copyright 2017 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.robotframework.ide.eclipse.main.plugin.tableeditor.cases;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.rf.ide.core.testdata.model.ModelType;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.RobotSuiteFileCreator;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCasesSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotDefinitionSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;

public class CasesColumnsPropertyAccessorTest {

    @Test
    public void nameOfTestCaseIsProvided_forFirstColumnOfRobotCase() {
        final int numberOfColumns = 7;
        final RobotCase testCase = createTestCase("case 1");

        final CasesColumnsPropertyAccessor propertyAccessor = new CasesColumnsPropertyAccessor(
                new RobotEditorCommandsStack(), numberOfColumns);

        assertThat(propertyAccessor.getDataValue(testCase, 0)).isEqualTo("case 1");
    }

    @Test
    public void emptyStringIsProvided_forOtherThanFirstColumnOfRobotCase() {
        final int numberOfColumns = 7;
        final RobotCase testCase = createTestCase("case 1");

        final CasesColumnsPropertyAccessor propertyAccessor = new CasesColumnsPropertyAccessor(
                new RobotEditorCommandsStack(), numberOfColumns);

        for (int i = 1; i < numberOfColumns; i++) {
            assertThat(propertyAccessor.getDataValue(testCase, i)).isEmpty();
        }
    }

    @Test
    public void wholeDocumentationIsWrittenInSecondColumnAndNothingInOtherColumns() {
        final int numberOfColumns = 7;
        final RobotCase testCase = createTestCase("case 1");
        final RobotDefinitionSetting documentation = testCase.findSetting(ModelType.TEST_CASE_DOCUMENTATION).get();

        final CasesColumnsPropertyAccessor propertyAccessor = new CasesColumnsPropertyAccessor(
                new RobotEditorCommandsStack(), numberOfColumns);

        assertThat(propertyAccessor.getDataValue(documentation, 0)).isEqualTo("[Documentation]");
        assertThat(propertyAccessor.getDataValue(documentation, 1)).isEqualTo("abc def");
        assertThat(propertyAccessor.getDataValue(documentation, 2)).isEmpty();
        assertThat(propertyAccessor.getDataValue(documentation, 3)).isEmpty();
        assertThat(propertyAccessor.getDataValue(documentation, 4)).isEmpty();
        assertThat(propertyAccessor.getDataValue(documentation, 5)).isEmpty();
        assertThat(propertyAccessor.getDataValue(documentation, 6)).isEmpty();
    }

    @Test
    public void settingNameOrCallIsReturned_forFirstColumnOfKeywordCall() {
        final int numberOfColumns = 7;
        final RobotCase testCase = createTestCase("case 1");

        final CasesColumnsPropertyAccessor propertyAccessor = new CasesColumnsPropertyAccessor(
                new RobotEditorCommandsStack(), numberOfColumns);

        assertThat(propertyAccessor.getDataValue(testCase.getChildren().get(0), 0)).isEqualTo("[Tags]");
        assertThat(propertyAccessor.getDataValue(testCase.getChildren().get(1), 0)).isEqualTo("Log");
        assertThat(propertyAccessor.getDataValue(testCase.getChildren().get(2), 0)).isEqualTo("[Setup]");
        assertThat(propertyAccessor.getDataValue(testCase.getChildren().get(3), 0)).isEqualTo("[Teardown]");
        assertThat(propertyAccessor.getDataValue(testCase.getChildren().get(4), 0)).isEqualTo("[Timeout]");
        assertThat(propertyAccessor.getDataValue(testCase.getChildren().get(5), 0)).isEqualTo("Log");
        // no assertion for documentation; asserted in separate test
        assertThat(propertyAccessor.getDataValue(testCase.getChildren().get(7), 0)).isEqualTo("[unknown]");
        assertThat(propertyAccessor.getDataValue(testCase.getChildren().get(8), 0)).isEqualTo("Log");
    }

    @Test
    public void settingArgumentOrCallArgumentIsReturned_forSecondAndConsecutiveColumns() {
        final int numberOfColumns = 7;
        final RobotCase testCase = createTestCase("case 1");

        final CasesColumnsPropertyAccessor propertyAccessor = new CasesColumnsPropertyAccessor(
                new RobotEditorCommandsStack(), numberOfColumns);

        assertThat(propertyAccessor.getDataValue(testCase.getChildren().get(0), 1)).isEqualTo("a");
        assertThat(propertyAccessor.getDataValue(testCase.getChildren().get(1), 1)).isEqualTo("10");
        assertThat(propertyAccessor.getDataValue(testCase.getChildren().get(2), 1)).isEqualTo("Log");
        assertThat(propertyAccessor.getDataValue(testCase.getChildren().get(3), 1)).isEqualTo("Log");
        assertThat(propertyAccessor.getDataValue(testCase.getChildren().get(4), 1)).isEqualTo("10");
        assertThat(propertyAccessor.getDataValue(testCase.getChildren().get(5), 1)).isEqualTo("10");
        // no assertion for documentation; asserted in separate test
        assertThat(propertyAccessor.getDataValue(testCase.getChildren().get(7), 1)).isEqualTo("abc");
        assertThat(propertyAccessor.getDataValue(testCase.getChildren().get(8), 1)).isEqualTo("10");

        assertThat(propertyAccessor.getDataValue(testCase.getChildren().get(0), 2)).isEqualTo("b");
        assertThat(propertyAccessor.getDataValue(testCase.getChildren().get(1), 2)).isEmpty();
        assertThat(propertyAccessor.getDataValue(testCase.getChildren().get(2), 2)).isEqualTo("xxx");
        assertThat(propertyAccessor.getDataValue(testCase.getChildren().get(3), 2)).isEqualTo("yyy");
        assertThat(propertyAccessor.getDataValue(testCase.getChildren().get(4), 2)).isEqualTo("x");
        assertThat(propertyAccessor.getDataValue(testCase.getChildren().get(5), 2)).isEmpty();
        // no assertion for documentation; asserted in separate test
        assertThat(propertyAccessor.getDataValue(testCase.getChildren().get(7), 2)).isEqualTo("def");
        assertThat(propertyAccessor.getDataValue(testCase.getChildren().get(8), 2)).isEmpty();

        assertThat(propertyAccessor.getDataValue(testCase.getChildren().get(0), 3)).isEmpty();
        assertThat(propertyAccessor.getDataValue(testCase.getChildren().get(1), 3)).isEmpty();
        assertThat(propertyAccessor.getDataValue(testCase.getChildren().get(2), 3)).isEmpty();
        assertThat(propertyAccessor.getDataValue(testCase.getChildren().get(3), 3)).isEmpty();
        assertThat(propertyAccessor.getDataValue(testCase.getChildren().get(4), 3)).isEqualTo("y");
        assertThat(propertyAccessor.getDataValue(testCase.getChildren().get(5), 3)).isEmpty();
        // no assertion for documentation; asserted in separate test
        assertThat(propertyAccessor.getDataValue(testCase.getChildren().get(7), 3)).isEmpty();
        assertThat(propertyAccessor.getDataValue(testCase.getChildren().get(8), 3)).isEmpty();

        for (int i = 4; i < numberOfColumns; i++) {
            assertThat(propertyAccessor.getDataValue(testCase.getChildren().get(0), i)).isEmpty();
            assertThat(propertyAccessor.getDataValue(testCase.getChildren().get(1), i)).isEmpty();
            assertThat(propertyAccessor.getDataValue(testCase.getChildren().get(2), i)).isEmpty();
            assertThat(propertyAccessor.getDataValue(testCase.getChildren().get(3), i)).isEmpty();
            assertThat(propertyAccessor.getDataValue(testCase.getChildren().get(4), i)).isEmpty();
            assertThat(propertyAccessor.getDataValue(testCase.getChildren().get(5), i)).isEmpty();
            // no assertion for documentation; asserted in separate test
            assertThat(propertyAccessor.getDataValue(testCase.getChildren().get(7), i)).isEmpty();
            assertThat(propertyAccessor.getDataValue(testCase.getChildren().get(8), i)).isEmpty();
        }
    }

    private static RobotCase createTestCase(final String caseName) {
        final RobotSuiteFile model = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine(caseName)
                .appendLine("  [Tags]  a  b")
                .appendLine("  Log  10")
                .appendLine("  [Setup]  Log  xxx")
                .appendLine("  [Teardown]  Log  yyy")
                .appendLine("  [Timeout]  10  x  y")
                .appendLine("  Log  10")
                .appendLine("  [Documentation]  abc    def")
                .appendLine("  [unknown]  abc    def")
                .appendLine("  Log  10")
                .build();
        final RobotCasesSection section = model.findSection(RobotCasesSection.class).get();
        return section.getChildren().get(0);
    }
}
