/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.code.handler;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCodeHoldingElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.InsertKeywordCallsCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.AddingToken;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.dnd.RedClipboard;
import org.robotframework.red.viewers.Selections;

import com.google.common.base.Optional;

public abstract class E4PasteCodeHoldersHandler {

    protected final void pasteHolders(final RobotSuiteFile fileModel, final IStructuredSelection selection,
            final RedClipboard clipboard, final RobotEditorCommandsStack commandsStack) {

        final RobotCodeHoldingElement<?>[] holders = getCodeHolders(clipboard);
        if (holders != null) {
            insertCodeHolders(fileModel, selection, holders, commandsStack);
            return;
        }

        final RobotKeywordCall[] calls = clipboard.getKeywordCalls();
        if (calls != null) {
            insertCalls(selection, calls, commandsStack);
        }
    }

    protected abstract RobotCodeHoldingElement<?>[] getCodeHolders(final RedClipboard clipboard);

    private void insertCodeHolders(final RobotSuiteFile fileModel, final IStructuredSelection selection,
            final RobotCodeHoldingElement<?>[] holders, final RobotEditorCommandsStack commandsStack) {

        createTargetSectionIfRequired(fileModel, commandsStack);

        if (selection.isEmpty()) {
            insertHoldersAtSectionEnd(fileModel, holders, commandsStack);
            return;
        }

        final Optional<Object> firstSelected = Selections.getOptionalFirstElement(selection, Object.class);
        if (firstSelected.get() instanceof AddingToken && !((AddingToken) firstSelected.get()).isNested()) {
            insertHoldersAtSectionEnd(fileModel, holders, commandsStack);

        } else if (firstSelected.get() instanceof AddingToken) {
            final AddingToken token = (AddingToken) firstSelected.get();
            final RobotCodeHoldingElement<?> targetHolder = (RobotCodeHoldingElement<?>) token.getParent();
            final int index = targetHolder.getIndex();
            insertHoldersAt(fileModel, index, holders, commandsStack);

        } else if (firstSelected.get() instanceof RobotCodeHoldingElement<?>) {
            final RobotCodeHoldingElement<?> targetHolder = (RobotCodeHoldingElement<?>) firstSelected.get();
            final int index = targetHolder.getIndex();
            insertHoldersAt(fileModel, index, holders, commandsStack);

        } else {
            final RobotKeywordCall call = (RobotKeywordCall) firstSelected.get();
            final RobotCodeHoldingElement<?> targetHolder = (RobotCodeHoldingElement<?>) call.getParent();
            final int index = targetHolder.getIndex();
            insertHoldersAt(fileModel, index, holders, commandsStack);
        }
    }

    protected abstract void createTargetSectionIfRequired(RobotSuiteFile fileModel,
            RobotEditorCommandsStack commandsStack);

    protected abstract void insertHoldersAtSectionEnd(final RobotSuiteFile fileModel,
            final RobotCodeHoldingElement<?>[] holders, final RobotEditorCommandsStack commandsStack);

    protected abstract void insertHoldersAt(final RobotSuiteFile fileModel, final int index,
            final RobotCodeHoldingElement<?>[] holders, final RobotEditorCommandsStack commandsStack);

    private void insertCalls(final IStructuredSelection selection, final RobotKeywordCall[] calls,
            final RobotEditorCommandsStack commandsStack) {
        if (selection.isEmpty()) {
            return;
        }

        final Optional<Object> firstSelected = Selections.getOptionalFirstElement(selection, Object.class);

        if (firstSelected.get() instanceof AddingToken && !((AddingToken) firstSelected.get()).isNested()) {
            return;

        } else if (firstSelected.get() instanceof AddingToken) {
            final AddingToken token = (AddingToken) firstSelected.get();
            final RobotCodeHoldingElement<?> targetHolder = (RobotCodeHoldingElement<?>) token.getParent();
            insertCallsAtHolderEnd(targetHolder, calls, commandsStack);

        } else if (firstSelected.get() instanceof RobotCodeHoldingElement<?>) {
            final RobotCodeHoldingElement<?> targetHolder = (RobotCodeHoldingElement<?>) firstSelected.get();
            final int index = targetHolder.getChildren().size();
            insertCallsAt(index, targetHolder, calls, commandsStack);

        } else {
            final RobotKeywordCall call = (RobotKeywordCall) firstSelected.get();
            final RobotCodeHoldingElement<?> targetHolder = (RobotCodeHoldingElement<?>) call.getParent();
            final int index = call.getIndex();
            insertCallsAt(index, targetHolder, calls, commandsStack);
        }
    }

    private void insertCallsAtHolderEnd(final RobotCodeHoldingElement<?> targetHolder, final RobotKeywordCall[] calls,
            final RobotEditorCommandsStack commandsStack) {
        commandsStack.execute(new InsertKeywordCallsCommand(targetHolder, calls));
    }

    private void insertCallsAt(final int index, final RobotCodeHoldingElement<?> targetHolder,
            final RobotKeywordCall[] calls, final RobotEditorCommandsStack commandsStack) {
        commandsStack.execute(new InsertKeywordCallsCommand(targetHolder, index, calls));
    }
}
