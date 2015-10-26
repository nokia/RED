/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.texteditor.contentAssist;

import org.eclipse.swt.graphics.Image;
import org.robotframework.ide.eclipse.main.plugin.assist.RedKeywordProposal;
import org.robotframework.red.graphics.ImagesManager;

class ContentAssistKeywordContext {

    private final RedKeywordProposal proposal;

    public ContentAssistKeywordContext(final RedKeywordProposal proposal) {
        this.proposal = proposal;
    }

    String getLibName() {
        return proposal.getLabelDecoration().substring(2);
    }
    
    String getArguments() {
        return proposal.getArgumentsLabel();
    }

    String getDescription() {
        return proposal.getDocumentation();
    }
    
    Image getImage() {
        return ImagesManager.getImage(proposal.getImage());
    }
}
