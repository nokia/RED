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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.rf.ide.core.project.RobotProjectConfig.LibraryType;
import org.rf.ide.core.project.RobotProjectConfig.ReferencedLibrary;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.project.library.KeywordSpecification;
import org.robotframework.ide.eclipse.main.plugin.project.library.LibrarySpecification;
import org.robotframework.red.junit.ProjectProvider;

public class RedKeywordProposalsTest {

    @ClassRule
    public static ProjectProvider projectProvider = new ProjectProvider(RedKeywordProposalsTest.class);

    private RobotModel robotModel;

    @Before
    public void beforeTest() throws Exception {
        robotModel = new RobotModel();

        projectProvider.createFile("res.robot",
                "*** Keywords ***",
                "a_res_kw1",
                "b_res_kw2",
                "c_res_kw3");
    }

    @Test
    public void noLocalKeywordsAreProvided_whenTheyDoNotMatchToGivenContentAndDefaultMatcherIsUsed()
            throws Exception {
        final IFile file = projectProvider.createFile("file.robot",
                "*** Keywords ***",
                "a_kw1",
                "b_kw2",
                "c_kw3",
                "*** Test Cases ***");
        final RobotSuiteFile suiteFile = robotModel.createSuiteFile(file);

        final RedKeywordProposals provider = new RedKeywordProposals(robotModel, suiteFile);

        assertThat(provider.getKeywordProposals("kw")).isEmpty();
    }

    @Test
    public void onlyLocalKeywordsMatchingGivenPrefixAreProvided_whenDefaultMatcherIsUsed() throws Exception {
        final IFile file = projectProvider.createFile("file.robot",
                "*** Keywords ***",
                "a_kw1",
                "b_kw2",
                "c_kw3",
                "*** Test Cases ***");
        final RobotSuiteFile suiteFile = robotModel.createSuiteFile(file);

        final RedKeywordProposals provider = new RedKeywordProposals(robotModel, suiteFile);
        final List<? extends AssistProposal> proposals = provider.getKeywordProposals("b_");

        assertThat(transform(proposals, toLabels())).containsExactly("b_kw2 - file.robot");
    }

    @Test
    public void onlyLocalKeywordsMatchedByGivenMatcherAreProvided_whenProvidingCustomMatcher()
            throws Exception {
        final IFile file = projectProvider.createFile("file.robot",
                "*** Keywords ***",
                "a_kw1",
                "b_kw2",
                "c_kw3",
                "*** Test Cases ***");
        final RobotSuiteFile suiteFile = robotModel.createSuiteFile(file);

        final RedKeywordProposals provider = new RedKeywordProposals(robotModel, suiteFile, substringMatcher(),
                AssistProposalPredicates.<LibrarySpecification> alwaysTrue());
        final List<? extends AssistProposal> proposals = provider.getKeywordProposals("3");

        assertThat(transform(proposals, toLabels())).containsExactly("c_kw3 - file.robot");
    }

    @Test
    public void allLocalKeywordsAreProvided_whenTheyAreMatched() throws Exception {
        final IFile file = projectProvider.createFile("file.robot",
                "*** Keywords ***",
                "a_kw1",
                "b_kw2",
                "c_kw3",
                "*** Test Cases ***");
        final RobotSuiteFile suiteFile = robotModel.createSuiteFile(file);

        final RedKeywordProposals provider = new RedKeywordProposals(robotModel, suiteFile);
        final List<? extends AssistProposal> proposals = provider.getKeywordProposals("");

        assertThat(transform(proposals, toLabels())).containsExactly("a_kw1 - file.robot", "b_kw2 - file.robot",
                "c_kw3 - file.robot");
    }

