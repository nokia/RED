/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor;

import static org.assertj.core.api.Assertions.assertThat;

import org.eclipse.jface.viewers.TreeViewer;
import org.junit.Rule;
import org.junit.Test;
import org.rf.ide.core.environment.RobotVersion;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.RobotSuiteFileCreator;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCasesSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSettingsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariable;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariablesSection;
import org.robotframework.ide.eclipse.main.plugin.navigator.ArtificialGroupingRobotElement;
import org.robotframework.red.junit.ShellProvider;

public class RobotOutlineContentProviderTest {

    @Rule
    public ShellProvider shellProvider = new ShellProvider();

    @Test
    public void sectionsAreDisplayed() {
        final RobotOutlineContentProvider provider = new RobotOutlineContentProvider();
        final RobotSuiteFile testSuite = new RobotSuiteFileCreator()
                .appendLine("*** Test Cases ***")
                .appendLine("case")
                .appendLine("*** Keywords ***")
                .appendLine("kw")
                .appendLine("*** Settings ***")
                .appendLine("Documentation  a")
                .appendLine("*** Variables ***")
                .appendLine("${a}  1")
                .appendLine("*** Comments ***")
                .appendLine("abc")
                .build();

        setInput(provider, testSuite);

        assertThat(provider.getChildren(testSuite)).hasSize(4)
                .hasOnlyElementsOfTypes(RobotCasesSection.class, RobotKeywordsSection.class, RobotSettingsSection.class,
                        RobotVariablesSection.class);
    }

    @Test
    public void settingsSectionIsDisplayed() {
        final RobotOutlineContentProvider provider = new RobotOutlineContentProvider();
        final RobotSuiteFile robotFile = new RobotSuiteFileCreator()
                .appendLine("*** Settings ***")
                .appendLine("# setups & teardowns")
                .appendLine("Suite Setup  Log  enter")
                .appendLine("Suite Teardown  Log  exit")
                .appendLine("Test Setup  Log  test enter")
                .appendLine("Test Teardown  Log  test exit")
                .appendLine("# tags")
                .appendLine("Default Tags  x  y  z")
                .appendLine("Force Tags  x  y  z")
                .appendLine("# imports")
                .appendLine("Library  Lib")
                .appendLine("Resource  res.robot")
                .appendLine("Variables  vars.py")
                .appendLine("# other")
                .appendLine("Test Template  Kw")
                .appendLine("Test Timeout  1234")
                .appendLine("Metadata  a")
                .appendLine("Documentation  a")
                .appendLine("...  b")
                .appendLine("...  c")
                .build();

        setInput(provider, robotFile);

        assertThat(provider.getChildren(robotFile.getChildren().get(0))).hasSize(13)
                .hasOnlyElementsOfTypes(RobotSetting.class, ArtificialGroupingRobotElement.class);
    }

    @Test
    public void variablesSectionIsDisplayed() {
        final RobotOutlineContentProvider provider = new RobotOutlineContentProvider();
        final RobotSuiteFile robotFile = new RobotSuiteFileCreator()
                .appendLine("*** Variables ***")
                .appendLine("# comment")
                .appendLine("${newScalar}  1")
                .appendLine("@{newList}  a  b")
                .appendLine("&{newDict}  key=value")
                .appendLine("incorrect")
                .build();

        setInput(provider, robotFile);

        assertThat(provider.getChildren(robotFile.getChildren().get(0))).hasSize(4)
                .hasOnlyElementsOfTypes(RobotVariable.class);
    }

    @Test
    public void keywordsSectionIsDisplayed() {
        final RobotOutlineContentProvider provider = new RobotOutlineContentProvider();
        final RobotSuiteFile robotFile = new RobotSuiteFileCreator()
                .appendLine("*** Keywords ***")
                .appendLine("kw 1")
                .appendLine("  [Teardown]  Log  kw exit")
                .appendLine("  Log  keyword")
                .appendLine("")
                .appendLine("  #comment")
                .appendLine("  Log  a")
                .appendLine("  Log  b")
                .appendLine("  [Documentation]  a")
                .appendLine("  ...  b")
                .appendLine("  ...  c")
                .appendLine("kw 2")
                .appendLine("  [Arguments]  ${a}")
                .appendLine("  #comment")
                .appendLine("  [Tags]  a  b  c")
                .appendLine("  [Teardown]  Kw")
                .appendLine("  [Timeout]  123")
                .appendLine("")
                .appendLine("  Log  keyword")
                .appendLine("")
                .appendLine("  [Return]  ${a}")
                .build();

        setInput(provider, robotFile);

        assertThat(provider.getChildren(robotFile.getChildren().get(0))).hasSize(2)
                .hasOnlyElementsOfTypes(RobotKeywordDefinition.class);
        assertThat(provider.getChildren(robotFile.getChildren().get(0).getChildren().get(0))).hasSize(3)
                .hasOnlyElementsOfTypes(RobotKeywordCall.class);
        assertThat(provider.getChildren(robotFile.getChildren().get(0).getChildren().get(1))).hasSize(1)
                .hasOnlyElementsOfTypes(RobotKeywordCall.class);
    }

