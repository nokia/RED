package org.robotframework.ide.eclipse.main.plugin.project.build;

import org.eclipse.core.resources.IFile;

public class NullProblemsReporter implements IProblemsReporter {

    @Override
    public void handleProblem(final RobotProblem problem, final IFile file, final int line) {
        // nothing to do
    }
}
