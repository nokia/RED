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
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.TextStyle;
import org.rf.ide.core.project.RobotProjectConfig.ReferencedLibrary;
import org.rf.ide.core.project.RobotProjectConfig.ReferencedLibraryArgumentsVariant;
import org.rf.ide.core.project.RobotProjectConfig.RemoteLocation;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.project.editor.RedProjectEditorInput;
import org.robotframework.ide.eclipse.main.plugin.project.editor.RedProjectEditorInput.RedXmlProblem;
import org.robotframework.ide.eclipse.main.plugin.project.editor.libraries.ReferencedLibrariesContentProvider.RemoteLibraryViewItem;
import org.robotframework.red.graphics.ColorsManager;
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
        if (element instanceof RemoteLibraryViewItem) {
            final StyledString label = new StyledString();
            label.append(" D ", new DynamicLibStyler());
            label.append(" Remote", Stylers.Common.EMPTY_STYLER);
            label.append(" - Standard library", Stylers.Common.ECLIPSE_DECORATION_STYLER);
            return label;

        } else if (element instanceof ReferencedLibrary) {
            final ReferencedLibrary lib = (ReferencedLibrary) element;

            final List<RedXmlProblem> problems = editorInput.getProblemsFor(lib);
            final boolean hasProblems = !problems.isEmpty();
            final boolean hasErrors = RedXmlProblem.hasErrors(problems);

            final Styler styler = hasProblems
                    ? (hasErrors ? Stylers.Common.ERROR_STYLER : Stylers.Common.WARNING_STYLER)
                    : Stylers.Common.EMPTY_STYLER;

            final StyledString label = new StyledString();
            if (lib.isDynamic()) {
                label.append(" D ", new DynamicLibStyler());
                label.append(" ");
            }
            label.append(lib.getName(), styler);
            label.append(" - " + lib.getPath(), Stylers.Common.ECLIPSE_DECORATION_STYLER);
            return label;

        } else if (element instanceof RemoteLocation) {
            final RemoteLocation location = (RemoteLocation) element;

            final Styler styler = editorInput.getProblemsFor(location).isEmpty() ? Stylers.Common.EMPTY_STYLER
                    : Stylers.Common.ERROR_STYLER;

            return new StyledString(location.getUri(), styler);

        } else if (element instanceof ReferencedLibraryArgumentsVariant) {
            final ReferencedLibraryArgumentsVariant variant = (ReferencedLibraryArgumentsVariant) element;

            final String label = variant.getArgsStream().collect(joining(", ", "[", "]"));
            return new StyledString(label, Stylers.Common.ECLIPSE_DECORATION_STYLER);

        } else {
            return ((ElementAddingToken) element).getStyledText();
        }
    }

    @Override
    public Image getImage(final Object element) {
        if (element instanceof RemoteLibraryViewItem) {
            return ImagesManager.getImage(RedImages.getLibraryImage());

        } else if (element instanceof ReferencedLibrary) {
            final ReferencedLibrary lib = (ReferencedLibrary) element;

            final List<RedXmlProblem> problems = editorInput.getProblemsFor(lib);
            if (RedXmlProblem.hasErrors(problems)) {
                return ImagesManager.getImage(RedImages.getRobotLibraryErrorImage());

            } else if (!problems.isEmpty()) {
                return ImagesManager.getImage(RedImages.getRobotLibraryWarnImage());

            } else {
                switch (lib.provideType()) {
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
            final RemoteLocation remoteLocation = (RemoteLocation) element;

            final List<RedXmlProblem> problems = editorInput.getProblemsFor(remoteLocation);
            return problems.isEmpty() ? ImagesManager.getImage(RedImages.getRemoteConnectedImage())
                    : ImagesManager.getImage(RedImages.getRemoteDisconnectedImage());

        } else if (element instanceof ReferencedLibraryArgumentsVariant) {
            return null;

        } else {
            return ((ElementAddingToken) element).getImage();
        }
    }

    @Override
    public String getToolTipText(final Object element) {
        if (element instanceof ReferencedLibrary || element instanceof RemoteLocation) {
            final List<RedXmlProblem> problems = editorInput.getProblemsFor(element);

            final String descriptions = problems.stream().map(RedXmlProblem::getDescription).collect(joining("\n"));
            return descriptions.isEmpty() ? null : descriptions;

        } else {
            return null;
        }
    }

    @Override
    public Image getToolTipImage(final Object element) {
        final List<RedXmlProblem> problems = editorInput.getProblemsFor(element);

        if (RedXmlProblem.hasErrors(problems)) {
            return ImagesManager.getImage(RedImages.getErrorImage());
        } else if (!problems.isEmpty()) {
            return ImagesManager.getImage(RedImages.getWarningImage());
        }
        return null;
    }

    private static final class DynamicLibStyler extends Styler {

        @Override
        public void applyStyles(final TextStyle textStyle) {
            textStyle.background = ColorsManager.getColor(190, 210, 255);
            textStyle.borderStyle = SWT.BORDER_SOLID;
        }
    }
}
