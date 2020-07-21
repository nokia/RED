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

public class SuiteTeardownTest {

    private static final RobotVersion ROBOT_VERSION = new RobotVersion(3, 0);

    @Test
    public void test_twoSuiteTeardownDeclarations_shouldReturn_twoTeardowns() {
        // prepare
        final RobotFileOutput rfo = new RobotFileOutput(ROBOT_VERSION);
        final RobotFile robotFile = new RobotFile(rfo);
        robotFile.includeSettingTableSection();
        final SettingTable settingTable = robotFile.getSettingTable();

        final SuiteTeardown teardownOne = new SuiteTeardown(createToken(""));
        teardownOne.setKeywordName(createToken("key1"));
        teardownOne.addArgument(createToken("arg1"));

        final SuiteTeardown teardownTwo = new SuiteTeardown(createToken(""));
        teardownTwo.setKeywordName(createToken("key2"));
        teardownTwo.addArgument(createToken("arg2"));

        settingTable.addSuiteTeardown(teardownOne);
        settingTable.addSuiteTeardown(teardownTwo);

        // execute
        final List<SuiteTeardown> suiteTeardowns = settingTable.getSuiteTeardowns();

        // verify
        assertThat(suiteTeardowns).hasSize(2);
        final SuiteTeardown teardown1 = suiteTeardowns.get(0);
        final SuiteTeardown teardown2 = suiteTeardowns.get(1);
        assertThat(getText(teardown1)).containsExactly("key1", "arg1");
        assertThat(getText(teardown2)).containsExactly("key2", "arg2");
        assertThat(settingTable.getSuiteTeardowns()).hasSize(2);
    }

    @Test
    public void test_twoSuiteTeardownDeclarations_addArguments_shouldReturn_twoTeardownWithNewArguments() {
        // prepare
        final RobotFileOutput rfo = new RobotFileOutput(ROBOT_VERSION);
        final RobotFile robotFile = new RobotFile(rfo);
        robotFile.includeSettingTableSection();
        final SettingTable settingTable = robotFile.getSettingTable();

        final SuiteTeardown teardownOne = new SuiteTeardown(createToken(""));
        teardownOne.setKeywordName(createToken("key1"));
        teardownOne.addArgument(createToken("arg1"));

        final SuiteTeardown teardownTwo = new SuiteTeardown(createToken(""));
        teardownTwo.setKeywordName(createToken("key2"));
        teardownTwo.addArgument(createToken("arg2"));

        settingTable.addSuiteTeardown(teardownOne);
        settingTable.addSuiteTeardown(teardownTwo);

        // execute
        final List<SuiteTeardown> suiteTeardown = settingTable.getSuiteTeardowns();
        assertThat(suiteTeardown).hasSize(2);
        final SuiteTeardown teardown1 = suiteTeardown.get(0);
        final SuiteTeardown teardown2 = suiteTeardown.get(1);
        teardown1.addArgument(createToken("newArg1"));
        teardown2.addArgument(createToken("newArg2"));

        // verify
        assertThat(getText(teardown1)).containsExactly("key1", "arg1", "newArg1");
        assertThat(getText(teardown2)).containsExactly("key2", "arg2", "newArg2");
        assertThat(settingTable.getSuiteTeardowns()).hasSize(2);
        assertThat(settingTable.getSuiteTeardowns().get(0)).isSameAs(teardown1);
        assertThat(settingTable.getSuiteTeardowns().get(1)).isSameAs(teardown2);
    }

    @Test
    public void test_twoSuiteTeardownDeclarations_modificationOfOneArgument_shouldReturn_twoTeardownsWithModifiedArgument() {
        // prepare
        final RobotFileOutput rfo = new RobotFileOutput(ROBOT_VERSION);
        final RobotFile robotFile = new RobotFile(rfo);
        robotFile.includeSettingTableSection();
        final SettingTable settingTable = robotFile.getSettingTable();

        final SuiteTeardown teardownOne = new SuiteTeardown(createToken(""));
        teardownOne.setKeywordName(createToken("key1"));
        teardownOne.addArgument(createToken("arg1"));

        final SuiteTeardown teardownTwo = new SuiteTeardown(createToken(""));
        teardownTwo.setKeywordName(createToken("key2"));
        teardownTwo.addArgument(createToken("arg2"));

        settingTable.addSuiteTeardown(teardownOne);
        settingTable.addSuiteTeardown(teardownTwo);

        // execute
        final List<SuiteTeardown> suiteTeardowns = settingTable.getSuiteTeardowns();
        assertThat(suiteTeardowns).hasSize(2);
        final SuiteTeardown teardown1 = suiteTeardowns.get(0);
        final SuiteTeardown teardown2 = suiteTeardowns.get(1);
        teardown2.getArguments().get(0).setText("mod");

        // verify
        assertThat(getText(teardown1)).containsExactly("key1", "arg1");
        assertThat(getText(teardown2)).containsExactly("key2", "mod");
        assertThat(settingTable.getSuiteTeardowns()).hasSize(2);
    }
}
