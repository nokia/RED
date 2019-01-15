/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.preferences;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.Arrays;

import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.TestRule;
import org.rf.ide.core.environment.InvalidPythonRuntimeEnvironment;
import org.rf.ide.core.environment.MissingRobotRuntimeEnvironment;
import org.rf.ide.core.environment.PythonInstallationDirectoryFinder.PythonInstallationDirectory;
import org.rf.ide.core.environment.RobotRuntimeEnvironment;
import org.rf.ide.core.environment.SuiteExecutor;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.preferences.InstalledRobotsEnvironmentsLabelProvider.InstalledRobotsNamesLabelProvider;
import org.robotframework.ide.eclipse.main.plugin.preferences.InstalledRobotsEnvironmentsLabelProvider.InstalledRobotsPathsLabelProvider;
import org.robotframework.red.graphics.FontsManager;
import org.robotframework.red.graphics.ImagesManager;
import org.robotframework.red.junit.ProjectProvider;
import org.robotframework.red.junit.ShellProvider;
import org.robotframework.red.viewers.ListInputStructuredContentProvider;

public class InstalledRobotsEnvironmentsLabelProviderTest {

    public static ProjectProvider projectProvider = new ProjectProvider(
            InstalledRobotsEnvironmentsLabelProviderTest.class);

    public static TemporaryFolder tempFolder = new TemporaryFolder();

    public static ShellProvider shellProvider = new ShellProvider();

    @ClassRule
    public static TestRule rulesChain = RuleChain.outerRule(projectProvider).around(tempFolder).around(shellProvider);

    @Test
    public void testGettingForeground_forInvalidPythonInstallation() throws Exception {
        final RobotRuntimeEnvironment environment = new InvalidPythonRuntimeEnvironment(new File("path"));
        final InstalledRobotsEnvironmentsLabelProvider labelProvider = createLabelProvider(createViewer());
        assertThat(labelProvider.getForeground(environment))
                .isEqualTo(Display.getCurrent().getSystemColor(SWT.COLOR_RED));
    }

    @Test
    public void testGettingForeground_forValidPythonInstallationWithoutRobotInstalled() throws Exception {
        final RobotRuntimeEnvironment environment = new MissingRobotRuntimeEnvironment(new File("path"));
        final InstalledRobotsEnvironmentsLabelProvider labelProvider = createLabelProvider(createViewer());
        assertThat(labelProvider.getForeground(environment))
                .isEqualTo(Display.getCurrent().getSystemColor(SWT.COLOR_DARK_YELLOW));
    }

    @Test
    public void testGettingForeground_forDeprecatedPythonInstallationWithRobotInstalled() throws Exception {
        final RobotRuntimeEnvironment environment = new RobotRuntimeEnvironment(new File("path"),
                "Robot Framework 3.0.2 (Python 2.6.1 on win32)");
        final InstalledRobotsEnvironmentsLabelProvider labelProvider = createLabelProvider(createViewer());
        assertThat(labelProvider.getForeground(environment))
                .isEqualTo(Display.getCurrent().getSystemColor(SWT.COLOR_DARK_YELLOW));
    }

    @Test
    public void testGettingForeground_forValidPythonInstallationWithRobotInstalled() throws Exception {
        final RobotRuntimeEnvironment environment = new RobotRuntimeEnvironment(new File("path"),
                "Robot Framework 3.0.2 (Python 3.6.5 on win32)");
        final InstalledRobotsEnvironmentsLabelProvider labelProvider = createLabelProvider(createViewer());
        assertThat(labelProvider.getForeground(environment)).isNull();
    }

    @Test
    public void testGettingFont_forNotSelectedPythonInstallation() throws Exception {
        final RobotRuntimeEnvironment environment = new InvalidPythonRuntimeEnvironment(new File("path"));
        final CheckboxTableViewer viewer = createViewer();
        viewer.setInput(Arrays.asList(environment));
        final InstalledRobotsEnvironmentsLabelProvider labelProvider = createLabelProvider(viewer);
        assertThat(labelProvider.getFont(environment)).isNull();
    }

