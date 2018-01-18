/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch.local;

import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.getFirst;
import static com.google.common.collect.Lists.newArrayList;
import static org.robotframework.ide.eclipse.main.plugin.RedPlugin.newCoreException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.rf.ide.core.executor.RedSystemProperties;
import org.rf.ide.core.executor.SuiteExecutor;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences;
import org.robotframework.ide.eclipse.main.plugin.launch.AbstractRobotLaunchConfiguration;
import org.robotframework.ide.eclipse.main.plugin.launch.RobotLaunchConfigurationNaming;
import org.robotframework.ide.eclipse.main.plugin.launch.RobotLaunchConfigurationNaming.RobotLaunchConfigurationType;
import org.robotframework.ide.eclipse.main.plugin.launch.RobotPathsNaming;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;

import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

public class RobotLaunchConfiguration extends AbstractRobotLaunchConfiguration {

    public static final String TYPE_ID = "org.robotframework.ide.robotLaunchConfiguration";

    private static final String USE_PROJECT_INTERPRETER_ATTRIBUTE = "Project interpreter";

    private static final String INTERPRETER_NAME_ATTRIBUTE = "Interpreter name";

    private static final String INTERPRETER_ARGUMENTS_ATTRIBUTE = "Interpreter arguments";

    private static final String TEST_SUITES_ATTRIBUTE = "Test suites";

    private static final String INCLUDE_TAGS_OPTION_ENABLED_ATTRIBUTE = "Include option enabled";

    private static final String INCLUDED_TAGS_ATTRIBUTE = "Included tags";

    private static final String EXCLUDE_TAGS_OPTION_ENABLED_ATTRIBUTE = "Exclude option enabled";

    private static final String EXCLUDED_TAGS_ATTRIBUTE = "Excluded tags";

    private static final String ROBOT_ARGUMENTS_ATTRIBUTE = "Robot arguments";

    private static final String GENERAL_PURPOSE_OPTION_ENABLED_ATTRIBUTE = "General purpose option enabled";

    private static final String EXECUTABLE_FILE_PATH_ATTRIBUTE = "Executable file path";

    private static final String EXECUTABLE_FILE_ARGUMENTS_ATTRIBUTE = "Executable file arguments";

    public static final String CURRENT_CONFIGURATION_VERSION = "1";

    public static String[] getSystemDependentExecutableFileExtensions() {
        return RedSystemProperties.isWindowsPlatform() ? new String[] { "*.bat;*.com;*.exe", "*.*" }
                : new String[] { "*.sh", "*.*" };
    }

    static ILaunchConfigurationWorkingCopy prepareDefault(final List<IResource> resources) throws CoreException {
        final Map<IResource, List<String>> suitesMapping = new HashMap<>();
        for (final IResource resource : resources) {
            suitesMapping.put(resource, new ArrayList<String>());
        }
        return prepareCopy(suitesMapping, RobotLaunchConfigurationType.GENERAL_PURPOSE);
    }

    static ILaunchConfigurationWorkingCopy prepareForSelectedTestCases(final Map<IResource, List<String>> suitesMapping)
            throws CoreException {
        return prepareCopy(suitesMapping, RobotLaunchConfigurationType.SELECTED_TEST_CASES);
    }

    private static ILaunchConfigurationWorkingCopy prepareCopy(final Map<IResource, List<String>> suitesMapping,
            final RobotLaunchConfigurationType type) throws CoreException {
        final ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
        final String namePrefix = RobotLaunchConfigurationNaming.getBasicName(suitesMapping.keySet(), type);
        final String name = launchManager.generateLaunchConfigurationName(namePrefix);

        final ILaunchConfigurationWorkingCopy configuration = launchManager.getLaunchConfigurationType(TYPE_ID)
                .newInstance(null, name);
        fillDefaults(configuration, suitesMapping, type);
        return configuration;
    }

    private static void fillDefaults(final ILaunchConfigurationWorkingCopy launchConfig,
            final Map<IResource, List<String>> suitesMapping, final RobotLaunchConfigurationType type)
            throws CoreException {
        final RobotLaunchConfiguration robotConfig = new RobotLaunchConfiguration(launchConfig);
        robotConfig.fillDefaults();
        final IProject project = getFirst(suitesMapping.keySet(), null).getProject();
        final RobotProject robotProject = RedPlugin.getModelManager().getModel().createRobotProject(project);
        if (robotProject.getRuntimeEnvironment() != null) {
            final SuiteExecutor interpreter = robotProject.getRuntimeEnvironment().getInterpreter();
            robotConfig.setInterpreter(interpreter);
        }
        robotConfig.setProjectName(project.getName());
        robotConfig.updateTestCases(suitesMapping);
        robotConfig.setIsGeneralPurposeEnabled(type == RobotLaunchConfigurationType.GENERAL_PURPOSE);

        robotConfig.setEnvironmentVariables(ImmutableMap.of("PYTHONIOENCODING", "utf8"));
    }