    @Test
    public void testCasesSectionIsDisplayed() {
        final RobotOutlineContentProvider provider = new RobotOutlineContentProvider();
        final RobotSuiteFile robotFile = new RobotSuiteFileCreator()
                .appendLine("*** Test Cases ***")
                .appendLine("case 1")
                .appendLine("  log  1")
                .appendLine("  log  1")
                .appendLine("")
                .appendLine("  #comment")
                .appendLine("  [Template]  NONE")
                .appendLine("  [Tags]  t1  t2")
                .appendLine("  [Timeout]  123")
                .appendLine("case 2")
                .appendLine("  [Setup]  Log  enter")
                .appendLine("")
                .appendLine("  log  2")
                .appendLine("")
                .appendLine("  [Teardown]  Log  exit")
                .appendLine("case 3")
                .appendLine("  log  3")
                .appendLine("  [Documentation]  a")
                .appendLine("  ...  b")
                .appendLine("  ...  c")
                .appendLine("  log  3")
                .appendLine("  log  3")
                .build();

        setInput(provider, robotFile);

        assertThat(provider.getChildren(robotFile.getChildren().get(0))).hasSize(3)
                .hasOnlyElementsOfTypes(RobotCase.class);
        assertThat(provider.getChildren(robotFile.getChildren().get(0).getChildren().get(0))).hasSize(2)
                .hasOnlyElementsOfTypes(RobotKeywordCall.class);
        assertThat(provider.getChildren(robotFile.getChildren().get(0).getChildren().get(1))).hasSize(1)
                .hasOnlyElementsOfTypes(RobotKeywordCall.class);
        assertThat(provider.getChildren(robotFile.getChildren().get(0).getChildren().get(2))).hasSize(3)
                .hasOnlyElementsOfTypes(RobotKeywordCall.class);
    }

    @Test
    public void forLoopElementsAreNotDisplayed() {
        final RobotOutlineContentProvider provider = new RobotOutlineContentProvider();
        final RobotSuiteFile robotFile = new RobotSuiteFileCreator(RobotVersion.from("3.1"))
                .appendLine("*** Test Cases ***")
                .appendLine("case 1")
                .appendLine("  FOR  ${index}  IN RANGE  123")
                .appendLine("    log  1")
                .appendLine("    log  2")
                .appendLine("    log  3")
                .appendLine("  END")
                .appendLine("case 2")
                .appendLine("  :FOR  ${x}  IN  @{list}")
                .appendLine("  \\  log  1")
                .appendLine("  \\  log  2")
                .appendLine("  \\  log  3")
                .build();

        setInput(provider, robotFile);

        assertThat(provider.getChildren(robotFile.getChildren().get(0))).hasSize(2)
                .hasOnlyElementsOfTypes(RobotCase.class);
        assertThat(provider.getChildren(robotFile.getChildren().get(0).getChildren().get(0))).hasSize(1)
                .hasOnlyElementsOfTypes(RobotKeywordCall.class);
        assertThat(provider.getChildren(robotFile.getChildren().get(0).getChildren().get(1))).hasSize(1)
                .hasOnlyElementsOfTypes(RobotKeywordCall.class);
    }

    @Test
    public void templateArgumentsAreNotDisplayed() {
        final RobotOutlineContentProvider provider = new RobotOutlineContentProvider();
        final RobotSuiteFile robotFile = new RobotSuiteFileCreator(RobotVersion.from("3.1"))
                .appendLine("*** Test Cases ***")
                .appendLine("case 1")
                .appendLine("  [Template]  Some Kw")
                .appendLine("  arg1  arg2")
                .appendLine("  arg3")
                .appendLine("  arg4  arg5  arg6")
                .appendLine("case 2")
                .appendLine("  [Template]  Some Kw")
                .appendLine("  FOR  ${index}  IN RANGE  123")
                .appendLine("    arg1  arg2")
                .appendLine("    arg3")
                .appendLine("    arg4  arg5  arg6")
                .appendLine("  END")
                .build();

        setInput(provider, robotFile);

        assertThat(provider.getChildren(robotFile.getChildren().get(0))).hasSize(2)
                .hasOnlyElementsOfTypes(RobotCase.class);
        assertThat(provider.getChildren(robotFile.getChildren().get(0).getChildren().get(0))).isEmpty();
        assertThat(provider.getChildren(robotFile.getChildren().get(0).getChildren().get(1))).hasSize(1)
                .hasOnlyElementsOfTypes(RobotKeywordCall.class);
    }

    private void setInput(final RobotOutlineContentProvider provider, final RobotSuiteFile suite) {
        final TreeViewer viewer = new TreeViewer(shellProvider.getShell());
        viewer.setContentProvider(provider);
        viewer.setInput(new Object[] { suite });
    }

}
