/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.project.ASuiteFileDescriber;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig.ExcludedFolderPath;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectNature;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.RobotFileValidator;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.RobotInitFileValidator;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.RobotProjectConfigFileValidator;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.RobotResourceFileValidator;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.RobotSuiteFileValidator;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.ValidationContext;

import com.google.common.base.Optional;
import com.google.common.collect.Queues;

public class RobotArtifactsValidator {

    private final BuildLogger logger;
    private final IProject project;

    public RobotArtifactsValidator(final IProject project, final BuildLogger logger) {
        this.project = project;
        this.logger = logger;
    }

    public static void revalidate(final RobotSuiteFile suiteModel) {
        if (suiteModel.getProject().getRobotProjectHolder().getRobotRuntime().getVersion() == null) {
            return;
        }
        final IFile file = suiteModel.getFile();
        if (file == null || !file.exists() || !RobotProjectNature.hasRobotNature(file.getProject())) {
            return;
        }

        final ValidationContext context = new ValidationContext(file.getProject(), new BuildLogger());

        try {
            final Optional<? extends ModelUnitValidator> validator = createValidationUnits(context, file,
                    ProblemsReportingStrategy.reportOnly(), true, false);
            if (validator.isPresent()) {
                final WorkspaceJob wsJob = new WorkspaceJob("Revalidating model") {

                    @Override
                    public IStatus runInWorkspace(final IProgressMonitor monitor) throws CoreException {
                        file.deleteMarkers(RobotProblem.TYPE_ID, true, 1);
                        ((RobotFileValidator) validator.get()).validate(suiteModel, new NullProgressMonitor());

                        return Status.OK_STATUS;
                    }
                };
                wsJob.setSystem(true);
                wsJob.schedule();
            }
        } catch (final CoreException e) {
            // so we won't revalidate
        }
    }

