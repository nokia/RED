/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.mockito.InOrder;
import org.rf.ide.core.executor.RobotRuntimeEnvironment;
import org.rf.ide.core.testdata.model.RobotProjectHolder;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;

public class RobotArtifactValidatorTest {

    @Test
    public void test_revalidate_noRobot_installed() {
        // prepare
        RobotSuiteFile suiteModel = mock(RobotSuiteFile.class);
        RobotProject project = mock(RobotProject.class);
        RobotProjectHolder projectHolder = mock(RobotProjectHolder.class);
        RobotRuntimeEnvironment runtime = mock(RobotRuntimeEnvironment.class);

        when(suiteModel.getProject()).thenReturn(project);
        when(project.getRobotProjectHolder()).thenReturn(projectHolder);
        when(projectHolder.getRobotRuntime()).thenReturn(runtime);
        when(runtime.getVersion()).thenReturn(null);

        // execute
        RobotArtifactsValidator.revalidate(suiteModel);

        // verify
        InOrder order = inOrder(suiteModel, project, projectHolder, runtime);
        order.verify(suiteModel, times(1)).getProject();
        order.verify(project, times(1)).getRobotProjectHolder();
        order.verify(projectHolder, times(1)).getRobotRuntime();
        order.verify(runtime, times(1)).getVersion();
        order.verifyNoMoreInteractions();
    }
}
