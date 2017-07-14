/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.settings;

import static com.google.common.collect.Lists.newArrayList;

import java.util.Optional;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.ui.PlatformUI;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSettingsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.CreateFreshSectionCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.settings.CreateFreshSettingCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;

public class ImportLibraryTableFixer {

    private final String libName;

    public ImportLibraryTableFixer(final String libName) {
        this.libName = libName;
    }

    public void apply(final RobotSuiteFile suiteModel) {
        Optional<RobotSettingsSection> section = suiteModel.findSection(RobotSettingsSection.class);

        if (!section.isPresent()) {
            final CreateFreshSectionCommand createSettings = new CreateFreshSectionCommand(suiteModel,
                    RobotSettingsSection.SECTION_NAME);
            execute(createSettings);
            section = suiteModel.findSection(RobotSettingsSection.class);
        }

        final CreateFreshSettingCommand importLibrary = new CreateFreshSettingCommand(section.get(), "Library",
                newArrayList(libName));
        execute(importLibrary);
    }

    private void execute(final EditorCommand command) {
        final IEclipseContext context = PlatformUI.getWorkbench().getService(IEclipseContext.class).getActiveLeaf();
        ContextInjectionFactory.inject(command, context);
        command.execute();
    }

}
