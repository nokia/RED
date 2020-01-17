/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.assist;

import static org.assertj.core.api.Assertions.assertThat;

import org.eclipse.jface.viewers.StyledString;
import org.junit.jupiter.api.Test;
import org.rf.ide.core.libraries.ArgumentsDescriptor;
import org.robotframework.ide.eclipse.main.plugin.RedImages;

public class RedTemplateArgumentsProposalTest {

    @Test
    public void templatedKeywordImageImageIsUsed() {
        final RedTemplateArgumentsProposal proposal = new RedTemplateArgumentsProposal("Template Kw",
                ArgumentsDescriptor.createDescriptor());

        assertThat(proposal.getImage()).isEqualTo(RedImages.getTemplatedKeywordImage());
    }

    @Test
    public void keywordIsAccessible() {
        final RedTemplateArgumentsProposal proposal = new RedTemplateArgumentsProposal("Template Kw",
                ArgumentsDescriptor.createDescriptor());

        assertThat(proposal.isAccessible()).isTrue();
    }

    @Test
    public void sourceDescriptionIsEmpty() {
        final RedTemplateArgumentsProposal proposal = new RedTemplateArgumentsProposal("Template Kw",
                ArgumentsDescriptor.createDescriptor());

        assertThat(proposal.getSourceDescription()).isEmpty();
    }

    @Test
    public void labelIsComposedOfKeywordNameAndPrefix() {
        final RedTemplateArgumentsProposal proposal = new RedTemplateArgumentsProposal("Template Kw",
                ArgumentsDescriptor.createDescriptor());

        assertThat(proposal.getLabel()).isEqualTo("Arguments for: Template Kw");
    }

    @Test
    public void styledLabelIsComposedOfKeywordNameAndPrefix_withoutStyleRanges() {
        final RedTemplateArgumentsProposal proposal = new RedTemplateArgumentsProposal("Template Kw",
                ArgumentsDescriptor.createDescriptor());

        final StyledString label = proposal.getStyledLabel();

        assertThat(label.getString()).isEqualTo("Arguments for: Template Kw");
        assertThat(label.getStyleRanges()).isEmpty();
    }

    @Test
    public void descriptionIsComposedOfKeywordNameAndArguments() {
        final RedTemplateArgumentsProposal proposal = new RedTemplateArgumentsProposal("Template Kw",
                ArgumentsDescriptor.createDescriptor("arg1", "arg2", "arg3", "default=123", "other=456"));

        assertThat(proposal.isDocumented()).isTrue();
        assertThat(proposal.getDescription())
                .isEqualTo("Name: Template Kw\n" + "Arguments: [arg1, arg2, arg3, default=123, other=456]\n\n");
    }

    @Test
    public void contentIsEmpty_whenTemplateKeywordDoesNotHaveArguments() {
        final RedTemplateArgumentsProposal proposal = new RedTemplateArgumentsProposal("Template Kw",
                ArgumentsDescriptor.createDescriptor());

        assertThat(proposal.getContent()).isEmpty();
    }

    @Test
    public void contentIsFirstArgument_whenTemplateKeywordHasArguments() {
        final RedTemplateArgumentsProposal proposal = new RedTemplateArgumentsProposal("Template Kw",
                ArgumentsDescriptor.createDescriptor("arg1", "arg2", "arg3", "default=123", "other=456"));

        assertThat(proposal.getContent()).isEqualTo("arg1");
    }

    @Test
    public void argumentsAreEmpty_whenTemplateKeywordHasAtMostOneArgument() {
        final RedTemplateArgumentsProposal proposal = new RedTemplateArgumentsProposal("Template Kw",
                ArgumentsDescriptor.createDescriptor("arg1"));

        assertThat(proposal.getArguments()).isEmpty();
    }

    @Test
    public void argumentsAreNotEmpty_whenTemplateKeywordHasAtLeastTwoArguments() {
        final RedTemplateArgumentsProposal proposal = new RedTemplateArgumentsProposal("Template Kw",
                ArgumentsDescriptor.createDescriptor("arg1", "arg2", "arg3", "default=123", "other=456"));

        assertThat(proposal.getArguments()).containsExactly("arg2", "arg3", "");
    }
}
