/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd;

import java.util.List;
import java.util.Objects;

import org.rf.ide.core.testdata.model.IDocumentationHolder;
import org.rf.ide.core.testdata.model.presenter.DocumentationServiceHandler;
import org.robotframework.ide.eclipse.main.plugin.model.RobotDefinitionSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;


public class SetDocumentationSettingCommand extends EditorCommand {

    private final RobotDefinitionSetting docSetting;

    private final String value;

    private String oldDoc;

    public SetDocumentationSettingCommand(final RobotDefinitionSetting docSetting, final String value) {
        this.docSetting = docSetting;
        this.value = value;
    }

    @Override
    public void execute() throws CommandExecutionException {
        final IDocumentationHolder docHolder = (IDocumentationHolder) docSetting.getLinkedElement();
        oldDoc = DocumentationServiceHandler.toEditConsolidated(docHolder);

        if (!Objects.equals(value, oldDoc)) {
            DocumentationServiceHandler.update(docHolder, value);

            eventBroker.send(RobotModelEvents.ROBOT_KEYWORD_CALL_ARGUMENT_CHANGE, docSetting);
        }
    }

    @Override
    public List<EditorCommand> getUndoCommands() {
        return newUndoCommands(new SetDocumentationSettingCommand(docSetting, oldDoc));
    }
}
