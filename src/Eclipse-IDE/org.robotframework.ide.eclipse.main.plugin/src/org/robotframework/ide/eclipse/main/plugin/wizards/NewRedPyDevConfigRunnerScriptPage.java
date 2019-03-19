/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.wizards;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.rf.ide.core.RedTemporaryDirectory;
import org.rf.ide.core.environment.EnvironmentSearchPaths;
import org.rf.ide.core.environment.IRuntimeEnvironment;
import org.rf.ide.core.environment.IRuntimeEnvironment.RuntimeEnvironmentException;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.red.graphics.ImagesManager;
import org.robotframework.red.jface.wizards.JobWizardPage;
import org.robotframework.red.swt.Listeners;


public class NewRedPyDevConfigRunnerScriptPage extends JobWizardPage {

    private IRuntimeEnvironment chosenEnv;

    private final NewRedPyDevConfigWizardData debuggingSessionSetup;

    private RedpydevdSearchingResult modulesInfo;

    private Label currentEnvLabel;
    private CLabel versionDescriptionLabel;
    
    private Button pythonModuleButton;
    private Button exportModuleButton;
    private Button userModelButton;

    private Button moduleLocationButton;
    private Text moduleLocationText;


    protected NewRedPyDevConfigRunnerScriptPage(final NewRedPyDevConfigWizardData debuggingSessionSetup) {
        super("Choose redpydevd runner module to be used",
                "Create launch configuration for RED and PyDev debugging session", null);
        setDescription("Choose redpydevd runner module to be used");

        this.debuggingSessionSetup = debuggingSessionSetup;
    }

    @Override
    protected void create(final Composite parent) {
        setPageComplete(false);

        final Composite gridParent = new Composite(parent, SWT.NONE);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(gridParent);
        GridLayoutFactory.fillDefaults().numColumns(2).extendedMargins(10, 10, 0, 0).applyTo(gridParent);

        createCurrentSetupInfo(gridParent);
        createChooseModuleGroup(gridParent);
    }

    private void createCurrentSetupInfo(final Composite parent) {
        final Label currentEnvInfoLabel = new Label(parent, SWT.NONE);
        currentEnvInfoLabel.setText("Chosen Python installation: ");
        GridDataFactory.fillDefaults().indent(0, 10).applyTo(currentEnvInfoLabel);

        currentEnvLabel = new Label(parent, SWT.NONE);
        currentEnvLabel.setText("checking...");
        GridDataFactory.fillDefaults().indent(0, 10).applyTo(currentEnvLabel);
    }

    private void createChooseModuleGroup(final Composite parent) {
        final Group group = new Group(parent, SWT.NONE);
        group.setText("Runner module");
        GridDataFactory.fillDefaults().span(2, 1).indent(0, 10).grab(true, false).applyTo(group);
        GridLayoutFactory.fillDefaults().numColumns(2).margins(10, 10).applyTo(group);

        createPythonModuleControls(group);
        createExportedModuleControls(group);
        createChooseOwnModuleControls(group);
        createModuleLocationControls(group);
        createRequiredModuleInfo(group);
    }

    private void createPythonModuleControls(final Composite parent) {
        pythonModuleButton = new Button(parent, SWT.RADIO);
        pythonModuleButton.setText("Use redpydevd installed in chosen Python (recommended)");
        GridDataFactory.fillDefaults().span(2, 1).applyTo(pythonModuleButton);
        pythonModuleButton.addSelectionListener(Listeners.widgetSelectedAdapter(e -> checkPageCompletion()));
    }

    private void createExportedModuleControls(final Composite parent) {
        exportModuleButton = new Button(parent, SWT.RADIO);
        exportModuleButton.setText("Export redpydevd to external location defined below");
        GridDataFactory.fillDefaults().span(2, 1).applyTo(exportModuleButton);
        exportModuleButton.addSelectionListener(Listeners.widgetSelectedAdapter(e -> checkPageCompletion()));
    }

