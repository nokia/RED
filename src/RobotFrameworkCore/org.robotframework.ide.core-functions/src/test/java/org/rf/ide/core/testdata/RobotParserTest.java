/*
* Copyright 2015 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.rf.ide.core.testdata;

import static com.google.common.collect.Sets.newHashSet;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.rf.ide.core.environment.RobotVersion;
import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.RobotFileOutput;
import org.rf.ide.core.testdata.model.RobotFileOutput.BuildMessage;
import org.rf.ide.core.testdata.model.RobotFileOutput.BuildMessage.LogLevel;
import org.rf.ide.core.testdata.model.RobotProjectHolder;
import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.model.table.TestCaseTable;
import org.rf.ide.core.testdata.model.table.VariableTable;
import org.rf.ide.core.testdata.model.table.exec.descs.IExecutableRowDescriptor;
import org.rf.ide.core.testdata.model.table.testcases.TestCase;
import org.rf.ide.core.testdata.model.table.variables.AVariable;
import org.rf.ide.core.testdata.model.table.variables.AVariable.VariableType;
import org.rf.ide.core.testdata.model.table.variables.ScalarVariable;
import org.rf.ide.core.testdata.model.table.variables.UnknownVariable;
import org.rf.ide.core.testdata.text.read.EndOfLineBuilder.EndOfLineTypes;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;

import com.google.common.io.Files;

public class RobotParserTest {

    @TempDir
    static File tempDir;

    @Test
    public void errorMessageIsReported_whenFileHasNotSupportedExtension() throws Exception {
        final String fileContent = "some content";
        final RobotProjectHolder projectHolder = new RobotProjectHolder();
        final RobotParser parser = new RobotParser(projectHolder, new RobotVersion(2, 9));

        final RobotFileOutput editorContent = parser.parseEditorContent(fileContent, new File("f.unknown"));

        final RobotFile fileModel = editorContent.getFileModel();
        final List<RobotLine> lineContents = fileModel.getFileContent();
        assertThat(lineContents).isEmpty();

        final List<BuildMessage> buildingMessages = editorContent.getBuildingMessages();
        assertThat(buildingMessages).hasSize(1);
        final BuildMessage buildMessage = buildingMessages.get(0);
        assertThat(buildMessage.getType()).isEqualTo(LogLevel.ERROR);
        assertThat(buildMessage.getMessage()).contains("No parser found for given file");
    }

    @Test
    public void emptyOutputIsReturned_whenContentIsNull() throws Exception {
        final String fileContent = null;
        final RobotProjectHolder projectHolder = new RobotProjectHolder();
        final RobotParser parser = new RobotParser(projectHolder, new RobotVersion(2, 9));

        final RobotFileOutput editorContent = parser.parseEditorContent(fileContent, new File("f.robot"));

        assertThat(editorContent.getFileModel().getFileContent()).isEmpty();
    }

    @Test
    public void emptyOutputIsReturned_whenContentIsEmpty() throws Exception {
        final String fileContent = "";
        final RobotProjectHolder projectHolder = new RobotProjectHolder();
        final RobotParser parser = new RobotParser(projectHolder, new RobotVersion(2, 9));

        final RobotFileOutput editorContent = parser.parseEditorContent(fileContent, new File("f.robot"));

        assertThat(editorContent.getFileModel().getFileContent()).isEmpty();
    }

    @Test
    public void notEmptyOutputIsReturned_whenContentIsNotEmptyAndFileHasSupportedExtension() throws Exception {
        final String fileContent = "***Settings***";
        final RobotProjectHolder projectHolder = new RobotProjectHolder();
        final RobotParser parser = new RobotParser(projectHolder, new RobotVersion(2, 9));

        for (final String name : newHashSet("f.robot", "f.txt", "f.tsv")) {
            final RobotFileOutput editorContent = parser.parseEditorContent(fileContent, new File(name));

            assertThat(editorContent.getFileModel().getFileContent()).isNotEmpty();
        }
    }

    @Test
    public void test_eolInLinux_lineChecksWithoutNewLineAtTheEnd_offsetCheck() {
        // prepare
        final String fileContent = "*** Test Cases ***\nTest1\n\tLog\t\tc";

        final RobotProjectHolder projectHolder = new RobotProjectHolder();

        // execute
        final RobotParser parser = new RobotParser(projectHolder, new RobotVersion(2, 9));
        final RobotFileOutput editorContent = parser.parseEditorContent(fileContent, new File("f.robot"));

        // verify
        final RobotFile fileModel = editorContent.getFileModel();
        final List<RobotLine> lineContents = fileModel.getFileContent();
        assertThat(lineContents).hasSize(3);
        assertLine(lineContents.get(0), Arrays.asList("*** Test Cases ***"), "\n");
        assertLine(lineContents.get(1), Arrays.asList("Test1"), "\n");
        assertLine(lineContents.get(2), Arrays.asList("\t", "Log", "\t\t", "c"), null);
    }

    @Test
    public void test_eolInLinux_lineChecks_offsetCheck() {
        // prepare
        final String fileContent = "*** Test Cases ***\nTest1\n\tLog\t\tc\n";

        final RobotProjectHolder projectHolder = new RobotProjectHolder();

        // execute
        final RobotParser parser = new RobotParser(projectHolder, new RobotVersion(2, 9));
        final RobotFileOutput editorContent = parser.parseEditorContent(fileContent, new File("f.robot"));

        // verify
        final RobotFile fileModel = editorContent.getFileModel();
        final List<RobotLine> lineContents = fileModel.getFileContent();
        assertThat(lineContents).hasSize(4);
        assertLine(lineContents.get(0), Arrays.asList("*** Test Cases ***"), "\n");
        assertLine(lineContents.get(1), Arrays.asList("Test1"), "\n");
        assertLine(lineContents.get(2), Arrays.asList("\t", "Log", "\t\t", "c"), "\n");
        assertLine(lineContents.get(3), new ArrayList<String>(0), null);
    }

    private void assertLine(final RobotLine line, final List<String> elems, final String eol) {
        int tokenId = 0;
        for (final String e : elems) {
            assertThat(line.getLineElements().get(tokenId).getText()).isEqualTo(e);
            tokenId++;
        }

        if (eol == null) {
            assertThat(line.getEndOfLine().getTypes()).containsOnly(EndOfLineTypes.EOF);
        } else {
            assertThat(line.getEndOfLine().getText()).isEqualTo(eol);
        }
    }

    @Test
    public void testGivenFileTSV_withVariableTable_withOneWrongVariable_andOneCorrect_thenCheckRawAndTextParameter()
            throws Exception {
        assertOneCorrectAndOneWrongVariable_ifAllWasReadAndWillBePresented(
                "parser/bugs/CorrectAndIncorrectNameVariable_inVariableTable.tsv");
    }

    @Test
    public void testGivenFileRobot_withVariableTable_withOneWrongVariable_andOneCorrect_thenCheckRawAndTextParameter()
            throws Exception {
        assertOneCorrectAndOneWrongVariable_ifAllWasReadAndWillBePresented(
                "parser/bugs/CorrectAndIncorrectNameVariable_inVariableTable.robot");
    }

    private void assertOneCorrectAndOneWrongVariable_ifAllWasReadAndWillBePresented(final String filename)
            throws Exception {
        // prepare
        final RobotProjectHolder projectHolder = new RobotProjectHolder();

        final RobotParser parser = new RobotParser(projectHolder, new RobotVersion(2, 9));

        //// prepare paths
        final File startFile = new File(this.getClass().getResource(filename).toURI());

        // execute
        final List<RobotFileOutput> output = parser.parse(startFile);

        // verify
        assertThat(output).hasSize(1);
        final RobotFileOutput file = output.get(0);
        final RobotFile robotModel = file.getFileModel();
        assertThatVariableTableIsIncluded(robotModel);
        final VariableTable variableTable = robotModel.getVariableTable();
        final List<AVariable> variables = variableTable.getVariables();
        assertThat(variables).hasSize(2);
        final AVariable varCorrect = variables.get(0);
        assertThat(varCorrect).isInstanceOf(ScalarVariable.class);
        assertThat(varCorrect.getDeclaration().getText()).isEqualTo("${var_ok}");
        assertThat(varCorrect.getType()).isEqualTo(VariableType.SCALAR);
        assertThat(varCorrect.getName()).isEqualTo("var_ok");

        final AVariable varIncorrect = variables.get(1);
        assertThat(varIncorrect).isInstanceOf(UnknownVariable.class);
        assertThat(varIncorrect.getDeclaration().getText()).isEqualTo("${var} data");
        assertThat(varIncorrect.getType()).isEqualTo(VariableType.INVALID);
        assertThat(varIncorrect.getName()).isEqualTo("${var} data");
    }

    private void assertThatVariableTableIsIncluded(final RobotFile fileModel) {
        assertThat(fileModel.getSettingTable().isPresent()).isFalse();
        assertThat(fileModel.getVariableTable().isPresent()).isTrue();
        assertThat(fileModel.getTestCaseTable().isPresent()).isFalse();
        assertThat(fileModel.getKeywordTable().isPresent()).isFalse();
    }

    private void assertThatTestCaseTableIsIncluded(final RobotFile fileModel) {
        assertThat(fileModel.getSettingTable().isPresent()).isFalse();
        assertThat(fileModel.getVariableTable().isPresent()).isFalse();
        assertThat(fileModel.getTestCaseTable().isPresent()).isTrue();
        assertThat(fileModel.getKeywordTable().isPresent()).isFalse();
    }

    @Test
    public void test_givenTwoTestCasesInTsvFile_oneIsEmpty_andSecondIsJustVariableName_withEmptyExecute()
            throws Exception {
        // prepare
        final RobotProjectHolder projectHolder = new RobotProjectHolder();

        final RobotParser parser = new RobotParser(projectHolder, new RobotVersion(2, 9));

        // prepare paths
        final File startFile = new File(this.getClass().getResource("parser/bugs/tsv_positionCheck.tsv").toURI());

        // execute
        final List<RobotFileOutput> output = parser.parse(startFile);

        // verify
        assertThat(output).hasSize(1);
        final RobotFileOutput file = output.get(0);
        final RobotFile robotModel = file.getFileModel();
        assertThatTestCaseTableIsIncluded(robotModel);
        final TestCaseTable testCaseTable = robotModel.getTestCaseTable();
        final List<TestCase> testCases = testCaseTable.getTestCases();
        assertThat(testCases).hasSize(2);
        final TestCase testCaseT3 = testCases.get(0);

        //// verify test case T3
        final RobotToken testCaseT3Name = testCaseT3.getName();
        assertThat(testCaseT3Name.getText()).isEqualTo("T3");
        final FilePosition tcT3Pos = testCaseT3Name.getFilePosition();
        assertThat(tcT3Pos.isSamePlace(new FilePosition(2, 0, 20))).as("got %s", tcT3Pos).isTrue();
        assertThat(testCaseT3.getExecutionContext()).isEmpty();

        //// verify test case ${x}
        final TestCase testCaseSpacesX = testCases.get(1);
        assertThat(testCaseSpacesX.getName().getText()).isEqualTo("${x}");
        final FilePosition tcXPos = testCaseSpacesX.getName().getFilePosition();
        assertThat(tcXPos.isSamePlace(new FilePosition(3, 4, 28))).as("got %s", tcXPos).isTrue();
        final List<RobotExecutableRow<TestCase>> xTestExecutionList = testCaseSpacesX.getExecutionContext();
        assertThat(xTestExecutionList).hasSize(1);
        final IExecutableRowDescriptor<TestCase> xTestFirstLineDescription = xTestExecutionList.get(0)
                .buildLineDescription();

        final RobotToken emptyAction = xTestFirstLineDescription.getAction();
        assertThat(emptyAction.getText()).isEmpty();
        final FilePosition emptyActionPosition = emptyAction.getFilePosition();
        assertThat(emptyActionPosition.isSamePlace(new FilePosition(4, 5, 43))).as("got %s", emptyActionPosition)
                .isTrue();
    }

    @Test
    public void emptyOutputIsReturned_whenFileIsNull() throws Exception {
        final RobotProjectHolder projectHolder = new RobotProjectHolder();

        final RobotParser parser = new RobotParser(projectHolder, new RobotVersion(2, 9));

        final File startFile = null;

        final List<RobotFileOutput> output = parser.parse(startFile);

        assertThat(output).isEmpty();
    }

    @Test
    public void emptyOutputIsReturned_whenFileHasNotSupportedExtension() throws Exception {
        final RobotProjectHolder projectHolder = new RobotProjectHolder();

        final RobotParser parser = new RobotParser(projectHolder, new RobotVersion(2, 9));

        final File startFile = new File(tempDir, "file.unknown");
        startFile.createNewFile();

        final List<RobotFileOutput> output = parser.parse(startFile);

        assertThat(output).isEmpty();
    }

    @Test
    public void notEmptyOutputIsReturned_whenFileHasSupportedExtension() throws Exception {
        final RobotProjectHolder projectHolder = new RobotProjectHolder();

        final RobotParser parser = new RobotParser(projectHolder, new RobotVersion(2, 9));

        for (final String name : newHashSet("f.robot", "f.txt", "f.tsv")) {
            final File startFile = new File(tempDir, name);
            final String fileContent = "***Settings***";
            Files.write(fileContent.getBytes(), startFile);

            final List<RobotFileOutput> output = parser.parse(startFile);

            assertThat(output).isNotEmpty();
        }
    }

    @Test
    public void fileShouldBeParsedOnlyOnce() throws Exception {
        final RobotProjectHolder projectHolder = new RobotProjectHolder();

        final RobotParser parser = new RobotParser(projectHolder, new RobotVersion(2, 9));

        final File startFile = new File(tempDir, "file.robot");
        final String fileContent = "***Settings***";
        Files.write(fileContent.getBytes(), startFile);

        final List<RobotFileOutput> output1 = parser.parse(startFile);
        final List<RobotFileOutput> output2 = parser.parse(startFile);
        final List<RobotFileOutput> output3 = parser.parse(startFile);

        assertThat(output1).hasSize(1);
        assertThat(output2).hasSize(1);
        assertThat(output3).hasSize(1);
        assertThat(output2.get(0)).isSameAs(output1.get(0));
        assertThat(output3.get(0)).isSameAs(output1.get(0));
    }

    @Test
    public void fileShouldBeCleared_whenContainsTooManyLines() throws Exception {
        final RobotProjectHolder projectHolder = new RobotProjectHolder();

        final RobotParser parser = new RobotParser(projectHolder, new RobotVersion(2, 9));

        final File startFile = new File(tempDir, "file_with_5000_lines.robot");
        final String fileContent = String.join("", Collections.nCopies(5000, "abc" + System.lineSeparator()));
        Files.write(fileContent.getBytes(), startFile);

        final List<RobotFileOutput> output = parser.parse(startFile);

        assertThat(output).hasSize(1);
        assertThat(output.get(0).getFileModel().getFileContent()).isEmpty();
    }

    @Test
    public void fileShouldNotBeCleared_whenDoesNotContainTooManyLines() throws Exception {
        final RobotProjectHolder projectHolder = new RobotProjectHolder();

        final RobotParser parser = new RobotParser(projectHolder, new RobotVersion(2, 9));

        final File startFile = new File(tempDir, "file_with_4999_lines.robot");
        final String fileContent = String.join("", Collections.nCopies(4999, "abc" + System.lineSeparator()));
        Files.write(fileContent.getBytes(), startFile);

        final List<RobotFileOutput> output = parser.parse(startFile);

        assertThat(output).hasSize(1);
        assertThat(output.get(0).getFileModel().getFileContent()).isNotEmpty();
    }

    @Test
    public void directoryShouldBeParsed() throws Exception {
        final RobotProjectHolder projectHolder = new RobotProjectHolder();

        final RobotParser parser = new RobotParser(projectHolder, new RobotVersion(2, 9));

        final File startDir = new File(tempDir, "dir_with_suites");
        startDir.mkdir();
        Files.write("***Settings***".getBytes(), new File(tempDir, "dir_with_suites/file1.robot"));
        Files.write("***Keywords***".getBytes(), new File(tempDir, "dir_with_suites/file2.robot"));
        Files.write("***Test Cases***".getBytes(), new File(tempDir, "dir_with_suites/file3.robot"));

        final List<RobotFileOutput> output = parser.parse(startDir);

        assertThat(output).hasSize(3);
        assertThat(output.get(0).getFileModel().getFileContent()).isNotEmpty();
        assertThat(output.get(1).getFileModel().getFileContent()).isNotEmpty();
        assertThat(output.get(2).getFileModel().getFileContent()).isNotEmpty();
    }

}
