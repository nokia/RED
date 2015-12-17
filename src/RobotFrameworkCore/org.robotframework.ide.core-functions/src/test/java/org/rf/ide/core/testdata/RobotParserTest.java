package org.rf.ide.core.testdata;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.rf.ide.core.executor.RobotRuntimeEnvironment;
import org.rf.ide.core.testdata.model.RobotProjectHolder;
import org.rf.ide.core.testdata.model.RobotVersion;

@SuppressWarnings("PMD.MethodNamingConventions")
public class RobotParserTest {

    @Test
    public void test_create_when_robotFramework_correct29() {
        // prepare
        RobotRuntimeEnvironment runtime = mock(RobotRuntimeEnvironment.class);
        when(runtime.getVersion()).thenReturn("2.9");
        RobotProjectHolder projectHolder = mock(RobotProjectHolder.class);
        when(projectHolder.getRobotRuntime()).thenReturn(runtime);

        // execute
        RobotParser parser = RobotParser.create(projectHolder);

        // verify
        RobotVersion robotVersion = parser.getRobotVersion();
        assertThat(robotVersion).isNotNull();
        assertThat(robotVersion.isEqualTo(new RobotVersion(2, 9))).isTrue();
    }

    @Test
    public void test_create_when_robotFramework_isNotPresent() {
        // prepare
        RobotRuntimeEnvironment runtime = mock(RobotRuntimeEnvironment.class);
        when(runtime.getVersion()).thenReturn(null);
        RobotProjectHolder projectHolder = mock(RobotProjectHolder.class);
        when(projectHolder.getRobotRuntime()).thenReturn(runtime);

        // execute
        RobotParser parser = RobotParser.create(projectHolder);

        // verify
        assertThat(parser.getRobotVersion()).isNull();
    }
}
