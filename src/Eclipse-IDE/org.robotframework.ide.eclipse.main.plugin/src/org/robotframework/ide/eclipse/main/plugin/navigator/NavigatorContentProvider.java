package org.robotframework.ide.eclipse.main.plugin.navigator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.robotframework.ide.eclipse.main.plugin.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.RobotFramework;

public class NavigatorContentProvider implements ITreeContentProvider, IResourceChangeListener {

    private Viewer viewer;

    public NavigatorContentProvider() {
        ResourcesPlugin.getWorkspace().addResourceChangeListener(this, IResourceChangeEvent.POST_CHANGE);
    }

	@Override
	public void dispose() {
        ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
	}

	@Override
	public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
        this.viewer = viewer;
	}

	@Override
	public Object[] getElements(final Object inputElement) {
		return null;
	}

	@Override
    public Object[] getChildren(final Object parentElement) {
        if (parentElement instanceof IFile) {
            return RobotFramework.getModelManager().createSuiteFile((IFile) parentElement).getSections().toArray();
        } else if (parentElement instanceof RobotElement) {
            return ((RobotElement) parentElement).getChildren().toArray();
        }
        return new Object[0];
    }

	@Override
	public Object getParent(final Object element) {
        if (element instanceof RobotElement) {
            return ((RobotElement) element).getParent();
        }
		return null;
	}

	@Override
	public boolean hasChildren(final Object element) {
        if (element instanceof RobotElement) {
            return !((RobotElement) element).getChildren().isEmpty();
        }
		return false;
	}

    @Override
    public void resourceChanged(final IResourceChangeEvent event) {
        if (viewer != null) {
            viewer.getControl().getDisplay().asyncExec(new Runnable() {
                @Override
                public void run() {
                    viewer.refresh();
                }
            });
        }
    }
}
