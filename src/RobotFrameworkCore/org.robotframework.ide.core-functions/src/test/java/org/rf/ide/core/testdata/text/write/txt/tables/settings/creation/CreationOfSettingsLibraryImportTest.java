/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.write.txt.tables.settings.creation;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;

import org.junit.Test;
import org.rf.ide.core.testdata.RobotFileDumper;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.RobotFileOutput;
import org.rf.ide.core.testdata.model.RobotVersion;
import org.rf.ide.core.testdata.model.table.SettingTable;
import org.rf.ide.core.testdata.model.table.setting.LibraryAlias;
import org.rf.ide.core.testdata.model.table.setting.LibraryImport;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.write.DumperTestHelper;
import org.rf.ide.core.testdata.text.write.DumperTestHelper.TextCompareResult;

public class CreationOfSettingsLibraryImportTest {

    private static final String PRETTY_NEW_DIR_LOCATION = "settings//libraryImport//new//";

    @Test
    public void test_emptyFile_and_thanCreateLibraryImport() throws Exception {
        // prepare
        final Path inputFile = DumperTestHelper.getINSTANCE()
                .getFile(PRETTY_NEW_DIR_LOCATION + "EmptyLibraryDeclarationOnly.txt");
        final String fileContent = DumperTestHelper.getINSTANCE()
                .readWithLineSeparatorPresave(inputFile)
                .replaceAll("\r\n", System.lineSeparator());
        final RobotFileDumper dumper = new RobotFileDumper();
        final RobotFileOutput created = new RobotFileOutput(RobotVersion.from("2.9"));

        // test data prepare
        final RobotFile modelFile = created.getFileModel();
        modelFile.includeSettingTableSection();
        final SettingTable settingTable = modelFile.getSettingTable();
        settingTable.newLibraryImport();

        // execute
        final String dumpResult = dumper.dump(modelFile.getParent());

        // verify
        final TextCompareResult cmpResult = DumperTestHelper.getINSTANCE().compare(fileContent, dumpResult);

        assertThat(cmpResult.report()).isNull();
    }

    @Test
    public void test_emptyFile_and_thanCreateLibraryImport_withName() throws Exception {
        // prepare
        final Path inputFile = DumperTestHelper.getINSTANCE()
                .getFile(PRETTY_NEW_DIR_LOCATION + "LibraryDeclarationWithLibraryNameOnly.txt");
        final String fileContent = DumperTestHelper.getINSTANCE()
                .readWithLineSeparatorPresave(inputFile)
                .replaceAll("\r\n", System.lineSeparator());
        final RobotFileDumper dumper = new RobotFileDumper();
        final RobotFileOutput created = new RobotFileOutput(RobotVersion.from("2.9"));

        // test data prepare
        final RobotFile modelFile = created.getFileModel();
        modelFile.includeSettingTableSection();
        final SettingTable settingTable = modelFile.getSettingTable();
        final LibraryImport lib = settingTable.newLibraryImport();
        RobotToken libName = new RobotToken();
        libName.setText("newLib.py");
        lib.setPathOrName(libName);

        // execute
        final String dumpResult = dumper.dump(modelFile.getParent());

        // verify
        final TextCompareResult cmpResult = DumperTestHelper.getINSTANCE().compare(fileContent, dumpResult);

        assertThat(cmpResult.report()).isNull();
    }

    @Test
    public void test_emptyFile_and_thanCreateLibraryImport_withNameAnd_ThreeCommentTokens() throws Exception {
        // prepare
        final Path inputFile = DumperTestHelper.getINSTANCE()
                .getFile(PRETTY_NEW_DIR_LOCATION + "LibraryDeclarationWithLibraryNameAndThreeCommentOnly.txt");
        final String fileContent = DumperTestHelper.getINSTANCE()
                .readWithLineSeparatorPresave(inputFile)
                .replaceAll("\r\n", System.lineSeparator());
        final RobotFileDumper dumper = new RobotFileDumper();
        final RobotFileOutput created = new RobotFileOutput(RobotVersion.from("2.9"));

        // test data prepare
        final RobotFile modelFile = created.getFileModel();
        modelFile.includeSettingTableSection();
        final SettingTable settingTable = modelFile.getSettingTable();
        final LibraryImport lib = settingTable.newLibraryImport();
        RobotToken libName = new RobotToken();
        libName.setText("newLib.py");
        lib.setPathOrName(libName);

        RobotToken cm1 = new RobotToken();
        cm1.setText("cm1");
        RobotToken cm2 = new RobotToken();
        cm2.setText("cm2");
        RobotToken cm3 = new RobotToken();
        cm3.setText("cm3");
        lib.addCommentPart(cm1);
        lib.addCommentPart(cm2);
        lib.addCommentPart(cm3);

        // execute
        final String dumpResult = dumper.dump(modelFile.getParent());

        // verify
        final TextCompareResult cmpResult = DumperTestHelper.getINSTANCE().compare(fileContent, dumpResult);

        assertThat(cmpResult.report()).isNull();
    }

