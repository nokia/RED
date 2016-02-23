/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.code.handler;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.code.handler.ExtractKeywordHandler.E4ExtractKeywordHandler;
import org.robotframework.red.commands.DIParameterizedHandler;
import org.robotframework.red.viewers.Selections;

public class ExtractKeywordHandler extends DIParameterizedHandler<E4ExtractKeywordHandler> {

    public ExtractKeywordHandler() {
        super(E4ExtractKeywordHandler.class);
    }

    public static class E4ExtractKeywordHandler {

        @Execute
        public Object extractKeyword(@Named(Selections.SELECTION) final IStructuredSelection selection) {
            throw new IllegalStateException("Not yet implemented!");
        }
    }
}
