/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.assist;

import static com.google.common.collect.Iterables.transform;
import static org.assertj.core.api.Assertions.assertThat;
import static org.robotframework.ide.eclipse.main.plugin.assist.Commons.firstProposalContaining;
import static org.robotframework.ide.eclipse.main.plugin.assist.Commons.prefixesMatcher;

import java.util.Comparator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.project.editor.libraries.Libraries;
import org.robotframework.red.junit.ProjectProvider;

public class RedLibraryProposalsTest {

    @ClassRule
    public static ProjectProvider projectProvider = new ProjectProvider(RedLibraryProposalsTest.class);

    private static RobotSuiteFile suiteFile;

    @BeforeClass
    public static void beforeSuite() throws Exception {
        final RobotModel robotModel = new RobotModel();

        final RobotProject robotProject = robotModel.createRobotProject(projectProvider.getProject());
        robotProject.setStandardLibraries(Libraries.createStdLibs("StdLib"));
        robotProject.setReferencedLibraries(Libraries.createRefLibs("TestLib"));

        final IFile file = projectProvider.createFile("file.robot", "");
        suiteFile = robotModel.createSuiteFile(file);
    }

    @AfterClass
    public static void afterSuite() {
        suiteFile.dispose();
        suiteFile = null;
    }

    @Test
    public void noLibraryProposalsProvided_whenNoResourceIsMatchingToGivenInput() {
        final RedLibraryProposals proposalsProvider = new RedLibraryProposals(suiteFile);

        assertThat(proposalsProvider.getLibrariesProposals("unknown")).isEmpty();
    }

    @Test
    public void onlyLibraryProposalsMatchingInputAreProvided_whenDefaultMatcherIsUsed() {
        final RedLibraryProposals proposalsProvider = new RedLibraryProposals(suiteFile);

        final List<? extends AssistProposal> proposals = proposalsProvider.getLibrariesProposals("dl");
        assertThat(transform(proposals, AssistProposal::getLabel)).containsExactly("StdLib");
    }

    @Test
    public void onlyLibraryProposalsMatchingInputAreProvided_whenCustomMatcherIsUsed() {
        final RedLibraryProposals proposalsProvider = new RedLibraryProposals(suiteFile, prefixesMatcher());

        final List<? extends AssistProposal> proposals = proposalsProvider.getLibrariesProposals("Std");
        assertThat(transform(proposals, AssistProposal::getLabel)).containsExactly("StdLib");
    }

    @Test
    public void allLibraryProposalsAreProvided_whenInputIsEmpty() {
        final RedLibraryProposals proposalsProvider = new RedLibraryProposals(suiteFile);

        final List<? extends AssistProposal> proposals = proposalsProvider.getLibrariesProposals("");
        assertThat(transform(proposals, AssistProposal::getLabel)).containsExactly("StdLib", "TestLib");
    }

    @Test
    public void allLibraryProposalsAreProvidedInOrderInducedByGivenComparator_whenCustomComparatorIsProvided() {
        final RedLibraryProposals proposalsProvider = new RedLibraryProposals(suiteFile);

        final Comparator<? super RedLibraryProposal> comparator = firstProposalContaining("TestLib");
        final List<? extends AssistProposal> proposals = proposalsProvider.getLibrariesProposals("", comparator);
        assertThat(transform(proposals, AssistProposal::getLabel)).containsExactly("TestLib", "StdLib");
    }
}
