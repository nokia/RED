/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.assist;

import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.robotframework.ide.eclipse.main.plugin.assist.AssistProposalPredicates.alwaysTrue;
import static org.robotframework.ide.eclipse.main.plugin.assist.Commons.firstProposalContaining;
import static org.robotframework.ide.eclipse.main.plugin.assist.Commons.prefixesMatcher;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.core.resources.IFile;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.rf.ide.core.libraries.LibraryDescriptor;
import org.rf.ide.core.libraries.LibrarySpecification;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.project.editor.libraries.Libraries;
import org.robotframework.red.junit.PreferenceUpdater;
import org.robotframework.red.junit.ProjectProvider;

public class RedKeywordProposalsTest {

    @ClassRule
    public static ProjectProvider projectProvider = new ProjectProvider(RedKeywordProposalsTest.class);

    @Rule
    public PreferenceUpdater preferenceUpdater = new PreferenceUpdater();

    private RobotModel robotModel;

    @BeforeClass
    public static void beforeSuite() throws Exception {
        projectProvider.createFile("res.robot",
                "*** Keywords ***",
                "a_res_kw1",
                "b_res_kw2",
                "c_res_kw3");
    }

    @Before
    public void beforeTest() throws Exception {
        robotModel = new RobotModel();
    }

    @Test
    public void noLocalKeywordsAreProvided_whenTheyDoNotMatchToGivenInputAndDefaultMatcherIsUsed()
            throws Exception {
        final IFile file = projectProvider.createFile("file.robot",
                "*** Keywords ***",
                "a_kw1",
                "b_kw2",
                "c_kw3",
                "*** Test Cases ***");
        final RobotSuiteFile suiteFile = robotModel.createSuiteFile(file);

        final RedKeywordProposals provider = new RedKeywordProposals(robotModel, suiteFile);

        assertThat(provider.getKeywordProposals("other")).isEmpty();
    }

    @Test
    public void onlyLocalKeywordsMatchingGivenInputAreProvided_whenDefaultMatcherIsUsed() throws Exception {
        final IFile file = projectProvider.createFile("file.robot",
                "*** Keywords ***",
                "a_kw1",
                "b_kw2",
                "c_kw3",
                "*** Test Cases ***");
        final RobotSuiteFile suiteFile = robotModel.createSuiteFile(file);

        final RedKeywordProposals provider = new RedKeywordProposals(robotModel, suiteFile);
        final List<? extends AssistProposal> proposals = provider.getKeywordProposals("2");

        assertThat(transform(proposals, AssistProposal::getLabel)).containsExactly("b_kw2 - file.robot");
    }

