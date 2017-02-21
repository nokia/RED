/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

public interface ISuitesRobotLaunchConfiguration extends IRobotLaunchConfiguration {

    String TEST_SUITES_ATTRIBUTE = "Test suites";

    void setSuitePaths(final Map<String, List<String>> suitesToCases) throws CoreException;

    Map<String, List<String>> getSuitePaths() throws CoreException;

    void updateTestCases(final Map<IResource, List<String>> suitesMapping) throws CoreException;

    Map<IResource, List<String>> collectSuitesToRun() throws CoreException;

    List<String> getSuitesToRun() throws CoreException;

    List<IResource> getResourcesUnderDebug() throws CoreException;

    Collection<String> getTestsToRun() throws CoreException;

}
