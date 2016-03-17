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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.fieldassist.ContentProposal;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.eclipse.jface.fieldassist.TextContentAdapter;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.rf.ide.core.testdata.model.ModelType;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCasesSection;
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
public class TagsProposalsSupport {

    private static Map<String, String> proposals;
    
    private static IProject project;
    
    private static List<Control> textControls = new ArrayList<>();
 
    private TagsProposalsSupport() {
    }

    public static void install(final Control textField) {
        final ContentProposalAdapter adapter = new ContentProposalAdapter(textField, new TextContentAdapter(),
                new TagsContentProposalProvider(), null, null);
        adapter.setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_REPLACE);
        textControls.add(textField);
    }

    public static void extractTagProposalsFromSettingsTable(final RobotSuiteFile robotSuiteFile) {
        Optional<RobotSettingsSection> section = robotSuiteFile.findSection(RobotSettingsSection.class);
        if (section.isPresent()) {
            RobotSettingsSection settingsSection = section.get();
            for (RobotKeywordCall setting : settingsSection.getChildren()) {
                final ModelType settingType = setting.getLinkedElement().getModelType();
                if (settingType == ModelType.FORCE_TAGS_SETTING || settingType == ModelType.DEFAULT_TAGS_SETTING) {
                    addTagProposals(setting.getArguments(), "SETTINGS_TABLE_TAG");
                }
            }
        }
    }

    public static void extractTagProposalsFromTestCaseTable(final RobotElement testCasesElement, final String suitePath) {
        RobotDefinitionSetting tagsSetting = ((RobotCase) testCasesElement).getTagsSetting();
        if (tagsSetting != null) {
            addTagProposals(tagsSetting.getArguments(), suitePath);
        }
    }
    
    private static void extractTagProposalsFromProject(final IProject project) {
        final List<RobotSuiteFile> robotSuiteFiles = new ArrayList<>();
        try {
            extractRobotSuiteFiles(robotSuiteFiles, project.members());
        } catch (CoreException e) {
            e.printStackTrace();
        }

        for (RobotSuiteFile robotSuiteFile : robotSuiteFiles) {
            extractTagProposalsFromSettingsTable(robotSuiteFile);
            Optional<RobotCasesSection> testCasesSection = robotSuiteFile.findSection(RobotCasesSection.class);
            if (testCasesSection.isPresent()) {
                for (RobotElement testCasesElement : testCasesSection.get().getChildren()) {
                    if (testCasesElement instanceof RobotCase) {
                        extractTagProposalsFromTestCaseTable(testCasesElement, robotSuiteFile.getName());
                    }
                }
            }
        }
    }
    
    private static void addTagProposals(final List<String> tags, final String suitePath) {
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
        clearProposals();
        clearTagTextControls();
    }
    
    public static void clearProjectTagProposals() {
        if (project != null) {
            clearProposals();
        }
    }
    
    private static void clearProposals() {
        if (proposals != null) {
            proposals.clear();
        }
        project = null;
    }
    
    private static void clearTagTextControls() {
        final Iterator<Control> iter = textControls.iterator();
        while (iter.hasNext()) {
            Control control = iter.next();
            if (control.isDisposed()) {
                iter.remove();
            } else {
                ((Text) control).setText("");
            }
        }
    }
    
    private static void extractRobotSuiteFiles(final List<RobotSuiteFile> suiteList, final IResource[] members)
            throws CoreException {
        for (int i = 0; i < members.length; i++) {
            if (members[i] instanceof IFile
                    && (members[i].getFileExtension().equalsIgnoreCase("robot") || members[i].getFileExtension()
                            .equalsIgnoreCase("txt") || members[i].getFileExtension().equalsIgnoreCase("tsv"))) {
                final RobotSuiteFile robotSuiteFile = RedPlugin.getModelManager().createSuiteFile((IFile) members[i]);
                if (robotSuiteFile != null && robotSuiteFile.isSuiteFile()) {
                    suiteList.add(robotSuiteFile);
                }
            } else if (members[i] instanceof IFolder) {
                extractRobotSuiteFiles(suiteList, ((IFolder) members[i]).members());
            }
        }
    }
    
    public static void setProject(final IProject p) {
        project = p;
    }

    private static class TagsContentProposalProvider implements IContentProposalProvider {

        @Override
        public IContentProposal[] getProposals(final String contents, final int position) {

            if (project != null && (proposals == null || proposals.isEmpty())) {
                extractTagProposalsFromProject(project);
            }
                    
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
