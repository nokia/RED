/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.code;

import java.util.Optional;

import org.robotframework.ide.eclipse.main.plugin.model.RobotDefinitionSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.SetDocumentationSettingCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.SetKeywordCallArgumentCommand2;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.SetKeywordCallNameCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;

public class KeywordCallsTableValuesChangingCommandsCollector {

    public Optional<? extends EditorCommand> collect(final RobotElement element, final String value, final int column) {
        if (element instanceof RobotKeywordCall) {
            final RobotKeywordCall call = (RobotKeywordCall) element;

            final Optional<? extends EditorCommand> commentCommands = ExecutablesRowHolderCommentService
                    .wasHandledAsComment(call, value, column);
            if (commentCommands.isPresent()) {
                return commentCommands;
            }

            if (column == 0) {
                return Optional.of(new SetKeywordCallNameCommand(call, value));
            } else if (isDocumentationSetting(call)) {
                return Optional.of(new SetDocumentationSettingCommand((RobotDefinitionSetting) call, value));
            } else {
                return Optional.of(new SetKeywordCallArgumentCommand2(call, column - 1, value));
            }
        }
        return Optional.empty();
    }

    private boolean isDocumentationSetting(final RobotKeywordCall call) {
        return call instanceof RobotDefinitionSetting && ((RobotDefinitionSetting) call).isDocumentation();
    }
}
