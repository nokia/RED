/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.keywords.handler;

import org.rf.ide.core.testdata.model.ModelType;
import org.robotframework.ide.eclipse.main.plugin.model.RobotDefinitionSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.SetKeywordCallArgumentCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.SetKeywordCallCommentCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.SetKeywordCallNameCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.SetKeywordSettingArgumentCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.keywords.SetKeywordDefinitionArgumentCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.keywords.SetKeywordDefinitionNameCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.handler.E4DeleteInTableHandler;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.keywords.handler.DeleteInKeywordTableHandler.E4DeleteInKeywordTableHandler;
import org.robotframework.red.commands.DIParameterizedHandler;

public class DeleteInKeywordTableHandler extends DIParameterizedHandler<E4DeleteInKeywordTableHandler> {

    public DeleteInKeywordTableHandler() {
        super(E4DeleteInKeywordTableHandler.class);
    }

    public static class E4DeleteInKeywordTableHandler extends E4DeleteInTableHandler {

        @Override
        protected EditorCommand getCommandForSelectedElement(final RobotElement selectedElement, final int columnIndex,
                final int tableColumnCount) {
            if (selectedElement instanceof RobotKeywordCall) {
                final RobotKeywordCall keywordCall = (RobotKeywordCall) selectedElement;
                if (keywordCall.getLinkedElement().getModelType() == ModelType.USER_KEYWORD_EXECUTABLE_ROW) {
                    if (columnIndex == 0) {
                        return new SetKeywordCallNameCommand(keywordCall, "");
                    } else if (columnIndex > 0 && columnIndex < tableColumnCount - 1) {
                        return new SetKeywordCallArgumentCommand(keywordCall, columnIndex - 1, null);
                    } else if (columnIndex == tableColumnCount - 1) {
                        return new SetKeywordCallCommentCommand(keywordCall, null);
                    }
                } else {
                    if (columnIndex > 0 && columnIndex < tableColumnCount - 1) {
                        return new SetKeywordSettingArgumentCommand(keywordCall, columnIndex - 1, null);
                    } else if (columnIndex == tableColumnCount - 1) {
                        return new SetKeywordCallCommentCommand(keywordCall, null);
                    }
                }
            } else if (selectedElement instanceof RobotKeywordDefinition) {
                final RobotKeywordDefinition keywordDef = (RobotKeywordDefinition) selectedElement;
                if (columnIndex == 0) {
                    return new SetKeywordDefinitionNameCommand(keywordDef, "\\");
                } else if (columnIndex > 0 && columnIndex < tableColumnCount - 1) {
                    final RobotDefinitionSetting argumentsSetting = keywordDef.getArgumentsSetting();
                    if (argumentsSetting != null && columnIndex - 1 < argumentsSetting.getArguments().size()) {
                        return new SetKeywordDefinitionArgumentCommand(keywordDef, columnIndex - 1, null);
                    }
                }
            }
            return null;
        }
    }
}
