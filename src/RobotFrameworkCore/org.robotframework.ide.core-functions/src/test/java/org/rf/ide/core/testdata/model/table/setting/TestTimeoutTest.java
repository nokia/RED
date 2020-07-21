/*
* Copyright 2017 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.rf.ide.core.testdata.model.table.setting;

import static org.assertj.core.api.Assertions.assertThat;
import static org.rf.ide.core.testdata.model.table.setting.ModelTokenTestHelper.createToken;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.rf.ide.core.environment.RobotVersion;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.RobotFileOutput;
import org.rf.ide.core.testdata.model.table.SettingTable;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public class TestTimeoutTest {

    private static final RobotVersion ROBOT_VERSION = new RobotVersion(3, 0);
    private RobotToken decToken;
    private TestTimeout testable;

    @BeforeEach
    public void setUp() {
        this.decToken = new RobotToken();
        this.testable = new TestTimeout(decToken);
    }

    @Test
    public void test_ifCellWillBeAdded_atArgumentPosition() {
        // prepare
        final RobotToken argToken = RobotToken.create("arg");
        final RobotToken cmtToken = RobotToken.create("#cmt");
        testable.addMessageArgument(argToken);
        testable.addCommentPart(cmtToken);

        // execute
        testable.insertValueAt("value", 2);

        // verify
        final List<RobotToken> elementTokens = testable.getElementTokens();
        assertThat(elementTokens).hasSize(4);
        assertThat(elementTokens.get(0)).isSameAs(decToken);
        assertThat(elementTokens.get(0).getTypes()).contains(RobotTokenType.SETTING_TEST_TIMEOUT_DECLARATION);
        assertThat(elementTokens.get(1).getText()).isEqualTo("value");
        assertThat(elementTokens.get(1).getTypes()).contains(RobotTokenType.SETTING_TEST_TIMEOUT_VALUE);
        assertThat(elementTokens.get(2)).isSameAs(argToken);
        assertThat(elementTokens.get(3)).isSameAs(cmtToken);
        assertThat(elementTokens.get(3).getTypes()).contains(RobotTokenType.COMMENT);
    }

    @Test
    public void test_ifCellWillBeAdded_atCommentPosition() {
        // prepare
        final RobotToken argToken = RobotToken.create("arg");
        final RobotToken cmtToken = RobotToken.create("#cmt");
        testable.addMessageArgument(argToken);
        testable.addCommentPart(cmtToken);

        // execute
        testable.insertValueAt("value", 3);

        // verify
        final List<RobotToken> elementTokens = testable.getElementTokens();
        assertThat(elementTokens).hasSize(4);
        assertThat(elementTokens.get(0)).isSameAs(decToken);
        assertThat(elementTokens.get(0).getTypes()).contains(RobotTokenType.SETTING_TEST_TIMEOUT_DECLARATION);
        assertThat(elementTokens.get(1)).isSameAs(argToken);
        assertThat(elementTokens.get(1).getTypes()).contains(RobotTokenType.UNKNOWN);
        assertThat(elementTokens.get(2).getText()).isEqualTo("value");
        assertThat(elementTokens.get(2).getTypes()).contains(RobotTokenType.SETTING_TEST_TIMEOUT_VALUE);
        assertThat(elementTokens.get(3)).isSameAs(cmtToken);
        assertThat(elementTokens.get(3).getTypes()).contains(RobotTokenType.COMMENT);
    }

    @Test
    public void test_twoTestTimeoutsDeclarations_shouldReturn_twoTimeouts() {
        // prepare
        final RobotFileOutput rfo = new RobotFileOutput(ROBOT_VERSION);
        final RobotFile robotFile = new RobotFile(rfo);
        robotFile.includeSettingTableSection();
        final SettingTable settingTable = robotFile.getSettingTable();

        final TestTimeout timeoutOne = new TestTimeout(createToken(""));
        timeoutOne.setTimeout("2 minutes");
        timeoutOne.addMessageArgument("arg1_tok1");

        final TestTimeout timeoutTwo = new TestTimeout(createToken(""));
        timeoutTwo.setTimeout("3 minutes");
        timeoutTwo.addMessageArgument("arg1_tok2");

        settingTable.addTestTimeout(timeoutOne);
        settingTable.addTestTimeout(timeoutTwo);

        // execute
        final List<TestTimeout> testTimeout = settingTable.getTestTimeouts();

        // verify
        assertThat(testTimeout).hasSize(2);
        assertThat(getText(testTimeout.get(0))).containsExactly("2 minutes", "arg1_tok1");
        assertThat(getText(testTimeout.get(1))).containsExactly("3 minutes", "arg1_tok2");
        assertThat(settingTable.getTestTimeouts()).hasSize(2);
    }

    @Test
    public void test_twoTestTimeoutsDeclarations_addArguments_shouldReturn_twoTimeoutsWithNewArguments() {
        // prepare
        final RobotFileOutput rfo = new RobotFileOutput(ROBOT_VERSION);
        final RobotFile robotFile = new RobotFile(rfo);
        robotFile.includeSettingTableSection();
        final SettingTable settingTable = robotFile.getSettingTable();

        final TestTimeout timeoutOne = new TestTimeout(createToken(""));
        timeoutOne.setTimeout("2 minutes");
        timeoutOne.addMessageArgument("arg1_tok1");

        final TestTimeout timeoutTwo = new TestTimeout(createToken(""));
        timeoutTwo.setTimeout("3 minutes");
        timeoutTwo.addMessageArgument("arg1_tok2");

        settingTable.addTestTimeout(timeoutOne);
        settingTable.addTestTimeout(timeoutTwo);

        // execute
        final List<TestTimeout> testTimeout = settingTable.getTestTimeouts();
        assertThat(testTimeout).hasSize(2);
        final TestTimeout tTimeout1 = testTimeout.get(0);
        final TestTimeout tTimeout2 = testTimeout.get(1);
        tTimeout1.addMessageArgument("new_arg1");
        tTimeout2.addMessageArgument("new_arg2");

        // verify
        assertThat(getText(tTimeout1)).containsExactly("2 minutes", "arg1_tok1", "new_arg1");
        assertThat(getText(tTimeout2)).containsExactly("3 minutes", "arg1_tok2", "new_arg2");
        assertThat(settingTable.getTestTimeouts()).hasSize(2);
        assertThat(settingTable.getTestTimeouts().get(0)).isSameAs(tTimeout1);
        assertThat(settingTable.getTestTimeouts().get(1)).isSameAs(tTimeout2);
    }

    @Test
    public void test_twoTestTimeoutsDeclarations_modificationOfOneArgument_shouldReturn_twoTimeoutsWithModifiedArgument() {
        // prepare
        final RobotFileOutput rfo = new RobotFileOutput(ROBOT_VERSION);
        final RobotFile robotFile = new RobotFile(rfo);
        robotFile.includeSettingTableSection();
        final SettingTable settingTable = robotFile.getSettingTable();

        final TestTimeout timeoutOne = new TestTimeout(createToken(""));
        timeoutOne.setTimeout("2 minutes");
        timeoutOne.addMessageArgument("arg1_tok1");

        final TestTimeout timeoutTwo = new TestTimeout(createToken(""));
        timeoutTwo.setTimeout("3 minutes");
        timeoutTwo.addMessageArgument("arg1_tok2");

        settingTable.addTestTimeout(timeoutOne);
        settingTable.addTestTimeout(timeoutTwo);

        // execute
        final List<TestTimeout> testTimeout = settingTable.getTestTimeouts();
        assertThat(testTimeout).hasSize(2);
        final TestTimeout tTimeout1 = testTimeout.get(0);
        final TestTimeout tTimeout2 = testTimeout.get(1);
        tTimeout2.getMessageArguments().get(0).setText("arg_mod");

        // verify
        assertThat(getText(tTimeout1)).containsExactly("2 minutes", "arg1_tok1");
        assertThat(getText(tTimeout2)).containsExactly("3 minutes", "arg_mod");
        assertThat(settingTable.getTestTimeouts()).hasSize(2);
    }

    private List<String> getText(final TestTimeout timeout) {
        final List<String> text = new ArrayList<>();

        if (timeout.getTimeout() != null) {
            text.add(timeout.getTimeout().getText());
        }
        text.addAll(ModelTokenTestHelper.getText(timeout.getMessageArguments()));
        text.addAll(ModelTokenTestHelper.getText(timeout.getComment()));
        return text;
    }
}
