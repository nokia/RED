/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.testcases;

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
public class TestCaseRemoveSettingsTest {

    private TestCase testCase;

    @Before
    public void setUp() {
        final RobotFileOutput out = new RobotFileOutput(RobotVersion.from("2.9.0"));
        out.getFileModel().includeTestCaseTableSection();
        testCase = new TestCase(new RobotToken());
        out.getFileModel().getTestCaseTable().addTest(testCase);
        addOthersToDoNotRemove(testCase);
    }

    @Test
    public void forDocumentation_whichExists() {
        // prepare
        final List<AModelElement<TestCase>> settingsWhichShouldRemain = new ArrayList<>(testCase.getUnitSettings());
        final TestDocumentation doc = testCase.newDocumentation();

        // execute
        final boolean state = testCase.removeUnitSettings(doc);

        // verify
        assertThat(state).isTrue();
        assertThat(testCase.getDocumentation()).doesNotContain(doc);
        assertThat(testCase.getUnitSettings()).containsOnlyElementsOf(settingsWhichShouldRemain);
    }

    @Test
    public void forDocumentation_whichNotExists() {
        // prepare
        final List<AModelElement<TestCase>> settingsWhichShouldRemain = new ArrayList<>(testCase.getUnitSettings());
        final TestDocumentation doc = new TestDocumentation(new RobotToken());

        // execute
        final boolean state = testCase.removeUnitSettings(doc);

        // verify
        assertThat(state).isFalse();
        assertThat(testCase.getDocumentation()).doesNotContain(doc);
        assertThat(testCase.getUnitSettings()).containsOnlyElementsOf(settingsWhichShouldRemain);
    }

    @Test
    public void forTags_whichExists() {
        // prepare
        final List<AModelElement<TestCase>> settingsWhichShouldRemain = new ArrayList<>(testCase.getUnitSettings());
        final TestCaseTags tags = testCase.newTags();

        // execute
        final boolean state = testCase.removeUnitSettings(tags);

        // verify
        assertThat(state).isTrue();
        assertThat(testCase.getTags()).doesNotContain(tags);
        assertThat(testCase.getUnitSettings()).containsOnlyElementsOf(settingsWhichShouldRemain);
    }

    @Test
    public void forTags_whichNotExists() {
        // prepare
        final List<AModelElement<TestCase>> settingsWhichShouldRemain = new ArrayList<>(testCase.getUnitSettings());
        final TestCaseTags tags = new TestCaseTags(new RobotToken());

        // execute
        final boolean state = testCase.removeUnitSettings(tags);

        // verify
        assertThat(state).isFalse();
        assertThat(testCase.getTags()).doesNotContain(tags);
        assertThat(testCase.getUnitSettings()).containsOnlyElementsOf(settingsWhichShouldRemain);
    }

    @Test
    public void forSetups_whichExists() {
        // prepare
        final List<AModelElement<TestCase>> settingsWhichShouldRemain = new ArrayList<>(testCase.getUnitSettings());
        final TestCaseSetup setups = testCase.newSetup();

        // execute
        final boolean state = testCase.removeUnitSettings(setups);

        // verify
        assertThat(state).isTrue();
        assertThat(testCase.getSetups()).doesNotContain(setups);
        assertThat(testCase.getUnitSettings()).containsOnlyElementsOf(settingsWhichShouldRemain);
    }

    @Test
    public void forSetups_whichNotExists() {
        // prepare
        final List<AModelElement<TestCase>> settingsWhichShouldRemain = new ArrayList<>(testCase.getUnitSettings());
        final TestCaseSetup setups = new TestCaseSetup(new RobotToken());

        // execute
        final boolean state = testCase.removeUnitSettings(setups);

        // verify
        assertThat(state).isFalse();
        assertThat(testCase.getSetups()).doesNotContain(setups);
        assertThat(testCase.getUnitSettings()).containsOnlyElementsOf(settingsWhichShouldRemain);
    }

    @Test
    public void forTeardowns_whichExists() {
        // prepare
        final List<AModelElement<TestCase>> settingsWhichShouldRemain = new ArrayList<>(testCase.getUnitSettings());
        final TestCaseTeardown teardowns = testCase.newTeardown();

        // execute
        final boolean state = testCase.removeUnitSettings(teardowns);

        // verify
        assertThat(state).isTrue();
        assertThat(testCase.getTeardowns()).doesNotContain(teardowns);
        assertThat(testCase.getUnitSettings()).containsOnlyElementsOf(settingsWhichShouldRemain);
    }

    @Test
    public void forTeardowns_whichNotExists() {
        // prepare
        final List<AModelElement<TestCase>> settingsWhichShouldRemain = new ArrayList<>(testCase.getUnitSettings());
        final TestCaseTeardown teardowns = new TestCaseTeardown(new RobotToken());

        // execute
        final boolean state = testCase.removeUnitSettings(teardowns);

        // verify
        assertThat(state).isFalse();
        assertThat(testCase.getTeardowns()).doesNotContain(teardowns);
        assertThat(testCase.getUnitSettings()).containsOnlyElementsOf(settingsWhichShouldRemain);
    }

