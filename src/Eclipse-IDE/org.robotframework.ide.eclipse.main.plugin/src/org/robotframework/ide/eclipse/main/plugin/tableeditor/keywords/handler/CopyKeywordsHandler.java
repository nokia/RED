/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.keywords.handler;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.dnd.ArraysSerializerDeserializer;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.dnd.RedClipboard;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.keywords.handler.CopyKeywordsHandler.E4CopyKeywordsHandler;
import org.robotframework.red.commands.DIParameterizedHandler;
import org.robotframework.red.viewers.Selections;

public class CopyKeywordsHandler extends DIParameterizedHandler<E4CopyKeywordsHandler> {

    public CopyKeywordsHandler() {
        super(E4CopyKeywordsHandler.class);
    }

    public static class E4CopyKeywordsHandler {

        @Execute
        public boolean copyKeywords(@Named(Selections.SELECTION) final IStructuredSelection selection,
                final RedClipboard clipboard) {

            final RobotKeywordDefinition[] defs = Selections.getElementsArray(selection, RobotKeywordDefinition.class);
            final RobotKeywordCall[] calls = Selections.getElementsArray(selection, RobotKeywordCall.class);

            if (defs.length > 0) {
                final Object data = ArraysSerializerDeserializer.copy(RobotKeywordDefinition.class, defs);
                clipboard.insertContent(data);
                return true;

            } else if (calls.length > 0) {
                final Object data = ArraysSerializerDeserializer.copy(RobotKeywordCall.class, calls);
                clipboard.insertContent(data);
                return true;
            }
            return false;
        }
    }
}
