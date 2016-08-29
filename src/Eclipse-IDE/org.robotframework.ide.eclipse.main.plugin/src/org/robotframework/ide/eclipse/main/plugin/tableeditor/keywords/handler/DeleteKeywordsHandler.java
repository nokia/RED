/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.keywords.handler;

import java.util.List;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.ISources;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.DeleteKeywordCallCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.DeleteKeywordDefinitionCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotFormEditor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.keywords.handler.DeleteKeywordsHandler.E4DeleteKeywordsHandler;
import org.robotframework.red.commands.DIParameterizedHandler;
import org.robotframework.red.viewers.Selections;

public class DeleteKeywordsHandler extends DIParameterizedHandler<E4DeleteKeywordsHandler> {

    public DeleteKeywordsHandler() {
        super(E4DeleteKeywordsHandler.class);
    }

    public static class E4DeleteKeywordsHandler {

        @Execute
        public void deleteKeywords(@Named(ISources.ACTIVE_EDITOR_NAME) final RobotFormEditor editor,
                final RobotEditorCommandsStack commandsStack,
                @Named(Selections.SELECTION) final IStructuredSelection selection) {
            
            final List<RobotKeywordCall> keywordCalls = Selections.getElements(selection, RobotKeywordCall.class);
            final List<RobotKeywordDefinition> keywordDefinitions = Selections.getElements(selection,
                    RobotKeywordDefinition.class);

            // it's not possible to have both lists non-empty (the handler is disabled in this situation)
            
            if (!keywordCalls.isEmpty()) {
                commandsStack.execute(new DeleteKeywordCallCommand(keywordCalls));
            } else if (!keywordDefinitions.isEmpty()) {
                commandsStack.execute(new DeleteKeywordDefinitionCommand(keywordDefinitions));
            }
            
            // needed for the same reason as in the cut handler
            editor.getSelectionLayerAccessor().clear();
        }
    }
}
