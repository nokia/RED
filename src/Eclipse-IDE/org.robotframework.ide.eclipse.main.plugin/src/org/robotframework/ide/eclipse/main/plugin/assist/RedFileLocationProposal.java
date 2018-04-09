/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.assist;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.resource.ImageDescriptor;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.project.ASuiteFileDescriber;
import org.robotframework.ide.eclipse.main.plugin.views.documentation.inputs.DocumentationViewInput;
import org.robotframework.ide.eclipse.main.plugin.views.documentation.inputs.SuiteFileInput;

class RedFileLocationProposal extends BaseAssistProposal {

    private final String label;

    private final IFile targetFile;

    RedFileLocationProposal(final String content, final String label, final IFile targetFile,
            final ProposalMatch match) {
        super(content, match);
        this.label = label;
        this.targetFile = targetFile;
    }

    @Override
    public ImageDescriptor getImage() {
        return RedImages.getImageForResource(targetFile);
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public boolean isDocumented() {
        return ASuiteFileDescriber.isResourceFile(targetFile);
    }

    @Override
    public DocumentationViewInput getDocumentationInput() {
        return new SuiteFileInput(RedPlugin.getModelManager().createSuiteFile(targetFile));
    }
}
