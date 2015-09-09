/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd;

import java.util.Collections;
import java.util.Comparator;

import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFileSection;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;

public class SortVariablesCommand extends EditorCommand {

    private final RobotSuiteFileSection variablesSection;

    public SortVariablesCommand(final RobotSuiteFileSection variablesSection) {
        this.variablesSection = variablesSection;
    }

    @Override
    public void execute() throws CommandExecutionException {
        Collections.sort(variablesSection.getChildren(), new Comparator<RobotElement>() {
            @Override
            public int compare(final RobotElement element1, final RobotElement element2) {
                return element1.getName().compareToIgnoreCase(element2.getName());
            }
        });

        eventBroker.post(RobotModelEvents.ROBOT_VARIABLES_SORTED, variablesSection);
    }
}
