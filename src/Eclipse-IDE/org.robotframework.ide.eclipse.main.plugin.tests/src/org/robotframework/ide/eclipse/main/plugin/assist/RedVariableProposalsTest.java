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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.rf.ide.core.project.RobotProjectConfig.ReferencedVariableFile;
import org.rf.ide.core.testdata.imported.ARobotInternalVariable;
import org.rf.ide.core.testdata.imported.ScalarRobotInternalVariable;
import org.rf.ide.core.testdata.importer.VariablesFileImportReference;
import org.rf.ide.core.testdata.model.RobotFileOutput;
import org.rf.ide.core.testdata.model.table.setting.VariablesImport;
import org.robotframework.ide.eclipse.main.plugin.model.RobotFileInternalElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSettingsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.red.junit.ProjectProvider;

public class RedVariableProposalsTest {

    @ClassRule
    public static ProjectProvider projectProvider = new ProjectProvider(RedVariableProposalsTest.class);

    private RobotModel robotModel;

    @Before
    public void beforeTest() throws Exception {
        robotModel = new RobotModel();

        projectProvider.createFile("res.robot",
                "*** Variables ***",
                "${a_res_var}  1",
                "${b_res_var}  2",
                "${c_res_var}  3");
    }

    @Test
    public void noGlobalVariablesAreProvided_whenTheyAreMatchedButGlobalVarPredicateIsAlwaysFalse() throws Exception {
        final RobotProject robotProject = robotModel.createRobotProject(projectProvider.getProject());
        createGlobalVariables(robotProject, "glb_var_1", "glb_var_2");
        createGlobalVarFilesVariables(robotProject);

        final IFile file = projectProvider.createFile("file.robot", "*** Test Cases ***");
        final RobotSuiteFile suiteFile = robotModel.createSuiteFile(file);

        final RedVariableProposals provider = new RedVariableProposals(robotModel, suiteFile,
                AssistProposalPredicates.<String> alwaysFalse());

        assertThat(provider.getVariableProposals("", 0)).isEmpty();
    }

    @Test
    public void noGlobalVariablesAreProvided_whenTheyDoNotMatchToGivenContentAndDefaultMatcherIsUsed()
            throws Exception {
        final RobotProject robotProject = robotModel.createRobotProject(projectProvider.getProject());
        createGlobalVariables(robotProject, "glb_var_1", "glb_var_2");
        createGlobalVarFilesVariables(robotProject);

        final IFile file = projectProvider.createFile("file.robot", "*** Test Cases ***");
        final RobotSuiteFile suiteFile = robotModel.createSuiteFile(file);

        final RedVariableProposals provider = new RedVariableProposals(robotModel, suiteFile,
                AssistProposalPredicates.<String> alwaysTrue());

        assertThat(provider.getVariableProposals("var", 0)).isEmpty();
    }

    @Test
    public void onlyGlobalVariablesSatisfyingPredicateAreProvided_evenWhenAllAreMatchedButPredicateIsSelective()
            throws Exception {
        final RobotProject robotProject = robotModel.createRobotProject(projectProvider.getProject());
        createGlobalVariables(robotProject, "glb_var_1", "glb_var_2");
        createGlobalVarFilesVariables(robotProject);

        final IFile file = projectProvider.createFile("file.robot", "*** Test Cases ***");
        final RobotSuiteFile suiteFile = robotModel.createSuiteFile(file);

        final RedVariableProposals provider = new RedVariableProposals(robotModel, suiteFile,
                new AssistProposalPredicate<String>() {

                    @Override
                    public boolean apply(final String globalVarName) {
                        return globalVarName.contains("2");
                    }
                });

        final List<? extends AssistProposal> proposals = provider.getVariableProposals("", 0);

        assertThat(transform(proposals, toLabels())).containsExactly("glb_var_2");
    }

    @Test
    public void onlyGlobalVariablesMatchingGivenPrefixAreProvided_whenDefaultMatcherIsUsed() throws Exception {
        final RobotProject robotProject = robotModel.createRobotProject(projectProvider.getProject());
        createGlobalVariables(robotProject, "glb_var_1", "glb_var_2");
        createGlobalVarFilesVariables(robotProject);

        final IFile file = projectProvider.createFile("file.robot", "*** Test Cases ***");
        final RobotSuiteFile suiteFile = robotModel.createSuiteFile(file);

        final RedVariableProposals provider = new RedVariableProposals(robotModel, suiteFile,
                AssistProposalPredicates.<String> alwaysTrue());
        final List<? extends AssistProposal> proposals = provider.getVariableProposals("glb_var_1", 0);

        assertThat(transform(proposals, toLabels())).containsExactly("glb_var_1");
    }

    @Test
    public void onlyGlobalVariablesMatchedByGivenMatcherAreProvided_whenProvidingCustomMatcher() throws Exception {
        final RobotProject robotProject = robotModel.createRobotProject(projectProvider.getProject());
        createGlobalVariables(robotProject, "glb_var_1", "glb_var_2");
        createGlobalVarFilesVariables(robotProject);

        final IFile file = projectProvider.createFile("file.robot", "*** Test Cases ***");
        final RobotSuiteFile suiteFile = robotModel.createSuiteFile(file);

        final RedVariableProposals provider = new RedVariableProposals(robotModel, suiteFile, substringMatcher(),
                AssistProposalPredicates.<String> alwaysTrue());
        final List<? extends AssistProposal> proposals = provider.getVariableProposals("2", 0);

        assertThat(transform(proposals, toLabels())).containsExactly("glb_var_2");
    }

