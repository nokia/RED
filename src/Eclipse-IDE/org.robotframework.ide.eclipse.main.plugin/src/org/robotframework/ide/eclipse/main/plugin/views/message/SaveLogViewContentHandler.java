/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.views.message;

import java.io.File;
import java.io.IOException;

import javax.inject.Named;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.ISources;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.views.message.SaveLogViewContentHandler.E4SaveLogViewContentHandler;
import org.robotframework.red.commands.DIParameterizedHandler;

import com.google.common.base.Charsets;
import com.google.common.io.Files;


public class SaveLogViewContentHandler extends DIParameterizedHandler<E4SaveLogViewContentHandler> {

    public SaveLogViewContentHandler() {
        super(E4SaveLogViewContentHandler.class);
    }

    public static class E4SaveLogViewContentHandler {

        @Execute
        public void save(final @Named(ISources.ACTIVE_PART_NAME) MessageLogViewWrapper view) {
            @SuppressWarnings("restriction")
            final MessageLogView msgLogView = view.getComponent();
            final StyledText control = msgLogView.getTextControl();

            final FileDialog dialog = new FileDialog(control.getShell(), SWT.SAVE);
            dialog.setOverwrite(true);
            dialog.setFilterPath(ResourcesPlugin.getWorkspace().getRoot().getLocation().toOSString());
            final String path = dialog.open();
            if (path == null) {
                return;
            }

            try {
                final String content = control.getText();
                Files.asCharSink(new File(path), Charsets.UTF_8).write(content);
            } catch (final IOException e) {
                ErrorDialog.openError(control.getShell(), "Error saving file",
                        "Unable to save Message Log view content to " + path + " file",
                        new Status(IStatus.ERROR, RedPlugin.PLUGIN_ID, "", e));
            }
        }
    }
}
