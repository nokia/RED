/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.assist;

import static org.assertj.core.api.Assertions.assertThat;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Text;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.red.jface.assist.RedContentProposal;
import org.robotframework.red.junit.ProjectProvider;
import org.robotframework.red.junit.ShellProvider;

public class ImportsInCodeProposalsProviderTest {

    @ClassRule
    public static ProjectProvider projectProvider = new ProjectProvider(ImportsInCodeProposalsProviderTest.class);

    @Rule
    public ShellProvider shellProvider = new ShellProvider();

    private static RobotModel robotModel;

    @BeforeClass
    public static void beforeSuite() throws Exception {
        robotModel = RedPlugin.getModelManager().getModel();

        projectProvider.createFile("suite.robot",
                "*** Settings ***",
                "Resource  ares.robot",
                "Resource  bres.robot");
    }

    @AfterClass
    public static void afterSuite() {
        RedPlugin.getModelManager().dispose();
    }

    @Test
    public void thereAreNoProposalsProvided_whenThereIsNoImportMatchingCurrentInput() throws Exception {
        final RobotSuiteFile suiteFile = robotModel.createSuiteFile(projectProvider.getFile("suite.robot"));
        final ImportsInCodeProposalsProvider provider = new ImportsInCodeProposalsProvider(suiteFile);

        final RedContentProposal[] proposals = provider.getProposals("foo", 1, null);
        assertThat(proposals).isEmpty();
    }

    @Test
    public void thereAreProposalsProvided_whenInputIsMatchingAndProperContentIsInserted() throws Exception {
        final Text text = new Text(shellProvider.getShell(), SWT.SINGLE);
        text.setText("abc");

        final RobotSuiteFile suiteFile = robotModel.createSuiteFile(projectProvider.getFile("suite.robot"));
        final ImportsInCodeProposalsProvider provider = new ImportsInCodeProposalsProvider(suiteFile);

        final RedContentProposal[] proposals = provider.getProposals(text.getText(), 1, null);
        assertThat(proposals).hasSize(1);

        proposals[0].getModificationStrategy().insert(text, proposals[0]);
        assertThat(text.getText()).isEqualTo("ares.");
    }
}