    @Test
    public void allGlobalVariablesAreProvided_whenTheyAreMatchedAndGlobalVarPredicateIsAlwaysTrue() throws Exception {
        final RobotProject robotProject = robotModel.createRobotProject(projectProvider.getProject());
        createGlobalVariables(robotProject, "glb_var_1", "glb_var_2");
        createGlobalVarFilesVariables(robotProject);

        final IFile file = projectProvider.createFile("file.robot", "*** Test Cases ***");
        final RobotSuiteFile suiteFile = robotModel.createSuiteFile(file);

        final RedVariableProposals provider = new RedVariableProposals(robotModel, suiteFile,
                AssistProposalPredicates.<String> alwaysTrue());
        final List<? extends AssistProposal> proposals = provider.getVariableProposals("", 0);

        assertThat(transform(proposals, toLabels())).containsExactly("glb_var_1", "glb_var_2");
    }

    @Test
    public void allGlobalVariablesAreProvidedInOrderInducedByGivenComparator_whenCustomComparatorIsProvided()
            throws Exception {
        final RobotProject robotProject = robotModel.createRobotProject(projectProvider.getProject());
        createGlobalVariables(robotProject, "glb_var_1", "glb_var_2");
        createGlobalVarFilesVariables(robotProject);

        final IFile file = projectProvider.createFile("file.robot", "*** Test Cases ***");
        final RobotSuiteFile suiteFile = robotModel.createSuiteFile(file);

        final RedVariableProposals provider = new RedVariableProposals(robotModel, suiteFile,
                AssistProposalPredicates.<String> alwaysTrue());
        final Comparator<? super RedVariableProposal> comparator = firstProposalContaining("2");
        final List<? extends AssistProposal> proposals = provider.getVariableProposals("", comparator, 0);

        assertThat(transform(proposals, toLabels())).containsExactly("glb_var_2", "glb_var_1");
    }

    @Test
    public void noGlobalVarFileVariablesAreProvided_whenTheyDoNotMatchToGivenContentAndDefaultMatcherIsUsed()
            throws Exception {
        final RobotProject robotProject = robotModel.createRobotProject(projectProvider.getProject());
        createGlobalVariables(robotProject);
        createGlobalVarFilesVariables(robotProject, "glb_var_file_1", "glb_var_file_2");

        final IFile file = projectProvider.createFile("file.robot", "*** Test Cases ***");
        final RobotSuiteFile suiteFile = robotModel.createSuiteFile(file);

        final RedVariableProposals provider = new RedVariableProposals(robotModel, suiteFile,
                AssistProposalPredicates.<String> alwaysTrue());

        assertThat(provider.getVariableProposals("var", 0)).isEmpty();
    }

    @Test
    public void onlyGlobalVarFileVariablesMatchingGivenPrefixAreProvided_whenDefaultMatcherIsUsed() throws Exception {
        final RobotProject robotProject = robotModel.createRobotProject(projectProvider.getProject());
        createGlobalVariables(robotProject);
        createGlobalVarFilesVariables(robotProject, "glb_var_file_1", "glb_var_file_2");

        final IFile file = projectProvider.createFile("file.robot", "*** Test Cases ***");
        final RobotSuiteFile suiteFile = robotModel.createSuiteFile(file);

        final RedVariableProposals provider = new RedVariableProposals(robotModel, suiteFile,
                AssistProposalPredicates.<String> alwaysTrue());
        final List<? extends AssistProposal> proposals = provider.getVariableProposals("${glb_var_file_1", 0);

        assertThat(transform(proposals, toLabels())).containsExactly("${glb_var_file_1}");
    }

    @Test
    public void onlyGlobalVarFileVariablesMatchedByGivenMatcherAreProvided_whenProvidingCustomMatcher()
            throws Exception {
        final RobotProject robotProject = robotModel.createRobotProject(projectProvider.getProject());
        createGlobalVariables(robotProject);
        createGlobalVarFilesVariables(robotProject, "glb_var_file_1", "glb_var_file_2");

        final IFile file = projectProvider.createFile("file.robot", "*** Test Cases ***");
        final RobotSuiteFile suiteFile = robotModel.createSuiteFile(file);

        final RedVariableProposals provider = new RedVariableProposals(robotModel, suiteFile, substringMatcher(),
                AssistProposalPredicates.<String> alwaysTrue());
        final List<? extends AssistProposal> proposals = provider.getVariableProposals("2", 0);

        assertThat(transform(proposals, toLabels())).containsExactly("${glb_var_file_2}");
    }

