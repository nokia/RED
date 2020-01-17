/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.wizards;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.io.File;

import org.junit.jupiter.api.Test;
import org.rf.ide.core.environment.IRuntimeEnvironment;

public class NewRedPyDevConfigWizardDataTest {

    @Test
    public void settingRuntimeEnvironmentMakesItChosen() {
        final NewRedPyDevConfigWizardData data = new NewRedPyDevConfigWizardData();

        assertThat(data.isInterpreterChosen()).isFalse();
        assertThat(data.getRedEnvironment()).isNull();

        final IRuntimeEnvironment env = mock(IRuntimeEnvironment.class);
        data.setRedEnvironment(env);

        assertThat(data.isInterpreterChosen()).isTrue();
        assertThat(data.getRedEnvironment()).isSameAs(env);
    }

    @Test
    public void changingRuntimeEnvironmentResetsOtherSettings_1() {
        final NewRedPyDevConfigWizardData data = new NewRedPyDevConfigWizardData();

        final IRuntimeEnvironment env1 = mock(IRuntimeEnvironment.class);
        final IRuntimeEnvironment env2 = mock(IRuntimeEnvironment.class);

        data.setRedEnvironment(env1);
        data.setPydevdLocation(new File("location"));

        assertThat(data.isUsingPydevdFromInterpreter()).isFalse();
        assertThat(data.getPydevdLocation()).isEqualTo(new File("location"));

        data.setRedEnvironment(env1);

        assertThat(data.isUsingPydevdFromInterpreter()).isFalse();
        assertThat(data.getPydevdLocation()).isEqualTo(new File("location"));

        data.setRedEnvironment(env2);

        assertThat(data.isUsingPydevdFromInterpreter()).isFalse();
        assertThat(data.getPydevdLocation()).isNull();
    }

    @Test
    public void changingRuntimeEnvironmentResetsOtherSettings_2() {
        final NewRedPyDevConfigWizardData data = new NewRedPyDevConfigWizardData();

        final IRuntimeEnvironment env1 = mock(IRuntimeEnvironment.class);
        final IRuntimeEnvironment env2 = mock(IRuntimeEnvironment.class);

        data.setRedEnvironment(env1);
        data.setUsePydevdFromInterpreter();

        assertThat(data.isUsingPydevdFromInterpreter()).isTrue();
        assertThat(data.getPydevdLocation()).isNull();

        data.setRedEnvironment(env1);

        assertThat(data.isUsingPydevdFromInterpreter()).isTrue();
        assertThat(data.getPydevdLocation()).isNull();

        data.setRedEnvironment(env2);

        assertThat(data.isUsingPydevdFromInterpreter()).isFalse();
        assertThat(data.getPydevdLocation()).isNull();
    }

    @Test
    public void changingRuntimeEnvironmentResetsOtherSettings_3() {
        final NewRedPyDevConfigWizardData data = new NewRedPyDevConfigWizardData();

        final IRuntimeEnvironment env1 = mock(IRuntimeEnvironment.class);
        final IRuntimeEnvironment env2 = mock(IRuntimeEnvironment.class);

        data.setRedEnvironment(env1);
        data.setRedpydevdLocation(new File("location"));

        assertThat(data.isUsingRedpydevdFromInterpreter()).isFalse();
        assertThat(data.getRedpydevdLocation()).isEqualTo(new File("location"));

        data.setRedEnvironment(env1);

        assertThat(data.isUsingRedpydevdFromInterpreter()).isFalse();
        assertThat(data.getRedpydevdLocation()).isEqualTo(new File("location"));

        data.setRedEnvironment(env2);

        assertThat(data.isUsingRedpydevdFromInterpreter()).isFalse();
        assertThat(data.getRedpydevdLocation()).isNull();
    }

    @Test
    public void changingRuntimeEnvironmentResetsOtherSettings_4() {
        final NewRedPyDevConfigWizardData data = new NewRedPyDevConfigWizardData();

        final IRuntimeEnvironment env1 = mock(IRuntimeEnvironment.class);
        final IRuntimeEnvironment env2 = mock(IRuntimeEnvironment.class);

        data.setRedEnvironment(env1);
        data.setUseRedpydevdFromInterpreter();

        assertThat(data.isUsingRedpydevdFromInterpreter()).isTrue();
        assertThat(data.getRedpydevdLocation()).isNull();

        data.setRedEnvironment(env1);

        assertThat(data.isUsingRedpydevdFromInterpreter()).isTrue();
        assertThat(data.getRedpydevdLocation()).isNull();

        data.setRedEnvironment(env2);

        assertThat(data.isUsingRedpydevdFromInterpreter()).isFalse();
        assertThat(data.getRedpydevdLocation()).isNull();
    }

    @Test
    public void changingRuntimeEnvironmentResetsOtherSettings_5() {
        final NewRedPyDevConfigWizardData data = new NewRedPyDevConfigWizardData();

        final IRuntimeEnvironment env1 = mock(IRuntimeEnvironment.class);
        final IRuntimeEnvironment env2 = mock(IRuntimeEnvironment.class);

        data.setRedEnvironment(env1);
        data.setRedpydevdRequiresInstallation(true);

        assertThat(data.requiresRedpydevdExport()).isFalse();
        assertThat(data.requiresRedpydevdInstallation()).isTrue();

        data.setRedEnvironment(env1);

        assertThat(data.requiresRedpydevdExport()).isFalse();
        assertThat(data.requiresRedpydevdInstallation()).isTrue();

        data.setRedEnvironment(env2);

        assertThat(data.requiresRedpydevdExport()).isFalse();
        assertThat(data.requiresRedpydevdInstallation()).isFalse();
    }

