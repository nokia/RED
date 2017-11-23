/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.RobotSuiteFileCreator;

public class RobotDefinitionSettingTest {

    @Test
    public void definitionSettingShouldBeCommented_whenNotCommented_andViceVersa() {
        final RobotSuiteFile model = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("t1")
                .appendLine("  [Documentation]")
                .build();
        final List<RobotDefinitionSetting> settings = model.findSection(RobotCasesSection.class)
                .get()
                .getChildren()
                .get(0)
                .getChildren()
                .stream()
                .filter(RobotDefinitionSetting.class::isInstance)
                .map(RobotDefinitionSetting.class::cast)
                .collect(Collectors.toList());
        assertThat(settings).hasSize(1);
        assertThat(settings.get(0).getName()).isEqualTo("Documentation");
        assertThat(settings.get(0).getComment()).isEmpty();
        assertThat(settings.get(0).shouldAddCommentMark()).isTrue();
    }

    @Test
    public void cellInserted_atFirstPosition() {
        final RobotSuiteFile model = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("t1")
                .appendLine("  [Template]  arg1  arg2  arg3  #comment1  comment2")
                .build();
        final RobotDefinitionSetting settingBefore = model.findSection(RobotCasesSection.class)
                .get()
                .getChildren()
                .get(0)
                .getChildren()
                .stream()
                .filter(RobotDefinitionSetting.class::isInstance)
                .map(RobotDefinitionSetting.class::cast)
                .collect(Collectors.toList()).get(0);
        final List<String> tokensBefore = settingBefore.getLinkedElement().getElementTokens().stream()
                .map(rt -> rt.getText()).collect(Collectors.toList());
        assertThat(tokensBefore).containsExactly("[Template]", "arg1", "arg2", "arg3", "#comment1", "comment2");

        settingBefore.insertCellAt(0, "");

        final RobotKeywordCall settingAfter = model.findSection(RobotCasesSection.class)
                .get()
                .getChildren()
                .get(0)
                .getChildren()
                .get(0);
        final List<String> tokensAfter = settingAfter.getLinkedElement().getElementTokens().stream()
                .map(rt -> rt.getText()).collect(Collectors.toList());
        assertThat(tokensAfter).containsExactly("", "[Template]", "arg1", "arg2", "arg3", "#comment1", "comment2");
    }

    @Test
    public void cellInserted_atFirstArgumentPosition() {
        final RobotSuiteFile model = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("t1")
                .appendLine("  [Template]  arg1  arg2  arg3  #comment1  comment2")
                .build();
        final RobotDefinitionSetting settingBefore = model.findSection(RobotCasesSection.class)
                .get()
                .getChildren()
                .get(0)
                .getChildren()
                .stream()
                .filter(RobotDefinitionSetting.class::isInstance)
                .map(RobotDefinitionSetting.class::cast)
                .collect(Collectors.toList()).get(0);
        final List<String> tokensBefore = settingBefore.getLinkedElement().getElementTokens().stream()
                .map(rt -> rt.getText()).collect(Collectors.toList());
        assertThat(tokensBefore).containsExactly("[Template]", "arg1", "arg2", "arg3", "#comment1", "comment2");

        settingBefore.insertCellAt(1, "");

        final RobotKeywordCall settingAfter = model.findSection(RobotCasesSection.class)
                .get()
                .getChildren()
                .get(0)
                .getChildren()
                .get(0);
        final List<String> tokensAfter = settingAfter.getLinkedElement().getElementTokens().stream()
                .map(rt -> rt.getText()).collect(Collectors.toList());
        assertThat(tokensAfter).containsExactly("[Template]", "", "arg1", "arg2", "arg3", "#comment1", "comment2");
    }

    @Test
    public void cellInserted_atMiddleArgumentPosition() {
        final RobotSuiteFile model = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("t1")
                .appendLine("  [Template]  arg1  arg2  arg3  #comment1  comment2")
                .build();
        final RobotDefinitionSetting settingBefore = model.findSection(RobotCasesSection.class)
                .get()
                .getChildren()
                .get(0)
                .getChildren()
                .stream()
                .filter(RobotDefinitionSetting.class::isInstance)
                .map(RobotDefinitionSetting.class::cast)
                .collect(Collectors.toList()).get(0);
        final List<String> tokensBefore = settingBefore.getLinkedElement().getElementTokens().stream()
                .map(rt -> rt.getText()).collect(Collectors.toList());
        assertThat(tokensBefore).containsExactly("[Template]", "arg1", "arg2", "arg3", "#comment1", "comment2");

        settingBefore.insertCellAt(2, "");

        final RobotKeywordCall settingAfter = model.findSection(RobotCasesSection.class)
                .get()
                .getChildren()
                .get(0)
                .getChildren()
                .get(0);
        final List<String> tokensAfter = settingAfter.getLinkedElement().getElementTokens().stream()
                .map(rt -> rt.getText()).collect(Collectors.toList());
        assertThat(tokensAfter).containsExactly("[Template]", "arg1", "", "arg2", "arg3", "#comment1", "comment2");
    }

