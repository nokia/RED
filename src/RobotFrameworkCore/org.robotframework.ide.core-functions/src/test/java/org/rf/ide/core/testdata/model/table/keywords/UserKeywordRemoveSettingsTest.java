/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.keywords;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.RobotFileOutput;
import org.rf.ide.core.testdata.model.RobotVersion;
import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.model.table.keywords.KeywordArguments;
import org.rf.ide.core.testdata.model.table.keywords.KeywordDocumentation;
import org.rf.ide.core.testdata.model.table.keywords.KeywordReturn;
import org.rf.ide.core.testdata.model.table.keywords.KeywordTags;
import org.rf.ide.core.testdata.model.table.keywords.KeywordTeardown;
import org.rf.ide.core.testdata.model.table.keywords.KeywordTimeout;
import org.rf.ide.core.testdata.model.table.keywords.KeywordUnknownSettings;
import org.rf.ide.core.testdata.model.table.keywords.UserKeyword;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;

/**
 * @author wypych
 */
public class UserKeywordRemoveSettingsTest {

    private UserKeyword keyword;

    @Before
    public void setUp() {
        final RobotFileOutput out = new RobotFileOutput(RobotVersion.from("2.9.0"));
        out.getFileModel().includeKeywordTableSection();
        keyword = new UserKeyword(new RobotToken());
        out.getFileModel().getKeywordTable().addKeyword(keyword);
        addOthersToDoNotRemove(keyword);
    }

    @Test
    public void forDocumentation_whichExists() {
        // prepare
        final List<AModelElement<UserKeyword>> settingsWhichShouldRemain = new ArrayList<>(keyword.getUnitSettings());
        final KeywordDocumentation doc = keyword.newDocumentation();

        // execute
        final boolean state = keyword.removeUnitSettings(doc);

        // verify
        assertThat(state).isTrue();
        assertThat(keyword.getDocumentation()).doesNotContain(doc);
        assertThat(keyword.getUnitSettings()).containsOnlyElementsOf(settingsWhichShouldRemain);
    }

    @Test
    public void forDocumentation_whichNotExists() {
        // prepare
        final List<AModelElement<UserKeyword>> settingsWhichShouldRemain = new ArrayList<>(keyword.getUnitSettings());
        final KeywordDocumentation doc = new KeywordDocumentation(new RobotToken());

        // execute
        final boolean state = keyword.removeUnitSettings(doc);

        // verify
        assertThat(state).isFalse();
        assertThat(keyword.getDocumentation()).doesNotContain(doc);
        assertThat(keyword.getUnitSettings()).containsOnlyElementsOf(settingsWhichShouldRemain);
    }

    @Test
    public void forTags_whichExists() {
        // prepare
        final List<AModelElement<UserKeyword>> settingsWhichShouldRemain = new ArrayList<>(keyword.getUnitSettings());
        final KeywordTags tags = keyword.newTags();

        // execute
        final boolean state = keyword.removeUnitSettings(tags);

        // verify
        assertThat(state).isTrue();
        assertThat(keyword.getTags()).doesNotContain(tags);
        assertThat(keyword.getUnitSettings()).containsOnlyElementsOf(settingsWhichShouldRemain);
    }

    @Test
    public void forTags_whichNotExists() {
        // prepare
        final List<AModelElement<UserKeyword>> settingsWhichShouldRemain = new ArrayList<>(keyword.getUnitSettings());
        final KeywordTags tags = new KeywordTags(new RobotToken());

        // execute
        final boolean state = keyword.removeUnitSettings(tags);

        // verify
        assertThat(state).isFalse();
        assertThat(keyword.getTags()).doesNotContain(tags);
        assertThat(keyword.getUnitSettings()).containsOnlyElementsOf(settingsWhichShouldRemain);
    }

    @Test
    public void forArguments_whichExists() {
        // prepare
        final List<AModelElement<UserKeyword>> settingsWhichShouldRemain = new ArrayList<>(keyword.getUnitSettings());
        final KeywordArguments args = keyword.newArguments();

        // execute
        final boolean state = keyword.removeUnitSettings(args);

        // verify
        assertThat(state).isTrue();
        assertThat(keyword.getArguments()).doesNotContain(args);
        assertThat(keyword.getUnitSettings()).containsOnlyElementsOf(settingsWhichShouldRemain);
    }

    @Test
    public void forArguments_whichNotExists() {
        // prepare
        final List<AModelElement<UserKeyword>> settingsWhichShouldRemain = new ArrayList<>(keyword.getUnitSettings());
        final KeywordArguments args = new KeywordArguments(new RobotToken());

        // execute
        final boolean state = keyword.removeUnitSettings(args);

        // verify
        assertThat(state).isFalse();
        assertThat(keyword.getArguments()).doesNotContain(args);
        assertThat(keyword.getUnitSettings()).containsOnlyElementsOf(settingsWhichShouldRemain);
    }

    @Test
    public void forReturns_whichExists() {
        // prepare
        final List<AModelElement<UserKeyword>> settingsWhichShouldRemain = new ArrayList<>(keyword.getUnitSettings());
        final KeywordReturn returns = keyword.newReturn();

        // execute
        final boolean state = keyword.removeUnitSettings(returns);

        // verify
        assertThat(state).isTrue();
        assertThat(keyword.getReturns()).doesNotContain(returns);
        assertThat(keyword.getUnitSettings()).containsOnlyElementsOf(settingsWhichShouldRemain);
    }

