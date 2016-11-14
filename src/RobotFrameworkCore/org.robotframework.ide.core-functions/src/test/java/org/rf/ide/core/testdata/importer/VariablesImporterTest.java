/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.importer;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.rf.ide.core.executor.RobotRuntimeEnvironment;
import org.rf.ide.core.project.ImportSearchPaths.PathsProvider;
import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.model.FileRegion;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.RobotFileOutput;
import org.rf.ide.core.testdata.model.RobotFileOutput.BuildMessage;
import org.rf.ide.core.testdata.model.RobotFileOutput.BuildMessage.LogLevel;
import org.rf.ide.core.testdata.model.RobotProjectHolder;
import org.rf.ide.core.testdata.model.RobotVersion;
import org.rf.ide.core.testdata.model.table.SettingTable;
import org.rf.ide.core.testdata.model.table.setting.AImported;
import org.rf.ide.core.testdata.model.table.setting.VariablesImport;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;

@SuppressWarnings("PMD.MethodNamingConventions")
public class VariablesImporterTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void importVariables_withWrongPath_shouldReturn_anEmptyList() throws IOException {
        // prepare
        final File processedFile = temporaryFolder.newFile("robot.robot");

        final VariablesImporter varImporter = new VariablesImporter();

        final RobotFileOutput robotFile = new RobotFileOutput(RobotVersion.UNKNOWN);
        robotFile.setProcessedFile(processedFile);
        final RobotFile fileModel = robotFile.getFileModel();
        fileModel.includeSettingTableSection();
        final SettingTable settingTable = fileModel.getSettingTable();
        final String varImport = "\\VariableFiles\u0000/UnicodeInVariables.py*** Test Cases ***";
        addNewVariableImport(settingTable, varImport);
        final RobotProjectHolder robotProject = mock(RobotProjectHolder.class);
        final RobotRuntimeEnvironment robotRunEnv = mock(RobotRuntimeEnvironment.class);

        // execute
        final List<VariablesFileImportReference> importVariables = varImporter.importVariables(null, robotRunEnv,
                robotProject, robotFile);

