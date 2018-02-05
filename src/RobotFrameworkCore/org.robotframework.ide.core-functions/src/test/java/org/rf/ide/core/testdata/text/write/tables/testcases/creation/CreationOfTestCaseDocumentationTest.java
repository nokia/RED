/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.write.tables.testcases.creation;

import org.junit.Test;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.presenter.DocumentationServiceHandler;
import org.rf.ide.core.testdata.model.table.TestCaseTable;
import org.rf.ide.core.testdata.model.table.testcases.TestCase;
import org.rf.ide.core.testdata.model.table.testcases.TestDocumentation;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.separators.TokenSeparatorBuilder.FileFormat;
import org.rf.ide.core.testdata.text.write.NewRobotFileTestHelper;
import org.rf.ide.core.testdata.text.write.RobotFormatParameterizedTest;

public class CreationOfTestCaseDocumentationTest extends RobotFormatParameterizedTest {

    public CreationOfTestCaseDocumentationTest(final String extension, final FileFormat format) {
        super(extension, format);
    }

    @Test
    public void test_emptyFile_and_thanCreateTestCaseDocumentation_withName_andThreeLinesOfDocumentation()
            throws Exception {
        // prepare
        final String filePath = convert("TestDocumentationWithThreeLinesCreation");
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeTestCaseTableSection();
        final TestCaseTable testCaseTable = modelFile.getTestCaseTable();

        final RobotToken testCaseName = new RobotToken();
        testCaseName.setText("TestCase");
        final TestCase testCase = new TestCase(testCaseName);
        testCaseTable.addTest(testCase);
        final TestDocumentation testDoc = testCase.newDocumentation(0);

        DocumentationServiceHandler.update(testDoc, "doc me" + "\n" + "textZero" + "\n" + "textTwo");

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(filePath, modelFile);
    }

    @Test
    public void test_emptyFile_and_thanCreateTestCaseDocumentation_withoutTestCaseName_andDocumentationDecOnly()
            throws Exception {
        test_onlyTestCaseDoc_decIncluded("EmptyTestDocumentationNoTestName", "");
    }

    @Test
    public void test_emptyFile_and_thanCreateTestCaseDocumentation_withTestCaseName_andDocumentationDecOnly()
            throws Exception {
        test_onlyTestCaseDoc_decIncluded("EmptyTestDocumentation", "TestCase");
    }

    private void test_onlyTestCaseDoc_decIncluded(final String fileNameWithoutExt, final String userTestCaseName)
            throws Exception {
        // prepare
        final String filePath = convert(fileNameWithoutExt);
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeTestCaseTableSection();
        final TestCaseTable testCaseTable = modelFile.getTestCaseTable();

        final RobotToken testCaseName = new RobotToken();
        testCaseName.setText(userTestCaseName);
        final TestCase testCase = new TestCase(testCaseName);
        testCaseTable.addTest(testCase);
        testCase.newDocumentation(0);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(filePath, modelFile);
    }

    @Test
    public void test_emptyFile_and_thanCreateTestCaseDocumentation_withoutTestCaseName_andDocumentationAndCommentOnly()
            throws Exception {
        test_testDoc_withCommentOnly("TestDocumentationNoTestNameAndComment", "");
    }

    @Test
    public void test_emptyFile_and_thanCreateTestCaseDocumentation_withTestCaseName_andDocumentationAndCommentOnly()
            throws Exception {
        test_testDoc_withCommentOnly("TestDocumentationAndComment", "TestCase");
    }

    private void test_testDoc_withCommentOnly(final String fileNameWithoutExt, final String userTestCaseName)
            throws Exception {
        // prepare
        final String filePath = convert(fileNameWithoutExt);
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeTestCaseTableSection();
        final TestCaseTable testCaseTable = modelFile.getTestCaseTable();

        final RobotToken testCaseName = new RobotToken();
        testCaseName.setText(userTestCaseName);
        final TestCase test = new TestCase(testCaseName);
        testCaseTable.addTest(test);
        final TestDocumentation testDoc = test.newDocumentation(0);

        final RobotToken cmTok1 = new RobotToken();
        cmTok1.setText("cm1");
        final RobotToken cmTok2 = new RobotToken();
        cmTok2.setText("cm2");
        final RobotToken cmTok3 = new RobotToken();
        cmTok3.setText("cm3");

        testDoc.addCommentPart(cmTok1);
        testDoc.addCommentPart(cmTok2);
        testDoc.addCommentPart(cmTok3);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(filePath, modelFile);
    }

