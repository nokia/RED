/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see licence.txt file for details.
 */
package org.robotframework.red.tmp;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;

public class ModelContentProvider implements ITreeContentProvider {

    @Override
    public void dispose() {
        // TODO Auto-generated method stub

    }

    @Override
    public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
        // TODO Auto-generated method stub

    }

    @Override
    public Object[] getElements(final Object inputElement) {
        return ((RobotElement) inputElement).getChildren().toArray();
    }

    @Override
    public Object[] getChildren(final Object parentElement) {
        return ((RobotElement) parentElement).getChildren().toArray();
    }

    @Override
    public Object getParent(final Object element) {
        return ((RobotElement) element).getParent();
    }

    @Override
    public boolean hasChildren(final Object element) {
        return true;
    }

}