    @Test
    public void allLocalKeywordsAreProvidedInOrderInducedByGivenComparator_whenCustomComparatorIsProvided()
            throws Exception {
        final IFile file = projectProvider.createFile("file.robot",
                "*** Keywords ***",
                "a_kw1",
                "b_kw2",
                "c_kw3",
                "*** Test Cases ***");
        final RobotSuiteFile suiteFile = robotModel.createSuiteFile(file);

        final RedKeywordProposals provider = new RedKeywordProposals(robotModel, suiteFile);
        final Comparator<? super RedKeywordProposal> comparator = firstProposalContaining("b");
        final List<? extends AssistProposal> proposals = provider.getKeywordProposals("", comparator);

        assertThat(transform(proposals, toLabels())).containsExactly("b_kw2 - file.robot", "a_kw1 - file.robot",
                "c_kw3 - file.robot");
    }

    @Test
    public void noResourceKeywordsAreProvided_whenTheyDoNotMatchToGivenContentAndDefaultMatcherIsUsed()
            throws Exception {
        final IFile file = projectProvider.createFile("file.robot",
                "*** Settings ***",
                "Resource  res.robot",
                "*** Test Cases ***");
        final RobotSuiteFile suiteFile = robotModel.createSuiteFile(file);

        final RedKeywordProposals provider = new RedKeywordProposals(robotModel, suiteFile);

        assertThat(provider.getKeywordProposals("kw")).isEmpty();
    }

    @Test
    public void onlyResourceKeywordsMatchingGivenPrefixAreProvided_whenDefaultMatcherIsUsed() throws Exception {
        final IFile file = projectProvider.createFile("file.robot",
                "*** Settings ***",
                "Resource  res.robot",
                "*** Test Cases ***");
        final RobotSuiteFile suiteFile = robotModel.createSuiteFile(file);

        final RedKeywordProposals provider = new RedKeywordProposals(robotModel, suiteFile);
        final List<? extends AssistProposal> proposals = provider.getKeywordProposals("b_");

        assertThat(transform(proposals, toLabels())).containsExactly("b_res_kw2 - res.robot");
    }

    @Test
    public void onlyResourceKeywordsMatchedByGivenMatcherAreProvided_whenProvidingCustomMatcher() throws Exception {
        final IFile file = projectProvider.createFile("file.robot",
                "*** Settings ***",
                "Resource  res.robot",
                "*** Test Cases ***");
        final RobotSuiteFile suiteFile = robotModel.createSuiteFile(file);

        final RedKeywordProposals provider = new RedKeywordProposals(robotModel, suiteFile, substringMatcher(),
                AssistProposalPredicates.<LibrarySpecification> alwaysTrue());
        final List<? extends AssistProposal> proposals = provider.getKeywordProposals("3");

        assertThat(transform(proposals, toLabels())).containsExactly("c_res_kw3 - res.robot");
    }

    @Test
    public void allResourceKeywordsAreProvided_whenTheyAreMatched() throws Exception {
        final IFile file = projectProvider.createFile("file.robot",
                "*** Settings ***",
                "Resource  res.robot",
                "*** Test Cases ***");
        final RobotSuiteFile suiteFile = robotModel.createSuiteFile(file);

        final RedKeywordProposals provider = new RedKeywordProposals(robotModel, suiteFile);
        final List<? extends AssistProposal> proposals = provider.getKeywordProposals("");

        assertThat(transform(proposals, toLabels())).containsExactly("a_res_kw1 - res.robot", "b_res_kw2 - res.robot",
                "c_res_kw3 - res.robot");
    }

    @Test
    public void allResourceKeywordsAreProvidedInOrderInducedByGivenComparator_whenCustomComparatorIsProvided()
            throws Exception {
        final IFile file = projectProvider.createFile("file.robot",
                "*** Settings ***",
                "Resource  res.robot",
                "*** Test Cases ***");
        final RobotSuiteFile suiteFile = robotModel.createSuiteFile(file);

        final RedKeywordProposals provider = new RedKeywordProposals(robotModel, suiteFile);
        final Comparator<? super RedKeywordProposal> comparator = firstProposalContaining("b");
        final List<? extends AssistProposal> proposals = provider.getKeywordProposals("", comparator);

        assertThat(transform(proposals, toLabels())).containsExactly("b_res_kw2 - res.robot", "a_res_kw1 - res.robot",
                "c_res_kw3 - res.robot");
    }

