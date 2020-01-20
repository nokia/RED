/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.tasks.handler;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.robotframework.ide.eclipse.main.plugin.model.ModelConditions.name;
import static org.robotframework.ide.eclipse.main.plugin.model.ModelConditions.noFilePositions;
import static org.robotframework.ide.eclipse.main.plugin.model.ModelConditions.nullParent;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.nebula.widgets.nattable.coordinate.PositionCoordinate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.rf.ide.core.environment.RobotVersion;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.robotframework.ide.eclipse.main.plugin.mockeclipse.RedClipboardMock;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.RobotSuiteFileCreator;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.RobotTask;
import org.robotframework.ide.eclipse.main.plugin.model.RobotTasksSection;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.AddingToken;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.AddingToken.TokenState;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotFormEditor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.SelectionLayerAccessor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.dnd.PositionCoordinateTransfer.SerializablePositionCoordinate;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.tasks.handler.CutInTasksTableHandler.E4CutInTasksTableHandler;

public class CutInTasksTableHandlerTest {

    private final E4CutInTasksTableHandler handler = new E4CutInTasksTableHandler();

    private final RobotFormEditor editor = mock(RobotFormEditor.class);

    private final SelectionLayerAccessor selectionLayerAccessor = mock(SelectionLayerAccessor.class);

    private final RobotEditorCommandsStack commandsStack = new RobotEditorCommandsStack();

    private RedClipboardMock clipboard;

    @BeforeEach
    public void beforeTest() {
        clipboard = new RedClipboardMock();
        when(editor.getSelectionLayerAccessor()).thenReturn(selectionLayerAccessor);
    }

    @Test
    public void actualHandlerUsesProperE4Handler() {
        final CutInTasksTableHandler handler = new CutInTasksTableHandler();

        assertThat(handler).extracting("component").isInstanceOf(E4CutInTasksTableHandler.class);
    }

    @Test
    public void nothingIsCut_whenNothingIsSelected() {
        when(selectionLayerAccessor.getSelectedPositions()).thenReturn(new PositionCoordinate[] {});
        final IStructuredSelection selection = new StructuredSelection(new ArrayList<>());

        handler.cutCellContent(editor, selection, commandsStack, clipboard);
        assertThat(clipboard.isEmpty()).isTrue();
    }

    @Test
    public void callsWithPositionsAreCut_whenOnlyCallsAreSelected() {
        final List<RobotTask> tasks = createTasks();
        final RobotKeywordCall selectedCall1 = tasks.get(0).getChildren().get(0);
        final RobotKeywordCall selectedCall2 = tasks.get(0).getChildren().get(2);

        final PositionCoordinate[] selectedPositions = new PositionCoordinate[] { new PositionCoordinate(null, 1, 1),
                new PositionCoordinate(null, 1, 3) };
        final List<?> selectedElements = newArrayList(selectedCall1, selectedCall2);

        when(selectionLayerAccessor.getSelectedPositions()).thenReturn(selectedPositions);
        when(selectionLayerAccessor.getElementSelectedAt(1)).thenReturn(selectedCall1);
        when(selectionLayerAccessor.getElementSelectedAt(3)).thenReturn(selectedCall2);
        when(selectionLayerAccessor.findNextSelectedElementRowIndex(any(Integer.class))).thenCallRealMethod();
        when(selectionLayerAccessor.findSelectedColumnsIndexesByRowIndex(any(Integer.class))).thenCallRealMethod();

        final IStructuredSelection selection = new StructuredSelection(selectedElements);

        handler.cutCellContent(editor, selection, commandsStack, clipboard);

        assertThat(clipboard.hasTasks()).isFalse();
        assertThat(clipboard.getKeywordCalls()).hasSize(2);
        assertThat(clipboard.getKeywordCalls()[0]).has(nullParent()).has(noFilePositions()).has(name("tags"));
        assertThat(clipboard.getKeywordCalls()[1]).has(nullParent()).has(noFilePositions()).has(name("b"));
        assertThat(clipboard.getPositionsCoordinates()).containsExactly(new SerializablePositionCoordinate(1, 1),
                new SerializablePositionCoordinate(1, 3));

        assertThat(selectedCall1.getLinkedElement().getElementTokens()).extracting(RobotToken::getText)
                .containsExactly("[tags]");
        assertThat(selectedCall2.getLinkedElement().getElementTokens()).extracting(RobotToken::getText)
                .containsExactly("b");
    }

    @Test
    public void tasksWithPositionsAreCut_whenOnlyTasksAreSelected() {
        final List<RobotTask> tasks = createTasks();
        final RobotTask selectedTask1 = tasks.get(0);
        final RobotTask selectedTask2 = tasks.get(1);

        final PositionCoordinate[] selectedPositions = new PositionCoordinate[] { new PositionCoordinate(null, 0, 0),
                new PositionCoordinate(null, 0, 5) };
        final List<?> selectedElements = newArrayList(selectedTask1, selectedTask2);

        when(selectionLayerAccessor.getSelectedPositions()).thenReturn(selectedPositions);
        when(selectionLayerAccessor.getElementSelectedAt(0)).thenReturn(selectedTask1);
        when(selectionLayerAccessor.getElementSelectedAt(5)).thenReturn(selectedTask2);
        when(selectionLayerAccessor.findNextSelectedElementRowIndex(any(Integer.class))).thenCallRealMethod();
        when(selectionLayerAccessor.findSelectedColumnsIndexesByRowIndex(any(Integer.class))).thenCallRealMethod();

        final IStructuredSelection selection = new StructuredSelection(selectedElements);

        handler.cutCellContent(editor, selection, commandsStack, clipboard);

        assertThat(clipboard.getTasks()).hasSize(2);
        assertThat(clipboard.getTasks()[0]).has(nullParent()).has(noFilePositions()).has(name("task 1"));
        assertThat(clipboard.getTasks()[1]).has(nullParent()).has(noFilePositions()).has(name("task 2"));
        assertThat(clipboard.hasKeywordCalls()).isFalse();
        assertThat(clipboard.getPositionsCoordinates()).containsExactly(new SerializablePositionCoordinate(0, 0),
                new SerializablePositionCoordinate(0, 5));

        assertThat(selectedTask1.getName()).isEqualTo("\\");
        assertThat(selectedTask2.getName()).isEqualTo("\\");
    }

    @Test
    public void nothingIsCopied_whenOnlyAddingTokensAreSelected() {
        final PositionCoordinate[] selectedPositions = new PositionCoordinate[] { new PositionCoordinate(null, 0, 0),
                new PositionCoordinate(null, 1, 1) };
        final List<?> selectedElements = newArrayList(new AddingToken(null, mock(TokenState.class)),
                new AddingToken(null, mock(TokenState.class)));

        when(selectionLayerAccessor.getSelectedPositions()).thenReturn(selectedPositions);

        final IStructuredSelection selection = new StructuredSelection(selectedElements);

        handler.cutCellContent(editor, selection, commandsStack, clipboard);
        assertThat(clipboard.isEmpty()).isTrue();
    }

    private static List<RobotTask> createTasks() {
        final RobotSuiteFile model = new RobotSuiteFileCreator().setVersion(new RobotVersion(3, 1))
                .appendLine("*** Tasks ***")
                .appendLine("task 1")
                .appendLine("  [tags]  tag1")
                .appendLine("  a  1")
                .appendLine("  b  2")
                .appendLine("task 2")
                .appendLine("  [tags]  tag2")
                .appendLine("  c  3")
                .appendLine("  d  4")
                .build();
        return model.findSection(RobotTasksSection.class).get().getChildren();
    }
}