    @Test
    public void test_emptyFile_and_thanCreateTestCaseDocumentation_withoutTestCaseName_andDocumentationAnd3Words()
            throws Exception {
        test_docOnlyWith3Words("TestDocumentationNoTestNameAnd3WordsInText", "");
    }

    @Test
    public void test_emptyFile_and_thanCreateTestCaseDocumentation_withTestCaseName_andDocumentationAnd3Words()
            throws Exception {
        test_docOnlyWith3Words("TestDocumentationAnd3WordsInText", "TestCase");
    }

    private void test_docOnlyWith3Words(final String fileNameWithoutExt, final String userTestCaseName)
            throws Exception {
        // prepare
        final String filePath = convert(fileNameWithoutExt);
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeTestCaseTableSection();
        final TestCaseTable testCaseTable = modelFile.getTestCaseTable();

        final RobotToken testCaseName = new RobotToken();
        testCaseName.setText(userTestCaseName);
        final TestCase test = new TestCase(testCaseName);
        testCaseTable.addTest(test);
        final TestDocumentation testDoc = test.newDocumentation(0);
        final RobotToken wr1 = new RobotToken();
        wr1.setText("w1");
        final RobotToken wr2 = new RobotToken();
        wr2.setText("w2");
        final RobotToken wr3 = new RobotToken();
        wr3.setText("w3");

        testDoc.addDocumentationText(wr1);
        testDoc.addDocumentationText(wr2);
        testDoc.addDocumentationText(wr3);
        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(filePath, modelFile);
    }

    @Test
    public void test_emptyFile_and_thanCreateTestCaseDocumentation_withoutTestCaseName_andDocumentationAnd3WordsAndComment()
            throws Exception {
        test_testDoc_withDoc3Words_andComment("TestDocumentationNoTestNameAnd3WordsInTextAndComment", "");
    }

    @Test
    public void test_emptyFile_and_thanCreateTestCaseDocumentation_withTestCaseName_andDocumentationAnd3WordsAndComment()
            throws Exception {
        test_testDoc_withDoc3Words_andComment("TestDocumentationAnd3WordsInTextAndComment", "TestCase");
    }

    private void test_testDoc_withDoc3Words_andComment(final String fileNameWithoutExt, final String userTestCaseName)
            throws Exception {
        // prepare
        final String filePath = convert(fileNameWithoutExt);
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeTestCaseTableSection();
        final TestCaseTable testCaseTable = modelFile.getTestCaseTable();

        final RobotToken testCaseName = new RobotToken();
        testCaseName.setText(userTestCaseName);
        final TestCase test = new TestCase(testCaseName);
        testCaseTable.addTest(test);
        final TestDocumentation testDoc = test.newDocumentation(0);
        final RobotToken wr1 = new RobotToken();
        wr1.setText("w1");
        final RobotToken wr2 = new RobotToken();
        wr2.setText("w2");
        final RobotToken wr3 = new RobotToken();
        wr3.setText("w3");

        testDoc.addDocumentationText(wr1);
        testDoc.addDocumentationText(wr2);
        testDoc.addDocumentationText(wr3);

        final RobotToken cmTok1 = new RobotToken();
        cmTok1.setText("cm1");
        final RobotToken cmTok2 = new RobotToken();
        cmTok2.setText("cm2");
        final RobotToken cmTok3 = new RobotToken();
        cmTok3.setText("cm3");

        testDoc.addCommentPart(cmTok1);
        testDoc.addCommentPart(cmTok2);
        testDoc.addCommentPart(cmTok3);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(filePath, modelFile);
    }

    private String convert(final String fileName) {
        return "testCases/setting/documentation/new/" + fileName + "." + getExtension();
    }
}
