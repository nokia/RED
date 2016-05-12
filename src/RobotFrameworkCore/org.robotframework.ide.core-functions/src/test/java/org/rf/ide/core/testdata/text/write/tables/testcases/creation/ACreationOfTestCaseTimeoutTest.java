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
import org.rf.ide.core.testdata.model.table.testcases.TestCaseTimeout;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.write.NewRobotFileTestHelper;

public abstract class ACreationOfTestCaseTimeoutTest {

    public static final String PRETTY_NEW_DIR_LOCATION = "testCases//setting//timeout//new//";

    private final String extension;

    public ACreationOfTestCaseTimeoutTest(final String extension) {
        this.extension = extension;
    }

    @Test
    public void test_emptyFile_and_thanCreateTestCaseTimeout_withoutKeywordName_andTimeoutDecOnly() throws Exception {
        test_timeoutDecOnly("EmptyTestCaseTimeoutNoTestName", "");
    }

    @Test
    public void test_emptyFile_and_thanCreateTestCaseTimeout_withKeywordName_andTimeoutDecOnly() throws Exception {
        test_timeoutDecOnly("EmptyTestCaseTimeout", "TestCase");
    }

    private void test_timeoutDecOnly(final String fileNameWithoutExt, final String userKeywordName) throws Exception {
        // prepare
        final String filePath = PRETTY_NEW_DIR_LOCATION + fileNameWithoutExt + "." + getExtension();
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeTestCaseTableSection();
        TestCaseTable testCaseTable = modelFile.getTestCaseTable();

        RobotToken keyName = new RobotToken();
        keyName.setText(userKeywordName);
        TestCase test = new TestCase(keyName);
        testCaseTable.addTest(test);
        test.newTimeout();

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(filePath, modelFile);
    }

    @Test
    public void test_emptyFile_and_thanCreateTestCaseTimeout_withoutKeywordName_andTimeoutDec_withComment()
            throws Exception {
        test_timeoutDec_withCommentOnly("EmptyTestCaseTimeoutCommentNoTestName", "");
    }

    @Test
    public void test_emptyFile_and_thanCreateTestCaseTimeout_withKeywordName_andTimeoutDec_withComment()
            throws Exception {
        test_timeoutDec_withCommentOnly("EmptyTestCaseTimeoutComment", "TestCase");
    }

    private void test_timeoutDec_withCommentOnly(final String fileNameWithoutExt, final String userKeywordName)
            throws Exception {
        // prepare
        final String filePath = PRETTY_NEW_DIR_LOCATION + fileNameWithoutExt + "." + getExtension();
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeTestCaseTableSection();
        TestCaseTable testCaseTable = modelFile.getTestCaseTable();

        RobotToken keyName = new RobotToken();
        keyName.setText(userKeywordName);
        TestCase test = new TestCase(keyName);
        testCaseTable.addTest(test);
        TestCaseTimeout testTimeout = test.newTimeout();

        RobotToken cmTok1 = new RobotToken();
        cmTok1.setText("cm1");
        RobotToken cmTok2 = new RobotToken();
        cmTok2.setText("cm2");
        RobotToken cmTok3 = new RobotToken();
        cmTok3.setText("cm3");

        testTimeout.addCommentPart(cmTok1);
        testTimeout.addCommentPart(cmTok2);
        testTimeout.addCommentPart(cmTok3);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(filePath, modelFile);
    }

    @Test
    public void test_emptyFile_and_thanCreateTestCaseTimeout_withoutKeywordName_andTimeoutDec_withValue()
            throws Exception {
        test_timeoutDec_withComment_andValue("EmptyTestCaseTimeoutWithValueNoTestName", "");
    }

    @Test
    public void test_emptyFile_and_thanCreateTestCaseTimeout_withKeywordName_andTimeoutDec_withValue()
            throws Exception {
        test_timeoutDec_withComment_andValue("EmptyTestCaseTimeoutWithValue", "TestCase");
    }

    private void test_timeoutDec_withComment_andValue(final String fileNameWithoutExt, final String userKeywordName)
            throws Exception {
        // prepare
        final String filePath = PRETTY_NEW_DIR_LOCATION + fileNameWithoutExt + "." + getExtension();
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeTestCaseTableSection();
        TestCaseTable testCaseTable = modelFile.getTestCaseTable();

        RobotToken keyName = new RobotToken();
        keyName.setText(userKeywordName);
        TestCase test = new TestCase(keyName);
        testCaseTable.addTest(test);
        TestCaseTimeout testTimeout = test.newTimeout();

        RobotToken timeout = new RobotToken();
        timeout.setText("1 hours");
        testTimeout.setTimeout(timeout);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(filePath, modelFile);
    }

    @Test
    public void test_emptyFile_and_thanCreateTestCaseTimeout_withoutKeywordName_andTimeoutDec_withValue_andComment()
            throws Exception {
        test_timeoutDec_withComment_andValue_andComment("EmptyTestCaseTimeoutWithValueAndCommentNoTestName", "");
    }

    @Test
    public void test_emptyFile_and_thanCreateTestCaseTimeout_withKeywordName_andTimeoutDec_withValue_andComment()
            throws Exception {
        test_timeoutDec_withComment_andValue_andComment("EmptyTestCaseTimeoutWithValueAndComment", "TestCase");
    }

