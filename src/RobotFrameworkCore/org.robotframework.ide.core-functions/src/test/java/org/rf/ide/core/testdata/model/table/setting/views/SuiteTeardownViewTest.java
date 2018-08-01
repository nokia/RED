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
import org.rf.ide.core.testdata.model.table.setting.SuiteTeardown;

public class SuiteTeardownViewTest {

    @Test
    public void test_twoSuiteTeardownDeclarations_shouldReturn_commonView() {
        // prepare
        final RobotFileOutput rfo = new RobotFileOutput(new RobotVersion(2, 9));
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
        final List<SuiteTeardown> suiteTeardowns = settingTable.getSuiteTeardownsViews();

        // verify
        assertThat(suiteTeardowns).hasSize(1);
        final SuiteTeardown common = suiteTeardowns.get(0);
        assertThat(getText(common)).containsExactly("key1", "arg1", "key2", "arg2");
        assertThat(settingTable.getSuiteTeardowns()).hasSize(2);
    }

    @Test
    public void test_twoSuiteTeardownDeclarations_addOneArgument_shouldReturn_singleElement() {
        // prepare
        final RobotFileOutput rfo = new RobotFileOutput(new RobotVersion(2, 9));
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
        final List<SuiteTeardown> suiteTeardown = settingTable.getSuiteTeardownsViews();
        assertThat(suiteTeardown).hasSize(1);
        final SuiteTeardown common = suiteTeardown.get(0);
        common.addArgument(createToken("newArg"));

        // verify
        assertThat(getText(common)).containsExactly("key1", "arg1", "key2", "arg2", "newArg");
        assertThat(settingTable.getSuiteTeardowns()).hasSize(1);
        assertThat(settingTable.getSuiteTeardowns().get(0)).isSameAs(common);
    }

    @Test
    public void test_twoSuiteTeardownDeclarations_modificationOfOneArgument_shouldReturn_twoElementsStill() {
        // prepare
        final RobotFileOutput rfo = new RobotFileOutput(new RobotVersion(2, 9));
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
        final List<SuiteTeardown> suiteTeardowns = settingTable.getSuiteTeardownsViews();
        assertThat(suiteTeardowns).hasSize(1);
        final SuiteTeardown common = suiteTeardowns.get(0);
        common.getArguments().get(2).setText("mod");

        // verify
        assertThat(getText(common)).containsExactly("key1", "arg1", "key2", "mod");
        assertThat(settingTable.getSuiteTeardowns()).hasSize(2);
    }
}
