/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.console;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.SaveAsDialog;
import org.robotframework.ide.eclipse.main.plugin.RedImages;


/**
 * @author Michal Anglart
 *
 */
class SaveRedSessionAction extends Action {

    private final Shell shell;

    private RedSessionConsole console;

    SaveRedSessionAction(final Shell shell, final RedSessionConsole sessionConsole) {
        super("Save session output to file", IAction.AS_PUSH_BUTTON);
        setImageDescriptor(RedImages.getSaveImage());

        this.shell = shell;
        this.console = sessionConsole;
    }

    @Override
    public void run() {
        final SaveAsDialog dialog = new SaveAsDialog(shell);
        if (dialog.open() == Window.OK) {
            final IPath chosenPath = dialog.getResult();
            final IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(chosenPath);

            try {
                if (file.exists()) {
                    final boolean shouldOverride = MessageDialog.openQuestion(shell, "File already exist",
                            "The file " + chosenPath.toString() + "already exist. Do you want ot override it?");
                    if (shouldOverride) {
                        file.setContents(createStream(), IResource.NONE, null);
                    }
                } else {
                    file.create(createStream(), IResource.NONE, null);
                }
            } catch (final CoreException e) {
                MessageDialog.openError(shell, "Unable to save file",
                        "Problem when writing to file. Details:\n" + e.getMessage());
            }
        }

    }

    private InputStream createStream() {
        final String consoleOutput = console.getDocument().get();
        return new ByteArrayInputStream(consoleOutput.getBytes());
    }

    void dispose() {
        this.console = null;
    }
}
