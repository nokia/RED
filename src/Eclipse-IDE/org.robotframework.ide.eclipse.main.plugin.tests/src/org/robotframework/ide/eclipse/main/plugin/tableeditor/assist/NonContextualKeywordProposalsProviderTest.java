/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.assist;

import static org.assertj.core.api.Assertions.assertThat;
import static org.robotframework.red.junit.jupiter.ProjectExtension.createFile;
import static org.robotframework.red.junit.jupiter.ProjectExtension.getFile;

import org.eclipse.core.resources.IProject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.red.jface.assist.RedContentProposal;
import org.robotframework.red.junit.jupiter.FreshShell;
import org.robotframework.red.junit.jupiter.FreshShellExtension;
import org.robotframework.red.junit.jupiter.Project;
import org.robotframework.red.junit.jupiter.ProjectExtension;

@ExtendWith({ ProjectExtension.class, FreshShellExtension.class })
public class NonContextualKeywordProposalsProviderTest {

    @Project
    static IProject project;

    @FreshShell
    Shell shell;

    private static RobotModel robotModel;

    @BeforeAll
    public static void beforeSuite() throws Exception {
        robotModel = RedPlugin.getModelManager().getModel();

        createFile(project, "keywords_with_args_suite.robot",
                "*** Keywords ***",
                "kw_no_args",
                "kw_with_args",
                "  [Arguments]  ${arg1}  ${arg2}");
    }

    @AfterAll
    public static void afterSuite() {
        RedPlugin.getModelManager().dispose();
    }

    @Test
    public void thereAreNoProposalsProvided_whenThereIsNoKeywordMatchingCurrentInput() throws Exception {
        final RobotSuiteFile suiteFile = robotModel
                .createSuiteFile(getFile(project, "keywords_with_args_suite.robot"));
        final NonContextualKeywordProposalsProvider provider = new NonContextualKeywordProposalsProvider(
                () -> suiteFile);

        final RedContentProposal[] proposals = provider.computeProposals("foo", 1, null);
        assertThat(proposals).isEmpty();
    }

    @Test
    public void thereAreProposalsProvided_whenInputIsMatchingAndProperContentIsInserted() throws Exception {
        final Text text = new Text(shell, SWT.SINGLE);
        text.setText("foo");

        final RobotSuiteFile suiteFile = robotModel
                .createSuiteFile(getFile(project, "keywords_with_args_suite.robot"));
        final NonContextualKeywordProposalsProvider provider = new NonContextualKeywordProposalsProvider(
                () -> suiteFile);

        final RedContentProposal[] proposals = provider.computeProposals(text.getText(), 0, null);
        assertThat(proposals).hasSize(2);

        proposals[0].getModificationStrategy().insert(text, proposals[0]);
        assertThat(text.getText()).isEqualTo("kw_no_args");
    }

    @Test
    public void thereAreNoOperationsToPerformAfterAccepting() throws Exception {
        final RobotSuiteFile suiteFile = robotModel
                .createSuiteFile(getFile(project, "keywords_with_args_suite.robot"));
        final NonContextualKeywordProposalsProvider provider = new NonContextualKeywordProposalsProvider(
                () -> suiteFile);

        final RedContentProposal[] proposals = provider.computeProposals("kw", 2, null);
        assertThat(proposals).hasSize(2);

        assertThat(proposals[0].getLabel()).isEqualTo("kw_no_args - keywords_with_args_suite.robot");
        assertThat(proposals[0].getOperationsToPerformAfterAccepting()).isEmpty();
        assertThat(proposals[1].getLabel()).isEqualTo("kw_with_args - keywords_with_args_suite.robot");
        assertThat(proposals[1].getOperationsToPerformAfterAccepting()).isEmpty();
    }

}
