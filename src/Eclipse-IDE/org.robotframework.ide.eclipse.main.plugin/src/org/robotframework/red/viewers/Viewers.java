package org.robotframework.red.viewers;

import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.contexts.IContextActivation;
import org.eclipse.ui.contexts.IContextService;

public class Viewers {

    public static void boundViewerWithContext(final ColumnViewer viewer, final IWorkbenchSite site,
            final String contextId) {
        viewer.getControl().addFocusListener(new ContextActivatingFocusListener(contextId, site));
    }

    private static class ContextActivatingFocusListener implements FocusListener {
        private final String contextId;
        private final IWorkbenchSite site;
        private IContextActivation activationToken = null;

        private ContextActivatingFocusListener(final String contextId, final IWorkbenchSite site) {
            this.contextId = contextId;
            this.site = site;
        }

        @Override
        public void focusLost(final FocusEvent e) {
            getContextService(site).deactivateContext(activationToken);
        }

        @Override
        public void focusGained(final FocusEvent e) {
            activationToken = getContextService(site).activateContext(contextId);
        }

        private IContextService getContextService(final IWorkbenchSite site) {
            return (IContextService) site.getService(IContextService.class);
        }
    }
}
