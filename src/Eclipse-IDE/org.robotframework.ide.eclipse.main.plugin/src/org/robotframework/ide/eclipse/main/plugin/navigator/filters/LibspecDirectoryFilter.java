package org.robotframework.ide.eclipse.main.plugin.navigator.filters;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

public class LibspecDirectoryFilter extends ViewerFilter {

    @Override
    public boolean select(final Viewer viewer, final Object parentElement, final Object element) {
        if (element instanceof IFolder) {
            final IFolder folder = (IFolder) element;

            return !(folder.getName().equals("libspecs") && folder.getParent() instanceof IProject);
        }
        return true;
    }
}