    @Test
    public void forReturns_whichNotExists() {
        // prepare
        final List<AModelElement<UserKeyword>> settingsWhichShouldRemain = new ArrayList<>(keyword.getUnitSettings());
        final KeywordReturn returns = new KeywordReturn(new RobotToken());

        // execute
        final boolean state = keyword.removeUnitSettings(returns);

        // verify
        assertThat(state).isFalse();
        assertThat(keyword.getReturns()).doesNotContain(returns);
        assertThat(keyword.getUnitSettings()).containsOnlyElementsOf(settingsWhichShouldRemain);
    }

    @Test
    public void forTeardowns_whichExists() {
        // prepare
        final List<AModelElement<UserKeyword>> settingsWhichShouldRemain = new ArrayList<>(keyword.getUnitSettings());
        final KeywordTeardown teardowns = keyword.newTeardown();

        // execute
        final boolean state = keyword.removeUnitSettings(teardowns);

        // verify
        assertThat(state).isTrue();
        assertThat(keyword.getTeardowns()).doesNotContain(teardowns);
        assertThat(keyword.getUnitSettings()).containsOnlyElementsOf(settingsWhichShouldRemain);
    }

    @Test
    public void forTeardowns_whichNotExists() {
        // prepare
        final List<AModelElement<UserKeyword>> settingsWhichShouldRemain = new ArrayList<>(keyword.getUnitSettings());
        final KeywordTeardown teardowns = new KeywordTeardown(new RobotToken());

        // execute
        final boolean state = keyword.removeUnitSettings(teardowns);

        // verify
        assertThat(state).isFalse();
        assertThat(keyword.getTeardowns()).doesNotContain(teardowns);
        assertThat(keyword.getUnitSettings()).containsOnlyElementsOf(settingsWhichShouldRemain);
    }

    @Test
    public void forTimeouts_whichExists() {
        // prepare
        final List<AModelElement<UserKeyword>> settingsWhichShouldRemain = new ArrayList<>(keyword.getUnitSettings());
        final KeywordTimeout timeouts = keyword.newTimeout();

        // execute
        final boolean state = keyword.removeUnitSettings(timeouts);

        // verify
        assertThat(state).isTrue();
        assertThat(keyword.getTimeouts()).doesNotContain(timeouts);
        assertThat(keyword.getUnitSettings()).containsOnlyElementsOf(settingsWhichShouldRemain);
    }

    @Test
    public void forTimeouts_whichNotExists() {
        // prepare
        final List<AModelElement<UserKeyword>> settingsWhichShouldRemain = new ArrayList<>(keyword.getUnitSettings());
        final KeywordTimeout timeouts = new KeywordTimeout(new RobotToken());

        // execute
        final boolean state = keyword.removeUnitSettings(timeouts);

        // verify
        assertThat(state).isFalse();
        assertThat(keyword.getTimeouts()).doesNotContain(timeouts);
        assertThat(keyword.getUnitSettings()).containsOnlyElementsOf(settingsWhichShouldRemain);
    }

    @Test
    public void forUnknownSettings_whichExists() {
        // prepare
        final List<AModelElement<UserKeyword>> settingsWhichShouldRemain = new ArrayList<>(keyword.getUnitSettings());
        final KeywordUnknownSettings unknownSetting = new KeywordUnknownSettings(new RobotToken());
        keyword.addUnknownSettings(unknownSetting);

        // execute
        final boolean state = keyword.removeUnitSettings(unknownSetting);

        // verify
        assertThat(state).isTrue();
        assertThat(keyword.getUnknownSettings()).doesNotContain(unknownSetting);
        assertThat(keyword.getUnitSettings()).containsOnlyElementsOf(settingsWhichShouldRemain);
    }

    @Test
    public void forUnknownSettings_whichNotExists() {
        // prepare
        final List<AModelElement<UserKeyword>> settingsWhichShouldRemain = new ArrayList<>(keyword.getUnitSettings());
        final KeywordUnknownSettings unknownSetting = new KeywordUnknownSettings(new RobotToken());

        // execute
        final boolean state = keyword.removeUnitSettings(unknownSetting);

        // verify
        assertThat(state).isFalse();
        assertThat(keyword.getUnknownSettings()).doesNotContain(unknownSetting);
        assertThat(keyword.getUnitSettings()).containsOnlyElementsOf(settingsWhichShouldRemain);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void forExecutableRow_shouldReturnFalse() {
        // prepare
        final RobotExecutableRow<UserKeyword> executionRow = new RobotExecutableRow<UserKeyword>();
        keyword.addKeywordExecutionRow(executionRow);
        final List<AModelElement<UserKeyword>> settingsWhichShouldRemain = new ArrayList<>(keyword.getUnitSettings());

        // execute
        final boolean state = keyword.removeUnitSettings(executionRow);

        // verify
        assertThat(state).isFalse();
        assertThat(keyword.getExecutionContext()).contains(executionRow);
        assertThat(keyword.getUnitSettings()).containsOnlyElementsOf(settingsWhichShouldRemain);
    }

    private void addOthersToDoNotRemove(final UserKeyword uk) {
        uk.newDocumentation();
        uk.newTags();
        uk.newArguments();
        uk.newReturn();
        uk.newTeardown();
        uk.newTimeout();
        uk.addUnknownSettings(new KeywordUnknownSettings(new RobotToken()));
    }
}
