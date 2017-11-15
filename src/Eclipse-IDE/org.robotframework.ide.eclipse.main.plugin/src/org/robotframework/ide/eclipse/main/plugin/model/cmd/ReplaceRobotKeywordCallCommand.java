/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd;

import java.util.List;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.robotframework.ide.eclipse.main.plugin.model.IRobotCodeHoldingElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCodeHoldingElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSettingsSection;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;
import org.robotframework.services.event.RedEventBroker;

public class ReplaceRobotKeywordCallCommand extends EditorCommand {

    private final RobotKeywordCall oldCall, newCall;

    public ReplaceRobotKeywordCallCommand(final IEventBroker eventBroker, final RobotKeywordCall oldCall,
            final RobotKeywordCall newCall) {
        this.eventBroker = eventBroker;
        this.oldCall = oldCall;
        this.newCall = newCall;
    }

    @Override
    public void execute() throws CommandExecutionException {
        final IRobotCodeHoldingElement parent = oldCall.getParent();
        final int index = oldCall.getIndex();
        if (index > -1) {
            parent.removeChild(oldCall);
            if (parent instanceof RobotCodeHoldingElement<?>) {
                final RobotCodeHoldingElement<?> codeHoldingElement = (RobotCodeHoldingElement<?>) parent;
                codeHoldingElement.insertKeywordCall(index, newCall);
            } else {
                final RobotSettingsSection settingsSection = (RobotSettingsSection) parent;
                settingsSection.insertSetting(newCall, index);
            }
            newCall.resetStored();
            RedEventBroker.using(eventBroker).additionallyBinding(RobotModelEvents.ADDITIONAL_DATA).to(newCall).send(
                    RobotModelEvents.ROBOT_KEYWORD_CALL_CONVERTED, newCall.getParent());
        }
    }

    @Override
    public List<EditorCommand> getUndoCommands() {
        return newUndoCommands(new ReplaceRobotKeywordCallCommand(eventBroker, newCall, oldCall));
    }

}
