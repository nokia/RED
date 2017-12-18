package org.robotframework.ide.eclipse.main.plugin.model.cmd.settings;

import static java.util.stream.Collectors.toMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.util.Map;
import java.util.function.Function;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.mockeclipse.ContextInjector;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.RobotSuiteFileCreator;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting.SettingsGroup;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSettingsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;

public class SetSettingNameCommandTest {

    @Test
    public void libraryImportSettingIsNotTouched_whenTransformingToLibraryImport() {
        final RobotSetting libraryImport = createLibraryImportSetting();

        final IEventBroker eventBroker = mock(IEventBroker.class);
        final SetSettingNameCommand command = ContextInjector.prepareContext().inWhich(eventBroker).isInjectedInto(
                new SetSettingNameCommand(libraryImport, "Library"));
        command.execute();

        assertThat(libraryImport.getName()).isEqualTo("Library");
        assertThat(libraryImport.getGroup()).isEqualTo(SettingsGroup.LIBRARIES);

        for (final EditorCommand undo : command.getUndoCommands()) {
            undo.execute();
        }

        assertThat(libraryImport.getName()).isEqualTo("Library");
        assertThat(libraryImport.getGroup()).isEqualTo(SettingsGroup.LIBRARIES);

        verifyZeroInteractions(eventBroker);
    }

    @Test
    public void libraryImportSettingIsProperlyTransformedIntoResourcesImportSetting() {
        final RobotSetting libraryImport = createLibraryImportSetting();

        final IEventBroker eventBroker = mock(IEventBroker.class);
        final SetSettingNameCommand command = ContextInjector.prepareContext().inWhich(eventBroker).isInjectedInto(
                new SetSettingNameCommand(libraryImport, "Resource"));
        command.execute();

        assertThat(libraryImport.getName()).isEqualTo("Resource");
        assertThat(libraryImport.getGroup()).isEqualTo(SettingsGroup.RESOURCES);

        for (final EditorCommand undo : command.getUndoCommands()) {
            undo.execute();
        }

        assertThat(libraryImport.getName()).isEqualTo("Library");
        assertThat(libraryImport.getGroup()).isEqualTo(SettingsGroup.LIBRARIES);

        verify(eventBroker, times(2)).send(RobotModelEvents.ROBOT_KEYWORD_CALL_NAME_CHANGE, libraryImport);
    }

    @Test
    public void libraryImportSettingIsProperlyTransformedIntoVariablesImportSetting() {
        final RobotSetting libraryImport = createLibraryImportSetting();

        final IEventBroker eventBroker = mock(IEventBroker.class);
        final SetSettingNameCommand command = ContextInjector.prepareContext().inWhich(eventBroker).isInjectedInto(
                new SetSettingNameCommand(libraryImport, "Variables"));
        command.execute();

        assertThat(libraryImport.getName()).isEqualTo("Variables");
        assertThat(libraryImport.getGroup()).isEqualTo(SettingsGroup.VARIABLES);

        for (final EditorCommand undo : command.getUndoCommands()) {
            undo.execute();
        }

        assertThat(libraryImport.getName()).isEqualTo("Library");
        assertThat(libraryImport.getGroup()).isEqualTo(SettingsGroup.LIBRARIES);

        verify(eventBroker, times(2)).send(RobotModelEvents.ROBOT_KEYWORD_CALL_NAME_CHANGE, libraryImport);
    }

    @Test
    public void resourceImportSettingIsNotTouched_whenTransformingToResourceImport() {
        final RobotSetting resourceImport = createResourceImportSetting();

        final IEventBroker eventBroker = mock(IEventBroker.class);
        final SetSettingNameCommand command = ContextInjector.prepareContext().inWhich(eventBroker).isInjectedInto(
                new SetSettingNameCommand(resourceImport, "Resource"));
        command.execute();

        assertThat(resourceImport.getName()).isEqualTo("Resource");
        assertThat(resourceImport.getGroup()).isEqualTo(SettingsGroup.RESOURCES);

        for (final EditorCommand undo : command.getUndoCommands()) {
            undo.execute();
        }

        assertThat(resourceImport.getName()).isEqualTo("Resource");
        assertThat(resourceImport.getGroup()).isEqualTo(SettingsGroup.RESOURCES);

        verifyZeroInteractions(eventBroker);
    }

    @Test
    public void resourceImportSettingIsProperlyTransformedIntoLibraryImportSetting() {
        final RobotSetting resourceImport = createResourceImportSetting();

        final IEventBroker eventBroker = mock(IEventBroker.class);
        final SetSettingNameCommand command = ContextInjector.prepareContext().inWhich(eventBroker).isInjectedInto(
                new SetSettingNameCommand(resourceImport, "Library"));
        command.execute();

        assertThat(resourceImport.getName()).isEqualTo("Library");
        assertThat(resourceImport.getGroup()).isEqualTo(SettingsGroup.LIBRARIES);

        for (final EditorCommand undo : command.getUndoCommands()) {
            undo.execute();
        }

        assertThat(resourceImport.getName()).isEqualTo("Resource");
        assertThat(resourceImport.getGroup()).isEqualTo(SettingsGroup.RESOURCES);

        verify(eventBroker, times(2)).send(RobotModelEvents.ROBOT_KEYWORD_CALL_NAME_CHANGE, resourceImport);
    }

