/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd;

import static java.util.stream.Collectors.toList;

import java.util.List;

import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
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
            final List<RobotToken> tokens) {
        return new ReplaceWithSettingCommand(empty, index, tokens, ReplaceWithEmptyCommand::replaceSetting);
    }

    public static ReplaceWithSettingCommand replaceCall(final RobotKeywordCall empty, final int index,
            final List<RobotToken> tokens) {
        return new ReplaceWithSettingCommand(empty, index, tokens, ReplaceWithCallCommand::replaceSetting);
    }

    private final RobotKeywordCall call;
    private final int index;
    private final List<RobotToken> tokens;

    private RobotDefinitionSetting newSetting;
    private List<RobotToken> oldTokens;
    private final TripleFunction<RobotDefinitionSetting, Integer, List<RobotToken>, EditorCommand> undoCommandProvider;


    private ReplaceWithSettingCommand(final RobotKeywordCall call, final int index, final List<RobotToken> tokens,
            final TripleFunction<RobotDefinitionSetting, Integer, List<RobotToken>, EditorCommand> undoCommandProvider) {
        this.call = call;
        this.index = index;
        this.tokens = tokens;
        this.undoCommandProvider = undoCommandProvider;
    }

    @Override
    public void execute() throws CommandExecutionException {
        oldTokens = ExecutablesRowView.rowTokens(call);

        final RobotCodeHoldingElement<?> parent = (RobotCodeHoldingElement<?>) call.getParent();
        parent.removeChild(index);
        newSetting = parent.createSetting(index, tokens.stream().map(RobotToken::getText).collect(toList()));

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
