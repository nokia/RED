package org.robotframework.ide.eclipse.main.plugin.navigator;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Display;

public class NavigatorLibrariesContentProvider implements ITreeContentProvider {

    protected TreeViewer viewer;

    private final IResourceChangeListener listener;

    public NavigatorLibrariesContentProvider() {
        listener = new IResourceChangeListener() {
            @Override
            public void resourceChanged(final IResourceChangeEvent event) {
                if (event.getBuildKind() != 0) {
                    Display.getDefault().asyncExec(new Runnable() {
                        @Override
                        public void run() {
                            viewer.refresh();
                        }
                    });
                }
            }
        };
        ResourcesPlugin.getWorkspace().addResourceChangeListener(listener, IResourceChangeEvent.POST_BUILD);
    }

    @Override
    public void dispose() {
        ResourcesPlugin.getWorkspace().removeResourceChangeListener(listener);
    }

    @Override
    public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
        this.viewer = (TreeViewer) viewer;
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
        return element instanceof RobotProjectDependencies;
    }

}
