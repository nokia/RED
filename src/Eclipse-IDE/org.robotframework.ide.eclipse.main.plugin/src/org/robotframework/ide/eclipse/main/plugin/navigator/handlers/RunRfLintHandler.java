package org.robotframework.ide.eclipse.main.plugin.navigator.handlers;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.stream.Collectors.toList;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.inject.Named;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.rf.ide.core.executor.RobotRuntimeEnvironment.RobotEnvironmentException;
import org.rf.ide.core.project.RobotProjectConfig.ExcludedFolderPath;
import org.rf.ide.core.rflint.RfLintClientEventsListener;
import org.rf.ide.core.rflint.RfLintIntegrationServer;
import org.rf.ide.core.rflint.RfLintRule;
import org.rf.ide.core.rflint.RfLintViolationSeverity;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.navigator.handlers.RunRfLintHandler.E4RunRfLintHandler;
import org.robotframework.red.commands.DIParameterizedHandler;
import org.robotframework.red.jface.dialogs.DetailedErrorDialog;
import org.robotframework.red.viewers.Selections;


public class RunRfLintHandler extends DIParameterizedHandler<E4RunRfLintHandler> {

    public RunRfLintHandler() {
        super(E4RunRfLintHandler.class);
    }

    public static class E4RunRfLintHandler {

        @Execute
        public void runRfLint(final @Named(Selections.SELECTION) IStructuredSelection selection) {
            final IResource selectedResource = Selections.getAdaptableElements(selection, IResource.class).get(0);

            RfLintProblem.cleanProblems(newArrayList(selectedResource));

            final RfLintIntegrationServer server = scheduleServerJob();
            try {
                server.waitForServerToSetup();
            } catch (final InterruptedException e) {
                showErrorDialog(e);
            }
            try {
                runRfLint(selectedResource, server);
            } catch (final RobotEnvironmentException e) {
                killServer(server);
                showErrorDialog(e);
            }
        }

        private RfLintIntegrationServer scheduleServerJob() {
            final RfLintIntegrationServer server = new RfLintIntegrationServer(e -> showErrorDialog(e));
            final Job job = new Job("Running RfLint analysis") {

                @Override
                protected IStatus run(final IProgressMonitor monitor) {
                    final RfLintListener listener = new RfLintListener(monitor, () -> killServer(server));
                    try {
                        server.start(listener);
                    } catch (final IOException e) {
                        return Status.CANCEL_STATUS;
                    }
                    return Status.OK_STATUS;
                }
            };
            job.setRule(new OrderingRule());
            job.setUser(true);
            job.schedule();
            return server;
        }

        static void killServer(final RfLintIntegrationServer server) {
            try {
                server.stop();
            } catch (final IOException e) {
                showErrorDialog(e);
            }
        }

        static void showErrorDialog(final Exception e) {
            DetailedErrorDialog.openErrorDialog("Error occurred when trying to run RfLint analysis", e.getMessage());
        }

        private void runRfLint(final IResource resource, final RfLintIntegrationServer server) {
            final RobotProject robotProject = RedPlugin.getModelManager().createProject(resource.getProject());
            final File projectLocation = robotProject.getProject().getLocation().toFile();
            final List<String> excludedPaths = robotProject.getRobotProjectConfig()
                    .getExcludedPath()
                    .stream()
                    .map(ExcludedFolderPath::getPath)
                    .collect(toList());
            final File filepath = resource.getLocation().toFile();
            final RedPreferences preferences = RedPlugin.getDefault().getPreferences();
            final List<RfLintRule> rules = preferences.getRfLintRules();
            final List<String> rulesFiles = preferences.getRfLintRulesFiles();

            robotProject.getRuntimeEnvironment().runRfLint(server.getHost(), server.getPort(), projectLocation,
                    excludedPaths, filepath, rules, rulesFiles);
        }
    }

    private static class OrderingRule implements ISchedulingRule {

        @Override
        public boolean contains(final ISchedulingRule rule) {
            return rule == this;
        }

        @Override
        public boolean isConflicting(final ISchedulingRule rule) {
            return rule instanceof OrderingRule;
        }
    }

    private static class RfLintListener implements RfLintClientEventsListener {

        private final IProgressMonitor monitor;
        private SubMonitor submonitor;

        private final Runnable serverDisabler;

        public RfLintListener(final IProgressMonitor monitor, final Runnable serverDisabler) {
            this.monitor = monitor;
            this.serverDisabler = serverDisabler;
        }

        @Override
        public void filesToProcess(final int numberOfFiles) {
            submonitor = SubMonitor.convert(this.monitor);
            submonitor.beginTask("Validating with RfLint", numberOfFiles);
        }

        @Override
        public void processingStarted(final File filepath) {
            if (submonitor.isCanceled()) {
                serverDisabler.run();
            }
            submonitor.subTask(filepath.getAbsolutePath());
        }

        @Override
        public void processingEnded(final File filepath) {
            if (submonitor.isCanceled()) {
                serverDisabler.run();
            }
            submonitor.worked(1);
        }

        @Override
        public void violationFound(final File filepath, final int line, final int character, final String ruleName,
                final RfLintViolationSeverity severity, final String message) {
            RfLintProblem.causedBy(ruleName, severity, message).createMarker(filepath, line);
        }

        @Override
        public void analysisFinished() {
            monitor.done();
        }

        @Override
        public void analysisFinished(final String errorMsg) {
            monitor.done();
            DetailedErrorDialog.openErrorDialog("Error occurred when trying to run RfLint analysis", errorMsg);
        }
    }
}
