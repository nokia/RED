package org.robotframework.ide.eclipse.main.plugin.navigator;

import javax.inject.Inject;

import org.eclipse.core.resources.IFile;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.PlatformUI;
import org.robotframework.ide.eclipse.main.plugin.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.RobotElementChange;
import org.robotframework.ide.eclipse.main.plugin.RobotElementChange.Kind;
import org.robotframework.ide.eclipse.main.plugin.RobotFramework;
import org.robotframework.ide.eclipse.main.plugin.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.RobotSuiteFile;

public class NavigatorContentProvider implements ITreeContentProvider {

    private Viewer viewer;

    public NavigatorContentProvider() {
        ContextInjectionFactory.inject(this, getContext());
    }

	@Override
	public void dispose() {
        ContextInjectionFactory.uninject(this, getContext());
	}

    private IEclipseContext getContext() {
        return (IEclipseContext) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getService(IEclipseContext.class);
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
        return true;
	}

    @SuppressWarnings("unused")
    @Inject
    @Optional
    private void whenSectionChanges(
            @UIEventTopic(RobotModelEvents.ROBOT_SUITE_FILE_ALL) final RobotSuiteFile affectedFile) {
        if (viewer != null) {
            viewer.refresh();
        }
    }

    @Inject
    @Optional
    private void whenSectionChanges(@UIEventTopic(RobotModelEvents.ROBOT_MODEL) final RobotElementChange change) {
        if (change.getElement() instanceof RobotSuiteFile && change.getKind() == Kind.CHANGED && viewer != null) {
            viewer.refresh();
        }
    }
}
