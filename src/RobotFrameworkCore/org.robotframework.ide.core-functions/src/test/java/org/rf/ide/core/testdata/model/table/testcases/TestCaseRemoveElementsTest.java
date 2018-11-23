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
import org.rf.ide.core.environment.RobotVersion;
import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.RobotFileOutput;
import org.rf.ide.core.testdata.model.table.LocalSetting;
import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;

/**
 * @author wypych
 */
public class TestCaseRemoveElementsTest {

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
        final List<AModelElement<TestCase>> elementsWhichShouldRemain = new ArrayList<>(testCase.getElements());
        final LocalSetting<TestCase> doc = testCase.newDocumentation(0);

        // execute
        final boolean state = testCase.removeElement(doc);

        // verify
        assertThat(state).isTrue();
        assertThat(testCase.getDocumentation()).doesNotContain(doc);
        assertThat(testCase.getElements()).containsOnlyElementsOf(elementsWhichShouldRemain);
    }

    @Test
    public void forDocumentation_whichNotExists() {
        // prepare
        final List<AModelElement<TestCase>> elementsWhichShouldRemain = new ArrayList<>(testCase.getElements());
        final LocalSetting<TestCase> doc = new LocalSetting<>(ModelType.TEST_CASE_DOCUMENTATION, new RobotToken());

        // execute
        final boolean state = testCase.removeElement(doc);

        // verify
        assertThat(state).isFalse();
        assertThat(testCase.getDocumentation()).doesNotContain(doc);
        assertThat(testCase.getElements()).containsOnlyElementsOf(elementsWhichShouldRemain);
    }

    @Test
    public void forTags_whichExists() {
        // prepare
        final List<AModelElement<TestCase>> elementsWhichShouldRemain = new ArrayList<>(testCase.getElements());
        final LocalSetting<TestCase> tags = testCase.newTags(0);

        // execute
        final boolean state = testCase.removeElement(tags);

        // verify
        assertThat(state).isTrue();
        assertThat(testCase.getTags()).doesNotContain(tags);
        assertThat(testCase.getElements()).containsOnlyElementsOf(elementsWhichShouldRemain);
    }

    @Test
    public void forTags_whichNotExists() {
        // prepare
        final List<AModelElement<TestCase>> elementsWhichShouldRemain = new ArrayList<>(testCase.getElements());
        final LocalSetting<TestCase> tags = new LocalSetting<>(ModelType.TEST_CASE_TAGS, new RobotToken());

        // execute
        final boolean state = testCase.removeElement(tags);

        // verify
        assertThat(state).isFalse();
        assertThat(testCase.getTags()).doesNotContain(tags);
        assertThat(testCase.getElements()).containsOnlyElementsOf(elementsWhichShouldRemain);
    }

    @Test
    public void forSetups_whichExists() {
        // prepare
        final List<AModelElement<TestCase>> elementsWhichShouldRemain = new ArrayList<>(testCase.getElements());
        final LocalSetting<TestCase> setups = testCase.newSetup(0);

        // execute
        final boolean state = testCase.removeElement(setups);

        // verify
        assertThat(state).isTrue();
        assertThat(testCase.getSetups()).doesNotContain(setups);
        assertThat(testCase.getElements()).containsOnlyElementsOf(elementsWhichShouldRemain);
    }

    @Test
    public void forSetups_whichNotExists() {
        // prepare
        final List<AModelElement<TestCase>> elementsWhichShouldRemain = new ArrayList<>(testCase.getElements());
        final LocalSetting<TestCase> setups = new LocalSetting<>(ModelType.TEST_CASE_SETUP, new RobotToken());

        // execute
        final boolean state = testCase.removeElement(setups);

        // verify
        assertThat(state).isFalse();
        assertThat(testCase.getSetups()).doesNotContain(setups);
        assertThat(testCase.getElements()).containsOnlyElementsOf(elementsWhichShouldRemain);
    }

    @Test
    public void forTeardowns_whichExists() {
        // prepare
        final List<AModelElement<TestCase>> elementsWhichShouldRemain = new ArrayList<>(testCase.getElements());
        final LocalSetting<TestCase> teardowns = testCase.newTeardown(0);

        // execute
        final boolean state = testCase.removeElement(teardowns);

        // verify
        assertThat(state).isTrue();
        assertThat(testCase.getTeardowns()).doesNotContain(teardowns);
        assertThat(testCase.getElements()).containsOnlyElementsOf(elementsWhichShouldRemain);
    }

    @Test
    public void forTeardowns_whichNotExists() {
        // prepare
        final List<AModelElement<TestCase>> elementsWhichShouldRemain = new ArrayList<>(testCase.getElements());
        final LocalSetting<TestCase> teardowns = new LocalSetting<>(ModelType.TEST_CASE_TEARDOWN, new RobotToken());

        // execute
        final boolean state = testCase.removeElement(teardowns);

        // verify
        assertThat(state).isFalse();
        assertThat(testCase.getTeardowns()).doesNotContain(teardowns);
        assertThat(testCase.getElements()).containsOnlyElementsOf(elementsWhichShouldRemain);
    }

    @Test
    public void forTemplates_whichExists() {
        // prepare
        final List<AModelElement<TestCase>> elementsWhichShouldRemain = new ArrayList<>(testCase.getElements());
        final LocalSetting<TestCase> templates = testCase.newTemplate(0);

        // execute
        final boolean state = testCase.removeElement(templates);

        // verify
        assertThat(state).isTrue();
        assertThat(testCase.getTemplates()).doesNotContain(templates);
        assertThat(testCase.getElements()).containsOnlyElementsOf(elementsWhichShouldRemain);
    }

    @Test
    public void forTemplates_whichNotExists() {
        // prepare
        final List<AModelElement<TestCase>> elementsWhichShouldRemain = new ArrayList<>(testCase.getElements());
        final LocalSetting<TestCase> templates = new LocalSetting<>(ModelType.TEST_CASE_TEMPLATE,
                new RobotToken());

        // execute
        final boolean state = testCase.removeElement(templates);

        // verify
        assertThat(state).isFalse();
        assertThat(testCase.getTemplates()).doesNotContain(templates);
        assertThat(testCase.getElements()).containsOnlyElementsOf(elementsWhichShouldRemain);
    }

    @Test
    public void forTimeouts_whichExists() {
        // prepare
        final List<AModelElement<TestCase>> elementsWhichShouldRemain = new ArrayList<>(testCase.getElements());
        final LocalSetting<TestCase> timeouts = testCase.newTimeout(0);

        // execute
        final boolean state = testCase.removeElement(timeouts);

        // verify
        assertThat(state).isTrue();
        assertThat(testCase.getTimeouts()).doesNotContain(timeouts);
        assertThat(testCase.getElements()).containsOnlyElementsOf(elementsWhichShouldRemain);
    }

    @Test
    public void forTimeouts_whichNotExists() {
        // prepare
        final List<AModelElement<TestCase>> elementsWhichShouldRemain = new ArrayList<>(testCase.getElements());
        final LocalSetting<TestCase> timeouts = new LocalSetting<>(ModelType.TEST_CASE_TIMEOUT, new RobotToken());

        // execute
        final boolean state = testCase.removeElement(timeouts);

        // verify
        assertThat(state).isFalse();
        assertThat(testCase.getTimeouts()).doesNotContain(timeouts);
        assertThat(testCase.getElements()).containsOnlyElementsOf(elementsWhichShouldRemain);
    }

    @Test
    public void forUnknownSetting_whichExists() {
        // prepare
        final List<AModelElement<TestCase>> elementsWhichShouldRemain = new ArrayList<>(testCase.getElements());
        final LocalSetting<TestCase> unknownSetting = new LocalSetting<>(ModelType.TEST_CASE_SETTING_UNKNOWN,
                RobotToken.create(""));
        testCase.addElement(unknownSetting);

        // execute
        final boolean state = testCase.removeElement(unknownSetting);

        // verify
        assertThat(state).isTrue();
        assertThat(testCase.getUnknownSettings()).doesNotContain(unknownSetting);
        assertThat(testCase.getElements()).containsOnlyElementsOf(elementsWhichShouldRemain);
    }

    @Test
    public void forUnknownSetting_whichNotExists() {
        // prepare
        final List<AModelElement<TestCase>> elementsWhichShouldRemain = new ArrayList<>(testCase.getElements());
        final LocalSetting<TestCase> unknownSetting = new LocalSetting<>(ModelType.TEST_CASE_SETTING_UNKNOWN,
                RobotToken.create(""));

        // execute
        final boolean state = testCase.removeElement(unknownSetting);

        // verify
        assertThat(state).isFalse();
        assertThat(testCase.getUnknownSettings()).doesNotContain(unknownSetting);
        assertThat(testCase.getElements()).containsOnlyElementsOf(elementsWhichShouldRemain);
    }

    @Test
    public void forExecutableRow_whichExists() {
        // prepare
        final List<AModelElement<TestCase>> elementsWhichShouldRemain = new ArrayList<>(testCase.getElements());
        final RobotExecutableRow<TestCase> executionRow = new RobotExecutableRow<>();
        testCase.addElement(executionRow);

        // execute
        final boolean state = testCase.removeElement(executionRow);

        // verify
        assertThat(state).isTrue();
        assertThat(testCase.getExecutionContext()).doesNotContain(executionRow);
        assertThat(testCase.getElements()).containsOnlyElementsOf(elementsWhichShouldRemain);
    }
    
    @Test
    public void forExecutableRow_whichNotExists() {
        // prepare
        final List<AModelElement<TestCase>> elementsWhichShouldRemain = new ArrayList<>(testCase.getElements());
        final RobotExecutableRow<TestCase> executionRow = new RobotExecutableRow<>();

        // execute
        final boolean state = testCase.removeElement(executionRow);

        // verify
        assertThat(state).isFalse();
        assertThat(testCase.getExecutionContext()).doesNotContain(executionRow);
        assertThat(testCase.getElements()).containsOnlyElementsOf(elementsWhichShouldRemain);
    }

    private void addOthersToDoNotRemove(final TestCase testCase) {
        testCase.newDocumentation(0);
        testCase.newTags(1);
        testCase.newSetup(2);
        testCase.newTeardown(3);
        testCase.newTemplate(4);
        testCase.newTimeout(5);
        testCase.addElement(new LocalSetting<>(ModelType.TEST_CASE_SETTING_UNKNOWN, RobotToken.create("")));
    }
}
