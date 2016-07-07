/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.cases.handler;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.TreePath;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCasesSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.InsertCasesCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.InsertKeywordCallsCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorSources;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.cases.handler.PasteCasesHandler.E4PasteCasesHandler;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.dnd.RedClipboard;
import org.robotframework.red.commands.DIParameterizedHandler;
import org.robotframework.red.viewers.Selections;

public class PasteCasesHandler extends DIParameterizedHandler<E4PasteCasesHandler> {
    public PasteCasesHandler() {
        super(E4PasteCasesHandler.class);
    }

    public static class E4PasteCasesHandler {

        @Inject
        @Named(RobotEditorSources.SUITE_FILE_MODEL)
        private RobotSuiteFile fileModel;

        @Inject
        private RobotEditorCommandsStack commandsStack;

        @Execute
        public void pasteKeywords(@Named(Selections.SELECTION) final ITreeSelection selection,
                final RedClipboard clipboard) {

            final RobotCase[] cases = clipboard.getCases();
            if (cases != null) {
                insertCases(selection, cases);
                return;
            }

            final RobotKeywordCall[] calls = clipboard.getKeywordCalls();
            if (calls != null) {
                insertCalls(selection, calls);
            }
        }

        private void insertCases(final ITreeSelection selection, final RobotCase[] cases) {
            final TreePath selectedPath = Selections.getFirstElementPath(selection);
            final RobotCase targetCase = getElementOfClass(selectedPath, RobotCase.class);

            if (targetCase != null) {
                final int index = targetCase.getParent().getChildren().indexOf(targetCase);
                commandsStack.execute(new InsertCasesCommand(targetCase.getParent(), index, cases));
            } else {
                final RobotCasesSection section = fileModel.findSection(RobotCasesSection.class)
                        .orNull();
                if (section != null) {
                    commandsStack.execute(new InsertCasesCommand(section, cases));
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
                    final RobotCase targetCase = getElementOfClass(selectedPath, RobotCase.class);
                    if (targetCase != null) {
                        commandsStack.execute(new InsertKeywordCallsCommand(targetCase, calls));
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
