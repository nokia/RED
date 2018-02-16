/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assume.assumeTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.junit.Before;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;
import org.rf.ide.core.testdata.model.ModelType;
import org.robotframework.ide.eclipse.main.plugin.mockeclipse.ContextInjector;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.RobotSuiteFileCreator;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCasesSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCodeHoldingElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotDefinitionSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Table;

@RunWith(Theories.class)
public class SetKeywordCallNameCommandTest {

    private final Table<CommonModelTypes, Class<?>, ModelType> modelTypes = HashBasedTable.create();
    {
        modelTypes.put(CommonModelTypes.EXECUTABLE_ROW, RobotKeywordDefinition.class,
                ModelType.USER_KEYWORD_EXECUTABLE_ROW);
        modelTypes.put(CommonModelTypes.EXECUTABLE_ROW, RobotCase.class, ModelType.TEST_CASE_EXECUTABLE_ROW);
        modelTypes.put(CommonModelTypes.TAGS, RobotKeywordDefinition.class, ModelType.USER_KEYWORD_TAGS);
        modelTypes.put(CommonModelTypes.TAGS, RobotCase.class, ModelType.TEST_CASE_TAGS);
        modelTypes.put(CommonModelTypes.ARGUMENTS, RobotKeywordDefinition.class, ModelType.USER_KEYWORD_ARGUMENTS);
        modelTypes.put(CommonModelTypes.ARGUMENTS, RobotCase.class, ModelType.TEST_CASE_SETTING_UNKNOWN);
        modelTypes.put(CommonModelTypes.SETUP, RobotKeywordDefinition.class, ModelType.USER_KEYWORD_SETTING_UNKNOWN);
        modelTypes.put(CommonModelTypes.SETUP, RobotCase.class, ModelType.TEST_CASE_SETUP);
        modelTypes.put(CommonModelTypes.TEARDOWN, RobotKeywordDefinition.class, ModelType.USER_KEYWORD_TEARDOWN);
        modelTypes.put(CommonModelTypes.TEARDOWN, RobotCase.class, ModelType.TEST_CASE_TEARDOWN);
        modelTypes.put(CommonModelTypes.UNKNOWN, RobotKeywordDefinition.class, ModelType.USER_KEYWORD_SETTING_UNKNOWN);
        modelTypes.put(CommonModelTypes.UNKNOWN, RobotCase.class, ModelType.TEST_CASE_SETTING_UNKNOWN);
    }

    private IEventBroker eventBroker;

    @Before
    public void beforeTest() {
        eventBroker = mock(IEventBroker.class);
    }

    @DataPoints
    public static RobotCodeHoldingElement<?>[] codeHolders() {
        final List<RobotCodeHoldingElement<?>> elements = new ArrayList<>();
        elements.addAll(createKeywords());
        elements.addAll(createTestCases());
        return elements.toArray(new RobotCodeHoldingElement<?>[0]);
    }

    @Theory
    public void callChangesNameProperlyToOtherCall(
            final RobotCodeHoldingElement<?> executablesHolder) {
        // only perform this test for keyword/case without special purpose
        assumeTrue(!executablesHolder.getName().contains("_"));

        final int callIndex = 2;
        final int callIndexAfterCommand = 2;

        final RobotKeywordCall call = executablesHolder.getChildren().get(callIndex);
        assertThat(call.getName()).isEqualTo("call");

        final SetKeywordCallNameCommand command = ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new SetKeywordCallNameCommand(call, "new_call"));

        command.execute();

        final RobotKeywordCall callAfterNameChange = executablesHolder.getChildren().get(callIndexAfterCommand);
        assertThat(constructRow(callAfterNameChange)).containsExactly("new_call", "arg1", "arg2", "arg3", "# comment");
        assertThat(callAfterNameChange.getLinkedElement().getModelType())
                .isEqualTo(modelTypes.get(CommonModelTypes.EXECUTABLE_ROW, executablesHolder.getClass()));

        for (final EditorCommand undo : command.getUndoCommands()) {
            undo.execute();
        }

        final RobotKeywordCall callAfterUndo = executablesHolder.getChildren().get(callIndex);
        assertThat(constructRow(callAfterUndo)).containsExactly("call", "arg1", "arg2", "arg3", "# comment");
        assertThat(callAfterUndo.getLinkedElement().getModelType())
                .isEqualTo(modelTypes.get(CommonModelTypes.EXECUTABLE_ROW, executablesHolder.getClass()));