    @Test
    public void onlyLocalKeywordsMatchingGivenInputAreProvidedWithCorrectOrder_whenDefaultMatcherIsUsed()
            throws Exception {
        final IFile file = projectProvider.createFile("file.robot",
                "*** Keywords ***",
                "a_kw_ab",
                "b_kw_cd",
                "c_kw_ef",
                "ab_kw",
                "cd_kw",
                "ef_kw",
                "Enable Frequency",
                "Edit Configuration File",
                "*** Test Cases ***");
        final RobotSuiteFile suiteFile = robotModel.createSuiteFile(file);

        final RedKeywordProposals provider = new RedKeywordProposals(robotModel, suiteFile);
        final List<? extends AssistProposal> proposals = provider.getKeywordProposals("EF");

        assertThat(transform(proposals, AssistProposal::getLabel)).containsExactly("Enable Frequency - file.robot",
                "ef_kw - file.robot", "c_kw_ef - file.robot");
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

        final RedKeywordProposals provider = new RedKeywordProposals(robotModel, suiteFile, prefixesMatcher(),
                alwaysTrue());
        final List<? extends AssistProposal> proposals = provider.getKeywordProposals("c_");

        assertThat(transform(proposals, AssistProposal::getLabel)).containsExactly("c_kw3 - file.robot");
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

        assertThat(transform(proposals, AssistProposal::getLabel)).containsExactly("a_kw1 - file.robot",
                "b_kw2 - file.robot",
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
        final Comparator<? super RedKeywordProposal> comparator = firstProposalContaining("2");
        final List<? extends AssistProposal> proposals = provider.getKeywordProposals("", comparator);

        assertThat(transform(proposals, AssistProposal::getLabel)).containsExactly("b_kw2 - file.robot",
                "a_kw1 - file.robot",
                "c_kw3 - file.robot");
    }

    @Test
    public void noResourceKeywordsAreProvided_whenTheyDoNotMatchToGivenInputAndDefaultMatcherIsUsed()
            throws Exception {
        final IFile file = projectProvider.createFile("file.robot",
                "*** Settings ***",
                "Resource  res.robot",
                "*** Test Cases ***");
        final RobotSuiteFile suiteFile = robotModel.createSuiteFile(file);

        final RedKeywordProposals provider = new RedKeywordProposals(robotModel, suiteFile);

        assertThat(provider.getKeywordProposals("other")).isEmpty();
    }

    @Test
    public void onlyResourceKeywordsMatchingGivenInputAreProvided_whenDefaultMatcherIsUsed() throws Exception {
        final IFile file = projectProvider.createFile("file.robot",
                "*** Settings ***",
                "Resource  res.robot",
                "*** Test Cases ***");
        final RobotSuiteFile suiteFile = robotModel.createSuiteFile(file);

        final RedKeywordProposals provider = new RedKeywordProposals(robotModel, suiteFile);
        final List<? extends AssistProposal> proposals = provider.getKeywordProposals("3");

        assertThat(transform(proposals, AssistProposal::getLabel)).containsExactly("c_res_kw3 - res.robot");
    }

    @Test
    public void onlyResourceKeywordsMatchingGivenInputAreProvidedWithCorrectOrder_whenDefaultMatcherIsUsed()
            throws Exception {
        projectProvider.createFile("res2.robot",
                "*** Keywords ***",
                "a_res_kw_ab",
                "b_res_kw_cd",
                "c_res_kw_ef",
                "ab_res_kw",
                "cd_res_kw",
                "ef_res_kw",
                "Create Duplicate",
                "Can Be Detected");
        final IFile file = projectProvider.createFile("file.robot",
                "*** Settings ***",
                "Resource  res2.robot",
                "*** Test Cases ***");
        final RobotSuiteFile suiteFile = robotModel.createSuiteFile(file);

        final RedKeywordProposals provider = new RedKeywordProposals(robotModel, suiteFile);
        final List<? extends AssistProposal> proposals = provider.getKeywordProposals("CD");

        assertThat(transform(proposals, AssistProposal::getLabel)).containsExactly("Create Duplicate - res2.robot",
                "cd_res_kw - res2.robot", "b_res_kw_cd - res2.robot");
    }

    @Test
    public void onlyResourceKeywordsMatchedByGivenMatcherAreProvided_whenProvidingCustomMatcher() throws Exception {
        final IFile file = projectProvider.createFile("file.robot",
                "*** Settings ***",
                "Resource  res.robot",
                "*** Test Cases ***");
        final RobotSuiteFile suiteFile = robotModel.createSuiteFile(file);

        final RedKeywordProposals provider = new RedKeywordProposals(robotModel, suiteFile, prefixesMatcher(),
                AssistProposalPredicates.alwaysTrue());
        final List<? extends AssistProposal> proposals = provider.getKeywordProposals("b_");

        assertThat(transform(proposals, AssistProposal::getLabel)).containsExactly("b_res_kw2 - res.robot");
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

        assertThat(transform(proposals, AssistProposal::getLabel)).containsExactly("a_res_kw1 - res.robot",
                "b_res_kw2 - res.robot",
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
        final Comparator<? super RedKeywordProposal> comparator = firstProposalContaining("2");
        final List<? extends AssistProposal> proposals = provider.getKeywordProposals("", comparator);

        assertThat(transform(proposals, AssistProposal::getLabel)).containsExactly("b_res_kw2 - res.robot",
                "a_res_kw1 - res.robot",
                "c_res_kw3 - res.robot");
    }

    @Test
    public void noLibraryKeywordsAreProvided_whenTheyDoNotMatchToGivenInputAndDefaultMatcherIsUsed()
            throws Exception {
        final RobotProject robotProject = robotModel.createRobotProject(projectProvider.getProject());
        robotProject.setStandardLibraries(Libraries.createStdLib("stdLib", "a_slib_kw1", "b_slib_kw2", "c_slib_kw3"));
        robotProject.setReferencedLibraries(Libraries.createRefLib("refLib", "a_rlib_kw1", "b_rlib_kw2", "c_rlib_kw3"));

        final IFile file = projectProvider.createFile("file.robot",
                "*** Settings ***",
                "Library  stdLib",
                "Library  refLib",
                "*** Test Cases ***");
        final RobotSuiteFile suiteFile = robotModel.createSuiteFile(file);

        final RedKeywordProposals provider = new RedKeywordProposals(robotModel, suiteFile);

        assertThat(provider.getKeywordProposals("other")).isEmpty();
    }

    @Test
    public void onlyLibraryKeywordsMatchingGivenInputAreProvided_whenDefaultMatcherIsUsed() throws Exception {
        final RobotProject robotProject = robotModel.createRobotProject(projectProvider.getProject());
        robotProject.setStandardLibraries(Libraries.createStdLib("stdLib", "a_slib_kw1", "b_slib_kw2", "c_slib_kw3"));
        robotProject.setReferencedLibraries(Libraries.createRefLib("refLib", "a_rlib_kw1", "b_rlib_kw2", "c_rlib_kw3"));

        final IFile file = projectProvider.createFile("file.robot",
                "*** Settings ***",
                "Library  stdLib",
                "Library  refLib",
                "*** Test Cases ***");
        final RobotSuiteFile suiteFile = robotModel.createSuiteFile(file);

        final RedKeywordProposals provider = new RedKeywordProposals(robotModel, suiteFile);
        final List<? extends AssistProposal> proposals = provider.getKeywordProposals("2");

        assertThat(transform(proposals, AssistProposal::getLabel)).containsExactly("b_rlib_kw2 - refLib",
                "b_slib_kw2 - stdLib");
    }

    @Test
    public void onlyLibraryKeywordsMatchingGivenInputAreProvidedWithCorrectOrder_whenDefaultMatcherIsUsed()
            throws Exception {
        final RobotProject robotProject = robotModel.createRobotProject(projectProvider.getProject());
        robotProject.setStandardLibraries(Libraries.createStdLib("stdLib", "a_slib_kw_ab", "b_slib_kw_cd",
                "c_slib_kw_ef", "ab_slib_kw", "cd_slib_kw", "ef_slib_kw", "Add Bookmark", "Should Activate Block"));
        robotProject.setReferencedLibraries(Libraries.createRefLib("refLib", "a_rlib_kw_ab", "b_rlib_kw_cd",
                "c_rlib_kw_ef", "ab_rlib_kw", "cd_rlib_kw", "ef_rlib_kw", "Activate Bluetooth", "Get Active Block"));

        final IFile file = projectProvider.createFile("file.robot",
                "*** Settings ***",
                "Library  stdLib",
                "Library  refLib",
                "*** Test Cases ***");
        final RobotSuiteFile suiteFile = robotModel.createSuiteFile(file);

        final RedKeywordProposals provider = new RedKeywordProposals(robotModel, suiteFile);
        final List<? extends AssistProposal> proposals = provider.getKeywordProposals("AB");

        assertThat(transform(proposals, AssistProposal::getLabel)).containsExactly("Activate Bluetooth - refLib",
                "Add Bookmark - stdLib",
                "ab_rlib_kw - refLib",
                "ab_slib_kw - stdLib",
                "a_rlib_kw_ab - refLib",
                "a_slib_kw_ab - stdLib");
    }

    @Test
    public void onlyLibraryKeywordsMatchedByGivenMatcherAreProvided_whenProvidingCustomMatcher() throws Exception {
        final RobotProject robotProject = robotModel.createRobotProject(projectProvider.getProject());
        robotProject.setStandardLibraries(Libraries.createStdLib("stdLib", "a_slib_kw1", "b_slib_kw2", "c_slib_kw3"));
        robotProject.setReferencedLibraries(Libraries.createRefLib("refLib", "a_rlib_kw1", "b_rlib_kw2", "c_rlib_kw3"));

        final IFile file = projectProvider.createFile("file.robot",
                "*** Settings ***",
                "Library  stdLib",
                "Library  refLib",
                "*** Test Cases ***");
        final RobotSuiteFile suiteFile = robotModel.createSuiteFile(file);

        final RedKeywordProposals provider = new RedKeywordProposals(robotModel, suiteFile, prefixesMatcher(),
                AssistProposalPredicates.alwaysTrue());
        final List<? extends AssistProposal> proposals = provider.getKeywordProposals("c_");

        assertThat(transform(proposals, AssistProposal::getLabel)).containsExactly("c_rlib_kw3 - refLib",
                "c_slib_kw3 - stdLib");
    }

    @Test
    public void allLibraryKeywordsAreProvided_whenTheyAreMatched() throws Exception {
        final RobotProject robotProject = robotModel.createRobotProject(projectProvider.getProject());
        robotProject.setStandardLibraries(Libraries.createStdLib("stdLib", "a_slib_kw1", "b_slib_kw2", "c_slib_kw3"));
        robotProject.setReferencedLibraries(Libraries.createRefLib("refLib", "a_rlib_kw1", "b_rlib_kw2", "c_rlib_kw3"));

        final IFile file = projectProvider.createFile("file.robot",
                "*** Settings ***",
                "Library  stdLib",
                "Library  refLib",
                "*** Test Cases ***");
        final RobotSuiteFile suiteFile = robotModel.createSuiteFile(file);

        final RedKeywordProposals provider = new RedKeywordProposals(robotModel, suiteFile);
        final List<? extends AssistProposal> proposals = provider.getKeywordProposals("");

        assertThat(transform(proposals, AssistProposal::getLabel)).containsExactly("a_rlib_kw1 - refLib",
                "a_slib_kw1 - stdLib",
                "b_rlib_kw2 - refLib",
                "b_slib_kw2 - stdLib",
                "c_rlib_kw3 - refLib",
                "c_slib_kw3 - stdLib");
    }

    @Test
    public void allLibraryKeywordsAreProvidedInOrderInducedByGivenComparator_whenCustomComparatorIsProvided()
            throws Exception {
        final RobotProject robotProject = robotModel.createRobotProject(projectProvider.getProject());
        robotProject.setStandardLibraries(Libraries.createStdLib("stdLib", "a_kw", "b_kw"));

        final IFile file = projectProvider.createFile("file.robot",
                "*** Settings ***",
                "Library  stdLib",
                "Library  refLib",
                "*** Test Cases ***");
        final RobotSuiteFile suiteFile = robotModel.createSuiteFile(file);

        final RedKeywordProposals provider = new RedKeywordProposals(robotModel, suiteFile);
        final Comparator<? super RedKeywordProposal> comparator = firstProposalContaining("b_");
        final List<? extends AssistProposal> proposals = provider.getKeywordProposals("", comparator);

        assertThat(transform(proposals, AssistProposal::getLabel)).containsExactly("b_kw - stdLib",
                "a_kw - stdLib");
    }

    @Test
    public void onlyLibraryKeywordsFromLibrariesSatisfyingPredicateAreProvided_whenPredicateFiltersLibraries()
            throws Exception {
        final RobotProject robotProject = robotModel.createRobotProject(projectProvider.getProject());
        robotProject.setStandardLibraries(Libraries.createStdLib("stdLib", "a_slib_kw1"));
        robotProject.setReferencedLibraries(Libraries.createRefLib("refLib", "a_rlib_kw1"));

        final IFile file = projectProvider.createFile("file.robot",
                "*** Settings ***",
                "Library  stdLib",
                "Library  refLib",
                "*** Test Cases ***");
        final RobotSuiteFile suiteFile = robotModel.createSuiteFile(file);

        final AssistProposalPredicate<LibrarySpecification> predicate = libSpec -> !libSpec.getName().equals("refLib");
        final RedKeywordProposals provider = new RedKeywordProposals(robotModel, suiteFile, prefixesMatcher(),
                predicate);
        final List<? extends AssistProposal> proposals = provider.getKeywordProposals("");

        assertThat(transform(proposals, AssistProposal::getLabel)).containsExactly("a_slib_kw1 - stdLib");
    }

    @Test
    public void allKeywordProposalsAreProvided_whenTheyArePrefixedWithBddSyntax() throws Exception {
        final RobotProject robotProject = robotModel.createRobotProject(projectProvider.getProject());
        robotProject.setStandardLibraries(Libraries.createStdLib("stdLib", "a_slib_kw"));

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

            assertThat(transform(proposals, AssistProposal::getLabel)).containsExactly("a_kw - file.robot",
                    "a_res_kw1 - res.robot",
                    "a_slib_kw - stdLib");
        }
    }

    @Test
    public void onlyKeywordProposalsMatchingQualifiedNameAreProvided_whenLibraryQualifiedNameIsUsed() throws Exception {
        final RobotProject robotProject = robotModel.createRobotProject(projectProvider.getProject());
        robotProject.setStandardLibraries(Libraries.createStdLib("stdLib", "a_slib_kw"));

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

        assertThat(transform(proposals, AssistProposal::getLabel)).containsExactly("a_slib_kw - stdLib");
    }

    @Test
    public void onlyKeywordProposalsMatchingQualifiedNameAreProvided_whenLibraryQualifiedNameIsUsed_withAlias()
            throws Exception {
        final RobotProject robotProject = robotModel.createRobotProject(projectProvider.getProject());
        robotProject.setStandardLibraries(Libraries.createStdLib("stdLib", "a_slib_kw"));

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
        assertThat(transform(proposals, AssistProposal::getLabel)).containsExactly("a_slib_kw - myLib");
    }

    @Test
    public void onlyKeywordProposalsMatchingQualifiedNameAreProvided_whenResourceQualifiedNameIsUsed()
            throws Exception {
        final RobotProject robotProject = robotModel.createRobotProject(projectProvider.getProject());
        robotProject.setStandardLibraries(Libraries.createStdLib("stdLib", "a_slib_kw"));

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

        assertThat(transform(proposals, AssistProposal::getLabel)).containsExactly("a_res_kw1 - res.robot",
                "b_res_kw2 - res.robot",
                "c_res_kw3 - res.robot");
    }

    @Test
    public void onlyKeywordProposalsMatchingQualifiedNameAreProvided_whenLocalQualifiedNameIsUsed() throws Exception {
        final RobotProject robotProject = robotModel.createRobotProject(projectProvider.getProject());
        robotProject.setStandardLibraries(Libraries.createStdLib("stdLib", "a_slib_kw"));

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

        assertThat(transform(proposals, AssistProposal::getLabel)).containsExactly("a_kw - file.robot");
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

        assertThat(transform(proposals, AssistProposal::getLabel))
                .containsExactly("kw with ${arg} and ${arg2} - file.robot");
    }

    @Test
    public void qualifiedNameIsAddedToInputForProposals_whenKeywordPrefixAutoAdditionPreferenceIsEnabled()
            throws Exception {
        preferenceUpdater.setValue(RedPreferences.ASSISTANT_KEYWORD_PREFIX_AUTO_ADDITION_ENABLED, true);

        final RobotProject robotProject = robotModel.createRobotProject(projectProvider.getProject());
        robotProject.setStandardLibraries(Libraries.createStdLib("stdLib", "a_lib_kw1"));

        final IFile file = projectProvider.createFile("file.robot",
                "*** Settings ***",
                "Library  stdLib",
                "*** Test Cases ***");
        final RobotSuiteFile suiteFile = robotModel.createSuiteFile(file);

        final RedKeywordProposals provider = new RedKeywordProposals(robotModel, suiteFile);

        final List<? extends AssistProposal> proposals = provider.getKeywordProposals("a_");

        assertThat(proposals).hasSize(1);
        assertThat(proposals.get(0).getContent()).isEqualTo("stdLib.a_lib_kw1");
    }

    @Test
    public void qualifiedNameIsAddedToInputForProposals_whenProposalIsConflicting() throws Exception {
        final RobotProject robotProject = robotModel.createRobotProject(projectProvider.getProject());
        robotProject.setStandardLibraries(Libraries.createStdLib("stdLib", "a_res_kw1"));

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

            switch (keywordProposal.getScope(file.getFullPath())) {
                case LOCAL:
                    assertThat(keywordProposal.getContent()).isEqualTo("a_res_kw1");
                    break;
                case RESOURCE:
                    assertThat(keywordProposal.getContent()).isEqualTo("res.a_res_kw1");
                    break;
                case STD_LIBRARY:
                    assertThat(keywordProposal.getContent()).isEqualTo("stdLib.a_res_kw1");
                    break;
                default:
                    throw new IllegalStateException();
            }
        }
    }

