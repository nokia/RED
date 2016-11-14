/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.mockeclipse;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;


public class WrappedResource implements IAdaptable {

    private final IResource resourceToWrap;

    public WrappedResource(final IResource resourceToWrap) {
        this.resourceToWrap = resourceToWrap;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object getAdapter(@SuppressWarnings("rawtypes") final Class adapter) {
        if (adapter.isInstance(resourceToWrap)) {
            return adapter.cast(resourceToWrap);
        }
        return null;
    }
}