    public static void fillForFailedTestsRerun(final ILaunchConfigurationWorkingCopy launchConfig,
            final String outputFilePath) throws CoreException {
        final RobotLaunchConfiguration robotConfig = new RobotLaunchConfiguration(launchConfig);
        robotConfig.setRobotArguments("-R " + outputFilePath);
        robotConfig.setSuitePaths(new HashMap<String, List<String>>());
    }

    public RobotLaunchConfiguration(final ILaunchConfiguration config) {
        super(config);
    }

    @Override
    public List<IResource> getResourcesUnderDebug() throws CoreException {
        final List<IResource> suiteResources = getSuiteResources();
        return suiteResources.isEmpty() ? newArrayList(getProject()) : suiteResources;
    }

    @Override
    public void fillDefaults() throws CoreException {
        final RedPreferences preferences = RedPlugin.getDefault().getPreferences();
        setUsingInterpreterFromProject(true);
        setInterpreter(SuiteExecutor.Python);
        setInterpreterArguments(preferences.getLaunchAdditionalInterpreterArguments());
        setExecutableFilePath(preferences.getLaunchExecutableFilePath());
        setExecutableFileArguments(preferences.getLaunchAdditionalExecutableFileArguments());
        setRobotArguments(preferences.getLaunchAdditionalRobotArguments());
        setSuitePaths(new HashMap<String, List<String>>());
        setIsIncludeTagsEnabled(false);
        setIsExcludeTagsEnabled(false);
        setIncludedTags(new ArrayList<String>());
        setExcludedTags(new ArrayList<String>());
        setIsGeneralPurposeEnabled(true);
        super.fillDefaults();
    }

    @Override
    public void setCurrentConfigurationVersion() throws CoreException {
        final ILaunchConfigurationWorkingCopy launchCopy = asWorkingCopy();
        launchCopy.setAttribute(VERSION_OF_CONFIGURATION, CURRENT_CONFIGURATION_VERSION);
    }

    @Override
    public boolean hasValidVersion() throws CoreException {
        return CURRENT_CONFIGURATION_VERSION.equals(getConfigurationVersion());
    }

    @Override
    public String getCurrentConfigurationVersion() throws CoreException {
        return CURRENT_CONFIGURATION_VERSION;
    }

    public void setUsingInterpreterFromProject(final boolean usesProjectExecutor) throws CoreException {
        final ILaunchConfigurationWorkingCopy launchCopy = asWorkingCopy();
        launchCopy.setAttribute(USE_PROJECT_INTERPRETER_ATTRIBUTE, usesProjectExecutor);
    }

    public void setInterpreter(final SuiteExecutor executor) throws CoreException {
        final ILaunchConfigurationWorkingCopy launchCopy = asWorkingCopy();
        launchCopy.setAttribute(INTERPRETER_NAME_ATTRIBUTE, executor == null ? "" : executor.name());
    }

    public void setRobotArguments(final String arguments) throws CoreException {
        final ILaunchConfigurationWorkingCopy launchCopy = asWorkingCopy();
        launchCopy.setAttribute(ROBOT_ARGUMENTS_ATTRIBUTE, arguments);
    }

    public void setInterpreterArguments(final String arguments) throws CoreException {
        final ILaunchConfigurationWorkingCopy launchCopy = asWorkingCopy();
        launchCopy.setAttribute(INTERPRETER_ARGUMENTS_ATTRIBUTE, arguments);
    }

    public boolean isUsingInterpreterFromProject() throws CoreException {
        return configuration.getAttribute(USE_PROJECT_INTERPRETER_ATTRIBUTE, true);
    }

    public SuiteExecutor getInterpreter() throws CoreException {
        try {
            return SuiteExecutor
                    .fromName(configuration.getAttribute(INTERPRETER_NAME_ATTRIBUTE, SuiteExecutor.Python.name()));
        } catch (final IllegalArgumentException e) {
            return null;
        }
    }

    public String getRobotArguments() throws CoreException {
        return configuration.getAttribute(ROBOT_ARGUMENTS_ATTRIBUTE, "");
    }

    public String getInterpreterArguments() throws CoreException {
        return configuration.getAttribute(INTERPRETER_ARGUMENTS_ATTRIBUTE, "");
    }

    public void setIsIncludeTagsEnabled(final boolean isIncludeTagsEnabled) throws CoreException {
        final ILaunchConfigurationWorkingCopy launchCopy = asWorkingCopy();
        launchCopy.setAttribute(INCLUDE_TAGS_OPTION_ENABLED_ATTRIBUTE, isIncludeTagsEnabled);
    }

    public void setIncludedTags(final List<String> tags) throws CoreException {
        final ILaunchConfigurationWorkingCopy launchCopy = asWorkingCopy();
        launchCopy.setAttribute(INCLUDED_TAGS_ATTRIBUTE, tags);
    }

    public void setIsExcludeTagsEnabled(final boolean isExcludeTagsEnabled) throws CoreException {
        final ILaunchConfigurationWorkingCopy launchCopy = asWorkingCopy();
        launchCopy.setAttribute(EXCLUDE_TAGS_OPTION_ENABLED_ATTRIBUTE, isExcludeTagsEnabled);
    }

