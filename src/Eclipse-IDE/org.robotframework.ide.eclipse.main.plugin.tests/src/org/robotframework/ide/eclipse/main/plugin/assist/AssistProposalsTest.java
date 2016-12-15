/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.assist;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.rf.ide.core.testdata.model.search.keyword.KeywordScope;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.RobotSuiteFileCreator;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariable;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariablesSection;
import org.robotframework.ide.eclipse.main.plugin.project.library.ArgumentsDescriptor;
import org.robotframework.ide.eclipse.main.plugin.project.library.KeywordSpecification;
import org.robotframework.ide.eclipse.main.plugin.project.library.LibrarySpecification;
import org.robotframework.red.junit.ProjectProvider;

public class AssistProposalsTest {

    private static final String PROJECT_NAME = AssistProposalsTest.class.getSimpleName();

    @ClassRule
    public static ProjectProvider projectProvider = new ProjectProvider(PROJECT_NAME);

    private static IFile file;

    @BeforeClass
    public static void beforeSuite() throws Exception {
        file = projectProvider.createFile(new Path("suite.robot"),
                "*** Keywords ***",
                "kw1",
                "  [Arguments]  ${x}  @{list}",
                "  [Documentation]  myDocumentation",
                "  Log  10",
                "kw2",
                "  Log  20");
    }

    @AfterClass
    public static void afterSuite() {
        file = null;
    }

    @Test
    public void verifyProposalPropertiesMadeFromUserKeyword() throws Exception {
        final RobotSuiteFile fileModel = new RobotModel().createSuiteFile(file);

        final RobotKeywordDefinition kw1 = fileModel.findSection(RobotKeywordsSection.class).get().getChildren().get(0);

        final RedKeywordProposal proposal = AssistProposals.createUserKeywordProposal(kw1, "Given ", KeywordScope.LOCAL,
                "suite", AssistProposalPredicates.<RedKeywordProposal> alwaysFalse(), ProposalMatch.EMPTY);

        assertThat(proposal.getStyledLabel().getString()).isEqualTo("kw1 - suite.robot");
        assertThat(proposal.getContent()).isEqualTo("Given kw1");
        assertThat(proposal.getSourceName()).isEqualTo("suite");
        assertThat(proposal.getArguments()).containsExactly("x", "");
        assertThat(proposal.getArgumentsDescriptor()).isEqualTo(ArgumentsDescriptor.createDescriptor("x", "*list"));
        assertThat(proposal.getImage()).isEqualTo(RedImages.getUserKeywordImage());
        assertThat(proposal.hasDescription()).isTrue();
        assertThat(proposal.getDescription()).contains("myDocumentation");
    }

    @Test
    public void verifyProposalPropertiesMadeFromLibraryKeyword() throws Exception, CoreException {
        final LibrarySpecification libSpec = new LibrarySpecification();
        libSpec.setName("myLibrary");
        final KeywordSpecification kwSpec = new KeywordSpecification();
        kwSpec.setFormat("ROBOT");
        kwSpec.setName("libKw");
        kwSpec.setArguments(newArrayList("x", "*list"));
        kwSpec.setDocumentation("myDocumentation");

        final RedKeywordProposal proposal = AssistProposals.createLibraryKeywordProposal(libSpec, kwSpec, "Given ",
                KeywordScope.STD_LIBRARY, "myLibrary", new Path("lib.py"),
                AssistProposalPredicates.<RedKeywordProposal> alwaysFalse(), ProposalMatch.EMPTY);

        assertThat(proposal.getStyledLabel().getString()).isEqualTo("libKw - myLibrary");
        assertThat(proposal.getContent()).isEqualTo("Given libKw");
        assertThat(proposal.getSourceName()).isEqualTo("myLibrary");
        assertThat(proposal.getArguments()).containsExactly("x", "");
        assertThat(proposal.getArgumentsDescriptor()).isEqualTo(ArgumentsDescriptor.createDescriptor("x", "*list"));
        assertThat(proposal.getImage()).isEqualTo(RedImages.getKeywordImage());
        assertThat(proposal.hasDescription()).isTrue();
        assertThat(proposal.getDescription()).contains("myDocumentation");
    }

