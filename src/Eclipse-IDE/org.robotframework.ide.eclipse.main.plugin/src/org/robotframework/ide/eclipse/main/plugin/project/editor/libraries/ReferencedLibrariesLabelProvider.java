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
import org.eclipse.swt.graphics.Image;
import org.rf.ide.core.project.RobotProjectConfig.ReferencedLibrary;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.project.editor.RedProjectEditorInput;
import org.robotframework.ide.eclipse.main.plugin.project.editor.RedProjectEditorInput.RedXmlProblem;
import org.robotframework.red.graphics.ImagesManager;
import org.robotframework.red.jface.viewers.Stylers;
import org.robotframework.red.viewers.ElementAddingToken;
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
        } else {
            return ((ElementAddingToken) element).getStyledText();
        }
    }

    @Override
    public Image getImage(final Object element) {
        if (element instanceof ElementAddingToken) {
            return ((ElementAddingToken) element).getImage();
        } else if (element instanceof ReferencedLibrary) {
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
        }
        return null;
    }
}