    public Job createValidationJob(final Job dependentJob, final IResourceDelta delta, final int kind,
            final ProblemsReportingStrategy reporter) {
        return new Job("Validating") {

            @Override
            protected IStatus run(final IProgressMonitor monitor) {
                try {
                    dependentJob.join();
                    if (!dependentJob.getResult().isOK()) {
                        return Status.CANCEL_STATUS;
                    }
                } catch (final InterruptedException e) {
                    RedPlugin.logError("Project validation was corrupted", e);
                    return Status.CANCEL_STATUS;
                }
                try {
                    logger.log("VALIDATING: validation of '" + project.getName() + "' project started");
                    logger.log("VALIDATING: gathering files to be validated");

                    final Queue<ModelUnitValidator> unitValidators = Queues.newArrayDeque();
                    final ValidationContext context = new ValidationContext(project, logger);

                    if (delta == null || kind == IncrementalProjectBuilder.FULL_BUILD) {
                        unitValidators.addAll(createValidationUnitsForWholeProject(context, reporter));
                        project.deleteMarkers(RobotProblem.TYPE_ID, true, IResource.DEPTH_INFINITE);
                    } else if (delta != null) {
                        unitValidators.addAll(createValidationUnitsForChangedFiles(context, delta, reporter));
                    }

                    final SubMonitor subMonitor = SubMonitor.convert(monitor, 100);
                    subMonitor.beginTask("Validating files", 100);

                    final SubMonitor validationSubMonitor = subMonitor.newChild(100);
                    validationSubMonitor.setWorkRemaining(unitValidators.size());

                    final int threadPoolSize = Runtime.getRuntime().availableProcessors();
                    logger.log("VALIDATING: " + threadPoolSize + " threads will be used");
                    final ExecutorService threadPool = Executors.newFixedThreadPool(threadPoolSize);

                    int current = 1;
                    final int total = unitValidators.size();
                    while (!unitValidators.isEmpty()) {
                        final ModelUnitValidator validator = unitValidators.poll();
                        threadPool.submit(
                                createValidationRunnable(monitor, validationSubMonitor, current, total, validator));
                        current++;
                    }
                    threadPool.shutdown();
                    threadPool.awaitTermination(1, TimeUnit.HOURS);

                    return Status.OK_STATUS;
                } catch (final CoreException | InterruptedException e) {
                    RedPlugin.logError("Project validation was corrupted", e);
                    return Status.CANCEL_STATUS;
                } finally {
                    monitor.done();
                }
            }

            private Runnable createValidationRunnable(final IProgressMonitor monitor,
                    final SubMonitor validationSubMonitor, final int id, final int total,
                    final ModelUnitValidator validator) {
                return new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (monitor.isCanceled()) {
                                logger.log("VALIDATING: cancelled (" + id + "/" + total + ")");
                                return;
                            }
                            validator.validate(monitor);
                            logger.log("VALIDATING: done (" + id + "/" + total + ")");
                        } catch (final Exception e) {
                            logger.log("VALIDATING: error (" + id + "/" + total + ")");
                            logger.logError("VALIDATING: error\n" + e.getMessage());
                        } finally {
                            validationSubMonitor.worked(1);
                        }
                    }
                };
            }
        };
    }

    private List<ModelUnitValidator> createValidationUnitsForWholeProject(final ValidationContext context,
            final ProblemsReportingStrategy reporter)
            throws CoreException {
        final List<ModelUnitValidator> validators = newArrayList();
        project.accept(new IResourceVisitor() {

            @Override
            public boolean visit(final IResource resource) throws CoreException {
                final Optional<? extends ModelUnitValidator> validationUnit = createValidationUnits(context, resource,
                        reporter, false, true);
                if (validationUnit.isPresent()) {
                    final ModelUnitValidator unit = validationUnit.get();
                    validators.add(unit);
                }
                return true;
            }
        });
        return validators;
    }

    private List<ModelUnitValidator> createValidationUnitsForChangedFiles(final ValidationContext context,
            final IResourceDelta delta, final ProblemsReportingStrategy reporter) throws CoreException {
        final List<ModelUnitValidator> validators = newArrayList();
        delta.accept(new IResourceDeltaVisitor() {

            @Override
            public boolean visit(final IResourceDelta delta) throws CoreException {
                if (delta.getKind() != IResourceDelta.REMOVED && (delta.getFlags() & IResourceDelta.CONTENT) != 0) {
                    final IResource file = delta.getResource();
                    final Optional<? extends ModelUnitValidator> validationUnit = createValidationUnits(context, file,
                            reporter, false, false);
                    if (validationUnit.isPresent()) {
                        validators.add(new ModelUnitValidator() {

                            @Override
                            public void validate(final IProgressMonitor monitor) throws CoreException {
                                file.deleteMarkers(RobotProblem.TYPE_ID, true, 1);
                                validationUnit.get().validate(monitor);
                            }
                        });
                    }
                }
                return true;
            }
        });
        return validators;
    }

    private static Optional<? extends ModelUnitValidator> createValidationUnits(final ValidationContext context,
            final IResource resource, final ProblemsReportingStrategy reporter, final boolean isRevalidating,
            final boolean isValidatingWholeProject) throws CoreException {
        context.setIsValidatingChangedFiles(!isRevalidating && !isValidatingWholeProject);
        return shouldValidate(context.getProjectConfiguration(), resource, isRevalidating) ? createProperValidator(
                context, (IFile) resource, reporter) : Optional.<ModelUnitValidator> absent();
    }

    private static boolean shouldValidate(final RobotProjectConfig robotProjectConfig, final IResource resource,
            final boolean isRevalidating) {
        if (resource.getType() == IResource.FILE && !isInsideEclipseHiddenDirectory(resource)
                && hasRequiredFileSize(robotProjectConfig, resource, isRevalidating)) {
            final List<ExcludedFolderPath> excludedPaths = robotProjectConfig.getExcludedPath();
            for (final ExcludedFolderPath excludedPath : excludedPaths) {

                if (excludedPath.asPath().isPrefixOf(resource.getProjectRelativePath())) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    private static boolean isInsideEclipseHiddenDirectory(final IResource resource) {
        for (final String segment : resource.getFullPath().segments()) {
            if (!segment.isEmpty() && segment.charAt(0) == '.') {
                return true;
            }
        }
        return false;
    }
    
    private static boolean hasRequiredFileSize(final RobotProjectConfig robotProjectConfig, final IResource resource,
            final boolean isRevalidating) {
        if (!isRevalidating && robotProjectConfig.isValidatedFileSizeCheckingEnabled()) {
            final IPath fileLocation = resource.getLocation();
            if (fileLocation != null) {
                final long fileSizeInKilobytes = fileLocation.toFile().length() / 1024;
                long maxFileSize = 0L;
                try {
                    maxFileSize = Long.parseLong(robotProjectConfig.getValidatedFileMaxSize());
                } catch (final NumberFormatException e) {
                    maxFileSize = Long.parseLong(robotProjectConfig.getValidatedFileDefaultMaxSize());
                }
                if (fileSizeInKilobytes > maxFileSize) {
                    return false;
                }
            }
        }
        return true;
    }

    public static Optional<? extends ModelUnitValidator> createProperValidator(final ValidationContext context,
            final IFile file, final ProblemsReportingStrategy reporter) {

        if (ASuiteFileDescriber.isSuiteFile(file)) {
            return Optional.of(new RobotSuiteFileValidator(context, file, reporter));
        } else if (ASuiteFileDescriber.isResourceFile(file)) {
            return Optional.of(new RobotResourceFileValidator(context, file, reporter));
        } else if (ASuiteFileDescriber.isInitializationFile(file)) {
            return Optional.of(new RobotInitFileValidator(context, file, reporter));
        } else if (file.getName().equals("red.xml") && file.getParent() == file.getProject()) {
            return Optional.of(new RobotProjectConfigFileValidator(context, file, reporter));
        }
        return Optional.absent();
    }

    public interface ModelUnitValidator {

        void validate(IProgressMonitor monitor) throws CoreException;
    }
}
