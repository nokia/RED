/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.preferences;

import static org.assertj.core.api.Assertions.assertThat;
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
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.rf.ide.core.executor.RobotRuntimeEnvironment;
import org.rf.ide.core.executor.SuiteExecutor;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.preferences.InstalledRobotsEnvironmentsLabelProvider.InstalledRobotsNamesLabelProvider;
import org.robotframework.ide.eclipse.main.plugin.preferences.InstalledRobotsEnvironmentsLabelProvider.InstalledRobotsPathsLabelProvider;
import org.robotframework.red.graphics.FontsManager;
import org.robotframework.red.graphics.ImagesManager;
import org.robotframework.red.junit.ProjectProvider;
import org.robotframework.red.junit.ShellProvider;
import org.robotframework.red.viewers.ListInputStructuredContentProvider;

@RunWith(MockitoJUnitRunner.class)
public class InstalledRobotsEnvironmentsLabelProviderTest {

    public static ProjectProvider projectProvider = new ProjectProvider(
            InstalledRobotsEnvironmentsLabelProviderTest.class);

    public static TemporaryFolder tempFolder = new TemporaryFolder();

    public static ShellProvider shellProvider = new ShellProvider();

    @ClassRule
    public static TestRule rulesChain = RuleChain.outerRule(projectProvider).around(tempFolder).around(shellProvider);

    @Mock
    private RobotRuntimeEnvironment environment;

    @Test
    public void testGettingForeground_forInvalidPythonInstallation() throws Exception {
        when(environment.isValidPythonInstallation()).thenReturn(false);
        final InstalledRobotsEnvironmentsLabelProvider labelProvider = createLabelProvider(createViewer());
        assertThat(labelProvider.getForeground(environment))
                .isEqualTo(Display.getCurrent().getSystemColor(SWT.COLOR_RED));
    }

    @Test
    public void testGettingForeground_forValidPythonInstallationWithoutRobotInstalled() throws Exception {
        when(environment.isValidPythonInstallation()).thenReturn(true);
        when(environment.hasRobotInstalled()).thenReturn(false);
        final InstalledRobotsEnvironmentsLabelProvider labelProvider = createLabelProvider(createViewer());
        assertThat(labelProvider.getForeground(environment))
                .isEqualTo(Display.getCurrent().getSystemColor(SWT.COLOR_DARK_YELLOW));
    }

    @Test
    public void testGettingForeground_forValidPythonInstallationWithRobotInstalled() throws Exception {
        when(environment.isValidPythonInstallation()).thenReturn(true);
        when(environment.hasRobotInstalled()).thenReturn(true);
        final InstalledRobotsEnvironmentsLabelProvider labelProvider = createLabelProvider(createViewer());
        assertThat(labelProvider.getForeground(environment)).isNull();
    }

    @Test
    public void testGettingFont_forNotSelectedPythonInstallation() throws Exception {
        final CheckboxTableViewer viewer = createViewer();
        viewer.setInput(Arrays.asList(environment));
        final InstalledRobotsEnvironmentsLabelProvider labelProvider = createLabelProvider(viewer);
        assertThat(labelProvider.getFont(environment)).isNull();
    }

    @Test
    public void testGettingFont_forSelectedPythonInstallation() throws Exception {
        final CheckboxTableViewer viewer = createViewer();
        viewer.setInput(Arrays.asList(environment));
        viewer.setCheckedElements(new Object[] { environment });
        final InstalledRobotsEnvironmentsLabelProvider labelProvider = createLabelProvider(viewer);
        assertThat(labelProvider.getFont(environment)).isEqualTo(FontsManager.transformFontWithStyle(SWT.BOLD));
    }

    @Test
    public void testGettingToolTipText_forInvalidPythonInstallation() throws Exception {
        final File pythonInstallation = new File("py_installation");
        when(environment.getFile()).thenReturn(pythonInstallation);
        when(environment.isValidPythonInstallation()).thenReturn(false);
        final InstalledRobotsEnvironmentsLabelProvider labelProvider = createLabelProvider(null);
        assertThat(labelProvider.getToolTipText(environment)).isEqualTo("The location '"
                + pythonInstallation.getAbsolutePath() + "' does not seem to be a valid python directory.");
    }