    @Test
    public void resourceImportSettingIsProperlyTransformedIntoVariablesImportSetting() {
        final RobotSetting resourceImport = createResourceImportSetting();

        final IEventBroker eventBroker = mock(IEventBroker.class);
        final SetSettingNameCommand command = ContextInjector.prepareContext().inWhich(eventBroker).isInjectedInto(
                new SetSettingNameCommand(resourceImport, "Variables"));
        command.execute();

        assertThat(resourceImport.getName()).isEqualTo("Variables");
        assertThat(resourceImport.getGroup()).isEqualTo(SettingsGroup.VARIABLES);

        for (final EditorCommand undo : command.getUndoCommands()) {
            undo.execute();
        }

        assertThat(resourceImport.getName()).isEqualTo("Resource");
        assertThat(resourceImport.getGroup()).isEqualTo(SettingsGroup.RESOURCES);

        verify(eventBroker, times(2)).send(RobotModelEvents.ROBOT_KEYWORD_CALL_NAME_CHANGE, resourceImport);
    }

    @Test
    public void variablesImportSettingIsNotTouched_whenTransformingToVariablesImport() {
        final RobotSetting variablesImport = createVariablesImportSetting();

        final IEventBroker eventBroker = mock(IEventBroker.class);
        final SetSettingNameCommand command = ContextInjector.prepareContext().inWhich(eventBroker).isInjectedInto(
                new SetSettingNameCommand(variablesImport, "Variables"));
        command.execute();

        assertThat(variablesImport.getName()).isEqualTo("Variables");
        assertThat(variablesImport.getGroup()).isEqualTo(SettingsGroup.VARIABLES);

        for (final EditorCommand undo : command.getUndoCommands()) {
            undo.execute();
        }

        assertThat(variablesImport.getName()).isEqualTo("Variables");
        assertThat(variablesImport.getGroup()).isEqualTo(SettingsGroup.VARIABLES);

        verifyZeroInteractions(eventBroker);
    }

    @Test
    public void variablesImportSettingIsProperlyTransformedIntoLibraryImportSetting() {
        final RobotSetting variablesImport = createVariablesImportSetting();

        final IEventBroker eventBroker = mock(IEventBroker.class);
        final SetSettingNameCommand command = ContextInjector.prepareContext().inWhich(eventBroker).isInjectedInto(
                new SetSettingNameCommand(variablesImport, "Library"));
        command.execute();

        assertThat(variablesImport.getName()).isEqualTo("Library");
        assertThat(variablesImport.getGroup()).isEqualTo(SettingsGroup.LIBRARIES);

        for (final EditorCommand undo : command.getUndoCommands()) {
            undo.execute();
        }

        assertThat(variablesImport.getName()).isEqualTo("Variables");
        assertThat(variablesImport.getGroup()).isEqualTo(SettingsGroup.VARIABLES);

        verify(eventBroker, times(2)).send(RobotModelEvents.ROBOT_KEYWORD_CALL_NAME_CHANGE, variablesImport);
    }

    @Test
    public void variablesImportSettingIsProperlyTransformedIntoResourceImportSetting() {
        final RobotSetting variablesImport = createVariablesImportSetting();

        final IEventBroker eventBroker = mock(IEventBroker.class);
        final SetSettingNameCommand command = ContextInjector.prepareContext().inWhich(eventBroker).isInjectedInto(
                new SetSettingNameCommand(variablesImport, "Resource"));
        command.execute();

        assertThat(variablesImport.getName()).isEqualTo("Resource");
        assertThat(variablesImport.getGroup()).isEqualTo(SettingsGroup.RESOURCES);

        for (final EditorCommand undo : command.getUndoCommands()) {
            undo.execute();
        }

        assertThat(variablesImport.getName()).isEqualTo("Variables");
        assertThat(variablesImport.getGroup()).isEqualTo(SettingsGroup.VARIABLES);

        verify(eventBroker, times(2)).send(RobotModelEvents.ROBOT_KEYWORD_CALL_NAME_CHANGE, variablesImport);
    }

    private static RobotSetting createLibraryImportSetting() {
        return (RobotSetting) createSettings().get("Library");
    }

    private RobotSetting createResourceImportSetting() {
        return (RobotSetting) createSettings().get("Resource");
    }

    private RobotSetting createVariablesImportSetting() {
        return (RobotSetting) createSettings().get("Variables");
    }

    private static Map<String, RobotKeywordCall> createSettings() {
        final RobotSuiteFile model = new RobotSuiteFileCreator().appendLine("*** Settings ***")
                .appendLine("Test Timeout    1   2   3   # old comment")
                .appendLine("Library    1   2   3   # old comment")
                .appendLine("Variables    1   2   3   # old comment")
                .appendLine("Resource    1   2   3   # old comment")
                .appendLine("Metadata    1   2   3   # old comment")
                .build();
        return model.findSection(RobotSettingsSection.class).get().getChildren().stream().collect(
                toMap(RobotKeywordCall::getName, Function.identity()));
    }
}
