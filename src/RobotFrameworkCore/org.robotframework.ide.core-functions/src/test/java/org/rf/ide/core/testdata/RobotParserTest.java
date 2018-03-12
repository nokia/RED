/*
* Copyright 2015 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.rf.ide.core.testdata;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.mockito.InOrder;
import org.rf.ide.core.executor.RobotRuntimeEnvironment;
import org.rf.ide.core.testdata.RobotParser.RobotParserConfig;
import org.rf.ide.core.testdata.importer.ResourceImportReference;
import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.RobotFileOutput;
import org.rf.ide.core.testdata.model.RobotProjectHolder;
import org.rf.ide.core.testdata.model.RobotVersion;
import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.model.table.TestCaseTable;
import org.rf.ide.core.testdata.model.table.VariableTable;
import org.rf.ide.core.testdata.model.table.exec.descs.IExecutableRowDescriptor;
import org.rf.ide.core.testdata.model.table.exec.descs.RobotAction;
import org.rf.ide.core.testdata.model.table.testcases.TestCase;
import org.rf.ide.core.testdata.model.table.variables.AVariable;
import org.rf.ide.core.testdata.model.table.variables.AVariable.VariableType;
import org.rf.ide.core.testdata.model.table.variables.ScalarVariable;
import org.rf.ide.core.testdata.model.table.variables.UnknownVariable;
import org.rf.ide.core.testdata.text.read.EndOfLineBuilder.EndOfLineTypes;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;

@SuppressWarnings("PMD.MethodNamingConventions")
public class RobotParserTest {

    @Test
    public void test_eolInLinux_lineChecksWithoutNewLineAtTheEnd_offsetCheck() {
        // prepare
        final String fileContent = "*** Test Cases ***\nTest1\n\tLog\t\tc";

        final RobotRuntimeEnvironment runtime = mock(RobotRuntimeEnvironment.class);
        when(runtime.getVersion()).thenReturn("2.9");
        final RobotProjectHolder projectHolder = spy(RobotProjectHolder.class);
        when(projectHolder.getRobotRuntime()).thenReturn(runtime);

        // execute
        final RobotParser parser = RobotParser.create(projectHolder, RobotParserConfig.allImportsLazy());
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

        final RobotRuntimeEnvironment runtime = mock(RobotRuntimeEnvironment.class);
        when(runtime.getVersion()).thenReturn("2.9");
        final RobotProjectHolder projectHolder = spy(RobotProjectHolder.class);
        when(projectHolder.getRobotRuntime()).thenReturn(runtime);

        // execute
        final RobotParser parser = RobotParser.create(projectHolder, RobotParserConfig.allImportsLazy());
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
    public void test_create_when_robotFramework_correct29() {
        // prepare
        final RobotRuntimeEnvironment runtime = mock(RobotRuntimeEnvironment.class);
        when(runtime.getVersion()).thenReturn("2.9");
        final RobotProjectHolder projectHolder = mock(RobotProjectHolder.class);
        when(projectHolder.getRobotRuntime()).thenReturn(runtime);

        // execute
        final RobotParser parser = RobotParser.create(projectHolder, RobotParserConfig.allImportsLazy());

        // verify
        final RobotVersion robotVersion = parser.getRobotVersion();
        assertThat(robotVersion).isNotNull();
        assertThat(robotVersion.isEqualTo(new RobotVersion(2, 9))).isTrue();
    }

    @Test
    public void test_create_when_robotFramework_isNotPresent() {
        // prepare
        final RobotRuntimeEnvironment runtime = mock(RobotRuntimeEnvironment.class);
        when(runtime.getVersion()).thenReturn(null);
        final RobotProjectHolder projectHolder = mock(RobotProjectHolder.class);
        when(projectHolder.getRobotRuntime()).thenReturn(runtime);

        // execute
        final RobotParser parser = RobotParser.create(projectHolder, RobotParserConfig.allImportsLazy());

        // verify
        assertThat(parser.getRobotVersion()).isNull();
    }

    @Test(timeout = 10000)
    public void test_loopedResources_shouldPassFastAndWithoutAny_reReadFiles_BUG_RED_352_GITHUB_23() throws Exception {
        // prepare
        final RobotRuntimeEnvironment runtime = mock(RobotRuntimeEnvironment.class);
        when(runtime.getVersion()).thenReturn("2.9");
        final RobotProjectHolder projectHolder = spy(RobotProjectHolder.class);
        when(projectHolder.getRobotRuntime()).thenReturn(runtime);

        final RobotParser parser = spy(RobotParser.create(projectHolder, RobotParserConfig.allImportsEager()));

        //// prepare paths
        final String mainPath = "parser/bugs/RED_352_ReadManyTimesPrevReadReferenceFile_LoopPrevent/";
        final File startFile = new File(this.getClass().getResource(mainPath + "StartFile.robot").toURI());
        final File normalFile = new File(this.getClass().getResource(mainPath + "NormalFile.robot").toURI());
        final File anotherLoop = new File(this.getClass().getResource(mainPath + "anotherLoop.robot").toURI());
        final File loopEndWithRefToFirst = new File(
                this.getClass().getResource(mainPath + "resources/loopEndWithRefToFirst.robot").toURI());
        final File middle = new File(this.getClass().getResource(mainPath + "resources/Middle.robot").toURI());
        final File theFirst = new File(this.getClass().getResource(mainPath + "resources/theFirst.robot").toURI());

        // execute
        final List<RobotFileOutput> output = parser.parse(startFile);

        // verify content
        //// StartFile.robot
        assertThat(output).hasSize(1);
        final RobotFileOutput startFileOutput = output.get(0);
        assertThat(startFileOutput.getProcessedFile()).isEqualTo(startFile);

        final List<ResourceImportReference> resourceImportReferences = startFileOutput.getResourceImportReferences();
        assertThat(resourceImportReferences).hasSize(3);

        final ResourceImportReference theFirstImportMain = resourceImportReferences.get(0);
        assertThat(theFirstImportMain.getImportDeclaration().getPathOrName().getText()).isEqualTo("NormalFile.robot");
        assertThat(theFirstImportMain.getReference().getProcessedFile()).isEqualTo(normalFile);

        final ResourceImportReference anotherFileResource = resourceImportReferences.get(1);
        assertThat(anotherFileResource.getImportDeclaration().getPathOrName().getText()).isEqualTo("anotherLoop.robot");
        assertThat(anotherFileResource.getReference().getProcessedFile()).isEqualTo(anotherLoop);

        final ResourceImportReference res_theFirst = resourceImportReferences.get(2);
        assertThat(res_theFirst.getImportDeclaration().getPathOrName().getText()).isEqualTo("resources/theFirst.robot");
        assertThat(res_theFirst.getReference().getProcessedFile()).isEqualTo(theFirst);

        //// NormalFile.robot
        final RobotFileOutput normalFileOutput = theFirstImportMain.getReference();
        assertThat(normalFileOutput.getResourceImportReferences()).hasSize(0);

        //// anotherLoop.robot
        final RobotFileOutput anotherFileOutput = anotherFileResource.getReference();
        final List<ResourceImportReference> anotherLoopRefs = anotherFileOutput.getResourceImportReferences();
        assertThat(anotherLoopRefs).hasSize(1);

        final ResourceImportReference loopEndRef = anotherLoopRefs.get(0);
        assertThat(loopEndRef.getImportDeclaration().getPathOrName().getText())
                .isEqualTo("resources/loopEndWithRefToFirst.robot");
        assertThat(loopEndRef.getReference().getProcessedFile()).isEqualTo(loopEndWithRefToFirst);

        //// loopEndWithRefToFirst.robot
        final RobotFileOutput loopEndOutput = loopEndRef.getReference();
        final List<ResourceImportReference> loopEndRefs = loopEndOutput.getResourceImportReferences();
        assertThat(loopEndRefs).hasSize(1);

        final ResourceImportReference middleRef = loopEndRefs.get(0);
        assertThat(middleRef.getImportDeclaration().getPathOrName().getText()).isEqualTo("../resources/Middle.robot");
        assertThat(middleRef.getReference().getProcessedFile()).isEqualTo(middle);

        //// middle.robot
        final RobotFileOutput middleOutput = middleRef.getReference();
        final List<ResourceImportReference> middleRefs = middleOutput.getResourceImportReferences();
        assertThat(middleRefs).hasSize(1);

        final ResourceImportReference res_theFirstAgain = middleRefs.get(0);
        assertThat(res_theFirstAgain.getImportDeclaration().getPathOrName().getText())
                .isEqualTo("../resources/theFirst.robot");
        assertThat(res_theFirstAgain.getReference()).isSameAs(res_theFirst.getReference());

        // verify order
        final InOrder order = inOrder(projectHolder, parser);
        order.verify(projectHolder, times(1)).shouldBeLoaded(startFile);
        order.verify(projectHolder, times(1)).addModelFile(output.get(0));
        order.verify(projectHolder, times(1)).shouldBeLoaded(normalFile);
        order.verify(projectHolder, times(1)).addModelFile(theFirstImportMain.getReference());
        order.verify(projectHolder, times(1)).shouldBeLoaded(anotherLoop);
        order.verify(projectHolder, times(1)).addModelFile(anotherFileResource.getReference());
        order.verify(projectHolder, times(1)).shouldBeLoaded(loopEndWithRefToFirst);
        order.verify(projectHolder, times(1)).addModelFile(loopEndRef.getReference());
        order.verify(projectHolder, times(1)).shouldBeLoaded(middle);
        order.verify(projectHolder, times(1)).addModelFile(middleRef.getReference());
        order.verify(projectHolder, times(1)).shouldBeLoaded(theFirst);
        order.verify(projectHolder, times(1)).addModelFile(res_theFirst.getReference());
        order.verify(projectHolder, times(1)).shouldBeLoaded(theFirst);
        order.verify(projectHolder, times(1)).findFileByName(theFirst);

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
        final RobotRuntimeEnvironment runtime = mock(RobotRuntimeEnvironment.class);
        when(runtime.getVersion()).thenReturn("2.9");
        final RobotProjectHolder projectHolder = spy(RobotProjectHolder.class);
        when(projectHolder.getRobotRuntime()).thenReturn(runtime);

        final RobotParser parser = spy(RobotParser.create(projectHolder, RobotParserConfig.allImportsLazy()));

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
        assertThat(varCorrect.getDeclaration().getRaw()).isEqualTo("${var_ok}");
        assertThat(varCorrect.getType()).isEqualTo(VariableType.SCALAR);
        assertThat(varCorrect.getName()).isEqualTo("var_ok");

        final AVariable varIncorrect = variables.get(1);
        assertThat(varIncorrect).isInstanceOf(UnknownVariable.class);
        assertThat(varIncorrect.getDeclaration().getText()).isEqualTo("${var} data");
        assertThat(varIncorrect.getDeclaration().getRaw()).isEqualTo("${var} data");
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
        final RobotRuntimeEnvironment runtime = mock(RobotRuntimeEnvironment.class);
        when(runtime.getVersion()).thenReturn("2.9");
        final RobotProjectHolder projectHolder = spy(RobotProjectHolder.class);
        when(projectHolder.getRobotRuntime()).thenReturn(runtime);

        final RobotParser parser = spy(RobotParser.create(projectHolder, RobotParserConfig.allImportsLazy()));

        //// prepare paths
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
        assertThat(testCaseT3Name.getRaw()).isEqualTo("T3");
        final FilePosition tcT3Pos = testCaseT3Name.getFilePosition();
        assertThat(tcT3Pos.isSamePlace(new FilePosition(2, 0, 20))).as("got %s", tcT3Pos).isTrue();
        assertThat(testCaseT3.getExecutionContext()).isEmpty();

        //// verify test case ${x}
        final TestCase testCaseSpacesX = testCases.get(1);
        assertThat(testCaseSpacesX.getName().getText()).isEqualTo("${x}");
        assertThat(testCaseSpacesX.getName().getRaw()).isEqualTo("${x}");
        final FilePosition tcXPos = testCaseSpacesX.getName().getFilePosition();
        assertThat(tcXPos.isSamePlace(new FilePosition(3, 4, 28))).as("got %s", tcXPos).isTrue();
        final List<RobotExecutableRow<TestCase>> xTestExecutionList = testCaseSpacesX.getExecutionContext();
        assertThat(xTestExecutionList).hasSize(1);
        final IExecutableRowDescriptor<TestCase> xTestFirstLineDescription = xTestExecutionList.get(0)
                .buildLineDescription();

        final RobotAction action = xTestFirstLineDescription.getAction();
        final RobotToken emptyAction = action.getToken();
        assertThat(emptyAction.getText()).isEmpty();
        assertThat(emptyAction.getRaw()).isEmpty();
        final FilePosition emptyActionPosition = emptyAction.getFilePosition();
        assertThat(emptyActionPosition.isSamePlace(new FilePosition(4, 5, 43))).as("got %s", emptyActionPosition)
                .isTrue();
    }
}