    @Test
    public void noLibraryKeywordsAreProvided_whenTheyDoNotMatchToGivenContentAndDefaultMatcherIsUsed()
            throws Exception {
        final RobotProject robotProject = robotModel.createRobotProject(projectProvider.getProject());
        robotProject.setStandardLibraries(
                createStandardLibraries(
                        libKeyword("stdLib", "a_slib_kw1"),
                        libKeyword("stdLib", "b_slib_kw2"),
                        libKeyword("stdLib", "c_slib_kw3")));
        robotProject.setReferencedLibraries(
                createReferencedLibraries(
                        libKeyword("refLib", "a_rlib_kw1"),
                        libKeyword("refLib", "b_rlib_kw2"),
                        libKeyword("refLib", "c_rlib_kw3")));

        final IFile file = projectProvider.createFile("file.robot",
                "*** Settings ***",
                "Library  stdLib",
                "Library  refLib",
                "*** Test Cases ***");
        final RobotSuiteFile suiteFile = robotModel.createSuiteFile(file);

        final RedKeywordProposals provider = new RedKeywordProposals(robotModel, suiteFile);

        assertThat(provider.getKeywordProposals("kw")).isEmpty();
    }

    @Test
    public void onlyLibraryKeywordsMatchingGivenPrefixAreProvided_whenDefaultMatcherIsUsed() throws Exception {
        final RobotProject robotProject = robotModel.createRobotProject(projectProvider.getProject());
        robotProject.setStandardLibraries(
                createStandardLibraries(
                        libKeyword("stdLib", "a_slib_kw1"),
                        libKeyword("stdLib", "b_slib_kw2"),
                        libKeyword("stdLib", "c_slib_kw3")));
        robotProject.setReferencedLibraries(
                createReferencedLibraries(
                        libKeyword("refLib", "a_rlib_kw1"),
                        libKeyword("refLib", "b_rlib_kw2"),
                        libKeyword("refLib", "c_rlib_kw3")));

        final IFile file = projectProvider.createFile("file.robot",
                "*** Settings ***",
                "Library  stdLib",
                "Library  refLib",
                "*** Test Cases ***");
        final RobotSuiteFile suiteFile = robotModel.createSuiteFile(file);

        final RedKeywordProposals provider = new RedKeywordProposals(robotModel, suiteFile);
        final List<? extends AssistProposal> proposals = provider.getKeywordProposals("b_");

        assertThat(transform(proposals, toLabels())).containsExactly("b_rlib_kw2 - refLib", "b_slib_kw2 - stdLib");
    }

    @Test
    public void onlyLibraryKeywordsMatchedByGivenMatcherAreProvided_whenProvidingCustomMatcher() throws Exception {
        final RobotProject robotProject = robotModel.createRobotProject(projectProvider.getProject());
        robotProject.setStandardLibraries(
                createStandardLibraries(
                        libKeyword("stdLib", "a_slib_kw1"),
                        libKeyword("stdLib", "b_slib_kw2"),
                        libKeyword("stdLib", "c_slib_kw3")));
        robotProject.setReferencedLibraries(
                createReferencedLibraries(
                        libKeyword("refLib", "a_rlib_kw1"),
                        libKeyword("refLib", "b_rlib_kw2"),
                        libKeyword("refLib", "c_rlib_kw3")));

        final IFile file = projectProvider.createFile("file.robot",
                "*** Settings ***",
                "Library  stdLib",
                "Library  refLib",
                "*** Test Cases ***");
        final RobotSuiteFile suiteFile = robotModel.createSuiteFile(file);

        final RedKeywordProposals provider = new RedKeywordProposals(robotModel, suiteFile, substringMatcher(),
                AssistProposalPredicates.<LibrarySpecification> alwaysTrue());
        final List<? extends AssistProposal> proposals = provider.getKeywordProposals("3");

        assertThat(transform(proposals, toLabels())).containsExactly("c_rlib_kw3 - refLib", "c_slib_kw3 - stdLib");
    }