    @Test
    public void allGlobalVarFileVariablesAreProvided_whenTheyAreMatchedAndGlobalVarPredicateIsAlwaysTrue()
            throws Exception {
        final RobotProject robotProject = robotModel.createRobotProject(projectProvider.getProject());
        createGlobalVariables(robotProject);
        createGlobalVarFilesVariables(robotProject, "glb_var_file_1", "glb_var_file_2");

        final IFile file = projectProvider.createFile("file.robot", "*** Test Cases ***");
        final RobotSuiteFile suiteFile = robotModel.createSuiteFile(file);

        final RedVariableProposals provider = new RedVariableProposals(robotModel, suiteFile,
                AssistProposalPredicates.<String> alwaysTrue());
        final List<? extends AssistProposal> proposals = provider.getVariableProposals("", 0);

        assertThat(transform(proposals, toLabels())).containsExactly("${glb_var_file_1}", "${glb_var_file_2}");
    }

    @Test
    public void allGlobalVarFileVariablesAreProvidedInOrderInducedByGivenComparator_whenCustomComparatorIsProvided()
            throws Exception {
        final RobotProject robotProject = robotModel.createRobotProject(projectProvider.getProject());
        createGlobalVariables(robotProject);
        createGlobalVarFilesVariables(robotProject, "glb_var_file_1", "glb_var_file_2");

        final IFile file = projectProvider.createFile("file.robot", "*** Test Cases ***");
        final RobotSuiteFile suiteFile = robotModel.createSuiteFile(file);

        final RedVariableProposals provider = new RedVariableProposals(robotModel, suiteFile,
                AssistProposalPredicates.<String> alwaysTrue());
        final Comparator<? super RedVariableProposal> comparator = firstProposalContaining("2");
        final List<? extends AssistProposal> proposals = provider.getVariableProposals("", comparator, 0);

        assertThat(transform(proposals, toLabels())).containsExactly("${glb_var_file_2}", "${glb_var_file_1}");
    }

    @Test
    public void noLocalVarFileVariablesAreProvided_whenTheyDoNotMatchToGivenContentAndDefaultMatcherIsUsed()
            throws Exception {
        final RobotProject robotProject = robotModel.createRobotProject(projectProvider.getProject());
        createGlobalVariables(robotProject);
        createGlobalVarFilesVariables(robotProject);

        final IFile file = projectProvider.createFile("file.robot",
                "*** Settings ***",
                "Variables  vars.py",
                "*** Test Cases ***");
        final RobotSuiteFile suiteFile = robotModel.createSuiteFile(file);
        addLocalVarFileVariables(suiteFile, "a_vf", "b_vf", "c_vf");

        final RedVariableProposals provider = new RedVariableProposals(robotModel, suiteFile,
                AssistProposalPredicates.<String> alwaysTrue());

        assertThat(provider.getVariableProposals("var", 0)).isEmpty();
    }

    @Test
    public void onlyLocalVarFileVariablesMatchingGivenPrefixAreProvided_whenDefaultMatcherIsUsed() throws Exception {
        final RobotProject robotProject = robotModel.createRobotProject(projectProvider.getProject());
        createGlobalVariables(robotProject);
        createGlobalVarFilesVariables(robotProject);

        final IFile file = projectProvider.createFile("file.robot",
                "*** Settings ***",
                "Variables  vars.py",
                "*** Test Cases ***");
        final RobotSuiteFile suiteFile = robotModel.createSuiteFile(file);
        addLocalVarFileVariables(suiteFile, "a_vf", "b_vf", "c_vf");

        final RedVariableProposals provider = new RedVariableProposals(robotModel, suiteFile,
                AssistProposalPredicates.<String> alwaysTrue());
        final List<? extends AssistProposal> proposals = provider.getVariableProposals("${b_v", 0);

        assertThat(transform(proposals, toLabels())).containsExactly("${b_vf}");
    }

    @Test
    public void onlyLocalVarFileVariablesMatchedByGivenMatcherAreProvided_whenProvidingCustomMatcher()
            throws Exception {
        final RobotProject robotProject = robotModel.createRobotProject(projectProvider.getProject());
        createGlobalVariables(robotProject);
        createGlobalVarFilesVariables(robotProject);

        final IFile file = projectProvider.createFile("file.robot",
                "*** Settings ***",
                "Variables  vars.py",
                "*** Test Cases ***");
        final RobotSuiteFile suiteFile = robotModel.createSuiteFile(file);
        addLocalVarFileVariables(suiteFile, "a_vf", "b_vf", "c_vf");

        final RedVariableProposals provider = new RedVariableProposals(robotModel, suiteFile, substringMatcher(),
                AssistProposalPredicates.<String> alwaysTrue());
        final List<? extends AssistProposal> proposals = provider.getVariableProposals("c", 0);

        assertThat(transform(proposals, toLabels())).containsExactly("${c_vf}");
    }

    @Test
    public void allLocalVarFileVariablesAreProvided_whenTheyAreMatched()
            throws Exception {
        final RobotProject robotProject = robotModel.createRobotProject(projectProvider.getProject());
        createGlobalVariables(robotProject);
        createGlobalVarFilesVariables(robotProject);

        final IFile file = projectProvider.createFile("file.robot",
                "*** Settings ***",
                "Variables  vars.py",
                "*** Test Cases ***");
        final RobotSuiteFile suiteFile = robotModel.createSuiteFile(file);
        addLocalVarFileVariables(suiteFile, "a_vf", "b_vf", "c_vf");

        final RedVariableProposals provider = new RedVariableProposals(robotModel, suiteFile,
                AssistProposalPredicates.<String> alwaysTrue());
        final List<? extends AssistProposal> proposals = provider.getVariableProposals("", 0);

        assertThat(transform(proposals, toLabels())).containsExactly("${a_vf}", "${b_vf}", "${c_vf}");
    }