    @Test
    public void testGettingFont_forSelectedPythonInstallation() throws Exception {
        final RobotRuntimeEnvironment environment = new InvalidPythonRuntimeEnvironment(new File("path"));
        final CheckboxTableViewer viewer = createViewer();
        viewer.setInput(Arrays.asList(environment));
        viewer.setCheckedElements(new Object[] { environment });
        final InstalledRobotsEnvironmentsLabelProvider labelProvider = createLabelProvider(viewer);
        assertThat(labelProvider.getFont(environment)).isEqualTo(FontsManager.transformFontWithStyle(SWT.BOLD));
    }

    @Test
    public void testGettingToolTipText_forInvalidPythonInstallation() throws Exception {
        final File pythonInstallation = new File("py_installation");
        final RobotRuntimeEnvironment environment = new InvalidPythonRuntimeEnvironment(pythonInstallation);
        final InstalledRobotsEnvironmentsLabelProvider labelProvider = createLabelProvider(null);
        assertThat(labelProvider.getToolTipText(environment)).isEqualTo("The location '"
                + pythonInstallation.getAbsolutePath() + "' does not seem to be a valid Python directory");
    }

    @Test
    public void testGettingToolTipText_forValidPythonInstallationWithoutRobotInstalled() throws Exception {
        final PythonInstallationDirectory pythonInstallation = mock(PythonInstallationDirectory.class);
        when(pythonInstallation.getInterpreter()).thenReturn(SuiteExecutor.Python);
        when(pythonInstallation.getAbsolutePath()).thenReturn("path");
        final RobotRuntimeEnvironment environment = new MissingRobotRuntimeEnvironment(pythonInstallation);
        final InstalledRobotsEnvironmentsLabelProvider labelProvider = createLabelProvider(null);
        assertThat(labelProvider.getToolTipText(environment))
                .isEqualTo("Python installation '" + pythonInstallation.getAbsolutePath() + File.separator
                        + SuiteExecutor.Python.executableName() + "' does not seem to have Robot Framework installed");
    }

    @Test
    public void testGettingToolTipText_forDeprecatedPythonInstallationWithRobotInstalled() throws Exception {
        final PythonInstallationDirectory pythonInstallation = mock(PythonInstallationDirectory.class);
        when(pythonInstallation.getInterpreter()).thenReturn(SuiteExecutor.Python);
        when(pythonInstallation.getAbsolutePath()).thenReturn("path");
        final RobotRuntimeEnvironment environment = new RobotRuntimeEnvironment(pythonInstallation,
                "Robot Framework 3.0.2 (Python 2.6.1 on win32)");
        final InstalledRobotsEnvironmentsLabelProvider labelProvider = createLabelProvider(null);
        assertThat(labelProvider.getToolTipText(environment)).isEqualTo("Python installation '"
                + pythonInstallation.getAbsolutePath() + File.separator + SuiteExecutor.Python.executableName()
                + "' has deprecated version (2.6.1). RED or Robot Framework may be not compatible with it.");
    }

    @Test
    public void testGettingToolTipText_forValidPythonInstallationWithRobotInstalled() throws Exception {
        final PythonInstallationDirectory pythonInstallation = mock(PythonInstallationDirectory.class);
        when(pythonInstallation.getInterpreter()).thenReturn(SuiteExecutor.Python);
        when(pythonInstallation.getAbsolutePath()).thenReturn("path");
        final RobotRuntimeEnvironment environment = new RobotRuntimeEnvironment(pythonInstallation,
                "Robot Framework 3.0.2 (Python 3.6.5 on win32)");
        final InstalledRobotsEnvironmentsLabelProvider labelProvider = createLabelProvider(null);
        assertThat(labelProvider.getToolTipText(environment)).isEqualTo("Python installation '"
                + pythonInstallation.getAbsolutePath() + File.separator + SuiteExecutor.Python.executableName()
                + "' has Robot Framework 3.0.2 (Python 3.6.5 on win32)");
    }

