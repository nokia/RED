package org.robotframework.ide.eclipse.main.plugin.model.cmd;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.junit.Test;
import org.rf.ide.core.testdata.model.RobotVersion;
import org.robotframework.ide.eclipse.main.plugin.mockeclipse.ContextInjector;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.RobotSuiteFileCreator;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCasesSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.RobotTask;
import org.robotframework.ide.eclipse.main.plugin.model.RobotTasksSection;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;


public class SetCodeHolderNameCommandTest {

    @Test
    public void nothingHappens_whenNewNameForTestIsEqualToOldOne() {
        final RobotCase testCase = createTestCase("case 1");

        final IEventBroker eventBroker = mock(IEventBroker.class);
        final SetCodeHolderNameCommand command = ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new SetCodeHolderNameCommand(testCase, "case 1"));
        command.execute();
        assertThat(testCase.getName()).isEqualTo("case 1");

        for (final EditorCommand undo : command.getUndoCommands()) {
            undo.execute();
        }
        verifyZeroInteractions(eventBroker);
    }

    @Test
    public void nothingHappens_whenNewNameForTaskIsEqualToOldOne() {
        final RobotTask task = createTask("task 1");

        final IEventBroker eventBroker = mock(IEventBroker.class);
        final SetCodeHolderNameCommand command = ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new SetCodeHolderNameCommand(task, "task 1"));
        command.execute();
        assertThat(task.getName()).isEqualTo("task 1");

        for (final EditorCommand undo : command.getUndoCommands()) {
            undo.execute();
        }
        verifyZeroInteractions(eventBroker);
    }

    @Test
    public void nothingHappens_whenNewNameForKeywordIsEqualToOldOne() {
        final RobotKeywordDefinition keyword = createKeyword("keyword 1");

        final IEventBroker eventBroker = mock(IEventBroker.class);
        final SetCodeHolderNameCommand command = ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new SetCodeHolderNameCommand(keyword, "keyword 1"));
        command.execute();
        assertThat(keyword.getName()).isEqualTo("keyword 1");

        for (final EditorCommand undo : command.getUndoCommands()) {
            undo.execute();
        }
        verifyZeroInteractions(eventBroker);
    }

    @Test
    public void nameIsProperlyChanged_whenNewNameForTestIsDifferentThanOldOne() {
        final RobotCase testCase = createTestCase("case 1");

        final IEventBroker eventBroker = mock(IEventBroker.class);
        final SetCodeHolderNameCommand command = ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new SetCodeHolderNameCommand(testCase, "new case"));
        command.execute();
        assertThat(testCase.getName()).isEqualTo("new case");

        for (final EditorCommand undo : command.getUndoCommands()) {
            undo.execute();
        }
        assertThat(testCase.getName()).isEqualTo("case 1");

        verify(eventBroker, times(2)).send(RobotModelEvents.ROBOT_ELEMENT_NAME_CHANGED, testCase);
    }

    @Test
    public void nameIsProperlyChanged_whenNewNameForTaskDifferentThanOldOne() {
        final RobotTask task = createTask("task 1");

        final IEventBroker eventBroker = mock(IEventBroker.class);
        final SetCodeHolderNameCommand command = ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new SetCodeHolderNameCommand(task, "new task"));
        command.execute();
        assertThat(task.getName()).isEqualTo("new task");

        for (final EditorCommand undo : command.getUndoCommands()) {
            undo.execute();
        }
        assertThat(task.getName()).isEqualTo("task 1");

        verify(eventBroker, times(2)).send(RobotModelEvents.ROBOT_ELEMENT_NAME_CHANGED, task);
    }

    @Test
    public void nameIsProperlyChanged_whenNewNameForKeywordIsDifferentThanOldOne() {
        final RobotKeywordDefinition keyword = createKeyword("keyword 1");

        final IEventBroker eventBroker = mock(IEventBroker.class);
        final SetCodeHolderNameCommand command = ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new SetCodeHolderNameCommand(keyword, "new keyword"));
        command.execute();
        assertThat(keyword.getName()).isEqualTo("new keyword");

        for (final EditorCommand undo : command.getUndoCommands()) {
            undo.execute();
        }
        assertThat(keyword.getName()).isEqualTo("keyword 1");

        verify(eventBroker, times(2)).send(RobotModelEvents.ROBOT_ELEMENT_NAME_CHANGED, keyword);
    }

    @Test
    public void nameOfTestBecomesBackslash_whenTryingToSetNull() {
        final RobotCase testCase = createTestCase("case 1");

        final IEventBroker eventBroker = mock(IEventBroker.class);
        final SetCodeHolderNameCommand command = ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new SetCodeHolderNameCommand(testCase, null));
        command.execute();
        assertThat(testCase.getName()).isEqualTo("\\");

        for (final EditorCommand undo : command.getUndoCommands()) {
            undo.execute();
        }
        assertThat(testCase.getName()).isEqualTo("case 1");

        verify(eventBroker, times(2)).send(RobotModelEvents.ROBOT_ELEMENT_NAME_CHANGED, testCase);
    }

    @Test
    public void nameOfTaskBecomesBackslash_whenTryingToSetNull() {
        final RobotTask task = createTask("task 1");

        final IEventBroker eventBroker = mock(IEventBroker.class);
        final SetCodeHolderNameCommand command = ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new SetCodeHolderNameCommand(task, null));
        command.execute();
        assertThat(task.getName()).isEqualTo("\\");

        for (final EditorCommand undo : command.getUndoCommands()) {
            undo.execute();
        }
        assertThat(task.getName()).isEqualTo("task 1");

        verify(eventBroker, times(2)).send(RobotModelEvents.ROBOT_ELEMENT_NAME_CHANGED, task);
    }

    @Test
    public void nameOfKeywordBecomesBackslash_whenTryingToSetNull() {
        final RobotKeywordDefinition keyword = createKeyword("keyword 1");

        final IEventBroker eventBroker = mock(IEventBroker.class);
        final SetCodeHolderNameCommand command = ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new SetCodeHolderNameCommand(keyword, null));
        command.execute();
        assertThat(keyword.getName()).isEqualTo("\\");

        for (final EditorCommand undo : command.getUndoCommands()) {
            undo.execute();
        }
        assertThat(keyword.getName()).isEqualTo("keyword 1");

        verify(eventBroker, times(2)).send(RobotModelEvents.ROBOT_ELEMENT_NAME_CHANGED, keyword);
    }

    @Test
    public void nameOfTestBecomesBackslash_whenTryingToSetEmptyName() {
        final RobotCase testCase = createTestCase("case 1");

        final IEventBroker eventBroker = mock(IEventBroker.class);
        final SetCodeHolderNameCommand command = ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new SetCodeHolderNameCommand(testCase, ""));
        command.execute();
        assertThat(testCase.getName()).isEqualTo("\\");

        for (final EditorCommand undo : command.getUndoCommands()) {
            undo.execute();
        }
        assertThat(testCase.getName()).isEqualTo("case 1");

        verify(eventBroker, times(2)).send(RobotModelEvents.ROBOT_ELEMENT_NAME_CHANGED, testCase);
    }

    @Test
    public void nameOfTaskBecomesBackslash_whenTryingToSetEmptyName() {
        final RobotTask task = createTask("task 1");

        final IEventBroker eventBroker = mock(IEventBroker.class);
        final SetCodeHolderNameCommand command = ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new SetCodeHolderNameCommand(task, ""));
        command.execute();
        assertThat(task.getName()).isEqualTo("\\");

        for (final EditorCommand undo : command.getUndoCommands()) {
            undo.execute();
        }
        assertThat(task.getName()).isEqualTo("task 1");

        verify(eventBroker, times(2)).send(RobotModelEvents.ROBOT_ELEMENT_NAME_CHANGED, task);
    }

    @Test
    public void nameOfKeywordBecomesBackslash_whenTryingToSetEmptyName() {
        final RobotKeywordDefinition keyword = createKeyword("keyword 1");

        final IEventBroker eventBroker = mock(IEventBroker.class);
        final SetCodeHolderNameCommand command = ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new SetCodeHolderNameCommand(keyword, ""));
        command.execute();
        assertThat(keyword.getName()).isEqualTo("\\");

        for (final EditorCommand undo : command.getUndoCommands()) {
            undo.execute();
        }
        assertThat(keyword.getName()).isEqualTo("keyword 1");

        verify(eventBroker, times(2)).send(RobotModelEvents.ROBOT_ELEMENT_NAME_CHANGED, keyword);
    }

    private static RobotCase createTestCase(final String caseName) {
        final RobotSuiteFile model = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine(caseName)
                .appendLine("  Log  10")
                .build();
        final RobotCasesSection section = model.findSection(RobotCasesSection.class).get();
        return section.getChildren().get(0);
    }

    private static RobotTask createTask(final String taskName) {
        final RobotSuiteFile model = new RobotSuiteFileCreator().setVersion(new RobotVersion(3, 1))
                .appendLine("*** Tasks ***")
                .appendLine(taskName)
                .appendLine("  Log  10")
                .build();
        final RobotTasksSection section = model.findSection(RobotTasksSection.class).get();
        return section.getChildren().get(0);
    }

    private static RobotKeywordDefinition createKeyword(final String kwName) {
        final RobotSuiteFile model = new RobotSuiteFileCreator().appendLine("*** Keywords ***")
                .appendLine(kwName)
                .appendLine("  Log  10")
                .build();
        final RobotKeywordsSection section = model.findSection(RobotKeywordsSection.class).get();
        return section.getChildren().get(0);
    }
}
