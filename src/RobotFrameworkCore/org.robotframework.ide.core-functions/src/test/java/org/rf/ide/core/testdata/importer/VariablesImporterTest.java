/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.importer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.List;

import org.junit.Test;
import org.rf.ide.core.executor.RobotRuntimeEnvironment;
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

    @Test
    public void importVariables_withWrongPath_shouldReturn_anEmptyList() {
        // prepare
        VariablesImporter varImporter = new VariablesImporter();

        RobotFileOutput robotFile = new RobotFileOutput(RobotVersion.UNKNOWN);
        File processedFile = mock(File.class);
        when(processedFile.exists()).thenReturn(false);
        when(processedFile.getName()).thenReturn("robot.robot");
        when(processedFile.getAbsoluteFile()).thenReturn(processedFile);
        when(processedFile.toString()).thenReturn("robot.robot");
        robotFile.setProcessedFile(processedFile);
        RobotFile fileModel = robotFile.getFileModel();
        fileModel.includeSettingTableSection();
        SettingTable settingTable = fileModel.getSettingTable();
        final String varImport = "\\VariableFiles\u0000/UnicodeInVariables.py*** Test Cases ***";
        addNewVariableImport(settingTable, varImport);
        RobotProjectHolder robotProject = mock(RobotProjectHolder.class);
        RobotRuntimeEnvironment robotRunEnv = mock(RobotRuntimeEnvironment.class);

        // execute
        List<VariablesFileImportReference> importVariables = varImporter.importVariables(robotRunEnv, robotProject,
                robotFile);

        // verify
        assertThat(importVariables).isEmpty();
        List<BuildMessage> buildingMessages = robotFile.getBuildingMessages();
        assertThat(buildingMessages.size()).isEqualTo(1);
        BuildMessage buildMessage = buildingMessages.get(0);
        assertThat(buildMessage.getFileName()).isEqualTo("robot.robot");
        assertThat(buildMessage.getType()).isEqualTo(LogLevel.ERROR);
        assertThat(buildMessage.getMessage()).contains(
                "Problem with importing variable file \\VariableFiles\u0000/UnicodeInVariables.py*** Test Cases *** with error stack: ");
        FileRegion fileRegion = buildMessage.getFileRegion();
        assertThat(fileRegion).isNotNull();
        assertThat(fileRegion.getStart().isSamePlace(new FilePosition(1, 0, 0))).isTrue();
        final int importTextLength = "Variables".length() + varImport.length();
        assertThat(fileRegion.getEnd().isSamePlace(new FilePosition(1, importTextLength, importTextLength))).isTrue();
    }

    @Test
    public void importVariables_withThreeElements_and_withOne_withWrongPath_shouldReturn_emptyList() {
        // prepare
        VariablesImporter varImporter = new VariablesImporter();

        RobotFileOutput robotFile = new RobotFileOutput(RobotVersion.UNKNOWN);
        File processedFile = mock(File.class);
        when(processedFile.exists()).thenReturn(false);
        when(processedFile.getName()).thenReturn("robot.robot");
        when(processedFile.getAbsoluteFile()).thenReturn(processedFile);
        robotFile.setProcessedFile(processedFile);
        RobotFile fileModel = robotFile.getFileModel();
        fileModel.includeSettingTableSection();
        SettingTable settingTable = fileModel.getSettingTable();
        addNewVariableImport(settingTable, "robot.py");
        addNewVariableImport(settingTable, "new /robot.py");
        addNewVariableImport(settingTable, "robot2.py");
        RobotProjectHolder robotProject = mock(RobotProjectHolder.class);
        RobotRuntimeEnvironment robotRunEnv = mock(RobotRuntimeEnvironment.class);

        // execute
        List<VariablesFileImportReference> importVariables = varImporter.importVariables(robotRunEnv, robotProject,
                robotFile);

        // verify
        assertThat(importVariables).hasSize(2);
        VariablesFileImportReference variablesFileImportReference = importVariables.get(0);
        assertThat(variablesFileImportReference.getVariablesFile().getName()).isEqualTo("robot.py");
        VariablesFileImportReference variablesFileImportReference2 = importVariables.get(1);
        assertThat(variablesFileImportReference2.getVariablesFile().getName()).isEqualTo("robot2.py");
    }

    @Test
    public void importVariables_withTwoElements_and_withOne_withWrongPath_shouldReturn_emptyList() {
        // prepare
        VariablesImporter varImporter = new VariablesImporter();

        RobotFileOutput robotFile = new RobotFileOutput(RobotVersion.UNKNOWN);
        File processedFile = mock(File.class);
        when(processedFile.exists()).thenReturn(false);
        when(processedFile.getName()).thenReturn("robot.robot");
        when(processedFile.getAbsoluteFile()).thenReturn(processedFile);
        robotFile.setProcessedFile(processedFile);
        RobotFile fileModel = robotFile.getFileModel();
        fileModel.includeSettingTableSection();
        SettingTable settingTable = fileModel.getSettingTable();
        addNewVariableImport(settingTable, "new /robot.py");
        addNewVariableImport(settingTable, "robot.py");

        RobotProjectHolder robotProject = mock(RobotProjectHolder.class);
        RobotRuntimeEnvironment robotRunEnv = mock(RobotRuntimeEnvironment.class);

        // execute
        List<VariablesFileImportReference> importVariables = varImporter.importVariables(robotRunEnv, robotProject,
                robotFile);

        // verify
        assertThat(importVariables).hasSize(1);
        VariablesFileImportReference variablesFileImportReference = importVariables.get(0);
        assertThat(variablesFileImportReference.getVariablesFile().getName()).isEqualTo("robot.py");
    }

    @Test
    public void importVariables_withOneElement_and_withWrongPath_shouldReturn_emptyList() {
        // prepare
        VariablesImporter varImporter = new VariablesImporter();

        RobotFileOutput robotFile = new RobotFileOutput(RobotVersion.UNKNOWN);
        RobotFile fileModel = robotFile.getFileModel();
        fileModel.includeSettingTableSection();
        SettingTable settingTable = fileModel.getSettingTable();
        addNewVariableImport(settingTable, "new /");

        RobotProjectHolder robotProject = null;
        RobotRuntimeEnvironment robotRunEnv = null;

        // execute
        List<VariablesFileImportReference> importVariables = varImporter.importVariables(robotRunEnv, robotProject,
                robotFile);

        // verify
        assertThat(importVariables).isEmpty();
    }

    private void addNewVariableImport(SettingTable settingTable, String text) {
        RobotToken rtDec = new RobotToken();
        rtDec.setLineNumber(1);
        rtDec.setStartColumn(0);
        rtDec.setStartOffset(0);
        rtDec.setText("Variables");
        rtDec.setRaw("Variables");

        AImported importedOne = new VariablesImport(rtDec);
        RobotToken varPathOne = new RobotToken();
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