    @Test
    public void allLocalVarFileVariablesAreProvidedInOrderInducedByGivenComparator_whenCustomComparatorIsProvided()
            throws Exception {
        final RobotProject robotProject = robotModel.createRobotProject(projectProvider.getProject());
        createGlobalVariables(robotProject);
        createGlobalVarFilesVariables(robotProject);

        final IFile file = projectProvider.createFile("file.robot",
                "*** Settings ***",
                "Variables  vars.py",
                "*** Test Cases ***");
        final RobotSuiteFile suiteFile = robotModel.createSuiteFile(file);
        addLocalVarFileVariables(suiteFile, "a_vf", "b_vf");

        final RedVariableProposals provider = new RedVariableProposals(robotModel, suiteFile,
                AssistProposalPredicates.<String> alwaysTrue());
        final Comparator<? super RedVariableProposal> comparator = firstProposalContaining("b");
        final List<? extends AssistProposal> proposals = provider.getVariableProposals("", comparator, 0);

        assertThat(transform(proposals, toLabels())).containsExactly("${b_vf}", "${a_vf}");
    }

    @Test
    public void noLocalVarTableVariablesAreProvided_whenTheyDoNotMatchToGivenContentAndDefaultMatcherIsUsed()
            throws Exception {
        final RobotProject robotProject = robotModel.createRobotProject(projectProvider.getProject());
        createGlobalVariables(robotProject);
        createGlobalVarFilesVariables(robotProject);

        final IFile file = projectProvider.createFile("file.robot",
                "*** Settings ***",
                "*** Variables ***",
                "${a_vt}  1",
                "${b_vt}  2",
                "${c_vt}  3",
                "*** Test Cases ***");
        final RobotSuiteFile suiteFile = robotModel.createSuiteFile(file);

        final RedVariableProposals provider = new RedVariableProposals(robotModel, suiteFile,
                AssistProposalPredicates.<String> alwaysTrue());

        assertThat(provider.getVariableProposals("var", 0)).isEmpty();
    }

    @Test
    public void onlyLocalVarTableVariablesMatchingGivenPrefixAreProvided_whenDefaultMatcherIsUsed() throws Exception {
        final RobotProject robotProject = robotModel.createRobotProject(projectProvider.getProject());
        createGlobalVariables(robotProject);
        createGlobalVarFilesVariables(robotProject);

        final IFile file = projectProvider.createFile("file.robot",
                "*** Settings ***",
                "*** Variables ***",
                "${a_vt}  1",
                "${b_vt}  2",
                "${c_vt}  3",
                "*** Test Cases ***");
        final RobotSuiteFile suiteFile = robotModel.createSuiteFile(file);

        final RedVariableProposals provider = new RedVariableProposals(robotModel, suiteFile,
                AssistProposalPredicates.<String> alwaysTrue());
        final List<? extends AssistProposal> proposals = provider.getVariableProposals("${b_v", 0);

        assertThat(transform(proposals, toLabels())).containsExactly("${b_vt}");
    }

    @Test
    public void onlyLocalVarTableVariablesMatchedByGivenMatcherAreProvided_whenProvidingCustomMatcher()
            throws Exception {
        final RobotProject robotProject = robotModel.createRobotProject(projectProvider.getProject());
        createGlobalVariables(robotProject);
        createGlobalVarFilesVariables(robotProject);

        final IFile file = projectProvider.createFile("file.robot",
                "*** Settings ***",
                "*** Variables ***",
                "${a_vt}  1",
                "${b_vt}  2",
                "${c_vt}  3",
                "*** Test Cases ***");
        final RobotSuiteFile suiteFile = robotModel.createSuiteFile(file);

        final RedVariableProposals provider = new RedVariableProposals(robotModel, suiteFile, substringMatcher(),
                AssistProposalPredicates.<String> alwaysTrue());
        final List<? extends AssistProposal> proposals = provider.getVariableProposals("c", 0);

        assertThat(transform(proposals, toLabels())).containsExactly("${c_vt}");
    }

    @Test
    public void allLocalVarTableVariablesAreProvided_whenTheyAreMatched()
            throws Exception {
        final RobotProject robotProject = robotModel.createRobotProject(projectProvider.getProject());
        createGlobalVariables(robotProject);
        createGlobalVarFilesVariables(robotProject);

        final IFile file = projectProvider.createFile("file.robot",
                "*** Settings ***",
                "*** Variables ***",
                "${a_vt}  1",
                "${b_vt}  2",
                "${c_vt}  3",
                "*** Test Cases ***");
        final RobotSuiteFile suiteFile = robotModel.createSuiteFile(file);

        final RedVariableProposals provider = new RedVariableProposals(robotModel, suiteFile,
                AssistProposalPredicates.<String> alwaysTrue());
        final List<? extends AssistProposal> proposals = provider.getVariableProposals("", 0);

        assertThat(transform(proposals, toLabels())).containsExactly("${a_vt}", "${b_vt}", "${c_vt}");
    }

