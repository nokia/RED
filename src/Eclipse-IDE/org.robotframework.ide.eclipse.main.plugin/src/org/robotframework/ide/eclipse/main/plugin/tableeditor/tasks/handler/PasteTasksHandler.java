/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.tasks.handler;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCodeHoldingElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFileSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotTasksSection;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorSources;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.code.handler.E4PasteCodeHoldersHandler;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.dnd.RedClipboard;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.tasks.handler.PasteTasksHandler.E4PasteTasksHandler;
import org.robotframework.red.commands.DIParameterizedHandler;
import org.robotframework.red.viewers.Selections;

public class PasteTasksHandler extends DIParameterizedHandler<E4PasteTasksHandler> {

    public PasteTasksHandler() {
        super(E4PasteTasksHandler.class);
    }

    public static class E4PasteTasksHandler extends E4PasteCodeHoldersHandler {

        @Execute
        public void pasteTasks(@Named(RobotEditorSources.SUITE_FILE_MODEL) final RobotSuiteFile fileModel,
                @Named(Selections.SELECTION) final IStructuredSelection selection, final RedClipboard clipboard,
                final RobotEditorCommandsStack commandsStack) {

            pasteHolders(fileModel, selection, clipboard, commandsStack);
        }

        @Override
        protected RobotCodeHoldingElement<?>[] getCodeHolders(final RedClipboard clipboard) {
            return clipboard.getTasks();
        }

        @Override
        protected Class<? extends RobotSuiteFileSection> getSectionClass() {
            return RobotTasksSection.class;
        }

        @Override
        protected String getSectionName() {
            return RobotTasksSection.SECTION_NAME;
        }
    }
}
