/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch;

import static com.google.common.collect.Iterables.getFirst;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;

public class RobotLaunchConfigurationNaming {

    private static final String NEW_CONFIGURATION_NAME = "New Configuration";

    public static String getNamePrefix(final Collection<IResource> resources, final RobotLaunchConfigurationType type) {
        if (resources.size() == 1) {
            return getFirst(resources, null).getName() + type.getNameSuffix();
        }
        final Set<IProject> projects = new HashSet<>();
        for (final IResource res : resources) {
            if (projects.add(res.getProject())) {
                if (projects.size() > 1) {
                    break;
                }
            }
        }
        if (projects.size() == 1) {
            return getFirst(projects, null).getName() + type.getNameSuffix();
        }
        return NEW_CONFIGURATION_NAME;
    }

    public enum RobotLaunchConfigurationType {
        GENERAL_PURPOSE(""),
        SELECTED_TEST_CASES(" (Selected Test Cases)");

        private final String nameSuffix;

        private RobotLaunchConfigurationType(final String nameSuffix) {
            this.nameSuffix = nameSuffix;
        }

        public String getNameSuffix() {
            return nameSuffix;
        }
    }
}
