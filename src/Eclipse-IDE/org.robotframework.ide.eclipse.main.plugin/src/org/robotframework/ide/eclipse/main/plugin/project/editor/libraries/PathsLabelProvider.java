/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.editor.libraries;

import static java.util.stream.Collectors.joining;

import java.io.File;
import java.util.Collection;
import java.util.List;

import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.jface.viewers.Stylers;
import org.eclipse.swt.graphics.Image;
import org.rf.ide.core.project.RobotProjectConfig.SearchPath;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.project.RedEclipseProjectConfig;
import org.robotframework.ide.eclipse.main.plugin.project.editor.RedProjectEditorInput;
import org.robotframework.ide.eclipse.main.plugin.project.editor.RedProjectEditorInput.RedXmlProblem;
import org.robotframework.red.graphics.ImagesManager;
import org.robotframework.red.viewers.ElementAddingToken;
import org.robotframework.red.viewers.RedCommonLabelProvider;

/**
 * @author Michal Anglart
 *
 */
class PathsLabelProvider extends RedCommonLabelProvider {

    private final String pathVariableName;

    private final RedProjectEditorInput editorInput;

    public PathsLabelProvider(final String pathVariableName, final RedProjectEditorInput editorInput) {
        this.pathVariableName = pathVariableName;
        this.editorInput = editorInput;
    }

    @Override
    public StyledString getStyledText(final Object element) {
        if (element instanceof SearchPath) {
            final SearchPath searchPath = (SearchPath) element;
            if (searchPath.isSystem()) {
                final StyledString label = new StyledString(searchPath.getLocation(),
                        Stylers.withForeground(150, 150, 150));
                label.append(" [already defined in " + pathVariableName + " variable]",
                        Stylers.Common.ECLIPSE_DECORATION_STYLER);
                return label;
            } else {
                Styler styler;
                final Collection<RedXmlProblem> problems = editorInput.getProblemsFor(searchPath);
                if (RedXmlProblem.hasErrors(problems)) {
                    styler = Stylers.Common.ERROR_STYLER;
                } else if (RedXmlProblem.hasProblems(problems)) {
                    styler = Stylers.Common.WARNING_STYLER;
                } else {
                    styler = Stylers.Common.EMPTY_STYLER;
                }
                return new StyledString(searchPath.getLocation(), styler);
            }
        } else {
            return ((ElementAddingToken) element).getStyledText();
        }
    }

    @Override
    public Image getImage(final Object element) {
        if (element instanceof ElementAddingToken) {
            return ((ElementAddingToken) element).getImage();
        }
        return null;
    }

    @Override
    public String getToolTipText(final Object element) {
        if (element instanceof SearchPath) {
            final SearchPath path = (SearchPath) element;
            final List<RedXmlProblem> problems = editorInput.getProblemsFor(element);
            if (problems.isEmpty()) {
                final RedEclipseProjectConfig redConfig = new RedEclipseProjectConfig(
                        editorInput.getRobotProject().getProject(), editorInput.getProjectConfiguration());
                return redConfig.toAbsolutePath(path)
                        .map(File::getPath)
                        .map(tooltipPath -> path.isSystem()
                                ? tooltipPath + " [already defined in " + pathVariableName + " variable]"
                                : tooltipPath)
                        .orElse(null);
            } else {
                return problems.stream().map(RedXmlProblem::getDescription).collect(joining("\n"));
            }
        }
        return null;
    }

    @Override
    public Image getToolTipImage(final Object element) {
        final List<RedXmlProblem> problems = editorInput.getProblemsFor(element);

        if (RedXmlProblem.hasErrors(problems)) {
            return ImagesManager.getImage(RedImages.getErrorImage());
        } else if (RedXmlProblem.hasProblems(problems)) {
            return ImagesManager.getImage(RedImages.getWarningImage());
        }
        return null;
    }
}
