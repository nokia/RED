/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.editor.libraries;

import static java.util.stream.Collectors.joining;

import java.util.List;

import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.jface.viewers.Stylers;
import org.eclipse.swt.graphics.Image;
import org.rf.ide.core.project.RobotProjectConfig.ReferencedLibrary;
import org.rf.ide.core.project.RobotProjectConfig.RemoteLocation;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.project.editor.RedProjectEditorInput;
import org.robotframework.ide.eclipse.main.plugin.project.editor.RedProjectEditorInput.RedXmlProblem;
import org.robotframework.red.graphics.ImagesManager;
import org.robotframework.red.viewers.RedCommonLabelProvider;

class ReferencedLibrariesLabelProvider extends RedCommonLabelProvider {

    private final RedProjectEditorInput editorInput;

    public ReferencedLibrariesLabelProvider(final RedProjectEditorInput editorInput) {
        this.editorInput = editorInput;
    }

    @Override
    public StyledString getStyledText(final Object element) {
        if (element instanceof ReferencedLibrary) {
            final ReferencedLibrary lib = (ReferencedLibrary) element;

            final List<RedXmlProblem> problems = editorInput.getProblemsFor(lib);
            final boolean hasProblems = !problems.isEmpty();
            final boolean hasErrors = RedXmlProblem.hasErrors(problems);

            final Styler styler = hasProblems
                    ? (hasErrors ? Stylers.Common.ERROR_STYLER : Stylers.Common.WARNING_STYLER)
                    : Stylers.Common.EMPTY_STYLER;

            final StyledString label = new StyledString(lib.getName(), styler);
            label.append(" - " + lib.getPath(), Stylers.Common.ECLIPSE_DECORATION_STYLER);
            return label;
        } else if (element instanceof RemoteLocation) {
            final RemoteLocation location = (RemoteLocation) element;

            final Styler styler = editorInput.getProblemsFor(location).isEmpty() ? Stylers.Common.EMPTY_STYLER
                    : Stylers.Common.ERROR_STYLER;
            final StyledString label = new StyledString(location.getUri(), styler);
            label.append(" - Remote", Stylers.Common.ECLIPSE_DECORATION_STYLER);
            return label;
        }
        return new StyledString();
    }

    @Override
    public Image getImage(final Object element) {
        if (element instanceof ReferencedLibrary) {
            final ReferencedLibrary library = (ReferencedLibrary) element;

            final List<RedXmlProblem> problems = editorInput.getProblemsFor(library);
            if (RedXmlProblem.hasErrors(problems)) {
                return ImagesManager.getImage(RedImages.getRobotLibraryErrorImage());
            } else if (!problems.isEmpty()) {
                return ImagesManager.getImage(RedImages.getRobotLibraryWarnImage());
            } else {
                switch (library.provideType()) {
                    case JAVA:
                        return ImagesManager.getImage(RedImages.getJavaLibraryImage());
                    case PYTHON:
                        return ImagesManager.getImage(RedImages.getPythonLibraryImage());
                    case VIRTUAL:
                        return ImagesManager.getImage(RedImages.getVirtualLibraryImage());
                    default:
                        return ImagesManager.getImage(RedImages.getLibraryImage());
                }
            }
        } else if (element instanceof RemoteLocation) {
            final RemoteLocation location = (RemoteLocation) element;

            final List<RedXmlProblem> problems = editorInput.getProblemsFor(location);
            if (problems.isEmpty()) {
                return ImagesManager.getImage(RedImages.getRemoteConnectedImage());
            } else {
                return ImagesManager.getImage(RedImages.getRemoteDisconnectedImage());
            }
        }
        return null;
    }

    @Override
    public String getToolTipText(final Object element) {
        final List<RedXmlProblem> problems = editorInput.getProblemsFor(element);

        final String descriptions = problems.stream().map(RedXmlProblem::getDescription).collect(joining("\n"));
        return descriptions.isEmpty() ? null : descriptions;
    }

    @Override
    public Image getToolTipImage(final Object element) {
        if (element instanceof ReferencedLibrary) {
            final List<RedXmlProblem> problems = editorInput.getProblemsFor(element);

            if (RedXmlProblem.hasErrors(problems)) {
                return ImagesManager.getImage(RedImages.getErrorImage());
            } else if (!problems.isEmpty()) {
                return ImagesManager.getImage(RedImages.getWarningImage());
            }
        } else if (element instanceof RemoteLocation) {
            final List<RedXmlProblem> problems = editorInput.getProblemsFor(element);
            if (!problems.isEmpty()) {
                return ImagesManager.getImage(RedImages.getErrorImage());
            }
        }
        return null;
    }
}
