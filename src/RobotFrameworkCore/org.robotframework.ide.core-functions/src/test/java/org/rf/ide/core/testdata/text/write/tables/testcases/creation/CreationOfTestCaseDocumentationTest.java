/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.write.tables.testcases.creation;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.rf.ide.core.testdata.model.FileFormat;
import org.rf.ide.core.testdata.model.IDocumentationHolder;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.presenter.DocumentationServiceHandler;
import org.rf.ide.core.testdata.model.table.LocalSetting;
import org.rf.ide.core.testdata.model.table.TestCaseTable;
import org.rf.ide.core.testdata.model.table.testcases.TestCase;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.write.NewRobotFileTestHelper;

public class CreationOfTestCaseDocumentationTest {

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_emptyFile_and_thanCreateTestCaseDocumentation_withName_andThreeLinesOfDocumentation(
            final FileFormat format) throws Exception {
        // prepare
        final String filePath = convert("TestDocumentationWithThreeLinesCreation", format);
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeTestCaseTableSection();
        final TestCaseTable testCaseTable = modelFile.getTestCaseTable();

        final RobotToken testCaseName = new RobotToken();
        testCaseName.setText("TestCase");
        final TestCase testCase = new TestCase(testCaseName);
        testCaseTable.addTest(testCase);
        final LocalSetting<TestCase> testDoc = testCase.newDocumentation(0);

        DocumentationServiceHandler.update(testDoc.adaptTo(IDocumentationHolder.class),
                "doc me" + "\n" + "textZero" + "\n" + "textTwo");

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(filePath, modelFile);
    }

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_emptyFile_and_thanCreateTestCaseDocumentation_withoutTestCaseName_andDocumentationDecOnly(
            final FileFormat format) throws Exception {
        test_onlyTestCaseDoc_decIncluded("EmptyTestDocumentationNoTestName", "", format);
    }

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_emptyFile_and_thanCreateTestCaseDocumentation_withTestCaseName_andDocumentationDecOnly(
            final FileFormat format) throws Exception {
        test_onlyTestCaseDoc_decIncluded("EmptyTestDocumentation", "TestCase", format);
    }

    private void test_onlyTestCaseDoc_decIncluded(final String fileNameWithoutExt, final String userTestCaseName,
            final FileFormat format) throws Exception {
        // prepare
        final String filePath = convert(fileNameWithoutExt, format);
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

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_emptyFile_and_thanCreateTestCaseDocumentation_withoutTestCaseName_andDocumentationAndCommentOnly(
            final FileFormat format) throws Exception {
        test_testDoc_withCommentOnly("TestDocumentationNoTestNameAndComment", "", format);
    }

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_emptyFile_and_thanCreateTestCaseDocumentation_withTestCaseName_andDocumentationAndCommentOnly(
            final FileFormat format) throws Exception {
        test_testDoc_withCommentOnly("TestDocumentationAndComment", "TestCase", format);
    }

    private void test_testDoc_withCommentOnly(final String fileNameWithoutExt, final String userTestCaseName,
            final FileFormat format) throws Exception {
        // prepare
        final String filePath = convert(fileNameWithoutExt, format);
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeTestCaseTableSection();
        final TestCaseTable testCaseTable = modelFile.getTestCaseTable();

        final RobotToken testCaseName = new RobotToken();
        testCaseName.setText(userTestCaseName);
        final TestCase test = new TestCase(testCaseName);
        testCaseTable.addTest(test);

        final LocalSetting<TestCase> testDoc = test.newDocumentation(0);
        testDoc.addCommentPart("cm1");
        testDoc.addCommentPart("cm2");
        testDoc.addCommentPart("cm3");

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(filePath, modelFile);
    }

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_emptyFile_and_thanCreateTestCaseDocumentation_withoutTestCaseName_andDocumentationAnd3Words(
            final FileFormat format) throws Exception {
        test_docOnlyWith3Words("TestDocumentationNoTestNameAnd3WordsInText", "", format);
    }

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_emptyFile_and_thanCreateTestCaseDocumentation_withTestCaseName_andDocumentationAnd3Words(
            final FileFormat format) throws Exception {
        test_docOnlyWith3Words("TestDocumentationAnd3WordsInText", "TestCase", format);
    }

    private void test_docOnlyWith3Words(final String fileNameWithoutExt, final String userTestCaseName,
            final FileFormat format) throws Exception {
        // prepare
        final String filePath = convert(fileNameWithoutExt, format);
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeTestCaseTableSection();
        final TestCaseTable testCaseTable = modelFile.getTestCaseTable();

        final RobotToken testCaseName = new RobotToken();
        testCaseName.setText(userTestCaseName);
        final TestCase test = new TestCase(testCaseName);
        testCaseTable.addTest(test);

        final LocalSetting<TestCase> testDoc = test.newDocumentation(0);
        testDoc.addToken("w1");
        testDoc.addToken("w2");
        testDoc.addToken("w3");

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(filePath, modelFile);
    }

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_emptyFile_and_thanCreateTestCaseDocumentation_withoutTestCaseName_andDocumentationAnd3WordsAndComment(
            final FileFormat format) throws Exception {
        test_testDoc_withDoc3Words_andComment("TestDocumentationNoTestNameAnd3WordsInTextAndComment", "", format);
    }

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_emptyFile_and_thanCreateTestCaseDocumentation_withTestCaseName_andDocumentationAnd3WordsAndComment(
            final FileFormat format) throws Exception {
        test_testDoc_withDoc3Words_andComment("TestDocumentationAnd3WordsInTextAndComment", "TestCase", format);
    }

    private void test_testDoc_withDoc3Words_andComment(final String fileNameWithoutExt, final String userTestCaseName,
            final FileFormat format) throws Exception {
        // prepare
        final String filePath = convert(fileNameWithoutExt, format);
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeTestCaseTableSection();
        final TestCaseTable testCaseTable = modelFile.getTestCaseTable();

        final RobotToken testCaseName = new RobotToken();
        testCaseName.setText(userTestCaseName);
        final TestCase test = new TestCase(testCaseName);
        testCaseTable.addTest(test);

        final LocalSetting<TestCase> testDoc = test.newDocumentation(0);
        testDoc.addToken("w1");
        testDoc.addToken("w2");
        testDoc.addToken("w3");
        testDoc.addCommentPart("cm1");
        testDoc.addCommentPart("cm2");
        testDoc.addCommentPart("cm3");

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(filePath, modelFile);
    }

    private String convert(final String fileName, final FileFormat format) {
        return "testCases/setting/documentation/new/" + fileName + "." + format.getExtension();
    }
}
