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

    public static final String SELECTED_TEST_CASES_SUFFIX = " (Selected Test Cases)";

    public static String getNamePrefix(final Collection<IResource> resources, final String suffix) {
        if (resources.size() == 1) {
            return getFirst(resources, null).getName() + suffix;
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
            return getFirst(projects, null).getName() + suffix;
        }
        return NEW_CONFIGURATION_NAME;
    }
}
