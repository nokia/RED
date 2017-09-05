/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.handler;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.nebula.widgets.nattable.coordinate.PositionCoordinate;
import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.RobotSuiteFileCreator;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCasesSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotFormEditor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.SelectionLayerAccessor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.handler.ToggleCommentInTableHandler.E4ToggleCommentInTableHandler;

public class ToggleCommentInTableHandlerTest {

    E4ToggleCommentInTableHandler handler = new E4ToggleCommentInTableHandler();

    private RobotFormEditor editor;

    private IStructuredSelection selection;

    private PositionCoordinate[] positions;

    private List<RobotKeywordCall> calls;

    private SelectionLayerAccessor accessor;

    @SuppressWarnings("rawtypes")
    private IRowDataProvider dataProvider;

    @Before
    public void resetVariables() {
        editor = null;
        positions = null;
        selection = null;
        calls = null;
        accessor = null;
        dataProvider = null;
    }

    @Test
    public void commentMarkIsAdded_whenThereIsUncommentedKeywordCallSelected() {

        final RobotSuiteFile model = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("t1")
                .appendLine("  Log  t  # comment")
                .build();
        initVariables(model);
        when(dataProvider.getDataValue(0, 1)).thenReturn("Log");
        handler.toggleCommentInTable(editor, selection);

        verify(dataProvider).setDataValue(0, 1, "# Log");
    }

    @Test
    public void commentMarkIsRemoved_whenThereIsCommentedKeywordCallSelected() {

        final RobotSuiteFile model = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("t1")
                .appendLine("  # Log  t  # comment")
                .build();
        initVariables(model);
        when(dataProvider.getDataValue(0, 1)).thenReturn("# Log");
        handler.toggleCommentInTable(editor, selection);

        verify(dataProvider).setDataValue(0, 1, "Log");
    }

    @Test
    public void commentMarkIsRemoved_whenThereIsCommentedKeywordCallWithoutSpaceSelected() {

        final RobotSuiteFile model = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("t1")
                .appendLine("  #Log  t  # comment")
                .build();
        initVariables(model);
        when(dataProvider.getDataValue(0, 1)).thenReturn("#Log");
        handler.toggleCommentInTable(editor, selection);

        verify(dataProvider).setDataValue(0, 1, "Log");
    }

    @Test
    public void commentMarksAdded_whenThereAreUncommentedKeywordCallsSelected() {

        final RobotSuiteFile model = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("t1")
                .appendLine("  Kw1  t  # comment")
                .appendLine("  Kw2  t  # comment")
                .build();
        initVariables(model);
        when(dataProvider.getDataValue(0, 1)).thenReturn("Kw1");
        when(dataProvider.getDataValue(0, 2)).thenReturn("Kw2");
        handler.toggleCommentInTable(editor, selection);

        verify(dataProvider).setDataValue(0, 1, "# Kw1");
        verify(dataProvider).setDataValue(0, 2, "# Kw2");
    }

    @Test
    public void commentMarksRemoved_whenThereAreCommentedKeywordCallsSelected() {

        final RobotSuiteFile model = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("t1")
                .appendLine("  # Kw1  t  # comment")
                .appendLine("  #Kw2  t  # comment")
                .build();
        initVariables(model);
        when(dataProvider.getDataValue(0, 1)).thenReturn("# Kw1");
        when(dataProvider.getDataValue(0, 2)).thenReturn("#Kw2");
        handler.toggleCommentInTable(editor, selection);

        verify(dataProvider).setDataValue(0, 1, "Kw1");
        verify(dataProvider).setDataValue(0, 2, "Kw2");
    }

    @Test
    public void commentMarksAdded_whenThereAreMixedKeywordCallsSelected() {

        final RobotSuiteFile model = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("t1")
                .appendLine("  # Kw1  t  # comment")
                .appendLine("  Kw2  t  # comment")
                .build();
        initVariables(model);
        when(dataProvider.getDataValue(0, 1)).thenReturn("# Kw1");
        when(dataProvider.getDataValue(0, 2)).thenReturn("Kw2");
        handler.toggleCommentInTable(editor, selection);

        verify(dataProvider).setDataValue(0, 1, "# # Kw1");
        verify(dataProvider).setDataValue(0, 2, "# Kw2");
    }

    @Test
    public void commentMarksAdded_whenThereIsRobotDefinitionSettingSelected() {

        final RobotSuiteFile model = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("t1")
                .appendLine("  [Documentation]  doc  # comment")
                .build();
        initVariables(model);
        when(dataProvider.getDataValue(0, 1)).thenReturn("[Documentation]");
        handler.toggleCommentInTable(editor, selection);

        verify(dataProvider).setDataValue(0, 1, "# [Documentation]");
    }

    @Test
    public void nothingHappened_whenThereAreNoKeywordCallsSelected() {

        final RobotSuiteFile model = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("t1")
                .build();
        initVariables(model);
        handler.toggleCommentInTable(editor, selection);

        Mockito.verifyZeroInteractions(dataProvider);
    }

    @SuppressWarnings("unchecked")
    private void initVariables(final RobotSuiteFile model) {
        calls = model.findSection(RobotCasesSection.class).get().getChildren().get(0).getChildren();
        positions = new PositionCoordinate[calls.size()];
        for (int i = 0; i < calls.size(); i++) {
            positions[i] = new PositionCoordinate(null, 0, i + 1);
        }
        editor = mock(RobotFormEditor.class);
        accessor = mock(SelectionLayerAccessor.class);
        dataProvider = mock(IRowDataProvider.class);
        when(accessor.getSelectedPositions()).thenReturn(positions);
        when(accessor.getDataProvider()).thenReturn(dataProvider);
        when(editor.getSelectionLayerAccessor()).thenReturn(accessor);
        selection = new StructuredSelection(calls);
    }
}
