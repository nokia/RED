/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.assist;

import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.robotframework.ide.eclipse.main.plugin.assist.Commons.substringMatcher;
import static org.robotframework.ide.eclipse.main.plugin.assist.Commons.toLabels;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.project.library.LibrarySpecification;
import org.robotframework.red.junit.ProjectProvider;

public class RedImportProposalsTest {

    @ClassRule
    public static ProjectProvider projectProvider = new ProjectProvider(RedImportProposalsTest.class);

    private RobotModel robotModel;

    @Before
    public void beforeTest() {
        robotModel = new RobotModel();

        final RobotProject robotProject = robotModel.createRobotProject(projectProvider.getProject());
        robotProject.setStandardLibraries(createStandardLibraries());
    }

    private static Map<String, LibrarySpecification> createStandardLibraries() {
        final Map<String, LibrarySpecification> stdLibs = new HashMap<>();
        for (final String libName : newArrayList("StdLib1", "StdLib2", "StdLib3")) {
            final LibrarySpecification stdLib = new LibrarySpecification();
            stdLib.setName(libName);

            stdLibs.put(stdLib.getName(), stdLib);
        }
        return stdLibs;
    }

    @Test
    public void noImportProposalsAreProvided_whenNothingIsImported_1() throws Exception {
        final IFile file = projectProvider.createFile("file.robot",
                "*** Test Cases ***");
        final RobotSuiteFile model = robotModel.createSuiteFile(file);

        final RedImportProposals proposalsProvider = new RedImportProposals(model);

        assertThat(proposalsProvider.getImportsProposals("")).isEmpty();

        file.delete(true, null);
    }

    @Test
    public void noImportProposalsAreProvided_whenNothingIsImported_2() throws Exception {
        final IFile file = projectProvider.createFile("file.robot",
                "*** Settings ***",
                "*** Test Cases ***");
        final RobotSuiteFile model = robotModel.createSuiteFile(file);

        final RedImportProposals proposalsProvider = new RedImportProposals(model);

        assertThat(proposalsProvider.getImportsProposals("")).isEmpty();

        file.delete(true, null);
    }

    @Test
    public void noImportProposalsAreProvided_whenNothingIsMatchingGivenPrefix() throws Exception {
        final IFile file = projectProvider.createFile("file.robot",
                "*** Settings ***",
                "Library  StdLib1",
                "Resource  res1.robot",
                "*** Test Cases ***");
        final RobotSuiteFile model = robotModel.createSuiteFile(file);

        final RedImportProposals proposalsProvider = new RedImportProposals(model);

        assertThat(proposalsProvider.getImportsProposals("unknown")).isEmpty();

        file.delete(true, null);
    }

    @Test
    public void onlyImportProposalsMatchingPrefixAreProvided_whenPrefixIsGivenAndDefaultMatcherIsUsed()
            throws Exception {
        final IFile file = projectProvider.createFile("file.robot",
                "*** Settings ***",
                "Library  StdLib1",
                "Library  RefLib1",
                "Resource  res1.robot",
                "Resource  other.robot",
                "*** Test Cases ***");
        final RobotSuiteFile model = robotModel.createSuiteFile(file);

        final RedImportProposals proposalsProvider = new RedImportProposals(model);

        final List<? extends AssistProposal> proposals = proposalsProvider.getImportsProposals("std");
        assertThat(transform(proposals, toLabels())).containsExactly("StdLib1");

        file.delete(true, null);
    }

    @Test
    public void onlyImportProposalsMatchingInputAreProvided_whenCustomMatcherIsUsed() throws Exception {
        final IFile file = projectProvider.createFile("file.robot",
                "*** Settings ***",
                "Library  StdLib1",
                "Library  RefLib1",
                "Resource  res1.robot",
                "Resource  other.robot",
                "*** Test Cases ***");
        final RobotSuiteFile model = robotModel.createSuiteFile(file);
        final RedImportProposals proposalsProvider = new RedImportProposals(model, substringMatcher());

        final List<? extends AssistProposal> proposals = proposalsProvider.getImportsProposals("1");
        assertThat(transform(proposals, toLabels())).containsExactly("StdLib1", "res1");

        file.delete(true, null);
    }

    @Test
    public void allImportProposalsAreProvided_whenPrefixIsEmptyAndDefaultMatcherIsUsed() throws Exception {
        final IFile file = projectProvider.createFile("file.robot",
                "*** Settings ***",
                "Library  StdLib1",
                "Library  RefLib1",
                "Resource  res1.robot",
                "Resource  other.robot",
                "*** Test Cases ***");
        final RobotSuiteFile model = robotModel.createSuiteFile(file);
        final RedImportProposals proposalsProvider = new RedImportProposals(model);

        final List<? extends AssistProposal> proposals = proposalsProvider.getImportsProposals("");
        assertThat(transform(proposals, toLabels())).containsExactly("StdLib1", "other", "res1");

        file.delete(true, null);
    }

    @Test
    public void allImportProposalsAreProvided_whenPrefixIsBddSyntaxEmptyAndDefaultMatcherIsUsed() throws Exception {
        final IFile file = projectProvider.createFile("file.robot",
                "*** Settings ***",
                "Library  StdLib1",
                "Library  RefLib1",
                "Resource  res1.robot",
                "Resource  other.robot",
                "*** Test Cases ***");
        final RobotSuiteFile model = robotModel.createSuiteFile(file);
        final RedImportProposals proposalsProvider = new RedImportProposals(model);

        for (final String bddPrefix : newArrayList("Given", "When", "And", "But", "Then")) {
            final List<? extends AssistProposal> proposals = proposalsProvider.getImportsProposals(bddPrefix + " ");
            assertThat(transform(proposals, toLabels())).containsExactly("StdLib1", "other", "res1");
        }

        file.delete(true, null);
    }

    @Test
    public void allImportProposalsAreProvidedInOrderInducedByComparator_whenCustomComparatorIsUsed()
            throws Exception {
        final IFile file = projectProvider.createFile("file.robot",
                "*** Settings ***",
                "Library  StdLib1",
                "Library  RefLib1",
                "Resource  res1.robot",
                "Resource  other.robot",
                "*** Test Cases ***");
        final RobotSuiteFile model = robotModel.createSuiteFile(file);
        final RedImportProposals proposalsProvider = new RedImportProposals(model, substringMatcher());

        final Comparator<? super RedImportProposal> comparator = new Comparator<RedImportProposal>() {

            @Override
            public int compare(final RedImportProposal p1, final RedImportProposal p2) {
                if (p1.equals(p2)) {
                    return 0;
                } else if (p1.getLabel().contains("res1")) {
                    return -1;
                } else {
                    return 1;
                }
            }
        };
        final List<? extends AssistProposal> proposals = proposalsProvider.getImportsProposals("e", comparator);
        assertThat(transform(proposals, toLabels())).containsExactly("res1", "other");
        
        file.delete(true, null);
    }

    @Test
    public void libraryAliasesAreProvided_whenImportsAreUsingWithNameSyntax() throws Exception {
        final IFile file = projectProvider.createFile("file.robot",
                "*** Settings ***",
                "Library  StdLib1  WITH NAME  lib_y",
                "Library  StdLib2  WITH NAME  lib_x",
                "*** Test Cases ***");
        final RobotSuiteFile model = robotModel.createSuiteFile(file);
        final RedImportProposals proposalsProvider = new RedImportProposals(model);

        final List<? extends AssistProposal> proposals = proposalsProvider.getImportsProposals("");
        assertThat(transform(proposals, toLabels())).containsExactly("lib_x", "lib_y");
        
        file.delete(true, null);
    }
}
