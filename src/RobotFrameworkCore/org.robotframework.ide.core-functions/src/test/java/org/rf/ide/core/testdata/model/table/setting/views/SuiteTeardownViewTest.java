/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.setting.views;

import static org.assertj.core.api.Assertions.assertThat;
import static org.rf.ide.core.testdata.model.table.setting.views.ModelTokenTestHelper.createToken;
import static org.rf.ide.core.testdata.model.table.setting.views.ModelTokenTestHelper.getText;

import org.junit.Test;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.table.SettingTable;
import org.rf.ide.core.testdata.model.table.setting.SuiteTeardown;

import com.google.common.base.Optional;

public class SuiteTeardownViewTest {

    @Test
    public void test_twoSuiteTeardownDeclarations_shouldReturn_commonView() {
        // prepare
        final RobotFile robotFile = new RobotFile(null);
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
        Optional<SuiteTeardown> suiteSetup = settingTable.suiteTeardown();

        // verify
        assertThat(suiteSetup.isPresent()).isTrue();
        SuiteTeardown common = suiteSetup.get();
        assertThat(getText(common)).containsExactly("key1", "arg1", "key2", "arg2");
        assertThat(settingTable.getSuiteTeardowns()).hasSize(2);
    }

    @Test
    public void test_twoSuiteTeardownDeclarations_addOneArgument_shouldReturn_singleElement() {
        // prepare
        final RobotFile robotFile = new RobotFile(null);
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
        Optional<SuiteTeardown> suiteSetup = settingTable.suiteTeardown();
        assertThat(suiteSetup.isPresent()).isTrue();
        SuiteTeardown common = suiteSetup.get();
        common.addArgument(createToken("newArg"));

        // verify
        assertThat(getText(common)).containsExactly("key1", "arg1", "key2", "arg2", "newArg");
        assertThat(settingTable.getSuiteTeardowns()).hasSize(1);
        assertThat(settingTable.getSuiteTeardowns().get(0)).isSameAs(common);
    }

    @Test
    public void test_twoSuiteTeardownDeclarations_modificationOfOneArgument_shouldReturn_twoElementsStill() {
        // prepare
        final RobotFile robotFile = new RobotFile(null);
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
        Optional<SuiteTeardown> suiteSetup = settingTable.suiteTeardown();
        assertThat(suiteSetup.isPresent()).isTrue();
        SuiteTeardown common = suiteSetup.get();
        common.getArguments().get(2).setText("mod");

        // verify
        assertThat(getText(common)).containsExactly("key1", "arg1", "key2", "mod");
        assertThat(settingTable.getSuiteTeardowns()).hasSize(2);
    }
}
