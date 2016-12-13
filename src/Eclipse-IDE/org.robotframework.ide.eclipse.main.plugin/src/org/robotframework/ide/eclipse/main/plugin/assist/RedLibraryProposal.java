/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.assist;

import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.StyledString;
import org.robotframework.ide.eclipse.main.plugin.RedImages;

import com.google.common.base.Joiner;

class RedLibraryProposal extends BaseAssistProposal {

    private final List<String> arguments;

    private final boolean isImported;

    private final String description;

    public RedLibraryProposal(final String content, final List<String> arguments, final boolean isImported,
            final String description, final ProposalMatch match) {
        super(content, match);
        this.arguments = arguments;
        this.isImported = isImported;
        this.description = description;
    }

    boolean isImported() {
        return isImported;
    }

    @Override
    public List<String> getArguments() {
        return arguments;
    }

    @Override
    public ImageDescriptor getImage() {
        return RedImages.getLibraryImage();
    }

    @Override
    public String getLabel() {
        return super.getLabel() + " " + Joiner.on(' ').join(arguments);
    }

    @Override
    public StyledString getStyledLabel() {
        final StyledString label = super.getStyledLabel();
        if (isImported) {
            label.append(" (already imported)", StyledString.DECORATIONS_STYLER);
        }
        return label;
    }

    @Override
    public String getDescription() {
        return description;
    }
}
