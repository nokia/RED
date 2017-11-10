/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assume.assumeTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.util.Collections;
import java.util.List;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;
import org.robotframework.ide.eclipse.main.plugin.mockeclipse.ContextInjector;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.RobotSuiteFileCreator;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCasesSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;

@RunWith(Theories.class)
public class SetKeywordCallArgumentCommand2Test {

    // FIXME : test documentation too

    @DataPoints
    public static int[] indexes = new int[] { 0, 1, 2, 3, 4, 5, 10, 50, 100 };

    @DataPoints
    public static RobotKeywordCall[] codeHolders() {
        final List<RobotKeywordCall> elements = newArrayList();
        for (final RobotCase testCase : createTestCases()) {
            elements.addAll(testCase.getChildren());
        }
        for (final RobotKeywordDefinition keyword : createKeywords()) {
            elements.addAll(keyword.getChildren());
        }
        return elements.toArray(new RobotKeywordCall[0]);
    }

    @Theory
    public void argumentChangesToGivenValue_whenGivenValueIsNonEmpty(final RobotKeywordCall call, final int index) {
        final List<String> arguments = call.getArguments();
        assumeTrue(arguments.size() > 0);
        assumeTrue(index < arguments.size());

        final List<String> expectedArgs = newArrayList(arguments);
        expectedArgs.set(index, "new_arg");

        checkArgumentsChangesProperly(call, "new_arg", index, expectedArgs);
    }

    @Theory
    public void argumentChangesToBackslash_whenGivenValueIsEmptyAndArgumentIsNotLast(final RobotKeywordCall call,
            final int index) {
        final List<String> arguments = call.getArguments();
        assumeTrue(arguments.size() > 1);
        assumeTrue(index < arguments.size() - 1);

        final List<String> expectedArgs = newArrayList(arguments);
        expectedArgs.set(index, "\\");

        if (arguments.equals(expectedArgs)) {
            // it may happen for elements inside case_3 and keyword_3
            checkArgumentsDoesNotChange(call, "", index);
        } else {
            checkArgumentsChangesProperly(call, "", index, expectedArgs);
        }
    }

    @Theory
    public void argumentIsRemoved_whenGivenValueIsEmptyAndArgumentIsLast(final RobotKeywordCall call, final int index) {
        final List<String> arguments = call.getArguments();
        assumeTrue(arguments.size() > 0);
        assumeTrue(index == arguments.size() - 1);

        final List<String> expectedArgs = newArrayList(arguments);
        expectedArgs.remove(index);
        // we expect not needed backslashes to be removed
        for (int i = expectedArgs.size() - 1; i >= 0; i--) {
            if (expectedArgs.get(i).equals("\\")) {
                expectedArgs.remove(i);
            } else {
                break;
            }
        }
        checkArgumentsChangesProperly(call, "", index, expectedArgs);
    }

    @Theory
    public void argumentIsRemoved_whenGivenValueIsNullAndArgumentIsLast(final RobotKeywordCall call, final int index) {
        final List<String> arguments = call.getArguments();
        assumeTrue(arguments.size() > 0);
        assumeTrue(index == arguments.size() - 1);

        final List<String> expectedArgs = newArrayList(arguments);
        expectedArgs.remove(index);
        // we expect not needed backslashes to be removed
        for (int i = expectedArgs.size() - 1; i >= 0; i--) {
            if (expectedArgs.get(i).equals("\\")) {
                expectedArgs.remove(i);
            } else {
                break;
            }
        }
        checkArgumentsChangesProperly(call, null, index, expectedArgs);
    }

    @Theory
    public void argumentIsRemoved_whenGivenValueIsNull(final RobotKeywordCall call, final int index) {
        final List<String> arguments = call.getArguments();
        assumeTrue(arguments.size() > 0);
        assumeTrue(index < arguments.size() - 1);

        final List<String> expectedArgs = newArrayList(arguments);
        expectedArgs.remove(index);

        checkArgumentsChangesProperly(call, null, index, expectedArgs);
    }

    @Theory
    public void nothingChanges_whenTryingToSetEmptyValueOutsideOfArgumentsList(final RobotKeywordCall call,
            final int index) {
        final List<String> arguments = call.getArguments();
        assumeTrue(index >= arguments.size());

        checkArgumentsDoesNotChange(call, "", index);
    }

