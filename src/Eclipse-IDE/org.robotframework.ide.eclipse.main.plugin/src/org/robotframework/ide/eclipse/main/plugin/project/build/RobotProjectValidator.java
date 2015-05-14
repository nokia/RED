package org.robotframework.ide.eclipse.main.plugin.project.build;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

class RobotProjectValidator {

    public void validate(final IProject project, final IProgressMonitor monitor) {
        // if (monitor.isCanceled()) {
        // return;
        // }
        //
        // if (!RobotProjectNature.hasRobotNature(project)) {
        // monitor.worked(1);
        // return;
        // }
        // monitor.subTask("Validating Robot project: " + project.getName());
        //
        // checkBuildpathFile(project);
        //
        // monitor.worked(1);
    }

    public Job createValidationJob(final IProject project) {
        return new Job("Validating") {
            @Override
            protected IStatus run(final IProgressMonitor monitor) {
                return Status.OK_STATUS;
            }
        };
    }

    // private void checkBuildpathFile(final IProject project) {
    // final IFile buildpathFile =
    // project.getFile(RobotProjectNature.BUILDPATH_FILE);
    // if (buildpathFile == null || !buildpathFile.exists()) {
    // final RobotProblem problem = new RobotProblem(Severity.ERROR, "Missing "
    // + RobotProjectNature.BUILDPATH_FILE +
    // " file with build paths definitions", "Project");
    // problem.createMarker(project, RobotProblem.MISSING_BUILDPATHS_FILE);
    // }
    // }
}
