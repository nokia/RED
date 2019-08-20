/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.editor.libraries;

import static java.util.stream.Collectors.joining;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.swt.graphics.Image;
import org.rf.ide.core.project.RobotProjectConfig.ReferencedLibraryArgumentsVariant;
import org.rf.ide.core.project.RobotProjectConfig.RemoteLocation;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.project.editor.RedProjectEditorInput;
import org.robotframework.ide.eclipse.main.plugin.project.editor.RedProjectEditorInput.RedXmlProblem;
import org.robotframework.ide.eclipse.main.plugin.project.editor.libraries.ReferencedLibrariesLabelProvider.LibraryStyledElement;
import org.robotframework.red.graphics.ImagesManager;
import org.robotframework.red.jface.viewers.Stylers;

public class RedXmlArgumentsVariant implements LibraryStyledElement {

    private final RedXmlLibrary parent;

    private final ReferencedLibraryArgumentsVariant variant;

    public RedXmlArgumentsVariant(final RedXmlLibrary parent, final ReferencedLibraryArgumentsVariant variant) {
        this.parent = parent;
        this.variant = variant;
    }

    public RedXmlLibrary getParent() {
        return parent;
    }

    public ReferencedLibraryArgumentsVariant getVariant() {
        return variant;
    }

    public String getArguments() {
        return variant.getArgsStream().collect(joining("::"));
    }

    public void setArguments(final String newValue) {
        variant.setArguments(Arrays.asList(newValue.split("::")));
    }

    @Override
    public StyledString getStyledText(final RedProjectEditorInput editorInput) {
        final String label = variant.getArgsStream().collect(joining(", ", "[", "]"));
        return new StyledString(label, Stylers.Common.ECLIPSE_DECORATION_STYLER);
    }

    @Override
    public Image getImage(final RedProjectEditorInput editorInput) {
        return null;
    }


    public static class RedXmlRemoteArgumentsVariant extends RedXmlArgumentsVariant {

        private final RemoteLocation remoteLocation;

        public RedXmlRemoteArgumentsVariant(final RedXmlLibrary parent, final RemoteLocation remoteLocation) {
            super(parent, null);
            this.remoteLocation = remoteLocation;
        }

        public RemoteLocation getRemoteLocation() {
            return remoteLocation;
        }

        @Override
        public String getArguments() {
            return remoteLocation.getUri();
        }

        @Override
        public void setArguments(final String newValue) {
            remoteLocation.setUri(newValue);
        }

        @Override
        public StyledString getStyledText(final RedProjectEditorInput editorInput) {
            final Styler styler = editorInput.getProblemsFor(remoteLocation).isEmpty() ? Stylers.Common.EMPTY_STYLER
                    : Stylers.Common.ERROR_STYLER;
            return new StyledString(remoteLocation.getUri(), styler);
        }

        @Override
        public Image getImage(final RedProjectEditorInput editorInput) {
            final List<RedXmlProblem> problems = editorInput.getProblemsFor(remoteLocation);
            return problems.isEmpty() ? ImagesManager.getImage(RedImages.getRemoteConnectedImage())
                    : ImagesManager.getImage(RedImages.getRemoteDisconnectedImage());
        }

        @Override
        public String getToolTip(final RedProjectEditorInput editorInput) {
            final List<RedXmlProblem> problems = editorInput.getProblemsFor(remoteLocation);

            final String descriptions = problems.stream().map(RedXmlProblem::getDescription).collect(joining("\n"));
            return descriptions.isEmpty() ? null : descriptions;
        }

        @Override
        public Image getToolTipImage(final RedProjectEditorInput editorInput) {
            final List<RedXmlProblem> problems = editorInput.getProblemsFor(remoteLocation);
            return problems.isEmpty() ? null : ImagesManager.getImage(RedImages.getErrorImage());
        }
    }
}