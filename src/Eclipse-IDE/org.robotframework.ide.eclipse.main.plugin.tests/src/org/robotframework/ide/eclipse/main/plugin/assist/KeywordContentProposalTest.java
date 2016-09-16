/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.assist;

import static org.assertj.core.api.Assertions.assertThat;

import org.eclipse.core.runtime.Path;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.assist.RedKeywordProposal.KeywordType;
import org.robotframework.ide.eclipse.main.plugin.model.KeywordScope;
import org.robotframework.ide.eclipse.main.plugin.project.library.ArgumentsDescriptor;

import com.google.common.base.Supplier;

public class KeywordContentProposalTest {

    @Test
    public void checkPropertiesExposedByContentProposalBean() {
        final KeywordContentProposal proposal = new KeywordContentProposal(createProposalToWrap(), "&na");
        
        assertThat(proposal.getContent()).isEqualTo("&name");
        assertThat(proposal.getCursorPosition()).isEqualTo(5);
        assertThat(proposal.getLabel()).isEqualTo("&name");
        assertThat(proposal.getImage()).isEqualTo(RedImages.getKeywordImage());
        assertThat(proposal.getLabelDecoration()).isEqualTo("decoration");
        assertThat(proposal.hasDescription()).isTrue();
        assertThat(proposal.getMatchingPrefix()).isEqualTo("&na");
        final String description = proposal.getDescription();

        assertThat(description).contains("&amp;name", "&lt;source&gt;", "arg&lt;&gt;");
    }

    private RedKeywordProposal createProposalToWrap() {
        final Supplier<String> docSupplier = new Supplier<String>() {
            @Override
            public String get() {
                return "<p>doc</p>";
            }
        };
        return new RedKeywordProposal("<source>", "source-alias", KeywordScope.LOCAL, KeywordType.LIBRARY, "&name",
                "decoration", true, ArgumentsDescriptor.createDescriptor("arg<>"), docSupplier,
                "documentation", false, new Path("path"));
    }

}
