package org.robotframework.ide.eclipse.main.plugin.model;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.robotframework.ide.eclipse.main.plugin.model.ModelConditions.filePositions;
import static org.robotframework.ide.eclipse.main.plugin.model.ModelConditions.noFilePositions;
import static org.robotframework.ide.eclipse.main.plugin.model.ModelConditions.nullParent;

import java.util.EnumSet;
import java.util.List;

import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.RobotSuiteFileCreator;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCase.PrioriterizedCaseSettings;

public class RobotCaseTest {

    @Test
    public void settingsOrderTest() {
        // the order of enum's fields is important, so we want to quickly see
        // that this change may harm GUI tests etc.
        final List<PrioriterizedCaseSettings> caseSettings = newArrayList(
                EnumSet.allOf(PrioriterizedCaseSettings.class));

        assertThat(caseSettings).containsExactly(PrioriterizedCaseSettings.DOCUMENTATION,
                PrioriterizedCaseSettings.TAGS, PrioriterizedCaseSettings.SETUP, PrioriterizedCaseSettings.TEARDOWN,
                PrioriterizedCaseSettings.TEMPLATE, PrioriterizedCaseSettings.TIMEOUT,
                PrioriterizedCaseSettings.UNKNOWN);
    }

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
