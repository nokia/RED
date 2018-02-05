/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.write.tables.testcases.creation;

import org.junit.Test;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.table.TestCaseTable;
import org.rf.ide.core.testdata.model.table.testcases.TestCase;
import org.rf.ide.core.testdata.model.table.testcases.TestCaseTeardown;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.separators.TokenSeparatorBuilder.FileFormat;
import org.rf.ide.core.testdata.text.write.NewRobotFileTestHelper;
import org.rf.ide.core.testdata.text.write.RobotFormatParameterizedTest;

public class CreationOfTestCaseTeardownTest extends RobotFormatParameterizedTest {

    public CreationOfTestCaseTeardownTest(final String extension, final FileFormat format) {
        super(extension, format);
    }

    @Test
    public void test_emptyFile_and_thanCreateTestTeardown_withoutTestName_andTeardownDecOnly() throws Exception {
        test_teardownDecOnly("EmptyTestTeardownNoTestName", "");
    }

    @Test
    public void test_emptyFile_and_thanCreateTestTeardown_withTestName_andTeardownDecOnly() throws Exception {
        test_teardownDecOnly("EmptyTestTeardown", "TestCase");
    }

    private void test_teardownDecOnly(final String fileNameWithoutExt, final String userTestName) throws Exception {
        // prepare
        final String filePath = convert(fileNameWithoutExt);
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

    @Test
    public void test_emptyFile_and_thanCreateTestTeardown_withoutTestName_andTeardown_andComment() throws Exception {
        test_teardownWithCommentOnly("EmptyTestTeardownCommentNoTestName", "");
    }

    @Test
    public void test_emptyFile_and_thanCreateTestTeardown_withTestName_andTeardown_andComment() throws Exception {
        test_teardownWithCommentOnly("EmptyTestTeardownComment", "TestCase");
    }

    private void test_teardownWithCommentOnly(final String fileNameWithoutExt, final String userTestName)
            throws Exception {
        // prepare
        final String filePath = convert(fileNameWithoutExt);
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeTestCaseTableSection();
        final TestCaseTable testCaseTable = modelFile.getTestCaseTable();

        final RobotToken testName = new RobotToken();
        testName.setText(userTestName);
        final TestCase test = new TestCase(testName);
        testCaseTable.addTest(test);
        final TestCaseTeardown testTeardown = test.newTeardown(0);

        final RobotToken cmTok1 = new RobotToken();
        cmTok1.setText("cm1");
        final RobotToken cmTok2 = new RobotToken();
        cmTok2.setText("cm2");
        final RobotToken cmTok3 = new RobotToken();
        cmTok3.setText("cm3");

        testTeardown.addCommentPart(cmTok1);
        testTeardown.addCommentPart(cmTok2);
        testTeardown.addCommentPart(cmTok3);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(filePath, modelFile);
    }

    @Test
    public void test_emptyFile_and_thanCreateTestTeardown_withoutTestName_andTeardown_andExecKey() throws Exception {
        test_teardownWithExec("TestTeardownExecKeywordNoTestName", "");
    }

    @Test
    public void test_emptyFile_and_thanCreateTestTeardown_withTestName_andTeardown_andExecKey() throws Exception {
        test_teardownWithExec("TestTeardownExecKeyword", "TestCase");
    }

    private void test_teardownWithExec(final String fileNameWithoutExt, final String userTestName) throws Exception {
        // prepare
        final String filePath = convert(fileNameWithoutExt);
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeTestCaseTableSection();
        final TestCaseTable testCaseTable = modelFile.getTestCaseTable();

        final RobotToken testName = new RobotToken();
        testName.setText(userTestName);
        final TestCase test = new TestCase(testName);
        testCaseTable.addTest(test);
        final TestCaseTeardown testTeardown = test.newTeardown(0);

        final RobotToken keywordName = new RobotToken();
        keywordName.setText("execKey");
        testTeardown.setKeywordName(keywordName);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(filePath, modelFile);
    }

    @Test
    public void test_emptyFile_and_thanCreateTestTeardown_withoutTestName_andTeardown_andExecKey_andComment()
            throws Exception {
        test_teardownWithExec_andComment("TestTeardownExecKeywordAndCommentNoTestName", "");
    }

    @Test
    public void test_emptyFile_and_thanCreateTestTeardown_withTestName_andTeardown_andExecKey_andComment()
            throws Exception {
        test_teardownWithExec_andComment("TestTeardownExecKeywordAndComment", "TestCase");
    }

    private void test_teardownWithExec_andComment(final String fileNameWithoutExt, final String userTestName)
            throws Exception {
        // prepare
        final String filePath = convert(fileNameWithoutExt);
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeTestCaseTableSection();
        final TestCaseTable testCaseTable = modelFile.getTestCaseTable();

        final RobotToken testName = new RobotToken();
        testName.setText(userTestName);
        final TestCase test = new TestCase(testName);
        testCaseTable.addTest(test);
        final TestCaseTeardown testTeardown = test.newTeardown(0);

        final RobotToken keywordName = new RobotToken();
        keywordName.setText("execKey");
        testTeardown.setKeywordName(keywordName);

        final RobotToken cmTok1 = new RobotToken();
        cmTok1.setText("cm1");
        final RobotToken cmTok2 = new RobotToken();
        cmTok2.setText("cm2");
        final RobotToken cmTok3 = new RobotToken();
        cmTok3.setText("cm3");

        testTeardown.addCommentPart(cmTok1);
        testTeardown.addCommentPart(cmTok2);
        testTeardown.addCommentPart(cmTok3);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(filePath, modelFile);
    }

    @Test
    public void test_emptyFile_and_thanCreateTestTeardown_withoutTestName_andTeardown_andExecKey_and3Args()
            throws Exception {
        test_teardownWithExec_and3Args("TestTeardownExecKeywordAnd3ArgsNoTestName", "");
    }

    @Test
    public void test_emptyFile_and_thanCreateTestTeardown_withTestName_andTeardown_andExecKey_and3Args()
            throws Exception {
        test_teardownWithExec_and3Args("TestTeardownExecKeywordAnd3Args", "TestCase");
    }

    private void test_teardownWithExec_and3Args(final String fileNameWithoutExt, final String userTestName)
            throws Exception {
        // prepare
        final String filePath = convert(fileNameWithoutExt);
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeTestCaseTableSection();
        final TestCaseTable testCaseTable = modelFile.getTestCaseTable();

        final RobotToken testName = new RobotToken();
        testName.setText(userTestName);
        final TestCase test = new TestCase(testName);
        testCaseTable.addTest(test);
        final TestCaseTeardown testTeardown = test.newTeardown(0);

        final RobotToken keywordName = new RobotToken();
        keywordName.setText("execKey");
        testTeardown.setKeywordName(keywordName);

        final RobotToken arg1 = new RobotToken();
        arg1.setText("arg1");
        final RobotToken arg2 = new RobotToken();
        arg2.setText("arg2");
        final RobotToken arg3 = new RobotToken();
        arg3.setText("arg3");

        testTeardown.addArgument(arg1);
        testTeardown.addArgument(arg2);
        testTeardown.addArgument(arg3);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(filePath, modelFile);
    }

    @Test
    public void test_emptyFile_and_thanCreateTestTeardown_withoutTestName_andTeardown_andExecKey_and3Args_andComment()
            throws Exception {
        test_teardownWithExec_and3Args_andComment("TestTeardownExecKeywordAnd3ArgsAndCommentNoTestName", "");
    }

    @Test
    public void test_emptyFile_and_thanCreateTestTeardown_withTestName_andTeardown_andExecKey_and3Args_andComment()
            throws Exception {
        test_teardownWithExec_and3Args_andComment("TestTeardownExecKeywordAnd3ArgsAndComment", "TestCase");
    }

    private void test_teardownWithExec_and3Args_andComment(final String fileNameWithoutExt, final String userTestName)
            throws Exception {
        // prepare
        final String filePath = convert(fileNameWithoutExt);
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeTestCaseTableSection();
        final TestCaseTable testCaseTable = modelFile.getTestCaseTable();

        final RobotToken testName = new RobotToken();
        testName.setText(userTestName);
        final TestCase test = new TestCase(testName);
        testCaseTable.addTest(test);
        final TestCaseTeardown testTeardown = test.newTeardown(0);

        final RobotToken keywordName = new RobotToken();
        keywordName.setText("execKey");
        testTeardown.setKeywordName(keywordName);

        final RobotToken arg1 = new RobotToken();
        arg1.setText("arg1");
        final RobotToken arg2 = new RobotToken();
        arg2.setText("arg2");
        final RobotToken arg3 = new RobotToken();
        arg3.setText("arg3");

        testTeardown.addArgument(arg1);
        testTeardown.addArgument(arg2);
        testTeardown.addArgument(arg3);

        final RobotToken cmTok1 = new RobotToken();
        cmTok1.setText("cm1");
        final RobotToken cmTok2 = new RobotToken();
        cmTok2.setText("cm2");
        final RobotToken cmTok3 = new RobotToken();
        cmTok3.setText("cm3");

        testTeardown.addCommentPart(cmTok1);
        testTeardown.addCommentPart(cmTok2);
        testTeardown.addCommentPart(cmTok3);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(filePath, modelFile);
    }

    private String convert(final String fileName) {
        return "testCases/setting/teardown/new/" + fileName + "." + getExtension();
    }
}
