package org.robotframework.tmp;

import java.util.Collection;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.contexts.IContextService;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;

public class ContextContentProvider implements IStructuredContentProvider {

    @Override
    public void dispose() {
        // TODO Auto-generated method stub

    }

    @Override
    public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
        // TODO Auto-generated method stub

    }

    @SuppressWarnings("unchecked")
    @Override
    public Object[] getElements(final Object inputElement) {
        final IContextService service = (IContextService) PlatformUI.getWorkbench().getService(IContextService.class);
        return filter(service.getActiveContextIds()).toArray();
    }

    private Collection<String> filter(final Collection<String> activeContextIds) {
        return Collections2.filter(activeContextIds, new Predicate<String>() {

            @Override
            public boolean apply(final String contextId) {
                return contextId.startsWith("org.robot");
            }
        });
    }
}