    @Test
    public void test_emptyFile_and_thanCreateLibraryImport_with_ThreeCommentTokens() throws Exception {
        // prepare
        final Path inputFile = DumperTestHelper.getINSTANCE()
                .getFile(PRETTY_NEW_DIR_LOCATION + "LibraryDeclarationWithThreeCommentOnly.txt");
        final String fileContent = DumperTestHelper.getINSTANCE()
                .readWithLineSeparatorPresave(inputFile)
                .replaceAll("\r\n", System.lineSeparator());
        final RobotFileDumper dumper = new RobotFileDumper();
        final RobotFileOutput created = new RobotFileOutput(RobotVersion.from("2.9"));

        // test data prepare
        final RobotFile modelFile = created.getFileModel();
        modelFile.includeSettingTableSection();
        final SettingTable settingTable = modelFile.getSettingTable();
        final LibraryImport lib = settingTable.newLibraryImport();

        RobotToken cm1 = new RobotToken();
        cm1.setText("cm1");
        RobotToken cm2 = new RobotToken();
        cm2.setText("cm2");
        RobotToken cm3 = new RobotToken();
        cm3.setText("cm3");
        lib.addCommentPart(cm1);
        lib.addCommentPart(cm2);
        lib.addCommentPart(cm3);

        // execute
        final String dumpResult = dumper.dump(modelFile.getParent());

        // verify
        final TextCompareResult cmpResult = DumperTestHelper.getINSTANCE().compare(fileContent, dumpResult);

        assertThat(cmpResult.report()).isNull();
    }

    @Test
    public void test_emptyFile_and_thanCreateLibraryImport_withNameAnd_ThreeArguments() throws Exception {
        // prepare
        final Path inputFile = DumperTestHelper.getINSTANCE()
                .getFile(PRETTY_NEW_DIR_LOCATION + "LibraryDeclarationWithLibraryNameAndThreeArgsOnly.txt");
        final String fileContent = DumperTestHelper.getINSTANCE()
                .readWithLineSeparatorPresave(inputFile)
                .replaceAll("\r\n", System.lineSeparator());
        final RobotFileDumper dumper = new RobotFileDumper();
        final RobotFileOutput created = new RobotFileOutput(RobotVersion.from("2.9"));

        // test data prepare
        final RobotFile modelFile = created.getFileModel();
        modelFile.includeSettingTableSection();
        final SettingTable settingTable = modelFile.getSettingTable();
        final LibraryImport lib = settingTable.newLibraryImport();
        RobotToken libName = new RobotToken();
        libName.setText("newLib.py");
        lib.setPathOrName(libName);

        RobotToken arg1 = new RobotToken();
        arg1.setText("arg1");
        RobotToken arg2 = new RobotToken();
        arg2.setText("arg2");
        RobotToken arg3 = new RobotToken();
        arg3.setText("arg3");
        lib.addArgument(arg1);
        lib.addArgument(arg2);
        lib.addArgument(arg3);

        // execute
        final String dumpResult = dumper.dump(modelFile.getParent());

        // verify
        final TextCompareResult cmpResult = DumperTestHelper.getINSTANCE().compare(fileContent, dumpResult);

        assertThat(cmpResult.report()).isNull();
    }

