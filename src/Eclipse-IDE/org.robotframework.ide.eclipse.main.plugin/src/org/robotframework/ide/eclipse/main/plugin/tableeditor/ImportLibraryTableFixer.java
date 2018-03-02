/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor;

import static com.google.common.collect.Lists.newArrayList;

import java.util.Optional;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSettingsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.CreateFreshSectionCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.settings.CreateFreshSettingCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.settings.SettingsEditorPart;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.settings.SettingsEditorPart.SettingsEditor;

public class ImportLibraryTableFixer {

    private final String libName;

    public ImportLibraryTableFixer(final String libName) {
        this.libName = libName;
    }

    public void apply(final RobotSuiteFile suiteModel) {
        final IEditorPart activeEditor = PlatformUI.getWorkbench()
                .getActiveWorkbenchWindow()
                .getActivePage()
                .getActiveEditor();
        if (activeEditor instanceof RobotFormEditor
                && activeEditor.getEditorInput().equals(new FileEditorInput(suiteModel.getFile()))) {
            final RobotEditorCommandsStack commandsStack = getSettingsCommandStack((RobotFormEditor) activeEditor);

            Optional<RobotSettingsSection> section = suiteModel.findSection(RobotSettingsSection.class);

            if (!section.isPresent()) {
                final CreateFreshSectionCommand createSettings = new CreateFreshSectionCommand(suiteModel,
                        RobotSettingsSection.SECTION_NAME);
                commandsStack.execute(createSettings);
                section = suiteModel.findSection(RobotSettingsSection.class);
            }

            final CreateFreshSettingCommand importLibrary = new CreateFreshSettingCommand(section.get(), "Library",
                    newArrayList(libName));
            commandsStack.execute(importLibrary);
        }
    }

    private RobotEditorCommandsStack getSettingsCommandStack(final RobotFormEditor robotFormEditor) {
        final SettingsEditorPart settingsEditorPart = robotFormEditor.getPage(SettingsEditorPart.class);
        final SettingsEditor settingsEditor = settingsEditorPart.getComponent();
        final IEclipseContext context = settingsEditor.getContext();
        return context.get(RobotEditorCommandsStack.class);
    }

}