    public void setExcludedTags(final List<String> tags) throws CoreException {
        final ILaunchConfigurationWorkingCopy launchCopy = asWorkingCopy();
        launchCopy.setAttribute(EXCLUDED_TAGS_ATTRIBUTE, tags);
    }

    public boolean isIncludeTagsEnabled() throws CoreException {
        return configuration.getAttribute(INCLUDE_TAGS_OPTION_ENABLED_ATTRIBUTE, false);
    }

    public List<String> getIncludedTags() throws CoreException {
        return configuration.getAttribute(INCLUDED_TAGS_ATTRIBUTE, new ArrayList<String>());
    }

    public boolean isExcludeTagsEnabled() throws CoreException {
        return configuration.getAttribute(EXCLUDE_TAGS_OPTION_ENABLED_ATTRIBUTE, false);
    }

    public List<String> getExcludedTags() throws CoreException {
        return configuration.getAttribute(EXCLUDED_TAGS_ATTRIBUTE, new ArrayList<String>());
    }

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
                return String.join("::", testSuites);
            }
        });
        launchCopy.setAttribute(TEST_SUITES_ATTRIBUTE, suites);
    }

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

    public Map<IResource, List<String>> collectSuitesToRun() throws CoreException {
        final Map<IResource, List<String>> suitesToRun = new HashMap<>();
        final String projectName = getProjectName();
        if (!projectName.isEmpty()) {
            final IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
            if (project.exists()) {
                final Map<String, List<String>> suitePaths = getSuitePaths();
                for (final Entry<String, List<String>> entry : suitePaths.entrySet()) {
                    final IPath path = Path.fromPortableString(entry.getKey());
                    final IResource resource = path.getFileExtension() == null ? project.getFolder(path)
                            : project.getFile(path);
                    suitesToRun.put(resource, entry.getValue());
                }
            }
        }
        return suitesToRun;
    }

    public List<String> getSuitesToRun() throws CoreException {
        final List<String> suiteNames = new ArrayList<>();
        for (final IResource suite : getSuiteResources()) {
            suiteNames.add(RobotPathsNaming.createSuiteName(suite));
        }
        return suiteNames;
    }

    List<IResource> getSuiteResources() throws CoreException {
        final List<IResource> resources = new ArrayList<>();
        final Set<String> problems = new HashSet<>();
        for (final String suitePath : getSuitePaths().keySet()) {
            final IProject project = getProject();
            final IResource resource = project.findMember(Path.fromPortableString(suitePath));
            if (resource != null) {
                resources.add(resource);
            } else {
                problems.add("Suite '" + suitePath + "' does not exist in project '" + project.getName() + "'");
            }
        }
        if (!problems.isEmpty()) {
            throw newCoreException(String.join("\n", problems));
        }
        return resources;
    }

    public List<String> getTestsToRun() throws CoreException {
        final List<String> tests = new ArrayList<>();
        for (final Entry<String, List<String>> entry : getSuitePaths().entrySet()) {
            final IProject project = getProject();
            final IPath path = Path.fromPortableString(entry.getKey());
            for (final String testName : entry.getValue()) {
                tests.add(RobotPathsNaming.createTestName(project, path, testName));
            }
        }
        return tests;
    }

    public void setIsGeneralPurposeEnabled(final boolean isGeneralPurposeEnabled) throws CoreException {
        final ILaunchConfigurationWorkingCopy launchCopy = asWorkingCopy();
        launchCopy.setAttribute(GENERAL_PURPOSE_OPTION_ENABLED_ATTRIBUTE, isGeneralPurposeEnabled);
    }

    public boolean isGeneralPurposeConfiguration() throws CoreException {
        return configuration.getAttribute(GENERAL_PURPOSE_OPTION_ENABLED_ATTRIBUTE, false);
    }

    public String getExecutableFilePath() throws CoreException {
        return configuration.getAttribute(EXECUTABLE_FILE_PATH_ATTRIBUTE, "");
    }

    public String getExecutableFileArguments() throws CoreException {
        return configuration.getAttribute(EXECUTABLE_FILE_ARGUMENTS_ATTRIBUTE, "");
    }

    public void setExecutableFilePath(final String path) throws CoreException {
        final ILaunchConfigurationWorkingCopy launchCopy = asWorkingCopy();
        launchCopy.setAttribute(EXECUTABLE_FILE_PATH_ATTRIBUTE, path);
    }

    public void setExecutableFileArguments(final String arguments) throws CoreException {
        final ILaunchConfigurationWorkingCopy launchCopy = asWorkingCopy();
        launchCopy.setAttribute(EXECUTABLE_FILE_ARGUMENTS_ATTRIBUTE, arguments);
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

    public void setEnvironmentVariables(final Map<String, String> mapping) throws CoreException {
        final ILaunchConfigurationWorkingCopy launchCopy = asWorkingCopy();
        launchCopy.setAttribute(ILaunchManager.ATTR_ENVIRONMENT_VARIABLES, mapping);
    }

}