    @Test
    public void allLocalVarTableVariablesAreProvidedInOrderInducedByGivenComparator_whenCustomComparatorIsProvided()
            throws Exception {
        final RobotProject robotProject = robotModel.createRobotProject(projectProvider.getProject());
        createGlobalVariables(robotProject);
        createGlobalVarFilesVariables(robotProject);

        final IFile file = projectProvider.createFile("file.robot",
                "*** Settings ***",
                "*** Variables ***",
                "${a_vt}  1",
                "${b_vt}  2",
                "*** Test Cases ***");
        final RobotSuiteFile suiteFile = robotModel.createSuiteFile(file);

        final RedVariableProposals provider = new RedVariableProposals(robotModel, suiteFile,
                AssistProposalPredicates.<String> alwaysTrue());
        final Comparator<? super RedVariableProposal> comparator = firstProposalContaining("b");
        final List<? extends AssistProposal> proposals = provider.getVariableProposals("", comparator, 0);

        assertThat(transform(proposals, toLabels())).containsExactly("${b_vt}", "${a_vt}");
    }

    @Test
    public void noResourceVariablesAreProvided_whenTheyDoNotMatchToGivenContentAndDefaultMatcherIsUsed()
            throws Exception {
        final RobotProject robotProject = robotModel.createRobotProject(projectProvider.getProject());
        createGlobalVariables(robotProject);
        createGlobalVarFilesVariables(robotProject);

        final IFile file = projectProvider.createFile("file.robot",
                "*** Settings ***",
                "Resource  res.robot",
                "*** Test Cases ***");
        final RobotSuiteFile suiteFile = robotModel.createSuiteFile(file);

        final RedVariableProposals provider = new RedVariableProposals(robotModel, suiteFile,
                AssistProposalPredicates.<String> alwaysTrue());

        assertThat(provider.getVariableProposals("var", 0)).isEmpty();
    }

    @Test
    public void onlyResourceVariablesMatchingGivenPrefixAreProvided_whenDefaultMatcherIsUsed() throws Exception {
        final RobotProject robotProject = robotModel.createRobotProject(projectProvider.getProject());
        createGlobalVariables(robotProject);
        createGlobalVarFilesVariables(robotProject);

        final IFile file = projectProvider.createFile("file.robot",
                "*** Settings ***",
                "Resource  res.robot",
                "*** Test Cases ***");
        final RobotSuiteFile suiteFile = robotModel.createSuiteFile(file);

        final RedVariableProposals provider = new RedVariableProposals(robotModel, suiteFile,
                AssistProposalPredicates.<String> alwaysTrue());
        final List<? extends AssistProposal> proposals = provider.getVariableProposals("${b_", 0);

        assertThat(transform(proposals, toLabels())).containsExactly("${b_res_var}");
    }

    @Test
    public void onlyResourceVariablesMatchedByGivenMatcherAreProvided_whenProvidingCustomMatcher()
            throws Exception {
        final RobotProject robotProject = robotModel.createRobotProject(projectProvider.getProject());
        createGlobalVariables(robotProject);
        createGlobalVarFilesVariables(robotProject);

        final IFile file = projectProvider.createFile("file.robot",
                "*** Settings ***",
                "Resource  res.robot",
                "*** Test Cases ***");
        final RobotSuiteFile suiteFile = robotModel.createSuiteFile(file);

        final RedVariableProposals provider = new RedVariableProposals(robotModel, suiteFile, substringMatcher(),
                AssistProposalPredicates.<String> alwaysTrue());
        final List<? extends AssistProposal> proposals = provider.getVariableProposals("c", 0);

        assertThat(transform(proposals, toLabels())).containsExactly("${c_res_var}");
    }

    @Test
    public void allResourceVariablesAreProvided_whenTheyAreMatched() throws Exception {
        final RobotProject robotProject = robotModel.createRobotProject(projectProvider.getProject());
        createGlobalVariables(robotProject);
        createGlobalVarFilesVariables(robotProject);

        final IFile file = projectProvider.createFile("file.robot",
                "*** Settings ***",
                "Resource  res.robot",
                "*** Test Cases ***");
        final RobotSuiteFile suiteFile = robotModel.createSuiteFile(file);

        final RedVariableProposals provider = new RedVariableProposals(robotModel, suiteFile,
                AssistProposalPredicates.<String> alwaysTrue());
        final List<? extends AssistProposal> proposals = provider.getVariableProposals("", 0);

        assertThat(transform(proposals, toLabels())).containsExactly("${a_res_var}", "${b_res_var}", "${c_res_var}");
    }

    @Test
    public void allResourceVariablesAreProvidedInOrderInducedByGivenComparator_whenCustomComparatorIsProvided()
            throws Exception {
        final RobotProject robotProject = robotModel.createRobotProject(projectProvider.getProject());
        createGlobalVariables(robotProject);
        createGlobalVarFilesVariables(robotProject);

        final IFile file = projectProvider.createFile("file.robot",
                "*** Settings ***",
                "Resource  res.robot",
                "*** Test Cases ***");
        final RobotSuiteFile suiteFile = robotModel.createSuiteFile(file);

        final RedVariableProposals provider = new RedVariableProposals(robotModel, suiteFile,
                AssistProposalPredicates.<String> alwaysTrue());
        final Comparator<? super RedVariableProposal> comparator = firstProposalContaining("b");
        final List<? extends AssistProposal> proposals = provider.getVariableProposals("", comparator, 0);

        assertThat(transform(proposals, toLabels())).containsExactly("${b_res_var}", "${a_res_var}", "${c_res_var}");
    }

