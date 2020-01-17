/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.assist;

import static org.assertj.core.api.Assertions.assertThat;
import static org.robotframework.red.junit.jupiter.ProjectExtension.createFile;
import static org.robotframework.red.junit.jupiter.ProjectExtension.getFile;

import java.util.List;

import org.eclipse.core.resources.IProject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.red.junit.jupiter.Project;
import org.robotframework.red.junit.jupiter.ProjectExtension;

@ExtendWith(ProjectExtension.class)
public class RedTemplateArgumentsProposalsTest {

    @Project
    static IProject project;

    private static RobotModel robotModel;

    @BeforeAll
    public static void beforeSuite() throws Exception {
        robotModel = new RobotModel();
        createFile(project, "file.robot",
                "*** Keywords ***",
                "Simple Keyword Name",
                "  [Arguments]  ${a1}  ${a2}  ${a3}",
                "  Log Many  ${a1}  ${a2}  ${a3}",
                "Embedded ${x1} Keyword ${x2} Name",
                "  Log Many  ${x1}  ${x2}");
    }

    @Test
    public void noProposalsAreProvided_forNotExistingKeyword() throws Exception {
        final RobotSuiteFile suiteFile = robotModel.createSuiteFile(getFile(project, "file.robot"));

        final RedTemplateArgumentsProposals provider = new RedTemplateArgumentsProposals(suiteFile);
        final List<RedTemplateArgumentsProposal> proposals = provider
                .getRedTemplateArgumentsProposal("Non Existing Keyword");

        assertThat(proposals).isEmpty();
    }

    @Test
    public void proposalsAreProvided_forExistingKeyword() throws Exception {
        final RobotSuiteFile suiteFile = robotModel.createSuiteFile(getFile(project, "file.robot"));

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
        final RobotSuiteFile suiteFile = robotModel.createSuiteFile(getFile(project, "file.robot"));

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
        final RobotSuiteFile suiteFile = robotModel.createSuiteFile(getFile(project, "file.robot"));

        final RedTemplateArgumentsProposals provider = new RedTemplateArgumentsProposals(suiteFile);
        final List<RedTemplateArgumentsProposal> proposals = provider
                .getRedTemplateArgumentsProposal("Other ${a} Embedded ${b} Keyword ${c} Name");

        assertThat(proposals).hasSize(1);
        assertThat(proposals.get(0).getLabel()).isEqualTo("Arguments for: Other ${a} Embedded ${b} Keyword ${c} Name");
        assertThat(proposals.get(0).getContent()).isEqualTo("a");
        assertThat(proposals.get(0).getArguments()).containsExactly("b", "c");
    }
}
