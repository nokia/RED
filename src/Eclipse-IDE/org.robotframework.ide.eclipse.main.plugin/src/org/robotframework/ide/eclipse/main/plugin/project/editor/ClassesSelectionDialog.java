/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.editor;

import java.util.List;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;

/**
 * @author Michal Anglart
 *
 */
public class ClassesSelectionDialog {

    static ElementListSelectionDialog create(final Shell shell, final List<?> classes,
            final LabelProvider labelProvider) {
        final ElementListSelectionDialog classesDialog = new ElementListSelectionDialog(shell, labelProvider);
        classesDialog.setMultipleSelection(true);
        classesDialog.setTitle("Select library class");
        classesDialog.setMessage("Select the class(es) which defines library:");
        classesDialog.setElements(classes.toArray());

        return classesDialog;
    }
}