    @Test
    public void changingRuntimeEnvironmentResetsOtherSettings_6() {
        final NewRedPyDevConfigWizardData data = new NewRedPyDevConfigWizardData();

        final IRuntimeEnvironment env1 = mock(IRuntimeEnvironment.class);
        final IRuntimeEnvironment env2 = mock(IRuntimeEnvironment.class);

        data.setRedEnvironment(env1);
        data.setRedpydevdRequiresExport(true);

        assertThat(data.requiresRedpydevdExport()).isTrue();
        assertThat(data.requiresRedpydevdInstallation()).isFalse();

        data.setRedEnvironment(env1);

        assertThat(data.requiresRedpydevdExport()).isTrue();
        assertThat(data.requiresRedpydevdInstallation()).isFalse();

        data.setRedEnvironment(env2);

        assertThat(data.requiresRedpydevdExport()).isFalse();
        assertThat(data.requiresRedpydevdInstallation()).isFalse();
    }

    @Test
    public void testArgumentsCreation_1() {
        final NewRedPyDevConfigWizardData data1 = new NewRedPyDevConfigWizardData();
        data1.setUseRedpydevdFromInterpreter();
        data1.setUsePydevdFromInterpreter();
        assertThat(data1.createArguments()).containsExactly("-m", "redpydevd");

        final NewRedPyDevConfigWizardData data2 = new NewRedPyDevConfigWizardData();
        data2.setUseRedpydevdFromInterpreter();
        data2.setUsePydevdFromInterpreter();
        data2.setAddress("127.0.0.42");
        assertThat(data2.createArguments()).containsExactly("-m", "redpydevd", "--client", "127.0.0.42");

        final NewRedPyDevConfigWizardData data3 = new NewRedPyDevConfigWizardData();
        data3.setUseRedpydevdFromInterpreter();
        data3.setUsePydevdFromInterpreter();
        data3.setPort(1729);
        assertThat(data3.createArguments()).containsExactly("-m", "redpydevd", "--port", "1729");

        final NewRedPyDevConfigWizardData data4 = new NewRedPyDevConfigWizardData();
        data4.setUseRedpydevdFromInterpreter();
        data4.setUsePydevdFromInterpreter();
        data4.setAddress("127.0.0.42");
        data4.setPort(1729);
        assertThat(data4.createArguments()).containsExactly("-m", "redpydevd", "--client", "127.0.0.42", "--port",
                "1729");

        final NewRedPyDevConfigWizardData data5 = new NewRedPyDevConfigWizardData();
        data5.setUseRedpydevdFromInterpreter();
        data5.setPydevdLocation(new File("pydevd.py"));
        assertThat(data5.createArguments()).containsExactly("-m", "redpydevd", "--pydevd",
                new File("pydevd.py").getAbsolutePath());

        final NewRedPyDevConfigWizardData data6 = new NewRedPyDevConfigWizardData();
        data6.setUseRedpydevdFromInterpreter();
        data6.setPydevdLocation(new File("pydevd.py"));
        data6.setAddress("127.0.0.42");
        data6.setPort(1729);
        assertThat(data6.createArguments()).containsExactly("-m", "redpydevd", "--pydevd",
                new File("pydevd.py").getAbsolutePath(), "--client", "127.0.0.42", "--port", "1729");
    }

    @Test
    public void testArgumentsCreation_2() {
        final NewRedPyDevConfigWizardData data1 = new NewRedPyDevConfigWizardData();
        data1.setRedpydevdLocation(new File("redpydevd.py"));
        data1.setUsePydevdFromInterpreter();
        assertThat(data1.createArguments()).containsExactly(new File("redpydevd.py").getAbsolutePath());

        final NewRedPyDevConfigWizardData data2 = new NewRedPyDevConfigWizardData();
        data2.setRedpydevdLocation(new File("redpydevd.py"));
        data2.setUsePydevdFromInterpreter();
        data2.setAddress("127.0.0.42");
        assertThat(data2.createArguments()).containsExactly(new File("redpydevd.py").getAbsolutePath(), "--client",
                "127.0.0.42");

        final NewRedPyDevConfigWizardData data3 = new NewRedPyDevConfigWizardData();
        data3.setRedpydevdLocation(new File("redpydevd.py"));
        data3.setUsePydevdFromInterpreter();
        data3.setPort(1729);
        assertThat(data3.createArguments()).containsExactly(new File("redpydevd.py").getAbsolutePath(), "--port",
                "1729");

        final NewRedPyDevConfigWizardData data4 = new NewRedPyDevConfigWizardData();
        data4.setRedpydevdLocation(new File("redpydevd.py"));
        data4.setUsePydevdFromInterpreter();
        data4.setAddress("127.0.0.42");
        data4.setPort(1729);
        assertThat(data4.createArguments()).containsExactly(new File("redpydevd.py").getAbsolutePath(), "--client",
                "127.0.0.42", "--port", "1729");

        final NewRedPyDevConfigWizardData data5 = new NewRedPyDevConfigWizardData();
        data5.setRedpydevdLocation(new File("redpydevd.py"));
        data5.setPydevdLocation(new File("pydevd.py"));
        assertThat(data5.createArguments()).containsExactly(new File("redpydevd.py").getAbsolutePath(), "--pydevd",
                new File("pydevd.py").getAbsolutePath());

        final NewRedPyDevConfigWizardData data6 = new NewRedPyDevConfigWizardData();
        data6.setRedpydevdLocation(new File("redpydevd.py"));
        data6.setPydevdLocation(new File("pydevd.py"));
        data6.setAddress("127.0.0.42");
        data6.setPort(1729);
        assertThat(data6.createArguments()).containsExactly(new File("redpydevd.py").getAbsolutePath(), "--pydevd",
                new File("pydevd.py").getAbsolutePath(), "--client", "127.0.0.42", "--port", "1729");
    }

}
