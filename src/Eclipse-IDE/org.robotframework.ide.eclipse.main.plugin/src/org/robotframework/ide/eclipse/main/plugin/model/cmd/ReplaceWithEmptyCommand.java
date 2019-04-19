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
import org.robotframework.ide.eclipse.main.plugin.model.RobotEmptyLine;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.ReplaceWithCallCommand.TripleFunction;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.code.ExecutablesRowView;
import org.robotframework.services.event.RedEventBroker;

public class ReplaceWithEmptyCommand extends EditorCommand {

    public static ReplaceWithEmptyCommand replaceCall(final RobotKeywordCall call, final int index,
            final List<RobotToken> tokens) {
        return new ReplaceWithEmptyCommand(call, index, tokens, ReplaceWithCallCommand::replaceEmpty);
    }

    public static ReplaceWithEmptyCommand replaceSetting(final RobotKeywordCall setting, final int index,
            final List<RobotToken> tokens) {
        return new ReplaceWithEmptyCommand(setting, index, tokens, ReplaceWithSettingCommand::replaceEmpty);
    }

    private final RobotKeywordCall call;
    private final int index;
    private final List<RobotToken> tokens;

    private RobotEmptyLine newEmpty;
    private List<RobotToken> oldTokens;
    private final TripleFunction<RobotEmptyLine, Integer, List<RobotToken>, EditorCommand> undoCommandProvider;


    private ReplaceWithEmptyCommand(final RobotKeywordCall call, final int index, final List<RobotToken> tokens,
            final TripleFunction<RobotEmptyLine, Integer, List<RobotToken>, EditorCommand> undoCommandProvider) {
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
        newEmpty = parent.createEmpty(index, tokens.stream().map(RobotToken::getText).collect(toList()));

        RedEventBroker.using(eventBroker)
                .additionallyBinding(RobotModelEvents.ADDITIONAL_DATA)
                .to(newEmpty)
                .send(RobotModelEvents.ROBOT_KEYWORD_CALL_CONVERTED, parent);
    }

    @Override
    public List<EditorCommand> getUndoCommands() {
        return newUndoCommands(undoCommandProvider.apply(newEmpty, index, oldTokens));
    }
}
