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

public class SuiteSetupTest {

    private static final RobotVersion ROBOT_VERSION = new RobotVersion(3, 0);

    @Test
    public void test_twoSuiteSetupDeclarationsOnlyKeywords_andAddThenArgumentInPosition2_shouldReturn_twoSetupsWithAdditionalArgument() {
        // prepare
        final RobotFileOutput rfo = new RobotFileOutput(ROBOT_VERSION);
        final RobotFile robotFile = new RobotFile(rfo);
        robotFile.includeSettingTableSection();
        final SettingTable settingTable = robotFile.getSettingTable();

        final SuiteSetup setupOne = new SuiteSetup(createToken(""));
        setupOne.setKeywordName(createToken("key1"));

        final SuiteSetup setupTwo = new SuiteSetup(createToken(""));
        setupTwo.setKeywordName(createToken("key2"));

        settingTable.addSuiteSetup(setupOne);
        settingTable.addSuiteSetup(setupTwo);

        // execute
        final List<SuiteSetup> suiteSetup = settingTable.getSuiteSetups();
        assertThat(suiteSetup).hasSize(2);
        final SuiteSetup setup1 = suiteSetup.get(0);
        final SuiteSetup setup2 = suiteSetup.get(1);
        setup2.setArgument(1, "arg");
        // verify
        assertThat(getText(setup1)).containsExactly("key1");
        assertThat(getText(setup2)).containsExactly("key2", "", "arg");
        assertThat(settingTable.getSuiteSetups()).hasSize(2);
        assertThat(settingTable.getSuiteSetups().get(0)).isSameAs(setup1);
    }

    @Test
    public void test_twoSuiteSetupDeclarations_shouldReturn_twoSetups() {
        // prepare
        final RobotFileOutput rfo = new RobotFileOutput(ROBOT_VERSION);
        final RobotFile robotFile = new RobotFile(rfo);
        robotFile.includeSettingTableSection();
        final SettingTable settingTable = robotFile.getSettingTable();

        final SuiteSetup setupOne = new SuiteSetup(createToken(""));
        setupOne.setKeywordName(createToken("key1"));
        setupOne.addArgument(createToken("arg1"));

        final SuiteSetup setupTwo = new SuiteSetup(createToken(""));
        setupTwo.setKeywordName(createToken("key2"));
        setupTwo.addArgument(createToken("arg2"));

        settingTable.addSuiteSetup(setupOne);
        settingTable.addSuiteSetup(setupTwo);

        // execute
        final List<SuiteSetup> suiteSetup = settingTable.getSuiteSetups();

        // verify
        assertThat(suiteSetup).hasSize(2);
        final SuiteSetup setup1 = suiteSetup.get(0);
        assertThat(getText(setup1)).containsExactly("key1", "arg1");
        final SuiteSetup setup2 = suiteSetup.get(1);
        assertThat(getText(setup2)).containsExactly("key2", "arg2");
        assertThat(settingTable.getSuiteSetups()).hasSize(2);
    }

    @Test
    public void test_twoSuiteSetupDeclarations_addArguments_shouldReturn_twoSetupsWithNewArguments() {
        // prepare
        final RobotFileOutput rfo = new RobotFileOutput(ROBOT_VERSION);
        final RobotFile robotFile = new RobotFile(rfo);
        robotFile.includeSettingTableSection();
        final SettingTable settingTable = robotFile.getSettingTable();

        final SuiteSetup setupOne = new SuiteSetup(createToken(""));
        setupOne.setKeywordName(createToken("key1"));
        setupOne.addArgument(createToken("arg1"));

        final SuiteSetup setupTwo = new SuiteSetup(createToken(""));
        setupTwo.setKeywordName(createToken("key2"));
        setupTwo.addArgument(createToken("arg2"));

        settingTable.addSuiteSetup(setupOne);
        settingTable.addSuiteSetup(setupTwo);

        // execute
        final List<SuiteSetup> suiteSetup = settingTable.getSuiteSetups();
        assertThat(suiteSetup).hasSize(2);
        final SuiteSetup setup1 = suiteSetup.get(0);
        setup1.addArgument(createToken("newArg1"));
        final SuiteSetup setup2 = suiteSetup.get(1);
        setup2.addArgument(createToken("newArg2"));

        // verify
        assertThat(getText(setup1)).containsExactly("key1", "arg1", "newArg1");
        assertThat(getText(setup2)).containsExactly("key2", "arg2", "newArg2");
        assertThat(settingTable.getSuiteSetups()).hasSize(2);
    }

    @Test
    public void test_twoSuiteSetupDeclarations_modificationOfOneArgument_shouldReturn_twoSetupsWithModifiedArgument() {
        // prepare
        final RobotFileOutput rfo = new RobotFileOutput(ROBOT_VERSION);
        final RobotFile robotFile = new RobotFile(rfo);
        robotFile.includeSettingTableSection();
        final SettingTable settingTable = robotFile.getSettingTable();

        final SuiteSetup setupOne = new SuiteSetup(createToken(""));
        setupOne.setKeywordName(createToken("key1"));
        setupOne.addArgument(createToken("arg1"));

        final SuiteSetup setupTwo = new SuiteSetup(createToken(""));
        setupTwo.setKeywordName(createToken("key2"));
        setupTwo.addArgument(createToken("arg2"));

        settingTable.addSuiteSetup(setupOne);
        settingTable.addSuiteSetup(setupTwo);

        // execute
        final List<SuiteSetup> suiteSetup = settingTable.getSuiteSetups();
        assertThat(suiteSetup).hasSize(2);
        final SuiteSetup setup1 = suiteSetup.get(0);
        final SuiteSetup setup2 = suiteSetup.get(1);
        setup2.getArguments().get(0).setText("mod");

        // verify
        assertThat(getText(setup1)).containsExactly("key1", "arg1");
        assertThat(getText(setup2)).containsExactly("key2", "mod");
        assertThat(settingTable.getSuiteSetups()).hasSize(2);
    }

}