    private void createChooseOwnModuleControls(final Composite parent) {
        userModelButton = new Button(parent, SWT.RADIO);
        userModelButton.setText("Use redpydevd from external location defined below");
        GridDataFactory.fillDefaults().span(2, 1).applyTo(userModelButton);
        userModelButton.addSelectionListener(Listeners.widgetSelectedAdapter(e -> checkPageCompletion()));
    }

    private void createModuleLocationControls(final Composite parent) {
        moduleLocationText = new Text(parent, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER);
        GridDataFactory.fillDefaults().span(2, 1).indent(20, 0).grab(true, false).applyTo(moduleLocationText);

        moduleLocationButton = new Button(parent, SWT.PUSH);
        moduleLocationButton.setText("Choose location");
        GridDataFactory.fillDefaults().span(2, 1).align(SWT.END, SWT.BEGINNING).applyTo(moduleLocationButton);
        moduleLocationButton.addSelectionListener(Listeners.widgetSelectedAdapter(event -> {
            final DirectoryDialog dirDialog = new DirectoryDialog(parent.getShell());
            dirDialog.setMessage("Choose redpydevd module location");
            final String location = dirDialog.open();
            if (location != null) {
                moduleLocationText.setText(location);
            }
            checkPageCompletion();
        }));
    }

    private void createRequiredModuleInfo(final Composite parent) {
        versionDescriptionLabel = new CLabel(parent, SWT.NONE);
        versionDescriptionLabel.setImage(ImagesManager.getImage(RedImages.getBigInfoImage()));
    }

    @Override
    public void setVisible(final boolean visible) {
        if (visible && !Objects.equals(chosenEnv, debuggingSessionSetup.getRedEnvironment())) {
            initializeValues();
        }
        super.setVisible(visible);
    }

    private void initializeValues() {
        versionDescriptionLabel.setText("Detecting supported redpydevd runner module...");
        versionDescriptionLabel.pack();

        pythonModuleButton.setEnabled(false);
        exportModuleButton.setEnabled(false);
        userModelButton.setEnabled(false);
        moduleLocationButton.setEnabled(false);

        scheduleOperation(RedpydevdSearchingResult.class, new ModulesInfoLoader(), new ModulesInfoLoadListener());
    }

    private void checkPageCompletion() {
        setErrorMessage(null);
        setMessage(null, IMessageProvider.NONE);
        setPageComplete(true);

        if (pythonModuleButton.getSelection()) {
            if (!modulesInfo.isInstalled()) {
                setMessage(
                        "No redpydevd module was found in chosen Python installation. RED will install redpydevd "
                                + modulesInfo.currentModuleVersion + ".",
                        IMessageProvider.WARNING);

            } else if (!modulesInfo.isInstalledInCurrentVersion()) {
                setMessage(
                        "The redpydevd module was found in chosen Python installation in different version than "
                                + "required (found: " + modulesInfo.pythonModuleVersion.get() + ", required: "
                                + modulesInfo.currentModuleVersion + "). RED will upgrade redpydevd.",
                        IMessageProvider.WARNING);
            }

            debuggingSessionSetup.setUseRedpydevdFromInterpreter();
            debuggingSessionSetup.setRedpydevdRequiresInstallation(
                    !modulesInfo.isInstalled() || !modulesInfo.isInstalledInCurrentVersion());

        } else if (exportModuleButton.getSelection()) {
            final String path = moduleLocationText.getText();

            if (path.isEmpty()) {
                setErrorMessage("The module path is empty");
                setPageComplete(false);

            } else {
                final File redpydevdLocation = new File(new File(path), "redpydevd.py");

                if (redpydevdLocation.exists()) {
                    setMessage("The file '" + redpydevdLocation.getAbsolutePath() + "' already exist. It will "
                            + "be overridden.", IMessageProvider.WARNING);
                }
                debuggingSessionSetup.setRedpydevdLocation(redpydevdLocation);
                debuggingSessionSetup.setRedpydevdRequiresExport(true);
            }

        } else if (userModelButton.getSelection()) {
            final String path = moduleLocationText.getText();

            if (path.isEmpty()) {
                setErrorMessage("The module path is empty");
                setPageComplete(false);

            } else {
                final File redpydevdLocation = new File(new File(path), "redpydevd.py");

                if (!redpydevdLocation.exists()) {
                    setErrorMessage("The file '" + redpydevdLocation.getAbsolutePath() + "' does not exist");
                    setPageComplete(false);

                } else {
                    final Optional<String> version = NewRedPyDevConfigDebuggerScriptPage
                            .getVersionInfoFrom(Optional.of(redpydevdLocation));

                    if (!version.equals(Optional.of(modulesInfo.currentModuleVersion))) {
                        final String versionInfo = version.isPresent() ? "version " + version.get() : "unknown version";
                        setMessage("The file '" + redpydevdLocation.getAbsolutePath() + "' has " + versionInfo
                                + ". It may not be compatible with the version RED is expecting ("
                                + modulesInfo.currentModuleVersion + ").", IMessageProvider.WARNING);
                    }

                    debuggingSessionSetup.setRedpydevdLocation(redpydevdLocation);
                    debuggingSessionSetup.setRedpydevdRequiresExport(false);
                }
            }

        } else {
            setErrorMessage("Choose which module should be used");
            setPageComplete(false);
        }
    }

