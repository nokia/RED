package org.robotframework.ide.eclipse.main.plugin.project.build;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

class RobotProjectValidator {

    public Job createValidationJob(final IProject project) {
        return new Job("Validating") {
            @Override
            protected IStatus run(final IProgressMonitor monitor) {
                monitor.done();
                return Status.OK_STATUS;
            }
        };
    }
}
