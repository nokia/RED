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
import org.rf.ide.core.testdata.model.table.testcases.TestCaseSetup;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.separators.TokenSeparatorBuilder.FileFormat;
import org.rf.ide.core.testdata.text.write.NewRobotFileTestHelper;
import org.rf.ide.core.testdata.text.write.RobotFormatParameterizedTest;

public class CreationOfTestCaseSetupTest extends RobotFormatParameterizedTest {

    public CreationOfTestCaseSetupTest(final String extension, final FileFormat format) {
        super(extension, format);
    }

    @Test
    public void test_emptyFile_and_thanCreateTestSetup_withoutTestName_andSetupDecOnly() throws Exception {
        test_setupDecOnly("EmptyTestSetupNoTestName", "");
    }

    @Test
    public void test_emptyFile_and_thanCreateTestSetup_withTestName_andSetupDecOnly() throws Exception {
        test_setupDecOnly("EmptyTestSetup", "TestCase");
    }

    private void test_setupDecOnly(final String fileNameWithoutExt, final String userTestName) throws Exception {
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
        test.newSetup(0);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(filePath, modelFile);
    }

    @Test
    public void test_emptyFile_and_thanCreateTestSetup_withoutTestName_andSetup_andComment() throws Exception {
        test_setupWithCommentOnly("EmptyTestSetupCommentNoTestName", "");
    }

    @Test
    public void test_emptyFile_and_thanCreateTestSetup_withTestName_andSetup_andComment() throws Exception {
        test_setupWithCommentOnly("EmptyTestSetupComment", "TestCase");
    }

    private void test_setupWithCommentOnly(final String fileNameWithoutExt, final String userTestName)
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
        final TestCaseSetup testSetup = test.newSetup(0);

        final RobotToken cmTok1 = new RobotToken();
        cmTok1.setText("cm1");
        final RobotToken cmTok2 = new RobotToken();
        cmTok2.setText("cm2");
        final RobotToken cmTok3 = new RobotToken();
        cmTok3.setText("cm3");

        testSetup.addCommentPart(cmTok1);
        testSetup.addCommentPart(cmTok2);
        testSetup.addCommentPart(cmTok3);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(filePath, modelFile);
    }

    @Test
    public void test_emptyFile_and_thanCreateTestSetup_withoutTestName_andSetup_andExecKey() throws Exception {
        test_setupWithExec("TestSetupExecKeywordNoTestName", "");
    }

    @Test
    public void test_emptyFile_and_thanCreateTestSetup_withTestName_andSetup_andExecKey() throws Exception {
        test_setupWithExec("TestSetupExecKeyword", "TestCase");
    }

    private void test_setupWithExec(final String fileNameWithoutExt, final String userTestName) throws Exception {
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
        final TestCaseSetup testSetup = test.newSetup(0);

        final RobotToken keywordName = new RobotToken();
        keywordName.setText("execKey");
        testSetup.setKeywordName(keywordName);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(filePath, modelFile);
    }

    @Test
    public void test_emptyFile_and_thanCreateTestSetup_withoutTestName_andSetup_andExecKey_andComment()
            throws Exception {
        test_setupWithExec_andComment("TestSetupExecKeywordAndCommentNoTestName", "");
    }

    @Test
    public void test_emptyFile_and_thanCreateTestSetup_withTestName_andSetup_andExecKey_andComment() throws Exception {
        test_setupWithExec_andComment("TestSetupExecKeywordAndComment", "TestCase");
    }

    private void test_setupWithExec_andComment(final String fileNameWithoutExt, final String userTestName)
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
        final TestCaseSetup testSetup = test.newSetup(0);

        final RobotToken keywordName = new RobotToken();
        keywordName.setText("execKey");
        testSetup.setKeywordName(keywordName);

        final RobotToken cmTok1 = new RobotToken();
        cmTok1.setText("cm1");
        final RobotToken cmTok2 = new RobotToken();
        cmTok2.setText("cm2");
        final RobotToken cmTok3 = new RobotToken();
        cmTok3.setText("cm3");

        testSetup.addCommentPart(cmTok1);
        testSetup.addCommentPart(cmTok2);
        testSetup.addCommentPart(cmTok3);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(filePath, modelFile);
    }

    @Test
    public void test_emptyFile_and_thanCreateTestSetup_withoutTestName_andSetup_andExecKey_and3Args() throws Exception {
        test_setupWithExec_and3Args("TestSetupExecKeywordAnd3ArgsNoTestName", "");
    }

    @Test
    public void test_emptyFile_and_thanCreateTestSetup_withTestName_andSetup_andExecKey_and3Args() throws Exception {
        test_setupWithExec_and3Args("TestSetupExecKeywordAnd3Args", "TestCase");
    }

    private void test_setupWithExec_and3Args(final String fileNameWithoutExt, final String userTestName)
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
        final TestCaseSetup testSetup = test.newSetup(0);

        final RobotToken keywordName = new RobotToken();
        keywordName.setText("execKey");
        testSetup.setKeywordName(keywordName);

        final RobotToken arg1 = new RobotToken();
        arg1.setText("arg1");
        final RobotToken arg2 = new RobotToken();
        arg2.setText("arg2");
        final RobotToken arg3 = new RobotToken();
        arg3.setText("arg3");

        testSetup.addArgument(arg1);
        testSetup.addArgument(arg2);
        testSetup.addArgument(arg3);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(filePath, modelFile);
    }

    @Test
    public void test_emptyFile_and_thanCreateTestSetup_withoutTestName_andSetup_andExecKey_and3Args_andComment()
            throws Exception {
        test_setupWithExec_and3Args_andComment("TestSetupExecKeywordAnd3ArgsAndCommentNoTestName", "");
    }

    @Test
    public void test_emptyFile_and_thanCreateTestSetup_withTestName_andSetup_andExecKey_and3Args_andComment()
            throws Exception {
        test_setupWithExec_and3Args_andComment("TestSetupExecKeywordAnd3ArgsAndComment", "TestCase");
    }

    private void test_setupWithExec_and3Args_andComment(final String fileNameWithoutExt, final String userTestName)
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
        final TestCaseSetup testSetup = test.newSetup(0);

        final RobotToken keywordName = new RobotToken();
        keywordName.setText("execKey");
        testSetup.setKeywordName(keywordName);

        final RobotToken arg1 = new RobotToken();
        arg1.setText("arg1");
        final RobotToken arg2 = new RobotToken();
        arg2.setText("arg2");
        final RobotToken arg3 = new RobotToken();
        arg3.setText("arg3");

        testSetup.addArgument(arg1);
        testSetup.addArgument(arg2);
        testSetup.addArgument(arg3);

        final RobotToken cmTok1 = new RobotToken();
        cmTok1.setText("cm1");
        final RobotToken cmTok2 = new RobotToken();
        cmTok2.setText("cm2");
        final RobotToken cmTok3 = new RobotToken();
        cmTok3.setText("cm3");

        testSetup.addCommentPart(cmTok1);
        testSetup.addCommentPart(cmTok2);
        testSetup.addCommentPart(cmTok3);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(filePath, modelFile);
    }

    private String convert(final String fileName) {
        return "testCases/setting/setup/new/" + fileName + "." + getExtension();
    }
}
