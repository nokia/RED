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

    private final DialogCloseListener listener;

    CloseDialogAction(final Shell shell, final ImageDescriptor image) {
        this(shell, image, new DialogCloseListener());
    }

    CloseDialogAction(final Shell shell, final ImageDescriptor image, final DialogCloseListener listener) {
        super(ACTION_TOOLTIP_NAME, AS_PUSH_BUTTON);
        setImageDescriptor(image);
        this.shell = shell;
        this.listener = listener;
    }

    @Override
    public void run() {
        listener.beforeClose();
        shell.close();
        listener.afterClose();
    }

    public static class DialogCloseListener {

        public void beforeClose() {
            // nothing to do
        }

        public void afterClose() {
            // nothing to do
        }
    }
}
