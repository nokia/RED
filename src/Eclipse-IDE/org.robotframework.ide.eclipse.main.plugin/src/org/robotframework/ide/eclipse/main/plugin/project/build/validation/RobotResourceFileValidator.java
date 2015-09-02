package org.robotframework.ide.eclipse.main.plugin.project.build.validation;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotArtifactsValidator.RobotFileValidator;

public class RobotResourceFileValidator implements RobotFileValidator {

    private final IFile file;

    public RobotResourceFileValidator(final IFile file) {
        this.file = file;
    }

    @Override
    public void validate(final IProgressMonitor monitor) throws CoreException {
        // nothing to do yet
    }
}