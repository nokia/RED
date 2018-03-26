/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.views.documentation.inputs;

import static com.google.common.collect.Lists.newArrayList;

import java.net.URI;
import java.util.EnumSet;
import java.util.Optional;

import org.rf.ide.core.libraries.Documentation;
import org.rf.ide.core.testdata.model.search.keyword.KeywordScope;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.assist.RedKeywordProposal;
import org.robotframework.ide.eclipse.main.plugin.assist.RedKeywordProposals;
import org.robotframework.ide.eclipse.main.plugin.model.RobotFileInternalElement;

public class KeywordProposalInput extends InternalElementInput<RobotFileInternalElement> {

    private RedKeywordProposal proposal;

    public KeywordProposalInput(final RobotFileInternalElement element, final String selectedLabel) {
        super(element, selectedLabel);
    }

    @Override
    public void prepare() {
        proposal = new RedKeywordProposals(element.getSuiteFile()).getBestMatchingKeywordProposal(selectedLabel)
                .orElseThrow(() -> new DocumentationInputGenerationException(
                        "Keyword " + selectedLabel + "not found, nothing to display"));
    }

    @Override
    protected String createHeader() {
        final boolean isLibraryKeyword = EnumSet.of(KeywordScope.STD_LIBRARY, KeywordScope.REF_LIBRARY)
                .contains(proposal.getScope(null));
        final Optional<URI> imgUri = isLibraryKeyword ? RedImages.getKeywordImageUri()
                : RedImages.getUserKeywordImageUri();
        final String source = isLibraryKeyword ? proposal.getSourceName() : proposal.getExposingFilepath().toOSString();

        return Headers.formatSimpleHeader(imgUri, proposal.getKeywordName(),
                newArrayList("Source", source),
                newArrayList("Arguments", proposal.getArgumentsDescriptor().getDescription()));
    }

    @Override
    protected Documentation createDocumentation() {
        return proposal.getDocumentation();
    }
}