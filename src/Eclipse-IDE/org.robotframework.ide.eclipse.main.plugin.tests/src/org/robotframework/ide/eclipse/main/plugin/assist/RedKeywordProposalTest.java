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
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.assist.RedKeywordProposal.KeywordType;
import org.robotframework.ide.eclipse.main.plugin.model.KeywordScope;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.project.library.KeywordSpecification;
import org.robotframework.ide.eclipse.main.plugin.project.library.LibrarySpecification;
import org.robotframework.red.junit.ProjectProvider;

import com.google.common.collect.Range;

public class RedKeywordProposalTest {

    private static final String PROJECT_NAME = RedKeywordProposalTest.class.getSimpleName();

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

        final RobotKeywordDefinition kw1 = fileModel.findSection(RobotKeywordsSection.class)
                .get()
                .getChildren()
                .get(0);

        final RedKeywordProposal proposal = RedKeywordProposal.create(fileModel, kw1, KeywordScope.LOCAL,
                "suite.robot");

        assertThat(proposal.getLabel()).isEqualTo("kw1");
        assertThat(proposal.getLabelDecoration()).isEqualTo("- suite.robot");

        assertThat(proposal.getContent()).isEqualTo("kw1");
        assertThat(proposal.getType()).isEqualTo(KeywordType.USER_DEFINED);

        assertThat(proposal.getSourceName()).isEqualTo("User Defined (/" + PROJECT_NAME + "/suite.robot)");
        assertThat(proposal.getSourcePrefix()).isEqualTo("suite.robot");

        assertThat(proposal.getNumberOfArguments()).isEqualTo(Range.atLeast(1));
        assertThat(proposal.getRequiredArguments()).containsOnly("x");
        assertThat(proposal.getArgumentsLabel()).isEqualTo("[x, *list]");

        assertThat(proposal.getImage()).isEqualTo(RedImages.getUserKeywordImage());

        assertThat(proposal.hasDescription()).isTrue();
        assertThat(proposal.getDocumentation()).contains(proposal.getContent(), proposal.getSourceName(),
                proposal.getArgumentsLabel(), "myDocumentation");
        assertThat(proposal.getHtmlDocumentation()).contains("myDocumentation");
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
        final RedKeywordProposal proposal = RedKeywordProposal.create(libSpec, kwSpec, KeywordScope.STD_LIBRARY,
                "source", new Path("libpath"));

        assertThat(proposal.getLabel()).isEqualTo("libKw");
        assertThat(proposal.getLabelDecoration()).isEqualTo("- myLibrary");

        assertThat(proposal.getContent()).isEqualTo("libKw");
        assertThat(proposal.getType()).isEqualTo(KeywordType.LIBRARY);

        assertThat(proposal.getSourceName()).isEqualTo("myLibrary");
        assertThat(proposal.getSourcePrefix()).isEqualTo("source");

        assertThat(proposal.getNumberOfArguments()).isEqualTo(Range.atLeast(1));
        assertThat(proposal.getRequiredArguments()).containsOnly("x");
        assertThat(proposal.getArgumentsLabel()).isEqualTo("[x, *list]");

        assertThat(proposal.getImage()).isEqualTo(RedImages.getKeywordImage());

        assertThat(proposal.hasDescription()).isTrue();
        assertThat(proposal.getDocumentation()).contains(proposal.getContent(), proposal.getSourceName(),
                proposal.getArgumentsLabel(), "myDocumentation");
        assertThat(proposal.getHtmlDocumentation()).contains("myDocumentation");
    }
}
