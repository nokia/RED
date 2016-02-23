/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.code.handler;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.code.handler.RenameKeywordsHandler.E4RenameKeywordsHandler;
import org.robotframework.red.commands.DIParameterizedHandler;
import org.robotframework.red.viewers.Selections;

public class RenameKeywordsHandler extends DIParameterizedHandler<E4RenameKeywordsHandler> {

    public RenameKeywordsHandler() {
        super(E4RenameKeywordsHandler.class);
    }

    public static class E4RenameKeywordsHandler {

        @Execute
        public Object renameKeywords(@Named(Selections.SELECTION) final IStructuredSelection selection) {
            throw new IllegalStateException("Not yet implemented!");
        }
    }
}
