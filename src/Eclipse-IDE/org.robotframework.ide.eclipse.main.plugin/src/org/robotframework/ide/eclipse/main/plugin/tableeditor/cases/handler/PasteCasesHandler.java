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
import org.robotframework.ide.eclipse.main.plugin.model.RobotCasesSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCodeHoldingElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.CreateFreshSectionCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.cases.InsertCasesCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorSources;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.cases.handler.PasteCasesHandler.E4PasteCasesHandler;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.code.handler.E4PasteCodeHoldersHandler;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.dnd.RedClipboard;
import org.robotframework.red.commands.DIParameterizedHandler;
import org.robotframework.red.viewers.Selections;

public class PasteCasesHandler extends DIParameterizedHandler<E4PasteCasesHandler> {

    public PasteCasesHandler() {
        super(E4PasteCasesHandler.class);
    }

    public static class E4PasteCasesHandler extends E4PasteCodeHoldersHandler {

        @Execute
        public void pasteCases(@Named(RobotEditorSources.SUITE_FILE_MODEL) final RobotSuiteFile fileModel,
                @Named(Selections.SELECTION) final IStructuredSelection selection, final RedClipboard clipboard,
                final RobotEditorCommandsStack commandsStack) {

            pasteHolders(fileModel, selection, clipboard, commandsStack);
        }

        @Override
        protected RobotCodeHoldingElement<?>[] getCodeHolders(final RedClipboard clipboard) {
            return clipboard.getCases();
        }

        @Override
        protected void createTargetSectionIfRequired(final RobotSuiteFile fileModel,
                final RobotEditorCommandsStack commandsStack) {
            if (fileModel.findSection(RobotCasesSection.class).isPresent()) {
                return;
            }
            commandsStack.execute(new CreateFreshSectionCommand(fileModel, RobotCasesSection.SECTION_NAME));
        }

        @Override
        protected void insertHoldersAtSectionEnd(final RobotSuiteFile fileModel,
                final RobotCodeHoldingElement<?>[] holders, final RobotEditorCommandsStack commandsStack) {
            final RobotCasesSection section = fileModel.findSection(RobotCasesSection.class).get();
            commandsStack.execute(new InsertCasesCommand(section, (RobotCase[]) holders));
        }

        @Override
        protected void insertHoldersAt(final RobotSuiteFile fileModel, final int index,
                final RobotCodeHoldingElement<?>[] holders, final RobotEditorCommandsStack commandsStack) {
            final RobotCasesSection section = fileModel.findSection(RobotCasesSection.class).get();
            commandsStack.execute(new InsertCasesCommand(section, index, (RobotCase[]) holders));
        }
    }
}
