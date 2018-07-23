/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.assist;

import static com.google.common.collect.Iterables.transform;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.robotframework.ide.eclipse.main.plugin.assist.Commons.firstProposalContaining;
import static org.robotframework.ide.eclipse.main.plugin.assist.Commons.prefixesMatcher;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.eclipse.core.resources.IFile;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.rf.ide.core.executor.RobotRuntimeEnvironment;
import org.rf.ide.core.libraries.LibrarySpecification;
import org.rf.ide.core.libraries.SitePackagesLibraries;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.project.editor.libraries.Libraries;
import org.robotframework.red.junit.ProjectProvider;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.SetMultimap;

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

    @Test
    public void noSitePackagesLibraryProposalsProvided_whenAllLibrariesAreAlreadyImported() {
        final SitePackagesLibraries sitePackagesLibs = new SitePackagesLibraries(
                Arrays.asList(Arrays.asList("libOne"), Arrays.asList("libTwo", "libThree")));
        final SetMultimap<LibrarySpecification, Optional<String>> importedLibs = LinkedHashMultimap.create();
        final LibrarySpecification libOneSpec = new LibrarySpecification();
        final LibrarySpecification libTwoSpec = new LibrarySpecification();
        final LibrarySpecification libThreeSpec = new LibrarySpecification();
        libOneSpec.setName("libOne");
        libTwoSpec.setName("libTwo");
        libThreeSpec.setName("libThree");
        importedLibs.put(libOneSpec, Optional.of("libOne"));
        importedLibs.put(libTwoSpec, Optional.of("libTwo"));
        importedLibs.put(libThreeSpec, Optional.of("libThree"));

        final RobotRuntimeEnvironment env = mock(RobotRuntimeEnvironment.class);
        final RobotSuiteFile suiteFile = mock(RobotSuiteFile.class);
        when(env.getSitePackagesLibrariesNames()).thenReturn(sitePackagesLibs);
        when(suiteFile.getRuntimeEnvironment()).thenReturn(env);
        when(suiteFile.getImportedLibraries()).thenReturn(importedLibs);

        final RedLibraryProposals proposalsProvider = new RedLibraryProposals(suiteFile);
        final List<? extends AssistProposal> proposals = proposalsProvider.getSitePackagesLibrariesProposals("");
        assertThat(transform(proposals, AssistProposal::getLabel)).isEmpty();
    }

    @Test
    public void allSitePackagesLibraryProposalsAreProvided_whenAllLibrariesAreNotImported() {
        final SitePackagesLibraries sitePackagesLibs = new SitePackagesLibraries(
                Arrays.asList(Arrays.asList("libOne"), Arrays.asList("libTwo", "libThree")));
        final SetMultimap<LibrarySpecification, Optional<String>> importedLibs = LinkedHashMultimap.create();
        final RobotRuntimeEnvironment env = mock(RobotRuntimeEnvironment.class);
        final RobotSuiteFile suiteFile = mock(RobotSuiteFile.class);
        when(env.getSitePackagesLibrariesNames()).thenReturn(sitePackagesLibs);
        when(suiteFile.getRuntimeEnvironment()).thenReturn(env);
        when(suiteFile.getImportedLibraries()).thenReturn(importedLibs);

        final RedLibraryProposals proposalsProvider = new RedLibraryProposals(suiteFile);
        final List<? extends AssistProposal> proposals = proposalsProvider.getSitePackagesLibrariesProposals("");
        assertThat(transform(proposals, AssistProposal::getLabel)).containsExactly("libOne", "libThree", "libTwo");
    }
}
