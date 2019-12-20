/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.wizards;

import static com.google.common.collect.Lists.newArrayList;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.rf.ide.core.RedSystemHelper;
import org.rf.ide.core.RedTemporaryDirectory;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences;
import org.robotframework.ide.eclipse.main.plugin.launch.local.RobotLaunchConfiguration;
import org.robotframework.red.jface.dialogs.ProgressMonitorDialogWithConsole;
import org.robotframework.red.swt.SwtThread;
import org.robotframework.red.viewers.Selections;

import com.sun.xml.txw2.IllegalSignatureException;

public class NewRedPyDevConfigWizard extends Wizard implements INewWizard {

    private final NewRedPyDevConfigWizardData debuggingSessionSetup;

    private List<IResource> selectedResources;

    public NewRedPyDevConfigWizard() {
        setWindowTitle("RED with PyDev debugging session");
        setNeedsProgressMonitor(false);
        setDefaultPageImageDescriptor(DebugUITools.getImageDescriptor(IDebugUIConstants.IMG_WIZBAN_DEBUG));

        this.debuggingSessionSetup = new NewRedPyDevConfigWizardData();
    }

    @Override
    public void init(final IWorkbench workbench, final IStructuredSelection selection) {
        this.selectedResources = Selections.getAdaptableElements(selection, IResource.class);
    }

    @Override
    public void addPages() {
        final IProject project = selectedResources.isEmpty() ? null : selectedResources.get(0).getProject();

        final List<WizardPage> pages = new ArrayList<>();
        pages.add(new NewRedPyDevConfigInterpreterPage(project, debuggingSessionSetup));
        pages.add(new NewRedPyDevConfigRunnerScriptPage(debuggingSessionSetup));
        pages.add(new NewRedPyDevConfigDebuggerScriptPage(debuggingSessionSetup));
        pages.forEach(page -> {
            page.setWizard(this);
            addPage(page);
        });
    }

    @Override
    public boolean performFinish() {
        if (debuggingSessionSetup.requiresRedpydevdInstallation()) {
            final boolean installed = installRedpydevdModule();
            if (!installed) {
                return false;
            }

        } else if (debuggingSessionSetup.requiresRedpydevdExport()) {
            final boolean exported = exportRedPydevdModule();
            if (!exported) {
                return false;
            }
        }
        final ILaunchConfiguration configuration = prepareConfiguration();
        if (configuration != null) {
            openConfiguration(configuration);
            return true;
        }
        return false;
    }

    private boolean installRedpydevdModule() {
        try {
            final ProgressMonitorDialogWithConsole dialog = new ProgressMonitorDialogWithConsole(getShell());
            dialog.run((monitor, output) -> {
                monitor.beginTask("Creating RED with PyDev debugger session", 4);

                monitor.subTask("Installing redpydevd module");

                try {
                    RedTemporaryDirectory.copyRedpydevdPackage();
                    final File setupScript = RedTemporaryDirectory.getRedpydevdSetup();
                    monitor.worked(1);

                    monitor.subTask("Installing redpydevd module: creating source distribution");
                    if (monitor.isCanceled()) {
                        return;
                    }

                    final int sdistExitCode = runProcess(setupScript.getParentFile(), output, setupScript.getName(),
                            "sdist", "--formats=gztar");
                    if (sdistExitCode != 0) {
                        throw new IllegalSignatureException("The sdist ended with exit code " + sdistExitCode);
                    }
                    monitor.worked(1);

                    final File[] archive = new File(setupScript.getParentFile(), "dist")
                            .listFiles(f -> f.getName().startsWith("red-pydevd") && f.getName().endsWith("tar.gz"));
                    if (archive.length == 0) {
                        throw new IllegalStateException("Cannot find red-pydevd source distrubution file");
                    }
                    monitor.worked(1);


                    monitor.subTask("Installing redpydevd module: calling pip");
                    if (monitor.isCanceled()) {
                        return;
                    }

                    final int pipExitCode = runProcess(setupScript.getParentFile(), output, "-m", "pip", "install",
                            "--upgrade", "--no-index", "--user",
                            setupScript.getParentFile().toPath().relativize(archive[0].toPath()).toString());
                    if (pipExitCode != 0) {
                        throw new IllegalSignatureException("The pip ended with exit code " + pipExitCode);
                    }
                    monitor.worked(1);

                } catch (final Exception e) {
                    dialog.markDoNotClose();
                    SwtThread.syncExec(() -> ErrorDialog.openError(getShell(), "Installing problem",
                            "Error occurred when installing redpydevd module",
                            new Status(IStatus.ERROR, RedPlugin.PLUGIN_ID, e.getMessage(), e)));
                    throw new InvocationTargetException(e);

                } finally {
                    monitor.done();
                }
            });
            return true;
        } catch (InvocationTargetException | InterruptedException e) {
            // handle inside
            return false;
        }
    }

