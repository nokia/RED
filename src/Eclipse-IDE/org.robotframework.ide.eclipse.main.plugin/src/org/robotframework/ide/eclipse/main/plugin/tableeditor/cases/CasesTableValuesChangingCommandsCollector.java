/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.cases;

import java.util.Optional;

import org.robotframework.ide.eclipse.main.plugin.model.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.cases.SetCaseNameCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.code.KeywordCallsTableValuesChangingCommandsCollector;

/**
 * @author Michal Anglart
 *
 */
public class CasesTableValuesChangingCommandsCollector {

    public Optional<? extends EditorCommand> collectForRemoval(final RobotElement element, final int column) {
        return collect(element, null, column);
    }

    public Optional<? extends EditorCommand> collectForChange(final RobotElement element, final String newValue,
            final int column) {
        return collect(element, newValue, column);
    }

    private Optional<? extends EditorCommand> collect(final RobotElement element, final String value,
            final int column) {
        if (element instanceof RobotCase) {
            final RobotCase testCase = (RobotCase) element;
            return column == 0 ? Optional.of(new SetCaseNameCommand(testCase, value)) : Optional.empty();
        } else {
            return new KeywordCallsTableValuesChangingCommandsCollector().collect(element, value, column);
        }
    }
}