    @Test
    public void allLibraryKeywordsAreProvided_whenTheyAreMatched() throws Exception {
        final RobotProject robotProject = robotModel.createRobotProject(projectProvider.getProject());
        robotProject.setStandardLibraries(
                createStandardLibraries(
                        libKeyword("stdLib", "a_slib_kw1"),
                        libKeyword("stdLib", "b_slib_kw2"),
                        libKeyword("stdLib", "c_slib_kw3")));
        robotProject.setReferencedLibraries(
                createReferencedLibraries(
                        libKeyword("refLib", "a_rlib_kw1"),
                        libKeyword("refLib", "b_rlib_kw2"),
                        libKeyword("refLib", "c_rlib_kw3")));

        final IFile file = projectProvider.createFile("file.robot",
                "*** Settings ***",
                "Library  stdLib",
                "Library  refLib",
                "*** Test Cases ***");
        final RobotSuiteFile suiteFile = robotModel.createSuiteFile(file);

        final RedKeywordProposals provider = new RedKeywordProposals(robotModel, suiteFile);
        final List<? extends AssistProposal> proposals = provider.getKeywordProposals("");

        assertThat(transform(proposals, toLabels())).containsExactly("a_rlib_kw1 - refLib", "a_slib_kw1 - stdLib",
                "b_rlib_kw2 - refLib", "b_slib_kw2 - stdLib", "c_rlib_kw3 - refLib", "c_slib_kw3 - stdLib");
    }

    @Test
    public void allLibraryKeywordsAreProvidedInOrderInducedByGivenComparator_whenCustomComparatorIsProvided()
            throws Exception {
        final RobotProject robotProject = robotModel.createRobotProject(projectProvider.getProject());
        robotProject.setStandardLibraries(
                createStandardLibraries(
                        libKeyword("stdLib", "a_kw"),
                        libKeyword("stdLib", "b_kw")));
        robotProject.setReferencedLibraries(createReferencedLibraries());

        final IFile file = projectProvider.createFile("file.robot",
                "*** Settings ***",
                "Library  stdLib",
                "Library  refLib",
                "*** Test Cases ***");
        final RobotSuiteFile suiteFile = robotModel.createSuiteFile(file);

        final RedKeywordProposals provider = new RedKeywordProposals(robotModel, suiteFile);
        final Comparator<? super RedKeywordProposal> comparator = firstProposalContaining("b");
        final List<? extends AssistProposal> proposals = provider.getKeywordProposals("", comparator);

        assertThat(transform(proposals, toLabels())).containsExactly("b_kw - stdLib", "a_kw - stdLib");
    }
    
    @Test
    public void onlyLibraryKeywordsFromLibrariesSatisfyingPredicateAreProvided_whenPredicateFiltersLibraries()
            throws Exception {
        final RobotProject robotProject = robotModel.createRobotProject(projectProvider.getProject());
        robotProject.setStandardLibraries(createStandardLibraries(libKeyword("stdLib", "a_slib_kw1")));
        robotProject.setReferencedLibraries(createReferencedLibraries(libKeyword("refLib", "a_rlib_kw1")));

        final IFile file = projectProvider.createFile("file.robot",
                "*** Settings ***",
                "Library  stdLib",
                "Library  refLib",
                "*** Test Cases ***");
        final RobotSuiteFile suiteFile = robotModel.createSuiteFile(file);

        final AssistProposalPredicate<LibrarySpecification> predicate = new AssistProposalPredicate<LibrarySpecification>() {
            @Override
            public boolean apply(final LibrarySpecification libSpec) {
                return !libSpec.getName().equals("refLib");
            }
        };
        final RedKeywordProposals provider = new RedKeywordProposals(robotModel, suiteFile, substringMatcher(),
                predicate);
        final List<? extends AssistProposal> proposals = provider.getKeywordProposals("");

        assertThat(transform(proposals, toLabels())).containsExactly("a_slib_kw1 - stdLib");
    }

