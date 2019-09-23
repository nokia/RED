/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.assist;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.StyledString;
import org.rf.ide.core.libraries.ArgumentsDescriptor;
import org.rf.ide.core.libraries.ArgumentsDescriptor.Argument;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.views.documentation.inputs.DocumentationViewInput;
import org.robotframework.ide.eclipse.main.plugin.views.documentation.inputs.SingleParagraphInput;

import com.google.common.annotations.VisibleForTesting;

public class RedTemplateArgumentsProposal extends RedKeywordProposal {

    @VisibleForTesting
    RedTemplateArgumentsProposal(final String name, final ArgumentsDescriptor argumentsDescriptor) {

        super("", Optional.empty(), null, "", name, argumentsDescriptor, null, false, null,
                AssistProposalPredicates.alwaysFalse(), ProposalMatch.EMPTY);
    }

    @Override
    public String getContent() {
        return getArgumentsDescriptor().getRequiredArguments().stream().findFirst().map(Argument::getName).orElse("");
    }

    @Override
    public List<String> getArguments() {
        final List<String> arguments = getArgumentsDescriptor().getRequiredArguments()
                .stream()
                .skip(1)
                .map(Argument::getName)
                .collect(Collectors.toList());

        if (!getArgumentsDescriptor().hasFixedNumberOfArguments()) {
            arguments.add("");
        }
        return arguments;
    }

    @Override
    public StyledString getStyledLabel() {
        return new StyledString(getLabel());
    }

    @Override
    public boolean isDocumented() {
        return true;
    }

    @Override
    public String getDescription() {
        final StringBuilder builder = new StringBuilder();
        builder.append("Name: ").append(getNameFromDefinition()).append("\n");
        builder.append("Arguments: ").append(getArgumentsDescriptor().getDescription()).append("\n\n");

        return builder.toString();
    }

    @Override
    public DocumentationViewInput getDocumentationInput() {
        return new SingleParagraphInput(this::getDescription);
    }

    @Override
    public boolean isAccessible() {
        return true;
    }

    @Override
    public ImageDescriptor getImage() {
        return RedImages.getTemplatedKeywordImage();
    }

    @Override
    public String getLabel() {
        return "Arguments for: " + getNameFromDefinition();
    }

    @Override
    protected String getSourceDescription() {
        return "";
    }

}
