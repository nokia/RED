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
import org.rf.ide.core.testdata.model.table.setting.TestSetup;

public class TestSetupViewTest {

    @Test
    public void test_twoTestSetupDeclarations_shouldReturn_commonView() {
        // prepare
        final RobotFileOutput rfo = new RobotFileOutput(new RobotVersion(2, 9));
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
        final List<TestSetup> suiteSetup = settingTable.getTestSetupsViews();

        // verify
        assertThat(suiteSetup).hasSize(1);
        final TestSetup common = suiteSetup.get(0);
        assertThat(getText(common)).containsExactly("key1", "arg1", "key2", "arg2");
        assertThat(settingTable.getTestSetups()).hasSize(2);
    }

    @Test
    public void test_twoTestSetupDeclarations_addOneArgument_shouldReturn_singleElement() {
        // prepare
        final RobotFileOutput rfo = new RobotFileOutput(new RobotVersion(2, 9));
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
        final List<TestSetup> suiteSetup = settingTable.getTestSetupsViews();
        assertThat(suiteSetup).hasSize(1);
        final TestSetup common = suiteSetup.get(0);
        common.addArgument(createToken("newArg"));

        // verify
        assertThat(getText(common)).containsExactly("key1", "arg1", "key2", "arg2", "newArg");
        assertThat(settingTable.getTestSetups()).hasSize(1);
        assertThat(settingTable.getTestSetups().get(0)).isSameAs(common);
    }

    @Test
    public void test_twoTestSetupDeclarations_modificationOfOneArgument_shouldReturn_twoElementsStill() {
        // prepare
        final RobotFileOutput rfo = new RobotFileOutput(new RobotVersion(2, 9));
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
        final List<TestSetup> suiteSetup = settingTable.getTestSetupsViews();
        assertThat(suiteSetup).hasSize(1);
        final TestSetup common = suiteSetup.get(0);
        common.getArguments().get(2).setText("mod");

        // verify
        assertThat(getText(common)).containsExactly("key1", "arg1", "key2", "mod");
        assertThat(settingTable.getTestSetups()).hasSize(2);
    }
}
