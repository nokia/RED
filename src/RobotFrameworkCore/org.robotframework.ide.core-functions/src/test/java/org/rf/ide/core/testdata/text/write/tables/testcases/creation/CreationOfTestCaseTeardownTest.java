/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.write.tables.testcases.creation;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.rf.ide.core.testdata.model.FileFormat;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.table.LocalSetting;
import org.rf.ide.core.testdata.model.table.TestCaseTable;
import org.rf.ide.core.testdata.model.table.testcases.TestCase;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.write.NewRobotFileTestHelper;

public class CreationOfTestCaseTeardownTest {

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_emptyFile_and_thanCreateTestTeardown_withoutTestName_andTeardownDecOnly(final FileFormat format)
            throws Exception {
        test_teardownDecOnly("EmptyTestTeardownNoTestName", "", format);
    }

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_emptyFile_and_thanCreateTestTeardown_withTestName_andTeardownDecOnly(final FileFormat format)
            throws Exception {
        test_teardownDecOnly("EmptyTestTeardown", "TestCase", format);
    }

    private void test_teardownDecOnly(final String fileNameWithoutExt, final String userTestName,
            final FileFormat format) throws Exception {
        // prepare
        final String filePath = convert(fileNameWithoutExt, format);
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeTestCaseTableSection();
        final TestCaseTable testCaseTable = modelFile.getTestCaseTable();

        final RobotToken testName = new RobotToken();
        testName.setText(userTestName);
        final TestCase test = new TestCase(testName);
        testCaseTable.addTest(test);
        test.newTeardown(0);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(filePath, modelFile);
    }

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_emptyFile_and_thanCreateTestTeardown_withoutTestName_andTeardown_andComment(
            final FileFormat format) throws Exception {
        test_teardownWithCommentOnly("EmptyTestTeardownCommentNoTestName", "", format);
    }

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_emptyFile_and_thanCreateTestTeardown_withTestName_andTeardown_andComment(final FileFormat format)
            throws Exception {
        test_teardownWithCommentOnly("EmptyTestTeardownComment", "TestCase", format);
    }

    private void test_teardownWithCommentOnly(final String fileNameWithoutExt, final String userTestName,
            final FileFormat format) throws Exception {
        // prepare
        final String filePath = convert(fileNameWithoutExt, format);
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeTestCaseTableSection();
        final TestCaseTable testCaseTable = modelFile.getTestCaseTable();

        final RobotToken testName = new RobotToken();
        testName.setText(userTestName);
        final TestCase test = new TestCase(testName);
        testCaseTable.addTest(test);

        final LocalSetting<TestCase> testTeardown = test.newTeardown(0);
        testTeardown.addCommentPart("cm1");
        testTeardown.addCommentPart("cm2");
        testTeardown.addCommentPart("cm3");

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(filePath, modelFile);
    }

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_emptyFile_and_thanCreateTestTeardown_withoutTestName_andTeardown_andExecKey(
            final FileFormat format) throws Exception {
        test_teardownWithExec("TestTeardownExecKeywordNoTestName", "", format);
    }

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_emptyFile_and_thanCreateTestTeardown_withTestName_andTeardown_andExecKey(final FileFormat format)
            throws Exception {
        test_teardownWithExec("TestTeardownExecKeyword", "TestCase", format);
    }

    private void test_teardownWithExec(final String fileNameWithoutExt, final String userTestName,
            final FileFormat format) throws Exception {
        // prepare
        final String filePath = convert(fileNameWithoutExt, format);
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeTestCaseTableSection();
        final TestCaseTable testCaseTable = modelFile.getTestCaseTable();

        final RobotToken testName = new RobotToken();
        testName.setText(userTestName);
        final TestCase test = new TestCase(testName);
        testCaseTable.addTest(test);

        final LocalSetting<TestCase> testTeardown = test.newTeardown(0);
        testTeardown.addToken("execKey");

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(filePath, modelFile);
    }

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_emptyFile_and_thanCreateTestTeardown_withoutTestName_andTeardown_andExecKey_andComment(
            final FileFormat format) throws Exception {
        test_teardownWithExec_andComment("TestTeardownExecKeywordAndCommentNoTestName", "", format);
    }

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_emptyFile_and_thanCreateTestTeardown_withTestName_andTeardown_andExecKey_andComment(
            final FileFormat format) throws Exception {
        test_teardownWithExec_andComment("TestTeardownExecKeywordAndComment", "TestCase", format);
    }

