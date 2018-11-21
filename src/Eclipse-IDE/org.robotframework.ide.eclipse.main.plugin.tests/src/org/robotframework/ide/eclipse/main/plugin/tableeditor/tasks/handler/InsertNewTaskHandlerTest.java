package org.robotframework.ide.eclipse.main.plugin.tableeditor.tasks.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.mock;

import java.util.Arrays;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.junit.Before;
import org.junit.Test;
import org.rf.ide.core.testdata.model.RobotVersion;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.RobotSuiteFileCreator;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.RobotTask;
import org.robotframework.ide.eclipse.main.plugin.model.RobotTaskConditions;
import org.robotframework.ide.eclipse.main.plugin.model.RobotTasksSection;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.AddingToken;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.AddingToken.TokenState;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.tasks.handler.InsertNewTaskHandler.E4InsertNewTaskHandler;

public class InsertNewTaskHandlerTest {

    private final E4InsertNewTaskHandler handler = new E4InsertNewTaskHandler();

    private final RobotEditorCommandsStack commandsStack = new RobotEditorCommandsStack();

    @Before
    public void beforeTest() {
        commandsStack.clear();
    }

    @Test
    public void actualHandlerUsesProperE4Handler() {
        final InsertNewTaskHandler handler = new InsertNewTaskHandler();

        assertThat(handler).extracting("component").hasSize(1).allMatch(c -> c instanceof E4InsertNewTaskHandler);
    }

    @Test
    public void exceptionIsThrown_whenThereAreMultipleElementsSelected() {
        final RobotSuiteFile fileModel = createTargetModel();
        final IStructuredSelection selection = selection(new Object(), new Object());

        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> handler.addNewTask(fileModel, selection, commandsStack))
                .withMessage("Given selection should contain at most one element, but has 2 instead");
    }

    @Test
    public void newTaskIsCreatedAsTheLastOne_whenNothingIsSelected() {
        final RobotSuiteFile fileModel = createTargetModel();
        final IStructuredSelection selection = selection();

        handler.addNewTask(fileModel, selection, commandsStack);

        final RobotTasksSection section = fileModel.findSection(RobotTasksSection.class).get();
        assertThat(section.getChildren()).extracting(RobotElement::getName)
                .containsExactly("existing task 1", "existing task 2", "existing task 3", "task");

        final RobotTask newTask = section.getChildren().get(3);
        assertThat(newTask).has(RobotTaskConditions.properlySetParent());
        assertThat(newTask.getChildren()).isEmpty();
    }

    @Test
    public void newTaskIsCreatedInPlaceOfSelectedTask() {
        final RobotSuiteFile fileModel = createTargetModel();
        final RobotTask selectedTask = fileModel.findSection(RobotTasksSection.class).get().getChildren().get(1);
        final IStructuredSelection selection = selection(selectedTask);

        handler.addNewTask(fileModel, selection, commandsStack);

        final RobotTasksSection section = fileModel.findSection(RobotTasksSection.class).get();
        assertThat(section.getChildren()).extracting(RobotElement::getName)
                .containsExactly("existing task 1", "task", "existing task 2", "existing task 3");

        final RobotTask newTask = section.getChildren().get(1);
        assertThat(newTask).has(RobotTaskConditions.properlySetParent());
        assertThat(newTask.getChildren()).isEmpty();
    }

    @Test
    public void newTaskIsCreatedInPlaceOfParentTaskOfSelectedCall() {
        final RobotSuiteFile fileModel = createTargetModel();
        final RobotKeywordCall selectedCall = fileModel.findSection(RobotTasksSection.class)
                .get()
                .getChildren()
                .get(2)
                .getChildren()
                .get(0);
        final IStructuredSelection selection = selection(selectedCall);

        handler.addNewTask(fileModel, selection, commandsStack);

        final RobotTasksSection section = fileModel.findSection(RobotTasksSection.class).get();
        assertThat(section.getChildren()).extracting(RobotElement::getName)
                .containsExactly("existing task 1", "existing task 2", "task", "existing task 3");

        final RobotTask newTask = section.getChildren().get(2);
        assertThat(newTask).has(RobotTaskConditions.properlySetParent());
        assertThat(newTask.getChildren()).isEmpty();
    }

    @Test
    public void newTaskIsCreatedInPlaceOfParentTaskOfSelectedAddingToken() {
        final RobotSuiteFile fileModel = createTargetModel();
        final RobotTask parent = fileModel.findSection(RobotTasksSection.class)
                .get()
                .getChildren()
                .get(2);
        final AddingToken selectedToken = new AddingToken(parent, mock(TokenState.class));
        final IStructuredSelection selection = selection(selectedToken);

        handler.addNewTask(fileModel, selection, commandsStack);

        final RobotTasksSection section = fileModel.findSection(RobotTasksSection.class).get();
        assertThat(section.getChildren()).extracting(RobotElement::getName)
                .containsExactly("existing task 1", "existing task 2", "task", "existing task 3");

        final RobotTask newTask = section.getChildren().get(2);
        assertThat(newTask).has(RobotTaskConditions.properlySetParent());
        assertThat(newTask.getChildren()).isEmpty();
    }

    @SafeVarargs
    private static <T> IStructuredSelection selection(final T... selectedElements) {
        return new StructuredSelection(Arrays.asList(selectedElements));
    }

    private static RobotSuiteFile createTargetModel() {
        return new RobotSuiteFileCreator().setVersion(new RobotVersion(3, 1))
                .appendLine("*** Tasks ***")
                .appendLine("existing task 1")
                .appendLine("  a  1  2")
                .appendLine("  b  3  4")
                .appendLine("existing task 2")
                .appendLine("  c  5  6")
                .appendLine("  d  7  8")
                .appendLine("existing task 3")
                .appendLine("  e  9  10")
                .appendLine("  f  11  12")
                .build();
    }
}
