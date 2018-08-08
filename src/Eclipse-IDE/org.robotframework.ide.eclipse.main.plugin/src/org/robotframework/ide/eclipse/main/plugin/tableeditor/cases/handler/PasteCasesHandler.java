/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.cases.handler;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCasesSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCodeHoldingElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFileSection;
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
        protected Class<? extends RobotSuiteFileSection> getSectionClass() {
            return RobotCasesSection.class;
        }

        @Override
        protected String getSectionName() {
            return RobotCasesSection.SECTION_NAME;
        }
    }
}
