package org.robotframework.ide.eclipse.main.plugin.tableeditor.tasks.handler;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.robotframework.ide.eclipse.main.plugin.model.ModelConditions.name;
import static org.robotframework.ide.eclipse.main.plugin.model.ModelConditions.noFilePositions;
import static org.robotframework.ide.eclipse.main.plugin.model.ModelConditions.nullParent;

import java.util.List;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.junit.Before;
import org.junit.Test;
import org.rf.ide.core.testdata.model.RobotVersion;
import org.robotframework.ide.eclipse.main.plugin.mockeclipse.RedClipboardMock;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.RobotSuiteFileCreator;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.RobotTask;
import org.robotframework.ide.eclipse.main.plugin.model.RobotTasksSection;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.tasks.handler.CopyTasksHandler.E4CopyTasksHandler;

public class CopyTasksHandlerTest {

    private final E4CopyTasksHandler handler = new E4CopyTasksHandler();

    private RedClipboardMock clipboard;

    @Before
    public void beforeTest() {
        clipboard = new RedClipboardMock();
    }

    @Test
    public void actualHandlerUsesProperE4Handler() {
        final CopyTasksHandler handler = new CopyTasksHandler();

        assertThat(handler).extracting("component").hasSize(1).allMatch(c -> c instanceof E4CopyTasksHandler);
    }

    @Test
    public void nothingIsCopied_whenNothingIsSelected() {
        final IStructuredSelection selection = new StructuredSelection(newArrayList());

        final boolean copied = handler.copyTasks(selection, clipboard);
        assertThat(copied).isFalse();
        assertThat(clipboard.isEmpty());
    }

    @Test
    public void tasksAreCopied_whenOnlyTheyAreSelected() {
        final List<RobotTask> tasks = createTasks();
        final RobotTask selectedTask1 = tasks.get(0);
        final RobotTask selectedTask2 = tasks.get(1);

        final IStructuredSelection selection = new StructuredSelection(newArrayList(selectedTask1, selectedTask2));

        final boolean copied = handler.copyTasks(selection, clipboard);
        assertThat(copied).isTrue();

        assertThat(clipboard.getTasks()).hasSize(2);
        assertThat(clipboard.getTasks()[0]).has(nullParent()).has(noFilePositions()).has(name("task 1"));
        assertThat(clipboard.getTasks()[1]).has(nullParent()).has(noFilePositions()).has(name("task 2"));

        assertThat(clipboard.hasKeywordCalls()).isFalse();
    }

    @Test
    public void callsAreCopied_whenOnlyTheyAreSelected() {
        final List<RobotTask> tasks = createTasks();
        final RobotKeywordCall selectedTask1 = tasks.get(1).getChildren().get(0);
        final RobotKeywordCall selectedTask2 = tasks.get(1).getChildren().get(2);

        final IStructuredSelection selection = new StructuredSelection(newArrayList(selectedTask1, selectedTask2));

        final boolean copied = handler.copyTasks(selection, clipboard);
        assertThat(copied).isTrue();

        assertThat(clipboard.hasTasks()).isFalse();

        assertThat(clipboard.getKeywordCalls()).hasSize(2);
        assertThat(clipboard.getKeywordCalls()[0]).has(nullParent()).has(noFilePositions()).has(name("tags"));
        assertThat(clipboard.getKeywordCalls()[1]).has(nullParent()).has(noFilePositions()).has(name("d"));
    }

    @Test
    public void tasksAreCopied_whenBothTasksAndCallsAreSelected() {
        final List<RobotTask> tasks = createTasks();
        final RobotTask selectedTask = tasks.get(0);
        final RobotKeywordCall selectedCall = tasks.get(0).getChildren().get(1);

        final IStructuredSelection selection = new StructuredSelection(newArrayList(selectedTask, selectedCall));

        final boolean copied = handler.copyTasks(selection, clipboard);
        assertThat(copied).isTrue();

        assertThat(clipboard.getTasks()).hasSize(1);
        assertThat(clipboard.getTasks()[0]).has(nullParent()).has(noFilePositions()).has(name("task 1"));

        assertThat(clipboard.hasKeywordCalls()).isFalse();
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
