/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist;

import static com.google.common.collect.Lists.newArrayList;
import static org.robotframework.ide.eclipse.main.plugin.assist.RedVariableProposal.variablesSortedByOriginAndNames;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.assist.RedKeywordProposal;
import org.robotframework.ide.eclipse.main.plugin.assist.RedKeywordProposals;
import org.robotframework.ide.eclipse.main.plugin.assist.RedVariableProposal;
import org.robotframework.ide.eclipse.main.plugin.assist.RedVariableProposals;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.locators.KeywordEntity;
import org.robotframework.ide.eclipse.main.plugin.project.ASuiteFileDescriber;
import org.robotframework.ide.eclipse.main.plugin.project.library.LibrarySpecification;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist.RedCompletionBuilder.AcceptanceMode;

import com.google.common.annotations.VisibleForTesting;

/**
 * @author Michal Anglart
 *
 */
public class SuiteSourceAssistantContext {

    private final RobotSuiteFile suiteModel;

    private final AssistPreferences assistPreferences;

    public SuiteSourceAssistantContext(final RobotSuiteFile robotSuiteFile) {
        this(robotSuiteFile, new AssistPreferences());
    }

    @VisibleForTesting
    public SuiteSourceAssistantContext(final RobotSuiteFile robotSuiteFile, final AssistPreferences assistPreferences) {
        this.suiteModel = robotSuiteFile;
        this.assistPreferences = assistPreferences;
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
        return assistPreferences.getSeparatorToFollow(isTsvFile());
    }

    public boolean isTsvFile() {
        return getFile().getFileExtension().equals("tsv");
    }

    public AcceptanceMode getAcceptanceMode() {
        return assistPreferences.getAcceptanceMode();
    }
    
    public boolean isKeywordPrefixAutoAdditionEnabled() {
        return assistPreferences.isKeywordPrefixAutoAdditionEnabled();
    }

    public List<RedVariableProposal> getVariables(final int offset) {
        return new RedVariableProposals(suiteModel).getVariableProposals(variablesSortedByOriginAndNames(), offset);
    }

    public List<RedKeywordProposal> getKeywords(final String prefix,
            final Comparator<? super RedKeywordProposal> comparator) {
        return new RedKeywordProposals(suiteModel).getKeywordProposals(prefix, comparator);
    }

    public KeywordEntity getBestMatchingKeyword(final String name) {
        return new RedKeywordProposals(suiteModel).getBestMatchingKeywordProposal(name);
    }

    public Collection<LibrarySpecification> getLibraries() {
        return suiteModel.getProject().getLibrariesSpecifications();
    }

    public boolean isAlreadyImported(final LibrarySpecification spec) {
        return suiteModel.getImportedLibraries().containsKey(spec);
    }

    public List<IFile> getVariableFiles() {
        final IWorkspaceRoot wsRoot = suiteModel.getProject().getProject().getWorkspace().getRoot();
        return getMatchingFiles(wsRoot, new FileMatcher() {
            @Override
            public boolean matches(final IFile file) {
                return file.getFileExtension().equals("py");
            }
        });
    }

    public List<IFile> getResourceFiles() {
        final IWorkspaceRoot wsRoot = suiteModel.getProject().getProject().getWorkspace().getRoot();
        return getMatchingFiles(wsRoot, new FileMatcher() {

            @Override
            public boolean matches(final IFile file) {
                return ASuiteFileDescriber.isResourceFile(file);
            }
        });
    }

    private List<IFile> getMatchingFiles(final IResource wsRoot, final FileMatcher matcher) {
        final List<IFile> matchingFiles = newArrayList();
        try {
            wsRoot.accept(new IResourceVisitor() {

                @Override
                public boolean visit(final IResource resource) throws CoreException {
                    if (resource.getType() == IResource.FILE) {
                        final IFile file = (IFile) resource;
                        if (matcher.matches(file)) {
                            matchingFiles.add(file);
                        }
                    }
                    return true;
                }
            });
        } catch (final CoreException e) {
            Collections.sort(matchingFiles, createComparator());
            return matchingFiles;
        }
        Collections.sort(matchingFiles, createComparator());
        return matchingFiles;
    }

