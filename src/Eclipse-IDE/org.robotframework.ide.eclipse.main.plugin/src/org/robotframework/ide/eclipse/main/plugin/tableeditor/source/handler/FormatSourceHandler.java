/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.handler;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.ISources;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotFormEditor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.handler.FormatSourceHandler.E4FormatSourceHandler;
import org.robotframework.red.commands.DIParameterizedHandler;

/**
 * @author Michal Anglart
 */
public class FormatSourceHandler extends DIParameterizedHandler<E4FormatSourceHandler> {

    public FormatSourceHandler() {
        super(E4FormatSourceHandler.class);
    }

    public static class E4FormatSourceHandler {

        @Execute
        public Object formatSource(final @Named(ISources.ACTIVE_EDITOR_NAME) RobotFormEditor editor) {
            editor.getSourceEditor().getViewer().doOperation(ISourceViewer.FORMAT);

            return null;
        }
    }
}
