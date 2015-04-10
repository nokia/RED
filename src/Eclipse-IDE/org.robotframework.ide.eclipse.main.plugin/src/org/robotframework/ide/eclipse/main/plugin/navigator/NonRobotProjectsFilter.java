package org.robotframework.ide.eclipse.main.plugin.navigator;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.robotframework.ide.eclipse.main.plugin.nature.RobotProjectNature;

public class NonRobotProjectsFilter extends ViewerFilter {

    @Override
    public boolean select(final Viewer viewer, final Object parentElement, final Object element) {
        if (element instanceof IProject) {
            final IProject project = (IProject) element;
            return !project.isOpen() || RobotProjectNature.hasRobotNature(project);
        }
        return true;
    }

}
