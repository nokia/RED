/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist;

import static com.google.common.collect.Lists.newArrayList;
import static org.robotframework.ide.eclipse.main.plugin.assist.RedKeywordProposals.sortedByNames;
import static org.robotframework.ide.eclipse.main.plugin.assist.RedVariableProposals.variablesSortedByTypesAndNames;

import java.util.Collection;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.content.IContentDescription;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.assist.RedKeywordProposal;
import org.robotframework.ide.eclipse.main.plugin.assist.RedKeywordProposals;
import org.robotframework.ide.eclipse.main.plugin.assist.RedVariableProposal;
import org.robotframework.ide.eclipse.main.plugin.assist.RedVariableProposals;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig.ReferencedVariableFile;
import org.robotframework.ide.eclipse.main.plugin.project.RobotSuiteFileDescriber;
import org.robotframework.ide.eclipse.main.plugin.project.library.LibrarySpecification;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist.RedCompletionBuilder.AcceptanceMode;


/**
 * @author Michal Anglart
 *
 */
public class SuiteSourceAssistantContext {

    private final RobotSuiteFile suiteModel;

    private final AssistPreferences assistPreferences;

    public SuiteSourceAssistantContext(final RobotSuiteFile robotSuiteFile) {
        this.suiteModel = robotSuiteFile;
        this.assistPreferences = new AssistPreferences();
    }

    public void refreshPreferences() {
        assistPreferences.refresh();
    }

    public RobotSuiteFile getModel() {
        return suiteModel;
    }

    public IFile getFile() {
        return suiteModel.getFile();
    }

    public String getSeparatorToFollow() {
        return assistPreferences.getSeparatorToFollow(getFile().getFileExtension().equals("tsv"));
    }

    public AcceptanceMode getAcceptanceMode() {
        return assistPreferences.getAcceptanceMode();
    }
    
    public boolean isKeywordPrefixAutoAdditionEnabled() {
        return assistPreferences.isKeywordPrefixAutoAdditionEnabled();
    }

    public List<RedVariableProposal> getVariables(final int offset) {
        return new RedVariableProposals(suiteModel).getVariableProposals(variablesSortedByTypesAndNames(), offset);
    }

    public Collection<RedKeywordProposal> getKeywords() {
        final RedKeywordProposals proposals = new RedKeywordProposals(suiteModel);
        return proposals.getKeywordProposals(sortedByNames());
    }

    public Collection<LibrarySpecification> getLibraries() {
        return suiteModel.getProject().getLibrariesSpecifications();
    }

    public boolean isAlreadyImported(final LibrarySpecification spec) {
        return suiteModel.getImportedLibraries().containsKey(spec);
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

    public static class AssistPreferences {

        private AcceptanceMode acceptanceMode;
        private boolean isKeywordPrefixAutoAdditionEnabled;

        AssistPreferences() {
            this.acceptanceMode = RedPlugin.getDefault().getPreferences().getAssistantAcceptanceMode();
            this.isKeywordPrefixAutoAdditionEnabled = RedPlugin.getDefault()
                    .getPreferences()
                    .isAssistantKeywordPrefixAutoAdditionEnabled();
        }

        void refresh() {
            acceptanceMode = RedPlugin.getDefault().getPreferences().getAssistantAcceptanceMode();
            isKeywordPrefixAutoAdditionEnabled = RedPlugin.getDefault()
                    .getPreferences()
                    .isAssistantKeywordPrefixAutoAdditionEnabled();
        }

        public String getSeparatorToFollow(final boolean isTsvFile) {
            return RedPlugin.getDefault().getPreferences().getSeparatorToUse(isTsvFile);
        }

        public AcceptanceMode getAcceptanceMode() {
            return acceptanceMode;
        }

        public boolean isKeywordPrefixAutoAdditionEnabled() {
            return isKeywordPrefixAutoAdditionEnabled;
        }

    }
}