    private void test_timeoutDec_withComment_andValue_andComment(final String fileNameWithoutExt,
            final String userKeywordName) throws Exception {
        // prepare
        final String filePath = PRETTY_NEW_DIR_LOCATION + fileNameWithoutExt + "." + getExtension();
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeTestCaseTableSection();
        TestCaseTable testCaseTable = modelFile.getTestCaseTable();

        RobotToken keyName = new RobotToken();
        keyName.setText(userKeywordName);
        TestCase test = new TestCase(keyName);
        testCaseTable.addTest(test);
        TestCaseTimeout testTimeout = test.newTimeout();

        RobotToken timeout = new RobotToken();
        timeout.setText("1 hours");
        testTimeout.setTimeout(timeout);

        RobotToken cmTok1 = new RobotToken();
        cmTok1.setText("cm1");
        RobotToken cmTok2 = new RobotToken();
        cmTok2.setText("cm2");
        RobotToken cmTok3 = new RobotToken();
        cmTok3.setText("cm3");

        testTimeout.addCommentPart(cmTok1);
        testTimeout.addCommentPart(cmTok2);
        testTimeout.addCommentPart(cmTok3);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(filePath, modelFile);
    }

    @Test
    public void test_emptyFile_and_thanCreateTestCaseTimeout_withoutKeywordName_andTimeoutDec_withValue_and3MsgArgs()
            throws Exception {
        test_timeoutDec_withComment_andValue_and3MsgArgs("TestCaseTimeoutWithValueAnd3MsgArgsNoTestName", "");
    }

    @Test
    public void test_emptyFile_and_thanCreateTestCaseTimeout_withKeywordName_andTimeoutDec_withValue_and3MsgArgs()
            throws Exception {
        test_timeoutDec_withComment_andValue_and3MsgArgs("TestCaseTimeoutWithValueAnd3MsgArgs", "TestCase");
    }

    private void test_timeoutDec_withComment_andValue_and3MsgArgs(final String fileNameWithoutExt,
            final String userKeywordName) throws Exception {
        // prepare
        final String filePath = PRETTY_NEW_DIR_LOCATION + fileNameWithoutExt + "." + getExtension();
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeTestCaseTableSection();
        TestCaseTable testCaseTable = modelFile.getTestCaseTable();

        RobotToken keyName = new RobotToken();
        keyName.setText(userKeywordName);
        TestCase test = new TestCase(keyName);
        testCaseTable.addTest(test);
        TestCaseTimeout testTimeout = test.newTimeout();

        RobotToken timeout = new RobotToken();
        timeout.setText("1 hours");
        testTimeout.setTimeout(timeout);

        RobotToken msg1 = new RobotToken();
        msg1.setText("msg1");
        RobotToken msg2 = new RobotToken();
        msg2.setText("msg2");
        RobotToken msg3 = new RobotToken();
        msg3.setText("msg3");

        testTimeout.addMessagePart(msg1);
        testTimeout.addMessagePart(msg2);
        testTimeout.addMessagePart(msg3);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(filePath, modelFile);
    }

    @Test
    public void test_emptyFile_and_thanCreateTestCaseTimeout_withoutKeywordName_andTimeoutDec_withValue_and3MsgArgs_andComment()
            throws Exception {
        test_timeoutDec_withComment_andValue_and3MsgArgs_andComment(
                "TestCaseTimeoutWithValueAnd3MsgArgsAndCommentNoTestName", "");
    }

    @Test
    public void test_emptyFile_and_thanCreateTestCaseTimeout_withKeywordName_andTimeoutDec_withValue_and3MsgArgs_andComment()
            throws Exception {
        test_timeoutDec_withComment_andValue_and3MsgArgs_andComment("TestCaseTimeoutWithValueAnd3MsgArgsAndComment",
                "TestCase");
    }

    private void test_timeoutDec_withComment_andValue_and3MsgArgs_andComment(final String fileNameWithoutExt,
            final String userKeywordName) throws Exception {
        // prepare
        final String filePath = PRETTY_NEW_DIR_LOCATION + fileNameWithoutExt + "." + getExtension();
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeTestCaseTableSection();
        TestCaseTable testCaseTable = modelFile.getTestCaseTable();

        RobotToken keyName = new RobotToken();
        keyName.setText(userKeywordName);
        TestCase test = new TestCase(keyName);
        testCaseTable.addTest(test);
        TestCaseTimeout testTimeout = test.newTimeout();

        RobotToken timeout = new RobotToken();
        timeout.setText("1 hours");
        testTimeout.setTimeout(timeout);

        RobotToken msg1 = new RobotToken();
        msg1.setText("msg1");
        RobotToken msg2 = new RobotToken();
        msg2.setText("msg2");
        RobotToken msg3 = new RobotToken();
        msg3.setText("msg3");

        testTimeout.addMessagePart(msg1);
        testTimeout.addMessagePart(msg2);
        testTimeout.addMessagePart(msg3);

        RobotToken cmTok1 = new RobotToken();
        cmTok1.setText("cm1");
        RobotToken cmTok2 = new RobotToken();
        cmTok2.setText("cm2");
        RobotToken cmTok3 = new RobotToken();
        cmTok3.setText("cm3");

        testTimeout.addCommentPart(cmTok1);
        testTimeout.addCommentPart(cmTok2);
        testTimeout.addCommentPart(cmTok3);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(filePath, modelFile);
    }

    public String getExtension() {
        return extension;
    }
}
