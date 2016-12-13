/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.assist;

import org.eclipse.jface.resource.ImageDescriptor;
import org.robotframework.ide.eclipse.main.plugin.RedImages;

class RedSectionProposal extends BaseAssistProposal {

    public RedSectionProposal(final String sectionName, final ProposalMatch match) {
        super(sectionName, match);
    }

    @Override
    public ImageDescriptor getImage() {
        return RedImages.getRobotCasesFileSectionImage();
    }
}
