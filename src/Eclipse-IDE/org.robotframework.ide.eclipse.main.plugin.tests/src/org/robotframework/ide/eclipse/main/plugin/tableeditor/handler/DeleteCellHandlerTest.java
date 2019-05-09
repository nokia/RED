/*
* Copyright 2017 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.robotframework.ide.eclipse.main.plugin.tableeditor.handler;

import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.nebula.widgets.nattable.coordinate.PositionCoordinate;
import org.junit.Test;
import org.rf.ide.core.testdata.model.table.setting.Metadata;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.settings.DeleteCellCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotFormEditor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.SelectionLayerAccessor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.handler.DeleteCellHandler.E4DeleteCellHandler;

public class DeleteCellHandlerTest {

    private final RobotEditorCommandsStack commandsStack = mock(RobotEditorCommandsStack.class);
    private final RobotFormEditor editor = mock(RobotFormEditor.class);
    private final IStructuredSelection selection = mock(IStructuredSelection.class);
    private final SelectionLayerAccessor selectionLayerAccessor = mock(SelectionLayerAccessor.class);

    @Test
    public void nothingExecuted_whenInColumnAfterTheCall() {
        final Metadata linkedElement = new Metadata(RobotToken.create("Metadata"));
        final RobotKeywordCall setting = new RobotSetting(null, linkedElement);
        when(selection.getFirstElement()).thenReturn(setting);
        when(editor.getSelectionLayerAccessor()).thenReturn(selectionLayerAccessor);
        when(selectionLayerAccessor.getSelectedPositions())
                .thenReturn(new PositionCoordinate[] { new PositionCoordinate(null, 3, 0) });

        new E4DeleteCellHandler().deleteCell(commandsStack, editor, selection);

        verifyZeroInteractions(commandsStack);
    }

    @Test
    public void commandExecuted_whenInsideTheCall() {
        final Metadata linkedElement = new Metadata(RobotToken.create("Metadata"));
        final RobotKeywordCall setting = new RobotSetting(null, linkedElement);
        when(selection.getFirstElement()).thenReturn(setting);
        when(editor.getSelectionLayerAccessor()).thenReturn(selectionLayerAccessor);
        when(selectionLayerAccessor.getSelectedPositions())
                .thenReturn(new PositionCoordinate[] { new PositionCoordinate(null, 0, 0) });

        new E4DeleteCellHandler().deleteCell(commandsStack, editor, selection);

        verify(commandsStack).execute(refEq(new DeleteCellCommand((RobotSetting) setting, 0)));
        verifyNoMoreInteractions(commandsStack);
    }
}