    private void test_teardownWithExec_andComment(final String fileNameWithoutExt, final String userTestName,
            final FileFormat format) throws Exception {
        // prepare
        final String filePath = convert(fileNameWithoutExt, format);
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeTestCaseTableSection();
        final TestCaseTable testCaseTable = modelFile.getTestCaseTable();

        final RobotToken testName = new RobotToken();
        testName.setText(userTestName);
        final TestCase test = new TestCase(testName);
        testCaseTable.addTest(test);

        final LocalSetting<TestCase> testTeardown = test.newTeardown(0);
        testTeardown.addToken("execKey");
        testTeardown.addCommentPart("cm1");
        testTeardown.addCommentPart("cm2");
        testTeardown.addCommentPart("cm3");

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(filePath, modelFile);
    }

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_emptyFile_and_thanCreateTestTeardown_withoutTestName_andTeardown_andExecKey_and3Args(
            final FileFormat format) throws Exception {
        test_teardownWithExec_and3Args("TestTeardownExecKeywordAnd3ArgsNoTestName", "", format);
    }

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_emptyFile_and_thanCreateTestTeardown_withTestName_andTeardown_andExecKey_and3Args(
            final FileFormat format) throws Exception {
        test_teardownWithExec_and3Args("TestTeardownExecKeywordAnd3Args", "TestCase", format);
    }

    private void test_teardownWithExec_and3Args(final String fileNameWithoutExt, final String userTestName,
            final FileFormat format) throws Exception {
        // prepare
        final String filePath = convert(fileNameWithoutExt, format);
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeTestCaseTableSection();
        final TestCaseTable testCaseTable = modelFile.getTestCaseTable();

        final RobotToken testName = new RobotToken();
        testName.setText(userTestName);
        final TestCase test = new TestCase(testName);
        testCaseTable.addTest(test);

        final LocalSetting<TestCase> testTeardown = test.newTeardown(0);
        testTeardown.addToken("execKey");
        testTeardown.addToken("arg1");
        testTeardown.addToken("arg2");
        testTeardown.addToken("arg3");

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(filePath, modelFile);
    }

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_emptyFile_and_thanCreateTestTeardown_withoutTestName_andTeardown_andExecKey_and3Args_andComment(
            final FileFormat format) throws Exception {
        test_teardownWithExec_and3Args_andComment("TestTeardownExecKeywordAnd3ArgsAndCommentNoTestName", "", format);
    }

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_emptyFile_and_thanCreateTestTeardown_withTestName_andTeardown_andExecKey_and3Args_andComment(
            final FileFormat format) throws Exception {
        test_teardownWithExec_and3Args_andComment("TestTeardownExecKeywordAnd3ArgsAndComment", "TestCase", format);
    }

    private void test_teardownWithExec_and3Args_andComment(final String fileNameWithoutExt, final String userTestName,
            final FileFormat format) throws Exception {
        // prepare
        final String filePath = convert(fileNameWithoutExt, format);
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeTestCaseTableSection();
        final TestCaseTable testCaseTable = modelFile.getTestCaseTable();

        final RobotToken testName = new RobotToken();
        testName.setText(userTestName);
        final TestCase test = new TestCase(testName);
        testCaseTable.addTest(test);

        final LocalSetting<TestCase> testTeardown = test.newTeardown(0);
        testTeardown.addToken("execKey");
        testTeardown.addToken("arg1");
        testTeardown.addToken("arg2");
        testTeardown.addToken("arg3");
        testTeardown.addCommentPart("cm1");
        testTeardown.addCommentPart("cm2");
        testTeardown.addCommentPart("cm3");

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(filePath, modelFile);
    }

    private String convert(final String fileName, final FileFormat format) {
        return "testCases/setting/teardown/new/" + fileName + "." + format.getExtension();
    }
}
