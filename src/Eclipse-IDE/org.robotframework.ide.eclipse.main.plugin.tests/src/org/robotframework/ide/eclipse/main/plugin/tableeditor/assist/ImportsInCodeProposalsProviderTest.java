/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.assist;

import static org.assertj.core.api.Assertions.assertThat;

import org.eclipse.core.resources.IFile;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Text;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.red.jface.assist.RedContentProposal;
import org.robotframework.red.junit.ProjectProvider;
import org.robotframework.red.junit.ShellProvider;

public class ImportsInCodeProposalsProviderTest {

    @ClassRule
    public static final ProjectProvider projectProvider = new ProjectProvider(ImportsInCodeProposalsProviderTest.class);

    @Rule
    public ShellProvider shellProvider = new ShellProvider();

    @Test
    public void thereAreNoProposalsProvided_whenThereIsNoImportMatchingCurrentPrefix() throws Exception {
        final IFile file = projectProvider.createFile("file.robot",
                "*** Settings ***",
                "Resource  ares.robot",
                "Resource  bres.robot");
        final RobotSuiteFile suiteFile = RedPlugin.getModelManager().createSuiteFile(file);
        final ImportsInCodeProposalsProvider provider = new ImportsInCodeProposalsProvider(suiteFile);

        final RedContentProposal[] proposals = provider.getProposals("foo", 1, null);
        assertThat(proposals).isEmpty();
    }

    @Test
    public void thereAreProposalsProvided_whenPrefixIsMatchingAndProperContentIsInserted() throws Exception {
        final Text text = new Text(shellProvider.getShell(), SWT.SINGLE);
        text.setText("abc");

        final IFile file = projectProvider.createFile("file.robot",
                "*** Settings ***",
                "Resource  ares.robot",
                "Resource  bres.robot");
        final RobotSuiteFile suiteFile = RedPlugin.getModelManager().createSuiteFile(file);
        final ImportsInCodeProposalsProvider provider = new ImportsInCodeProposalsProvider(suiteFile);

        final RedContentProposal[] proposals = provider.getProposals(text.getText(), 1, null);
        assertThat(proposals).hasSize(1);

        proposals[0].getModificationStrategy().insert(text, proposals[0]);
        assertThat(text.getText()).isEqualTo("ares.");
    }
}
