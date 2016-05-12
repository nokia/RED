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
import org.rf.ide.core.testdata.text.write.NewRobotFileTestHelper;

public abstract class ACreationOfTestCaseSetupTest {

    public static final String PRETTY_NEW_DIR_LOCATION = "testCases//setting//setup//new//";

    private final String extension;

    public ACreationOfTestCaseSetupTest(final String extension) {
        this.extension = extension;
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
        final String filePath = PRETTY_NEW_DIR_LOCATION + fileNameWithoutExt + "." + getExtension();
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeTestCaseTableSection();
        TestCaseTable testCaseTable = modelFile.getTestCaseTable();

        RobotToken testName = new RobotToken();
        testName.setText(userTestName);
        TestCase test = new TestCase(testName);
        testCaseTable.addTest(test);
        test.newSetup();

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
        final String filePath = PRETTY_NEW_DIR_LOCATION + fileNameWithoutExt + "." + getExtension();
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeTestCaseTableSection();
        TestCaseTable testCaseTable = modelFile.getTestCaseTable();

        RobotToken testName = new RobotToken();
        testName.setText(userTestName);
        TestCase test = new TestCase(testName);
        testCaseTable.addTest(test);
        TestCaseSetup testSetup = test.newSetup();

        RobotToken cmTok1 = new RobotToken();
        cmTok1.setText("cm1");
        RobotToken cmTok2 = new RobotToken();
        cmTok2.setText("cm2");
        RobotToken cmTok3 = new RobotToken();
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
        final String filePath = PRETTY_NEW_DIR_LOCATION + fileNameWithoutExt + "." + getExtension();
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeTestCaseTableSection();
        TestCaseTable testCaseTable = modelFile.getTestCaseTable();

        RobotToken testName = new RobotToken();
        testName.setText(userTestName);
        TestCase test = new TestCase(testName);
        testCaseTable.addTest(test);
        TestCaseSetup testSetup = test.newSetup();

        RobotToken keywordName = new RobotToken();
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
        final String filePath = PRETTY_NEW_DIR_LOCATION + fileNameWithoutExt + "." + getExtension();
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeTestCaseTableSection();
        TestCaseTable testCaseTable = modelFile.getTestCaseTable();

        RobotToken testName = new RobotToken();
        testName.setText(userTestName);
        TestCase test = new TestCase(testName);
        testCaseTable.addTest(test);
        TestCaseSetup testSetup = test.newSetup();

        RobotToken keywordName = new RobotToken();
        keywordName.setText("execKey");
        testSetup.setKeywordName(keywordName);

        RobotToken cmTok1 = new RobotToken();
        cmTok1.setText("cm1");
        RobotToken cmTok2 = new RobotToken();
        cmTok2.setText("cm2");
        RobotToken cmTok3 = new RobotToken();
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
        final String filePath = PRETTY_NEW_DIR_LOCATION + fileNameWithoutExt + "." + getExtension();
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeTestCaseTableSection();
        TestCaseTable testCaseTable = modelFile.getTestCaseTable();

        RobotToken testName = new RobotToken();
        testName.setText(userTestName);
        TestCase test = new TestCase(testName);
        testCaseTable.addTest(test);
        TestCaseSetup testSetup = test.newSetup();

        RobotToken keywordName = new RobotToken();
        keywordName.setText("execKey");
        testSetup.setKeywordName(keywordName);

        RobotToken arg1 = new RobotToken();
        arg1.setText("arg1");
        RobotToken arg2 = new RobotToken();
        arg2.setText("arg2");
        RobotToken arg3 = new RobotToken();
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
        final String filePath = PRETTY_NEW_DIR_LOCATION + fileNameWithoutExt + "." + getExtension();
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeTestCaseTableSection();
        TestCaseTable testCaseTable = modelFile.getTestCaseTable();

        RobotToken testName = new RobotToken();
        testName.setText(userTestName);
        TestCase test = new TestCase(testName);
        testCaseTable.addTest(test);
        TestCaseSetup testSetup = test.newSetup();

        RobotToken keywordName = new RobotToken();
        keywordName.setText("execKey");
        testSetup.setKeywordName(keywordName);

        RobotToken arg1 = new RobotToken();
        arg1.setText("arg1");
        RobotToken arg2 = new RobotToken();
        arg2.setText("arg2");
        RobotToken arg3 = new RobotToken();
        arg3.setText("arg3");

        testSetup.addArgument(arg1);
        testSetup.addArgument(arg2);
        testSetup.addArgument(arg3);

        RobotToken cmTok1 = new RobotToken();
        cmTok1.setText("cm1");
        RobotToken cmTok2 = new RobotToken();
        cmTok2.setText("cm2");
        RobotToken cmTok3 = new RobotToken();
        cmTok3.setText("cm3");

        testSetup.addCommentPart(cmTok1);
        testSetup.addCommentPart(cmTok2);
        testSetup.addCommentPart(cmTok3);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(filePath, modelFile);
    }

    public String getExtension() {
        return extension;
    }
}
