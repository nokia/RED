/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd;

import org.robotframework.ide.eclipse.main.plugin.model.RobotCasesSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSettingsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFileSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariablesSection;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;

public class DeleteSectionCommand extends EditorCommand {

    private final RobotSuiteFileSection sectionToDelete;

    public DeleteSectionCommand(final RobotSuiteFileSection section) {
        this.sectionToDelete = section;
    }

    @Override
    public void execute() throws CommandExecutionException {
        if (sectionToDelete == null) {
            throw new IllegalStateException("Unable to delete section <null> has been given");
        }

        final RobotSuiteFile suiteFile = sectionToDelete.getSuiteFile();
        suiteFile.getChildren().remove(sectionToDelete);

        if (sectionToDelete instanceof RobotVariablesSection) {
            suiteFile.getLinkedElement().excludeVariableTableSection();
        } else if (sectionToDelete instanceof RobotCasesSection) {
            suiteFile.getLinkedElement().excludeTestCaseTableSection();
        } else if (sectionToDelete instanceof RobotKeywordsSection) {
            suiteFile.getLinkedElement().excludeKeywordTableSection();
        } else if (sectionToDelete instanceof RobotSettingsSection) {
            suiteFile.getLinkedElement().excludeSettingTableSection();
        } else {
            throw new IllegalStateException("Unable to delete unrecognized section <" + sectionToDelete + ">");
        }

        eventBroker.send(RobotModelEvents.ROBOT_SUITE_SECTION_REMOVED, suiteFile);
    }

    // TODO : requires proper undo which will insert section again and build on core model side
}