    @Test
    public void testGettingToolTipText_forValidPythonInstallationWithoutRobotInstalled() throws Exception {
        final File pythonInstallation = new File("py_installation");
        when(environment.getFile()).thenReturn(pythonInstallation);
        when(environment.isValidPythonInstallation()).thenReturn(true);
        when(environment.hasRobotInstalled()).thenReturn(false);
        when(environment.getInterpreter()).thenReturn(SuiteExecutor.Python);
        final InstalledRobotsEnvironmentsLabelProvider labelProvider = createLabelProvider(null);
        assertThat(labelProvider.getToolTipText(environment))
                .isEqualTo("Python installation '" + pythonInstallation.getAbsolutePath() + File.separator
                        + SuiteExecutor.Python.executableName() + "' does not seem to have robot framework installed.");
    }

    @Test
    public void testGettingToolTipText_forValidPythonInstallationWithRobotInstalled() throws Exception {
        final File pythonInstallation = new File("py_installation");
        when(environment.getFile()).thenReturn(pythonInstallation);
        when(environment.isValidPythonInstallation()).thenReturn(true);
        when(environment.hasRobotInstalled()).thenReturn(true);
        when(environment.getInterpreter()).thenReturn(SuiteExecutor.Python);
        when(environment.getVersion()).thenReturn("Robot.Version.123");
        final InstalledRobotsEnvironmentsLabelProvider labelProvider = createLabelProvider(null);
        assertThat(labelProvider.getToolTipText(environment))
                .isEqualTo("Python installation '" + pythonInstallation.getAbsolutePath() + File.separator
                        + SuiteExecutor.Python.executableName() + "' has Robot.Version.123.");
    }

    @Test
    public void testGettingToolTipImage_forInvalidPythonInstallation() throws Exception {
        when(environment.isValidPythonInstallation()).thenReturn(false);
        final InstalledRobotsEnvironmentsLabelProvider labelProvider = createLabelProvider(null);
        assertThat(labelProvider.getToolTipImage(environment))
                .isSameAs(ImagesManager.getImage(RedImages.getTooltipProhibitedImage()));
    }

    @Test
    public void testGettingToolTipImage_forValidPythonInstallationWithoutRobotInstalled() throws Exception {
        when(environment.isValidPythonInstallation()).thenReturn(true);
        when(environment.hasRobotInstalled()).thenReturn(false);
        final InstalledRobotsEnvironmentsLabelProvider labelProvider = createLabelProvider(null);
        assertThat(labelProvider.getToolTipImage(environment))
                .isSameAs(ImagesManager.getImage(RedImages.getTooltipWarnImage()));
    }

    @Test
    public void testGettingToolTipImage_forValidPythonInstallationWithRobotInstalled() throws Exception {
        when(environment.isValidPythonInstallation()).thenReturn(true);
        when(environment.hasRobotInstalled()).thenReturn(true);
        final InstalledRobotsEnvironmentsLabelProvider labelProvider = createLabelProvider(null);
        assertThat(labelProvider.getToolTipImage(environment))
                .isSameAs(ImagesManager.getImage(RedImages.getTooltipImage()));
    }

    @Test
    public void testGettingName_forPythonInstallationWithUnknownVersion() throws Exception {
        when(environment.getVersion()).thenReturn(null);
        final InstalledRobotsNamesLabelProvider labelProvider = new InstalledRobotsNamesLabelProvider(null);
        assertThat(labelProvider.getText(environment)).isEqualTo("<unknown>");
    }

    @Test
    public void testGettingName_forPythonInstallationWithKnownVersion() throws Exception {
        when(environment.getVersion()).thenReturn("Robot.Version.123");
        final InstalledRobotsNamesLabelProvider labelProvider = new InstalledRobotsNamesLabelProvider(null);
        assertThat(labelProvider.getText(environment)).isEqualTo("Robot.Version.123");
    }

    @Test
    public void testGettingPath_forWorkspacePythonInstallation() throws Exception {
        projectProvider.createDir("workspace_py_installation");
        final File file = projectProvider.createFile("workspace_py_installation/python").getLocation().toFile();
        when(environment.getFile()).thenReturn(file);
        final InstalledRobotsPathsLabelProvider labelProvider = new InstalledRobotsPathsLabelProvider(null);
        assertThat(labelProvider.getText(environment)).isEqualTo(file.getAbsolutePath());
    }

    @Test
    public void testGettingPath_forNonWorkspacePythonInstallation() throws Exception {
        tempFolder.newFolder("non_workspace_py_installation");
        final File file = tempFolder.newFile("python");
        when(environment.getFile()).thenReturn(file);
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