    @Test
    public void onlyKeywordsFromImportedLibrariesOrAccessibleWithoutImportAreProvided_whenKeywordFromNotImportedLibraryPreferenceIsDisabled()
            throws Exception {
        final RobotProject robotProject = robotModel.createRobotProject(projectProvider.getProject());
        final Map<LibraryDescriptor, LibrarySpecification> stdLibs = new HashMap<>();
        stdLibs.putAll(Libraries.createStdLib("BuiltIn", "a_kw1"));
        stdLibs.putAll(Libraries.createStdLib("stdLib", "a_lib_kw1"));
        stdLibs.putAll(Libraries.createStdLib("otherLib", "a_other_kw1"));
        robotProject.setStandardLibraries(stdLibs);
        robotProject.setReferencedLibraries(Libraries.createRefLib("refLib", "a_rlib_kw1"));

        final IFile file = projectProvider.createFile("file.robot",
                "*** Settings ***",
                "Library  stdLib",
                "*** Test Cases ***");
        final RobotSuiteFile suiteFile = robotModel.createSuiteFile(file);

        final RedKeywordProposals provider = new RedKeywordProposals(robotModel, suiteFile);

        final List<? extends AssistProposal> proposals = provider.getKeywordProposals("a_");

        assertThat(transform(proposals, AssistProposal::getLabel)).containsExactly("a_kw1 - BuiltIn",
                "a_lib_kw1 - stdLib");
    }

