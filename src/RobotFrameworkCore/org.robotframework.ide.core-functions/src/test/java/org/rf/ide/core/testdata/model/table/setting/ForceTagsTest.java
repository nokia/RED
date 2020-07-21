/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.setting;

import static org.assertj.core.api.Assertions.assertThat;
import static org.rf.ide.core.testdata.model.table.setting.ModelTokenTestHelper.getText;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.rf.ide.core.environment.RobotVersion;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.RobotFileOutput;
import org.rf.ide.core.testdata.model.table.SettingTable;
import org.rf.ide.core.testdata.model.table.setting.ForceTags;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;

public class ForceTagsTest {

    private static final RobotVersion ROBOT_VERSION = new RobotVersion(3, 0);

    @Test
    public void test_tags_retrival_twoForceTags() {
        // prepare
        final RobotFileOutput rfo = new RobotFileOutput(ROBOT_VERSION);
        final RobotFile robotFile = new RobotFile(rfo);
        robotFile.includeSettingTableSection();
        final SettingTable settingTable = robotFile.getSettingTable();

        final RobotToken tagDecOne = new RobotToken();
        final ForceTags defaultOne = new ForceTags(tagDecOne);
        defaultOne.addTag("tag1");
        defaultOne.addTag("tag2");

        final RobotToken tagDecTwo = new RobotToken();
        final ForceTags defaultTwo = new ForceTags(tagDecTwo);
        defaultTwo.addTag("tag1a");
        defaultTwo.addTag("tag2a");

        settingTable.addForceTags(defaultOne);
        settingTable.addForceTags(defaultTwo);

        // execute
        final List<ForceTags> defaultTags = settingTable.getForceTags();

        // verify
        assertThat(defaultTags).hasSize(2);
        final ForceTags tag1 = defaultTags.get(0);
        final ForceTags tag2 = defaultTags.get(1);
        assertThat(tag1.getTags()).hasSize(2);
        assertThat(tag2.getTags()).hasSize(2);
        assertThat(getText(tag1.getTags())).containsExactly("tag1", "tag2");
        assertThat(getText(tag2.getTags())).containsExactly("tag1a", "tag2a");
        assertThat(settingTable.getForceTags()).hasSize(2);
    }

    @Test
    public void test_tags_retrival_afterTagAddition_twoForceTags() {
        // prepare
        final RobotFileOutput rfo = new RobotFileOutput(ROBOT_VERSION);
        final RobotFile robotFile = new RobotFile(rfo);
        robotFile.includeSettingTableSection();
        final SettingTable settingTable = robotFile.getSettingTable();

        final RobotToken tagDecOne = new RobotToken();
        final ForceTags defaultOne = new ForceTags(tagDecOne);
        defaultOne.addTag("tag1");
        defaultOne.addTag("tag2");

        final RobotToken tagDecTwo = new RobotToken();
        final ForceTags defaultTwo = new ForceTags(tagDecTwo);
        defaultTwo.addTag("tag1a");
        defaultTwo.addTag("tag2a");

        settingTable.addForceTags(defaultOne);
        settingTable.addForceTags(defaultTwo);

        // execute
        final List<ForceTags> defaultTags = settingTable.getForceTags();
        defaultTags.get(1).addTag("tag3");

        // verify
        assertThat(defaultTags).hasSize(2);
        final ForceTags tag1 = defaultTags.get(0);
        final ForceTags tag2 = defaultTags.get(1);
        assertThat(tag1.getTags()).hasSize(2);
        assertThat(tag2.getTags()).hasSize(3);
        assertThat(getText(tag1.getTags())).containsExactly("tag1", "tag2");
        assertThat(getText(tag2.getTags())).containsExactly("tag1a", "tag2a", "tag3");
        assertThat(settingTable.getForceTags()).hasSize(2);
        assertThat(settingTable.getForceTags().get(0)).isSameAs(tag1);
        assertThat(settingTable.getForceTags().get(1)).isSameAs(tag2);
    }

    @Test
    public void test_tags_retrival_afterModification_twoModifiedForceTags() {
        // prepare
        final RobotFileOutput rfo = new RobotFileOutput(ROBOT_VERSION);
        final RobotFile robotFile = new RobotFile(rfo);
        robotFile.includeSettingTableSection();
        final SettingTable settingTable = robotFile.getSettingTable();

        final RobotToken tagDecOne = new RobotToken();
        final ForceTags defaultOne = new ForceTags(tagDecOne);
        defaultOne.addTag("tag1");
        defaultOne.addTag("tag2");

        final RobotToken tagDecTwo = new RobotToken();
        final ForceTags defaultTwo = new ForceTags(tagDecTwo);
        defaultTwo.addTag("tag1a");
        defaultTwo.addTag("tag2a");

        settingTable.addForceTags(defaultOne);
        settingTable.addForceTags(defaultTwo);

        // execute
        final List<ForceTags> defaultTags = settingTable.getForceTags();
        defaultTwo.getTags().get(0).setText("mod");

        // verify
        assertThat(defaultTags).hasSize(2);
        final ForceTags tag1 = defaultTags.get(0);
        final ForceTags tag2 = defaultTags.get(1);
        assertThat(tag1.getTags()).hasSize(2);
        assertThat(tag2.getTags()).hasSize(2);
        assertThat(getText(tag1.getTags())).containsExactly("tag1", "tag2");
        assertThat(getText(tag2.getTags())).containsExactly("mod", "tag2a");
        assertThat(settingTable.getForceTags()).hasSize(2);
    }
}