    private int runProcess(final File workingDirectory, final Consumer<String> linesConsumer,
            final String... arguments) throws IOException, InterruptedException {

        final String executable = debuggingSessionSetup.createPythonExecutablePath();
        final List<String> command = newArrayList(executable);
        command.addAll(Arrays.asList(arguments));

        linesConsumer.accept("> " + String.join(" ", command));

        return RedSystemHelper.runExternalProcess(workingDirectory, command, line -> linesConsumer.accept("  " + line));
    }

    private boolean exportRedPydevdModule() {
        try {
            final ProgressMonitorDialog dialog = new ProgressMonitorDialog(getShell());
            dialog.run(true, true, monitor -> {

                monitor.beginTask("Creating RED with PyDev debugger session", 1);
                monitor.subTask("Exporting redpydevd module");

                final File exportLocation = debuggingSessionSetup.getRedpydevdLocation();
                try (InputStream source = RedTemporaryDirectory.getRedpydevdFileAsStream()) {
                    if (monitor.isCanceled()) {
                        return;
                    }
                    Files.copy(source, exportLocation.toPath(), StandardCopyOption.REPLACE_EXISTING);

                } catch (final Exception e) {
                    SwtThread.syncExec(() -> ErrorDialog.openError(getShell(), "Exporting problem",
                            "Error occurred when exporting redpydevd module",
                            new Status(IStatus.ERROR, RedPlugin.PLUGIN_ID, e.getMessage(), e)));
                    throw new InvocationTargetException(e);

                } finally {
                    monitor.worked(1);
                    monitor.done();
                }
            });
            return true;

        } catch (InvocationTargetException | InterruptedException e) {
            // handled inside
            return false;
        }
    }

    private ILaunchConfiguration prepareConfiguration() {
        try {
            final String exec = debuggingSessionSetup.createPythonExecutablePath();
            final List<String> arguments = debuggingSessionSetup.createArguments();

            final ILaunchConfigurationWorkingCopy preparedConfig = RobotLaunchConfiguration
                    .prepareDefault(selectedResources);
            final RobotLaunchConfiguration robotConfig = new RobotLaunchConfiguration(preparedConfig);
            robotConfig.setExecutableFilePath(exec);
            robotConfig.setExecutableFileArguments(String.join(" ", arguments));
            if (debuggingSessionSetup.hasGeventSupport()) {
                robotConfig.addEnvironmentVariable("GEVENT_SUPPORT", "True");
            }
            return preparedConfig.doSave();

        } catch (final CoreException e) {
            return null;
        }
    }

    private void openConfiguration(final ILaunchConfiguration configuration) {
        final Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();

        askForPreferenceChange(shell);

        SwtThread.asyncExec(() -> {
            // make it async in order to be able to finish wizard
            final StructuredSelection toSelect = new StructuredSelection(new Object[] { configuration });
            final String groupId = DebugUITools.getLaunchGroup(configuration, "debug").getIdentifier();
            DebugUITools.openLaunchConfigurationDialogOnGroup(shell, toSelect, groupId);
        });
    }

    private void askForPreferenceChange(final Shell shell) {
        final RedPreferences preferences = RedPlugin.getDefault().getPreferences();
        if (preferences.shouldUseSingleCommandLineArgument()) {
            final MessageDialog dialog = new MessageDialog(shell, "Warning", null, "RED is configured to pass all the "
                    + "command line arguments to custom executor as a single argument. The redpydevd runner module "
                    + "requires this preference to be disabled. Do you want to disable it?", MessageDialog.QUESTION, 0,
                    "Yes", "No");

            if (dialog.open() == Window.OK) {
                preferences.setShouldUseSingleCommandLineArgument(false);
            }
        }
    }
}
