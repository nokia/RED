/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch.tabs;

import static com.google.common.collect.Lists.newArrayList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.fieldassist.ContentProposal;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.eclipse.jface.fieldassist.TextContentAdapter;
import org.eclipse.swt.widgets.Control;
import org.rf.ide.core.testdata.model.ModelType;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCasesSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotDefinitionSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSettingsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;

/**
 * @author mmarzec
 *
 */
class TagsProposalsSupport {

    private final Multimap<IPath, String> allTagsCache;

    private final Map<IResource, List<String>> currentSuitesToRun;

    private Optional<? extends Set<String>> proposals;

    TagsProposalsSupport() {
        this.allTagsCache = LinkedHashMultimap.create();
        this.currentSuitesToRun = new HashMap<IResource, List<String>>();
        this.proposals = Optional.absent();
    }

    void install(final Control textField) {
        final ContentProposalAdapter adapter = new ContentProposalAdapter(textField, new TextContentAdapter(),
                new TagsContentProposalProvider(), null, null);
        adapter.setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_REPLACE);
    }

    void switchTo(final String projectName, final Map<IResource, List<String>> suitesToRun) {
        this.proposals = Optional.absent();
        this.currentSuitesToRun.clear();
        this.currentSuitesToRun.putAll(suitesToRun);

        if (currentSuitesToRun.isEmpty() && !projectName.isEmpty()) {
            final IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
            if (project.exists() && project.isOpen()) {
                currentSuitesToRun.put(project, new ArrayList<String>());
            }
        }
    }
    
    @VisibleForTesting
    List<IContentProposal> getProposals(final String contents) {
        return newArrayList(new TagsContentProposalProvider().getProposals(contents, -1));
    }

    private class TagsContentProposalProvider implements IContentProposalProvider {

        @Override
        public IContentProposal[] getProposals(final String contents, final int position) {
            if (!proposals.isPresent()) {
                proposals = Optional.of(new LinkedHashSet<String>());
                proposals.get().addAll(extractTagProposals());
            }

            final String trimmedFieldContent = contents.trim();
            final List<IContentProposal> filteredProposals = new ArrayList<>();
            for (final String proposal : proposals.get()) {
                if (proposal.toLowerCase().contains(trimmedFieldContent.toLowerCase())) {
                    filteredProposals.add(new ContentProposal(proposal));
                }
            }
            return filteredProposals.toArray(new IContentProposal[0]);
        }

        private Collection<String> extractTagProposals() {
            final Multimap<IPath, String> groupedTags = groupTagsBySuites();

            final List<String> tags = new ArrayList<>();
            for (final Entry<IPath, String> entry : groupedTags.entries()) {
                final String tag = entry.getValue();
                allTagsCache.put(entry.getKey(), tag);
                tags.add(tag);
            }
            return tags;
        }

        private Multimap<IPath, String> groupTagsBySuites() {
            final Multimap<IPath, String> groupedTags = LinkedHashMultimap.create();

            for (final Entry<IResource, List<String>> entry : currentSuitesToRun.entrySet()) {
                try {
                    entry.getKey().accept(new IResourceVisitor() {

                        @Override
                        public boolean visit(final IResource resource) throws CoreException {
                            if (resource.getType() == IResource.FILE
                                    && (resource.getFileExtension().equalsIgnoreCase("robot")
                                            || resource.getFileExtension().equalsIgnoreCase("txt")
                                            || resource.getFileExtension().equalsIgnoreCase("tsv"))) {
                                final RobotSuiteFile suiteModel = RedPlugin.getModelManager()
                                        .createSuiteFile((IFile) resource);
                                if (suiteModel != null && suiteModel.isSuiteFile()) {
                                    final IPath groupingPath = suiteModel.getFile().getFullPath();
                                    if (allTagsCache.containsKey(groupingPath)) {
                                        groupedTags.putAll(groupingPath, allTagsCache.get(groupingPath));
                                    } else {
                                        groupedTags.putAll(groupingPath,
                                                extractTagsFromSuite(suiteModel, entry.getValue()));
                                    }
                                }
                            }
                            return true;
                        }
                    });
                } catch (final CoreException e) {
                    // nothing
                }
            }
            return groupedTags;
        }

        private Collection<String> extractTagsFromSuite(final RobotSuiteFile suiteModel, final List<String> caseNames) {
            final List<String> tags = new ArrayList<>();
            tags.addAll(extractTagProposalsFromSettingsTable(suiteModel));
            tags.addAll(extractTagsFromCases(suiteModel, caseNames));
            return tags;
        }

        private Collection<String> extractTagProposalsFromSettingsTable(final RobotSuiteFile robotSuiteFile) {
            final Optional<RobotSettingsSection> section = robotSuiteFile.findSection(RobotSettingsSection.class);
            if (!section.isPresent()) {
                return new ArrayList<>();
            }
            final List<String> tags = new ArrayList<>();
            final RobotSettingsSection settingsSection = section.get();
            for (final RobotKeywordCall setting : settingsSection.getChildren()) {
                final ModelType settingType = setting.getLinkedElement().getModelType();
                if (settingType == ModelType.FORCE_TAGS_SETTING || settingType == ModelType.DEFAULT_TAGS_SETTING) {
                    tags.addAll(setting.getArguments());
                }
            }
            return tags;
        }

        private Collection<String> extractTagsFromCases(final RobotSuiteFile suiteModel, final List<String> caseNames) {
            final Collection<RobotCase> cases = collectCases(suiteModel, caseNames);
            final List<String> tags = new ArrayList<>();
            for (final RobotCase testCase : cases) {
                tags.addAll(extractTagProposalsFromTestCase(testCase));
            }
            return tags;
        }

        private Collection<RobotCase> collectCases(final RobotSuiteFile suiteModel, final List<String> caseNames) {
            final Optional<RobotCasesSection> testCasesSection = suiteModel.findSection(RobotCasesSection.class);
            if (!testCasesSection.isPresent()) {
                return new ArrayList<>();
            }
            if (caseNames.isEmpty()) {
                return newArrayList(testCasesSection.get().getChildren());
            } else {
                return Collections2.filter(testCasesSection.get().getChildren(), new Predicate<RobotCase>() {
                    @Override
                    public boolean apply(final RobotCase test) {
                        return caseNames.contains(test.getName().toLowerCase());
                    }
                });
            }
        }

        private Collection<String> extractTagProposalsFromTestCase(final RobotCase testCasesElement) {
            final RobotDefinitionSetting tagsSetting = testCasesElement.getTagsSetting();
            if (tagsSetting != null && tagsSetting.getArguments() != null) {
                return newArrayList(tagsSetting.getArguments());
            }
            return new ArrayList<>();
        }
    }
}
