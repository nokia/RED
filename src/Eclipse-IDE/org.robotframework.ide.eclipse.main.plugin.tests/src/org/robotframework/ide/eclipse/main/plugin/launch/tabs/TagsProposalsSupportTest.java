/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch.tabs;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.robotframework.red.junit.jupiter.ProjectExtension.createFile;
import static org.robotframework.red.junit.jupiter.ProjectExtension.getDir;
import static org.robotframework.red.junit.jupiter.ProjectExtension.getFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.robotframework.red.junit.jupiter.Project;
import org.robotframework.red.junit.jupiter.ProjectExtension;

@ExtendWith(ProjectExtension.class)
public class TagsProposalsSupportTest {

    @Project(dirs = { "suites" })
    static IProject project;

    @BeforeAll
    public static void before() throws Exception {
        createFile(project, "suites/s1.robot",
                "*** Settings ***",
                "Documentation  docu",
                "Force Tags  tag1",
                "Default Tags  tag2",
                "*** Test Cases ***",
                "case1",
                "  [Tags]  tag3",
                "  Log  10");
        createFile(project, "suites/s2.robot",
                "*** Test Cases ***",
                "case2",
                "  [Tags]  tag4",
                "  Log  10");
        createFile(project, "s3.robot",
                "*** Test Cases ***",
                "case3",
                "  [Tags]  tag5",
                "  Log  10",
                "case4",
                "  [Tags]  tag4",
                "  Log 10",
                "case5",
                "  Log 10");
    }

    @Test
    public void whenThereAreNoResourcesSpecified_tagsFromWholeProjectAreProvided() {
        final TagsProposalsSupport support = new TagsProposalsSupport();
        assertThat(support.getProposals("")).isEmpty();

        support.switchTo(project.getName(), new HashMap<>());

        assertThat(support.getProposals("")).extracting(IContentProposal::getContent)
                .containsOnly("tag1", "tag2", "tag3", "tag4", "tag5");
        assertThat(support.getProposals("3")).extracting(IContentProposal::getContent).containsOnly("tag3");
    }

    @Test
    public void whenSwitchingToNonExistingProject_noProposalAreProvided_1() {
        final TagsProposalsSupport support = new TagsProposalsSupport();
        support.switchTo(project.getName(), new HashMap<>());

        support.switchTo("", new HashMap<>());
        assertThat(support.getProposals("")).isEmpty();
    }

    @Test
    public void whenSwitchingToNonExistingProject_noProposalAreProvided_2() {
        final TagsProposalsSupport support = new TagsProposalsSupport();
        support.switchTo(project.getName(), new HashMap<>());

        support.switchTo("B", new HashMap<>());
        assertThat(support.getProposals("")).isEmpty();
    }

    @Test
    public void whenSupportIsSwitched_proposalsWouldChangeAccordingly_1() {
        final TagsProposalsSupport support = new TagsProposalsSupport();

        support.switchTo(project.getName(), new HashMap<>());

        final HashMap<IResource, List<String>> suites = new HashMap<>();
        suites.put(getDir(project, "suites"), new ArrayList<>());
        support.switchTo(project.getName(), suites);

        assertThat(support.getProposals("")).extracting(IContentProposal::getContent)
                .containsOnly("tag1", "tag2", "tag3", "tag4");
    }

    @Test
    public void whenSupportIsSwitched_proposalsWouldChangeAccordingly_2() {
        final TagsProposalsSupport support = new TagsProposalsSupport();

        support.switchTo(project.getName(), new HashMap<>());

        final HashMap<IResource, List<String>> suites = new HashMap<>();
        suites.put(getFile(project, "suites/s1.robot"), new ArrayList<>());
        suites.put(getFile(project, "suites/s2.robot"), new ArrayList<>());
        support.switchTo(project.getName(), suites);

        assertThat(support.getProposals("")).extracting(IContentProposal::getContent)
                .containsOnly("tag1", "tag2", "tag3", "tag4");
    }

    @Test
    public void whenSupportIsSwitched_proposalsWouldChangeAccordingly_3() {
        final TagsProposalsSupport support = new TagsProposalsSupport();

        support.switchTo(project.getName(), new HashMap<>());

        final HashMap<IResource, List<String>> suites = new HashMap<>();
        suites.put(getFile(project, "suites/s1.robot"), new ArrayList<>());
        support.switchTo(project.getName(), suites);

        assertThat(support.getProposals("")).extracting(IContentProposal::getContent)
                .containsOnly("tag1", "tag2", "tag3");
    }

    @Test
    public void whenSupportIsSwitched_proposalsWouldChangeAccordingly_4() {
        final TagsProposalsSupport support = new TagsProposalsSupport();

        support.switchTo(project.getName(), new HashMap<>());

        final HashMap<IResource, List<String>> suites1 = new HashMap<>();
        suites1.put(getFile(project, "s3.robot"), newArrayList("case4"));
        support.switchTo(project.getName(), suites1);

        assertThat(support.getProposals("")).extracting(IContentProposal::getContent).containsOnly("tag4");

        final HashMap<IResource, List<String>> suites2 = new HashMap<>();
        suites2.put(getFile(project, "s3.robot"), newArrayList("case3"));
        support.switchTo(project.getName(), suites2);

        assertThat(support.getProposals("")).extracting(IContentProposal::getContent).containsOnly("tag5");
    }
}
