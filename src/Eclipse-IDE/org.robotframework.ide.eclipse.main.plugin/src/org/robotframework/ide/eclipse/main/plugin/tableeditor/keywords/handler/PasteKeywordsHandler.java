/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.keywords.handler;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.TreePath;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.InsertKeywordCallsCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.InsertKeywordDefinitionsCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorSources;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.dnd.RedClipboard;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.keywords.handler.PasteKeywordsHandler.E4PasteKeywordsHandler;
import org.robotframework.red.commands.DIParameterizedHandler;
import org.robotframework.red.viewers.Selections;

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
        public void pasteKeywords(@Named(Selections.SELECTION) final ITreeSelection selection,
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

        private void insertDefinitions(final ITreeSelection selection, final RobotKeywordDefinition[] definitions) {
            final TreePath selectedPath = Selections.getFirstElementPath(selection);
            final RobotKeywordDefinition targetDef = getElementOfClass(selectedPath, RobotKeywordDefinition.class);

            if (targetDef != null) {
                final int index = targetDef.getParent().getChildren().indexOf(targetDef);
                commandsStack.execute(new InsertKeywordDefinitionsCommand(targetDef.getParent(),
                        index, definitions));
            } else {
                final RobotKeywordsSection section = fileModel.findSection(
                        RobotKeywordsSection.class).orNull();
                if (section != null) {
                    commandsStack.execute(new InsertKeywordDefinitionsCommand(section, definitions));
                }
            }
        }

        private void insertCalls(final ITreeSelection selection, final RobotKeywordCall[] calls) {
            final TreePath selectedPath = Selections.getFirstElementPath(selection);

            if (selectedPath.getSegmentCount() > 0) {
                final RobotKeywordCall targetCall = getElementOfClass(selectedPath, RobotKeywordCall.class);
                if (targetCall != null) {
                    final int index = targetCall.getParent().getChildren().indexOf(targetCall);
                    commandsStack.execute(new InsertKeywordCallsCommand(targetCall.getParent(), index, calls));
                } else {
                    final RobotKeywordDefinition targetDef = getElementOfClass(selectedPath,
                            RobotKeywordDefinition.class);
                    if (targetDef != null) {
                        commandsStack.execute(new InsertKeywordCallsCommand(targetDef, calls));
                    }
                }
            }
        }

        private static <T> T getElementOfClass(final TreePath path, final Class<? extends T> clazz) {
            if (path.getSegmentCount() == 0) {
                return null;
            }
            for (int i = path.getSegmentCount() - 1; i >= 0; i--) {
                final Object current = path.getSegment(i);
                if (clazz.isInstance(current)) {
                    return clazz.cast(current);
                }
            }
            return null;
        }
    }
}