        // verify
        assertThat(importVariables).isEmpty();
        final List<BuildMessage> buildingMessages = robotFile.getBuildingMessages();
        assertThat(buildingMessages.size()).isEqualTo(1);
        final BuildMessage buildMessage = buildingMessages.get(0);
        assertThat(buildMessage.getFileName()).contains("robot.robot");
        assertThat(buildMessage.getType()).isEqualTo(LogLevel.ERROR);
        assertThat(buildMessage.getMessage()).contains(
                "Problem with importing variable file \\VariableFiles\u0000/UnicodeInVariables.py*** Test Cases *** with error stack: ");
        final FileRegion fileRegion = buildMessage.getFileRegion();
        assertThat(fileRegion).isNotNull();
        assertThat(fileRegion.getStart().isSamePlace(new FilePosition(1, 0, 0))).isTrue();
        final int importTextLength = "Variables".length() + varImport.length();
        assertThat(fileRegion.getEnd().isSamePlace(new FilePosition(1, importTextLength, importTextLength))).isTrue();
    }

    @Test
    public void importVariables_withThreeElements_and_withOne_withWrongPath_shouldReturn_emptyList()
            throws IOException {
        // prepare
        final File processedFile = temporaryFolder.newFile("importing.robot");
        temporaryFolder.newFile("robot.py");
        temporaryFolder.newFile("robot2.py");
        
        final VariablesImporter varImporter = new VariablesImporter();

        final RobotFileOutput robotFile = new RobotFileOutput(RobotVersion.UNKNOWN);
        robotFile.setProcessedFile(processedFile);
        final RobotFile fileModel = robotFile.getFileModel();
        fileModel.includeSettingTableSection();
        final SettingTable settingTable = fileModel.getSettingTable();
        addNewVariableImport(settingTable, "robot.py");
        addNewVariableImport(settingTable, "new /robot.py");
        addNewVariableImport(settingTable, "robot2.py");
        final RobotProjectHolder robotProject = mock(RobotProjectHolder.class);
        final RobotRuntimeEnvironment robotRunEnv = mock(RobotRuntimeEnvironment.class);

        final PathsProvider pathsProvider = new PathsProvider() {

            @Override
            public List<File> provideUserSearchPaths() {
                return newArrayList(new File(""));
            }

            @Override
            public List<File> providePythonModulesSearchPaths() {
                return newArrayList(new File(""));
            }
        };
        // execute
        final List<VariablesFileImportReference> importVariables = varImporter.importVariables(pathsProvider,
                robotRunEnv, robotProject, robotFile);

        // verify
        assertThat(importVariables).hasSize(2);
        final VariablesFileImportReference variablesFileImportReference = importVariables.get(0);
        assertThat(variablesFileImportReference.getVariablesFile().getName()).isEqualTo("robot.py");
        final VariablesFileImportReference variablesFileImportReference2 = importVariables.get(1);
        assertThat(variablesFileImportReference2.getVariablesFile().getName()).isEqualTo("robot2.py");
    }

    @Test
    public void importVariables_withTwoElements_and_withOne_withWrongPath_shouldReturn_emptyList() throws IOException {
        // prepare
        final File processedFile = temporaryFolder.newFile("robot.robot");
        temporaryFolder.newFile("robot.py");

        final VariablesImporter varImporter = new VariablesImporter();

        final RobotFileOutput robotFile = new RobotFileOutput(RobotVersion.UNKNOWN);
        robotFile.setProcessedFile(processedFile);
        final RobotFile fileModel = robotFile.getFileModel();
        fileModel.includeSettingTableSection();
        final SettingTable settingTable = fileModel.getSettingTable();
        addNewVariableImport(settingTable, "new /robot.py");
        addNewVariableImport(settingTable, "robot.py");

        final RobotProjectHolder robotProject = mock(RobotProjectHolder.class);
        final RobotRuntimeEnvironment robotRunEnv = mock(RobotRuntimeEnvironment.class);

        // execute
        final List<VariablesFileImportReference> importVariables = varImporter.importVariables(null, robotRunEnv,
                robotProject, robotFile);

        // verify
        assertThat(importVariables).hasSize(1);
        final VariablesFileImportReference variablesFileImportReference = importVariables.get(0);
        assertThat(variablesFileImportReference.getVariablesFile().getName()).isEqualTo("robot.py");
    }

    @Test
    public void importVariables_withOneElement_and_withWrongPath_shouldReturn_emptyList() {
        // prepare
        final VariablesImporter varImporter = new VariablesImporter();

        final RobotFileOutput robotFile = new RobotFileOutput(RobotVersion.UNKNOWN);
        final RobotFile fileModel = robotFile.getFileModel();
        fileModel.includeSettingTableSection();
        final SettingTable settingTable = fileModel.getSettingTable();
        addNewVariableImport(settingTable, "new /");

        final RobotProjectHolder robotProject = null;
        final RobotRuntimeEnvironment robotRunEnv = null;

        // execute
        final List<VariablesFileImportReference> importVariables = varImporter.importVariables(null, robotRunEnv,
                robotProject, robotFile);

        // verify
        assertThat(importVariables).isEmpty();
    }

    private void addNewVariableImport(final SettingTable settingTable, final String text) {
        final RobotToken rtDec = new RobotToken();
        rtDec.setLineNumber(1);
        rtDec.setStartColumn(0);
        rtDec.setStartOffset(0);
        rtDec.setText("Variables");
        rtDec.setRaw("Variables");

        final AImported importedOne = new VariablesImport(rtDec);
        final RobotToken varPathOne = new RobotToken();
        varPathOne.setRaw(text);
        varPathOne.setText(text);
        varPathOne.setStartOffset(importedOne.getEndPosition().getOffset());
        varPathOne.setLineNumber(1);
        varPathOne.setStartColumn(importedOne.getEndPosition().getColumn());
        importedOne.setPathOrName(varPathOne);
        settingTable.addImported(importedOne);
    }

    @Test
    public void isCorrectPath_containsSpaceBackslash_AtDir2_WINDOWS_shouldReturn_FALSE() {
        assertThat(new VariablesImporter().isCorrectPath("d:\\${var} \\windows")).isFalse();
    }

    @Test
    public void isCorrectPath_containsSpaceBackslashSpaceInTheEnd_WINDOWS_shouldReturn_TRUE() {
        assertThat(new VariablesImporter().isCorrectPath("${var} \\ ")).isTrue();
    }

    @Test
    public void isCorrectPath_containsSpaceBackslashSpace_WINDOWS_shouldReturn_TRUE() {
        assertThat(new VariablesImporter().isCorrectPath("${var} \\ windows")).isTrue();
    }

    @Test
    public void isCorrectPath_containsSpaceBackslash_WINDOWS_shouldReturn_FALSE() {
        assertThat(new VariablesImporter().isCorrectPath("${var} \\windows")).isFalse();
    }

    @Test
    public void isCorrectPath_containsSpaceSlash_AtDir2_UNIX_shouldReturn_FALSE() {
        assertThat(new VariablesImporter().isCorrectPath("/d/${var} /linux")).isFalse();
    }

    @Test
    public void isCorrectPath_containsSpaceSlash_UNIX_shouldReturn_FALSE() {
        assertThat(new VariablesImporter().isCorrectPath("${var} /linux")).isFalse();
    }
}
