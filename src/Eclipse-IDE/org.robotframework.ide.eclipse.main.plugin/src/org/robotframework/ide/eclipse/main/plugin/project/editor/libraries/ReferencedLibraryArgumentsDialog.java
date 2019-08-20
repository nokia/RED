/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.editor.libraries;

import org.eclipse.swt.widgets.Shell;
import org.rf.ide.core.project.RobotProjectConfig.ReferencedLibraryArgumentsVariant;

public class ReferencedLibraryArgumentsDialog extends AddNewElementDialog<ReferencedLibraryArgumentsVariant> {

    public ReferencedLibraryArgumentsDialog(final Shell parentShell) {
        super(parentShell);
    }

    @Override
    protected String getDialogTitle() {
        return "Add arguments";
    }

    @Override
    protected String getInfoText() {
        return "Specify arguments for library constructor which will be used to generate library specifications. "
                + "Multiple arguments should be separated by :: separator.";
    }

    @Override
    protected String getTextLabel() {
        return "Arguments";
    }

    @Override
    protected String getDefaultText() {
        return "";
    }

    @Override
    protected void validate(final String text,
            final AddNewElementDialog<ReferencedLibraryArgumentsVariant>.LocalValidationCallback callback) {
        callback.passed();
    }

    @Override
    protected ReferencedLibraryArgumentsVariant createElement(final String text) {
        return ReferencedLibraryArgumentsVariant.create(text.split("::"));
    }
}
