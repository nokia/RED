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
import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.model.table.keywords.UserKeyword;
import org.rf.ide.core.testdata.model.table.testcases.TestCase;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;
import org.robotframework.ide.eclipse.main.plugin.mockeclipse.ContextInjector;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.RobotSuiteFileCreator;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCasesSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;

import com.google.common.collect.ImmutableMap;

public class ReplaceRobotKeywordCallCommandTest {

    private IEventBroker eventBroker;

    @Before
    public void beforeTest() {
        eventBroker = mock(IEventBroker.class);
    }

    @Test
    public void robotCallProperlyReplaced_forTestCases() {
        final RobotSuiteFile model = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("case")
                .appendLine("  call  1  2  #comment")
                .build();
        final RobotCase testCase = model.findSection(RobotCasesSection.class).get().getChildren().get(0);
        final RobotKeywordCall keywordCall = testCase.getChildren().get(0);
        final RobotSuiteFile anotherModel = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("case")
                .appendLine("  call2  3  4  #comment2")
                .build();
        final RobotCase anotherTestCase = anotherModel.findSection(RobotCasesSection.class).get().getChildren().get(0);
        final RobotKeywordCall anotherKeywordCall = anotherTestCase.getChildren().get(0);

        @SuppressWarnings("unchecked")
        final RobotExecutableRow<TestCase> oldLinked = (RobotExecutableRow<TestCase>) keywordCall.getLinkedElement();

        ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new ReplaceRobotKeywordCallCommand(eventBroker, keywordCall, anotherKeywordCall))
                .execute();

        assertThat(testCase.getChildren().size()).isEqualTo(1);
        final RobotKeywordCall result = testCase.getChildren().get(0);
        assertThat(result).isExactlyInstanceOf(RobotKeywordCall.class);
        assertThat(result).isEqualTo(anotherKeywordCall);
        assertThat(result.getLinkedElement().getDeclaration().getTypes())
                .containsExactly(RobotTokenType.TEST_CASE_ACTION_NAME);
        assertThat(result).has(properlySetParent());
        assertThat(testCase.getLinkedElement().getElements()).doesNotContain(oldLinked);
        assertThat(testCase.getLinkedElement().getElements().size()).isEqualTo(1);

        assertThat(anotherTestCase.getChildren().size()).isEqualTo(1);
        assertThat(anotherTestCase.getChildren()).containsExactly(anotherKeywordCall);

        verify(eventBroker, times(1)).send(eq(RobotModelEvents.ROBOT_KEYWORD_CALL_CONVERTED), eq(ImmutableMap
                .of(IEventBroker.DATA, testCase, RobotModelEvents.ADDITIONAL_DATA, result)));
        verifyNoMoreInteractions(eventBroker);
    }

    @Test
    public void robotCallProperlyReplaced_forKeywords() {
        final RobotSuiteFile model = new RobotSuiteFileCreator().appendLine("*** Keywords ***")
                .appendLine("kw1")
                .appendLine("  call  1  2  #comment")
                .build();
        final RobotKeywordDefinition kwDef = model.findSection(RobotKeywordsSection.class).get().getChildren().get(0);
        final RobotKeywordCall keywordCall = kwDef.getChildren().get(0);
        final RobotSuiteFile anotherModel = new RobotSuiteFileCreator().appendLine("*** Keywords ***")
                .appendLine("kw1")
                .appendLine("  call2  3  4  #comment2")
                .build();
        final RobotKeywordDefinition anotherKwDef = anotherModel.findSection(RobotKeywordsSection.class)
                .get()
                .getChildren()
                .get(0);
        final RobotKeywordCall anotherKeywordCall = anotherKwDef.getChildren().get(0);

        @SuppressWarnings("unchecked")
        final RobotExecutableRow<UserKeyword> oldLinked = (RobotExecutableRow<UserKeyword>) keywordCall
                .getLinkedElement();

        ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new ReplaceRobotKeywordCallCommand(eventBroker, keywordCall, anotherKeywordCall))
                .execute();

        assertThat(kwDef.getChildren().size()).isEqualTo(1);
        final RobotKeywordCall result = kwDef.getChildren().get(0);
        assertThat(result).isExactlyInstanceOf(RobotKeywordCall.class);
        assertThat(result).isEqualTo(anotherKeywordCall);
        assertThat(result.getLinkedElement().getDeclaration().getTypes())
                .containsExactly(RobotTokenType.KEYWORD_ACTION_NAME);
        assertThat(result).has(properlySetParent());
        assertThat(kwDef.getLinkedElement().getElements()).doesNotContain(oldLinked);
        assertThat(kwDef.getLinkedElement().getElements().size()).isEqualTo(1);

        assertThat(anotherKwDef.getChildren().size()).isEqualTo(1);
        assertThat(anotherKwDef.getChildren()).containsExactly(anotherKeywordCall);

        verify(eventBroker, times(1)).send(eq(RobotModelEvents.ROBOT_KEYWORD_CALL_CONVERTED), eq(
                ImmutableMap.of(IEventBroker.DATA, kwDef, RobotModelEvents.ADDITIONAL_DATA, result)));
        verifyNoMoreInteractions(eventBroker);
    }
}
