/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.cases.handler;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.MoveCaseDownCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.MoveKeywordCallDownCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.cases.handler.MoveCaseDownHandler.E4MoveCaseDownHandler;
import org.robotframework.red.commands.DIParameterizedHandler;
import org.robotframework.red.viewers.Selections;

import com.google.common.base.Optional;

public class MoveCaseDownHandler extends DIParameterizedHandler<E4MoveCaseDownHandler> {

    public MoveCaseDownHandler() {
        super(E4MoveCaseDownHandler.class);
    }

    public static class E4MoveCaseDownHandler {

        @Inject
        private RobotEditorCommandsStack commandsStack;

        @Execute
        public Object moveCaseDown(@Named(Selections.SELECTION) final IStructuredSelection selection) {
            final Optional<RobotKeywordCall> maybeKeywordCall = Selections.getOptionalFirstElement(selection,
                    RobotKeywordCall.class);
            final Optional<RobotCase> maybeTestCase = Selections.getOptionalFirstElement(selection, RobotCase.class);

            if (maybeKeywordCall.isPresent()) {
                commandsStack.execute(new MoveKeywordCallDownCommand(maybeKeywordCall.get()));
            } else if (maybeTestCase.isPresent()) {
                commandsStack.execute(new MoveCaseDownCommand(maybeTestCase.get()));
            }
            return null;
        }
    }
}
