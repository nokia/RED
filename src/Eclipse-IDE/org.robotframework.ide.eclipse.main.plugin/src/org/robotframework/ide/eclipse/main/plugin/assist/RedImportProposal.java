/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.assist;

import java.util.EnumSet;

import org.eclipse.jface.resource.ImageDescriptor;
import org.rf.ide.core.testdata.model.ModelType;
import org.robotframework.ide.eclipse.main.plugin.RedImages;

import com.google.common.base.Preconditions;

class RedImportProposal extends BaseAssistProposal {

    private final ModelType modelType;

    RedImportProposal(final String content, final ModelType modelType, final ProposalMatch match) {
        super(content, match);
        Preconditions.checkArgument(
                EnumSet.of(ModelType.LIBRARY_IMPORT_SETTING, ModelType.RESOURCE_IMPORT_SETTING).contains(modelType));
        this.modelType = modelType;
    }

    @Override
    public String getContent() {
        return super.getContent() + ".";
    }

    @Override
    public ImageDescriptor getImage() {
        if (modelType ==  ModelType.LIBRARY_IMPORT_SETTING) {
            return RedImages.getLibraryImage();
        } else {
            return RedImages.getRobotFileImage();
        }
    }
}