    @Test
    public void allKeywordsFromProjectLibrariesAreProvided_whenKeywordFromNotImportedLibraryPreferenceIsEnabled()
            throws Exception {
        preferenceUpdater.setValue(RedPreferences.ASSISTANT_KEYWORD_FROM_NOT_IMPORTED_LIBRARY_ENABLED, true);

        final RobotProject robotProject = robotModel.createRobotProject(projectProvider.getProject());
        final Map<LibraryDescriptor, LibrarySpecification> stdLibs = new HashMap<>();
        stdLibs.putAll(Libraries.createStdLib("BuiltIn", "a_kw1"));
        stdLibs.putAll(Libraries.createStdLib("stdLib", "a_lib_kw1"));
        stdLibs.putAll(Libraries.createStdLib("otherLib", "a_other_kw1"));
        robotProject.setStandardLibraries(stdLibs);
        robotProject.setReferencedLibraries(Libraries.createRefLib("refLib", "a_rlib_kw1"));

        final IFile file = projectProvider.createFile("file.robot",
                "*** Settings ***",
                "Library  stdLib",
                "*** Test Cases ***");
        final RobotSuiteFile suiteFile = robotModel.createSuiteFile(file);

        final RedKeywordProposals provider = new RedKeywordProposals(robotModel, suiteFile);

        final List<? extends AssistProposal> proposals = provider.getKeywordProposals("a_");

        assertThat(transform(proposals, AssistProposal::getLabel)).containsExactly("a_kw1 - BuiltIn",
                "a_lib_kw1 - stdLib",
                "a_other_kw1 - otherLib",
                "a_rlib_kw1 - refLib");
    }

