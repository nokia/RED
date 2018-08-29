/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.jface.dialogs;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Shell;
import org.rf.ide.core.RedTemporaryDirectory;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;

public class ScriptExportDialog {

    private final Shell parent;

    private final String scriptName;

    public ScriptExportDialog(final Shell parent, final String scriptName) {
        this.parent = parent;
        this.scriptName = scriptName;
    }

    public void open() {
        final DirectoryDialog dirDialog = new DirectoryDialog(parent);
        dirDialog.setMessage("Choose \"" + scriptName + "\" export destination.");
        final String dir = dirDialog.open();
        if (dir != null) {
            exportScriptFile(dir);
        }
    }

    private void exportScriptFile(final String dir) {
        final File scriptFile = new File(dir + File.separator + scriptName);
        try {
            try (InputStream source = RedTemporaryDirectory.getScriptFileAsStream(scriptName)) {
                Files.copy(source, scriptFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (final IOException e) {
            final String message = "Unable to copy file to " + scriptFile.getAbsolutePath();
            ErrorDialog.openError(parent, "File copy problem", message,
                    new Status(IStatus.ERROR, RedPlugin.PLUGIN_ID, message, e));
        }
    }

}
