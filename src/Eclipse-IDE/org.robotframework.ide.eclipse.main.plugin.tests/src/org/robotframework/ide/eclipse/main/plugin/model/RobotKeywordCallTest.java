package org.robotframework.ide.eclipse.main.plugin.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.robotframework.ide.eclipse.main.plugin.model.ModelConditions.filePositions;
import static org.robotframework.ide.eclipse.main.plugin.model.ModelConditions.noFilePositions;
import static org.robotframework.ide.eclipse.main.plugin.model.ModelConditions.nullParent;

import java.util.List;

import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.RobotSuiteFileCreator;

public class RobotKeywordCallTest {

    @Test
    public void testNameGettingForCaseCalls() {
        assertName(createCallsFromCaseForTest());
    }

    @Test
    public void testNameGettingForKeywordCalls() {
        assertName(createCallsFromKeywordForTest());
    }

    @Test
    public void testLabelGettingForCaseCalls() {
        assertLabel(createCallsFromCaseForTest());
    }

    @Test
    public void testLabelGettingForKeywordCalls() {
        assertLabel(createCallsFromKeywordForTest());
    }

    @Test
    public void testArgumentsGettingForCaseCalls() {
        assertArguments(createCallsFromCaseForTest());
    }

    @Test
    public void testArgumentsGettingForKeywordCalls() {
        assertArguments(createCallsFromKeywordForTest());
    }

    @Test
    public void testArgumentsForKeywordCallFollowedByCommentedSection() {
        final RobotSuiteFile model = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("t1")
                .appendLine("  Log  t")
                .appendLine("  ")
                .appendLine("  ")
                .appendLine("# *** Settings ***")
                .appendLine("# Documentation    set3  set4")
                .build();
        final List<RobotKeywordCall> calls = model.findSection(RobotCasesSection.class)
                .get()
                .getChildren()
                .get(0)
                .getChildren();
        assertThat(calls).hasSize(2);
        assertThat(calls.get(0).getName()).isEqualTo("Log");
        assertThat(calls.get(0).getArguments()).containsExactly("t");
        assertThat(calls.get(0).getComment()).isEmpty();
        assertThat(calls.get(1).getName()).isEmpty();
        assertThat(calls.get(1).getArguments()).isEmpty();
        assertThat(calls.get(1).getComment()).isEqualTo("# *** Settings ***");
    }

    private static void assertArguments(final List<RobotKeywordCall> calls) {
        assertThat(calls.get(0).getArguments()).isEmpty();
        assertThat(calls.get(0).getArguments()).isEmpty();
        assertThat(calls.get(2).getArguments()).containsExactly("1");
        assertThat(calls.get(3).getArguments()).containsExactly("1");
        assertThat(calls.get(4).getArguments()).containsExactly("1", "2");
        assertThat(calls.get(5).getArguments()).containsExactly("1", "2");
        assertThat(calls.get(6).getArguments()).containsExactly("kw6", "1", "2");
        assertThat(calls.get(7).getArguments()).containsExactly("kw7", "1", "2");
        assertThat(calls.get(8).getArguments()).containsExactly("kw8", "1", "2");
        assertThat(calls.get(9).getArguments()).containsExactly("kw9", "1", "2");
        assertThat(calls.get(10).getArguments()).containsExactly("${y}", "kw10", "1", "2");
        assertThat(calls.get(11).getArguments()).containsExactly("${y}", "kw11", "1", "2");
        assertThat(calls.get(12).getArguments()).containsExactly("${y}=", "kw12", "1", "2");
        assertThat(calls.get(13).getArguments()).containsExactly("${y}=", "kw13", "1", "2");
    }

