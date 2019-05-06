/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd;

import java.util.List;

import org.robotframework.ide.eclipse.main.plugin.model.RobotCodeHoldingElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotDefinitionSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.ReplaceWithCallCommand.TripleFunction;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.code.ExecutablesRowView;
import org.robotframework.services.event.RedEventBroker;

public class ReplaceWithSettingCommand extends EditorCommand {

    public static ReplaceWithSettingCommand replaceEmpty(final RobotKeywordCall empty, final int index,
            final List<String> tokens) {
        return new ReplaceWithSettingCommand(empty, index, tokens, ReplaceWithEmptyCommand::replaceSetting);
    }

    public static ReplaceWithSettingCommand replaceCall(final RobotKeywordCall empty, final int index,
            final List<String> tokens) {
        return new ReplaceWithSettingCommand(empty, index, tokens, ReplaceWithCallCommand::replaceSetting);
    }

    private final RobotKeywordCall call;
    private final int index;

    private final List<String> tokens;

    private RobotDefinitionSetting newSetting;
    private List<String> oldTokens;

    private final TripleFunction<RobotDefinitionSetting, Integer, List<String>, EditorCommand> undoCommandProvider;


    private ReplaceWithSettingCommand(final RobotKeywordCall call, final int index, final List<String> tokens,
            final TripleFunction<RobotDefinitionSetting, Integer, List<String>, EditorCommand> undoCommandProvider) {
        this.call = call;
        this.index = index;
        this.tokens = tokens;
        this.undoCommandProvider = undoCommandProvider;
    }

    @Override
    public void execute() throws CommandExecutionException {
        oldTokens = ExecutablesRowView.rowData(call);

        final RobotCodeHoldingElement<?> parent = (RobotCodeHoldingElement<?>) call.getParent();
        parent.removeChild(index);
        newSetting = parent.createSetting(index, tokens);

        RedEventBroker.using(eventBroker)
                .additionallyBinding(RobotModelEvents.ADDITIONAL_DATA)
                .to(newSetting)
                .send(RobotModelEvents.ROBOT_KEYWORD_CALL_CONVERTED, parent);
    }

    @Override
    public List<EditorCommand> getUndoCommands() {
        return newUndoCommands(undoCommandProvider.apply(newSetting, index, oldTokens));
    }
}
