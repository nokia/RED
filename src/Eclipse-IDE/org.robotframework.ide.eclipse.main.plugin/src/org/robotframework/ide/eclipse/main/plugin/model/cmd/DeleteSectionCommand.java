/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd;

import java.util.List;

import org.robotframework.ide.eclipse.main.plugin.model.RobotCasesSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSettingsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFileSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariablesSection;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;

public class DeleteSectionCommand extends EditorCommand {

    private final List<RobotSuiteFileSection> sectionsToDelete;

    public DeleteSectionCommand(final List<RobotSuiteFileSection> sections) {
        this.sectionsToDelete = sections;
    }

    @Override
    public void execute() throws CommandExecutionException {
        if (sectionsToDelete.isEmpty()) {
            return;
        }
        final RobotSuiteFile suiteFile = sectionsToDelete.get(0).getSuiteFile();
        suiteFile.getChildren().removeAll(sectionsToDelete);

        for (final RobotSuiteFileSection section : sectionsToDelete) {
            if (section instanceof RobotVariablesSection) {
                suiteFile.getLinkedElement().excludeVariableTableSection();
            } else if (section instanceof RobotCasesSection) {
                suiteFile.getLinkedElement().excludeTestCaseTableSection();
            } else if (section instanceof RobotKeywordsSection) {
                suiteFile.getLinkedElement().excludeKeywordTableSection();
            } else if (section instanceof RobotSettingsSection) {
                suiteFile.getLinkedElement().excludeSettingTableSection();
            }
        }

        eventBroker.post(RobotModelEvents.ROBOT_SUITE_SECTION_REMOVED, suiteFile);
    }
}
