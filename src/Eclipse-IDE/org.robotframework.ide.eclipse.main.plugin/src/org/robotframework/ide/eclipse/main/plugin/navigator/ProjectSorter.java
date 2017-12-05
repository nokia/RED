/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.navigator;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;

public class ProjectSorter extends ViewerComparator {

    @Override
    public int category(final Object element) {
        return element instanceof RobotProjectExternalDependencies ? 1 : 0;
    }

    @Override
    public int compare(final Viewer viewer, final Object e1, final Object e2) {
        return category(e1) - category(e2);
    }
}
