/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch.tabs;

import static com.google.common.collect.Lists.newArrayList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
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
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelManager;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSettingsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;

import com.google.common.base.Optional;

/**
 * @author mmarzec
 *
 */
class TagsProposalsSupport2 {

    private final Map<IResource, List<String>> suitesToRun;

    private final Set<String> proposals;

    TagsProposalsSupport2(final Map<IResource, List<String>> suitesToRun) {
        this.suitesToRun = suitesToRun;
        this.proposals = new LinkedHashSet<>();
    }

    void install(final Control textField) {
        final ContentProposalAdapter adapter = new ContentProposalAdapter(textField, new TextContentAdapter(),
                new TagsContentProposalProvider(), null, null);
        adapter.setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_REPLACE);
    }

    private void extractTagProposals() {
        final Map<RobotSuiteFile, List<RobotCase>> allSuites = new HashMap<>();
        
        for (final Entry<IResource, List<String>> entry : suitesToRun.entrySet()) {
            if (entry.getKey().getType() == IResource.FILE) {
                final RobotSuiteFile suiteModel = RobotModelManager.getInstance()
                        .createSuiteFile((IFile) entry.getKey());
                if (suiteModel != null && suiteModel.isSuiteFile()) {
                    allSuites.put(suiteModel, extractCases(suiteModel, entry.getValue()));
                }
            } else {
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
                                    allSuites.put(suiteModel, extractCases(suiteModel, new ArrayList<String>()));
                                }
                            }
                            return true;
                        }
                    });
                } catch (final CoreException e) {
                    // nothing
                }
            }
        }

        for (final Entry<RobotSuiteFile, List<RobotCase>> entry : allSuites.entrySet()) {
            extractTagProposalsFromSettingsTable(entry.getKey());
            for (final RobotCase test : entry.getValue()) {
                extractTagProposalsFromTestCase(test);
            }
        }
    }

    private List<RobotCase> extractCases(final RobotSuiteFile suiteModel, final List<String> caseNames) {
        final Optional<RobotCasesSection> testCasesSection = suiteModel.findSection(RobotCasesSection.class);
        if (testCasesSection.isPresent()) {
            if (caseNames.isEmpty()) {
                return newArrayList(testCasesSection.get().getChildren());
            }
            final List<RobotCase> cases = new ArrayList<>();
            for (final RobotCase testCase : testCasesSection.get().getChildren()) {
                if (caseNames.contains(testCase.getName().toLowerCase())) {
                    cases.add(testCase);
                }
            }
        }
        return new ArrayList<>();
    }

    private void extractTagProposalsFromSettingsTable(final RobotSuiteFile robotSuiteFile) {
        final Optional<RobotSettingsSection> section = robotSuiteFile.findSection(RobotSettingsSection.class);
        if (section.isPresent()) {
            final RobotSettingsSection settingsSection = section.get();
            for (final RobotKeywordCall setting : settingsSection.getChildren()) {
                final ModelType settingType = setting.getLinkedElement().getModelType();
                if (settingType == ModelType.FORCE_TAGS_SETTING || settingType == ModelType.DEFAULT_TAGS_SETTING) {
                    addTagProposals(setting.getArguments());
                }
            }
        }
    }

    private void extractTagProposalsFromTestCase(final RobotElement testCasesElement) {
        final RobotDefinitionSetting tagsSetting = ((RobotCase) testCasesElement).getTagsSetting();
        if (tagsSetting != null) {
            addTagProposals(tagsSetting.getArguments());
        }
    }
    
    private void addTagProposals(final List<String> tags) {
        if (tags == null) {
            return;
        }
        for (final String tag : tags) {
            proposals.add(tag);
        }
    }
    
    private class TagsContentProposalProvider implements IContentProposalProvider {

        @Override
        public IContentProposal[] getProposals(final String contents, final int position) {
            if (proposals.isEmpty()) {
                extractTagProposals();
            }
            if (proposals.isEmpty()) {
                return new IContentProposal[0];
            }

            final String trimmedFieldContent = contents.trim();

            final List<String> filteredProposals = new ArrayList<>();
            for (final String proposal : proposals) {
                if (proposal.toLowerCase().contains(trimmedFieldContent.toLowerCase())) {
                    filteredProposals.add(proposal);
                }
            }

            final IContentProposal[] contentProposals = new IContentProposal[filteredProposals.size()];
            for (int i = 0; i < filteredProposals.size(); i++) {
                contentProposals[i] = new ContentProposal(filteredProposals.get(i));
            }
            return contentProposals;
        }

    }
}
