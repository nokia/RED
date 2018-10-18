/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch.tabs;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.robotframework.red.junit.ProjectProvider;

public class TagsProposalsSupportTest {

    private static final String PROJECT_NAME = TagsProposalsSupportTest.class.getSimpleName();

    @ClassRule
    public static ProjectProvider projectProvider = new ProjectProvider(PROJECT_NAME);

    @BeforeClass
    public static void before() throws Exception {
        projectProvider.createDir("suites");
        projectProvider.createFile("suites/s1.robot",
                "*** Settings ***",
                "Documentation  docu",
                "Force Tags  tag1",
                "Default Tags  tag2",
                "*** Test Cases ***",
                "case1",
                "  [Tags]  tag3",
                "  Log  10");
        projectProvider.createFile("suites/s2.robot",
                "*** Test Cases ***",
                "case2",
                "  [Tags]  tag4",
                "  Log  10");
        projectProvider.createFile("s3.robot",
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

        support.switchTo(PROJECT_NAME, new HashMap<>());

        assertThat(support.getProposals("")).extracting(IContentProposal::getContent)
                .containsOnly("tag1", "tag2", "tag3", "tag4", "tag5");
        assertThat(support.getProposals("3")).extracting(IContentProposal::getContent).containsOnly("tag3");
    }

    @Test
    public void whenSwitchingToNonExistingProject_noProposalAreProvided_1() {
        final TagsProposalsSupport support = new TagsProposalsSupport();
        support.switchTo(PROJECT_NAME, new HashMap<>());

        support.switchTo("", new HashMap<>());
        assertThat(support.getProposals("")).isEmpty();
    }

    @Test
    public void whenSwitchingToNonExistingProject_noProposalAreProvided_2() {
        final TagsProposalsSupport support = new TagsProposalsSupport();
        support.switchTo(PROJECT_NAME, new HashMap<>());

        support.switchTo("B", new HashMap<>());
        assertThat(support.getProposals("")).isEmpty();
    }

    @Test
    public void whenSupportIsSwitched_proposalsWouldChangeAccordingly_1() {
        final TagsProposalsSupport support = new TagsProposalsSupport();

        support.switchTo(PROJECT_NAME, new HashMap<>());

        final HashMap<IResource, List<String>> suites = new HashMap<>();
        suites.put(projectProvider.getDir("suites"), new ArrayList<>());
        support.switchTo(PROJECT_NAME, suites);

        assertThat(support.getProposals("")).extracting(IContentProposal::getContent)
                .containsOnly("tag1", "tag2", "tag3", "tag4");
    }

    @Test
    public void whenSupportIsSwitched_proposalsWouldChangeAccordingly_2() {
        final TagsProposalsSupport support = new TagsProposalsSupport();

        support.switchTo(PROJECT_NAME, new HashMap<>());

        final HashMap<IResource, List<String>> suites = new HashMap<>();
        suites.put(projectProvider.getFile("suites/s1.robot"), new ArrayList<>());
        suites.put(projectProvider.getFile("suites/s2.robot"), new ArrayList<>());
        support.switchTo(PROJECT_NAME, suites);

        assertThat(support.getProposals("")).extracting(IContentProposal::getContent)
                .containsOnly("tag1", "tag2", "tag3", "tag4");
    }

    @Test
    public void whenSupportIsSwitched_proposalsWouldChangeAccordingly_3() {
        final TagsProposalsSupport support = new TagsProposalsSupport();

        support.switchTo(PROJECT_NAME, new HashMap<>());

        final HashMap<IResource, List<String>> suites = new HashMap<>();
        suites.put(projectProvider.getFile("suites/s1.robot"), new ArrayList<>());
        support.switchTo(PROJECT_NAME, suites);

        assertThat(support.getProposals("")).extracting(IContentProposal::getContent)
                .containsOnly("tag1", "tag2", "tag3");
    }

    @Test
    public void whenSupportIsSwitched_proposalsWouldChangeAccordingly_4() {
        final TagsProposalsSupport support = new TagsProposalsSupport();

        support.switchTo(PROJECT_NAME, new HashMap<>());

        final HashMap<IResource, List<String>> suites1 = new HashMap<>();
        suites1.put(projectProvider.getFile("s3.robot"), newArrayList("case4"));
        support.switchTo(PROJECT_NAME, suites1);

        assertThat(support.getProposals("")).extracting(IContentProposal::getContent).containsOnly("tag4");

        final HashMap<IResource, List<String>> suites2 = new HashMap<>();
        suites2.put(projectProvider.getFile("s3.robot"), newArrayList("case3"));
        support.switchTo(PROJECT_NAME, suites2);

        assertThat(support.getProposals("")).extracting(IContentProposal::getContent).containsOnly("tag5");
    }
}
