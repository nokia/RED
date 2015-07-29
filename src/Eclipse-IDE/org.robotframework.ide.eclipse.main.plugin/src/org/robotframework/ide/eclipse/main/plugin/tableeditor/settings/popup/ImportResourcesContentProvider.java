package org.robotframework.ide.eclipse.main.plugin.tableeditor.settings.popup;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

class ImportResourcesContentProvider implements IStructuredContentProvider {

    @Override
    public void dispose() {
        // nothing to do
    }

    @Override
    public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
        // nothing to do
    }

    @Override
    public Object[] getElements(final Object inputElement) {
        final List<IPath> libraries = ((Settings) inputElement).getImportedResources();
        Collections.sort(libraries, new Comparator<IPath>() {

            @Override
            public int compare(final IPath spec1, final IPath spec2) {
                return spec1.lastSegment().compareTo(spec2.lastSegment());
            }
        });
        return libraries.toArray();
    }

}
