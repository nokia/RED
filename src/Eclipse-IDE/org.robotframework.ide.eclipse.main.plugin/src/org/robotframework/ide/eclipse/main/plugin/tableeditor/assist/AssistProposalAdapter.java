/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.assist;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.StyledString;
import org.robotframework.ide.eclipse.main.plugin.assist.AssistProposal;
import org.robotframework.red.jface.assist.RedContentProposal;
import org.robotframework.red.jface.assist.RedTextContentAdapter.SubstituteTextModificationStrategy;

import com.google.common.base.Optional;

public class AssistProposalAdapter implements RedContentProposal {

    private final AssistProposal wrappedProposal;

    private final Optional<ModificationStrategy> modificationStrategy;

    private final String additionalSuffix;

    public AssistProposalAdapter(final AssistProposal wrappedProposal) {
        this(wrappedProposal, null, "");
    }

    public AssistProposalAdapter(final AssistProposal wrappedProposal,
            final ModificationStrategy modificationStrategy) {
        this(wrappedProposal, modificationStrategy, "");
    }

    public AssistProposalAdapter(final AssistProposal wrappedProposal, final String additionalSuffix) {
        this(wrappedProposal, null, additionalSuffix);
    }

    private AssistProposalAdapter(final AssistProposal wrappedProposal,
            final ModificationStrategy modificationStrategy, final String additionalSuffix) {
        this.wrappedProposal = wrappedProposal;
        this.modificationStrategy = Optional.fromNullable(modificationStrategy);
        this.additionalSuffix = additionalSuffix;
    }

    @Override
    public String getContent() {
        return wrappedProposal.getContent() + additionalSuffix;
    }

    @Override
    public int getCursorPosition() {
        return getContent().length();
    }

    @Override
    public ImageDescriptor getImage() {
        return wrappedProposal.getImage();
    }

    @Override
    public StyledString getStyledLabel() {
        return wrappedProposal.getStyledLabel();
    }

    @Override
    public String getLabel() {
        return wrappedProposal.getLabel();
    }

    @Override
    public boolean hasDescription() {
        return wrappedProposal.hasDescription();
    }

    @Override
    public String getDescription() {
        return wrappedProposal.getDescription();
    }

    @Override
    public ModificationStrategy getModificationStrategy() {
        return modificationStrategy.or(new SubstituteTextModificationStrategy());
    }
}
