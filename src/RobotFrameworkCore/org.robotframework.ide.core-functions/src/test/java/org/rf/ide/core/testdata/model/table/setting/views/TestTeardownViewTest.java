/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.setting.views;

import static org.assertj.core.api.Assertions.assertThat;
import static org.rf.ide.core.testdata.model.table.setting.views.ModelTokenTestHelper.createToken;
import static org.rf.ide.core.testdata.model.table.setting.views.ModelTokenTestHelper.getText;

import java.util.List;

import org.junit.Test;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.RobotFileOutput;
import org.rf.ide.core.testdata.model.RobotVersion;
import org.rf.ide.core.testdata.model.table.SettingTable;
import org.rf.ide.core.testdata.model.table.setting.TestTeardown;

public class TestTeardownViewTest {

    @Test
    public void test_twoTestTeardownDeclarations_shouldReturn_commonView() {
        // prepare
        final RobotFileOutput rfo = new RobotFileOutput(new RobotVersion(2, 9));
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
        final List<TestTeardown> testTeardowns = settingTable.getTestTeardownsViews();

        // verify
        assertThat(testTeardowns).hasSize(1);
        final TestTeardown common = testTeardowns.get(0);
        assertThat(getText(common)).containsExactly("key1", "arg1", "key2", "arg2");
        assertThat(settingTable.getTestTeardowns()).hasSize(2);
    }

    @Test
    public void test_twoTestTeardownDeclarations_addOneArgument_shouldReturn_singleElement() {
        // prepare
        final RobotFileOutput rfo = new RobotFileOutput(new RobotVersion(2, 9));
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
        final List<TestTeardown> testTeardowns = settingTable.getTestTeardownsViews();
        assertThat(testTeardowns).hasSize(1);
        final TestTeardown common = testTeardowns.get(0);
        common.addArgument(createToken("newArg"));

        // verify
        assertThat(getText(common)).containsExactly("key1", "arg1", "key2", "arg2", "newArg");
        assertThat(settingTable.getTestTeardowns()).hasSize(1);
        assertThat(settingTable.getTestTeardowns().get(0)).isSameAs(common);
    }

    @Test
    public void test_twoTestTeardownDeclarations_modificationOfOneArgument_shouldReturn_twoElementsStill() {
        // prepare
        final RobotFileOutput rfo = new RobotFileOutput(new RobotVersion(2, 9));
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
        final List<TestTeardown> testTeardowns = settingTable.getTestTeardownsViews();
        assertThat(testTeardowns).hasSize(1);
        final TestTeardown common = testTeardowns.get(0);
        common.getArguments().get(2).setText("mod");

        // verify
        assertThat(getText(common)).containsExactly("key1", "arg1", "key2", "mod");
        assertThat(settingTable.getTestTeardowns()).hasSize(2);
    }
}
