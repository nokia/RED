/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch;

import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Predicates;
import com.google.common.base.Splitter;
import com.google.common.collect.Maps;

abstract class AbstractRobotLaunchConfiguration
        implements ITagsRobotLaunchConfiguration, ISuitesRobotLaunchConfiguration {

    final ILaunchConfiguration configuration;

    AbstractRobotLaunchConfiguration(final ILaunchConfiguration config) {
        this.configuration = config;
    }

    @Override
    public String getName() {
        return configuration.getName();
    }

    @Override
    public void setProjectName(final String projectName) throws CoreException {
        final ILaunchConfigurationWorkingCopy launchCopy = asWorkingCopy();
        launchCopy.setAttribute(PROJECT_NAME_ATTRIBUTE, projectName);
    }

    @Override
    public String getProjectName() throws CoreException {
        return configuration.getAttribute(PROJECT_NAME_ATTRIBUTE, "");
    }

    @Override
    public void setIsIncludeTagsEnabled(final boolean isIncludeTagsEnabled) throws CoreException {
        final ILaunchConfigurationWorkingCopy launchCopy = asWorkingCopy();
        launchCopy.setAttribute(INCLUDE_TAGS_OPTION_ENABLED_ATTRIBUTE, isIncludeTagsEnabled);
    }

    @Override
    public void setIncludedTags(final List<String> tags) throws CoreException {
        final ILaunchConfigurationWorkingCopy launchCopy = asWorkingCopy();
        launchCopy.setAttribute(INCLUDED_TAGS_ATTRIBUTE, tags);
    }

    @Override
    public void setIsExcludeTagsEnabled(final boolean isExcludeTagsEnabled) throws CoreException {
        final ILaunchConfigurationWorkingCopy launchCopy = asWorkingCopy();
        launchCopy.setAttribute(EXCLUDE_TAGS_OPTION_ENABLED_ATTRIBUTE, isExcludeTagsEnabled);
    }

    @Override
    public void setExcludedTags(final List<String> tags) throws CoreException {
        final ILaunchConfigurationWorkingCopy launchCopy = asWorkingCopy();
        launchCopy.setAttribute(EXCLUDED_TAGS_ATTRIBUTE, tags);
    }

    @Override
    public boolean isIncludeTagsEnabled() throws CoreException {
        return configuration.getAttribute(INCLUDE_TAGS_OPTION_ENABLED_ATTRIBUTE, false);
    }

    @Override
    public List<String> getIncludedTags() throws CoreException {
        return configuration.getAttribute(INCLUDED_TAGS_ATTRIBUTE, new ArrayList<String>());
    }

    @Override
    public boolean isExcludeTagsEnabled() throws CoreException {
        return configuration.getAttribute(EXCLUDE_TAGS_OPTION_ENABLED_ATTRIBUTE, false);
    }

    @Override
    public List<String> getExcludedTags() throws CoreException {
        return configuration.getAttribute(EXCLUDED_TAGS_ATTRIBUTE, new ArrayList<String>());
    }

    @Override
    public void setSuitePaths(final Map<String, List<String>> suitesToCases) throws CoreException {
        // test case names should be always in lower case
        final ILaunchConfigurationWorkingCopy launchCopy = asWorkingCopy();
        final Map<String, String> suites = Maps.asMap(suitesToCases.keySet(), new Function<String, String>() {

            @Override
            public String apply(final String path) {
                final List<String> testSuites = new ArrayList<>();
                final Iterable<String> temp = filter(suitesToCases.get(path), Predicates.notNull());
                for (final String s : temp) {
                    testSuites.add(s.toLowerCase());
                }
                return Joiner.on("::").join(testSuites);
            }
        });
        launchCopy.setAttribute(TEST_SUITES_ATTRIBUTE, suites);
    }

    @Override
    public Map<String, List<String>> getSuitePaths() throws CoreException {
        final Map<String, String> mapping = configuration.getAttribute(TEST_SUITES_ATTRIBUTE,
                new HashMap<String, String>());
        final Map<String, List<String>> suitesToTestsMapping = new HashMap<>();
        for (final Entry<String, String> entry : mapping.entrySet()) {
            final List<String> splittedTestNames = Splitter.on("::").omitEmptyStrings().splitToList(entry.getValue());
            suitesToTestsMapping.put(entry.getKey(), splittedTestNames);
        }
        return suitesToTestsMapping;
    }

    @Override
    public void updateTestCases(final Map<IResource, List<String>> suitesMapping) throws CoreException {

        final Map<String, List<String>> suitesNamesMapping = new HashMap<>();
        for (final IResource resource : suitesMapping.keySet()) {
            if (!(resource instanceof IProject)) {
                suitesNamesMapping.put(resource.getProjectRelativePath().toPortableString(),
                        suitesMapping.get(resource));
            }
        }
        setSuitePaths(suitesNamesMapping);
    }

    @Override
    public Map<IResource, List<String>> collectSuitesToRun() throws CoreException {
        IProject project;
        final String projectName = getProjectName();
        if (projectName.isEmpty()) {
            project = null;
        } else {
            project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
            if (!project.exists()) {
                project = null;
            }
        }
        final Map<IResource, List<String>> suitesToRun = new HashMap<>();
        if (project == null) {
            return suitesToRun;
        }

        final Map<String, List<String>> suitePaths = getSuitePaths();
        for (final Entry<String, List<String>> entry : suitePaths.entrySet()) {
            final IPath path = Path.fromPortableString(entry.getKey());
            final IResource resource = path.getFileExtension() == null ? project.getFolder(path)
                    : project.getFile(path);
            suitesToRun.put(resource, entry.getValue());
        }
        return suitesToRun;
    }

    public String[] getEnvironmentVariables() throws CoreException {
        final Map<String, String> vars = configuration.getAttribute(ILaunchManager.ATTR_ENVIRONMENT_VARIABLES,
                (Map<String, String>) null);
        if (vars == null) {
            return null;
        }
        final boolean shouldAppendVars = configuration.getAttribute(ILaunchManager.ATTR_APPEND_ENVIRONMENT_VARIABLES,
                true);
        final List<String> varMappings = new ArrayList<>();
        if (shouldAppendVars) {
            appendVariables(varMappings, System.getenv());
        }
        appendVariables(varMappings, vars);
        return varMappings.toArray(new String[0]);
    }

    private void appendVariables(final List<String> varMappings, final Map<String, String> vars) {
        for (final Entry<String, String> entry : vars.entrySet()) {
            varMappings.add(entry.getKey() + "=" + entry.getValue());
        }
    }

    public RobotProject getRobotProject() throws CoreException {
        final IProject project = getProject();
        return RedPlugin.getModelManager().getModel().createRobotProject(project);
    }

    IProject getProject() throws CoreException {
        final String projectName = getProjectName();
        if (projectName.isEmpty()) {
            return null;
        }
        final IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
        if (!project.exists()) {
            throw newCoreException("Project '" + projectName + "' cannot be found in workspace", null);
        }
        return project;
    }

    @Override
    public List<String> getSuitesToRun() throws CoreException {
        final Collection<IResource> suites = getSuiteResources();

        final List<String> suiteNames = new ArrayList<>();
        for (final IResource suite : suites) {
            suiteNames.add(RobotSuitesNaming.createSuiteName(suite));
        }
        return suiteNames;
    }

    @Override
    public List<IResource> getResourcesUnderDebug() throws CoreException {
        final List<IResource> suiteResources = newArrayList(getSuiteResources());
        if (suiteResources.isEmpty()) {
            suiteResources.add(getProject());
        }
        return suiteResources;
    }

    private Collection<IResource> getSuiteResources() throws CoreException {
        final Collection<String> suitePaths = getSuitePaths().keySet();

        final Map<String, IResource> resources = Maps.asMap(newHashSet(suitePaths), new Function<String, IResource>() {

            @Override
            public IResource apply(final String suitePath) {
                try {
                    return getProject().findMember(Path.fromPortableString(suitePath));
                } catch (final CoreException e) {
                    return null;
                }
            }
        });

        final List<String> problems = new ArrayList<>();
        for (final Entry<String, IResource> entry : resources.entrySet()) {
            if (entry.getValue() == null || !entry.getValue().exists()) {
                problems.add(
                        "Suite '" + entry.getKey() + "' does not exist in project '" + getProject().getName() + "'");
            }
        }
        if (!problems.isEmpty()) {
            throw newCoreException(Joiner.on('\n').join(problems));
        }
        return resources.values();
    }

    @Override
    public Collection<String> getTestsToRun() throws CoreException {
        final List<String> tests = new ArrayList<>();
        for (final Entry<String, List<String>> entries : getSuitePaths().entrySet()) {
            for (final String testName : entries.getValue()) {
                tests.add(RobotSuitesNaming.createSuiteName(getProject(), Path.fromPortableString(entries.getKey()))
                        + "." + testName);
            }
        }
        return tests;
    }

    ILaunchConfigurationWorkingCopy asWorkingCopy() throws CoreException {
        return configuration instanceof ILaunchConfigurationWorkingCopy
                ? (ILaunchConfigurationWorkingCopy) configuration : configuration.getWorkingCopy();
    }

    static CoreException newCoreException(final String message) {
        return newCoreException(message, null);
    }

    static CoreException newCoreException(final String message, final Throwable cause) {
        return new CoreException(new Status(IStatus.ERROR, RedPlugin.PLUGIN_ID, message, cause));
    }

}
