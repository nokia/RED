/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd;

import static com.google.common.collect.Lists.newArrayList;

import java.util.ArrayList;
import java.util.List;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.presenter.update.IExecutablesTableModelUpdater;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCodeHoldingElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotDefinitionSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;
import org.robotframework.ide.eclipse.main.plugin.views.documentation.DocumentationView;

public class SetKeywordCallArgumentCommand extends EditorCommand {

    protected final RobotKeywordCall keywordCall;
    protected final int index;
    protected final String value;
    protected boolean shouldReplaceValue;
    protected String previousValue;

    public SetKeywordCallArgumentCommand(final RobotKeywordCall keywordCall, final int index, final String value) {
        this(keywordCall, index, value, true);
    }
    
    protected SetKeywordCallArgumentCommand(final RobotKeywordCall keywordCall, final int index, final String value, final boolean shouldReplaceValue) {
        this.keywordCall = keywordCall;
        this.index = index;
        this.value = value;
        this.shouldReplaceValue = shouldReplaceValue;
    }

    @Override
    public void execute() throws CommandExecutionException {
        final boolean isSetting = keywordCall instanceof RobotDefinitionSetting;
        final List<String> oldArguments = keywordCall.getArguments();
        final List<String> arguments = prepareArgumentsList(keywordCall, value, index, isSetting);

        if (!arguments.equals(oldArguments) || (value == null && "\\".equals(oldArguments.get(index)))) {
            updateModelElement(arguments);
            keywordCall.resetStored();

            if (isSetting && ((RobotDefinitionSetting) keywordCall).isDocumentation()) {
                eventBroker.post(DocumentationView.REFRESH_DOC_EVENT_TOPIC, keywordCall);
            }
            eventBroker.send(RobotModelEvents.ROBOT_KEYWORD_CALL_ARGUMENT_CHANGE, keywordCall);
        }
    }

    private List<String> prepareArgumentsList(final RobotKeywordCall call, final String value, final int index, final boolean isSetting) {
        final List<String> arguments = createArgumentsList(call, index);
        
        previousValue = index >= 0 && index < arguments.size() ? arguments.get(index) : value;

        fillArgumentsList(value, index, arguments, shouldReplaceValue);
        
        checkIfPreviousCommandWasAddingNewValue();
        checkIfUndoCommandShouldAddArgument(arguments.get(index), isSetting);

        return arguments;
    }

    public static List<String> createArgumentsList(final RobotKeywordCall call, final int index) {
        final List<String> arguments = call == null ? new ArrayList<String>() : newArrayList(call.getArguments());
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
        if(!shouldReplaceValue) {
            previousValue = null; // when new value was not replaced but added by undo command, then redo command should remove this value
        }
    }
    
    private void checkIfUndoCommandShouldAddArgument(final String currentArgValue, final boolean isSetting) {
        if(currentArgValue != null && currentArgValue.equals("\\") && value == null && !isFirstArgumentInKeywordBasedSetting(isSetting)) {
            shouldReplaceValue = false; // when arg is deleted not on last position, undo command will add new arg on this position and shifts other args to the right
        } else {
            shouldReplaceValue = true; // reset the flag for future undo commands
        }
    }
    
    private boolean isFirstArgumentInKeywordBasedSetting(final boolean isSetting) {
        if (isSetting && ((RobotDefinitionSetting) keywordCall).isKeywordBased()) {
            return index == 0;
        }
        return false;
    }

    protected void updateModelElement(final List<String> arguments) {
        final RobotCodeHoldingElement<?> parent = (RobotCodeHoldingElement<?>) keywordCall.getParent();
        final IExecutablesTableModelUpdater<?> updater = parent.getModelUpdater();

        final AModelElement<?> linkedElement = keywordCall.getLinkedElement();
        if (value != null) {
            for (int i = arguments.size() - 1; i >= 0; i--) {
                updater.updateArgument(linkedElement, i, arguments.get(i));
            }
        } else {
            updater.updateArgument(linkedElement, index, value);
        }
    }

    @Override
    public List<EditorCommand> getUndoCommands() {
        return newUndoCommands(new SetKeywordCallArgumentCommand(keywordCall, index, previousValue, shouldReplaceValue));
    }
}
