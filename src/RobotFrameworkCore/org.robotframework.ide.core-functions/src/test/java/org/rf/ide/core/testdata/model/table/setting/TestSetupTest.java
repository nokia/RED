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

public class TestSetupTest {

    private static final RobotVersion ROBOT_VERSION = new RobotVersion(3, 0);

    @Test
    public void test_twoTestSetupDeclarations_shouldReturn_twoSetups() {
        // prepare
        final RobotFileOutput rfo = new RobotFileOutput(ROBOT_VERSION);
        final RobotFile robotFile = new RobotFile(rfo);
        robotFile.includeSettingTableSection();
        final SettingTable settingTable = robotFile.getSettingTable();

        final TestSetup setupOne = new TestSetup(createToken(""));
        setupOne.setKeywordName(createToken("key1"));
        setupOne.addArgument(createToken("arg1"));

        final TestSetup setupTwo = new TestSetup(createToken(""));
        setupTwo.setKeywordName(createToken("key2"));
        setupTwo.addArgument(createToken("arg2"));

        settingTable.addTestSetup(setupOne);
        settingTable.addTestSetup(setupTwo);

        // execute
        final List<TestSetup> suiteSetup = settingTable.getTestSetups();

        // verify
        assertThat(suiteSetup).hasSize(2);
        final TestSetup setup1 = suiteSetup.get(0);
        assertThat(getText(setup1)).containsExactly("key1", "arg1");
        final TestSetup setup2 = suiteSetup.get(1);
        assertThat(getText(setup2)).containsExactly("key2", "arg2");
        assertThat(settingTable.getTestSetups()).hasSize(2);
    }

    @Test
    public void test_twoTestSetupDeclarations_addArguments_shouldReturn_twoSetupsWithNewArguments() {
        // prepare
        final RobotFileOutput rfo = new RobotFileOutput(ROBOT_VERSION);
        final RobotFile robotFile = new RobotFile(rfo);
        robotFile.includeSettingTableSection();
        final SettingTable settingTable = robotFile.getSettingTable();

        final TestSetup setupOne = new TestSetup(createToken(""));
        setupOne.setKeywordName(createToken("key1"));
        setupOne.addArgument(createToken("arg1"));

        final TestSetup setupTwo = new TestSetup(createToken(""));
        setupTwo.setKeywordName(createToken("key2"));
        setupTwo.addArgument(createToken("arg2"));

        settingTable.addTestSetup(setupOne);
        settingTable.addTestSetup(setupTwo);

        // execute
        final List<TestSetup> suiteSetup = settingTable.getTestSetups();
        assertThat(suiteSetup).hasSize(2);
        final TestSetup setup1 = suiteSetup.get(0);
        setup1.addArgument(createToken("newArg1"));
        final TestSetup setup2 = suiteSetup.get(1);
        setup2.addArgument(createToken("newArg2"));

        // verify
        assertThat(getText(setup1)).containsExactly("key1", "arg1", "newArg1");
        assertThat(getText(setup2)).containsExactly("key2", "arg2", "newArg2");
        assertThat(settingTable.getTestSetups()).hasSize(2);
        assertThat(settingTable.getTestSetups().get(0)).isSameAs(setup1);
        assertThat(settingTable.getTestSetups().get(1)).isSameAs(setup2);
    }

    @Test
    public void test_twoTestSetupDeclarations_modificationOfOneArgument_shouldReturn_twoSetupsWithModifiedArgument() {
        // prepare
        final RobotFileOutput rfo = new RobotFileOutput(ROBOT_VERSION);
        final RobotFile robotFile = new RobotFile(rfo);
        robotFile.includeSettingTableSection();
        final SettingTable settingTable = robotFile.getSettingTable();

        final TestSetup setupOne = new TestSetup(createToken(""));
        setupOne.setKeywordName(createToken("key1"));
        setupOne.addArgument(createToken("arg1"));

        final TestSetup setupTwo = new TestSetup(createToken(""));
        setupTwo.setKeywordName(createToken("key2"));
        setupTwo.addArgument(createToken("arg2"));

        settingTable.addTestSetup(setupOne);
        settingTable.addTestSetup(setupTwo);

        // execute
        final List<TestSetup> suiteSetup = settingTable.getTestSetups();
        assertThat(suiteSetup).hasSize(2);
        final TestSetup setup1 = suiteSetup.get(0);
        final TestSetup setup2 = suiteSetup.get(1);
        setup2.getArguments().get(0).setText("mod");

        // verify
        assertThat(getText(setup1)).containsExactly("key1", "arg1");
        assertThat(getText(setup2)).containsExactly("key2", "mod");
        assertThat(settingTable.getTestSetups()).hasSize(2);
    }
}
