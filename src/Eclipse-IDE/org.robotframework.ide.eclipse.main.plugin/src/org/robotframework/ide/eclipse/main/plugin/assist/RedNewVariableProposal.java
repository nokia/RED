/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.assist;

import java.util.EnumSet;
import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
import org.rf.ide.core.testdata.model.table.variables.AVariable.VariableType;
import org.robotframework.ide.eclipse.main.plugin.views.documentation.inputs.DocumentationViewInput;
import org.robotframework.ide.eclipse.main.plugin.views.documentation.inputs.SingleParagraphInput;

import com.google.common.base.Preconditions;

public class RedNewVariableProposal extends BaseAssistProposal {

    private final VariableType type;

    private final List<String> arguments;

    private final ImageDescriptor image;

    private final String label;

    private final String description;

    RedNewVariableProposal(final String content, final VariableType type, final List<String> arguments,
            final ImageDescriptor image, final String label, final String description) {
        super(content, ProposalMatch.EMPTY);
        Preconditions.checkArgument(
                EnumSet.of(VariableType.SCALAR, VariableType.LIST, VariableType.DICTIONARY).contains(type));
        this.type = type;
        this.arguments = arguments;
        this.image = image;
        this.label = label;
        this.description = description;
    }

    public VariableType getType() {
        return type;
    }

    @Override
    public List<String> getArguments() {
        return arguments;
    }

    @Override
    public ImageDescriptor getImage() {
        return image;
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public boolean isDocumented() {
        return true;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public DocumentationViewInput getDocumentationInput() {
        return new SingleParagraphInput(this::getDescription);
    }
}
