/*
 * Copyright 2019 Nokia Solutions and Networks
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
import org.rf.ide.core.project.RobotProjectConfig.RemoteLocation;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.project.editor.RedProjectEditorInput;
import org.robotframework.ide.eclipse.main.plugin.project.editor.RedProjectEditorInput.RedXmlProblem;
import org.robotframework.red.graphics.ImagesManager;
import org.robotframework.red.viewers.ElementAddingToken;
import org.robotframework.red.viewers.RedCommonLabelProvider;

class RemoteLocationsLabelProvider extends RedCommonLabelProvider {

    private final RedProjectEditorInput editorInput;

    public RemoteLocationsLabelProvider(final RedProjectEditorInput editorInput) {
        this.editorInput = editorInput;
    }

    @Override
    public StyledString getStyledText(final Object element) {
        if (element instanceof RemoteLocation) {
            final RemoteLocation location = (RemoteLocation) element;

            final Styler styler = editorInput.getProblemsFor(location).isEmpty() ? Stylers.Common.EMPTY_STYLER
                    : Stylers.Common.ERROR_STYLER;
            final StyledString label = new StyledString(location.getUri(), styler);
            label.append(" - Remote", Stylers.Common.ECLIPSE_DECORATION_STYLER);
            return label;
        } else {
            return ((ElementAddingToken) element).getStyledText();
        }
    }

    @Override
    public Image getImage(final Object element) {
        if (element instanceof ElementAddingToken) {
            return ((ElementAddingToken) element).getImage();
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
        if (element instanceof RemoteLocation) {
            final List<RedXmlProblem> problems = editorInput.getProblemsFor(element);
            if (!problems.isEmpty()) {
                return ImagesManager.getImage(RedImages.getErrorImage());
            }
        }
        return null;
    }
}
