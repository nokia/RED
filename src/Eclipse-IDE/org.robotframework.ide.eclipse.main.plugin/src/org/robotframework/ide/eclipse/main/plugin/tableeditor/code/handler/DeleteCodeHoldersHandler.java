/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.code.handler;

import static java.util.stream.Collectors.toList;

import java.util.List;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCodeHoldingElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.DeleteHoldersCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.DeleteKeywordCallCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.code.handler.DeleteCodeHoldersHandler.E4DeleteHoldersHandler;
import org.robotframework.red.commands.DIParameterizedHandler;
import org.robotframework.red.viewers.Selections;

public class DeleteCodeHoldersHandler extends DIParameterizedHandler<E4DeleteHoldersHandler> {

    public DeleteCodeHoldersHandler() {
        super(E4DeleteHoldersHandler.class);
    }

    public static class E4DeleteHoldersHandler {

        @Execute
        public void deleteHoldersAndCalls(final RobotEditorCommandsStack commandsStack,
                @Named(Selections.SELECTION) final IStructuredSelection selection) {

            final List<RobotKeywordCall> keywordCalls = Selections.getElements(selection, RobotKeywordCall.class);
            final List<RobotCodeHoldingElement<?>> holders = Selections
                    .getElements(selection, RobotCodeHoldingElement.class)
                    .stream()
                    .map(elem -> (RobotCodeHoldingElement<?>) elem)
                    .collect(toList());

            if (!keywordCalls.isEmpty()) {
                commandsStack.execute(new DeleteKeywordCallCommand(keywordCalls));

            } else if (!holders.isEmpty()) {
                commandsStack.execute(new DeleteHoldersCommand(holders));
            }
        }
    }
}