    @Test
    public void allKeywordProposalsAreProvided_whenTheyArePrefixedWithBddSyntax() throws Exception {
        final RobotProject robotProject = robotModel.createRobotProject(projectProvider.getProject());
        robotProject.setStandardLibraries(
                createStandardLibraries(
                        libKeyword("stdLib", "a_slib_kw")));
        robotProject.setReferencedLibraries(createReferencedLibraries());

        final IFile file = projectProvider.createFile("file.robot",
                "*** Settings ***",
                "Library  stdLib",
                "Resource  res.robot",
                "*** Keywords ***",
                "a_kw",
                "*** Test Cases ***");
        final RobotSuiteFile suiteFile = robotModel.createSuiteFile(file);

        final RedKeywordProposals provider = new RedKeywordProposals(robotModel, suiteFile);

        for (final String bddPrefix : newArrayList("Given", "When", "And", "But", "Then")) {
            final List<? extends AssistProposal> proposals = provider.getKeywordProposals(bddPrefix + " a");

            assertThat(transform(proposals, toLabels())).containsExactly(
                    "a_kw - file.robot", "a_res_kw1 - res.robot", "a_slib_kw - stdLib");
        }
    }

    @Test
    public void onlyKeywordProposalsMatchingQualifiedNameAreProvided_whenQualifiedNameIsUsed_1() throws Exception {
        final RobotProject robotProject = robotModel.createRobotProject(projectProvider.getProject());
        robotProject.setStandardLibraries(createStandardLibraries(libKeyword("stdLib", "a_slib_kw")));
        robotProject.setReferencedLibraries(createReferencedLibraries());

        final IFile file = projectProvider.createFile("file.robot",
                "*** Settings ***",
                "Library  stdLib",
                "Resource  res.robot",
                "*** Keywords ***",
                "a_kw",
                "*** Test Cases ***");
        final RobotSuiteFile suiteFile = robotModel.createSuiteFile(file);

        final RedKeywordProposals provider = new RedKeywordProposals(robotModel, suiteFile);

        final List<? extends AssistProposal> proposals = provider.getKeywordProposals("stdLib.");

        assertThat(transform(proposals, toLabels())).containsExactly("a_slib_kw - stdLib");
    }

    @Test
    public void onlyKeywordProposalsMatchingQualifiedNameAreProvided_whenQualifiedNameIsUsed_withAlias() throws Exception {
        final RobotProject robotProject = robotModel.createRobotProject(projectProvider.getProject());
        robotProject.setStandardLibraries(createStandardLibraries(libKeyword("stdLib", "a_slib_kw")));
        robotProject.setReferencedLibraries(createReferencedLibraries());

        final IFile file = projectProvider.createFile("file.robot",
                "*** Settings ***",
                "Library  stdLib  WITH NAME  myLib",
                "Resource  res.robot",
                "*** Keywords ***",
                "a_kw",
                "*** Test Cases ***");
        final RobotSuiteFile suiteFile = robotModel.createSuiteFile(file);

        final RedKeywordProposals provider = new RedKeywordProposals(robotModel, suiteFile);

        assertThat(provider.getKeywordProposals("stdLib.")).isEmpty();

        final List<? extends AssistProposal> proposals = provider.getKeywordProposals("myLib.");
        assertThat(transform(proposals, toLabels())).containsExactly("a_slib_kw - myLib");
    }

