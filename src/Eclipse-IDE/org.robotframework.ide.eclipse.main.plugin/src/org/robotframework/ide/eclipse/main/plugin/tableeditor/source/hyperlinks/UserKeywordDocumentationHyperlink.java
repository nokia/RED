/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.hyperlinks;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.IRegion;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.navigator.actions.KeywordDocumentationPopup;


/**
 * @author Michal Anglart
 *
 */
public class UserKeywordDocumentationHyperlink implements RedHyperlink {

    private final IRegion sourceRegion;

    private final RobotKeywordDefinition userKeyword;

    private final RobotSuiteFile exposingResource;

    private final String additionalLabelDecoration;

    public UserKeywordDocumentationHyperlink(final IRegion sourceRegion, final RobotSuiteFile exposingResource,
            final RobotKeywordDefinition userKeyword, final String additionalLabelDecoration) {
        this.sourceRegion = sourceRegion;
        this.exposingResource = exposingResource;
        this.userKeyword = userKeyword;
        this.additionalLabelDecoration = additionalLabelDecoration;
    }

    @Override
    public IRegion getHyperlinkRegion() {
        return sourceRegion;
    }

    @Override
    public String getTypeLabel() {
        return null;
    }

    @Override
    public String getHyperlinkText() {
        return "Open Documentation";
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
        final Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
        new KeywordDocumentationPopup(shell, userKeyword.createSpecification()).open();
    }

}
