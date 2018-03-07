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
import org.rf.ide.core.libraries.KeywordSpecification;
import org.robotframework.red.jface.dialogs.InputLoadingFormComposite;
import org.robotframework.red.jface.dialogs.RobotPopupDialog;

@SuppressWarnings("restriction")
public class KeywordDocumentationPopup extends RobotPopupDialog {

    public static final String POPUP_TEXT = "Keyword Documentation";

    private InputLoadingFormComposite composite;
    private final KeywordSpecification specification;

    public KeywordDocumentationPopup(final Shell parent, final IThemeEngine engine, final KeywordSpecification spec) {
        super(parent, engine);
        this.specification = spec;
    }

    @Override
    protected Control createDialogControls(final Composite parent) {
        parent.getShell().setText(POPUP_TEXT);
        composite = new KeywordDocumentationComposite(parent, specification);
        return composite;
    }

    @Override
    protected Control getFocusControl() {
        return composite.getFocusControl();
    }
}