    @Test
    public void onlyKeywordProposalsMatchingQualifiedNameAreProvided_whenQualifiedNameIsUsed_2() throws Exception {
        final RobotProject robotProject = robotModel.createRobotProject(projectProvider.getProject());
        robotProject.setStandardLibraries(
                createStandardLibraries(
                        libKeyword("stdLib", "a_slib_kw")));
        robotProject.setReferencedLibraries(createReferencedLibraries());

        final IFile file = projectProvider.createFile("file.robot",
                "*** Settings ***",
                "Library  stdLib",
                "Resource  res.robot",
                "*** Keywords ***",
                "a_kw",
                "*** Test Cases ***");
        final RobotSuiteFile suiteFile = robotModel.createSuiteFile(file);

        final RedKeywordProposals provider = new RedKeywordProposals(robotModel, suiteFile);

        final List<? extends AssistProposal> proposals = provider.getKeywordProposals("res.");

        assertThat(transform(proposals, toLabels())).containsExactly("a_res_kw1 - res.robot", "b_res_kw2 - res.robot",
                "c_res_kw3 - res.robot");
    }

    @Test
    public void onlyKeywordProposalsMatchingQualifiedNameAreProvided_whenQualifiedNameIsUsed_3() throws Exception {
        final RobotProject robotProject = robotModel.createRobotProject(projectProvider.getProject());
        robotProject.setStandardLibraries(createStandardLibraries(libKeyword("stdLib", "a_slib_kw")));
        robotProject.setReferencedLibraries(createReferencedLibraries());

        final IFile file = projectProvider.createFile("file.robot",
                "*** Settings ***",
                "Library  stdLib",
                "Resource  res.robot",
                "*** Keywords ***",
                "a_kw",
                "*** Test Cases ***");
        final RobotSuiteFile suiteFile = robotModel.createSuiteFile(file);

        final RedKeywordProposals provider = new RedKeywordProposals(robotModel, suiteFile);

        final List<? extends AssistProposal> proposals = provider.getKeywordProposals("file.");

        assertThat(transform(proposals, toLabels())).containsExactly("a_kw - file.robot");
    }

    @Test
    public void keywordsUsingEmbeddedSyntaxAreProvided_whenPrefixMatchesArguments() throws Exception {
        final IFile file = projectProvider.createFile("file.robot",
                "*** Settings ***",
                "*** Keywords ***",
                "kw with ${arg} and ${arg2}",
                "*** Test Cases ***");
        final RobotSuiteFile suiteFile = robotModel.createSuiteFile(file);

        final RedKeywordProposals provider = new RedKeywordProposals(robotModel, suiteFile);

        final List<? extends AssistProposal> proposals = provider.getKeywordProposals("kw with something");

        assertThat(transform(proposals, toLabels())).containsExactly("kw with ${arg} and ${arg2} - file.robot");
    }
    
    @Test
    public void qualifiedNameIsAddedToContentForProposals_whenProposalIsConflicting() throws Exception {
        final RobotProject robotProject = robotModel.createRobotProject(projectProvider.getProject());
        robotProject.setStandardLibraries(createStandardLibraries(libKeyword("stdLib", "a_res_kw1")));
        robotProject.setReferencedLibraries(createReferencedLibraries());

        final IFile file = projectProvider.createFile("file.robot",
                "*** Settings ***",
                "Library  stdLib",
                "Resource  res.robot",
                "*** Keywords ***",
                "a_res_kw1",
                "*** Test Cases ***");
        final RobotSuiteFile suiteFile = robotModel.createSuiteFile(file);

        final RedKeywordProposals provider = new RedKeywordProposals(robotModel, suiteFile);

        final List<? extends AssistProposal> proposals = provider.getKeywordProposals("a_");

        assertThat(proposals).hasSize(3);

        for (final AssistProposal proposal : proposals) {
            final RedKeywordProposal keywordProposal = (RedKeywordProposal) proposal;

            switch (keywordProposal.getAlias()) {
                case "file":
                    assertThat(keywordProposal.getContent()).isEqualTo("a_res_kw1");
                    break;
                case "res":
                    assertThat(keywordProposal.getContent()).isEqualTo("res.a_res_kw1");
                    break;
                case "stdLib":
                    assertThat(keywordProposal.getContent()).isEqualTo("stdLib.a_res_kw1");
                    break;
            }
        }
    }
    
