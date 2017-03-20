/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor;

import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveListener;
import org.eclipse.ui.IWorkbenchPage;

class RobotFormEditorPerspectiveListener implements IPerspectiveListener {

    @Override
    public void perspectiveActivated(final IWorkbenchPage page, final IPerspectiveDescriptor perspective) {
        if (IDebugUIConstants.ID_DEBUG_PERSPECTIVE.equals(perspective.getId())) {
            RobotFormEditor.activateSourcePageInActiveEditor(page.getWorkbenchWindow());
        }
    }

    @Override
    public void perspectiveChanged(final IWorkbenchPage page, final IPerspectiveDescriptor perspective,
            final String changeId) {
        // nothing to do
    }
}
