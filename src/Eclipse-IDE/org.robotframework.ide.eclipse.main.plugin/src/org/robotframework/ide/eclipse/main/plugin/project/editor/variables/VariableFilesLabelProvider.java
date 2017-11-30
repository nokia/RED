/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.editor.variables;

import static com.google.common.collect.Iterables.transform;

import java.util.List;

import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.jface.viewers.Stylers;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.TextStyle;
import org.rf.ide.core.project.RobotProjectConfig.ReferencedVariableFile;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.RedTheme;
import org.robotframework.ide.eclipse.main.plugin.project.editor.RedProjectEditorInput;
import org.robotframework.ide.eclipse.main.plugin.project.editor.RedProjectEditorInput.RedXmlProblem;
import org.robotframework.red.graphics.ImagesManager;
import org.robotframework.red.viewers.ElementAddingToken;
import org.robotframework.red.viewers.RedCommonLabelProvider;

import com.google.common.base.Joiner;

class VariableFilesLabelProvider extends RedCommonLabelProvider {
    
    private final RedProjectEditorInput editorInput;

    public VariableFilesLabelProvider(final RedProjectEditorInput editorInput) {
        this.editorInput = editorInput;
    }

    @Override
    public StyledString getStyledText(final Object element) {
        if (element instanceof ReferencedVariableFile) {
            final ReferencedVariableFile varFile = (ReferencedVariableFile) element;
            return getStyledText(varFile, editorInput.getProblemsFor(varFile));
        } else if (element instanceof ElementAddingToken) {
            return ((ElementAddingToken) element).getStyledText();
        } else {
            return new StyledString();
        }
    }

    private StyledString getStyledText(final ReferencedVariableFile element, final List<RedXmlProblem> problems) {
        final ReferencedVariableFile varFile = element;
        final StyledString label = new StyledString();

        final boolean hasProblems = !problems.isEmpty();
        final boolean hasErrors = RedXmlProblem.hasErrors(problems);

        final Styler styler = hasProblems ? (hasErrors ? Stylers.Common.ERROR_STYLER : Stylers.Common.WARNING_STYLER)
                : Stylers.Common.EMPTY_STYLER;

        label.append(Path.fromPortableString(varFile.getPath()).lastSegment(), styler);
        label.append(' ');
        label.append("- " + new Path(varFile.getPath()), new Styler() {
            @Override
            public void applyStyles(final TextStyle textStyle) {
                textStyle.foreground = RedTheme.Colors.getEclipseDecorationColor();
            }
        });
        return label;
    }

    @Override
    public Image getImage(final Object element) {
        if (element instanceof ElementAddingToken) {
            return ((ElementAddingToken) element).getImage();
        } else {
            final ReferencedVariableFile varFile = (ReferencedVariableFile) element;

            final List<RedXmlProblem> problems = editorInput.getProblemsFor(varFile);
            if (RedXmlProblem.hasErrors(problems)) {
                return ImagesManager.getImage(RedImages.getRobotUnknownVariableImage());
            } else if (!problems.isEmpty()) {
                return ImagesManager.getImage(RedImages.getRobotWarnedVariableImage());
            } else {
                return ImagesManager.getImage(RedImages.getRobotScalarVariableImage());
            }
        }
    }

    @Override
    public String getToolTipText(final Object element) {
        if (element instanceof ReferencedVariableFile) {
            final ReferencedVariableFile varFile = (ReferencedVariableFile) element;

            final List<RedXmlProblem> problems = editorInput.getProblemsFor(varFile);
            final String descriptions = Joiner.on('\n').join(transform(problems, RedXmlProblem.toDescriptions()));
            return descriptions.isEmpty() ? null : descriptions;
        }
        return null;
    }

    @Override
    public Image getToolTipImage(final Object element) {
        if (element instanceof ReferencedVariableFile) {
            final ReferencedVariableFile varFile = (ReferencedVariableFile) element;

            final List<RedXmlProblem> problems = editorInput.getProblemsFor(varFile);

            if (RedXmlProblem.hasErrors(problems)) {
                return ImagesManager.getImage(RedImages.getErrorImage());
            } else if (!problems.isEmpty()) {
                return ImagesManager.getImage(RedImages.getWarningImage());
            }
        }
        return null;
    }
}
