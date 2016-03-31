/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch.tabs;

import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.robotframework.red.junit.ProjectProvider;

import com.google.common.base.Function;

public class TagsProposalsSupportTest {

    @ClassRule
    public static ProjectProvider projectProvider = new ProjectProvider();

    @BeforeClass
    public static void before() throws Exception {
        createProject();
    }

    @Test
    public void whenThereAreNoResourcesSpecified_tagsFromWholeProjectAreProvided() {
        final TagsProposalsSupport support = new TagsProposalsSupport();
        assertThat(support.getProposals("")).isEmpty();

        support.switchTo("A", new HashMap<IResource, List<String>>());

        assertThat(transform(support.getProposals(""), toContents())).containsOnly("tag1", "tag2", "tag3", "tag4",
                "tag5");
        assertThat(transform(support.getProposals("3"), toContents())).containsOnly("tag3");
    }
    
    @Test
    public void whenSwitchingToNonExistingProject_noProposalAreProvided_1() {
        final TagsProposalsSupport support = new TagsProposalsSupport();
        support.switchTo("A", new HashMap<IResource, List<String>>());

        support.switchTo("", new HashMap<IResource, List<String>>());
        assertThat(support.getProposals("")).isEmpty();
    }

    @Test
    public void whenSwitchingToNonExistingProject_noProposalAreProvided_2() {
        final TagsProposalsSupport support = new TagsProposalsSupport();
        support.switchTo("A", new HashMap<IResource, List<String>>());

        support.switchTo("B", new HashMap<IResource, List<String>>());
        assertThat(support.getProposals("")).isEmpty();
    }

    @Test
    public void whenSupportIsSwitched_proposalsWouldChangeAccordingly_1() {
        final TagsProposalsSupport support = new TagsProposalsSupport();

        support.switchTo("A", new HashMap<IResource, List<String>>());

        final HashMap<IResource, List<String>> suites = new HashMap<IResource, List<String>>();
        suites.put(projectProvider.getDir(Path.fromPortableString("suites")), new ArrayList<String>());
        support.switchTo("A", suites);

        assertThat(transform(support.getProposals(""), toContents())).containsOnly("tag1", "tag2", "tag3", "tag4");
    }

    @Test
    public void whenSupportIsSwitched_proposalsWouldChangeAccordingly_2() {
        final TagsProposalsSupport support = new TagsProposalsSupport();

        support.switchTo("A", new HashMap<IResource, List<String>>());

        final HashMap<IResource, List<String>> suites = new HashMap<IResource, List<String>>();
        suites.put(projectProvider.getFile(Path.fromPortableString("suites/s1.robot")), new ArrayList<String>());
        suites.put(projectProvider.getFile(Path.fromPortableString("suites/s2.robot")), new ArrayList<String>());
        support.switchTo("A", suites);

        assertThat(transform(support.getProposals(""), toContents())).containsOnly("tag1", "tag2", "tag3", "tag4");
    }

    @Test
    public void whenSupportIsSwitched_proposalsWouldChangeAccordingly_3() {
        final TagsProposalsSupport support = new TagsProposalsSupport();

        support.switchTo("A", new HashMap<IResource, List<String>>());

        final HashMap<IResource, List<String>> suites = new HashMap<IResource, List<String>>();
        suites.put(projectProvider.getFile(Path.fromPortableString("suites/s1.robot")), new ArrayList<String>());
        support.switchTo("A", suites);

        assertThat(transform(support.getProposals(""), toContents())).containsOnly("tag1", "tag2", "tag3");
    }

    @Test
    public void whenSupportIsSwitched_proposalsWouldChangeAccordingly_4() {
        final TagsProposalsSupport support = new TagsProposalsSupport();

        support.switchTo("A", new HashMap<IResource, List<String>>());

        final HashMap<IResource, List<String>> suites = new HashMap<IResource, List<String>>();
        suites.put(projectProvider.getFile(Path.fromPortableString("s3.robot")), newArrayList("case4"));
        support.switchTo("A", suites);

        assertThat(transform(support.getProposals(""), toContents())).containsOnly("tag4");
    }

    private static void createProject() throws Exception {
        projectProvider.create("A");
        projectProvider.createDir(Path.fromPortableString("suites"));
        projectProvider.createFile(Path.fromPortableString("suites/s1.robot"), 
                "*** Settings ***",
                "Documentation  docu",
                "Force Tags  tag1", 
                "Default Tags  tag2", 
                "*** Test Cases ***", 
                "case1", 
                "  [Tags]  tag3",
                "  Log  10");
        projectProvider.createFile(Path.fromPortableString("suites/s2.robot"), 
                "*** Test Cases ***", 
                "case2", 
                "  [Tags]  tag4", 
                "  Log  10");
        projectProvider.createFile(Path.fromPortableString("s3.robot"), 
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

    private static Function<IContentProposal, String> toContents() {
        return new Function<IContentProposal, String>() {
            @Override
            public String apply(final IContentProposal proposal) {
                return proposal.getContent();
            }
        };
    }
}
