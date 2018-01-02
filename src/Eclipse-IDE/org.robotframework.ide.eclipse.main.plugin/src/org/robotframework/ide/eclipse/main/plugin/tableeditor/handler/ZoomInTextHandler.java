package org.robotframework.ide.eclipse.main.plugin.tableeditor.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.texteditor.TextZoomInHandler;


public class ZoomInTextHandler extends AbstractHandler {

    @Override
    public Object execute(final ExecutionEvent event) throws ExecutionException {
        return new TextZoomInHandler().execute(event);
    }
}
