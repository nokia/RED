package org.robotframework.ide.eclipse.main.plugin.project.build;

import org.eclipse.core.resources.IFile;

public interface IProblemsReporter {

    void handleProblem(RobotProblem problem, IFile file, int line);

}