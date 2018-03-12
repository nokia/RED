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

public class RobotKeywordDefinitionTest {

    @Test
    public void copyBySerializationTest() {
        for (final RobotKeywordDefinition keyword : createKeywordsForTest()) {
            
            assertThat(keyword).has(RobotKeywordDefinitionConditions.properlySetParent()).has(filePositions());
            for (final RobotKeywordCall call : keyword.getChildren()) {
                assertThat(call).has(RobotKeywordCallConditions.properlySetParent()).has(filePositions());
            }

            final RobotKeywordDefinition keywordCopy = ModelElementsSerDe.copy(keyword);

            assertThat(keywordCopy).isNotSameAs(keyword).has(nullParent()).has(noFilePositions());
            assertThat(keywordCopy.getChildren().size()).isEqualTo(keyword.getChildren().size());
            for (int i = 0; i < keywordCopy.getChildren().size(); i++) {
                final RobotKeywordCall call = keyword.getChildren().get(i);
                final RobotKeywordCall callCopy = keywordCopy.getChildren().get(i);

                assertThat(callCopy).isNotSameAs(call)
                        .has(RobotKeywordCallConditions.properlySetParent())
                        .has(noFilePositions());

                assertThat(callCopy.getName()).isEqualTo(call.getName());
                assertThat(callCopy.getArguments()).containsExactlyElementsOf(call.getArguments());
                assertThat(callCopy.getComment()).isEqualTo(call.getComment());
            }

            assertThat(keywordCopy.getName()).isEqualTo(keyword.getName());
            assertThat(keywordCopy.getComment()).isEqualTo(keyword.getComment());
        }
    }

    private static List<RobotKeywordDefinition> createKeywordsForTest() {
        final RobotSuiteFile model = new RobotSuiteFileCreator().appendLine("*** Keywords ***")
                .appendLine("kw1")
                .appendLine("  kw  1  2  3")
                .appendLine("  ${x}=  fib  5")
                .appendLine("  kw  1  # c")
                .appendLine("  [Documentation]  # c  d")
                .appendLine("  [Tags]  a  b  # c  d")
                .appendLine("  [Teardown]  a  b  # c  d")
                .appendLine("  [Timeout]  a  b  # c  d")
                .appendLine("  [Arguments]  a  b  # c  d")
                .appendLine("  [Return]  a  b  # c  d")
                .appendLine("  [unknown]  a  b  # c  d")
                .appendLine("kw2")
                .appendLine("kw3")
                .appendLine("  abc")
                .build();
        final RobotKeywordsSection kwSection = model.findSection(RobotKeywordsSection.class).get();
        return kwSection.getChildren();
    }
}
