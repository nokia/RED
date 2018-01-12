/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.jface.dialogs;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Shell;

class CloseDialogAction extends Action {

    private static final String ACTION_TOOLTIP_NAME = "Close";

    private final Shell shell;

    CloseDialogAction(final Shell shell, final ImageDescriptor image) {
        super(ACTION_TOOLTIP_NAME, AS_PUSH_BUTTON);
        setImageDescriptor(image);
        this.shell = shell;
    }

    @Override
    public void run() {
        shell.close();
    }
}
