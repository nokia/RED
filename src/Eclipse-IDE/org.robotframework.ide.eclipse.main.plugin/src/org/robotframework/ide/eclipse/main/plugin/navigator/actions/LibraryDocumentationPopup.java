/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.navigator.actions;

import org.eclipse.e4.ui.css.swt.theme.IThemeEngine;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.rf.ide.core.libraries.LibrarySpecification;
import org.robotframework.red.jface.dialogs.InputLoadingFormComposite;
import org.robotframework.red.jface.dialogs.RobotPopupDialog;

@SuppressWarnings("restriction")
class LibraryDocumentationPopup extends RobotPopupDialog {

    private InputLoadingFormComposite composite;

    private final LibrarySpecification specification;

    LibraryDocumentationPopup(final Shell parent, final IThemeEngine engine, final LibrarySpecification spec) {
        super(parent, engine);
        this.specification = spec;
    }

    @Override
    protected Control createDialogControls(final Composite parent) {
        composite = new LibraryDocumentationComposite(parent, specification);
        return composite;
    }

    @Override
    protected Control getFocusControl() {
        return composite.getFocusControl();
    }
}