    private Comparator<IFile> createComparator() {
        return new Comparator<IFile>() {
            @Override
            public int compare(final IFile file1, final IFile file2) {
                final String currentProject = suiteModel.getProject().getProject().getFullPath().segment(0);

                final IPath path1 = file1.getFullPath();
                final IPath path2 = file2.getFullPath();

                if (path1.segment(0).equals(currentProject) && !path2.segment(0).equals(currentProject)) {
                    return -1;
                } else if (!path1.segment(0).equals(currentProject) && path2.segment(0).equals(currentProject)) {
                    return 1;
                } else {
                    int i = 0;
                    for (; i < path1.segmentCount() && i < path2.segmentCount(); i++) {
                        if (i == path1.segmentCount() - 1 && i < path2.segmentCount() - 1) {
                            return -1;
                        } else if (i < path1.segmentCount() - 1 && i == path2.segmentCount() - 1) {
                            return 1;
                        }

                        final int segmentResult = path1.segment(i).compareTo(path2.segment(i));
                        if (segmentResult != 0) {
                            return segmentResult;
                        }
                    }

                    if (i >= path1.segmentCount() && i >= path2.segmentCount()) {
                        return 0;
                    }
                    return i < path1.segmentCount() ? -1 : 1;
                }
            }
        };
    }

    private interface FileMatcher {

        boolean matches(IFile file);
    }

    public static class AssistPreferences {

        private AcceptanceMode acceptanceMode;
        private boolean isKeywordPrefixAutoAdditionEnabled;

        private String separatorToUseInTsv;

        private String separatorToUseInRobot;

        AssistPreferences() {
            this(RedPlugin.getDefault().getPreferences().getAssistantAcceptanceMode(),
                    RedPlugin.getDefault().getPreferences().isAssistantKeywordPrefixAutoAdditionEnabled(),
                    RedPlugin.getDefault().getPreferences().getSeparatorToUse(false),
                    RedPlugin.getDefault().getPreferences().getSeparatorToUse(true));
        }

        @VisibleForTesting
        public AssistPreferences(final AcceptanceMode acceptanceMode, final boolean isKeywordPrefixAutoAdditionEnabled,
                final String separatorToUse) {
            this(acceptanceMode, isKeywordPrefixAutoAdditionEnabled, separatorToUse, separatorToUse);
        }

        @VisibleForTesting
        public AssistPreferences(final AcceptanceMode acceptanceMode, final boolean isKeywordPrefixAutoAdditionEnabled,
                final String separatorToUseInRobot, final String separatorToUseInTsv) {
            this.acceptanceMode = acceptanceMode;
            this.isKeywordPrefixAutoAdditionEnabled = isKeywordPrefixAutoAdditionEnabled;
            this.separatorToUseInRobot = separatorToUseInRobot;
            this.separatorToUseInTsv = separatorToUseInTsv;
        }

        void refresh() {
            acceptanceMode = RedPlugin.getDefault().getPreferences().getAssistantAcceptanceMode();
            isKeywordPrefixAutoAdditionEnabled = RedPlugin.getDefault()
                    .getPreferences()
                    .isAssistantKeywordPrefixAutoAdditionEnabled();
            separatorToUseInRobot = RedPlugin.getDefault().getPreferences().getSeparatorToUse(false);
            separatorToUseInTsv = RedPlugin.getDefault().getPreferences().getSeparatorToUse(true);
        }

        public String getSeparatorToFollow(final boolean isTsvFile) {
            return isTsvFile ? separatorToUseInTsv : separatorToUseInRobot;
        }

        public AcceptanceMode getAcceptanceMode() {
            return acceptanceMode;
        }

        public boolean isKeywordPrefixAutoAdditionEnabled() {
            return isKeywordPrefixAutoAdditionEnabled;
        }

    }
}
