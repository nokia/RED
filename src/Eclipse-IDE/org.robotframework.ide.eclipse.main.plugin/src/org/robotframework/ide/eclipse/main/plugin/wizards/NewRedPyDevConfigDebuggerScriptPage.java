/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.wizards;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.rf.ide.core.environment.EnvironmentSearchPaths;
import org.rf.ide.core.environment.IRuntimeEnvironment;
import org.rf.ide.core.environment.IRuntimeEnvironment.RuntimeEnvironmentException;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.launch.tabs.ExecutableFileComposite;
import org.robotframework.ide.eclipse.main.plugin.launch.variables.RedStringVariablesManager;
import org.robotframework.red.graphics.ImagesManager;
import org.robotframework.red.jface.wizards.JobWizardPage;
import org.robotframework.red.swt.Listeners;

import com.google.common.base.Charsets;
import com.google.common.collect.Range;
import com.google.common.primitives.Ints;


public class NewRedPyDevConfigDebuggerScriptPage extends JobWizardPage {

    private IRuntimeEnvironment chosenEnv;

    private final NewRedPyDevConfigWizardData debuggingSessionSetup;

    private PydevdSearchingResult modulesInfo;

    private Label currentEnvLabel;

    private Button pyDevDbgButton;
    private Label pyDevDbgVersionLabel;

    private Button pythonDbgButton;
    private Label pythonDbgVersionLabel;

    private Button userDbgButton;
    private ExecutableFileComposite userDevModuleChooser;

    private Button geventButton;

    private Text clientAddress;
    private Text clientPort;


    protected NewRedPyDevConfigDebuggerScriptPage(final NewRedPyDevConfigWizardData debuggingSessionSetup) {
        super("Choose pydevd debugger module to be used",
                "Create launch configuration for RED and PyDev debugging session", null);
        setDescription("Choose pydevd debugger module to be used");

        this.debuggingSessionSetup = debuggingSessionSetup;
    }

    @Override
    protected void create(final Composite parent) {
        setPageComplete(false);

        final Composite gridParent = new Composite(parent, SWT.NONE);
        GridDataFactory.fillDefaults().grab(true, true).hint(600, SWT.DEFAULT).applyTo(gridParent);
        GridLayoutFactory.fillDefaults().numColumns(2).extendedMargins(10, 10, 0, 0).applyTo(gridParent);

        createCurrentSetupInfo(gridParent);
        createChooseModuleGroup(gridParent);
        createAdditionalSetupGroup(gridParent);
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
        group.setText("Debugger module");
        GridDataFactory.fillDefaults().span(2, 1).indent(0, 10).grab(true, false).applyTo(group);
        GridLayoutFactory.fillDefaults().numColumns(2).margins(10, 10).applyTo(group);

        createPyDevModuleControls(group);
        createPythonModuleControls(group);
        createOwnModuleControls(group);
        createGeventCheckbox(group);
    }

    private void createPyDevModuleControls(final Composite parent) {
        pyDevDbgButton = new Button(parent, SWT.RADIO);
        pyDevDbgButton.setText("Use pydevd distributed with PyDev (recommended)");
        GridDataFactory.fillDefaults().span(2, 1).applyTo(pyDevDbgButton);
        pyDevDbgButton.addSelectionListener(Listeners.widgetSelectedAdapter(e -> {
            if (pyDevDbgButton.getSelection()) {
                checkPageCompletion();
            }
        }));

        final Composite inner = new Composite(parent, SWT.NONE);
        GridDataFactory.fillDefaults().span(2, 1).indent(50, 0).grab(true, false).applyTo(inner);
        GridLayoutFactory.fillDefaults().numColumns(2).applyTo(inner);

        final Label versionTitleLabel = new Label(inner, SWT.NONE);
        versionTitleLabel.setText("pydevd version: ");
        pyDevDbgVersionLabel = new Label(inner, SWT.NONE);
    }

