/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.wizards;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.dialogs.WizardResourceImportPage;


/**
 * @author Michal Anglart
 *
 */
public class WizardImportRobotProjectPage extends WizardResourceImportPage {

    protected WizardImportRobotProjectPage() {
        super("import", StructuredSelection.EMPTY);
    }

    @Override
    protected void createSourceGroup(final Composite parent) {
        // TODO Auto-generated method stub
    }

    @Override
    protected ITreeContentProvider getFileProvider() {
        return null;
    }

    @Override
    protected ITreeContentProvider getFolderProvider() {
        return null;
    }

}
