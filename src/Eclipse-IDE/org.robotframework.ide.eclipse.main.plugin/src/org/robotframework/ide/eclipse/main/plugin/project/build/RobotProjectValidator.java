package org.robotframework.ide.eclipse.main.plugin.project.build;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectNature;

class RobotProjectValidator {

    public void validate(final IProject project, final IProgressMonitor monitor) {
        if (monitor.isCanceled()) {
            return;
        }

        if (!RobotProjectNature.hasRobotNature(project)) {
            monitor.worked(1);
            return;
        }
        monitor.subTask("Validating Robot project: " + project.getName());
        
        checkBuildpathFile(project);

        monitor.worked(1);
    }

    private void checkBuildpathFile(final IProject project) {
        final IFile buildpathFile = project.getFile(RobotProjectNature.BUILDPATH_FILE);
        if (buildpathFile == null || !buildpathFile.exists()) {
            try {
                final IMarker marker = project.createMarker(IMarker.PROBLEM);
                marker.setAttribute(IMarker.MESSAGE, "Missing " + RobotProjectNature.BUILDPATH_FILE
                        + " file with build paths definitions");
                marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
                marker.setAttribute(IMarker.LOCATION, "Project");
                marker.setAttribute("isRobotProblem", true);
            } catch (final CoreException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}
