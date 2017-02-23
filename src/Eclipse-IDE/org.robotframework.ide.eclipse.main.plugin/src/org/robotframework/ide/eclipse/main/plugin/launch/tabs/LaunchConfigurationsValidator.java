/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch.tabs;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.rf.ide.core.executor.RobotRuntimeEnvironment;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.launch.IRemoteRobotLaunchConfiguration;
import org.robotframework.ide.eclipse.main.plugin.launch.local.RobotLaunchConfiguration;
import org.robotframework.ide.eclipse.main.plugin.launch.script.ScriptRobotLaunchConfiguration;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCasesSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelManager;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;

/**
 * @author Michal Anglart
 */
public class LaunchConfigurationsValidator {

    private final RobotModel model;

    public LaunchConfigurationsValidator() {
        this(RedPlugin.getModelManager().getModel());
    }

    @VisibleForTesting
    LaunchConfigurationsValidator(final RobotModel model) {
        this.model = model;
    }

    public void validate(final IRemoteRobotLaunchConfiguration robotConfig)
            throws LaunchConfigurationValidationFatalException {

        try {
            if (robotConfig.isDefiningProjectDirectly()) {
                final String projectName = robotConfig.getProjectName();
                validateProject(projectName);
            }

            try {
                robotConfig.getRemoteDebugHost();
                robotConfig.getRemoteDebugPort();
                robotConfig.getRemoteDebugTimeout();
            } catch (final CoreException e) {
                throw new LaunchConfigurationValidationFatalException(e.getStatus().getMessage());
            }
        } catch (final CoreException e) {
            throw new LaunchConfigurationValidationFatalException(
                    "Run configuration '" + robotConfig.getName() + "' contains problems: " + e.getMessage() + ".", e);
        }
    }

    void validate(final RobotLaunchConfiguration robotConfig)
            throws LaunchConfigurationValidationException, LaunchConfigurationValidationFatalException {
        try {
            final List<String> warnings = new ArrayList<>();

            final String projectName = robotConfig.getProjectName();
            validateProject(projectName);

            if (robotConfig.isUsingInterpreterFromProject()) {
                validateRuntimeEnvironment(projectName);
            } else {
                warnings.add("Tests will be launched using '" + robotConfig.getExecutor().name()
                        + "' interpreter as defined in PATH environment variable.");
            }

            final Map<IResource, List<String>> suitesToRun = robotConfig.collectSuitesToRun();
            if (suitesToRun.isEmpty()) {
                warnings.add("There are no suites specified. All suites in '" + projectName + "' will be executed.");
            }
            validateSuitesToRun(suitesToRun);

            if (!warnings.isEmpty()) {
                throw new LaunchConfigurationValidationException(String.join("\n", warnings));
            }

        } catch (final CoreException e) {
            throw new LaunchConfigurationValidationFatalException(
                    "Run configuration '" + robotConfig.getName() + "' contains problems: " + e.getMessage() + ".", e);
        }
    }

    void validate(final ScriptRobotLaunchConfiguration robotConfig) {
        try {
            final List<String> warnings = new ArrayList<>();

            final String projectName = robotConfig.getProjectName();
            validateProject(projectName);

            validateExecutorScript(robotConfig.getScriptPath());

            final Map<IResource, List<String>> suitesToRun = robotConfig.collectSuitesToRun();
            if (suitesToRun.isEmpty()) {
                warnings.add("There are no suites specified. All suites in '" + projectName + "' will be executed.");
            }
            validateSuitesToRun(suitesToRun);

            if (!warnings.isEmpty()) {
                throw new LaunchConfigurationValidationException(String.join("\n", warnings));
            }

        } catch (final CoreException e) {
            throw new LaunchConfigurationValidationFatalException(
                    "Run configuration '" + robotConfig.getName() + "' contains problems: " + e.getMessage() + ".", e);
        }
    }

    private void validateProject(final String projectName) throws CoreException {
        final Optional<IProject> project = getProject(projectName);
        if (!project.isPresent() || !project.get().exists()) {
            throw new LaunchConfigurationValidationFatalException(
                    "Project '" + projectName + "' does not exist in workspace.");
        }
        if (!project.get().isOpen()) {
            throw new LaunchConfigurationValidationFatalException("Project '" + projectName + "' is currently closed.");
        }
    }

    private void validateRuntimeEnvironment(final String projectName) {
        final IProject project = getProject(projectName).get();
        final RobotProject robotProject = model.createRobotProject(project);
        final RobotRuntimeEnvironment env = robotProject.getRuntimeEnvironment();
        if (env == null || !env.isValidPythonInstallation()) {
            throw new LaunchConfigurationValidationFatalException(
                    "Project '" + projectName + "' is using invalid Python environment.");
        } else if (!env.hasRobotInstalled()) {
            throw new LaunchConfigurationValidationFatalException(
                    "Project '" + projectName + "' is using invalid Python environment (missing Robot Framework).");
        }
    }

    private void validateExecutorScript(final String filePath) {
        if (filePath.isEmpty()) {
            throw new LaunchConfigurationValidationFatalException("Executor script file path is not defined.");
        }
        final File file = new File(filePath);
        if (!file.exists()) {
            throw new LaunchConfigurationValidationFatalException("Executor script file does not exist.");
        }
    }

    private Optional<IProject> getProject(final String projectName) {
        return projectName.isEmpty() ? Optional.<IProject> absent()
                : Optional.of(ResourcesPlugin.getWorkspace().getRoot().getProject(projectName));
    }

    private void validateSuitesToRun(final Map<IResource, List<String>> suitesToRun) {
        final List<String> problematicSuites = new ArrayList<>();
        final List<String> problematicTests = new ArrayList<>();
        for (final IResource resource : suitesToRun.keySet()) {
            if (!resource.exists()) {
                problematicSuites.add(resource.getFullPath().toString());
            } else if (resource.getType() == IResource.FILE) {
                final RobotSuiteFile suiteModel = RobotModelManager.getInstance().createSuiteFile((IFile) resource);
                final List<RobotCase> cases = new ArrayList<>();
                final Optional<RobotCasesSection> section = suiteModel.findSection(RobotCasesSection.class);
                if (section.isPresent()) {
                    cases.addAll(section.get().getChildren());
                }
                for (final String caseName : suitesToRun.get(resource)) {
                    boolean exist = false;
                    for (final RobotCase test : cases) {
                        if (test.getName().equalsIgnoreCase(caseName)) {
                            exist = true;
                            break;
                        }
                    }
                    if (!exist) {
                        problematicTests.add(caseName);
                    }
                }
            }
        }
        if (!problematicSuites.isEmpty()) {
            throw new LaunchConfigurationValidationFatalException(
                    "Following suites does not exist: " + String.join(", ", problematicSuites) + ".");
        }
        if (!problematicTests.isEmpty()) {
            throw new LaunchConfigurationValidationFatalException(
                    "Following tests does not exist: " + String.join(", ", problematicTests) + ".");
        }
    }

    class LaunchConfigurationValidationException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        public LaunchConfigurationValidationException(final String message) {
            super(message);
        }
    }

    class LaunchConfigurationValidationFatalException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        public LaunchConfigurationValidationFatalException(final String message) {
            super(message);
        }

        public LaunchConfigurationValidationFatalException(final String message, final Exception e) {
            super(message, e);
        }
    }
}
