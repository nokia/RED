/*
* Copyright 2017 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.rf.ide.core.testdata.model.table.testcases;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public class TestCaseTemplateTest {

    private RobotToken decToken;
    private TestCaseTemplate testable;

    @Before
    public void setUp() {
        this.decToken = new RobotToken();
        this.testable = new TestCaseTemplate(decToken);
    }

    @Test
    public void test_ifCellWillBeAdded_atKeywordPosition() {
        // prepare
        final RobotToken valToken = RobotToken.create("val");
        final RobotToken argToken = RobotToken.create("arg");
        final RobotToken cmtToken = RobotToken.create("#cmt");
        testable.setKeywordName(valToken);
        testable.addUnexpectedTrashArgument(argToken);
        testable.addCommentPart(cmtToken);

        // execute
        testable.insertValueAt("value", 1);

        // verify
        final List<RobotToken> elementTokens = testable.getElementTokens();
        assertThat(elementTokens).hasSize(5);
        assertThat(elementTokens.get(0)).isSameAs(decToken);
        assertThat(elementTokens.get(0).getTypes()).contains(RobotTokenType.TEST_CASE_SETTING_TEMPLATE);
        assertThat(elementTokens.get(1).getText()).isEqualTo("value");
        assertThat(elementTokens.get(1).getTypes())
                .contains(RobotTokenType.TEST_CASE_SETTING_TEMPLATE_KEYWORD_NAME);
        assertThat(elementTokens.get(2)).isSameAs(valToken);
        assertThat(elementTokens.get(2).getTypes())
                .contains(RobotTokenType.TEST_CASE_SETTING_TEMPLATE_KEYWORD_UNWANTED_ARGUMENT);
        assertThat(elementTokens.get(3)).isSameAs(argToken);
        assertThat(elementTokens.get(3).getTypes())
                .contains(RobotTokenType.TEST_CASE_SETTING_TEMPLATE_KEYWORD_UNWANTED_ARGUMENT);
        assertThat(elementTokens.get(4)).isSameAs(cmtToken);
        assertThat(elementTokens.get(4).getTypes()).contains(RobotTokenType.START_HASH_COMMENT);
    }

    @Test
    public void test_ifCellWillBeAdded_atArgumentPosition() {
        // prepare
        final RobotToken argToken = RobotToken.create("arg");
        final RobotToken cmtToken = RobotToken.create("#cmt");
        testable.addUnexpectedTrashArgument(argToken);
        testable.addCommentPart(cmtToken);

        // execute
        testable.insertValueAt("value", 2);

        // verify
        final List<RobotToken> elementTokens = testable.getElementTokens();
        assertThat(elementTokens).hasSize(4);
        assertThat(elementTokens.get(0)).isSameAs(decToken);
        assertThat(elementTokens.get(0).getTypes()).contains(RobotTokenType.TEST_CASE_SETTING_TEMPLATE);
        assertThat(elementTokens.get(1).getText()).isEqualTo("value");
        assertThat(elementTokens.get(1).getTypes())
                .contains(RobotTokenType.TEST_CASE_SETTING_TEMPLATE_KEYWORD_UNWANTED_ARGUMENT);
        assertThat(elementTokens.get(2)).isSameAs(argToken);
        assertThat(elementTokens.get(3)).isSameAs(cmtToken);
        assertThat(elementTokens.get(3).getTypes()).contains(RobotTokenType.START_HASH_COMMENT);
    }

    @Test
    public void test_ifCellWillBeAdded_atCommentPosition() {
        // prepare
        final RobotToken argToken = RobotToken.create("arg");
        final RobotToken cmtToken = RobotToken.create("#cmt");
        testable.addUnexpectedTrashArgument(argToken);
        testable.addCommentPart(cmtToken);

        // execute
        testable.insertValueAt("value", 3);

        // verify
        final List<RobotToken> elementTokens = testable.getElementTokens();
        assertThat(elementTokens).hasSize(4);
        assertThat(elementTokens.get(0)).isSameAs(decToken);
        assertThat(elementTokens.get(0).getTypes()).contains(RobotTokenType.TEST_CASE_SETTING_TEMPLATE);
        assertThat(elementTokens.get(1)).isSameAs(argToken);
        assertThat(elementTokens.get(1).getTypes()).contains(RobotTokenType.UNKNOWN);
        assertThat(elementTokens.get(2).getText()).isEqualTo("value");
        assertThat(elementTokens.get(2).getTypes())
                .contains(RobotTokenType.TEST_CASE_SETTING_TEMPLATE_KEYWORD_UNWANTED_ARGUMENT);
        assertThat(elementTokens.get(3)).isSameAs(cmtToken);
        assertThat(elementTokens.get(3).getTypes()).contains(RobotTokenType.START_HASH_COMMENT);
    }
}
