/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.assist;

import static com.google.common.collect.Iterables.transform;
import static org.assertj.core.api.Assertions.assertThat;
import static org.robotframework.ide.eclipse.main.plugin.assist.Commons.substringMatcher;
import static org.robotframework.ide.eclipse.main.plugin.assist.Commons.toLabels;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.rf.ide.core.project.RobotProjectConfig.ReferencedLibrary;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.project.library.LibrarySpecification;
import org.robotframework.red.junit.ProjectProvider;

public class RedLibraryProposalsTest {

    @ClassRule
    public static ProjectProvider projectProvider = new ProjectProvider(RedLibraryProposalsTest.class);
    private static RobotSuiteFile model;

    @BeforeClass
    public static void beforeSuite() throws Exception {
        final RobotModel robotModel = new RobotModel();

        final RobotProject robotProject = robotModel.createRobotProject(projectProvider.getProject());
        robotProject.setStandardLibraries(createStandardLibraries());
        robotProject.setReferencedLibraries(createReferencedLibraries());

        final IFile file = projectProvider.createFile("file.robot", "");
        model = robotModel.createSuiteFile(file);
    }

    private static Map<String, LibrarySpecification> createStandardLibraries() {
        final LibrarySpecification stdLib = new LibrarySpecification();
        stdLib.setName("StdLib");
        final Map<String, LibrarySpecification> stdLibs = new HashMap<>();
        stdLibs.put(stdLib.getName(), stdLib);
        return stdLibs;
    }

    private static Map<ReferencedLibrary, LibrarySpecification> createReferencedLibraries() {
        final ReferencedLibrary refTestLib = new ReferencedLibrary();
        refTestLib.setName("TestLib");
        final LibrarySpecification testLib = new LibrarySpecification();
        testLib.setName("TestLib");
        final Map<ReferencedLibrary, LibrarySpecification> refLibs = new HashMap<>();
        refLibs.put(refTestLib, testLib);
        return refLibs;
    }
    
    @AfterClass
    public static void afterSuite() {
        model.dispose();
        model = null;
    }

    @Test
    public void noLibraryProposalsProvided_whenNoResourceIsMatchingToGivenPrefix() {
        final RedLibraryProposals proposalsProvider = new RedLibraryProposals(model);

        assertThat(proposalsProvider.getLibrariesProposals("unknown")).isEmpty();
    }

    @Test
    public void onlyLibraryProposalsMatchingPrefixAreProvided_whenPrefixIsGivenAndDefaultMatcherIsUsed() {
        final RedLibraryProposals proposalsProvider = new RedLibraryProposals(model);

        final List<? extends AssistProposal> proposals = proposalsProvider.getLibrariesProposals("Std");
        assertThat(transform(proposals, toLabels())).containsExactly("StdLib");
    }

    @Test
    public void onlyLibraryProposalsMatchingInputAreProvided_whenCustomMatcherIsUsed() {
        final RedLibraryProposals proposalsProvider = new RedLibraryProposals(model, substringMatcher());

        final List<? extends AssistProposal> proposals = proposalsProvider.getLibrariesProposals("dl");
        assertThat(transform(proposals, toLabels())).containsExactly("StdLib");
    }

    @Test
    public void allLibraryProposalsAreProvided_whenPrefixIsEmptyAndDefaultMatcherIsUsed() {
        final RedLibraryProposals proposalsProvider = new RedLibraryProposals(model);

        final List<? extends AssistProposal> proposals = proposalsProvider.getLibrariesProposals("");
        assertThat(transform(proposals, toLabels())).containsExactly("StdLib", "TestLib");
    }

    @Test
    public void allLibraryProposalsAreProvidedInOrderInducedByComparator_whenCustomComparatorIsUsed() {
        final RedLibraryProposals proposalsProvider = new RedLibraryProposals(model);

        final Comparator<? super RedLibraryProposal> comparator = new Comparator<RedLibraryProposal>() {

            @Override
            public int compare(final RedLibraryProposal p1, final RedLibraryProposal p2) {
                if (p1.equals(p2)) {
                    return 0;
                } else if (p1.getLabel().contains("TestLib")) {
                    return -1;
                } else {
                    return 1;
                }
            }
        };
        final List<? extends AssistProposal> proposals = proposalsProvider.getLibrariesProposals("", comparator);
        assertThat(transform(proposals, toLabels())).containsExactly("TestLib", "StdLib");
    }

}
