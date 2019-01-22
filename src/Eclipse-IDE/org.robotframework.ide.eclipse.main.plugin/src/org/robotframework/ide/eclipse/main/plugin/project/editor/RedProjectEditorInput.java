/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.editor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.rf.ide.core.project.RobotProjectConfig;
import org.rf.ide.core.project.RobotProjectConfigReader.RobotProjectConfigWithLines;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.ProblemCategory.Severity;

public class RedProjectEditorInput {

    private final IFile file;

    private final boolean isEditable;

    private final RobotProjectConfigWithLines projectConfiguration;

    public RedProjectEditorInput(final boolean isEditable, final RobotProjectConfigWithLines projectConfiguration) {
        this(null, isEditable, projectConfiguration);
    }

    public RedProjectEditorInput(final IFile file, final boolean isEditable,
            final RobotProjectConfigWithLines projectConfiguration) {
        this.file = file;
        this.isEditable = isEditable;
        this.projectConfiguration = projectConfiguration;
    }

    public RobotProject getRobotProject() {
        if (file != null) {
            return RedPlugin.getModelManager().getModel().createRobotProject(file.getProject());
        } else {
            return null;
        }
    }

    RobotProjectConfigWithLines getProjectConfig() {
        return projectConfiguration;
    }

    public RobotProjectConfig getProjectConfiguration() {
        return projectConfiguration.getConfigurationModel();
    }

    public boolean isEditable() {
        return isEditable;
    }

    public List<RedXmlProblem> getProblemsFor(final Object modelPart) {
        if (file != null) {
            final int xmlLine = projectConfiguration.getLineFor(modelPart);
            if (xmlLine == -1) {
                return new ArrayList<>();
            }
            try {
                final IMarker[] markers = file.findMarkers(RobotProblem.TYPE_ID, true, 1);
                final List<RedXmlProblem> problems = new ArrayList<>();
                for (final IMarker marker : markers) {
                    if (marker.getAttribute(IMarker.LINE_NUMBER, -1) == xmlLine) {
                        final Severity severity = Severity
                                .fromMarkerSeverity(marker.getAttribute(IMarker.SEVERITY, -1));
                        problems.add(new RedXmlProblem(severity, marker.getAttribute(IMarker.MESSAGE, "")));
                    }
                }
                return problems;
            } catch (final CoreException e) {
                return new ArrayList<>();
            }
        }
        return new ArrayList<>();
    }

    public static class RedXmlProblem {

        public static boolean hasProblems(final Collection<RedXmlProblem> problems) {
            return !problems.isEmpty();
        }

        public static boolean hasWarnings(final Collection<RedXmlProblem> problems) {
            return hasProblemsOfSeverity(problems, Severity.WARNING);
        }

        public static boolean hasErrors(final Collection<RedXmlProblem> problems) {
            return hasProblemsOfSeverity(problems, Severity.ERROR);
        }

        private static boolean hasProblemsOfSeverity(final Collection<RedXmlProblem> problems,
                final Severity severity) {
            return problems.stream().anyMatch(problem -> problem.getSeverity() == severity);
        }

        private final Severity severity;

        private final String description;

        public RedXmlProblem(final Severity severity, final String description) {
            this.severity = severity;
            this.description = description;
        }

        public Severity getSeverity() {
            return severity;
        }

        public String getDescription() {
            return description;
        }
    }
}