    @Test
    public void test_emptyFile_and_thanCreateLibraryImport_withNameAnd_ThreeArgumentsAnd_ThreeComments()
            throws Exception {
        // prepare
        final Path inputFile = DumperTestHelper.getINSTANCE().getFile(
                PRETTY_NEW_DIR_LOCATION + "LibraryDeclarationWithLibraryNameAndThreeArgsThreeCommentsOnly.txt");
        final String fileContent = DumperTestHelper.getINSTANCE()
                .readWithLineSeparatorPresave(inputFile)
                .replaceAll("\r\n", System.lineSeparator());
        final RobotFileDumper dumper = new RobotFileDumper();
        final RobotFileOutput created = new RobotFileOutput(RobotVersion.from("2.9"));

        // test data prepare
        final RobotFile modelFile = created.getFileModel();
        modelFile.includeSettingTableSection();
        final SettingTable settingTable = modelFile.getSettingTable();
        final LibraryImport lib = settingTable.newLibraryImport();
        RobotToken libName = new RobotToken();
        libName.setText("newLib.py");
        lib.setPathOrName(libName);

        RobotToken cm1 = new RobotToken();
        cm1.setText("cm1");
        RobotToken cm2 = new RobotToken();
        cm2.setText("cm2");
        RobotToken cm3 = new RobotToken();
        cm3.setText("cm3");
        lib.addCommentPart(cm1);
        lib.addCommentPart(cm2);
        lib.addCommentPart(cm3);

        RobotToken arg1 = new RobotToken();
        arg1.setText("arg1");
        RobotToken arg2 = new RobotToken();
        arg2.setText("arg2");
        RobotToken arg3 = new RobotToken();
        arg3.setText("arg3");
        lib.addArgument(arg1);
        lib.addArgument(arg2);
        lib.addArgument(arg3);

        // execute
        final String dumpResult = dumper.dump(modelFile.getParent());

        // verify
        final TextCompareResult cmpResult = DumperTestHelper.getINSTANCE().compare(fileContent, dumpResult);

        assertThat(cmpResult.report()).isNull();
    }

    @Test
    public void test_emptyFile_and_thanCreateLibraryImport_withNameAnd_ThreeArgumentsAnd_ThreeCommentsAndAlias()
            throws Exception {
        // prepare
        final Path inputFile = DumperTestHelper.getINSTANCE().getFile(
                PRETTY_NEW_DIR_LOCATION + "LibraryDeclarationWithLibraryNameAndThreeArgsThreeCommentsAliasOnly.txt");
        final String fileContent = DumperTestHelper.getINSTANCE()
                .readWithLineSeparatorPresave(inputFile)
                .replaceAll("\r\n", System.lineSeparator());
        final RobotFileDumper dumper = new RobotFileDumper();
        final RobotFileOutput created = new RobotFileOutput(RobotVersion.from("2.9"));

        // test data prepare
        final RobotFile modelFile = created.getFileModel();
        modelFile.includeSettingTableSection();
        final SettingTable settingTable = modelFile.getSettingTable();
        final LibraryImport lib = settingTable.newLibraryImport();
        RobotToken libName = new RobotToken();
        libName.setText("newLib.py");
        lib.setPathOrName(libName);

        LibraryAlias newAlias = lib.newAlias();
        RobotToken libraryAlias = new RobotToken();
        libraryAlias.setText("aliasLib");
        newAlias.setLibraryAlias(libraryAlias);

        RobotToken cm1 = new RobotToken();
        cm1.setText("cm1");
        RobotToken cm2 = new RobotToken();
        cm2.setText("cm2");
        RobotToken cm3 = new RobotToken();
        cm3.setText("cm3");
        lib.addCommentPart(cm1);
        lib.addCommentPart(cm2);
        lib.addCommentPart(cm3);

        RobotToken arg1 = new RobotToken();
        arg1.setText("arg1");
        RobotToken arg2 = new RobotToken();
        arg2.setText("arg2");
        RobotToken arg3 = new RobotToken();
        arg3.setText("arg3");
        lib.addArgument(arg1);
        lib.addArgument(arg2);
        lib.addArgument(arg3);

        // execute
        final String dumpResult = dumper.dump(modelFile.getParent());

        // verify
        final TextCompareResult cmpResult = DumperTestHelper.getINSTANCE().compare(fileContent, dumpResult);

        assertThat(cmpResult.report()).isNull();
    }
}
