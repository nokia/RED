/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.setting;

import static org.assertj.core.api.Assertions.assertThat;
import static org.rf.ide.core.testdata.model.table.setting.ModelTokenTestHelper.createToken;
import static org.rf.ide.core.testdata.model.table.setting.ModelTokenTestHelper.getText;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.rf.ide.core.environment.RobotVersion;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.RobotFileOutput;
import org.rf.ide.core.testdata.model.table.SettingTable;

public class TestTeardownTest {

    private static final RobotVersion ROBOT_VERSION = new RobotVersion(3, 0);

    @Test
    public void test_twoTestTeardownDeclarations_shouldReturn_twoTeardowns() {
        // prepare
        final RobotFileOutput rfo = new RobotFileOutput(ROBOT_VERSION);
        final RobotFile robotFile = new RobotFile(rfo);
        robotFile.includeSettingTableSection();
        final SettingTable settingTable = robotFile.getSettingTable();

        final TestTeardown teardownOne = new TestTeardown(createToken(""));
        teardownOne.setKeywordName(createToken("key1"));
        teardownOne.addArgument(createToken("arg1"));

        final TestTeardown teardownTwo = new TestTeardown(createToken(""));
        teardownTwo.setKeywordName(createToken("key2"));
        teardownTwo.addArgument(createToken("arg2"));

        settingTable.addTestTeardown(teardownOne);
        settingTable.addTestTeardown(teardownTwo);

        // execute
        final List<TestTeardown> testTeardowns = settingTable.getTestTeardowns();

        // verify
        assertThat(testTeardowns).hasSize(2);
        final TestTeardown teardown1 = testTeardowns.get(0);
        final TestTeardown teardown2 = testTeardowns.get(1);
        assertThat(getText(teardown1)).containsExactly("key1", "arg1");
        assertThat(getText(teardown2)).containsExactly("key2", "arg2");
        assertThat(settingTable.getTestTeardowns()).hasSize(2);
    }

    @Test
    public void test_twoTestTeardownDeclarations_addArguments_shouldReturn_twoTeardownsWithNewArguments() {
        // prepare
        final RobotFileOutput rfo = new RobotFileOutput(ROBOT_VERSION);
        final RobotFile robotFile = new RobotFile(rfo);
        robotFile.includeSettingTableSection();
        final SettingTable settingTable = robotFile.getSettingTable();

        final TestTeardown teardownOne = new TestTeardown(createToken(""));
        teardownOne.setKeywordName(createToken("key1"));
        teardownOne.addArgument(createToken("arg1"));

        final TestTeardown teardownTwo = new TestTeardown(createToken(""));
        teardownTwo.setKeywordName(createToken("key2"));
        teardownTwo.addArgument(createToken("arg2"));

        settingTable.addTestTeardown(teardownOne);
        settingTable.addTestTeardown(teardownTwo);

        // execute
        final List<TestTeardown> testTeardowns = settingTable.getTestTeardowns();
        assertThat(testTeardowns).hasSize(2);
        final TestTeardown teardown1 = testTeardowns.get(0);
        final TestTeardown teardown2 = testTeardowns.get(1);
        teardown1.addArgument(createToken("newArg1"));
        teardown2.addArgument(createToken("newArg2"));

        // verify
        assertThat(getText(teardown1)).containsExactly("key1", "arg1", "newArg1");
        assertThat(getText(teardown2)).containsExactly("key2", "arg2", "newArg2");
        assertThat(settingTable.getTestTeardowns()).hasSize(2);
        assertThat(settingTable.getTestTeardowns().get(0)).isSameAs(teardown1);
        assertThat(settingTable.getTestTeardowns().get(1)).isSameAs(teardown2);
    }

    @Test
    public void test_twoTestTeardownDeclarations_modificationOfOneArgument_shouldReturn_twoTeardownsWithModifiedArgument() {
        // prepare
        final RobotFileOutput rfo = new RobotFileOutput(ROBOT_VERSION);
        final RobotFile robotFile = new RobotFile(rfo);
        robotFile.includeSettingTableSection();
        final SettingTable settingTable = robotFile.getSettingTable();

        final TestTeardown teardownOne = new TestTeardown(createToken(""));
        teardownOne.setKeywordName(createToken("key1"));
        teardownOne.addArgument(createToken("arg1"));

        final TestTeardown teardownTwo = new TestTeardown(createToken(""));
        teardownTwo.setKeywordName(createToken("key2"));
        teardownTwo.addArgument(createToken("arg2"));

        settingTable.addTestTeardown(teardownOne);
        settingTable.addTestTeardown(teardownTwo);

        // execute
        final List<TestTeardown> testTeardowns = settingTable.getTestTeardowns();
        assertThat(testTeardowns).hasSize(2);
        final TestTeardown teardown1 = testTeardowns.get(0);
        final TestTeardown teardown2 = testTeardowns.get(1);
        teardown2.getArguments().get(0).setText("mod");

        // verify
        assertThat(getText(teardown1)).containsExactly("key1", "arg1");
        assertThat(getText(teardown2)).containsExactly("key2", "mod");
        assertThat(settingTable.getTestTeardowns()).hasSize(2);
    }
}
