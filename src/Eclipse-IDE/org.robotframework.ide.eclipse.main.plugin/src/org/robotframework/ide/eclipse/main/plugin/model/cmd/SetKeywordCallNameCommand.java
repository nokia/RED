/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd;

import java.util.List;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCodeHoldingElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotDefinitionSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;

public class SetKeywordCallNameCommand extends EditorCommand {

    private final RobotKeywordCall keywordCall;

    private final String newName;

    private final String oldName;

    public SetKeywordCallNameCommand(final RobotKeywordCall keywordCall, final String name) {
        this.keywordCall = keywordCall;
        this.newName = name == null ? "" : name;
        this.oldName = keywordCall.getName();
    }

    @Override
    public void execute() throws CommandExecutionException {
        // FIXME : look how settings uses this command
        if (oldName.equals(newName)) {
            return;
        }

        if (keywordCall.isExecutable()) {
            if (looksLikeSetting()) {
                changeToSetting(keywordCall, newName);
            } else {
                changeName(keywordCall, newName);
            }
        } else {
            if (looksLikeSetting() && !isDifferentSetting()) {
                changeName(keywordCall, newName);
            } else if (looksLikeSetting() && isDifferentSetting()) {
                changeToSetting(keywordCall, newName);
            } else {
                changeToCall(keywordCall, newName);
            }
        }
    }

    private boolean isDifferentSetting() {
        final RobotCodeHoldingElement<?> parent = (RobotCodeHoldingElement<?>) keywordCall.getParent();
        final RobotTokenType tokenType = parent.getSettingDeclarationTokenTypeFor(newName);
        return !keywordCall.getLinkedElement().getDeclaration().getTypes().contains(tokenType);
    }

    private boolean looksLikeSetting() {
        return newName.startsWith("[") && newName.endsWith("]");
    }

    private void changeToSetting(final RobotKeywordCall call, final String settingName) {
        final RobotCodeHoldingElement<?> parent = (RobotCodeHoldingElement<?>) call.getParent();

        final int index = call.getIndex();
        parent.removeChild(call);

        final int lastSettingIndex = calculateIndexOfLastSetting(parent);
        parent.createSetting(Math.min(index, lastSettingIndex), settingName, call.getArguments(), call.getComment());

        eventBroker.send(RobotModelEvents.ROBOT_KEYWORD_CALL_CONVERTED, parent);
    }

    private void changeToCall(final RobotKeywordCall call, final String name) {
        final RobotCodeHoldingElement<?> parent = (RobotCodeHoldingElement<?>) call.getParent();

        final int index = call.getIndex();
        parent.removeChild(call);

        final int lastSettingIndex = calculateIndexOfLastSetting(parent);
        parent.createKeywordCall(Math.max(index, lastSettingIndex + 1), name, call.getArguments(), call.getComment());

        eventBroker.send(RobotModelEvents.ROBOT_KEYWORD_CALL_CONVERTED, parent);
    }

    private int calculateIndexOfLastSetting(final RobotCodeHoldingElement<?> parent) {
        for (int i = parent.getChildren().size() - 1; i >= 0; i--) {
            if (parent.getChildren().get(i) instanceof RobotDefinitionSetting) {
                return i;
            }
        }
        return 0;
    }

    private void changeName(final RobotKeywordCall element, final String name) {
        final AModelElement<?> linkedElement = element.getLinkedElement();
        linkedElement.getDeclaration().setText(name);

        eventBroker.send(RobotModelEvents.ROBOT_KEYWORD_CALL_NAME_CHANGE, keywordCall);
    }

    @Override
    public List<EditorCommand> getUndoCommands() {
        return newUndoCommands(new SetKeywordCallNameCommand(keywordCall, oldName));
    }
}
