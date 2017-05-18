/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch;

import static com.google.common.collect.Lists.newArrayList;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;

import com.google.common.base.CaseFormat;

public class RobotPathsNaming {

    public static String createSuiteName(final IResource suite) {
        return createSuiteName(suite.getProject(), suite.getProjectRelativePath());
    }

    public static String createSuiteName(final IProject project, final IPath path) {
        final List<String> nameParts = newArrayList(project.getLocation().lastSegment());
        nameParts.addAll(Arrays.asList(path.removeFileExtension().segments()));
        return nameParts.stream().map(RobotPathsNaming::toRobotFrameworkName).collect(Collectors.joining("."));
    }

    public static String createTestName(final IProject project, final IPath path, final String testName) {
        return createSuiteName(project, path) + "." + testName;
    }

    private static String toRobotFrameworkName(final String name) {
        // converts suite/test name to name used by RF
        String resultName = name;
        final int prefixIndex = resultName.indexOf("__");
        if (prefixIndex != -1) {
            resultName = resultName.substring(prefixIndex + 2);
        }
        return Arrays.stream(resultName.split(" "))
                .map(namePart -> CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, namePart))
                .collect(Collectors.joining(" "));
    }
}
