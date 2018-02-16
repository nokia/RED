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
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;

/**
 * @author mmarzec
 *
 */
class TagsProposalsSupport {

    private final Multimap<IPath, SourcedTag> allTagsCache;

    private final Map<IResource, List<String>> currentSuitesToRun;

    private Optional<? extends Set<String>> proposals;

    TagsProposalsSupport() {
        this.allTagsCache = LinkedHashMultimap.create();
        this.currentSuitesToRun = new HashMap<IResource, List<String>>();
        this.proposals = Optional.empty();
    }

    void install(final Control textField) {
        final ContentProposalAdapter adapter = new ContentProposalAdapter(textField, new TextContentAdapter(),
                new TagsContentProposalProvider(), null, null);
        adapter.setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_REPLACE);
    }

    void switchTo(final String projectName, final Map<IResource, List<String>> suitesToRun) {
        this.proposals = Optional.empty();
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

            final String trimmedFieldContent = contents.trim().toLowerCase();
            final List<IContentProposal> filteredProposals = new ArrayList<>();
            for (final String proposal : proposals.get()) {
                if (proposal.toLowerCase().contains(trimmedFieldContent)) {
                    filteredProposals.add(new ContentProposal(proposal));
                }
            }
            return filteredProposals.toArray(new IContentProposal[0]);
        }

        private Collection<String> extractTagProposals() {
            final Multimap<IPath, SourcedTag> groupedTags = groupTagsBySuites();
            return groupedTags.values().stream().map(sourcedTag -> sourcedTag.tag).collect(Collectors.toList());
        }

        private Multimap<IPath, SourcedTag> groupTagsBySuites() {
            final Multimap<IPath, SourcedTag> groupedTags = LinkedHashMultimap.create();

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

                                    if (!allTagsCache.containsKey(groupingPath)) {
                                        allTagsCache.putAll(groupingPath, extractTagsFromSuite(suiteModel));
                                    }
                                    final Collection<SourcedTag> tags = allTagsCache.get(groupingPath);
                                    groupedTags.putAll(groupingPath, filterMatchingTags(tags, entry.getValue()));
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

        private Collection<SourcedTag> filterMatchingTags(final Collection<SourcedTag> tags,
                final Collection<String> testNames) {
            if (testNames.isEmpty()) {
                return tags;
            } else {
                final List<SourcedTag> filteredTags = new ArrayList<>();
                final List<String> sources = newArrayList(SourcedTag.SETTINGS_SOURCE);
                sources.addAll(testNames);

                for (final SourcedTag sourcedTag : tags) {
                    for (final String source : sources) {
                        if (source.equals(sourcedTag.source)) {
                            filteredTags.add(sourcedTag);
                        }
                    }
                }
                return filteredTags;
            }
        }

        private Collection<SourcedTag> extractTagsFromSuite(final RobotSuiteFile suiteModel) {
            final Collection<SourcedTag> tags = new ArrayList<>();
            tags.addAll(extractTagProposalsFromSettingsTable(suiteModel));
            tags.addAll(extractTagsFromCases(suiteModel));
            return tags;
        }

        private Collection<SourcedTag> extractTagProposalsFromSettingsTable(final RobotSuiteFile robotSuiteFile) {
            final Optional<RobotSettingsSection> section = robotSuiteFile.findSection(RobotSettingsSection.class);
            if (!section.isPresent()) {
                return new ArrayList<>();
            }
            final List<SourcedTag> tags = new ArrayList<>();
            final RobotSettingsSection settingsSection = section.get();
            for (final RobotKeywordCall setting : settingsSection.getChildren()) {
                final ModelType settingType = setting.getLinkedElement().getModelType();
                if (settingType == ModelType.FORCE_TAGS_SETTING || settingType == ModelType.DEFAULT_TAGS_SETTING) {
                    for (final String tag : setting.getArguments()) {
                        tags.add(new SourcedTag(SourcedTag.SETTINGS_SOURCE, tag));
                    }
                }
            }
            return tags;
        }

        private Collection<SourcedTag> extractTagsFromCases(final RobotSuiteFile suiteModel) {
            final Collection<RobotCase> cases = collectCases(suiteModel);
            final Collection<SourcedTag> tags = new ArrayList<>();
            for (final RobotCase testCase : cases) {
                tags.addAll(extractTagProposalsFromTestCase(testCase));
            }
            return tags;
        }

        private Collection<RobotCase> collectCases(final RobotSuiteFile suiteModel) {
            final Optional<RobotCasesSection> testCasesSection = suiteModel.findSection(RobotCasesSection.class);
            if (!testCasesSection.isPresent()) {
                return new ArrayList<>();
            }
            return newArrayList(testCasesSection.get().getChildren());
        }

        private Collection<SourcedTag> extractTagProposalsFromTestCase(final RobotCase testCases) {
            final List<RobotDefinitionSetting> tagsSettings = testCases.getTagsSetting();
            final List<SourcedTag> tags = new ArrayList<>();
            for (final RobotDefinitionSetting tagsSetting : tagsSettings) {
                if (tagsSetting.getArguments() != null) {
                    for (final String tag : tagsSetting.getArguments()) {
                        tags.add(new SourcedTag(testCases.getName().toLowerCase(), tag));
                    }
                }
            }
            return tags;
        }
    }

    private static final class SourcedTag {

        public final static String SETTINGS_SOURCE = "##SETTINGS##";

        private final String source;

        private final String tag;

        public SourcedTag(final String source, final String tag) {
            this.source = source;
            this.tag = tag;
        }
    }
}
