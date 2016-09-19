/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.keywords.handler;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCodeHoldingElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.CreateFreshSectionCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.keywords.InsertKeywordDefinitionsCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorSources;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.code.handler.E4PasteCodeHoldersHandler;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.dnd.RedClipboard;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.keywords.handler.PasteKeywordsHandler.E4PasteKeywordsHandler;
import org.robotframework.red.commands.DIParameterizedHandler;
import org.robotframework.red.viewers.Selections;

public class PasteKeywordsHandler extends DIParameterizedHandler<E4PasteKeywordsHandler> {

    public PasteKeywordsHandler() {
        super(E4PasteKeywordsHandler.class);
    }

    public static class E4PasteKeywordsHandler extends E4PasteCodeHoldersHandler {

        @Execute
        public void pasteKeywords(@Named(RobotEditorSources.SUITE_FILE_MODEL) final RobotSuiteFile fileModel,
                @Named(Selections.SELECTION) final IStructuredSelection selection, final RedClipboard clipboard,
                final RobotEditorCommandsStack commandsStack) {

            pasteHolders(fileModel, selection, clipboard, commandsStack);
        }

        @Override
        protected RobotCodeHoldingElement<?>[] getCodeHolders(final RedClipboard clipboard) {
            return clipboard.getKeywordDefinitions();
        }

        @Override
        protected void createTargetSectionIfRequired(final RobotSuiteFile fileModel,
                final RobotEditorCommandsStack commandsStack) {
            if (fileModel.findSection(RobotKeywordsSection.class).isPresent()) {
                return;
            }
            commandsStack.execute(new CreateFreshSectionCommand(fileModel, RobotKeywordsSection.SECTION_NAME));
        }

        @Override
        protected void insertHoldersAtSectionEnd(final RobotSuiteFile fileModel,
                final RobotCodeHoldingElement<?>[] holders, final RobotEditorCommandsStack commandsStack) {
            final RobotKeywordsSection section = fileModel.findSection(RobotKeywordsSection.class).get();
            commandsStack.execute(new InsertKeywordDefinitionsCommand(section, (RobotKeywordDefinition[]) holders));
        }

        @Override
        protected void insertHoldersAt(final RobotSuiteFile fileModel, final int index,
                final RobotCodeHoldingElement<?>[] holders, final RobotEditorCommandsStack commandsStack) {
            final RobotKeywordsSection section = fileModel.findSection(RobotKeywordsSection.class).get();
            commandsStack
                    .execute(new InsertKeywordDefinitionsCommand(section, index, (RobotKeywordDefinition[]) holders));
        }
    }
}
