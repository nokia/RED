/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.handler;

import static java.util.stream.Collectors.toList;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.code.ExecutablesRowView;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.code.KeywordCallsTableValuesChangingCommandsCollector;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.handler.ToggleCommentInTableHandler.E4ToggleCommentInTableHandler;
import org.robotframework.red.commands.DIParameterizedHandler;
import org.robotframework.red.viewers.Selections;

import com.google.common.base.Predicates;

public class ToggleCommentInTableHandler extends DIParameterizedHandler<E4ToggleCommentInTableHandler> {

    public ToggleCommentInTableHandler() {
        super(E4ToggleCommentInTableHandler.class);
    }

    public static class E4ToggleCommentInTableHandler {

        @Execute
        public void toggleCommentInTable(final RobotEditorCommandsStack commandsStack,
                @Named(Selections.SELECTION) final IStructuredSelection selection) {

            final List<Object> noncommentable = Arrays.stream(selection.toArray())
                    .filter(Predicates.not(RobotKeywordCall.class::isInstance))
                    .collect(toList());

            if (!noncommentable.isEmpty()) {
                return;
            }
            final List<RobotKeywordCall> commentables = Selections
                    .getAdaptableElements(selection, RobotKeywordCall.class)
                    .stream()
                    .filter(kw -> !kw.getName().isEmpty() || !kw.getComment().isEmpty())
                    .collect(Collectors.toList());
            final boolean shouldComment = commentables.stream().anyMatch(this::shouldAddCommentMark);


            for (final RobotKeywordCall commentable : commentables) {
                final List<String> execRowView = ExecutablesRowView.rowData(commentable);
                final String firstCell = execRowView.isEmpty() ? "" : execRowView.get(0).trim();

                final String newCell = generateNewCell(shouldComment, firstCell);
                new KeywordCallsTableValuesChangingCommandsCollector().collectForUpdate(commentable, newCell, 0)
                        .ifPresent(commandsStack::execute);
            }
        }

        private boolean shouldAddCommentMark(final RobotKeywordCall call) {
            final List<String> cells = ExecutablesRowView.rowData(call);
            return !cells.isEmpty() && !cells.get(0).trim().startsWith("#");
        }

        private String generateNewCell(final boolean shouldComment, final String firstCell) {
            if (shouldComment) {
                return "# " + firstCell;
            } else {
                return firstCell.startsWith("#") ? firstCell.substring(1).trim() : firstCell;
            }
        }
    }
}
