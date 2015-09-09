/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.navigator;

import java.text.Collator;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

public class ProjectSorter extends ViewerSorter {

    public ProjectSorter() {
        // nothing to do
    }

    public ProjectSorter(final Collator collator) {
        super(collator);
    }

    @Override
    public int category(final Object element) {
        return element instanceof RobotProjectExternalDependencies ? 1 : 0;
    }

    @Override
    public int compare(final Viewer viewer, final Object e1, final Object e2) {
        return category(e1) - category(e2);
    }
}
