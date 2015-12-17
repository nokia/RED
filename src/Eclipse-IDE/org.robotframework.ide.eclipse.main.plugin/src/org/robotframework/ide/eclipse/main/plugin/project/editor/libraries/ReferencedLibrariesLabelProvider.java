/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.editor.libraries;

import static com.google.common.collect.Iterables.transform;

import java.util.List;

import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.jface.viewers.Stylers;
import org.eclipse.jface.viewers.StylersDisposingLabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.TextStyle;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.RedTheme;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig.ReferencedLibrary;
import org.robotframework.ide.eclipse.main.plugin.project.editor.RedProjectEditorInput;
import org.robotframework.ide.eclipse.main.plugin.project.editor.RedProjectEditorInput.RedXmlProblem;
import org.robotframework.red.graphics.ImagesManager;

import com.google.common.base.Function;
import com.google.common.base.Joiner;

class ReferencedLibrariesLabelProvider extends StylersDisposingLabelProvider {

    private final RedProjectEditorInput editorInput;

    public ReferencedLibrariesLabelProvider(final RedProjectEditorInput editorInput) {
        this.editorInput = editorInput;
    }

    @Override
    public StyledString getStyledText(final Object element) {
        final ReferencedLibrary lib = (ReferencedLibrary) element;

        final List<RedXmlProblem> problems = editorInput.getProblemsFor(lib);
        final boolean hasProblems = !problems.isEmpty();
        final boolean hasErrors = RedXmlProblem.hasErrors(problems);

        final Styler styler = hasProblems ? (hasErrors ? Stylers.Common.ERROR_STYLER : Stylers.Common.WARNING_STYLER)
                : Stylers.Common.EMPTY_STYLER;

        final StyledString label = new StyledString(lib.getName(), styler);
        label.append(" - " + lib.getPath(), new Styler() {

            @Override
            public void applyStyles(final TextStyle textStyle) {
                textStyle.foreground = RedTheme.getEclipseDecorationColor();
            }
        });
        return label;
    }

    @Override
    public Image getImage(final Object element) {
        final ReferencedLibrary library = (ReferencedLibrary) element;

        final List<RedXmlProblem> problems = editorInput.getProblemsFor(library);
        if (RedXmlProblem.hasErrors(problems)) {
            return ImagesManager.getImage(RedImages.getRobotLibraryErrorImage());
        } else if (!problems.isEmpty()) {
            return ImagesManager.getImage(RedImages.getRobotLibraryWarnImage());
        } else {
            return ImagesManager.getImage(library.getImage());
        }
    }

    @Override
    public String getToolTipText(final Object element) {
        final ReferencedLibrary library = (ReferencedLibrary) element;

        final List<RedXmlProblem> problems = editorInput.getProblemsFor(library);
        final String descriptions = Joiner.on('\n').join(transform(problems, new Function<RedXmlProblem, String>() {
            @Override
            public String apply(final RedXmlProblem problem) {
                return problem.getDescription();
            }
        }));
        return descriptions.isEmpty() ? null : descriptions;
    }

    @Override
    public Image getToolTipImage(final Object element) {
        final ReferencedLibrary library = (ReferencedLibrary) element;

        final List<RedXmlProblem> problems = editorInput.getProblemsFor(library);

        if (RedXmlProblem.hasErrors(problems)) {
            return ImagesManager.getImage(RedImages.getErrorImage());
        } else if (!problems.isEmpty()) {
            return ImagesManager.getImage(RedImages.getWarningImage());
        }
        return null;
    }
}
