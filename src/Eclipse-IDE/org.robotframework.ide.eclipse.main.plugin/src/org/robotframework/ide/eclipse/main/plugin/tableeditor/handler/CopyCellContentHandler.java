/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.handler;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.ui.ISources;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.FocusedViewerAccessor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotFormEditor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.dnd.RedClipboard;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.handler.CopyCellContentHandler.E4CopyCellContentHandler;
import org.robotframework.red.commands.DIParameterizedHandler;

public class CopyCellContentHandler extends DIParameterizedHandler<E4CopyCellContentHandler> {

    public CopyCellContentHandler() {
        super(E4CopyCellContentHandler.class);
    }

    public static class E4CopyCellContentHandler {

        @Execute
        public void copyContent(final @Named(ISources.ACTIVE_EDITOR_NAME) RobotFormEditor editor,
                final RedClipboard clipboard) {
            final FocusedViewerAccessor viewerAccessor = editor.getFocusedViewerAccessor();
            final String cellContent = viewerAccessor.getFocusedCell().getText();

            clipboard.insertContent(cellContent);
        }
    }
}
