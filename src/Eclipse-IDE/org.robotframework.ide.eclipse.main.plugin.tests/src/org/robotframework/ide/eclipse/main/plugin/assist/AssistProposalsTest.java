/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.assist;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.transform;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.rf.ide.core.project.RobotProjectConfig.RemoteLocation;
import org.rf.ide.core.testdata.model.search.keyword.KeywordScope;
import org.rf.ide.core.testdata.model.table.variables.AVariable.VariableType;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.assist.RedSettingProposals.SettingTarget;
import org.robotframework.ide.eclipse.main.plugin.assist.RedVariableProposal.VariableOrigin;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.RobotSuiteFileCreator;
import org.robotframework.ide.eclipse.main.plugin.model.LibraryDescriptor;
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

    @ClassRule
    public static ProjectProvider projectProvider = new ProjectProvider(AssistProposalsTest.class);

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
    public void verifyKeywordProposalPropertiesMadeFromUserKeyword() throws Exception {
        final RobotSuiteFile fileModel = new RobotModel().createSuiteFile(file);

        final RobotKeywordDefinition kw1 = fileModel.findSection(RobotKeywordsSection.class).get().getChildren().get(0);

        final RedKeywordProposal proposal = AssistProposals.createUserKeywordProposal(kw1, "Given ", KeywordScope.LOCAL,
                AssistProposalPredicates.alwaysFalse(), ProposalMatch.EMPTY);

        assertThat(proposal.getContent()).isEqualTo("Given kw1");
        assertThat(proposal.getSourceName()).isEqualTo("suite");
        assertThat(proposal.getArguments()).containsExactly("x", "");
        assertThat(proposal.getArgumentsDescriptor()).isEqualTo(ArgumentsDescriptor.createDescriptor("x", "*list"));
        assertThat(proposal.getImage()).isEqualTo(RedImages.getUserKeywordImage());
        assertThat(proposal.getStyledLabel().getString()).isEqualTo("kw1 - suite.robot");
        assertThat(proposal.hasDescription()).isTrue();
        assertThat(proposal.getDescription()).contains("myDocumentation");
        assertThat(proposal.isAccessible()).isTrue();
    }

    @Test
    public void verifyKeywordProposalPropertiesMadeFromLibraryKeyword() throws Exception, CoreException {
        final LibrarySpecification libSpec = new LibrarySpecification();
        libSpec.setName("myLibrary");
        final KeywordSpecification kwSpec = new KeywordSpecification();
        kwSpec.setFormat("ROBOT");
        kwSpec.setName("libKw");
        kwSpec.setArguments(newArrayList("x", "*list"));
        kwSpec.setDocumentation("myDocumentation");

        final RedKeywordProposal proposal = AssistProposals.createLibraryKeywordProposal(libSpec, kwSpec, "Given ",
                KeywordScope.STD_LIBRARY, Optional.empty(), new Path("test.robot"),
                AssistProposalPredicates.alwaysFalse(), ProposalMatch.EMPTY);

        assertThat(proposal.getContent()).isEqualTo("Given libKw");
        assertThat(proposal.getSourceName()).isEqualTo("myLibrary");
        assertThat(proposal.getArguments()).containsExactly("x", "");
        assertThat(proposal.getArgumentsDescriptor()).isEqualTo(ArgumentsDescriptor.createDescriptor("x", "*list"));
        assertThat(proposal.getImage()).isEqualTo(RedImages.getKeywordImage());
        assertThat(proposal.getStyledLabel().getString()).isEqualTo("libKw - myLibrary");
        assertThat(proposal.hasDescription()).isTrue();
        assertThat(proposal.getDescription()).contains("myDocumentation");
        assertThat(proposal.isAccessible()).isTrue();
    }

    @Test
    public void verifyKeywordProposalPropertiesMadeFromNotImportedLibraryKeyword() throws Exception, CoreException {
        final LibrarySpecification libSpec = new LibrarySpecification();
        libSpec.setName("myNotImportedLibrary");
        final KeywordSpecification kwSpec = new KeywordSpecification();
        kwSpec.setFormat("ROBOT");
        kwSpec.setName("libKw");
        kwSpec.setArguments(newArrayList("x", "*list"));
        kwSpec.setDocumentation("myDocumentation");

        final RedKeywordProposal proposal = AssistProposals.createNotAccessibleLibraryKeywordProposal(libSpec, kwSpec,
                "Given ", KeywordScope.STD_LIBRARY, Optional.empty(), new Path("test.robot"),
                AssistProposalPredicates.alwaysFalse(), ProposalMatch.EMPTY);

        assertThat(proposal.getContent()).isEqualTo("Given libKw");
        assertThat(proposal.getSourceName()).isEqualTo("myNotImportedLibrary");
        assertThat(proposal.getArguments()).containsExactly("x", "");
        assertThat(proposal.getArgumentsDescriptor()).isEqualTo(ArgumentsDescriptor.createDescriptor("x", "*list"));
        assertThat(proposal.getImage()).isEqualTo(RedImages.getKeywordImage());
        assertThat(proposal.getStyledLabel().getString()).isEqualTo("libKw - myNotImportedLibrary");
        assertThat(proposal.hasDescription()).isTrue();
        assertThat(proposal.getDescription()).contains("myDocumentation");
        assertThat(proposal.isAccessible()).isFalse();
    }

    @Test
    public void verifyVariableProposalPropertiesMadeFromBuiltInVariable() {
        final RedVariableProposal proposal = AssistProposals.createBuiltInVariableProposal("${VAR}", "value",
                ProposalMatch.EMPTY);

        assertThat(proposal.getContent()).isEqualTo("${VAR}");
        assertThat(proposal.getArguments()).isEmpty();
        assertThat(proposal.getImage()).isEqualTo(RedImages.getRobotScalarVariableImage());
        assertThat(proposal.getStyledLabel().getString()).isEqualTo("${VAR}");
        assertThat(proposal.hasDescription()).isTrue();
        assertThat(proposal.getDescription()).isEqualTo("Source: Built-in\nValue: value");
    }

    @Test
    public void verifyVariableProposalPropertiesMadeFromVariableFiles() {
        final RedVariableProposal proposal = AssistProposals.createVarFileVariableProposal("@{var}", "value",
                "varfile.py", ProposalMatch.EMPTY);

        assertThat(proposal.getContent()).isEqualTo("@{var}");
        assertThat(proposal.getArguments()).isEmpty();
        assertThat(proposal.getImage()).isEqualTo(RedImages.getRobotListVariableImage());
        assertThat(proposal.getStyledLabel().getString()).isEqualTo("@{var}");
        assertThat(proposal.hasDescription()).isTrue();
        assertThat(proposal.getDescription()).isEqualTo("Source: varfile.py\nValue: value");
    }

    @Test
    public void verifyVariableProposalPropertiesMadeFromLocalDefinition_1() {
        final RedVariableProposal proposal = AssistProposals.createLocalVariableProposal("${var}=10", "resfile.robot",
                ProposalMatch.EMPTY);

        assertThat(proposal.getContent()).isEqualTo("${var}");
        assertThat(proposal.getArguments()).isEmpty();
        assertThat(proposal.getImage()).isEqualTo(RedImages.getRobotScalarVariableImage());
        assertThat(proposal.getStyledLabel().getString()).isEqualTo("${var}");
        assertThat(proposal.hasDescription()).isTrue();
        assertThat(proposal.getDescription()).isEqualTo("Source: resfile.robot");
    }

    @Test
    public void verifyVariableProposalPropertiesMadeFromLocalDefinition_2() {
        final RedVariableProposal proposal = AssistProposals.createLocalVariableProposal("&{var}", "resfile.robot",
                ProposalMatch.EMPTY);

        assertThat(proposal.getContent()).isEqualTo("&{var}");
        assertThat(proposal.getArguments()).isEmpty();
        assertThat(proposal.getImage()).isEqualTo(RedImages.getRobotDictionaryVariableImage());
        assertThat(proposal.getStyledLabel().getString()).isEqualTo("&{var}");
        assertThat(proposal.hasDescription()).isTrue();
        assertThat(proposal.getDescription()).isEqualTo("Source: resfile.robot");
    }

    @Test
    public void verifyVariableProposalPropertiesMadeFromModelPart() {
        final RobotVariable variable = createVariable();
        final RedVariableProposal proposal = AssistProposals.createUserVariableProposal(variable, ProposalMatch.EMPTY);

        assertThat(proposal.getContent()).isEqualTo("${scalar}");
        assertThat(proposal.getArguments()).isEmpty();
        assertThat(proposal.getImage()).isEqualTo(RedImages.getRobotScalarVariableImage());
        assertThat(proposal.getStyledLabel().getString()).isEqualTo("${scalar}");
        assertThat(proposal.hasDescription()).isTrue();
        assertThat(proposal.getDescription()).isEqualTo("Source: file.robot\nValue: 0\nComment: #something");
    }

    @Test
    public void verifySectionProposalProposal() {
        final RedSectionProposal proposal = AssistProposals.createSectionProposal("name", ProposalMatch.EMPTY);

        assertThat(proposal.getContent()).isEqualTo("*** name ***");
        assertThat(proposal.getArguments()).isEmpty();
        assertThat(proposal.getImage()).isEqualTo(RedImages.getRobotCasesFileSectionImage());
        assertThat(proposal.getStyledLabel().getString()).isEqualTo("*** name ***");
        assertThat(proposal.hasDescription()).isFalse();
        assertThat(proposal.getDescription()).isEmpty();
    }

    @Test
    public void verifyFileLocationProposalPropertiesMadeFromOneFileToAnother() throws CoreException, IOException {
        final IFolder dir = projectProvider.createDir("dir");
        final IFile toFile = projectProvider.createFile("dir/file.txt");

        final RedFileLocationProposal proposal = AssistProposals.createFileLocationProposal(file, toFile,
                ProposalMatch.EMPTY);
        assertThat(proposal.getContent()).isEqualTo("dir/file.txt");
        assertThat(proposal.getArguments()).isEmpty();
        assertThat(proposal.getImage()).isEqualTo(RedImages.getImageForResource(toFile));
        assertThat(proposal.getStyledLabel().getString()).isEqualTo("dir/file.txt");
        assertThat(proposal.hasDescription()).isFalse();
        assertThat(proposal.getDescription()).isEmpty();

        dir.delete(true, null);
    }

    @Test
    public void verifyLibraryProposalProperties() {
        final RobotSuiteFile suiteFile = new RobotModel().createSuiteFile(file);
        final LibrarySpecification libSpec = new LibrarySpecification();
        libSpec.setName("library");
        libSpec.setDocumentation("docu");
        libSpec.setDescriptor(LibraryDescriptor.ofStandardLibrary("library"));

        final RedLibraryProposal proposal = AssistProposals.createLibraryProposal(suiteFile, libSpec,
                ProposalMatch.EMPTY);
        assertThat(proposal.getContent()).isEqualTo("library");
        assertThat(proposal.getArguments()).isEmpty();
        assertThat(proposal.getImage()).isEqualTo(RedImages.getLibraryImage());
        assertThat(proposal.getStyledLabel().getString()).isEqualTo("library");
        assertThat(proposal.hasDescription()).isTrue();
        assertThat(proposal.getDescription()).isEqualTo("docu");
    }

    @Test
    public void verifyRemoteLibraryProposalProperties() {
        final RobotSuiteFile suiteFile = new RobotModel().createSuiteFile(file);
        final LibrarySpecification libSpec = new LibrarySpecification();
        libSpec.setDescriptor(LibraryDescriptor.ofStandardRemoteLibrary(RemoteLocation.create("http://location.org")));
        libSpec.setName("library");
        libSpec.setDocumentation("docu");

        final RedLibraryProposal proposal = AssistProposals.createLibraryProposal(suiteFile, libSpec,
                ProposalMatch.EMPTY);
        assertThat(proposal.getContent()).isEqualTo("library");
        assertThat(proposal.getArguments()).containsExactly("http://location.org");
        assertThat(proposal.getImage()).isEqualTo(RedImages.getLibraryImage());
        assertThat(proposal.getStyledLabel().getString()).isEqualTo("library http://location.org");
        assertThat(proposal.hasDescription()).isTrue();
        assertThat(proposal.getDescription()).isEqualTo("docu");
    }

    @Test
    public void verifyReservedWordsProposalProperties() {
        final RedCodeReservedWordProposal proposal = AssistProposals.createCodeReservedWordProposal("word",
                ProposalMatch.EMPTY);
        assertThat(proposal.getContent()).isEqualTo("word");
        assertThat(proposal.getArguments()).isEmpty();
        assertThat(proposal.getImage()).isNull();
        assertThat(proposal.getStyledLabel().getString()).isEqualTo("word");
        assertThat(proposal.hasDescription()).isFalse();
        assertThat(proposal.getDescription()).isEmpty();
    }

    @Test
    public void verifySettingProposalProperties() {
        final RedSettingProposal proposal = AssistProposals.createSettingProposal("Documentation",
                SettingTarget.GENERAL, ProposalMatch.EMPTY);
        assertThat(proposal.getContent()).isEqualTo("Documentation");
        assertThat(proposal.getArguments()).isEmpty();
        assertThat(proposal.getImage()).isEqualTo(RedImages.getRobotSettingImage());
        assertThat(proposal.getStyledLabel().getString()).isEqualTo("Documentation");
        assertThat(proposal.hasDescription()).isTrue();
        assertThat(proposal.getDescription()).isEmpty();
    }

    @Test
    public void verifyResourceImportInCodeProposalProperties1() {
        final RedImportProposal proposal = AssistProposals.createResourceImportInCodeProposal("resource", "",
                ProposalMatch.EMPTY);
        assertThat(proposal.getContent()).isEqualTo("resource.");
        assertThat(proposal.getArguments()).isEmpty();
        assertThat(proposal.getImage()).isEqualTo(RedImages.getRobotFileImage());
        assertThat(proposal.getStyledLabel().getString()).isEqualTo("resource");
        assertThat(proposal.hasDescription()).isFalse();
        assertThat(proposal.getDescription()).isEmpty();
    }

    @Test
    public void verifyResourceImportInCodeProposalProperties2() {
        final RedImportProposal proposal = AssistProposals.createResourceImportInCodeProposal("resource", "given ",
                ProposalMatch.EMPTY);
        assertThat(proposal.getContent()).isEqualTo("given resource.");
        assertThat(proposal.getArguments()).isEmpty();
        assertThat(proposal.getImage()).isEqualTo(RedImages.getRobotFileImage());
        assertThat(proposal.getStyledLabel().getString()).isEqualTo("resource");
        assertThat(proposal.hasDescription()).isFalse();
        assertThat(proposal.getDescription()).isEmpty();
    }

    @Test
    public void verifyLibraryImportInCodeProposalProperties1() {
        final RedImportProposal proposal = AssistProposals.createLibraryImportInCodeProposal("library", "",
                ProposalMatch.EMPTY);
        assertThat(proposal.getContent()).isEqualTo("library.");
        assertThat(proposal.getArguments()).isEmpty();
        assertThat(proposal.getImage()).isEqualTo(RedImages.getLibraryImage());
        assertThat(proposal.getStyledLabel().getString()).isEqualTo("library");
        assertThat(proposal.hasDescription()).isFalse();
        assertThat(proposal.getDescription()).isEmpty();
    }

    @Test
    public void verifyLibraryImportInCodeProposalProperties2() {
        final RedImportProposal proposal = AssistProposals.createLibraryImportInCodeProposal("library", "given ",
                ProposalMatch.EMPTY);
        assertThat(proposal.getContent()).isEqualTo("given library.");
        assertThat(proposal.getArguments()).isEmpty();
        assertThat(proposal.getImage()).isEqualTo(RedImages.getLibraryImage());
        assertThat(proposal.getStyledLabel().getString()).isEqualTo("library");
        assertThat(proposal.hasDescription()).isFalse();
        assertThat(proposal.getDescription()).isEmpty();
    }

    @Test
    public void verifyNewScalarProposalProperties() {
        final RedNewVariableProposal proposal = AssistProposals.createNewVariableProposal(VariableType.SCALAR);
        assertThat(proposal.getContent()).isEqualTo("${newScalar}");
        assertThat(proposal.getArguments()).isEmpty();
        assertThat(proposal.getImage()).isEqualTo(RedImages.getRobotScalarVariableImage());
        assertThat(proposal.getStyledLabel().getString()).isEqualTo("Fresh scalar");
        assertThat(proposal.hasDescription()).isTrue();
        assertThat(proposal.getDescription()).isEqualTo("Creates fresh scalar variable");
    }

    @Test
    public void verifyNewListProposalProperties() {
        final RedNewVariableProposal proposal = AssistProposals.createNewVariableProposal(VariableType.LIST);
        assertThat(proposal.getContent()).isEqualTo("@{newList}");
        assertThat(proposal.getArguments()).containsExactly("item");
        assertThat(proposal.getImage()).isEqualTo(RedImages.getRobotListVariableImage());
        assertThat(proposal.getStyledLabel().getString()).isEqualTo("Fresh list");
        assertThat(proposal.hasDescription()).isTrue();
        assertThat(proposal.getDescription()).isEqualTo("Creates fresh list variable");
    }

    @Test
    public void verifyNewDictionaryProposalProperties() {
        final RedNewVariableProposal proposal = AssistProposals.createNewVariableProposal(VariableType.DICTIONARY);
        assertThat(proposal.getContent()).isEqualTo("&{newDict}");
        assertThat(proposal.getArguments()).containsExactly("key=value");
        assertThat(proposal.getImage()).isEqualTo(RedImages.getRobotDictionaryVariableImage());
        assertThat(proposal.getStyledLabel().getString()).isEqualTo("Fresh dictionary");
        assertThat(proposal.hasDescription()).isTrue();
        assertThat(proposal.getDescription()).isEqualTo("Creates fresh dictionary variable");
    }

    @Test(expected = IllegalStateException.class)
    public void cannotCreateNewScalarAsListProposalProperties() {
        AssistProposals.createNewVariableProposal(VariableType.SCALAR_AS_LIST);
    }

    @Test(expected = IllegalStateException.class)
    public void cannotCreateNewInvalidVariableProposalProperties() {
        AssistProposals.createNewVariableProposal(VariableType.INVALID);
    }

    @Test(expected = IllegalStateException.class)
    public void cannotCreateNewEnvironmentalVariableProposalProperties() {
        AssistProposals.createNewVariableProposal(VariableType.ENVIRONMENT);
    }

    @Test
    public void byLabelsComparatorProperlySortsProposals() {
        final List<AssistProposal> proposals = newArrayList(proposal("xyz"), proposal("Xyz1"), proposal("zzz1"),
                proposal("ZZZ"), proposal("ABC"), proposal("abc1"));

        Collections.sort(proposals, AssistProposals.sortedByLabels());

        assertThat(transform(proposals, AssistProposal::getLabel)).containsExactly("ABC", "abc1", "xyz", "Xyz1", "ZZZ",
                "zzz1");
    }

    @Test
    public void byLabelsPrefixedFirstComparatorProperlySortsProposals() {
        final List<AssistProposal> proposals = newArrayList(proposal("xyz"), proposal("Xyz1"), proposal("Cd1"),
                proposal("zzz1"), proposal("ZZZ"), proposal("CD345"), proposal("ABC"), proposal("abc1"),
                proposal("cD2"), proposal("abCD"));

        Collections.sort(proposals, AssistProposals.sortedByLabelsPrefixedFirst("cd"));

        assertThat(transform(proposals, AssistProposal::getLabel)).containsExactly("Cd1", "cD2", "CD345", "ABC", "abc1",
                "abCD", "xyz", "Xyz1", "ZZZ", "zzz1");
    }

    @Test
    public void byLabelsCamelCaseAndPrefixedFirstComparatorProperlySortsProposals() {
        final List<RedKeywordProposal> proposals = newArrayList(
                kwProposal("xyz", "src1"),
                kwProposal("Xyz1", "src2"),
                kwProposal("Cd1", "src3"),
                kwProposal("zzz1", "src1"),
                kwProposal("Create Duplicated Name", "src2"),
                kwProposal("ZZZ", "src3"),
                kwProposal("CD345", "src1"),
                kwProposal("ABC", "src2"),
                kwProposal("Can Detect", "src3"),
                kwProposal("abc1", "src1"),
                kwProposal("cD2", "src2"),
                kwProposal("abCD", "src3"),
                kwProposal("Do Not Create Docs", "src1"));

        Collections.sort(proposals, AssistProposals.sortedByLabelsCamelCaseAndPrefixedFirst("CD"));

        assertThat(transform(proposals, AssistProposal::getLabel)).containsExactly("Can Detect - src3",
                "Create Duplicated Name - src2", "Cd1 - src3", "cD2 - src2", "CD345 - src1", "ABC - src2",
                "abc1 - src1", "abCD - src3", "Do Not Create Docs - src1", "xyz - src1", "Xyz1 - src2", "ZZZ - src3",
                "zzz1 - src1");
    }

    @Test
    public void byTypesAndOriginComparator_isSortingVariableProposalsFirstlyByTypesThenByOriginAndFinallyByNames() {
        final List<RedVariableProposal> proposals = newArrayList(
                varProposal("${Xyz3}", VariableOrigin.BUILTIN),
                varProposal("@{Xyz3}", VariableOrigin.BUILTIN),
                varProposal("${Xyz2}", VariableOrigin.IMPORTED),
                varProposal("@{Xyz2}", VariableOrigin.IMPORTED),
                varProposal("${Xyz1}", VariableOrigin.LOCAL),
                varProposal("@{Xyz1}", VariableOrigin.LOCAL),
                varProposal("${abc3}", VariableOrigin.BUILTIN),
                varProposal("@{abc3}", VariableOrigin.BUILTIN),
                varProposal("${abc2}", VariableOrigin.IMPORTED),
                varProposal("@{abc2}", VariableOrigin.IMPORTED),
                varProposal("${abc1}", VariableOrigin.LOCAL),
                varProposal("@{abc1}", VariableOrigin.LOCAL),
                varProposal("${klm3}", VariableOrigin.BUILTIN),
                varProposal("@{klm3}", VariableOrigin.BUILTIN),
                varProposal("${klm2}", VariableOrigin.IMPORTED),
                varProposal("@{klm2}", VariableOrigin.IMPORTED),
                varProposal("${klm1}", VariableOrigin.LOCAL),
                varProposal("@{klm1}", VariableOrigin.LOCAL));
        Collections.sort(proposals, AssistProposals.sortedByTypesAndOrigin());

        assertThat(transform(proposals, AssistProposal::getLabel)).containsExactly("${abc1}", "${abc2}", "${klm1}", "${klm2}",
                "${Xyz1}", "${Xyz2}", "${abc3}", "${klm3}", "${Xyz3}", "@{abc1}", "@{abc2}", "@{klm1}", "@{klm2}",
                "@{Xyz1}", "@{Xyz2}", "@{abc3}", "@{klm3}", "@{Xyz3}");
    }

    @Test
    public void byLabelsAndImportedComparator_isSortingLibraryProposalsFirstlyByImportedFlagAndThenByNames() {
        final List<RedLibraryProposal> proposals = newArrayList(
                libProposal("Xyz2", true),
                libProposal("Xyz1", false),
                libProposal("abc2", true),
                libProposal("abc1", false),
                libProposal("klm2", true),
                libProposal("klm1", false));
        Collections.sort(proposals, AssistProposals.sortedByLabelsNotImportedFirst());

        assertThat(transform(proposals, AssistProposal::getLabel)).containsExactly("abc1", "klm1", "Xyz1", "abc2", "klm2", "Xyz2");
    }

    private AssistProposal proposal(final String content) {
        return new BaseAssistProposal(content, ProposalMatch.EMPTY) {
        };
    }

    private RedKeywordProposal kwProposal(final String name, final String source) {
        final LibrarySpecification libSpec = new LibrarySpecification();
        libSpec.setName(source);
        final KeywordSpecification kwSpec = new KeywordSpecification();
        kwSpec.setName(name);
        return AssistProposals.createLibraryKeywordProposal(libSpec, kwSpec, "", KeywordScope.REF_LIBRARY,
                Optional.empty(), new Path("test.robot"), AssistProposalPredicates.alwaysTrue(), ProposalMatch.EMPTY);
    }

    private RedVariableProposal varProposal(final String name, final VariableOrigin origin) {
        return new RedVariableProposal(name, "source", "", "", origin, ProposalMatch.EMPTY);
    }

    private RedLibraryProposal libProposal(final String name, final boolean isImported) {
        return new RedLibraryProposal(name, new ArrayList<>(), isImported, "", ProposalMatch.EMPTY);
    }

    private static RobotVariable createVariable() {
        final RobotSuiteFile model = new RobotSuiteFileCreator()
                .appendLine("*** Variables ***")
                .appendLine("${scalar}  0  #something")
                .build();
        return model.findSection(RobotVariablesSection.class).get().getChildren().get(0);
    }
}
