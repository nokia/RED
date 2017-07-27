/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.assist;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Text;
import org.junit.Rule;
import org.junit.Test;
import org.rf.ide.core.project.RobotProjectConfig.ReferencedLibrary;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.project.library.Libraries;
import org.robotframework.ide.eclipse.main.plugin.project.library.LibrarySpecification;
import org.robotframework.red.jface.assist.RedContentProposal;
import org.robotframework.red.junit.PreferenceUpdater;
import org.robotframework.red.junit.ProjectProvider;
import org.robotframework.red.junit.ShellProvider;

public class KeywordProposalsProviderTest {

    @Rule
    public ProjectProvider projectProvider = new ProjectProvider(KeywordProposalsProviderTest.class);

    @Rule
    public ShellProvider shellProvider = new ShellProvider();

    @Rule
    public PreferenceUpdater preferenceUpdater = new PreferenceUpdater();

    @Test
    public void thereAreNoProposalsProvided_whenThereIsNoKeywordMatchingCurrentInput() throws Exception {
        final IFile file = projectProvider.createFile("file.robot",
                "*** Keywords ***",
                "kw1",
                "kw2");
        final RobotSuiteFile suiteFile = RedPlugin.getModelManager().createSuiteFile(file);
        final KeywordProposalsProvider provider = new KeywordProposalsProvider(suiteFile);

        final RedContentProposal[] proposals = provider.getProposals("foo", 1, null);
        assertThat(proposals).isEmpty();
    }

    @Test
    public void thereAreProposalsProvided_whenInputIsMatchingAndProperContentIsInserted() throws Exception {
        final Text text = new Text(shellProvider.getShell(), SWT.SINGLE);
        text.setText("foo");

        final IFile file = projectProvider.createFile("file.robot",
                "*** Keywords ***",
                "kw1",
                "kw2");
        final RobotSuiteFile suiteFile = RedPlugin.getModelManager().createSuiteFile(file);
        final KeywordProposalsProvider provider = new KeywordProposalsProvider(suiteFile);

        final RedContentProposal[] proposals = provider.getProposals(text.getText(), 0, null);
        assertThat(proposals).hasSize(2);

        proposals[0].getModificationStrategy().insert(text, proposals[0]);
        assertThat(text.getText()).isEqualTo("kw1");
    }

    @Test
    public void thereAreOperationsToPerformAfterAccepting_onlyForNotAccessibleKeywordProposals() throws Exception {
        preferenceUpdater.setValue(RedPreferences.ASSISTANT_KEYWORD_FROM_NOT_IMPORTED_LIBRARY_ENABLED, true);

        final Map<ReferencedLibrary, LibrarySpecification> refLibs = new LinkedHashMap<>();
        refLibs.putAll(Libraries.createRefLib("LibImported", "kw1", "kw2"));
        refLibs.putAll(Libraries.createRefLib("LibNotImported", "kw3", "kw4"));

        final RobotProject project = RedPlugin.getModelManager().createProject(projectProvider.getProject());
        project.setReferencedLibraries(refLibs);

        final IFile file = projectProvider.createFile("file.robot",
                "*** Settings ***",
                "Library  LibImported");
        final RobotSuiteFile suiteFile = RedPlugin.getModelManager().createSuiteFile(file);
        final KeywordProposalsProvider provider = new KeywordProposalsProvider(suiteFile);

        final RedContentProposal[] proposals = provider.getProposals("kw", 2, null);
        assertThat(proposals).hasSize(4);

        assertThat(proposals[0].getLabel()).isEqualTo("kw1 - LibImported");
        assertThat(proposals[0].getOperationsToPerformAfterAccepting()).isEmpty();
        assertThat(proposals[1].getLabel()).isEqualTo("kw2 - LibImported");
        assertThat(proposals[1].getOperationsToPerformAfterAccepting()).isEmpty();
        assertThat(proposals[2].getLabel()).isEqualTo("kw3 - LibNotImported");
        assertThat(proposals[2].getOperationsToPerformAfterAccepting()).hasSize(1);
        assertThat(proposals[3].getLabel()).isEqualTo("kw4 - LibNotImported");
        assertThat(proposals[3].getOperationsToPerformAfterAccepting()).hasSize(1);
    }

}
