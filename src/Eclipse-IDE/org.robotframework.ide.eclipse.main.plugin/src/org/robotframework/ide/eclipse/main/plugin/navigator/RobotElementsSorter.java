/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.navigator;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;

public class RobotElementsSorter extends ViewerComparator {

    @Override
    public int category(final Object element) {
        return 0;
    }

    @Override
    public int compare(final Viewer viewer, final Object e1, final Object e2) {
        final int cat1 = category(e1);
        final int cat2 = category(e2);

        if (cat1 != cat2) {
            return cat1 - cat2;
        }
        final RobotElement el1 = (RobotElement) e1;
        final RobotElement el2 = (RobotElement) e2;

        final int index1 = el1.getParent().getChildren().indexOf(el1);
        final int index2 = el2.getParent().getChildren().indexOf(el2);

        return index1 - index2;
    }
}
