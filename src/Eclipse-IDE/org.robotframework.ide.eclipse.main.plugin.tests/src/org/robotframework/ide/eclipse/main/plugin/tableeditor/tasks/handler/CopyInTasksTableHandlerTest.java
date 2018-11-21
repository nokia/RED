package org.robotframework.ide.eclipse.main.plugin.tableeditor.tasks.handler;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
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
import org.junit.Before;
import org.junit.Test;
import org.rf.ide.core.testdata.model.RobotVersion;
import org.robotframework.ide.eclipse.main.plugin.mockeclipse.RedClipboardMock;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.RobotSuiteFileCreator;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.RobotTask;
import org.robotframework.ide.eclipse.main.plugin.model.RobotTasksSection;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.AddingToken;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.AddingToken.TokenState;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotFormEditor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.SelectionLayerAccessor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.dnd.PositionCoordinateTransfer.SerializablePositionCoordinate;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.tasks.handler.CopyInTasksTableHandler.E4CopyInTasksTableHandler;

public class CopyInTasksTableHandlerTest {

    private final E4CopyInTasksTableHandler handler = new E4CopyInTasksTableHandler();

    private final RobotFormEditor editor = mock(RobotFormEditor.class);
    private final SelectionLayerAccessor selectionLayerAccessor = mock(SelectionLayerAccessor.class);

    private RedClipboardMock clipboard;

    @Before
    public void beforeTest() {
        clipboard = new RedClipboardMock();
        when(editor.getSelectionLayerAccessor()).thenReturn(selectionLayerAccessor);
    }

    @Test
    public void actualHandlerUsesProperE4Handler() {
        final CopyInTasksTableHandler handler = new CopyInTasksTableHandler();

        assertThat(handler).extracting("component").hasSize(1).allMatch(c -> c instanceof E4CopyInTasksTableHandler);
    }

    @Test
    public void nothingIsCopied_whenNothingIsSelected() {
        when(selectionLayerAccessor.getSelectedPositions()).thenReturn(new PositionCoordinate[] {});
        final IStructuredSelection selection = new StructuredSelection(new ArrayList<>());

        final boolean copied = handler.copyContent(editor, selection, clipboard);
        assertThat(copied).isFalse();
        assertThat(clipboard.isEmpty()).isTrue();
    }

    @Test
    public void callsWithPositionsAreCopied_whenOnlyCallsAreSelected() {
        final List<RobotTask> tasks = createTasks();
        final RobotKeywordCall selectedCall1 = tasks.get(0).getChildren().get(0);
        final RobotKeywordCall selectedCall2 = tasks.get(0).getChildren().get(2);

        final PositionCoordinate[] selectedPositions = new PositionCoordinate[] { new PositionCoordinate(null, 1, 1),
                new PositionCoordinate(null, 3, 3) };
        final List<?> selectedElements = newArrayList(selectedCall1, selectedCall2);

        when(selectionLayerAccessor.getSelectedPositions()).thenReturn(selectedPositions);
        when(selectionLayerAccessor.getElementSelectedAt(1)).thenReturn(selectedCall1);
        when(selectionLayerAccessor.getElementSelectedAt(3)).thenReturn(selectedCall2);

        final IStructuredSelection selection = new StructuredSelection(selectedElements);

        final boolean copied = handler.copyContent(editor, selection, clipboard);
        assertThat(copied).isTrue();

        assertThat(clipboard.hasTasks()).isFalse();

        assertThat(clipboard.getKeywordCalls()).hasSize(2);
        assertThat(clipboard.getKeywordCalls()[0]).has(nullParent()).has(noFilePositions()).has(name("tags"));
        assertThat(clipboard.getKeywordCalls()[1]).has(nullParent()).has(noFilePositions()).has(name("b"));

        assertThat(clipboard.getPositionsCoordinates()).containsExactly(new SerializablePositionCoordinate(1, 1),
                new SerializablePositionCoordinate(3, 3));
    }

    @Test
    public void tasksWithPositionsAreCopied_whenOnlyTasksAreSelected() {
        final List<RobotTask> tasks = createTasks();
        final RobotTask selectedTask1 = tasks.get(0);
        final RobotTask selectedTask2 = tasks.get(1);

        final PositionCoordinate[] selectedPositions = new PositionCoordinate[] { new PositionCoordinate(null, 0, 0),
                new PositionCoordinate(null, 2, 5) };
        final List<?> selectedElements = newArrayList(selectedTask1, selectedTask2);

        when(selectionLayerAccessor.getSelectedPositions()).thenReturn(selectedPositions);
        when(selectionLayerAccessor.getElementSelectedAt(0)).thenReturn(selectedTask1);
        when(selectionLayerAccessor.getElementSelectedAt(5)).thenReturn(selectedTask2);

        final IStructuredSelection selection = new StructuredSelection(selectedElements);

        final boolean copied = handler.copyContent(editor, selection, clipboard);
        assertThat(copied).isTrue();

        assertThat(clipboard.getTasks()).hasSize(2);
        assertThat(clipboard.getTasks()[0]).has(nullParent()).has(noFilePositions()).has(name("task 1"));
        assertThat(clipboard.getTasks()[1]).has(nullParent()).has(noFilePositions()).has(name("task 2"));

        assertThat(clipboard.hasKeywordCalls()).isFalse();

        assertThat(clipboard.getPositionsCoordinates()).containsExactly(new SerializablePositionCoordinate(0, 0),
                new SerializablePositionCoordinate(2, 5));
    }