    @Test
    public void noLocalVariablesAreProvided_whenTheyDoNotMatchToGivenContentAndDefaultMatcherIsUsed()
            throws Exception {
        final RobotProject robotProject = robotModel.createRobotProject(projectProvider.getProject());
        createGlobalVariables(robotProject);
        createGlobalVarFilesVariables(robotProject);

        final IFile file = projectProvider.createFile("file.robot",
                "*** Keywords ***",
                "kw",
                "  [Arguments]  ${a_arg}  ${b_arg}",
                "  ${a_local}=  call1",
                "  ${b_local}=  call2",
                "  log",
                "*** Test Cases ***");
        final RobotSuiteFile suiteFile = robotModel.createSuiteFile(file);

        final RedVariableProposals provider = new RedVariableProposals(robotModel, suiteFile,
                AssistProposalPredicates.<String> alwaysTrue());

        assertThat(provider.getVariableProposals("var", 100)).isEmpty();
    }

    @Test
    public void onlyLocalVariablesMatchingGivenPrefixAreProvided_whenDefaultMatcherIsUsed_usingOffset()
            throws Exception {
        final RobotProject robotProject = robotModel.createRobotProject(projectProvider.getProject());
        createGlobalVariables(robotProject);
        createGlobalVarFilesVariables(robotProject);

        final IFile file = projectProvider.createFile("file.robot",
                "*** Keywords ***",
                "kw",
                "  [Arguments]  ${a_arg}  ${b_arg}",
                "  ${a_local}=  call1",
                "  ${b_local}=  call2",
                "  log",
                "*** Test Cases ***");
        final RobotSuiteFile suiteFile = robotModel.createSuiteFile(file);

        final RedVariableProposals provider = new RedVariableProposals(robotModel, suiteFile,
                AssistProposalPredicates.<String> alwaysTrue());
        final List<? extends AssistProposal> proposals = provider.getVariableProposals("${b_", 100);

        assertThat(transform(proposals, toLabels())).containsExactly("${b_arg}", "${b_local}");
    }

    @Test
    public void onlyLocalVariablesMatchingGivenPrefixAreProvided_whenDefaultMatcherIsUsed_usingElement()
            throws Exception {
        final RobotProject robotProject = robotModel.createRobotProject(projectProvider.getProject());
        createGlobalVariables(robotProject);
        createGlobalVarFilesVariables(robotProject);

        final IFile file = projectProvider.createFile("file.robot",
                "*** Keywords ***",
                "kw",
                "  [Arguments]  ${a_arg}  ${b_arg}",
                "  ${a_local}=  call1",
                "  ${b_local}=  call2",
                "  log",
                "*** Test Cases ***");
        final RobotSuiteFile suiteFile = robotModel.createSuiteFile(file);
        final RobotFileInternalElement logCall = suiteFile.findSection(RobotKeywordsSection.class).get()
                .getChildren().get(0).getChildren().get(3);
        
        final RedVariableProposals provider = new RedVariableProposals(robotModel, suiteFile,
                AssistProposalPredicates.<String> alwaysTrue());
        final List<? extends AssistProposal> proposals = provider.getVariableProposals("${b_", logCall);

        assertThat(transform(proposals, toLabels())).containsExactly("${b_arg}", "${b_local}");
    }

    @Test
    public void onlyLocalVariablesMatchedByGivenMatcherAreProvided_whenProvidingCustomMatcher_usingOffset()
            throws Exception {
        final RobotProject robotProject = robotModel.createRobotProject(projectProvider.getProject());
        createGlobalVariables(robotProject);
        createGlobalVarFilesVariables(robotProject);

        final IFile file = projectProvider.createFile("file.robot",
                "*** Keywords ***",
                "kw",
                "  [Arguments]  ${a_arg}  ${b_arg}",
                "  ${a_local}=  call1",
                "  ${b_local}=  call2",
                "  log",
                "*** Test Cases ***");
        final RobotSuiteFile suiteFile = robotModel.createSuiteFile(file);

        final RedVariableProposals provider = new RedVariableProposals(robotModel, suiteFile, substringMatcher(),
                AssistProposalPredicates.<String> alwaysTrue());
        final List<? extends AssistProposal> proposals = provider.getVariableProposals("b", 100);

        assertThat(transform(proposals, toLabels())).containsExactly("${b_arg}", "${b_local}");
    }

    @Test
    public void onlyLocalVariablesMatchedByGivenMatcherAreProvided_whenProvidingCustomMatcher_usingElement()
            throws Exception {
        final RobotProject robotProject = robotModel.createRobotProject(projectProvider.getProject());
        createGlobalVariables(robotProject);
        createGlobalVarFilesVariables(robotProject);

        final IFile file = projectProvider.createFile("file.robot",
                "*** Keywords ***",
                "kw",
                "  [Arguments]  ${a_arg}  ${b_arg}",
                "  ${a_local}=  call1",
                "  ${b_local}=  call2",
                "  log",
                "*** Test Cases ***");
        final RobotSuiteFile suiteFile = robotModel.createSuiteFile(file);
        final RobotFileInternalElement logCall = suiteFile.findSection(RobotKeywordsSection.class).get()
                .getChildren().get(0).getChildren().get(3);

        final RedVariableProposals provider = new RedVariableProposals(robotModel, suiteFile, substringMatcher(),
                AssistProposalPredicates.<String> alwaysTrue());
        final List<? extends AssistProposal> proposals = provider.getVariableProposals("b", logCall);

        assertThat(transform(proposals, toLabels())).containsExactly("${b_arg}", "${b_local}");
    }

