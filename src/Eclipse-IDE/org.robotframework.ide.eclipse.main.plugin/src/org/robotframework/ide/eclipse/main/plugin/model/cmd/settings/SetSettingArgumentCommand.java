/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd.settings;

import java.util.ArrayList;
import java.util.List;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.presenter.update.SettingTableModelUpdater;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;

public class SetSettingArgumentCommand extends EditorCommand {

    private final RobotSetting setting;
    private final int index;
    private final String value;

    private boolean shouldReplaceValue;

    private String previousValue;

    public SetSettingArgumentCommand(final RobotSetting setting, final int index, final String value) {
        this(setting, index, value, true);
    }

    private SetSettingArgumentCommand(final RobotSetting setting, final int index, final String value,
            final boolean shouldReplaceValue) {
        this.setting = setting;
        this.index = index;
        this.value = value;
        this.shouldReplaceValue = shouldReplaceValue;
    }

    @Override
    public void execute() throws CommandExecutionException {
        final List<String> oldArguments = setting.getArguments();
        final List<String> arguments = prepareArgumentsList(setting, value, index);

        if (!arguments.equals(oldArguments) || (value == null && "\\".equals(oldArguments.get(index)))) {
            updateModelElement(arguments);
            setting.resetStored();

            eventBroker.send(RobotModelEvents.ROBOT_KEYWORD_CALL_ARGUMENT_CHANGE, setting);
        }
    }

    private List<String> prepareArgumentsList(final RobotKeywordCall call, final String value, final int index) {
        final List<String> arguments = createArgumentsList(call, index);

        previousValue = index >= 0 && index < arguments.size() ? arguments.get(index) : value;

        fillArgumentsList(value, index, arguments, shouldReplaceValue);

        checkIfPreviousCommandWasAddingNewValue();
        checkIfUndoCommandShouldAddArgument(arguments.get(index));

        return arguments;
    }

    public static List<String> createArgumentsList(final RobotKeywordCall call, final int index) {
        final List<String> arguments = call == null ? new ArrayList<>() : new ArrayList<>(call.getArguments());
        for (int i = arguments.size(); i <= index; i++) {
            arguments.add("\\");
        }
        return arguments;
    }

    public static void fillArgumentsList(final String value, final int index, final List<String> arguments,
            final boolean shouldReplaceValue) {
        final String newValue = value == null || value.trim().isEmpty() ? "\\" : value;
        if (shouldReplaceValue) {
            arguments.set(index, newValue);
        } else {
            arguments.add(index, newValue);
        }
        for (int i = arguments.size() - 1; i >= 0; i--) {
            if (!arguments.get(i).equals("\\")) {
                break;
            }
            arguments.set(i, null);
        }
    }

    private void checkIfPreviousCommandWasAddingNewValue() {
        if (!shouldReplaceValue) {
            previousValue = null; // when new value was not replaced but added by undo command, then
                                  // redo command should remove this value
        }
    }

    private void checkIfUndoCommandShouldAddArgument(final String currentArgValue) {
        if (currentArgValue != null && currentArgValue.equals("\\") && value == null) {
            shouldReplaceValue = false; // when arg is deleted not on last position, undo command
                                        // will add new arg on this position and shifts other args
                                        // to the right
        } else {
            shouldReplaceValue = true; // reset the flag for future undo commands
        }
    }

    private void updateModelElement(final List<String> arguments) {
        final AModelElement<?> linkedElement = setting.getLinkedElement();
        final SettingTableModelUpdater updater = new SettingTableModelUpdater();
        if (value != null) {
            for (int i = arguments.size() - 1; i >= 0; i--) {
                updater.update(linkedElement, i, arguments.get(i));
            }
        } else {
            updater.update(linkedElement, index, value);
        }
    }

    @Override
    public List<EditorCommand> getUndoCommands() {
        return newUndoCommands(new SetSettingArgumentCommand(setting, index, previousValue,
                isFirstArgAndShouldAlwaysReplaceValue() ? true : shouldReplaceValue));
    }

    private boolean isFirstArgAndShouldAlwaysReplaceValue() {
        return index == 0 && setting.getLinkedElement().getModelType() != ModelType.DEFAULT_TAGS_SETTING
                && setting.getLinkedElement().getModelType() != ModelType.FORCE_TAGS_SETTING;
    }
}
