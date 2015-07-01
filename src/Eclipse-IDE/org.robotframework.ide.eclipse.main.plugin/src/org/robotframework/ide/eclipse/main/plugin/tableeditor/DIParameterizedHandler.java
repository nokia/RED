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
        final IEclipseContext context = getActiveContext();
        component = ContextInjectionFactory.make(clazz, context);
    }

    private static IEclipseContext getActiveContext() {
        final IEclipseContext parentContext = getParentContext();
        return parentContext.getActiveLeaf();
    }

    private static IEclipseContext getParentContext() {
        return (IEclipseContext) PlatformUI.getWorkbench().getService(IEclipseContext.class);
    }

    @Override
    public Object execute(final ExecutionEvent event) throws ExecutionException {
        final IEclipseContext activeContext = getActiveContext();
        final IEclipseContext child = activeContext.createChild();
        
        for (final Object key : event.getParameters().keySet()) {
            if (key instanceof String) {
                child.set((String) key, event.getParameters().get(key));
            }
        }
        return ContextInjectionFactory.invoke(component, Execute.class, child);
    }
}
