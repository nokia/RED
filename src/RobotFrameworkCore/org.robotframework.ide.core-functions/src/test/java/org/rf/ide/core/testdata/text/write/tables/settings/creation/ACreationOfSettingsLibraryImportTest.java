/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.write.tables.settings.creation;

import org.junit.Test;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.table.SettingTable;
import org.rf.ide.core.testdata.model.table.setting.LibraryAlias;
import org.rf.ide.core.testdata.model.table.setting.LibraryImport;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.write.NewRobotFileTestHelper;

public abstract class ACreationOfSettingsLibraryImportTest {

    public static final String PRETTY_NEW_DIR_LOCATION = "settings//libraryImport//new//";

    private final String extension;

    public ACreationOfSettingsLibraryImportTest(final String extension) {
        this.extension = extension;
    }

    public String getExtension() {
        return extension;
    }

    @Test
    public void test_emptyFile_and_thanCreateLibraryImport() throws Exception {
        // prepare
        final String fileName = PRETTY_NEW_DIR_LOCATION + "EmptyLibraryDeclarationOnly." + getExtension();
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeSettingTableSection();
        final SettingTable settingTable = modelFile.getSettingTable();
        settingTable.newLibraryImport();

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    @Test
    public void test_emptyFile_and_thanCreateLibraryImport_withName() throws Exception {
        // prepare
        final String fileName = PRETTY_NEW_DIR_LOCATION + "LibraryDeclarationWithLibraryNameOnly." + getExtension();
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

    @Test
    public void test_emptyFile_and_thanCreateLibraryImport_withNameAnd_ThreeCommentTokens() throws Exception {
        // prepare
        final String fileName = PRETTY_NEW_DIR_LOCATION + "LibraryDeclarationWithLibraryNameAndThreeCommentOnly."
                + getExtension();
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

    @Test
    public void test_emptyFile_and_thanCreateLibraryImport_with_ThreeCommentTokens() throws Exception {
        // prepare
        final String fileName = PRETTY_NEW_DIR_LOCATION + "LibraryDeclarationWithThreeCommentOnly." + getExtension();
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

    @Test
    public void test_emptyFile_and_thanCreateLibraryImport_withNameAnd_ThreeArguments() throws Exception {
        // prepare
        final String fileName = PRETTY_NEW_DIR_LOCATION + "LibraryDeclarationWithLibraryNameAndThreeArgsOnly."
                + getExtension();
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

    @Test
    public void test_emptyFile_and_thanCreateLibraryImport_withNameAnd_ThreeArgumentsAnd_ThreeComments()
            throws Exception {
        // prepare
        final String fileName = PRETTY_NEW_DIR_LOCATION
                + "LibraryDeclarationWithLibraryNameAndThreeArgsThreeCommentsOnly." + getExtension();
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

    @Test
    public void test_emptyFile_and_thanCreateLibraryImport_withNameAnd_ThreeArgumentsAnd_ThreeCommentsAndAlias()
            throws Exception {
        // prepare
        final String fileName = PRETTY_NEW_DIR_LOCATION
                + "LibraryDeclarationWithLibraryNameAndThreeArgsThreeCommentsAliasOnly." + getExtension();
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
}
