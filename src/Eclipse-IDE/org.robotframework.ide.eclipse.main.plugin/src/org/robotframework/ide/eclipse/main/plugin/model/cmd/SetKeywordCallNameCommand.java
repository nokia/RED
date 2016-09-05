/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCodeHoldingElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;

public class SetKeywordCallNameCommand extends EditorCommand {

    private final RobotKeywordCall keywordCall;
    private final String name;

    public SetKeywordCallNameCommand(final RobotKeywordCall keywordCall, final String name) {
        this.keywordCall = keywordCall;
        this.name = name == null ? "" : name;
    }

    @Override
    public void execute() throws CommandExecutionException {
        // FIXME : look how settings uses this command
        if (keywordCall.getName().equals(name)) {
            return;
        }

        if (keywordCall.isExecutable()) {
            if (looksLikeSetting()) {
                changeToSetting(keywordCall, name);
            } else {
                changeName(keywordCall, name);
            }
        } else {
            if (looksLikeSetting() && !isDifferentSetting()) {
                changeName(keywordCall, name);
            } else if (looksLikeSetting() && isDifferentSetting()) {
                changeToSetting(keywordCall, name);
            } else {
                changeToCall(keywordCall, name);
            }
        }
    }

    private boolean isDifferentSetting() {
        final RobotCodeHoldingElement<?> parent = (RobotCodeHoldingElement<?>) keywordCall.getParent();
        final RobotTokenType tokenType = parent.getSettingDeclarationTokenTypeFor(name);
        return !keywordCall.getLinkedElement().getDeclaration().getTypes().contains(tokenType);
    }

    private boolean looksLikeSetting() {
        return name.startsWith("[") && name.endsWith("]");
    }

    private void changeToSetting(final RobotKeywordCall call, final String settingName) {
        final RobotCodeHoldingElement<?> parent = (RobotCodeHoldingElement<?>) call.getParent();

        final int index = call.getIndex();
        parent.removeChild(call);

        parent.createSetting(index, settingName, call.getArguments(), call.getComment());

        eventBroker.send(RobotModelEvents.ROBOT_KEYWORD_CALL_CONVERTED, parent);
    }

    private void changeToCall(final RobotKeywordCall call, final String name) {
        final RobotCodeHoldingElement<?> parent = (RobotCodeHoldingElement<?>) call.getParent();

        final int index = call.getIndex();
        parent.removeChild(call);

        parent.createKeywordCall(index, name, call.getArguments(), call.getComment());

        eventBroker.send(RobotModelEvents.ROBOT_KEYWORD_CALL_CONVERTED, parent);
    }

    private void changeName(final RobotKeywordCall element, final String name) {
        final AModelElement<?> linkedElement = element.getLinkedElement();
        linkedElement.getDeclaration().setText(name);

        eventBroker.send(RobotModelEvents.ROBOT_KEYWORD_CALL_NAME_CHANGE, keywordCall);
    }
}
