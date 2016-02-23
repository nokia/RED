/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.variables.handler;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.dnd.Clipboard;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariable;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariablesSection;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.InsertVariablesCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorSources;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.dnd.VariablesTransfer;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.variables.handler.PasteVariablesHandler.E4PasteVariablesHandler;
import org.robotframework.red.commands.DIParameterizedHandler;
import org.robotframework.red.viewers.Selections;

import com.google.common.base.Optional;

public class PasteVariablesHandler extends DIParameterizedHandler<E4PasteVariablesHandler> {

    public PasteVariablesHandler() {
        super(E4PasteVariablesHandler.class);
    }

    public static class E4PasteVariablesHandler {

        @Inject
        @Named(RobotEditorSources.SUITE_FILE_MODEL)
        private RobotSuiteFile fileModel;

        @Inject
        private RobotEditorCommandsStack commandsStack;

        @Execute
        public Object pasteVariables(@Named(Selections.SELECTION) final IStructuredSelection selection,
                final Clipboard clipboard) {
            final Object probablyVariables = clipboard.getContents(VariablesTransfer.getInstance());

            if (probablyVariables instanceof RobotVariable[]) {
                insertVariables(selection, (RobotVariable[]) probablyVariables);
                return null;
            }

            return null;
        }

        private void insertVariables(final IStructuredSelection selection, final RobotVariable[] variables) {
            final Optional<RobotVariable> firstSelected = Selections.getOptionalFirstElement(selection,
                    RobotVariable.class);

            if (firstSelected.isPresent()) {
                final int index = firstSelected.get().getParent().getChildren().indexOf(firstSelected);
                commandsStack.execute(new InsertVariablesCommand(firstSelected.get().getParent(), index, variables));
            } else {
                final RobotVariablesSection section = fileModel.findSection(
                        RobotVariablesSection.class).orNull();
                if (section != null) {
                    commandsStack.execute(new InsertVariablesCommand(section, variables));
                }
            }
        }
    }
}