    private void createPythonModuleControls(final Composite parent) {
        pythonDbgButton = new Button(parent, SWT.RADIO);
        pythonDbgButton.setText("Use pydevd installed in chosen Python");
        GridDataFactory.fillDefaults().span(2, 1).applyTo(pythonDbgButton);
        pythonDbgButton.addSelectionListener(Listeners.widgetSelectedAdapter(e -> {
            if (pythonDbgButton.getSelection()) {
                checkPageCompletion();
            }
        }));

        final Composite inner = new Composite(parent, SWT.NONE);
        GridDataFactory.fillDefaults().span(2, 1).indent(50, 0).grab(true, false).applyTo(inner);
        GridLayoutFactory.fillDefaults().numColumns(2).applyTo(inner);

        final Label versionTitleLabel = new Label(inner, SWT.NONE);
        versionTitleLabel.setText("pydevd version: ");
        pythonDbgVersionLabel = new Label(inner, SWT.NONE);
    }

    private void createOwnModuleControls(final Composite parent) {
        userDbgButton = new Button(parent, SWT.RADIO);
        userDbgButton.setText("Use pydevd from external location defined below");
        GridDataFactory.fillDefaults().span(2, 1).applyTo(userDbgButton);
        userDbgButton.addSelectionListener(Listeners.widgetSelectedAdapter(e -> {
            if (userDbgButton.getSelection()) {
                checkPageCompletion();
            }
        }));

        userDevModuleChooser = new ExecutableFileComposite(parent, "Select pydved script file:",
                new String[] { "*.py" });
        userDevModuleChooser.addModifyListener(e -> checkPageCompletion());
        GridDataFactory.fillDefaults().span(2, 1).indent(20, 0).applyTo(userDevModuleChooser);
    }

    private void createGeventCheckbox(final Group parent) {
        geventButton = new Button(parent, SWT.CHECK);
        geventButton.setText("Gevent compatible");
        geventButton.setToolTipText("When this option is on the pydevd will be able to debug Gevent based code."
                + " This will add GEVENT_SUPPORT environment variable to generated launch configuration.");
        geventButton.setSelection(false);
        geventButton.addSelectionListener(Listeners.widgetSelectedAdapter(e -> checkPageCompletion()));
    }

    private void createAdditionalSetupGroup(final Composite parent) {
        final Group connectionGroup = new Group(parent, SWT.NONE);
        connectionGroup.setText("Debugger module settings");
        GridDataFactory.fillDefaults().span(2, 1).grab(true, false).applyTo(connectionGroup);
        GridLayoutFactory.fillDefaults().numColumns(2).margins(10, 10).applyTo(connectionGroup);

        final Label clientLabel = new Label(connectionGroup, SWT.NONE);
        clientLabel.setText("Client");

        clientAddress = new Text(connectionGroup, SWT.BORDER);
        clientAddress.addModifyListener(e -> checkPageCompletion());
        GridDataFactory.swtDefaults().hint(120, SWT.DEFAULT).applyTo(clientAddress);

        final Label portLabel = new Label(connectionGroup, SWT.NONE);
        portLabel.setText("Port");

        clientPort = new Text(connectionGroup, SWT.BORDER);
        clientPort.addModifyListener(e -> checkPageCompletion());
        GridDataFactory.swtDefaults().hint(120, SWT.DEFAULT).applyTo(clientPort);

        final CLabel dbgServerReminder = new CLabel(connectionGroup, SWT.NONE);
        dbgServerReminder.setText("Remember to start PyDev Debug Server prior to launching session");
        dbgServerReminder.setImage(ImagesManager.getImage(RedImages.getBigWarningImage()));
        GridDataFactory.fillDefaults().span(2, 1).applyTo(dbgServerReminder);
    }

    @Override
    public void setVisible(final boolean visible) {
        if (visible && !Objects.equals(chosenEnv, debuggingSessionSetup.getRedEnvironment())) {
            initializeValues();
        }
        super.setVisible(visible);
    }

    private void initializeValues() {
        pyDevDbgButton.setEnabled(false);
        pyDevDbgVersionLabel.setText("detecting...");
        pyDevDbgVersionLabel.pack();

        pythonDbgButton.setEnabled(false);
        pythonDbgVersionLabel.setText("detecting...");
        pythonDbgVersionLabel.pack();

        userDbgButton.setEnabled(false);
        userDevModuleChooser.setEnabled(false);

        clientAddress.setEnabled(false);
        clientPort.setEnabled(false);

        scheduleOperation(PydevdSearchingResult.class, new PydevdModulesInfoLoader(),
                new PydevdModulesInfoLoadListener());
    }

