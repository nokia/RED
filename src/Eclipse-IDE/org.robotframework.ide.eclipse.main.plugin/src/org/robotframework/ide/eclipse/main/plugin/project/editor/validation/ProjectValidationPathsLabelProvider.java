/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.editor.validation;

import static com.google.common.collect.Iterables.transform;
import static org.eclipse.jface.viewers.Stylers.withForeground;

import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.Stylers;
import org.eclipse.swt.graphics.Image;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig.ExcludedFolderPath;
import org.robotframework.ide.eclipse.main.plugin.project.editor.RedProjectEditorInput;
import org.robotframework.ide.eclipse.main.plugin.project.editor.RedProjectEditorInput.RedXmlProblem;
import org.robotframework.red.graphics.ImagesManager;
import org.robotframework.red.viewers.RedCommonLabelProvider;

import com.google.common.base.Joiner;

class ProjectValidationPathsLabelProvider extends RedCommonLabelProvider {
    
    private final RedProjectEditorInput editorInput;

    public ProjectValidationPathsLabelProvider(final RedProjectEditorInput editorInput) {
        this.editorInput = editorInput;
    }

    @Override
    public StyledString getStyledText(final Object element) {
        final ProjectTreeElement projectTreeElement = (ProjectTreeElement) element;
        
        if (projectTreeElement.isExcluded()) {
            final ExcludedFolderPath excludedPath = editorInput.getProjectConfiguration()
                    .getExcludedPath(projectTreeElement.getPath());
            final List<RedXmlProblem> problems = editorInput.getProblemsFor(excludedPath);
            if (!problems.isEmpty()) {
                return new StyledString(projectTreeElement.getLabel() + " [excluded]", Stylers.Common.WARNING_STYLER);
            } else {
                return new StyledString(projectTreeElement.getLabel() + " [excluded]", withForeground(220, 220, 220));
            }
        } else if (projectTreeElement.isExcludedViaInheritance()) {
            return new StyledString(projectTreeElement.getLabel(), withForeground(220, 220, 220));
        } else {
            return new StyledString(projectTreeElement.getLabel());
        }
    }

    @Override
    public Image getImage(final Object element) {
        final ProjectTreeElement projectTreeElement = (ProjectTreeElement) element;

        final ExcludedFolderPath excludedPath = editorInput.getProjectConfiguration().getExcludedPath(projectTreeElement.getPath());
        final List<RedXmlProblem> problems = editorInput.getProblemsFor(excludedPath);
        if (!problems.isEmpty()) {
            return ImagesManager.getImage(RedImages.getBigWarningImage());
        }

        final ImageDescriptor imageDescriptor = projectTreeElement.getImageDescriptor();
        if (projectTreeElement.isExcluded()) {
            return ImagesManager.getImage(RedImages.getGreyedImage(imageDescriptor));
        } else {
            return ImagesManager.getImage(imageDescriptor);
        }
    }

    @Override
    public String getToolTipText(final Object element) {
        final ProjectTreeElement projectTreeElement = (ProjectTreeElement) element;

        final ExcludedFolderPath excludedPath = editorInput.getProjectConfiguration()
                .getExcludedPath(projectTreeElement.getPath());
        final List<RedXmlProblem> problems = editorInput.getProblemsFor(excludedPath);

        final String descriptions = Joiner.on('\n').join(transform(problems, RedXmlProblem.toDescriptions()));
        return descriptions.isEmpty() ? null : descriptions;
    }

    @Override
    public Image getToolTipImage(final Object element) {
        final ProjectTreeElement projectTreeElement = (ProjectTreeElement) element;

        final ExcludedFolderPath excludedPath = editorInput.getProjectConfiguration()
                .getExcludedPath(projectTreeElement.getPath());
        final List<RedXmlProblem> problems = editorInput.getProblemsFor(excludedPath);

        return problems.isEmpty() ? null : ImagesManager.getImage(RedImages.getWarningImage());
    }
}
