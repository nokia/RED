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
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.InsertKeywordCallsCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.cases.InsertCasesCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.AddingToken;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorSources;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.cases.handler.PasteCasesHandler.E4PasteCasesHandler;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.dnd.RedClipboard;
import org.robotframework.red.commands.DIParameterizedHandler;
import org.robotframework.red.viewers.Selections;

import com.google.common.base.Optional;

public class PasteCasesHandler extends DIParameterizedHandler<E4PasteCasesHandler> {

    public PasteCasesHandler() {
        super(E4PasteCasesHandler.class);
    }

    public static class E4PasteCasesHandler {

        @Execute
        public void pasteCases(@Named(RobotEditorSources.SUITE_FILE_MODEL) final RobotSuiteFile fileModel,
                @Named(Selections.SELECTION) final IStructuredSelection selection, final RedClipboard clipboard,
                final RobotEditorCommandsStack commandsStack) {

            final RobotCase[] cases = clipboard.getCases();
            if (cases != null) {
                insertCases(fileModel, selection, cases, commandsStack);
                return;
            }

            final RobotKeywordCall[] calls = clipboard.getKeywordCalls();
            if (calls != null) {
                insertCalls(selection, calls, commandsStack);
            }
        }

        private void insertCases(final RobotSuiteFile fileModel, final IStructuredSelection selection,
                final RobotCase[] cases, final RobotEditorCommandsStack commandsStack) {
            if (selection.isEmpty()) {
                insertCasesAtSectionEnd(fileModel, cases, commandsStack);
                return;
            }

            final Optional<Object> firstSelected = Selections.getOptionalFirstElement(selection, Object.class);

            if (firstSelected.get() instanceof AddingToken && ((AddingToken) firstSelected.get()).getParent() == null) {
                insertCasesAtSectionEnd(fileModel, cases, commandsStack);

            } else if (firstSelected.get() instanceof AddingToken) {
                final AddingToken token = (AddingToken) firstSelected.get();
                final RobotCase targetCase = (RobotCase) token.getParent();
                final int index = targetCase.getParent().getChildren().indexOf(targetCase);
                insertCaseAt(fileModel, index, cases, commandsStack);

            } else if (firstSelected.get() instanceof RobotCase) {
                final RobotCase targetCase = (RobotCase) firstSelected.get();
                final int index = targetCase.getParent().getChildren().indexOf(targetCase);
                insertCaseAt(fileModel, index, cases, commandsStack);

            } else {
                final RobotKeywordCall call = (RobotKeywordCall) firstSelected.get();
                final RobotCase targetCase = (RobotCase) call.getParent();
                final int index = targetCase.getParent().getChildren().indexOf(targetCase);
                insertCaseAt(fileModel, index, cases, commandsStack);
            }
        }

        private void insertCasesAtSectionEnd(final RobotSuiteFile fileModel, final RobotCase[] cases,
                final RobotEditorCommandsStack commandsStack) {
            final RobotCasesSection section = fileModel.findSection(RobotCasesSection.class).get();
            commandsStack.execute(new InsertCasesCommand(section, cases));
        }

        private void insertCaseAt(final RobotSuiteFile fileModel, final int index, final RobotCase[] cases,
                final RobotEditorCommandsStack commandsStack) {
            final RobotCasesSection section = fileModel.findSection(RobotCasesSection.class).get();
            commandsStack.execute(new InsertCasesCommand(section, index, cases));
        }

        private void insertCalls(final IStructuredSelection selection, final RobotKeywordCall[] calls,
                final RobotEditorCommandsStack commandsStack) {
            if (selection.isEmpty()) {
                return;
            }

            final Optional<Object> firstSelected = Selections.getOptionalFirstElement(selection, Object.class);

            if (firstSelected.get() instanceof AddingToken && ((AddingToken) firstSelected.get()).getParent() == null) {
                return;

            } else if (firstSelected.get() instanceof AddingToken) {
                final AddingToken token = (AddingToken) firstSelected.get();
                final RobotCase targetCase = (RobotCase) token.getParent();
                insertCallsAtCaseEnd(targetCase, calls, commandsStack);

            } else if (firstSelected.get() instanceof RobotCase) {
                final RobotCase targetCase = (RobotCase) firstSelected.get();
                final int index = targetCase.getChildren().size();
                insertCallsAt(index, targetCase, calls, commandsStack);

            } else {
                final RobotKeywordCall call = (RobotKeywordCall) firstSelected.get();
                final RobotCase targetCase = (RobotCase) call.getParent();
                final int index = call.getIndex();
                insertCallsAt(index, targetCase, calls, commandsStack);
            }
        }

        private void insertCallsAtCaseEnd(final RobotCase targetCase, final RobotKeywordCall[] calls,
                final RobotEditorCommandsStack commandsStack) {
            commandsStack.execute(new InsertKeywordCallsCommand(targetCase, calls));
        }

        private void insertCallsAt(final int index, final RobotCase targetCase, final RobotKeywordCall[] calls,
                final RobotEditorCommandsStack commandsStack) {
            commandsStack.execute(new InsertKeywordCallsCommand(targetCase, index, calls));
        }
    }
}