    @Test
    public void testGettingToolTipImage_forInvalidPythonInstallation() throws Exception {
        final RobotRuntimeEnvironment environment = new InvalidPythonRuntimeEnvironment(new File("path"));
        final InstalledRobotsEnvironmentsLabelProvider labelProvider = createLabelProvider(null);
        assertThat(labelProvider.getToolTipImage(environment))
                .isSameAs(ImagesManager.getImage(RedImages.getTooltipProhibitedImage()));
    }

    @Test
    public void testGettingToolTipImage_forValidPythonInstallationWithoutRobotInstalled() throws Exception {
        final RobotRuntimeEnvironment environment = new MissingRobotRuntimeEnvironment(new File("path"));
        final InstalledRobotsEnvironmentsLabelProvider labelProvider = createLabelProvider(null);
        assertThat(labelProvider.getToolTipImage(environment))
                .isSameAs(ImagesManager.getImage(RedImages.getTooltipWarnImage()));
    }

    @Test
    public void testGettingToolTipImage_forDeprecatedPythonInstallationWithRobotInstalled() throws Exception {
        final RobotRuntimeEnvironment environment = new RobotRuntimeEnvironment(new File("path"),
                "Robot Framework 3.0.2 (Python 2.6.1 on win32)");
        final InstalledRobotsEnvironmentsLabelProvider labelProvider = createLabelProvider(null);
        assertThat(labelProvider.getToolTipImage(environment))
                .isSameAs(ImagesManager.getImage(RedImages.getTooltipWarnImage()));
    }

    @Test
    public void testGettingToolTipImage_forValidPythonInstallationWithRobotInstalled() throws Exception {
        final RobotRuntimeEnvironment environment = new RobotRuntimeEnvironment(new File("path"),
                "Robot Framework 3.0.2 (Python 3.6.5 on win32)");
        final InstalledRobotsEnvironmentsLabelProvider labelProvider = createLabelProvider(null);
        assertThat(labelProvider.getToolTipImage(environment))
                .isSameAs(ImagesManager.getImage(RedImages.getTooltipImage()));
    }

    @Test
    public void testGettingName_forPythonInstallationWithKnownVersion() throws Exception {
        final RobotRuntimeEnvironment environment = new RobotRuntimeEnvironment(new File("path"),
                "Robot Framework 3.0.2 (Python 3.6.5 on win32)");
        final InstalledRobotsNamesLabelProvider labelProvider = new InstalledRobotsNamesLabelProvider(null);
        assertThat(labelProvider.getText(environment)).isEqualTo("Robot Framework 3.0.2 (Python 3.6.5 on win32)");
    }

    @Test
    public void testGettingPath_forWorkspacePythonInstallation() throws Exception {
        projectProvider.createDir("workspace_py_installation");
        final File file = projectProvider.createFile("workspace_py_installation/python").getLocation().toFile();
        final RobotRuntimeEnvironment environment = new RobotRuntimeEnvironment(file, "RF 3.0");
        final InstalledRobotsPathsLabelProvider labelProvider = new InstalledRobotsPathsLabelProvider(null);
        assertThat(labelProvider.getText(environment)).isEqualTo(file.getAbsolutePath());
    }

    @Test
    public void testGettingPath_forNonWorkspacePythonInstallation() throws Exception {
        tempFolder.newFolder("non_workspace_py_installation");
        final File file = tempFolder.newFile("python");
        final RobotRuntimeEnvironment environment = new RobotRuntimeEnvironment(file, "RF 3.0");
        final InstalledRobotsPathsLabelProvider labelProvider = new InstalledRobotsPathsLabelProvider(null);
        assertThat(labelProvider.getText(environment)).isEqualTo(file.getAbsolutePath());
    }

    private CheckboxTableViewer createViewer() {
        final CheckboxTableViewer viewer = CheckboxTableViewer
                .newCheckList(new Composite(shellProvider.getShell(), SWT.NONE), SWT.NONE);
        viewer.setContentProvider(new ListInputStructuredContentProvider());
        return viewer;
    }

    private InstalledRobotsEnvironmentsLabelProvider createLabelProvider(final CheckboxTableViewer viewer) {
        return new InstalledRobotsEnvironmentsLabelProvider(viewer) {
            // nothing to implement
        };
    }

}