    @Test
    public void forTemplates_whichExists() {
        // prepare
        final List<AModelElement<TestCase>> settingsWhichShouldRemain = new ArrayList<>(testCase.getUnitSettings());
        final TestCaseTemplate templates = testCase.newTemplate();

        // execute
        final boolean state = testCase.removeUnitSettings(templates);

        // verify
        assertThat(state).isTrue();
        assertThat(testCase.getTemplates()).doesNotContain(templates);
        assertThat(testCase.getUnitSettings()).containsOnlyElementsOf(settingsWhichShouldRemain);
    }

    @Test
    public void forTemplates_whichNotExists() {
        // prepare
        final List<AModelElement<TestCase>> settingsWhichShouldRemain = new ArrayList<>(testCase.getUnitSettings());
        final TestCaseTemplate templates = new TestCaseTemplate(new RobotToken());

        // execute
        final boolean state = testCase.removeUnitSettings(templates);

        // verify
        assertThat(state).isFalse();
        assertThat(testCase.getTemplates()).doesNotContain(templates);
        assertThat(testCase.getUnitSettings()).containsOnlyElementsOf(settingsWhichShouldRemain);
    }

    @Test
    public void forTimeouts_whichExists() {
        // prepare
        final List<AModelElement<TestCase>> settingsWhichShouldRemain = new ArrayList<>(testCase.getUnitSettings());
        final TestCaseTimeout timeouts = testCase.newTimeout();

        // execute
        final boolean state = testCase.removeUnitSettings(timeouts);

        // verify
        assertThat(state).isTrue();
        assertThat(testCase.getTimeouts()).doesNotContain(timeouts);
        assertThat(testCase.getUnitSettings()).containsOnlyElementsOf(settingsWhichShouldRemain);
    }

    @Test
    public void forTimeouts_whichNotExists() {
        // prepare
        final List<AModelElement<TestCase>> settingsWhichShouldRemain = new ArrayList<>(testCase.getUnitSettings());
        final TestCaseTimeout timeouts = new TestCaseTimeout(new RobotToken());

        // execute
        final boolean state = testCase.removeUnitSettings(timeouts);

        // verify
        assertThat(state).isFalse();
        assertThat(testCase.getTimeouts()).doesNotContain(timeouts);
        assertThat(testCase.getUnitSettings()).containsOnlyElementsOf(settingsWhichShouldRemain);
    }

    @Test
    public void forUnknownSetting_whichExists() {
        // prepare
        final List<AModelElement<TestCase>> settingsWhichShouldRemain = new ArrayList<>(testCase.getUnitSettings());
        final TestCaseUnknownSettings unknownSetting = new TestCaseUnknownSettings(new RobotToken());
        testCase.addUnknownSettings(unknownSetting);

        // execute
        final boolean state = testCase.removeUnitSettings(unknownSetting);

        // verify
        assertThat(state).isTrue();
        assertThat(testCase.getUnknownSettings()).doesNotContain(unknownSetting);
        assertThat(testCase.getUnitSettings()).containsOnlyElementsOf(settingsWhichShouldRemain);
    }

    @Test
    public void forUnknownSetting_whichNotExists() {
        // prepare
        final List<AModelElement<TestCase>> settingsWhichShouldRemain = new ArrayList<>(testCase.getUnitSettings());
        final TestCaseUnknownSettings unknownSetting = new TestCaseUnknownSettings(new RobotToken());

        // execute
        final boolean state = testCase.removeUnitSettings(unknownSetting);

        // verify
        assertThat(state).isFalse();
        assertThat(testCase.getUnknownSettings()).doesNotContain(unknownSetting);
        assertThat(testCase.getUnitSettings()).containsOnlyElementsOf(settingsWhichShouldRemain);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void forExecutableRow_shouldReturnFalse() {
        // prepare
        final RobotExecutableRow<TestCase> executionRow = new RobotExecutableRow<>();
        testCase.addTestExecutionRow(executionRow);
        final List<AModelElement<TestCase>> settingsWhichShouldRemain = new ArrayList<>(testCase.getUnitSettings());

        // execute
        final boolean state = testCase.removeUnitSettings(executionRow);

        // verify
        assertThat(state).isFalse();
        assertThat(testCase.getExecutionContext()).contains(executionRow);
        assertThat(testCase.getUnitSettings()).containsOnlyElementsOf(settingsWhichShouldRemain);
    }

    private void addOthersToDoNotRemove(final TestCase testCase) {
        testCase.newDocumentation();
        testCase.newTags();
        testCase.newSetup();
        testCase.newTeardown();
        testCase.newTemplate();
        testCase.newTimeout();
        testCase.addUnknownSettings(new TestCaseUnknownSettings(new RobotToken()));
    }
}