    @Test
    public void allKeywordsFromProjectLibrariesAreProvided_whenQualifiedNameIsUsedAndKeywordFromNotImportedLibraryPreferenceIsEnabled()
            throws Exception {
        preferenceUpdater.setValue(RedPreferences.ASSISTANT_KEYWORD_FROM_NOT_IMPORTED_LIBRARY_ENABLED, true);

        final RobotProject robotProject = robotModel.createRobotProject(projectProvider.getProject());
        robotProject.setStandardLibraries(Libraries.createStdLib("otherLib", "a_other_kw1", "a_other_kw2"));
        robotProject.setReferencedLibraries(Libraries.createRefLib("refLib", "a_rlib_kw1"));

        final IFile file = projectProvider.createFile("file.robot");
        final RobotSuiteFile suiteFile = robotModel.createSuiteFile(file);

        final RedKeywordProposals provider = new RedKeywordProposals(robotModel, suiteFile);

        final List<? extends AssistProposal> proposals = provider.getKeywordProposals("otherLib.");

        assertThat(transform(proposals, AssistProposal::getLabel)).containsExactly("a_other_kw1 - otherLib",
                "a_other_kw2 - otherLib");
    }

    @Test
    public void bestMatchingKeywordIsLocalWhenAllExist() throws Exception {
        final RobotProject robotProject = robotModel.createRobotProject(projectProvider.getProject());
        robotProject.setStandardLibraries(Libraries.createStdLib("stdLib", "a_res_kw1"));
        robotProject.setReferencedLibraries(Libraries.createRefLib("refLib", "a_res_kw1"));

        final IFile file = projectProvider.createFile("file.robot",
                "*** Settings ***",
                "Library  stdLib",
                "Resource  res.robot",
                "*** Keywords ***",
                "a_res_kw1",
                "*** Test Cases ***");
        final RobotSuiteFile suiteFile = robotModel.createSuiteFile(file);

        final RedKeywordProposals provider = new RedKeywordProposals(robotModel, suiteFile);

        final Optional<RedKeywordProposal> bestMatch = provider.getBestMatchingKeywordProposal("a_res_kw1");
        assertThat(bestMatch).hasValueSatisfying(proposal -> assertThat(proposal.getSourceName()).isEqualTo("file"));
    }

