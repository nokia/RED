/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.cases;

import java.util.ArrayList;
import java.util.List;

import org.robotframework.ide.eclipse.main.plugin.model.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.SetKeywordCallArgumentCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.SetKeywordCallCommentCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.SetKeywordCallNameCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.cases.SetCaseNameCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;

/**
 * @author Michal Anglart
 *
 */
public class CasesTableValuesChangingCommandsCollector {

    public List<? extends EditorCommand> collectForRemoval(final RobotElement element, final int column,
            final int numberOfColumns) {
        return collect(element, null, column, numberOfColumns);
    }

    public List<? extends EditorCommand> collectForChange(final RobotElement element, final String newValue,
            final int column, final int numberOfColumns) {
        return collect(element, newValue, column, numberOfColumns);
    }

    private List<? extends EditorCommand> collect(final RobotElement element, final String value, final int column,
            final int numberOfColumns) {
        final List<EditorCommand> commands = new ArrayList<>();

        if (element instanceof RobotCase) {
            final RobotCase testCase = (RobotCase) element;
            if (column == 0) {
                commands.add(new SetCaseNameCommand(testCase, value));
            }
        } else if (element instanceof RobotKeywordCall) {
            final RobotKeywordCall call = (RobotKeywordCall) element;

            if (column == 0) {
                commands.add(new SetKeywordCallNameCommand(call, value));
            } else if (column > 0 && column < (numberOfColumns - 1)) {
                commands.add(new SetKeywordCallArgumentCommand(call, column - 1, value));
            } else {
                commands.add(new SetKeywordCallCommentCommand(call, value));
            }
        }
        return commands;
    }
    
}
