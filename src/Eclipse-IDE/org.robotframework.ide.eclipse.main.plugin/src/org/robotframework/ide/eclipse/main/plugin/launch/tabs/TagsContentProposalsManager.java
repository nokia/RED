/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch.tabs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.fieldassist.ContentProposal;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.eclipse.jface.fieldassist.TextContentAdapter;
import org.eclipse.swt.widgets.Control;
import org.robotframework.ide.core.testData.model.ModelType;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.model.RobotDefinitionSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSettingsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;

import com.google.common.base.Optional;

/**
 * @author mmarzec
 *
 */
public class TagsContentProposalsManager {

    private static Map<String, String> proposals;

    private TagsContentProposalsManager() {
    }

    public static void install(final Control textField) {
        final ContentProposalAdapter adapter = new ContentProposalAdapter(textField, new TextContentAdapter(),
                new TagsContentProposalProvider(), null, null);
        adapter.setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_REPLACE);

    }

    public static void extractTagProposalsFromSettingsTable(final RobotSuiteFile robotSuiteFile) {
        Optional<RobotSettingsSection> section = robotSuiteFile.findSection(RobotSettingsSection.class);
        if (section.isPresent()) {
            RobotSettingsSection settingsSection = section.get();
            for (RobotKeywordCall setting : settingsSection.getChildren()) {
                if (setting.getLinkedElement().getModelType() == ModelType.FORCE_TAGS_SETTING) {
                    addTagsProposal(setting.getArguments(), "SETTINGS_TABLE_TAG");
                }
            }
        }
    }

    public static void extractTagProposalsFromTestCaseTable(final RobotElement testCasesElement, final String suitePath) {
        RobotDefinitionSetting tagsSetting = ((RobotCase) testCasesElement).getTagsSetting();
        if (tagsSetting != null) {
            addTagsProposal(tagsSetting.getArguments(), suitePath);
        }
    }

    private static void addTagsProposal(final List<String> tags, final String suitePath) {
        if (proposals == null) {
            proposals = new HashMap<>();
        }
        if (tags != null) {
            for (String tag : tags) {
                proposals.put(tag, suitePath);
            }
        }
    }
    
    public static void removeTagsProposals(final List<String> suitesPathList) {
        if (proposals != null && !proposals.isEmpty() && !suitesPathList.isEmpty()) {
            Iterator<Map.Entry<String, String>> iter = proposals.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<String, String> entry = iter.next();
                if (suitesPathList.contains(entry.getValue())) {
                    iter.remove();
                }
            }
        }
    }

    public static void clearTagProposals() {
        if (proposals != null) {
            proposals.clear();
        }
    }

    private static class TagsContentProposalProvider implements IContentProposalProvider {

        @Override
        public IContentProposal[] getProposals(final String contents, final int position) {

            if (proposals == null || proposals.isEmpty()) {
                return new IContentProposal[0];
            }

            final String textFromField = contents.trim();

            List<String> filteredProposals = new ArrayList<>();
            if (!textFromField.equals("")) {
                for (String proposal : proposals.keySet()) {
                    if (proposal.toLowerCase().contains(textFromField.toLowerCase())) {
                        filteredProposals.add(proposal);
                    }
                }
            } else {
                filteredProposals.addAll(proposals.keySet());
            }

            IContentProposal[] contentProposals = new IContentProposal[filteredProposals.size()];
            for (int i = 0; i < filteredProposals.size(); i++) {
                contentProposals[i] = new ContentProposal(filteredProposals.get(i));
            }
            return contentProposals;
        }

    }
}
