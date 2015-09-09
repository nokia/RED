/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.tmp;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;

public class ModelLabelProvider extends ColumnLabelProvider {

    @Override
    public String getText(final Object element) {
        return ((RobotElement) element).getName();
    }
}
