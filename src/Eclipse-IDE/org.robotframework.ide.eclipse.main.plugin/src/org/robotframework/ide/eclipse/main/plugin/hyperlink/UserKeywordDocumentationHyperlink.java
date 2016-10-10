/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.hyperlink;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.IRegion;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;


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
        open(userKeyword.createSpecification());
    }
}