        verify(eventBroker, times(2)).send(RobotModelEvents.ROBOT_KEYWORD_CALL_NAME_CHANGE, call);
        verifyNoMoreInteractions(eventBroker);
    }

    @Theory
    public void callChangesNameProperlyToEmptyNamedCall(
            final RobotCodeHoldingElement<?> executablesHolder) {
        // only perform this test for keyword/case without special purpose
        assumeTrue(!executablesHolder.getName().contains("_"));

        final int callIndex = 2;
        final int callIndexAfterCommand = 2;

        final RobotKeywordCall call = executablesHolder.getChildren().get(callIndex);
        assertThat(call.getName()).isEqualTo("call");

        final SetKeywordCallNameCommand command = ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new SetKeywordCallNameCommand(call, ""));

        command.execute();

        final RobotKeywordCall callAfterNameChange = executablesHolder.getChildren().get(callIndexAfterCommand);
        assertThat(constructRow(callAfterNameChange)).containsExactly("\\", "arg1", "arg2", "arg3", "# comment");
        assertThat(callAfterNameChange.getLinkedElement().getModelType())
                .isEqualTo(modelTypes.get(CommonModelTypes.EXECUTABLE_ROW, executablesHolder.getClass()));

        for (final EditorCommand undo : command.getUndoCommands()) {
            undo.execute();
        }

        final RobotKeywordCall callAfterUndo = executablesHolder.getChildren().get(callIndex);
        assertThat(constructRow(callAfterUndo)).containsExactly("call", "arg1", "arg2", "arg3", "# comment");
        assertThat(callAfterUndo.getLinkedElement().getModelType())
                .isEqualTo(modelTypes.get(CommonModelTypes.EXECUTABLE_ROW, executablesHolder.getClass()));

        verify(eventBroker, times(2)).send(RobotModelEvents.ROBOT_KEYWORD_CALL_NAME_CHANGE, call);
        verifyNoMoreInteractions(eventBroker);
    }

    @Theory
    public void callChangesNameProperlyToSetting(
            final RobotCodeHoldingElement<?> executablesHolder) {
        // only perform this test for keyword/case without special purpose
        assumeTrue(!executablesHolder.getName().contains("_"));

        final int callIndex = 1;
        final int callIndexAfterCommand = 1;

        final RobotKeywordCall call = executablesHolder.getChildren().get(callIndex);
        assertThat(call.getName()).isEqualTo("call_to_setting");

        final SetKeywordCallNameCommand command = ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new SetKeywordCallNameCommand(call, "[Tags]"));
        command.execute();

        final RobotKeywordCall callAfterNameChange = executablesHolder.getChildren().get(callIndexAfterCommand);
        assertThat(constructRow(callAfterNameChange)).containsExactly("Tags", "[setup]", "arg1", "arg2", "# comment");
        assertThat(callAfterNameChange.getLinkedElement().getModelType())
                .isEqualTo(modelTypes.get(CommonModelTypes.TAGS, executablesHolder.getClass()));

        for (final EditorCommand undo : command.getUndoCommands()) {
            undo.execute();
        }

        final RobotKeywordCall callAfterUndo = executablesHolder.getChildren().get(callIndex);
        assertThat(constructRow(callAfterUndo)).containsExactly("call_to_setting", "[setup]", "arg1", "arg2",
                "# comment");
        assertThat(callAfterUndo.getLinkedElement().getModelType())
                .isEqualTo(modelTypes.get(CommonModelTypes.EXECUTABLE_ROW, executablesHolder.getClass()));

        verify(eventBroker, times(1)).send(eq(RobotModelEvents.ROBOT_KEYWORD_CALL_CONVERTED), eq(ImmutableMap
                .of(IEventBroker.DATA, executablesHolder, RobotModelEvents.ADDITIONAL_DATA, callAfterNameChange)));
        verify(eventBroker, times(1)).send(eq(RobotModelEvents.ROBOT_KEYWORD_CALL_CONVERTED), eq(ImmutableMap
                .of(IEventBroker.DATA, executablesHolder, RobotModelEvents.ADDITIONAL_DATA, callAfterUndo)));
        verifyNoMoreInteractions(eventBroker);
    }

    @Theory
    public void settingChangesNameProperlyToCall(final RobotCodeHoldingElement<?> executablesHolder) {
        // only perform this test for keyword/case without special purpose
        assumeTrue(!executablesHolder.getName().contains("_"));

        final int callIndex = 0;
        final int callIndexAfterCommand = 0;

        final RobotDefinitionSetting setting = (RobotDefinitionSetting) executablesHolder.getChildren().get(callIndex);
        assertThat(setting.getName()).isEqualTo("tags");

        final SetKeywordCallNameCommand command = ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new SetKeywordCallNameCommand(setting, "new_call"));
        command.execute();

        final RobotKeywordCall callAfterNameChange = executablesHolder.getChildren().get(callIndexAfterCommand);
        assertThat(constructRow(callAfterNameChange)).containsExactly("new_call", "tag1", "tag2", "tag3", "# comment");
        assertThat(callAfterNameChange.getLinkedElement().getModelType())
                .isEqualTo(modelTypes.get(CommonModelTypes.EXECUTABLE_ROW, executablesHolder.getClass()));

        for (final EditorCommand undo : command.getUndoCommands()) {
            undo.execute();
        }

        final RobotKeywordCall callAfterUndo = executablesHolder.getChildren().get(callIndex);
        assertThat(constructRow(callAfterUndo)).containsExactly("tags", "tag1", "tag2", "tag3", "# comment");
        assertThat(callAfterUndo.getLinkedElement().getModelType())
                .isEqualTo(modelTypes.get(CommonModelTypes.TAGS, executablesHolder.getClass()));

        verify(eventBroker, times(1)).send(eq(RobotModelEvents.ROBOT_KEYWORD_CALL_CONVERTED), eq(ImmutableMap
                .of(IEventBroker.DATA, executablesHolder, RobotModelEvents.ADDITIONAL_DATA, callAfterNameChange)));
        verify(eventBroker, times(1)).send(eq(RobotModelEvents.ROBOT_KEYWORD_CALL_CONVERTED), eq(ImmutableMap
                .of(IEventBroker.DATA, executablesHolder, RobotModelEvents.ADDITIONAL_DATA, callAfterUndo)));
        verifyNoMoreInteractions(eventBroker);
    }

    @Theory
    public void settingChangesNameProperlyToDifferentSetting(
            final RobotCodeHoldingElement<?> executablesHolder) {
        // only perform this test for keyword/case without special purpose
        assumeTrue(!executablesHolder.getName().contains("_"));

        final int callIndex = 0;
        final int callIndexAfterCommand = 0;

        final RobotDefinitionSetting setting = (RobotDefinitionSetting) executablesHolder.getChildren().get(callIndex);
        assertThat(setting.getName()).isEqualTo("tags");

        final SetKeywordCallNameCommand command = ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new SetKeywordCallNameCommand(setting, "[Arguments]"));
        command.execute();

        final RobotKeywordCall callAfterNameChange = executablesHolder.getChildren().get(callIndexAfterCommand);
        assertThat(constructRow(callAfterNameChange)).containsExactly("Arguments", "tag1", "tag2", "tag3", "# comment");
        assertThat(callAfterNameChange.getLinkedElement().getModelType())
                .isEqualTo(modelTypes.get(CommonModelTypes.ARGUMENTS, executablesHolder.getClass()));

        for (final EditorCommand undo : command.getUndoCommands()) {
            undo.execute();
        }

        final RobotKeywordCall callAfterUndo = executablesHolder.getChildren().get(callIndex);
        assertThat(constructRow(callAfterUndo)).containsExactly("tags", "tag1", "tag2", "tag3", "# comment");
        assertThat(callAfterUndo.getLinkedElement().getModelType())
                .isEqualTo(modelTypes.get(CommonModelTypes.TAGS, executablesHolder.getClass()));

        verify(eventBroker, times(1)).send(eq(RobotModelEvents.ROBOT_KEYWORD_CALL_CONVERTED), eq(ImmutableMap
                .of(IEventBroker.DATA, executablesHolder, RobotModelEvents.ADDITIONAL_DATA, callAfterNameChange)));
        verify(eventBroker, times(1)).send(eq(RobotModelEvents.ROBOT_KEYWORD_CALL_CONVERTED), eq(ImmutableMap
                .of(IEventBroker.DATA, executablesHolder, RobotModelEvents.ADDITIONAL_DATA, callAfterUndo)));
        verifyNoMoreInteractions(eventBroker);
    }

    @Theory
    public void callChangesNameToFirstArgumentIntoOtherCall_whenNameIsNull(
            final RobotCodeHoldingElement<?> executablesHolder) {
        // only perform this test for keyword/case without special purpose
        assumeTrue(!executablesHolder.getName().contains("_"));

        final int callIndex = 2;
        final int callIndexAfterCommand = 2;

        final RobotKeywordCall call = executablesHolder.getChildren().get(callIndex);
        assertThat(call.getName()).isEqualTo("call");

        final SetKeywordCallNameCommand command = ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new SetKeywordCallNameCommand(call, null));
        command.execute();

        final RobotKeywordCall callAfterNameChange = executablesHolder.getChildren().get(callIndexAfterCommand);
        assertThat(constructRow(callAfterNameChange)).containsExactly("arg1", "arg2", "arg3", "# comment");
        assertThat(callAfterNameChange.getLinkedElement().getModelType())
                .isEqualTo(modelTypes.get(CommonModelTypes.EXECUTABLE_ROW, executablesHolder.getClass()));

        for (final EditorCommand undo : command.getUndoCommands()) {
            undo.execute();
        }

        final RobotKeywordCall callAfterUndo = executablesHolder.getChildren().get(callIndex);
        assertThat(constructRow(callAfterUndo)).containsExactly("call", "arg1", "arg2", "arg3", "# comment");
        assertThat(callAfterNameChange.getLinkedElement().getModelType())
                .isEqualTo(modelTypes.get(CommonModelTypes.EXECUTABLE_ROW, executablesHolder.getClass()));

        verify(eventBroker, times(2)).send(RobotModelEvents.ROBOT_KEYWORD_CALL_NAME_CHANGE, call);
        verify(eventBroker, times(2)).send(RobotModelEvents.ROBOT_KEYWORD_CALL_ARGUMENT_CHANGE, call);
        verifyNoMoreInteractions(eventBroker);
    }

    @Theory
    public void callChangesNameToFirstArgumentIntoSetting_whenNameIsNull(
            final RobotCodeHoldingElement<?> executablesHolder) {
        // only perform this test for keyword/case without special purpose
        assumeTrue(!executablesHolder.getName().contains("_"));

        final int callIndex = 1;
        final int callIndexAfterCommand = 1;

        final RobotKeywordCall call = executablesHolder.getChildren().get(callIndex);
        assertThat(call.getName()).isEqualTo("call_to_setting");

        final SetKeywordCallNameCommand command = ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new SetKeywordCallNameCommand(call, null));
        command.execute();

        final RobotKeywordCall callAfterNameChange = executablesHolder.getChildren().get(callIndexAfterCommand);
        assertThat(constructRow(callAfterNameChange)).containsExactly("setup", "arg1", "arg2", "# comment");
        assertThat(callAfterNameChange.getLinkedElement().getModelType())
                .isEqualTo(modelTypes.get(CommonModelTypes.SETUP, executablesHolder.getClass()));

        for (final EditorCommand undo : command.getUndoCommands()) {
            undo.execute();
        }

        final RobotKeywordCall callAfterUndo = executablesHolder.getChildren().get(callIndex);
        assertThat(constructRow(callAfterUndo)).containsExactly("call_to_setting", "[setup]", "arg1", "arg2",
                "# comment");
        assertThat(callAfterUndo.getLinkedElement().getModelType())
                .isEqualTo(modelTypes.get(CommonModelTypes.EXECUTABLE_ROW, executablesHolder.getClass()));

        verify(eventBroker, times(1)).send(eq(RobotModelEvents.ROBOT_KEYWORD_CALL_CONVERTED), eq(ImmutableMap
                .of(IEventBroker.DATA, executablesHolder, RobotModelEvents.ADDITIONAL_DATA, callAfterNameChange)));
        verify(eventBroker, times(1)).send(eq(RobotModelEvents.ROBOT_KEYWORD_CALL_CONVERTED), eq(ImmutableMap
                .of(IEventBroker.DATA, executablesHolder, RobotModelEvents.ADDITIONAL_DATA, callAfterUndo)));
        verify(eventBroker, times(2)).send(RobotModelEvents.ROBOT_KEYWORD_CALL_ARGUMENT_CHANGE, callAfterNameChange);
        verifyNoMoreInteractions(eventBroker);
    }

    @Theory
    public void settingChangesNameToFirstArgumentIntoCall_whenNameIsNull(
            final RobotCodeHoldingElement<?> executablesHolder) {
        // only perform this test for keyword/case without special purpose
        assumeTrue(!executablesHolder.getName().contains("_"));

        final int callIndex = 0;
        final int callIndexAfterCommand = 0;

        final RobotKeywordCall call = executablesHolder.getChildren().get(callIndex);
        assertThat(call.getName()).isEqualTo("tags");

        final SetKeywordCallNameCommand command = ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new SetKeywordCallNameCommand(call, null));
        command.execute();

        final RobotKeywordCall callAfterNameChange = executablesHolder.getChildren().get(callIndexAfterCommand);
        assertThat(constructRow(callAfterNameChange)).containsExactly("tag1", "tag2", "tag3", "# comment");
        assertThat(callAfterNameChange.getLinkedElement().getModelType())
                .isEqualTo(modelTypes.get(CommonModelTypes.EXECUTABLE_ROW, executablesHolder.getClass()));

        for (final EditorCommand undo : command.getUndoCommands()) {
            undo.execute();
        }

        final RobotKeywordCall callAfterUndo = executablesHolder.getChildren().get(callIndex);
        assertThat(constructRow(callAfterUndo)).containsExactly("tags", "tag1", "tag2", "tag3",
                "# comment");
        assertThat(callAfterUndo.getLinkedElement().getModelType())
                .isEqualTo(modelTypes.get(CommonModelTypes.TAGS, executablesHolder.getClass()));

        verify(eventBroker, times(1)).send(eq(RobotModelEvents.ROBOT_KEYWORD_CALL_CONVERTED), eq(ImmutableMap
                .of(IEventBroker.DATA, executablesHolder, RobotModelEvents.ADDITIONAL_DATA, callAfterNameChange)));
        verify(eventBroker, times(1)).send(eq(RobotModelEvents.ROBOT_KEYWORD_CALL_CONVERTED), eq(ImmutableMap
                .of(IEventBroker.DATA, executablesHolder, RobotModelEvents.ADDITIONAL_DATA, callAfterUndo)));
        verify(eventBroker, times(2)).send(RobotModelEvents.ROBOT_KEYWORD_CALL_ARGUMENT_CHANGE, callAfterNameChange);
        verifyNoMoreInteractions(eventBroker);
    }

    @Theory
    public void settingChangesNameToFirstArgumentIntoOtherSetting_whenNameIsNull(
            final RobotCodeHoldingElement<?> executablesHolder) {
        // only perform this test for specific keyword/case
        assumeTrue(executablesHolder.getName().contains("setting_to_other_setting"));

        final int callIndex = 0;
        final int callIndexAfterCommand = 0;

        final RobotKeywordCall call = executablesHolder.getChildren().get(callIndex);
        assertThat(call.getName()).isEqualTo("tags");

        final SetKeywordCallNameCommand command = ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new SetKeywordCallNameCommand(call, null));
        command.execute();

        final RobotKeywordCall callAfterNameChange = executablesHolder.getChildren().get(callIndexAfterCommand);
        assertThat(constructRow(callAfterNameChange)).containsExactly("setup", "tag1", "tag2", "# comment");
        assertThat(callAfterNameChange.getLinkedElement().getModelType())
                .isEqualTo(modelTypes.get(CommonModelTypes.SETUP, executablesHolder.getClass()));

        for (final EditorCommand undo : command.getUndoCommands()) {
            undo.execute();
        }

        final RobotKeywordCall callAfterUndo = executablesHolder.getChildren().get(callIndex);
        assertThat(constructRow(callAfterUndo)).containsExactly("tags", "[setup]", "tag1", "tag2", "# comment");
        assertThat(callAfterUndo.getLinkedElement().getModelType())
                .isEqualTo(modelTypes.get(CommonModelTypes.TAGS, executablesHolder.getClass()));

        verify(eventBroker, times(1)).send(eq(RobotModelEvents.ROBOT_KEYWORD_CALL_CONVERTED), eq(ImmutableMap
                .of(IEventBroker.DATA, executablesHolder, RobotModelEvents.ADDITIONAL_DATA, callAfterNameChange)));
        verify(eventBroker, times(1)).send(eq(RobotModelEvents.ROBOT_KEYWORD_CALL_CONVERTED), eq(ImmutableMap
                .of(IEventBroker.DATA, executablesHolder, RobotModelEvents.ADDITIONAL_DATA, callAfterUndo)));
        verify(eventBroker, times(2)).send(RobotModelEvents.ROBOT_KEYWORD_CALL_ARGUMENT_CHANGE, callAfterNameChange);
        verifyNoMoreInteractions(eventBroker);
    }

    @Theory
    public void settingChangesNameToFirstArgumentIntoSameSetting_whenNameIsNull(
            final RobotCodeHoldingElement<?> executablesHolder) {
        // only perform this test for specific keyword/case
        assumeTrue(executablesHolder.getName().contains("setting_to_same_setting"));

        final int callIndex = 0;
        final int callIndexAfterCommand = 0;

        final RobotKeywordCall setting = executablesHolder.getChildren().get(callIndex);
        assertThat(setting.getName()).isEqualTo("tags");

        final SetKeywordCallNameCommand command = ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new SetKeywordCallNameCommand(setting, null));
        command.execute();

        final RobotKeywordCall callAfterNameChange = executablesHolder.getChildren().get(callIndexAfterCommand);
        assertThat(constructRow(callAfterNameChange)).containsExactly("tags", "tag1", "tag2", "# comment");
        assertThat(callAfterNameChange.getLinkedElement().getModelType())
                .isEqualTo(modelTypes.get(CommonModelTypes.TAGS, executablesHolder.getClass()));

        for (final EditorCommand undo : command.getUndoCommands()) {
            undo.execute();
        }

        final RobotKeywordCall callAfterUndo = executablesHolder.getChildren().get(callIndex);
        assertThat(constructRow(callAfterUndo)).containsExactly("tags", "[tags]", "tag1", "tag2", "# comment");
        assertThat(callAfterUndo.getLinkedElement().getModelType())
                .isEqualTo(modelTypes.get(CommonModelTypes.TAGS, executablesHolder.getClass()));

        verify(eventBroker, times(2)).send(RobotModelEvents.ROBOT_KEYWORD_CALL_NAME_CHANGE, callAfterNameChange);
        verify(eventBroker, times(2)).send(RobotModelEvents.ROBOT_KEYWORD_CALL_ARGUMENT_CHANGE, callAfterNameChange);
        verifyNoMoreInteractions(eventBroker);
    }

    @Theory
    public void settingChangesNameConvertsToCallAndIsNotMoved_whenThereAreOtherSettingsAfterIt(
            final RobotCodeHoldingElement<?> executablesHolder) {
        // only perform this test for specific keyword/case
        assumeTrue(executablesHolder.getName().contains("_does_not_move"));

        final int callIndex = 1;
        final int callIndexAfterCommand = 1;

        final RobotKeywordCall setting = executablesHolder.getChildren().get(callIndex);
        assertThat(setting.getName()).isEqualTo("teardown");

        final SetKeywordCallNameCommand command = ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new SetKeywordCallNameCommand(setting, "call"));
        command.execute();

        assertThat(actionNames(executablesHolder)).containsExactly("tags", "call", "unknown", "call1", "call2",
                "call3");

        final RobotKeywordCall callAfterNameChange = executablesHolder.getChildren().get(callIndexAfterCommand);
        assertThat(constructRow(callAfterNameChange)).containsExactly("call", "arg1", "arg2", "# comment");
        assertThat(callAfterNameChange.getLinkedElement().getModelType())
                .isEqualTo(modelTypes.get(CommonModelTypes.EXECUTABLE_ROW, executablesHolder.getClass()));

        for (final EditorCommand undo : command.getUndoCommands()) {
            undo.execute();
        }

        assertThat(actionNames(executablesHolder)).containsExactly("tags", "teardown", "unknown", "call1", "call2",
                "call3");

        final RobotKeywordCall callAfterUndo = executablesHolder.getChildren().get(callIndex);
        assertThat(constructRow(callAfterUndo)).containsExactly("teardown", "arg1", "arg2", "# comment");
        assertThat(callAfterUndo.getLinkedElement().getModelType())
                .isEqualTo(modelTypes.get(CommonModelTypes.TEARDOWN, executablesHolder.getClass()));


        verify(eventBroker, times(1)).send(eq(RobotModelEvents.ROBOT_KEYWORD_CALL_CONVERTED), eq(ImmutableMap
                .of(IEventBroker.DATA, executablesHolder, RobotModelEvents.ADDITIONAL_DATA, callAfterNameChange)));
        verify(eventBroker, times(1)).send(eq(RobotModelEvents.ROBOT_KEYWORD_CALL_CONVERTED), eq(ImmutableMap
                .of(IEventBroker.DATA, executablesHolder, RobotModelEvents.ADDITIONAL_DATA, callAfterUndo)));
        verifyNoMoreInteractions(eventBroker);
    }

    @Theory
    public void callChangesNameConvertsAndIsNotMoved_whenThereAreOtherCallsBeforeIt(
            final RobotCodeHoldingElement<?> executablesHolder) {
        // only perform this test for specific keyword/case
        assumeTrue(executablesHolder.getName().contains("_does_not_move"));

        final int callIndex = 5;
        final int callIndexAfterCommand = 5;

        final RobotKeywordCall setting = executablesHolder.getChildren().get(callIndex);
        assertThat(setting.getName()).isEqualTo("call3");

        final SetKeywordCallNameCommand command = ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new SetKeywordCallNameCommand(setting, "[setup]"));
        command.execute();

        assertThat(actionNames(executablesHolder)).containsExactly("tags", "teardown", "unknown", "call1", "call2",
                "setup");

        final RobotKeywordCall callAfterNameChange = executablesHolder.getChildren().get(callIndexAfterCommand);
        assertThat(constructRow(callAfterNameChange)).containsExactly("setup", "arg1", "arg2", "# comment");
        assertThat(callAfterNameChange.getLinkedElement().getModelType())
                .isEqualTo(modelTypes.get(CommonModelTypes.SETUP, executablesHolder.getClass()));

        for (final EditorCommand undo : command.getUndoCommands()) {
            undo.execute();
        }

        assertThat(actionNames(executablesHolder)).containsExactly("tags", "teardown", "unknown", "call1", "call2",
                "call3");

        final RobotKeywordCall callAfterUndo = executablesHolder.getChildren().get(callIndex);
        assertThat(constructRow(callAfterUndo)).containsExactly("call3", "arg1", "arg2", "# comment");
        assertThat(callAfterUndo.getLinkedElement().getModelType())
                .isEqualTo(modelTypes.get(CommonModelTypes.EXECUTABLE_ROW, executablesHolder.getClass()));

        verify(eventBroker, times(1)).send(eq(RobotModelEvents.ROBOT_KEYWORD_CALL_CONVERTED), eq(ImmutableMap
                .of(IEventBroker.DATA, executablesHolder, RobotModelEvents.ADDITIONAL_DATA, callAfterNameChange)));
        verify(eventBroker, times(1)).send(eq(RobotModelEvents.ROBOT_KEYWORD_CALL_CONVERTED), eq(ImmutableMap
                .of(IEventBroker.DATA, executablesHolder, RobotModelEvents.ADDITIONAL_DATA, callAfterUndo)));
        verifyNoMoreInteractions(eventBroker);
    }

    @Theory
    public void callChangesNameProperlyToSettingAndThenReturnsToPreviousState(
            final RobotCodeHoldingElement<?> executablesHolder) {
        // only perform this test for keyword/case without special purpose
        assumeTrue(!executablesHolder.getName().contains("_"));

        final int callIndex = 1;
        final int callIndexAfterCommand = 1;

        final RobotKeywordCall call = executablesHolder.getChildren().get(callIndex);
        assertThat(call.getName()).isEqualTo("call_to_setting");

        final SetKeywordCallNameCommand command = ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new SetKeywordCallNameCommand(call, "[Tags]"));
        command.execute();

        final RobotKeywordCall callAfterNameChange = executablesHolder.getChildren().get(callIndexAfterCommand);
        assertThat(constructRow(callAfterNameChange)).containsExactly("Tags", "[setup]", "arg1", "arg2", "# comment");
        assertThat(callAfterNameChange.getLinkedElement().getModelType())
                .isEqualTo(modelTypes.get(CommonModelTypes.TAGS, executablesHolder.getClass()));

        List<EditorCommand> undoCommands = command.getUndoCommands();
        for (final EditorCommand undoCommand : undoCommands) {
            undoCommand.execute();
        }

        RobotKeywordCall callAfterUndo = executablesHolder.getChildren().get(callIndex);
        assertThat(constructRow(callAfterUndo)).containsExactly("call_to_setting", "[setup]", "arg1", "arg2",
                "# comment");
        assertThat(callAfterUndo.getLinkedElement().getModelType())
                .isEqualTo(modelTypes.get(CommonModelTypes.EXECUTABLE_ROW, executablesHolder.getClass()));

        final List<EditorCommand> redoCommands = new ArrayList<>();
        for (final EditorCommand undoCommand : undoCommands) {
            redoCommands.addAll(0, undoCommand.getUndoCommands());
        }
        for (final EditorCommand redoCommand : redoCommands) {
            redoCommand.execute();
        }

        final RobotKeywordCall callAfterRedo = executablesHolder.getChildren().get(callIndexAfterCommand);
        assertThat(constructRow(callAfterRedo)).containsExactly("Tags", "[setup]", "arg1", "arg2", "# comment");
        assertThat(callAfterRedo.getLinkedElement().getModelType())
                .isEqualTo(modelTypes.get(CommonModelTypes.TAGS, executablesHolder.getClass()));

        undoCommands = new ArrayList<>();
        for (final EditorCommand redoCommand : redoCommands) {
            undoCommands.addAll(0, redoCommand.getUndoCommands());
        }
        for (final EditorCommand undoCommand : undoCommands) {
            undoCommand.execute();
        }

        callAfterUndo = executablesHolder.getChildren().get(callIndex);
        assertThat(constructRow(callAfterUndo)).containsExactly("call_to_setting", "[setup]", "arg1", "arg2",
                "# comment");
        assertThat(callAfterUndo.getLinkedElement().getModelType())
                .isEqualTo(modelTypes.get(CommonModelTypes.EXECUTABLE_ROW, executablesHolder.getClass()));

        verify(eventBroker, times(2)).send(eq(RobotModelEvents.ROBOT_KEYWORD_CALL_CONVERTED), eq(ImmutableMap
                .of(IEventBroker.DATA, executablesHolder, RobotModelEvents.ADDITIONAL_DATA, callAfterNameChange)));
        verify(eventBroker, times(2)).send(eq(RobotModelEvents.ROBOT_KEYWORD_CALL_CONVERTED), eq(ImmutableMap
                .of(IEventBroker.DATA, executablesHolder, RobotModelEvents.ADDITIONAL_DATA, callAfterUndo)));
        verifyNoMoreInteractions(eventBroker);
    }

    private List<String> actionNames(final RobotCodeHoldingElement<?> executablesHolder) {
        return executablesHolder.getChildren().stream().map(RobotKeywordCall::getName).collect(Collectors.toList());
    }

    private List<String> constructRow(final RobotKeywordCall call) {
        final List<String> row = newArrayList(call.getName());
        row.addAll(call.getArguments());
        row.add(call.getComment());
        return row;
    }

    private static List<RobotCase> createTestCases() {
        final RobotSuiteFile model = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("case")
                .appendLine("  [tags]  tag1  tag2  tag3  # comment")
                .appendLine("  call_to_setting  [setup]  arg1  arg2  # comment")
                .appendLine("  call  arg1  arg2  arg3  # comment")
                .appendLine("case_with_setting_to_same_setting")
                .appendLine("  [tags]  [tags]  tag1  tag2  # comment")
                .appendLine("case_with_setting_to_other_setting")
                .appendLine("  [tags]  [setup]  tag1  tag2  # comment")
                .appendLine("case_does_not_move")
                .appendLine("  [tags]  arg1  arg2  # comment")
                .appendLine("  [teardown]  arg1  arg2  # comment")
                .appendLine("  [unknown]  arg1  arg2  # comment")
                .appendLine("  call1  arg1  arg2  # comment")
                .appendLine("  call2  arg1  arg2  # comment")
                .appendLine("  call3  arg1  arg2  # comment")
                .build();
        return model.findSection(RobotCasesSection.class).get().getChildren();
    }

    private static List<RobotKeywordDefinition> createKeywords() {
        final RobotSuiteFile model = new RobotSuiteFileCreator().appendLine("*** Keywords ***")
                .appendLine("keyword")
                .appendLine("  [tags]  tag1  tag2  tag3  # comment")
                .appendLine("  call_to_setting  [setup]  arg1  arg2  # comment")
                .appendLine("  call  arg1  arg2  arg3  # comment")
                .appendLine("keyword_with_setting_to_same_setting")
                .appendLine("  [tags]  [tags]  tag1  tag2  # comment")
                .appendLine("keyword_with_setting_to_other_setting")
                .appendLine("  [tags]  [setup]  tag1  tag2  # comment")
                .appendLine("keyword_does_not_move")
                .appendLine("  [tags]  arg1  arg2  # comment")
                .appendLine("  [teardown]  arg1  arg2  # comment")
                .appendLine("  [unknown]  arg1  arg2  # comment")
                .appendLine("  call1  arg1  arg2  # comment")
                .appendLine("  call2  arg1  arg2  # comment")
                .appendLine("  call3  arg1  arg2  # comment")
                .build();
        return model.findSection(RobotKeywordsSection.class).get().getChildren();
    }

    private enum CommonModelTypes {
        EXECUTABLE_ROW,
        TAGS,
        ARGUMENTS,
        SETUP,
        TEARDOWN,
        UNKNOWN
    }
}
