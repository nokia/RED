package org.robotframework.ide.eclipse.main.plugin.navigator;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

public class NavigatorLibrariesContentProvider implements ITreeContentProvider {

    @Override
    public void dispose() {
        // TODO Auto-generated method stub

    }

    @Override
    public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
        // TODO Auto-generated method stub

    }

    @Override
    public Object[] getElements(final Object inputElement) {
        return null;
    }

    @Override
    public Object[] getChildren(final Object parentElement) {
        if (parentElement instanceof IProject) {
            return new Object[] { new RobotProjectDependencies((IProject) parentElement) };
        } else if (parentElement instanceof RobotProjectDependencies) {
            return ((RobotProjectDependencies) parentElement).getStandardLibraries().toArray();
        }
        return new Object[0];
    }

    @Override
    public Object getParent(final Object element) {
        return null;
    }

    @Override
    public boolean hasChildren(final Object element) {
        return true;
    }

}