    @Theory
    public void nothingChanges_whenTryingToSetNullValueOutsideOfArgumentsList(final RobotKeywordCall call,
            final int index) {
        final List<String> arguments = call.getArguments();
        assumeTrue(index >= arguments.size());

        checkArgumentsDoesNotChange(call, null, index);
    }

    @Theory
    public void argumentsAreAugmentedWithBackslashes_whenTryingToSetNonEmptyValueOutsideOfArgumentsList(
            final RobotKeywordCall call, final int index) {
        final List<String> arguments = call.getArguments();
        assumeTrue(index >= arguments.size());

        final List<String> expectedArgs = newArrayList(Collections.nCopies(index + 1, "\\"));
        for (int i = 0; i < arguments.size(); i++) {
            expectedArgs.set(i, arguments.get(i));
        }
        expectedArgs.set(index, "new_arg");

        checkArgumentsChangesProperly(call, "new_arg", index, expectedArgs);
    }

    @Test
    public void argumentsAndNameAreAugmentedWithBackslash_whenTryingToSetNonEmptyValueOutsideOfArgumentsList_andNameIsEmpty() {
        final RobotKeywordCall call = createEmptyKeywordCall();

        final IEventBroker eventBroker = mock(IEventBroker.class);

        assertThat(call.getName()).isEqualTo("");
        assertThat(call.getArguments()).isEmpty();
        assertThat(call.getComment()).isEqualTo("# comment");

        final SetKeywordCallArgumentCommand2 command = ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new SetKeywordCallArgumentCommand2(call, 3, "arg"));
        command.execute();

        assertThat(call.getName()).isEqualTo("\\");
        assertThat(call.getArguments()).containsExactlyElementsOf(newArrayList("\\", "\\", "\\", "arg"));
        assertThat(call.getComment()).isEqualTo("# comment");

        for (final EditorCommand undo : command.getUndoCommands()) {
            undo.execute();
        }

        assertThat(call.getName()).isEqualTo("");
        assertThat(call.getArguments()).isEmpty();
        assertThat(call.getComment()).isEqualTo("# comment");

