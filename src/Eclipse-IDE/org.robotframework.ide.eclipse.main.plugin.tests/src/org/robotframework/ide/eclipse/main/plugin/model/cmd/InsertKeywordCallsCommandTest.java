/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCallConditions.properlySetParent;

import java.util.Arrays;
import java.util.List;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.table.LocalSetting;
import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.model.table.keywords.UserKeyword;
import org.rf.ide.core.testdata.model.table.testcases.TestCase;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;
import org.robotframework.ide.eclipse.main.plugin.mockeclipse.ContextInjector;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.RobotSuiteFileCreator;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCasesSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;

import com.google.common.collect.ImmutableMap;

public class InsertKeywordCallsCommandTest {

    private IEventBroker eventBroker;

    @BeforeEach
    public void beforeTest() {
        eventBroker = mock(IEventBroker.class);
    }

    @Test
    public void testCaseExecutableRowIsProperlyInsertedIntoTestCase() {
        final RobotCase testCase = createTestCaseForInsertions();
        final RobotKeywordCall[] callsToInsert = new RobotKeywordCall[] { createTestCaseExecutableRow("call") };

        ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new InsertKeywordCallsCommand(testCase, callsToInsert))
                .execute();

        assertThat(testCase.getChildren().size()).isEqualTo(3);
        assertThat(testCase.getChildren().get(2)).isExactlyInstanceOf(RobotKeywordCall.class);
        assertThat(testCase.getChildren().get(2).getLinkedElement().getDeclaration().getTypes())
                .contains(RobotTokenType.TEST_CASE_ACTION_NAME);
        assertThat(testCase.getChildren().get(2).getName()).isEqualTo("call");
        assertThat(testCase.getChildren().get(2).getArguments()).containsExactly("arg1", "arg2");
        assertThat(testCase.getChildren().get(2).getComment()).isEqualTo("#comment");
        assertThat(testCase.getChildren().get(2)).has(properlySetParent());

