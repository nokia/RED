/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd;

import java.util.List;

import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCodeHoldingElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.code.ExecutablesRowView;
import org.robotframework.services.event.RedEventBroker;

public class ReplaceWithCallCommand extends EditorCommand {

    public static ReplaceWithCallCommand replaceEmpty(final RobotKeywordCall empty, final int index,
            final List<String> tokens) {
        return new ReplaceWithCallCommand(empty, index, tokens, ReplaceWithEmptyCommand::replaceCall);
    }

    public static ReplaceWithCallCommand replaceSetting(final RobotKeywordCall setting, final int index,
            final List<String> tokens) {
        return new ReplaceWithCallCommand(setting, index, tokens, ReplaceWithSettingCommand::replaceCall);
    }

    private final RobotKeywordCall call;
    private final int index;
    private final List<String> tokens;

    private List<String> oldTokens;

    private final TripleFunction<RobotKeywordCall, Integer, List<String>, EditorCommand> undoCommandProvider;

    private ReplaceWithCallCommand(final RobotKeywordCall call, final int index, final List<String> tokens,
            final TripleFunction<RobotKeywordCall, Integer, List<String>, EditorCommand> undoCommandProvider) {
        this.call = call;
        this.index = index;
        this.tokens = tokens;
        this.undoCommandProvider = undoCommandProvider;
    }

    @Override
    public void execute() throws CommandExecutionException {
        final FilePosition firstTokenPosition = ExecutablesRowView.rowTokens(call).get(0).getFilePosition();
        oldTokens = ExecutablesRowView.rowData(call);

        final RobotCodeHoldingElement<?> parent = (RobotCodeHoldingElement<?>) call.getParent();
        parent.replaceWithKeywordCall(index, tokens);

        final RobotToken firstToken = call.getLinkedElement().getElementTokens().get(0);
        firstToken.setFilePosition(firstTokenPosition);
        firstToken.markAsDirty();

        RedEventBroker.using(eventBroker).send(RobotModelEvents.ROBOT_KEYWORD_CALL_CELL_CHANGE, call);
    }

    @Override
    public List<EditorCommand> getUndoCommands() {
        return newUndoCommands(undoCommandProvider.apply(call, index, oldTokens));
    }

    @FunctionalInterface
    static interface TripleFunction<A, B, C, R> {

        R apply(A a, B b, C c);

    }
}
