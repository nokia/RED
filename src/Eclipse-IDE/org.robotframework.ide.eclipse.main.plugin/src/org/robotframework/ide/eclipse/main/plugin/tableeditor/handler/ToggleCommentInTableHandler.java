/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.handler;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.nebula.widgets.nattable.coordinate.PositionCoordinate;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.ui.ISources;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCasesSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordsSection;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotFormEditor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.cases.CasesDataProvider;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.handler.ToggleCommentInTableHandler.E4ToggleCommentInTableHandler;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.keywords.KeywordsDataProvider;
import org.robotframework.red.commands.DIParameterizedHandler;
import org.robotframework.red.viewers.Selections;

public class ToggleCommentInTableHandler extends DIParameterizedHandler<E4ToggleCommentInTableHandler> {

    public ToggleCommentInTableHandler() {
        super(E4ToggleCommentInTableHandler.class);
    }

    public static class E4ToggleCommentInTableHandler {

        @Execute
        public void toggleCommentInTable(final @Named(ISources.ACTIVE_EDITOR_NAME) RobotFormEditor editor,
                @Named(Selections.SELECTION) final IStructuredSelection selection,
                final RobotEditorCommandsStack commandsStack) {

            final List<RobotKeywordCall> commentable = Selections
                    .getAdaptableElements(selection, RobotKeywordCall.class)
                    .stream()
                    .filter(kw -> (!kw.getName().isEmpty() || !kw.getComment().isEmpty()))
                    .collect(Collectors.toList());
            final PositionCoordinate[] positions = editor.getSelectionLayerAccessor().getSelectedPositions();
            if (positions.length != commentable.size() || commentable.size() == 0) {
                // something that should not be commented is selected or nothing selected
                return;
            }
            final Set<Integer> lines = Arrays.stream(positions)
                    .map(pos -> pos.getRowPosition())
                    .collect(Collectors.toSet());
            final IDataProvider dataProvider = getDataProvider(commentable.get(0), commandsStack);
            final boolean shouldComment = shouldAddCommentMarks(commentable);
            for (final Integer line : lines) {
                final String newName = shouldComment ? generateCommented(line, dataProvider)
                        : generateUncommented(line, dataProvider);
                dataProvider.setDataValue(0, line, newName);
            }
        }

        private boolean shouldAddCommentMarks(final List<RobotKeywordCall> calls) {
            final Optional<RobotKeywordCall> uncommented = calls.stream()
                    .filter(RobotKeywordCall::shouldAddCommentMark)
                    .findFirst();
            return uncommented.isPresent();
        }

        private IDataProvider getDataProvider(final RobotKeywordCall call,
                final RobotEditorCommandsStack commandsStack) {
            IDataProvider dataProvider = null;
            final RobotElement section = call.getParent().getParent();
            if (section instanceof RobotCasesSection) {
                dataProvider = new CasesDataProvider(commandsStack, (RobotCasesSection) section);
            } else if (section instanceof RobotKeywordsSection) {
                dataProvider = new KeywordsDataProvider(commandsStack, (RobotKeywordsSection) section);
            }
            return dataProvider;
        }

        private String generateCommented(final int line, final IDataProvider dataProvider) {
            return "# " + ((String) dataProvider.getDataValue(0, line)).trim();
        }

        private String generateUncommented(final int line, final IDataProvider dataProvider) {
            final String oldName = (String) dataProvider.getDataValue(0, line);
            return oldName.trim().startsWith("#") ? oldName.substring(oldName.indexOf('#') + 1).trim() : oldName;
        }
    }

}
