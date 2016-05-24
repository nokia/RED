/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.setting.views;

import static org.assertj.core.api.Assertions.assertThat;
import static org.rf.ide.core.testdata.model.table.setting.views.ModelTokenTestHelper.createToken;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.table.SettingTable;
import org.rf.ide.core.testdata.model.table.setting.TestTimeout;

import com.google.common.base.Optional;

public class TestTimeoutViewTest {

    @Test
    public void test_twoTestTimeoutsDeclarations_shouldReturn_commonView() {
        // prepare
        final RobotFile robotFile = new RobotFile(null);
        robotFile.includeSettingTableSection();
        final SettingTable settingTable = robotFile.getSettingTable();

        TestTimeout timeoutOne = new TestTimeout(createToken(""));
        timeoutOne.setTimeout("2 minutes");
        timeoutOne.addMessageArgument("arg1_tok1");

        TestTimeout timeoutTwo = new TestTimeout(createToken(""));
        timeoutTwo.setTimeout("3 minutes");
        timeoutTwo.addMessageArgument("arg1_tok2");

        settingTable.addTestTimeout(timeoutOne);
        settingTable.addTestTimeout(timeoutTwo);

        // execute
        Optional<TestTimeout> testTimeout = settingTable.testTimeout();

        // verify
        assertThat(testTimeout.isPresent()).isTrue();
        assertThat(getText(testTimeout.get())).containsExactly("2 minutes", "arg1_tok1", "3 minutes", "arg1_tok2");
        assertThat(settingTable.getTestTimeouts()).hasSize(2);
    }

    @Test
    public void test_twoTestTimeoutsDeclarations_addOneMessageArgument_shouldReturn_singleTimeout() {
        // prepare
        final RobotFile robotFile = new RobotFile(null);
        robotFile.includeSettingTableSection();
        final SettingTable settingTable = robotFile.getSettingTable();

        TestTimeout timeoutOne = new TestTimeout(createToken(""));
        timeoutOne.setTimeout("2 minutes");
        timeoutOne.addMessageArgument("arg1_tok1");

        TestTimeout timeoutTwo = new TestTimeout(createToken(""));
        timeoutTwo.setTimeout("3 minutes");
        timeoutTwo.addMessageArgument("arg1_tok2");

        settingTable.addTestTimeout(timeoutOne);
        settingTable.addTestTimeout(timeoutTwo);

        // execute
        Optional<TestTimeout> testTimeout = settingTable.testTimeout();
        assertThat(testTimeout.isPresent()).isTrue();
        TestTimeout tTimeout = testTimeout.get();
        tTimeout.addMessageArgument("new_arg1");

        // verify
        assertThat(getText(tTimeout)).containsExactly("2 minutes", "arg1_tok1", "3 minutes", "arg1_tok2", "new_arg1");
        assertThat(settingTable.getTestTimeouts()).hasSize(1);
    }

    @Test
    public void test_twoTestTimeoutsDeclarations_modificationOfOneArgument_shouldReturn_stillTwoTimeouts() {
        // prepare
        final RobotFile robotFile = new RobotFile(null);
        robotFile.includeSettingTableSection();
        final SettingTable settingTable = robotFile.getSettingTable();

        TestTimeout timeoutOne = new TestTimeout(createToken(""));
        timeoutOne.setTimeout("2 minutes");
        timeoutOne.addMessageArgument("arg1_tok1");

        TestTimeout timeoutTwo = new TestTimeout(createToken(""));
        timeoutTwo.setTimeout("3 minutes");
        timeoutTwo.addMessageArgument("arg1_tok2");

        settingTable.addTestTimeout(timeoutOne);
        settingTable.addTestTimeout(timeoutTwo);

        // execute
        Optional<TestTimeout> testTimeout = settingTable.testTimeout();
        assertThat(testTimeout.isPresent()).isTrue();
        TestTimeout tTimeout = testTimeout.get();
        tTimeout.getMessageArguments().get(2).setText("arg_mod");

        // verify
        assertThat(getText(tTimeout)).containsExactly("2 minutes", "arg1_tok1", "3 minutes", "arg_mod");
        assertThat(settingTable.getTestTimeouts()).hasSize(2);
    }

    private List<String> getText(final TestTimeout timeout) {
        List<String> text = new ArrayList<>();

        if (timeout.getTimeout() != null) {
            text.add(timeout.getTimeout().getText());
        }

        text.addAll(ModelTokenTestHelper.getText(timeout.getMessageArguments()));
        text.addAll(ModelTokenTestHelper.getText(timeout.getComment()));

        return text;
    }
}