        verify(eventBroker, times(1)).send(eq(RobotModelEvents.ROBOT_KEYWORD_CALL_ADDED), eq(ImmutableMap
                .of(IEventBroker.DATA, testCase, RobotModelEvents.ADDITIONAL_DATA, Arrays.asList(callsToInsert))));
        verifyNoMoreInteractions(eventBroker);
    }

    @Test
    public void multipleExecutableRowsAreProperlyInsertedIntoTestCase() {
        final RobotCase testCase = createTestCaseForInsertions();
        final RobotKeywordCall[] callsToInsert = new RobotKeywordCall[] { createTestCaseExecutableRow("call_a"),
                createTestCaseExecutableRow("call_b") };

        ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new InsertKeywordCallsCommand(testCase, 0, callsToInsert))
                .execute();

        assertThat(testCase.getChildren().size()).isEqualTo(4);
        assertThat(testCase.getChildren().get(0)).isExactlyInstanceOf(RobotKeywordCall.class);
        assertThat(testCase.getChildren().get(0).getLinkedElement().getDeclaration().getTypes())
                .contains(RobotTokenType.TEST_CASE_ACTION_NAME);
        assertThat(testCase.getChildren().get(0).getName()).isEqualTo("call_a");
        assertThat(testCase.getChildren().get(0).getArguments()).containsExactly("arg1", "arg2");
        assertThat(testCase.getChildren().get(0).getComment()).isEqualTo("#comment");
        assertThat(testCase.getChildren().get(0)).has(properlySetParent());
        assertThat(testCase.getChildren().get(1)).isExactlyInstanceOf(RobotKeywordCall.class);
        assertThat(testCase.getChildren().get(1).getLinkedElement().getDeclaration().getTypes())
                .contains(RobotTokenType.TEST_CASE_ACTION_NAME);
        assertThat(testCase.getChildren().get(1).getName()).isEqualTo("call_b");
        assertThat(testCase.getChildren().get(1).getArguments()).containsExactly("arg1", "arg2");
        assertThat(testCase.getChildren().get(1).getComment()).isEqualTo("#comment");
        assertThat(testCase.getChildren().get(1)).has(properlySetParent());

        verify(eventBroker, times(1)).send(eq(RobotModelEvents.ROBOT_KEYWORD_CALL_ADDED), eq(ImmutableMap
                .of(IEventBroker.DATA, testCase, RobotModelEvents.ADDITIONAL_DATA, Arrays.asList(callsToInsert))));
        verifyNoMoreInteractions(eventBroker);
    }

    @Test
    public void testCaseSettingIsInsertedToProperPosition_whenInsertingIntoTestCaseBody() {
        final RobotCase testCase = createTestCaseWithSettingsForInsertions();
        final RobotKeywordCall[] callsToInsert = new RobotKeywordCall[] { createTestCaseUnknownSetting("setting") };

        final InsertKeywordCallsCommand command = new InsertKeywordCallsCommand(testCase, 4, callsToInsert);
        command.setEventBroker(eventBroker);
        command.execute();

        assertThat(testCase.getChildren()).extracting(RobotElement::getName)
                .containsExactly("tags", "setup", "teardown", "call1", "setting", "call2", "call3");

        verify(eventBroker, times(1)).send(eq(RobotModelEvents.ROBOT_KEYWORD_CALL_ADDED), eq(ImmutableMap
                .of(IEventBroker.DATA, testCase, RobotModelEvents.ADDITIONAL_DATA, Arrays.asList(callsToInsert))));
        verifyNoMoreInteractions(eventBroker);
    }

    @Test
    public void testCaseExecutableRowIsInsertedToProperPosition_whenInsertingIntoTestCaseSettings() {
        final RobotCase testCase = createTestCaseWithSettingsForInsertions();
        final RobotKeywordCall[] callsToInsert = new RobotKeywordCall[] { createTestCaseExecutableRow("action") };

        final InsertKeywordCallsCommand command = new InsertKeywordCallsCommand(testCase, 1, callsToInsert);
        command.setEventBroker(eventBroker);
        command.execute();

        assertThat(testCase.getChildren()).extracting(RobotElement::getName)
                .containsExactly("tags", "action", "setup", "teardown", "call1", "call2", "call3");

        verify(eventBroker, times(1)).send(eq(RobotModelEvents.ROBOT_KEYWORD_CALL_ADDED), eq(ImmutableMap
                .of(IEventBroker.DATA, testCase, RobotModelEvents.ADDITIONAL_DATA, Arrays.asList(callsToInsert))));
        verifyNoMoreInteractions(eventBroker);
    }

    @Test
    public void keywordExecutableRowIsProperlyInsertedIntoTestCase() {
        final RobotCase testCase = createTestCaseForInsertions();
        final RobotKeywordCall[] callsToInsert = new RobotKeywordCall[] { createKeywordExecutableRow("call") };

        ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new InsertKeywordCallsCommand(testCase, 0, callsToInsert))
                .execute();

        assertThat(testCase.getChildren().size()).isEqualTo(3);
        assertThat(testCase.getChildren().get(0)).isExactlyInstanceOf(RobotKeywordCall.class);
        assertThat(testCase.getChildren().get(0).getLinkedElement().getDeclaration().getTypes())
                .contains(RobotTokenType.TEST_CASE_ACTION_NAME);
        assertThat(testCase.getChildren().get(0).getName()).isEqualTo("call");
        assertThat(testCase.getChildren().get(0).getArguments()).containsExactly("arg1", "arg2");
        assertThat(testCase.getChildren().get(0).getComment()).isEqualTo("#comment");
        assertThat(testCase.getChildren().get(0)).has(properlySetParent());

        verify(eventBroker, times(1)).send(eq(RobotModelEvents.ROBOT_KEYWORD_CALL_ADDED), eq(ImmutableMap
                .of(IEventBroker.DATA, testCase, RobotModelEvents.ADDITIONAL_DATA, Arrays.asList(callsToInsert))));
        verifyNoMoreInteractions(eventBroker);
    }

    @Test
    public void keywordExecutableRowIsProperlyInsertedIntoKeyword() {
        final RobotKeywordDefinition keyword = createKeywordForInsertions();
        final RobotKeywordCall executableRow = createKeywordExecutableRow("call");
        final RobotKeywordCall[] callsToInsert = new RobotKeywordCall[] { executableRow };

        ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new InsertKeywordCallsCommand(keyword, callsToInsert))
                .execute();

        assertThat(keyword.getChildren().size()).isEqualTo(3);
        assertThat(keyword.getChildren().get(2)).isExactlyInstanceOf(RobotKeywordCall.class);
        assertThat(keyword.getChildren().get(2).getLinkedElement().getDeclaration().getTypes())
                .contains(RobotTokenType.KEYWORD_ACTION_NAME);
        assertThat(keyword.getChildren().get(2).getName()).isEqualTo("call");
        assertThat(keyword.getChildren().get(2).getArguments()).containsExactly("arg1", "arg2");
        assertThat(keyword.getChildren().get(2).getComment()).isEqualTo("#comment");
        assertThat(keyword.getChildren().get(2)).has(properlySetParent());

        verify(eventBroker, times(1)).send(eq(RobotModelEvents.ROBOT_KEYWORD_CALL_ADDED), eq(ImmutableMap
                .of(IEventBroker.DATA, keyword, RobotModelEvents.ADDITIONAL_DATA, newArrayList(executableRow))));
        verifyNoMoreInteractions(eventBroker);
    }

    @Test
    public void multipleExecutableRowsAreProperlyInsertedIntoKeyword() {
        final RobotKeywordDefinition keyword = createKeywordForInsertions();
        final RobotKeywordCall[] callsToInsert = new RobotKeywordCall[] { createKeywordExecutableRow("call_a"),
                createKeywordExecutableRow("call_b") };

        ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new InsertKeywordCallsCommand(keyword, 0, callsToInsert))
                .execute();

        assertThat(keyword.getChildren().size()).isEqualTo(4);
        assertThat(keyword.getChildren().get(0)).isExactlyInstanceOf(RobotKeywordCall.class);
        assertThat(keyword.getChildren().get(0).getLinkedElement().getDeclaration().getTypes())
                .contains(RobotTokenType.KEYWORD_ACTION_NAME);
        assertThat(keyword.getChildren().get(0).getName()).isEqualTo("call_a");
        assertThat(keyword.getChildren().get(0).getArguments()).containsExactly("arg1", "arg2");
        assertThat(keyword.getChildren().get(0).getComment()).isEqualTo("#comment");
        assertThat(keyword.getChildren().get(0)).has(properlySetParent());
        assertThat(keyword.getChildren().get(1)).isExactlyInstanceOf(RobotKeywordCall.class);
        assertThat(keyword.getChildren().get(1).getLinkedElement().getDeclaration().getTypes())
                .contains(RobotTokenType.KEYWORD_ACTION_NAME);
        assertThat(keyword.getChildren().get(1).getName()).isEqualTo("call_b");
        assertThat(keyword.getChildren().get(1).getArguments()).containsExactly("arg1", "arg2");
        assertThat(keyword.getChildren().get(1).getComment()).isEqualTo("#comment");
        assertThat(keyword.getChildren().get(1)).has(properlySetParent());

        verify(eventBroker, times(1)).send(eq(RobotModelEvents.ROBOT_KEYWORD_CALL_ADDED), eq(ImmutableMap
                .of(IEventBroker.DATA, keyword, RobotModelEvents.ADDITIONAL_DATA, Arrays.asList(callsToInsert))));
        verifyNoMoreInteractions(eventBroker);
    }

    @Test
    public void keywordSettingIsInsertedToProperPosition_whenInsertingIntoTestCaseBody() {
        final RobotKeywordDefinition keyword = createKeywordWithSettingsForInsertions();
        final RobotKeywordCall[] callsToInsert = new RobotKeywordCall[] { createKeywordUnknownSetting("setting") };

        final InsertKeywordCallsCommand command = new InsertKeywordCallsCommand(keyword, 4, callsToInsert);
        command.setEventBroker(eventBroker);
        command.execute();

        assertThat(keyword.getChildren()).extracting(RobotElement::getName)
                .containsExactly("arguments", "tags", "return", "call1", "setting", "call2", "call3");

        verify(eventBroker, times(1)).send(eq(RobotModelEvents.ROBOT_KEYWORD_CALL_ADDED), eq(ImmutableMap
                .of(IEventBroker.DATA, keyword, RobotModelEvents.ADDITIONAL_DATA, Arrays.asList(callsToInsert))));
        verifyNoMoreInteractions(eventBroker);
    }

    @Test
    public void keywordExecutableRowIsInsertedToProperPosition_whenInsertingIntoTestCaseSettings() {
        final RobotKeywordDefinition keyword = createKeywordWithSettingsForInsertions();
        final RobotKeywordCall[] callsToInsert = new RobotKeywordCall[] { createKeywordExecutableRow("action") };

        final InsertKeywordCallsCommand command = new InsertKeywordCallsCommand(keyword, 1, callsToInsert);
        command.setEventBroker(eventBroker);
        command.execute();

        assertThat(keyword.getChildren()).extracting(RobotElement::getName)
                .containsExactly("arguments", "action", "tags", "return", "call1", "call2", "call3");

        verify(eventBroker, times(1)).send(eq(RobotModelEvents.ROBOT_KEYWORD_CALL_ADDED), eq(ImmutableMap
                .of(IEventBroker.DATA, keyword, RobotModelEvents.ADDITIONAL_DATA, Arrays.asList(callsToInsert))));
        verifyNoMoreInteractions(eventBroker);
    }

    @Test
    public void testCaseExecutableRowIsProperlyInsertedIntoKeyword() {
        final RobotKeywordDefinition keyword = createKeywordForInsertions();
        final RobotKeywordCall executableRow = createKeywordExecutableRow("call");
        final RobotKeywordCall[] callsToInsert = new RobotKeywordCall[] { executableRow };

        ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new InsertKeywordCallsCommand(keyword, 0, callsToInsert))
                .execute();

        assertThat(keyword.getChildren().size()).isEqualTo(3);
        assertThat(keyword.getChildren().get(0)).isExactlyInstanceOf(RobotKeywordCall.class);
        assertThat(keyword.getChildren().get(0).getLinkedElement().getDeclaration().getTypes())
                .contains(RobotTokenType.KEYWORD_ACTION_NAME);
        assertThat(keyword.getChildren().get(0).getName()).isEqualTo("call");
        assertThat(keyword.getChildren().get(0).getArguments()).containsExactly("arg1", "arg2");
        assertThat(keyword.getChildren().get(0).getComment()).isEqualTo("#comment");
        assertThat(keyword.getChildren().get(0)).has(properlySetParent());

        verify(eventBroker, times(1)).send(eq(RobotModelEvents.ROBOT_KEYWORD_CALL_ADDED), eq(ImmutableMap
                .of(IEventBroker.DATA, keyword, RobotModelEvents.ADDITIONAL_DATA, newArrayList(executableRow))));
        verifyNoMoreInteractions(eventBroker);
    }

    @Test
    public void testCaseTagsSettingIsProperlyInsertedIntoTestCase() {
        testCaseSettingIsProperlyInsertedIntoTestCase(createTestCaseTagsSetting(), ModelType.TEST_CASE_TAGS, "Tags",
                newArrayList("arg1", "arg2"), "#comment");
    }

    @Test
    public void testCaseTagsSettingIsProperlyInsertedIntoKeyword() {
        testCaseSettingIsProperlyInsertedIntoKeyword(createTestCaseTagsSetting(), ModelType.USER_KEYWORD_TAGS, "Tags",
                newArrayList("arg1", "arg2"), "#comment");
    }

    @Test
    public void testCaseSetupSettingIsProperlyInsertedIntoTestCase() {
        testCaseSettingIsProperlyInsertedIntoTestCase(createTestCaseSetupSetting(), ModelType.TEST_CASE_SETUP, "Setup",
                newArrayList("call", "arg1", "arg2"), "#comment");
    }

    @Test
    public void testCaseSetupSettingIsProperlyInsertedIntoKeyword() {
        testCaseSettingIsProperlyInsertedIntoKeyword(createTestCaseSetupSetting(),
                ModelType.USER_KEYWORD_SETTING_UNKNOWN, "Setup", newArrayList("call", "arg1", "arg2"), "#comment");
    }

    @Test
    public void testCaseTeardownSettingIsProperlyInsertedIntoTestCase() {
        testCaseSettingIsProperlyInsertedIntoTestCase(createTestCaseTeardownSetting(), ModelType.TEST_CASE_TEARDOWN,
                "Teardown", newArrayList("call", "arg1", "arg2"), "#comment");
    }

    @Test
    public void testCaseTeardownSettingIsProperlyInsertedIntoKeyword() {
        testCaseSettingIsProperlyInsertedIntoKeyword(createTestCaseTeardownSetting(), ModelType.USER_KEYWORD_TEARDOWN,
                "Teardown", newArrayList("call", "arg1", "arg2"), "#comment");
    }

    @Test
    public void testCaseTemplateSettingIsProperlyInsertedIntoTestCase() {
        testCaseSettingIsProperlyInsertedIntoTestCase(createTestCaseTemplateSetting(), ModelType.TEST_CASE_TEMPLATE,
                "Template", newArrayList("call", "arg1", "arg2"), "#comment");
    }

    @Test
    public void testCaseTemplateSettingIsProperlyInsertedIntoKeyword() {
        testCaseSettingIsProperlyInsertedIntoKeyword(createTestCaseTemplateSetting(),
                ModelType.USER_KEYWORD_SETTING_UNKNOWN, "Template", newArrayList("call", "arg1", "arg2"), "#comment");
    }

    @Test
    public void testCaseTimeoutSettingIsProperlyInsertedIntoTestCase() {
        testCaseSettingIsProperlyInsertedIntoTestCase(createTestCaseTimeoutSetting(), ModelType.TEST_CASE_TIMEOUT,
                "Timeout", newArrayList("10", "arg1", "arg2"), "#comment");
    }

    @Test
    public void testCaseTimeoutSettingIsProperlyInsertedIntoKeyword() {
        testCaseSettingIsProperlyInsertedIntoKeyword(createTestCaseTimeoutSetting(), ModelType.USER_KEYWORD_TIMEOUT,
                "Timeout", newArrayList("10", "arg1", "arg2"), "#comment");
    }

    @Test
    public void testCaseDocumentationSettingIsProperlyInsertedIntoTestCase() {
        testCaseSettingIsProperlyInsertedIntoTestCase(createTestCaseDocumentationSetting(),
                ModelType.TEST_CASE_DOCUMENTATION, "Documentation", newArrayList("arg1", "arg2"), "#comment");
    }

    @Test
    public void testCaseDocumentationSettingIsProperlyInsertedIntoKeyword() {
        testCaseSettingIsProperlyInsertedIntoKeyword(createTestCaseDocumentationSetting(),
                ModelType.USER_KEYWORD_DOCUMENTATION, "Documentation", newArrayList("arg1", "arg2"), "#comment");
    }

    @Test
    public void testCaseUnknownSettingIsProperlyInsertedIntoTestCase() {
        testCaseSettingIsProperlyInsertedIntoTestCase(createTestCaseUnknownSetting("unknown"),
                ModelType.TEST_CASE_SETTING_UNKNOWN, "unknown", newArrayList("arg1", "arg2"), "#comment");
    }

    @Test
    public void testCaseUnknownSettingIsProperlyInsertedIntoKeyword_1() {
        testCaseSettingIsProperlyInsertedIntoKeyword(createTestCaseUnknownSetting("unknown"),
                ModelType.USER_KEYWORD_SETTING_UNKNOWN, "unknown", newArrayList("arg1", "arg2"), "#comment");
    }

    @Test
    public void testCaseUnknownSettingIsProperlyInsertedIntoKeyword_2() {
        testCaseSettingIsProperlyInsertedIntoKeyword(createTestCaseUnknownSetting("Arguments"),
                ModelType.USER_KEYWORD_ARGUMENTS, "Arguments", newArrayList("arg1", "arg2"), "#comment");
    }

    @Test
    public void testCaseUnknownSettingIsProperlyInsertedIntoKeyword_3() {
        testCaseSettingIsProperlyInsertedIntoKeyword(createTestCaseUnknownSetting("Return"),
                ModelType.USER_KEYWORD_RETURN, "Return", newArrayList("arg1", "arg2"), "#comment");
    }

    private void testCaseSettingIsProperlyInsertedIntoTestCase(final RobotKeywordCall setting,
            final ModelType expectedTypeAfterInsert, final String expectedName, final List<String> expectedArguments,
            final String expectedComment) {
        final RobotCase testCase = createTestCaseForInsertions();
        final RobotKeywordCall[] callsToInsert = new RobotKeywordCall[] { setting };

        ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new InsertKeywordCallsCommand(testCase, 0, callsToInsert))
                .execute();

        assertThat(testCase.getChildren().size()).isEqualTo(3);
        assertThat(testCase.getChildren().get(0)).isExactlyInstanceOf(RobotKeywordCall.class);
        assertThat(testCase.getChildren().get(0).getLinkedElement().getModelType()).isEqualTo(expectedTypeAfterInsert);
        assertThat(testCase.getChildren().get(0).getName()).isEqualTo(expectedName);
        assertThat(testCase.getChildren().get(0).getArguments()).containsExactlyElementsOf(expectedArguments);
        assertThat(testCase.getChildren().get(0).getComment()).isEqualTo(expectedComment);
        assertThat(testCase.getChildren().get(0)).has(properlySetParent());

        verify(eventBroker, times(1)).send(eq(RobotModelEvents.ROBOT_KEYWORD_CALL_ADDED), eq(
                ImmutableMap.of(IEventBroker.DATA, testCase, RobotModelEvents.ADDITIONAL_DATA, newArrayList(setting))));
        verifyNoMoreInteractions(eventBroker);
    }

    private void testCaseSettingIsProperlyInsertedIntoKeyword(final RobotKeywordCall setting,
            final ModelType expectedTypeAfterInsert, final String expectedName, final List<String> expectedArguments,
            final String expectedComment) {
        final RobotKeywordDefinition keyword = createKeywordForInsertions();
        final RobotKeywordCall[] callsToInsert = new RobotKeywordCall[] { setting };

        ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new InsertKeywordCallsCommand(keyword, 0, callsToInsert))
                .execute();

        assertThat(keyword.getChildren().size()).isEqualTo(3);
        assertThat(keyword.getChildren().get(0)).isExactlyInstanceOf(RobotKeywordCall.class);
        assertThat(keyword.getChildren().get(0).getLinkedElement().getModelType()).isEqualTo(expectedTypeAfterInsert);
        assertThat(keyword.getChildren().get(0).getName()).isEqualTo(expectedName);
        assertThat(keyword.getChildren().get(0).getArguments()).containsExactlyElementsOf(expectedArguments);
        assertThat(keyword.getChildren().get(0).getComment()).isEqualTo(expectedComment);
        assertThat(keyword.getChildren().get(0)).has(properlySetParent());

        verify(eventBroker, times(1)).send(eq(RobotModelEvents.ROBOT_KEYWORD_CALL_ADDED), eq(
                ImmutableMap.of(IEventBroker.DATA, keyword, RobotModelEvents.ADDITIONAL_DATA, newArrayList(setting))));
        verifyNoMoreInteractions(eventBroker);
    }

    @Test
    public void keywordTagsSettingIsProperlyInsertedIntoTestCase() {
        keywordSettingIsProperlyInsertedIntoTestCase(createKeywordTagsSetting(), ModelType.TEST_CASE_TAGS, "Tags",
                newArrayList("arg1", "arg2"), "#comment");
    }

    @Test
    public void keywordTagsSettingIsProperlyInsertedIntoKeyword() {
        keywordSettingIsProperlyInsertedIntoKeyword(createKeywordTagsSetting(), ModelType.USER_KEYWORD_TAGS, "Tags",
                newArrayList("arg1", "arg2"), "#comment");
    }

    @Test
    public void keywordArgumentsSettingIsProperlyInsertedIntoTestCase() {
        keywordSettingIsProperlyInsertedIntoTestCase(createKeywordArgumentsSetting(),
                ModelType.TEST_CASE_SETTING_UNKNOWN, "Arguments", newArrayList("arg1", "arg2"), "#comment");
    }

    @Test
    public void keywordArgumentsSettingIsProperlyInsertedIntoKeyword() {
        keywordSettingIsProperlyInsertedIntoKeyword(createKeywordArgumentsSetting(), ModelType.USER_KEYWORD_ARGUMENTS,
                "Arguments", newArrayList("arg1", "arg2"), "#comment");
    }

    @Test
    public void keywordTeardownSettingIsProperlyInsertedIntoTestCase() {
        keywordSettingIsProperlyInsertedIntoTestCase(createKeywordTeardownSetting(), ModelType.TEST_CASE_TEARDOWN,
                "Teardown", newArrayList("call", "arg1", "arg2"), "#comment");
    }

    @Test
    public void keywordTeardownSettingIsProperlyInsertedIntoKeyword() {
        keywordSettingIsProperlyInsertedIntoKeyword(createKeywordTeardownSetting(), ModelType.USER_KEYWORD_TEARDOWN,
                "Teardown", newArrayList("call", "arg1", "arg2"), "#comment");
    }

    @Test
    public void keywordTimeoutSettingIsProperlyInsertedIntoTestCase() {
        keywordSettingIsProperlyInsertedIntoTestCase(createKeywordTimeoutSetting(), ModelType.TEST_CASE_TIMEOUT,
                "Timeout", newArrayList("10", "arg1", "arg2"), "#comment");
    }

    @Test
    public void keywordTimeoutSettingIsProperlyInsertedIntoKeyword() {
        keywordSettingIsProperlyInsertedIntoKeyword(createKeywordTimeoutSetting(), ModelType.USER_KEYWORD_TIMEOUT,
                "Timeout", newArrayList("10", "arg1", "arg2"), "#comment");
    }

    @Test
    public void keywordReturnSettingIsProperlyInsertedIntoTestCase() {
        keywordSettingIsProperlyInsertedIntoTestCase(createKeywordReturnSetting(), ModelType.TEST_CASE_SETTING_UNKNOWN,
                "Return", newArrayList("arg1", "arg2"), "#comment");
    }

    @Test
    public void keywordReturnSettingIsProperlyInsertedIntoKeyword() {
        keywordSettingIsProperlyInsertedIntoKeyword(createKeywordReturnSetting(), ModelType.USER_KEYWORD_RETURN,
                "Return", newArrayList("arg1", "arg2"), "#comment");
    }

    @Test
    public void keywordDocumentationSettingIsProperlyInsertedIntoTestCase() {
        keywordSettingIsProperlyInsertedIntoTestCase(createKeywordDocumentationSetting(),
                ModelType.TEST_CASE_DOCUMENTATION, "Documentation", newArrayList("arg1", "arg2"), "#comment");
    }

    @Test
    public void keywordDocumentationSettingIsProperlyInsertedIntoKeyword() {
        keywordSettingIsProperlyInsertedIntoKeyword(createKeywordDocumentationSetting(),
                ModelType.USER_KEYWORD_DOCUMENTATION, "Documentation", newArrayList("arg1", "arg2"), "#comment");
    }

    @Test
    public void keywordUnknownSettingIsProperlyInsertedIntoKeyword() {
        keywordSettingIsProperlyInsertedIntoKeyword(createKeywordUnknownSetting("unknown"),
                ModelType.USER_KEYWORD_SETTING_UNKNOWN, "unknown", newArrayList("arg1", "arg2"), "#comment");
    }

    @Test
    public void keywordUnknownSettingIsProperlyInsertedIntoTestCase_1() {
        keywordSettingIsProperlyInsertedIntoTestCase(createKeywordUnknownSetting("unknown"),
                ModelType.TEST_CASE_SETTING_UNKNOWN, "unknown", newArrayList("arg1", "arg2"), "#comment");
    }

    @Test
    public void keywordUnknownSettingIsProperlyInsertedIntoTestCase_2() {
        keywordSettingIsProperlyInsertedIntoTestCase(createKeywordUnknownSetting("Setup"), ModelType.TEST_CASE_SETUP,
                "Setup", newArrayList("arg1", "arg2"), "#comment");
    }

    @Test
    public void keywordUnknownSettingIsProperlyInsertedIntoTestCase_3() {
        keywordSettingIsProperlyInsertedIntoTestCase(createKeywordUnknownSetting("Template"),
                ModelType.TEST_CASE_TEMPLATE, "Template", newArrayList("arg1", "arg2"), "#comment");
    }

    private void keywordSettingIsProperlyInsertedIntoKeyword(final RobotKeywordCall setting,
            final ModelType expectedTypeAfterInsert, final String expectedName, final List<String> expectedArguments,
            final String expectedComment) {
        final RobotKeywordDefinition keyword = createKeywordForInsertions();
        final RobotKeywordCall[] callsToInsert = new RobotKeywordCall[] { setting };

        ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new InsertKeywordCallsCommand(keyword, 0, callsToInsert))
                .execute();

        assertThat(keyword.getChildren().size()).isEqualTo(3);
        assertThat(keyword.getChildren().get(0)).isExactlyInstanceOf(RobotKeywordCall.class);
        assertThat(keyword.getChildren().get(0).getLinkedElement().getModelType()).isEqualTo(expectedTypeAfterInsert);
        assertThat(keyword.getChildren().get(0).getName()).isEqualTo(expectedName);
        assertThat(keyword.getChildren().get(0).getArguments()).containsExactlyElementsOf(expectedArguments);
        assertThat(keyword.getChildren().get(0).getComment()).isEqualTo(expectedComment);
        assertThat(keyword.getChildren().get(0)).has(properlySetParent());

        verify(eventBroker, times(1)).send(eq(RobotModelEvents.ROBOT_KEYWORD_CALL_ADDED), eq(
                ImmutableMap.of(IEventBroker.DATA, keyword, RobotModelEvents.ADDITIONAL_DATA, newArrayList(setting))));
        verifyNoMoreInteractions(eventBroker);
    }

    private void keywordSettingIsProperlyInsertedIntoTestCase(final RobotKeywordCall setting,
            final ModelType expectedTypeAfterInsert, final String expectedName, final List<String> expectedArguments,
            final String expectedComment) {
        final RobotCase testCase = createTestCaseForInsertions();
        final RobotKeywordCall[] callsToInsert = new RobotKeywordCall[] { setting };

        ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new InsertKeywordCallsCommand(testCase, 0, callsToInsert))
                .execute();

        assertThat(testCase.getChildren().size()).isEqualTo(3);
        assertThat(testCase.getChildren().get(0)).isExactlyInstanceOf(RobotKeywordCall.class);
        assertThat(testCase.getChildren().get(0).getLinkedElement().getModelType()).isEqualTo(expectedTypeAfterInsert);
        assertThat(testCase.getChildren().get(0).getName()).isEqualTo(expectedName);
        assertThat(testCase.getChildren().get(0).getArguments()).containsExactlyElementsOf(expectedArguments);
        assertThat(testCase.getChildren().get(0).getComment()).isEqualTo(expectedComment);
        assertThat(testCase.getChildren().get(0)).has(properlySetParent());

        verify(eventBroker, times(1)).send(eq(RobotModelEvents.ROBOT_KEYWORD_CALL_ADDED), eq(
                ImmutableMap.of(IEventBroker.DATA, testCase, RobotModelEvents.ADDITIONAL_DATA, newArrayList(setting))));
        verifyNoMoreInteractions(eventBroker);
    }

    private static RobotCase createTestCaseForInsertions() {
        final RobotSuiteFile model = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("case")
                .appendLine("  call1  1")
                .appendLine("  call2  2")
                .build();
        return model.findSection(RobotCasesSection.class).get().getChildren().get(0);
    }

    private static RobotCase createTestCaseWithSettingsForInsertions() {
        final RobotSuiteFile model = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("case")
                .appendLine("  [tags]  t1  t2")
                .appendLine("  [setup]  kw")
                .appendLine("  [teardown]  kw")
                .appendLine("  call1  1")
                .appendLine("  call2  2")
                .appendLine("  call3  3")
                .build();
        return model.findSection(RobotCasesSection.class).get().getChildren().get(0);
    }

    private static RobotKeywordDefinition createKeywordForInsertions() {
        final RobotSuiteFile model = new RobotSuiteFileCreator().appendLine("*** Keywords ***")
                .appendLine("kw")
                .appendLine("  call1  1")
                .appendLine("  call2  2")
                .build();
        return model.findSection(RobotKeywordsSection.class).get().getChildren().get(0);
    }

    private static RobotKeywordDefinition createKeywordWithSettingsForInsertions() {
        final RobotSuiteFile model = new RobotSuiteFileCreator().appendLine("*** Keywords ***")
                .appendLine("kw")
                .appendLine("  [arguments]  kw")
                .appendLine("  [tags]  t1  t2")
                .appendLine("  [return]  x")
                .appendLine("  call1  1")
                .appendLine("  call2  2")
                .appendLine("  call3  3")
                .build();
        return model.findSection(RobotKeywordsSection.class).get().getChildren().get(0);
    }

    private static RobotKeywordCall createTestCaseExecutableRow(final String action) {
        final RobotExecutableRow<?> linkedElement = new RobotExecutableRow<>();
        final RobotToken actionToken = RobotToken.create(action);
        actionToken.setType(RobotTokenType.TEST_CASE_ACTION_NAME);
        linkedElement.setAction(actionToken);
        linkedElement.setArgument(0, "arg1");
        linkedElement.setArgument(1, "arg2");
        linkedElement.setComment("comment");
        return new RobotKeywordCall(null, linkedElement);
    }

    private static RobotKeywordCall createKeywordExecutableRow(final String action) {
        final RobotExecutableRow<?> linkedElement = new RobotExecutableRow<>();
        final RobotToken actionToken = RobotToken.create(action);
        actionToken.setType(RobotTokenType.KEYWORD_ACTION_NAME);
        linkedElement.setAction(actionToken);
        linkedElement.setArgument(0, "arg1");
        linkedElement.setArgument(1, "arg2");
        linkedElement.setComment("comment");
        return new RobotKeywordCall(null, linkedElement);
    }

    private static RobotKeywordCall createTestCaseTagsSetting() {
        final LocalSetting<TestCase> linkedElement = new LocalSetting<>(ModelType.TEST_CASE_TAGS,
                RobotToken.create("[Tags]"));
        linkedElement.addToken("arg1");
        linkedElement.addToken("arg2");
        linkedElement.setComment("comment");
        return new RobotKeywordCall(null, linkedElement);
    }

    private static RobotKeywordCall createTestCaseSetupSetting() {
        final LocalSetting<TestCase> linkedElement = new LocalSetting<>(ModelType.TEST_CASE_SETUP,
                RobotToken.create("[Setup]"));
        linkedElement.addToken("call");
        linkedElement.addToken("arg1");
        linkedElement.addToken("arg2");
        linkedElement.setComment("comment");
        return new RobotKeywordCall(null, linkedElement);
    }

    private static RobotKeywordCall createTestCaseTeardownSetting() {
        final LocalSetting<TestCase> linkedElement = new LocalSetting<>(ModelType.TEST_CASE_TEARDOWN,
                RobotToken.create("[Teardown]"));
        linkedElement.addToken("call");
        linkedElement.addToken("arg1");
        linkedElement.addToken("arg2");
        linkedElement.setComment("comment");
        return new RobotKeywordCall(null, linkedElement);
    }

    private static RobotKeywordCall createTestCaseTimeoutSetting() {
        final LocalSetting<TestCase> linkedElement = new LocalSetting<>(ModelType.TEST_CASE_TIMEOUT,
                RobotToken.create("[Timeout]"));
        linkedElement.addToken("10");
        linkedElement.addToken("arg1");
        linkedElement.addToken("arg2");
        linkedElement.setComment("comment");
        return new RobotKeywordCall(null, linkedElement);
    }

    private static RobotKeywordCall createTestCaseTemplateSetting() {
        final LocalSetting<TestCase> linkedElement = new LocalSetting<>(ModelType.TEST_CASE_TEMPLATE,
                RobotToken.create("[Template]"));
        linkedElement.addToken("call");
        linkedElement.addToken("arg1");
        linkedElement.addToken("arg2");
        linkedElement.setComment("comment");
        return new RobotKeywordCall(null, linkedElement);
    }

    private static RobotKeywordCall createTestCaseDocumentationSetting() {
        final LocalSetting<TestCase> linkedElement = new LocalSetting<>(ModelType.TEST_CASE_DOCUMENTATION,
                RobotToken.create("[Documentation]"));
        linkedElement.addToken("arg1");
        linkedElement.addToken("arg2");
        linkedElement.setComment("comment");
        return new RobotKeywordCall(null, linkedElement);
    }

    private static RobotKeywordCall createTestCaseUnknownSetting(final String settingName) {
        final LocalSetting<TestCase> linkedElement = new LocalSetting<>(ModelType.TEST_CASE_SETTING_UNKNOWN,
                RobotToken.create("[" + settingName + "]"));
        linkedElement.addToken("arg1");
        linkedElement.addToken("arg2");
        linkedElement.setComment("comment");
        return new RobotKeywordCall(null, linkedElement);
    }

    private static RobotKeywordCall createKeywordTagsSetting() {
        final LocalSetting<TestCase> linkedElement = new LocalSetting<>(ModelType.TEST_CASE_TAGS,
                RobotToken.create("[Tags]"));
        linkedElement.addToken("arg1");
        linkedElement.addToken("arg2");
        linkedElement.setComment("comment");
        return new RobotKeywordCall(null, linkedElement);
    }

    private static RobotKeywordCall createKeywordArgumentsSetting() {
        final LocalSetting<UserKeyword> linkedElement = new LocalSetting<>(ModelType.USER_KEYWORD_ARGUMENTS,
                RobotToken.create("[Arguments]"));
        linkedElement.addToken("arg1");
        linkedElement.addToken("arg2");
        linkedElement.setComment("comment");
        return new RobotKeywordCall(null, linkedElement);
    }

    private static RobotKeywordCall createKeywordTeardownSetting() {
        final LocalSetting<UserKeyword> linkedElement = new LocalSetting<>(ModelType.USER_KEYWORD_TEARDOWN,
                RobotToken.create("[Teardown]"));
        linkedElement.addToken("call");
        linkedElement.addToken("arg1");
        linkedElement.addToken("arg2");
        linkedElement.setComment("comment");
        return new RobotKeywordCall(null, linkedElement);
    }

    private static RobotKeywordCall createKeywordTimeoutSetting() {
        final LocalSetting<UserKeyword> linkedElement = new LocalSetting<>(ModelType.USER_KEYWORD_TIMEOUT,
                RobotToken.create("[Timeout]"));
        linkedElement.addToken("10");
        linkedElement.addToken("arg1");
        linkedElement.addToken("arg2");
        linkedElement.setComment("comment");
        return new RobotKeywordCall(null, linkedElement);
    }

    private static RobotKeywordCall createKeywordReturnSetting() {
        final LocalSetting<UserKeyword> linkedElement = new LocalSetting<>(ModelType.USER_KEYWORD_RETURN,
                RobotToken.create("[Return]"));
        linkedElement.addToken("arg1");
        linkedElement.addToken("arg2");
        linkedElement.setComment("comment");
        return new RobotKeywordCall(null, linkedElement);
    }

    private static RobotKeywordCall createKeywordDocumentationSetting() {
        final LocalSetting<UserKeyword> linkedElement = new LocalSetting<>(ModelType.USER_KEYWORD_DOCUMENTATION,
                RobotToken.create("[Documentation]"));
        linkedElement.addToken("arg1");
        linkedElement.addToken("arg2");
        linkedElement.setComment("comment");
        return new RobotKeywordCall(null, linkedElement);
    }

    private static RobotKeywordCall createKeywordUnknownSetting(final String settingName) {
        final LocalSetting<UserKeyword> linkedElement = new LocalSetting<>(ModelType.USER_KEYWORD_SETTING_UNKNOWN,
                RobotToken.create("[" + settingName + "]"));
        linkedElement.addToken("arg1");
        linkedElement.addToken("arg2");
        linkedElement.setComment("comment");
        return new RobotKeywordCall(null, linkedElement);
    }
}
