/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.write.tables.variables.creation;

import org.junit.Test;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.table.ARobotSectionTable;
import org.rf.ide.core.testdata.model.table.TableHeader;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.separators.TokenSeparatorBuilder.FileFormat;
import org.rf.ide.core.testdata.text.write.NewRobotFileTestHelper;
import org.rf.ide.core.testdata.text.write.RobotFormatParameterizedTest;

public class CreationOfVariableTableHeaderTest extends RobotFormatParameterizedTest {

    public CreationOfVariableTableHeaderTest(final String extension, final FileFormat format) {
        super(extension, format);
    }

    @Test
    public void test_emptyFile_and_thanCreateVariableHeaderOnly() throws Exception {
        // prepare
        final String fileName = convert("VariablesHeaderOnly");
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeVariableTableSection();

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    @Test
    public void test_emptyFile_and_thanCreateVariableHeader_withTwoNamedColumns() throws Exception {
        // prepare
        final String fileName = convert("VariablesHeaderWithColumns");
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeVariableTableSection();
        final TableHeader<? extends ARobotSectionTable> tableHeader = modelFile.getVariableTable().getHeaders().get(0);
        final RobotToken columnOne = new RobotToken();
        columnOne.setText("*** col1 ***");
        tableHeader.addColumnName(columnOne);
        final RobotToken columnTwo = new RobotToken();
        columnTwo.setText("*** col2 ***");
        tableHeader.addColumnName(columnTwo);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    @Test
    public void test_emptyFile_and_thanCreateVariableHeader_withTwoCommentTokens() throws Exception {
        // prepare
        final String fileName = convert("VariablesHeaderWithComments");
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeVariableTableSection();
        final TableHeader<? extends ARobotSectionTable> tableHeader = modelFile.getVariableTable().getHeaders().get(0);
        final RobotToken commentOne = new RobotToken();
        commentOne.setText("comment");
        tableHeader.addCommentPart(commentOne);
        final RobotToken commentTwo = new RobotToken();
        commentTwo.setText("comment2");
        tableHeader.addCommentPart(commentTwo);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    @Test
    public void test_emptyFile_and_thanCreateVariableHeader_withTwoNamedColumns_and_withTwoCommentTokens()
            throws Exception {
        // prepare
        final String fileName = convert("VariablesHeaderWithColumnsAndComments");
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeVariableTableSection();
        final TableHeader<? extends ARobotSectionTable> tableHeader = modelFile.getVariableTable().getHeaders().get(0);

        final RobotToken columnOne = new RobotToken();
        columnOne.setText("*** col1 ***");
        tableHeader.addColumnName(columnOne);

        final RobotToken commentOne = new RobotToken();
        commentOne.setText("comment");
        tableHeader.addCommentPart(commentOne);

        final RobotToken columnTwo = new RobotToken();
        columnTwo.setText("*** col2 ***");
        tableHeader.addColumnName(columnTwo);

        final RobotToken commentTwo = new RobotToken();
        commentTwo.setText("comment2");
        tableHeader.addCommentPart(commentTwo);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    private String convert(final String fileName) {
        return "variables/header/new/" + fileName + "." + getExtension();
    }
}
