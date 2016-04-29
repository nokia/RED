/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.write.txt.tables.settings;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;

import org.junit.Test;
import org.rf.ide.core.testdata.RobotFileDumper;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.RobotFileOutput;
import org.rf.ide.core.testdata.model.RobotVersion;
import org.rf.ide.core.testdata.model.table.ARobotSectionTable;
import org.rf.ide.core.testdata.model.table.TableHeader;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.write.DumperTestHelper;
import org.rf.ide.core.testdata.text.write.DumperTestHelper.TextCompareResult;

public class CreationOfSettingTableHeaderTest {

    private static final String PRETTY_NEW_DIR_LOCATION = "settings//header//new//";

    @Test
    public void test_emptyFile_and_thanCreateSettingHeaderOnly() throws Exception {
        // prepare
        final Path inputFile = DumperTestHelper.getINSTANCE()
                .getFile(PRETTY_NEW_DIR_LOCATION + "SettingsHeaderOnly.txt");
        final String fileContent = DumperTestHelper.getINSTANCE().readWithLineSeparatorPresave(inputFile);
        final RobotFileDumper dumper = new RobotFileDumper();
        final RobotFileOutput created = new RobotFileOutput(RobotVersion.from("2.9"));

        // test data prepare
        final RobotFile modelFile = created.getFileModel();
        modelFile.includeSettingTableSection();

        // execute
        final String dumpResult = dumper.dump(modelFile.getParent());

        // verify
        final TextCompareResult cmpResult = DumperTestHelper.getINSTANCE().compare(fileContent, dumpResult);

        assertThat(cmpResult.report()).isNull();
    }

    @Test
    public void test_emptyFile_and_thanCreateSettingHeader_withTwoNamedColumns() throws Exception {
        // prepare
        final Path inputFile = DumperTestHelper.getINSTANCE()
                .getFile(PRETTY_NEW_DIR_LOCATION + "SettingsHeaderWithColumns.txt");
        final String fileContent = DumperTestHelper.getINSTANCE().readWithLineSeparatorPresave(inputFile);
        final RobotFileDumper dumper = new RobotFileDumper();
        final RobotFileOutput created = new RobotFileOutput(RobotVersion.from("2.9"));

        // test data prepare
        final RobotFile modelFile = created.getFileModel();
        modelFile.includeSettingTableSection();
        final TableHeader<? extends ARobotSectionTable> tableHeader = modelFile.getSettingTable().getHeaders().get(0);
        RobotToken columnOne = new RobotToken();
        columnOne.setText("*** col1 ***");
        tableHeader.addColumnName(columnOne);
        RobotToken columnTwo = new RobotToken();
        columnTwo.setText("*** col2 ***");
        tableHeader.addColumnName(columnTwo);

        // execute
        final String dumpResult = dumper.dump(modelFile.getParent());

        // verify
        final TextCompareResult cmpResult = DumperTestHelper.getINSTANCE().compare(fileContent, dumpResult);

        assertThat(cmpResult.report()).isNull();
    }

    @Test
    public void test_emptyFile_and_thanCreateSettingHeader_withTwoCommentTokens() throws Exception {
        // prepare
        final Path inputFile = DumperTestHelper.getINSTANCE()
                .getFile(PRETTY_NEW_DIR_LOCATION + "SettingsHeaderWithComments.txt");
        final String fileContent = DumperTestHelper.getINSTANCE().readWithLineSeparatorPresave(inputFile);
        final RobotFileDumper dumper = new RobotFileDumper();
        final RobotFileOutput created = new RobotFileOutput(RobotVersion.from("2.9"));

        // test data prepare
        final RobotFile modelFile = created.getFileModel();
        modelFile.includeSettingTableSection();
        final TableHeader<? extends ARobotSectionTable> tableHeader = modelFile.getSettingTable().getHeaders().get(0);
        RobotToken commentOne = new RobotToken();
        commentOne.setText("comment");
        tableHeader.addComment(commentOne);
        RobotToken commentTwo = new RobotToken();
        commentTwo.setText("comment2");
        tableHeader.addComment(commentTwo);

        // execute
        final String dumpResult = dumper.dump(modelFile.getParent());

        // verify
        final TextCompareResult cmpResult = DumperTestHelper.getINSTANCE().compare(fileContent, dumpResult);

        assertThat(cmpResult.report()).isNull();
    }

    @Test
    public void test_emptyFile_and_thanCreateSettingHeader_withTwoNamedColumns_and_withTwoCommentTokens()
            throws Exception {
        // prepare
        final Path inputFile = DumperTestHelper.getINSTANCE()
                .getFile(PRETTY_NEW_DIR_LOCATION + "SettingsHeaderWithColumnsAndComments.txt");
        final String fileContent = DumperTestHelper.getINSTANCE().readWithLineSeparatorPresave(inputFile);
        final RobotFileDumper dumper = new RobotFileDumper();
        final RobotFileOutput created = new RobotFileOutput(RobotVersion.from("2.9"));

        // test data prepare
        final RobotFile modelFile = created.getFileModel();
        modelFile.includeSettingTableSection();
        final TableHeader<? extends ARobotSectionTable> tableHeader = modelFile.getSettingTable().getHeaders().get(0);

        RobotToken columnOne = new RobotToken();
        columnOne.setText("*** col1 ***");
        tableHeader.addColumnName(columnOne);

        RobotToken commentOne = new RobotToken();
        commentOne.setText("comment");
        tableHeader.addComment(commentOne);

        RobotToken columnTwo = new RobotToken();
        columnTwo.setText("*** col2 ***");
        tableHeader.addColumnName(columnTwo);

        RobotToken commentTwo = new RobotToken();
        commentTwo.setText("comment2");
        tableHeader.addComment(commentTwo);

        // execute
        final String dumpResult = dumper.dump(modelFile.getParent());

        // verify
        final TextCompareResult cmpResult = DumperTestHelper.getINSTANCE().compare(fileContent, dumpResult);

        assertThat(cmpResult.report()).isNull();
    }
}