    @Test
    public void verifyProposalPropertiesMadeFromBuiltInVariable() {
        final RedVariableProposal proposal = AssistProposals.createBuiltInVariableProposal("${VAR}", "value",
                ProposalMatch.EMPTY);

        assertThat(proposal.getStyledLabel().getString()).isEqualTo("${VAR}");
        assertThat(proposal.getContent()).isEqualTo("${VAR}");
        assertThat(proposal.getArguments()).isEmpty();
        assertThat(proposal.getImage()).isEqualTo(RedImages.getRobotScalarVariableImage());
        assertThat(proposal.hasDescription()).isTrue();
        assertThat(proposal.getDescription()).isEqualTo("Source: Built-in\nValue: value");
    }

    @Test
    public void verifyProposalPropertiesMadeFromVariableFiles() {
        final RedVariableProposal proposal = AssistProposals.createVarFileVariableProposal("@{var}", "value",
                "varfile.py", ProposalMatch.EMPTY);

        assertThat(proposal.getStyledLabel().getString()).isEqualTo("@{var}");
        assertThat(proposal.getContent()).isEqualTo("@{var}");
        assertThat(proposal.getArguments()).isEmpty();
        assertThat(proposal.getImage()).isEqualTo(RedImages.getRobotListVariableImage());
        assertThat(proposal.hasDescription()).isTrue();
        assertThat(proposal.getDescription()).isEqualTo("Source: varfile.py\nValue: value");
    }

    @Test
    public void verifyProposalPropertiesMadeFromLocalDefinition_1() {
        final RedVariableProposal proposal = AssistProposals.createLocalVariableProposal("${var}=10", "resfile.robot",
                ProposalMatch.EMPTY);

        assertThat(proposal.getStyledLabel().getString()).isEqualTo("${var}");
        assertThat(proposal.getContent()).isEqualTo("${var}");
        assertThat(proposal.getArguments()).isEmpty();
        assertThat(proposal.getImage()).isEqualTo(RedImages.getRobotScalarVariableImage());
        assertThat(proposal.hasDescription()).isTrue();
        assertThat(proposal.getDescription()).isEqualTo("Source: resfile.robot");
    }

    @Test
    public void verifyProposalPropertiesMadeFromLocalDefinition_2() {
        final RedVariableProposal proposal = AssistProposals.createLocalVariableProposal("&{var}", "resfile.robot",
                ProposalMatch.EMPTY);

        assertThat(proposal.getStyledLabel().getString()).isEqualTo("&{var}");
        assertThat(proposal.getContent()).isEqualTo("&{var}");
        assertThat(proposal.getArguments()).isEmpty();
        assertThat(proposal.getImage()).isEqualTo(RedImages.getRobotDictionaryVariableImage());
        assertThat(proposal.hasDescription()).isTrue();
        assertThat(proposal.getDescription()).isEqualTo("Source: resfile.robot");
    }

    @Test
    public void verifyProposalPropertiesMadeFromModelPart() {
        final RobotVariable variable = createVariable();
        final RedVariableProposal proposal = AssistProposals.createUserVariableProposal(variable, ProposalMatch.EMPTY);

        assertThat(proposal.getStyledLabel().getString()).isEqualTo("${scalar}");
        assertThat(proposal.getContent()).isEqualTo("${scalar}");
        assertThat(proposal.getArguments()).isEmpty();
        assertThat(proposal.getImage()).isEqualTo(RedImages.getRobotScalarVariableImage());
        assertThat(proposal.hasDescription()).isTrue();
        assertThat(proposal.getDescription()).isEqualTo("Source: file.robot\nValue: 0\nComment: #something");
    }

    private static RobotVariable createVariable() {
        final RobotSuiteFile model = new RobotSuiteFileCreator().appendLine("*** Variables ***")
                .appendLine("${scalar}  0  #something")
                .build();
        return model.findSection(RobotVariablesSection.class).get().getChildren().get(0);
    }
}