    @Test
    public void bestMatchingKeywordIsResourceKeywordIfThereIsNoLocal() throws Exception {
        final RobotProject robotProject = robotModel.createRobotProject(projectProvider.getProject());
        robotProject.setStandardLibraries(Libraries.createStdLib("stdLib", "a_res_kw1"));
        robotProject.setReferencedLibraries(Libraries.createRefLib("refLib", "a_res_kw1"));

        final IFile file = projectProvider.createFile("file.robot",
                "*** Settings ***",
                "Library  stdLib",
                "Library  refLib",
                "Resource  res.robot",
                "*** Test Cases ***");
        final RobotSuiteFile suiteFile = robotModel.createSuiteFile(file);

        final RedKeywordProposals provider = new RedKeywordProposals(robotModel, suiteFile);

        final Optional<RedKeywordProposal> bestMatch = provider.getBestMatchingKeywordProposal("a_res_kw1");
        assertThat(bestMatch).hasValueSatisfying(proposal -> assertThat(proposal.getSourceName()).isEqualTo("res"));
    }

    @Test
    public void bestMatchingKeywordIsUserLibKeywordIfThereIsNoLocalAndResource() throws Exception {
        final RobotProject robotProject = robotModel.createRobotProject(projectProvider.getProject());
        robotProject.setStandardLibraries(Libraries.createStdLib("stdLib", "a_res_kw1"));
        robotProject.setReferencedLibraries(Libraries.createRefLib("refLib", "a_res_kw1"));

        final IFile file = projectProvider.createFile("file.robot",
                "*** Settings ***",
                "Library  stdLib",
                "Library  refLib",
                "*** Test Cases ***");
        final RobotSuiteFile suiteFile = robotModel.createSuiteFile(file);

        final RedKeywordProposals provider = new RedKeywordProposals(robotModel, suiteFile);

        final Optional<RedKeywordProposal> bestMatch = provider.getBestMatchingKeywordProposal("a_res_kw1");
        assertThat(bestMatch).hasValueSatisfying(proposal -> assertThat(proposal.getSourceName()).isEqualTo("refLib"));
    }

