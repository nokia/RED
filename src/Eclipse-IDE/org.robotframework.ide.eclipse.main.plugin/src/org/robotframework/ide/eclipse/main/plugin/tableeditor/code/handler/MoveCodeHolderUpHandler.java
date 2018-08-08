/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.code.handler;

import java.util.Optional;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCodeHoldingElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.MoveCodeHolderUpCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.MoveKeywordCallUpCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.code.handler.MoveCodeHolderUpHandler.E4MoveCodeHolderUpHandler;
import org.robotframework.red.commands.DIParameterizedHandler;
import org.robotframework.red.viewers.Selections;

public class MoveCodeHolderUpHandler extends DIParameterizedHandler<E4MoveCodeHolderUpHandler> {

    public MoveCodeHolderUpHandler() {
        super(E4MoveCodeHolderUpHandler.class);
    }

    public static class E4MoveCodeHolderUpHandler {

        @Execute
        public void moveHolderUp(@Named(Selections.SELECTION) final IStructuredSelection selection,
                final RobotEditorCommandsStack commandsStack) {

            final Optional<RobotKeywordCall> maybeKeywordCall = Selections.getOptionalFirstElement(selection,
                    RobotKeywordCall.class);
            @SuppressWarnings("rawtypes")
            final Optional<RobotCodeHoldingElement> maybeHolder = Selections.getOptionalFirstElement(selection,
                    RobotCodeHoldingElement.class);

            if (maybeKeywordCall.isPresent()) {
                commandsStack.execute(new MoveKeywordCallUpCommand(maybeKeywordCall.get()));
            } else if (maybeHolder.isPresent()) {
                commandsStack.execute(new MoveCodeHolderUpCommand(maybeHolder.get()));
            }
        }
    }
}
