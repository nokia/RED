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
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.dnd.RedClipboard;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.handler.TableHandlersSupport;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.keywords.handler.CopyKeywordsHandler.E4CopyKeywordsHandler;
import org.robotframework.red.commands.DIParameterizedHandler;
import org.robotframework.red.viewers.Selections;

public class CopyKeywordsHandler extends DIParameterizedHandler<E4CopyKeywordsHandler> {

    public CopyKeywordsHandler() {
        super(E4CopyKeywordsHandler.class);
    }

    public static class E4CopyKeywordsHandler {

        @Execute
        public void copyKeywords(@Named(Selections.SELECTION) final IStructuredSelection selection,
                final RedClipboard clipboard) {

            final List<RobotKeywordDefinition> defs = Selections.getElements(selection, RobotKeywordDefinition.class);
            final List<RobotKeywordCall> calls = Selections.getElements(selection, RobotKeywordCall.class);

            if (!defs.isEmpty()) {
                final Object data = TableHandlersSupport.createKeywordDefsCopy(defs).toArray(new RobotKeywordDefinition[0]);
                clipboard.insertContent(data);

            } else if (!calls.isEmpty()) {
                final Object data = TableHandlersSupport.createKeywordCallsCopy(calls).toArray(new RobotKeywordCall[0]);
                clipboard.insertContent(data);
            }
        }
    }
}
