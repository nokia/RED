/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch;

import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Lists.newArrayList;

import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;

import com.google.common.base.CaseFormat;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

public class RobotSuitesAndTestsNaming {

    public static String createSuiteName(final IResource suite) {
        return createSuiteName(suite.getProject(), suite.getProjectRelativePath());
    }

    public static String createSuiteName(final IProject project, final IPath path) {
        final String actualProjectName = project.getLocation().lastSegment();
        final List<String> upperCased = newArrayList(toRobotFrameworkName(actualProjectName));
        upperCased
                .addAll(Lists.transform(Arrays.asList(path.removeFileExtension().segments()), toRobotFrameworkName()));
        return Joiner.on('.').join(upperCased);
    }

    private static Function<String, String> toRobotFrameworkName() {
        return new Function<String, String>() {

            @Override
            public String apply(final String name) {
                return toRobotFrameworkName(name);
            }
        };
    }

    private static String toRobotFrameworkName(final String name) {
        // converts suite/test name to name used by RF
        String resultName = name;
        final int prefixIndex = resultName.indexOf("__");
        if (prefixIndex != -1) {
            resultName = resultName.substring(prefixIndex + 2);
        }
        final List<String> splittedNames = Splitter.on(' ').splitToList(resultName);
        final Iterable<String> titled = transform(splittedNames, new Function<String, String>() {

            @Override
            public String apply(final String name) {
                return CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, name);
            }
        });
        return Joiner.on(' ').join(titled);
    }
}
