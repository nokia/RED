/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.handler;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.ui.ISources;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.FocusedViewerAccessor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotFormEditor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.handler.CopyCellContentHandler.E4CopyCellContentHandler;
import org.robotframework.red.commands.DIParameterizedHandler;

public class CopyCellContentHandler extends DIParameterizedHandler<E4CopyCellContentHandler> {

    public CopyCellContentHandler() {
        super(E4CopyCellContentHandler.class);
    }

    public static class E4CopyCellContentHandler {

        @Execute
        public Object copyContent(final @Named(ISources.ACTIVE_EDITOR_NAME) RobotFormEditor editor,
                final Clipboard clipboard) {
            final FocusedViewerAccessor viewerAccessor = editor.getFocusedViewerAccessor();
            final String cellContent = viewerAccessor.getFocusedCell().getText();
            clipboard.setContents(new String[] { cellContent }, new Transfer[] { TextTransfer.getInstance() });

            return null;
        }
    }
}
