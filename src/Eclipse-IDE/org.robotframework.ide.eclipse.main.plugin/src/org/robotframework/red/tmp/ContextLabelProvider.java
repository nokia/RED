/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.tmp;

import org.eclipse.jface.viewers.ColumnLabelProvider;

public class ContextLabelProvider extends ColumnLabelProvider {

    @Override
    public String getText(final Object element) {
        return element.toString();
    }
}
