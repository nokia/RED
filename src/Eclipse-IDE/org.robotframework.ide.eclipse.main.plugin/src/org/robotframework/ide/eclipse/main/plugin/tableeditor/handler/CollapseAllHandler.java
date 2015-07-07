package org.robotframework.ide.eclipse.main.plugin.tableeditor.handler;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.tools.compat.parts.DIHandler;
import org.eclipse.jface.viewers.TreeViewer;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.FocusedViewerAccessor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.handler.CollapseAllHandler.E4CollapseAllHandler;

public class CollapseAllHandler extends DIHandler<E4CollapseAllHandler> {

    public CollapseAllHandler() {
        super(E4CollapseAllHandler.class);
    }

    public static class E4CollapseAllHandler {
        @Execute
        public Object collapseAll(final FocusedViewerAccessor viewerAccessor) {
            final TreeViewer viewer = (TreeViewer) viewerAccessor.getViewer();
            viewer.getTree().setRedraw(false);
            viewer.collapseAll();
            viewer.getTree().setRedraw(true);
            return null;
        }

    }

}
