/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.code.handler;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCodeHoldingElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.CreateFreshKeywordCallCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.AddingToken;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.code.handler.InsertNewLineHandler.E4InsertNewLineHandler;
import org.robotframework.red.commands.DIParameterizedHandler;
import org.robotframework.red.viewers.Selections;

import com.google.common.base.Optional;

public class InsertNewLineHandler extends DIParameterizedHandler<E4InsertNewLineHandler> {

    public InsertNewLineHandler() {
        super(E4InsertNewLineHandler.class);
    }

    public static class E4InsertNewLineHandler {

        @Execute
        public void addNewLine(@Named(Selections.SELECTION) final IStructuredSelection selection, final RobotEditorCommandsStack stack) {

            final Optional<RobotElement> selectedElement = Selections.getOptionalFirstElement(selection,
                    RobotElement.class);

            EditorCommand newLineCommand = null;

            RobotCodeHoldingElement<?> codeHoldingElement = null;
            int index = -1;
            if (selectedElement.isPresent() && selectedElement.get() instanceof RobotKeywordCall) {
                codeHoldingElement = (RobotCodeHoldingElement<?>) selectedElement.get().getParent();
                index = selectedElement.get().getIndex();
            } else if (selectedElement.isPresent() && selectedElement.get() instanceof RobotCodeHoldingElement) {
                codeHoldingElement = (RobotCodeHoldingElement<?>) selectedElement.get();
                index = 0;
            }

            if (codeHoldingElement != null && index >= 0) {
                newLineCommand = new CreateFreshKeywordCallCommand(codeHoldingElement, index);
            } else {
                final Optional<AddingToken> token = Selections.getOptionalFirstElement(selection, AddingToken.class);
                if (token.isPresent() && token.get().isNested()) {
                    final RobotCodeHoldingElement<?> parent = (RobotCodeHoldingElement<?>) token.get().getParent();
                    newLineCommand = new CreateFreshKeywordCallCommand(parent);
                }
            }

            if (newLineCommand != null) {
                stack.execute(newLineCommand);
            }
        }
    }
}
