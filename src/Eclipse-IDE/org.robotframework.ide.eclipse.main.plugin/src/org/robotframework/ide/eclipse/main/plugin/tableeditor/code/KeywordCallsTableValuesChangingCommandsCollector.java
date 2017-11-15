/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.code;

import java.util.ArrayList;
import java.util.List;

import org.robotframework.ide.eclipse.main.plugin.model.RobotDefinitionSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.SetDocumentationSettingCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.SetKeywordCallArgumentCommand2;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.SetKeywordCallNameCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;

public class KeywordCallsTableValuesChangingCommandsCollector {

    public List<? extends EditorCommand> collect(final RobotElement element, final String value, final int column) {
        final List<EditorCommand> commands = new ArrayList<>();

        if (element instanceof RobotKeywordCall) {
            final RobotKeywordCall call = (RobotKeywordCall) element;

            if (ExecutablesRowHolderCommentService.wasHandledAsComment(commands, call, value, column)) {
                return commands;
            }

            if (column == 0) {
                commands.add(new SetKeywordCallNameCommand(call, value));
            } else if (isDocumentationSetting(call)) {
                commands.add(new SetDocumentationSettingCommand((RobotDefinitionSetting) call, value));
            } else {
                commands.add(new SetKeywordCallArgumentCommand2(call, column - 1, value));
            }
        }
        return commands;
    }

    private boolean isDocumentationSetting(final RobotKeywordCall call) {
        return call instanceof RobotDefinitionSetting && ((RobotDefinitionSetting) call).isDocumentation();
    }
}
