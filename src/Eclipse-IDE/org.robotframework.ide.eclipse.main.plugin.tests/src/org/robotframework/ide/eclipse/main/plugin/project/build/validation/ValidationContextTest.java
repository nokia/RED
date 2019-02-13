/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.core.resources.IProject;
import org.junit.Test;
import org.rf.ide.core.environment.IRuntimeEnvironment;
import org.rf.ide.core.environment.RobotVersion;
import org.rf.ide.core.project.RobotProjectConfig;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.project.build.BuildLogger;

public class ValidationContextTest {

    @Test
    public void construct_ValidationContext_with_RobotFramework30_installed() {
        // prepare
        final RobotModel model = mock(RobotModel.class);
        final IProject project = mock(IProject.class);
        final RobotProject robotProject = mock(RobotProject.class);
        final RobotProjectConfig robotProjectConfig = mock(RobotProjectConfig.class);
        final IRuntimeEnvironment runtimeEnvironment = mock(IRuntimeEnvironment.class);

        when(model.createRobotProject(project)).thenReturn(robotProject);
        when(robotProject.getRobotProjectConfig()).thenReturn(robotProjectConfig);
        when(robotProject.getRuntimeEnvironment()).thenReturn(runtimeEnvironment);
        when(robotProject.getRobotParserComplianceVersion()).thenReturn(RobotVersion.from("3.0"));

        // execute
        final ValidationContext valCtx = new ValidationContext(robotProject, new BuildLogger());

        // verify
        assertThat(valCtx.getVersion()).isEqualTo(new RobotVersion(3, 0));
    }

    @Test
    public void construct_ValidationContext_withNo_RobotFramework_installed() {
        // prepare
        final RobotModel model = mock(RobotModel.class);
        final IProject project = mock(IProject.class);
        final RobotProject robotProject = mock(RobotProject.class);
        final RobotProjectConfig robotProjectConfig = mock(RobotProjectConfig.class);
        final IRuntimeEnvironment runtimeEnvironment = mock(IRuntimeEnvironment.class);

        when(model.createRobotProject(project)).thenReturn(robotProject);
        when(robotProject.getRobotProjectConfig()).thenReturn(robotProjectConfig);
        when(robotProject.getRuntimeEnvironment()).thenReturn(runtimeEnvironment);
        when(robotProject.getRobotParserComplianceVersion()).thenReturn(RobotVersion.from(null));

        // execute
        final ValidationContext valCtx = new ValidationContext(robotProject, new BuildLogger());

        // verify
        assertThat(valCtx.getVersion()).isEqualTo(RobotVersion.UNKNOWN);
    }
}
