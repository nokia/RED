/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.write.tables.settings.creation;

import org.junit.Test;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.table.SettingTable;
import org.rf.ide.core.testdata.model.table.setting.VariablesImport;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.separators.TokenSeparatorBuilder.FileFormat;
import org.rf.ide.core.testdata.text.write.NewRobotFileTestHelper;
import org.rf.ide.core.testdata.text.write.RobotFormatParameterizedTest;

public class CreationOfSettingsVariablesImportTest extends RobotFormatParameterizedTest {

    public CreationOfSettingsVariablesImportTest(final String extension, final FileFormat format) {
        super(extension, format);
    }

    @Test
    public void test_emptyFile_and_thanCreateVariablesImport() throws Exception {
        // prepare
        final String fileName = convert("EmptyVariableDeclarationOnly");
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeSettingTableSection();
        final SettingTable settingTable = modelFile.getSettingTable();
        settingTable.newVariablesImport();

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    @Test
    public void test_emptyFile_and_thanCreateVariablesImport_withName() throws Exception {
        // prepare
        final String fileName = convert("VariableDeclarationWithVariableNameOnly");
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeSettingTableSection();
        final SettingTable settingTable = modelFile.getSettingTable();
        final VariablesImport var = settingTable.newVariablesImport();
        final RobotToken varName = new RobotToken();
        varName.setText("newVar.py");
        var.setPathOrName(varName);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    @Test
    public void test_emptyFile_and_thanCreateVariablesImport_withNameAnd_ThreeCommentTokens() throws Exception {
        // prepare
        final String fileName = convert("VariableDeclarationWithPathNameAndThreeCommentOnly");
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeSettingTableSection();
        final SettingTable settingTable = modelFile.getSettingTable();
        final VariablesImport var = settingTable.newVariablesImport();
        final RobotToken varName = new RobotToken();
        varName.setText("newVar.py");
        var.setPathOrName(varName);

        final RobotToken cm1 = new RobotToken();
        cm1.setText("cm1");
        final RobotToken cm2 = new RobotToken();
        cm2.setText("cm2");
        final RobotToken cm3 = new RobotToken();
        cm3.setText("cm3");
        var.addCommentPart(cm1);
        var.addCommentPart(cm2);
        var.addCommentPart(cm3);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    @Test
    public void test_emptyFile_and_thanCreateVariablesImport_with_ThreeCommentTokens() throws Exception {
        // prepare
        final String fileName = convert("VariableDeclarationWithThreeCommentOnly");
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeSettingTableSection();
        final SettingTable settingTable = modelFile.getSettingTable();
        final VariablesImport var = settingTable.newVariablesImport();

        final RobotToken cm1 = new RobotToken();
        cm1.setText("cm1");
        final RobotToken cm2 = new RobotToken();
        cm2.setText("cm2");
        final RobotToken cm3 = new RobotToken();
        cm3.setText("cm3");
        var.addCommentPart(cm1);
        var.addCommentPart(cm2);
        var.addCommentPart(cm3);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    @Test
    public void test_emptyFile_and_thanCreateVariablesImport_withNameAnd_ThreeArguments() throws Exception {
        // prepare
        final String fileName = convert("VariableDeclarationWithPathNameAndThreeArgsOnly");
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeSettingTableSection();
        final SettingTable settingTable = modelFile.getSettingTable();
        final VariablesImport var = settingTable.newVariablesImport();
        final RobotToken varName = new RobotToken();
        varName.setText("newVar.py");
        var.setPathOrName(varName);

        final RobotToken arg1 = new RobotToken();
        arg1.setText("arg1");
        final RobotToken arg2 = new RobotToken();
        arg2.setText("arg2");
        final RobotToken arg3 = new RobotToken();
        arg3.setText("arg3");
        var.addArgument(arg1);
        var.addArgument(arg2);
        var.addArgument(arg3);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    @Test
    public void test_emptyFile_and_thanCreateVariablesImport_withNameAnd_ThreeArgumentsAnd_ThreeComments()
            throws Exception {
        // prepare
        final String fileName = convert("VariableDeclarationWithPathNameAndThreeArgsThreeCommentsOnly");
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeSettingTableSection();
        final SettingTable settingTable = modelFile.getSettingTable();
        final VariablesImport var = settingTable.newVariablesImport();
        final RobotToken varName = new RobotToken();
        varName.setText("newVar.py");
        var.setPathOrName(varName);

        final RobotToken cm1 = new RobotToken();
        cm1.setText("cm1");
        final RobotToken cm2 = new RobotToken();
        cm2.setText("cm2");
        final RobotToken cm3 = new RobotToken();
        cm3.setText("cm3");
        var.addCommentPart(cm1);
        var.addCommentPart(cm2);
        var.addCommentPart(cm3);

        final RobotToken arg1 = new RobotToken();
        arg1.setText("arg1");
        final RobotToken arg2 = new RobotToken();
        arg2.setText("arg2");
        final RobotToken arg3 = new RobotToken();
        arg3.setText("arg3");
        var.addArgument(arg1);
        var.addArgument(arg2);
        var.addArgument(arg3);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    private String convert(final String fileName) {
        return "settings/variablesImport/new/" + fileName + "." + getExtension();
    }
}
