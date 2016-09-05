/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.cases.handler;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.MoveKeywordCallUpCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.cases.MoveCaseUpCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.cases.handler.MoveCaseUpHandler.E4MoveCaseUpHandler;
import org.robotframework.red.commands.DIParameterizedHandler;
import org.robotframework.red.viewers.Selections;

import com.google.common.base.Optional;

public class MoveCaseUpHandler extends DIParameterizedHandler<E4MoveCaseUpHandler> {

    public MoveCaseUpHandler() {
        super(E4MoveCaseUpHandler.class);
    }

    public static class E4MoveCaseUpHandler {

        @Execute
        public void moveCaseUp(@Named(Selections.SELECTION) final IStructuredSelection selection,
                final RobotEditorCommandsStack commandsStack) {
            final Optional<RobotKeywordCall> maybeKeywordCall = Selections.getOptionalFirstElement(selection,
                    RobotKeywordCall.class);
            final Optional<RobotCase> maybeTestCase = Selections.getOptionalFirstElement(selection, RobotCase.class);

            if (maybeKeywordCall.isPresent()) {
                commandsStack.execute(new MoveKeywordCallUpCommand(maybeKeywordCall.get()));
            } else if (maybeTestCase.isPresent()) {
                commandsStack.execute(new MoveCaseUpCommand(maybeTestCase.get()));
            }
        }
    }
}
