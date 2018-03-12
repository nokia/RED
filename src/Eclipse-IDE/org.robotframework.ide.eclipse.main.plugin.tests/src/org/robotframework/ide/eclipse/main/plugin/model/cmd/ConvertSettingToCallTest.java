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
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.table.keywords.KeywordTeardown;
import org.rf.ide.core.testdata.model.table.testcases.TestCaseTeardown;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;
import org.robotframework.ide.eclipse.main.plugin.mockeclipse.ContextInjector;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.RobotSuiteFileCreator;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCasesSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotDefinitionSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;

import com.google.common.collect.ImmutableMap;

public class ConvertSettingToCallTest {

    private IEventBroker eventBroker;

    @Before
    public void beforeTest() {
        eventBroker = mock(IEventBroker.class);
    }

    @Test
    public void testCaseSettingIsProperlyChangedToCall() {
        final RobotSuiteFile model = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("case")
                .appendLine("  [Teardown]  Log  1  #comment")
                .build();
        final RobotCase testCase = model.findSection(RobotCasesSection.class).get().getChildren().get(0);
        final RobotDefinitionSetting setting = testCase.findSetting(ModelType.TEST_CASE_TEARDOWN).get();
        final TestCaseTeardown oldLinked = (TestCaseTeardown) setting.getLinkedElement();

        ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new ConvertSettingToCall(eventBroker, setting, "call"))
                .execute();

        assertThat(testCase.getChildren().size()).isEqualTo(1);
        final RobotKeywordCall result = testCase.getChildren().get(0);
        assertThat(result).isExactlyInstanceOf(RobotKeywordCall.class);
        assertThat(result.getLinkedElement().getDeclaration().getTypes())
                .containsExactly(RobotTokenType.TEST_CASE_ACTION_NAME);
        assertThat(result.getName()).isEqualTo("call");
        assertThat(result.getArguments()).contains("Log", "1");
        assertThat(result.getComment()).isEqualTo("#comment");
        assertThat(result).has(properlySetParent());
        assertThat(testCase.getLinkedElement().getTeardowns()).doesNotContain(oldLinked);
        assertThat(testCase.getLinkedElement().getExecutionContext().size()).isEqualTo(1);

        verify(eventBroker, times(1)).send(eq(RobotModelEvents.ROBOT_KEYWORD_CALL_CONVERTED), eq(ImmutableMap
                .of(IEventBroker.DATA, testCase, RobotModelEvents.ADDITIONAL_DATA, result)));
        verifyNoMoreInteractions(eventBroker);
    }

    @Test
    public void keywordSettingIsProperlyChangedToCall() {
        final RobotSuiteFile model = new RobotSuiteFileCreator().appendLine("*** Keywords ***")
                .appendLine("kw")
                .appendLine("  [Teardown]  Log  1  #comment")
                .build();
        final RobotKeywordDefinition keyword = model.findSection(RobotKeywordsSection.class).get().getChildren().get(0);
        final RobotDefinitionSetting keywordCall = (RobotDefinitionSetting) keyword.getChildren().get(0);
        final KeywordTeardown oldLinked = (KeywordTeardown) keywordCall.getLinkedElement();

        ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new ConvertSettingToCall(eventBroker, keywordCall, "call"))
                .execute();

        assertThat(keyword.getChildren().size()).isEqualTo(1);
        final RobotKeywordCall result = keyword.getChildren().get(0);
        assertThat(result).isExactlyInstanceOf(RobotKeywordCall.class);
        assertThat(result.getLinkedElement().getDeclaration().getTypes())
                .containsExactly(RobotTokenType.KEYWORD_ACTION_NAME);
        assertThat(result.getName()).isEqualTo("call");
        assertThat(result.getArguments()).contains("Log", "1");
        assertThat(result.getComment()).isEqualTo("#comment");
        assertThat(result).has(properlySetParent());
        assertThat(keyword.getLinkedElement().getTeardowns()).doesNotContain(oldLinked);
        assertThat(keyword.getLinkedElement().getExecutionContext().size()).isEqualTo(1);

        verify(eventBroker, times(1)).send(eq(RobotModelEvents.ROBOT_KEYWORD_CALL_CONVERTED),
                eq(ImmutableMap
                .of(IEventBroker.DATA, keyword, RobotModelEvents.ADDITIONAL_DATA, result)));
        verifyNoMoreInteractions(eventBroker);
    }
}
