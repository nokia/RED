/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor;

import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPart;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotArtifactsValidator;

class RobotFormEditorPartListener implements IPartListener {

    @Override
    public void partOpened(IWorkbenchPart part) {
    }

    @Override
    public void partDeactivated(IWorkbenchPart part) {
    }

    @Override
    public void partClosed(IWorkbenchPart part) {
    }

    @Override
    public void partBroughtToTop(IWorkbenchPart part) {
    }

    @Override
    public void partActivated(IWorkbenchPart part) {
        if (part instanceof RobotFormEditor) {
            final RobotFormEditor editor = (RobotFormEditor) part;
            RobotArtifactsValidator.revalidate(editor.provideSuiteModel());
        }
    }
}