    private void checkPageCompletion() {
        setErrorMessage(null);
        setMessage(null, IMessageProvider.NONE);
        setPageComplete(true);

        debuggingSessionSetup.setGeventSupport(geventButton.getSelection());
        if (pyDevDbgButton.getSelection()) {
            debuggingSessionSetup.setPydevdLocation(modulesInfo.pyDevDbgModuleLocation.get());

        } else if (pythonDbgButton.getSelection()) {
            debuggingSessionSetup.setUsePydevdFromInterpreter();
            if (!modulesInfo.pyDevDbgModuleVersion.equals(modulesInfo.pythonDbgModuleVersion)) {
                setMessage("The version installed into your Python is different than the one provided by PyDev. "
                        + "This may result in unexpected debugger behavior", IMessageProvider.WARNING);
            }

        } else if (userDbgButton.getSelection()) {
            final String userPath = userDevModuleChooser.getFilePath();
            if (userPath.isEmpty()) {
                setErrorMessage("Script path is empty");
                setPageComplete(false);
            } else {
                final RedStringVariablesManager variableManager = new RedStringVariablesManager();
                try {
                    final File file = new File(variableManager.substituteUsingQuickValuesSet(userPath));
                    if (!file.exists()) {
                        setErrorMessage("Script file does not exist");
                        setPageComplete(false);

                    } else if (!file.getName().equalsIgnoreCase("pydevd.py")) {
                        setErrorMessage("Script file is not 'pydevd.py'");
                        setPageComplete(false);

                    } else {
                        debuggingSessionSetup.setPydevdLocation(file);
                    }

                } catch (final CoreException e) {
                    setErrorMessage("Given script file does not exist");
                    setPageComplete(false);
                }
            }

        } else {
            setErrorMessage("Choose which module should be used");
            setPageComplete(false);
        }

        if (clientAddress.getText().isEmpty()) {
            setErrorMessageIfNoneIsSet("Address cannot be empty");
            setPageComplete(false);

            debuggingSessionSetup.setAddress(null);
        } else {
            debuggingSessionSetup.setAddress(clientAddress.getText());
        }

        final Integer port = Ints.tryParse(clientPort.getText());
        if (port == null || !Range.closed(1, 65_535).contains(port)) {
            setErrorMessageIfNoneIsSet("Port should be a an integer value between 1 and 65 535");
            setPageComplete(false);

            debuggingSessionSetup.setPort(null);
        } else {
            debuggingSessionSetup.setPort(port);
        }
    }

    private void setErrorMessageIfNoneIsSet(final String newMessage) {
        if (getErrorMessage() == null) {
            setErrorMessage(newMessage);
        }
    }

    static Optional<String> getVersionInfoFrom(final Optional<File> pydevdFile) {
        return pydevdFile.map(Stream::of)
                .orElseGet(Stream::empty)
                .flatMap(NewRedPyDevConfigDebuggerScriptPage::fileLines)
                .filter(line -> line.trim().matches("^ *__version_info__ =.*$"))
                .findFirst()
                .map(NewRedPyDevConfigDebuggerScriptPage::extractVersion);
    }

