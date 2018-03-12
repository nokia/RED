/*
* Copyright 2017 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.robotframework.ide.eclipse.main.plugin.model.cmd;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCallConditions.properlySetParent;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.junit.Before;
import org.junit.Test;
import org.rf.ide.core.testdata.model.table.RobotEmptyRow;
import org.rf.ide.core.testdata.model.table.keywords.UserKeyword;
import org.rf.ide.core.testdata.model.table.testcases.TestCase;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;
import org.robotframework.ide.eclipse.main.plugin.mockeclipse.ContextInjector;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.RobotSuiteFileCreator;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCasesSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotDefinitionSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotEmptyLine;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;

import com.google.common.collect.ImmutableMap;

public class ConvertEmptyToSettingTest {

    private IEventBroker eventBroker;

    @Before
    public void beforeTest() {
        eventBroker = mock(IEventBroker.class);
    }

    @Test
    public void testCaseExecutableRowIsProperlyChangedToSetting() {
        final RobotSuiteFile model = new RobotSuiteFileCreator()
                .appendLine("*** Test Cases ***")
                .appendLine("case")
                .appendLine("")
                .appendLine("  call  1  2  #comment")
                .build();
        final RobotCase testCase = model.findSection(RobotCasesSection.class).get().getChildren().get(0);
        final RobotEmptyLine keywordCall = (RobotEmptyLine)testCase.getChildren().get(0);
        @SuppressWarnings("unchecked")
        final RobotEmptyRow<TestCase> oldLinked = (RobotEmptyRow<TestCase>) keywordCall.getLinkedElement();

        ContextInjector.prepareContext().inWhich(eventBroker)
                .isInjectedInto(new ConvertEmptyToSetting(eventBroker, keywordCall, "[Tags]")).execute();

        assertThat(testCase.getChildren().size()).isEqualTo(2);
        final RobotKeywordCall result = testCase.getChildren().get(0);
        assertThat(result).isExactlyInstanceOf(RobotDefinitionSetting.class);
        assertThat(result.getLinkedElement().getDeclaration().getTypes())
                .contains(RobotTokenType.TEST_CASE_SETTING_TAGS_DECLARATION);
        assertThat(result.getName()).isEqualTo("Tags");
        assertThat(result.getArguments()).isEmpty();
        assertThat(result.getComment()).isEmpty();
        assertThat(result).has(properlySetParent());
        assertThat(testCase.getLinkedElement().getElements()).doesNotContain(oldLinked);
        assertThat(testCase.getLinkedElement().getElements().size()).isEqualTo(2);

        verify(eventBroker, times(1)).send(eq(RobotModelEvents.ROBOT_KEYWORD_CALL_CONVERTED),
                eq(ImmutableMap.of(IEventBroker.DATA, testCase, RobotModelEvents.ADDITIONAL_DATA, result)));
        verifyNoMoreInteractions(eventBroker);
    }

    @Test
    public void keywordExecutableRowIsProperlyChangedToSetting() {
        final RobotSuiteFile model = new RobotSuiteFileCreator()
                .appendLine("*** Keywords ***")
                .appendLine("kw")
                .appendLine("")
                .appendLine("  call  1  2  #comment")
                .build();
        final RobotKeywordDefinition keyword = model.findSection(RobotKeywordsSection.class).get().getChildren().get(0);
        final RobotEmptyLine keywordCall = (RobotEmptyLine) keyword.getChildren().get(0);
        @SuppressWarnings("unchecked")
        final RobotEmptyRow<UserKeyword> oldLinked = (RobotEmptyRow<UserKeyword>) keywordCall
                .getLinkedElement();

        ContextInjector.prepareContext().inWhich(eventBroker)
                .isInjectedInto(new ConvertEmptyToSetting(eventBroker, keywordCall, "[Tags]")).execute();

        assertThat(keyword.getChildren().size()).isEqualTo(2);
        final RobotKeywordCall result = keyword.getChildren().get(0);
        assertThat(result).isExactlyInstanceOf(RobotDefinitionSetting.class);
        assertThat(result.getLinkedElement().getDeclaration().getTypes())
                .contains(RobotTokenType.KEYWORD_SETTING_TAGS);
        assertThat(result.getName()).isEqualTo("Tags");
        assertThat(result.getArguments()).isEmpty();
        assertThat(result.getComment()).isEmpty();
        assertThat(result).has(properlySetParent());
        assertThat(keyword.getLinkedElement().getElements()).doesNotContain(oldLinked);
        assertThat(keyword.getLinkedElement().getElements().size()).isEqualTo(2);

        verify(eventBroker, times(1)).send(eq(RobotModelEvents.ROBOT_KEYWORD_CALL_CONVERTED),
                eq(ImmutableMap.of(IEventBroker.DATA, keyword, RobotModelEvents.ADDITIONAL_DATA, result)));
        verifyNoMoreInteractions(eventBroker);
    }

    @Test
    public void testCaseExecutableRowIsProperlyChangedToSetting_whenThereAreMoreCalls() {
        final RobotSuiteFile model = new RobotSuiteFileCreator()
                .appendLine("*** Test Cases ***")
                .appendLine("case")
                .appendLine("  Log  nothing")
                .appendLine("")
                .appendLine("  Log  something")
                .build();
        final RobotCase testCase = model.findSection(RobotCasesSection.class).get().getChildren().get(0);
        final RobotEmptyLine keywordCall = (RobotEmptyLine) testCase.getChildren().get(1);
        @SuppressWarnings("unchecked")
        final RobotEmptyRow<TestCase> oldLinked = (RobotEmptyRow<TestCase>) keywordCall.getLinkedElement();

        ContextInjector.prepareContext().inWhich(eventBroker)
                .isInjectedInto(new ConvertEmptyToSetting(eventBroker, keywordCall, "[Tags]")).execute();

        assertThat(testCase.getChildren().size()).isEqualTo(3);
        final RobotKeywordCall result = testCase.getChildren().get(1);
        assertThat(result).isExactlyInstanceOf(RobotDefinitionSetting.class);
        assertThat(result.getLinkedElement().getDeclaration().getTypes())
                .contains(RobotTokenType.TEST_CASE_SETTING_TAGS_DECLARATION);
        assertThat(result.getName()).isEqualTo("Tags");
        assertThat(result.getArguments()).isEmpty();
        assertThat(result.getComment()).isEmpty();
        assertThat(result).has(properlySetParent());
        assertThat(testCase.getLinkedElement().getElements()).doesNotContain(oldLinked);
        assertThat(testCase.getLinkedElement().getElements().size()).isEqualTo(3);

        verify(eventBroker, times(1)).send(eq(RobotModelEvents.ROBOT_KEYWORD_CALL_CONVERTED),
                eq(ImmutableMap.of(IEventBroker.DATA, testCase, RobotModelEvents.ADDITIONAL_DATA, result)));
        verifyNoMoreInteractions(eventBroker);
    }

    @Test
    public void keywordExecutableRowIsProperlyChangedToSetting_whenThereAreMoreCalls() {
        final RobotSuiteFile model = new RobotSuiteFileCreator()
                .appendLine("*** Keywords ***")
                .appendLine("kw")
                .appendLine("  Log nothing")
                .appendLine("")
                .appendLine("  Log  something")
                .build();
        final RobotKeywordDefinition keyword = model.findSection(RobotKeywordsSection.class).get().getChildren().get(0);
        final RobotEmptyLine keywordCall = (RobotEmptyLine) keyword.getChildren().get(1);
        @SuppressWarnings("unchecked")
        final RobotEmptyRow<UserKeyword> oldLinked = (RobotEmptyRow<UserKeyword>) keywordCall
                .getLinkedElement();

        ContextInjector.prepareContext().inWhich(eventBroker)
                .isInjectedInto(new ConvertEmptyToSetting(eventBroker, keywordCall, "[Tags]")).execute();

        assertThat(keyword.getChildren().size()).isEqualTo(3);
        final RobotKeywordCall result = keyword.getChildren().get(1);
        assertThat(result).isExactlyInstanceOf(RobotDefinitionSetting.class);
        assertThat(result.getLinkedElement().getDeclaration().getTypes())
                .contains(RobotTokenType.KEYWORD_SETTING_TAGS);
        assertThat(result.getName()).isEqualTo("Tags");
        assertThat(result.getArguments()).isEmpty();
        assertThat(result.getComment()).isEmpty();
        assertThat(result).has(properlySetParent());
        assertThat(keyword.getLinkedElement().getElements()).doesNotContain(oldLinked);

        verify(eventBroker, times(1)).send(eq(RobotModelEvents.ROBOT_KEYWORD_CALL_CONVERTED),
                eq(ImmutableMap.of(IEventBroker.DATA, keyword, RobotModelEvents.ADDITIONAL_DATA, result)));
        verifyNoMoreInteractions(eventBroker);
    }
}
