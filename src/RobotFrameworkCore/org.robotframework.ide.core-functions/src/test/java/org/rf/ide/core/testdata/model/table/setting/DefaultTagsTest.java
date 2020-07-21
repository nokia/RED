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
import org.rf.ide.core.testdata.model.table.setting.DefaultTags;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;

public class DefaultTagsTest {

    private static final RobotVersion ROBOT_VERSION = new RobotVersion(3, 0);

    @Test
    public void test_tags_retrival_twoDefaultTags() {
        // prepare
        final RobotFileOutput rfo = new RobotFileOutput(ROBOT_VERSION);
        final RobotFile robotFile = new RobotFile(rfo);
        robotFile.includeSettingTableSection();
        final SettingTable settingTable = robotFile.getSettingTable();

        final RobotToken tagDecOne = new RobotToken();
        final DefaultTags defaultOne = new DefaultTags(tagDecOne);
        defaultOne.addTag("tag1");
        defaultOne.addTag("tag2");

        final RobotToken tagDecTwo = new RobotToken();
        final DefaultTags defaultTwo = new DefaultTags(tagDecTwo);
        defaultTwo.addTag("tag1a");
        defaultTwo.addTag("tag2a");

        settingTable.addDefaultTags(defaultOne);
        settingTable.addDefaultTags(defaultTwo);

        // execute
        final List<DefaultTags> defaultTags = settingTable.getDefaultTags();

        // verify
        assertThat(defaultTags).hasSize(2);
        final DefaultTags tag1 = defaultTags.get(0);
        final DefaultTags tag2 = defaultTags.get(1);
        assertThat(tag1.getTags()).hasSize(2);
        assertThat(tag2.getTags()).hasSize(2);
        assertThat(getText(tag1.getTags())).containsExactly("tag1", "tag2");
        assertThat(getText(tag2.getTags())).containsExactly("tag1a", "tag2a");
        assertThat(settingTable.getDefaultTags()).hasSize(2);
    }

    @Test
    public void test_tags_retrival_afterTagAddition_twoDefaultTags() {
        // prepare
        final RobotFileOutput rfo = new RobotFileOutput(ROBOT_VERSION);
        final RobotFile robotFile = new RobotFile(rfo);
        robotFile.includeSettingTableSection();
        final SettingTable settingTable = robotFile.getSettingTable();

        final RobotToken tagDecOne = new RobotToken();
        final DefaultTags defaultOne = new DefaultTags(tagDecOne);
        defaultOne.addTag("tag1");
        defaultOne.addTag("tag2");

        final RobotToken tagDecTwo = new RobotToken();
        final DefaultTags defaultTwo = new DefaultTags(tagDecTwo);
        defaultTwo.addTag("tag1a");
        defaultTwo.addTag("tag2a");

        settingTable.addDefaultTags(defaultOne);
        settingTable.addDefaultTags(defaultTwo);

        // execute
        final List<DefaultTags> defaultTags = settingTable.getDefaultTags();
        defaultTags.get(1).addTag("tag3");

        // verify
        assertThat(defaultTags).hasSize(2);
        final DefaultTags tag1 = defaultTags.get(0);
        final DefaultTags tag2 = defaultTags.get(1);
        assertThat(tag1.getTags()).hasSize(2);
        assertThat(tag2.getTags()).hasSize(3);
        assertThat(getText(tag1.getTags())).containsExactly("tag1", "tag2");
        assertThat(getText(tag2.getTags())).containsExactly("tag1a", "tag2a", "tag3");
        assertThat(settingTable.getDefaultTags()).hasSize(2);
        assertThat(settingTable.getDefaultTags().get(0)).isSameAs(tag1);
        assertThat(settingTable.getDefaultTags().get(1)).isSameAs(tag2);
    }

    @Test
    public void test_tags_retrival_afterModification_twoDefaultTags() {
        // prepare
        final RobotFileOutput rfo = new RobotFileOutput(ROBOT_VERSION);
        final RobotFile robotFile = new RobotFile(rfo);
        robotFile.includeSettingTableSection();
        final SettingTable settingTable = robotFile.getSettingTable();

        final RobotToken tagDecOne = new RobotToken();
        final DefaultTags defaultOne = new DefaultTags(tagDecOne);
        defaultOne.addTag("tag1");
        defaultOne.addTag("tag2");

        final RobotToken tagDecTwo = new RobotToken();
        final DefaultTags defaultTwo = new DefaultTags(tagDecTwo);
        defaultTwo.addTag("tag1a");
        defaultTwo.addTag("tag2a");

        settingTable.addDefaultTags(defaultOne);
        settingTable.addDefaultTags(defaultTwo);

        // execute
        final List<DefaultTags> defaultTags = settingTable.getDefaultTags();
        defaultTwo.getTags().get(0).setText("mod");

        // verify
        assertThat(defaultTags).hasSize(2);
        final DefaultTags tag1 = defaultTags.get(0);
        final DefaultTags tag2 = defaultTags.get(1);
        assertThat(tag1.getTags()).hasSize(2);
        assertThat(tag2.getTags()).hasSize(2);
        assertThat(getText(tag1.getTags())).containsExactly("tag1", "tag2");
        assertThat(getText(tag2.getTags())).containsExactly("mod", "tag2a");
        assertThat(settingTable.getDefaultTags()).hasSize(2);
    }
}
