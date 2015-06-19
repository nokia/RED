package org.robotframework.ide.eclipse.main.plugin.project.build;

import org.robotframework.ide.core.executor.RobotRuntimeEnvironment;

interface ILibdocGenerator {

    void generateLibdoc(RobotRuntimeEnvironment runtimeEnvironment);

    String getMessage();

}
