/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source;

import static com.google.common.collect.Lists.newArrayList;
import static org.robotframework.ide.eclipse.main.plugin.assist.RedKeywordProposals.sortedByNames;
import static org.robotframework.ide.eclipse.main.plugin.assist.RedVariableProposals.variablesSortedByTypesAndNames;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.content.IContentDescription;
import org.robotframework.ide.eclipse.main.plugin.assist.RedKeywordProposal;
import org.robotframework.ide.eclipse.main.plugin.assist.RedKeywordProposals;
import org.robotframework.ide.eclipse.main.plugin.assist.RedVariableProposal;
import org.robotframework.ide.eclipse.main.plugin.assist.RedVariableProposals;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig.ReferencedVariableFile;
import org.robotframework.ide.eclipse.main.plugin.project.RobotSuiteFileDescriber;
import org.robotframework.ide.eclipse.main.plugin.project.library.LibrarySpecification;
import org.robotframework.ide.eclipse.main.plugin.texteditor.contentAssist.ContentAssistKeywordContext;
import org.robotframework.ide.eclipse.main.plugin.texteditor.contentAssist.TextEditorContentAssist;


/**
 * @author Michal Anglart
 *
 */
public class SuiteSourceEditorContentAssist extends TextEditorContentAssist {

    private final RobotSuiteFile suiteModel;

    public SuiteSourceEditorContentAssist(final RobotSuiteFile robotSuiteFile) {
        super(null, null);
        this.suiteModel = robotSuiteFile;
    }

    public IFile getFile() {
        return suiteModel.getFile();
    }

    @Override
    public List<RedVariableProposal> getVariables() {
        return new RedVariableProposals(suiteModel).getVariableProposals(variablesSortedByTypesAndNames());
    }

    @Override
    public List<RedVariableProposal> getVariables(final int offset) {
        return new RedVariableProposals(suiteModel).getVariableProposals(variablesSortedByTypesAndNames(), offset);
    }

    @Override
    public Map<String, ContentAssistKeywordContext> getKeywordMap() {
        final RedKeywordProposals proposals = new RedKeywordProposals(suiteModel);
        final List<RedKeywordProposal> keywordProposals = proposals.getKeywordProposals(sortedByNames());

        final Map<String, ContentAssistKeywordContext> mapping = new LinkedHashMap<>();
        for (final RedKeywordProposal proposal : keywordProposals) {
            mapping.put(proposal.getLabel(), new ContentAssistKeywordContext(proposal));
        }
        return mapping;
    }

    public Collection<LibrarySpecification> getLibraries() {
        return suiteModel.getProject().getLibrariesSpecifications();
    }

    public boolean isAlreadyImported(final LibrarySpecification spec) {
        return suiteModel.getImportedLibraries().contains(spec);
    }

    public List<ReferencedVariableFile> getReferencedVariableFiles() {
        return suiteModel.getProject().getVariablesFromReferencedFiles();
    }

    public List<IFile> getResourceFiles() {
        final IWorkspaceRoot wsRoot = suiteModel.getProject().getProject().getWorkspace().getRoot();
        final List<IFile> resourceFiles = newArrayList();
        try {
            wsRoot.accept(new IResourceVisitor() {
                @Override
                public boolean visit(final IResource resource) throws CoreException {
                    if (resource.getType() == IResource.FILE) {
                        final IFile file = (IFile) resource;
                        final IContentDescription contentDescription = file.getContentDescription();
                        if (contentDescription != null && RobotSuiteFileDescriber.RESOURCE_FILE_CONTENT_ID
                                .equals(contentDescription.getContentType().getId())) {
                            resourceFiles.add(file);
                        }
                    }
                    return true;
                }
            });
        } catch (final CoreException e) {
            return resourceFiles;
        }
        return resourceFiles;
    }
}
