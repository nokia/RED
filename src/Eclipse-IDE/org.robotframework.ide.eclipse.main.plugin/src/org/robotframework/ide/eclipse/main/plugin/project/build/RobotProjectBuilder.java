package org.robotframework.ide.eclipse.main.plugin.project.build;

import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

public class RobotProjectBuilder extends IncrementalProjectBuilder {

    @Override
    protected IProject[] build(final int kind, final Map<String, String> args, final IProgressMonitor monitor) throws CoreException {
        if (kind == FULL_BUILD) {
            new RobotProjectValidator().validate(getProject(), monitor);

            // get list of standard libraries
            // (?) get list of external libraries
            // for each standard library:
            //      generate spec
            // for each external library:
            //      generate spec
        }
        return new IProject[0];
    }

    @Override
    protected void clean(final IProgressMonitor monitor) throws CoreException {
        getProject().deleteMarkers(IMarker.PROBLEM, true, IResource.DEPTH_INFINITE);
    }

}