    @Test
    public void bestMatchingKeywordIsLocalWhenAllExist() throws Exception {
        final RobotProject robotProject = robotModel.createRobotProject(projectProvider.getProject());
        robotProject.setStandardLibraries(createStandardLibraries(libKeyword("stdLib", "a_res_kw1")));
        robotProject.setReferencedLibraries(createReferencedLibraries(libKeyword("refLib", "a_res_kw1")));

        final IFile file = projectProvider.createFile("file.robot",
                "*** Settings ***",
                "Library  stdLib",
                "Resource  res.robot",
                "*** Keywords ***",
                "a_res_kw1",
                "*** Test Cases ***");
        final RobotSuiteFile suiteFile = robotModel.createSuiteFile(file);

        final RedKeywordProposals provider = new RedKeywordProposals(robotModel, suiteFile);

        final RedKeywordProposal bestMatch = provider.getBestMatchingKeywordProposal("a_res_kw1");
        assertThat(bestMatch.getAlias()).isEqualTo("file");
    }

    @Test
    public void bestMatchingKeywordIsResourceKeywordIfThereIsNoLocal() throws Exception {
        final RobotProject robotProject = robotModel.createRobotProject(projectProvider.getProject());
        robotProject.setStandardLibraries(createStandardLibraries(libKeyword("stdLib", "a_res_kw1")));
        robotProject.setReferencedLibraries(createReferencedLibraries(libKeyword("refLib", "a_res_kw1")));

        final IFile file = projectProvider.createFile("file.robot",
                "*** Settings ***",
                "Library  stdLib",
                "Library  refLib",
                "Resource  res.robot",
                "*** Test Cases ***");
        final RobotSuiteFile suiteFile = robotModel.createSuiteFile(file);

        final RedKeywordProposals provider = new RedKeywordProposals(robotModel, suiteFile);
        
        final RedKeywordProposal bestMatch = provider.getBestMatchingKeywordProposal("a_res_kw1");
        assertThat(bestMatch.getAlias()).isEqualTo("res");
    }

    @Test
    public void bestMatchingKeywordIsUserLibKeywordIfThereIsNoLocalAndResource() throws Exception {
        final RobotProject robotProject = robotModel.createRobotProject(projectProvider.getProject());
        robotProject.setStandardLibraries(createStandardLibraries(libKeyword("stdLib", "a_res_kw1")));
        robotProject.setReferencedLibraries(createReferencedLibraries(libKeyword("refLib", "a_res_kw1")));

        final IFile file = projectProvider.createFile("file.robot",
                "*** Settings ***",
                "Library  stdLib",
                "Library  refLib",
                "*** Test Cases ***");
        final RobotSuiteFile suiteFile = robotModel.createSuiteFile(file);

        final RedKeywordProposals provider = new RedKeywordProposals(robotModel, suiteFile);
        
        final RedKeywordProposal bestMatch = provider.getBestMatchingKeywordProposal("a_res_kw1");
        assertThat(bestMatch.getAlias()).isEqualTo("refLib");
    }

    @Test
    public void bestMatchingKeywordIsStdLibKeywordIfThereAreNoOther() throws Exception {
        final RobotProject robotProject = robotModel.createRobotProject(projectProvider.getProject());
        robotProject.setStandardLibraries(createStandardLibraries(libKeyword("stdLib", "a_res_kw1")));
        robotProject.setReferencedLibraries(createReferencedLibraries());

        final IFile file = projectProvider.createFile("file.robot",
                "*** Settings ***",
                "Library  stdLib",
                "Library  refLib",
                "*** Test Cases ***");
        final RobotSuiteFile suiteFile = robotModel.createSuiteFile(file);

        final RedKeywordProposals provider = new RedKeywordProposals(robotModel, suiteFile);
        
        final RedKeywordProposal bestMatch = provider.getBestMatchingKeywordProposal("a_res_kw1");
        assertThat(bestMatch.getAlias()).isEqualTo("stdLib");
    }

