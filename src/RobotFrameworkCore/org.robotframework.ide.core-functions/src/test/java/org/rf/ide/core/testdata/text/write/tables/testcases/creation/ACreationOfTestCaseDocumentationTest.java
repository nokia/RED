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
import org.rf.ide.core.testdata.text.write.NewRobotFileTestHelper;

public abstract class ACreationOfTestCaseDocumentationTest {

    public static final String PRETTY_NEW_DIR_LOCATION = "testCases//setting//documentation//new//";

    private final String extension;

    public ACreationOfTestCaseDocumentationTest(final String extension) {
        this.extension = extension;
    }

    @Test
    public void test_emptyFile_and_thanCreateTestCaseDocumentation_withName_andThreeLinesOfDocumentation()
            throws Exception {
        // prepare
        final String filePath = PRETTY_NEW_DIR_LOCATION + "TestDocumentationWithThreeLinesCreation" + "."
                + getExtension();
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeTestCaseTableSection();
        TestCaseTable testCaseTable = modelFile.getTestCaseTable();

        RobotToken testCaseName = new RobotToken();
        testCaseName.setText("TestCase");
        TestCase testCase = new TestCase(testCaseName);
        testCaseTable.addTest(testCase);
        TestDocumentation testDoc = testCase.newDocumentation();

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

    private void test_onlyTestCaseDoc_decIncluded(final String fileName, final String userTestCaseName)
            throws Exception {
        // prepare
        final String filePath = PRETTY_NEW_DIR_LOCATION + fileName + "." + getExtension();
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeTestCaseTableSection();
        TestCaseTable testCaseTable = modelFile.getTestCaseTable();

        RobotToken testCaseName = new RobotToken();
        testCaseName.setText(userTestCaseName);
        TestCase testCase = new TestCase(testCaseName);
        testCaseTable.addTest(testCase);
        testCase.newDocumentation();

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

    private void test_testDoc_withCommentOnly(final String fileName, final String userTestCaseName) throws Exception {
        // prepare
        final String filePath = PRETTY_NEW_DIR_LOCATION + fileName + "." + getExtension();
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeTestCaseTableSection();
        TestCaseTable testCaseTable = modelFile.getTestCaseTable();

        RobotToken testCaseName = new RobotToken();
        testCaseName.setText(userTestCaseName);
        TestCase test = new TestCase(testCaseName);
        testCaseTable.addTest(test);
        TestDocumentation testDoc = test.newDocumentation();

        RobotToken cmTok1 = new RobotToken();
        cmTok1.setText("cm1");
        RobotToken cmTok2 = new RobotToken();
        cmTok2.setText("cm2");
        RobotToken cmTok3 = new RobotToken();
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

    private void test_docOnlyWith3Words(final String fileName, final String userTestCaseName) throws Exception {
        // prepare
        final String filePath = PRETTY_NEW_DIR_LOCATION + fileName + "." + getExtension();
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeTestCaseTableSection();
        TestCaseTable testCaseTable = modelFile.getTestCaseTable();

        RobotToken testCaseName = new RobotToken();
        testCaseName.setText(userTestCaseName);
        TestCase test = new TestCase(testCaseName);
        testCaseTable.addTest(test);
        TestDocumentation testDoc = test.newDocumentation();
        RobotToken wr1 = new RobotToken();
        wr1.setText("w1");
        RobotToken wr2 = new RobotToken();
        wr2.setText("w2");
        RobotToken wr3 = new RobotToken();
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

    private void test_testDoc_withDoc3Words_andComment(final String fileName, final String userTestCaseName)
            throws Exception {
        // prepare
        final String filePath = PRETTY_NEW_DIR_LOCATION + fileName + "." + getExtension();
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeTestCaseTableSection();
        TestCaseTable testCaseTable = modelFile.getTestCaseTable();

        RobotToken testCaseName = new RobotToken();
        testCaseName.setText(userTestCaseName);
        TestCase test = new TestCase(testCaseName);
        testCaseTable.addTest(test);
        TestDocumentation testDoc = test.newDocumentation();
        RobotToken wr1 = new RobotToken();
        wr1.setText("w1");
        RobotToken wr2 = new RobotToken();
        wr2.setText("w2");
        RobotToken wr3 = new RobotToken();
        wr3.setText("w3");

        testDoc.addDocumentationText(wr1);
        testDoc.addDocumentationText(wr2);
        testDoc.addDocumentationText(wr3);

        RobotToken cmTok1 = new RobotToken();
        cmTok1.setText("cm1");
        RobotToken cmTok2 = new RobotToken();
        cmTok2.setText("cm2");
        RobotToken cmTok3 = new RobotToken();
        cmTok3.setText("cm3");

        testDoc.addCommentPart(cmTok1);
        testDoc.addCommentPart(cmTok2);
        testDoc.addCommentPart(cmTok3);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(filePath, modelFile);
    }

    public String getExtension() {
        return extension;
    }
}