    @Test
    public void tasksAndCallsWithPositionsAreCopied_whenBothAreSelected() {
        final List<RobotTask> tasks = createTasks();
        final RobotTask selectedTask = tasks.get(0);
        final RobotKeywordCall selectedCall = tasks.get(0).getChildren().get(1);

        final PositionCoordinate[] selectedPositions = new PositionCoordinate[] { new PositionCoordinate(null, 0, 0),
                new PositionCoordinate(null, 1, 2) };
        final List<?> selectedElements = newArrayList(selectedTask, selectedCall);

        when(selectionLayerAccessor.getSelectedPositions()).thenReturn(selectedPositions);
        when(selectionLayerAccessor.getElementSelectedAt(0)).thenReturn(selectedTask);
        when(selectionLayerAccessor.getElementSelectedAt(2)).thenReturn(selectedCall);

        final IStructuredSelection selection = new StructuredSelection(selectedElements);

        final boolean copied = handler.copyContent(editor, selection, clipboard);
        assertThat(copied).isTrue();

        assertThat(clipboard.getTasks()).hasSize(1);
        assertThat(clipboard.getTasks()[0]).has(nullParent()).has(noFilePositions()).has(name("task 1"));

        assertThat(clipboard.getKeywordCalls()).hasSize(1);
        assertThat(clipboard.getKeywordCalls()[0]).has(nullParent()).has(noFilePositions()).has(name("a"));

        assertThat(clipboard.getPositionsCoordinates()).containsExactly(new SerializablePositionCoordinate(0, 0),
                new SerializablePositionCoordinate(1, 2));
    }

    @Test
    public void nothingIsCopied_whenOnlyAddingTokensAreSelected() {
        final PositionCoordinate[] selectedPositions = new PositionCoordinate[] { new PositionCoordinate(null, 0, 0),
                new PositionCoordinate(null, 1, 1) };
        final List<?> selectedElements = newArrayList(new AddingToken(null, mock(TokenState.class)),
                new AddingToken(null, mock(TokenState.class)));

        when(selectionLayerAccessor.getSelectedPositions()).thenReturn(selectedPositions);

        final IStructuredSelection selection = new StructuredSelection(selectedElements);

        final boolean copied = handler.copyContent(editor, selection, clipboard);
        assertThat(copied).isFalse();
        assertThat(clipboard.isEmpty()).isTrue();
    }

    @Test
    public void positionsOfAddingTokensAreNotCopied_whenTheyAreSelectedAmongOtherRobotElements() {
        final List<RobotTask> tasks = createTasks();
        final RobotTask selectedTask = tasks.get(0);
        final RobotKeywordCall selectedCall = tasks.get(0).getChildren().get(0);
        final AddingToken selectedToken = new AddingToken(null, mock(TokenState.class));

        final PositionCoordinate[] selectedPositions = new PositionCoordinate[] { new PositionCoordinate(null, 0, 0),
                new PositionCoordinate(null, 1, 1), new PositionCoordinate(null, 2, 4) };
        final List<?> selectedElements = newArrayList(selectedTask, selectedCall, selectedToken);

        when(selectionLayerAccessor.getSelectedPositions()).thenReturn(selectedPositions);
        when(selectionLayerAccessor.getElementSelectedAt(0)).thenReturn(selectedTask);
        when(selectionLayerAccessor.getElementSelectedAt(1)).thenReturn(selectedCall);
        when(selectionLayerAccessor.getElementSelectedAt(4)).thenReturn(selectedToken);

        final IStructuredSelection selection = new StructuredSelection(selectedElements);

        final boolean copied = handler.copyContent(editor, selection, clipboard);
        assertThat(copied).isTrue();

        assertThat(clipboard.getTasks()).hasSize(1);
        assertThat(clipboard.getTasks()[0]).has(nullParent()).has(noFilePositions()).has(name("task 1"));

        assertThat(clipboard.getKeywordCalls()).hasSize(1);
        assertThat(clipboard.getKeywordCalls()[0]).has(nullParent()).has(noFilePositions()).has(name("tags"));

        assertThat(clipboard.getPositionsCoordinates()).containsExactly(new SerializablePositionCoordinate(0, 0),
                new SerializablePositionCoordinate(1, 1));
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
