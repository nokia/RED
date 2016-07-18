/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.keywords.handler;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.robotframework.ide.eclipse.main.plugin.model.IRobotCodeHoldingElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCodeHoldingElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.InsertKeywordCallsCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.InsertKeywordDefinitionsCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.AddingToken;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorSources;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.dnd.RedClipboard;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.keywords.handler.PasteKeywordsHandler.E4PasteKeywordsHandler;
import org.robotframework.red.commands.DIParameterizedHandler;
import org.robotframework.red.viewers.Selections;

import com.google.common.base.Optional;

public class PasteKeywordsHandler extends DIParameterizedHandler<E4PasteKeywordsHandler> {

    public PasteKeywordsHandler() {
        super(E4PasteKeywordsHandler.class);
    }

    public static class E4PasteKeywordsHandler {

        @Inject
        @Named(RobotEditorSources.SUITE_FILE_MODEL)
        private RobotSuiteFile fileModel;

        @Inject
        private RobotEditorCommandsStack commandsStack;

        @Execute
        public void pasteKeywords(@Named(Selections.SELECTION) final IStructuredSelection selection,
                final RedClipboard clipboard) {

            final RobotKeywordDefinition[] keywordDefs = clipboard.getKeywordDefinitions();
            if (keywordDefs != null) {
                insertDefinitions(selection, keywordDefs);
                return;
            }

            final RobotKeywordCall[] keywordCalls = clipboard.getKeywordCalls();
            if (keywordCalls != null) {
                insertCalls(selection, keywordCalls);
            }
        }

        private void insertDefinitions(final IStructuredSelection selection,
                final RobotKeywordDefinition[] definitions) {
            final Optional<RobotKeywordDefinition> firstSelected = Selections.getOptionalFirstElement(selection,
                    RobotKeywordDefinition.class);

            if (firstSelected.isPresent()) {
                final int index = firstSelected.get().getParent().getChildren().indexOf(firstSelected.get());
                commandsStack.execute(
                        new InsertKeywordDefinitionsCommand(firstSelected.get().getParent(), index, definitions));
            } else {
                final RobotKeywordsSection section = fileModel.findSection(RobotKeywordsSection.class).orNull();
                if (section != null) {
                    commandsStack.execute(new InsertKeywordDefinitionsCommand(section, definitions));
                }
            }
        }

        private void insertCalls(final IStructuredSelection selection, final RobotKeywordCall[] calls) {
            final Optional<RobotKeywordCall> firstSelected = Selections.getOptionalFirstElement(selection,
                    RobotKeywordCall.class);

            //TODO: insert keyword settings
            
            if (firstSelected.isPresent()) {
                final int index = firstSelected.get().getParent().getChildren().indexOf(firstSelected.get());
                final int modelTableIndex = ((RobotCodeHoldingElement) firstSelected.get().getParent())
                        .findExecutableRowIndex(firstSelected.get());
                commandsStack.execute(
                        new InsertKeywordCallsCommand(firstSelected.get().getParent(), modelTableIndex, index, calls));
            } else {
                final Optional<AddingToken> selected = Selections.getOptionalFirstElement(selection, AddingToken.class);
                if (selected.isPresent() && selected.get().getParent() != null) {
                    commandsStack.execute(new InsertKeywordCallsCommand(
                            (IRobotCodeHoldingElement) selected.get().getParent(), calls));
                }
            }

        }
    }
}
