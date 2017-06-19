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
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;

/**
 * @author wypych
 */
public class UserKeywordRemoveElementsTest {

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
        final List<AModelElement<UserKeyword>> elementsWhichShouldRemain = new ArrayList<>(keyword.getElements());
        final KeywordDocumentation doc = keyword.newDocumentation(0);

        // execute
        final boolean state = keyword.removeElement(doc);

        // verify
        assertThat(state).isTrue();
        assertThat(keyword.getDocumentation()).doesNotContain(doc);
        assertThat(keyword.getElements()).containsOnlyElementsOf(elementsWhichShouldRemain);
    }

    @Test
    public void forDocumentation_whichNotExists() {
        // prepare
        final List<AModelElement<UserKeyword>> elementsWhichShouldRemain = new ArrayList<>(keyword.getElements());
        final KeywordDocumentation doc = new KeywordDocumentation(new RobotToken());

        // execute
        final boolean state = keyword.removeElement(doc);

        // verify
        assertThat(state).isFalse();
        assertThat(keyword.getDocumentation()).doesNotContain(doc);
        assertThat(keyword.getElements()).containsOnlyElementsOf(elementsWhichShouldRemain);
    }

    @Test
    public void forTags_whichExists() {
        // prepare
        final List<AModelElement<UserKeyword>> elementsWhichShouldRemain = new ArrayList<>(keyword.getElements());
        final KeywordTags tags = keyword.newTags(0);

        // execute
        final boolean state = keyword.removeElement(tags);

        // verify
        assertThat(state).isTrue();
        assertThat(keyword.getTags()).doesNotContain(tags);
        assertThat(keyword.getElements()).containsOnlyElementsOf(elementsWhichShouldRemain);
    }

    @Test
    public void forTags_whichNotExists() {
        // prepare
        final List<AModelElement<UserKeyword>> elementsWhichShouldRemain = new ArrayList<>(keyword.getElements());
        final KeywordTags tags = new KeywordTags(new RobotToken());

        // execute
        final boolean state = keyword.removeElement(tags);

        // verify
        assertThat(state).isFalse();
        assertThat(keyword.getTags()).doesNotContain(tags);
        assertThat(keyword.getElements()).containsOnlyElementsOf(elementsWhichShouldRemain);
    }

    @Test
    public void forArguments_whichExists() {
        // prepare
        final List<AModelElement<UserKeyword>> elementsWhichShouldRemain = new ArrayList<>(keyword.getElements());
        final KeywordArguments args = keyword.newArguments(0);

        // execute
        final boolean state = keyword.removeElement(args);

        // verify
        assertThat(state).isTrue();
        assertThat(keyword.getArguments()).doesNotContain(args);
        assertThat(keyword.getElements()).containsOnlyElementsOf(elementsWhichShouldRemain);
    }

    @Test
    public void forArguments_whichNotExists() {
        // prepare
        final List<AModelElement<UserKeyword>> elementsWhichShouldRemain = new ArrayList<>(keyword.getElements());
        final KeywordArguments args = new KeywordArguments(new RobotToken());

        // execute
        final boolean state = keyword.removeElement(args);

        // verify
        assertThat(state).isFalse();
        assertThat(keyword.getArguments()).doesNotContain(args);
        assertThat(keyword.getElements()).containsOnlyElementsOf(elementsWhichShouldRemain);
    }

    @Test
    public void forReturns_whichExists() {
        // prepare
        final List<AModelElement<UserKeyword>> elementsWhichShouldRemain = new ArrayList<>(keyword.getElements());
        final KeywordReturn returns = keyword.newReturn(0);

        // execute
        final boolean state = keyword.removeElement(returns);

        // verify
        assertThat(state).isTrue();
        assertThat(keyword.getReturns()).doesNotContain(returns);
        assertThat(keyword.getElements()).containsOnlyElementsOf(elementsWhichShouldRemain);
    }

    @Test
    public void forReturns_whichNotExists() {
        // prepare
        final List<AModelElement<UserKeyword>> elementsWhichShouldRemain = new ArrayList<>(keyword.getElements());
        final KeywordReturn returns = new KeywordReturn(new RobotToken());

        // execute
        final boolean state = keyword.removeElement(returns);

        // verify
        assertThat(state).isFalse();
        assertThat(keyword.getReturns()).doesNotContain(returns);
        assertThat(keyword.getElements()).containsOnlyElementsOf(elementsWhichShouldRemain);
    }

    @Test
    public void forTeardowns_whichExists() {
        // prepare
        final List<AModelElement<UserKeyword>> elementsWhichShouldRemain = new ArrayList<>(keyword.getElements());
        final KeywordTeardown teardowns = keyword.newTeardown(0);

        // execute
        final boolean state = keyword.removeElement(teardowns);

        // verify
        assertThat(state).isTrue();
        assertThat(keyword.getTeardowns()).doesNotContain(teardowns);
        assertThat(keyword.getElements()).containsOnlyElementsOf(elementsWhichShouldRemain);
    }

    @Test
    public void forTeardowns_whichNotExists() {
        // prepare
        final List<AModelElement<UserKeyword>> elementsWhichShouldRemain = new ArrayList<>(keyword.getElements());
        final KeywordTeardown teardowns = new KeywordTeardown(new RobotToken());

        // execute
        final boolean state = keyword.removeElement(teardowns);

        // verify
        assertThat(state).isFalse();
        assertThat(keyword.getTeardowns()).doesNotContain(teardowns);
        assertThat(keyword.getElements()).containsOnlyElementsOf(elementsWhichShouldRemain);
    }

    @Test
    public void forTimeouts_whichExists() {
        // prepare
        final List<AModelElement<UserKeyword>> elementsWhichShouldRemain = new ArrayList<>(keyword.getElements());
        final KeywordTimeout timeouts = keyword.newTimeout(0);

        // execute
        final boolean state = keyword.removeElement(timeouts);

        // verify
        assertThat(state).isTrue();
        assertThat(keyword.getTimeouts()).doesNotContain(timeouts);
        assertThat(keyword.getElements()).containsOnlyElementsOf(elementsWhichShouldRemain);
    }

    @Test
    public void forTimeouts_whichNotExists() {
        // prepare
        final List<AModelElement<UserKeyword>> elementsWhichShouldRemain = new ArrayList<>(keyword.getElements());
        final KeywordTimeout timeouts = new KeywordTimeout(new RobotToken());

        // execute
        final boolean state = keyword.removeElement(timeouts);

        // verify
        assertThat(state).isFalse();
        assertThat(keyword.getTimeouts()).doesNotContain(timeouts);
        assertThat(keyword.getElements()).containsOnlyElementsOf(elementsWhichShouldRemain);
    }

    @Test
    public void forUnknownSettings_whichExists() {
        // prepare
        final List<AModelElement<UserKeyword>> elementsWhichShouldRemain = new ArrayList<>(keyword.getElements());
        final KeywordUnknownSettings unknownSetting = new KeywordUnknownSettings(new RobotToken());
        keyword.addElement(unknownSetting);

        // execute
        final boolean state = keyword.removeElement(unknownSetting);

        // verify
        assertThat(state).isTrue();
        assertThat(keyword.getUnknownSettings()).doesNotContain(unknownSetting);
        assertThat(keyword.getElements()).containsOnlyElementsOf(elementsWhichShouldRemain);
    }

    @Test
    public void forUnknownSettings_whichNotExists() {
        // prepare
        final List<AModelElement<UserKeyword>> elementsWhichShouldRemain = new ArrayList<>(keyword.getElements());
        final KeywordUnknownSettings unknownSetting = new KeywordUnknownSettings(new RobotToken());

        // execute
        final boolean state = keyword.removeElement(unknownSetting);

        // verify
        assertThat(state).isFalse();
        assertThat(keyword.getUnknownSettings()).doesNotContain(unknownSetting);
        assertThat(keyword.getElements()).containsOnlyElementsOf(elementsWhichShouldRemain);
    }

    @Test
    public void forExecutableRow_whichExists() {
        // prepare
        final List<AModelElement<UserKeyword>> elementsWhichShouldRemain = new ArrayList<>(keyword.getElements());
        final RobotExecutableRow<UserKeyword> executionRow = new RobotExecutableRow<UserKeyword>();
        keyword.addElement(executionRow);

        // execute
        final boolean state = keyword.removeElement(executionRow);

        // verify
        assertThat(state).isTrue();
        assertThat(keyword.getExecutionContext()).doesNotContain(executionRow);
        assertThat(keyword.getElements()).containsOnlyElementsOf(elementsWhichShouldRemain);
    }

    @Test
    public void forExecutableRow_whichNotExists() {
        // prepare
        final List<AModelElement<UserKeyword>> elementsWhichShouldRemain = new ArrayList<>(keyword.getElements());
        final RobotExecutableRow<UserKeyword> executionRow = new RobotExecutableRow<UserKeyword>();

        // execute
        final boolean state = keyword.removeElement(executionRow);

        // verify
        assertThat(state).isFalse();
        assertThat(keyword.getExecutionContext()).doesNotContain(executionRow);
        assertThat(keyword.getElements()).containsOnlyElementsOf(elementsWhichShouldRemain);
    }

    private void addOthersToDoNotRemove(final UserKeyword uk) {
        uk.newDocumentation(0);
        uk.newTags(1);
        uk.newArguments(2);
        uk.newReturn(3);
        uk.newTeardown(4);
        uk.newTimeout(5);
        uk.addElement(new KeywordUnknownSettings(new RobotToken()));
    }
}
