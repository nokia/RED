/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch;

import static com.google.common.collect.Iterables.getOnlyElement;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.robotframework.ide.eclipse.main.plugin.launch.local.RobotLaunchConfiguration;

public class RobotLaunchConfigurationNaming {

    private static final String NEW_CONFIGURATION_NAME = "New Configuration";

    public static String getBasicName(final Collection<IResource> resources, final RobotLaunchConfigurationType type) {
        if (resources.size() == 1) {
            return getOnlyElement(resources).getName() + type.getNameSuffix();
        }
        final Set<IProject> projects = resources.stream()
                .map(IResource::getProject)
                .distinct()
                .limit(2)
                .collect(Collectors.toSet());
        if (projects.size() == 1) {
            return getOnlyElement(projects).getName() + type.getNameSuffix();
        }
        return NEW_CONFIGURATION_NAME;
    }

    public static String getRerunConfigurationName(final ILaunchConfiguration launchConfig,
            final RobotLaunchConfigurationType type) throws CoreException {
        final RobotLaunchConfiguration robotConfig = new RobotLaunchConfiguration(launchConfig);
        return robotConfig.isRerunConfiguration() ? launchConfig.getName()
                : launchConfig.getName() + type.getNameSuffix();
    }

    public enum RobotLaunchConfigurationType {
        GENERAL_PURPOSE(""),
        SELECTED_TEST_CASES(" (Selected Test Cases)"),
        RERUN_TEST_CASES(" (Rerun)");

        private final String nameSuffix;

        private RobotLaunchConfigurationType(final String nameSuffix) {
            this.nameSuffix = nameSuffix;
        }

        public String getNameSuffix() {
            return nameSuffix;
        }
    }
}
