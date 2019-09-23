/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.assist;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.red.junit.ProjectProvider;

public class RedTemplateArgumentsProposalsTest {

    @ClassRule
    public static ProjectProvider projectProvider = new ProjectProvider(RedTemplateArgumentsProposalsTest.class);

    private static RobotModel robotModel;

    @BeforeClass
    public static void beforeSuite() throws Exception {
        robotModel = new RobotModel();
        projectProvider.createFile("file.robot",
                "*** Keywords ***",
                "Simple Keyword Name",
                "  [Arguments]  ${a1}  ${a2}  ${a3}",
                "  Log Many  ${a1}  ${a2}  ${a3}",
                "Embedded ${x1} Keyword ${x2} Name",
                "  Log Many  ${x1}  ${x2}");
    }

    @Test
    public void noProposalsAreProvided_forNotExistingKeyword() throws Exception {
        final RobotSuiteFile suiteFile = robotModel.createSuiteFile(projectProvider.getFile("file.robot"));

        final RedTemplateArgumentsProposals provider = new RedTemplateArgumentsProposals(suiteFile);
        final List<RedTemplateArgumentsProposal> proposals = provider
                .getRedTemplateArgumentsProposal("Non Existing Keyword");

        assertThat(proposals).isEmpty();
    }

    @Test
    public void proposalsAreProvided_forExistingKeyword() throws Exception {
        final RobotSuiteFile suiteFile = robotModel.createSuiteFile(projectProvider.getFile("file.robot"));

        final RedTemplateArgumentsProposals provider = new RedTemplateArgumentsProposals(suiteFile);
        final List<RedTemplateArgumentsProposal> proposals = provider
                .getRedTemplateArgumentsProposal("Simple Keyword Name");

        assertThat(proposals).hasSize(1);
        assertThat(proposals.get(0).getLabel()).isEqualTo("Arguments for: Simple Keyword Name");
        assertThat(proposals.get(0).getContent()).isEqualTo("a1");
        assertThat(proposals.get(0).getArguments()).containsExactly("a2", "a3");
    }

    @Test
    public void proposalsAreProvided_forExistingKeywordWithEmbeddedArguments() throws Exception {
        final RobotSuiteFile suiteFile = robotModel.createSuiteFile(projectProvider.getFile("file.robot"));

        final RedTemplateArgumentsProposals provider = new RedTemplateArgumentsProposals(suiteFile);
        final List<RedTemplateArgumentsProposal> proposals = provider
                .getRedTemplateArgumentsProposal("Embedded ${x1} Keyword ${x2} Name");

        assertThat(proposals).hasSize(1);
        assertThat(proposals.get(0).getLabel()).isEqualTo("Arguments for: Embedded ${x1} Keyword ${x2} Name");
        assertThat(proposals.get(0).getContent()).isEqualTo("x1");
        assertThat(proposals.get(0).getArguments()).containsExactly("x2");
    }

    @Test
    public void proposalsAreProvided_forNonExistingKeywordWithEmbeddedArguments() throws Exception {
        final RobotSuiteFile suiteFile = robotModel.createSuiteFile(projectProvider.getFile("file.robot"));

        final RedTemplateArgumentsProposals provider = new RedTemplateArgumentsProposals(suiteFile);
        final List<RedTemplateArgumentsProposal> proposals = provider
                .getRedTemplateArgumentsProposal("Other ${a} Embedded ${b} Keyword ${c} Name");

        assertThat(proposals).hasSize(1);
        assertThat(proposals.get(0).getLabel()).isEqualTo("Arguments for: Other ${a} Embedded ${b} Keyword ${c} Name");
        assertThat(proposals.get(0).getContent()).isEqualTo("a");
        assertThat(proposals.get(0).getArguments()).containsExactly("b", "c");
    }
}
