package org.robotframework.ide.eclipse.main.plugin.tableeditor.handler;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.tools.compat.parts.DIHandler;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.FocusedViewerAccessor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.handler.CopyCellContentHandler.E4CopyCellContentHandler;

public class CopyCellContentHandler extends DIHandler<E4CopyCellContentHandler> {

    public CopyCellContentHandler() {
        super(E4CopyCellContentHandler.class);
    }

    public static class E4CopyCellContentHandler {

        @Execute
        public Object copyContent(final FocusedViewerAccessor viewerAccessor, final Clipboard clipboard) {
            final String cellContent = viewerAccessor.getFocusedCell().getText();
            clipboard.setContents(new String[] { cellContent }, new Transfer[] { TextTransfer.getInstance() });

            return null;
        }
    }
}