    @Test
    public void allLocalVariablesAreProvidedDependingOnOffset_whenTheyAreMatched_1() throws Exception {
        final RobotProject robotProject = robotModel.createRobotProject(projectProvider.getProject());
        createGlobalVariables(robotProject);
        createGlobalVarFilesVariables(robotProject);

        final IFile file = projectProvider.createFile("file.robot",
                "*** Keywords ***",
                "kw",
                "  [Arguments]  ${a_arg}  ${b_arg}",
                "  ${a_local}=  call1",
                "  ${b_local}=  call2",
                "  log",
                "*** Test Cases ***");
        final RobotSuiteFile suiteFile = robotModel.createSuiteFile(file);

        final RedVariableProposals provider = new RedVariableProposals(robotModel, suiteFile,
                AssistProposalPredicates.<String> alwaysTrue());
        final List<? extends AssistProposal> proposals = provider.getVariableProposals("", 100);

        assertThat(transform(proposals, toLabels())).containsExactly("${a_arg}", "${a_local}", "${b_arg}", "${b_local}");
    }

    @Test
    public void allLocalVariablesAreProvidedDependingOnElement_whenTheyAreMatched_1() throws Exception {
        final RobotProject robotProject = robotModel.createRobotProject(projectProvider.getProject());
        createGlobalVariables(robotProject);
        createGlobalVarFilesVariables(robotProject);

        final IFile file = projectProvider.createFile("file.robot",
                "*** Keywords ***",
                "kw",
                "  [Arguments]  ${a_arg}  ${b_arg}",
                "  ${a_local}=  call1",
                "  ${b_local}=  call2",
                "  log",
                "*** Test Cases ***");
        final RobotSuiteFile suiteFile = robotModel.createSuiteFile(file);
        final RobotFileInternalElement logCall = suiteFile.findSection(RobotKeywordsSection.class).get()
                .getChildren().get(0).getChildren().get(3);

        final RedVariableProposals provider = new RedVariableProposals(robotModel, suiteFile,
                AssistProposalPredicates.<String> alwaysTrue());
        final List<? extends AssistProposal> proposals = provider.getVariableProposals("", logCall);

        assertThat(transform(proposals, toLabels())).containsExactly("${a_arg}", "${a_local}", "${b_arg}", "${b_local}");
    }

    @Test
    public void allLocalVariablesAreProvidedDependingOnOffset_whenTheyAreMatched_2() throws Exception {
        final RobotProject robotProject = robotModel.createRobotProject(projectProvider.getProject());
        createGlobalVariables(robotProject);
        createGlobalVarFilesVariables(robotProject);

        final IFile file = projectProvider.createFile("file.robot",
                "*** Keywords ***",
                "kw",
                "  [Arguments]  ${a_arg}  ${b_arg}",
                "  ${a_local}=  call1",
                "  ${b_local}=  call2",
                "  log",
                "*** Test Cases ***");
        final RobotSuiteFile suiteFile = robotModel.createSuiteFile(file);

        final RedVariableProposals provider = new RedVariableProposals(robotModel, suiteFile,
                AssistProposalPredicates.<String> alwaysTrue());
        final List<? extends AssistProposal> proposals = provider.getVariableProposals("", 90);

        assertThat(transform(proposals, toLabels())).containsExactly("${a_arg}", "${a_local}", "${b_arg}");
    }

    @Test
    public void allLocalVariablesAreProvidedDependingOnElement_whenTheyAreMatched_2() throws Exception {
        final RobotProject robotProject = robotModel.createRobotProject(projectProvider.getProject());
        createGlobalVariables(robotProject);
        createGlobalVarFilesVariables(robotProject);

        final IFile file = projectProvider.createFile("file.robot",
                "*** Keywords ***",
                "kw",
                "  [Arguments]  ${a_arg}  ${b_arg}",
                "  ${a_local}=  call1",
                "  ${b_local}=  call2",
                "  log",
                "*** Test Cases ***");
        final RobotSuiteFile suiteFile = robotModel.createSuiteFile(file);
        final RobotFileInternalElement call2Call = suiteFile.findSection(RobotKeywordsSection.class).get()
                .getChildren().get(0).getChildren().get(2);

        final RedVariableProposals provider = new RedVariableProposals(robotModel, suiteFile,
                AssistProposalPredicates.<String> alwaysTrue());
        final List<? extends AssistProposal> proposals = provider.getVariableProposals("", call2Call);

        assertThat(transform(proposals, toLabels())).containsExactly("${a_arg}", "${a_local}", "${b_arg}");
    }

