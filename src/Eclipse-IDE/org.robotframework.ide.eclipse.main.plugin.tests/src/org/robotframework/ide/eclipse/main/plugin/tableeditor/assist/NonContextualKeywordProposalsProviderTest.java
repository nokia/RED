/*
 * Copyright 2019 Nokia Solutions and Networks
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
import org.robotframework.red.jface.assist.AssistantContext;
import org.robotframework.red.jface.assist.RedContentProposal;
import org.robotframework.red.junit.PreferenceUpdater;
import org.robotframework.red.junit.ProjectProvider;
import org.robotframework.red.junit.ShellProvider;
import org.robotframework.red.nattable.edit.AssistanceSupport.NatTableAssistantContext;

public class NonContextualKeywordProposalsProviderTest {

    @ClassRule
    public static ProjectProvider projectProvider = new ProjectProvider(NonContextualKeywordProposalsProviderTest.class);

    @Rule
    public ShellProvider shellProvider = new ShellProvider();

    @Rule
    public PreferenceUpdater preferenceUpdater = new PreferenceUpdater();

    private static RobotModel robotModel;

    @BeforeClass
    public static void beforeSuite() throws Exception {
        robotModel = RedPlugin.getModelManager().getModel();

        projectProvider.createFile("keywords_with_args_suite.robot",
                "*** Keywords ***",
                "kw_no_args",
                "kw_with_args",
                "  [Arguments]  ${arg1}  ${arg2}");
    }

    @AfterClass
    public static void afterSuite() {
        RedPlugin.getModelManager().dispose();
    }

    @Test
    public void thereAreNoProposalsProvided_whenThereIsNoKeywordMatchingCurrentInput() throws Exception {
        final RobotSuiteFile suiteFile = robotModel
                .createSuiteFile(projectProvider.getFile("keywords_with_args_suite.robot"));
        final NonContextualKeywordProposalsProvider provider = new NonContextualKeywordProposalsProvider(
                () -> suiteFile);

        final RedContentProposal[] proposals = provider.getProposals("foo", 1, null);
        assertThat(proposals).isEmpty();
    }

    @Test
    public void thereAreProposalsProvided_whenInputIsMatchingAndProperContentIsInserted() throws Exception {
        final Text text = new Text(shellProvider.getShell(), SWT.SINGLE);
        text.setText("foo");

        final RobotSuiteFile suiteFile = robotModel
                .createSuiteFile(projectProvider.getFile("keywords_with_args_suite.robot"));
        final NonContextualKeywordProposalsProvider provider = new NonContextualKeywordProposalsProvider(
                () -> suiteFile);

        final RedContentProposal[] proposals = provider.getProposals(text.getText(), 0, null);
        assertThat(proposals).hasSize(2);

        proposals[0].getModificationStrategy().insert(text, proposals[0]);
        assertThat(text.getText()).isEqualTo("kw_no_args");
    }

    @Test
    public void thereAreNoOperationsToPerformAfterAccepting() throws Exception {
        final RobotSuiteFile suiteFile = robotModel
                .createSuiteFile(projectProvider.getFile("keywords_with_args_suite.robot"));
        final NonContextualKeywordProposalsProvider provider = new NonContextualKeywordProposalsProvider(
                () -> suiteFile);

        final AssistantContext context = new NatTableAssistantContext(1, 3);
        final RedContentProposal[] proposals = provider.getProposals("kw", 2, context);
        assertThat(proposals).hasSize(2);

        assertThat(proposals[0].getLabel()).isEqualTo("kw_no_args - keywords_with_args_suite.robot");
        assertThat(proposals[0].getOperationsToPerformAfterAccepting()).isEmpty();
        assertThat(proposals[1].getLabel()).isEqualTo("kw_with_args - keywords_with_args_suite.robot");
        assertThat(proposals[1].getOperationsToPerformAfterAccepting()).isEmpty();
    }

}
