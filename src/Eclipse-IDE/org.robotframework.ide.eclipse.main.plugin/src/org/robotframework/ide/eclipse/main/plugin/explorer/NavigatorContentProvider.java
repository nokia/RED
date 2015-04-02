package org.robotframework.ide.eclipse.main.plugin.explorer;

import java.io.IOException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.robotframework.ide.eclipse.main.plugin.tempmodel.FileSectionsParser;

public class NavigatorContentProvider implements ITreeContentProvider {

	@Override
	public void dispose() {
	}

	@Override
	public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
	}

	@Override
	public Object[] getElements(final Object inputElement) {
		return null;
	}

	@Override
    public Object[] getChildren(final Object parentElement) {
        try {
            return new FileSectionsParser().parseSections((IFile) parentElement);
        } catch (CoreException | IOException e) {
            return new Object[0];
        }
    }

	@Override
	public Object getParent(final Object element) {
		return null;
	}

	@Override
	public boolean hasChildren(final Object element) {
		return false;
	}

}