        verify(eventBroker, times(2)).send(RobotModelEvents.ROBOT_KEYWORD_CALL_ARGUMENT_CHANGE, call);
        verify(eventBroker, times(2)).send(RobotModelEvents.ROBOT_KEYWORD_CALL_NAME_CHANGE, call);
        verifyNoMoreInteractions(eventBroker);
    }

    @Test
    public void nothingChanges_whenTryingToSetEmptyValueOutsideOfArgumentsList_andCallHasEmptyName() {
        checkArgumentsDoesNotChange(createEmptyKeywordCall(), "", 3);
    }

    @Test
    public void nothingChanges_whenTryingToSetNullValueOutsideOfArgumentsList_andCallHasEmptyName() {
        checkArgumentsDoesNotChange(createEmptyKeywordCall(), null, 3);
    }

    private static void checkArgumentsChangesProperly(final RobotKeywordCall call, final String argToSet,
            final int argumentIndex, final List<String> expectedArgs) {
        final IEventBroker eventBroker = mock(IEventBroker.class);

        checkArgumentChanges(eventBroker, call, argToSet, argumentIndex, expectedArgs);

        verify(eventBroker, times(2)).send(RobotModelEvents.ROBOT_KEYWORD_CALL_ARGUMENT_CHANGE, call);
        verifyNoMoreInteractions(eventBroker);
    }

    private static void checkArgumentsDoesNotChange(final RobotKeywordCall call, final String argToSet,
            final int argumentIndex) {
        final IEventBroker eventBroker = mock(IEventBroker.class);

        checkArgumentChanges(eventBroker, call, argToSet, argumentIndex, call.getArguments());

        verifyZeroInteractions(eventBroker);
    }

    private static void checkArgumentChanges(final IEventBroker eventBroker, final RobotKeywordCall call,
            final String argToSet, final int argumentIndex, final List<String> expectedArgs) {
        final String oldName = call.getName();
        final List<String> oldArgs = call.getArguments();
        final String oldComment = call.getComment();

        final SetKeywordCallArgumentCommand2 command = ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new SetKeywordCallArgumentCommand2(call, argumentIndex, argToSet));
        command.execute();

        assertThat(call.getName()).isEqualTo(oldName);
        assertThat(call.getArguments()).containsExactlyElementsOf(expectedArgs);
        assertThat(call.getComment()).isEqualTo(oldComment);

        for (final EditorCommand undo : command.getUndoCommands()) {
            undo.execute();
        }

        assertThat(call.getName()).isEqualTo(oldName);
        assertThat(call.getArguments()).containsExactlyElementsOf(oldArgs);
        assertThat(call.getComment()).isEqualTo(oldComment);
    }

    private RobotKeywordCall createEmptyKeywordCall() {
        final RobotSuiteFile model = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("case_0")
                .appendLine("    # comment")
                .build();
        return model.findSection(RobotCasesSection.class).get().getChildren().get(0).getChildren().get(0);
    }

    private static List<RobotCase> createTestCases() {
        final RobotSuiteFile model = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("case_0")
                .appendLine("  [tags]                       # comment")
                .appendLine("  [setup]                      # comment")
                .appendLine("  [teardown]                   # comment")
                .appendLine("  [timeout]                    # comment")
                .appendLine("  [template]                   # comment")
                .appendLine("  [unknown]                    # comment")
                .appendLine("  call                         # comment")
                .appendLine("case_1")
                .appendLine("  [tags]           arg1        # comment")
                .appendLine("  [setup]          arg1        # comment")
                .appendLine("  [teardown]       arg1        # comment")
                .appendLine("  [timeout]        arg1        # comment")
                .appendLine("  [template]       arg1        # comment")
                .appendLine("  [unknown]        arg1        # comment")
                .appendLine("  call             arg1        # comment")
                .appendLine("case_2")
                .appendLine("  [tags]           arg1  arg2  # comment")
                .appendLine("  [setup]          arg1  arg2  # comment")
                .appendLine("  [teardown]       arg1  arg2  # comment")
                .appendLine("  [timeout]        arg1  arg2  # comment")
                .appendLine("  [template]       arg1  arg2  # comment")
                .appendLine("  [unknown]        arg1  arg2  # comment")
                .appendLine("  call             arg1  arg2  # comment")
                .appendLine("case_3")
                .appendLine("  [tags]           arg1  \\     \\  # comment")
                .appendLine("  [setup]          arg1  \\     \\  # comment")
                .appendLine("  [teardown]       arg1  \\     \\  # comment")
                .appendLine("  [timeout]        arg1  \\     \\  # comment")
                .appendLine("  [template]       arg1  \\     \\  # comment")
                .appendLine("  [unknown]        arg1  \\     \\  # comment")
                .appendLine("  call             arg1  \\     \\  # comment")
                .build();
        return model.findSection(RobotCasesSection.class).get().getChildren();
    }

    private static List<RobotKeywordDefinition> createKeywords() {
        final RobotSuiteFile model = new RobotSuiteFileCreator().appendLine("*** Keywords ***")
                .appendLine("keyword_0")
                .appendLine("  [tags]                       # comment")
                .appendLine("  [arguments]                  # comment")
                .appendLine("  [teardown]                   # comment")
                .appendLine("  [timeout]                    # comment")
                .appendLine("  [return]                     # comment")
                .appendLine("  [unknown]                    # comment")
                .appendLine("  call                         # comment")
                .appendLine("keyword_1")
                .appendLine("  [tags]           arg1        # comment")
                .appendLine("  [arguments]      arg1        # comment")
                .appendLine("  [teardown]       arg1        # comment")
                .appendLine("  [timeout]        arg1        # comment")
                .appendLine("  [return]         arg1        # comment")
                .appendLine("  [unknown]        arg1        # comment")
                .appendLine("  call             arg1        # comment")
                .appendLine("keyword_2")
                .appendLine("  [tags]           arg1  arg2  # comment")
                .appendLine("  [arguments]      arg1  arg2  # comment")
                .appendLine("  [teardown]       arg1  arg2  # comment")
                .appendLine("  [timeout]        arg1  arg2  # comment")
                .appendLine("  [return]         arg1  arg2  # comment")
                .appendLine("  [unknown]        arg1  arg2  # comment")
                .appendLine("  call             arg1  arg2  # comment")
                .appendLine("keyword_3")
                .appendLine("  [tags]           arg1  \\     \\  # comment")
                .appendLine("  [arguments]      arg1  \\     \\  # comment")
                .appendLine("  [teardown]       arg1  \\     \\  # comment")
                .appendLine("  [timeout]        arg1  \\     \\  # comment")
                .appendLine("  [return]         arg1  \\     \\  # comment")
                .appendLine("  [unknown]        arg1  \\     \\  # comment")
                .appendLine("  call             arg1  \\     \\  # comment")
                .build();
        return model.findSection(RobotKeywordsSection.class).get().getChildren();
    }
}
