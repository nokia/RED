/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.views.documentation.inputs;

import static com.google.common.collect.Lists.newArrayList;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.EnumSet;
import java.util.Optional;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.rf.ide.core.libraries.Documentation;
import org.rf.ide.core.testdata.model.search.keyword.KeywordScope;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.assist.RedKeywordProposal;
import org.robotframework.ide.eclipse.main.plugin.assist.RedKeywordProposals;
import org.robotframework.ide.eclipse.main.plugin.model.RobotFileInternalElement;
import org.robotframework.ide.eclipse.main.plugin.views.documentation.LibraryUri;
import org.robotframework.ide.eclipse.main.plugin.views.documentation.WorkspaceFileUri;

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
        final Optional<URI> imgUri = isLibraryKeyword() ? RedImages.getKeywordImageUri()
                : RedImages.getUserKeywordImageUri();
        final String srcHref = createShowKeywordSrcUri();
        final String srcLabel = isLibraryKeyword() ? proposal.getSourceName() : proposal.getExposingFilepath().toString();
        final String docHref = createShowSuiteOrLibDocUri();

        final String source = String.format("%s [%s]", Formatters.formatHyperlink(srcHref, srcLabel),
                Formatters.formatHyperlink(docHref, "Documentation"));

        return Formatters.formatSimpleHeader(imgUri, proposal.getKeywordName(),
                newArrayList("Source", source),
                newArrayList("Arguments", proposal.getArgumentsDescriptor().getDescription()));
    }

    private boolean isLibraryKeyword() {
        return EnumSet.of(KeywordScope.STD_LIBRARY, KeywordScope.REF_LIBRARY)
                .contains(proposal.getScope(null));
    }

    private String createShowKeywordSrcUri() {
        try {
            if (isLibraryKeyword()) {
                final String project = proposal.getExposingFilepath().segment(0);
                final String library = proposal.getSourceName();
                final String keyword = proposal.getKeywordName();
                return LibraryUri.createShowKeywordSourceUri(project, library, keyword).toString();
            } else {
                final IResource file = ResourcesPlugin.getWorkspace().getRoot().findMember(
                        proposal.getExposingFilepath());
                return WorkspaceFileUri.createShowKeywordSourceUri((IFile) file, proposal.getKeywordName()).toString();
            }
        } catch (final URISyntaxException e) {
            return "#";
        }
    }

    private String createShowSuiteOrLibDocUri() {
        try {
            if (isLibraryKeyword()) {
                final String projectName = proposal.getExposingFilepath().segment(0);
                return LibraryUri.createShowLibraryDocUri(projectName, proposal.getSourceName()).toString();

            } else {
                final IResource file = ResourcesPlugin.getWorkspace().getRoot().findMember(
                        proposal.getExposingFilepath());
                return WorkspaceFileUri.createShowSuiteDocUri((IFile) file).toString();
            }
        } catch (final URISyntaxException e) {
            return "#";
        }
    }

    @Override
    protected Documentation createDocumentation() {
        return proposal.getDocumentation();
    }

    @Override
    protected String localKeywordsLinker(final String name) {
        try {
            if (isLibraryKeyword()) {
                final String projectName = proposal.getExposingFilepath().segment(0);
                return LibraryUri.createShowKeywordDocUri(projectName, proposal.getSourceName(), name).toString();

            } else {
                final IResource file = ResourcesPlugin.getWorkspace().getRoot().findMember(
                        proposal.getExposingFilepath());
                return WorkspaceFileUri.createShowKeywordDocUri((IFile) file, name).toString();
            }
        } catch (final URISyntaxException e) {
            return "#";
        }
    }
}