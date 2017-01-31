/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.viewers;

import java.util.List;

public class ListInputStructuredContentProvider extends StructuredContentProvider {

    @Override
    public Object[] getElements(final Object inputElement) {
        return ((List<?>) inputElement).toArray();
    }
}
