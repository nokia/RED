/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd.keywords;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;
import org.robotframework.ide.eclipse.main.plugin.mockeclipse.ContextInjector;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.RobotSuiteFileCreator;
import org.robotframework.ide.eclipse.main.plugin.model.RobotDefinitionSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;

import com.google.common.base.Joiner;
import com.google.common.collect.ContiguousSet;
import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Range;

@RunWith(Theories.class)
public class SetKeywordDefinitionArgumentCommandTest {

    private IEventBroker eventBroker;

    @Before
    public void beforeTest() {
        eventBroker = mock(IEventBroker.class);
    }

    @DataPoints
    public static int[] indexes = new int[] { 0, 1, 2, 3, 4, 5, 10, 50, 100 };

    @Theory
    public void nothingHappens_whenThereIsNoArgumentsSettingAndTryingToSetNullAtSomeIndex(final int index) {
        final RobotKeywordDefinition def = createKeywordWithoutArguments();

        final SetKeywordDefinitionArgumentCommand command = new SetKeywordDefinitionArgumentCommand(def, index, null);
        command.setEventBroker(eventBroker);

        command.execute();
        assertThat(def.getArgumentsSetting()).isNull();

        for (final EditorCommand undo : command.getUndoCommands()) {
            undo.execute();
        }
        assertThat(def.getArgumentsSetting()).isNull();

        verifyZeroInteractions(eventBroker);
    }

    @Theory
    public void nothingHappens_whenThereIsNoArgumentsSettingAndTryingToSetEmptyAtSomeIndex(final int index) {
        final RobotKeywordDefinition def = createKeywordWithoutArguments();

        final SetKeywordDefinitionArgumentCommand command = new SetKeywordDefinitionArgumentCommand(def, index, "");
        command.setEventBroker(eventBroker);

        command.execute();
        assertThat(def.getArgumentsSetting()).isNull();

        for (final EditorCommand undo : command.getUndoCommands()) {
            undo.execute();
        }
        assertThat(def.getArgumentsSetting()).isNull();

        verifyZeroInteractions(eventBroker);
    }

    @Theory
    public void settingIsCreatedAndFilledWithArguments_whenItDidntExistedAndTryingToSetNonEmptyStringAtSomeIndex(
            final int index) {
        final RobotKeywordDefinition def = createKeywordWithoutArguments();

        final SetKeywordDefinitionArgumentCommand command = new SetKeywordDefinitionArgumentCommand(def, index, "arg");
        command.setEventBroker(eventBroker);

        final List<String> expectedArgs = index <= 0 ? new ArrayList<String>()
                : newArrayList(Collections.nCopies(index, "\\"));
        expectedArgs.add("arg");

        command.execute();
        final RobotDefinitionSetting setting = def.getArgumentsSetting();
        assertThat(setting).isNotNull();
        assertThat(setting.getArguments()).containsExactlyElementsOf(expectedArgs);

        for (final EditorCommand undo : command.getUndoCommands()) {
            undo.execute();
        }
        assertThat(def.getArgumentsSetting()).isNull();

        verify(eventBroker).send(eq(RobotModelEvents.ROBOT_KEYWORD_CALL_ADDED),
                eq(ImmutableMap.of(IEventBroker.DATA, def, RobotModelEvents.ADDITIONAL_DATA, setting)));
        verify(eventBroker).send(eq(RobotModelEvents.ROBOT_KEYWORD_CALL_REMOVED),
                eq(ImmutableMap.of(IEventBroker.DATA, def, RobotModelEvents.ADDITIONAL_DATA, setting)));
        verifyNoMoreInteractions(eventBroker);
    }

    @Test
    public void settingIsRemoved_whenItExistedAndLastArgumentIsRemoved() {
        final RobotKeywordDefinition def = createKeywordWithArguments(1);
        final RobotDefinitionSetting setting = def.getArgumentsSetting();

        final SetKeywordDefinitionArgumentCommand command = new SetKeywordDefinitionArgumentCommand(def, 0, null);
        command.setEventBroker(eventBroker);

        command.execute();
        assertThat(def.getArgumentsSetting()).isNull();

        for (final EditorCommand undo : command.getUndoCommands()) {
            undo.execute();
        }
        assertThat(def.getArgumentsSetting()).isNotNull();
        assertThat(def.getArgumentsSetting().getArguments()).containsExactly("1");

        verify(eventBroker).send(eq(RobotModelEvents.ROBOT_KEYWORD_CALL_REMOVED),
                eq(ImmutableMap.of(IEventBroker.DATA, def, RobotModelEvents.ADDITIONAL_DATA, setting)));
        verify(eventBroker).send(eq(RobotModelEvents.ROBOT_KEYWORD_CALL_ADDED), eq(
                ImmutableMap.of(IEventBroker.DATA, def, RobotModelEvents.ADDITIONAL_DATA, def.getArgumentsSetting())));
        verifyNoMoreInteractions(eventBroker);
    }

    @Test
    public void testUndoRedoOnFirstArgumentInKeywordDefinition() {
        final RobotKeywordDefinition def = createKeywordWithArguments(3);
        final RobotDefinitionSetting args = def.getArgumentsSetting();
        final SetKeywordDefinitionArgumentCommand command = new SetKeywordDefinitionArgumentCommand(def, 0, null);

        ContextInjector.prepareContext().inWhich(eventBroker).isInjectedInto(command).execute();

        assertThat(def.getArgumentsSetting().getArguments()).containsExactly("2", "3");

        final EditorCommand undoCommand = command.getUndoCommands().get(0);
        undoCommand.execute();
        assertThat(def.getArgumentsSetting().getArguments()).containsExactly("1", "2", "3");

        final EditorCommand redoCommand = undoCommand.getUndoCommands().get(0);
        redoCommand.execute();
        assertThat(def.getArgumentsSetting().getArguments()).containsExactly("2", "3");

        verify(eventBroker, times(3)).send(RobotModelEvents.ROBOT_KEYWORD_CALL_ARGUMENT_CHANGE, args);
        verifyNoMoreInteractions(eventBroker);
    }

    private static RobotKeywordDefinition createKeywordWithArguments(final int numberOfArgs) {
        final String arguments;
        if (numberOfArgs <= 0) {
            arguments = "";
        } else {
            final ImmutableList<Integer> args = ContiguousSet
                    .create(Range.closed(1, numberOfArgs), DiscreteDomain.integers()).asList();
            arguments = "  " + Joiner.on("  ").join(args);
        }

        final RobotSuiteFile model = new RobotSuiteFileCreator().appendLine("*** Keywords ***")
                .appendLine("keyword")
                .appendLine("  [Arguments]" + arguments)
                .appendLine("  call  1  # comment")
                .build();
        return model.findSection(RobotKeywordsSection.class).get().getChildren().get(0);
    }

    private static RobotKeywordDefinition createKeywordWithoutArguments() {
        final RobotSuiteFile model = new RobotSuiteFileCreator().appendLine("*** Keywords ***")
                .appendLine("keyword")
                .appendLine("  call  1  # comment")
                .build();
        return model.findSection(RobotKeywordsSection.class).get().getChildren().get(0);
    }
}