    @Test
    public void allLocalVariablesAreProvidedDependingOnOffset_whenTheyAreMatched_3() throws Exception {
        final RobotProject robotProject = robotModel.createRobotProject(projectProvider.getProject());
        createGlobalVariables(robotProject);
        createGlobalVarFilesVariables(robotProject);

        final IFile file = projectProvider.createFile("file.robot",
                "*** Keywords ***",
                "kw",
                "  [Arguments]  ${a_arg}  ${b_arg}",
                "  ${a_local}=  call1",
                "  ${b_local}=  call2",
                "  log",
                "*** Test Cases ***");
        final RobotSuiteFile suiteFile = robotModel.createSuiteFile(file);

        final RedVariableProposals provider = new RedVariableProposals(robotModel, suiteFile,
                AssistProposalPredicates.<String> alwaysTrue());
        final List<? extends AssistProposal> proposals = provider.getVariableProposals("", 70);

        assertThat(transform(proposals, toLabels())).containsExactly("${a_arg}", "${b_arg}");
    }

    @Test
    public void allLocalVariablesAreProvidedDependingOnElement_whenTheyAreMatched_3() throws Exception {
        final RobotProject robotProject = robotModel.createRobotProject(projectProvider.getProject());
        createGlobalVariables(robotProject);
        createGlobalVarFilesVariables(robotProject);

        final IFile file = projectProvider.createFile("file.robot",
                "*** Keywords ***",
                "kw",
                "  [Arguments]  ${a_arg}  ${b_arg}",
                "  ${a_local}=  call1",
                "  ${b_local}=  call2",
                "  log",
                "*** Test Cases ***");
        final RobotSuiteFile suiteFile = robotModel.createSuiteFile(file);
        final RobotFileInternalElement call1Call = suiteFile.findSection(RobotKeywordsSection.class).get()
                .getChildren().get(0).getChildren().get(1);

        final RedVariableProposals provider = new RedVariableProposals(robotModel, suiteFile,
                AssistProposalPredicates.<String> alwaysTrue());
        final List<? extends AssistProposal> proposals = provider.getVariableProposals("", call1Call);

        assertThat(transform(proposals, toLabels())).containsExactly("${a_arg}", "${b_arg}");
    }

    @Test
    public void allLocalVariablesAreProvidedInOrderInducedByGivenComparator_whenCustomComparatorIsProvided()
            throws Exception {
        final RobotProject robotProject = robotModel.createRobotProject(projectProvider.getProject());
        createGlobalVariables(robotProject);
        createGlobalVarFilesVariables(robotProject);

        final IFile file = projectProvider.createFile("file.robot",
                "*** Keywords ***",
                "kw",
                "  [Arguments]  ${a_arg}  ${b_arg}",
                "  ${a_local}=  call1",
                "  ${b_local}=  call2",
                "  log",
                "*** Test Cases ***");
        final RobotSuiteFile suiteFile = robotModel.createSuiteFile(file);

        final RedVariableProposals provider = new RedVariableProposals(robotModel, suiteFile,
                AssistProposalPredicates.<String> alwaysTrue());
        final Comparator<? super RedVariableProposal> comparator = firstProposalContaining("b");
        final List<? extends AssistProposal> proposals = provider.getVariableProposals("", comparator, 100);

        assertThat(transform(proposals, toLabels())).containsExactly("${b_local}", "${b_arg}", "${a_arg}",
                "${a_local}");
    }

    private static void createGlobalVariables(final RobotProject robotProject, final String... vars) {
        final List<ARobotInternalVariable<?>> variables = new ArrayList<>();
        for (final String varName : vars) {
            variables.add(new ScalarRobotInternalVariable(varName, "some_value"));
        }
        final List<ARobotInternalVariable<?>> globalVariables = robotProject.getRobotProjectHolder()
                .getGlobalVariables();
        globalVariables.clear();
        globalVariables.addAll(variables);
    }

    private static void createGlobalVarFilesVariables(final RobotProject robotProject, final String... vars) {
        final Map<String, Object> variables = new HashMap<>();
        for (final String varName : vars) {
            variables.put(varName, "some_value");
        }
        final ReferencedVariableFile varsImportRef = new ReferencedVariableFile();
        varsImportRef.setVariables(variables);
        robotProject.setReferencedVariablesFiles(newArrayList(varsImportRef));
    }

    private static void addLocalVarFileVariables(final RobotSuiteFile suiteFile, final String... vars) {
        final Map<String, Object> variables = new HashMap<>();
        for (final String varName : vars) {
            variables.put(varName, "some_value");
        }

        final VariablesImport varsImport = (VariablesImport) suiteFile.findSection(RobotSettingsSection.class)
                .get().getChildren().get(0).getLinkedElement();
        final VariablesFileImportReference varsImportRef = new VariablesFileImportReference(varsImport);
        varsImportRef.map(variables);
        final RobotFileOutput output = suiteFile.getLinkedElement().getParent();
        output.setVariablesImportReferences(newArrayList(varsImportRef));
    }

    private static Comparator<? super RedVariableProposal> firstProposalContaining(final String toContain) {
        return new Comparator<RedVariableProposal>() {

            @Override
            public int compare(final RedVariableProposal p1, final RedVariableProposal p2) {
                if (p1.equals(p2)) {
                    return 0;
                } else if (p1.getLabel().contains(toContain)) {
                    return -1;
                } else {
                    return 1;
                }
            }
        };
    }
}
