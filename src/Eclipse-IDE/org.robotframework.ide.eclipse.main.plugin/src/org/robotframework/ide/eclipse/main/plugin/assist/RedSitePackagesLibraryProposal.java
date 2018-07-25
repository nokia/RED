/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.assist;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.StyledString;
import org.robotframework.ide.eclipse.main.plugin.RedImages;

class RedSitePackagesLibraryProposal extends BaseAssistProposal {

    private final boolean isImported;

    RedSitePackagesLibraryProposal(final String name, final boolean isImported, final ProposalMatch match) {
        super(name, match);
        this.isImported = isImported;
    }

    boolean isImported() {
        return isImported;
    }

    @Override
    public ImageDescriptor getImage() {
        if (isImported) {
            return RedImages.getLibraryImage();
        } else {
            return RedImages.getPythonSitePackagesLibraryImage();
        }
    }

    @Override
    public String getLabel() {
        return super.getLabel();
    }

    @Override
    public StyledString getStyledLabel() {
        final StyledString label = super.getStyledLabel();
        if (isImported) {
            label.append(" (already imported)", StyledString.DECORATIONS_STYLER);
        }
        return label;
    }
}