    private class ModulesInfoLoader implements MonitoredJobFunction<RedpydevdSearchingResult> {

        @Override
        public RedpydevdSearchingResult run(final IProgressMonitor monitor) {
            final RedpydevdSearchingResult result = new RedpydevdSearchingResult();
            try {
                result.currentModuleVersion = NewRedPyDevConfigDebuggerScriptPage
                        .getVersionInfoFrom(RedTemporaryDirectory.getRedpydevdInitAsStream())
                        .get();

            } catch (final IOException e) {
                throw new IllegalStateException("The redpydevd module should be available in RED", e);
            }
            Optional<File> pythonModuleLocation;
            try {
                pythonModuleLocation = debuggingSessionSetup.getRedEnvironment()
                        .getModulePath("redpydevd", new EnvironmentSearchPaths());
            } catch (final RuntimeEnvironmentException e) {
                pythonModuleLocation = Optional.empty();
            }
            result.pythonModuleVersion = NewRedPyDevConfigDebuggerScriptPage
                    .getVersionInfoFrom(pythonModuleLocation.map(f -> new File(f, "__init__.py")));
            return result;
        }
    }

    private class ModulesInfoLoadListener implements JobFinishListener<RedpydevdSearchingResult> {

        @Override
        public void jobFinished(final RedpydevdSearchingResult result) {
            final Control control = NewRedPyDevConfigRunnerScriptPage.this.getControl();
            if (control == null || control.isDisposed()) {
                return;
            }
            chosenEnv = debuggingSessionSetup.getRedEnvironment();
            modulesInfo = result;

            currentEnvLabel.setText(chosenEnv.getFile().getAbsolutePath());
            versionDescriptionLabel.setText("This version of RED requires and supports redpydevd runner module "
                    + "in version " + result.currentModuleVersion);
            versionDescriptionLabel.pack();

            pythonModuleButton.setEnabled(true);
            exportModuleButton.setEnabled(true);
            userModelButton.setEnabled(true);

            moduleLocationButton.setEnabled(true);

            if (!pythonModuleButton.getSelection() && !exportModuleButton.getSelection()
                    && !userModelButton.getSelection()) {
                pythonModuleButton.setSelection(true);
            }

            checkPageCompletion();
        }
    }

    private static class RedpydevdSearchingResult {

        private String currentModuleVersion;

        private Optional<String> pythonModuleVersion = Optional.empty();

        private boolean isInstalled() {
            return pythonModuleVersion.isPresent();
        }

        private boolean isInstalledInCurrentVersion() {
            return isInstalled() && pythonModuleVersion.get().equals(currentModuleVersion);
        }
    }
}
