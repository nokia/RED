/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.handler;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.ISources;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotFormEditor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.handler.OpenDebugTestMenuHandler.E4OpenDebugTestMenuHandler;
import org.robotframework.red.actions.QuickMenuRegistrator;
import org.robotframework.red.commands.DIParameterizedHandler;


public class OpenDebugTestMenuHandler extends DIParameterizedHandler<E4OpenDebugTestMenuHandler> {

    public OpenDebugTestMenuHandler() {
        super(E4OpenDebugTestMenuHandler.class);
    }

    public static class E4OpenDebugTestMenuHandler {
        @Execute
        public Object openMenu(final @Named(ISources.ACTIVE_EDITOR_NAME) RobotFormEditor editor) {
            final SourceViewer viewer = editor.getSourceEditor().getViewer();
            final IEditorSite site = editor.getEditorSite();

            final QuickMenuRegistrator menu = new QuickMenuRegistrator(site, viewer,
                    "org.robotframework.red.source.editor.runSingleDebug");
            menu.createMenu();

            return null;
        }
    }
}
