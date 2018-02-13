/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd;

import java.util.List;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCodeHoldingElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotDefinitionSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;
import org.robotframework.services.event.RedEventBroker;

public class ConvertCallToSetting extends EditorCommand {

    private final RobotKeywordCall call;

    private final String settingName;

    private RobotDefinitionSetting newSetting;

    public ConvertCallToSetting(final IEventBroker eventBroker, final RobotKeywordCall call, final String settingName) {
        this.eventBroker = eventBroker;
        this.call = call;
        this.settingName = settingName;
    }

    @Override
    public void execute() throws CommandExecutionException {
        final RobotCodeHoldingElement<?> parent = (RobotCodeHoldingElement<?>) call.getParent();

        final int index = call.getIndex();
        parent.removeChild(call);
        newSetting = parent.createSetting(index, settingName, call.getArguments(), call.getComment());

        RedEventBroker.using(eventBroker).additionallyBinding(RobotModelEvents.ADDITIONAL_DATA).to(newSetting).send(
                RobotModelEvents.ROBOT_KEYWORD_CALL_CONVERTED, parent);
    }

    @Override
    public List<EditorCommand> getUndoCommands() {
        return newUndoCommands(new ReplaceRobotKeywordCallCommand(eventBroker, newSetting, call));
    }
}
