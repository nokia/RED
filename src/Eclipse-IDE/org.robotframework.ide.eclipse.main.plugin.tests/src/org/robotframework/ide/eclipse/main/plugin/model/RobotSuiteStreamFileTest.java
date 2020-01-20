/**
 * Copyright 2019 Nokia Solutions and Networks*Licensed under the Apache License,Version 2.0,*see
 * license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.eclipse.core.runtime.Path;
import org.junit.jupiter.api.Test;
import org.rf.ide.core.environment.NullRuntimeEnvironment;
import org.rf.ide.core.environment.RobotVersion;
public class RobotSuiteStreamFileTest {

    @Test
    public void nullEnvironmentIsReturned() throws Exception {
        final RobotSuiteStreamFile suiteFile = new RobotSuiteStreamFile("file.robot", new Path("dir/file.robot"), "abc",
                false, RobotVersion.UNKNOWN);
        assertThat(suiteFile.getRuntimeEnvironment()).isExactlyInstanceOf(NullRuntimeEnvironment.class);
    }

    @Test
    public void robotParserFileIsReturned() throws Exception {
        final RobotSuiteStreamFile suiteFile = new RobotSuiteStreamFile("file.robot", new Path("dir/file.robot"), "abc",
                false, RobotVersion.UNKNOWN);
        assertThat(suiteFile.getRobotParserFile()).hasName("file.robot");
    }
}
