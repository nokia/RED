/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor;

import static com.google.common.collect.Lists.newArrayList;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IExecutionListener;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.rf.ide.core.project.RobotProjectConfig;
import org.rf.ide.core.testdata.model.table.setting.LibraryImport;
import org.rf.ide.core.validation.ProblemPosition;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.project.ExcludedResources;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectNature;
import org.robotframework.ide.eclipse.main.plugin.project.build.BuildLogger;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotTask;
import org.robotframework.ide.eclipse.main.plugin.project.build.ValidationReportingStrategy;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.GeneralSettingsProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.FileValidationContext;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.GeneralSettingsLibrariesImportValidator;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.ValidationContext;
import org.robotframework.ide.eclipse.main.plugin.project.dryrun.CombinedLibrariesAutoDiscoverer;
import org.robotframework.ide.eclipse.main.plugin.project.dryrun.LibrariesAutoDiscoverer;
import org.robotframework.ide.eclipse.main.plugin.project.dryrun.LibrariesAutoDiscoverer.DiscovererFactory;
import org.robotframework.ide.eclipse.main.plugin.project.dryrun.LibrariesAutoDiscovererWindow;

class OnSaveLibrariesAutodiscoveryTrigger implements IExecutionListener {

    static final String SAVE_ALL_COMMAND_ID = "org.eclipse.ui.file.saveAll";

    private static OnSaveLibrariesAutodiscoveryTrigger globalBatchSaveResponsibleTrigger = null;

    private static final List<RobotSuiteFile> SUITES_FOR_DISCOVER = new ArrayList<>();

    private final DiscovererFactory discovererFactory;

    OnSaveLibrariesAutodiscoveryTrigger() {
        this((robotProject, suites) -> {
            final boolean showSummary = robotProject.getRobotProjectConfig()
                    .isLibrariesAutoDiscoveringSummaryWindowEnabled();
            return new CombinedLibrariesAutoDiscoverer(robotProject, suites,
                    showSummary ? LibrariesAutoDiscovererWindow.openSummary() : libraryImports -> {});
        });
    }

    OnSaveLibrariesAutodiscoveryTrigger(final DiscovererFactory discovererFactory) {
        this.discovererFactory = discovererFactory;
    }

    @Override
    public void preExecute(final String commandId, final ExecutionEvent event) {
        if (globalBatchSaveResponsibleTrigger == null && SAVE_ALL_COMMAND_ID.equals(commandId)) {
            globalBatchSaveResponsibleTrigger = this;
        }
    }

    @Override
    public void notHandled(final String commandId, final NotHandledException exception) {
        if (SAVE_ALL_COMMAND_ID.equals(commandId)) {
            globalBatchSaveResponsibleTrigger = null;
            SUITES_FOR_DISCOVER.clear();
        }
    }

    @Override
    public void postExecuteFailure(final String commandId, final ExecutionException exception) {
        if (SAVE_ALL_COMMAND_ID.equals(commandId)) {
            globalBatchSaveResponsibleTrigger = null;
            SUITES_FOR_DISCOVER.clear();
        }
    }

    @Override
    public void postExecuteSuccess(final String commandId, final Object returnValue) {
        if (globalBatchSaveResponsibleTrigger == this && SAVE_ALL_COMMAND_ID.equals(commandId)) {
            globalBatchSaveResponsibleTrigger = null;

            if (!SUITES_FOR_DISCOVER.isEmpty()) {
                LibrariesAutoDiscoverer.start(SUITES_FOR_DISCOVER, discovererFactory);
            }
            SUITES_FOR_DISCOVER.clear();
        }
    }

    void startLibrariesAutoDiscoveryIfRequired(final RobotSuiteFile suite) {
        if (shouldStartAutoDiscovering(suite)) {

            if (globalBatchSaveResponsibleTrigger == null) {
                LibrariesAutoDiscoverer.start(newArrayList(suite), discovererFactory);
            } else {
                SUITES_FOR_DISCOVER.add(suite);
            }
        }
    }

    private boolean shouldStartAutoDiscovering(final RobotSuiteFile suite) {
        final RobotProjectConfig projectConfig = suite.getProject().getRobotProjectConfig();
        return RobotProjectNature.hasRobotNature(suite.getProject().getProject())
                && projectConfig.isReferencedLibrariesAutoDiscoveringEnabled()
                && !ExcludedResources.isHiddenInEclipse(suite.getFile())
                && !ExcludedResources.isInsideExcludedPath(suite.getFile(), projectConfig)
                && suiteHasUnknownLibraryIncludingNestedResources(suite);
    }

    private boolean suiteHasUnknownLibraryIncludingNestedResources(final RobotSuiteFile suite) {
        final Map<RobotSuiteFile, List<LibraryImport>> imports = LibraryImportCollector
                .collectLibraryImportsIncludingNestedResources(suite);
        for (final Entry<RobotSuiteFile, List<LibraryImport>> importEntry : imports.entrySet()) {
            if (suiteHasUnknownLibrary(importEntry.getKey(), importEntry.getValue())) {
                return true;
            }
        }
        return false;
    }

    private boolean suiteHasUnknownLibrary(final RobotSuiteFile suite, final List<LibraryImport> imports) {
        final UnknownLibraryDetectingReportingStrategy reporter = new UnknownLibraryDetectingReportingStrategy();

        final ValidationContext generalContext = new ValidationContext(suite.getProject(), new BuildLogger());
        final FileValidationContext fileContext = new FileValidationContext(generalContext, suite.getFile());
        final GeneralSettingsLibrariesImportValidator importsValidator = new GeneralSettingsLibrariesImportValidator(
                fileContext, suite, imports, reporter);
        try {
            importsValidator.validate(new NullProgressMonitor());
            return reporter.unknownLibraryWasDetected();
        } catch (final CoreException e) {
            return false;
        }
    }

    private static class UnknownLibraryDetectingReportingStrategy extends ValidationReportingStrategy {

        private boolean detectedLibraryProblem = false;

        UnknownLibraryDetectingReportingStrategy() {
            super(false);
        }

        @Override
        public void handleTask(final RobotTask task, final IFile file) {
            // not interested in tasks reporting
        }

        @Override
        public void handleProblem(final RobotProblem problem, final IFile file, final ProblemPosition filePosition,
                final Map<String, Object> additionalAttributes) {
            if (problem.getCause() == GeneralSettingsProblem.NON_EXISTING_LIBRARY_IMPORT
                    || problem.getCause() == GeneralSettingsProblem.NON_EXISTING_REMOTE_LIBRARY_IMPORT) {
                this.detectedLibraryProblem = true;
            }
        }

        boolean unknownLibraryWasDetected() {
            return detectedLibraryProblem;
        }
    }
}