    private static void assertName(final List<RobotKeywordCall> calls) {
        assertThat(calls.get(0).getName()).isEqualTo("kw0");
        assertThat(calls.get(1).getName()).isEqualTo("kw1");
        assertThat(calls.get(2).getName()).isEqualTo("kw2");
        assertThat(calls.get(3).getName()).isEqualTo("kw3");
        assertThat(calls.get(4).getName()).isEqualTo("kw4");
        assertThat(calls.get(5).getName()).isEqualTo("kw5");
        assertThat(calls.get(6).getName()).isEqualTo("${x}");
        assertThat(calls.get(7).getName()).isEqualTo("${x}");
        assertThat(calls.get(8).getName()).isEqualTo("${x}=");
        assertThat(calls.get(9).getName()).isEqualTo("${x}=");
        assertThat(calls.get(10).getName()).isEqualTo("${x}");
        assertThat(calls.get(11).getName()).isEqualTo("${x}");
        assertThat(calls.get(12).getName()).isEqualTo("${x}");
        assertThat(calls.get(13).getName()).isEqualTo("${x}");
    }

    private static void assertLabel(final List<RobotKeywordCall> calls) {
        int i = 0;
        for (final RobotKeywordCall call : calls) {
            assertThat(call.getLabel()).isEqualTo("kw" + i);
            i++;
        }
    }

    @Test
    public void copyBySerializationTest() {
        for (final RobotKeywordCall call : createCallsFromCaseForTest()) {
            assertThat(call).has(RobotKeywordCallConditions.properlySetParent()).has(filePositions());

            final RobotKeywordCall callCopy = ModelElementsSerDe.copy(call);

            assertThat(callCopy).isNotSameAs(call).has(nullParent()).has(noFilePositions());

            assertThat(callCopy.getName()).isEqualTo(call.getName());
            assertThat(callCopy.getArguments()).isEqualTo(call.getArguments());
            assertThat(callCopy.getComment()).isEqualTo(call.getComment());
        }
    }

    private static List<RobotKeywordCall> createCallsFromCaseForTest() {
        final RobotSuiteFile model = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("case1")
                .appendLine("  kw0")
                .appendLine("  kw1  # comment")
                .appendLine("  kw2  1")
                .appendLine("  kw3  1  # comment    rest")
                .appendLine("  kw4  1  2")
                .appendLine("  kw5  1  2  # comment    rest")
                .appendLine("  ${x}  kw6  1  2")
                .appendLine("  ${x}  kw7  1  2  # comment    rest")
                .appendLine("  ${x}=  kw8  1  2")
                .appendLine("  ${x}=  kw9  1  2  # comment    rest")
                .appendLine("  ${x}  ${y}  kw10  1  2")
                .appendLine("  ${x}  ${y}  kw11  1  2  # comment    rest")
                .appendLine("  ${x}  ${y}=  kw12  1  2")
                .appendLine("  ${x}  ${y}=  kw13  1  2  # comment    rest")
                .build();
        final RobotCasesSection section = model.findSection(RobotCasesSection.class).get();
        return section.getChildren().get(0).getChildren();
    }

    private static List<RobotKeywordCall> createCallsFromKeywordForTest() {
        final RobotSuiteFile model = new RobotSuiteFileCreator().appendLine("*** Keywords ***")
                .appendLine("kw1")
                .appendLine("  kw0")
                .appendLine("  kw1  # comment")
                .appendLine("  kw2  1")
                .appendLine("  kw3  1  # comment  rest")
                .appendLine("  kw4  1  2")
                .appendLine("  kw5  1  2  # comment  rest")
                .appendLine("  ${x}  kw6  1  2")
                .appendLine("  ${x}  kw7  1  2  # comment  rest")
                .appendLine("  ${x}=  kw8  1  2")
                .appendLine("  ${x}=  kw9  1  2  # comment  rest")
                .appendLine("  ${x}  ${y}  kw10  1  2")
                .appendLine("  ${x}  ${y}  kw11  1  2  # comment  rest")
                .appendLine("  ${x}  ${y}=  kw12  1  2")
                .appendLine("  ${x}  ${y}=  kw13  1  2  # comment  rest")
                .build();
        final RobotKeywordsSection section = model.findSection(RobotKeywordsSection.class).get();
        return section.getChildren().get(0).getChildren();
    }
}