    static Optional<String> getVersionInfoFrom(final InputStream stream) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, Charsets.UTF_8))) {
            return reader.lines()
                    .filter(line -> line.trim().matches("^ *__version_info__ =.*$"))
                    .findFirst()
                    .map(NewRedPyDevConfigDebuggerScriptPage::extractVersion);
        } catch (final IOException e) {
            return Optional.empty();
        }
    }

    private static Stream<String> fileLines(final File filePath) {
        try {
            return Files.lines(filePath.toPath(), Charsets.UTF_8);
        } catch (final IOException e1) {
            return Stream.empty();
        }
    }

    private static String extractVersion(final String line) {
        final Matcher matcher = Pattern.compile("\\( *(\\d+ *, *\\d+ *, *\\d+) *\\)").matcher(line);
        return matcher.find() ? matcher.group(1).trim().replaceAll(", *", ".") : null;
    }

    private class PydevdModulesInfoLoader implements MonitoredJobFunction<PydevdSearchingResult> {

        @Override
        public PydevdSearchingResult run(final IProgressMonitor monitor) {
            final PydevdSearchingResult result = new PydevdSearchingResult();
            result.foundPyDev = isPyDevInstalled();
            result.pyDevDbgModuleLocation = findPydevdInPyDev();
            result.pyDevDbgModuleVersion = getVersionInfoFrom(result.pyDevDbgModuleLocation);
            Optional<File> pythonDbgModuleLocation;
            try {
                pythonDbgModuleLocation = debuggingSessionSetup.getRedEnvironment()
                        .getModulePath("pydevd", new EnvironmentSearchPaths());
            } catch (final RuntimeEnvironmentException e) {
                pythonDbgModuleLocation = Optional.empty();
            }
            result.pythonDbgModuleVersion = getVersionInfoFrom(pythonDbgModuleLocation);
            return result;
        }

        private boolean isPyDevInstalled() {
            return Platform.getBundle("org.python.pydev") != null;
        }

        private Optional<File> findPydevdInPyDev() {
            return Stream.of("org.python.pydev.core", "org.python.pydev")
                    .map(Platform::getBundle)
                    .map(bundle -> bundle == null ? null : FileLocator.find(bundle, new Path("pysrc/pydevd.py"), null))
                    .filter(url -> url != null)
                    .findFirst()
                    .map(this::resolveUrlToFile);
        }

        private File resolveUrlToFile(final URL url) {
            try {
                return new File(FileLocator.resolve(url).toURI());
            } catch (URISyntaxException | IOException e) {
                return null;
            }
        }
    }

    private class PydevdModulesInfoLoadListener implements JobFinishListener<PydevdSearchingResult> {

        @Override
        public void jobFinished(final PydevdSearchingResult result) {
            final Control control = NewRedPyDevConfigDebuggerScriptPage.this.getControl();
            if (control == null || control.isDisposed()) {
                return;
            }
            chosenEnv = debuggingSessionSetup.getRedEnvironment();
            modulesInfo = result;

            currentEnvLabel.setText(chosenEnv.getFile().getAbsolutePath());

            final boolean isPyDevDbgPossible = result.foundPyDev && result.pyDevDbgModuleLocation.isPresent();
            final boolean isPythonDbgPossible = result.pythonDbgModuleVersion.isPresent();

            pyDevDbgButton.setEnabled(isPyDevDbgPossible);
            pyDevDbgVersionLabel.setText(result.pyDevDbgModuleVersion.orElse("<unknown>"));
            pyDevDbgVersionLabel.pack();

            pythonDbgButton.setEnabled(isPythonDbgPossible);
            pythonDbgVersionLabel.setText(result.pythonDbgModuleVersion.orElse("<unknown>"));
            pythonDbgVersionLabel.pack();

            userDbgButton.setEnabled(true);
            userDevModuleChooser.setEnabled(true);

            if (pyDevDbgButton.getSelection() || pythonDbgButton.getSelection() || userDbgButton.getSelection()) {
                if (pyDevDbgButton.getSelection() && !isPyDevDbgPossible) {
                    pyDevDbgButton.setSelection(false);
                    if (isPythonDbgPossible) {
                        pythonDbgButton.setSelection(true);
                    } else {
                        userDbgButton.setSelection(true);
                    }

                } else if (pythonDbgButton.getSelection() && !isPythonDbgPossible) {
                    pythonDbgButton.setSelection(false);
                    if (isPyDevDbgPossible) {
                        pyDevDbgButton.setSelection(true);
                    } else {
                        userDbgButton.setSelection(true);
                    }
                }
                // otherwise the previous selection stays on same button

            } else {
                if (isPyDevDbgPossible) {
                    pyDevDbgButton.setSelection(true);
                } else if (isPythonDbgPossible) {
                    pythonDbgButton.setSelection(true);
                } else {
                    userDbgButton.setSelection(true);
                }
            }

            clientAddress.setEnabled(true);
            if (clientAddress.getText().isEmpty()) {
                clientAddress.setText(NewRedPyDevConfigWizardData.DEFAULT_ADDRESS);
            }
            clientPort.setEnabled(true);
            if (clientPort.getText().isEmpty()) {
                clientPort.setText(Integer.toString(NewRedPyDevConfigWizardData.DEFAULT_PORT));
            }

            checkPageCompletion();
        }
    }

    private static class PydevdSearchingResult {

        private boolean foundPyDev = false;
        private Optional<File> pyDevDbgModuleLocation = Optional.empty();
        private Optional<String> pyDevDbgModuleVersion = Optional.empty();

        private Optional<String> pythonDbgModuleVersion = Optional.empty();

    }
}
