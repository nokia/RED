/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch.tabs;

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
import org.robotframework.ide.eclipse.main.plugin.launch.RobotLaunchConfiguration;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCasesSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelManager;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;

/**
 * @author Michal Anglart
 *
 */
class RobotLaunchConfigurationValidator {

    private final RobotModel model;

    RobotLaunchConfigurationValidator() {
        this(RedPlugin.getModelManager().getModel());
    }

    @VisibleForTesting
    RobotLaunchConfigurationValidator(final RobotModel model) {
        this.model = model;
    }

    void validate(final RobotLaunchConfiguration robotConfig) {
        try {
            final List<String> warnings = new ArrayList<>();

            final String projectName = robotConfig.getProjectName();
            final Optional<IProject> project = getProject(projectName);
            if (!project.isPresent() || !project.get().exists()) {
                throw new RobotLaunchConfigurationValidationFatalException(
                        "Project '" + projectName + "' does not exist in workspace");
            }
            if (!project.get().isOpen()) {
                throw new RobotLaunchConfigurationValidationFatalException(
                        "Project '" + projectName + "' is currently closed");
            }
            if (robotConfig.isUsingInterpreterFromProject()) {
                final RobotProject robotProject = model.createRobotProject(project.get());
                final RobotRuntimeEnvironment env = robotProject.getRuntimeEnvironment();
                if (env == null || !env.isValidPythonInstallation()) {
                    throw new RobotLaunchConfigurationValidationFatalException(
                            "Project '" + projectName + "' is using invalid Python environment");
                } else if (!env.hasRobotInstalled()) {
                    throw new RobotLaunchConfigurationValidationFatalException("Project '" + projectName
                            + "' is using invalid Python environment (missing Robot Framework)");
                }
            } else {
                warnings.add("Tests will be launched using '" + robotConfig.getExecutor().name()
                        + "' interpreter as defined in PATH environment variable");
            }


            final Map<IResource, List<String>> suitesToRun = robotConfig.collectSuitesToRun();
            if (suitesToRun.isEmpty()) {
                warnings.add("There are no suites specified. All suites in '" + projectName + "' will be executed");
            }
            validateSuitesToRun(suitesToRun);

            if (!warnings.isEmpty()) {
                throw new RobotLaunchConfigurationValidationException(Joiner.on('\n').join(warnings));
            }

        } catch (final CoreException e) {
            throw new RobotLaunchConfigurationValidationFatalException(
                    "Run configuration '" + robotConfig.getName() + "' contains problems: " + e.getMessage(), e);
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
            throw new RobotLaunchConfigurationValidationFatalException(
                    "Following suites does not exist: " + Joiner.on(", ").join(problematicSuites));
        }
        if (!problematicTests.isEmpty()) {
            throw new RobotLaunchConfigurationValidationFatalException(
                    "Following tests does not exist: " + Joiner.on(", ").join(problematicTests));
        }
    }

    class RobotLaunchConfigurationValidationException extends RuntimeException {
        public RobotLaunchConfigurationValidationException(final String message) {
            super(message);
        }
    }

    class RobotLaunchConfigurationValidationFatalException extends RuntimeException {
        public RobotLaunchConfigurationValidationFatalException(final String message) {
            super(message);
        }
        public RobotLaunchConfigurationValidationFatalException(final String message, final Exception e) {
            super(message, e);
        }
    }
}