    @Test
    public void bestMatchingKeywordIsNullForUnknownName() throws Exception {
        final RobotProject robotProject = robotModel.createRobotProject(projectProvider.getProject());
        robotProject.setStandardLibraries(createStandardLibraries(libKeyword("stdLib", "a_res_kw1")));
        robotProject.setReferencedLibraries(createReferencedLibraries(libKeyword("refLib", "a_res_kw1")));

        final IFile file = projectProvider.createFile("file.robot",
                "*** Settings ***",
                "Library  stdLib",
                "Library  refLib",
                "Resource  res.robot",
                "*** Keywords ***",
                "a_res_kw1",
                "*** Test Cases ***");
        final RobotSuiteFile suiteFile = robotModel.createSuiteFile(file);

        final RedKeywordProposals provider = new RedKeywordProposals(robotModel, suiteFile);
        
        final RedKeywordProposal bestMatch = provider.getBestMatchingKeywordProposal("unknown");
        assertThat(bestMatch).isNull();
    }

    private static Map<String, LibrarySpecification> createStandardLibraries(final LibKeyword... keywords) {
        final Map<String, LibrarySpecification> stdLibs = new LinkedHashMap<>();
        for (final LibKeyword keyword : keywords) {
            if (!stdLibs.containsKey(keyword.library)) {
                final LibrarySpecification stdLib = new LibrarySpecification();
                stdLib.setName(keyword.library);
                stdLibs.put(keyword.library, stdLib);
            }
            final KeywordSpecification keywordSpecification = new KeywordSpecification();
            keywordSpecification.setName(keyword.keyword);

            final LibrarySpecification libSpec = stdLibs.get(keyword.library);
            libSpec.getKeywords().add(keywordSpecification);
        }
        return stdLibs;
    }

    private static Map<ReferencedLibrary, LibrarySpecification> createReferencedLibraries(
            final LibKeyword... keywords) {
        final Map<ReferencedLibrary, LibrarySpecification> refLibs = new LinkedHashMap<>();
        for (final LibKeyword keyword : keywords) {
            final ReferencedLibrary refLibrary = ReferencedLibrary.create(LibraryType.PYTHON, keyword.library, "");
            if (!refLibs.containsKey(refLibrary)) {

                final LibrarySpecification refLibSpec = new LibrarySpecification();
                refLibSpec.setName(keyword.library);
                refLibSpec.setReferenced(refLibrary);
                refLibs.put(refLibrary, refLibSpec);
            }
            final KeywordSpecification keywordSpecification = new KeywordSpecification();
            keywordSpecification.setName(keyword.keyword);

            final LibrarySpecification libSpec = refLibs.get(refLibrary);
            libSpec.getKeywords().add(keywordSpecification);
        }
        return refLibs;
    }

    private static Comparator<? super RedKeywordProposal> firstProposalContaining(final String toContain) {
        return new Comparator<RedKeywordProposal>() {

            @Override
            public int compare(final RedKeywordProposal p1, final RedKeywordProposal p2) {
                if (p1.equals(p2)) {
                    return 0;
                } else if (p1.getNameFromDefinition().contains(toContain)) {
                    return -1;
                } else {
                    return 1;
                }
            }
        };
    }

    private static LibKeyword libKeyword(final String libName, final String kwName) {
        return new LibKeyword(libName, kwName);
    }

    private static class LibKeyword {

        private final String library;

        private final String keyword;

        public LibKeyword(final String libName, final String kwName) {
            this.library = libName;
            this.keyword = kwName;
        }
    }
}
