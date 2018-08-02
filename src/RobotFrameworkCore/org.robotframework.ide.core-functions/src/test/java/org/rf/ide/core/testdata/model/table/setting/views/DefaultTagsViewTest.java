/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.setting.views;

import static org.assertj.core.api.Assertions.assertThat;
import static org.rf.ide.core.testdata.model.table.setting.views.ModelTokenTestHelper.getText;

import java.util.List;

import org.junit.Test;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.RobotFileOutput;
import org.rf.ide.core.testdata.model.RobotVersion;
import org.rf.ide.core.testdata.model.table.SettingTable;
import org.rf.ide.core.testdata.model.table.setting.DefaultTags;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;

public class DefaultTagsViewTest {

    @Test
    public void test_tags_retrival_twoDefaultTags() {
        // prepare
        final RobotFileOutput rfo = new RobotFileOutput(new RobotVersion(2, 9));
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
        final List<DefaultTags> defaultTags = settingTable.getDefaultTagsViews();

        // verify
        assertThat(defaultTags).hasSize(1);
        final DefaultTags joined = defaultTags.get(0);
        assertThat(joined.getTags()).hasSize(4);
        assertThat(getText(joined.getTags())).containsExactly("tag1", "tag2", "tag1a", "tag2a");
        assertThat(settingTable.getDefaultTags()).hasSize(2);
    }

    @Test
    public void test_tags_retrival_afterTagRemove_singleDefaultTags() {
        // prepare
        final RobotFileOutput rfo = new RobotFileOutput(new RobotVersion(2, 9));
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
        final List<DefaultTags> defaultTags = settingTable.getDefaultTagsViews();
        defaultTags.get(0).addTag("tag3");

        // verify
        assertThat(defaultTags).hasSize(1);
        final DefaultTags joined = defaultTags.get(0);
        assertThat(joined.getTags()).hasSize(5);
        assertThat(getText(joined.getTags())).containsExactly("tag1", "tag2", "tag1a", "tag2a", "tag3");
        assertThat(settingTable.getDefaultTags()).hasSize(1);
        assertThat(settingTable.getDefaultTags().get(0)).isSameAs(joined);
    }

    @Test
    public void test_tags_retrival_afterModification_twoDefaultTags() {
        // prepare
        final RobotFileOutput rfo = new RobotFileOutput(new RobotVersion(2, 9));
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
        final List<DefaultTags> defaultTags = settingTable.getDefaultTagsViews();
        defaultTwo.getTags().get(0).setText("mod");

        // verify
        assertThat(defaultTags).hasSize(1);
        final DefaultTags joined = defaultTags.get(0);
        assertThat(joined.getTags()).hasSize(4);
        assertThat(getText(joined.getTags())).containsExactly("tag1", "tag2", "mod", "tag2a");
        assertThat(settingTable.getDefaultTags()).hasSize(2);
    }
}
