/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.hyperlink;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.IRegion;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.views.documentation.Documentations;

import com.google.common.annotations.VisibleForTesting;

/**
 * @author Michal Anglart
 *
 */
public class UserKeywordDocumentationHyperlink extends KeywordDocumentationHyperlink {

    private final RobotKeywordDefinition userKeyword;

    private final RobotSuiteFile exposingResource;

    private final String additionalLabelDecoration;

    public UserKeywordDocumentationHyperlink(final IRegion from, final RobotSuiteFile exposingResource,
            final RobotKeywordDefinition userKeyword, final String additionalLabelDecoration) {
        super(from, null, null, null);
        this.exposingResource = exposingResource;
        this.userKeyword = userKeyword;
        this.additionalLabelDecoration = additionalLabelDecoration;
    }

    @VisibleForTesting
    public RobotKeywordDefinition getDestinationKeyword() {
        return userKeyword;
    }

    @Override
    public String getLabelForCompoundHyperlinksDialog() {
        return exposingResource.getName();
    }

    @Override
    public String additionalLabelDecoration() {
        return additionalLabelDecoration;
    }

    @Override
    public ImageDescriptor getImage() {
        return RedImages.getImageForFileWithExtension(exposingResource.getFileExtension());
    }

    @Override
    public void open() {
        final IWorkbenchWindow workbench = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        final IWorkbenchPage page = workbench.getActivePage();
        Documentations.showDocForRobotElement(page, userKeyword);
    }
}
