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
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.TextStyle;
import org.rf.ide.core.project.RobotProjectConfig.ReferencedLibrary;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.project.editor.RedProjectEditorInput;
import org.robotframework.ide.eclipse.main.plugin.project.editor.RedProjectEditorInput.RedXmlProblem;
import org.robotframework.ide.eclipse.main.plugin.project.editor.libraries.ReferencedLibrariesLabelProvider.LibraryStyledElement;
import org.robotframework.red.graphics.ColorsManager;
import org.robotframework.red.graphics.ImagesManager;
import org.robotframework.red.jface.viewers.Stylers;

import com.google.common.base.Objects;

public class RedXmlLibrary implements LibraryStyledElement {

    private final ReferencedLibrary lib;

    private List<RedXmlArgumentsVariant> variants;

    public RedXmlLibrary(final ReferencedLibrary lib) {
        this.lib = lib;
    }

    public ReferencedLibrary getLibrary() {
        return lib;
    }

    void setVariants(final List<RedXmlArgumentsVariant> variants) {
        this.variants = variants;
    }

    public List<RedXmlArgumentsVariant> getVariants() {
        return variants;
    }

    public boolean isDynamic() {
        return lib.isDynamic();
    }

    public void setDynamic(final boolean isDynamic) {
        lib.setDynamic(isDynamic);
    }

    @Override
    public StyledString getStyledText(final RedProjectEditorInput editorInput) {
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
    }

    @Override
    public Image getImage(final RedProjectEditorInput editorInput) {
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
    }

    @Override
    public String getToolTip(final RedProjectEditorInput editorInput) {
        final List<RedXmlProblem> problems = editorInput.getProblemsFor(lib);
        final String descriptions = problems.stream().map(RedXmlProblem::getDescription).collect(joining("\n"));
        return descriptions.isEmpty() ? null : descriptions;
    }

    @Override
    public Image getToolTipImage(final RedProjectEditorInput editorInput) {
        final List<RedXmlProblem> problems = editorInput.getProblemsFor(lib);

        if (RedXmlProblem.hasErrors(problems)) {
            return ImagesManager.getImage(RedImages.getErrorImage());
        } else if (!problems.isEmpty()) {
            return ImagesManager.getImage(RedImages.getWarningImage());
        }
        return null;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj.getClass() == this.getClass()) {
            final RedXmlLibrary that = (RedXmlLibrary) obj;
            return Objects.equal(this.lib, that.lib);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(lib);
    }

    private static final class DynamicLibStyler extends Styler {

        @Override
        public void applyStyles(final TextStyle textStyle) {
            textStyle.background = ColorsManager.getColor(190, 210, 255);
            textStyle.borderStyle = SWT.BORDER_SOLID;
        }
    }

    public static class RedXmlRemoteLib extends RedXmlLibrary {

        public RedXmlRemoteLib() {
            super(null);
        }

        @Override
        public boolean isDynamic() {
            return true;
        }

        @Override
        public StyledString getStyledText(final RedProjectEditorInput editorInput) {
            final StyledString label = new StyledString();
            label.append(" D ", new DynamicLibStyler());
            label.append(" Remote", Stylers.Common.EMPTY_STYLER);
            label.append(" - Standard library", Stylers.Common.ECLIPSE_DECORATION_STYLER);
            return label;
        }

        @Override
        public Image getImage(final RedProjectEditorInput editorInput) {
            return ImagesManager.getImage(RedImages.getLibraryImage());
        }
    }
}