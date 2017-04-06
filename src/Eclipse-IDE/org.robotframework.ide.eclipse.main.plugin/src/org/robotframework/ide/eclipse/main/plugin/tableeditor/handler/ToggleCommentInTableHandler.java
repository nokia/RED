/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.handler;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.nebula.widgets.nattable.coordinate.PositionCoordinate;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.ui.ISources;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotFormEditor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.SelectionLayerAccessor;
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
        public void toggleCommentInTable(final @Named(ISources.ACTIVE_EDITOR_NAME) RobotFormEditor editor,
                @Named(Selections.SELECTION) final IStructuredSelection selection) {

            final List<Object> noncommentable = Arrays.stream(selection.toArray())
                    .filter(Predicates.not(RobotKeywordCall.class::isInstance))
                    .collect(Collectors.toList());
            if (noncommentable.size() == 0) {
                final List<RobotKeywordCall> commentable = Selections
                        .getAdaptableElements(selection, RobotKeywordCall.class)
                        .stream()
                        .filter(kw -> (!kw.getName().isEmpty() || !kw.getComment().isEmpty()))
                        .collect(Collectors.toList());
                if (commentable.size() > 0) {
                    final SelectionLayerAccessor accessor = editor.getSelectionLayerAccessor();
                    final IDataProvider dataProvider = accessor.getDataProvider();
                    final PositionCoordinate[] positions = accessor.getSelectedPositions();
                    final Set<Integer> lines = Arrays.stream(positions)
                            .map(PositionCoordinate::getRowPosition)
                            .collect(Collectors.toSet());
                    final boolean shouldComment = commentable.stream().anyMatch(RobotKeywordCall::shouldAddCommentMark);
                    for (final Integer line : lines) {
                        final String newName = shouldComment ? generateCommented(line, dataProvider)
                                : generateUncommented(line, dataProvider);
                        dataProvider.setDataValue(0, line, newName);
                    }
                }
            }

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
