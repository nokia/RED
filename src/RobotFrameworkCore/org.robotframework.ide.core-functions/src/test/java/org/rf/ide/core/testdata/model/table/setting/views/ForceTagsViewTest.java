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
import org.rf.ide.core.testdata.model.table.setting.ForceTags;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;

public class ForceTagsViewTest {

    @Test
    public void test_tags_retrival_twoForceTags() {
        // prepare
        final RobotFileOutput rfo = new RobotFileOutput(new RobotVersion(2, 9));
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
        final List<ForceTags> defaultTags = settingTable.getForceTagsViews();

        // verify
        assertThat(defaultTags).hasSize(1);
        final ForceTags joined = defaultTags.get(0);
        assertThat(joined.getTags()).hasSize(4);
        assertThat(getText(joined.getTags())).containsExactly("tag1", "tag2", "tag1a", "tag2a");
        assertThat(settingTable.getForceTags()).hasSize(2);
    }

    @Test
    public void test_tags_retrival_afterTagRemove_singleForceTags() {
        // prepare
        final RobotFileOutput rfo = new RobotFileOutput(new RobotVersion(2, 9));
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
        final List<ForceTags> defaultTags = settingTable.getForceTagsViews();
        defaultTags.get(0).addTag("tag3");

        // verify
        assertThat(defaultTags).hasSize(1);
        final ForceTags joined = defaultTags.get(0);
        assertThat(joined.getTags()).hasSize(5);
        assertThat(getText(joined.getTags())).containsExactly("tag1", "tag2", "tag1a", "tag2a", "tag3");
        assertThat(settingTable.getForceTags()).hasSize(1);
        assertThat(settingTable.getForceTags().get(0)).isSameAs(joined);
    }

    @Test
    public void test_tags_retrival_afterModification_twoForceTags() {
        // prepare
        final RobotFileOutput rfo = new RobotFileOutput(new RobotVersion(2, 9));
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
        final List<ForceTags> defaultTags = settingTable.getForceTagsViews();
        defaultTwo.getTags().get(0).setText("mod");

        // verify
        assertThat(defaultTags).hasSize(1);
        final ForceTags joined = defaultTags.get(0);
        assertThat(joined.getTags()).hasSize(4);
        assertThat(getText(joined.getTags())).containsExactly("tag1", "tag2", "mod", "tag2a");
        assertThat(settingTable.getForceTags()).hasSize(2);
    }
}