    @Test
    public void bestMatchingKeywordIsStdLibKeywordIfThereAreNoOther() throws Exception {
        final RobotProject robotProject = robotModel.createRobotProject(projectProvider.getProject());
        robotProject.setStandardLibraries(Libraries.createStdLib("stdLib", "a_res_kw1"));

        final IFile file = projectProvider.createFile("file.robot",
                "*** Settings ***",
                "Library  stdLib",
                "Library  refLib",
                "*** Test Cases ***");
        final RobotSuiteFile suiteFile = robotModel.createSuiteFile(file);

        final RedKeywordProposals provider = new RedKeywordProposals(robotModel, suiteFile);

        final Optional<RedKeywordProposal> bestMatch = provider.getBestMatchingKeywordProposal("a_res_kw1");
        assertThat(bestMatch).hasValueSatisfying(proposal -> assertThat(proposal.getSourceName()).isEqualTo("stdLib"));
    }

    @Test
    public void bestMatchingKeywordIsNullForUnknownName() throws Exception {
        final RobotProject robotProject = robotModel.createRobotProject(projectProvider.getProject());
        robotProject.setStandardLibraries(Libraries.createStdLib("stdLib", "a_res_kw1"));
        robotProject.setReferencedLibraries(Libraries.createRefLib("refLib", "a_res_kw1"));

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

        final Optional<RedKeywordProposal> bestMatch = provider.getBestMatchingKeywordProposal("unknown");
        assertThat(bestMatch).isNotPresent();
    }
}