    @Test
    public void cellInserted_atLastArgumentPosition() {
        final RobotSuiteFile model = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("t1")
                .appendLine("  [Template]  arg1  arg2  arg3  #comment1  comment2")
                .build();
        final RobotDefinitionSetting settingBefore = model.findSection(RobotCasesSection.class)
                .get()
                .getChildren()
                .get(0)
                .getChildren()
                .stream()
                .filter(RobotDefinitionSetting.class::isInstance)
                .map(RobotDefinitionSetting.class::cast)
                .collect(Collectors.toList()).get(0);
        final List<String> tokensBefore = settingBefore.getLinkedElement().getElementTokens().stream()
                .map(rt -> rt.getText()).collect(Collectors.toList());
        assertThat(tokensBefore).containsExactly("[Template]", "arg1", "arg2", "arg3", "#comment1", "comment2");

        settingBefore.insertCellAt(3, "");

        final RobotKeywordCall settingAfter = model.findSection(RobotCasesSection.class)
                .get()
                .getChildren()
                .get(0)
                .getChildren()
                .get(0);
        final List<String> tokensAfter = settingAfter.getLinkedElement().getElementTokens().stream()
                .map(rt -> rt.getText()).collect(Collectors.toList());
        assertThat(tokensAfter).containsExactly("[Template]", "arg1", "arg2", "", "arg3", "#comment1", "comment2");
    }

    @Test
    public void cellInserted_atFirstCommentPosition() {
        final RobotSuiteFile model = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("t1")
                .appendLine("  [Template]  arg1  arg2  arg3  #comment1  comment2")
                .build();
        final RobotDefinitionSetting settingBefore = model.findSection(RobotCasesSection.class)
                .get()
                .getChildren()
                .get(0)
                .getChildren()
                .stream()
                .filter(RobotDefinitionSetting.class::isInstance)
                .map(RobotDefinitionSetting.class::cast)
                .collect(Collectors.toList()).get(0);
        final List<String> tokensBefore = settingBefore.getLinkedElement().getElementTokens().stream()
                .map(rt -> rt.getText()).collect(Collectors.toList());
        assertThat(tokensBefore).containsExactly("[Template]", "arg1", "arg2", "arg3", "#comment1", "comment2");

        settingBefore.insertCellAt(4, "");

        final RobotKeywordCall settingAfter = model.findSection(RobotCasesSection.class)
                .get()
                .getChildren()
                .get(0)
                .getChildren()
                .get(0);
        final List<String> tokensAfter = settingAfter.getLinkedElement().getElementTokens().stream()
                .map(rt -> rt.getText()).collect(Collectors.toList());
        assertThat(tokensAfter).containsExactly("[Template]", "arg1", "arg2", "arg3", "", "#comment1", "comment2");
    }

    @Test
    public void cellInserted_atLastCommentPosition() {
        final RobotSuiteFile model = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("t1")
                .appendLine("  [Template]  arg1  arg2  arg3  #comment1  comment2")
                .build();
        final RobotDefinitionSetting settingBefore = model.findSection(RobotCasesSection.class)
                .get()
                .getChildren()
                .get(0)
                .getChildren()
                .stream()
                .filter(RobotDefinitionSetting.class::isInstance)
                .map(RobotDefinitionSetting.class::cast)
                .collect(Collectors.toList()).get(0);
        final List<String> tokensBefore = settingBefore.getLinkedElement().getElementTokens().stream()
                .map(rt -> rt.getText()).collect(Collectors.toList());
        assertThat(tokensBefore).containsExactly("[Template]", "arg1", "arg2", "arg3", "#comment1", "comment2");

        settingBefore.insertCellAt(5, "");

        final RobotKeywordCall settingAfter = model.findSection(RobotCasesSection.class)
                .get()
                .getChildren()
                .get(0)
                .getChildren()
                .get(0);
        final List<String> tokensAfter = settingAfter.getLinkedElement().getElementTokens().stream()
                .map(rt -> rt.getText()).collect(Collectors.toList());
        assertThat(tokensAfter).containsExactly("[Template]", "arg1", "arg2", "arg3", "#comment1", "", "comment2");
    }

}
