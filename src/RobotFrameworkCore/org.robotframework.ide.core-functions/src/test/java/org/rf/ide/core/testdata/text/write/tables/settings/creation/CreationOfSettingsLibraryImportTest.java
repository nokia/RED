/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.write.tables.settings.creation;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.rf.ide.core.testdata.model.FileFormat;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.table.SettingTable;
import org.rf.ide.core.testdata.model.table.setting.LibraryAlias;
import org.rf.ide.core.testdata.model.table.setting.LibraryImport;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.write.NewRobotFileTestHelper;

public class CreationOfSettingsLibraryImportTest {

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_emptyFile_and_thanCreateLibraryImport(final FileFormat format) throws Exception {
        // prepare
        final String fileName = convert("EmptyLibraryDeclarationOnly", format);
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeSettingTableSection();
        final SettingTable settingTable = modelFile.getSettingTable();
        settingTable.newLibraryImport();

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_emptyFile_and_thanCreateLibraryImport_withName(final FileFormat format) throws Exception {
        // prepare
        final String fileName = convert("LibraryDeclarationWithLibraryNameOnly", format);
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeSettingTableSection();
        final SettingTable settingTable = modelFile.getSettingTable();
        final LibraryImport lib = settingTable.newLibraryImport();
        final RobotToken libName = new RobotToken();
        libName.setText("newLib.py");
        lib.setPathOrName(libName);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_emptyFile_and_thanCreateLibraryImport_withNameAnd_ThreeCommentTokens(final FileFormat format)
            throws Exception {
        // prepare
        final String fileName = convert("LibraryDeclarationWithLibraryNameAndThreeCommentOnly", format);
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeSettingTableSection();
        final SettingTable settingTable = modelFile.getSettingTable();
        final LibraryImport lib = settingTable.newLibraryImport();
        final RobotToken libName = new RobotToken();
        libName.setText("newLib.py");
        lib.setPathOrName(libName);

        final RobotToken cm1 = new RobotToken();
        cm1.setText("cm1");
        final RobotToken cm2 = new RobotToken();
        cm2.setText("cm2");
        final RobotToken cm3 = new RobotToken();
        cm3.setText("cm3");
        lib.addCommentPart(cm1);
        lib.addCommentPart(cm2);
        lib.addCommentPart(cm3);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_emptyFile_and_thanCreateLibraryImport_with_ThreeCommentTokens(final FileFormat format)
            throws Exception {
        // prepare
        final String fileName = convert("LibraryDeclarationWithThreeCommentOnly", format);
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeSettingTableSection();
        final SettingTable settingTable = modelFile.getSettingTable();
        final LibraryImport lib = settingTable.newLibraryImport();

        final RobotToken cm1 = new RobotToken();
        cm1.setText("cm1");
        final RobotToken cm2 = new RobotToken();
        cm2.setText("cm2");
        final RobotToken cm3 = new RobotToken();
        cm3.setText("cm3");
        lib.addCommentPart(cm1);
        lib.addCommentPart(cm2);
        lib.addCommentPart(cm3);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_emptyFile_and_thanCreateLibraryImport_withNameAnd_ThreeArguments(final FileFormat format)
            throws Exception {
        // prepare
        final String fileName = convert("LibraryDeclarationWithLibraryNameAndThreeArgsOnly", format);
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeSettingTableSection();
        final SettingTable settingTable = modelFile.getSettingTable();
        final LibraryImport lib = settingTable.newLibraryImport();
        final RobotToken libName = new RobotToken();
        libName.setText("newLib.py");
        lib.setPathOrName(libName);

        final RobotToken arg1 = new RobotToken();
        arg1.setText("arg1");
        final RobotToken arg2 = new RobotToken();
        arg2.setText("arg2");
        final RobotToken arg3 = new RobotToken();
        arg3.setText("arg3");
        lib.addArgument(arg1);
        lib.addArgument(arg2);
        lib.addArgument(arg3);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_emptyFile_and_thanCreateLibraryImport_withNameAnd_ThreeArgumentsAnd_ThreeComments(
            final FileFormat format) throws Exception {
        // prepare
        final String fileName = convert("LibraryDeclarationWithLibraryNameAndThreeArgsThreeCommentsOnly", format);
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeSettingTableSection();
        final SettingTable settingTable = modelFile.getSettingTable();
        final LibraryImport lib = settingTable.newLibraryImport();
        final RobotToken libName = new RobotToken();
        libName.setText("newLib.py");
        lib.setPathOrName(libName);

        final RobotToken cm1 = new RobotToken();
        cm1.setText("cm1");
        final RobotToken cm2 = new RobotToken();
        cm2.setText("cm2");
        final RobotToken cm3 = new RobotToken();
        cm3.setText("cm3");
        lib.addCommentPart(cm1);
        lib.addCommentPart(cm2);
        lib.addCommentPart(cm3);

        final RobotToken arg1 = new RobotToken();
        arg1.setText("arg1");
        final RobotToken arg2 = new RobotToken();
        arg2.setText("arg2");
        final RobotToken arg3 = new RobotToken();
        arg3.setText("arg3");
        lib.addArgument(arg1);
        lib.addArgument(arg2);
        lib.addArgument(arg3);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_emptyFile_and_thanCreateLibraryImport_withNameAnd_ThreeArgumentsAnd_ThreeCommentsAndAlias(
            final FileFormat format) throws Exception {
        // prepare
        final String fileName = convert("LibraryDeclarationWithLibraryNameAndThreeArgsThreeCommentsAliasOnly", format);
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeSettingTableSection();
        final SettingTable settingTable = modelFile.getSettingTable();
        final LibraryImport lib = settingTable.newLibraryImport();
        final RobotToken libName = new RobotToken();
        libName.setText("newLib.py");
        lib.setPathOrName(libName);

        final LibraryAlias newAlias = lib.newAlias();
        final RobotToken libraryAlias = new RobotToken();
        libraryAlias.setText("aliasLib");
        newAlias.setLibraryAlias(libraryAlias);

        final RobotToken cm1 = new RobotToken();
        cm1.setText("cm1");
        final RobotToken cm2 = new RobotToken();
        cm2.setText("cm2");
        final RobotToken cm3 = new RobotToken();
        cm3.setText("cm3");
        lib.addCommentPart(cm1);
        lib.addCommentPart(cm2);
        lib.addCommentPart(cm3);

        final RobotToken arg1 = new RobotToken();
        arg1.setText("arg1");
        final RobotToken arg2 = new RobotToken();
        arg2.setText("arg2");
        final RobotToken arg3 = new RobotToken();
        arg3.setText("arg3");
        lib.addArgument(arg1);
        lib.addArgument(arg2);
        lib.addArgument(arg3);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    private String convert(final String fileName, final FileFormat format) {
        return "settings/libraryImport/new/" + fileName + "." + format.getExtension();
    }
}
