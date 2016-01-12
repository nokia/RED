/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.ui.PlatformUI;

public class DIParameterizedHandler<C> extends AbstractHandler {

    private final C component;

    public DIParameterizedHandler(final Class<C> clazz) {
        component = ContextInjectionFactory.make(clazz, getActiveContext());
    }

    private static IEclipseContext getActiveContext() {
        return getParentContext().getActiveLeaf();
    }

    private static IEclipseContext getParentContext() {
        return (IEclipseContext) PlatformUI.getWorkbench().getService(IEclipseContext.class);
    }

    @Override
    public Object execute(final ExecutionEvent event) throws ExecutionException {
        final IEclipseContext child = getActiveContext().createChild();
        
        for (final Object key : event.getParameters().keySet()) {
            if (key instanceof String) {
                child.set((String) key, event.getParameters().get(key));
            }
        }
        return ContextInjectionFactory.invoke(component, Execute.class, child);
    }
}
