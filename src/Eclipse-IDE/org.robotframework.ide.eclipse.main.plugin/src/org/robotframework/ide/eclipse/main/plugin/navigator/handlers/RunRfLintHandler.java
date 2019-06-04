/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.navigator.handlers;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.stream.Collectors.toList;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import javax.inject.Named;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;
import org.rf.ide.core.environment.IRuntimeEnvironment;
import org.rf.ide.core.environment.IRuntimeEnvironment.RuntimeEnvironmentException;
import org.rf.ide.core.project.RobotProjectConfig.ExcludedPath;
import org.rf.ide.core.rflint.RfLintClientEventsListener;
import org.rf.ide.core.rflint.RfLintIntegrationServer;
import org.rf.ide.core.rflint.RfLintRule;
import org.rf.ide.core.rflint.RfLintRuleConfiguration;
import org.rf.ide.core.rflint.RfLintRules;
import org.rf.ide.core.rflint.RfLintViolationSeverity;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences;
import org.robotframework.ide.eclipse.main.plugin.launch.variables.RedStringVariablesManager;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.navigator.handlers.RunRfLintHandler.E4RunRfLintHandler;
import org.robotframework.red.commands.DIParameterizedHandler;
import org.robotframework.red.jface.dialogs.DetailedErrorDialog;
import org.robotframework.red.viewers.Selections;

import com.google.common.annotations.VisibleForTesting;

public class RunRfLintHandler extends DIParameterizedHandler<E4RunRfLintHandler> {

    public RunRfLintHandler() {
        super(E4RunRfLintHandler.class);
    }

    public static class E4RunRfLintHandler {

        @Execute
        public void runRfLint(final @Named(Selections.SELECTION) IStructuredSelection selection) {
            final IResource selectedResource = Selections.getAdaptableElements(selection, IResource.class).get(0);

            final RobotProject robotProject = RedPlugin.getModelManager().createProject(selectedResource.getProject());
            final IRuntimeEnvironment env = chooseEnvironment(robotProject);
            if (env == null) {
                return;
            }
            
            RfLintProblem.cleanProblems(newArrayList(selectedResource));
            final RfLintIntegrationServer server = scheduleServerJob();
            try {
                server.waitForServerToSetup();
            } catch (final InterruptedException e) {
                showErrorDialog(e);
            }
            try {
                runRfLint(env, selectedResource, server);
            } catch (final RuntimeEnvironmentException e) {
                killServer(server);
                showErrorDialog(e);
            }
        }

        private static IRuntimeEnvironment chooseEnvironment(final RobotProject robotProject) {
            final AtomicReference<IRuntimeEnvironment> envRef = new AtomicReference<>(null);

            BusyIndicator.showWhile(Display.getCurrent(), () -> {
                final IRuntimeEnvironment globalEnv = RedPlugin.getDefault().getActiveRobotInstallation();
                final IRuntimeEnvironment projectEnv = robotProject.getRuntimeEnvironment();

                if (!globalEnv.equals(projectEnv)) {
                    final String globalEnvPath = Optional.of(globalEnv)
                            .map(IRuntimeEnvironment::getFile)
                            .map(File::getAbsolutePath)
                            .orElse("<unknown>");
                    final String projectEnvPath = Optional.of(projectEnv)
                            .map(IRuntimeEnvironment::getFile)
                            .map(File::getAbsolutePath)
                            .orElse("<unknown>");

                    final MessageDialog dialog = new MessageDialog(Display.getCurrent().getActiveShell(),
                            "Different environment detected", null,
                            String.format("Your RfLint preferences are configured for '%s' environment while the '%s' "
                                    + "project is using '%s' environment. Do you want to use global environment "
                                    + "(recommended) or project one?", globalEnvPath, robotProject.getName(),
                                    projectEnvPath),
                            MessageDialog.QUESTION, new String[] { "Use global", "Use project" }, 0);

                    final int result = dialog.open();
                    if (result == 0) {
                        envRef.set(globalEnv);
                    } else if (result == 1) {
                        envRef.set(projectEnv);
                    }
                } else {
                    envRef.set(globalEnv);
                }
            });
            return envRef.get();
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

        private static void killServer(final RfLintIntegrationServer server) {
            try {
                server.stop();
            } catch (final IOException e) {
                showErrorDialog(e);
            }
        }

        private static void showErrorDialog(final Exception e) {
            DetailedErrorDialog.openErrorDialog("Error occurred when trying to run RfLint analysis", e.getMessage());
        }

        private static void runRfLint(final IRuntimeEnvironment env, final IResource resource,
                final RfLintIntegrationServer server) {
            final RobotProject robotProject = RedPlugin.getModelManager().createProject(resource.getProject());
            final RedPreferences preferences = RedPlugin.getDefault().getPreferences();

            final Map<String, RfLintRule> rules = getRules(preferences, env);
            runRfLint(env, robotProject, resource, server, preferences, rules);
        }

        private static Map<String, RfLintRule> getRules(final RedPreferences preferences,
                final IRuntimeEnvironment env) {
            final Map<String, RfLintRule> rules = new HashMap<>();
            BusyIndicator.showWhile(Display.getCurrent(), () -> {
                final List<String> rulesFiles = preferences.getRfLintRulesFiles();
                final Map<String, RfLintRule> origRules = RfLintRules.getInstance()
                        .loadRules(() -> env.getRfLintRules(rulesFiles));
                rules.putAll(origRules);
            });
            return rules;
        }

        @VisibleForTesting
        static void runRfLint(final IRuntimeEnvironment env, final RobotProject robotProject, final IResource resource,
                final RfLintIntegrationServer server, final RedPreferences preferences,
                final Map<String, RfLintRule> rules) {
            final File projectLocation = robotProject.getProject().getLocation().toFile();
            final List<String> excludedPaths = robotProject.getRobotProjectConfig()
                    .getExcludedPaths()
                    .stream()
                    .map(ExcludedPath::getPath)
                    .collect(toList());
            final File filepath = resource.getLocation().toFile();
            final List<String> rulesFiles = preferences.getRfLintRulesFiles();
            final Map<String, RfLintRuleConfiguration> ruleConfigs = preferences.getRfLintRulesConfigs();
            rules.forEach((name, rule) -> rule.configure(ruleConfigs.get(name)));

            final List<String> additionalArguments = parseArguments(preferences.getRfLintAdditionalArguments());
            env.runRfLint(server.getHost(), server.getPort(), projectLocation, excludedPaths, filepath,
                    new ArrayList<>(rules.values()), rulesFiles, additionalArguments);
        }

        private static List<String> parseArguments(final String arguments) {
            final RedStringVariablesManager variableManager = new RedStringVariablesManager();
            return Stream.of(DebugPlugin.parseArguments(arguments)).map(argument -> {
                try {
                    return variableManager.substituteUsingQuickValuesSet(argument);
                } catch (final CoreException e) {
                    return argument;
                }
            }).collect(toList());
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
