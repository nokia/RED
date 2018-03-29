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
import java.util.Optional;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.variables.IStringVariableManager;
import org.eclipse.core.variables.VariablesPlugin;
import org.rf.ide.core.executor.RobotRuntimeEnvironment;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.launch.IRobotLaunchConfiguration;
import org.robotframework.ide.eclipse.main.plugin.launch.local.RobotLaunchConfiguration;
import org.robotframework.ide.eclipse.main.plugin.launch.remote.RemoteRobotLaunchConfiguration;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCasesSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelManager;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;

import com.google.common.annotations.VisibleForTesting;

/**
 * @author Michal Anglart
 */
class LaunchConfigurationTabValidator {

    private final RobotModel model;

    LaunchConfigurationTabValidator() {
        this(RedPlugin.getModelManager().getModel());
    }

    @VisibleForTesting
    LaunchConfigurationTabValidator(final RobotModel model) {
        this.model = model;
    }

    void validateRobotTab(final RobotLaunchConfiguration robotConfig)
            throws LaunchConfigurationValidationException, LaunchConfigurationValidationFatalException {
        try {
            final List<String> warnings = new ArrayList<>();

            try {
                robotConfig.getProject();
            } catch (final CoreException e) {
                throw new LaunchConfigurationValidationFatalException(e.getStatus().getMessage());
            }

            final Map<IResource, List<String>> suitesToRun = robotConfig.collectSuitesToRun();
            if (suitesToRun.isEmpty()) {
                warnings.add("There are no suites specified. All suites in '" + robotConfig.getProjectName()
                        + "' will be executed.");
            }
            validateSuitesToRun(suitesToRun);

            if (!warnings.isEmpty()) {
                throw new LaunchConfigurationValidationException(String.join("\n", warnings));
            }

        } catch (final CoreException e) {
            throw new LaunchConfigurationValidationFatalException(
                    "Run configuration '" + robotConfig.getName() + "' contains problems: " + e.getMessage(), e);
        }
    }

    void validateListenerTab(final IRobotLaunchConfiguration robotConfig)
            throws LaunchConfigurationValidationFatalException {
        try {
            if (robotConfig instanceof RemoteRobotLaunchConfiguration) {
                robotConfig.getProject();
            }
            if (robotConfig.isUsingRemoteAgent()) {
                robotConfig.getAgentConnectionHost();
                robotConfig.getAgentConnectionPort();
                robotConfig.getAgentConnectionTimeout();
            }
        } catch (final CoreException e) {
            throw new LaunchConfigurationValidationFatalException(e.getStatus().getMessage());
        }
    }

    void validateExecutorTab(final RobotLaunchConfiguration robotConfig)
            throws LaunchConfigurationValidationException, LaunchConfigurationValidationFatalException {
        try {
            final List<String> warnings = new ArrayList<>();

            if (robotConfig.isUsingInterpreterFromProject()) {
                validateRuntimeEnvironment(robotConfig.getProjectName(), robotConfig.getExecutableFilePath());
            } else {
                warnings.add("Tests will be launched using '" + robotConfig.getInterpreter().name()
                        + "' interpreter as defined in PATH environment variable");
            }

            validateExecutableFile(robotConfig.getExecutableFilePath());

            if (!warnings.isEmpty()) {
                throw new LaunchConfigurationValidationException(String.join("\n", warnings));
            }

        } catch (final CoreException e) {
            throw new LaunchConfigurationValidationFatalException(
                    "Run configuration '" + robotConfig.getName() + "' contains problems: " + e.getMessage(), e);
        }
    }

    private void validateRuntimeEnvironment(final String projectName, final String executableFilePath) {
        if (!projectName.isEmpty() && executableFilePath.isEmpty()) {
            final IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
            final RobotProject robotProject = model.createRobotProject(project);
            final RobotRuntimeEnvironment env = robotProject.getRuntimeEnvironment();
            if (env == null || !env.isValidPythonInstallation()) {
                throw new LaunchConfigurationValidationFatalException(
                        "Project '" + projectName + "' is using invalid Python environment");
            } else if (!env.hasRobotInstalled()) {
                throw new LaunchConfigurationValidationFatalException(
                        "Project '" + projectName + "' is using invalid Python environment (missing Robot Framework)");
            }
        }
    }

    private void validateExecutableFile(final String filePath) {
        if (!filePath.isEmpty()) {
            final IStringVariableManager variableManager = VariablesPlugin.getDefault().getStringVariableManager();
            try {
                final File file = new File(variableManager.performStringSubstitution(filePath));
                if (!file.exists()) {
                    throw new LaunchConfigurationValidationFatalException("Executable file does not exist");
                }
            } catch (final CoreException e) {
                throw new LaunchConfigurationValidationFatalException("Executable file does not exist", e);
            }
        }
    }

    private void validateSuitesToRun(final Map<IResource, List<String>> suitesToRun) {
        final List<String> problematicSuites = new ArrayList<>();
        final List<String> problematicTests = new ArrayList<>();
        suitesToRun.forEach((resource, caseNames) -> {
            if (!resource.exists()) {
                problematicSuites.add(resource.getFullPath().toString());
            } else if (resource.getType() == IResource.FILE) {
                final RobotSuiteFile suiteModel = RobotModelManager.getInstance().createSuiteFile((IFile) resource);
                final List<RobotCase> cases = new ArrayList<>();
                final Optional<RobotCasesSection> section = suiteModel.findSection(RobotCasesSection.class);
                if (section.isPresent()) {
                    cases.addAll(section.get().getChildren());
                }
                for (final String caseName : caseNames) {
                    if (cases.stream().noneMatch(test -> test.getName().equalsIgnoreCase(caseName))) {
                        problematicTests.add(caseName);
                    }
                }
            }
        });
        if (!problematicSuites.isEmpty()) {
            throw new LaunchConfigurationValidationFatalException(
                    "Following suites does not exist: " + String.join(", ", problematicSuites));
        }
        if (!problematicTests.isEmpty()) {
            throw new LaunchConfigurationValidationFatalException(
                    "Following tests does not exist: " + String.join(", ", problematicTests));
        }
    }

    static class LaunchConfigurationValidationException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        LaunchConfigurationValidationException(final String message) {
            super(message);
        }
    }

    static class LaunchConfigurationValidationFatalException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        LaunchConfigurationValidationFatalException(final String message) {
            super(message);
        }

        LaunchConfigurationValidationFatalException(final String message, final Exception e) {
            super(message, e);
        }
    }
}
